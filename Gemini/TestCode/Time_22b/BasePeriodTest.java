package org.joda.time.base;

import org.junit.Test;
import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.ReadablePeriod;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import static org.junit.Assert.*;

public class BasePeriodTest {

    @Test
    public void testConstructorsAndGetters() {
        // 1. BasePeriod(int hours, int minutes, int seconds, int millis, PeriodType type)
        BasePeriod p1 = new BasePeriod(1, 2, 3, 4, PeriodType.time());
        assertNotNull(p1);
        assertEquals(PeriodType.time(), p1.getPeriodType());
        assertEquals(4, p1.size());
        assertEquals(1, p1.getValue(0)); // hours
        assertEquals(2, p1.getValue(1)); // minutes
        assertEquals(3, p1.getValue(2)); // seconds
        assertEquals(4, p1.getValue(3)); // millis

        // 2. BasePeriod(long duration)
        BasePeriod p2 = new BasePeriod(1500L);
        assertNotNull(p2);
        assertEquals(PeriodType.standard(), p2.getPeriodType());

        // 3. BasePeriod(long duration, PeriodType type, Chronology chrono)
        BasePeriod p3 = new BasePeriod(1500L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertNotNull(p3);
        assertEquals(PeriodType.standard(), p3.getPeriodType());

        // 4. BasePeriod(ReadableInstant start, ReadableInstant end, PeriodType type)
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, 0);
        DateTime end = new DateTime(2020, 1, 1, 1, 1, 1, 1);
        BasePeriod p4 = new BasePeriod(start, end, PeriodType.standard());
        assertNotNull(p4);

        // 5. BasePeriod(ReadableInstant start, ReadableInstant end, boolean) - wait, null/null instants
        BasePeriod p5 = new BasePeriod((ReadableInstant) null, (ReadableInstant) null, PeriodType.standard());
        assertNotNull(p5);
        for (int i = 0; i < p5.size(); i++) {
            assertEquals(0, p5.getValue(i));
        }

        // 6. BasePeriod(ReadablePartial start, ReadablePartial end, PeriodType type)
        LocalDate startDate = new LocalDate(2020, 1, 1);
        LocalDate endDate = new LocalDate(2020, 1, 2);
        BasePeriod p6 = new BasePeriod(startDate, endDate, PeriodType.standard());
        assertNotNull(p6);

        // 7. BasePeriod(ReadablePartial start, ReadablePartial end, boolean) - null/null partials
        BasePeriod p7 = new BasePeriod((ReadablePartial) null, (ReadablePartial) null, PeriodType.standard());
        assertNotNull(p7);

        // 8. BasePeriod(long duration, long endInstant, PeriodType type, Chronology chrono)
        BasePeriod p8 = new BasePeriod(1000L, 2000L, PeriodType.standard(), ISOChronology.getInstanceUTC());
        assertNotNull(p8);
    }

    @Test
    public void testArrayConstructorsAndSetters() {
        int[] values = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        BasePeriod p = new BasePeriod(values, PeriodType.standard());
        assertNotNull(p);
        assertEquals(8, p.size());
        assertEquals(1, p.getValue(0));
        assertEquals(8, p.getValue(7));

        // Test setValues
        int[] newValues = new int[]{8, 7, 6, 5, 4, 3, 2, 1};
        p.setValues(newValues);
        assertEquals(8, p.getValue(0));
        assertEquals(1, p.getValue(7));

        // Test setValue
        p.setValue(0, 99);
        assertEquals(99, p.getValue(0));

        // Test addValue
        p.addValue(0, 1);
        assertEquals(100, p.getValue(0));
    }

    @Test
    public void testReadablePeriodAndDurationConstructors() {
        Period period = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        BasePeriod p1 = new BasePeriod(period);
        assertEquals(8, p1.size());
        assertEquals(1, p1.getValue(0));

        BasePeriod p2 = new BasePeriod(period, PeriodType.time());
        assertNotNull(p2);

        ReadableDuration duration = new org.joda.time.Duration(5000L);
        BasePeriod p3 = new BasePeriod(duration, PeriodType.standard());
        assertNotNull(p3);

        BasePeriod p4 = new BasePeriod(duration, (PeriodType) null);
        assertNotNull(p4);

        BasePeriod p5 = new BasePeriod((ReadableDuration) null, PeriodType.standard());
        assertNotNull(p5);
    }

    @Test
    public void testObjectConstructorBug22Context() {
        // Target bug 22 specifically involves object/constructor handling for periods or durations.
        // Let's pass various objects to BasePeriod(Object, PeriodType, Chronology)
        
        // 1. Object as ReadableInterval or similar through general constructor or null object
        BasePeriod pNull = new BasePeriod((Object) null, PeriodType.standard(), ISOChronology.getInstance());
        assertNotNull(pNull);

        // 2. Object as ReadableDuration
        org.joda.time.Duration dur = org.joda.time.Duration.standardHours(2);
        BasePeriod pDur = new BasePeriod(dur, PeriodType.standard(), ISOChronology.getInstance());
        assertNotNull(pDur);

        // 3. Object as ReadableInterval (via Period)
        BasePeriod pObj = new BasePeriod(new Period(10), PeriodType.standard(), ISOChronology.getInstance());
        assertNotNull(pObj);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidObjectConstructor() {
        // Passing an unsupported object type to trigger IllegalArgumentException
        new BasePeriod("NotAPeriodOrDuration", PeriodType.standard(), ISOChronology.getInstance());
    }

    @Test
    public void testFieldTypeOperations() {
        BasePeriod p = new BasePeriod(1, 2, 3, 4, 5, 6, 7, 8);
        DurationFieldType[] types = p.getFieldTypes();
        assertNotNull(types);
        assertEquals(8, types.length);

        int[] vals = p.getValues();
        assertNotNull(vals);
        assertEquals(8, vals.length);

        assertEquals(1, p.get(DurationFieldType.years()));
        assertEquals(2, p.get(DurationFieldType.months()));
        assertEquals(3, p.get(DurationFieldType.weeks()));
        assertEquals(4, p.get(DurationFieldType.days()));
        assertEquals(5, p.get(DurationFieldType.hours()));
        assertEquals(6, p.get(DurationFieldType.minutes()));
        assertEquals(7, p.get(DurationFieldType.seconds()));
        assertEquals(8, p.get(DurationFieldType.millis()));
        
        // Unsupported field type should return 0
        assertEquals(0, p.get(null));
        assertEquals(0, p.get(DurationFieldType.eras()));
    }

    @Test
    public void testProtectedMethodsAndHelpers() {
        // Accessing methods like mergePeriod, setPeriod, etc. via a subclass or MutablePeriod/Period
        Period p = new Period(1, 2, 3, 4);
        // BasePeriod methods protection check via subclassing or direct calls if public/protected
        // BasePeriod has protected setPeriod(ReadablePeriod), mergePeriod(ReadablePeriod), etc.
        SubBasePeriod sub = new SubBasePeriod(1, 2, 3, 4, 5, 6, 7, 8, PeriodType.standard());
        sub.setPeriod(new Period(10, 10, 10, 10, 10, 10, 10, 10));
        assertEquals(10, sub.getValue(0));

        sub.addPeriod(new Period(1, 1, 1, 1, 1, 1, 1, 1));
        assertEquals(11, sub.getValue(0));
        
        sub.setField(DurationFieldType.hours(), 15);
        assertEquals(15, sub.get(DurationFieldType.hours()));

        sub.addField(DurationFieldType.hours(), 5);
        assertEquals(20, sub.get(DurationFieldType.hours()));
        
        sub.mergePeriod(new Period(0, 0, 0, 0, 2, 0, 0, 0));
        assertEquals(2, sub.get(DurationFieldType.hours())); // set by merge if supported or checks field
    }

    @Test
    public void testEqualsAndHashCode() {
        Period p1 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p2 = new Period(1, 2, 3, 4, 5, 6, 7, 8);
        Period p3 = new Period(8, 7, 6, 5, 4, 3, 2, 1);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "SomeString");
    }

    // Helper subclass to expose protected methods of BasePeriod
    private static class SubBasePeriod extends BasePeriod {
        public SubBasePeriod(int years, int months, int weeks, int days, int hours, int minutes, int seconds, int millis, PeriodType type) {
            super(years, months, weeks, days, hours, minutes, seconds, millis, type);
        }

        public void setPeriod(ReadablePeriod period) {
            super.setPeriod(period);
        }

        public void addPeriod(ReadablePeriod period) {
            super.addPeriod(period);
        }

        public void setField(DurationFieldType field, int value) {
            super.setField(field, value);
        }

        public void addField(DurationFieldType field, int value) {
            super.addField(field, value);
        }

        public void mergePeriod(ReadablePeriod period) {
            super.mergePeriod(period);
        }
    }
}