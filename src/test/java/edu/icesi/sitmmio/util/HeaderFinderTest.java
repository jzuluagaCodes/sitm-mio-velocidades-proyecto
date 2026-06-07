package edu.icesi.sitmmio.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public final class HeaderFinderTest {

    @Test
    public void testNormalize() {
        assertEquals("ruta", HeaderFinder.normalize(" RUTA "));
        assertEquals("fechahora", HeaderFinder.normalize("fecha_hora"));
        assertEquals("velocidad", HeaderFinder.normalize("Velocidád"));
        assertEquals("eventtime", HeaderFinder.normalize("Event-Time!"));
    }

    @Test
    public void testFindExactAndApproximate() {
        Map<String, String> row = Map.of(
            "Ruta", "P10",
            "Fecha_Hora", "2025-01-10 08:00:00",
            "Velocidad", "25.5"
        );

        List<String> routeCandidates = List.of("route", "ruta");
        List<String> speedCandidates = List.of("speed", "velocidad");

        assertEquals("P10", HeaderFinder.find(row, routeCandidates));
        assertEquals("25.5", HeaderFinder.find(row, speedCandidates));
    }
}
