# European Variation Archive (EVA) Tools [![Build Status](https://travis-ci.org/EBIvariation/eva-tools.svg)](https://travis-ci.org/EBIvariation/eva-tools)

This repository contains tools compatible with the European Variation Archive infrastructure. Please checkout
[the ETL pipeline repository](https://github.com/EBIvariation/eva-pipeline) to load data these tools can consume, and
[the REST web services API repository](https://github.com/EBIvariation/eva-ws) to retrieve information conveniently.

The first tool currently included in this project is a VCF dumper, a web service which supports some of the queries
from the core REST web services API and writes the output in VCF format, for later consumption by other applications.

The second tool allows to migrate MongoDB variation databases to the latest schema version, keeping track of
which migrations were applied to each database.
