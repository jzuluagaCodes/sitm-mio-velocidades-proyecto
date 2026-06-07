package edu.icesi.sitmmio.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class MonthExtractorTest {

    @Test
    public void testToYearMonthFromStandard() {
        assertEquals("2025-01", MonthExtractor.toYearMonth("2025-01-10 08:00:00"));
        assertEquals("2025-02", MonthExtractor.toYearMonth("2025-02-15T10:30:00"));
        assertEquals("2025-03", MonthExtractor.toYearMonth("2025-03-20"));
    }

    @Test
    public void testToYearMonthFromCustomFormats() {
        assertEquals("2025-04", MonthExtractor.toYearMonth("2025/04/10 12:00:00"));
        assertEquals("2025-05", MonthExtractor.toYearMonth("25/05/2025 14:15:30"));
        assertEquals("2025-06", MonthExtractor.toYearMonth("06/25/2025 09:45:00")); // MM/dd/yyyy
    }

    @Test
    public void testInvalidFormats() {
        assertNull(MonthExtractor.toYearMonth("invalid-date"));
        assertNull(MonthExtractor.toYearMonth(""));
        assertNull(MonthExtractor.toYearMonth(null));
    }
}
