module sitmmio {

    /** Una fila del CSV: mapa de nombre_columna -> valor */
    dictionary<string, string> CsvRow;

    /** Lista de filas que componen una partición del dataset */
    sequence<CsvRow> Partition;

    /** Conjunto de rutas activas (identificadores de ruta) */
    sequence<string> ActiveRoutes;

    /**
     * Interfaz remota expuesta por cada Worker.
     */
    interface Calculadora {
        string calcular(Partition partition, ActiveRoutes activeRoutes);
    }
}