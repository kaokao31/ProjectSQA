package com.fasterxml.jackson.databind.util;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    // ============================================================
    // Basic parsing tests
    // ============================================================

    @Test
    public void testParseISO8601DateWithZ() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000Z");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test
    public void testParseISO8601DateWithPositiveOffset() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000+0000");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test
    public void testParseISO8601DateWithNegativeOffset() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000-0000");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test
    public void testParseISO8601DateWithColonOffset() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000+00:00");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test
    public void testParseISO8601DateWithoutMilliseconds() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00Z");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test
    public void testParseISO8601DateWithMilliseconds() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.123Z");
        assertNotNull(date);
        assertEquals(123L, date.getTime());
    }

    @Test
    public void testParseISO8601DateWithMicroseconds() throws Exception {
        // Microseconds should be truncated to milliseconds
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.123456Z");
        assertNotNull(date);
        assertEquals(123L, date.getTime());
    }

    @Test
    public void testParseISO8601DateWithNanoseconds() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.123456789Z");
        assertNotNull(date);
        assertEquals(123L, date.getTime());
    }

    @Test
    public void testParseDateWithSpaceSeparator() throws Exception {
        // Some implementations accept space instead of 'T'
        Date date = stdDateFormat.parse("1970-01-01 00:00:00.000Z");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test
    public void testParseDateWithoutTime() throws Exception {
        // Date only
        Date date = stdDateFormat.parse("1970-01-01");
        assertNotNull(date);
        // Should be midnight UTC
        assertEquals(0L, date.getTime());
    }

    @Test
    public void testParseDateWithTimeNoTimezone() throws Exception {
        // No timezone specified, should default to UTC
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    @Test
    public void testParseDateWithTimeAndNoTimezoneWithSpace() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01 00:00:00.000");
        assertNotNull(date);
        assertEquals(0L, date.getTime());
    }

    // ============================================================
    // Edge cases and boundary values
    // ============================================================

    @Test
    public void testParseMinDate() throws Exception {
        // Earliest date representable by Date (year 1)
        Date date = stdDateFormat.parse("0001-01-01T00:00:00.000Z");
        assertNotNull(date);
        // Just check it's not null and no exception
    }

    @Test
    public void testParseMaxDate() throws Exception {
        // Far future date
        Date date = stdDateFormat.parse("9999-12-31T23:59:59.999Z");
        assertNotNull(date);
    }

    @Test
    public void testParseYearZero() throws Exception {
        // Year 0 is not valid ISO 8601, but some parsers accept it
        try {
            stdDateFormat.parse("0000-01-01T00:00:00.000Z");
            // If no exception, it's acceptable
        } catch (ParseException e) {
            // Expected if not supported
        }
    }

    @Test
    public void testParseLeapSecond() throws Exception {
        // 23:59:60 is a leap second, should be handled
        try {
            Date date = stdDateFormat.parse("2015-06-30T23:59:60.000Z");
            assertNotNull(date);
        } catch (ParseException e) {
            // Some implementations reject leap seconds
        }
    }

    @Test
    public void testParseDateWithExtraWhitespace() throws Exception {
        try {
            stdDateFormat.parse("  1970-01-01T00:00:00.000Z  ");
            fail("Expected ParseException for whitespace");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseNull() throws Exception {
        try {
            stdDateFormat.parse(null);
            fail("Expected NullPointerException or ParseException");
        } catch (NullPointerException | ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseEmptyString() throws Exception {
        try {
            stdDateFormat.parse("");
            fail("Expected ParseException for empty string");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseInvalidDate() throws Exception {
        try {
            stdDateFormat.parse("not-a-date");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseInvalidMonth() throws Exception {
        try {
            stdDateFormat.parse("1970-13-01T00:00:00.000Z");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseInvalidDay() throws Exception {
        try {
            stdDateFormat.parse("1970-01-32T00:00:00.000Z");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseInvalidHour() throws Exception {
        try {
            stdDateFormat.parse("1970-01-01T24:00:00.000Z");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseInvalidMinute() throws Exception {
        try {
            stdDateFormat.parse("1970-01-01T00:60:00.000Z");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseInvalidSecond() throws Exception {
        try {
            stdDateFormat.parse("1970-01-01T00:00:61.000Z");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    // ============================================================
    // Formatting tests
    // ============================================================

    @Test
    public void testFormatDate() throws Exception {
        Date date = new Date(0L);
        String formatted = stdDateFormat.format(date);
        // Should be ISO 8601 with milliseconds and Z
        assertEquals("1970-01-01T00:00:00.000+0000", formatted);
    }

    @Test
    public void testFormatDateWithMilliseconds() throws Exception {
        Date date = new Date(123L);
        String formatted = stdDateFormat.format(date);
        assertEquals("1970-01-01T00:00:00.123+0000", formatted);
    }

    @Test
    public void testFormatDateWithNegativeOffset() throws Exception {
        // StdDateFormat uses UTC by default, so offset is always +0000
        Date date = new Date(0L);
        String formatted = stdDateFormat.format(date);
        assertTrue(formatted.endsWith("+0000"));
    }

    // ============================================================
    // Thread safety tests
    // ============================================================

    @Test
    public void testThreadSafety() throws Exception {
        final int threadCount = 10;
        final int iterations = 100;
        final AtomicInteger failures = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterations; j++) {
                        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000Z");
                        if (date.getTime() != 0L) {
                            failures.incrementAndGet();
                        }
                        String formatted = stdDateFormat.format(date);
                        if (!"1970-01-01T00:00:00.000+0000".equals(formatted)) {
                            failures.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals("Thread safety failures detected", 0, failures.get());
    }

    // ============================================================
    // Clone and equals tests
    // ============================================================

    @Test
    public void testClone() throws Exception {
        StdDateFormat cloned = (StdDateFormat) stdDateFormat.clone();
        assertNotNull(cloned);
        assertNotSame(stdDateFormat, cloned);
        // Ensure cloned instance works
        Date date = cloned.parse("1970-01-01T00:00:00.000Z");
        assertNotNull(date);
    }

    @Test
    public void testEquals() {
        StdDateFormat other = new StdDateFormat();
        assertTrue(stdDateFormat.equals(other));
        assertTrue(other.equals(stdDateFormat));
        assertEquals(stdDateFormat.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse(stdDateFormat.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        assertFalse(stdDateFormat.equals("string"));
    }

    // ============================================================
    // Timezone handling tests (potential bug area)
    // ============================================================

    @Test
    public void testParseWithDifferentTimeZone() throws Exception {
        // Parse a date with a non-UTC timezone and verify it's converted to UTC
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000+0100");
        assertNotNull(date);
        // +0100 means one hour ahead of UTC, so UTC time should be 23:00:00 previous day
        assertEquals(-3600000L, date.getTime());
    }

    @Test
    public void testParseWithTimeZoneAbbreviation() throws Exception {
        // Some implementations accept abbreviations like "GMT", "UTC"
        try {
            Date date = stdDateFormat.parse("1970-01-01T00:00:00.000 GMT");
            assertNotNull(date);
        } catch (ParseException e) {
            // Expected if not supported
        }
    }

    @Test
    public void testParseWithTimeZoneName() throws Exception {
        try {
            Date date = stdDateFormat.parse("1970-01-01T00:00:00.000 America/New_York");
            assertNotNull(date);
        } catch (ParseException e) {
            // Expected if not supported
        }
    }

    // ============================================================
    // RFC 1123 format tests (if supported)
    // ============================================================

    @Test
    public void testParseRFC1123Date() throws Exception {
        // Some implementations also parse RFC 1123
        try {
            Date date = stdDateFormat.parse("Thu, 01 Jan 1970 00:00:00 GMT");
            assertNotNull(date);
            assertEquals(0L, date.getTime());
        } catch (ParseException e) {
            // Expected if not supported
        }
    }

    // ============================================================
    // Bug-specific test: Defects4J bug 87
    // ============================================================

    @Test
    public void testBug87_TimeZoneOffsetParsing() throws Exception {
        // Bug 87 might involve parsing dates with timezone offsets that have
        // missing leading zeros or other edge cases.
        // Test with offset -05:00 (should be -5 hours)
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000-05:00");
        assertNotNull(date);
        // UTC time should be 05:00:00
        assertEquals(5 * 3600000L, date.getTime());
    }

    @Test
    public void testBug87_TimeZoneOffsetWithoutColon() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000-0500");
        assertNotNull(date);
        assertEquals(5 * 3600000L, date.getTime());
    }

    @Test
    public void testBug87_TimeZoneOffsetWithSingleDigitHour() throws Exception {
        // Offset -5:00 (single digit hour)
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000-5:00");
        assertNotNull(date);
        assertEquals(5 * 3600000L, date.getTime());
    }

    @Test
    public void testBug87_TimeZoneOffsetWithSingleDigitMinute() throws Exception {
        // Offset -00:30 (30 minutes)
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000-00:30");
        assertNotNull(date);
        assertEquals(30 * 60000L, date.getTime());
    }

    @Test
    public void testBug87_TimeZoneOffsetWithNoMinutes() throws Exception {
        // Offset -05 (no minutes)
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000-05");
        assertNotNull(date);
        assertEquals(5 * 3600000L, date.getTime());
    }

    @Test
    public void testBug87_TimeZoneOffsetWithPositiveSingleDigit() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000+5:00");
        assertNotNull(date);
        assertEquals(-5 * 3600000L, date.getTime());
    }

    @Test
    public void testBug87_TimeZoneOffsetWithZAndExtra() throws Exception {
        // Some implementations might incorrectly handle "Z" with extra characters
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.000Z ");
            fail("Expected ParseException for trailing space after Z");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testBug87_TimeZoneOffsetWithPlusMinusSignOnly() throws Exception {
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.000+");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testBug87_TimeZoneOffsetWithInvalidCharacter() throws Exception {
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.000+AB00");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    // ============================================================
    // Additional edge cases
    // ============================================================

    @Test
    public void testParseDateWithFractionalSecondsOnly() throws Exception {
        // Only fractional seconds, no timezone
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.123");
        assertNotNull(date);
        assertEquals(123L, date.getTime());
    }

    @Test
    public void testParseDateWithTrailingZerosInFraction() throws Exception {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.100Z");
        assertNotNull(date);
        assertEquals(100L, date.getTime());
    }

    @Test
    public void testParseDateWithNoFractionButDot() throws Exception {
        // Trailing dot without digits
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.Z");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseDateWithMultipleDots() throws Exception {
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.123.456Z");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseDateWithNegativeYear() throws Exception {
        try {
            stdDateFormat.parse("-1970-01-01T00:00:00.000Z");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseDateWithTooManyDigitsInYear() throws Exception {
        try {
            stdDateFormat.parse("01970-01-01T00:00:00.000Z");
            // Might be accepted as year 1970 with leading zero
        } catch (ParseException e) {
            // Expected if strict
        }
    }

    @Test
    public void testParseDateWithTimeZoneOffsetBeyond2359() throws Exception {
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.000+2400");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseDateWithTimeZoneOffsetMinutesBeyond59() throws Exception {
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.000+0060");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // Expected
        }
    }

    // ============================================================
    // getInstance() and static methods
    // ============================================================

    @Test
    public void testGetInstance() {
        DateFormat instance = StdDateFormat.getInstance();
        assertNotNull(instance);
        assertTrue(instance instanceof StdDateFormat);
    }

    @Test
    public void testGetBlueprint() {
        DateFormat blueprint = StdDateFormat.getBlueprint();
        assertNotNull(blueprint);
        assertTrue(blueprint instanceof StdDateFormat);
    }

    @Test
    public void testGetISO8601Format() {
        DateFormat isoFormat = StdDateFormat.getISO8601Format(TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(isoFormat);
        assertTrue(isoFormat instanceof SimpleDateFormat);
    }

    @Test
    public void testGetRFC1123Format() {
        DateFormat rfcFormat = StdDateFormat.getRFC1123Format(TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(rfcFormat);
        assertTrue(rfcFormat instanceof SimpleDateFormat);
    }

    // ============================================================
    // TimeZone cloning and immutability
    // ============================================================

    @Test
    public void testSetTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        StdDateFormat modified = (StdDateFormat) stdDateFormat.clone();
        modified.setTimeZone(tz);
        // Should not affect original
        assertNotEquals(tz, stdDateFormat.getTimeZone());
    }

    @Test
    public void testSetLenient() {
        StdDateFormat modified = (StdDateFormat) stdDateFormat.clone();
        modified.setLenient(false);
        // Should not affect original
        assertTrue(stdDateFormat.isLenient());
    }
}