package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for LocalDate, targeting bug ID 12 in Defects4J (Joda-Time).
 * Focuses on plusYears and plusMonths with leap year edge cases.
 */
public class LocalDateTest {

    // --- plusYears tests ---

    @Test
    public void testPlusYears_LeapDayToNonLeapYear() {
        // February 29, 2000 (leap) -> plus 1 year -> should be February 28, 2001
        LocalDate leapDay = new LocalDate(2000, 2, 29);
        LocalDate expected = new LocalDate(2001, 2, 28);
        assertEquals(expected, leapDay.plusYears(1));
    }

    @Test
    public void testPlusYears_LeapDayToLeapYear() {
        // February 29, 2000 -> plus 4 years -> February 29, 2004
        LocalDate leapDay = new LocalDate(2000, 2, 29);
        LocalDate expected = new LocalDate(2004, 2, 29);
        assertEquals(expected, leapDay.plusYears(4));
    }

    @Test
    public void testPlusYears_LeapDayToNonLeapYearNegative() {
        // February 29, 2004 -> minus 4 years -> February 29, 2000
        LocalDate leapDay = new LocalDate(2004, 2, 29);
        LocalDate expected = new LocalDate(2000, 2, 29);
        assertEquals(expected, leapDay.plusYears(-4));
    }

    @Test
    public void testPlusYears_NonLeapDay() {
        // March 15, 2000 -> plus 1 year -> March 15, 2001
        LocalDate date = new LocalDate(2000, 3, 15);
        LocalDate expected = new LocalDate(2001, 3, 15);
        assertEquals(expected, date.plusYears(1));
    }

    @Test
    public void testPlusYears_Zero() {
        LocalDate date = new LocalDate(2000, 6, 15);
        assertSame(date, date.plusYears(0));
    }

    @Test
    public void testPlusYears_Negative() {
        LocalDate date = new LocalDate(2005, 6, 15);
        LocalDate expected = new LocalDate(2000, 6, 15);
        assertEquals(expected, date.plusYears(-5));
    }

    @Test
    public void testPlusYears_EndOfMonthFebruary() {
        // January 31, 2000 -> plus 1 month -> February 29, 2000 (leap year)
        LocalDate jan31 = new LocalDate(2000, 1, 31);
        LocalDate expected = new LocalDate(2000, 2, 29);
        assertEquals(expected, jan31.plusMonths(1));
    }

    @Test
    public void testPlusYears_EndOfMonthFebruaryNonLeap() {
        // January 31, 2001 -> plus 1 month -> February 28, 2001
        LocalDate jan31 = new LocalDate(2001, 1, 31);
        LocalDate expected = new LocalDate(2001, 2, 28);
        assertEquals(expected, jan31.plusMonths(1));
    }

    // --- plusMonths tests ---

    @Test
    public void testPlusMonths_LeapDayToNonLeapYear() {
        // February 29, 2000 -> plus 12 months -> February 28, 2001
        LocalDate leapDay = new LocalDate(2000, 2, 29);
        LocalDate expected = new LocalDate(2001, 2, 28);
        assertEquals(expected, leapDay.plusMonths(12));
    }

    @Test
    public void testPlusMonths_LeapDayToLeapYear() {
        // February 29, 2000 -> plus 48 months -> February 29, 2004
        LocalDate leapDay = new LocalDate(2000, 2, 29);
        LocalDate expected = new LocalDate(2004, 2, 29);
        assertEquals(expected, leapDay.plusMonths(48));
    }

    @Test
    public void testPlusMonths_LeapDayToNonLeapYearNegative() {
        // February 29, 2004 -> minus 48 months -> February 29, 2000
        LocalDate leapDay = new LocalDate(2004, 2, 29);
        LocalDate expected = new LocalDate(2000, 2, 29);
        assertEquals(expected, leapDay.plusMonths(-48));
    }

    @Test
    public void testPlusMonths_NonLeapDay() {
        LocalDate date = new LocalDate(2000, 3, 15);
        LocalDate expected = new LocalDate(2001, 3, 15);
        assertEquals(expected, date.plusMonths(12));
    }

    @Test
    public void testPlusMonths_Zero() {
        LocalDate date = new LocalDate(2000, 6, 15);
        assertSame(date, date.plusMonths(0));
    }

    @Test
    public void testPlusMonths_Negative() {
        LocalDate date = new LocalDate(2005, 6, 15);
        LocalDate expected = new LocalDate(2000, 6, 15);
        assertEquals(expected, date.plusMonths(-60));
    }

    @Test
    public void testPlusMonths_EndOfMonthMarch() {
        // January 31, 2000 -> plus 2 months -> March 31, 2000
        LocalDate jan31 = new LocalDate(2000, 1, 31);
        LocalDate expected = new LocalDate(2000, 3, 31);
        assertEquals(expected, jan31.plusMonths(2));
    }

    @Test
    public void testPlusMonths_EndOfMonthApril() {
        // January 31, 2000 -> plus 3 months -> April 30, 2000
        LocalDate jan31 = new LocalDate(2000, 1, 31);
        LocalDate expected = new LocalDate(2000, 4, 30);
        assertEquals(expected, jan31.plusMonths(3));
    }

    // --- Additional edge cases ---

    @Test
    public void testPlusYears_YearZero() {
        // Year 0 is leap in proleptic Gregorian? Joda-Time supports year 0.
        // February 29, 0 -> plus 1 year -> February 28, 1
        LocalDate leapDay = new LocalDate(0, 2, 29);
        LocalDate expected = new LocalDate(1, 2, 28);
        assertEquals(expected, leapDay.plusYears(1));
    }

    @Test
    public void testPlusYears_NegativeYear() {
        // February 29, 4 -> minus 4 years -> February 29, 0
        LocalDate leapDay = new LocalDate(4, 2, 29);
        LocalDate expected = new LocalDate(0, 2, 29);
        assertEquals(expected, leapDay.plusYears(-4));
    }

    @Test
    public void testPlusMonths_YearZero() {
        // February 29, 0 -> plus 12 months -> February 28, 1
        LocalDate leapDay = new LocalDate(0, 2, 29);
        LocalDate expected = new LocalDate(1, 2, 28);
        assertEquals(expected, leapDay.plusMonths(12));
    }

    @Test
    public void testPlusMonths_NegativeYear() {
        // February 29, 4 -> minus 48 months -> February 29, 0
        LocalDate leapDay = new LocalDate(4, 2, 29);
        LocalDate expected = new LocalDate(0, 2, 29);
        assertEquals(expected, leapDay.plusMonths(-48));
    }

    @Test
    public void testPlusYears_LargePositive() {
        // February 29, 2000 -> plus 400 years -> February 29, 2400 (leap)
        LocalDate leapDay = new LocalDate(2000, 2, 29);
        LocalDate expected = new LocalDate(2400, 2, 29);
        assertEquals(expected, leapDay.plusYears(400));
    }

    @Test
    public void testPlusYears_LargeNegative() {
        // February 29, 2000 -> minus 400 years -> February 29, 1600 (leap)
        LocalDate leapDay = new LocalDate(2000, 2, 29);
        LocalDate expected = new LocalDate(1600, 2, 29);
        assertEquals(expected, leapDay.plusYears(-400));
    }

    @Test
    public void testPlusMonths_LargePositive() {
        // February 29, 2000 -> plus 4800 months -> February 29, 2400
        LocalDate leapDay = new LocalDate(2000, 2, 29);
        LocalDate expected = new LocalDate(2400, 2, 29);
        assertEquals(expected, leapDay.plusMonths(4800));
    }

    @Test
    public void testPlusMonths_LargeNegative() {
        // February 29, 2000 -> minus 4800 months -> February 29, 1600
        LocalDate leapDay = new LocalDate(2000, 2, 29);
        LocalDate expected = new LocalDate(1600, 2, 29);
        assertEquals(expected, leapDay.plusMonths(-4800));
    }

    // --- Constructor and basic getter tests (for coverage) ---

    @Test
    public void testConstructor() {
        LocalDate date = new LocalDate(2020, 12, 25);
        assertEquals(2020, date.getYear());
        assertEquals(12, date.getMonthOfYear());
        assertEquals(25, date.getDayOfMonth());
    }

    @Test
    public void testEqualsAndHashCode() {
        LocalDate a = new LocalDate(2000, 2, 29);
        LocalDate b = new LocalDate(2000, 2, 29);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testToString() {
        LocalDate date = new LocalDate(2000, 2, 29);
        assertNotNull(date.toString());
    }
}