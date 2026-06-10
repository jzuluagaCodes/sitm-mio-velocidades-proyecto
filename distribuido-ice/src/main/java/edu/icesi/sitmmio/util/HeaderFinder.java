package edu.icesi.sitmmio.util;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HeaderFinder {
    private HeaderFinder() {}

    public static String find(Map<String, String> row, List<String> candidates) {
        for (String candidate : candidates)
            for (String header : row.keySet())
                if (normalize(header).equals(normalize(candidate)))
                    return row.get(header);
        for (String candidate : candidates) {
            String nc = normalize(candidate);
            for (String header : row.keySet()) {
                String nh = normalize(header);
                if (nh.contains(nc) || nc.contains(nh))
                    return row.get(header);
            }
        }
        return null;
    }

    public static String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT)
                .replace("á","a").replace("é","e").replace("í","i")
                .replace("ó","o").replace("ú","u").replace("ñ","n")
                .replaceAll("[^a-z0-9]","");
    }
}
