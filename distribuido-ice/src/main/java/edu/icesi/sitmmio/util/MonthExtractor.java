package edu.icesi.sitmmio.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class MonthExtractor {
    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    );
    private MonthExtractor() {}

    public static String toYearMonth(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String v = raw.trim();
        if (v.length() >= 7 && v.charAt(4) == '-') return v.substring(0, 7);
        try {
            OffsetDateTime dt = OffsetDateTime.parse(v);
            return String.format("%04d-%02d", dt.getYear(), dt.getMonthValue());
        } catch (DateTimeParseException ignored) {}
        for (DateTimeFormatter fmt : FORMATTERS) {
            try {
                LocalDateTime dt = LocalDateTime.parse(v, fmt);
                return String.format("%04d-%02d", dt.getYear(), dt.getMonthValue());
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}
