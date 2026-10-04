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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for StdDateFormat.
 * Targets maximum code coverage and fault detection (including Defects4J bug 104).
 */
public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    // ---------- Basic instantiation and configuration ----------

    @Test
    public void testDefaultInstance() {
        assertNotNull(StdDateFormat.instance);
        assertSame(StdDateFormat.instance, StdDateFormat.instance);
    }

    @Test
    public void testClone() {
        DateFormat clone = (DateFormat) stdDateFormat.clone();
        assertNotNull(clone);
        assertNotSame(stdDateFormat, clone);
    }

    @Test
    public void testEqualsAndHashCode() {
        StdDateFormat other = new StdDateFormat();
        assertEquals(stdDateFormat, other);
        assertEquals(stdDateFormat.hashCode(), other.hashCode());
    }

    @Test
    public void testWithLocale() {
        Locale locale = Locale.GERMANY;
        StdDateFormat localized = stdDateFormat.withLocale(locale);
        assertNotNull(localized);
        // Should return a new instance with the locale set
        assertNotSame(stdDateFormat, localized);
    }

    @Test
    public void testWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        StdDateFormat tzInstance = stdDateFormat.withTimeZone(tz);
        assertNotNull(tzInstance);
        assertNotSame(stdDateFormat, tzInstance);
    }

    // ---------- Parsing tests ----------

    @Test
    public void testParseISO8601() throws ParseException {
        // Standard ISO8601 with timezone
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.000+0000");
        assertNotNull(date);
        // Verify by formatting back
        String formatted = stdDateFormat.format(date);
        assertTrue(formatted.contains("2020"));
    }

    @Test
    public void testParseISO8601WithColonInTZ() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.000+00:00");
        assertNotNull(date);
    }

    @Test
    public void testParseISO8601NoMillis() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01T00:00:00+0000");
        assertNotNull(date);
    }

    @Test
    public void testParseISO8601NoTZ() throws ParseException {
        // Without timezone, should default to UTC
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.000");
        assertNotNull(date);
    }

    @Test
    public void testParseISO8601WithZ() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.000Z");
        assertNotNull(date);
    }

    @Test
    public void testParseISO8601WithSpace() throws ParseException {
        // Some variants use space instead of 'T'
        Date date = stdDateFormat.parse("2020-01-01 00:00:00.000");
        assertNotNull(date);
    }

    @Test
    public void testParseRFC1123() throws ParseException {
        Date date = stdDateFormat.parse("Wed, 01 Jan 2020 00:00:00 GMT");
        assertNotNull(date);
    }

    @Test
    public void testParseRFC1123WithUSLocale() throws ParseException {
        // Ensure locale doesn't break parsing
        StdDateFormat usFormat = stdDateFormat.withLocale(Locale.US);
        Date date = usFormat.parse("Wed, 01 Jan 2020 00:00:00 GMT");
        assertNotNull(date);
    }

    @Test
    public void testParseCTimestamp() throws ParseException {
        // Unix timestamp in milliseconds
        Date date = stdDateFormat.parse("1577836800000");
        assertNotNull(date);
        assertEquals(1577836800000L, date.getTime());
    }

    @Test
    public void testParseNegativeTimestamp() throws ParseException {
        // Negative timestamp (before epoch)
        Date date = stdDateFormat.parse("-1000000000000");
        assertNotNull(date);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidEmptyString() throws ParseException {
        stdDateFormat.parse("");
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidNull() throws ParseException {
        stdDateFormat.parse(null);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidGarbage() throws ParseException {
        stdDateFormat.parse("not a date");
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidPartial() throws ParseException {
        stdDateFormat.parse("2020-01-01");
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidISO8601MissingTime() throws ParseException {
        stdDateFormat.parse("2020-01-01T");
    }

    // ---------- Formatting tests ----------

    @Test
    public void testFormatDate() {
        Date date = new Date(1577836800000L); // 2020-01-01 00:00:00 UTC
        String formatted = stdDateFormat.format(date);
        // Should produce ISO8601 with timezone
        assertTrue(formatted.startsWith("2020-01-01T00:00:00.000+"));
    }

    @Test
    public void testFormatWithTimeZone() {
        StdDateFormat gmtFormat = stdDateFormat.withTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = new Date(1577836800000L);
        String formatted = gmtFormat.format(date);
        assertEquals("2020-01-01T00:00:00.000+0000", formatted);
    }

    // ---------- Thread safety tests ----------

    @Test
    public void testThreadSafety() throws InterruptedException {
        final int THREAD_COUNT = 10;
        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        final AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        Date parsed = stdDateFormat.parse("2020-01-01T00:00:00.000+0000");
                        assertNotNull(parsed);
                        String formatted = stdDateFormat.format(parsed);
                        assertNotNull(formatted);
                    }
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        assertNull("Thread safety failure: " + error.get(), error.get());
    }

    // ---------- Edge cases ----------

    @Test
    public void testParseYear0000() throws ParseException {
        // Year 0 is valid in some contexts
        Date date = stdDateFormat.parse("0000-01-01T00:00:00.000+0000");
        assertNotNull(date);
    }

    @Test
    public void testParseNegativeYear() throws ParseException {
        // Negative year (e.g., -0001)
        Date date = stdDateFormat.parse("-0001-01-01T00:00:00.000+0000");
        assertNotNull(date);
    }

    @Test
    public void testParseLeapSecond() throws ParseException {
        // 23:59:60 is a valid leap second representation
        Date date = stdDateFormat.parse("2016-12-31T23:59:60.000+0000");
        assertNotNull(date);
    }

    @Test
    public void testParseMaxDate() throws ParseException {
        // Far future date
        Date date = stdDateFormat.parse("9999-12-31T23:59:59.999+0000");
        assertNotNull(date);
    }

    @Test
    public void testParseMinDate() throws ParseException {
        // Far past date
        Date date = stdDateFormat.parse("0001-01-01T00:00:00.000+0000");
        assertNotNull(date);
    }

    // ---------- Additional format variants ----------

    @Test
    public void testParseISO8601WithMillisAndZ() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.123Z");
        assertNotNull(date);
        assertEquals(123, date.getTime() % 1000);
    }

    @Test
    public void testParseISO8601WithPlusSign() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.000+0530");
        assertNotNull(date);
    }

    @Test
    public void testParseISO8601WithNegativeOffset() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.000-0530");
        assertNotNull(date);
    }

    @Test
    public void testParseISO8601WithColonAndMinutes() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.000+05:30");
        assertNotNull(date);
    }

    @Test
    public void testParseISO8601WithSpaceAndNoMillis() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01 00:00:00+0000");
        assertNotNull(date);
    }

    // ---------- Bug-specific tests (Defects4J 104) ----------

    @Test
    public void testParseWithDifferentLocale() throws ParseException {
        // Bug 104 might involve locale-dependent parsing
        StdDateFormat frenchFormat = stdDateFormat.withLocale(Locale.FRANCE);
        // French locale uses different month abbreviations, but ISO8601 should be unaffected
        Date date = frenchFormat.parse("2020-01-01T00:00:00.000+0000");
        assertNotNull(date);
    }

    @Test
    public void testParseRFC1123WithNonEnglishLocale() throws ParseException {
        // RFC1123 uses English day/month abbreviations; other locales may break
        StdDateFormat germanFormat = stdDateFormat.withLocale(Locale.GERMANY);
        try {
            germanFormat.parse("Wed, 01 Jan 2020 00:00:00 GMT");
            // If it fails, that's expected; but we want to ensure no exception is thrown
        } catch (ParseException e) {
            // Acceptable if locale causes failure, but we should still test
        }
    }

    @Test
    public void testParseTimestampWithLeadingZeros() throws ParseException {
        // Timestamp as string with leading zeros? Should be treated as number
        Date date = stdDateFormat.parse("000001577836800000");
        assertNotNull(date);
        assertEquals(1577836800000L, date.getTime());
    }

    @Test
    public void testParseTimestampWithPlusSign() throws ParseException {
        // Positive timestamp with explicit plus
        Date date = stdDateFormat.parse("+1577836800000");
        assertNotNull(date);
    }

    // ---------- Null/empty handling ----------

    @Test(expected = ParseException.class)
    public void testParseBlankString() throws ParseException {
        stdDateFormat.parse("   ");
    }

    @Test(expected = ParseException.class)
    public void testParseOnlySpaces() throws ParseException {
        stdDateFormat.parse("   ");
    }

    // ---------- Additional coverage for internal methods ----------

    @Test
    public void testGetDefaultDateFormat() {
        DateFormat df = stdDateFormat.getDefaultDateFormat();
        assertNotNull(df);
        assertTrue(df instanceof SimpleDateFormat);
    }

    @Test
    public void testGetISO8601DateFormat() {
        DateFormat df = stdDateFormat.getISO8601DateFormat();
        assertNotNull(df);
    }

    @Test
    public void testGetRFC1123DateFormat() {
        DateFormat df = stdDateFormat.getRFC1123DateFormat();
        assertNotNull(df);
    }

    @Test
    public void testGetClone() {
        DateFormat clone = stdDateFormat.getDateFormat();
        assertNotNull(clone);
        // Should be a clone each time
        DateFormat clone2 = stdDateFormat.getDateFormat();
        assertNotSame(clone, clone2);
    }

    // ---------- Timezone handling ----------

    @Test
    public void testParseWithDifferentTimeZone() throws ParseException {
        StdDateFormat tzFormat = stdDateFormat.withTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = tzFormat.parse("2020-01-01T00:00:00.000+0000");
        assertNotNull(date);
        // The parsed date should be the same instant, but formatting will use the new timezone
        String formatted = tzFormat.format(date);
        assertTrue(formatted.contains("-05") || formatted.contains("-04")); // EST or EDT
    }

    @Test
    public void testFormatWithUTCTimeZone() {
        StdDateFormat utcFormat = stdDateFormat.withTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(1577836800000L);
        String formatted = utcFormat.format(date);
        assertEquals("2020-01-01T00:00:00.000+0000", formatted);
    }

    // ---------- Edge cases for format ----------

    @Test
    public void testFormatNull() {
        try {
            stdDateFormat.format((Date) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFormatObject() {
        // format(Object) should delegate to format(Date)
        Date date = new Date(1577836800000L);
        String result = stdDateFormat.format((Object) date);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNonDateObject() {
        stdDateFormat.format("not a date");
    }

    // ---------- Parse with multiple formats ----------

    @Test
    public void testParseMultipleFormatsInSequence() throws ParseException {
        // Ensure parsing different formats works without state corruption
        stdDateFormat.parse("2020-01-01T00:00:00.000+0000");
        stdDateFormat.parse("Wed, 01 Jan 2020 00:00:00 GMT");
        stdDateFormat.parse("1577836800000");
        // Should not throw
    }

    // ---------- Additional coverage for internal patterns ----------

    @Test
    public void testParseISO8601WithFractionalSeconds() throws ParseException {
        // More than 3 fractional digits
        Date date = stdDateFormat.parse("2020-01-01T00:00:00.123456+0000");
        assertNotNull(date);
        // Should truncate to milliseconds
        assertEquals(123, date.getTime() % 1000);
    }

    @Test
    public void testParseISO8601WithNoFraction() throws ParseException {
        Date date = stdDateFormat.parse("2020-01-01T00:00:00+0000");
        assertNotNull(date);
    }

    @Test
    public void testParseISO8601WithDateOnly() throws ParseException {
        // Date only (no time) - should fail? Actually StdDateFormat expects time
        try {
            stdDateFormat.parse("2020-01-01");
            fail("Expected ParseException for date-only input");
        } catch (ParseException e) {
            // expected
        }
    }

    // ---------- Thread safety with timezone ----------

    @Test
    public void testThreadSafetyWithTimeZone() throws InterruptedException {
        final int THREAD_COUNT = 5;
        final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        final AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final TimeZone tz = (i % 2 == 0) ? TimeZone.getTimeZone("GMT") : TimeZone.getTimeZone("America/New_York");
            new Thread(() -> {
                try {
                    StdDateFormat localFormat = stdDateFormat.withTimeZone(tz);
                    for (int j = 0; j < 50; j++) {
                        Date parsed = localFormat.parse("2020-01-01T00:00:00.000+0000");
                        assertNotNull(parsed);
                        String formatted = localFormat.format(parsed);
                        assertNotNull(formatted);
                    }
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        assertNull("Thread safety failure with timezone: " + error.get(), error.get());
    }
}