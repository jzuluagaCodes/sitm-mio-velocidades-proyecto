@echo off
REM Ejecuta el Master con 4 Workers locales en puertos 10001-10004.
REM Ajusta --workers si los Workers estan en maquinas distintas.
java -Xmx4g -jar ..\target$JAR ^
  --lines ..$DATA_LINES ^
  --datagrams ..$DATA_DGRAMS ^
  --output ..\output\distribuido.csv ^
  --workers localhost:10001,localhost:10002,localhost:10003,localhost:10004
