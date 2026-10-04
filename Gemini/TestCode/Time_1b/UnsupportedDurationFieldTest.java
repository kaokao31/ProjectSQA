package org.joda.time.field;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UnsupportedDurationFieldTest {

    @Test
    public void testGetInstance() {
        DurationFieldType hoursType = DurationFieldType.hours();
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(hoursType);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(hoursType);

        assertNotNull(field1);
        assertSame(field1, field2);
        assertSame(hoursType, field1.getType());
        assertEquals("hours", field1.getName());
    }

    @Test
    public void testGetInstanceDifferentTypes() {
        UnsupportedDurationField hours = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField days = UnsupportedDurationField.getInstance(DurationFieldType.days());

        assertNotNull(hours);
        assertNotNull(days);
        assertFalse(hours.equals(days));
    }

    @Test
    public void testIsSupported() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertFalse(field.isSupported());
    }

    @Test
    public void testIsPrecise() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertTrue(field.isPrecise());
    }

    @Test
    public void testGetUnitMillis() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertEquals(0L, field.getUnitMillis());
    }

    @Test
    public void testGetValueInt() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        try {
            field.getValue(100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("minutes"));
        }
    }

    @Test
    public void testGetValueLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        try {
            field.getValueAsLong(100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("minutes"));
        }
    }

    @Test
    public void testGetValueIntWithInstant() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        try {
            field.getValue(100L, 500L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("seconds"));
        }
    }

    @Test
    public void testGetValueLongWithInstant() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        try {
            field.getValueAsLong(100L, 500L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("seconds"));
        }
    }

    @Test
    public void testGetMillisInt() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        try {
            field.getMillis(10);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("millis"));
        }
    }

    @Test
    public void testGetMillisLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        try {
            field.getMillis(10L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("millis"));
        }
    }

    @Test
    public void testGetMillisIntWithInstant() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
        try {
            field.getMillis(5, 12345L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("halfdays"));
        }
    }

    @Test
    public void testGetMillisLongWithInstant() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
        try {
            field.getMillis(5L, 12345L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("halfdays"));
        }
    }

    @Test
    public void testAddInt() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        try {
            field.add(1000L, 1);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("days"));
        }
    }

    @Test
    public void testAddLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        try {
            field.add(1000L, 1L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("days"));
        }
    }

    @Test
    public void testGetDifference() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        try {
            field.getDifference(1000L, 500L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("months"));
        }
    }

    @Test
    public void testGetDifferenceAsLong() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        try {
            field.getDifferenceAsLong(1000L, 500L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("months"));
        }
    }

    @Test
    public void testCompareTo() {
        UnsupportedDurationField unsupported1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField unsupported2 = UnsupportedDurationField.getInstance(DurationFieldType.days());
        DurationField supported = ISOChronology.getInstanceUTC().hours();

        assertEquals(0, unsupported1.compareTo(unsupported1));
        assertEquals(0, unsupported1.compareTo(unsupported2));
        assertEquals(1, unsupported1.compareTo(supported));
    }

    @Test
    public void testEqualsAndHashCode() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField field3 = UnsupportedDurationField.getInstance(DurationFieldType.days());

        assertTrue(field1.equals(field1));
        assertTrue(field1.equals(field2));
        assertFalse(field1.equals(field3));
        assertFalse(field1.equals(null));
        assertFalse(field1.equals("Not a DurationField"));

        assertEquals(field1.hashCode(), field2.hashCode());
        assertEquals("hours".hashCode(), field1.hashCode());
    }

    @Test
    public void testToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        assertEquals("UnsupportedDurationField[hours]", field.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(field);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertSame(field, deserialized);
    }
}