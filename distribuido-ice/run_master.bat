@echo off
echo [Master] Iniciando el Orquestador Central de Velocidades...

java -Xmx4g -jar target/sitm-mio-master-1.0.0.jar ^
  --lines data/sample/lines-241-ActiveGT.csv ^
  --datagrams data/sample/datagrams-MiniPilot.csv ^
  --output output/distribuido.csv ^
  --workers localhost:10001,localhost:10002,localhost:10003,localhost:10004

echo [Master] Procesamiento finalizado con exito.
pause