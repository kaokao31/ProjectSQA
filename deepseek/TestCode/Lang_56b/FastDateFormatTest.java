package org.apache.commons.lang.time;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import static org.junit.Assert.*;

/**
 * Test suite for FastDateFormat, targeting bug #56 (serialization of PaddedNumberField).
 */
public class FastDateFormatTest {

    // --- Existing basic tests (to maintain regression coverage) ---

    @Test
    public void testBasicFormat() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Date date = new Date(0);
        assertEquals("1970-01-01", fdf.format(date));
    }

    @Test
    public void testFormatWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm:ss", tz);
        Date date = new Date(0);
        assertEquals("00:00:00", fdf.format(date));
    }

    @Test
    public void testFormatWithLocale() {
        Locale locale = Locale.GERMANY;
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT, locale);
        Date date = new Date(1000000000000L);
        String result = fdf.format(date);
        // Expected pattern for Germany: "09.09.01" (based on locale)
        assertEquals("09.09.01", result);
    }

    @Test
    public void testEmptyPattern() {
        FastDateFormat fdf = FastDateFormat.getInstance("");
        Date date = new Date();
        assertEquals("", fdf.format(date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern() {
        FastDateFormat.getInstance(null);
    }

    // --- Serialization tests (bug #56 related) ---

    @Test
    public void testLang303() throws Exception {
        // This test triggers serialization of a FastDateFormat instance that uses PaddedNumberField
        // (e.g., with two-digit month or day pattern)
        FastDateFormat original = FastDateFormat.getInstance("MM/dd/yy");
        Date date = new Date(946684800000L); // 2000-01-01
        String expectedFormat = original.format(date);

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        byte[] bytes = baos.toByteArray();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        // Verify that formatting produces the same result
        assertEquals(expectedFormat, deserialized.format(date));
    }

    @Test
    public void testSerializationWithLongPattern() throws Exception {
        FastDateFormat original = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date date = new Date(1234567890123L);
        String expected = original.format(date);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(expected, deserialized.format(date));
    }

    @Test
    public void testSerializationWithTimeZoneAndLocale() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("PST");
        Locale locale = Locale.US;
        FastDateFormat original = FastDateFormat.getInstance("EEEE, MMMM d, yyyy h:mm a", tz, locale);
        Date date = new Date(1609459200000L); // 2021-01-01 00:00 UTC
        String expected = original.format(date);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(expected, deserialized.format(date));
    }

    // --- Edge case tests ---

    @Test
    public void testFormatMilliseconds() {
        FastDateFormat fdf = FastDateFormat.getInstance("SSS");
        Date date = new Date(1234L);
        assertEquals("1234", fdf.format(date));
    }

    @Test
    public void testFormatTwelveHour() {
        FastDateFormat fdf = FastDateFormat.getInstance("hh:mm a");
        Date date = new Date(3600000L); // 01:00 GMT
        String result = fdf.format(date);
        assertEquals("01:00 AM", result);
    }

    @Test
    public void testFormatWithQuotedText() {
        FastDateFormat fdf = FastDateFormat.getInstance("'Today is' EEEE");
        Date date = new Date(0);
        assertEquals("Today is Thursday", fdf.format(date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPattern() {
        FastDateFormat.getInstance("xyz");
    }

    @Test
    public void testSameInstanceReused() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyyMMdd");
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyyMMdd");
        assertSame("Same pattern should return same instance", fdf1, fdf2);
    }

    @Test
    public void testDateInstanceShort() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.US);
        Date date = new Date(946684800000L); // 2000-01-01
        assertEquals("1/1/00", fdf.format(date));
    }

    @Test
    public void testDateTimeInstance() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US);
        Date date = new Date(946684800000L);
        String result = fdf.format(date);
        // Expected: "1/1/00 12:00 AM" (depending on timezone and time)
        assertNotNull(result);
        assertTrue(result.contains("1/1/00"));
    }

    @Test
    public void testEqualsAndHashCode() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());
    }

    @Test
    public void testToString() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        assertNotNull(fdf.toString());
        assertTrue(fdf.toString().contains("yyyy-MM-dd"));
    }

    @Test
    public void testBareInstance() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        assertNotNull(fdf);
    }

    @Test
    public void testFormatWithNullDate() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        assertEquals("", fdf.format((Date) null));
    }

    @Test
    public void testParse() throws ParseException {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Date parsed = fdf.parse("1970-01-01");
        assertNotNull(parsed);
        assertEquals(0, parsed.getTime());
    }

    @Test(expected = ParseException.class)
    public void testParseInvalid() throws ParseException {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.parse("invalid");
    }
}