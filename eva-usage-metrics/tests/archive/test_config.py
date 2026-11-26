import pytest

from eva_usage_metrics.archive import config
from eva_usage_metrics.archive.config import get_archive_dir, get_maven_settings, load_config


def test_load_config_from_the_environment_variable(tmp_path, monkeypatch):
    config_file = tmp_path / 'usage_metrics_config.yml'
    config_file.write_text('maven:\n  environment: production_processing\n  settings_file: /path/settings.xml\n'
                           'usage_metrics:\n  archive_dir: /path/archive\n')
    monkeypatch.setenv(config.CONFIG_ENVIRONMENT_VARIABLE, str(config_file))
    load_config()
    assert get_maven_settings() == ('production_processing', '/path/settings.xml')
    assert get_archive_dir() == '/path/archive'


def test_missing_maven_settings(tmp_path):
    config_file = tmp_path / 'usage_metrics_config.yml'
    config_file.write_text('maven:\n  environment: production_processing\n')
    load_config(str(config_file))
    with pytest.raises(ValueError):
        get_maven_settings()
    with pytest.raises(ValueError):
        get_archive_dir()
