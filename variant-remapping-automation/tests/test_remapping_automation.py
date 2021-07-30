import os
import unittest

from remapping_automation import count_variants_extracted, pretty_print


class TestRemappingAutomation(unittest.TestCase):

    def test_count_variants_extracted(self):
        log_file = os.path.abspath(os.path.join(os.path.dirname(__file__), 'ressources', 'vcf_extractor.log'))
        assert count_variants_extracted(log_file) == (25434599, 25434599, 72446279, 72446277)

    def test_pretty_print(self):
        pretty_print(['Header 1', 'Long Header 2'], [['row1 cell 1', 'row1 cell 2'], ['row2 cell 1', 'Super long row2 cell 2']])