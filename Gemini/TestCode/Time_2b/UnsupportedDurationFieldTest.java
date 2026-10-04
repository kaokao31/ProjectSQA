package org.joda.time.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for UnsupportedDurationField.
 */
public class UnsupportedDurationFieldTest {

    private DurationFieldType fieldType;
    private UnsupportedDurationField unsupportedField;

    @Before
    public void setUp() {
        fieldType = DurationFieldType.seconds();
        unsupportedField = UnsupportedDurationField.getInstance(fieldType);
    }

    @Test
    public void testGetInstanceCaching() {
        UnsupportedDurationField instance1 = UnsupportedDurationField.getInstance(fieldType);
        UnsupportedDurationField instance2 = UnsupportedDurationField.getInstance(fieldType);
        Assert.assertSame("Instances with the same type should be cached and identical", instance1, instance2);

        UnsupportedDurationField instanceOther = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        Assert.assertNotNull(instanceOther);
        Assert.assertNotSame(instance1, instanceOther);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceNull() {
        UnsupportedDurationField.getInstance(null);
    }

    @Test
    public void testGetType() {
        Assert.assertEquals(fieldType, unsupportedField.getType());
    }

    @Test
    public void testGetName() {
        Assert.assertEquals(fieldType.getName(), unsupportedField.getName());
    }

    @Test
    public void testIsSupported() {
        Assert.assertFalse(unsupportedField.isSupported());
    }

    @Test
    public void testGetUnitMillis() {
        Assert.assertEquals(0L, unsupportedField.getUnitMillis());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueLongTime() {
        unsupportedField.getValue(100L, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetValueIntTime() {
        unsupportedField.getValue(100L, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisLongValue() {
        unsupportedField.getMillis(100L, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMillisIntValue() {
        unsupportedField.getMillis(100, 1000L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddLongValue() {
        unsupportedField.add(100L, 10L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddIntValue() {
        unsupportedField.add(100L, 10);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddLongValueWithInstant() {
        unsupportedField.add(100L, 10L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddIntValueWithInstant() {
        unsupportedField.add(100L, 10);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDifferenceLong() {
        unsupportedField.difference(100L, 50L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDifferenceAsLong() {
        unsupportedField.differenceAsLong(100L, 50L);
    }

    @Test
    public void testCompareTo() {
        DurationField supported = DurationFieldType.seconds().getField(org.joda.time.chrono.ISOChronology.getInstanceUTC());
        Assert.assertEquals(1, unsupportedField.compareTo(supported));
        
        DurationField anotherUnsupported = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        Assert.assertEquals(0, unsupportedField.compareTo(anotherUnsupported));
        
        Assert.assertEquals(0, unsupportedField.compareTo(unsupportedField));

        try {
            unsupportedField.compareTo(null);
            // Depending on implementation, might throw NullPointerException or handle it.
        } catch (Exception e) {
            // expected or allowed
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        UnsupportedDurationField instance1 = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        UnsupportedDurationField instance2 = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        UnsupportedDurationField instanceMinutes = UnsupportedDurationField.getInstance(DurationFieldType.minutes());

        Assert.assertTrue(instance1.equals(instance1));
        Assert.assertTrue(instance1.equals(instance2));
        Assert.assertFalse(instance1.equals(null));
        Assert.assertFalse(instance1.equals("Some String"));
        Assert.assertFalse(instance1.equals(instanceMinutes));

        Assert.assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void testToString() {
        String str = unsupportedField.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("UnsupportedDurationField"));
        Assert.assertTrue(str.contains(fieldType.getName()));
    }

    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(unsupportedField);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        Assert.assertTrue(deserialized instanceof UnsupportedDurationField);
        UnsupportedDurationField deserializedField = (UnsupportedDurationField) deserialized;
        
        // Verify singleton / readResolve behavior if implemented
        Assert.assertSame(unsupportedField, deserializedField);
        Assert.assertEquals(unsupportedField.getType(), deserializedField.getType());
    }
}