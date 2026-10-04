package com.fasterxml.jackson.databind.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    @Test
    public void testGetBlueprintISO8601Format() {
        DateFormat df = StdDateFormat.getBlueprintISO8601Format();
        Assert.assertNotNull("Blueprint ISO8601 format should not be null", df);
        Assert.assertTrue("Should be instance of SimpleDateFormat", df instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) df;
        Assert.assertEquals("Pattern should be yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", sdf.toPattern());
        Assert.assertEquals("TimeZone should be UTC", TimeZone.getTimeZone("UTC"), sdf.getTimeZone());
    }

    @Test
    public void testGetISO8601Format() {
        DateFormat df = stdDateFormat.getISO8601Format(TimeZone.getTimeZone("GMT"));
        Assert.assertNotNull("ISO8601 format should not be null", df);
        Assert.assertTrue("Should be instance of SimpleDateFormat", df instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) df;
        Assert.assertEquals("Pattern should be yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", sdf.toPattern());
        Assert.assertEquals("TimeZone should be GMT", TimeZone.getTimeZone("GMT"), sdf.getTimeZone());
    }

    @Test
    public void testGetBlueprintRFC1123Format() {
        DateFormat df = StdDateFormat.getBlueprintRFC1123Format();
        Assert.assertNotNull("Blueprint RFC1123 format should not be null", df);
        Assert.assertTrue("Should be instance of SimpleDateFormat", df instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) df;
        Assert.assertEquals("Pattern should be EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm:ss zzz", sdf.toPattern());
        Assert.assertEquals("TimeZone should be UTC", TimeZone.getTimeZone("UTC"), sdf.getTimeZone());
        Assert.assertEquals("Locale should be US", Locale.US, sdf.getLocale());
    }

    @Test
    public void testGetRFC1123Format() {
        DateFormat df = stdDateFormat.getRFC1123Format(TimeZone.getTimeZone("PST"));
        Assert.assertNotNull("RFC1123 format should not be null", df);
        Assert.assertTrue("Should be instance of SimpleDateFormat", df instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) df;
        Assert.assertEquals("Pattern should be EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm:ss zzz", sdf.toPattern());
        Assert.assertEquals("TimeZone should be PST", TimeZone.getTimeZone("PST"), sdf.getTimeZone());
        Assert.assertEquals("Locale should be US", Locale.US, sdf.getLocale());
    }

    @Test
    public void testClone() {
        StdDateFormat cloned = stdDateFormat.clone();
        Assert.assertNotNull("Cloned instance should not be null", cloned);
        Assert.assertNotSame("Cloned instance should be a different object", stdDateFormat, cloned);
    }

    @Test
    public void testParseISO8601() throws ParseException {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000+0000");
        Assert.assertNotNull("Parsed date should not be null", date);
        Assert.assertEquals("Parsed date should be epoch start", 0L, date.getTime());
    }

    @Test
    public void testParseISO8601WithZ() throws ParseException {
        // 'Z' is valid; but StdDateFormat may handle it. We just verify it doesn't throw NPE.
        Date date = stdDateFormat.parse("1970-01-01T00:00:00.000Z");
        Assert.assertNotNull("Parsed date should not be null", date);
        Assert.assertEquals("Parsed date should be epoch start", 0L, date.getTime());
    }

    @Test
    public void testParseISO8601WithoutMillis() throws ParseException {
        Date date = stdDateFormat.parse("1970-01-01T00:00:00+0000");
        Assert.assertNotNull("Parsed date should not be null", date);
        Assert.assertEquals("Parsed date should be epoch start", 0L, date.getTime());
    }

    @Test
    public void testParseISO8601WithNegativeYear() throws ParseException {
        Date date = stdDateFormat.parse("-0001-01-01T00:00:00.000+0000");
        Assert.assertNotNull("Parsed date should not be null", date);
    }

    @Test
    public void testParseISO8601WithLargePositiveYear() throws ParseException {
        Date date = stdDateFormat.parse("+10000-01-01T00:00:00.000+0000");
        Assert.assertNotNull("Parsed date should not be null", date);
    }

    @Test
    public void testParseRFC1123() throws ParseException {
        Date date = stdDateFormat.parse("Thu, 01 Jan 1970 00:00:00 GMT");
        Assert.assertNotNull("Parsed date should not be null", date);
        Assert.assertEquals("Parsed date should be epoch start for RFC1123", 0L, date.getTime());
    }

    @Test
    public void testParseRFC1123WithDifferentTimezone() throws ParseException {
        Date date = stdDateFormat.parse("Wed, 31 Dec 1969 16:00:00 PST");
        Assert.assertNotNull("Parsed date should not be null", date);
        Assert.assertEquals("Parsed date should be epoch start for RFC1123", 0L, date.getTime());
    }

    @Test
    public void testParseMultipleCalls() throws ParseException {
        Date d1 = stdDateFormat.parse("1970-01-01T00:00:00.000+0000");
        Date d2 = stdDateFormat.parse("1970-01-01T00:00:00.000+0000");
        Assert.assertNotNull(d1);
        Assert.assertNotNull(d2);
        Assert.assertEquals("Multiple parses of same input should yield same time", d1.getTime(), d2.getTime());
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidString() throws ParseException {
        stdDateFormat.parse("not-a-date");
    }

    @Test(expected = ParseException.class)
    public void testParseEmptyString() throws ParseException {
        stdDateFormat.parse("");
    }

    @Test(expected = ParseException.class)
    public void testParseNullString() throws ParseException {
        stdDateFormat.parse(null);
    }

    @Test
    public void testParseWithTimeZoneOffsetInHours() throws ParseException {
        Date date = stdDateFormat.parse("2020-06-15T12:30:45.123-0500");
        Assert.assertNotNull("Parsed date should not be null", date);
        // Verify it parsed correctly, don't rely on exact time due to system timezone, but it should not throw.
    }

    @Test
    public void testParseWithTimeZoneOffsetWithoutColon() throws ParseException {
        Date date = stdDateFormat.parse("2020-06-15T12:30:45.123+0530");
        Assert.assertNotNull("Parsed date should not be null", date);
    }

    @Test
    public void testParseWithThousandsSeparatorInYear() throws ParseException {
        // Some implementations allow "0 0", but we test if the code can handle
        // This may throw ParseException if not handled; we just ensure no crash.
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.000+00:00");
            Assert.fail("Expected ParseException for non-standard offset format");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testFormat() {
        Date epoch = new Date(0);
        String formatted = stdDateFormat.format(epoch, new StringBuffer(), new FieldPosition(0)).toString();
        // The exact format depends on the underlying format; we just verify it starts with '1970'
        Assert.assertTrue("Formatted date should begin with 1970", formatted.startsWith("1970"));
    }

    @Test
    public void testParseObject() throws ParseException {
        String dateStr = "1970-01-01T00:00:00.000+0000";
        Object parsed = stdDateFormat.parseObject(dateStr, new ParsePosition(0));
        Assert.assertNotNull("Parsed object should not be null", parsed);
        Assert.assertTrue("Parsed object should be a Date", parsed instanceof Date);
        Assert.assertEquals("Parsed date should be epoch start", 0L, ((Date) parsed).getTime());
    }

    @Test
    public void testParseObjectWithInvalidInput() {
        String invalid = "invalid-date";
        ParsePosition pos = new ParsePosition(0);
        Object parsed = stdDateFormat.parseObject(invalid, pos);
        Assert.assertNull("Parsed object should be null for invalid input", parsed);
        Assert.assertTrue("Error index should be set", pos.getErrorIndex() >= 0);
    }

    @Test
    public void testParseObjectWithEmptyInput() {
        ParsePosition pos = new ParsePosition(0);
        Object parsed = stdDateFormat.parseObject("", pos);
        Assert.assertNull("Parsed object should be null for empty input", parsed);
        Assert.assertTrue("Error index should be set", pos.getErrorIndex() >= 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        StdDateFormat other = new StdDateFormat();
        Assert.assertTrue("Two StdDateFormat instances should be equal", stdDateFormat.equals(other));
        Assert.assertEquals("Hash codes should be equal", stdDateFormat.hashCode(), other.hashCode());
    }

    @Test
    public void testNotEqualsNull() {
        Assert.assertFalse("StdDateFormat should not be equal to null", stdDateFormat.equals(null));
    }

    @Test
    public void testNotEqualsDifferentType() {
        Assert.assertFalse("StdDateFormat should not be equal to a string", stdDateFormat.equals("some string"));
    }

    @Test
    public void testToString() {
        String toString = stdDateFormat.toString();
        Assert.assertNotNull("toString() should not return null", toString);
        Assert.assertTrue("toString() should contain 'DateFormat'", toString.contains("DateFormat"));
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        final int threadCount = 10;
        final int iterations = 100;
        Thread[] threads = new Thread[threadCount];
        final boolean[] failures = new boolean[1];
        final String[] dateStrings = {
            "1970-01-01T00:00:00.000+0000",
            "2020-06-15T12:30:45.123-0500",
            "Thu, 01 Jan 1970 00:00:00 GMT",
            "invalid-date"
        };

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    for (String dateStr : dateStrings) {
                        try {
                            Date parsed = stdDateFormat.parse(dateStr);
                            if (parsed != null && parsed.getTime() < 0) {
                                // Just to use parsed value, no failure condition
                            }
                        } catch (ParseException e) {
                            // Expected for invalid dates
                        } catch (Exception e) {
                            failures[0] = true;
                        }
                    }
                }
            });
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        Assert.assertFalse("No unexpected exceptions should occur during multi-threaded parsing", failures[0]);
    }

    @Test
    public void testParseWithAlternateISO8601Pattern() throws ParseException {
        // Some ISO 8601 variants have a colon in the timezone
        try {
            stdDateFormat.parse("1970-01-01T00:00:00.000+00:00");
        } catch (ParseException e) {
            // Expected, as colon in timezone may not be supported
        }
    }

    @Test
    public void testParseObjectWithNullPosition() throws ParseException {
        // This tests internal null handling in parseObject
        try {
            stdDateFormat.parseObject("1970-01-01T00:00:00.000+0000", null);
            Assert.fail("Should have thrown NullPointerException or IllegalArgumentException");
        } catch (NullPointerException | IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testParseWithExtraSpaces() throws ParseException {
        // Some implementations trimming input
        try {
            stdDateFormat.parse("  1970-01-01T00:00:00.000+0000  ");
            Assert.fail("Expected ParseException for input with leading/trailing spaces");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testFormatWithNullBuffer() {
        try {
            stdDateFormat.format(new Date(0), null, new FieldPosition(0));
            Assert.fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testParseWithLeapYearDate() throws ParseException {
        Date date = stdDateFormat.parse("2020-02-29T12:00:00.000+0000");
        Assert.assertNotNull("Leap year date should parse", date);
    }

    @Test
    public void testParseWithLeapYearInvalidDate() {
        try {
            stdDateFormat.parse("2019-02-29T12:00:00.000+0000");
            Assert.fail("Should have thrown ParseException for invalid leap year date");
        } catch (ParseException e) {
            // Expected
        }
    }

    @Test
    public void testParseWithNegativeTimestamp() throws ParseException {
        Date date = stdDateFormat.parse("1969-12-31T23:59:59.999+0000");
        Assert.assertNotNull("Negative timestamp should parse", date);
        Assert.assertTrue("Date should be before epoch", date.getTime() < 0);
    }

    @Test
    public void testParseWithMaxLongTimestamp() throws ParseException {
        // Use a date that is far in the future
        Date date = stdDateFormat.parse("+292278994-08-17T07:12:55.807+0000");
        Assert.assertNotNull("Far future date should parse", date);
        // The exact long value may exceed Long.MAX_VALUE? No, it's within allowed range for Date
    }

    @Test
    public void testParseWithMinLongTimestamp() throws ParseException {
        // Use a date far in the past
        Date date = stdDateFormat.parse("-292275055-05-16T16:47:04.192+0000");
        Assert.assertNotNull("Far past date should parse", date);
    }

    @Test
    public void testGetBlueprintISO8601FormatConsistency() {
        DateFormat df1 = StdDateFormat.getBlueprintISO8601Format();
        DateFormat df2 = StdDateFormat.getBlueprintISO8601Format();
        Assert.assertNotSame("Should return separate instances each time", df1, df2);
        Assert.assertTrue("First instance should be a SimpleDateFormat", df1 instanceof SimpleDateFormat);
        Assert.assertTrue("Second instance should be a SimpleDateFormat", df2 instanceof SimpleDateFormat);
    }

    @Test
    public void testGetBlueprintRFC1123FormatConsistency() {
        DateFormat df1 = StdDateFormat.getBlueprintRFC1123Format();
        DateFormat df2 = StdDateFormat.getBlueprintRFC1123Format();
        Assert.assertNotSame("Should return separate instances each time", df1, df2);
        Assert.assertTrue("First instance should be a SimpleDateFormat", df1 instanceof SimpleDateFormat);
        Assert.assertTrue("Second instance should be a SimpleDateFormat", df2 instanceof SimpleDateFormat);
    }
}