import os
import unittest

from remapping_automation import count_variants_extracted


class TestRemmapingAutomation(unittest.TestCase):

    def test_count_variants_extracted(self):
        log_file = os.path.abspath(os.path.join(os.path.dirname(__file__), 'ressources', 'vcf_extractor.log'))
        assert count_variants_extracted(log_file) == (25434599, 25434599, 72446279, 72446277)

