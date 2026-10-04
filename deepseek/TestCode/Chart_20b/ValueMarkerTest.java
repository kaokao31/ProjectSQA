package org.jfree.chart.plot;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.BasicStroke;
import org.jfree.chart.event.MarkerChangeListener;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

public class ValueMarkerTest {

    private ValueMarker marker;
    private static final double TEST_VALUE = 50.0;
    private static final double EPSILON = 0.000000001d;

    @Before
    public void setUp() {
        marker = new ValueMarker(TEST_VALUE);
    }

    @Test
    public void testConstructorDefault() {
        assertEquals(TEST_VALUE, marker.getValue(), EPSILON);
        assertEquals(Color.gray, marker.getPaint());
        assertEquals(new BasicStroke(0.5f), marker.getStroke());
        assertEquals(LengthAdjustmentType.CENTER, marker.getLabelOffsetType());
        assertEquals(RectangleAnchor.BOTTOM, marker.getLabelAnchor());
        assertEquals(TextAnchor.CENTER, marker.getLabelTextAnchor());
    }

    @Test
    public void testConstructorWithPaintAndStroke() {
        Color paint = Color.RED;
        BasicStroke stroke = new BasicStroke(2.0f);
        ValueMarker m = new ValueMarker(TEST_VALUE, paint, stroke);
        assertEquals(TEST_VALUE, m.getValue(), EPSILON);
        assertEquals(paint, m.getPaint());
        assertEquals(stroke, m.getStroke());
    }

    @Test
    public void testSetValue() {
        double newValue = 100.0;
        marker.setValue(newValue);
        assertEquals(newValue, marker.getValue(), EPSILON);
    }

    @Test
    public void testSetValueWithListener() {
        final boolean[] notified = {false};
        marker.addChangeListener(new MarkerChangeListener() {
            @Override
            public void markerChanged(MarkerChangeEvent event) {
                notified[0] = true;
            }
        });
        marker.setValue(200.0);
        assertTrue("Listener should be notified", notified[0]);
    }

    @Test
    public void testSetValueNegative() {
        marker.setValue(-10.0);
        assertEquals(-10.0, marker.getValue(), EPSILON);
    }

    @Test
    public void testSetValueZero() {
        marker.setValue(0.0);
        assertEquals(0.0, marker.getValue(), EPSILON);
    }

    @Test
    public void testSetValueMaxDouble() {
        marker.setValue(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, marker.getValue(), EPSILON);
    }

    @Test
    public void testSetValueMinDouble() {
        marker.setValue(-Double.MAX_VALUE);
        assertEquals(-Double.MAX_VALUE, marker.getValue(), EPSILON);
    }

    @Test
    public void testSetValueNaN() {
        marker.setValue(Double.NaN);
        assertTrue(Double.isNaN(marker.getValue()));
    }

    @Test
    public void testSetValueInfinity() {
        marker.setValue(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, marker.getValue(), EPSILON);
    }

    @Test
    public void testSetValueNegativeInfinity() {
        marker.setValue(Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, marker.getValue(), EPSILON);
    }

    @Test
    public void testGetValue() {
        assertEquals(TEST_VALUE, marker.getValue(), EPSILON);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        ValueMarker cloned = (ValueMarker) marker.clone();
        assertNotSame(marker, cloned);
        assertEquals(marker.getValue(), cloned.getValue(), EPSILON);
        assertEquals(marker.getPaint(), cloned.getPaint());
        assertEquals(marker.getStroke(), cloned.getStroke());
    }

    @Test
    public void testEquals() {
        ValueMarker m1 = new ValueMarker(TEST_VALUE);
        ValueMarker m2 = new ValueMarker(TEST_VALUE);
        assertTrue(m1.equals(m2));
        assertTrue(m2.equals(m1));
    }

    @Test
    public void testEqualsDifferentValue() {
        ValueMarker m1 = new ValueMarker(10.0);
        ValueMarker m2 = new ValueMarker(20.0);
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse(marker.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        assertFalse(marker.equals("Not a marker"));
    }

    @Test
    public void testEqualsWithSameObject() {
        assertTrue(marker.equals(marker));
    }

    @Test
    public void testHashCode() {
        ValueMarker m1 = new ValueMarker(TEST_VALUE);
        ValueMarker m2 = new ValueMarker(TEST_VALUE);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    public void testHashCodeDifferentValues() {
        ValueMarker m1 = new ValueMarker(10.0);
        ValueMarker m2 = new ValueMarker(20.0);
        assertNotEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    public void testSerialization() {
        ValueMarker m1 = new ValueMarker(TEST_VALUE);
        ValueMarker m2 = null;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(m1);
            oos.flush();
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            m2 = (ValueMarker) ois.readObject();
        } catch (Exception e) {
            fail("Serialization failed: " + e.getMessage());
        }
        assertNotNull(m2);
        assertEquals(m1.getValue(), m2.getValue(), EPSILON);
        assertEquals(m1.getPaint(), m2.getPaint());
        assertEquals(m1.getStroke(), m2.getStroke());
    }

    @Test
    public void testSerializationWithNaN() {
        ValueMarker m1 = new ValueMarker(Double.NaN);
        ValueMarker m2 = null;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(m1);
            oos.flush();
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            m2 = (ValueMarker) ois.readObject();
        } catch (Exception e) {
            fail("Serialization with NaN failed: " + e.getMessage());
        }
        assertNotNull(m2);
        assertTrue(Double.isNaN(m2.getValue()));
    }

    @Test
    public void testSerializationWithInfinity() {
        ValueMarker m1 = new ValueMarker(Double.POSITIVE_INFINITY);
        ValueMarker m2 = null;
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(m1);
            oos.flush();
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            m2 = (ValueMarker) ois.readObject();
        } catch (Exception e) {
            fail("Serialization with Infinity failed: " + e.getMessage());
        }
        assertNotNull(m2);
        assertEquals(Double.POSITIVE_INFINITY, m2.getValue(), EPSILON);
    }

    @Test
    public void testListenerNotificationOnValueChange() {
        final boolean[] notified = {false};
        marker.addChangeListener(new MarkerChangeListener() {
            @Override
            public void markerChanged(MarkerChangeEvent event) {
                notified[0] = true;
                assertSame(marker, event.getMarker());
            }
        });
        marker.setValue(75.0);
        assertTrue("Listener should be notified on value change", notified[0]);
    }

    @Test
    public void testListenerNotificationOnSameValue() {
        final boolean[] notified = {false};
        marker.addChangeListener(new MarkerChangeListener() {
            @Override
            public void markerChanged(MarkerChangeEvent event) {
                notified[0] = true;
            }
        });
        marker.setValue(TEST_VALUE);
        assertFalse("Listener should not be notified when value unchanged", notified[0]);
    }

    @Test
    public void testMultipleListeners() {
        final int[] count = {0};
        MarkerChangeListener listener1 = new MarkerChangeListener() {
            @Override
            public void markerChanged(MarkerChangeEvent event) {
                count[0]++;
            }
        };
        MarkerChangeListener listener2 = new MarkerChangeListener() {
            @Override
            public void markerChanged(MarkerChangeEvent event) {
                count[0]++;
            }
        };
        marker.addChangeListener(listener1);
        marker.addChangeListener(listener2);
        marker.setValue(100.0);
        assertEquals(2, count[0]);
    }

    @Test
    public void testRemoveListener() {
        final boolean[] notified = {false};
        MarkerChangeListener listener = new MarkerChangeListener() {
            @Override
            public void markerChanged(MarkerChangeEvent event) {
                notified[0] = true;
            }
        };
        marker.addChangeListener(listener);
        marker.removeChangeListener(listener);
        marker.setValue(100.0);
        assertFalse("Listener should not be notified after removal", notified[0]);
    }

    @Test
    public void testConstructorWithNullPaint() {
        try {
            new ValueMarker(TEST_VALUE, null, new BasicStroke(1.0f));
            fail("Should throw IllegalArgumentException for null paint");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithNullStroke() {
        try {
            new ValueMarker(TEST_VALUE, Color.RED, null);
            fail("Should throw IllegalArgumentException for null stroke");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithNullPaintAndStroke() {
        try {
            new ValueMarker(TEST_VALUE, null, null);
            fail("Should throw IllegalArgumentException for null paint and stroke");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetValueWithNullListenerList() {
        // This tests the case where listenerList might be null (defensive)
        ValueMarker m = new ValueMarker(10.0);
        // Access private field via reflection to set listenerList to null
        try {
            java.lang.reflect.Field field = Marker.class.getDeclaredField("listenerList");
            field.setAccessible(true);
            field.set(m, null);
        } catch (Exception e) {
            // If reflection fails, skip this test
            return;
        }
        // This should not throw NullPointerException
        m.setValue(20.0);
        assertEquals(20.0, m.getValue(), EPSILON);
    }

    @Test
    public void testInheritedLabelMethods() {
        marker.setLabel("Test Label");
        assertEquals("Test Label", marker.getLabel());
        
        marker.setLabelFont(new Font("Serif", Font.BOLD, 14));
        assertNotNull(marker.getLabelFont());
        
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        assertEquals(LengthAdjustmentType.EXPAND, marker.getLabelOffsetType());
        
        marker.setLabelAnchor(RectangleAnchor.TOP);
        assertEquals(RectangleAnchor.TOP, marker.getLabelAnchor());
        
        marker.setLabelTextAnchor(TextAnchor.BASELINE_LEFT);
        assertEquals(TextAnchor.BASELINE_LEFT, marker.getLabelTextAnchor());
    }

    @Test
    public void testEqualsWithDifferentLabel() {
        ValueMarker m1 = new ValueMarker(TEST_VALUE);
        ValueMarker m2 = new ValueMarker(TEST_VALUE);
        m1.setLabel("Label1");
        m2.setLabel("Label2");
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsWithDifferentPaint() {
        ValueMarker m1 = new ValueMarker(TEST_VALUE, Color.RED, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(TEST_VALUE, Color.BLUE, new BasicStroke(1.0f));
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testEqualsWithDifferentStroke() {
        ValueMarker m1 = new ValueMarker(TEST_VALUE, Color.RED, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(TEST_VALUE, Color.RED, new BasicStroke(2.0f));
        assertFalse(m1.equals(m2));
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = marker.hashCode();
        marker.setValue(100.0);
        int hash2 = marker.hashCode();
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testToString() {
        String str = marker.toString();
        assertNotNull(str);
        assertTrue(str.contains("ValueMarker"));
        assertTrue(str.contains(String.valueOf(TEST_VALUE)));
    }
}