#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/domain"
liquibase --url="jdbc:postgresql://localhost:5432/hemis_db" --username=postgres --password=postgres --changeLogFile=src/main/resources/db/changelog/db.changelog-master.yaml --searchPath=src/main/resources,src/main/resources/db/changelog,src/main/resources/db/changelog/changesets status --verbose
