package org.apache.commons.lang3;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link org.apache.commons.lang3.SerializationUtils}.
 */
public class SerializationUtilsTest {

    static class TestClass implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final int value;

        TestClass(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestClass testClass = (TestClass) obj;
            return value == testClass.value && (name != null ? name.equals(testClass.name) : testClass.name == null);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + value;
            return result;
        }
    }

    static class NonSerializableClass {
        private final String name;

        NonSerializableClass(String name) {
            this.name = name;
        }
    }

    @Test
    public void testConstructor() {
        assertNotNull(new SerializationUtils());
        Constructor<?>[] constructors = SerializationUtils.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPublic(constructors[0].getModifiers()));
    }

    @Test
    public void testCloneNull() {
        Object cloned = SerializationUtils.clone(null);
        assertNull(cloned);
    }

    @Test
    public void testClonePrimitive() {
        Integer i = 12345;
        Integer clonedI = SerializationUtils.clone(i);
        assertEquals(i, clonedI);
        assertNotSame(i, clonedI);

        String s = "Hello World";
        String clonedS = SerializationUtils.clone(s);
        assertEquals(s, clonedS);
    }

    @Test
    public void testCloneComplexObject() {
        TestClass original = new TestClass("test", 42);
        TestClass cloned = SerializationUtils.clone(original);

        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals("test", cloned.getName());
        assertEquals(42, cloned.getValue());
    }

    @Test
    public void testCloneCollections() {
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");

        List<String> clonedList = SerializationUtils.clone((ArrayList<String>) list);
        assertNotNull(clonedList);
        assertNotSame(list, clonedList);
        assertEquals(list, clonedList);

        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("key1", 1);
        map.put("key2", 2);

        HashMap<String, Integer> clonedMap = SerializationUtils.clone((HashMap<String, Integer>) map);
        assertNotNull(clonedMap);
        assertNotSame(map, clonedMap);
        assertEquals(map, clonedMap);
    }

    @Test(expected = SerializationException.class)
    public void testCloneUnserializableSubclass() {
        class UnserializableHolder implements Serializable {
            private static final long serialVersionUID = 1L;
            private final NonSerializableClass nonSerializable = new NonSerializableClass("unserializable");
        }

        SerializationUtils.clone(new UnserializableHolder());
    }

    @Test
    public void testPrimitiveTypeClassSerialization() {
        Class<?>[] primitiveTypes = new Class<?>[] {
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
            char.class,
            void.class
        };

        for (Class<?> primitiveType : primitiveTypes) {
            Class<?> cloned = SerializationUtils.clone(primitiveType);
            assertSame("Failed to clone primitive type: " + primitiveType, primitiveType, cloned);
        }
    }

    @Test
    public void testPrimitiveArrayTypeClassSerialization() {
        Class<?>[] primitiveArrayTypes = new Class<?>[] {
            byte[].class,
            short[].class,
            int[].class,
            long[].class,
            float[].class,
            double[].class,
            boolean[].class,
            char[].class
        };

        for (Class<?> arrayType : primitiveArrayTypes) {
            Class<?> cloned = SerializationUtils.clone(arrayType);
            assertSame("Failed to clone primitive array type: " + arrayType, arrayType, cloned);
        }
    }

    @Test
    public void testSerializeStreamNull() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize(null, baos);
        byte[] bytes = baos.toByteArray();
        Object deserialized = SerializationUtils.deserialize(new ByteArrayInputStream(bytes));
        assertNull(deserialized);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeStreamNullStream() {
        SerializationUtils.serialize("test", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeNullStreamWithNullObject() {
        SerializationUtils.serialize(null, null);
    }

    @Test
    public void testSerializeStreamSuccess() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TestClass testObj = new TestClass("streamTest", 999);
        SerializationUtils.serialize(testObj, baos);

        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 0);

        Object deserialized = SerializationUtils.deserialize(new ByteArrayInputStream(bytes));
        assertEquals(testObj, deserialized);
    }

    @Test(expected = SerializationException.class)
    public void testSerializeStreamIOException() {
        OutputStream failingStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Simulated write error");
            }
        };

        SerializationUtils.serialize("test", failingStream);
    }

    @Test
    public void testSerializeBytesNull() {
        byte[] bytes = SerializationUtils.serialize(null);
        assertNotNull(bytes);
        Object deserialized = SerializationUtils.deserialize(bytes);
        assertNull(deserialized);
    }

    @Test
    public void testSerializeBytesSuccess() {
        TestClass testObj = new TestClass("byteTest", 777);
        byte[] bytes = SerializationUtils.serialize(testObj);
        assertNotNull(bytes);

        TestClass deserialized = (TestClass) SerializationUtils.deserialize(bytes);
        assertEquals(testObj, deserialized);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeStreamNull() {
        SerializationUtils.deserialize((InputStream) null);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeStreamCorruptData() {
        byte[] corruptBytes = new byte[] { 0, 1, 2, 3, 4, 5 };
        SerializationUtils.deserialize(new ByteArrayInputStream(corruptBytes));
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeStreamIOException() {
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated read error");
            }
        };

        SerializationUtils.deserialize(failingStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeBytesNull() {
        SerializationUtils.deserialize((byte[]) null);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeBytesCorruptData() {
        byte[] corruptBytes = new byte[] { (byte) 0xAC, (byte) 0xED, 0, 5, 1, 2 };
        SerializationUtils.deserialize(corruptBytes);
    }

    @Test
    public void testRoundtripSerializableTypes() {
        String testStr = "Serialization Roundtrip Test";
        byte[] serializedStr = SerializationUtils.serialize(testStr);
        String deserializedStr = (String) SerializationUtils.deserialize(serializedStr);
        assertEquals(testStr, deserializedStr);

        Integer testInt = 2023;
        byte[] serializedInt = SerializationUtils.serialize(testInt);
        Integer deserializedInt = (Integer) SerializationUtils.deserialize(serializedInt);
        assertEquals(testInt, deserializedInt);

        String[] testArray = new String[] { "a", "b", "c" };
        byte[] serializedArray = SerializationUtils.serialize(testArray);
        String[] deserializedArray = (String[]) SerializationUtils.deserialize(serializedArray);
        assertArrayEquals(testArray, deserializedArray);
    }
}