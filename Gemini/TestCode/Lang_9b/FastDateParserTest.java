package org.apache.commons.lang3.time;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FastDateParserTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone EST = TimeZone.getTimeZone("America/New_York");
    private static final Locale US = Locale.US;
    private static final Locale GERMANY = Locale.GERMANY;
    private static final Locale JAPAN = Locale.JAPAN;

    @Test
    public void testBasicPatternParse() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd HH:mm:ss.SSS", UTC, US);
        final Date date = fdp.parse("2023-11-15 14:30:45.123");
        
        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParsePositionOffset() {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd", UTC, US);
        final String input = "Prefix 2023-10-05 Suffix";
        final ParsePosition pos = new ParsePosition(7);
        final Date date = fdp.parse(input, pos);

        assertNotNull(date);
        assertEquals(17, pos.getIndex());
        assertEquals(-1, pos.getErrorIndex());

        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testParsePositionFailure() {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd", UTC, US);
        final ParsePosition pos = new ParsePosition(0);
        final Date date = fdp.parse("InvalidDate", pos);

        assertNull(date);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test(expected = ParseException.class)
    public void testParseExceptionOnMismatch() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd", UTC, US);
        fdp.parse("Not a date");
    }

    @Test(expected = ParseException.class)
    public void testParseExceptionOnShortString() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd", UTC, US);
        fdp.parse("2023");
    }

    @Test
    public void testTwoDigitYearParsing() throws ParseException {
        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.set(2000, Calendar.JANUARY, 1);
        final Date centuryStart = cal.getTime();

        final FastDateParser fdp = new FastDateParser("yy-MM-dd", UTC, US, centuryStart);
        final Date d1 = fdp.parse("23-05-10");
        cal.setTime(d1);
        assertEquals(2023, cal.get(Calendar.YEAR));

        final Date d2 = fdp.parse("99-05-10");
        cal.setTime(d2);
        assertEquals(2099, cal.get(Calendar.YEAR));
    }

    @Test
    public void testCenturyStartRollover() throws ParseException {
        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.set(1950, Calendar.JANUARY, 1);
        final Date centuryStart = cal.getTime();

        final FastDateParser fdp = new FastDateParser("yy-MM-dd", UTC, US, centuryStart);
        final Date parsed1 = fdp.parse("49-01-01");
        cal.setTime(parsed1);
        assertEquals(2049, cal.get(Calendar.YEAR));

        final Date parsed2 = fdp.parse("50-01-01");
        cal.setTime(parsed2);
        assertEquals(1950, cal.get(Calendar.YEAR));
    }

    @Test
    public void testSpecialRegexCharactersInPattern() throws ParseException {
        final String pattern = "yyyy.MM.dd 'at' HH:mm:ss (z) [?*+^$()|]";
        final FastDateParser fdp = new FastDateParser(pattern, UTC, US);

        final Date date = fdp.parse("2023.12.25 at 18:00:00 UTC [?*+^$()|]");
        assertNotNull(date);

        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testQuotedLiteralsAndEscapedQuotes() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyy''MM''dd 'o''clock' HH", UTC, US);
        final Date date = fdp.parse("2023'04'15 o'clock 10");

        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testEraParsing() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd G", UTC, US);
        final Date date = fdp.parse("0044-03-15 BC");

        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(Calendar.BC, cal.get(Calendar.ERA));
        assertEquals(44, cal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testMonthTextParsing() throws ParseException {
        final FastDateParser fdpShort = new FastDateParser("dd-MMM-yyyy", UTC, US);
        final Date dShort = fdpShort.parse("15-Jul-2023");
        Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(dShort);
        assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));

        final FastDateParser fdpLong = new FastDateParser("dd-MMMM-yyyy", UTC, US);
        final Date dLong = fdpLong.parse("15-September-2023");
        cal.setTime(dLong);
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH));
    }

    @Test
    public void testDifferentLocales() throws ParseException {
        final FastDateParser fdpGerman = new FastDateParser("dd. MMMM yyyy", GERMANY);
        final Date dGerman = fdpGerman.parse("15. Oktober 2023");
        assertNotNull(dGerman);

        final FastDateParser fdpJapan = new FastDateParser("yyyy/MM/dd", JAPAN);
        final Date dJapan = fdpJapan.parse("2023/10/15");
        assertNotNull(dJapan);
    }

    @Test
    public void testDayOfWeekParsing() throws ParseException {
        final FastDateParser fdp = new FastDateParser("EEEE, dd MMMM yyyy", UTC, US);
        final Date date = fdp.parse("Sunday, 15 October 2023");

        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(2023, cal.get(Calendar.YEAR));
    }

    @Test
    public void testAmPmAndHourVariants() throws ParseException {
        final FastDateParser fdp12 = new FastDateParser("hh:mm a", UTC, US);
        final Date dPm = fdp12.parse("10:30 PM");
        Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(dPm);
        assertEquals(22, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.PM, cal.get(Calendar.AM_PM));

        final Date dAm = fdp12.parse("12:15 AM");
        cal.setTime(dAm);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(Calendar.AM, cal.get(Calendar.AM_PM));

        final FastDateParser fdpK = new FastDateParser("K:mm a", UTC, US);
        final Date dK = fdpK.parse("0:45 PM");
        cal.setTime(dK);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));

        final FastDateParser fdpk = new FastDateParser("k:mm", UTC, US);
        final Date dk = fdpk.parse("24:00");
        cal.setTime(dk);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testDayOfYearAndWeekOfYear() throws ParseException {
        final FastDateParser fdpDayOfYear = new FastDateParser("yyyy-D", UTC, US);
        final Date d1 = fdpDayOfYear.parse("2023-365");
        Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(d1);
        assertEquals(365, cal.get(Calendar.DAY_OF_YEAR));

        final FastDateParser fdpWeek = new FastDateParser("yyyy-w-W-F", UTC, US);
        final Date d2 = fdpWeek.parse("2023-40-2-2");
        assertNotNull(d2);
    }

    @Test
    public void testTimeZoneParsing() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd HH:mm:ss z", UTC, US);
        final Date date = fdp.parse("2023-01-01 12:00:00 GMT+03:00");

        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(9, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testISO8601TimeZoneParsing() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd HH:mm:ss ZZ", UTC, US);
        final Date date = fdp.parse("2023-01-01 12:00:00 +0200");

        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testParseObject() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd", UTC, US);
        final Object parsedObj = fdp.parseObject("2023-08-20");
        assertTrue(parsedObj instanceof Date);

        final ParsePosition pos = new ParsePosition(0);
        final Object parsedPos = fdp.parseObject("2023-08-20", pos);
        assertTrue(parsedPos instanceof Date);
        assertEquals(10, pos.getIndex());

        final ParsePosition failPos = new ParsePosition(0);
        assertNull(fdp.parseObject("invalid", failPos));
        assertEquals(0, failPos.getErrorIndex());
    }

    @Test
    public void testGetters() {
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd", EST, GERMANY);
        assertEquals("yyyy-MM-dd", fdp.getPattern());
        assertEquals(EST, fdp.getTimeZone());
        assertEquals(GERMANY, fdp.getLocale());
        assertNotNull(fdp.getParsePattern());
        assertTrue(fdp.toString().contains("yyyy-MM-dd"));
    }

    @Test
    public void testEqualsAndHashCode() {
        final FastDateParser fdp1 = new FastDateParser("yyyy-MM-dd", UTC, US);
        final FastDateParser fdp2 = new FastDateParser("yyyy-MM-dd", UTC, US);
        final FastDateParser fdpDiffPattern = new FastDateParser("yyyy/MM/dd", UTC, US);
        final FastDateParser fdpDiffTz = new FastDateParser("yyyy-MM-dd", EST, US);
        final FastDateParser fdpDiffLocale = new FastDateParser("yyyy-MM-dd", UTC, GERMANY);

        assertEquals(fdp1, fdp1);
        assertEquals(fdp1, fdp2);
        assertEquals(fdp1.hashCode(), fdp2.hashCode());

        assertFalse(fdp1.equals(null));
        assertFalse(fdp1.equals("NotAFastDateParser"));
        assertFalse(fdp1.equals(fdpDiffPattern));
        assertFalse(fdp1.equals(fdpDiffTz));
        assertFalse(fdp1.equals(fdpDiffLocale));
    }

    @Test
    public void testSerialization() throws Exception {
        final FastDateParser fdpOriginal = new FastDateParser("yyyy-MM-dd HH:mm:ss", UTC, US);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fdpOriginal);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final FastDateParser fdpDeserialized = (FastDateParser) ois.readObject();
        ois.close();

        assertEquals(fdpOriginal, fdpDeserialized);
        final Date d1 = fdpOriginal.parse("2023-09-17 08:30:00");
        final Date d2 = fdpDeserialized.parse("2023-09-17 08:30:00");
        assertEquals(d1, d2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPatternThrowsIllegalArgumentException() {
        new FastDateParser("yyyy-MM-dd ?? Invalid Letter J", UTC, US);
    }

    @Test
    public void testDefaultConstructors() {
        final FastDateParser fdpDefault = new FastDateParser("yyyy-MM-dd");
        assertEquals(TimeZone.getDefault(), fdpDefault.getTimeZone());
        assertEquals(Locale.getDefault(), fdpDefault.getLocale());

        final FastDateParser fdpTzOnly = new FastDateParser("yyyy-MM-dd", EST);
        assertEquals(EST, fdpTzOnly.getTimeZone());
        assertEquals(Locale.getDefault(), fdpTzOnly.getLocale());

        final FastDateParser fdpLocOnly = new FastDateParser("yyyy-MM-dd", GERMANY);
        assertEquals(TimeZone.getDefault(), fdpLocOnly.getTimeZone());
        assertEquals(GERMANY, fdpLocOnly.getLocale());
    }

    @Test
    public void testAdjacentNumericFields() throws ParseException {
        final FastDateParser fdp = new FastDateParser("yyyyMMddHHmmss", UTC, US);
        final Date date = fdp.parse("20231128154530");

        final Calendar cal = Calendar.getInstance(UTC, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH));
        assertEquals(28, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(15, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
    }

    @Test
    public void testCustomTimeZoneDisplayName() throws ParseException {
        final SimpleTimeZone customTz = new SimpleTimeZone(3600000, "CustomTz");
        final FastDateParser fdp = new FastDateParser("yyyy-MM-dd z", customTz, US);
        final Date date = fdp.parse("2023-01-01 GMT+01:00");
        assertNotNull(date);
    }
}