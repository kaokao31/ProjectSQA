package org.joda.time.format;

import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for DateTimeFormatter.
 * Targets high coverage and potential fault detection (including Defects4J bug 16).
 */
public class DateTimeFormatterTest {

    private DateTimeFormatter basicFormatter;
    private DateTimeFormatter fullFormatter;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;
    private DateTimeFormatter offsetFormatter;
    private DateTimeFormatter noOffsetFormatter;

    @Before
    public void setUp() {
        basicFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        fullFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
        dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
        offsetFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(DateTimeZone.forOffsetHours(-5));
        noOffsetFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZoneUTC();
    }

    // ==================== Basic Print Tests ====================

    @Test
    public void testPrint_DateTime() {
        DateTime dt = new DateTime(2000, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC);
        String result = basicFormatter.print(dt);
        assertEquals("2000-01-01T12:00:00.000+00:00", result);
    }

    @Test
    public void testPrint_MutableDateTime() {
        MutableDateTime mdt = new MutableDateTime(2000, 6, 15, 10, 30, 0, 0, DateTimeZone.forOffsetHours(2));
        String result = fullFormatter.print(mdt);
        assertEquals("2000-06-15T10:30:00.000+02:00", result);
    }

    @Test
    public void testPrint_ReadableInstant_Null() {
        try {
            basicFormatter.print((ReadableInstant) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrint_ReadablePartial() {
        LocalDate date = new LocalDate(2010, 12, 25);
        String result = dateFormatter.print(date);
        assertEquals("2010-12-25", result);
    }

    @Test
    public void testPrint_ReadablePartial_Null() {
        try {
            dateFormatter.print((ReadablePartial) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrint_Appendable() throws IOException {
        StringBuilder sb = new StringBuilder();
        DateTime dt = new DateTime(2015, 3, 8, 2, 0, 0, 0, DateTimeZone.forOffsetHours(-5));
        basicFormatter.printTo(sb, dt);
        assertEquals("2015-03-08T02:00:00.000-05:00", sb.toString());
    }

    @Test
    public void testPrint_StringBuffer() {
        StringBuffer sb = new StringBuffer();
        DateTime dt = new DateTime(2015, 3, 8, 2, 0, 0, 0, DateTimeZone.forOffsetHours(-5));
        basicFormatter.printTo(sb, dt);
        assertEquals("2015-03-08T02:00:00.000-05:00", sb.toString());
    }

    @Test
    public void testPrint_Writer() throws IOException {
        StringWriter sw = new StringWriter();
        DateTime dt = new DateTime(2015, 3, 8, 2, 0, 0, 0, DateTimeZone.forOffsetHours(-5));
        basicFormatter.printTo(sw, dt);
        assertEquals("2015-03-08T02:00:00.000-05:00", sw.toString());
    }

    // ==================== Basic Parse Tests ====================

    @Test
    public void testParseDateTime() {
        String input = "2012-06-30T23:59:59.999+00:00";
        DateTime result = basicFormatter.parseDateTime(input);
        assertEquals(new DateTime(2012, 6, 30, 23, 59, 59, 999, DateTimeZone.UTC), result);
    }

    @Test
    public void testParseDateTime_WithZone() {
        String input = "2012-06-30T23:59:59.999-05:00";
        DateTime result = basicFormatter.parseDateTime(input);
        // Expected: parsed with offset -05:00, then converted to UTC? Actually parseDateTime returns DateTime in the parsed zone.
        assertEquals(DateTimeZone.forOffsetHours(-5), result.getZone());
        assertEquals(1341100799999L, result.getMillis()); // 2012-06-30T23:59:59.999-05:00 = 2012-07-01T04:59:59.999Z
    }

    @Test
    public void testParseDateTime_NoOffset() {
        String input = "2012-06-30T23:59:59.999";
        DateTime result = noOffsetFormatter.parseDateTime(input);
        assertEquals(DateTimeZone.UTC, result.getZone());
        assertEquals(1341100799999L, result.getMillis());
    }

    @Test
    public void testParseDateTime_Null() {
        try {
            basicFormatter.parseDateTime(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseMutableDateTime() {
        String input = "2012-06-30T23:59:59.999+00:00";
        MutableDateTime result = basicFormatter.parseMutableDateTime(input);
        assertEquals(new DateTime(2012, 6, 30, 23, 59, 59, 999, DateTimeZone.UTC), result.toDateTime());
    }

    @Test
    public void testParseMutableDateTime_WithZone() {
        String input = "2012-06-30T23:59:59.999-05:00";
        MutableDateTime result = basicFormatter.parseMutableDateTime(input);
        assertEquals(DateTimeZone.forOffsetHours(-5), result.getZone());
        assertEquals(1341100799999L, result.getMillis());
    }

    @Test
    public void testParseMutableDateTime_Null() {
        try {
            basicFormatter.parseMutableDateTime(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseLocalDateTime() {
        String input = "2012-06-30T23:59:59.999";
        LocalDateTime result = noOffsetFormatter.parseLocalDateTime(input);
        assertEquals(new LocalDateTime(2012, 6, 30, 23, 59, 59, 999), result);
    }

    @Test
    public void testParseLocalDate() {
        String input = "2012-06-30";
        LocalDate result = dateFormatter.parseLocalDate(input);
        assertEquals(new LocalDate(2012, 6, 30), result);
    }

    @Test
    public void testParseLocalTime() {
        String input = "23:59:59";
        LocalTime result = timeFormatter.parseLocalTime(input);
        assertEquals(new LocalTime(23, 59, 59), result);
    }

    // ==================== ParseInto Tests ====================

    @Test
    public void testParseInto() {
        DateTimeParser parser = basicFormatter.getParser();
        DateTimeParserBucket bucket = new DateTimeParserBucket(0, null, null);
        String text = "2012-06-30T23:59:59.999+00:00";
        int result = basicFormatter.parseInto(bucket, text, 0);
        assertEquals(text.length(), result);
        assertEquals(1341100799999L, bucket.computeMillis(false));
    }

    @Test
    public void testParseInto_Partial() {
        DateTimeParser parser = dateFormatter.getParser();
        DateTimeParserBucket bucket = new DateTimeParserBucket(0, null, null);
        String text = "2012-06-30";
        int result = dateFormatter.parseInto(bucket, text, 0);
        assertEquals(text.length(), result);
        assertEquals(new LocalDate(2012, 6, 30).toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis(), bucket.computeMillis(false));
    }

    @Test
    public void testParseInto_NullText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0, null, null);
        try {
            basicFormatter.parseInto(bucket, null, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseInto_NullBucket() {
        try {
            basicFormatter.parseInto(null, "test", 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ==================== Zone/Chronology/Locale Tests ====================

    @Test
    public void testWithZone() {
        DateTimeFormatter withZone = basicFormatter.withZone(DateTimeZone.forID("America/New_York"));
        assertNotNull(withZone);
        assertEquals(DateTimeZone.forID("America/New_York"), withZone.getZone());
    }

    @Test
    public void testWithZone_Null() {
        DateTimeFormatter withZone = basicFormatter.withZone(null);
        assertNull(withZone.getZone());
    }

    @Test
    public void testWithChronology() {
        DateTimeFormatter withChrono = basicFormatter.withChronology(ISOChronology.getInstanceUTC());
        assertNotNull(withChrono);
        assertEquals(ISOChronology.getInstanceUTC(), withChrono.getChronology());
    }

    @Test
    public void testWithChronology_Null() {
        DateTimeFormatter withChrono = basicFormatter.withChronology(null);
        assertNull(withChrono.getChronology());
    }

    @Test
    public void testWithLocale() {
        DateTimeFormatter withLocale = basicFormatter.withLocale(Locale.GERMANY);
        assertNotNull(withLocale);
        assertEquals(Locale.GERMANY, withLocale.getLocale());
    }

    @Test
    public void testWithLocale_Null() {
        DateTimeFormatter withLocale = basicFormatter.withLocale(null);
        assertNull(withLocale.getLocale());
    }

    @Test
    public void testWithPivotYear() {
        DateTimeFormatter withPivot = basicFormatter.withPivotYear(2000);
        assertNotNull(withPivot);
        assertEquals(Integer.valueOf(2000), withPivot.getPivotYear());
    }

    @Test
    public void testWithPivotYear_Null() {
        DateTimeFormatter withPivot = basicFormatter.withPivotYear(null);
        assertNull(withPivot.getPivotYear());
    }

    // ==================== Printer/Parser Methods ====================

    @Test
    public void testIsPrinter() {
        assertTrue(basicFormatter.isPrinter());
        assertTrue(dateFormatter.isPrinter());
    }

    @Test
    public void testIsParser() {
        assertTrue(basicFormatter.isParser());
        assertTrue(dateFormatter.isParser());
    }

    @Test
    public void testGetPrinter() {
        assertNotNull(basicFormatter.getPrinter());
    }

    @Test
    public void testGetParser() {
        assertNotNull(basicFormatter.getParser());
    }

    // ==================== Edge Cases and Bug Triggers ====================

    @Test
    public void testParseDateTime_NegativeOffset() {
        // Defects4J bug 16: parsing with negative offset might produce wrong result
        String input = "2000-01-01T00:00:00.000-08:00";
        DateTime result = basicFormatter.parseDateTime(input);
        assertEquals(DateTimeZone.forOffsetHours(-8), result.getZone());
        // Expected millis: 2000-01-01T00:00:00.000-08:00 = 2000-01-01T08:00:00.000Z
        assertEquals(946684800000L, result.getMillis());
    }

    @Test
    public void testParseDateTime_PositiveOffset() {
        String input = "2000-01-01T00:00:00.000+05:30";
        DateTime result = basicFormatter.parseDateTime(input);
        assertEquals(DateTimeZone.forOffsetHoursMinutes(5, 30), result.getZone());
        // Expected millis: 2000-01-01T00:00:00.000+05:30 = 1999-12-31T18:30:00.000Z
        assertEquals(946665000000L, result.getMillis());
    }

    @Test
    public void testParseDateTime_ZeroOffset() {
        String input = "2000-01-01T00:00:00.000+00:00";
        DateTime result = basicFormatter.parseDateTime(input);
        assertEquals(DateTimeZone.UTC, result.getZone());
        assertEquals(946684800000L, result.getMillis());
    }

    @Test
    public void testParseDateTime_DSTTransition() {
        // Spring forward: March 8, 2015 at 2:00 AM EST -> EDT (America/New_York)
        DateTimeFormatter nyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .withZone(DateTimeZone.forID("America/New_York"));
        String input = "2015-03-08T02:00:00.000-05:00";
        DateTime result = nyFormatter.parseDateTime(input);
        // The parsed time is ambiguous; the formatter should use the offset given
        assertEquals(DateTimeZone.forOffsetHours(-5), result.getZone());
        assertEquals(1425794400000L, result.getMillis()); // 2015-03-08T07:00:00.000Z
    }

    @Test
    public void testParseDateTime_FallBack() {
        // Fall back: November 1, 2015 at 1:00 AM EDT -> EST (America/New_York)
        DateTimeFormatter nyFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .withZone(DateTimeZone.forID("America/New_York"));
        String input = "2015-11-01T01:00:00.000-04:00";
        DateTime result = nyFormatter.parseDateTime(input);
        assertEquals(DateTimeZone.forOffsetHours(-4), result.getZone());
        assertEquals(1446368400000L, result.getMillis()); // 2015-11-01T05:00:00.000Z
    }

    @Test
    public void testParseDateTime_WithZoneOverride() {
        // Formatter with fixed zone, but input includes offset
        DateTimeFormatter fixedZoneFormatter = basicFormatter.withZone(DateTimeZone.UTC);
        String input = "2000-01-01T00:00:00.000-05:00";
        DateTime result = fixedZoneFormatter.parseDateTime(input);
        // The offset in the string should be ignored? Actually withZone sets the zone to use for parsing,
        // but the offset in the string is still parsed and the result is in the formatter's zone.
        // The millis should be computed as if the string is in the given zone? Need to check behavior.
        // In Joda-Time, withZone sets the zone for the result, but the offset in the string is used to compute the millis.
        // So the result should be in UTC with millis corresponding to 2000-01-01T00:00:00.000-05:00 = 2000-01-01T05:00:00.000Z
        assertEquals(DateTimeZone.UTC, result.getZone());
        assertEquals(946702800000L, result.getMillis());
    }

    @Test
    public void testParseDateTime_InvalidFormat() {
        try {
            basicFormatter.parseDateTime("not a date");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseDateTime_EmptyString() {
        try {
            basicFormatter.parseDateTime("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrint_WithZone() {
        DateTime dt = new DateTime(2000, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC);
        String result = offsetFormatter.print(dt);
        // offsetFormatter has zone -05:00, so it will print in that zone
        assertEquals("2000-01-01T07:00:00.000-05:00", result);
    }

    @Test
    public void testPrint_WithChronology() {
        DateTimeFormatter chronoFormatter = basicFormatter.withChronology(ISOChronology.getInstanceUTC());
        DateTime dt = new DateTime(2000, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC);
        String result = chronoFormatter.print(dt);
        assertEquals("2000-01-01T12:00:00.000+00:00", result);
    }

    @Test
    public void testPrint_WithLocale() {
        // Locale affects printing of month names, but we use numeric pattern, so no change
        DateTimeFormatter localeFormatter = DateTimeFormat.forPattern("MMMM").withLocale(Locale.FRANCE);
        DateTime dt = new DateTime(2000, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC);
        String result = localeFormatter.print(dt);
        assertEquals("janvier", result);
    }

    @Test
    public void testPrint_Appendable_Null() {
        try {
            basicFormatter.printTo((Appendable) null, new DateTime());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrint_StringBuffer_Null() {
        try {
            basicFormatter.printTo((StringBuffer) null, new DateTime());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrint_Writer_Null() {
        try {
            basicFormatter.printTo((java.io.Writer) null, new DateTime());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrint_ReadablePartial_Appendable() throws IOException {
        StringBuilder sb = new StringBuilder();
        LocalDate date = new LocalDate(2010, 12, 25);
        dateFormatter.printTo(sb, date);
        assertEquals("2010-12-25", sb.toString());
    }

    @Test
    public void testPrint_ReadablePartial_StringBuffer() {
        StringBuffer sb = new StringBuffer();
        LocalDate date = new LocalDate(2010, 12, 25);
        dateFormatter.printTo(sb, date);
        assertEquals("2010-12-25", sb.toString());
    }

    @Test
    public void testPrint_ReadablePartial_Writer() throws IOException {
        StringWriter sw = new StringWriter();
        LocalDate date = new LocalDate(2010, 12, 25);
        dateFormatter.printTo(sw, date);
        assertEquals("2010-12-25", sw.toString());
    }

    @Test
    public void testParseLocalDateTime_WithZone() {
        // parseLocalDateTime should ignore zone and return LocalDateTime
        String input = "2012-06-30T23:59:59.999+00:00";
        LocalDateTime result = basicFormatter.parseLocalDateTime(input);
        assertEquals(new LocalDateTime(2012, 6, 30, 23, 59, 59, 999), result);
    }

    @Test
    public void testParseLocalDate_WithZone() {
        String input = "2012-06-30+00:00";
        LocalDate result = DateTimeFormat.forPattern("yyyy-MM-ddZ").parseLocalDate(input);
        assertEquals(new LocalDate(2012, 6, 30), result);
    }

    @Test
    public void testParseLocalTime_WithZone() {
        String input = "23:59:59+00:00";
        LocalTime result = DateTimeFormat.forPattern("HH:mm:ssZ").parseLocalTime(input);
        assertEquals(new LocalTime(23, 59, 59), result);
    }

    // ==================== Builder and Complex Patterns ====================

    @Test
    public void testBuilderFormats() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendYear(4, 4);
        builder.appendLiteral('-');
        builder.appendMonthOfYear(2);
        builder.appendLiteral('-');
        builder.appendDayOfMonth(2);
        DateTimeFormatter customFormatter = builder.toFormatter();
        String result = customFormatter.print(new LocalDate(2010, 6, 15));
        assertEquals("2010-06-15", result);
    }

    @Test
    public void testBuilderWithZone() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.append(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getPrinter(), 
                       DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").getParser());
        DateTimeFormatter customFormatter = builder.toFormatter().withZone(DateTimeZone.UTC);
        String input = "2000-01-01T00:00:00.000-05:00";
        DateTime result = customFormatter.parseDateTime(input);
        assertEquals(DateTimeZone.UTC, result.getZone());
        assertEquals(946702800000L, result.getMillis());
    }

    // ==================== Null Safety and Exception Tests ====================

    @Test(expected = IllegalArgumentException.class)
    public void testPrint_NullReadableInstant() {
        basicFormatter.print((ReadableInstant) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrint_NullReadablePartial() {
        dateFormatter.print((ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDateTime_NullString() {
        basicFormatter.parseDateTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMutableDateTime_NullString() {
        basicFormatter.parseMutableDateTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLocalDateTime_NullString() {
        noOffsetFormatter.parseLocalDateTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLocalDate_NullString() {
        dateFormatter.parseLocalDate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLocalTime_NullString() {
        timeFormatter.parseLocalTime(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInto_NullText() {
        DateTimeParserBucket bucket = new DateTimeParserBucket(0, null, null);
        basicFormatter.parseInto(bucket, null, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInto_NullBucket() {
        basicFormatter.parseInto(null, "test", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintTo_Appendable_Null() {
        basicFormatter.printTo((Appendable) null, new DateTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintTo_StringBuffer_Null() {
        basicFormatter.printTo((StringBuffer) null, new DateTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintTo_Writer_Null() {
        basicFormatter.printTo((java.io.Writer) null, new DateTime());
    }

    // ==================== Additional Edge Cases ====================

    @Test
    public void testParseDateTime_MaxDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        String input = "292278994-08-17"; // near max year
        try {
            formatter.parseDateTime(input);
            fail("Expected IllegalArgumentException for out-of-bounds year");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseDateTime_MinDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        String input = "-292275055-05-16"; // near min year
        try {
            formatter.parseDateTime(input);
            fail("Expected IllegalArgumentException for out-of-bounds year");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrint_WithPivotYear() {
        DateTimeFormatter pivotFormatter = DateTimeFormat.forPattern("yy-MM-dd").withPivotYear(2000);
        DateTime dt = new DateTime(1999, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
        String result = pivotFormatter.print(dt);
        assertEquals("99-01-01", result);
    }

    @Test
    public void testParseDateTime_WithPivotYear() {
        DateTimeFormatter pivotFormatter = DateTimeFormat.forPattern("yy-MM-dd").withPivotYear(2000);
        String input = "99-01-01";
        DateTime result = pivotFormatter.parseDateTime(input);
        assertEquals(1999, result.getYear());
    }

    @Test
    public void testParseDateTime_WithPivotYear_DefaultCentury() {
        DateTimeFormatter pivotFormatter = DateTimeFormat.forPattern("yy-MM-dd").withPivotYear(1950);
        String input = "49-01-01";
        DateTime result = pivotFormatter.parseDateTime(input);
        assertEquals(2049, result.getYear());
    }

    @Test
    public void testParseDateTime_WithPivotYear_DefaultCentury2() {
        DateTimeFormatter pivotFormatter = DateTimeFormat.forPattern("yy-MM-dd").withPivotYear(1950);
        String input = "50-01-01";
        DateTime result = pivotFormatter.parseDateTime(input);
        assertEquals(1950, result.getYear());
    }

    // ==================== Bug-Specific Tests (Defects4J Bug 16) ====================

    @Test
    public void testParseDateTime_Bug16_NegativeOffset() {
        // This test targets the specific bug: parsing with negative offset might produce wrong millis
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String input = "2004-06-09T10:20:30.000-08:00";
        DateTime result = formatter.parseDateTime(input);
        // Expected: 2004-06-09T10:20:30.000-08:00 = 2004-06-09T18:20:30.000Z
        assertEquals(1086790830000L, result.getMillis());
        assertEquals(DateTimeZone.forOffsetHours(-8), result.getZone());
    }

    @Test
    public void testParseDateTime_Bug16_PositiveOffset() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String input = "2004-06-09T10:20:30.000+05:30";
        DateTime result = formatter.parseDateTime(input);
        // Expected: 2004-06-09T10:20:30.000+05:30 = 2004-06-09T04:50:30.000Z
        assertEquals(1086763830000L, result.getMillis());
        assertEquals(DateTimeZone.forOffsetHoursMinutes(5, 30), result.getZone());
    }

    @Test
    public void testParseDateTime_Bug16_ZeroOffset() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String input = "2004-06-09T10:20:30.000+00:00";
        DateTime result = formatter.parseDateTime(input);
        assertEquals(1086786030000L, result.getMillis());
        assertEquals(DateTimeZone.UTC, result.getZone());
    }

    @Test
    public void testParseDateTime_Bug16_WithZoneOverride() {
        // Using withZone should still respect the offset in the string for millis calculation
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(DateTimeZone.UTC);
        String input = "2004-06-09T10:20:30.000-08:00";
        DateTime result = formatter.parseDateTime(input);
        // The result should be in UTC, but millis should be the same as if parsed with offset -08:00
        assertEquals(1086790830000L, result.getMillis());
        assertEquals(DateTimeZone.UTC, result.getZone());
    }

    @Test
    public void testParseMutableDateTime_Bug16() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String input = "2004-06-09T10:20:30.000-08:00";
        MutableDateTime result = formatter.parseMutableDateTime(input);
        assertEquals(1086790830000L, result.getMillis());
        assertEquals(DateTimeZone.forOffsetHours(-8), result.getZone());
    }

    @Test
    public void testParseInto_Bug16() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        DateTimeParserBucket bucket = new DateTimeParserBucket(0, null, null);
        String text = "2004-06-09T10:20:30.000-08:00";
        int parsedPos = formatter.parseInto(bucket, text, 0);
        assertEquals(text.length(), parsedPos);
        long millis = bucket.computeMillis(false);
        assertEquals(1086790830000L, millis);
    }

    @Test
    public void testParseInto_Bug16_WithZone() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(DateTimeZone.UTC);
        DateTimeParserBucket bucket = new DateTimeParserBucket(0, null, null);
        String text = "2004-06-09T10:20:30.000-08:00";
        int parsedPos = formatter.parseInto(bucket, text, 0);
        assertEquals(text.length(), parsedPos);
        long millis = bucket.computeMillis(false);
        // The bucket should have the offset from the string, not the formatter's zone
        assertEquals(1086790830000L, millis);
    }
}