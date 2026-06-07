# Proyecto final ISW4 - Velocidades promedio SITM-MIO

El proyecto calcula la velocidad promedio por ruta y por mes para las rutas activas del piloto. Incluye tres versiones:

1. **Monolito simple**: lectura, filtrado y agregación en un solo flujo.
2. **Monolito concurrente con hilos**: particiona los datagramas y usa `ExecutorService`.
3. **Distribuido Master-Worker**: un maestro reparte particiones a workers por sockets.

## Requisitos

- Java 11 o superior
- Maven 3.6 o superior

## Estructura

```text
src/main/java/edu/icesi/sitmmio
├── app                 Entradas MainSimple, MainThreads, MainDistributedMaster y MainDistributedWorker
├── core                Cálculo, agregación y escritura de resultados
├── csv                 Lector CSV, lector de rutas activas y mapeo de datagramas
├── concurrent          Particionador y tareas concurrentes
├── distributed         Implementación Master-Worker
├── domain              Clases de dominio
└── util                Utilidades de encabezados y fechas
```

## Compilar

```bash
mvn clean package
```

Genera el archivo `target/sitm-mio-velocidades-1.0.0.jar`.

---

## Ejecutar monolito simple

**Linux / Mac:**
```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainSimple \
  --lines data/sample/lines-241-ActiveGT.csv \
  --datagrams data/sample/datagrams-MiniPilot.csv \
  --output output/simple.csv
```

**Windows (PowerShell):**
```powershell
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainSimple --lines data/sample/lines-241-ActiveGT.csv --datagrams data/sample/datagrams-MiniPilot.csv --output output/simple.csv
```

> Para usar el dataset completo reemplazar `datagrams-MiniPilot.csv` por la ruta a `datagrams4Pilot.csv` en `/opt/sitm-mio/`.

---

## Ejecutar monolito concurrente con hilos

**Linux / Mac:**
```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainThreads \
  --lines data/sample/lines-241-ActiveGT.csv \
  --datagrams data/sample/datagrams-MiniPilot.csv \
  --output output/hilos.csv \
  --threads 4
```

**Windows (PowerShell):**
```powershell
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainThreads --lines data/sample/lines-241-ActiveGT.csv --datagrams data/sample/datagrams-MiniPilot.csv --output output/hilos.csv --threads 4
```

> El parámetro `--threads` es opcional. Por defecto usa el número de núcleos disponibles del sistema.

---

## Ejecutar distribuido Master-Worker

Abrir **terminales separadas** en la carpeta del proyecto.

**Terminal 1 — Worker 1:**

Linux/Mac:
```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedWorker --port 9090
```
Windows:
```powershell
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedWorker --port 9090
```

**Terminal 2 — Worker 2:**

Linux/Mac:
```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedWorker --port 9091
```
Windows:
```powershell
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedWorker --port 9091
```

**Terminal 3 — Maestro:**

Linux/Mac:
```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedMaster \
  --lines data/sample/lines-241-ActiveGT.csv \
  --datagrams data/sample/datagrams-MiniPilot.csv \
  --output output/distribuido.csv \
  --workers localhost:9090,localhost:9091
```
Windows:
```powershell
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedMaster --lines data/sample/lines-241-ActiveGT.csv --datagrams data/sample/datagrams-MiniPilot.csv --output output/distribuido.csv --workers localhost:9090,localhost:9091
```

> Se puede agregar más workers repitiendo el parámetro con puertos adicionales, por ejemplo: `--workers localhost:9090,localhost:9091,localhost:9092`

---

## Salida

El CSV de salida tiene el siguiente formato:

```text
ruta,mes,velocidad_promedio,cantidad_registros
P10,2025-01,28.000000,2
```

Los archivos generados quedan en la carpeta `output/`.

---

## Nota importante sobre columnas del CSV

El código detecta automáticamente nombres de columnas comunes en español e inglés para ruta, fecha y velocidad. Si el CSV del curso usa un nombre de columna diferente, solo se ajustan las listas de candidatos en `DatagramMapper` y `ActiveRoutesReader`.
