package org.joda.time.format;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for DateTimeFormatterBuilder.
 * Targets maximum coverage and fault detection (including Defects4J Time-20).
 */
public class DateTimeFormatterBuilderTest {

    private DateTimeZone utc;
    private Locale englishLocale;

    @Before
    public void setUp() {
        utc = DateTimeZone.UTC;
        englishLocale = Locale.ENGLISH;
    }

    // ---------------------------------------------------------------
    // Basic literal and number fields
    // ---------------------------------------------------------------

    @Test
    public void testAppendLiteral() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("test");
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("test", formatter.print(new DateTime(0L, utc)));
    }

    @Test
    public void testAppendLiteralEmptyString() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("");
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("", formatter.print(new DateTime(0L, utc)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteralNullString() {
        new DateTimeFormatterBuilder().appendLiteral((String) null);
    }

    @Test
    public void testAppendTwoDigitYear() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTwoDigitYear(2000, true);
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("14", formatter.print(new DateTime(2014, 1, 1, 0, 0, utc)));
    }

    @Test
    public void testAppendDayOfMonth() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDayOfMonth(1);
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("1", formatter.print(new DateTime(2014, 1, 1, 0, 0, utc)));
        assertEquals("31", formatter.print(new DateTime(2014, 1, 31, 0, 0, utc)));
    }

    @Test
    public void testAppendDayOfMonthZeroPadded() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDayOfMonth(2);
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("01", formatter.print(new DateTime(2014, 1, 1, 0, 0, utc)));
    }

    // ---------------------------------------------------------------
    // Text fields (short and long)
    // ---------------------------------------------------------------

    @Test
    public void testAppendMonthOfYearTextShort() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendMonthOfYearText();
        DateTimeFormatter formatter = builder.toFormatter().withLocale(englishLocale);
        assertEquals("January", formatter.print(new DateTime(2014, 1, 1, 0, 0, utc)));
    }

    @Test
    public void testAppendMonthOfYearTextShortWithLocale() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendMonthOfYearShortText();
        DateTimeFormatter formatter = builder.toFormatter().withLocale(englishLocale);
        assertEquals("Jan", formatter.print(new DateTime(2014, 1, 1, 0, 0, utc)));
    }

    @Test
    public void testAppendDayOfWeekText() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDayOfWeekText();
        DateTimeFormatter formatter = builder.toFormatter().withLocale(englishLocale);
        // 2014-01-01 was Wednesday
        assertEquals("Wednesday", formatter.print(new DateTime(2014, 1, 1, 0, 0, utc)));
    }

    // ---------------------------------------------------------------
    // Fraction of second (critical for bug Time-20)
    // ---------------------------------------------------------------

    @Test
    public void testAppendFractionOfSecondMin3Max3() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendSecondOfMinute(2);
        builder.appendLiteral('.');
        builder.appendFractionOfSecond(3, 3);
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("00.000", formatter.print(new DateTime(2014, 1, 1, 12, 0, 0, 0, utc)));
        assertEquals("00.500", formatter.print(new DateTime(2014, 1, 1, 12, 0, 0, 500, utc)));
    }

    @Test
    public void testAppendFractionOfSecondMin1Max3() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendSecondOfMinute(2);
        builder.appendLiteral('.');
        builder.appendFractionOfSecond(1, 3);
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("00.0", formatter.print(new DateTime(2014, 1, 1, 12, 0, 0, 0, utc)));
        assertEquals("00.5", formatter.print(new DateTime(2014, 1, 1, 12, 0, 0, 500, utc)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendFractionOfSecondInvalidMinDigits() {
        new DateTimeFormatterBuilder().appendFractionOfSecond(0, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendFractionOfSecondMinGreaterThanMax() {
        new DateTimeFormatterBuilder().appendFractionOfSecond(4, 3);
    }

    // ---------------------------------------------------------------
    // Print ReadablePartial with fraction (triggers Defects4J Time-20)
    // ---------------------------------------------------------------

    @Test
    public void testPrintReadablePartialWithFractionOfSecond() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendHourOfDay(2);
        builder.appendLiteral(':');
        builder.appendMinuteOfHour(2);
        builder.appendLiteral(':');
        builder.appendSecondOfMinute(2);
        builder.appendLiteral('.');
        builder.appendFractionOfSecond(3, 3);
        DateTimeFormatter formatter = builder.toFormatter();
        LocalTime time = new LocalTime(12, 0, 0, 0);
        // This should not throw; prior to fix (Time-20) it would
        String result = formatter.print((ReadablePartial) time);
        assertEquals("12:00:00.000", result);
    }

    @Test
    public void testPrintReadablePartialWithFractionNonZero() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendHourOfDay(2);
        builder.appendLiteral(':');
        builder.appendMinuteOfHour(2);
        builder.appendLiteral(':');
        builder.appendSecondOfMinute(2);
        builder.appendLiteral('.');
        builder.appendFractionOfSecond(3, 3);
        DateTimeFormatter formatter = builder.toFormatter();
        LocalTime time = new LocalTime(12, 0, 0, 500);
        String result = formatter.print((ReadablePartial) time);
        assertEquals("12:00:00.500", result);
    }

    @Test
    public void testPrintReadablePartialWithFractionMin1Max3() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendSecondOfMinute(2);
        builder.appendLiteral('.');
        builder.appendFractionOfSecond(1, 3);
        DateTimeFormatter formatter = builder.toFormatter();
        LocalTime time = new LocalTime(12, 0, 0, 0);
        String result = formatter.print((ReadablePartial) time);
        assertEquals("00.0", result);
    }

    // ---------------------------------------------------------------
    // Time zone fields
    // ---------------------------------------------------------------

    @Test
    public void testAppendTimeZoneId() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneId();
        DateTimeFormatter formatter = builder.toFormatter();
        // Use a known zone
        String result = formatter.print(new DateTime(2014, 1, 1, 0, 0, DateTimeZone.forID("Europe/Paris")));
        assertTrue(result.startsWith("Europe/Paris") || result.equals("+01:00") || result.equals("CET"));
    }

    @Test
    public void testAppendTimeZoneOffsetShort() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendTimeZoneOffset(null, false, 2, 2);
        DateTimeFormatter formatter = builder.toFormatter();
        DateTime dt = new DateTime(2014, 1, 1, 0, 0, DateTimeZone.forOffsetHours(1));
        assertEquals("+01:00", formatter.print(dt));
    }

    // ---------------------------------------------------------------
    // Combined pattern building (multiple fields)
    // ---------------------------------------------------------------

    @Test
    public void testComplexDateTimePrint() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendDayOfMonth(2);
        builder.appendLiteral('-');
        builder.appendMonthOfYearShortText();
        builder.appendLiteral('-');
        builder.appendYear(4, 4);
        DateTimeFormatter formatter = builder.toFormatter().withLocale(englishLocale);
        assertEquals("01-Jan-2014", formatter.print(new DateTime(2014, 1, 1, 0, 0, utc)));
    }

    @Test
    public void testPrintMillisWithFraction() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendHalfdayOfDayText();
        builder.appendLiteral(' ');
        builder.appendClockhourOfHalfday(2);
        builder.appendLiteral(':');
        builder.appendMinuteOfHour(2);
        builder.appendLiteral(':');
        builder.appendSecondOfMinute(2);
        builder.appendLiteral('.');
        builder.appendFractionOfSecond(3, 3);
        DateTimeFormatter formatter = builder.toFormatter().withLocale(englishLocale);
        String result = formatter.print(new DateTime(2014, 1, 1, 14, 5, 7, 250, utc));
        // Expected: "PM 02:05:07.250" (locale may vary, but we can check pattern)
        assertTrue(result.contains("02:05:07.250") || result.contains("02:05:07.250"));
    }

    // ---------------------------------------------------------------
    // Parsing (basic)
    // ---------------------------------------------------------------

    @Test
    public void testParseBasicDateTime() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendYear(4, 4);
        builder.appendLiteral('-');
        builder.appendMonthOfYear(2);
        builder.appendLiteral('-');
        builder.appendDayOfMonth(2);
        builder.appendLiteral(' ');
        builder.appendHourOfDay(2);
        builder.appendLiteral(':');
        builder.appendMinuteOfHour(2);
        builder.appendLiteral(':');
        builder.appendSecondOfMinute(2);
        DateTimeFormatter formatter = builder.toFormatter();
        DateTime result = formatter.parseDateTime("2014-01-01 12:00:00");
        assertEquals(2014, result.getYear());
        assertEquals(1, result.getMonthOfYear());
        assertEquals(1, result.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInvalidInput() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendYear(4, 4);
        DateTimeFormatter formatter = builder.toFormatter();
        formatter.parseDateTime("abc");
    }

    // ---------------------------------------------------------------
    // toPrinter and toParser
    // ---------------------------------------------------------------

    @Test
    public void testToPrinter() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendLiteral("test");
        DateTimePrinter printer = builder.toPrinter();
        assertNotNull(printer);
        StringBuffer buf = new StringBuffer();
        printer.printTo(buf, new DateTime(0L, utc), utc);
        assertEquals("test", buf.toString());
    }

    @Test
    public void testToParser() {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        builder.appendYear(4, 4);
        DateTimeParser parser = builder.toParser();
        assertNotNull(parser);
    }

    // ---------------------------------------------------------------
    // Edge cases and null/empty handling
    // ---------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullPrinter() {
        new DateTimeFormatterBuilder().append((DateTimePrinter) null, (DateTimeParser) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullParser() {
        new DateTimeFormatterBuilder().append((DateTimePrinter) null, (DateTimeParser) null);
    }

    @Test
    public void testAppendFormatterThenClear() {
        DateTimeFormatterBuilder innerBuilder = new DateTimeFormatterBuilder();
        innerBuilder.appendLiteral("inner");
        DateTimeFormatter inner = innerBuilder.toFormatter();
        DateTimeFormatterBuilder outerBuilder = new DateTimeFormatterBuilder();
        outerBuilder.append(inner.getPrinter(), inner.getParser());
        // Append something else to ensure mixing works
        outerBuilder.appendLiteral("outer");
        DateTimeFormatter formatter = outerBuilder.toFormatter();
        assertEquals("innerouter", formatter.print(new DateTime(0L, utc)));
    }
}