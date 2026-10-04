package org.joda.time.format;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

public class DateTimeFormatterBuilderTest {

    private DateTimeFormatterBuilder builder;

    @Before
    public void setUp() {
        builder = new DateTimeFormatterBuilder();
    }

    @Test
    public void testAppendLiteral_String() {
        assertNotNull(builder.appendLiteral("test"));
        DateTimeFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);
        assertEquals("test", formatter.print(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteral_NullString() {
        builder.appendLiteral((String) null);
    }

    @Test
    public void testAppendLiteral_Char() {
        assertNotNull(builder.appendLiteral('c'));
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("c", formatter.print(0L));
    }

    @Test
    public void testAppend_Formatter() {
        DateTimeFormatter inner = DateTimeFormat.forPattern("yyyy");
        assertNotNull(builder.append(inner));
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("2023", formatter.print(new DateTime(2023, 1, 1, 0, 0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppend_NullFormatter() {
        builder.append((DateTimeFormatter) null);
    }

    @Test
    public void testAppend_Provider() {
        DateTimePrinter printer = DateTimeFormat.forPattern("yyyy").getPrinter();
        DateTimeParser parser = DateTimeFormat.forPattern("yyyy").getParser();
        assertNotNull(builder.append(printer, parser));
    }

    @Test
    public void testAppend_PrinterOnly() {
        DateTimePrinter printer = DateTimeFormat.forPattern("yyyy").getPrinter();
        assertNotNull(builder.append(printer, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppend_NeitherPrinterNorParser() {
        builder.append(null, null);
    }

    @Test
    public void testAppend_ProvidersArray() {
        DateTimePrinter printer = DateTimeFormat.forPattern("yyyy").getPrinter();
        DateTimeParser parser = DateTimeFormat.forPattern("yyyy").getParser();
        DateTimeParser[] parsers = new DateTimeParser[] { parser, parser };
        assertNotNull(builder.append(printer, parsers));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppend_ProvidersArrayNull() {
        builder.append(null, null);
    }

    @Test
    public void testAppendTimeZoneId() {
        assertNotNull(builder.appendTimeZoneId());
        DateTimeFormatter formatter = builder.toFormatter();
        assertEquals("UTC", formatter.withZone(DateTimeZone.UTC).print(0L));
    }

    @Test
    public void testAppendTimeZoneName() {
        assertNotNull(builder.appendTimeZoneName());
        assertNotNull(builder.appendTimeZoneName(null));
    }

    @Test
    public void testAppendTimeZoneShortName() {
        assertNotNull(builder.appendTimeZoneShortName());
        assertNotNull(builder.appendTimeZoneShortName(null));
    }

    @Test
    public void testAppendTimeZoneOffset() {
        assertNotNull(builder.appendTimeZoneOffset("Z", true, 2, 4));
        assertNotNull(builder.appendTimeZoneOffset("Z", false, 2, 2));
    }

    @Test
    public void testAppendMonthOfYearShortText() {
        assertNotNull(builder.appendMonthOfYearShortText());
    }

    @Test
    public void testAppendMonthOfYearText() {
        assertNotNull(builder.appendMonthOfYearText());
    }

    @Test
    public void testAppendMonthOfYear() {
        assertNotNull(builder.appendMonthOfYear(2));
    }

    @Test
    public void testAppendDayOfMonth() {
        assertNotNull(builder.appendDayOfMonth(2));
    }

    @Test
    public void testAppendDayOfWeekText() {
        assertNotNull(builder.appendDayOfWeekText());
    }

    @Test
    public void testAppendDayOfWeekShortText() {
        assertNotNull(builder.appendDayOfWeekShortText());
    }

    @Test
    public void testAppendDayOfWeek() {
        assertNotNull(builder.appendDayOfWeek(1));
    }

    @Test
    public void testAppendDayOfYear() {
        assertNotNull(builder.appendDayOfYear(3));
    }

    @Test
    public void testAppendWeekyear() {
        assertNotNull(builder.appendWeekyear(4, 4));
    }

    @Test
    public void testAppendWeekOfWeekyear() {
        assertNotNull(builder.appendWeekOfWeekyear(2));
    }

    @Test
    public void testAppendYear() {
        assertNotNull(builder.appendYear(4, 4));
    }

    @Test
    public void testAppendYearOfEra() {
        assertNotNull(builder.appendYearOfEra(4, 4));
    }

    @Test
    public void testAppendTwoDigitYear() {
        assertNotNull(builder.appendTwoDigitYear(2000));
        assertNotNull(builder.appendTwoDigitYear(2000, true));
    }

    @Test
    public void testAppendTwoDigitWeekyear() {
        assertNotNull(builder.appendTwoDigitWeekyear(2000));
        assertNotNull(builder.appendTwoDigitWeekyear(2000, true));
    }

    @Test
    public void testAppendCenturyOfEra() {
        assertNotNull(builder.appendCenturyOfEra(2, 2));
    }

    @Test
    public void testAppendEraText() {
        assertNotNull(builder.appendEraText());
    }

    @Test
    public void testAppendHalfdayOfDayText() {
        assertNotNull(builder.appendHalfdayOfDayText());
    }

    @Test
    public void testAppendHourOfDay() {
        assertNotNull(builder.appendHourOfDay(2));
    }

    @Test
    public void testAppendClockhourOfDay() {
        assertNotNull(builder.appendClockhourOfDay(2));
    }

    @Test
    public void testAppendHourOfHalfday() {
        assertNotNull(builder.appendHourOfHalfday(2));
    }

    @Test
    public void testAppendClockhourOfHalfday() {
        assertNotNull(builder.appendClockhourOfHalfday(2));
    }

    @Test
    public void testAppendMinuteOfDay() {
        assertNotNull(builder.appendMinuteOfDay(4));
    }

    @Test
    public void testAppendMinuteOfHour() {
        assertNotNull(builder.appendMinuteOfHour(2));
    }

    @Test
    public void testAppendSecondOfDay() {
        assertNotNull(builder.appendSecondOfDay(6));
    }

    @Test
    public void testAppendSecondOfMinute() {
        assertNotNull(builder.appendSecondOfMinute(2));
    }

    @Test
    public void testAppendMillisOfDay() {
        assertNotNull(builder.appendMillisOfDay(8));
    }

    @Test
    public void testAppendMillisOfSecond() {
        assertNotNull(builder.appendMillisOfSecond(3));
    }

    @Test
    public void testAppendDecimal() {
        assertNotNull(builder.appendDecimal(org.joda.time.DateTimeFieldType.yearOfEra(), 2, 4));
    }

    @Test
    public void testAppendFixedDecimal() {
        assertNotNull(builder.appendFixedDecimal(org.joda.time.DateTimeFieldType.yearOfEra(), 4));
    }

    @Test
    public void testAppendOptional() {
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormat.forPattern("yyyy"),
            DateTimeFormat.forPattern("yyyy-MM-dd")
        };
        assertNotNull(builder.appendOptional(formatters[0], formatters));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendOptional_Null() {
        builder.appendOptional(null, null);
    }

    @Test
    public void testAppend_Callback() {
        assertNotNull(builder.append((DateTimePrinter)null, null));
    }

    @Test
    public void testCanBuildFormatter() {
        builder.appendLiteral("A");
        assertTrue(builder.canBuildFormatter());
        assertTrue(builder.canBuildPrinter());
        assertTrue(builder.canBuildParser());
    }

    @Test
    public void testToFormatter() {
        builder.appendLiteral("Test");
        DateTimeFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);
    }

    @Test
    public void testToPrinterParser() {
        builder.appendLiteral("Test");
        assertNotNull(builder.toPrinter());
        assertNotNull(builder.toParser());
    }

    @Test
    public void testLocalizedBy() {
        assertNotNull(builder.toFormatter().withLocale(Locale.GERMAN));
    }
}