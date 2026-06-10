package edu.icesi.sitmmio.csv;

import edu.icesi.sitmmio.util.HeaderFinder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public final class ActiveRoutesReader {
    private static final List<String> ROUTE_COLUMNS = List.of(
            "route_id","routeid","line_id","lineid","route","ruta","line","linea","linename","nombre"
    );

    public Set<String> readActiveRoutes(Path path) throws IOException {
        char sep = CsvReader.detectSeparator(path);
        List<Map<String, String>> rows = new CsvReader(sep).readAll(path);
        Set<String> routes = new HashSet<>();
        for (Map<String, String> row : rows) {
            String route = HeaderFinder.find(row, ROUTE_COLUMNS);
            if (route != null && !route.isBlank()) routes.add(route.trim());
        }
        return routes;
    }
}
