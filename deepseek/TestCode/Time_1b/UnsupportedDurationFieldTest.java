package org.joda.time.field;

import static org.junit.Assert.*;
import org.junit.Test;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.ReadablePartial;
import org.joda.time.LocalDate;

public class UnsupportedDurationFieldTest {

    @Test(expected = NullPointerException.class)
    public void testGetInstanceNull() {
        UnsupportedDurationField.getInstance(null);
    }

    @Test
    public void testGetInstanceSingleton() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(type);
        assertSame(field1, field2);
    }

    @Test
    public void testGetInstanceDifferentTypes() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        assertNotSame(field1, field2);
    }

    @Test
    public void testIsSupported() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertFalse(field.isSupported());
    }

    @Test
    public void testGetType() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        assertSame(type, field.getType());
    }

    @Test
    public void testGetName() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        assertEquals(type.getName(), field.getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getMillis(1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisReadablePartialLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getMillis((ReadablePartial) null, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisReadablePartialNonNull() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        LocalDate date = new LocalDate(2020, 1, 1);
        field.getMillis(date, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getValue(1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLongLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getValueAsLong(1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueReadablePartialLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getValue((ReadablePartial) null, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueReadablePartialNonNull() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        LocalDate date = new LocalDate(2020, 1, 1);
        field.getValue(date, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueAsLongReadablePartialLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getValueAsLong((ReadablePartial) null, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddLongInt() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.add(1000L, 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddLongLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.add(1000L, 1L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddReadablePartialIntArray() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        int[] values = new int[1];
        field.add((ReadablePartial) null, 0, values);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddReadablePartialIntArrayNonNull() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        LocalDate date = new LocalDate(2020, 1, 1);
        int[] values = new int[1];
        field.add(date, 0, values);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddWrapField() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.addWrapField(1000L, 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceLongLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getDifference(1000L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLongLongLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getDifferenceAsLong(1000L, 500L);
    }

    @Test
    public void testCompareToSupported() {
        UnsupportedDurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.years());
        DurationField supported = DurationField.millis();
        int result = unsupported.compareTo(supported);
        assertTrue("UnsupportedDurationField.compareTo should return negative when compared to a supported field", result < 0);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.compareTo(null);
    }

    @Test
    public void testEqualsSameType() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(type);
        assertEquals(field1, field2);
    }

    @Test
    public void testEqualsDifferentType() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        assertFalse(field1.equals(field2));
    }

    @Test
    public void testEqualsNull() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertFalse(field.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertFalse(field.equals("string"));
    }

    @Test
    public void testHashCode() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(type);
        assertEquals(field1.hashCode(), field2.hashCode());
    }

    @Test
    public void testToString() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        String expected = "UnsupportedDurationField[" + type.getName() + "]";
        assertEquals(expected, field.toString());
    }
}