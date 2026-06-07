# Documento de resultados del experimento

## Objetivo

Comparar tres alternativas de arquitectura para calcular velocidades promedio por ruta por mes:

1. Monolito simple.
2. Monolito concurrente con hilos.
3. Solución distribuida con patrón Master-Worker.

## Métrica principal

Tiempo total de ejecución en milisegundos, medido desde antes del cálculo hasta después de generar la estructura final de resultados. También se valida que las salidas sean equivalentes.

## Procedimiento propuesto

1. Compilar el proyecto con `mvn clean package`.
2. Ejecutar el monolito simple con `datagrams-MiniPilot.csv`.
3. Ejecutar el monolito concurrente con 2, 4 y 8 hilos.
4. Ejecutar las mismas pruebas con `datagrams4Pilot.csv`.
5. Para probar el punto de distribución, crear entradas más grandes concatenando el archivo piloto varias veces.
6. Ejecutar la versión distribuida con 2 workers y luego con 4 workers.
7. Comparar tiempos y verificar que el CSV final conserve los mismos promedios.

## Tabla para registrar resultados

| Dataset | Versión | Recursos | Tiempo ms | Registros de salida | ¿Resultado correcto? |
|---|---:|---:|---:|---:|---:|
| MiniPilot | Monolito simple | 1 proceso | Pendiente | Pendiente | Sí/No |
| MiniPilot | Hilos | 4 hilos | Pendiente | Pendiente | Sí/No |
| datagrams4Pilot | Monolito simple | 1 proceso | Pendiente | Pendiente | Sí/No |
| datagrams4Pilot | Hilos | 4 hilos | Pendiente | Pendiente | Sí/No |
| datagrams4Pilot x N | Distribuido | 2 workers | Pendiente | Pendiente | Sí/No |

## Criterio para determinar cuándo vale la pena distribuir

La distribución vale la pena cuando el ahorro de tiempo por repartir el cálculo supera el costo de comunicación, serialización y coordinación entre maestro y workers. En términos prácticos, se recomienda distribuir solo desde el primer tamaño de archivo donde:

```text
T_distribuido < T_hilos_locales
```

y además la reducción sea estable en varias repeticiones, por ejemplo una mejora mayor al 15% en al menos tres ejecuciones.

## Interpretación esperada

- En `datagrams-MiniPilot.csv`, normalmente gana el monolito simple porque el archivo es pequeño y no compensa crear hilos o coordinar workers.
- En `datagrams4Pilot.csv`, la versión de hilos puede empezar a mejorar si hay suficientes registros.
- La versión distribuida solo debería justificarse con archivos más grandes que `datagrams4Pilot.csv`, porque en datos pequeños el costo de red domina.
