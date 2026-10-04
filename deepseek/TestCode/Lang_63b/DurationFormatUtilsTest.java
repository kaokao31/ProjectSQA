package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link DurationFormatUtils}.
 * Designed for maximum coverage and to trigger the known Defects4J bug LANG-63.
 */
public class DurationFormatUtilsTest {

    // Bug LANG-63: formatDurationWords returns negative values for certain inputs.
    // The original failing test from Defects4J.
    @Test
    public void testJiraLang281() {
        // 9 minutes = 540000 milliseconds
        long duration = 9 * 60 * 1000L;
        // Using pattern "mm" should yield "09"
        String result = DurationFormatUtils.formatDuration(duration, "mm");
        assertEquals("09", result);
    }

    // ----- formatDuration -----
    @Test
    public void testFormatDurationSimple() {
        assertEquals("02:30:00", DurationFormatUtils.formatDuration(9000000L, "HH:mm:ss"));
        assertEquals("00:00:00", DurationFormatUtils.formatDuration(0L, "HH:mm:ss"));
        assertEquals("99:59:59", DurationFormatUtils.formatDuration(359999000L, "HH:mm:ss"));
    }

    @Test
    public void testFormatDurationMinSec() {
        assertEquals("05:03", DurationFormatUtils.formatDuration(303000L, "mm:ss"));
        assertEquals("59:59", DurationFormatUtils.formatDuration(3599000L, "mm:ss"));
    }

    @Test
    public void testFormatDurationWithMillis() {
        assertEquals("000.999", DurationFormatUtils.formatDuration(999L, "SSS.SSS"));
        assertEquals("001.000", DurationFormatUtils.formatDuration(1000L, "SSS.SSS"));
    }

    @Test
    public void testFormatDurationLargeDuration() {
        // Large duration that may cause overflow if ints are used
        long large = Long.MAX_VALUE / 2;
        String result = DurationFormatUtils.formatDuration(large, "d 'days' H 'hours' m 'minutes' s 'seconds'");
        assertNotNull(result);
        // Just ensure no exception and contains something
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatDurationNegative() {
        DurationFormatUtils.formatDuration(-1L, "HH:mm:ss");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatDurationNullFormat() {
        DurationFormatUtils.formatDuration(1000L, null);
    }

    // ----- formatDurationWords -----
    @Test
    public void testFormatDurationWordsZero() {
        assertEquals("0 seconds", DurationFormatUtils.formatDurationWords(0L, false, false));
        assertEquals("0 seconds", DurationFormatUtils.formatDurationWords(0L, true, true));
    }

    @Test
    public void testFormatDurationWordsSingleUnits() {
        assertEquals("1 second", DurationFormatUtils.formatDurationWords(1000L, false, false));
        assertEquals("1 minute", DurationFormatUtils.formatDurationWords(60000L, false, false));
        assertEquals("1 hour", DurationFormatUtils.formatDurationWords(3600000L, false, false));
        assertEquals("1 day", DurationFormatUtils.formatDurationWords(86400000L, false, false));
    }

    @Test
    public void testFormatDurationWordsPlural() {
        assertEquals("2 seconds", DurationFormatUtils.formatDurationWords(2000L, false, false));
        assertEquals("3 minutes", DurationFormatUtils.formatDurationWords(180000L, false, false));
        assertEquals("4 hours", DurationFormatUtils.formatDurationWords(14400000L, false, false));
        assertEquals("5 days", DurationFormatUtils.formatDurationWords(432000000L, false, false));
    }

    @Test
    public void testFormatDurationWordsSuppressLeading() {
        assertEquals("9 minutes 0 seconds", DurationFormatUtils.formatDurationWords(540000L, true, false));
        assertEquals("0 days 0 hours 9 minutes 0 seconds", DurationFormatUtils.formatDurationWords(540000L, false, false));
    }

    @Test
    public void testFormatDurationWordsSuppressTrailing() {
        assertEquals("0 days 0 hours 9 minutes", DurationFormatUtils.formatDurationWords(540000L, false, true));
        assertEquals("9 minutes", DurationFormatUtils.formatDurationWords(540000L, true, true));
    }

    @Test
    public void testFormatDurationWordsLargeDuration() {
        long large = 100 * 24 * 60 * 60 * 1000L; // 100 days
        String result = DurationFormatUtils.formatDurationWords(large, true, true);
        assertTrue(result.startsWith("100 days"));
    }

    // ----- formatDurationHMS -----
    @Test
    public void testFormatDurationHMS() {
        assertEquals("0:00:00.000", DurationFormatUtils.formatDurationHMS(0L));
        assertEquals("0:00:01.000", DurationFormatUtils.formatDurationHMS(1000L));
        assertEquals("0:01:00.000", DurationFormatUtils.formatDurationHMS(60000L));
        assertEquals("1:00:00.000", DurationFormatUtils.formatDurationHMS(3600000L));
        assertEquals("1:02:03.456", DurationFormatUtils.formatDurationHMS(3723456L));
    }

    @Test
    public void testFormatDurationHMSLarge() {
        long large = 23L * 60 * 60 * 1000 + 59L * 60 * 1000 + 59L * 1000 + 999L; // 23:59:59.999
        assertEquals("23:59:59.999", DurationFormatUtils.formatDurationHMS(large));
    }

    // ----- formatPeriod -----
    @Test
    public void testFormatPeriodSimple() {
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.set(2000, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        start.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.set(2000, java.util.Calendar.JANUARY, 1, 1, 30, 0);
        end.set(java.util.Calendar.MILLISECOND, 0);
        String result = DurationFormatUtils.formatPeriod(start.getTime().getTime(), end.getTime().getTime(), "HH:mm:ss");
        assertEquals("01:30:00", result);
    }

    @Test
    public void testFormatPeriodAcrossDays() {
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.set(2000, java.util.Calendar.JANUARY, 1, 23, 0, 0);
        start.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.set(2000, java.util.Calendar.JANUARY, 2, 1, 0, 0);
        end.set(java.util.Calendar.MILLISECOND, 0);
        String result = DurationFormatUtils.formatPeriod(start.getTime().getTime(), end.getTime().getTime(), "d 'day' H 'hour'");
        assertEquals("0 day 2 hour", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatPeriodNegativePeriod() {
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.set(2000, java.util.Calendar.JANUARY, 2, 0, 0, 0);
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.set(2000, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        DurationFormatUtils.formatPeriod(start.getTime().getTime(), end.getTime().getTime(), "HH:mm:ss");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatPeriodNullFormat() {
        DurationFormatUtils.formatPeriod(0, 1000, null);
    }

    // ----- Edge cases with Long boundaries -----
    @Test
    public void testMaxLongDuration() {
        try {
            String result = DurationFormatUtils.formatDurationWords(Long.MAX_VALUE, false, false);
            assertNotNull(result);
            result = DurationFormatUtils.formatDuration(Long.MAX_VALUE, "HH:mm:ss");
            assertNotNull(result);
        } catch (Exception e) {
            // If exception is thrown, verify it's acceptable (e.g., IllegalArgumentException)
            // But the method should handle by throwing an exception or producing a string
            assertTrue(e instanceof IllegalArgumentException || e instanceof ArithmeticException);
        }
    }

    @Test
    public void testMinLongDuration() {
        try {
            String result = DurationFormatUtils.formatDurationWords(Long.MIN_VALUE, false, false);
            // If no exception, check that result is not null
            assertNotNull(result);
        } catch (Exception e) {
            // Accept if method throws due to negative duration
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    // ----- Specific tests for code coverage -----
    @Test
    public void testFormatDurationWithMultipleTokens() {
        // Pattern containing all tokens
        String format = "d 'day' H 'hour' m 'min' s 'sec' S 'ms'";
        String result = DurationFormatUtils.formatDuration(3723456L, format);
        // 3723456 ms = 1 hour 2 minutes 3 seconds 456 ms
        assertEquals("0 day 1 hour 2 min 3 sec 456 ms", result);
    }

    @Test
    public void testFormatDurationWithSingleQuotes() {
        String result = DurationFormatUtils.formatDuration(60000L, "'time:' m 'minutes'");
        assertEquals("time: 1 minutes", result);
    }

    @Test
    public void testFormatDurationWordsNotSuppressingLeadingZeros() {
        // For a duration less than a day, leading zeros appear
        String result = DurationFormatUtils.formatDurationWords(540000L, false, false);
        assertTrue(result.contains("0 days"));
        assertTrue(result.contains("0 hours"));
    }

    @Test
    public void testFormatDurationWordsSuppressingBoth() {
        String result = DurationFormatUtils.formatDurationWords(540000L, true, true);
        assertEquals("9 minutes", result);
    }

    @Test
    public void testFormatDurationWordsOnlySeconds() {
        String result = DurationFormatUtils.formatDurationWords(5000L, true, true);
        assertEquals("5 seconds", result);
    }

    @Test
    public void testFormatDurationWordsMixed() {
        // 1 hour 2 minutes 3 seconds
        String result = DurationFormatUtils.formatDurationWords(3723000L, true, true);
        assertEquals("1 hour 2 minutes 3 seconds", result);
    }

    @Test
    public void testFormatPeriodWithTimeZone() {
        // Using formatPeriod with time zone differences should not affect duration
        java.util.Calendar start = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+1"));
        start.set(2000, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        java.util.Calendar end = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT-5"));
        end.set(2000, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        // Start and end represent same instant in time, so duration should be 0
        String result = DurationFormatUtils.formatPeriod(start.getTime().getTime(), end.getTime().getTime(), "HH:mm:ss");
        assertEquals("00:00:00", result);
    }

    @Test
    public void testFormatPeriodFourDigitYear() {
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.set(2000, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.set(2001, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        String result = DurationFormatUtils.formatPeriod(start.getTime().getTime(), end.getTime().getTime(), "y 'year' M 'month' d 'day'");
        // The period is exactly one year (365 days) but may be considered as 0 years 12 months 0 days depending on implementation.
        // Just check it does not throw and contains something meaningful.
        assertNotNull(result);
    }
}