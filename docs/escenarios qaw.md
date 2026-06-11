# Drivers de Arquitectura y Escenarios QAW
## Proyecto: Estimación de Velocidad Promedio por Ruta por Mes — SITM-MIO

---

## 1. Identificación y Priorización de Drivers de Arquitectura

El sistema tiene como propósito calcular la velocidad promedio por ruta y por mes a partir
de los datagramas del piloto SITM-MIO, filtrando por las rutas activas definidas en
`lines-241-ActiveGT.csv`. El contexto del problema involucra entre 2.5 y 3 millones de
eventos diarios, con proyección de crecimiento de la flota de buses, lo que hace necesario
que la arquitectura priorice no solo la correctitud del resultado sino también la capacidad
de procesar grandes volúmenes de datos eficientemente.

Los drivers de arquitectura identificados y su orden de prioridad son:

| Prioridad | Driver | Tipo |
|-----------|--------|------|
| 1 | Correctitud | Atributo de calidad funcional |
| 2 | Performance (tiempo de respuesta) | Atributo de calidad no funcional |
| 3 | Escalabilidad | Atributo de calidad no funcional |

### Driver 1 - Correctitud
El sistema debe calcular de forma completa y correcta la velocidad promedio por ruta y por
mes para todas las rutas activas del piloto, produciendo una salida consistente
independientemente de la versión de la arquitectura utilizada (monolítica, concurrente o
distribuida). Este driver es prioritario sobre los demás porque ninguna mejora de
rendimiento tiene valor si los resultados calculados son incorrectos. No requiere escenario
QAW.

### Driver 2 - Performance (tiempo de respuesta)
El sistema debe reducir el tiempo total de procesamiento del archivo de datagramas conforme
aumenta el volumen de datos. Este driver es crítico porque se exige comparar
tiempos entre la solución monolítica y la distribuida, y porque el dataset de producción
involucra millones de registros que el monolito no puede procesar de forma eficiente. La
correctitud tiene prioridad sobre la performance: ninguna optimización de velocidad es
válida si altera los resultados del cálculo.

### Driver 3 - Escalabilidad
La arquitectura debe permitir aumentar la capacidad de procesamiento de forma controlada,
ya sea incrementando el número de hilos en la versión concurrente o el número de nodos en
la versión distribuida, manteniendo siempre la correctitud del resultado y evidenciando
mejoras medibles en el tiempo de ejecución a medida que el dataset crece. La escalabilidad
y la performance están relacionadas: escalar horizontalmente es el mecanismo principal para
mejorar los tiempos de respuesta ante datasets de mayor volumen. Sin embargo, escalar
introduce overhead de comunicación, serialización y sincronización, que puede perjudicar la
performance para datasets pequeños.

---

## 2. Escenarios QAW

### Escenario 1 - Performance

| Campo | Descripción |
|-------|-------------|
| **Proyecto** | Sistema de cálculo de velocidades promedio SITM-MIO |
| **Escenario y contexto** | El equipo de análisis del CCO necesita calcular la velocidad promedio por ruta y por mes para las 241 rutas activas definidas en `lines-241-ActiveGT.csv`, usando el dataset completo `datagrams4Pilot.csv`, que contiene aproximadamente 9 veces más registros que el subconjunto de prueba `datagrams-MiniPilot.csv`. La versión monolítica procesa los registros de forma secuencial en un único hilo sobre la JVM, recorriendo linealmente cada fila del CSV, calculando la velocidad instantánea como distancia entre datagramas consecutivos dividida entre el tiempo transcurrido, y acumulando el promedio por ruta y mes. Este procesamiento secuencial escala linealmente con el volumen de datos: al multiplicar por 9 el tamaño del dataset, el tiempo de ejecución se multiplica proporcionalmente, haciendo inviable el procesamiento dentro de una ventana operativa aceptable para el CCO. |
| **Objetivo del negocio** | Reducir el tiempo de procesamiento del cálculo de velocidades para poder operar con el dataset completo `datagrams4Pilot.csv` de manera eficiente, minimizando el tiempo de espera para obtener el reporte mensual y reduciendo costos operativos asociados al uso prolongado de infraestructura de cómputo. |
| **QA's Relevantes** | Performance, Tiempo de respuesta |
| **Estímulo** | Se lanza el procesamiento completo del archivo `datagrams4Pilot.csv` (todas las rutas activas de `lines-241-ActiveGT.csv`) desde el nodo de procesamiento central. |
| **Fuente de Estímulo** | El equipo de operación del CCO ejecuta el sistema de cálculo de velocidades para generar el reporte mensual de velocidades promedio por ruta. |
| **Artefacto(s)** | Subsistema de procesamiento de velocidades; Componente `LectorCSV` (lectura y parsing de `datagrams4Pilot.csv`); Componente `ParticionadorCSV` (división del dataset en subconjuntos independientes); `ThreadPool` de Workers de cálculo concurrente; Componente `Calculadora` (cálculo de velocidad promedio por partición); Componente `AggregatorResultados` (consolidación de resultados parciales en mapa final). |
| **Entorno** | Sistema en ejecución batch sobre servidor del laboratorio (`x104m03`), con el dataset completo `datagrams4Pilot.csv` cargado en disco local, bajo condiciones normales de carga sin procesos concurrentes externos que compitan por CPU o memoria. |
| **Respuesta** | El sistema carga `datagrams4Pilot.csv`, particiona el dataset en N subconjuntos (uno por hilo disponible), procesa cada partición en paralelo mediante el ThreadPool, consolida los resultados parciales en un único mapa ordenado por ruta y mes, y escribe el archivo CSV de salida con todas las velocidades promedio correctamente calculadas, produciendo resultados idénticos a los de la versión monolítica. |
| **Medida de Respuesta** | La versión concurrente con ThreadPool (N hilos igual al número de núcleos disponibles en `x104m03`) debe reducir el tiempo total de ejecución al menos un 30% respecto a la versión monolítica para el archivo `datagrams4Pilot.csv`, manteniendo exactamente los mismos valores calculados. El tiempo se mide desde el inicio de la lectura del CSV hasta la escritura completa del archivo de salida. |
| **Preguntas Relevantes** | ¿A partir de qué tamaño de dataset (número de filas) la versión concurrente supera en rendimiento a la monolítica, dado el overhead de creación y coordinación del ThreadPool? ¿Cuántos hilos resultan óptimos para el hardware disponible en `x104m03`? |
| **Otros Aspectos** | Con `datagrams-MiniPilot.csv` (dataset pequeño) el overhead de crear y coordinar hilos supera los beneficios del paralelismo: la versión monolítica resulta más rápida (~25 ms vs ~40 ms con hilos). Con `datagrams4Pilot.csv` se espera que la ganancia de paralelismo supere ese overhead y la versión con ThreadPool sea significativamente más rápida. |

---

### Escenario 2 — Escalabilidad

| Campo | Descripción |
|-------|-------------|
| **Proyecto** | Sistema de cálculo de velocidades promedio SITM-MIO |
| **Escenario y contexto** | La organización necesita procesar el dataset de producción completo del SITM-MIO a partir del archivo `datagrams4Pilot.csv`, que representa datos históricos de hasta 1300 buses con proyección de crecimiento a 2000, generando entre 2.5 y 3 millones de eventos diarios. La arquitectura debe poder escalar horizontalmente agregando nodos de procesamiento para manejar ese volumen sin colapsar ni comprometer la correctitud de los resultados, superando la limitación de una sola máquina. |
| **Objetivo del negocio** | Garantizar que el sistema pueda crecer junto con el volumen de datos del SITM-MIO sin necesidad de rediseñar la arquitectura, aprovechando múltiples nodos de cómputo disponibles en el laboratorio (`x104m03`, `x205m03`, `x206m03`) para reducir tiempos de procesamiento y costos operativos. |
| **QA's Relevantes** | Escalabilidad, Performance |
| **Estímulo** | Se incrementa el número de Workers en la arquitectura distribuida Master-Worker de 1 a 2 a 3 nodos, procesando el archivo `datagrams4Pilot.csv` con las rutas activas de `lines-241-ActiveGT.csv` en cada configuración. |
| **Fuente de Estímulo** | El arquitecto del sistema agrega nodos de procesamiento adicionales en los servidores del laboratorio para mejorar el tiempo de respuesta ante el dataset completo del piloto. |
| **Artefacto(s)** | Componente Master (orquestador de particiones, nodo `x104m03`); Componente Worker (procesador parcial en nodo remoto, nodos `x205m03` y `x206m03`); Componente `ParticionadorCSV`; Componente Agregador de Resultados parciales; Infraestructura de comunicación ICE sobre TCP/IP. |
| **Entorno** | Sistema desplegado en múltiples nodos del laboratorio (`x104m03`, `x205m03`, `x206m03`), comunicados por red local, con el archivo `datagrams4Pilot.csv` accesible desde el nodo Master, bajo condiciones normales de operación sin tráfico de red externo significativo. |
| **Respuesta** | El Master carga `datagrams4Pilot.csv` y `lines-241-ActiveGT.csv`, distribuye las particiones del dataset entre los Workers disponibles, cada Worker calcula sus resultados parciales de forma independiente sobre su partición, y el Master consolida todos los resultados en el archivo CSV final, manteniendo la correctitud e integridad del cálculo. |
| **Medida de Respuesta** | Al pasar de 1 a 2 Workers el tiempo de ejecución debe reducirse al menos un 30% para `datagrams4Pilot.csv`. Al pasar de 2 a 3 Workers debe haber una reducción adicional de al menos un 15%, manteniendo resultados idénticos a los de la versión monolítica. Se identifica el punto de inflexión cuando agregar un Worker adicional no reduce el tiempo más de un 5%, indicando que el overhead de serialización y comunicación ICE supera la ganancia de paralelismo. |
| **Preguntas Relevantes** | ¿A partir de cuántos Workers el overhead de serialización y comunicación ICE por TCP/IP empieza a superar la ganancia de paralelismo en la red del laboratorio? ¿Cuál es el número óptimo de Workers para `datagrams4Pilot.csv` en la infraestructura disponible? |
| **Otros Aspectos** | Con `datagrams-MiniPilot.csv` el distribuido es el más lento (~173 ms) debido al overhead de sockets ICE y serialización. Con `datagrams4Pilot.csv` se espera que la ventaja del procesamiento distribuido supere ese overhead y los tiempos con múltiples Workers sean menores que el monolito. |

---

## 3. Trade-offs entre Drivers

La arquitectura enfrenta un trade-off fundamental entre simplicidad operativa y capacidad
de procesamiento, directamente derivado de las relaciones entre los drivers identificados.

La correctitud tiene prioridad absoluta sobre performance y escalabilidad: ninguna
optimización de velocidad ni ningún esquema de distribución es válido si produce resultados
incorrectos o distintos a los de la versión monolítica. Esto implica que toda decisión
arquitectónica de distribución debe estar acompañada de un mecanismo de consolidación que
garantice la integridad del resultado final.

Performance y escalabilidad están relacionadas pero no son equivalentes: la escalabilidad
es el mecanismo arquitectónico para alcanzar la performance requerida cuando el volumen de
datos supera la capacidad de procesamiento de un solo nodo. Sin embargo, escalar introduce
overhead de comunicación por red, serialización de objetos y sincronización de resultados,
lo que puede perjudicar la performance para datasets pequeños como `datagrams-MiniPilot.csv`
donde el costo de coordinación supera la ganancia de paralelismo.

La versión monolítica es la más simple de implementar, desplegar y mantener, pero escala
linealmente con el volumen de datos. La versión concurrente con hilos mejora
significativamente la performance para `datagrams4Pilot.csv` sin aumentar la complejidad
del despliegue, pero está limitada a los recursos de una sola máquina. La versión
distribuida Master-Worker con ICE permite escalar horizontalmente hacia múltiples nodos
(`x104m03`, `x205m03`, `x206m03`), satisfaciendo el driver de escalabilidad, pero introduce
la mayor complejidad arquitectónica y solo se justifica cuando el volumen de datos supera
la capacidad del procesamiento en una sola máquina.