package edu.icesi.sitmmio.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;


public final class ActiveRoutesReader {

    public Set<String> readActiveRoutes(Path path) throws IOException {
        Set<String> routes = new HashSet<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String header = br.readLine(); // saltar encabezado
            if (header == null) return routes;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                // split por coma respetando posibles comillas
                String[] cols = line.split(",", -1);
                if (cols.length == 0) continue;
                String lineId = cols[0].replace("\"", "").trim();
                if (!lineId.isBlank()) routes.add(lineId);
            }
        }
        return routes;
    }
}