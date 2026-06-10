#!/bin/bash
# Ejecuta el Master con 4 Workers locales en puertos 10001-10004.
# Ajusta --workers si los Workers están en máquinas distintas.
JAR="../target/sitm-mio-distribuido-ice-1.0.0.jar"
java -jar "$JAR" \
  --lines ../data/sample/lines-241-ActiveGT.csv \
  --datagrams ../data/sample/datagrams-MiniPilot.csv \
  --output ../output/distribuido.csv \
  --workers localhost:10001,localhost:10002,localhost:10003,localhost:10004
