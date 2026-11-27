"""
Location of an IP address, used when neometer did not provide one. The records cannot be updated once archived, so
the location has to be found when they are collected.
"""
from functools import lru_cache

import requests
from requests import RequestException
from retry import retry


@retry(exceptions=RequestException, tries=5, delay=8, backoff=1.2, jitter=(1, 3))
def _get_location(ip_address):
    response = requests.get('https://geolocation-db.com/json/' + ip_address, timeout=30)
    response.raise_for_status()
    return response.json()


@lru_cache(maxsize=None)
def get_location(ip_address):
    """
    Return a dict with country_code, city, state, latitude and longitude, empty when the location is unknown.
    """
    if not ip_address:
        return {}
    try:
        location = _get_location(ip_address)
    except RequestException:
        return {}
    # The service reports unknown values as 'Not found'
    return {key: value for key, value in location.items() if value not in ('Not found', '')}
