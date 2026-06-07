# Drivers de arquitectura - Escenarios QAW

## Escenario 1 - Rendimiento

**Fuente:** estudiante/usuario que ejecuta el experimento.  
**Estímulo:** se ejecuta el cálculo con `datagrams4Pilot.csv`, que tiene más datos que el piloto mini.  
**Ambiente:** ejecución normal en el laboratorio o computador asignado.  
**Artefacto:** módulo de cálculo de velocidades promedio.  
**Respuesta:** el sistema procesa todos los registros válidos y genera el CSV final.  
**Medida:** la versión concurrente debe reducir el tiempo frente al monolito simple cuando el volumen de datos supera el punto de equilibrio.

## Escenario 2 - Correctitud

**Fuente:** profesor/evaluador.  
**Estímulo:** se comparan los resultados entre monolito simple, hilos y distribuido.  
**Ambiente:** mismos archivos de entrada.  
**Artefacto:** agregador de velocidades.  
**Respuesta:** las tres versiones producen los mismos promedios por ruta y mes.  
**Medida:** diferencia máxima aceptada: 0.000001 por efecto de redondeo.

## Escenario 3 - Escalabilidad

**Fuente:** aumento de datos.  
**Estímulo:** se incrementa el tamaño de entrada desde MiniPilot hasta datagrams4Pilot y luego copias ampliadas para experimento.  
**Ambiente:** ejecución con 1, 2, 4 y más workers/hilos.  
**Artefacto:** particionador y calculadora.  
**Respuesta:** el sistema divide el trabajo en particiones independientes.  
**Medida:** el tiempo no debe crecer linealmente al mismo ritmo del tamaño de datos cuando se agregan recursos.

## Escenario 4 - Disponibilidad en distribuido

**Fuente:** falla de worker.  
**Estímulo:** un worker no está disponible o el puerto no responde.  
**Ambiente:** ejecución distribuida.  
**Artefacto:** maestro Master-Worker.  
**Respuesta:** el maestro reporta claramente el error de conexión.  
**Medida:** el sistema no debe generar una salida incompleta como si fuera correcta.

## Escenario 5 - Modificabilidad

**Fuente:** cambio en nombres de columnas del CSV.  
**Estímulo:** el diccionario de datos usa un nombre diferente para ruta, fecha o velocidad.  
**Ambiente:** mantenimiento del código.  
**Artefacto:** `DatagramMapper` y `ActiveRoutesReader`.  
**Respuesta:** se ajustan únicamente las listas de columnas candidatas.  
**Medida:** no se modifica la lógica de agregación ni la lógica concurrente/distribuida.
