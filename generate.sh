#!/bin/sh
export JAVA_OPTS="-Xmx8g"
if [ "$#" -ne 1 ]; then
  echo "No data directory provided, usage: ./generate.sh <DATA_DIR>"
  exit 1;
fi
kotlinc -script ./src/generate.main.kts "$@"