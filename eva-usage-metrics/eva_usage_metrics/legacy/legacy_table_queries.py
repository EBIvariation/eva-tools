"""
Postgres queries reading the legacy usage tables into the columns of the archive datasets.

The web service endpoint is normalised in DuckDB from the endpoint_path column, the other columns are those of the
dataset, in the same order.
"""
from dataclasses import dataclass

from eva_usage_metrics.archive.parquet_datasets import FTP_TRANSFER, LEGACY_FTP_TRANSFER, LEGACY_WEB_SERVICE, WEB_SERVICE

LEGACY_SCHEMA = 'eva_web_srvc_stats'
ENDPOINT_PATH = 'endpoint_path'


def number_or_null(column, sql_type):
    """Cast a column to a number, ignoring placeholders such as '-' or 'Not found' left by the old loaders."""
    return f"CASE WHEN {column}::text ~ '^-?[0-9]+(\\.[0-9]+)?$' THEN {column}::text::numeric::{sql_type} END"


def placeholder_to_null(column):
    """IP2Location and geolocation-db report unknown locations as '-' or 'Not found'."""
    return f"NULLIF(NULLIF({column}::text, '-'), 'Not found')"


@dataclass(frozen=True)
class LegacyExport:
    dataset: object          # dataset written to the archive
    live_dataset: object     # dataset collected from neometer that follows it: the export stops where it starts
    default_tables: tuple
    columns: dict            # column name -> Postgres expression, where {table} is the name of the table

    def select_sql(self, table):
        expressions = ',\n       '.join(
            f"{expression.replace('{table}', repr(table))} AS {name}" for name, expression in self.columns.items()
        )
        return f'SELECT {expressions}\nFROM {LEGACY_SCHEMA}.{table}'


WEB_SERVICE_EXPORT = LegacyExport(
    dataset=LEGACY_WEB_SERVICE,
    live_dataset=WEB_SERVICE,
    default_tables=('ws_traffic', 'ws_traffic_hist'),
    columns={
        'event_ts': 'cast(event_ts_txt AS timestamp with time zone)',
        'es_index': '{table}',
        'es_id': 'NULL::text',
        'client_id': 'client_ip::text',
        'direction': 'NULL::text',
        'path': 'path::text',
        'bytes': number_or_null('bytes_out', 'bigint'),
        'duration_secs': number_or_null('duration', 'double precision'),
        'status': 'http_status::text',
        'country_code': placeholder_to_null('client_country_code'),
        'region': placeholder_to_null('client_state'),
        'city': placeholder_to_null('client_city'),
        ENDPOINT_PATH: 'request_uri_path::text',
        'query': 'request_query::text',
        'http_method': 'request_type::text',
        'user_agent': 'user_agent::text',
        'server_node': 'server_node::text',
        'latitude': number_or_null('client_latitude', 'double precision'),
        'longitude': number_or_null('client_longitude', 'double precision'),
    },
)

FTP_TRANSFER_EXPORT = LegacyExport(
    dataset=LEGACY_FTP_TRANSFER,
    live_dataset=FTP_TRANSFER,
    default_tables=('ftp_traffic',),
    columns={
        'event_ts': 'cast(event_ts_txt AS timestamp with time zone)',
        'es_index': '_index::text',
        'es_id': '_id::text',
        # The legacy host identifier cannot be compared with the neometer client hash
        'client_id': 'uhost::text',
        'direction': "CASE direction WHEN 'o' THEN 'download' WHEN 'i' THEN 'upload' END",
        'path': 'file_name::text',
        'bytes': number_or_null('file_size', 'bigint'),
        'duration_secs': number_or_null('transfer_time', 'double precision'),
        # The legacy logs did not record the outcome of the transfers
        'status': 'NULL::text',
        'country_code': placeholder_to_null('country'),
        'region': placeholder_to_null('region'),
        'city': placeholder_to_null('city'),
        'access_mode': 'access_mode::text',
        'user_name': 'NULL::text',
    },
)

LEGACY_EXPORTS = {export.dataset.name: export for export in (WEB_SERVICE_EXPORT, FTP_TRANSFER_EXPORT)}
