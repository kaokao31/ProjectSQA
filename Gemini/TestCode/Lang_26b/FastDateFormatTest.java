package org.apache.commons.lang3.time;

import org.junit.Test;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FastDateFormatTest {

    @Test
    public void testLang645() {
        Locale locale = new Locale("sv", "SE");
        Calendar cal = Calendar.getInstance(locale);
        cal.set(2010, Calendar.JANUARY, 1, 12, 0, 0);
        Date d = cal.getTime();
        FastDateFormat fdf = FastDateFormat.getInstance("EEEE', week 'ww", locale);
        assertEquals("fredag, week 53", fdf.format(d));
    }

    @Test
    public void testGetInstance() {
        FastDateFormat format1 = FastDateFormat.getInstance();
        FastDateFormat format2 = FastDateFormat.getInstance();
        assertSame(format1, format2);

        FastDateFormat format3 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertNotNull(format3);
        assertEquals("yyyy-MM-dd", format3.getPattern());

        FastDateFormat format4 = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US);
        assertNotNull(format4);
        assertEquals(Locale.US, format4.getLocale());

        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat format5 = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        assertNotNull(format5);
        assertEquals(tz, format5.getTimeZone());

        FastDateFormat format6 = FastDateFormat.getInstance("yyyy-MM-dd", tz, Locale.US);
        assertNotNull(format6);
        assertEquals(tz, format6.getTimeZone());
        assertEquals(Locale.US, format6.getLocale());
    }

    @Test
    public void testGetDateInstance() {
        FastDateFormat f1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.US);
        FastDateFormat f3 = FastDateFormat.getDateInstance(FastDateFormat.LONG, TimeZone.getTimeZone("UTC"));
        FastDateFormat f4 = FastDateFormat.getDateInstance(FastDateFormat.FULL, TimeZone.getTimeZone("UTC"), Locale.GERMANY);

        assertNotNull(f1);
        assertNotNull(f2);
        assertNotNull(f3);
        assertNotNull(f4);

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateInstance(FastDateFormat.SHORT);
        assertEquals(sdf.toPattern(), f1.getPattern());
    }

    @Test
    public void testGetTimeInstance() {
        FastDateFormat f1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.US);
        FastDateFormat f3 = FastDateFormat.getTimeInstance(FastDateFormat.LONG, TimeZone.getTimeZone("UTC"));
        FastDateFormat f4 = FastDateFormat.getTimeInstance(FastDateFormat.FULL, TimeZone.getTimeZone("UTC"), Locale.GERMANY);

        assertNotNull(f1);
        assertNotNull(f2);
        assertNotNull(f3);
        assertNotNull(f4);

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertEquals(sdf.toPattern(), f1.getPattern());
    }

    @Test
    public void testGetDateTimeInstance() {
        FastDateFormat f1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US);
        FastDateFormat f3 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"));
        FastDateFormat f4 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"), Locale.GERMANY);

        assertNotNull(f1);
        assertNotNull(f2);
        assertNotNull(f3);
        assertNotNull(f4);

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertEquals(sdf.toPattern(), f1.getPattern());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPattern() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern() {
        FastDateFormat.getInstance(null);
    }

    @Test
    public void testFormatDate() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss.SSS", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.MARCH, 15, 13, 45, 30);
        cal.set(Calendar.MILLISECOND, 123);
        Date date = cal.getTime();

        assertEquals("2023/03/15 13:45:30.123", format.format(date));
        assertEquals("2023/03/15 13:45:30.123", format.format(cal));
        assertEquals("2023/03/15 13:45:30.123", format.format(date.getTime()));

        StringBuffer buf = new StringBuffer("Prefix: ");
        format.format(date, buf);
        assertEquals("Prefix: 2023/03/15 13:45:30.123", buf.toString());

        buf = new StringBuffer("Prefix: ");
        format.format(cal, buf);
        assertEquals("Prefix: 2023/03/15 13:45:30.123", buf.toString());

        buf = new StringBuffer("Prefix: ");
        format.format(date.getTime(), buf);
        assertEquals("Prefix: 2023/03/15 13:45:30.123", buf.toString());
    }

    @Test
    public void testFormatObject() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.set(2021, Calendar.DECEMBER, 25, 0, 0, 0);
        Date date = cal.getTime();

        StringBuffer sb1 = new StringBuffer();
        format.format((Object) date, sb1, new FieldPosition(0));
        assertEquals("2021-12-25", sb1.toString());

        StringBuffer sb2 = new StringBuffer();
        format.format((Object) cal, sb2, new FieldPosition(0));
        assertEquals("2021-12-25", sb2.toString());

        StringBuffer sb3 = new StringBuffer();
        format.format((Object) Long.valueOf(date.getTime()), sb3, new FieldPosition(0));
        assertEquals("2021-12-25", sb3.toString());

        try {
            format.format("not a date", new StringBuffer(), new FieldPosition(0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }

        try {
            format.format((Object) null, new StringBuffer(), new FieldPosition(0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVariousPatternTokens() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Locale locale = Locale.US;

        Calendar cal = Calendar.getInstance(tz, locale);
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        cal.set(Calendar.YEAR, 2004);
        cal.set(Calendar.MONTH, Calendar.JUNE);
        cal.set(Calendar.DAY_OF_MONTH, 9);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 20);
        cal.set(Calendar.SECOND, 30);
        cal.set(Calendar.MILLISECOND, 40);

        String[] patterns = new String[]{
                "G", "GG", "GGG", "GGGG",
                "y", "yy", "yyy", "yyyy",
                "M", "MM", "MMM", "MMMM",
                "d", "dd",
                "h", "hh", "H", "HH", "k", "kk", "K", "KK",
                "m", "mm",
                "s", "ss",
                "S", "SS", "SSS", "SSSS",
                "E", "EE", "EEE", "EEEE",
                "D", "DD", "DDD",
                "F",
                "w", "ww",
                "W",
                "a",
                "z", "zz", "zzz", "zzzz",
                "Z", "ZZ",
                "''", "'literal'", "''y''"
        };

        for (String pattern : patterns) {
            FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, locale);
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
            sdf.setTimeZone(tz);

            assertEquals("Pattern mismatch for [" + pattern + "]", sdf.format(cal.getTime()), fdf.format(cal));
            assertEquals("Pattern mismatch for Date [" + pattern + "]", sdf.format(cal.getTime()), fdf.format(cal.getTime()));
        }
    }

    @Test
    public void testTwoDigitYear() {
        FastDateFormat fdf = FastDateFormat.getInstance("yy", Locale.US);
        Calendar cal = Calendar.getInstance(Locale.US);

        cal.set(Calendar.YEAR, 2009);
        assertEquals("09", fdf.format(cal));

        cal.set(Calendar.YEAR, 1999);
        assertEquals("99", fdf.format(cal));

        cal.set(Calendar.YEAR, 2000);
        assertEquals("00", fdf.format(cal));
    }

    @Test
    public void testPaddingRules() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.set(2005, Calendar.JANUARY, 2, 3, 4, 5);
        cal.set(Calendar.MILLISECOND, 6);

        assertEquals("2005-01-02 03:04:05.006", fdf.format(cal));
    }

    @Test
    public void testTimeZoneDisplay() {
        SimpleTimeZone customTz = new SimpleTimeZone(
                3600000,
                "CustomZone",
                Calendar.APRIL, 1, 0, 0,
                Calendar.OCTOBER, -1, 0, 0,
                3600000
        );

        FastDateFormat fdf1 = FastDateFormat.getInstance("z zzzz Z ZZ", customTz, Locale.US);
        Calendar cal = Calendar.getInstance(customTz, Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        String result = fdf1.format(cal);
        assertNotNull(result);
        assertTrue(result.contains("+0100"));
        assertTrue(result.contains("+01:00"));

        cal.set(2023, Calendar.JULY, 1, 0, 0, 0);
        String daylightResult = fdf1.format(cal);
        assertNotNull(daylightResult);
        assertTrue(daylightResult.contains("+0200"));
        assertTrue(daylightResult.contains("+02:00"));
    }

    @Test
    public void testQuotedStrings() {
        FastDateFormat fdf = FastDateFormat.getInstance("'Date: 'yyyy-MM-dd' Time: 'HH:mm:ss", Locale.US);
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.set(2022, Calendar.DECEMBER, 31, 23, 59, 59);

        assertEquals("Date: 2022-12-31 Time: 23:59:59", fdf.format(cal));

        FastDateFormat fdf2 = FastDateFormat.getInstance("''yyyy''", Locale.US);
        assertEquals("'2022'", fdf2.format(cal));
    }

    @Test
    public void testUnclosedQuote() {
        try {
            FastDateFormat.getInstance("'unclosed quote");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testParseObject() throws ParseException {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US);
        try {
            fdf.parseObject("2020-01-01");
            fail("parseObject(String) is not supported and should throw ParseException");
        } catch (ParseException expected) {
        }

        ParsePosition pos = new ParsePosition(0);
        Object obj = fdf.parseObject("2020-01-01", pos);
        assertEquals(0, pos.getIndex());
        assertEquals(null, obj);
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdf4 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf5 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.GERMANY);

        assertEquals(fdf1, fdf1);
        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());

        assertFalse(fdf1.equals(fdf3));
        assertFalse(fdf1.equals(fdf4));
        assertFalse(fdf1.equals(fdf5));
        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("a string"));
    }

    @Test
    public void testToStringAndProperties() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("yyyy-MM-dd", fdf.getPattern());
        assertEquals(TimeZone.getTimeZone("UTC"), fdf.getTimeZone());
        assertEquals(Locale.US, fdf.getLocale());
        assertTrue(fdf.getMaxLengthEstimate() > 0);
        assertTrue(fdf.toString().contains("FastDateFormat[yyyy-MM-dd]"));
    }

    @Test
    public void testTimeZones() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss Z", TimeZone.getTimeZone("GMT-8"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"), Locale.US);
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        String formatted = fdf.format(cal);
        assertTrue(formatted.endsWith("-0800"));

        FastDateFormat fdfISO = FastDateFormat.getInstance("ZZ", TimeZone.getTimeZone("GMT-8"), Locale.US);
        assertEquals("-08:00", fdfISO.format(cal));

        FastDateFormat fdfISOZero = FastDateFormat.getInstance("ZZ", TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals("+00:00", fdfISOZero.format(cal));

        FastDateFormat fdfRFCZero = FastDateFormat.getInstance("Z", TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals("+0000", fdfRFCZero.format(cal));
    }
}