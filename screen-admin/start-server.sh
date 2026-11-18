#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/server"
MAVEN="../.tools/apache-maven-3.9.9/bin/mvn"

if [[ ! -x "$MAVEN" ]]; then
  echo "Maven is missing. Run the project setup first." >&2
  exit 1
fi

exec "$MAVEN" -Dmaven.repo.local=../.m2/repository spring-boot:run
