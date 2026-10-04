package com.fasterxml.jackson.databind.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;

import static org.junit.Assert.*;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;
    private static final TimeZone TZ_GMT = TimeZone.getTimeZone("GMT");
    private static final Locale LOCALE_US = Locale.US;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    @After
    public void tearDown() {
        stdDateFormat = null;
    }

    @Test
    public void testConstantsAndDefaults() {
        assertNotNull(StdDateFormat.instance);
        assertNotNull(StdDateFormat.DATE_FORMAT_STR_ISO8601);
    }

    @Test
    public void testConstructorsAndCloning() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        Locale locale = Locale.GERMAN;
        
        StdDateFormat df1 = new StdDateFormat(tz, locale);
        assertSame(tz, df1.getTimeZone());
        
        StdDateFormat cloned = df1.clone();
        assertNotNull(cloned);
        assertNotSame(df1, cloned);
        
        StdDateFormat dfDefault = new StdDateFormat();
        assertNotNull(dfDefault.getTimeZone());
    }

    @Test
    public void testWithTimeZoneAndLocale() {
        TimeZone tz = TimeZone.getTimeZone("EST");
        Locale locale = Locale.FRANCE;
        
        StdDateFormat df = stdDateFormat.withTimeZone(tz);
        assertSame(tz, df.getTimeZone());
        
        StdDateFormat dfLocale = stdDateFormat.withLocale(locale);
        assertNotNull(dfLocale);
        
        // Testing setTimeZone
        stdDateFormat.setTimeZone(tz);
        assertSame(tz, stdDateFormat.getTimeZone());
    }

    @Test
    public void testGetinstance() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Locale locale = Locale.US;
        DateFormatImplNotStd notStd = new DateFormatImplNotStd();
        
        // test standard instance retrieval if overridden
        assertNotNull(StdDateFormat.getISO8601Format(tz, locale));
        assertNotNull(StdDateFormat.getRFC1123Format(tz, locale));
    }

    @Test
    public void testParseISO8601VariousLengths() throws ParseException {
        // Various ISO-8601 string lengths to hit branches in parseDateTime / parse
        // YYYY-MM-DD
        Date d1 = stdDateFormat.parse("2020-01-01");
        assertNotNull(d1);
        
        // YYYY-MM-DDTHH:mm
        Date d2 = stdDateFormat.parse("2020-01-01T12:00");
        assertNotNull(d2);

        // YYYY-MM-DDTHH:mm:ss
        Date d3 = stdDateFormat.parse("2020-01-01T12:00:30");
        assertNotNull(d3);

        // YYYY-MM-DDTHH:mm:ss.SSS
        Date d4 = stdDateFormat.parse("2020-01-01T12:00:30.123");
        assertNotNull(d4);

        // With 'Z' or timezone offsets
        Date d5 = stdDateFormat.parse("2020-01-01T12:00:30.123Z");
        assertNotNull(d5);

        Date d6 = stdDateFormat.parse("2020-01-01T12:00:30.123+0000");
        assertNotNull(d6);

        Date d7 = stdDateFormat.parse("2020-01-01T12:00:30.123+00:00");
        assertNotNull(d7);
        
        Date d8 = stdDateFormat.parse("2020-01-01T12:00:30.123-0500");
        assertNotNull(d8);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidFormat() throws ParseException {
        stdDateFormat.parse("invalid-date-string");
    }

    @Test
    public void testParseAsISO8601WithParsePosition() {
        ParsePosition pos = new ParsePosition(0);
        Date date = stdDateFormat.parse("2020-06-15T10:15:30.000Z", pos);
        assertNotNull(date);
        assertEquals(-1, pos.getErrorIndex());
        assertTrue(pos.getIndex() > 0);
    }

    @Test
    public void testParseAsRFC1123() {
        // e.g., "Tue, 03 Jun 2008 11:05:30 GMT"
        ParsePosition pos = new ParsePosition(0);
        Date date = stdDateFormat.parse("Tue, 03 Jun 2008 11:05:30 GMT", pos);
        assertNotNull(date);
    }

    @Test
    public void testParseTimestamp() {
        ParsePosition pos = new ParsePosition(0);
        Date date = stdDateFormat.parse("1585785600000", pos);
        assertNotNull(date);
        assertEquals(1585785600000L, date.getTime());
    }

    @Test
    public void testParseBlankOrNull() {
        ParsePosition pos = new ParsePosition(0);
        assertNull(stdDateFormat.parse("", pos));
        
        ParsePosition posNull = new ParsePosition(0);
        assertNull(stdDateFormat.parse(null, posNull));
    }

    @Test(expected = ParseException.class)
    public void testParseThrowsParseExceptionOnInvalidNumber() throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        stdDateFormat.parse("not-a-number-or-date", pos);
    }

    @Test
    public void testFormatDate() {
        Date now = new Date(1585785600000L);
        String formatted = stdDateFormat.format(now);
        assertNotNull(formatted);
        assertTrue(formatted.startsWith("2020-"));
    }

    @Test
    public void testFormatWithStringBufferAndFieldPosition() {
        Date now = new Date(1585785600000L);
        StringBuffer sb = new StringBuffer();
        FieldPosition fp = new FieldPosition(0);
        
        StringBuffer result = stdDateFormat.format(now, sb, fp);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        StdDateFormat df1 = new StdDateFormat(TZ_GMT, LOCALE_US);
        StdDateFormat df2 = new StdDateFormat(TZ_GMT, LOCALE_US);
        StdDateFormat df3 = new StdDateFormat(TimeZone.getTimeZone("PST"), LOCALE_US);

        assertEquals(df1, df1);
        assertEquals(df1, df2);
        assertNotEquals(df1, df3);
        assertNotEquals(df1, null);
        assertNotEquals(df1, "some-string");

        assertEquals(df1.hashCode(), df2.hashCode());
    }

    @Test
    public void testToString() {
        String str = stdDateFormat.toString();
        assertNotNull(str);
        assertTrue(str.contains("StdDateFormat"));
    }

    @Test
    public void testLenientSetting() {
        // Test setLenient if available or via standard methods
        stdDateFormat.setLenient(Boolean.TRUE);
        assertEquals(Boolean.TRUE, stdDateFormat.isLenient());

        stdDateFormat.setLenient(Boolean.FALSE);
        assertEquals(Boolean.FALSE, stdDateFormat.isLenient());
    }

    @Test
    public void testNumericDateParsingEdgeCases() {
        // Test specific digit checks for parse
        ParsePosition pos = new ParsePosition(0);
        // All digits but invalid range or length
        Date d = stdDateFormat.parse("  1585785600000  ", pos);
        assertNotNull(d);
    }

    @Test
    public void testPadInt() {
        StringBuffer sb = new StringBuffer();
        StdDateFormat.padInt(sb, 5);
        StdDateFormat.padInt(sb, 123);
        assertTrue(sb.length() > 0);
    }

    // Helper dummy class if needed
    private static class DateFormatImplNotStd extends java.text.SimpleDateFormat {
        public DateFormatImplNotStd() {
            super("yyyy-MM-dd");
        }
    }
}