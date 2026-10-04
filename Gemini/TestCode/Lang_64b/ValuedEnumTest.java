package org.apache.commons.lang.enums;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ValuedEnum}.
 */
public class ValuedEnumTest {

    // Concrete test subclass 1
    public static final class ValuedColorEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final ValuedColorEnum RED = new ValuedColorEnum("Red", 1);
        public static final ValuedColorEnum GREEN = new ValuedColorEnum("Green", 2);
        public static final ValuedColorEnum BLUE = new ValuedColorEnum("Blue", 3);

        private ValuedColorEnum(String name, int value) {
            super(name, value);
        }

        public static ValuedColorEnum getEnum(int value) {
            return (ValuedColorEnum) getEnum(ValuedColorEnum.class, value);
        }

        public static Map getEnumMap() {
            return getEnumMap(ValuedColorEnum.class);
        }

        public static List getEnumList() {
            return getEnumList(ValuedColorEnum.class);
        }

        public static Iterator iterator() {
            return iterator(ValuedColorEnum.class);
        }
    }

    // Concrete test subclass 2
    public static final class ValuedOtherEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final ValuedOtherEnum FIRST = new ValuedOtherEnum("First", 1);
        public static final ValuedOtherEnum SECOND = new ValuedOtherEnum("Second", 2);

        private ValuedOtherEnum(String name, int value) {
            super(name, value);
        }

        public static ValuedOtherEnum getEnum(int value) {
            return (ValuedOtherEnum) getEnum(ValuedOtherEnum.class, value);
        }
    }

    // Concrete test subclass with negative and zero values
    public static final class ValuedBoundaryEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final ValuedBoundaryEnum MIN = new ValuedBoundaryEnum("Min", Integer.MIN_VALUE);
        public static final ValuedBoundaryEnum NEGATIVE = new ValuedBoundaryEnum("Negative", -10);
        public static final ValuedBoundaryEnum ZERO = new ValuedBoundaryEnum("Zero", 0);
        public static final ValuedBoundaryEnum MAX = new ValuedBoundaryEnum("Max", Integer.MAX_VALUE);

        private ValuedBoundaryEnum(String name, int value) {
            super(name, value);
        }
    }

    @Test
    public void testGetValue() {
        assertEquals(1, ValuedColorEnum.RED.getValue());
        assertEquals(2, ValuedColorEnum.GREEN.getValue());
        assertEquals(3, ValuedColorEnum.BLUE.getValue());
    }

    @Test
    public void testGetName() {
        assertEquals("Red", ValuedColorEnum.RED.getName());
        assertEquals("Green", ValuedColorEnum.GREEN.getName());
        assertEquals("Blue", ValuedColorEnum.BLUE.getName());
    }

    @Test
    public void testGetEnum() {
        assertSame(ValuedColorEnum.RED, ValuedColorEnum.getEnum(1));
        assertSame(ValuedColorEnum.GREEN, ValuedColorEnum.getEnum(2));
        assertSame(ValuedColorEnum.BLUE, ValuedColorEnum.getEnum(3));
        assertNull(ValuedColorEnum.getEnum(4));
        assertNull(ValuedColorEnum.getEnum(-1));
    }

    @Test
    public void testGetEnum_Class() {
        assertSame(ValuedColorEnum.RED, ValuedEnum.getEnum(ValuedColorEnum.class, 1));
        assertSame(ValuedColorEnum.GREEN, ValuedEnum.getEnum(ValuedColorEnum.class, 2));
        assertSame(ValuedColorEnum.BLUE, ValuedEnum.getEnum(ValuedColorEnum.class, 3));
        assertNull(ValuedEnum.getEnum(ValuedColorEnum.class, 99));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnum_NullClass() {
        ValuedEnum.getEnum(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnum_NonEnumClass() {
        ValuedEnum.getEnum(String.class, 1);
    }

    @Test
    public void testGetEnumMap() {
        Map map = ValuedColorEnum.getEnumMap();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertSame(ValuedColorEnum.RED, map.get("Red"));
        assertSame(ValuedColorEnum.GREEN, map.get("Green"));
        assertSame(ValuedColorEnum.BLUE, map.get("Blue"));
    }

    @Test
    public void testGetEnumList() {
        List list = ValuedColorEnum.getEnumList();
        assertNotNull(list);
        assertEquals(3, list.size());
        assertSame(ValuedColorEnum.RED, list.get(0));
        assertSame(ValuedColorEnum.GREEN, list.get(1));
        assertSame(ValuedColorEnum.BLUE, list.get(2));
    }

    @Test
    public void testIterator() {
        Iterator it = ValuedColorEnum.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertSame(ValuedColorEnum.RED, it.next());
        assertTrue(it.hasNext());
        assertSame(ValuedColorEnum.GREEN, it.next());
        assertTrue(it.hasNext());
        assertSame(ValuedColorEnum.BLUE, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testCompareTo_sameObject() {
        assertEquals(0, ValuedColorEnum.RED.compareTo(ValuedColorEnum.RED));
        assertEquals(0, ValuedColorEnum.GREEN.compareTo(ValuedColorEnum.GREEN));
        assertEquals(0, ValuedColorEnum.BLUE.compareTo(ValuedColorEnum.BLUE));
    }

    @Test
    public void testCompareTo_sameEnumType() {
        assertTrue(ValuedColorEnum.RED.compareTo(ValuedColorEnum.GREEN) < 0);
        assertTrue(ValuedColorEnum.GREEN.compareTo(ValuedColorEnum.RED) > 0);
        assertTrue(ValuedColorEnum.RED.compareTo(ValuedColorEnum.BLUE) < 0);
        assertTrue(ValuedColorEnum.BLUE.compareTo(ValuedColorEnum.RED) > 0);
        assertTrue(ValuedColorEnum.GREEN.compareTo(ValuedColorEnum.BLUE) < 0);
        assertTrue(ValuedColorEnum.BLUE.compareTo(ValuedColorEnum.GREEN) > 0);
    }

    @Test(expected = ClassCastException.class)
    public void testCompareTo_otherEnumType() {
        ValuedColorEnum.RED.compareTo(ValuedOtherEnum.FIRST);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareTo_null() {
        ValuedColorEnum.RED.compareTo(null);
    }

    @Test(expected = ClassCastException.class)
    public void testCompareTo_nonEnum() {
        ValuedColorEnum.RED.compareTo("Not an Enum");
    }

    @Test
    public void testBoundaryValues() {
        assertSame(ValuedBoundaryEnum.MIN, ValuedEnum.getEnum(ValuedBoundaryEnum.class, Integer.MIN_VALUE));
        assertSame(ValuedBoundaryEnum.NEGATIVE, ValuedEnum.getEnum(ValuedBoundaryEnum.class, -10));
        assertSame(ValuedBoundaryEnum.ZERO, ValuedEnum.getEnum(ValuedBoundaryEnum.class, 0));
        assertSame(ValuedBoundaryEnum.MAX, ValuedEnum.getEnum(ValuedBoundaryEnum.class, Integer.MAX_VALUE));

        assertTrue(ValuedBoundaryEnum.MIN.compareTo(ValuedBoundaryEnum.MAX) < 0);
        assertTrue(ValuedBoundaryEnum.MAX.compareTo(ValuedBoundaryEnum.MIN) > 0);
        assertTrue(ValuedBoundaryEnum.NEGATIVE.compareTo(ValuedBoundaryEnum.ZERO) < 0);
        assertTrue(ValuedBoundaryEnum.ZERO.compareTo(ValuedBoundaryEnum.NEGATIVE) > 0);
    }

    @Test
    public void testToString() {
        String redStr = ValuedColorEnum.RED.toString();
        assertNotNull(redStr);
        assertTrue(redStr.contains("Red"));
        assertTrue(redStr.contains("1"));
        assertTrue(redStr.contains(ValuedColorEnum.class.getName()));
    }

    @Test
    public void testEqualsAndHashCode() {
        assertSame(ValuedColorEnum.RED, ValuedColorEnum.RED);
        assertEquals(ValuedColorEnum.RED, ValuedColorEnum.RED);
        assertNotEquals(ValuedColorEnum.RED, ValuedColorEnum.GREEN);
        assertNotEquals(ValuedColorEnum.RED, ValuedOtherEnum.FIRST);
        assertNotEquals(ValuedColorEnum.RED, null);
        assertNotEquals(ValuedColorEnum.RED, "Red");

        assertEquals(ValuedColorEnum.RED.hashCode(), ValuedColorEnum.RED.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ValuedColorEnum.RED);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertSame(ValuedColorEnum.RED, deserialized);
    }

    private static void assertNotEquals(Object o1, Object o2) {
        if (o1 == null) {
            assertNotNull(o2);
        } else {
            assertFalse(o1.equals(o2));
        }
    }
}