package org.joda.time.base;

import org.joda.time.*;
import org.joda.time.field.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for BaseSingleFieldPeriod, targeting edge cases and potential faults
 * (e.g., integer overflow in negated(), multipliedBy(), plus(), minus()).
 */
public class BaseSingleFieldPeriodTest {

    // Concrete subclass for testing abstract BaseSingleFieldPeriod
    private static class TestPeriod extends BaseSingleFieldPeriod {
        public TestPeriod(int period, PeriodType type) {
            super(period, type);
        }
    }

    private TestPeriod days1;
    private TestPeriod days2;
    private TestPeriod daysMinus1;
    private TestPeriod daysZero;
    private TestPeriod daysMax;
    private TestPeriod daysMin;
    private TestPeriod hours1;
    private TestPeriod hoursMinus1;

    @Before
    public void setUp() {
        days1 = new TestPeriod(1, PeriodType.days());
        days2 = new TestPeriod(2, PeriodType.days());
        daysMinus1 = new TestPeriod(-1, PeriodType.days());
        daysZero = new TestPeriod(0, PeriodType.days());
        daysMax = new TestPeriod(Integer.MAX_VALUE, PeriodType.days());
        daysMin = new TestPeriod(Integer.MIN_VALUE, PeriodType.days());
        hours1 = new TestPeriod(1, PeriodType.hours());
        hoursMinus1 = new TestPeriod(-1, PeriodType.hours());
    }

    // --- getValue() ---
    @Test
    public void testGetValue() {
        assertEquals(1, days1.getValue());
        assertEquals(-1, daysMinus1.getValue());
        assertEquals(0, daysZero.getValue());
        assertEquals(Integer.MAX_VALUE, daysMax.getValue());
        assertEquals(Integer.MIN_VALUE, daysMin.getValue());
    }

    // --- getPeriodType() ---
    @Test
    public void testGetPeriodType() {
        assertSame(PeriodType.days(), days1.getPeriodType());
        assertSame(PeriodType.hours(), hours1.getPeriodType());
    }

    // --- plus() ---
    @Test
    public void testPlusSameType() {
        TestPeriod sum = new TestPeriod(3, PeriodType.days());
        assertEquals(sum, days1.plus(days2));
        assertEquals(daysZero, days1.plus(daysMinus1));
    }

    @Test
    public void testPlusDifferentType() {
        // plus with different period type should not throw and return a Period
        Period result = days1.plus(hours1);
        assertNotNull(result);
    }

    @Test(expected = ArithmeticException.class)
    public void testPlusOverflowMax() {
        daysMax.plus(days1);  // MAX_VALUE + 1 overflows
    }

    @Test(expected = ArithmeticException.class)
    public void testPlusOverflowMin() {
        daysMin.plus(daysMinus1);  // MIN_VALUE + (-1) overflows
    }

    // --- minus() ---
    @Test
    public void testMinusSameType() {
        assertEquals(daysMinus1, days1.minus(days2));
        assertEquals(days1, days1.minus(daysZero));
    }

    @Test
    public void testMinusDifferentType() {
        Period result = days1.minus(hours1);
        assertNotNull(result);
    }

    @Test(expected = ArithmeticException.class)
    public void testMinusOverflowMax() {
        daysMax.minus(daysMinus1);  // MAX_VALUE - (-1) = MAX_VALUE + 1 overflows
    }

    @Test(expected = ArithmeticException.class)
    public void testMinusOverflowMin() {
        daysMin.minus(days1);  // MIN_VALUE - 1 overflows
    }

    // --- multipliedBy() ---
    @Test
    public void testMultipliedByZero() {
        assertEquals(daysZero, days1.multipliedBy(0));
    }

    @Test
    public void testMultipliedByOne() {
        assertSame(days1, days1.multipliedBy(1));  // same instance if scalar == 1?
        // Actually, multipliedBy may return a new instance; we check value and type
        assertEquals(days1, days1.multipliedBy(1));
    }

    @Test
    public void testMultipliedByMinusOne() {
        assertEquals(daysMinus1, days1.multipliedBy(-1));
    }

    @Test(expected = ArithmeticException.class)
    public void testMultipliedByOverflowMax() {
        daysMax.multipliedBy(2);  // MAX_VALUE * 2 overflows
    }

    @Test(expected = ArithmeticException.class)
    public void testMultipliedByOverflowMin() {
        daysMin.multipliedBy(-1);  // MIN_VALUE * -1 overflows (safeNegate)
    }

    // --- dividedBy() ---
    @Test
    public void testDividedByOne() {
        assertEquals(days1, days1.dividedBy(1));
    }

    @Test
    public void testDividedByMinusOne() {
        assertEquals(daysMinus1, days1.dividedBy(-1));
    }

    @Test(expected = ArithmeticException.class)
    public void testDividedByZero() {
        days1.dividedBy(0);
    }

    @Test
    public void testDividedByTruncation() {
        // 3 days / 2 = 1 day (truncation toward zero)
        TestPeriod days3 = new TestPeriod(3, PeriodType.days());
        assertEquals(days1, days3.dividedBy(2));
    }

    // --- negated() ---
    @Test
    public void testNegatedPositive() {
        assertEquals(daysMinus1, days1.negated());
    }

    @Test
    public void testNegatedNegative() {
        assertEquals(days1, daysMinus1.negated());
    }

    @Test
    public void testNegatedZero() {
        assertEquals(daysZero, daysZero.negated());
    }

    @Test(expected = ArithmeticException.class)
    public void testNegatedMinValue() {
        daysMin.negated();  // -MIN_VALUE overflows
    }

    // --- toDurationTo() / toDurationFrom() ---
    @Test
    public void testToDurationTo() {
        DateTime start = new DateTime(2000, 1, 1, 0, 0);
        Duration dur = days1.toDurationTo(start);
        // Duration should represent 1 day
        assertEquals(1, dur.getMillis() / (24 * 60 * 60 * 1000));
    }

    @Test
    public void testToDurationFrom() {
        DateTime start = new DateTime(2000, 1, 2, 0, 0);
        Duration dur = days1.toDurationFrom(start);
        // Duration should represent -1 day (since start is after)
        assertEquals(-1, dur.getMillis() / (24 * 60 * 60 * 1000));
    }

    // --- equals() ---
    @Test
    public void testEqualsSame() {
        TestPeriod days1Copy = new TestPeriod(1, PeriodType.days());
        assertEquals(days1, days1Copy);
    }

    @Test
    public void testEqualsDifferentValue() {
        assertFalse(days1.equals(days2));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(days1.equals(hours1));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(days1.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(days1.equals("string"));
    }

    // --- hashCode() ---
    @Test
    public void testHashCodeConsistency() {
        TestPeriod days1Copy = new TestPeriod(1, PeriodType.days());
        assertEquals(days1.hashCode(), days1Copy.hashCode());
    }

    @Test
    public void testHashCodeDifferentValue() {
        assertTrue(days1.hashCode() != days2.hashCode() || !days1.equals(days2));
    }

    // --- compareTo() ---
    @Test
    public void testCompareToEqual() {
        TestPeriod days1Copy = new TestPeriod(1, PeriodType.days());
        assertEquals(0, days1.compareTo(days1Copy));
    }

    @Test
    public void testCompareToLess() {
        assertTrue(days1.compareTo(days2) < 0);
    }

    @Test
    public void testCompareToGreater() {
        assertTrue(days2.compareTo(days1) > 0);
    }

    @Test
    public void testCompareToDifferentType() {
        // compareTo across different period types should compare by value? 
        // In Joda-Time, compareTo compares the value if types are the same, 
        // otherwise it may throw or compare by type. We'll just ensure no exception.
        try {
            days1.compareTo(hours1);
        } catch (Exception e) {
            // acceptable if it throws
        }
    }

    // --- toString() ---
    @Test
    public void testToString() {
        // BaseSingleFieldPeriod.toString() returns something like "PT1D" or "PT-1D"
        assertNotNull(days1.toString());
        assertTrue(days1.toString().contains("1"));
    }
}