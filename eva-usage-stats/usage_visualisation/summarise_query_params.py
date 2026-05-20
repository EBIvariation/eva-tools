from argparse import ArgumentParser
from collections import defaultdict
from urllib.parse import parse_qs

from ebi_eva_common_pyutils.common_utils import pretty_print
from ebi_eva_internal_pyutils.metadata_utils import get_metadata_connection_handle

from summarise_endpoints import normalise_path, stream_rows, BATCH_SIZE

TARGET_ENDPOINTS = {
    '/eva/webservices/rest/v1/genes/{gene_name}/variants',
    '/eva/webservices/rest/v1/variants/{variant_id}/info',
    '/eva/webservices/rest/v1/segments/{region}/variants',
    '/eva/webservices/rest/v2/variants/{variant_id}/sources',
    '/eva/webservices/rest/v1/variants/{variant_id}',
    '/eva/webservices/rest/v1/segments/{region}',
}

def main():
    parser = ArgumentParser(description='Summarise query parameters used on specific EVA endpoints.')
    parser.add_argument('--date', required=True, help='Date to analyse (YYYY-MM-DD)')
    parser.add_argument('--config-file', default='maven_settings.xml',
                        help='Path to private config XML (default: maven_settings.xml)')
    args = parser.parse_args()

    query = f"""
        SELECT request_uri_path, request_query, http_status
        FROM eva_web_srvc_stats.ws_traffic_useful_cols
        WHERE request_ts >= '{args.date}'
    """

    # {endpoint: {param_name: request_count}}
    param_counts = defaultdict(lambda: defaultdict(int))
    endpoint_counts = defaultdict(int)
    total_rows = 0

    print(f"Querying traffic since {args.date}")
    with get_metadata_connection_handle('production_processing', args.config_file) as conn:
        for request_uri_path, request_query, http_status in stream_rows(conn, query):
            total_rows += 1
            if total_rows % BATCH_SIZE == 0:
                print(f"processed {total_rows:,} rows", end='\r')
            if not str(http_status).startswith('2'):
                continue
            endpoint = normalise_path(request_uri_path)
            if endpoint not in TARGET_ENDPOINTS:
                continue
            endpoint_counts[endpoint] += 1
            for param in parse_qs(request_query or '', keep_blank_values=True):
                param_counts[endpoint][param] += 1

    print()
    print(f"Fetched {total_rows:,} rows total.\n")
    print(f"=== Query parameter usage since {args.date} ===\n")

    for endpoint in sorted(TARGET_ENDPOINTS):
        n = endpoint_counts[endpoint]
        print(f"{endpoint}  ({n:,} requests)")
        params = param_counts[endpoint]
        if not params:
            print("No query parameters observed)")
        else:
            rows = sorted(
                ([param, count, f"{100 * count // n}%"] for param, count in params.items()),
                key=lambda r: -r[1]
            )
            pretty_print(['parameter', 'requests', '%'], rows)
        print()


if __name__ == '__main__':
    main()
