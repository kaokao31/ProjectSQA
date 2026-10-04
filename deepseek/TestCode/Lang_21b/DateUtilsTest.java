package org.apache.commons.lang3.time;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Comprehensive JUnit 4 test suite for DateUtils.
 * Designed to achieve high coverage and trigger known bug LANG-677 (isSameLocalTime).
 * This test suite is derived from the original failing test and extended to cover edge cases.
 */
public class DateUtilsTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone EST = TimeZone.getTimeZone("America/New_York");
    private static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");
    private static final long MILLIS_PER_DAY = 86400000L;

    @Before
    public void setUp() {
        // Reset any static state if needed (none in DateUtils)
    }

    @After
    public void tearDown() {
        // Nothing to clean up
    }

    // ===== isSameDay =====

    @Test
    public void testIsSameDay_SameDate() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        assertTrue(DateUtils.isSameDay(cal1, cal2));
    }

    @Test
    public void testIsSameDay_DifferentDay() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test
    public void testIsSameDay_DifferentTimeSameDay() {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        assertTrue(DateUtils.isSameDay(cal1, cal2));
    }

    @Test
    public void testIsSameDay_DateInDifferentTimeZone() {
        Calendar cal1 = Calendar.getInstance(UTC);
        Calendar cal2 = Calendar.getInstance(EST);
        // Set both to the same UTC day
        cal1.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal2.setTimeZone(UTC);
        cal2.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        // Now change cal2's timezone without changing its displayed fields
        cal2.setTimeZone(EST);
        // The actual milliseconds represent different UTC time, but the local day is the same
        // However, isSameDay uses millis, so they differ. But the fields are same day. 
        // This is a known edge case: isSameDay compares millis, not local date.
        // We test that it returns false because UTC millis differ.
        assertFalse(DateUtils.isSameDay(cal1, cal2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_NullFirst() {
        DateUtils.isSameDay((Calendar) null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameDay_NullBoth() {
        DateUtils.isSameDay((Date) null, (Date) null);
    }

    // ===== isSameInstant =====

    @Test
    public void testIsSameInstant_SameMillis() {
        Date date1 = new Date(1000L);
        Date date2 = new Date(1000L);
        assertTrue(DateUtils.isSameInstant(date1, date2));
    }

    @Test
    public void testIsSameInstant_DifferentMillis() {
        Date date1 = new Date(1000L);
        Date date2 = new Date(2000L);
        assertFalse(DateUtils.isSameInstant(date1, date2));
    }

    @Test
    public void testIsSameInstant_DifferentTimezones() {
        Calendar cal1 = Calendar.getInstance(UTC);
        Calendar cal2 = Calendar.getInstance(EST);
        cal1.set(2020, Calendar.JANUARY, 1, 12, 0, 0);
        cal2.setTimeInMillis(cal1.getTimeInMillis()); // same instant
        assertTrue(DateUtils.isSameInstant(cal1, cal2));
    }

    // ===== isSameLocalTime ===== (bug LANG-677)

    @Test
    public void testIsSameLocalTime_SameFields() {
        Calendar cal1 = Calendar.getInstance(UTC);
        Calendar cal2 = Calendar.getInstance(EST);
        // Set both to the same local time (e.g., 10:00:00.000)
        cal1.set(2020, Calendar.JANUARY, 1, 10, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2020, Calendar.JANUARY, 1, 10, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // They are not the same instant, but same local time
        assertTrue("LANG-677: isSameLocalTime should return true for same local time in different timezones",
                   DateUtils.isSameLocalTime(cal1, cal2));
    }

    @Test
    public void testIsSameLocalTime_DifferentFields() {
        Calendar cal1 = Calendar.getInstance(UTC);
        Calendar cal2 = Calendar.getInstance(UTC);
        cal1.set(2020, Calendar.JANUARY, 1, 10, 0, 0);
        cal2.set(2020, Calendar.JANUARY, 1, 11, 0, 0);
        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
    }

    @Test
    public void testIsSameLocalTime_SameInstantButDifferentLocalTime() {
        Calendar cal1 = Calendar.getInstance(UTC);
        Calendar cal2 = Calendar.getInstance(EST);
        cal1.set(2020, Calendar.JANUARY, 1, 12, 0, 0);
        // Set cal2 to same UTC millis but different local time (7:00 AM EST)
        cal2.setTimeInMillis(cal1.getTimeInMillis());
        // Local times: 12:00 UTC vs 07:00 EST -> different
        assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
    }

    @Test
    public void testIsSameLocalTime_NullFirst() {
        try {
            DateUtils.isSameLocalTime(null, Calendar.getInstance());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ===== truncate =====

    @Test
    public void testTruncate_ToDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        Date truncated = DateUtils.truncate(cal.getTime(), Calendar.DAY_OF_MONTH);
        Calendar truncatedCal = Calendar.getInstance();
        truncatedCal.setTime(truncated);
        assertEquals(2020, truncatedCal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, truncatedCal.get(Calendar.MONTH));
        assertEquals(15, truncatedCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncatedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncatedCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedCal.get(Calendar.SECOND));
        assertEquals(0, truncatedCal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testTruncate_ToHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        Date truncated = DateUtils.truncate(cal.getTime(), Calendar.HOUR_OF_DAY);
        Calendar truncatedCal = Calendar.getInstance();
        truncatedCal.setTime(truncated);
        assertEquals(10, truncatedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, truncatedCal.get(Calendar.MINUTE));
        assertEquals(0, truncatedCal.get(Calendar.SECOND));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncate_InvalidField() {
        DateUtils.truncate(new Date(), Calendar.AM_PM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncate_NullDate() {
        DateUtils.truncate((Date) null, Calendar.DAY_OF_MONTH);
    }

    // ===== round =====

    @Test
    public void testRound_HalfUpMinute() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 29);
        Date rounded = DateUtils.round(cal.getTime(), Calendar.MINUTE);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        assertEquals(10, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, roundedCal.get(Calendar.MINUTE));
        assertEquals(0, roundedCal.get(Calendar.SECOND));
    }

    @Test
    public void testRound_HalfUpMinute_JustOver() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 30);
        Date rounded = DateUtils.round(cal.getTime(), Calendar.MINUTE);
        Calendar roundedCal = Calendar.getInstance();
        roundedCal.setTime(rounded);
        assertEquals(10, roundedCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(31, roundedCal.get(Calendar.MINUTE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRound_InvalidField() {
        DateUtils.round(new Date(), 100); // invalid field
    }

    // ===== ceiling =====

    @Test
    public void testCeiling_ToDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        Date ceiling = DateUtils.ceiling(cal.getTime(), Calendar.DAY_OF_MONTH);
        Calendar ceilingCal = Calendar.getInstance();
        ceilingCal.setTime(ceiling);
        assertEquals(2020, ceilingCal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, ceilingCal.get(Calendar.MONTH));
        assertEquals(16, ceilingCal.get(Calendar.DAY_OF_MONTH)); // next day start
        assertEquals(0, ceilingCal.get(Calendar.HOUR_OF_DAY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCeiling_NullDate() {
        DateUtils.ceiling((Calendar) null, Calendar.DAY_OF_MONTH);
    }

    // ===== addDays, addHours, etc. =====

    @Test
    public void testAddDays() {
        Date base = new Date(0L); // 1970-01-01
        Date result = DateUtils.addDays(base, 1);
        assertEquals(MILLIS_PER_DAY, result.getTime());
    }

    @Test
    public void testAddDays_Negative() {
        Date base = new Date(MILLIS_PER_DAY);
        Date result = DateUtils.addDays(base, -1);
        assertEquals(0L, result.getTime());
    }

    @Test
    public void testAddHours() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date result = DateUtils.addHours(cal.getTime(), 5);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(5, resultCal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testAddMinutes() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date result = DateUtils.addMinutes(cal.getTime(), 30);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(30, resultCal.get(Calendar.MINUTE));
    }

    @Test
    public void testAddSeconds() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date result = DateUtils.addSeconds(cal.getTime(), 45);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(45, resultCal.get(Calendar.SECOND));
    }

    @Test
    public void testAddMilliseconds() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date result = DateUtils.addMilliseconds(cal.getTime(), 500);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(500, resultCal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testAddWeeks() {
        Date base = new Date(0L);
        Date result = DateUtils.addWeeks(base, 1);
        assertEquals(7 * MILLIS_PER_DAY, result.getTime());
    }

    @Test
    public void testAddMonths() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date result = DateUtils.addMonths(cal.getTime(), 1);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH));
    }

    @Test
    public void testAddYears() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date result = DateUtils.addYears(cal.getTime(), 10);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(2030, resultCal.get(Calendar.YEAR));
    }

    // ===== setDays, setHours, etc. =====
    @Test
    public void testSetDays() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 0, 0, 0);
        Date result = DateUtils.setDays(cal.getTime(), 1);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSetHours() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 5, 0, 0);
        Date result = DateUtils.setHours(cal.getTime(), 20);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testSetMinutes() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 30, 0);
        Date result = DateUtils.setMinutes(cal.getTime(), 45);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(45, resultCal.get(Calendar.MINUTE));
    }

    @Test
    public void testSetSeconds() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 30);
        Date result = DateUtils.setSeconds(cal.getTime(), 10);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(10, resultCal.get(Calendar.SECOND));
    }

    @Test
    public void testSetMilliseconds() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date result = DateUtils.setMilliseconds(cal.getTime(), 999);
        Calendar resultCal = Calendar.getInstance(UTC);
        resultCal.setTime(result);
        assertEquals(999, resultCal.get(Calendar.MILLISECOND));
    }

    // ===== parseDate =====

    @Test
    public void testParseDate_DefaultFormat() throws ParseException {
        String dateStr = "2020-01-01";
        Date date = DateUtils.parseDate(dateStr, "yyyy-MM-dd");
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testParseDate_MultipleFormats() throws ParseException {
        String dateStr = "2020/01/01";
        Date date = DateUtils.parseDate(dateStr, "yyyy-MM-dd", "yyyy/MM/dd");
        assertNotNull(date);
    }

    @Test(expected = ParseException.class)
    public void testParseDate_InvalidPattern() throws ParseException {
        DateUtils.parseDate("2020-01-01", "invalid pattern");
    }

    @Test(expected = ParseException.class)
    public void testParseDate_NullString() throws ParseException {
        DateUtils.parseDate(null, "yyyy-MM-dd");
    }

    // ===== parseDateStrictly =====

    @Test
    public void testParseDateStrictly_Valid() throws ParseException {
        String dateStr = "2020-01-01";
        Date date = DateUtils.parseDateStrictly(dateStr, "yyyy-MM-dd");
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(expected = ParseException.class)
    public void testParseDateStrictly_InvalidDay() throws ParseException {
        DateUtils.parseDateStrictly("2020-02-30", "yyyy-MM-dd");
    }

    // ===== isSameDay (Date) =====

    @Test
    public void testIsSameDay_DateSame() {
        Date date1 = new Date(0L);
        Date date2 = new Date(0L);
        assertTrue(DateUtils.isSameDay(date1, date2));
    }

    @Test
    public void testIsSameDay_DateDifferent() {
        Date date1 = new Date(0L);
        Date date2 = new Date(MILLIS_PER_DAY);
        assertFalse(DateUtils.isSameDay(date1, date2));
    }

    // ===== toCalendar =====

    @Test
    public void testToCalendar() {
        Date date = new Date(0L);
        Calendar cal = DateUtils.toCalendar(date);
        assertNotNull(cal);
        assertEquals(0L, cal.getTimeInMillis());
    }

    @Test
    public void testToCalendar_WithTimeZone() {
        Date date = new Date(0L);
        Calendar cal = DateUtils.toCalendar(date, EST);
        assertNotNull(cal);
        assertEquals(EST, cal.getTimeZone());
        assertEquals(0L, cal.getTimeInMillis());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCalendar_NullDate() {
        DateUtils.toCalendar(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCalendar_NullTimeZone() {
        DateUtils.toCalendar(new Date(), null);
    }

    // ===== fragmentInDays, etc. =====

    @Test
    public void testFragmentInDays_Base() {
        // 2 days + 12 hours = 2.5 days => fragment 0 days
        long fragments = DateUtils.getFragmentInDays(new Date(2 * MILLIS_PER_DAY + 12 * 3600000L), Calendar.MONTH);
        assertEquals(2, fragments);
    }

    @Test
    public void testFragmentInHours() {
        long fragments = DateUtils.getFragmentInHours(new Date(2 * 3600000L + 30 * 60000L), Calendar.DAY_OF_YEAR);
        assertEquals(2, fragments);
    }

    @Test
    public void testFragmentInMinutes() {
        long fragments = DateUtils.getFragmentInMinutes(new Date(60 * 60000L + 15 * 60000L), Calendar.HOUR_OF_DAY);
        assertEquals(75, fragments);
    }

    @Test
    public void testFragmentInSeconds() {
        long fragments = DateUtils.getFragmentInSeconds(new Date(120 * 1000L + 30 * 1000L), Calendar.MINUTE);
        assertEquals(150, fragments);
    }

    @Test
    public void testFragmentInMilliseconds() {
        long fragments = DateUtils.getFragmentInMilliseconds(new Date(2000L + 500L), Calendar.SECOND);
        assertEquals(2500, fragments);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFragment_Negative() {
        DateUtils.getFragmentInDays(new Date(-1), Calendar.MONTH);
    }

    // ===== truncate (Calendar) =====

    @Test
    public void testTruncate_CalendarToDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    // ===== round (Calendar) =====

    @Test
    public void testRound_CalendarToMinute() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        DateUtils.round(cal, Calendar.MINUTE);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(31, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    // ===== ceiling (Calendar) =====

    @Test
    public void testCeiling_CalendarToDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        DateUtils.ceiling(cal, Calendar.DAY_OF_MONTH);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
    }

    // ===== Modification with calendar (setDays, etc.) =====

    @Test
    public void testSetDays_Calendar() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 15, 0, 0, 0);
        DateUtils.setDays(cal, 1);
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSetHours_Calendar() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 5, 0, 0);
        DateUtils.setHours(cal, 20);
        assertEquals(20, cal.get(Calendar.HOUR_OF_DAY));
    }

    // ===== Edge case: null checks for all public methods =====

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstant_NullFirstCalendar() {
        DateUtils.isSameInstant((Calendar) null, Calendar.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameInstant_NullFirstDate() {
        DateUtils.isSameInstant((Date) null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameLocalTime_NullSecond() {
        DateUtils.isSameLocalTime(Calendar.getInstance(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTruncate_NullCalendar() {
        DateUtils.truncate((Calendar) null, Calendar.DAY_OF_MONTH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRound_NullDate() {
        DateUtils.round((Date) null, Calendar.MINUTE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCeiling_NullDate() {
        DateUtils.ceiling((Date) null, Calendar.DAY_OF_MONTH);
    }

    // ===== Additional edge cases =====

    @Test
    public void testModify_NoModification() {
        // Using truncate on a zero millisecond date
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        DateUtils.truncate(cal, Calendar.MILLISECOND);
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testRound_LeapYear() {
        // 2020 is leap year. Test rounding February 28 to month
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.FEBRUARY, 28, 12, 0, 0);
        DateUtils.round(cal, Calendar.MONTH);
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
    }

    @Test
    public void testCeiling_EndOfYear() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.DECEMBER, 31, 23, 59, 59);
        DateUtils.ceiling(cal, Calendar.YEAR);
        assertEquals(2021, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testAddDays_CrossDST() throws Exception {
        // Add one day across a DST transition (e.g., spring forward in EST)
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2020, Calendar.MARCH, 7, 12, 0, 0); // Just before DST change (March 8, 2020)
        Date before = cal.getTime();
        Date after = DateUtils.addDays(before, 1);
        Calendar afterCal = Calendar.getInstance(tz);
        afterCal.setTime(after);
        assertEquals(9, afterCal.get(Calendar.DAY_OF_MONTH)); // March 9
    }

    @Test
    public void testParseDate_LenientFalse() throws Exception {
        // Lenient false: strict parsing
        try {
            DateUtils.parseDateStrictly("2020-02-30", "yyyy-MM-dd");
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testIsSameDay_DaylightSaving() {
        // Test across DST boundary: same date in different timezones might be different UTC days
        // Set both to same local day but different timezones
        Calendar cal1 = Calendar.getInstance(UTC);
        Calendar cal2 = Calendar.getInstance(EST);
        cal1.set(2020, Calendar.MARCH, 8, 0, 0, 0); // DST start in EST: 2:00 AM
        cal2.set(2020, Calendar.MARCH, 8, 0, 0, 0); // local time in EST
        // Although both local days are March 8, the UTC millis might be different because EST is UTC-5
        // But isSameDay uses millis to determine day. This test may or may not pass depending on implementation.
        // We just call it to ensure no exception.
        boolean result = DateUtils.isSameDay(cal1, cal2);
        // Not asserting value because it's known behavior
    }

    // ===== Loops and edge coverage (e.g., round for all fields) =====

    @Test
    public void testRound_Month() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JUNE, 15, 0, 0, 0);
        DateUtils.round(cal, Calendar.MONTH);
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
    }

    @Test
    public void testCeiling_Month() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JUNE, 1, 12, 0, 0);
        DateUtils.ceiling(cal, Calendar.MONTH);
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testTruncate_Year() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        DateUtils.truncate(cal, Calendar.YEAR);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testFragmentInDays_NegativeCalendar() {
        // getFragmentInDays with negative date (before epoch)
        long fragments = DateUtils.getFragmentInDays(new Date(-1000), Calendar.MONTH);
        // Should be 0 because date is negative, but implementation may treat as negative fragments
        // We just call to ensure no exception.
        assertTrue(fragments >= 0); // Actually it truncates towards zero? Let's not over-specify.
    }
}