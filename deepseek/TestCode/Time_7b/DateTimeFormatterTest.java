package org.joda.time.format;

import org.junit.Test;
import static org.junit.Assert.*;
import org.joda.time.*;
import org.joda.time.format.*;
import org.joda.time.tz.*;

public class DateTimeFormatterTest {

    // -----------------------------------------------------------------------
    // Test parsing with timezone offset without colon (pattern "Z")
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithOffsetWithoutColon() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00.000+0530");
        assertEquals(2012, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(30, dt.getDayOfMonth());
        assertEquals(12, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
        assertEquals(DateTimeZone.forID("+05:30"), dt.getZone());
    }

    // -----------------------------------------------------------------------
    // Test parsing with timezone offset with colon (pattern "ZZ")
    // This is the core scenario for Defects4J bug 7.
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithOffsetWithColon() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00.000+05:30");
        assertEquals(2012, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(30, dt.getDayOfMonth());
        assertEquals(12, dt.getHourOfDay());
        assertEquals(0, dt.getMinuteOfHour());
        assertEquals(0, dt.getSecondOfMinute());
        assertEquals(0, dt.getMillisOfSecond());
        assertEquals(DateTimeZone.forID("+05:30"), dt.getZone());
    }

    // -----------------------------------------------------------------------
    // Test parsing with zero offset (without colon)
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithZeroOffsetWithoutColon() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00.000+0000");
        assertEquals(DateTimeZone.UTC, dt.getZone());
    }

    // -----------------------------------------------------------------------
    // Test parsing with zero offset (with colon)
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithZeroOffsetWithColon() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00.000+00:00");
        assertEquals(DateTimeZone.UTC, dt.getZone());
    }

    // -----------------------------------------------------------------------
    // Test parsing with negative offset (without colon)
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithNegativeOffsetWithoutColon() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00.000-0530");
        assertEquals(DateTimeZone.forID("-05:30"), dt.getZone());
    }

    // -----------------------------------------------------------------------
    // Test parsing with negative offset (with colon)
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithNegativeOffsetWithColon() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00.000-05:30");
        assertEquals(DateTimeZone.forID("-05:30"), dt.getZone());
    }

    // -----------------------------------------------------------------------
    // Test parsing with UTC timezone designator "Z"
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithUTCDesignator() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00.000Z");
        assertEquals(DateTimeZone.UTC, dt.getZone());
    }

    // -----------------------------------------------------------------------
    // Test parsing with milliseconds
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithMilliseconds() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00.123+0530");
        assertEquals(123, dt.getMillisOfSecond());
    }

    // -----------------------------------------------------------------------
    // Test parsing of a date without timezone (pattern without Z)
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithoutTimezone() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = fmt.parseDateTime("2012-06-30");
        assertEquals(2012, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(30, dt.getDayOfMonth());
    }

    // -----------------------------------------------------------------------
    // Test formatting with timezone offset without colon (pattern "Z")
    // -----------------------------------------------------------------------
    @Test
    public void testFormatWithOffsetWithoutColon() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTime dt = new DateTime(2012, 6, 30, 12, 0, 0, 0, DateTimeZone.forID("+05:30"));
        String formatted = fmt.print(dt);
        assertTrue(formatted.endsWith("+0530"));
    }

    // -----------------------------------------------------------------------
    // Test formatting with timezone offset with colon (pattern "ZZ")
    // -----------------------------------------------------------------------
    @Test
    public void testFormatWithOffsetWithColon() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        DateTime dt = new DateTime(2012, 6, 30, 12, 0, 0, 0, DateTimeZone.forID("+05:30"));
        String formatted = fmt.print(dt);
        assertTrue(formatted.endsWith("+05:30"));
    }

    // -----------------------------------------------------------------------
    // Test formatting with UTC timezone
    // -----------------------------------------------------------------------
    @Test
    public void testFormatWithUTC() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTime dt = new DateTime(2012, 6, 30, 12, 0, 0, 0, DateTimeZone.UTC);
        String formatted = fmt.print(dt);
        assertTrue(formatted.endsWith("Z"));
    }

    // -----------------------------------------------------------------------
    // Test that parsing an invalid string throws IllegalArgumentException
    // -----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        fmt.parseDateTime("not a date");
    }

    // -----------------------------------------------------------------------
    // Test that parsing a null string throws IllegalArgumentException
    // -----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testParseNullString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        fmt.parseDateTime(null);
    }

    // -----------------------------------------------------------------------
    // Test that parsing an empty string throws IllegalArgumentException
    // -----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testParseEmptyString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        fmt.parseDateTime("");
    }

    // -----------------------------------------------------------------------
    // Test that the formatter is not null after creation
    // -----------------------------------------------------------------------
    @Test
    public void testFormatterNotNull() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNotNull(fmt);
    }

    // -----------------------------------------------------------------------
    // Test that getParser() returns a non-null parser
    // -----------------------------------------------------------------------
    @Test
    public void testGetParser() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNotNull(fmt.getParser());
    }

    // -----------------------------------------------------------------------
    // Test that getPrinter() returns a non-null printer
    // -----------------------------------------------------------------------
    @Test
    public void testGetPrinter() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNotNull(fmt.getPrinter());
    }

    // -----------------------------------------------------------------------
    // Test printTo(StringBuffer)
    // -----------------------------------------------------------------------
    @Test
    public void testPrintToStringBuffer() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = new DateTime(2012, 6, 30, 0, 0, 0, 0);
        StringBuffer buf = new StringBuffer();
        fmt.printTo(buf, dt);
        assertEquals("2012-06-30", buf.toString());
    }

    // -----------------------------------------------------------------------
    // Test that the formatter is immutable (multiple uses produce same result)
    // -----------------------------------------------------------------------
    @Test
    public void testFormatterImmutability() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt1 = new DateTime(2012, 6, 30, 0, 0, 0, 0);
        DateTime dt2 = new DateTime(2013, 7, 4, 0, 0, 0, 0);
        assertEquals("2012-06-30", fmt.print(dt1));
        assertEquals("2013-07-04", fmt.print(dt2));
    }

    // -----------------------------------------------------------------------
    // Test parsing with a pattern that includes literal text
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithLiteralText() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("'Date:' yyyy-MM-dd");
        DateTime dt = fmt.parseDateTime("Date: 2012-06-30");
        assertEquals(2012, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(30, dt.getDayOfMonth());
    }

    // -----------------------------------------------------------------------
    // Test parsing with a pattern that includes optional parts
    // -----------------------------------------------------------------------
    @Test
    public void testParseWithOptionalParts() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd['T'HH:mm:ss]");
        DateTime dt = fmt.parseDateTime("2012-06-30T12:00:00");
        assertEquals(12, dt.getHourOfDay());
        // also test without optional part
        DateTime dt2 = fmt.parseDateTime("2012-06-30");
        assertEquals(0, dt2.getHourOfDay());
    }

    // -----------------------------------------------------------------------
    // Test that parseInto returns the correct position (if method exists)
    // -----------------------------------------------------------------------
    @Test
    public void testParseInto() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeParserBucket bucket = new DateTimeParserBucket();
        String text = "2012-06-30";
        int result = fmt.parseInto(bucket, text, 0);
        assertEquals(text.length(), result);
        // verify bucket contains parsed date
        DateTime dt = bucket.computeDateTime();
        assertEquals(2012, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(30, dt.getDayOfMonth());
    }

    // -----------------------------------------------------------------------
    // Test that parseInto returns -1 for invalid input
    // -----------------------------------------------------------------------
    @Test
    public void testParseIntoInvalid() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeParserBucket bucket = new DateTimeParserBucket();
        String text = "not a date";
        int result = fmt.parseInto(bucket, text, 0);
        assertEquals(-1, result);
    }
}