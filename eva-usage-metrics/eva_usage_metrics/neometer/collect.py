#!/usr/bin/env python
"""
Collect the EVA records of the neometer data streams into the daily Parquet archive.

Neometer keeps 15 days of records: each run loads the complete days of that window that are not archived yet, and
reloads the most recent ones to include documents indexed late. Gaps longer than the window cannot be recovered.
"""
import sys
from argparse import ArgumentParser
from datetime import date

from ebi_eva_common_pyutils.logger import logging_config

from eva_usage_metrics.archive.config import get_archive_dir, get_maven_settings, load_config
from eva_usage_metrics.archive.storage import day_path, days_to_load, write_day
from eva_usage_metrics.neometer.elastic import ElasticClient
from eva_usage_metrics.neometer.extractors import EXTRACTORS

logger = logging_config.get_logger(__name__)


def collect_day(elastic_client, extractor, archive_root, day, dry_run=False):
    dataset = extractor.dataset
    hits = elastic_client.iter_day(extractor.data_stream, extractor.es_filters, day)
    rows = [extractor.map_record(hit) for hit in hits]
    if dry_run:
        logger.info(f'Dry run: {len(rows)} records found for {dataset.name} on {day}. '
                    f'First record: {rows[0] if rows else None}')
        return len(rows)
    # A day without records still gets a file, so that it is not loaded again
    nb_written = write_day(day_path(archive_root, dataset.name, day), rows, dataset.schema)
    logger.info(f'Archived {nb_written} records of {dataset.name} for {day}')
    return nb_written


def collect(elastic_client, extractors, archive_root, days=None, dry_run=False, max_age_days=14, reload_days=2):
    """Collect each data stream and return the names of the datasets that failed."""
    failed_datasets = []
    for extractor in extractors:
        dataset_name = extractor.dataset.name
        dataset_days = days or days_to_load(archive_root, dataset_name, max_age_days=max_age_days,
                                            reload_days=reload_days)
        logger.info(f'Collecting {dataset_name} from {extractor.data_stream} for {len(dataset_days)} days')
        try:
            for day in dataset_days:
                collect_day(elastic_client, extractor, archive_root, day, dry_run)
        except Exception:
            # Carry on with the other data streams: each of them can only be recovered within the neometer window
            logger.exception(f'Failed to collect {dataset_name}')
            failed_datasets.append(dataset_name)
    return failed_datasets


def main():
    parser = ArgumentParser(description=__doc__)
    parser.add_argument('--dataset', action='append', choices=sorted(EXTRACTORS),
                        help='Dataset to collect, can be repeated. All datasets by default')
    parser.add_argument('--day', action='append', type=date.fromisoformat,
                        help='UTC day to (re)load (YYYY-MM-DD), can be repeated. '
                             'By default, the days of the neometer window that need loading')
    parser.add_argument('--max-age-days', type=int, default=14,
                        help='Oldest complete day still available in neometer')
    parser.add_argument('--reload-days', type=int, default=2,
                        help='Number of most recent days reloaded even if already archived')
    parser.add_argument('--batch-size', type=int, default=10000, help='Number of records retrieved per request')
    parser.add_argument('--no-geolocation', action='store_true', default=False,
                        help='Do not look up the location of web service clients that neometer did not locate')
    parser.add_argument('--dry-run', action='store_true', default=False,
                        help='Retrieve and map the records without writing them')
    args = parser.parse_args()
    logging_config.add_stdout_handler()
    load_config()
    profile, private_config_xml_file = get_maven_settings()
    archive_root = get_archive_dir()

    extractors = [EXTRACTORS[name] for name in (args.dataset or EXTRACTORS)]
    EXTRACTORS['web_service'].geolocate = not args.no_geolocation
    elastic_client = ElasticClient.from_config(profile, private_config_xml_file, args.batch_size)
    failed_datasets = collect(elastic_client, extractors, archive_root, args.day, args.dry_run,
                              args.max_age_days, args.reload_days)
    if failed_datasets:
        logger.error(f'Collection failed for: {", ".join(failed_datasets)}')
        sys.exit(1)


if __name__ == '__main__':
    main()
