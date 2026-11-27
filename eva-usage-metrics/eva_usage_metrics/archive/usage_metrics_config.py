"""
Configuration of the commands, loaded the same way as in eva-submission. Example:

    maven:
      environment: production_processing
      settings_file: /path/to/eva-maven-settings.xml
    usage_metrics:
      archive_dir: /path/to/usage_archive
"""
import os

from ebi_eva_common_pyutils.config import cfg

CONFIG_ENVIRONMENT_VARIABLE = 'USAGEMETRICSCONFIG'
DEFAULT_CONFIG_FILE = '~/.usage_metrics_config.yml'


def load_config(*args):
    """Load a config file from any path provided.
    If none are provided then read from a file path provided in the environment variable USAGEMETRICSCONFIG.
    If not provided then default to .usage_metrics_config.yml placed in the current user's home"""
    cfg.load_config_file(
        *args,
        os.getenv(CONFIG_ENVIRONMENT_VARIABLE),
        os.path.expanduser(DEFAULT_CONFIG_FILE),
    )


def get_maven_settings():
    """Return the profile and the settings file providing the neometer and Postgres credentials."""
    profile = cfg.query('maven', 'environment')
    settings_file = cfg.query('maven', 'settings_file')
    if not profile or not settings_file:
        raise ValueError(f'maven.environment and maven.settings_file must be set in {cfg.config_file}')
    return profile, settings_file


def get_archive_dir():
    """Return the base directory of the Parquet archive."""
    archive_dir = cfg.query('usage_metrics', 'archive_dir')
    if not archive_dir:
        raise ValueError(f'usage_metrics.archive_dir must be set in {cfg.config_file}')
    return archive_dir
