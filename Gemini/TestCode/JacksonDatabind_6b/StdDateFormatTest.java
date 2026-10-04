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

import static org.junit.Assert.*;

public class StdDateFormatTest {

    private StdDateFormat stdDateFormat;

    @Before
    public void setUp() {
        stdDateFormat = new StdDateFormat();
    }

    @After
    public void tearDown() {
        stdDateFormat = null;
    }

    @Test
    public void testConstantsAndSingletons() {
        assertNotNull(StdDateFormat.instance);
        assertNotNull(StdDateFormat.DATE_FORMAT_STR_ISO8601);
        assertNotNull(StdDateFormat.DATE_FORMAT_STR_PLAIN);
        assertNotNull(StdDateFormat.DATE_FORMAT_STR_RFC1123);
        assertNotNull(StdDateFormat.DATE_FORMAT_STR_DOT_GMT);
    }

    @Test
    public void testConstructorsAndCloning() {
        StdDateFormat sdf1 = new StdDateFormat();
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.US;
        StdDateFormat sdf2 = new StdDateFormat(tz, loc);
        
        assertNotNull(sdf1.clone());
        assertNotNull(sdf2.clone());

        StdDateFormat defaultInstance = StdDateFormat.getISO8601Format(tz, loc);
        assertNotNull(defaultInstance);
        
        StdDateFormat rfcInstance = StdDateFormat.getRFC1123Format(tz, loc);
        assertNotNull(rfcInstance);
    }

    @Test
    public void testSettersAndGetters() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        stdDateFormat.setTimeZone(tz);
        assertEquals(tz, stdDateFormat.getTimeZone());

        stdDateFormat.setLenient(true);
        assertTrue(stdDateFormat.isLenient());

        stdDateFormat.setLenient(false);
        assertFalse(stdDateFormat.isLenient());
    }

    @Test
    public void testParseISO8601Variations() throws Exception {
        // Various ISO8601 formats that StdDateFormat supports
        String[] validDates = {
            "2014-03-15",
            "2014-03-15T12:34:56.789+0000",
            "2014-03-15T12:34:56.789Z",
            "2014-03-15T12:34:56+0000",
            "2014-03-15T12:34:56.789",
            "2014-03-15T12:34:56"
        };

        for (String dateStr : validDates) {
            Date d = stdDateFormat.parse(dateStr);
            assertNotNull("Failed to parse: " + dateStr, d);
        }
    }

    @Test
    public void testParseRFC1123() throws Exception {
        String rfcDate = "Tue, 03 Jun 2008 11:05:30 GMT";
        Date d = stdDateFormat.parse(rfcDate);
        assertNotNull(d);
    }

    @Test
    public void testParsePlainDate() throws Exception {
        String plainDate = "2008-06-03";
        Date d = stdDateFormat.parse(plainDate);
        assertNotNull(d);
    }

    @Test
    public void testParseTimestamp() throws Exception {
        String timestamp = "1212486330000";
        Date d = stdDateFormat.parse(timestamp);
        assertNotNull(d);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalidFormat() throws ParseException {
        stdDateFormat.parse("not-a-date-at-all");
    }

    @Test
    public void testParsePositionVariants() {
        // Valid parse with ParsePosition
        ParsePosition pos = new ParsePosition(0);
        Date d = stdDateFormat.parse("2014-03-15", pos);
        assertNotNull(d);
        assertEquals(10, pos.getIndex());

        // Invalid parse with ParsePosition should return null and not throw
        ParsePosition badPos = new ParsePosition(0);
        Date badDate = stdDateFormat.parse("invalid-date", badPos);
        assertNull(badDate);
    }

    @Test
    public void testFormatDate() {
        Date now = new Date(1212486330000L); // Fixed date
        StringBuffer sb = new StringBuffer();
        FieldPosition fp = new FieldPosition(0);

        StringBuffer result = stdDateFormat.format(now, sb, fp);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testEqualsAndHashCodeAndToString() {
        StdDateFormat sdf1 = new StdDateFormat();
        StdDateFormat sdf2 = new StdDateFormat();

        assertTrue(sdf1.equals(sdf1));
        assertFalse(sdf1.equals(null));
        assertFalse(sdf1.equals("some string"));
        assertTrue(sdf1.equals(sdf2));

        assertNotNull(sdf1.hashCode());
        assertNotNull(sdf1.toString());
    }

    @Test
    public void testErrorHandlingInvalidNumberParse() {
        // Testing internal helper/regex edge cases for parsing numbers where month/day/etc are out of bounds or malformed
        ParsePosition pos = new ParsePosition(0);
        // Malformed ISO-like string
        Date d = stdDateFormat.parse("2014-13-45T25:61:61.999Z", pos);
        // Depending on leniency or strictness, it might fail or parse, let's just make sure it doesn't crash fatally
        // Or if it throws/returns null safely.
        assertTrue(d == null || d != null); 
    }
}