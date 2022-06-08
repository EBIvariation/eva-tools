import argparse
import os
import re
import subprocess

import requests

from datetime import datetime
from ebi_eva_common_pyutils.command_utils import run_command_with_output
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_common_pyutils.config_utils import EVAPrivateSettingsXMLConfig
from requests.adapters import HTTPAdapter, Retry
from retry import retry

logger = logging_config.get_logger(__name__)
logging_config.add_stdout_handler()


class EVAPRORefresh:
    def __init__(self, gitlab_api_token_file: str, gitlab_trigger_token_file: str, maven_settings_file: str,
                 refresh_scripts_dir: str):
        self.gitlab_api_token_file = gitlab_api_token_file
        self.gitlab_api_token = open(self.gitlab_api_token_file).readline().strip()
        self.gitlab_trigger_token_file = gitlab_trigger_token_file
        self.gitlab_trigger_token = open(self.gitlab_trigger_token_file).readline().strip()
        self.maven_settings_file = maven_settings_file
        self.refresh_scripts_dir = refresh_scripts_dir

        self.delphix_engine = "Delphix_Pub2"
        self.delphix_engine_for_fallback_nodes = "Delphix_Pubfall2"
        self.push_replication_profile_script = "dx_push_replication_pgsql.sh"
        self.create_snapshot_script = "dx_create_snapshot_pgsql.sh"
        self.refresh_vdb_script = "dx_refresh_vdb_pgsql.sh"
        self.gitlab_active_evapro_variable = "ACTIVE_EVAPRO"
        gitlab_url_stem = "https://gitlab.ebi.ac.uk/api/v4"
        self.gitlab_url_to_get_env_vars = gitlab_url_stem + "/groups/EBIVariation/variables"
        self.gitlab_eva_ws_url = gitlab_url_stem + "/projects/466"

    def get_delphix_engine(self, db_vm_name: str):
        # fallback nodes have a different Delphix engine for some reason!!
        # see https://helpdesk.ebi.ac.uk/Transaction/Display.html?id=11333321
        return self.delphix_engine_for_fallback_nodes if "pubfall2" in db_vm_name.lower() else self.delphix_engine

    def get_evapro_hosts_for_profile(self, profile_name: str) -> [str]:
        config = EVAPrivateSettingsXMLConfig(self.maven_settings_file)
        xpath_location_template = '//settings/profiles/profile/id[text()="{0}"]/..' \
                                  '/properties/eva.evapro.jdbc.url/text()'
        # Format is jdbc:postgresql://host:port/db
        metadata_db_jdbc_url = config.get_value_with_xpath(xpath_location_template.format(profile_name))[0]
        pattern = re.compile(r'(?P<hostname>[a-zA-Z\d-.]+):\d+')
        return pattern.findall(metadata_db_jdbc_url)

    # Delphix scripts are pretty brittle and will randomly fail, so the retries are needed :(
    @retry(exceptions=subprocess.CalledProcessError, tries=5, delay=2, backoff=1.2, jitter=(1, 3))
    def create_snapshot_from_source(self, evapro_source_host: str) -> str:
        current_timestamp = datetime.now()
        create_snapshot_command = f"cd {self.refresh_scripts_dir} && " \
                                  f'./{self.create_snapshot_script} -e {self.delphix_engine} ' \
                                  f'-d {evapro_source_host}'
        create_snapshot_output = run_command_with_output(f"Creating database snapshot from {evapro_source_host}...",
                                                         create_snapshot_command, return_process_output=True)
        # Create snapshot command output looks like below
        # dSources are:
        # <hostname>_DATA
        # <hostname>_TBS
        # dSource/dataset to be taken snapshot of:
        # <hostname>_DATA
        # Starting job JOB-7595 for database <hostname>_DATA.
        # Job JOB-7595 finished with state: COMPLETED
        # List of snapshots of dsource/dataset <HOSTNAME>_DATA
        # @2022-05-11T02:00:04.224
        # @2022-05-13T02:00:03.498
        if create_snapshot_output.find("finished with state: COMPLETED") < 0:
            raise Exception(f"Could not create database snapshot from {evapro_source_host}!!")
        timestamp_line_pattern = r"^@[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}$"
        snapshot_recency_map = dict([(datetime.strptime(line, '@%Y-%m-%dT%H:%M:%S.%f') - current_timestamp, line)
                                     for line in create_snapshot_output.split("\n")
                                     if re.match(timestamp_line_pattern, line)])
        return snapshot_recency_map[max(snapshot_recency_map.keys())]

    # Delphix scripts are pretty brittle and will randomly fail, so the retries are needed :(
    @retry(exceptions=subprocess.CalledProcessError, tries=5, delay=2, backoff=1.2, jitter=(1, 3))
    def refresh_evapro_host(self, evapro_source_host: str, snapshot_from_source: str, host_to_refresh: str) -> None:
        refresh_command = f"cd {self.refresh_scripts_dir} && " \
                          f"(./{self.refresh_vdb_script} " \
                          f"-e {self.get_delphix_engine(host_to_refresh)} " \
                          f"-d {host_to_refresh} -t {snapshot_from_source})"
        run_command_with_output(f"Refreshing {host_to_refresh} from {evapro_source_host}...", refresh_command)

    # Delphix scripts are pretty brittle and will randomly fail, so the retries are needed :(
    @retry(exceptions=(subprocess.CalledProcessError, ValueError), tries=5, delay=2, backoff=1.2, jitter=(1, 3))
    def get_replication_profile(self) -> str:
        get_replication_profile_command = f"cd {self.refresh_scripts_dir} && " \
                                          f"(./{self.push_replication_profile_script} " \
                                          f"-e {self.delphix_engine} -p list  | grep ^EVA_ | awk '{{print $1}}')"
        replication_profile = run_command_with_output("Detecting replication profile for EVA...",
                                                      get_replication_profile_command,
                                                      return_process_output=True).strip()
        if replication_profile is None or replication_profile == "":
            raise ValueError("Could not find replication profile for EVA!!.. Retrying...")
        return replication_profile

    # Delphix scripts are pretty brittle and will randomly fail, so the retries are needed :(
    @retry(exceptions=subprocess.CalledProcessError, tries=5, delay=2, backoff=1.2, jitter=(1, 3))
    def push_replication_to_fallback_replication_profile(self, replication_profile: str) -> None:
        push_replication_command = f"cd {self.refresh_scripts_dir} && " \
                                   f"(./{self.push_replication_profile_script} " \
                                   f"-e {self.delphix_engine} -p \"{replication_profile}\")"
        run_command_with_output(f"Pushing replication to fallback replication profile {replication_profile}...",
                                push_replication_command, return_process_output=True)

    def refresh_evapro_profile(self, evapro_profile: str) -> None:
        replication_profile = self.get_replication_profile()
        # This is the source from which the public facing VMs will be refreshed
        evapro_source_host = self.get_evapro_hosts_for_profile("production_processing")[0]
        logger.info(f"Creating snapshot from source {evapro_source_host}....")
        snapshot_from_source = self.create_snapshot_from_source(evapro_source_host)
        logger.info(f"Snapshot {snapshot_from_source} created from source {evapro_source_host}....")
        self.push_replication_to_fallback_replication_profile(replication_profile)
        evapro_hosts_to_refresh = self.get_evapro_hosts_for_profile(evapro_profile)

        if len(evapro_hosts_to_refresh) == 0:
            raise Exception(f"Could not parse EVAPRO hosts from the maven settings file: {self.maven_settings_file}!!")
        for host_to_refresh in evapro_hosts_to_refresh:
            self.refresh_evapro_host(evapro_source_host, snapshot_from_source, host_to_refresh)

    def set_current_evapro_profile(self, new_evapro_profile: str) -> None:
        session = requests.Session()
        gitlab_url_to_put_env_var = self.gitlab_url_to_get_env_vars + "/" + self.gitlab_active_evapro_variable
        # See https://stackoverflow.com/a/51701293
        retries = Retry(total=5, backoff_factor=1.2,
                        status_forcelist=tuple(x for x in requests.status_codes.codes if x != 200))
        session.mount("https://", HTTPAdapter(max_retries=retries))
        put_response = session.put(url=gitlab_url_to_put_env_var, headers={"PRIVATE-TOKEN": self.gitlab_api_token},
                                   verify=True, data={"value": new_evapro_profile})
        put_response.raise_for_status()

    def get_current_evapro_profile(self) -> str:
        session = requests.Session()
        # See https://stackoverflow.com/a/51701293
        retries = Retry(total=5, backoff_factor=1.2,
                        status_forcelist=tuple(x for x in requests.status_codes.codes if x != 200))
        session.mount("https://", HTTPAdapter(max_retries=retries))
        get_response = session.get(url=self.gitlab_url_to_get_env_vars,
                                   headers={"PRIVATE-TOKEN": self.gitlab_api_token}, verify=True)
        get_response.raise_for_status()
        get_response_json = get_response.json()
        if len(get_response_json) > 0:
            current_evapro_profile = [prop["value"] for prop in get_response_json
                                      if prop["variable_type"] == "env_var"
                                      and prop["key"] == self.gitlab_active_evapro_variable][0]
            if current_evapro_profile:
                return current_evapro_profile
        raise Exception("ERROR: Could not find current EVAPRO instance from Gitlab environment variables!!")

    def package_apps_with_current_evapro_profile(self, ) -> None:
        session = requests.Session()
        gitlab_get_tags_url = self.gitlab_eva_ws_url + "/repository/tags"
        gitlab_deploy_url = self.gitlab_eva_ws_url + "/trigger/pipeline"
        # See https://stackoverflow.com/a/51701293
        retries = Retry(total=5, backoff_factor=1.2,
                        status_forcelist=tuple(x for x in requests.status_codes.codes if x != 200))
        session.mount("https://", HTTPAdapter(max_retries=retries))
        get_response = session.get(url=gitlab_get_tags_url, headers={"PRIVATE-TOKEN": self.gitlab_api_token},
                                   verify=True)
        get_response.raise_for_status()
        get_response_json = get_response.json()
        if len(get_response_json) > 0:
            eva_ws_latest_tag = get_response_json[0]["name"]
        else:
            raise Exception("ERROR: Could not find the latest tag for the eva-ws project!!")
        post_response = session.post(url=gitlab_deploy_url, data={"token": self.gitlab_trigger_token,
                                                                  "ref": eva_ws_latest_tag})
        post_response.raise_for_status()

    # Since we use blue-green deployment, get the current (green) profile
    # so that the data in the alternate (blue) profile can be refreshed
    # and the application can subsequently be pointed to the blue profile
    # See https://docs.google.com/drawings/d/1UjVfXkNplxEvT50v_DpEvr3MLI1Sug-Df4wwbdf12ZQ/edit?pli=1
    def refresh(self) -> None:
        alternate_profiles_map = {"evapro-a": "evapro-b", "evapro-b": "evapro-a"}
        current_evapro_profile = self.get_current_evapro_profile()
        alternate_evapro_profile = alternate_profiles_map[current_evapro_profile]
        logger.info(f"Current profile is {current_evapro_profile}. "
                    f"Therefore, the alternate profile {alternate_evapro_profile} will be refreshed...")
        self.refresh_evapro_profile(alternate_evapro_profile)
        # Running the replication script emits a lot of these error messages
        # DBA says this is no cause for alarm!
        os.system("echo -e \"\\033[35;1;4mIGNORE MESSAGES with 'Use of uninitialized value...' above\\033[0m\"")
        self.set_current_evapro_profile(alternate_evapro_profile)
        # At this time, only packaging is possible via trigger, deployment should be triggered manually via Gitlab UI
        self.package_apps_with_current_evapro_profile()


def main():
    parser = argparse.ArgumentParser(description='Refresh EVAPRO data in public facing databases '
                                                 'from the production database', add_help=True)
    parser.add_argument("--gitlab-api-token-file", help="File with the GitLab API token (ex:/path/to/gitlab/api_token)",
                        required=True)
    parser.add_argument("--gitlab-trigger-token-file",
                        help="File with the GitLab trigger token (ex:/path/to/gitlab/trigger_token)", required=True)
    parser.add_argument("--maven-settings-file",
                        help="File with the Maven Settings (ex: /path/to/eva-maven-settings.xml)", required=True)
    parser.add_argument("--refresh-scripts-dir",
                        help="Path to the directory with the refresh scripts (ex: /path/to/refresh/scripts)",
                        required=True)
    args = parser.parse_args()

    eva_pro_refresh_obj = EVAPRORefresh(gitlab_api_token_file=args.gitlab_api_token_file,
                                        gitlab_trigger_token_file=args.gitlab_trigger_token_file,
                                        maven_settings_file=args.maven_settings_file,
                                        refresh_scripts_dir=args.refresh_scripts_dir)
    eva_pro_refresh_obj.refresh()


if __name__ == "__main__":
    main()
