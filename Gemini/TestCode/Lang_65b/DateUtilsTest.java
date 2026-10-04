package org.apache.commons.lang.time;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateUtilsTest {

    private DateFormat dateParser;
    private TimeZone defaultZone;
    private Date date1;
    private Date date2;
    private Calendar cal1;
    private Calendar cal2;

    @Before
    public void setUp() throws Exception {
        dateParser = new SimpleDateFormat("MMM dd, yyyy H:mm:ss.SSS");
        defaultZone = TimeZone.getDefault();
        date1 = dateParser.parse("February 12, 2002 12:34:56.789");
        date2 = dateParser.parse("November 18, 2001 1:23:11.400");
        cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2 = Calendar.getInstance();
        cal2.setTime(date2);
    }

    @After
    public void tearDown() throws Exception {
        TimeZone.setDefault(defaultZone);
    }

    // -----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertNotNull(new DateUtils());
    }

    // -----------------------------------------------------------------------
    @Test
    public void testIsSameDay_Date() {
        assertFalse(DateUtils.isSameDay((Date) null, (Date) null));
        assertFalse(DateUtils.isSameDay(date1, (Date) null));
        assertFalse(DateUtils.isSameDay((Date) null, date2));
        assertFalse(DateUtils.isSameDay(date1, date2));
        assertTrue(DateUtils.isSameDay(date1, date1));

        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);
        c1.add(Calendar.HOUR_OF_DAY, 1);
        assertTrue(DateUtils.isSameDay(date1, c1.getTime()));

        c1.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(DateUtils.isSameDay(date1, c1.getTime()));

        c1.setTime(date1);
        c1.add(Calendar.YEAR, 1);
        assertFalse(DateUtils.isSameDay(date1, c1.getTime()));
    }

    @Test
    public void testIsSameDay_Calendar() {
        assertFalse(DateUtils.isSameDay((Calendar) null, (Calendar) null));
        assertFalse(DateUtils.isSameDay(cal1, (Calendar) null));
        assertFalse(DateUtils.isSameDay((Calendar) null, cal2));
        assertFalse(DateUtils.isSameDay(cal1, cal2));
        assertTrue(DateUtils.isSameDay(cal1, cal1));

        Calendar c1 = (Calendar) cal1.clone();
        c1.add(Calendar.HOUR_OF_DAY, 1);
        assertTrue(DateUtils.isSameDay(cal1, c1));

        c1.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(DateUtils.isSameDay(cal1, c1));

        c1 = (Calendar) cal1.clone();
        c1.add(Calendar.ERA, 1);
        assertFalse(DateUtils.isSameDay(cal1, c1));
    }

    @Test
    public void testIsSameInstant_Date() {
        assertFalse(DateUtils.isSameInstant((Date) null, (Date) null));
        assertFalse(DateUtils.isSameInstant(date1, (Date) null));
        assertFalse(DateUtils.isSameInstant((Date) null, date2));
        assertFalse(DateUtils.isSameInstant(date1, date2));
        assertTrue(DateUtils.isSameInstant(date1, date1));
        assertTrue(DateUtils.isSameInstant(date1, new Date(date1.getTime())));
    }

    @Test
    public void testIsSameInstant_Calendar() {
        assertFalse(DateUtils.isSameInstant((Calendar) null, (Calendar) null));
        assertFalse(DateUtils.isSameInstant(cal1, (Calendar) null));
        assertFalse(DateUtils.isSameInstant((Calendar) null, cal2));
        assertFalse(DateUtils.isSameInstant(cal1, cal2));
        assertTrue(DateUtils.isSameInstant(cal1, cal1));
        assertTrue(DateUtils.isSameInstant(cal1, (Calendar) cal1.clone()));
    }

    @Test
    public void testIsSameLocalTime_Calendar() {
        assertFalse(DateUtils.isSameLocalTime((Calendar) null, (Calendar) null));
        assertFalse(DateUtils.isSameLocalTime(cal1, (Calendar) null));
        assertFalse(DateUtils.isSameLocalTime((Calendar) null, cal2));
        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
        assertTrue(DateUtils.isSameLocalTime(cal1, cal1));

        Calendar cal3 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar cal4 = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        cal3.set(2004, Calendar.JUNE, 9, 13, 45, 0);
        cal3.set(Calendar.MILLISECOND, 0);
        cal4.set(2004, Calendar.JUNE, 9, 13, 45, 0);
        cal4.set(Calendar.MILLISECOND, 0);
        assertTrue(DateUtils.isSameLocalTime(cal3, cal4));

        cal4.set(Calendar.MILLISECOND, 1);
        assertFalse(DateUtils.isSameLocalTime(cal3, cal4));
        cal4.set(Calendar.MILLISECOND, 0);

        cal4.set(Calendar.SECOND, 1);
        assertFalse(DateUtils.isSameLocalTime(cal3, cal4));
        cal4.set(Calendar.SECOND, 0);

        cal4.set(Calendar.MINUTE, 1);
        assertFalse(DateUtils.isSameLocalTime(cal3, cal4));
        cal4.set(Calendar.MINUTE, 45);

        cal4.set(Calendar.HOUR_OF_DAY, 1);
        assertFalse(DateUtils.isSameLocalTime(cal3, cal4));
        cal4.set(Calendar.HOUR_OF_DAY, 13);

        cal4.set(Calendar.DAY_OF_YEAR, 1);
        assertFalse(DateUtils.isSameLocalTime(cal3, cal4));
        cal4.set(Calendar.DAY_OF_YEAR, cal3.get(Calendar.DAY_OF_YEAR));

        cal4.set(Calendar.YEAR, 2005);
        assertFalse(DateUtils.isSameLocalTime(cal3, cal4));
        cal4.set(Calendar.YEAR, 2004);

        cal4.set(Calendar.ERA, GregorianCalendar.BC);
        assertFalse(DateUtils.isSameLocalTime(cal3, cal4));
    }

    // -----------------------------------------------------------------------
    @Test
    public void testParseDate() throws Exception {
        String[] parsers = new String[]{"yyyy'-'DDD", "yyyyMMdd", "yyyyMM", "yyyy/MM/dd", "yyyy-MM-dd"};
        Date expected = dateParser.parse("February 12, 2002 00:00:00.000");

        assertEquals(expected, DateUtils.parseDate("2002-043", parsers));
        assertEquals(expected, DateUtils.parseDate("20020212", parsers));
        assertEquals(expected, DateUtils.parseDate("2002/02/12", parsers));
        assertEquals(expected, DateUtils.parseDate("2002-02-12", parsers));

        try {
            DateUtils.parseDate("2002-02-12", (String[]) null);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.parseDate((String) null, parsers);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.parseDate("PURPLE", parsers);
            fail();
        } catch (ParseException ex) {}
    }

    // -----------------------------------------------------------------------
    @Test
    public void testAddYears() {
        Date result = DateUtils.addYears(date1, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(2003, cal.get(Calendar.YEAR));

        try {
            DateUtils.addYears(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testAddMonths() {
        Date result = DateUtils.addMonths(date1, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));

        try {
            DateUtils.addMonths(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testAddWeeks() {
        Date result = DateUtils.addWeeks(date1, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(19, cal.get(Calendar.DAY_OF_MONTH));

        try {
            DateUtils.addWeeks(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testAddDays() {
        Date result = DateUtils.addDays(date1, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(13, cal.get(Calendar.DAY_OF_MONTH));

        try {
            DateUtils.addDays(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testAddHours() {
        Date result = DateUtils.addHours(date1, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));

        try {
            DateUtils.addHours(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testAddMinutes() {
        Date result = DateUtils.addMinutes(date1, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(35, cal.get(Calendar.MINUTE));

        try {
            DateUtils.addMinutes(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testAddSeconds() {
        Date result = DateUtils.addSeconds(date1, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(57, cal.get(Calendar.SECOND));

        try {
            DateUtils.addSeconds(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testAddMilliseconds() {
        Date result = DateUtils.addMilliseconds(date1, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(790, cal.get(Calendar.MILLISECOND));

        try {
            DateUtils.addMilliseconds(null, 1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    // -----------------------------------------------------------------------
    @Test
    public void testTruncateLang59() throws Exception {
        // Specifically targets Lang-59 / Lang-65 bug
        TimeZone defaultZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
            Date date = df.parse("2004-10-31 01:02:03.004 MDT");
            Date expectedSecond = df.parse("2004-10-31 01:02:03.000 MDT");
            Date actualSecond = DateUtils.truncate(date, Calendar.SECOND);
            assertEquals("Truncate Calendar.SECOND", expectedSecond, actualSecond);

            Date expectedMillis = df.parse("2004-10-31 01:02:03.004 MDT");
            Date actualMillis = DateUtils.truncate(date, Calendar.MILLISECOND);
            assertEquals("Truncate Calendar.MILLISECOND", expectedMillis, actualMillis);
        } finally {
            TimeZone.setDefault(defaultZone);
        }
    }

    // -----------------------------------------------------------------------
    @Test
    public void testRoundDate() throws Exception {
        Date target = dateParser.parse("February 12, 2002 12:34:56.789");
        Date expected = dateParser.parse("February 12, 2002 12:34:57.000");
        assertEquals(expected, DateUtils.round(target, Calendar.SECOND));

        expected = dateParser.parse("February 12, 2002 12:35:00.000");
        assertEquals(expected, DateUtils.round(target, Calendar.MINUTE));

        expected = dateParser.parse("February 12, 2002 13:00:00.000");
        assertEquals(expected, DateUtils.round(target, Calendar.HOUR_OF_DAY));

        expected = dateParser.parse("February 13, 2002 00:00:00.000");
        assertEquals(expected, DateUtils.round(target, Calendar.DATE));

        expected = dateParser.parse("February 01, 2002 00:00:00.000");
        assertEquals(expected, DateUtils.round(target, Calendar.MONTH));

        expected = dateParser.parse("January 01, 2002 00:00:00.000");
        assertEquals(expected, DateUtils.round(target, Calendar.YEAR));

        try {
            DateUtils.round((Date) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.round(target, -999999);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testRoundCalendar() throws Exception {
        Calendar target = Calendar.getInstance();
        target.setTime(dateParser.parse("February 12, 2002 12:34:56.789"));
        Calendar expected = Calendar.getInstance();
        expected.setTime(dateParser.parse("February 12, 2002 12:34:57.000"));
        assertEquals(expected.getTime(), DateUtils.round(target, Calendar.SECOND).getTime());

        try {
            DateUtils.round((Calendar) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testRoundObject() throws Exception {
        Date target = dateParser.parse("February 12, 2002 12:34:56.789");
        Date expected = dateParser.parse("February 12, 2002 12:34:57.000");
        assertEquals(expected, DateUtils.round((Object) target, Calendar.SECOND));

        Calendar calTarget = Calendar.getInstance();
        calTarget.setTime(target);
        Calendar calExpected = Calendar.getInstance();
        calExpected.setTime(expected);
        assertEquals(calExpected, DateUtils.round((Object) calTarget, Calendar.SECOND));

        try {
            DateUtils.round((Object) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.round("Not a date", Calendar.SECOND);
            fail();
        } catch (ClassCastException ex) {}
    }

    // -----------------------------------------------------------------------
    @Test
    public void testTruncateDate() throws Exception {
        Date target = dateParser.parse("February 12, 2002 12:34:56.789");
        Date expected = dateParser.parse("February 12, 2002 12:34:56.000");
        assertEquals(expected, DateUtils.truncate(target, Calendar.SECOND));

        expected = dateParser.parse("February 12, 2002 12:34:00.000");
        assertEquals(expected, DateUtils.truncate(target, Calendar.MINUTE));

        expected = dateParser.parse("February 12, 2002 12:00:00.000");
        assertEquals(expected, DateUtils.truncate(target, Calendar.HOUR_OF_DAY));

        expected = dateParser.parse("February 12, 2002 00:00:00.000");
        assertEquals(expected, DateUtils.truncate(target, Calendar.DATE));

        expected = dateParser.parse("February 01, 2002 00:00:00.000");
        assertEquals(expected, DateUtils.truncate(target, Calendar.MONTH));

        expected = dateParser.parse("January 01, 2002 00:00:00.000");
        assertEquals(expected, DateUtils.truncate(target, Calendar.YEAR));

        try {
            DateUtils.truncate((Date) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.truncate(target, -999999);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testTruncateCalendar() throws Exception {
        Calendar target = Calendar.getInstance();
        target.setTime(dateParser.parse("February 12, 2002 12:34:56.789"));
        Calendar expected = Calendar.getInstance();
        expected.setTime(dateParser.parse("February 12, 2002 12:34:56.000"));
        assertEquals(expected.getTime(), DateUtils.truncate(target, Calendar.SECOND).getTime());

        try {
            DateUtils.truncate((Calendar) null, Calendar.DATE);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testTruncateObject() throws Exception {
        Date target = dateParser.parse("February 12, 2002 12:34:56.789");
        Date expected = dateParser.parse("February 12, 2002 12:34:56.000");
        assertEquals(expected, DateUtils.truncate((Object) target, Calendar.SECOND));

        Calendar calTarget = Calendar.getInstance();
        calTarget.setTime(target);
        Calendar calExpected = Calendar.getInstance();
        calExpected.setTime(expected);
        assertEquals(calExpected, DateUtils.truncate((Object) calTarget, Calendar.SECOND));

        try {
            DateUtils.truncate((Object) null, Calendar.SECOND);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.truncate("Not a date", Calendar.SECOND);
            fail();
        } catch (ClassCastException ex) {}
    }

    // -----------------------------------------------------------------------
    @Test
    public void testIterator() throws Exception {
        Date date = dateParser.parse("July 4, 2004 12:34:56.789");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        Iterator<?> it = DateUtils.iterator(date, DateUtils.RANGE_WEEK_SUNDAY);
        int count = 0;
        while (it.hasNext()) {
            Object obj = it.next();
            assertTrue(obj instanceof Calendar);
            count++;
        }
        assertEquals(7, count);

        try {
            it.next();
            fail();
        } catch (NoSuchElementException ex) {}

        try {
            it.remove();
            fail();
        } catch (UnsupportedOperationException ex) {}

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_MONDAY);
        count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(7, count);

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_RELATIVE);
        count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(7, count);

        it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_CENTER);
        count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(7, count);

        it = DateUtils.iterator(cal, DateUtils.RANGE_MONTH_SUNDAY);
        count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertTrue(count >= 28 && count <= 42);

        it = DateUtils.iterator(cal, DateUtils.RANGE_MONTH_MONDAY);
        count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertTrue(count >= 28 && count <= 42);

        it = DateUtils.iterator((Object) date, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(it);

        try {
            DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_SUNDAY);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.iterator((Object) null, DateUtils.RANGE_WEEK_SUNDAY);
            fail();
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.iterator("Not a date", DateUtils.RANGE_WEEK_SUNDAY);
            fail();
        } catch (ClassCastException ex) {}

        try {
            DateUtils.iterator(cal, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

    // -----------------------------------------------------------------------
    @Test
    public void testRoundCeilTruncateSpecialCases() throws Exception {
        Date d = dateParser.parse("February 15, 2002 12:34:56.789");
        DateUtils.round(d, DateUtils.SEMI_MONTH);
        DateUtils.truncate(d, DateUtils.SEMI_MONTH);
        DateUtils.round(d, Calendar.AM_PM);
        DateUtils.truncate(d, Calendar.AM_PM);
        DateUtils.round(d, Calendar.HOUR);
        DateUtils.truncate(d, Calendar.HOUR);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        DateUtils.round(c, Calendar.DATE);
    }
}