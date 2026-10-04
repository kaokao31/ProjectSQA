package org.apache.commons.lang.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TimeZone;

import org.junit.Test;

public class FastDateFormatTest {

    @Test
    public void testGetInstance() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance();
        assertSame(fdf1, fdf2);
    }

    @Test
    public void testGetInstanceWithPattern() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        assertNotNull(fdf);
        assertEquals("yyyy-MM-dd", fdf.getPattern());
    }

    @Test
    public void testGetInstanceWithPatternAndTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        assertNotNull(fdf);
        assertEquals(tz, fdf.getTimeZone());
    }

    @Test
    public void testGetInstanceWithPatternAndLocale() {
        Locale locale = Locale.US;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", locale);
        assertNotNull(fdf);
        assertEquals(locale, fdf.getLocale());
    }

    @Test
    public void testGetInstanceFull() {
        TimeZone tz = TimeZone.getTimeZone("EST");
        Locale locale = Locale.US;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", tz, locale);
        assertNotNull(fdf);
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(locale, fdf.getLocale());
    }

    @Test
    public void testGetDateInstance() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        assertNotNull(fdf);
    }

    @Test
    public void testGetTimeInstance() {
        FastDateFormat fdf = FastDateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        assertNotNull(fdf);
    }

    @Test
    public void testGetDateTimeInstance() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
        assertNotNull(fdf);
    }

    @Test
    public void testFormatMethods() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2008, Calendar.JANUARY, 1, 0, 0, 0);

        assertEquals("2008-01-01", fdf.format(cal));
        assertEquals("2008-01-01", fdf.format(cal.getTime()));
        assertEquals("2008-01-01", fdf.format(cal.getTimeInMillis()));

        StringBuffer sb = new StringBuffer();
        assertEquals(sb, fdf.format(cal, sb));
        assertEquals("2008-01-01", sb.toString());
    }

    @Test
    public void testParseMethods() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Date date = fdf.parse("2008-01-01", new ParsePosition(0));
        assertNotNull(date);

        Calendar cal = Calendar.getInstance();
        fdf.parse("2008-01-01", new ParsePosition(0), cal);
        assertNotNull(cal);
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy-MM");

        assertTrue(fdf1.equals(fdf2));
        assertTrue(fdf1.hashCode() == fdf2.hashCode());
        assertTrue(!fdf1.equals(fdf3));
        assertTrue(!fdf1.equals(null));
        assertTrue(!fdf1.equals("NotAFastDateFormat"));
    }

    @Test
    public void testToString() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        assertNotNull(fdf.toString());
    }

    @Test
    public void test_changeDefault_Locale_DateInstance() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateInstance(FastDateFormat.LONG, Locale.US);
            
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateInstance(FastDateFormat.LONG, Locale.US);
            
            assertEquals(format1.getLocale(), format2.getLocale());
            assertSame(format1, format2);
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void test_changeDefault_Locale_DateTimeInstance() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.LONG, Locale.US);
            
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.LONG, Locale.US);
            
            assertEquals(format1.getLocale(), format2.getLocale());
            assertSame(format1, format2);
        } finally {
            Locale.setDefault(originalLocale);
        }
    }
}