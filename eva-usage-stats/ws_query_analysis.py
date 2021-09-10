#!/usr/bin/python
import datetime
import json
import os
import traceback
from argparse import ArgumentParser
from urllib.parse import unquote

import requests

from ebi_eva_common_pyutils.metadata_utils import get_metadata_connection_handle


def main():
    parser = ArgumentParser(description='Retrieves data from Kibana (tracker for Web Service requests) and dumps into '
                                        'a local postgres instance for analysis')
    parser.add_argument('--kibana-host', help='Kibana host to query, e.g. http://example.ebi.ac.uk:9200', required=True)
    parser.add_argument("--private-config-xml-file", help="ex: /path/to/eva-maven-settings.xml", required=True)
    args = parser.parse_args()
    print("Job ran at " + str(datetime.datetime.now()))

    postgres_conn_handle = get_metadata_connection_handle("development", args.private_config_xml_file)
    result_cursor = postgres_conn_handle.cursor()

    try:
        most_recent_timestamp = None
        result_cursor.execute("select max(event_ts_txt) as recent_ts from eva_web_srvc_stats.ws_traffic;")
        results = result_cursor.fetchall()
        if results:
            if results[0][0]:
                most_recent_timestamp = results[0][0]

        result_chunk_size = 1000
        logstash_url_template = os.path.join(args.kibana_host, "_search?ignore_unavailable=true&size={0}&from={1}")
        query_conditions = [{"query_string": {"query": "request_uri_path:(\"/eva/webservices/*\")"}}]
        if most_recent_timestamp:
            query_conditions.append({"range": {"@timestamp": {"gt": most_recent_timestamp}}})
        post_query = {
            "query": {
                "bool": {
                    "must": query_conditions
                }
            }
        }

        result_size_offset = 0
        while True:
            logstash_url = logstash_url_template.format(result_chunk_size, result_size_offset)
            response = requests.post(logstash_url, json.dumps(post_query))
            result_set = json.loads(response.content)["hits"]["hits"]
            cur_result_size = len(result_set)
            if cur_result_size == 0:
                break
            print("Inserting {0} records into Postgres".format(cur_result_size))
            for result in result_set:
                data = result["_source"]
                tot_segment_length = 0
                data["request_uri_path"] = unquote(data["request_uri_path"])
                if "/segments/" in data["request_uri_path"]:
                    try:
                        segments = data["request_uri_path"].split("/segments/")[1].split("/variants")[0].split(",")
                        for segment in segments:
                            segment = segment.strip()
                            if segment:
                                segment_lbub = segment.split(":")[1].split("-")
                                segment_length = float(segment_lbub[1]) - float(segment_lbub[0])
                                if segment_length > 0:
                                    tot_segment_length += segment_length
                    except Exception:
                        pass
                result_cursor.execute("insert into eva_web_srvc_stats.ws_traffic values (%s, %s,%s,%s,%s,%s,%s,%s,%s, "
                                     "cast(%s as timestamp with time zone),%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);",
                                     (data["@timestamp"], data["@timestamp"], data["type"],
                                    data["host"],
                                    data["path"],
                                    data["syslog_pri"],
                                    data["syslog_timestamp"],
                                    data["syslog_hostname"],
                                    data["remote_host"],
                                    data["request_timestamp"],
                                    data["client"],
                                    data["bytes_out"],
                                    data["bytes_in"],
                                    data["duration"],
                                    data["pool_name"],
                                    data["server_node"],
                                    data["user_agent"],
                                    data["request_type"],
                                    data["http_status"] if "http_status" in data else '',
                                    data["is_https"],
                                    data["virtual_host"],
                                    data["request_uri_path"],
                                    data["request_query"] if "request_query" in data else '',
                                    data["cookie_header"] if "cookie_header" in data else '', tot_segment_length))
            postgres_conn_handle.commit()
            if cur_result_size < result_chunk_size:
                break
            result_size_offset += result_chunk_size

    except Exception:
        traceback.print_exc()
    finally:
        result_cursor.close()
        postgres_conn_handle.close()


if __name__ == '__main__':
    main()
