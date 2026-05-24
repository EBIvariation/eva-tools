# European Variation Archive (EVA) Tools [![Build Status](https://travis-ci.com/EBIvariation/eva-tools.svg?branch=develop)](https://travis-ci.com/EBIvariation/eva-tools)

This repository contains tools compatible with the European Variation Archive infrastructure. Please checkout
[the ETL pipeline repository](https://github.com/EBIvariation/eva-pipeline) to load data these tools can consume, and
[the REST web services API repository](https://github.com/EBIvariation/eva-ws) to retrieve information conveniently.

## Modules

### Java Modules

#### vcf-dumper
Web service and CLI that support queries from the EVA REST API and write the output in VCF format, for later consumption by other applications. The web service module produces a WAR deployable on Apache Tomcat; the CLI module dumps entire studies. Supports filtering by variant properties such as annotations, MAF, SIFT, and PolyPhen scores.

#### database-migration
MongoDB schema migration tool built on MongoBee. Keeps track of which migrations were applied to each database, allowing incremental updates of the variation database schema.

#### dbsnp-importer
Imports dbSNP variant data into the EVA MongoDB databases.

---

### Python Modules

Each Python module has its own `pyproject.toml` and can be installed independently with `pip install <module-directory>/`.

#### eva-stats
Collects variant and submission statistics from the accessioning warehouse (MongoDB) and stores them in PostgreSQL. Provides two scripts: `stats-accessioning` for RS/SS ID counts per assembly, and `stats-variant-warehouse` for per-project counts.

#### eva-usage-stats
Analyzes EVA web service and FTP usage patterns from Kibana logs. Enriches query records with geolocation data and produces summaries of API endpoint usage and query parameters. Includes scripts: `ftp-usage`, `ws-query-analysis`, `ws-query-fill-in-location`, `summarise-endpoints`, and `summarise-query-params`.

#### evapro-refresh
Automates refresh of EVA staging/testing databases from production snapshots via the Delphix data virtualization API. Manages snapshot creation, replication profiles, and integrates with GitLab to trigger downstream pipelines.

#### accession-monitoring
Monitors MongoDB accession collections for duplicate RS or SS IDs. Exports accessions, detects duplicates, and sends email notifications to configured recipients. Can be run standalone or via the included Nextflow workflow.

#### eva-cli-usage-stats
Queries PostgreSQL for EVA CLI call-home events and produces weekly usage reports as CSV files and matplotlib charts. Tracks runs per week, task types, exception rates, and validation error rates.

#### update_sc_name
Updates taxonomy scientific names in the EVA PostgreSQL metadata database (`evapro.taxonomy`) and reorganizes the corresponding reference sequence directories on disk, creating symlinks for backward compatibility.

#### vcf-release-benchmarking
Benchmarks VCF release query performance against MongoDB using different lookup strategies. Runs `perf stat` over a configurable number of iterations and reports timing for each strategy.

---

### Bash / Script Modules

#### slurm-scripts
Wrapper scripts (`eva-sbatch`, `eva-srun`) for submitting jobs to SLURM HPC clusters with EVA-specific defaults.

#### deployment_scripts
Template shell scripts for deploying Python modules from a GitHub tag or branch, either into an existing directory (`deploy_python_project_as_dir.sh`) or inside a dedicated virtual environment (`deploy_python_project_in_venv.sh`).
