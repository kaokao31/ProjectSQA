package org.joda.time.chrono;

import org.junit.Test;
import org.joda.time.Chronology;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.Months;
import org.joda.time.ReadablePartial;
import org.joda.time.YearMonth;
import org.joda.time.LocalDate;

import static org.junit.Assert.*;

public class BasicMonthOfYearDateTimeFieldTest {

    @Test
    public void testBasicFunctionality() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        assertTrue(field.isSupported());
        assertEquals(1, field.getMinimumValue());
        assertEquals(12, field.getMaximumValue());
        
        DurationField rangeField = field.getRangeDurationField();
        assertNotNull(rangeField);
        
        DurationField durField = field.getDurationField();
        assertNotNull(durField);
    }

    @Test
    public void testGet() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        // 1388534400000L is 2014-01-01
        int month = field.get(1388534400000L);
        assertEquals(1, month);
    }

    @Test
    public void testAdd() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        // Add months to 2014-01-01
        long newInstant = field.add(1388534400000L, 1);
        int month = field.get(newInstant);
        assertEquals(2, month);
    }

    @Test
    public void testAddZero() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        long instant = 1388534400000L;
        long newInstant = field.add(instant, 0);
        assertEquals(instant, newInstant);
    }

    @Test
    public void testAddNegative() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        long instant = 1388534400000L; // 2014-01-01
        long newInstant = field.add(instant, -1);
        int month = field.get(newInstant);
        assertEquals(12, month);
    }

    @Test
    public void testAddLong() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        long instant = 1388534400000L;
        long newInstant = field.add(instant, 2L);
        int month = field.get(newInstant);
        assertEquals(3, month);
    }

    @Test
    public void testAddWrapped() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        long instant = 1388534400000L; // Jan 2014
        long newInstant = field.addWrapField(instant, 15); // 15 months wrap around 12 -> 3rd month
        int month = field.get(newInstant);
        assertEquals(3, month);
        
        long newInstantNeg = field.addWrapField(instant, -15);
        int monthNeg = field.get(newInstantNeg);
        assertEquals(10, monthNeg);
    }

    @Test
    public void testGetDifference() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        long jan2014 = 1388534400000L;
        long mar2014 = 1393641600000L;

        int diff = field.getDifference(mar2014, jan2014);
        assertEquals(2, diff);

        long diffLong = field.getDifferenceAsLong(mar2014, jan2014);
        assertEquals(2L, diffLong);
    }

    @Test
    public void testSet() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        long jan2014 = 1388534400000L;
        long apr2014 = field.set(jan2014, 4);
        assertEquals(4, field.get(apr2014));
    }

    @Test
    public void testRemainder() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        long instant = 1388534400000L;
        long remainder = field.remainder(instant);
        assertTrue(remainder >= 0);
    }

    @Test
    public void testAddIntoPartial() {
        Chronology chrono = GregorianChronology.getInstance(DateTimeZone.UTC);
        BasicMonthOfYearDateTimeField field = new BasicMonthOfYearDateTimeField((BasicChronology) chrono, 2);

        YearMonth ym = new YearMonth(2014, 1, chrono);
        int[] values = new int[] { 2014, 1 };
        int[] newValues = field.add(ym, 0, values, 1);
        assertEquals(2, newValues[1]);
        
        int[] newValuesWrap = field.addWrapField(ym, 0, values, 15);
        assertEquals(3, newValuesWrap[1]);
    }
}