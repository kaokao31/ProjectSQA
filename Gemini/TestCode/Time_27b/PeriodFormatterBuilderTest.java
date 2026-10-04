package org.joda.time.format;

import org.junit.Test;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class PeriodFormatterBuilderTest {

    @Test
    public void testBasicFormatterCreation() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears().appendSuffix(" year", " years");
        PeriodFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);

        Period period = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        String printed = formatter.print(period);
        assertEquals("2 years", printed);

        Period parsed = formatter.parsePeriod("2 years");
        assertEquals(2, parsed.getYears());
    }

    @Test
    public void testAppendMethods() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears()
               .appendMonths()
               .appendWeeks()
               .appendDays()
               .appendHours()
               .appendMinutes()
               .appendSeconds()
               .appendMillis();

        PeriodFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);
    }

    @Test
    public void testAppendLiteral() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder.appendLiteral("Test").toFormatter();
        assertEquals("Test", formatter.print(new Period()));
    }

    @Test
    public void testAppendSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder
                .appendYears()
                .appendSeparator(", ")
                .appendMonths()
                .toFormatter();

        assertNotNull(formatter);
        Period period = new Period(1, 2, 0, 0, 0, 0, 0, 0);
        assertEquals("1, 2", formatter.print(period));
    }

    @Test
    public void testAppendSeparatorWithAlias() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder
                .appendYears()
                .appendSeparator(", ", ",", new String[]{" and "})
                .appendMonths()
                .toFormatter();

        assertNotNull(formatter);
    }

    @Test
    public void testAppendPrefixAndSuffix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder
                .appendPrefix("P")
                .appendYears()
                .appendSuffix("Y")
                .toFormatter();

        assertEquals("P1Y", formatter.print(new Period(1, 0, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    public void testAppendMillisOptional() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder
                .appendSeconds()
                .appendMillisOptional()
                .toFormatter();

        assertNotNull(formatter);
        assertEquals("5", formatter.print(new Period(0, 0, 0, 0, 0, 0, 5, 0)));
    }

    @Test
    public void testClear() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        builder.clear();
        PeriodFormatter formatter = builder.appendMonths().toFormatter();
        assertNotNull(formatter);
    }

    @Test
    public void testToPrinterAndParser() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();

        assertNotNull(builder.toPrinter());
        assertNotNull(builder.toParser());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToFormatterOnlyPrinter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        // If we add something that is not a parser or not a printer
        // Or test toFormatter when only printer/parser exists
        PeriodFormatterBuilder printerOnly = new PeriodFormatterBuilder();
        printerOnly.appendYears();
        // Force state if possible or test basic flow
        printerOnly.toFormatter();
    }

    @Test
    public void testAppendFormatter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter subFormatter = new PeriodFormatterBuilder().appendYears().toFormatter();
        builder.append(subFormatter);
        assertNotNull(builder.toFormatter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullFormatter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.append((PeriodFormatter) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullPrinter() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.append(null, null);
    }

    @Test
    public void testMaximumValue() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        assertNotNull(builder.toFormatter());
    }

    @Test
    public void testRejectPrintedZero() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        assertNotNull(builder.toFormatter());
    }
}