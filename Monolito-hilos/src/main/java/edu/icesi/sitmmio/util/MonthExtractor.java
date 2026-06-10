package edu.icesi.sitmmio.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class MonthExtractor {
    private static final List<DateTimeFormatter> FORMATTERS = new ArrayList<>();

    static {
        FORMATTERS.add(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private MonthExtractor() { }

    public static String toYearMonth(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) return null;
        String value = rawTimestamp.trim();
        if (value.length() >= 7 && value.charAt(4) == '-') {
            return value.substring(0, 7);
        }
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(value);
            return String.format("%04d-%02d", dateTime.getYear(), dateTime.getMonthValue());
        } catch (DateTimeParseException ignored) {
            // Continue trying local date-time patterns.
        }
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
                return String.format("%04d-%02d", dateTime.getYear(), dateTime.getMonthValue());
            } catch (DateTimeParseException ignored) {
                // Try the next formatter.
            }
        }
        return null;
    }
}
