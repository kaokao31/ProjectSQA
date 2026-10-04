package org.joda.time.format;

import org.junit.Test;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadWritableInstant;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.BuddhistChronology;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import static org.junit.Assert.*;

public class DateTimeFormatterTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintToWriterUnsupported() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.print((java.io.Writer) null, 123456789L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintToAppendableUnsupported() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.print((Appendable) null, 123456789L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintReadableInstantWriterUnsupported() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.print((java.io.Writer) null, new DateTime());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintReadableInstantAppendableUnsupported() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        formatter.print((Appendable) null, new DateTime());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintMillisPrinterNull() {
        DateTimeFormatter formatter = new DateTimeFormatter(null, null);
        formatter.print(new StringBuffer(), 0L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintReadableInstantPrinterNull() {
        DateTimeFormatter formatter = new DateTimeFormatter(null, null);
        formatter.print(new StringBuffer(), new DateTime());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrintPartialPrinterNull() {
        DateTimeFormatter formatter = new DateTimeFormatter(null, null);
        formatter.print(new StringBuffer(), new LocalDate());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testParseMillisNull() {
        DateTimeFormatter formatter = new DateTimeFormatter(null, null);
        formatter.parseInto(new MutableDateTime(), "2020-01-01", 0);
    }

    @Test
    public void testBasicGettersAndSetters() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNotNull(formatter.print(0L));
        
        DateTimeFormatter withZone = formatter.withZone(DateTimeZone.UTC);
        assertNotNull(withZone);
        assertEquals(DateTimeZone.UTC, withZone.getZone());

        DateTimeFormatter withChronology = formatter.withChronology(ISOChronology.getInstance());
        assertNotNull(withChronology);
        assertEquals(ISOChronology.getInstance(), withChronology.getChronology());

        DateTimeFormatter withLocale = formatter.withLocale(Locale.US);
        assertNotNull(withLocale);
        assertEquals(Locale.US, withLocale.getLocale());

        DateTimeFormatter withPivotYear = formatter.withPivotYear(2050);
        assertNotNull(withPivotYear);
        assertEquals(Integer.valueOf(2050), withPivotYear.getPivotYear());

        DateTimeFormatter withOffsetParsed = formatter.withOffsetParsed();
        assertNotNull(withOffsetParsed);
        assertTrue(withOffsetParsed.isOffsetParsed());
    }

    @Test
    public void testWithDefaultYear() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM-dd");
        DateTimeFormatter withDefaultYear = formatter.withDefaultYear(2020);
        assertNotNull(withDefaultYear);
        assertEquals(Integer.valueOf(2020), withDefaultYear.getDefaultYear());
    }

    @Test
    public void testPrintVariousTypes() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        
        long millis = 1577836800000L; // 2020-01-01 00:00:00 UTC
        assertEquals("2020-01-01 00:00:00", formatter.withZone(DateTimeZone.UTC).print(millis));
        
        DateTime dt = new DateTime(millis, DateTimeZone.UTC);
        assertEquals("2020-01-01 00:00:00", formatter.withZone(DateTimeZone.UTC).print(dt));
        
        MutableDateTime mdt = new MutableDateTime(millis, DateTimeZone.UTC);
        assertEquals("2020-01-01 00:00:00", formatter.withZone(DateTimeZone.UTC).print(mdt));
        
        LocalDate ld = new LocalDate(2020, 1, 1);
        DateTimeFormatter dateOnly = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertEquals("2020-01-01", dateOnly.print(ld));

        LocalTime lt = new LocalTime(12, 34, 56);
        DateTimeFormatter timeOnly = DateTimeFormat.forPattern("HH:mm:ss");
        assertEquals("12:34:56", timeOnly.print(lt));
    }

    @Test
    public void testPrintToAppendableAndWriter() throws IOException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        
        StringBuilder sb = new StringBuilder();
        formatter.printTo(sb, 1577836800000L);
        assertEquals("2020-01-01", sb.toString());

        StringWriter sw = new StringWriter();
        formatter.printTo(sw, 1577836800000L);
        assertEquals("2020-01-01", sw.toString());

        StringBuilder sbInstant = new StringBuilder();
        formatter.printTo(sbInstant, new DateTime(1577836800000L, DateTimeZone.UTC));
        assertEquals("2020-01-01", sbInstant.toString());

        StringWriter swInstant = new StringWriter();
        formatter.printTo(swInstant, new DateTime(1577836800000L, DateTimeZone.UTC));
        assertEquals("2020-01-01", swInstant.toString());

        StringBuilder sbPartial = new StringBuilder();
        formatter.printTo(sbPartial, new LocalDate(2020, 1, 1));
        assertEquals("2020-01-01", sbPartial.toString());

        StringWriter swPartial = new StringWriter();
        formatter.printTo(swPartial, new LocalDate(2020, 1, 1));
        assertEquals("2020-01-01", swPartial.toString());
    }

    @Test
    public void testParseMethods() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.UTC);
        
        long parsedMillis = formatter.parseMillis("2020-01-01");
        assertEquals(1577836800000L, parsedMillis);

        DateTime parsedDt = formatter.parseDateTime("2020-01-01");
        assertEquals(new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC), parsedDt);

        MutableDateTime parsedMdt = formatter.parseMutableDateTime("2020-01-01");
        assertEquals(new MutableDateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC), parsedMdt);

        LocalDate parsedLd = formatter.parseLocalDate("2020-01-01");
        assertEquals(new LocalDate(2020, 1, 1), parsedLd);

        LocalDateTime parsedLdt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseLocalDateTime("2020-01-01 12:34:56");
        assertEquals(new LocalDateTime(2020, 1, 1, 12, 34, 56), parsedLdt);

        LocalTime parsedLt = DateTimeFormat.forPattern("HH:mm:ss").parseLocalTime("12:34:56");
        assertEquals(new LocalTime(12, 34, 56), parsedLt);
    }

    @Test
    public void testParseIntoAndToFormatter() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertNotNull(formatter.getPrinter());
        assertNotNull(formatter.getParser());
        assertTrue(formatter.isPrinter());
        assertTrue(formatter.isParser());

        MutableDateTime mdt = new MutableDateTime(0L);
        int newPos = formatter.parseInto(mdt, "2020-01-01", 0);
        assertTrue(newPos > 0);
    }
}