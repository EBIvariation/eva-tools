import pytest

from eva_usage_metrics.archive.endpoint_normalisation import normalise_path


@pytest.mark.parametrize('path, endpoint', [
    ('/eva/webservices/contig-alias/v1/chromosomes/md5checksum/7b6e06758e53927330346e9e7cc00cce',
     '/eva/webservices/contig-alias/v1/chromosomes/md5checksum/{md5}'),
    ('/eva/webservices/rest/v1/segments/1:100-200/variants', '/eva/webservices/rest/v1/segments/{region}/variants'),
    ('/eva/webservices/release/v2/info', '/eva/webservices/release/v2/info'),
])
def test_normalise_path(path, endpoint):
    assert normalise_path(path) == endpoint
