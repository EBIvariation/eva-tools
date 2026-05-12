from argparse import ArgumentParser
from collections import defaultdict

from ebi_eva_common_pyutils.common_utils import pretty_print
from ebi_eva_internal_pyutils.metadata_utils import get_metadata_connection_handle

# Keys are tuples of consecutive path segments; the segment immediately following the match
# is replaced with the placeholder. Longer (more specific) keys take precedence over shorter ones.
CONTEXT_RULES = {
    ('chromosomes', 'md5checksum'): '{md5}',
    ('chromosomes', 'insdc'):       '{insdc_accession}',
    ('chromosomes', 'name'):        '{chromosome_name}',
    ('variants',):                  '{variant_id}',
    ('segments',):                  '{region}',
    ('genes',):                     '{gene_name}',
    ('studies',):                   '{study_accession}',
    ('studies', 'ro-crate'):        '{study_accession}',
    ('species',):                   '{species}',
    ('files',):                     '{file_id}',
    ('clustered-variants',):        '{variant_id}',
    ('submitted-variants',):        '{variant_id}',
    ('chromosome',):                '{chromosome_accession}',
    ('submission',):                '{submission_id}',
    ('assemblies',):                '{assembly_accession}',
    ('genbank',):                   '{genbank_accession}',
    ('chromosomes', 'genbank'):     '{genbank_accession}',
}

_RULE_KEYS = set(CONTEXT_RULES.keys())
# All proper prefixes of rule keys — used to know when to keep accumulating segments.
_RULE_KEY_PREFIXES = {k[:i] for k in _RULE_KEYS for i in range(1, len(k))}

BATCH_SIZE = 10_000

def normalise_path(path):
    """
    Normalise a path by splitting it into segments and applying rules to replace segments with placeholders.
    E.g. /eva/webservices/contig-alias/v1/chromosomes/md5checksum/7b6e06758e53927330346e9e7cc00cce
     ==> /eva/webservices/contig-alias/v1/chromosomes/md5checksum/{md5}
    """
    segments = path.split('/')
    normalised = []
    pending_key = ()   # rule-key prefix accumulated so far
    replace_next = None

    for seg in segments:
        if replace_next is not None:
            normalised.append(replace_next)
            replace_next = None
            continue

        candidate = pending_key + (seg,)
        is_complete = candidate in _RULE_KEYS
        is_prefix   = candidate in _RULE_KEY_PREFIXES

        if is_complete and not is_prefix:
            # Unambiguous complete match — flag next segment for replacement.
            normalised.append(seg)
            replace_next = CONTEXT_RULES[candidate]
            pending_key = ()
        elif is_complete or is_prefix:
            # Complete but extendable, or still just a prefix — keep accumulating.
            normalised.append(seg)
            pending_key = candidate
        else:
            # Dead end: if accumulated pending_key is itself a complete rule,
            # the current segment is the parameter; otherwise pass it through.
            if pending_key in _RULE_KEYS:
                normalised.append(CONTEXT_RULES[pending_key])
            else:
                normalised.append(seg)
            pending_key = ()

    return '/'.join(normalised)


def stream_rows(conn, query):
    cursor = conn.cursor()
    cursor.execute(query)
    while True:
        batch = cursor.fetchmany(BATCH_SIZE)
        if not batch:
            break
        yield from batch


def main():
    parser = ArgumentParser(description='Summarise EVA web service endpoint usage for a given date.')
    parser.add_argument('--date', required=True, help='Date to analyse (YYYY-MM-DD)')
    parser.add_argument('--config-file', default='maven_settings.xml',
                        help='Path to private config XML (default: maven_settings.xml)')
    args = parser.parse_args()

    query = f"""
        SELECT client_ip, bytes_out, duration, request_uri_path, http_status
        FROM eva_web_srvc_stats.ws_traffic_useful_cols
        WHERE request_ts >= '{args.date}'
    """

    # Incremental accumulators — only one batch lives in memory at a time.
    counts = defaultdict(int)
    ip_sets = defaultdict(set)
    bytes_sums = defaultdict(int)
    duration_sums = defaultdict(float)
    total_rows = 0

    print(f"Querying traffic since {args.date}")
    with get_metadata_connection_handle('production_processing', args.config_file) as conn:
        for client_ip, bytes_out, duration, request_uri_path, http_status in stream_rows(conn, query):
            total_rows += 1
            if total_rows % BATCH_SIZE == 0:
                print(f"processed {total_rows:,} rows", end='\r')
            if not str(http_status).startswith('2'):
                continue
            if request_uri_path.endswith(('.js', '.css', 'png', 'html')):
                continue
            endpoint = normalise_path(request_uri_path)
            counts[endpoint] += 1
            ip_sets[endpoint].add(client_ip)
            bytes_sums[endpoint] += int(bytes_out or 0)
            duration_sums[endpoint] += float(duration or 0)

    print()
    if total_rows == 0:
        print("No data found since that date.")
        return

    print(f"Fetched {total_rows:,} rows total.")

    rows = []
    for endpoint, n in counts.items():
        rows.append([
            endpoint,
            n,
            len(ip_sets[endpoint]),
            bytes_sums[endpoint],
            round(duration_sums[endpoint] / n, 1),
        ])
    rows.sort(key=lambda r: -r[1])

    print(f"\n=== Endpoint summary since {args.date} ===\n")
    pretty_print(
        ['endpoint', 'requests', 'unique_ips', 'total_bytes_out', 'avg_duration_ms'],
        rows
    )


if __name__ == '__main__':
    main()
