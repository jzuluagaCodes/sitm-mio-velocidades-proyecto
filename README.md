# Proyecto Final ISW4 - Velocidades Promedio SITM-MIO
## Versión Distribuida con ZeroC ICE

Este proyecto calcula la velocidad promedio por ruta y por mes para las rutas activas del piloto del MIO en Cali. Esta versión implementa una arquitectura distribuida eficiente mediante el patrón **Master-Worker** utilizando el middleware **ZeroC ICE**.

---

## Requisitos

- Java 11 o superior
- Maven 3.6 o superior
- Conectividad de red (Local, ZeroTier o LAN del laboratorio)

---

## Estructura de Paquetes

```text
src/main/java/edu/icesi/sitmmio
├── app         Entradas principales (MainMaster y MainWorker)
├── concurrent  Particionador de datagramas y Pool de Hilos concurrente (ThreadPool/ExecutorService)
├── core        Cálculo de velocidades, agregación local y escritura de resultados CSV
├── csv         Lector de datagramas, lector de rutas activas y mapeo dinámico de columnas
├── domain      Clases de dominio y llaves de agregación (SpeedRecord, RouteMonthKey, etc.)
├── ice         Implementación del Middleware (CalculadoraImpl, MasterIceCalculator, Serializadores)
└── util        Utilidades de parsing de encabezados y extracción de fechas/meses
```

---

## Compilar y Empaquetar

El proyecto utiliza el plugin `maven-shade-plugin` para compilar la especificación de Slice (`Calculadora.ice`) y generar dos unidades de despliegue (JARs) totalmente independientes con todas sus dependencias embebidas.

```bash
mvn clean package
```

Esto generará en la carpeta `target/` los siguientes ejecutables:

- `target/sitm-mio-master-1.0.0.jar` *(Orquestador Central)*
- `target/sitm-mio-worker-1.0.0.jar` *(Nodo de Computación con Flujo Concurrente)*

---

## Ejecución con Scripts Automatizados

> Ejecutar siempre desde la raíz del proyecto.

### 1. Desplegar Nodos Worker 

Los Workers reciben de forma obligatoria el puerto y opcional el número de hilos concurrentes para su `ThreadPool` interno.

> Si se omite el número de hilos, se utilizarán 4 hilos por defecto.

#### Windows (PowerShell o CMD)

```powershell
# Uso: .\run_worker.bat <puerto> [hilos]
.\run_worker.bat 10001 8
```

#### Linux / macOS / Git Bash

```bash
# Otorgar permisos de ejecución la primera vez
chmod +x run_worker.sh

# Uso: ./run_worker.sh <puerto> [hilos]
./run_worker.sh 10001 8
```

---

### 2. Desplegar Nodo Master 

El Master leerá los datasets locales, particionará los datagramas y los enviará de forma asíncrona (**AMI - Asynchronous Method Invocation**) a la lista de Workers configurados en el script.

#### Configuración previa

Antes de ejecutar, abre el script del Master con un editor de texto y actualiza el parámetro `--workers` con las direcciones IP reales de los Workers disponibles en la red.

Ejemplo:

```text
--workers 10.111.44.14:10001,10.111.44.146:10001
```

#### Windows (PowerShell o CMD)

```powershell
.\run_master.bat
```

#### Linux / macOS / Git Bash

```bash
# Otorgar permisos de ejecución la primera vez
chmod +x run_master.sh

./run_master.sh
```

---

## Monitoreo en Tiempo Real

Cuando el Master distribuya las cargas de datos a la red, cada consola de los Workers imprimirá un mensaje indicando la recepción del paquete y el inicio de su procesamiento concurrente.

```text
[Worker] ¡Paquete recibido! 
```

---

## Salida

Los resultados consolidados se generarán automáticamente en:

```text
output/distribuido.csv
```

Este archivo contendrá las velocidades promedio calculadas por ruta y por mes a partir de los datos procesados de manera distribuida.
