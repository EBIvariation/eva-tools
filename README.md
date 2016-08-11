# European Variation Archive (EVA) Tools [![Build Status](https://travis-ci.org/EBIvariation/eva-tools.svg)](https://travis-ci.org/EBIvariation/eva-tools)

This repository contains tools compatible with the infrastructure of the European Variation Archive pipeline. If you are 
looking for the production source code, please check https://github.com/EBIvariation/eva-ws for the REST web services 
API, and the `master` branch of https://github.com/EBIvariation/eva-pipeline for the ETL.

The only tool currently included in this project is the VCF dumper, which supports some of the queries from the REST web 
services API and writes the output in VCF format, for later consumption by other applications.

