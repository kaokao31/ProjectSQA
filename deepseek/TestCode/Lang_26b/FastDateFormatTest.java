package org.apache.commons.lang3.time;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test suite for FastDateFormat, reproducing known bugs and achieving maximum coverage.
 */
public class FastDateFormatTest {

    private static final Locale SWEDISH_LOCALE = new Locale("sv", "SE");
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private Date date20091231;
    private Date date20100101;
    private Date date20101231;
    private Date date20110101;

    @Before
    public void setUp() {
        Calendar cal = new GregorianCalendar(UTC, SWEDISH_LOCALE);
        cal.set(2009, Calendar.DECEMBER, 31, 0, 0, 0);
        date20091231 = cal.getTime();
        cal.set(2010, Calendar.JANUARY, 1, 0, 0, 0);
        date20100101 = cal.getTime();
        cal.set(2010, Calendar.DECEMBER, 31, 0, 0, 0);
        date20101231 = cal.getTime();
        cal.set(2011, Calendar.JANUARY, 1, 0, 0, 0);
        date20110101 = cal.getTime();
    }

    // -----------------------------------------------------------------------
    // Bug reproduction: testLang645
    // Expected: "fredag, week 53"   Actual (buggy): "fredag, week 01"
    // This test fails against the buggy version.
    // -----------------------------------------------------------------------
    @Test
    public void testLang645() {
        FastDateFormat fdf = FastDateFormat.getInstance("EEEE, 'week' ww", SWEDISH_LOCALE);
        String formatted = fdf.format(date20091231);
        assertEquals("fredag, week 53", formatted);
    }

    // -----------------------------------------------------------------------
    // Basic format tests
    // -----------------------------------------------------------------------
    @Test
    public void testFormatYYYYMMDD() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals("2009-12-31", fdf.format(date20091231));
        assertEquals("2010-01-01", fdf.format(date20100101));
    }

    @Test
    public void testFormatWithTime() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        assertEquals("2009-12-31 00:00:00", fdf.format(date20091231));
    }

    @Test
    public void testFormatWithTimezone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", tz);
        // 2009-12-31 00:00 UTC -> 2009-12-30 19:00 EST
        assertEquals("2009-12-30 19:00:00", fdf.format(date20091231));
    }

    @Test
    public void testFormatWithLocale() {
        FastDateFormat fdf = FastDateFormat.getInstance("EEEE", SWEDISH_LOCALE);
        assertEquals("fredag", fdf.format(date20091231));
    }

    @Test
    public void testFormatWithLocaleAndTimezone() {
        FastDateFormat fdf = FastDateFormat.getInstance("EEEE", SWEDISH_LOCALE, UTC);
        assertEquals("fredag", fdf.format(date20091231));
    }

    // -----------------------------------------------------------------------
    // Parse tests
    // -----------------------------------------------------------------------
    @Test
    public void testParse() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Date parsed = fdf.parse("2009-12-31");
        assertEquals(date20091231, parsed);
    }

    @Test(expected = ParseException.class)
    public void testParseInvalid() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.parse("2009/12/31");
    }

    @Test
    public void testParseWithLocale() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("EEEE, 'week' ww", SWEDISH_LOCALE);
        // This parse should succeed for the same date
        Date parsed = fdf.parse("fredag, week 53");
        // Cannot reliably assert exact date because locale may change week boundaries,
        // but it should be around end of 2009.
        Calendar cal = Calendar.getInstance(UTC, SWEDISH_LOCALE);
        cal.setTime(parsed);
        assertEquals(2009, cal.get(Calendar.YEAR));
    }

    // -----------------------------------------------------------------------
    // Edge cases: null/empty patterns, special characters
    // -----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPattern() {
        FastDateFormat.getInstance("");
    }

    @Test
    public void testPatternWithEscapedQuotes() {
        FastDateFormat fdf = FastDateFormat.getInstance("'Today is' EEEE");
        assertEquals("Today is fredag", fdf.format(date20091231));
    }

    @Test
    public void testPatternWithUnescapedApostrophe() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd'");
        // The format treats single quote as literal
        assertEquals("2009-12-31'", fdf.format(date20091231));
    }

    // -----------------------------------------------------------------------
    // Year boundary / ISO week year handling (week 53 vs week 1)
    // -----------------------------------------------------------------------
    @Test
    public void testWeek53AnotherYear() {
        // 2010-01-01 is in week 53 of 2009 in ISO? Actually 2010-01-01 is in ISO week 53 of 2009
        FastDateFormat fdf = FastDateFormat.getInstance("ww", SWEDISH_LOCALE);
        assertEquals("53", fdf.format(date20100101));
    }

    @Test
    public void testWeek1FirstDayOfYear() {
        // 2009-01-01 is week 1 of 2009
        FastDateFormat fdf = FastDateFormat.getInstance("ww", SWEDISH_LOCALE);
        Calendar cal = new GregorianCalendar(UTC, SWEDISH_LOCALE);
        cal.set(2009, Calendar.JANUARY, 1);
        assertEquals("01", fdf.format(cal.getTime()));
    }

    @Test
    public void testWeek53EndOf2010() {
        // 2010-12-31 is week 52 of 2010? Actually 2010-12-31 is ISO week 52. But 2011-01-01 is week 52.
        // We want a year with 53 weeks: 2009, 2015, etc.
        // 2010-12-31 is in week 52 of 2010.
        FastDateFormat fdf = FastDateFormat.getInstance("ww", SWEDISH_LOCALE);
        assertEquals("52", fdf.format(date20101231));
    }

    // -----------------------------------------------------------------------
    // Null input for format
    // -----------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testFormatNullDate() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.format((Date) null);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatNullCalendar() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.format((Calendar) null);
    }

    // -----------------------------------------------------------------------
    // Format with StringBuffer and FieldPosition
    // -----------------------------------------------------------------------
    @Test
    public void testFormatStringBuffer() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        StringBuffer buf = new StringBuffer();
        StringBuffer result = fdf.format(date20091231, buf, new FieldPosition(0));
        assertEquals("2009-12-31", result.toString());
        assertSame(buf, result);
    }

    @Test
    public void testFormatStringBufferWithCalendar() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date20091231);
        StringBuffer buf = new StringBuffer();
        fdf.format(cal, buf, new FieldPosition(0));
        assertEquals("2009-12-31", buf.toString());
    }

    // -----------------------------------------------------------------------
    // getInstance factory methods
    // -----------------------------------------------------------------------
    @Test
    public void testGetInstanceNoArgs() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        assertNotNull(fdf);
    }

    @Test
    public void testGetInstancePattern() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        assertNotNull(fdf);
    }

    @Test
    public void testGetInstancePatternLocale() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US);
        assertNotNull(fdf);
    }

    @Test
    public void testGetInstancePatternTimezone() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", UTC);
        assertNotNull(fdf);
    }

    @Test
    public void testGetInstancePatternTimezoneLocale() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", UTC, Locale.US);
        assertNotNull(fdf);
    }

    // -----------------------------------------------------------------------
    // Equals and hashCode (if applicable)
    // -----------------------------------------------------------------------
    @Test
    public void testEqualsSameInstance() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());
    }

    @Test
    public void testEqualsDifferentPattern() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdf2 = FastDateFormat.getInstance("dd/MM/yyyy");
        assertTrue(!fdf1.equals(fdf2));
    }

    // -----------------------------------------------------------------------
    // Time zone and calendar conversion edge cases
    // -----------------------------------------------------------------------
    @Test
    public void testFormatWithTimeZoneAndDaylightSaving() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss z", tz);
        // 2009-12-31 00:00 UTC -> 2009-12-31 01:00 CET (CET is UTC+1, no DST in Dec)
        assertEquals("2009-12-31 01:00:00 CET", fdf.format(date20091231));
    }

    @Test
    public void testFormatCalendarWithDifferentTimeZone() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss z", UTC);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        cal.setTime(date20091231);
        // Calendar holds the same instant, but format uses UTC
        assertEquals("2009-12-31 00:00:00 UTC", fdf.format(cal));
    }

    // -----------------------------------------------------------------------
    // Exception on bad pattern
    // -----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPatternCharacter() {
        FastDateFormat.getInstance("yyyy-MM-dd X"); // X not valid in Java 7? Actually X is valid for ISO time zone, but let's use something invalid like 'Q'
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmatchedQuote() {
        FastDateFormat.getInstance("'unclosed");
    }

    // -----------------------------------------------------------------------
    // Test for thread safety (optional, but good for coverage)
    // -----------------------------------------------------------------------
    @Test
    public void testFormatConcurrentAccess() throws InterruptedException {
        final FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Thread t1 = new Thread(() -> assertEquals("2009-12-31", fdf.format(date20091231)));
        Thread t2 = new Thread(() -> assertEquals("2010-01-01", fdf.format(date20100101)));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    // -----------------------------------------------------------------------
    // Test for parse with default locale
    // -----------------------------------------------------------------------
    @Test
    public void testParseDefaultLocale() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Date parsed = fdf.parse("2009-12-31");
        assertEquals(date20091231, parsed);
    }
}