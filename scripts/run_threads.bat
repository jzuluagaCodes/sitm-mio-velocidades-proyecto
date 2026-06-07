@echo off
echo Compilando el proyecto...
call mvn -q clean package
if %ERRORLEVEL% neq 0 (
    echo Error al compilar el proyecto.
    pause
    exit /b %ERRORLEVEL%
)

echo Ejecutando monolito concurrente con 4 hilos...
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainThreads ^
  --lines data/sample/lines-241-ActiveGT.csv ^
  --datagrams data/sample/datagrams-MiniPilot.csv ^
  --output output/hilos.csv ^
  --threads 4

echo Proceso terminado.
pause
