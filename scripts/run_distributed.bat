@echo off
echo Compilando el proyecto...
call mvn -q clean package
if %ERRORLEVEL% neq 0 (
    echo Error al compilar el proyecto.
    pause
    exit /b %ERRORLEVEL%
)

echo Iniciando Workers en ventanas independientes...
start "Worker 9090" java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedWorker --port 9090
start "Worker 9091" java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedWorker --port 9091

echo Esperando 2 segundos a que los workers inicien...
ping -n 3 127.0.0.1 > nul

echo Ejecutando Master distribuido...
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedMaster ^
  --lines data/sample/lines-241-ActiveGT.csv ^
  --datagrams data/sample/datagrams-MiniPilot.csv ^
  --output output/distribuido.csv ^
  --workers localhost:9090,localhost:9091

echo.
echo Proceso terminado. Puedes cerrar las ventanas de los Workers.
pause
