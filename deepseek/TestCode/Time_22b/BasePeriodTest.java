package org.joda.time.base;

import org.junit.Test;
import static org.junit.Assert.*;
import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.field.DurationFieldType;

/**
 * Test suite for BasePeriod, targeting Defects4J Time-22.
 */
public class BasePeriodTest {
    private static final long DAY_MILLIS = 86400000L;

    // Helper subclass to access protected constructors
    private static class TestableBasePeriod extends BasePeriod {
        private static final long serialVersionUID = 1L;

        public TestableBasePeriod(int[] values, PeriodType type) {
            super(values, type);
        }

        public TestableBasePeriod(long duration, PeriodType type, Chronology chrono) {
            super(duration, type, chrono);
        }

        public TestableBasePeriod(long startMillis, long endMillis, PeriodType type, Chronology chrono) {
            super(startMillis, endMillis, type, chrono);
        }
    }

    // --- Constructor tests (int[], PeriodType) ---

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPeriodType() {
        new TestableBasePeriod(new int[]{1}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorValuesLengthMismatch() {
        PeriodType type = PeriodType.yearMonthDay(); // 3 fields
        new TestableBasePeriod(new int[]{1, 2}, type); // 2 values
    }

    @Test
    public void testConstructorValidValues() {
        PeriodType type = PeriodType.weeks();
        int[] values = new int[]{5};
        TestableBasePeriod p = new TestableBasePeriod(values, type);
        assertArrayEquals(values, p.getValues());
        assertSame(type, p.getPeriodType());
    }

    @Test
    public void testGetValuesReturnsCopy() {
        PeriodType type = PeriodType.days();
        int[] values = new int[]{7};
        TestableBasePeriod p = new TestableBasePeriod(values, type);
        values[0] = 10;
        assertArrayEquals(new int[]{7}, p.getValues());
    }

    // --- size() tests ---

    @Test
    public void testSize() {
        assertEquals(3, new TestableBasePeriod(new int[3], PeriodType.yearMonthDay()).size());
        assertEquals(7, new TestableBasePeriod(new int[7], PeriodType.yearMonthDayTime()).size());
        assertEquals(1, new TestableBasePeriod(new int[1], PeriodType.days()).size());
    }

    // --- setValue tests ---

    @Test
    public void testSetValue() {
        PeriodType type = PeriodType.minutes();
        TestableBasePeriod p = new TestableBasePeriod(new int[]{0}, type);
        p.setValue(0, 59);
        assertEquals(59, p.getValues()[0]);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetValueIndexOutOfBounds() {
        PeriodType type = PeriodType.weeks();
        TestableBasePeriod p = new TestableBasePeriod(new int[]{1}, type);
        p.setValue(1, 5);
    }

    // --- setPeriod tests ---

    @Test
    public void testSetPeriod() {
        PeriodType type = PeriodType.yearMonthDay();
        TestableBasePeriod p = new TestableBasePeriod(new int[]{1, 2, 3}, type);
        p.setPeriod(new int[]{4, 5, 6});
        assertArrayEquals(new int[]{4, 5, 6}, p.getValues());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPeriodNull() {
        PeriodType type = PeriodType.yearMonthDay();
        TestableBasePeriod p = new TestableBasePeriod(new int[]{1, 2, 3}, type);
        p.setPeriod(null);
    }

    @Test
    public void testSetPeriodCopiesArray() {
        PeriodType type = PeriodType.years();
        int[] src = new int[]{10};
        TestableBasePeriod p = new TestableBasePeriod(new int[]{0}, type);
        p.setPeriod(src);
        src[0] = 20;
        assertEquals(10, p.getValues()[0]);
    }

    // --- indexOf tests ---

    @Test
    public void testIndexOf() {
        PeriodType type = PeriodType.yearMonthDay();
        TestableBasePeriod p = new TestableBasePeriod(new int[]{1, 2, 3}, type);
        assertEquals(0, p.indexOf(DateTimeFieldType.year()));
        assertEquals(1, p.indexOf(DateTimeFieldType.monthOfYear()));
        assertEquals(2, p.indexOf(DateTimeFieldType.dayOfMonth()));
        assertEquals(-1, p.indexOf(DateTimeFieldType.hourOfDay()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfNullFieldType() {
        PeriodType type = PeriodType.weeks();
        TestableBasePeriod p = new TestableBasePeriod(new int[]{1}, type);
        p.indexOf(null);
    }

    // --- Constructor tests (long, PeriodType, Chronology) ---

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLongNullPeriodType() {
        long duration = 1000L;
        new TestableBasePeriod(duration, null, ISOChronology.getInstanceUTC());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorLongNullChronology() {
        long duration = 1000L;
        new TestableBasePeriod(duration, PeriodType.seconds(), null);
    }

    @Test
    public void testConstructorLongDurationDay() {
        long duration = DAY_MILLIS;
        PeriodType type = PeriodType.dayTime();
        TestableBasePeriod p = new TestableBasePeriod(duration, type, ISOChronology.getInstanceUTC());
        int[] values = p.getValues();
        // dayTime order: days, hours, minutes, seconds, millis
        assertEquals(1, values[0]);
        assertEquals(0, values[1]);
        assertEquals(0, values[2]);
        assertEquals(0, values[3]);
        assertEquals(0, values[4]);
    }

    @Test
    public void testConstructorLongDurationZero() {
        TestableBasePeriod p = new TestableBasePeriod(0L, PeriodType.seconds(), ISOChronology.getInstanceUTC());
        assertArrayEquals(new int[]{0}, p.getValues());
    }

    // --- Constructor tests (long, long, PeriodType, Chronology) ---

    @Test
    public void testConstructorBetweenDays() {
        long start = 0L;
        long end = DAY_MILLIS;
        PeriodType type = PeriodType.dayTime();
        TestableBasePeriod p = new TestableBasePeriod(start, end, type, ISOChronology.getInstanceUTC());
        int[] values = p.getValues();
        assertEquals(1, values[0]); // day
        assertEquals(0, values[1]);
        assertEquals(0, values[2]);
        assertEquals(0, values[3]);
        assertEquals(0, values[4]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBetweenNullType() {
        new TestableBasePeriod(0L, 1000L, null, ISOChronology.getInstanceUTC());
    }

    // --- Edge cases and bug-specific tests (Defects4J Time-22) ---

    @Test
    public void testConstructorLongDurationYearMonthOnly() {
        // Use a period type that contains only years and months (imprecise fields)
        PeriodType ymOnly = PeriodType.forFields(new DurationFieldType[]{
                DurationFieldType.years(),
                DurationFieldType.months()
        });
        long duration = (long) (3 * 365.25 * DAY_MILLIS); // ~3 years
        try {
            TestableBasePeriod p = new TestableBasePeriod(duration, ymOnly, ISOChronology.getInstanceUTC());
            assertNotNull(p);
        } catch (ArithmeticException e) {
            fail("Constructor should not throw ArithmeticException: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("Constructor should not throw ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
    }

    @Test
    public void testConstructorLongDurationLargeValue() {
        // Long duration that might cause overflow if not handled properly
        long duration = Long.MAX_VALUE / 1000;
        PeriodType type = PeriodType.yearMonthDayTime();
        try {
            TestableBasePeriod p = new TestableBasePeriod(duration, type, ISOChronology.getInstanceUTC());
            assertNotNull(p);
        } catch (Exception e) {
            // Should not throw, but if it does, must be IllegalArgumentException for field sizes
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testConstructorLongDurationNegative() {
        // Negative duration should be allowed (period can be negative)
        long duration = -DAY_MILLIS;
        PeriodType type = PeriodType.days();
        TestableBasePeriod p = new TestableBasePeriod(duration, type, ISOChronology.getInstanceUTC());
        assertEquals(-1, p.getValues()[0]);
    }

    @Test
    public void testSetValueAfterPeriodSet() {
        PeriodType type = PeriodType.weeks();
        TestableBasePeriod p = new TestableBasePeriod(new int[]{1}, type);
        p.setPeriod(new int[]{5});
        p.setValue(0, 10);
        assertEquals(10, p.getValues()[0]);
    }

    @Test
    public void testGetValuesImmutability() {
        PeriodType type = PeriodType.weeks();
        TestableBasePeriod p = new TestableBasePeriod(new int[]{1}, type);
        int[] vals = p.getValues();
        vals[0] = 100; // Should not affect the internal array
        assertEquals(1, p.getValues()[0]);
    }
}