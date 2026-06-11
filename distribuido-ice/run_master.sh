#!/bin/bash
# Ejecuta el Master con 4 Workers locales en puertos 10001-10004.
# Ajusta --workers si los Workers estan en maquinas distintas.

echo "[Master] Iniciando el Orquestador Central de Velocidades (Linux/Bash)..."

java -Xmx4g -jar target/sitm-mio-master-1.0.0.jar \
  --lines data/sample/lines-241-ActiveGT.csv \
  --datagrams data/sample/datagrams-MiniPilot.csv \
  --output output/distribuido.csv \
  --workers localhost:10001,localhost:10002,localhost:10003,localhost:10004

echo "[Master] Procesamiento finalizado con exito."