"""
Information derived from the path of a transferred file, as DuckDB SQL expressions so they can be computed over a
whole year of records. The patterns follow the ones used in the EVA-2766 species analysis.

    /pub/databases/eva/rs_releases/release_5/by_species/homo_sapiens/GRCh38/...  -> release_5/homo_sapiens
    /pub/databases/eva/rs_releases/release_5/by_assembly/GCA_000001405.15/...    -> release_5/GCA_000001405.15
    /pub/databases/eva/PRJEB97324/chr22.vcf.csi                                  -> study PRJEB97324
"""

RELEASE_PATTERN = r'/rs_releases/(release_[0-9]+)/'
SPECIES_PATTERN = r'/by_species/([^/]+)/'
ASSEMBLY_PATTERN = r'(GCA_[0-9]{9}\.[0-9]+)'
STUDY_PATTERN = r'/(PRJ[A-Z]{2}[0-9]+)(/|$)'


def _extract(column, pattern):
    return f"nullif(regexp_extract({column}, '{pattern}', 1), '')"


def release_species_sql(column):
    """'<release>/<species or assembly>' for the files of the RS releases, NULL for other files."""
    release = _extract(column, RELEASE_PATTERN)
    species = f"coalesce({_extract(column, SPECIES_PATTERN)}, {_extract(column, ASSEMBLY_PATTERN)}, 'unknown')"
    return f"CASE WHEN {release} IS NOT NULL THEN {release} || '/' || {species} END"


def study_sql(column):
    """Study (project) accession of the files submitted to EVA, NULL for other files."""
    return _extract(column, STUDY_PATTERN)
