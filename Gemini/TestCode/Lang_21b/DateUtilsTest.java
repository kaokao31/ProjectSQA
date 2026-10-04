package org.apache.commons.lang3.time;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateUtilsTest {

    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("GMT");
    private static final TimeZone ZONE_PARIS = TimeZone.getTimeZone("Europe/Paris");
    private static final TimeZone ZONE_NEW_YORK = TimeZone.getTimeZone("America/New_York");

    @Test
    public void testConstructor() {
        assertNotNull(new DateUtils());
    }

    @Test
    public void testConstants() {
        assertEquals(1000L, DateUtils.MILLIS_PER_SECOND);
        assertEquals(60000L, DateUtils.MILLIS_PER_MINUTE);
        assertEquals(3600000L, DateUtils.MILLIS_PER_HOUR);
        assertEquals(86400000L, DateUtils.MILLIS_PER_DAY);
    }

    @Test
    public void testIsSameDay_Date() {
        Date date1 = new Date(1111111111L);
        Date date2 = new Date(1111111111L);
        assertTrue(DateUtils.isSameDay(date1, date2));

        Date date3 = new Date(date1.getTime() + 86400000L * 2);
        assertFalse(DateUtils.isSameDay(date1, date3));

        try {
            DateUtils.isSameDay((Date) null, date2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.isSameDay(date1, (Date) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIsSameDay_Calendar() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date(1111111111L));
        cal2.setTime(new Date(1111111111L));
        assertTrue(DateUtils.isSameDay(cal1, cal2));

        cal2.add(Calendar.DAY_OF_YEAR, 1);
        assertFalse(DateUtils.isSameDay(cal1, cal2));

        try {
            DateUtils.isSameDay((Calendar) null, cal2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.isSameDay(cal1, (Calendar) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIsSameInstant_Date() {
        Date date1 = new Date(1111111111L);
        Date date2 = new Date(1111111111L);
        Date date3 = new Date(1111111112L);

        assertTrue(DateUtils.isSameInstant(date1, date2));
        assertFalse(DateUtils.isSameInstant(date1, date3));

        try {
            DateUtils.isSameInstant((Date) null, date2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.isSameInstant(date1, (Date) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIsSameInstant_Calendar() {
        Calendar cal1 = Calendar.getInstance(ZONE_PARIS);
        Calendar cal2 = Calendar.getInstance(ZONE_NEW_YORK);
        cal1.setTimeInMillis(1111111111L);
        cal2.setTimeInMillis(1111111111L);
        assertTrue(DateUtils.isSameInstant(cal1, cal2));

        cal2.setTimeInMillis(2222222222L);
        assertFalse(DateUtils.isSameInstant(cal1, cal2));

        try {
            DateUtils.isSameInstant((Calendar) null, cal2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.isSameInstant(cal1, (Calendar) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIsSameLocalTime_Cal() {
        Calendar cal1 = Calendar.getInstance(ZONE_PARIS);
        Calendar cal2 = Calendar.getInstance(ZONE_PARIS);

        cal1.set(2004, Calendar.JUNE, 9, 13, 45, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2004, Calendar.JUNE, 9, 13, 45, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        assertTrue(DateUtils.isSameLocalTime(cal1, cal2));

        Calendar cal3 = Calendar.getInstance(ZONE_NEW_YORK);
        cal3.set(2004, Calendar.JUNE, 9, 13, 45, 0);
        cal3.set(Calendar.MILLISECOND, 0);
        assertTrue(DateUtils.isSameLocalTime(cal1, cal3));

        Calendar cal4 = Calendar.getInstance(ZONE_NEW_YORK);
        cal4.set(2004, Calendar.JUNE, 9, 14, 45, 0);
        cal4.set(Calendar.MILLISECOND, 0);
        assertFalse(DateUtils.isSameLocalTime(cal1, cal4));

        // LANG-677: Trigger bug where HOUR instead of HOUR_OF_DAY causes AM and PM with same 12-hour value to match
        Calendar calAm = Calendar.getInstance(ZONE_NEW_YORK);
        calAm.set(2004, Calendar.JUNE, 9, 4, 45, 0);
        calAm.set(Calendar.MILLISECOND, 0);

        Calendar calPm = Calendar.getInstance(ZONE_NEW_YORK);
        calPm.set(2004, Calendar.JUNE, 9, 16, 45, 0);
        calPm.set(Calendar.MILLISECOND, 0);

        assertFalse("LANG-677: 4 AM and 4 PM (16:00) should not be the same local time",
                DateUtils.isSameLocalTime(calAm, calPm));

        try {
            DateUtils.isSameLocalTime(null, cal2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.isSameLocalTime(cal1, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testParseDate() throws ParseException {
        String[] parsePatterns = new String[]{"yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss"};
        Date d1 = DateUtils.parseDate("2020/01/15", parsePatterns);
        assertNotNull(d1);

        Date d2 = DateUtils.parseDate("2020-01-15 10:20:30", parsePatterns);
        assertNotNull(d2);

        try {
            DateUtils.parseDate("invalid date", parsePatterns);
            fail("Expected ParseException");
        } catch (ParseException expected) {
        }

        try {
            DateUtils.parseDate(null, parsePatterns);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.parseDate("2020/01/15", (String[]) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testParseDateWithLeniency() throws Exception {
        String[] parsePatterns = new String[]{"yyyy-MM-dd"};
        try {
            DateUtils.parseDateStrictly("2020-02-30", parsePatterns);
            fail("Expected ParseException for lenient date with strict parse");
        } catch (ParseException expected) {
        }

        Date lenientDate = DateUtils.parseDate("2020-02-30", parsePatterns);
        assertNotNull(lenientDate);

        try {
            DateUtils.parseDateWithLeniency(null, parsePatterns, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.parseDateWithLeniency("2020-02-30", null, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testAddDateComponents() {
        Calendar base = Calendar.getInstance();
        base.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);
        Date date = base.getTime();

        Date result = DateUtils.addYears(date, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(2021, cal.get(Calendar.YEAR));

        result = DateUtils.addMonths(date, 2);
        cal.setTime(result);
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));

        result = DateUtils.addWeeks(date, 1);
        cal.setTime(result);
        assertEquals(8, cal.get(Calendar.DAY_OF_MONTH));

        result = DateUtils.addDays(date, 5);
        cal.setTime(result);
        assertEquals(6, cal.get(Calendar.DAY_OF_MONTH));

        result = DateUtils.addHours(date, 10);
        cal.setTime(result);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));

        result = DateUtils.addMinutes(date, 30);
        cal.setTime(result);
        assertEquals(30, cal.get(Calendar.MINUTE));

        result = DateUtils.addSeconds(date, 45);
        cal.setTime(result);
        assertEquals(45, cal.get(Calendar.SECOND));

        result = DateUtils.addMilliseconds(date, 500);
        cal.setTime(result);
        assertEquals(500, cal.get(Calendar.MILLISECOND));

        try {
            DateUtils.addDays(null, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testSetDateComponents() {
        Calendar base = Calendar.getInstance();
        base.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);
        Date date = base.getTime();

        Date result = DateUtils.setYears(date, 2022);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(2022, cal.get(Calendar.YEAR));

        result = DateUtils.setMonths(date, Calendar.DECEMBER);
        cal.setTime(result);
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));

        result = DateUtils.setDays(date, 15);
        cal.setTime(result);
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));

        result = DateUtils.setHours(date, 18);
        cal.setTime(result);
        assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));

        result = DateUtils.setMinutes(date, 42);
        cal.setTime(result);
        assertEquals(42, cal.get(Calendar.MINUTE));

        result = DateUtils.setSeconds(date, 59);
        cal.setTime(result);
        assertEquals(59, cal.get(Calendar.SECOND));

        result = DateUtils.setMilliseconds(date, 999);
        cal.setTime(result);
        assertEquals(999, cal.get(Calendar.MILLISECOND));

        try {
            DateUtils.setDays(null, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testToCalendar() {
        Date date = new Date(123456789L);
        Calendar cal = DateUtils.toCalendar(date);
        assertNotNull(cal);
        assertEquals(date.getTime(), cal.getTimeInMillis());

        try {
            DateUtils.toCalendar(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testRoundTruncateCeiling() {
        Calendar cal = Calendar.getInstance(UTC_TIME_ZONE);
        cal.set(2020, Calendar.JANUARY, 15, 12, 30, 40);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();

        Date truncated = DateUtils.truncate(date, Calendar.DATE);
        Calendar resCal = Calendar.getInstance(UTC_TIME_ZONE);
        resCal.setTime(truncated);
        assertEquals(0, resCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, resCal.get(Calendar.MINUTE));
        assertEquals(0, resCal.get(Calendar.SECOND));
        assertEquals(0, resCal.get(Calendar.MILLISECOND));

        Date rounded = DateUtils.round(date, Calendar.DATE);
        resCal.setTime(rounded);
        assertEquals(16, resCal.get(Calendar.DAY_OF_MONTH));

        Date ceiling = DateUtils.ceiling(date, Calendar.DATE);
        resCal.setTime(ceiling);
        assertEquals(16, resCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, resCal.get(Calendar.HOUR_OF_DAY));

        try {
            DateUtils.round((Date) null, Calendar.DATE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.truncate((Date) null, Calendar.DATE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.ceiling((Date) null, Calendar.DATE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testRoundTruncateCeilingCalendar() {
        Calendar cal = Calendar.getInstance(UTC_TIME_ZONE);
        cal.set(2020, Calendar.JANUARY, 15, 12, 30, 40);
        cal.set(Calendar.MILLISECOND, 500);

        Calendar truncated = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
        assertEquals(0, truncated.get(Calendar.HOUR_OF_DAY));

        Calendar rounded = DateUtils.round(cal, Calendar.DAY_OF_MONTH);
        assertEquals(16, rounded.get(Calendar.DAY_OF_MONTH));

        Calendar ceiling = DateUtils.ceiling(cal, Calendar.DAY_OF_MONTH);
        assertEquals(16, ceiling.get(Calendar.DAY_OF_MONTH));

        try {
            DateUtils.round((Calendar) null, Calendar.DATE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.truncate((Calendar) null, Calendar.DATE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.ceiling((Calendar) null, Calendar.DATE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testRoundTruncateCeilingObject() {
        Date date = new Date();
        assertNotNull(DateUtils.round((Object) date, Calendar.DATE));
        assertNotNull(DateUtils.truncate((Object) date, Calendar.DATE));
        assertNotNull(DateUtils.ceiling((Object) date, Calendar.DATE));

        try {
            DateUtils.round((Object) "Not a date", Calendar.DATE);
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }

        try {
            DateUtils.truncate((Object) "Not a date", Calendar.DATE);
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }

        try {
            DateUtils.ceiling((Object) "Not a date", Calendar.DATE);
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testIterator() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JULY, 4);

        Iterator<?> it = DateUtils.iterator(cal, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(it);
        assertTrue(it.hasNext());

        int count = 0;
        while (it.hasNext()) {
            Calendar c = (Calendar) it.next();
            assertNotNull(c);
            count++;
        }
        assertEquals(7, count);

        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
        }

        try {
            DateUtils.iterator((Calendar) null, DateUtils.RANGE_WEEK_SUNDAY);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.iterator(cal, 9999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIteratorDateAndObject() {
        Date date = new Date();
        Iterator<?> itDate = DateUtils.iterator(date, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(itDate);
        assertTrue(itDate.hasNext());

        Iterator<?> itObj = DateUtils.iterator((Object) date, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(itObj);
        assertTrue(itObj.hasNext());

        try {
            DateUtils.iterator((Date) null, DateUtils.RANGE_WEEK_SUNDAY);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            DateUtils.iterator((Object) "not a date", DateUtils.RANGE_WEEK_SUNDAY);
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }
}