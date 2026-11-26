import duckdb
import pytest

from eva_usage_metrics.summary.files import release_species_sql, study_sql


@pytest.mark.parametrize('path, release_species, study', [
    ('/pub/databases/eva/rs_releases/release_5/by_species/homo_sapiens/GRCh38/x.vcf.gz', 'release_5/homo_sapiens',
     None),
    ('/pub/databases/eva/rs_releases/release_4/by_assembly/GCA_000001405.15/x.vcf.gz',
     'release_4/GCA_000001405.15', None),
    ('/pub/databases/eva/rs_releases/release_4/README.txt', 'release_4/unknown', None),
    ('/pub/databases/eva/PRJEB97324/chr22.vcf.csi', None, 'PRJEB97324'),
    ('/pub/databases/eva/PRJEB97324', None, 'PRJEB97324'),
    ('/pub/databases/eva/Association', None, None),
    (None, None, None),
])
def test_file_path_derived_columns(path, release_species, study):
    result = duckdb.execute(
        f'SELECT {release_species_sql("path")}, {study_sql("path")} FROM (SELECT CAST(? AS VARCHAR) AS path)',
        [path]
    ).fetchone()
    assert result == (release_species, study)
