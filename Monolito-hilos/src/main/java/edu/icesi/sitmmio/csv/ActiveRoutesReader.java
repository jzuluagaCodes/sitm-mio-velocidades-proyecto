package edu.icesi.sitmmio.csv;

import edu.icesi.sitmmio.util.HeaderFinder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ActiveRoutesReader {
    private static final List<String> ROUTE_COLUMNS = List.of(
            "route_id", "routeid", "line_id", "lineid", "route", "ruta", "line", "linea", "linename", "nombre"
    );

    public Set<String> readActiveRoutes(Path path) throws IOException {
        char separator = CsvReader.detectSeparator(path);
        CsvReader reader = new CsvReader(separator);
        List<Map<String, String>> rows = reader.readAll(path);
        Set<String> routes = new HashSet<>();
        for (Map<String, String> row : rows) {
            String route = HeaderFinder.find(row, ROUTE_COLUMNS);
            if (route != null && !route.isBlank()) {
                routes.add(route.trim());
            }
        }
        return routes;
    }
}
