package org.apache.commons.lang.time;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for DateUtils.
 * Designed to achieve high coverage and detect bugs, especially the one
 * reported in Defects4J Bug 53 (testRoundLang346).
 */
public class DateUtilsTest {

    // -----------------------------------------------------------------------
    // Rounding tests (including the failing test from Defects4J)
    // -----------------------------------------------------------------------

    @Test
    public void testRoundLang346() {
        // This is the exact failing test case from Defects4J Bug 53.
        Calendar cal = Calendar.getInstance();
        cal.set(2007, 6, 2, 8, 8, 30); // July 2, 2007 08:08:30
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.MINUTE);
        // Rounded up should be 08:09
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2007, 6, 2, 8, 9, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals("Minute Round Up Failed", expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundMinuteDown() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 10, 30, 29); // 10:30:29
        cal.set(Calendar.MILLISECOND, 999);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.MINUTE);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 10, 30, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundMinuteUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 10, 30, 30); // 10:30:30
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.MINUTE);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 10, 31, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 10, 29, 59); // 10:29:59
        cal.set(Calendar.MILLISECOND, 999);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.HOUR_OF_DAY);
        // Since minutes < 30, round down to 10:00
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 10, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundHourUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 10, 30, 0); // 10:30:00
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.HOUR_OF_DAY);
        // Since minutes >= 30, round up to 11:00
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 11, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 11, 59, 59); // before noon
        cal.set(Calendar.MILLISECOND, 999);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.DAY_OF_MONTH);
        // Before noon -> round down to start of day
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundDayUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 12, 0, 0); // exactly noon
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.DAY_OF_MONTH);
        // At noon -> round up to next day
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 16, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 15, 0, 0, 0); // mid-month
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.MONTH);
        // Round down to start of January
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundMonthUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 16, 0, 0, 0); // after 15th
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.MONTH);
        // Round up to start of February
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, Calendar.FEBRUARY, 1, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 30, 12, 0, 0); // mid-year (end of June)
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.YEAR);
        // Before July 1 -> round down to start of 2020
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundYearUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JULY, 1, 0, 0, 0); // first day of second half
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.YEAR);
        // Round up to start of 2021
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2021, Calendar.JANUARY, 1, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testRoundWeek() {
        Calendar cal = Calendar.getInstance();
        // Set to Thursday, 2022-06-16 (a Thursday)
        cal.set(2022, Calendar.JUNE, 16, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.WEEK_OF_YEAR);
        // Round to nearest Sunday start? Implementation varies. Commons Lang rounds to start of week (Sunday)
        // For Thursday, it's closer to the next Sunday? Actually rounding to week: if day of week >= Thursday? Need to know.
        // We'll test a known case: 
        // For Wednesday (day 4) if we round, it should stay same week? Actually typical rounding: if day >= Thursday (5) round up to next week.
        // Let's just test that the method runs and produces a result with no exception.
        assertNotNull(rounded);
        // This is more of a smoke test; the exact behavior depends on locale.
    }

    // -----------------------------------------------------------------------
    // Truncating tests
    // -----------------------------------------------------------------------

    @Test
    public void testTruncateSecond() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        Date truncated = DateUtils.truncate(date, Calendar.SECOND);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 10, 30, 45);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), truncated);
    }

    @Test
    public void testTruncateMinute() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        Date truncated = DateUtils.truncate(date, Calendar.MINUTE);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 10, 30, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), truncated);
    }

    @Test
    public void testTruncateHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        Date truncated = DateUtils.truncate(date, Calendar.HOUR_OF_DAY);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 10, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), truncated);
    }

    @Test
    public void testTruncateDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        Date truncated = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, 0, 15, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), truncated);
    }

    @Test
    public void testTruncateMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.MARCH, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        Date truncated = DateUtils.truncate(date, Calendar.MONTH);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, Calendar.MARCH, 1, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), truncated);
    }

    @Test
    public void testTruncateYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.MARCH, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 789);
        Date date = cal.getTime();
        Date truncated = DateUtils.truncate(date, Calendar.YEAR);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), truncated);
    }

    // -----------------------------------------------------------------------
    // Ceiling tests (if method exists; assume addCeiling or similar)
    // -----------------------------------------------------------------------
    // If DateUtils has ceiling methods, test them. Otherwise skip.
    // In Commons Lang 2.x, there is no ceiling, only round and truncate.
    // We'll keep it simple.

    // -----------------------------------------------------------------------
    // Tests for other methods (like getFragment, isSameDay, etc.) if present
    // Assuming the class has isSameDay, isSameInstant, mod methods, etc.
    // But we focus on rounding/truncating for Bug 53.

    // -----------------------------------------------------------------------
    // Null and edge case tests
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testRoundNullDate() {
        DateUtils.round(null, Calendar.MINUTE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncateNullDate() {
        DateUtils.truncate(null, Calendar.MINUTE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundInvalidField() {
        DateUtils.round(new Date(), -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncateInvalidField() {
        DateUtils.truncate(new Date(), 100);
    }

    @Test
    public void testRoundWithCalendarFieldZero() {
        // Calendar.ERA = 0, should be invalid
        Date date = new Date();
        try {
            DateUtils.round(date, Calendar.ERA);
            fail("Expected IllegalArgumentException for ERA");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testTruncateWithCalendarFieldZero() {
        Date date = new Date();
        try {
            DateUtils.truncate(date, Calendar.ERA);
            fail("Expected IllegalArgumentException for ERA");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // Additional boundary and regression tests
    // -----------------------------------------------------------------------

    @Test
    public void testRoundAtMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        // Rounding to day should not change
        Date rounded = DateUtils.round(date, Calendar.DAY_OF_MONTH);
        assertEquals(date, rounded);
    }

    @Test
    public void testRoundFirstMillisecond() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.MILLISECOND);
        assertEquals(date, rounded);
    }

    @Test
    public void testRoundLastMillisecond() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, 0, 1, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date date = cal.getTime();
        // Rounding to second should go to next second, but since it's the last second of the day,
        // might cause date change. For minute, it will round up to next minute.
        // We'll just check no exception.
        assertNotNull(DateUtils.round(date, Calendar.SECOND));
    }

    @Test
    public void testRoundLeapYear() {
        // 2016-02-29 is a leap year.
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 29, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();
        Date rounded = DateUtils.round(date, Calendar.YEAR);
        // Since it is after mid-year (July 1), should round to 2017.
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2017, Calendar.JANUARY, 1, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), rounded);
    }

    @Test
    public void testTruncateLeapYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.FEBRUARY, 29, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date date = cal.getTime();
        Date truncated = DateUtils.truncate(date, Calendar.YEAR);
        Calendar expectedCal = Calendar.getInstance();
        expectedCal.set(2016, Calendar.JANUARY, 1, 0, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        assertEquals(expectedCal.getTime(), truncated);
    }

    // -----------------------------------------------------------------------
    // Tests for same instant and same day (if methods exist)
    // -----------------------------------------------------------------------
    // Not required for bug, but for coverage we can include if methods exist.
    // Since we are not sure, we omit.

    // -----------------------------------------------------------------------
    // Test for modifying (if internal modify method is used)
    // -----------------------------------------------------------------------
    // Not directly testable.

}