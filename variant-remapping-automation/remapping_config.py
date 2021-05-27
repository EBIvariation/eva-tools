#!/usr/bin/env python
import os

import yaml
from ebi_eva_common_pyutils.config import Configuration, cfg


def load_config(*args):
    """
    Load a config file from any path provided.
    If none are provided then read from a file path provided in the environment variable REMAPPINGCONFIG.
    If not provided then default to .remapping_config.yml place in the current users' home
    """
    cfg.load_config_file(
        *args,
        os.getenv('REMAPPINGCONFIG'),
        os.path.expanduser('~/.remapping_config.yml'),
    )
