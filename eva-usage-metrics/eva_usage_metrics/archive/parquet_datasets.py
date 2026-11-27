"""
Datasets of the archive: the columns of each of them, and how the summaries use them.

This is the contract between the parts of the package: neometer and legacy fill the datasets, summary reads them.
"""
from dataclasses import dataclass, field

import pyarrow as pa

# Columns shared by all the datasets, used by the summaries
COMMON_FIELDS = [
    pa.field('event_ts', pa.timestamp('us', tz='UTC')),
    pa.field('es_index', pa.string()),
    pa.field('es_id', pa.string()),
    pa.field('client_id', pa.string()),     # client IP for the web services, client hash for file transfers
    pa.field('direction', pa.string()),     # 'download' or 'upload' for file transfers, empty for web services
    pa.field('path', pa.string()),
    pa.field('bytes', pa.int64()),
    pa.field('duration_secs', pa.float64()),
    pa.field('status', pa.string()),        # HTTP status code, or the outcome of FTP and Globus transfers
    pa.field('country_code', pa.string()),
    pa.field('region', pa.string()),
    pa.field('city', pa.string()),
]

WEB_SERVICE_FIELDS = [
    pa.field('endpoint', pa.string()),      # path normalised with archive.endpoints.normalise_path
    pa.field('query', pa.string()),
    pa.field('http_method', pa.string()),
    pa.field('user_agent', pa.string()),
    pa.field('server_node', pa.string()),
    pa.field('latitude', pa.float64()),
    pa.field('longitude', pa.float64()),
]
HTTP_TRANSFER_FIELDS = [pa.field('http_method', pa.string())]
FTP_TRANSFER_FIELDS = [
    pa.field('access_mode', pa.string()),   # xferlog access mode: 'a' anonymous, 'r' real user, 'g' guest
    pa.field('user_name', pa.string()),
]
GLOBUS_TRANSFER_FIELDS = [
    pa.field('user_name', pa.string()),
    pa.field('event_start', pa.timestamp('us', tz='UTC')),
    pa.field('event_end', pa.timestamp('us', tz='UTC')),
]


@dataclass(frozen=True)
class Dataset:
    name: str               # directory of the dataset in the archive
    metric_source: str      # name used in the summaries: legacy and current records of a service share it
    is_transfer: bool       # file transfers, as opposed to web service requests
    item_column: str        # column the daily summary is grouped by
    extra_fields: list = field(default_factory=list)

    @property
    def schema(self):
        return pa.schema(COMMON_FIELDS + self.extra_fields)


WEB_SERVICE = Dataset('web_service', 'web_service', False, 'endpoint', WEB_SERVICE_FIELDS)
HTTP_TRANSFER = Dataset('http_transfer', 'http_transfer', True, 'path', HTTP_TRANSFER_FIELDS)
FTP_TRANSFER = Dataset('ftp_transfer', 'ftp_transfer', True, 'path', FTP_TRANSFER_FIELDS)
GLOBUS_TRANSFER = Dataset('globus_transfer', 'globus_transfer', True, 'path', GLOBUS_TRANSFER_FIELDS)
# Exported from the legacy Postgres tables, with the columns of the dataset that follows them
LEGACY_WEB_SERVICE = Dataset('legacy_web_service', 'web_service', False, 'endpoint', WEB_SERVICE_FIELDS)
LEGACY_FTP_TRANSFER = Dataset('legacy_ftp_transfer', 'ftp_transfer', True, 'path', FTP_TRANSFER_FIELDS)

DATASETS = {dataset.name: dataset for dataset in (
    WEB_SERVICE, HTTP_TRANSFER, FTP_TRANSFER, GLOBUS_TRANSFER, LEGACY_WEB_SERVICE, LEGACY_FTP_TRANSFER
)}
