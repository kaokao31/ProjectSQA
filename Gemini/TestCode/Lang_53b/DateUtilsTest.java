package org.apache.commons.lang.time;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Date;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

public class DateUtilsTest {

    private TimeZone defaultTimeZone;

    @Before
    public void setUp() {
        defaultTimeZone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    public void testRoundLang346() {
        // This specifically targets the Lang-53 bug where rounding up minutes/hours/seconds
        // fails under certain Calendar states or timezones.
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JULY, 2, 8, 8, 30);
        cal.set(Calendar.MILLISECOND, 0);
        
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.MINUTE);
        
        Calendar expected = Calendar.getInstance();
        expected.set(2007, Calendar.JULY, 2, 8, 9, 0);
        expected.set(Calendar.MILLISECOND, 0);
        
        assertEquals("Minute Round Up Failed", expected.getTime(), rounded);
    }

    @Test
    public void testRoundSemantics() {
        Date date = null;
        try {
            DateUtils.round(date, Calendar.DATE);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            DateUtils.round((Object) null, Calendar.DATE);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            DateUtils.round(Calendar.getInstance(), Calendar.DATE);
            fail("Expecting ClassCastException");
        } catch (ClassCastException ex) {
            // expected
        }

        try {
            DateUtils.round("Not A Date", Calendar.DATE);
            fail("Expecting ClassCastException");
        } catch (ClassCastException ex) {
            // expected
        }

        Calendar cal = Calendar.getInstance();
        assertNotNull(DateUtils.round(cal, Calendar.SECOND));
        assertNotNull(DateUtils.round(cal.getTime(), Calendar.SECOND));
        assertNotNull(DateUtils.round((Object) cal.getTime(), Calendar.SECOND));
    }

    @Test
    public void testTruncateSemantics() {
        Date date = null;
        try {
            DateUtils.truncate(date, Calendar.DATE);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            DateUtils.truncate((Object) null, Calendar.DATE);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            DateUtils.truncate(Calendar.getInstance(), Calendar.DATE);
            fail("Expecting ClassCastException");
        } catch (ClassCastException ex) {
            // expected
        }

        try {
            DateUtils.truncate("Not A Date", Calendar.DATE);
            fail("Expecting ClassCastException");
        } catch (ClassCastException ex) {
            // expected
        }

        Calendar cal = Calendar.getInstance();
        assertNotNull(DateUtils.truncate(cal, Calendar.SECOND));
        assertNotNull(DateUtils.truncate(cal.getTime(), Calendar.SECOND));
        assertNotNull(DateUtils.truncate((Object) cal.getTime(), Calendar.SECOND));
    }

    @Test
    public void testCeilSemantics() {
        Date date = null;
        try {
            DateUtils.ceiling(date, Calendar.DATE);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            DateUtils.ceiling((Object) null, Calendar.DATE);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            DateUtils.ceiling(Calendar.getInstance(), Calendar.DATE);
            fail("Expecting ClassCastException");
        } catch (ClassCastException ex) {
            // expected
        }

        try {
            DateUtils.ceiling("Not A Date", Calendar.DATE);
            fail("Expecting ClassCastException");
        } catch (ClassCastException ex) {
            // expected
        }

        Calendar cal = Calendar.getInstance();
        assertNotNull(DateUtils.ceiling(cal, Calendar.SECOND));
        assertNotNull(DateUtils.ceiling(cal.getTime(), Calendar.SECOND));
        assertNotNull(DateUtils.ceiling((Object) cal.getTime(), Calendar.SECOND));
    }

    @Test
    public void testModifyEdgeCases() {
        Calendar val = Calendar.getInstance();
        // Test various fields in modify to hit switch cases and default/range limits
        DateUtils.modify(val, Calendar.MILLISECOND, false);
        DateUtils.modify(val, Calendar.SECOND, false);
        DateUtils.modify(val, Calendar.MINUTE, false);
        DateUtils.modify(val, Calendar.HOUR_OF_DAY, false);
        DateUtils.modify(val, Calendar.DATE, false);
        DateUtils.modify(val, Calendar.MONTH, false);
        DateUtils.modify(val, Calendar.YEAR, false);

        try {
            DateUtils.modify(val, 99999, false);
            fail("Expected IllegalArgumentException for unknown field");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testIterator() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 86400000L * 5);

        Iterator<?> it = DateUtils.iterator(start, DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertNotNull(it.next());

        it = DateUtils.iterator(start, DateUtils.RANGE_WEEK_MONDAY);
        assertNotNull(it);

        it = DateUtils.iterator(start, DateUtils.RANGE_WEEK_RELATIVE);
        assertNotNull(it);

        it = DateUtils.iterator(start, DateUtils.RANGE_MONTH_SUNDAY);
        assertNotNull(it);

        it = DateUtils.iterator(start, DateUtils.RANGE_MONTH_MONDAY);
        assertNotNull(it);

        it = DateUtils.iterator(Calendar.getInstance(), DateUtils.RANGE_WEEK_SUNDAY);
        assertNotNull(it);

        it = DateUtils.iterator(Calendar.getInstance(), DateUtils.RANGE_MONTH_SUNDAY);
        assertNotNull(it);

        try {
            DateUtils.iterator("Invalid", DateUtils.RANGE_WEEK_SUNDAY);
            fail("Expected ClassCastException");
        } catch (ClassCastException ex) {
            // expected
        }

        try {
            DateUtils.iterator(start, 999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testAddOperations() {
        Date date = new Date();
        assertNotNull(DateUtils.addYears(date, 1));
        assertNotNull(DateUtils.addMonths(date, 1));
        assertNotNull(DateUtils.addWeeks(date, 1));
        assertNotNull(DateUtils.addDays(date, 1));
        assertNotNull(DateUtils.addHours(date, 1));
        assertNotNull(DateUtils.addMinutes(date, 1));
        assertNotNull(DateUtils.addSeconds(date, 1));
        assertNotNull(DateUtils.addMilliseconds(date, 1));

        Calendar cal = Calendar.getInstance();
        assertNotNull(DateUtils.addYears(cal, 1));
        assertNotNull(DateUtils.addMonths(cal, 1));
        assertNotNull(DateUtils.addWeeks(cal, 1));
        assertNotNull(DateUtils.addDays(cal, 1));
        assertNotNull(DateUtils.addHours(cal, 1));
        assertNotNull(DateUtils.addMinutes(cal, 1));
        assertNotNull(DateUtils.addSeconds(cal, 1));
        assertNotNull(DateUtils.addMilliseconds(cal, 1));
    }

    @Test
    public void testSetOperations() {
        Date date = new Date();
        assertNotNull(DateUtils.setYears(date, 2012));
        assertNotNull(DateUtils.setMonths(date, 5));
        assertNotNull(DateUtils.setDays(date, 15));
        assertNotNull(DateUtils.setHours(date, 12));
        assertNotNull(DateUtils.setMinutes(date, 30));
        assertNotNull(DateUtils.setSeconds(date, 45));
        assertNotNull(DateUtils.setMilliseconds(date, 500));

        Calendar cal = Calendar.getInstance();
        assertNotNull(DateUtils.setYears(cal, 2012));
        assertNotNull(DateUtils.setMonths(cal, 5));
        assertNotNull(DateUtils.setDays(cal, 15));
        assertNotNull(DateUtils.setHours(cal, 12));
        assertNotNull(DateUtils.setMinutes(cal, 30));
        assertNotNull(DateUtils.setSeconds(cal, 45));
        assertNotNull(DateUtils.setMilliseconds(cal, 500));
    }

    @Test
    public void testToCalendarAndFragment() {
        Date date = new Date();
        assertNotNull(DateUtils.toCalendar(date));

        long millis = DateUtils.getFragmentInMilliseconds(date, Calendar.DATE);
        assertTrue(millis >= 0);
        long seconds = DateUtils.getFragmentInSeconds(date, Calendar.DATE);
        assertTrue(seconds >= 0);
        long minutes = DateUtils.getFragmentInMinutes(date, Calendar.DATE);
        assertTrue(minutes >= 0);
        long hours = DateUtils.getFragmentInHours(date, Calendar.DATE);
        assertTrue(hours >= 0);
        long days = DateUtils.getFragmentInDays(date, Calendar.MONTH);
        assertTrue(days >= 0);

        Calendar cal = Calendar.getInstance();
        assertNotNull(DateUtils.getFragmentInMilliseconds(cal, Calendar.DATE));
        assertNotNull(DateUtils.getFragmentInSeconds(cal, Calendar.DATE));
        assertNotNull(DateUtils.getFragmentInMinutes(cal, Calendar.DATE));
        assertNotNull(DateUtils.getFragmentInHours(cal, Calendar.DATE));
        assertNotNull(DateUtils.getFragmentInDays(cal, Calendar.MONTH));
        
        try {
            DateUtils.getFragmentInMilliseconds(null, Calendar.DATE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.getFragmentInMilliseconds(cal, 9999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}
    }

    @Test
    public void testIsSameDayAndInstant() {
        Date d1 = new Date();
        Date d2 = new Date(d1.getTime());
        assertTrue(DateUtils.isSameInstant(d1, d2));
        assertTrue(DateUtils.isSameDay(d1, d2));

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = (Calendar) c1.clone();
        assertTrue(DateUtils.isSameInstant(c1, c2));
        assertTrue(DateUtils.isSameDay(c1, c2));

        c2.add(Calendar.DATE, 1);
        assertFalse(DateUtils.isSameDay(c1, c2));
        assertFalse(DateUtils.isSameDay(d1, new Date(d2.getTime() + 86400000L * 2)));

        try {
            DateUtils.isSameInstant((Date)null, (Date)null);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {}

        try {
            DateUtils.isSameInstant((Calendar)null, (Calendar)null);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {}

        try {
            DateUtils.isSameDay((Date)null, (Date)null);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {}

        try {
            DateUtils.isSameDay((Calendar)null, (Calendar)null);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {}
    }

    @Test
    public void testParseDate() throws Exception {
        String[] parsePatterns = {"yyyy-MM-dd", "yyyy/MM/dd"};
        Date parsed = DateUtils.parseDate("2007-07-02", parsePatterns);
        assertNotNull(parsed);

        try {
            DateUtils.parseDate("2007-07-02", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.parseDate(null, parsePatterns);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}

        try {
            DateUtils.parseDate("bad-date", parsePatterns);
            fail("Expected ParseException");
        } catch (java.text.ParseException ex) {}
    }

    @Test
    public void testDateIteratorClass() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 3);
        
        Iterator<?> it = new DateUtils.DateIterator(startDate, endDate);
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {}
    }
}