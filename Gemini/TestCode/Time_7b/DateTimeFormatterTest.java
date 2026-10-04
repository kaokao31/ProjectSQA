package org.joda.time.format;

import org.junit.Test;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadWritableInstant;
import org.joda.time.ReadableInstant;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.BuddhistChronology;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateTimeFormatterTest {

    @Test
    public void testPrinterParserGetters() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNotNull(formatter.getPrinter());
        assertNotNull(formatter.getParser());
        assertTrue(formatter.isPrinter());
        assertTrue(formatter.isParser());
    }

    @Test
    public void testWithChronology() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        Chronology bmc = BuddhistChronology.getInstance();
        DateTimeFormatter withChrono = formatter.withChronology(bmc);
        assertEquals(bmc, withChrono.getChronology());
        assertSame(formatter, formatter.withChronology(null)); // implementation usually returns this if chrono is already null or same
    }

    @Test
    public void testWithLocale() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeFormatter withLocale = formatter.withLocale(Locale.FRANCE);
        assertEquals(Locale.FRANCE, withLocale.getLocale());
        assertSame(formatter, formatter.withLocale(null));
    }

    @Test
    public void testWithZone() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        DateTimeFormatter withZone = formatter.withZone(zone);
        assertEquals(zone, withZone.getZone());
        assertNull(formatter.withZone(null).getZone());
    }

    @Test
    public void testWithOffsetParsed() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertFalse(formatter.isOffsetParsed());
        assertTrue(formatter.withOffsetParsed().isOffsetParsed());
    }

    @Test
    public void testWithPivotYear() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yy-MM-dd");
        DateTimeFormatter withPivot = formatter.withPivotYear(2050);
        assertEquals(Integer.valueOf(2050), withPivot.getPivotYear());
        assertSame(formatter, formatter.withPivotYear(null));
    }

    @Test
    public void testWithDefaultYear() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd");
        DateTimeFormatter withDefYear = formatter.withDefaultYear(2020);
        assertEquals(2020, withDefYear.getDefaultYear());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintPrinterNull() {
        DateTimeFormatter formatter = new DateTimeFormatter(null, null);
        formatter.print(123456L);
    }

    @Test
    public void testPrintLong() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        String result = formatter.print(2023L);
        assertEquals("2023", result);
    }

    @Test
    public void testPrintReadableInstant() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        ReadableInstant instant = new DateTime(2023, 5, 10, 0, 0, ISOChronology.getInstance());
        String result = formatter.print(instant);
        assertEquals("2023", result);

        String nullResult = formatter.print((ReadableInstant) null);
        // Depending on implementation, null instant might print current time or throw/return default.
        // Let's verify it doesn't crash completely or check behavior.
        assertNotNull(nullResult);
    }

    @Test
    public void testPrintAppendable() throws IOException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        StringBuffer sb = new StringBuffer();
        formatter.print(sb, 2023L);
        assertEquals("2023", sb.toString());

        StringBuilder sbuild = new StringBuilder();
        formatter.print(sbuild, 2023L);
        assertEquals("2023", sbuild.toString());

        StringWriter out = new StringWriter();
        formatter.print(out, 2023L);
        assertEquals("2023", out.toString());
    }

    @Test
    public void testPrintAppendableReadableInstant() throws IOException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        ReadableInstant instant = new DateTime(2023, 5, 10, 0, 0);
        StringBuffer sb = new StringBuffer();
        formatter.print(sb, instant);
        assertEquals("2023", sb.toString());
    }

    @Test
    public void testPrintLocalDateLocalTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate date = new LocalDate(2023, 5, 10);
        LocalTime time = new LocalTime(12, 34, 56);
        String result = formatter.print(date);
        assertNotNull(result);

        String timeResult = formatter.print(time);
        assertNotNull(timeResult);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testParseParserNull() {
        DateTimeFormatter formatter = new DateTimeFormatter(null, null);
        formatter.parseMillis("2023");
    }

    @Test
    public void testParseMillis() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        long millis = formatter.parseMillis("2023");
        assertTrue(millis > 0L);
    }

    @Test
    public void testParseDateTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        DateTime dt = formatter.parseDateTime("2023");
        assertNotNull(dt);
        assertEquals(2023, dt.getYear());
    }

    @Test
    public void testParseLocalDateLocalTime() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate date = formatter.parseLocalDate("2023-05-10");
        assertEquals(2023, date.getYear());
        assertEquals(5, date.getMonthOfYear());
        assertEquals(10, date.getDayOfMonth());

        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
        LocalTime time = timeFormatter.parseLocalTime("12:34:56");
        assertEquals(12, time.getHourOfDay());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIntoNullInstant() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        formatter.parseInto(null, "2023", 0);
    }

    @Test
    public void testParseIntoReadWritableInstant() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        ReadWritableInstant instant = new DateTime(0L);
        int pos = formatter.parseInto(instant, "2023", 0);
        assertTrue(pos >= 0);
    }

    @Test
    public void testParseIntoReadWritableLocalDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        long instant = 0L;
        // Test parsing with position
        int newPos = formatter.parseInto(null, instant, "2023-05-10", 0);
        assertTrue(newPos > 0);
    }

    @Test
    public void testParseIntoInvalidText() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy");
        int pos = formatter.parseInto(null, 0L, "abcd", 0);
        assertTrue(pos < 0);
    }
}