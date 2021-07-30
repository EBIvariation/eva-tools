#!/usr/bin/env python

# Copyright 2019 EMBL - European Bioinformatics Institute
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import argparse
import os
import re
import shutil
import sys
import urllib
from csv import DictReader, excel_tab, DictWriter

from cached_property import cached_property
from ebi_eva_common_pyutils.config import cfg
from ebi_eva_common_pyutils.logger import AppLogger
from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle
from ebi_eva_common_pyutils.pg_utils import get_all_results_for_query
from retry import retry

sys.path.append(os.path.dirname(__file__))

from remapping_config import load_config


class CustomAssembly(AppLogger):

    def __init__(self, assembly_accession, assembly_fasta_path, assembly_report_path, eutils_api_key=None):
        self.assembly_accession = assembly_accession
        self.assembly_fasta_path = assembly_fasta_path
        self.assembly_report_path = assembly_report_path
        self.eutils_api_key = eutils_api_key or cfg.get('eutils_api_key')
        self.assembly_report_headers = None

    @property
    def output_assembly_report_path(self):
        base, ext = os.path.splitext(self.assembly_report_path)
        return base + '_custom' + ext

    @property
    def output_assembly_fasta_path(self):
        base, ext = os.path.splitext(self.assembly_fasta_path)
        return base + '_custom' + ext

    @property
    def assembly_directory(self):
        return os.path.dirname(self.assembly_fasta_path)

    @cached_property
    def required_contigs(self):
        with get_metadata_connection_handle(cfg['maven']['environment'], cfg['maven']['settings_file']) as pg_conn:
            query = ("select distinct contig_accession from eva_tasks.eva2469_contig_analysis "
                     "where source_table in ('dbsnpSubmittedVariantEntity', 'submittedVariantEntity') "
                     f"and assembly_accession='{self.assembly_accession}'")
            return [genbank_accession for genbank_accession, in get_all_results_for_query(pg_conn, query)]

    @cached_property
    def assembly_report_rows(self):
        """Download the assembly report if it does not exist then parse it to create a generator
        that return each row as a dict."""
        with open(self.assembly_report_path) as open_file:
            # Parse the assembly report file to find the header then stop
            for line in open_file:
                if line.lower().startswith("# sequence-name") and "sequence-role" in line.lower():
                    self.assembly_report_headers = line.strip().split('\t')
                    break
            reader = DictReader(open_file, fieldnames=self.assembly_report_headers, dialect=excel_tab)
            return [record for record in reader]

    @property
    def genbank_contig_to_add(self):
        return set(self.required_contigs) - set(row['GenBank-Accn'] for row in self.assembly_report_rows)

    @staticmethod
    def get_written_contigs(fasta_path):
        written_contigs = []
        match = re.compile(r'>(.*?)\s')
        if os.path.isfile(fasta_path):
            with open(fasta_path, 'r') as file:
                for line in file:
                    written_contigs.extend(match.findall(line))
        return written_contigs

    @retry(tries=4, delay=2, backoff=1.2, jitter=(1, 3))
    def download_contig_from_ncbi(self, contig_accession):
        sequence_tmp_path = os.path.join(self.assembly_directory, contig_accession + '.fa')
        self.info(contig_accession + " downloaded and added to FASTA sequence")
        parameters = {
            'db': 'nuccore',
            'id': contig_accession,
            'rettype': 'fasta',
            'retmode': 'text',
            'tool': 'eva',
            'email': 'eva-dev@ebi.ac.uk'
        }
        if self.eutils_api_key:
            parameters['api_key'] = self.eutils_api_key
        url = 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?' + urllib.parse.urlencode(parameters)
        self.info('Downloading ' + contig_accession)
        urllib.request.urlretrieve(url, sequence_tmp_path)

        return sequence_tmp_path

    def generate_assembly_report(self):
        assembly_report_rows = self.assembly_report_rows
        for genbank_contig in self.genbank_contig_to_add:
            self.assembly_report_rows.append({
                "# Sequence-Name": genbank_contig,
                "Sequence-Role": "scaffold",
                "GenBank-Accn":genbank_contig
            })

        with open(self.output_assembly_report_path, 'w') as open_output:
            writer = DictWriter(open_output, fieldnames=self.assembly_report_headers,  dialect=excel_tab, restval='na')
            writer.writeheader()
            for row in assembly_report_rows:
                writer.writerow(row)

    def generate_fasta(self):
        """
        Download the assembly report if it does not exist then create the assembly fasta from the contig.
        If the assembly already exist then it only add any missing contig.
        """
        shutil.copy(self.assembly_fasta_path, self.output_assembly_fasta_path, follow_symlinks=True)
        written_contigs = self.get_written_contigs(self.output_assembly_fasta_path)
        # Now append all the new contigs to the existing fasta
        contig_to_append = []
        for contig_accession in self.genbank_contig_to_add:
            if contig_accession not in written_contigs:
                contig_to_append.append(self.download_contig_from_ncbi(contig_accession))

        with open(self.output_assembly_fasta_path, 'a+') as fasta:
            for contig_path in contig_to_append:
                with open(contig_path) as sequence:
                    for line in sequence:
                        # Check that the line is not empty
                        if line.strip():
                            fasta.write(line)
                os.remove(contig_path)


def main():
    parser = argparse.ArgumentParser(description='Generate custom assembly report for a given species and assembly',
                                     add_help=False)
    parser.add_argument("-a", "--assembly-accession",
                        help="Assembly for which the process has to be run, e.g. GCA_000002315.3",
                        required=True)
    parser.add_argument("-f", "--fasta-file", help="Path to the fasta file containing the assembly", required=True)
    parser.add_argument("-r", "--report-file",
                        help="Path to the assembly report file containing the assembly", required=True)
    parser.add_argument('--help', action='help', help='Show this help message and exit')

    args = parser.parse_args()

    load_config()

    assembly = CustomAssembly(args.assembly_accession, args.fasta_file, args.report_file)
    assembly.generate_assembly_report()
    assembly.generate_fasta()


if __name__ == "__main__":
    main()
