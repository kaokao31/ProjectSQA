package org.joda.time.format;

import org.junit.Test;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class PeriodFormatterBuilderTest {

    @Test
    public void testToFormatterEmpty() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);
        
        Period period = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        String printed = formatter.print(period);
        assertNotNull(printed);
    }

    @Test
    public void testAppendLiteral() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral("test");
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("test", formatter.print(period));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendLiteralNull() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendLiteral(null);
    }

    @Test
    public void testAppendSeparator() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        builder.appendSeparator("-");
        builder.appendMonths();
        
        PeriodFormatter formatter = builder.toFormatter();
        Period period = new Period(2, 3, 0, 0, 0, 0, 0, 0);
        assertEquals("2-3", formatter.print(period));
        
        // Test separator not printed when zero/omitted
        Period periodYearsOnly = new Period(2, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("2", formatter.print(periodYearsOnly));
    }

    @Test
    public void testAppendSeparatorWithTexts() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        builder.appendSeparator(",", ",", new String[]{","});
        builder.appendMonths();
        
        PeriodFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);
    }

    @Test
    public void testAppendFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter formatter = builder
            .appendYears()
            .appendSuffix(" year", " years")
            .appendSeparator(" ")
            .appendMonths()
            .appendSuffix(" month", " months")
            .appendSeparator(" ")
            .appendDays()
            .appendSuffix(" day", " days")
            .toFormatter();

        Period period = new Period(1, 2, 0, 3, 0, 0, 0, 0);
        String result = formatter.print(period);
        assertTrue(result.contains("1 year"));
        assertTrue(result.contains("2 months"));
        assertTrue(result.contains("3 days"));
    }

    @Test
    public void testAppendFieldTypes() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendYears();
        builder.appendMonths();
        builder.appendWeeks();
        builder.appendDays();
        builder.appendHours();
        builder.appendMinutes();
        builder.appendSeconds();
        builder.appendMillis();
        builder.appendSecondsWithMillis();
        builder.appendSecondsWithOptionalMillis();

        PeriodFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);
    }

    @Test
    public void testAppendPrefix() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("P");
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("P5", formatter.print(period));
    }

    @Test
    public void testAppendPrefixPair() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.appendPrefix("singular", "plural");
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period periodSingular = new Period(1, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("singular5".replace("singular", "singular"), formatter.print(periodSingular).trim());
    }

    @Test
    public void testAppendPrintedTypes() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.append(PeriodFormat.getDefault());
        assertNotNull(builder.toFormatter());
    }

    @Test
    public void testAppendComposite() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        PeriodFormatter subFormatter = PeriodFormat.getDefault();
        builder.append(subFormatter, subFormatter);
        assertNotNull(builder.toFormatter());
    }

    @Test
    public void testMinimumPrintedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.minimumPrintedDigits(4);
        builder.appendYears();
        PeriodFormatter formatter = builder.toFormatter();
        
        Period period = new Period(5, 0, 0, 0, 0, 0, 0, 0);
        assertEquals("0005", formatter.print(period));
    }

    @Test
    public void testRejectZeroFields() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.rejectZeroAdded();
        builder.appendYears();
        builder.appendMonths();
        PeriodFormatter formatter = builder.toFormatter();
        assertNotNull(formatter);
    }

    @Test
    public void testPrintZeroNeverAlwaysRarely() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.printZeroNever();
        builder.printZeroRarelyFirst();
        builder.printZeroRarelyLast();
        builder.printZeroAlways();
        builder.appendYears();
        assertNotNull(builder.toFormatter());
    }

    @Test
    public void testMaximumParsedDigits() {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        builder.maximumParsedDigits(5);
        builder.appendYears();
        assertNotNull(builder.toFormatter());
    }
}