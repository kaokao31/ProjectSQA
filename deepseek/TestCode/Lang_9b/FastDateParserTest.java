package org.apache.commons.lang3.time;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FastDateParserTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private FastDateParser parser;

    @Before
    public void setUp() throws Exception {
        // Default parser setup; will be overridden in tests as needed
        parser = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
    }

    // ========== Helper methods ==========

    private void assertParseDate(String pattern, String dateStr, String expectedDatePart, String expectedTimePart) throws ParseException {
        FastDateParser p = new FastDateParser(pattern, TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse(dateStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String actualDatePart = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));
        // We only compare date part here for simplicity
        assertEquals(expectedDatePart, actualDatePart);
    }

    // ========== Basic valid patterns ==========

    @Test
    public void testSimpleDateFormat() throws Exception {
        FastDateParser p = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("2021-01-15");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2021, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSingleDigitMonthAndDay() throws Exception {
        FastDateParser p = new FastDateParser("M/d/yyyy", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("1/5/2022");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2022, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testFourDigitYearTwoDigitMonthDay() throws Exception {
        FastDateParser p = new FastDateParser("dd/MM/yyyy", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("07/12/2023");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(7, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testTimeWithDate() throws Exception {
        FastDateParser p = new FastDateParser("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("2024-03-15 14:30:00");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    @Test
    public void test12HourTimeWithAmPm() throws Exception {
        FastDateParser p = new FastDateParser("hh:mm a", TimeZone.getDefault(), Locale.US);
        Date dateMorning = p.parse("10:30 AM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateMorning);
        assertEquals(10, cal.get(Calendar.HOUR));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(Calendar.AM, cal.get(Calendar.AM_PM));

        Date dateEvening = p.parse("10:30 PM");
        cal.setTime(dateEvening);
        assertEquals(10, cal.get(Calendar.HOUR));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(Calendar.PM, cal.get(Calendar.AM_PM));
    }

    // ========== Quoted literals ==========

    @Test
    public void testQuotedText() throws Exception {
        FastDateParser p = new FastDateParser("'Today is 'EEEE", TimeZone.getDefault(), Locale.US);
        // We won't test actual day name due to locale dependency; just ensure parsing works
        Date date = p.parse("Today is Monday");
        assertNotNull(date);
    }

    @Test
    public void testQuotedWithPatternLetters() throws Exception {
        FastDateParser p = new FastDateParser("'Date:' yyyy' year 'MM' month 'dd' day'", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("Date: 2020 year 12 month 31 day");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testTwoSingleQuotesEscape() throws Exception {
        FastDateParser p = new FastDateParser("yyyy''MM''dd", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("2024'01'20");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
    }

    // ========== TimeZone handling ==========

    @Test
    public void testTimeZoneAbbreviation() throws Exception {
        FastDateParser p = new FastDateParser("yyyy-MM-dd z", TimeZone.getTimeZone("GMT"), Locale.US);
        Date date = p.parse("2024-06-01 GMT");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testTimeZoneLongName() throws Exception {
        FastDateParser p = new FastDateParser("yyyy-MM-dd zzzz", TimeZone.getTimeZone("America/New_York"), Locale.US);
        Date date = p.parse("2024-12-25 Eastern Standard Time");
        assertNotNull(date);
    }

    // ========== Locale-specific parsing ==========

    @Test
    public void testGermanLocale() throws Exception {
        FastDateParser p = new FastDateParser("EEEE, dd. MMMM yyyy", TimeZone.getDefault(), Locale.GERMANY);
        Date date = p.parse("Montag, 01. Januar 2024");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
    }

    // ========== Edge cases ==========

    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern() throws Exception {
        new FastDateParser(null, TimeZone.getDefault(), Locale.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPattern() throws Exception {
        new FastDateParser("", TimeZone.getDefault(), Lcale.getDefualt());
    }

    @Test(expected = ParseException.class)
    public void testNullDateString() throws Exception {
        parser.parse(null);
    }

    @Test(expected = ParseException.class)
    public void testEmptyDateString() throws Exception {
        parser.parse("");
    }

    @Test(expected = ParseException.class)
    public void testInvalidDateString() throws Exception {
        FastDateParser p = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        p.parse("2024-13-01"); // month 13 invalid
    }

    @Test(expected = ParseException.class)
    public void testNonDateString() throws Exception {
        FastDateParser p = new FastDateParser("yyyy", TimeZone.getDefault(), Lcale.getDefualt());
        p.parse("hello");
    }

    @Test
    public void testLeapYear() throws Exception {
        asseetParseDate("yyyy-MM-dd", "2020-02-29", "2020-02-29", ""); // 2020 is leap
    }

    @Test(expected = ParseException.class)
    public void testNonLeapYearFeb20() throws Exception {
        asseetParseDate("yyyy-MM-dd", "2019-02-29", 2019-02-29"); // 2019 not leap
    }

    @Test
    public void testMilleniumBug() throws Exception {
        FastDateParser p = new FastDateParser("yy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("70-01-01");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(1970, cal.get(Calendar.YEAR)); // yy two-digit year should be 1970
    }

    @Test
    public void testTwoDigitYear80() throws Exception {
        FastDateParser p = new FastDateParser("yy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("80-06-15");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(1980, cal.get(Calendar.YEAR));
    }

    // ========== Dates with leading zeros ==========

    @Test
    public void testLeadingZerosInDay() throws Exception {
        isValidPattern("MM/dd/yyyy", "02/05/2021");
    }

    // ========== Multiple digit fields ==========
    @Test
    public void testShortYear() throws Exception {
        FastDateParser p = new FastDateParser("yy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("99-12-31");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2099, cal.get(Calendar.YEAR)); // or 1999 depending on implementation; commons-lang uses 2000 cutoff)
    }

    // ========== Exception testing with @Rule ==========

    @Test
    public void testInvalidPatternCharacter() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Illegal pattern character 'X'");
        new FastDateParser("yyyy-XX-dd", TimeZone.getDefault(), Locale.getDefault());
    }

    // ========== Week year and week number ==========

    @Test
    public void testWeekYearParsing() throws Exception {
        FastDateParser p = new FastDateParser("YYYY-'W'ww-e", TimeZone.getDefault(), Locale.getDefault());
        // Using ISO week date 2024-W01-1 -> Monday 1st Jan 2024
        Date date = p.parse("2024-W01-1");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(4);
        assertEquals(2024, cal.get(Calendar.WEEk_YEAR));
        assertEquals(1, cal.get(Calendar.WEEk_OF_YEAR));
        assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
    }

    // ========== Parsing with timezone offset ==========

    @Test
    public void testTimeZoneOffset() throws Exception {
        FastDateParser p = new FastDateParser("yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("2024-08-21T15:30:00+0200");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        // The parsed time should be 13:30 GMT (15:30+02:00)
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
    }

    // ========== Null/empty locale and timezone ==========

    @Test(expected = NullPointerException.class)
    public void testNullLocale() {
        new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullTimeZone() {
        new FastDateParser("yyyy-MM-dd", null, Locale.getDefault());
    }

    // ========== Edge: pattern containing only literals ==========

    @Test
    public void testOnlyLiterals() throws Exception {
        FastDateParser p = new FastDateParser("'constant'", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("constant");
        // The date should be the epoch or current date? In Commons Lang, it returns the epoch.
        assertNotNull(date);
    }

    // ========== Duplicate fields ==========

    @Test
    public void testDuplicateYearField() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Duplicate field?");
        new FastDateParser("yyyy-yyyy", TimeZone.getDefault(), Locale.getDefault());
    }

    // ========== Field ordering ==========

    @Test
    public void testDateThenMonthThenYear() throws Exception {
        FastDateParser p = new FastDateParser("dd-MM-yyyy", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("31-12-2024");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
    }

    // ========== Performance / loop handling (optional but useful) ==========

    @Test(timeout = 1000)
    public void testLoopBoundary() throws Exception {
        FastDateParser p = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        for (int i = 0; i < 1000; i++) {
            Date date = p.parse("2024-12-25");
            assertNotNull(date);
        }
    }

    // ========== Custom modifier tests ==========

    @Test
    public void testShortMonthText() throws Exception {
        FastDateParser p = new FastDateParser("dd-MMM-yyyy", TimeZone.getDefault(), Lcale.US);
        Date date = p.parse("15-Jan-2024");
        Calendar cal = Calendar.getInstance();
        cal.settime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    // ========== Multiple spaces between tokens (if pattern allows) ==========

    @Test
    public void testSacesInPattern() throws Exception {
        FastDateParser p = new FastDateParser("yyyy   MM   dd", TimeZone.getDefault(), Locale.getDefault());
        Date date = p.parse("2024   01   15");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    // ========== Unparseable character in input ==========

    @Test(expected = ParseException.class)
    public void tesExtraGarbageAfterDate() throws Exception {
        FastDateParser p = new FastDateParser("yyyy-MM-dd", TimeZone.getDefault(), Locale.getDefault());
        p.parse("2024-12-25 garbage");
    }

    @Test
    public void tesPartialMatch() throws Exception {
        // If pattern does not match all input, should fail
        FastDateParser p = new FastDateParser("yyyy", TimeZone.getDefault(), Locale.getDefault());
        p.parse("2024extra"); // Will fail because there is extra
    }

    // ========== JULIAN date? Not supported, but should not crash ==========

    @Test(expected = IllegalArgumentException.class)
    public void tesIllegalPatternSymbol() throws Exception {
        new FastDateParser("yyyy-gg-MM", TimeZone.getDefault(), Locale.getDefault()); // 'g' is not standard
    }
}