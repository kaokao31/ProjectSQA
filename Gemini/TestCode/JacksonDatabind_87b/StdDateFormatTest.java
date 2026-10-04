package com.fasterxml.jackson.databind.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.util.Calendar;

import static org.junit.Assert.*;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;
    private TimeZone originalTimeZone;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalTimeZone = TimeZone.getDefault();
        originalLocale = Locale.getDefault();
        stdDateFormat = new StdDateFormat();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(originalTimeZone);
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testConstantsAndDefaults() {
        assertNotNull(StdDateFormat.instance);
        assertNotNull(StdDateFormat.DATE_FORMAT_STR_ISO8601);
    }

    @Test
    public void testConstructorsAndClone() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.US;
        StdDateFormat sdf = new StdDateFormat(tz, loc);

        StdDateFormat cloned = sdf.clone();
        assertNotNull(cloned);
        assertNotSame(sdf, cloned);

        StdDateFormat sdfWithBoolean = new StdDateFormat(tz, loc, true);
        assertNotNull(sdfWithBoolean);
        
        StdDateFormat sdfWithLenient = new StdDateFormat(tz, loc, true, Boolean.TRUE);
        assertNotNull(sdfWithLenient);
    }

    @Test
    public void testWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        StdDateFormat modified = stdDateFormat.withTimeZone(tz);
        assertNotNull(modified);
        assertEquals(tz, modified.getTimeZone());

        // Test with same timezone
        assertSame(stdDateFormat, stdDateFormat.withTimeZone(stdDateFormat.getTimeZone()));
        
        // Test with null timezone
        StdDateFormat nullTzSdf = stdDateFormat.withTimeZone(null);
        assertNotNull(nullTzSdf);
    }

    @Test
    public void testWithLocale() {
        Locale loc = Locale.FRANCE;
        StdDateFormat modified = stdDateFormat.withLocale(loc);
        assertNotNull(modified);

        // Test with same locale
        assertSame(stdDateFormat, stdDateFormat.withLocale(stdDateFormat.getLocale()));
    }

    @Test
    public void testSetTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        stdDateFormat.setTimeZone(tz);
        assertEquals(tz, stdDateFormat.getTimeZone());
    }

    @Test
    public void testSetLenient() {
        stdDateFormat.setLenient(true);
        assertTrue(stdDateFormat.isLenient());

        stdDateFormat.setLenient(false);
        assertFalse(stdDateFormat.isLenient());
    }

    @Test
    public void testGetTimeZone() {
        assertNotNull(stdDateFormat.getTimeZone());
    }

    @Test
    public void testHashCodeAndToString() {
        assertTrue(stdDateFormat.hashCode() != 0);
        assertNotNull(stdDateFormat.toString());
    }

    @Test
    public void testParseISO8601Dates() throws Exception {
        // Full ISO8601 variations
        String[] dates = {
            "2015-01-01T00:00:00.000+0000",
            "2015-01-01T00:00:00.000+00",
            "2015-01-01T00:00:00.000Z",
            "2015-01-01T00:00:00+0000",
            "2015-01-01T00:00:00+00",
            "2015-01-01T00:00:00Z",
            "2015-01-01",
            "2015-01-01T00:00",
            "2015-01-01T00:00:00"
        };

        for (String dateStr : dates) {
            Date date = stdDateFormat.parse(dateStr);
            assertNotNull("Failed to parse: " + dateStr, date);
        }
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidDate() throws Exception {
        stdDateFormat.parse("invalid-date-string");
    }

    @Test
    public void testParseAsISO8601WithParsePosition() {
        ParsePosition pos = new ParsePosition(0);
        Date date = stdDateFormat.parse("2015-01-01T00:00:00.000+0000", pos);
        assertNotNull(date);
        assertEquals(-1, pos.getErrorIndex());
    }

    @Test
    public void testParseAsISO8601InvalidPosition() {
        ParsePosition pos = new ParsePosition(0);
        Date date = stdDateFormat.parse("not-a-date", pos);
        assertNull(date);
        assertTrue(pos.getErrorIndex() >= 0 || pos.getIndex() >= 0);
    }

    @Test
    public void testParsePlainTimestamp() throws Exception {
        String timestamp = "1420070400000";
        Date date = stdDateFormat.parse(timestamp);
        assertNotNull(date);
        assertEquals(1420070400000L, date.getTime());
    }

    @Test
    public void testParseBlankString() {
        ParsePosition pos = new ParsePosition(0);
        Date date = stdDateFormat.parse("   ", pos);
        assertNull(date);
    }

    @Test(expected = ParseException.class)
    public void testParseBlankStringThrows() throws ParseException {
        stdDateFormat.parse("   ");
    }

    @Test
    public void testFormatDate() {
        Date date = new Date(1420070400000L);
        String formatted = stdDateFormat.format(date);
        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
    }

    @Test
    public void testFormatCalendar() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTimeInMillis(1420070400000L);
        
        StringBuffer sb = new StringBuffer();
        FieldPosition fp = new FieldPosition(0);
        StringBuffer result = stdDateFormat.format(cal, sb, fp);
        assertNotNull(result);
        assertFalse(result.toString().isEmpty());
    }

    @Test
    public void testInstanceMethods() {
        // Testing static helpers exposed by StdDateFormat
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Locale loc = Locale.US;
        
        StdDateFormat customInstance = new StdDateFormat(tz, loc);
        assertNotNull(customInstance);
    }

    @Test
    public void testEquals() {
        StdDateFormat sdf1 = new StdDateFormat(TimeZone.getDefault(), Locale.getDefault());
        StdDateFormat sdf2 = new StdDateFormat(TimeZone.getDefault(), Locale.getDefault());
        
        assertTrue(sdf1.equals(sdf2));
        assertTrue(sdf1.equals(sdf1));
        assertFalse(sdf1.equals(null));
        assertFalse(sdf1.equals("some string"));
    }
}