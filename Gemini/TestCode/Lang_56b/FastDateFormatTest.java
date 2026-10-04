package org.apache.commons.lang.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;

/**
 * Unit tests for {@link org.apache.commons.lang.time.FastDateFormat}.
 */
public class FastDateFormatTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone CST = TimeZone.getTimeZone("CST");
    private static final TimeZone EST = TimeZone.getTimeZone("EST");

    /**
     * LANG-303: FastDateFormat does not serialize properly when padded number rules are present.
     */
    @Test
    public void testLang303() throws Exception {
        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss.SSS zzz", CST, Locale.US);
        FastDateFormat cloned = (FastDateFormat) SerializationUtils.clone(format);
        assertNotNull(cloned);
        assertEquals(format, cloned);
        
        Date date = new GregorianCalendar(2004, Calendar.DECEMBER, 31, 23, 59, 59).getTime();
        assertEquals(format.format(date), cloned.format(date));
    }

    @Test
    public void testSerializationAllPatterns() throws Exception {
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSZZ",
            "yyyy-MM-dd",
            "HH:mm:ss",
            "k:m:s a d/M/yy G",
            "K:h:m:s z Z EEE, d MMM yyyy",
            "w W D F",
            "''yyyy'' MM ''dd''"
        };
        for (String pattern : patterns) {
            FastDateFormat fdf = FastDateFormat.getInstance(pattern);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(fdf);
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            FastDateFormat deserialized = (FastDateFormat) ois.readObject();
            ois.close();

            assertEquals(fdf, deserialized);
            assertEquals(fdf.hashCode(), deserialized.hashCode());
            Date now = new Date();
            assertEquals(fdf.format(now), deserialized.format(now));
        }
    }

    @Test
    public void testGetInstance() {
        FastDateFormat format1 = FastDateFormat.getInstance();
        FastDateFormat format2 = FastDateFormat.getInstance();
        assertSame(format1, format2);

        FastDateFormat custom1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat custom2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertSame(custom1, custom2);

        FastDateFormat customTz1 = FastDateFormat.getInstance("yyyy-MM-dd", GMT);
        FastDateFormat customTz2 = FastDateFormat.getInstance("yyyy-MM-dd", GMT);
        assertSame(customTz1, customTz2);

        FastDateFormat customLoc1 = FastDateFormat.getInstance("yyyy-MM-dd", Locale.GERMANY);
        FastDateFormat customLoc2 = FastDateFormat.getInstance("yyyy-MM-dd", Locale.GERMANY);
        assertSame(customLoc1, customLoc2);
    }

    @Test
    public void testGetDateInstance() {
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        assertSame(fdf1, fdf2);

        FastDateFormat fdfTz = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, UTC);
        assertEquals(UTC, fdfTz.getTimeZone());

        FastDateFormat fdfLoc = FastDateFormat.getDateInstance(FastDateFormat.LONG, Locale.FRANCE);
        assertEquals(Locale.FRANCE, fdfLoc.getLocale());

        FastDateFormat fdfAll = FastDateFormat.getDateInstance(FastDateFormat.FULL, UTC, Locale.UK);
        assertEquals(UTC, fdfAll.getTimeZone());
        assertEquals(Locale.UK, fdfAll.getLocale());
    }

    @Test
    public void testGetTimeInstance() {
        FastDateFormat fdf1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        FastDateFormat fdf2 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertSame(fdf1, fdf2);

        FastDateFormat fdfTz = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, UTC);
        assertEquals(UTC, fdfTz.getTimeZone());

        FastDateFormat fdfLoc = FastDateFormat.getTimeInstance(FastDateFormat.LONG, Locale.FRANCE);
        assertEquals(Locale.FRANCE, fdfLoc.getLocale());

        FastDateFormat fdfAll = FastDateFormat.getTimeInstance(FastDateFormat.FULL, UTC, Locale.UK);
        assertEquals(UTC, fdfAll.getTimeZone());
        assertEquals(Locale.UK, fdfAll.getLocale());
    }

    @Test
    public void testGetDateTimeInstance() {
        FastDateFormat fdf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        FastDateFormat fdf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertSame(fdf1, fdf2);

        FastDateFormat fdfTz = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.LONG, UTC);
        assertEquals(UTC, fdfTz.getTimeZone());

        FastDateFormat fdfLoc = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.MEDIUM, Locale.FRANCE);
        assertEquals(Locale.FRANCE, fdfLoc.getLocale());

        FastDateFormat fdfAll = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, UTC, Locale.UK);
        assertEquals(UTC, fdfAll.getTimeZone());
        assertEquals(Locale.UK, fdfAll.getLocale());
    }

    @Test
    public void testFormatDateAndCalendar() {
        Calendar cal = new GregorianCalendar(2003, Calendar.JANUARY, 10, 15, 33, 20);
        cal.set(Calendar.MILLISECOND, 123);
        cal.setTimeZone(GMT);
        Date date = cal.getTime();

        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", GMT);
        assertEquals("2003-01-10 15:33:20.123", fdf.format(date));
        assertEquals("2003-01-10 15:33:20.123", fdf.format(cal));
        assertEquals("2003-01-10 15:33:20.123", fdf.format(date.getTime()));

        StringBuffer buf = new StringBuffer();
        fdf.format(date, buf);
        assertEquals("2003-01-10 15:33:20.123", buf.toString());

        buf = new StringBuffer();
        fdf.format(cal, buf);
        assertEquals("2003-01-10 15:33:20.123", buf.toString());

        buf = new StringBuffer();
        fdf.format(date.getTime(), buf);
        assertEquals("2003-01-10 15:33:20.123", buf.toString());
    }

    @Test
    public void testFormatObject() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Calendar cal = new GregorianCalendar(2020, Calendar.JULY, 15);
        Date date = cal.getTime();

        assertEquals("2020-07-15", fdf.format((Object) date));
        assertEquals("2020-07-15", fdf.format((Object) cal));
        assertEquals("2020-07-15", fdf.format((Object) Long.valueOf(date.getTime())));

        StringBuffer sb = new StringBuffer();
        FieldPosition fp = new FieldPosition(0);
        fdf.format(date, sb, fp);
        assertEquals("2020-07-15", sb.toString());

        try {
            fdf.format("Invalid Object");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            fdf.format((Object) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseObject() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        try {
            fdf.parseObject("2020-07-15");
            fail("parseObject(String) should throw UnsupportedOperationException");
        } catch (ParseException e) {
            fail("Unexpected ParseException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            fdf.parseObject("2020-07-15", new java.text.ParsePosition(0));
            fail("parseObject(String, ParsePosition) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testPatternSymbols() {
        Calendar cal = new GregorianCalendar(2004, Calendar.FEBRUARY, 9, 13, 5, 6);
        cal.set(Calendar.MILLISECOND, 7);
        cal.setTimeZone(GMT);
        Date date = cal.getTime();

        // Era: G, Year: y/yy/yyyy, Month: M/MM/MMM/MMMM, Day in month: d/dd
        // Hour in day: H/HH/k/kk/K/KK/h/hh, am/pm: a, Minute: m/mm, Second: s/ss, Millisecond: S/SSS
        // Day in year: D/DDD, Day of week in month: F, Week in year: w/ww, Week in month: W
        // Timezone: z/zzzz/Z/ZZ
        String pattern = "G y yy yyyy M MM MMM MMMM d dd H HH k kk K KK h hh a m mm s ss S SSS D DDD F w ww W z zzzz Z ZZ";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, GMT, Locale.US);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        sdf.setTimeZone(GMT);

        assertEquals(sdf.format(date), fdf.format(date));
    }

    @Test
    public void testTimeZoneFormatting() {
        Calendar cal = new GregorianCalendar(2004, Calendar.FEBRUARY, 9, 13, 5, 6);
        cal.setTimeZone(CST);
        Date date = cal.getTime();

        FastDateFormat fdfZ = FastDateFormat.getInstance("ZZ", CST, Locale.US);
        String formattedZ = fdfZ.format(date);
        assertTrue(formattedZ.equals("-06:00") || formattedZ.equals("-05:00"));

        FastDateFormat fdfRFC = FastDateFormat.getInstance("Z", CST, Locale.US);
        String formattedRFC = fdfRFC.format(date);
        assertTrue(formattedRFC.equals("-0600") || formattedRFC.equals("-0500"));

        FastDateFormat fdfZoneName = FastDateFormat.getInstance("z zzzz", CST, Locale.US);
        SimpleDateFormat sdfZoneName = new SimpleDateFormat("z zzzz", Locale.US);
        sdfZoneName.setTimeZone(CST);
        assertEquals(sdfZoneName.format(date), fdfZoneName.format(date));
    }

    @Test
    public void testQuotesAndEscapes() {
        FastDateFormat fdf = FastDateFormat.getInstance("''yyyy'' 'quoted' MM ''dd''", Locale.US);
        Calendar cal = new GregorianCalendar(2020, Calendar.DECEMBER, 25);
        assertEquals("'2020' quoted 12 '25'", fdf.format(cal));

        try {
            FastDateFormat.getInstance("yyyy 'unclosed quote", Locale.US);
            fail("Expected IllegalArgumentException for unclosed quote");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIllegalPatternSymbols() {
        try {
            FastDateFormat.getInstance("yyyy-MM-dd X");
            fail("Expected IllegalArgumentException for illegal character");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            FastDateFormat.getInstance(null);
            fail("Expected IllegalArgumentException for null pattern");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", GMT, Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", GMT, Locale.US);
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy-MM-dd", EST, Locale.US);
        FastDateFormat fdf4 = FastDateFormat.getInstance("yyyy-MM-dd", GMT, Locale.GERMANY);
        FastDateFormat fdf5 = FastDateFormat.getInstance("yyyy/MM/dd", GMT, Locale.US);

        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());
        assertFalse(fdf1.equals(fdf3));
        assertFalse(fdf1.equals(fdf4));
        assertFalse(fdf1.equals(fdf5));
        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("A String"));

        assertEquals(fdf1.getPattern(), "yyyy-MM-dd");
        assertEquals(fdf1.getTimeZone(), GMT);
        assertEquals(fdf1.getLocale(), Locale.US);
        assertFalse(fdf1.getTimeZoneOverridesCalendar());
        assertTrue(fdf1.getMaxLengthEstimate() > 0);
        assertNotNull(fdf1.toString());
    }

    @Test
    public void testPaddedNumbers() {
        Calendar cal = new GregorianCalendar(2004, Calendar.JANUARY, 1, 1, 1, 1);
        cal.set(Calendar.MILLISECOND, 1);
        cal.setTimeZone(GMT);

        FastDateFormat fdf = FastDateFormat.getInstance("yyyyy-MMMMM-ddddd HH:mm:ss.SSSSS", GMT, Locale.US);
        assertEquals("02004-00001-00001 01:01:01.00001", fdf.format(cal));
    }

    @Test
    public void testTwoDigitYear() {
        Calendar cal1 = new GregorianCalendar(1999, Calendar.JANUARY, 1);
        Calendar cal2 = new GregorianCalendar(2001, Calendar.JANUARY, 1);

        FastDateFormat fdf = FastDateFormat.getInstance("yy");
        assertEquals("99", fdf.format(cal1));
        assertEquals("01", fdf.format(cal2));
    }

    @Test
    public void testTimeZoneDisplayRules() {
        Calendar cal = new GregorianCalendar(2004, Calendar.JULY, 9, 13, 5, 6);
        cal.setTimeZone(CST);
        Date date = cal.getTime();

        FastDateFormat fdfShort = FastDateFormat.getInstance("z", CST, Locale.US);
        FastDateFormat fdfLong = FastDateFormat.getInstance("zzzz", CST, Locale.US);

        SimpleDateFormat sdfShort = new SimpleDateFormat("z", Locale.US);
        sdfShort.setTimeZone(CST);
        SimpleDateFormat sdfLong = new SimpleDateFormat("zzzz", Locale.US);
        sdfLong.setTimeZone(CST);

        assertEquals(sdfShort.format(date), fdfShort.format(date));
        assertEquals(sdfLong.format(date), fdfLong.format(date));
    }
}