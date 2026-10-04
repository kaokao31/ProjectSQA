package org.apache.commons.lang3.time;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FastDateFormatTest {

    @Test
    public void testLang538() {
        final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        FastDateFormat format = FastDateFormat.getInstance(pattern, TimeZone.getTimeZone("GMT"));

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"));
        cal.clear();
        cal.set(2009, Calendar.OCTOBER, 16, 8, 42, 16);
        cal.set(Calendar.MILLISECOND, 0);

        String formatted = format.format(cal);
        assertEquals("2009-10-16T16:42:16.000Z", formatted);
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

        FastDateFormat format5 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"));
        assertNotNull(format5);
        assertEquals(TimeZone.getTimeZone("UTC"), format5.getTimeZone());

        FastDateFormat format6 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotNull(format6);
        assertEquals(TimeZone.getTimeZone("UTC"), format6.getTimeZone());
        assertEquals(Locale.US, format6.getLocale());
    }

    @Test
    public void testGetDateInstance() {
        FastDateFormat f1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.US);
        FastDateFormat f3 = FastDateFormat.getDateInstance(FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"));
        FastDateFormat f4 = FastDateFormat.getDateInstance(FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"), Locale.US);

        assertNotNull(f1);
        assertNotNull(f2);
        assertNotNull(f3);
        assertNotNull(f4);
    }

    @Test
    public void testGetTimeInstance() {
        FastDateFormat f1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, Locale.US);
        FastDateFormat f3 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"));
        FastDateFormat f4 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"), Locale.US);

        assertNotNull(f1);
        assertNotNull(f2);
        assertNotNull(f3);
        assertNotNull(f4);
    }

    @Test
    public void testGetDateTimeInstance() {
        FastDateFormat f1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        FastDateFormat f2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US);
        FastDateFormat f3 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"));
        FastDateFormat f4 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, TimeZone.getTimeZone("UTC"), Locale.US);

        assertNotNull(f1);
        assertNotNull(f2);
        assertNotNull(f3);
        assertNotNull(f4);
    }

    @Test
    public void testFormatDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 15, 12, 30, 45);
        cal.set(Calendar.MILLISECOND, 500);
        Date date = cal.getTime();

        FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("2023-01-15 12:30:45.500", f.format(date));
        assertEquals("2023-01-15 12:30:45.500", f.format(date.getTime()));
    }

    @Test
    public void testFormatCalendar() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 15, 12, 30, 45);

        FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("2023-01-15 12:30:45", f.format(cal));

        StringBuffer buf = new StringBuffer("Result: ");
        StringBuffer returnedBuf = f.format(cal, buf);
        assertSame(buf, returnedBuf);
        assertEquals("Result: 2023-01-15 12:30:45", buf.toString());
    }

    @Test
    public void testFormatObject() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 15, 12, 30, 45);
        Date date = cal.getTime();

        FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"), Locale.US);

        assertEquals("2023-01-15 12:30:45", f.format((Object) date));
        assertEquals("2023-01-15 12:30:45", f.format((Object) cal));
        assertEquals("2023-01-15 12:30:45", f.format((Object) Long.valueOf(date.getTime())));

        StringBuffer sb = new StringBuffer();
        f.format((Object) date, sb, new FieldPosition(0));
        assertEquals("2023-01-15 12:30:45", sb.toString());

        try {
            f.format("Not a date");
            fail("Expected IllegalArgumentException for non-date object");
        } catch (IllegalArgumentException expected) {
            // Success
        }

        try {
            f.format((Object) null);
            fail("Expected IllegalArgumentException for null object");
        } catch (IllegalArgumentException expected) {
            // Success
        }
    }

    @Test
    public void testPatternTokens() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.DECEMBER, 31, 23, 59, 58);
        cal.set(Calendar.MILLISECOND, 999);

        String pattern = "G yyyy yy MMMM MMM MM M d dd D DDD EEEE EEE E a H HH k kk K KK h hh m mm s ss S SSS z zzzz Z '' 'quoted text'";
        FastDateFormat f = FastDateFormat.getInstance(pattern, TimeZone.getTimeZone("America/New_York"), Locale.US);
        String formatted = f.format(cal);
        assertNotNull(formatted);
        assertTrue(formatted.contains("AD"));
        assertTrue(formatted.contains("2023"));
        assertTrue(formatted.contains("23"));
        assertTrue(formatted.contains("December"));
        assertTrue(formatted.contains("Dec"));
        assertTrue(formatted.contains("12"));
        assertTrue(formatted.contains("31"));
        assertTrue(formatted.contains("PM"));
        assertTrue(formatted.contains("quoted text"));
        assertTrue(formatted.contains("'"));
    }

    @Test
    public void testTwoDigitYear() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2005, Calendar.JANUARY, 1);

        FastDateFormat f = FastDateFormat.getInstance("yy", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("05", f.format(cal));

        cal.set(Calendar.YEAR, 1999);
        assertEquals("99", f.format(cal));

        cal.set(Calendar.YEAR, 2000);
        assertEquals("00", f.format(cal));
    }

    @Test
    public void testTimeZones() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Calendar cal = Calendar.getInstance(tz, Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JULY, 4, 12, 0, 0);

        FastDateFormat f1 = FastDateFormat.getInstance("z", tz, Locale.US);
        FastDateFormat f2 = FastDateFormat.getInstance("zzzz", tz, Locale.US);
        FastDateFormat f3 = FastDateFormat.getInstance("Z", tz, Locale.US);

        assertEquals("EDT", f1.format(cal));
        assertEquals("Eastern Daylight Time", f2.format(cal));
        assertEquals("-0400", f3.format(cal));
    }

    @Test
    public void testInvalidPattern() {
        try {
            FastDateFormat.getInstance("yyyy-MM-dd X");
            fail("Expected IllegalArgumentException for illegal pattern character");
        } catch (IllegalArgumentException expected) {
            // Success
        }

        try {
            FastDateFormat.getInstance("yyyy-MM-dd 'unterminated quote");
            fail("Expected IllegalArgumentException for unterminated quote");
        } catch (IllegalArgumentException expected) {
            // Success
        }

        try {
            FastDateFormat.getInstance(null);
            fail("Expected IllegalArgumentException for null pattern");
        } catch (IllegalArgumentException expected) {
            // Success
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDateFormat f1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat f2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat f3 = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat f4 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat f5 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.GERMANY);

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());

        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Some String"));
        assertFalse(f1.equals(f3));
        assertFalse(f1.equals(f4));
        assertFalse(f1.equals(f5));
    }

    @Test
    public void testGetMaxLengthEstimate() {
        FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        assertTrue(f.getMaxLengthEstimate() > 0);
    }

    @Test
    public void testParseObjectUnsupported() {
        FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd");
        try {
            f.parseObject("2023-01-01", new ParsePosition(0));
            fail("Expected ParseException or UnsupportedOperationException");
        } catch (ParseException | UnsupportedOperationException expected) {
            // FastDateFormat parseObject throws ParseException in standard implementation
        }

        try {
            f.parseObject("2023-01-01");
            fail("Expected ParseException or UnsupportedOperationException");
        } catch (ParseException | UnsupportedOperationException expected) {
            // Success
        }
    }

    @Test
    public void testSerialization() throws Exception {
        FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"), Locale.US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(f);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(f, deserialized);
        assertEquals(f.getPattern(), deserialized.getPattern());
        assertEquals(f.getTimeZone(), deserialized.getTimeZone());
        assertEquals(f.getLocale(), deserialized.getLocale());
    }

    @Test
    public void testCustomTimeZoneRules() {
        SimpleTimeZone customTz = new SimpleTimeZone(3600000, "Custom");
        FastDateFormat format = FastDateFormat.getInstance("Z z", customTz, Locale.US);

        Calendar cal = Calendar.getInstance(customTz, Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);

        String result = format.format(cal);
        assertTrue(result.contains("+0100"));
    }

    @Test
    public void testToString() {
        FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        String str = f.toString();
        assertNotNull(str);
        assertTrue(str.contains("yyyy-MM-dd"));
        assertTrue(str.contains("UTC"));
        assertTrue(str.contains(Locale.US.toString()));
    }
}