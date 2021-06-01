#!/usr/bin/env python
import os
import subprocess
import sys
from argparse import ArgumentParser
from datetime import datetime
from urllib.parse import urlsplit

import psycopg2
import yaml
from ebi_eva_common_pyutils import command_utils
from ebi_eva_common_pyutils.config import cfg
from ebi_eva_common_pyutils.config_utils import get_properties_from_xml_file
from ebi_eva_common_pyutils.logger import logging_config, AppLogger
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query, execute_query
from pymongo.uri_parser import split_hosts

sys.path.append(os.path.dirname(__file__))

from remapping_config import load_config


class RemappingJob(AppLogger):

    @staticmethod
    def get_metadata_creds():
        properties = get_properties_from_xml_file(cfg['maven']['environment'], cfg['maven']['settings_file'])
        pg_url = properties['eva.evapro.jdbc.url']
        pg_user = properties['eva.evapro.user']
        pg_pass = properties['eva.evapro.password']
        return pg_url, pg_user, pg_pass

    @staticmethod
    def get_mongo_creds():
        properties = get_properties_from_xml_file(cfg['maven']['environment'], cfg['maven']['settings_file'])
        # Use the primary mongo host from configuration:
        # https://github.com/EBIvariation/configuration/blob/master/eva-maven-settings.xml#L111
        # TODO: revisit once accessioning/variant pipelines can support multiple hosts
        mongo_host = split_hosts(properties['eva.mongo.host'])[1][0]
        mongo_user = properties['eva.mongo.user']
        mongo_pass = properties['eva.mongo.passwd']
        return mongo_host, mongo_user, mongo_pass

    @staticmethod
    def get_accession_pg_creds():
        properties = get_properties_from_xml_file(cfg['maven']['environment'], cfg['maven']['settings_file'])
        pg_url = properties['eva.accession.jdbc.url']
        pg_user = properties['eva.accession.user']
        pg_pass = properties['eva.accession.password']
        return pg_url, pg_user, pg_pass

    @staticmethod
    def write_remapping_process_props_template(template_file_path):
        mongo_host, mongo_user, mongo_pass = RemappingJob.get_mongo_creds()
        pg_url, pg_user, pg_pass = RemappingJob.get_accession_pg_creds()
        with open(template_file_path, 'w') as open_file:
            open_file.write(f'''spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url={pg_url}
spring.datasource.username={pg_user}
spring.datasource.password={pg_pass}
spring.datasource.tomcat.max-active=3

spring.jpa.generate-ddl=true

spring.data.mongodb.host={mongo_host}
spring.data.mongodb.port=27017
spring.data.mongodb.database=eva_accession_sharded
spring.data.mongodb.username={mongo_user}
spring.data.mongodb.password={mongo_pass}

spring.data.mongodb.authentication-database=admin
mongodb.read-preference=primaryPreferred
spring.main.web-environment=false
spring.main.allow-bean-definition-overriding=true
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
logging.level.uk.ac.ebi.eva.accession.remapping=INFO
parameters.chunkSize=1000
''')
        return template_file_path

    @staticmethod
    def get_metadata_conn():
        pg_url, pg_user, pg_pass = RemappingJob.get_metadata_creds()
        return psycopg2.connect(urlsplit(pg_url).path, user=pg_user, password=pg_pass)

    def get_job_information(self, assembly):
        query = (
            'SELECT source, scientific_name, taxid, target_assembly_accession, SUM(number_of_study), '
            'SUM(number_submitted_variants) '
            'FROM remapping_progress '
            f"WHERE assembly_accession='{assembly}' "
            'GROUP BY source, assembly_accession, scientific_name, taxid, target_assembly_accession'
        )
        source_list = []
        scientific_name = None
        taxid = None
        target_assembly = None
        n_study = 0
        n_variants = 0
        with self.get_metadata_conn() as pg_conn:
            for source, scientific_name, taxid, target_assembly, n_st, n_var in get_all_results_for_query(pg_conn, query):

                source_list.append(source)
                n_study += n_st
                n_variants += n_var

        sources = ', '.join(source_list)
        return sources, scientific_name, taxid, target_assembly, n_study, n_variants

    def list_assemblies_to_process(self):
        query = 'SELECT DISTINCT assembly_accession FROM remapping_progress'
        with self.get_metadata_conn() as pg_conn:
            for assembly, in get_all_results_for_query(pg_conn, query):
                sources, scientific_name, taxid, target_assembly, n_study, n_variants = self.get_job_information(assembly)
                print(sources, scientific_name, taxid, assembly, target_assembly, n_study, n_variants)

    def set_status_start(self, assembly):
        query = ('UPDATE remapping_progress '
                 f"SET progress_status='Started', start_time = '{datetime.now().isoformat()}' "
                 f"WHERE assembly_accession='{assembly}'")
        with self.get_metadata_conn() as pg_conn:
            execute_query(pg_conn, query)

    def set_status_end(self, assembly):
        query = ('UPDATE remapping_progress '
                 f"SET progress_status='Completed', completion_time = '{datetime.now().isoformat()}' "
                 f"WHERE assembly_accession='{assembly}'")
        with self.get_metadata_conn() as pg_conn:
            execute_query(pg_conn, query)

    def set_status_failed(self, assembly):
        query = ('UPDATE remapping_progress '
                 f"SET progress_status = 'Failed' "
                 f"WHERE assembly_accession='{assembly}'")
        with self.get_metadata_conn() as pg_conn:
            execute_query(pg_conn, query)

    def check_processing_required(self, assembly, target_assembly, n_variants):
        if str(target_assembly) != 'None' and assembly != target_assembly and int(n_variants) > 0:
            return True
        return False

    def process_one_assembly(self, assembly, resume):
        self.set_status_start(assembly)
        base_directory = cfg['remapping']['base_directory']
        sources, scientific_name, taxid, target_assembly, n_study, n_variants = self.get_job_information(assembly)
        if not self.check_processing_required(assembly, target_assembly, n_variants):
            self.info(f'Not Processing assembly {assembly} -> {target_assembly} for taxonomy {taxid}: '
                      f'{n_study} studies with {n_variants} '
                      f'found in {sources}')
            self.set_status_end(assembly)
            return

        self.info(f'Process assembly {assembly} for taxonomy {taxid}: {n_study} studies with {n_variants} '
                  f'found in {sources}')
        nextflow_remapping_process = os.path.join(os.path.dirname(__file__), 'remapping_process.nf')
        assembly_directory = os.path.join(base_directory, assembly)
        work_dir = os.path.join(assembly_directory, 'work')
        prop_template_file = os.path.join(assembly_directory, 'template.properties')
        os.makedirs(work_dir, exist_ok=True)
        remapping_log = os.path.join(assembly_directory, 'remapping_process.log')
        remapping_config_file = os.path.join(assembly_directory, 'remapping_process_config_file.yaml')
        remapping_config = {
            'source_assembly_accession': assembly,
            'target_assembly_accession': target_assembly,
            'species_name': scientific_name,
            'output_dir': assembly_directory,
            'genome_assembly_dir': cfg['genome_downloader']['output_directory'],
            'template_properties': self.write_remapping_process_props_template(prop_template_file),
        }

        for part in ['executable', 'nextflow', 'jar']:
            remapping_config[part] = cfg[part]

        with open(remapping_config_file, 'w') as open_file:
            yaml.safe_dump(remapping_config, open_file)

        try:
            command = [
                cfg['executable']['nextflow'],
                '-log', remapping_log,
                'run', nextflow_remapping_process,
                '-params-file', remapping_config_file,
                '-work-dir', work_dir
            ]
            if resume:
                command.append('-resume')
            curr_working_dir = os.getcwd()
            os.chdir(assembly_directory)
            command_utils.run_command_with_output('Nextflow remapping process', ' '.join(command))
        except subprocess.CalledProcessError as e:
            self.error('Nextflow reampping pipeline failed')
            self.set_status_failed(assembly)
            raise e
        finally:
            os.chdir(curr_working_dir)
        self.set_status_end(assembly)


def main():
    argparse = ArgumentParser(description='')
    argparse.add_argument('--assembly', help='Assembly to be process', required=True)
    argparse.add_argument('--list_jobs', help='Display the list of jobs to be run.', action='store_true', default=False)
    argparse.add_argument('--resume', help='If a process has been run already This will resume it.',
                          action='store_true', default=False)

    args = argparse.parse_args()

    load_config()

    if args.list_jobs:
        RemappingJob().list_assemblies_to_process()
    else:
        logging_config.add_stdout_handler()
        RemappingJob().process_one_assembly(args.assembly, args.resume)


if __name__ == "__main__":
    main()
