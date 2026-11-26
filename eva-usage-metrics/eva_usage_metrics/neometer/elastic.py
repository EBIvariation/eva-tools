"""
Access to the neometer Elasticsearch data streams and to the fields of their documents.
"""
from datetime import timedelta

import requests
from ebi_eva_common_pyutils.logger import logging_config
from ebi_eva_internal_pyutils.config_utils import get_properties_from_xml_file
from requests.auth import HTTPBasicAuth
from retry import retry

logger = logging_config.get_logger(__name__)

SCROLL_DURATION = '10m'


def get_field(source, field_name):
    """
    Retrieve a dotted field (e.g. 'file.path') from a document source whether it is stored flat or nested.
    """
    if field_name in source:
        return source[field_name]
    prefix, _, remainder = field_name.partition('.')
    if remainder and isinstance(source.get(prefix), dict):
        return get_field(source[prefix], remainder)
    return None


def get_first_field(source, *field_names):
    """
    Retrieve the first of the fields present in the document source. Unlike chaining with `or`, falsy values such as
    a file size of 0 are kept.
    """
    for field_name in field_names:
        value = get_field(source, field_name)
        if value is not None:
            return value
    return None


class ElasticClient:

    def __init__(self, elastic_url, basic_auth, batch_size=10000):
        self.elastic_url = elastic_url.rstrip('/')
        self.basic_auth = basic_auth
        self.batch_size = batch_size

    @classmethod
    def from_config(cls, profile, private_config_xml_file, batch_size=10000):
        properties = get_properties_from_xml_file(profile, private_config_xml_file)
        basic_auth = HTTPBasicAuth(properties['ebi.neometer.username'], properties['ebi.neometer.password'])
        return cls(properties['ebi.neometer.elastic.url'], basic_auth, batch_size)

    @retry(tries=4, delay=2, backoff=1.2, jitter=(1, 3))
    def _post(self, path, body):
        response = requests.post(f'{self.elastic_url}/{path}', auth=self.basic_auth, json=body)
        response.raise_for_status()
        return response.json()

    def _clear_scroll(self, scroll_id):
        try:
            requests.delete(f'{self.elastic_url}/_search/scroll', auth=self.basic_auth,
                            json={'scroll_id': scroll_id}).raise_for_status()
        except requests.RequestException as error:
            # The scroll expires on its own: failing to clear it is not a reason to fail the collection
            logger.warning(f'Could not clear the scroll: {error}')

    def iter_day(self, data_stream, filters, day):
        """
        Yield every document of the data stream matching the filters with a @timestamp within the UTC day.
        """
        day_filter = {'range': {'@timestamp': {
            'gte': f'{day.isoformat()}T00:00:00Z', 'lt': f'{(day + timedelta(days=1)).isoformat()}T00:00:00Z'
        }}}
        body = {
            'size': self.batch_size,
            'track_total_hits': True,
            'sort': ['_doc'],
            'query': {'bool': {'filter': list(filters) + [day_filter]}},
        }
        data = self._post(f'{data_stream}/_search?scroll={SCROLL_DURATION}', body)
        scroll_id = data.get('_scroll_id')
        try:
            hits = data['hits']['hits']
            while hits:
                yield from hits
                data = self._post('_search/scroll', {'scroll': SCROLL_DURATION, 'scroll_id': scroll_id})
                scroll_id = data.get('_scroll_id', scroll_id)
                hits = data['hits']['hits']
        finally:
            if scroll_id:
                self._clear_scroll(scroll_id)
