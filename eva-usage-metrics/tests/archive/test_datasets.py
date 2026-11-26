from eva_usage_metrics.archive.datasets import COMMON_FIELDS, DATASETS


def test_every_dataset_has_the_common_columns_first():
    common = [field.name for field in COMMON_FIELDS]
    for dataset in DATASETS.values():
        assert dataset.schema.names[:len(common)] == common
        assert dataset.item_column in dataset.schema.names
        assert len(set(dataset.schema.names)) == len(dataset.schema.names)


def test_legacy_datasets_share_the_columns_and_metric_source_of_the_dataset_they_precede():
    for legacy_name, live_name in (('legacy_web_service', 'web_service'), ('legacy_ftp_transfer', 'ftp_transfer')):
        legacy, live = DATASETS[legacy_name], DATASETS[live_name]
        assert legacy.schema == live.schema
        assert (legacy.metric_source, legacy.item_column, legacy.is_transfer) == \
               (live.metric_source, live.item_column, live.is_transfer)
