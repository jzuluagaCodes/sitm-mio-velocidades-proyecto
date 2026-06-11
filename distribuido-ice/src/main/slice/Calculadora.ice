module sitmmio {

    sequence<string> RawLines;
    sequence<string> ActiveRoutes;

    interface Calculadora {
        string calcular(RawLines partition, ActiveRoutes activeRoutes);
    }
}