#!/usr/bin/env bash
set -euo pipefail
mvn -q clean package
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainSimple \
  --lines /opt/sitm-mio/lines-241-ActiveGT.csv \
  --datagrams /opt/sitm-mio/datagrams-MiniPilot.csv \
  --output output/simple.csv
