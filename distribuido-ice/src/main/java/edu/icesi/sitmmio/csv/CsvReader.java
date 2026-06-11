package edu.icesi.sitmmio.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class CsvReader {
    private final char separator;

    public CsvReader(char separator) { this.separator = separator; }

    public List<Map<String, String>> readAll(Path path) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) return rows;
            List<String> headers = parseLine(headerLine);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> values = parseLine(line);
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++)
                    row.put(headers.get(i).trim(), i < values.size() ? values.get(i).trim() : "");
                rows.add(row);
            }
        }
        return rows;
    }

    public static List<String> readLines(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) lines.add(line);
            }
        }
        return lines;
    }

    public static char detectSeparator(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null) return ',';
            return count(line, ';') > count(line, ',') ? ';' : ',';
        }
    }

    private static int count(String v, char t) {
        int n = 0;
        for (int i = 0; i < v.length(); i++) if (v.charAt(i) == t) n++;
        return n;
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else inQuotes = !inQuotes;
            } else if (ch == separator && !inQuotes) { values.add(cur.toString()); cur.setLength(0); }
            else cur.append(ch);
        }
        values.add(cur.toString());
        return values;
    }
}