#!/bin/bash
# Levanta un Worker ICE.
# Uso: ./run_worker.sh <puerto> [hilos]
PORT=${1:?"Debes indicar el puerto. Ejemplo: ./run_worker.sh 10001 4"}
THREADS=${2:-4}
JAR="../target/sitm-mio-distribuido-ice-1.0.0.jar"
echo "[Worker] Iniciando en puerto $PORT con $THREADS hilos..."
java -cp "$JAR" edu.icesi.sitmmio.app.MainWorker --port "$PORT" --threads "$THREADS"
