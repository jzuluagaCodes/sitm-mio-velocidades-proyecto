module sitmmio {

    /** Una línea cruda del CSV de datagramas (sin encabezado, columnas por índice) */
    sequence<string> RawLines;

    /** Conjunto de rutas activas (LINEID numérico como string) */
    sequence<string> ActiveRoutes;

    /**
     * Interfaz remota expuesta por cada Worker.
     */
    interface Calculadora {
        string calcular(RawLines partition, ActiveRoutes activeRoutes);
    }
}