#!/usr/bin/env bash
set -euo pipefail
mvn -q clean package
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainThreads \
  --lines /opt/sitm-mio/lines-241-ActiveGT.csv \
  --datagrams /opt/sitm-mio/datagrams4Pilot.csv \
  --output output/hilos.csv \
  --threads 4
