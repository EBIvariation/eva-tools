# eva-usage-metrics

Gathers EVA usage metrics: it archives the neometer Elasticsearch records as daily Parquet files and summarises them
into small Postgres tables. It replaces `eva-usage-stats`, whose tables stored every request in Postgres. Reporting
on the summary tables is outside the scope of this package.

| Dataset | Origin | Records |
|---|---|---|
| `web_service` | `logs-web-realtime` | Requests to `/eva/webservices/*` |
| `http_transfer` | `logs-transfer.http-default` | HTTP downloads and uploads of `*/pub/databases/eva/*` |
| `ftp_transfer` | `logs-transfer.ftp-default` | FTP downloads and uploads of `*/pub/databases/eva/*` |
| `globus_transfer` | `logs-transfer.globus-default` | Globus downloads and uploads of `*/pub/databases/eva/*` |
| `legacy_web_service` | `ws_traffic`, `ws_traffic_hist` tables | Exported once by `export-legacy-usage` |
| `legacy_ftp_transfer` | `ftp_traffic` table | Exported once by `export-legacy-usage` |

## Structure

| Subpackage | Role | Command |
|---|---|---|
| `archive` | Shared core: the datasets and their columns, the Parquet layout, endpoint normalisation, the config | |
| `neometer` | Collects the neometer data streams into the archive | `collect-usage` |
| `legacy` | Exports the legacy Postgres tables into the archive, once. Remove it once it has been used | `export-legacy-usage` |
| `summary` | Summarises the archive into the Postgres summary tables | `summarise-usage` |

`neometer`, `legacy` and `summary` only depend on `archive`, never on each other. The datasets defined in
`archive/datasets.py` are the contract between them:
- each extractor in `neometer/extractors.py` maps one data stream to a dataset;
- each export in `legacy/queries.py` maps legacy tables to a dataset;
- `summary` reads the datasets.

## Install

```bash
pip install eva-usage-metrics/            # add [test] for pytest
pytest tests                              # run from eva-usage-metrics/
```

## Archive

```
<archive_dir>/<dataset>/day=YYYY-MM-DD/data.parquet
```

There is one file per dataset and UTC day. A file is written to a temporary name and then renamed, so a day is either
complete or absent. All datasets share these columns: `event_ts`, `es_index`, `es_id`, `client_id`, `direction`,
`path`, `bytes`, `duration_secs`, `status`, `country_code`, `region`, `city`. Each dataset then adds its own
(see `eva_usage_metrics/archive/datasets.py`).

Neometer keeps **15 days** of records, so the archive is the only copy of anything older. The archive directory must be
on backed-up storage.

## Commands

The commands read the location of the archive, and the Maven settings file providing the credentials, from a YAML
config file, loaded the same way as in eva-submission. The config file is the `USAGEMETRICSCONFIG` environment
variable, or `~/.usage_metrics_config.yml` by default:

```yaml
maven:
  environment: production_processing
  settings_file: /path/to/eva-maven-settings.xml
usage_metrics:
  archive_dir: /path/to/usage_archive   # base directory of the Parquet archive, on backed-up storage
```

| Command | What it does |
|---|---|
| `collect-usage` | Writes the complete days of the neometer window that are not archived yet, and reloads the last 2 days. `--day` forces given days, `--dataset` selects datasets, `--dry-run` writes nothing. Exits non-zero if any dataset failed. |
| `summarise-usage` | Rebuilds the summaries of the last 15 days, plus the current month and year. `--from/--to` or `--rebuild-all` for other ranges, `--create-tables` the first time. |
| `export-legacy-usage` | One-off export of the legacy tables, stopping at the first day collected from neometer. It checks the number of records per day. `--dry-run` first; `--method csv` if the DuckDB postgres extension cannot be installed. |

Nightly, after 00:00 UTC: `collect-usage`, then `summarise-usage`. Alert on any failure: gaps of more than 15 days
cannot be recovered.

## Summary tables (schema `eva_web_srvc_stats`)

- `usage_daily`: one row per day, source, direction, item, status class and country.
  - The item is the normalised endpoint for the web services and the file path for file transfers.
  - The RS release species and the study are derived from the file path.
  - Measures: `n_events`, `n_distinct_clients`, `bytes`, `total_duration_secs`.
- `usage_distinct_clients`: distinct clients per `day`, `month` and `year` (`period_type`).
  - Counted per source, and for `all_transfers`.
  - Dimensions: `all`; and, per month and year, `country`, `release_species` and `study`.
  - Distinct clients cannot be added up across days or groups, so they are computed for each period.

The `source` of a summary row is the service, so legacy and neometer records of the same service share it, e.g.
`ftp_transfer`.

Caveats:
- **Client identifiers differ by source.** They are client IPs for the web services, neometer client hashes for file
  transfers, and the old host identifier for `legacy_ftp_transfer`. Distinct clients cannot be compared across these.
- **HTTP 206 responses overcount downloads.** Partial responses make `n_events` higher than the number of downloads.
  For file transfers, `n_distinct_clients` per file and day is a better estimate.
