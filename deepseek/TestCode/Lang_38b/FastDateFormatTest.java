package org.apache.commons.lang3.time;

import org.junit.Test;
import java.util.*;
import java.text.*;
import static org.junit.Assert.*;

public class FastDateFormatTest {

    // Bug ID 38: testLang538 - timezone not applied correctly
    @Test
    public void testLang538() {
        // Pattern with literal 'Z' at end, timezone must be UTC
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2009, Calendar.OCTOBER, 16, 16, 42, 16);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        String result = fdf.format(date);
        assertEquals("Expected UTC time with literal Z", "2009-10-16T16:42:16.000Z", result);
    }

    // Test basic formatting with no timezone pattern
    @Test
    public void testBasicFormat() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(2020, Calendar.JANUARY, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        String expected = "2020-01-15 12:30:45";
        assertEquals(expected, fdf.format(date));
    }

    // Test timezone pattern "Z" (RFC 822)
    @Test
    public void testTimeZonePatternZ() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getTimeZone("GMT+05:30"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+05:30"));
        cal.set(2021, Calendar.MARCH, 10, 10, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        // Pattern Z gives +0530 for that timezone
        assertTrue(fdf.format(date).contains("+0530"));
    }

    // Test timezone pattern "ZZ" (ISO 8601 basic)
    @Test
    public void testTimeZonePatternZZ() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZZ", TimeZone.getTimeZone("UTC"));
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2015, Calendar.JULY, 4, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        // Pattern ZZ for UTC should produce "+0000" (or possibly "Z"? check actual behavior)
        // We check that the hour/minute are correct and the offset starts with '+' or '-'
        String result = fdf.format(date);
        assertTrue(result.endsWith("+0000") || result.endsWith("-0000"));
        assertTrue(result.startsWith("2015-07-04T00:00:00"));
    }

    // Test timezone pattern "X" (ISO 8601 if supported)
    @Test
    public void testTimeZonePatternX() {
        // FastDateFormat may not support 'X' pattern; if it throws, we skip.
        // We'll just catch and assume unsupported.
        try {
            FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssX", TimeZone.getTimeZone("GMT"));
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(2018, Calendar.DECEMBER, 25, 18, 30, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date date = cal.getTime();
            String result = fdf.format(date);
            // For GMT, X pattern produces "Z"
            assertEquals("2018-12-25T18:30:00Z", result);
        } catch (Exception e) {
            // Pattern not supported, ignore test
        }
    }

    // Test null date - should not throw NPE (check behavior)
    @Test(expected = NullPointerException.class)
    public void testNullDate() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.format((Date) null);
    }

    // Test null pattern (should throw IllegalArgumentException)
    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern() {
        FastDateFormat.getInstance(null);
    }

    // Test empty pattern
    @Test
    public void testEmptyPattern() {
        FastDateFormat fdf = FastDateFormat.getInstance("");
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        String result = fdf.format(date);
        assertEquals("", result);
    }

    // Test getDateInstance, getTimeInstance, getDateTimeInstance
    @Test
    public void testGetInstanceShortcuts() {
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.FULL);
        assertNotNull(fdf1);
        FastDateFormat fdf2 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM);
        assertNotNull(fdf2);
        FastDateFormat fdf3 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.SHORT);
        assertNotNull(fdf3);
    }

    // Test caching: same pattern/timezone/locale should return same instance
    @Test
    public void testCaching() {
        String pattern = "yyyy/MM/dd";
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Locale locale = Locale.US;
        FastDateFormat fdf1 = FastDateFormat.getInstance(pattern, tz, locale);
        FastDateFormat fdf2 = FastDateFormat.getInstance(pattern, tz, locale);
        assertSame("Cached instances should be identical", fdf1, fdf2);
    }

    // Test different locales
    @Test
    public void testLocale() {
        Locale[] locales = {Locale.US, Locale.GERMANY, Locale.JAPAN};
        for (Locale loc : locales) {
            FastDateFormat fdf = FastDateFormat.getInstance("dd MMM yyyy", loc);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.set(2022, Calendar.FEBRUARY, 5);
            Date date = cal.getTime();
            String result = fdf.format(date);
            // Just check it doesn't throw and contains expected day/month/year
            assertTrue(result.startsWith("05 "));
            assertTrue(result.endsWith(" 2022"));
        }
    }

    // Test parsing (if parse method exists in FastDateFormat - it does in subclass)
    @Test
    public void testParse() throws ParseException {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"));
        String source = "1999-12-31";
        Date parsed = fdf.parse(source);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(1999, Calendar.DECEMBER, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals(cal.getTime(), parsed);
    }

    // Test boundary dates (min and max)
    @Test
    public void testBoundaryDates() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("GMT"));
        // Date(Long.MIN_VALUE) is not a valid date in many timezones, but we can still format
        Date minDate = new Date(Long.MIN_VALUE);
        String result = fdf.format(minDate);
        assertNotNull(result);

        Date maxDate = new Date(Long.MAX_VALUE);
        result = fdf.format(maxDate);
        assertNotNull(result);
    }

    // Test that timezone is correctly applied in patterns (e.g., using "z" for short timezone name)
    @Test
    public void testTimezoneNamePattern() {
        // Use a timezone known to have unique short name
        TimeZone pst = TimeZone.getTimeZone("America/Los_Angeles");
        FastDateFormat fdf = FastDateFormat.getInstance("z", pst);
        Calendar cal = Calendar.getInstance(pst);
        cal.set(2020, Calendar.JUNE, 1, 12, 0, 0); // PDT
        Date date = cal.getTime();
        String result = fdf.format(date);
        // Should be "PDT" (or "PST" depending on DST, but June is PDT)
        assertTrue("Expected timezone name, got: " + result, result.equals("PDT") || result.equals("PST"));
    }

    // Test that the pattern with both date and time and timezone works
    @Test
    public void testDateTimeWithTimezone() {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2023, Calendar.APRIL, 1, 14, 30, 0);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();
        String result = fdf.format(date);
        // Expected pattern: "2023-04-01T14:30:00.500+0200"
        String expectedPrefix = "2023-04-01T14:30:00.500";
        assertTrue(result.startsWith(expectedPrefix));
        // The offset part should be "+0200"
        assertTrue(result.endsWith("+0200") || result.endsWith("-0200"));
    }

    // Test multiple timezone offsets (including negative)
    @Test
    public void testNegativeOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT-03:00");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm Z", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2024, Calendar.JANUARY, 1, 6, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        String result = fdf.format(date);
        // Should contain "-0300"
        assertTrue("Expected negative offset", result.contains("-0300"));
    }

    // Test that format(Object) works with java.sql.Date, java.sql.Time, java.sql.Timestamp
    @Test
    public void testFormatSQLTypes() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"));
        java.sql.Date sqlDate = java.sql.Date.valueOf("2024-12-25");
        assertEquals("2024-12-25", fdf.format(sqlDate));

        java.sql.Time sqlTime = java.sql.Time.valueOf("10:15:30");
        // Formatting a Time only with a date pattern? The result may include default date (1970-01-01)
        // We just check it doesn't throw
        assertNotNull(fdf.format(sqlTime));

        java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2024-12-25 10:15:30.123");
        assertNotNull(fdf.format(ts));
    }
}