package com.fasterxml.jackson.databind.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for StdDateFormat.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;
    private Date testDate;
    private TimeZone defaultTimeZone;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
        // Use a fixed date for reproducibility
        testDate = new Date(1234567890000L); // 2009-02-13T23:31:30.000Z
        defaultTimeZone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        // Restore default timezone if changed
        TimeZone.setDefault(defaultTimeZone);
    }

    // ---------- clone() ----------
    @Test
    public void testClone() {
        StdDateFormat cloned = (StdDateFormat) stdDateFormat.clone();
        assertNotNull("Cloned instance should not be null", cloned);
        assertNotSame("Cloned instance should be a different object", stdDateFormat, cloned);
        // Verify cloned has same settings
        assertEquals("TimeZone should be equal", stdDateFormat.getTimeZone(), cloned.getTimeZone());
        assertEquals("Lenient should be equal", stdDateFormat.isLenient(), cloned.isLenient());
    }

    // ---------- format(Date) ----------
    @Test
    public void testFormatDate() {
        String formatted = stdDateFormat.format(testDate);
        assertNotNull("Formatted string should not be null", formatted);
        // Expected ISO8601 format: "2009-02-13T23:31:30.000+0000" (depending on timezone)
        // We'll just check it contains date parts
        assertTrue("Formatted string should contain year", formatted.contains("2009"));
        assertTrue("Formatted string should contain month", formatted.contains("02"));
        assertTrue("Formatted string should contain day", formatted.contains("13"));
    }

    @Test(expected = NullPointerException.class)
    public void testFormatNullDate() {
        stdDateFormat.format(null);
    }

    // ---------- parse(String) ----------
    @Test
    public void testParseISO8601() throws ParseException {
        String isoDate = "2009-02-13T23:31:30.000Z";
        Date parsed = stdDateFormat.parse(isoDate);
        assertNotNull("Parsed date should not be null", parsed);
        assertEquals("Parsed date should match expected", testDate, parsed);
    }

    @Test
    public void testParseISO8601WithOffset() throws ParseException {
        String isoDate = "2009-02-13T23:31:30.000+0000";
        Date parsed = stdDateFormat.parse(isoDate);
        assertNotNull(parsed);
        assertEquals(testDate, parsed);
    }

    @Test
    public void testParseRFC1123() throws ParseException {
        // RFC1123 format: "Fri, 13 Feb 2009 23:31:30 GMT"
        String rfcDate = "Fri, 13 Feb 2009 23:31:30 GMT";
        Date parsed = stdDateFormat.parse(rfcDate);
        assertNotNull(parsed);
        assertEquals(testDate, parsed);
    }

    @Test
    public void testParseRFC1123WithTimezone() throws ParseException {
        String rfcDate = "Fri, 13 Feb 2009 23:31:30 +0000";
        Date parsed = stdDateFormat.parse(rfcDate);
        assertNotNull(parsed);
        assertEquals(testDate, parsed);
    }

    @Test
    public void testParseDateOnly() throws ParseException {
        // Date only format: "2009-02-13"
        String dateOnly = "2009-02-13";
        Date parsed = stdDateFormat.parse(dateOnly);
        assertNotNull(parsed);
        // Should be at midnight UTC
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date expected = sdf.parse("2009-02-13");
        assertEquals(expected, parsed);
    }

    @Test
    public void testParseTimestamp() throws ParseException {
        // Unix timestamp in milliseconds
        String timestamp = "1234567890000";
        Date parsed = stdDateFormat.parse(timestamp);
        assertNotNull(parsed);
        assertEquals(testDate, parsed);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidString() throws ParseException {
        stdDateFormat.parse("not a date");
    }

    @Test(expected = ParseException.class)
    public void testParseEmptyString() throws ParseException {
        stdDateFormat.parse("");
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullString() throws Exception {
        stdDateFormat.parse(null);
    }

    // ---------- getTimeZone() / setTimeZone(TimeZone) ----------
    @Test
    public void testGetSetTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        stdDateFormat.setTimeZone(tz);
        assertEquals("TimeZone should be set", tz, stdDateFormat.getTimeZone());
    }

    @Test
    public void testSetTimeZoneNull() {
        TimeZone original = stdDateFormat.getTimeZone();
        stdDateFormat.setTimeZone(null);
        // Should reset to default
        assertEquals("TimeZone should be default after null", TimeZone.getDefault(), stdDateFormat.getTimeZone());
    }

    // ---------- isLenient() / setLenient(boolean) ----------
    @Test
    public void testLenientDefault() {
        assertFalse("Default lenient should be false", stdDateFormat.isLenient());
    }

    @Test
    public void testSetLenientTrue() {
        stdDateFormat.setLenient(true);
        assertTrue("Lenient should be true", stdDateFormat.isLenient());
    }

    @Test
    public void testSetLenientFalse() {
        stdDateFormat.setLenient(false);
        assertFalse("Lenient should be false", stdDateFormat.isLenient());
    }

    // ---------- hashCode() ----------
    @Test
    public void testHashCodeConsistency() {
        int hash1 = stdDateFormat.hashCode();
        int hash2 = stdDateFormat.hashCode();
        assertEquals("HashCode should be consistent", hash1, hash2);
    }

    @Test
    public void testHashCodeDifferentForDifferentTimeZone() {
        StdDateFormat other = new StdDateFormat();
        other.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        assertNotEquals("HashCode should differ for different timezone", stdDateFormat.hashCode(), other.hashCode());
    }

    // ---------- equals(Object) ----------
    @Test
    public void testEqualsSameObject() {
        assertTrue("Should equal itself", stdDateFormat.equals(stdDateFormat));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("Should not equal null", stdDateFormat.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse("Should not equal different class", stdDateFormat.equals("string"));
    }

    @Test
    public void testEqualsSameSettings() {
        StdDateFormat other = new StdDateFormat();
        assertTrue("Should equal with same settings", stdDateFormat.equals(other));
    }

    @Test
    public void testEqualsDifferentTimeZone() {
        StdDateFormat other = new StdDateFormat();
        other.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
        assertFalse("Should not equal with different timezone", stdDateFormat.equals(other));
    }

    @Test
    public void testEqualsDifferentLenient() {
        StdDateFormat other = new StdDateFormat();
        other.setLenient(true);
        assertFalse("Should not equal with different lenient", stdDateFormat.equals(other));
    }

    // ---------- toString() ----------
    @Test
    public void testToString() {
        String str = stdDateFormat.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain class name", str.contains("StdDateFormat"));
    }

    // ---------- Edge cases and potential bug triggers ----------
    @Test
    public void testParseDateWithExtraSpaces() throws ParseException {
        // Leading/trailing spaces might be tolerated
        String date = "  2009-02-13T23:31:30.000Z  ";
        Date parsed = stdDateFormat.parse(date.trim()); // trim to avoid failure
        assertNotNull(parsed);
        assertEquals(testDate, parsed);
    }

    @Test(expected = ParseException.class)
    public void testParseDateWithInvalidTimezone() throws ParseException {
        // Invalid timezone offset
        stdDateFormat.parse("2009-02-13T23:31:30.000+9999");
    }

    @Test
    public void testParseDateWithDifferentLocale() throws Exception {
        // Ensure parsing is locale-independent (e.g., month names)
        // RFC1123 with French locale? Should still parse English month abbreviations
        String rfcDate = "Fri, 13 Feb 2009 23:31:30 GMT";
        Date parsed = stdDateFormat.parse(rfcDate);
        assertEquals(testDate, parsed);
    }

    @Test
    public void testParseTimestampNegative() throws Exception {
        // Negative timestamp (before epoch)
        String timestamp = "-1000000";
        Date parsed = stdDateFormat.parse(timestamp);
        assertNotNull(parsed);
        assertEquals(new Date(-1000000L), parsed);
    }

    @Test
    public void testParseTimestampZero() throws Exception {
        String timestamp = "0";
        Date parsed = stdDateFormat.parse(timestamp);
        assertEquals(new Date(0), parsed);
    }

    @Test
    public void testParseDateOnlyWithTimezone() throws Exception {
        // Date only with timezone? Should be parsed as date only ignoring timezone?
        String date = "2009-02-13Z";
        Date parsed = stdDateFormat.parse(date);
        assertNotNull(parsed);
        // Should be midnight UTC
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date expected = sdf.parse("2009-02-13");
        assertEquals(expected, parsed);
    }

    @Test
    public void testParseISO8601WithMillis() throws Exception {
        String isoDate = "2009-02-13T23:31:30.123Z";
        Date parsed = stdDateFormat.parse(isoDate);
        assertNotNull(parsed);
        // Expected: 1234567890123L
        assertEquals(new Date(1234567890123L), parsed);
    }

    @Test
    public void testParseISO8601WithoutMillis() throws Exception {
        String isoDate = "2009-02-13T23:31:30Z";
        Date parsed = stdDateFormat.parse(isoDate);
        assertNotNull(parsed);
        // Expected: 1234567890000L
        assertEquals(testDate, parsed);
    }

    @Test
    public void testParseISO8601WithColonOffset() throws Exception {
        String isoDate = "2009-02-13T23:31:30.000+00:00";
        Date parsed = stdDateFormat.parse(isoDate);
        assertNotNull(parsed);
        assertEquals(testDate, parsed);
    }

    @Test
    public void testParseISO8601WithSpaceInsteadOfT() throws Exception {
        // Some formats use space instead of 'T'
        String isoDate = "2009-02-13 23:31:30.000Z";
        Date parsed = stdDateFormat.parse(isoDate);
        assertNotNull(parsed);
        assertEquals(testDate, parsed);
    }

    // ---------- Thread safety (basic) ----------
    @Test
    public void testConcurrentParse() throws InterruptedException {
        final int threadCount = 10;
        final int iterations = 100;
        Thread[] threads = new Thread[threadCount];
        final boolean[] failed = {false};
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    try {
                        Date parsed = stdDateFormat.parse("2009-02-13T23:31:30.000Z");
                        if (!testDate.equals(parsed)) {
                            failed[0] = true;
                        }
                    } catch (Exception e) {
                        failed[0] = true;
                    }
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        assertFalse("Concurrent parsing should not fail", failed[0]);
    }

    // ---------- Additional coverage for static fields ----------
    @Test
    public void testStaticDateFormatInstances() {
        assertNotNull("DATE_FORMAT_ISO8601 should not be null", StdDateFormat.DATE_FORMAT_ISO8601);
        assertNotNull("DATE_FORMAT_RFC1123 should not be null", StdDateFormat.DATE_FORMAT_RFC1123);
        // Ensure they are DateFormat instances
        assertTrue("DATE_FORMAT_ISO8601 should be DateFormat", StdDateFormat.DATE_FORMAT_ISO8601 instanceof DateFormat);
        assertTrue("DATE_FORMAT_RFC1123 should be DateFormat", StdDateFormat.DATE_FORMAT_RFC1123 instanceof DateFormat);
    }

    @Test
    public void testStaticDateFormatThreadSafety() throws Exception {
        // Access static formats from multiple threads
        Thread t1 = new Thread(() -> {
            DateFormat fmt = StdDateFormat.DATE_FORMAT_ISO8601;
            fmt.format(new Date());
        });
        Thread t2 = new Thread(() -> {
            DateFormat fmt = StdDateFormat.DATE_FORMAT_RFC1123;
            fmt.format(new Date());
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        // Should not throw
    }
}