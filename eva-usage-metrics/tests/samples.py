"""Documents of the neometer data streams, trimmed from real records."""
GEO = {'geo.country_iso_code': 'US', 'geo.region_name': 'Maryland', 'geo.city_name': 'Gaithersburg'}

FTP_FAILURE = {
    '_index': '.ds-logs-transfer.ftp-default-2026.11.10-000001', '_id': 'ftp1',
    '_source': {
        'ftp': {'transfer': {'duration_secs': 1, 'type': 'b'}, 'access': {'mode': 'a'}}, **GEO,
        'network': {'direction': 'egress'}, '@timestamp': '2026-11-10T03:27:46.000Z',
        'file': {'path': '/pub/databases/eva/Association', 'size': 0},
        'data_stream': {'dataset': 'transfer.ftp'}, 'client': {'hash': 'iNu6'}, 'event': {'outcome': 'failure'},
        'user': {},
    },
}

HTTP_PARTIAL = {
    '_index': '.ds-logs-transfer.http-default-2026.11.19-000001', '_id': 'http1',
    '_source': {
        **GEO, 'url': {'original': '/pub/databases/eva/PRJEB97324/chr22.vcf.csi'},
        '@timestamp': '2026-11-19T08:06:07.000Z', 'data_stream': {'dataset': 'transfer.http'},
        'client': {'hash': '4fx'},
        'http': {'request': {'method': 'GET'}, 'response': {'status_code': '206', 'body': {'bytes': 8981}}},
        'user': {},
    },
}

GLOBUS = {
    '_index': '.ds-logs-transfer.globus-default-2026.11.19-000001', '_id': 'globus1',
    '_source': {
        'network': {'direction': 'egress'}, '@timestamp': '2026-11-19T08:18:21.813Z',
        'file': {'path': '/xfer/public/pride/x.mzXML.gz', 'size': 67108864},
        'data_stream': {'dataset': 'transfer.globus'}, 'client': {'hash': 'CdG'},
        'event': {'start': '2026-11-19T08:11:00.694Z', 'end': '2026-11-19T08:18:21.813Z', 'outcome': 'success'},
        'user': {'name': 'globus-ebi'},
    },
}

WEB_SERVICE = {
    '_index': '.ds-logs-web-realtime-2026.11.25-000001', '_id': 'ws1',
    '_source': {
        **GEO, 'geo.location': [-77.2, 39.14], '@timestamp': '2026-11-25T10:00:00.000Z',
        'client': {'ip': '192.0.2.10'}, 'destination': {'address': 'wp-p1m-01:8080'},
        'url': {'path': '/eva/webservices/rest/v1/variants/rs123', 'path_base': '/eva/webservices/rest/v1/variants/rs123',
                'query': 'species=hsapiens_grch38', 'domain': 'www.ebi.ac.uk'},
        'http': {'request': {'method': 'GET'}, 'response': {'status_code': 200, 'bytes': '1234',
                                                            'time_to_first_byte': 0.25}},
        'user_agent': {'original': 'curl/8.0'},
    },
}
