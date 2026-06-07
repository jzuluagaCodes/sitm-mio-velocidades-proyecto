# Revisión de los deployments entregados

## Monolito simple - diagrama izquierdo

Está bien encaminado porque muestra un solo dispositivo de procesamiento, un Java Runtime Environment, el artefacto `calculadorPromedios.jar` y los componentes `LectorCSV`, `Calculadora` y `VistaUsuario`. Eso corresponde a una solución monolítica: todo corre en el mismo proceso y la comunicación entre componentes es local.

Ajustes recomendados para dejarlo perfecto:

- El archivo `datagrams-MiniPilot.csv` debe aparecer como artefacto externo de entrada, no como parte del `.jar`.
- Conviene agregar también `lines-241-ActiveGT.csv`, porque el enunciado pide calcular solo para las rutas activas del piloto.
- El componente `Calculadora` debería depender de `LectorCSV`, porque primero se leen los datos y luego se agregan por ruta/mes.
- La salida debería aparecer como `velocidades-promedio.csv`, porque el sistema debe producir resultados verificables.
- `VistaUsuario` puede quedarse si representa consola/CLI, pero no debe parecer una interfaz gráfica obligatoria.

Conclusión: el diagrama izquierdo sí representa el monolito simple, pero le faltaba mostrar el archivo de rutas activas y el archivo de salida.

## Monolito concurrente con hilos - diagrama derecho

La idea general está bien: sigue siendo un solo dispositivo y un solo `.jar`, pero ahora aparecen `ParticionadorCSV`, `ThreadPool`, `CalculadoraMultihilo`, `LectorCSV` y `VistaUsuario`. Eso sí representa una versión concurrente dentro del mismo proceso.

Ajustes recomendados:

- No debe verse como una arquitectura distribuida; los hilos están dentro del mismo Java Runtime Environment.
- `ParticionadorCSV` debe alimentar al `ThreadPool` con subconjuntos de filas.
- Cada hilo ejecuta una tarea de agregación parcial.
- `CalculadoraMultihilo` debe unir los resultados parciales en un resultado final.
- Debe aparecer `lines-241-ActiveGT.csv` como entrada junto con `datagrams-MiniPilot.csv` o `datagrams4Pilot.csv`.
- La salida debe ser un CSV final con ruta, mes, velocidad promedio y cantidad de registros.

Conclusión: el diagrama derecho está bien para la versión de hilos. Solo habría que ajustar el flujo para que se entienda que `ThreadPool` ejecuta varias tareas y que la calculadora multihilo consolida los resultados.
