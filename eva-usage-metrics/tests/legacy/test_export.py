import os
from datetime import date

from eva_usage_metrics.archive.storage import connect_duckdb, existing_days, register_views
from eva_usage_metrics.legacy.export import (archive_select, compare_counts, count_per_day_in_archive, get_cutoff,
                                             legacy_column_types, legacy_query, libpq_connection_string,
                                             register_endpoint_function)
from eva_usage_metrics.legacy.queries import ENDPOINT_PATH, LEGACY_EXPORTS

WS_EXPORT = LEGACY_EXPORTS['legacy_web_service']
FTP_EXPORT = LEGACY_EXPORTS['legacy_ftp_transfer']


def test_legacy_query_applies_the_cutoff():
    query = legacy_query(WS_EXPORT, ['ws_traffic', 'ws_traffic_hist'], date(2026, 11, 13))
    assert 'FROM eva_web_srvc_stats.ws_traffic UNION ALL' in query
    assert 'FROM eva_web_srvc_stats.ws_traffic_hist' in query
    assert "event_ts < '2026-11-13T00:00:00Z'::timestamp with time zone" in query
    # The name of the table is recorded in place of the Elasticsearch index
    assert "'ws_traffic_hist' AS es_index" in query
    assert 'event_ts <' not in legacy_query(FTP_EXPORT, ['ftp_traffic'], None)


def test_legacy_queries_return_the_columns_of_their_dataset():
    for export in LEGACY_EXPORTS.values():
        expected = [ENDPOINT_PATH if name == 'endpoint' else name for name in export.dataset.schema.names]
        assert list(export.columns) == expected
        assert export.live_dataset.schema == export.dataset.schema


def test_legacy_column_types_replace_the_endpoint():
    column_types = legacy_column_types(WS_EXPORT)
    assert 'endpoint' not in column_types and column_types[ENDPOINT_PATH] == 'VARCHAR'
    assert column_types['event_ts'] == 'TIMESTAMPTZ' and column_types['bytes'] == 'BIGINT'


def test_export_to_daily_partitions(tmp_path):
    """Run the DuckDB part of the export on records shaped like the result of the legacy query."""
    root = str(tmp_path)
    connection = connect_duckdb()
    register_endpoint_function(connection)
    column_types = legacy_column_types(WS_EXPORT)
    connection.execute(f"CREATE TABLE legacy ({', '.join(f'{n} {t}' for n, t in column_types.items())})")
    connection.execute(
        "INSERT INTO legacy (event_ts, es_index, client_id, path, bytes, status, endpoint_path) VALUES "
        "('2023-01-01 23:30:00+00', 'ws_traffic', '1.1.1.1', '/p', 10, '200', "
        "'/eva/webservices/rest/v1/variants/rs1'), "
        "('2023-01-02 00:30:00+00', 'ws_traffic', '1.1.1.2', '/p', 20, '200', NULL)"
    )
    target = os.path.join(root, WS_EXPORT.dataset.name)
    connection.execute(
        f"COPY ({archive_select(WS_EXPORT, 'legacy')}) TO '{target}' "
        "(FORMAT parquet, COMPRESSION zstd, PARTITION_BY (day), OVERWRITE true, FILENAME_PATTERN 'data_{i}')"
    )
    assert existing_days(root, WS_EXPORT.dataset.name) == {date(2023, 1, 1), date(2023, 1, 2)}
    counts = count_per_day_in_archive(connection, root, WS_EXPORT.dataset.name)
    assert counts == {date(2023, 1, 1): 1, date(2023, 1, 2): 1}
    assert compare_counts({date(2023, 1, 1): 1, date(2023, 1, 2): 2}, counts) == {date(2023, 1, 2): (2, 1)}

    register_views(connection, root, [WS_EXPORT.dataset.name])
    row = connection.execute(
        'SELECT endpoint, bytes FROM legacy_web_service WHERE day = DATE \'2023-01-01\''
    ).fetchone()
    assert row == ('/eva/webservices/rest/v1/variants/{variant_id}', 10)
    columns = [r[0] for r in connection.execute('DESCRIBE legacy_web_service').fetchall()]
    assert columns == WS_EXPORT.dataset.schema.names + ['day']


def test_cutoff_defaults_to_the_first_live_day(tmp_path):
    root = str(tmp_path)
    assert get_cutoff(root, FTP_EXPORT, None, False) is None
    os.makedirs(os.path.join(root, 'ftp_transfer', 'day=2026-11-13'))
    open(os.path.join(root, 'ftp_transfer', 'day=2026-11-13', 'data.parquet'), 'w').close()
    assert get_cutoff(root, FTP_EXPORT, None, False) == date(2026, 11, 13)
    assert get_cutoff(root, FTP_EXPORT, date(2026, 1, 1), False) == date(2026, 1, 1)
    assert get_cutoff(root, FTP_EXPORT, None, True) is None


def test_libpq_connection_string(monkeypatch):
    monkeypatch.setattr('eva_usage_metrics.legacy.export.get_metadata_creds_for_profile',
                        lambda profile, xml: ('jdbc:postgresql://pg.example.org:5433/evapro', 'user', 'secret'))
    assert libpq_connection_string('p', 'x.xml') == (
        "host='pg.example.org' port='5433' dbname='evapro' user='user' password='secret'"
    )
