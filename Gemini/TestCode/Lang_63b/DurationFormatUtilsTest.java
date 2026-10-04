package org.apache.commons.lang.time;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DurationFormatUtilsTest {

    @Test
    public void testConstructor() throws Exception {
        Constructor<DurationFormatUtils> constructor = DurationFormatUtils.class.getDeclaredConstructor();
        assertTrue(Modifier.isPublic(constructor.getModifiers()));
        DurationFormatUtils instance = new DurationFormatUtils();
        assertNotNull(instance);
    }

    @Test
    public void testFormatDurationHMS() {
        long time = 0;
        assertEquals("0:00:00.000", DurationFormatUtils.formatDurationHMS(time));

        time = (1000 * 60 * 60 * 2) + (1000 * 60 * 4) + (1000 * 8) + 12;
        assertEquals("2:04:08.012", DurationFormatUtils.formatDurationHMS(time));

        time = (1000 * 60 * 60 * 25) + (1000 * 60 * 59) + (1000 * 59) + 999;
        assertEquals("25:59:59.999", DurationFormatUtils.formatDurationHMS(time));
    }

    @Test
    public void testFormatDurationISO() {
        long time = 0;
        assertEquals("P0Y0M0DT0H0M0.000S", DurationFormatUtils.formatDurationISO(time));

        time = (1000L * 60 * 60 * 24 * 365 * 2) + (1000L * 60 * 60 * 24 * 30 * 3) +
                (1000L * 60 * 60 * 24 * 4) + (1000L * 60 * 60 * 5) +
                (1000L * 60 * 6) + (1000L * 7) + 8;
        assertNotNull(DurationFormatUtils.formatDurationISO(time));
    }

    @Test
    public void testFormatDurationWords() {
        String text = DurationFormatUtils.formatDurationWords(0, true, true);
        assertEquals("", text);

        text = DurationFormatUtils.formatDurationWords(0, false, false);
        assertEquals("0 days 0 hours 0 minutes 0 seconds", text);

        long duration = 1000L * 60 * 60 * 24; // 1 day
        assertEquals("1 day", DurationFormatUtils.formatDurationWords(duration, true, true));
        assertEquals("1 day 0 hours 0 minutes 0 seconds", DurationFormatUtils.formatDurationWords(duration, false, false));
        assertEquals("1 day", DurationFormatUtils.formatDurationWords(duration, true, false));

        duration = (1000L * 60 * 60 * 24 * 2) + (1000L * 60 * 60) + (1000L * 60 * 2) + (1000L * 3);
        assertEquals("2 days 1 hour 2 minutes 3 seconds", DurationFormatUtils.formatDurationWords(duration, true, true));

        duration = 1000L * 60; // 1 minute
        assertEquals("1 minute", DurationFormatUtils.formatDurationWords(duration, true, true));
        assertEquals("0 days 0 hours 1 minute 0 seconds", DurationFormatUtils.formatDurationWords(duration, false, false));

        duration = 1000L; // 1 second
        assertEquals("1 second", DurationFormatUtils.formatDurationWords(duration, true, true));

        duration = (1000L * 60 * 60 * 24) + 1000L;
        assertEquals("1 day 0 hours 0 minutes 1 second", DurationFormatUtils.formatDurationWords(duration, false, true));
    }

    @Test
    public void testFormatDurationCustom() {
        long duration = (1000L * 60 * 60 * 24 * 5) + (1000L * 60 * 60 * 4) + (1000L * 60 * 3) + (1000L * 2) + 1;
        assertEquals("05 04 03 02 001", DurationFormatUtils.formatDuration(duration, "dd HH mm ss SSS"));
        assertEquals("5 4 3 2 1", DurationFormatUtils.formatDuration(duration, "d H m s S", false));
        assertEquals("05 04 03 02 001", DurationFormatUtils.formatDuration(duration, "dd HH mm ss SSS", true));

        // Testing literal quotes in format
        assertEquals("5 days, 4 hours", DurationFormatUtils.formatDuration(duration, "d' days, 'H' hours'"));
        assertEquals("'5'", DurationFormatUtils.formatDuration(duration, "''d''"));
    }

    @Test
    public void testFormatPeriodISO() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        cal1.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2021, Calendar.FEBRUARY, 2, 1, 1, 1);
        cal2.set(Calendar.MILLISECOND, 1);

        String result = DurationFormatUtils.formatPeriodISO(cal1.getTimeInMillis(), cal2.getTimeInMillis());
        assertEquals("P1Y1M1DT1H1M1.001S", result);
    }

    @Test
    public void testFormatPeriod() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        cal1.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2020, Calendar.JANUARY, 15, 10, 20, 30);
        cal2.set(Calendar.MILLISECOND, 40);

        assertEquals("00 00 14 10 20 30 040", DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "yy MM dd HH mm ss SSS"));
        assertEquals("14 days", DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "d' days'"));
    }

    @Test
    public void testFormatPeriodDifferentTimeZones() {
        TimeZone tz = TimeZone.getTimeZone("GMT-5");
        Calendar cal1 = Calendar.getInstance(tz);
        Calendar cal2 = Calendar.getInstance(tz);

        cal1.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2020, Calendar.FEBRUARY, 1, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        String formatted = DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "MM dd", true, tz);
        assertEquals("01 00", formatted);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatPeriodNegativeDurationThrows() {
        DurationFormatUtils.formatPeriod(1000L, 0L, "d");
    }

    @Test
    public void testJiraLang281() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.YEAR, 2005);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.MONTH, Calendar.JANUARY);
        cal2.set(Calendar.DAY_OF_MONTH, 9);
        cal2.set(Calendar.YEAR, 2006);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        String res = DurationFormatUtils.formatPeriod(cal.getTimeInMillis(), cal2.getTimeInMillis(), "d");
        assertEquals("9", res);

        res = DurationFormatUtils.formatPeriod(cal.getTimeInMillis(), cal2.getTimeInMillis(), "dd");
        assertEquals("09", res);
    }

    @Test
    public void testPeriodMonthDaysAdjustment() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // From Jan 31 to Mar 1 in non-leap year (2019)
        cal1.set(2019, Calendar.JANUARY, 31, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2019, Calendar.MARCH, 1, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        assertEquals("01 01", DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "MM dd", false, TimeZone.getTimeZone("UTC")));

        // From Jan 31 to Mar 1 in leap year (2020)
        cal1.set(2020, Calendar.JANUARY, 31, 0, 0, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        cal2.set(2020, Calendar.MARCH, 1, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        assertEquals("01 01", DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "MM dd", false, TimeZone.getTimeZone("UTC")));
    }

    @Test
    public void testPeriodNegativeFieldsAdjustment() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        cal1.set(2020, Calendar.OCTOBER, 31, 23, 59, 59);
        cal1.set(Calendar.MILLISECOND, 999);
        cal2.set(2020, Calendar.NOVEMBER, 1, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        assertEquals("00 00 00 00 00 00 001", DurationFormatUtils.formatPeriod(cal1.getTimeInMillis(), cal2.getTimeInMillis(), "yy MM dd HH mm ss SSS", false, TimeZone.getTimeZone("UTC")));
    }

    @Test
    public void testTokenClassEqualsAndHashCode() {
        DurationFormatUtils.Token token1 = new DurationFormatUtils.Token("y", 1);
        DurationFormatUtils.Token token2 = new DurationFormatUtils.Token("y", 1);
        DurationFormatUtils.Token token3 = new DurationFormatUtils.Token("M", 1);
        DurationFormatUtils.Token token4 = new DurationFormatUtils.Token("y", 2);

        assertTrue(token1.equals(token1));
        assertTrue(token1.equals(token2));
        assertFalse(token1.equals(token3));
        assertFalse(token1.equals(token4));
        assertFalse(token1.equals("non-token object"));
        assertFalse(token1.equals(null));

        assertEquals(token1.hashCode(), token2.hashCode());
        assertNotNull(token1.toString());

        DurationFormatUtils.Token tokenSB = new DurationFormatUtils.Token(new StringBuffer("literal"), 1);
        DurationFormatUtils.Token tokenSB2 = new DurationFormatUtils.Token(new StringBuffer("literal"), 1);
        assertTrue(tokenSB.equals(tokenSB2));

        DurationFormatUtils.Token tokenNumber = new DurationFormatUtils.Token(new Integer(1), 1);
        DurationFormatUtils.Token tokenNumber2 = new DurationFormatUtils.Token(new Integer(1), 1);
        assertTrue(tokenNumber.equals(tokenNumber2));

        DurationFormatUtils.Token[] tokens = new DurationFormatUtils.Token[]{token1, token3};
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "y"));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, "M"));
        assertFalse(DurationFormatUtils.Token.containsTokenWithValue(tokens, "d"));
    }
}