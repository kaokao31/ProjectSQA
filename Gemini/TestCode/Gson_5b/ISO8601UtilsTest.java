package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ISO8601UtilsTest {

    @Test
    public void testFormatBasic() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = new GregorianCalendar(tz);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 15, 12, 34, 56);
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();

        String formatted = ISO8601Utils.format(date, true, tz);
        assertNotNull(formatted);
        assertTrue(formatted.startsWith("2023-01-15T12:34:56.789"));
        assertTrue(formatted.endsWith("Z"));
    }

    @Test
    public void testFormatWithoutMillis() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = new GregorianCalendar(tz);
        cal.clear();
        cal.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        Date date = cal.getTime();

        String formatted = ISO8601Utils.format(date, false, tz);
        assertNotNull(formatted);
        assertEquals("2023-12-31T23:59:59Z", formatted);
    }

    @Test
    public void testFormatWithCustomTimeZoneOffset() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        Calendar cal = new GregorianCalendar(tz);
        cal.clear();
        cal.set(2020, Calendar.JUNE, 1, 10, 0, 0);
        Date date = cal.getTime();

        String formatted = ISO8601Utils.format(date, false, tz);
        assertNotNull(formatted);
        assertTrue(formatted.endsWith("+02:00") || formatted.endsWith("+0200"));
    }

    @Test
    public void testFormatNoParameters() {
        Date date = new Date(0L); // 1970-01-01T00:00:00.000Z
        String formatted = ISO8601Utils.format(date);
        assertNotNull(formatted);
        assertTrue(formatted.startsWith("1970-01-01T00:00:00"));
    }

    @Test
    public void testParseValidUtcString() throws ParseException {
        String dateStr = "2023-01-15T12:34:56.789Z";
        Date parsed = ISO8601Utils.parse(dateStr, new java.text.ParsePosition(0));
        assertNotNull(parsed);
        
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(parsed);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(34, cal.get(Calendar.MINUTE));
        assertEquals(56, cal.get(Calendar.SECOND));
    }

    @Test
    public void testParseValidOffsetStringPlus() throws ParseException {
        String dateStr = "2023-01-15T12:34:56+02:00";
        Date parsed = ISO8601Utils.parse(dateStr, new java.text.ParsePosition(0));
        assertNotNull(parsed);
    }

    @Test
    public void testParseValidOffsetStringMinus() throws ParseException {
        String dateStr = "2023-01-15T12:34:56-05:00";
        Date parsed = ISO8601Utils.parse(dateStr, new java.text.ParsePosition(0));
        assertNotNull(parsed);
    }

    @Test
    public void testParseWithoutMillisAndNoTz() throws ParseException {
        String dateStr = "2023-01-15T12:34:56";
        Date parsed = ISO8601Utils.parse(dateStr, new java.text.ParsePosition(0));
        assertNotNull(parsed);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidFormatShort() throws ParseException {
        ISO8601Utils.parse("2023", new java.text.ParsePosition(0));
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidDay() throws ParseException {
        ISO8601Utils.parse("2023-01-32T12:34:56Z", new java.text.ParsePosition(0));
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidMonth() throws ParseException {
        ISO8601Utils.parse("2023-13-15T12:34:56Z", new java.text.ParsePosition(0));
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidHour() throws ParseException {
        ISO8601Utils.parse("2023-01-15T25:34:56Z", new java.text.ParsePosition(0));
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidMinute() throws ParseException {
        ISO8601Utils.parse("2023-01-15T12:60:56Z", new java.text.ParsePosition(0));
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidSecond() throws ParseException {
        ISO8601Utils.parse("2023-01-15T12:34:60Z", new java.text.ParsePosition(0));
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidTimeZone() throws ParseException {
        ISO8601Utils.parse("2023-01-15T12:34:56+99:99", new java.text.ParsePosition(0));
    }

    @Test(expected = ParseException.class)
    public void testParseNullString() throws ParseException {
        ISO8601Utils.parse(null, new java.text.ParsePosition(0));
    }
}