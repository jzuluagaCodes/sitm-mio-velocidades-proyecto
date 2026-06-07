# Proyecto final ISW4 - Velocidades promedio SITM-MIO

El proyecto calcula la velocidad promedio por ruta y por mes para las rutas activas del piloto. Incluye tres versiones:

1. **Monolito simple**: lectura, filtrado y agregación en un solo flujo.
2. **Monolito concurrente con hilos**: particiona los datagramas y usa `ExecutorService`.
3. **Distribuido Master-Worker**: un maestro reparte particiones a workers por sockets.

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

## Ejecutar monolito simple

```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainSimple \
  --lines /opt/sitm-mio/lines-241-ActiveGT.csv \
  --datagrams /opt/sitm-mio/datagrams-MiniPilot.csv \
  --output output/simple.csv
```

## Ejecutar monolito concurrente

```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainThreads \
  --lines /opt/sitm-mio/lines-241-ActiveGT.csv \
  --datagrams /opt/sitm-mio/datagrams4Pilot.csv \
  --output output/hilos.csv \
  --threads 4
```

## Ejecutar distribuido

En terminales diferentes:

```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedWorker --port 9090
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedWorker --port 9091
```

Luego el maestro:

```bash
java -cp target/sitm-mio-velocidades-1.0.0.jar edu.icesi.sitmmio.app.MainDistributedMaster \
  --lines /opt/sitm-mio/lines-241-ActiveGT.csv \
  --datagrams /opt/sitm-mio/datagrams4Pilot.csv \
  --output output/distribuido.csv \
  --workers localhost:9090,localhost:9091
```

## Salida

El CSV final tiene este formato:

```text
ruta,mes,velocidad_promedio,cantidad_registros
P10,2025-01,28.000000,2
```

## Nota importante sobre columnas

El código detecta nombres de columnas comunes en español e inglés para ruta, fecha y velocidad. Si el diccionario del curso usa un nombre muy diferente, solo se ajustan las listas de candidatos en `DatagramMapper` y `ActiveRoutesReader`.
