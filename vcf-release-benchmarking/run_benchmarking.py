import argparse
import os

parser = argparse.ArgumentParser(description='Run benchmarking for VCF release')
parser.add_argument('-u', '--mongo-user', help='MongoDB username', required=True)
parser.add_argument('-p', '--mongo-password', help='MongoDB password', required=True)
parser.add_argument('-m', '--mongo-host', help='MongoDB host', required=True)
parser.add_argument('-d', '--authdb', default='admin', help='MongoDB authentication database')
parser.add_argument('-a', '--assembly', help='Assembly of the species to be used for benchmarking', required=True)
parser.add_argument('-n', '--num-runs', type=int, default=5, help='Number of script runs')

args=parser.parse_args()
command_to_run = """module load mongo && perf stat -r %d mongo -u %s -p %s --authenticationDatabase %s --host %s --eval "var assemblyToUse='%s';" benchmarking_strategies.js 2>&1""" % (args.num_runs, args.mongo_user, args.mongo_password, args.authdb, args.mongo_host, args.assembly)
os.system(command_to_run)
