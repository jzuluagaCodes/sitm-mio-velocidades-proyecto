# SITM-MIO — Versión Distribuida con ZeroC ICE

Implementación del patrón **Master-Worker** sobre el middleware **ZeroC ICE**
para el cálculo distribuido de velocidades promedio por ruta y mes del SITM-MIO.

---

## Arquitectura

```
Dispositivo Central (Master)
├── MainMaster            → orquesta todo el flujo
├── MasterIceCalculator   → crea proxies ICE, particiona dataset, consolida resultados
├── CalculadoraPrx        → proxy ICE generado por slice2java
├── ParticionadorCSV      → reparte filas entre Workers
└── ResultWriter          → escribe el CSV de salida final

Worker1..N (cada nodo)
├── MainWorker            → punto de entrada del Worker
├── WorkerServerIce       → levanta el ObjectAdapter ICE
├── CalculadoraImpl       → implementa la interfaz ICE
│   └── ThreadPool interno → procesa su partición con AggregationTask
```

---

## Prerequisitos

- Java 11+
- Maven 3.8+
- ZeroC ICE 3.7 instalado (para `slice2java`)
  - Windows: https://zeroc.com/downloads/ice
  - Linux: `apt install zeroc-ice-all-dev` o desde el repositorio oficial

---

## Compilar

```bash
# Desde la carpeta distribuido-ice/
mvn clean package
```

El fat-jar queda en `target/sitm-mio-distribuido-ice-1.0.0.jar`.

> **Nota sobre slice2java:** La interfaz `Calculadora.ice` debe compilarse con
> `slice2java` para generar `CalculadoraPrx`, `CalculadoraPrxHelper` y la clase
> base `Calculadora`. El plugin Maven de ICE (`zeroc:ice-maven-plugin`) lo hace
> automáticamente durante `mvn package` si ICE está instalado.

---

## Ejecutar

### Paso 1 — Levantar los Workers (una terminal por Worker)

```bash
# Worker 1 en puerto 10001 con 4 hilos internos
scripts/run_worker.sh 10001 4

# Worker 2 en puerto 10002
scripts/run_worker.sh 10002 4

# Worker 3 en puerto 10003
scripts/run_worker.sh 10003 4

# Worker 4 en puerto 10004
scripts/run_worker.sh 10004 4
```

### Paso 2 — Ejecutar el Master (otra terminal)

```bash
scripts/run_master.sh
```

El resultado queda en `output/distribuido.csv`.

---

## Argumentos del Master

| Argumento | Default | Descripción |
|-----------|---------|-------------|
| `--lines` | `data/sample/lines-241-ActiveGT.csv` | CSV de rutas activas |
| `--datagrams` | `data/sample/datagrams-MiniPilot.csv` | CSV de datagramas |
| `--output` | `output/distribuido.csv` | Ruta del archivo de salida |
| `--workers` | (obligatorio) | Lista `host:port` separada por comas |

---

## Argumentos del Worker

| Argumento | Default | Descripción |
|-----------|---------|-------------|
| `--port` | `10000` | Puerto TCP donde escucha |
| `--threads` | núcleos disponibles | Hilos del ThreadPool interno |

---

## Salida esperada

```
route_id,year_month,avg_speed_kmh,record_count
A101,2023-01,28.4312,1543
A101,2023-02,27.9801,1489
...
```
