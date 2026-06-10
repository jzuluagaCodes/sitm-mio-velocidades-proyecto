@echo off
REM Levanta un Worker ICE.
REM Uso: run_worker.bat <puerto> [hilos]
REM Ejemplo: run_worker.bat 10001 4
IF "%1"=="" (
    echo ERROR: Debes indicar el puerto. Ejemplo: run_worker.bat 10001
    exit /b 1
)
SET PORT=%1
SET THREADS=%2
IF "%THREADS%"=="" SET THREADS=4
echo [Worker] Iniciando en puerto %PORT% con %THREADS% hilos...
java -cp ..\target$JAR edu.icesi.sitmmio.app.MainWorker --port %PORT% --threads %THREADS%
