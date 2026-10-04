package org.apache.commons.lang3;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for SerializationUtils.
 * Designed to achieve high coverage and reveal the Defects4J bug #13.
 */
public class SerializationUtilsTest {

    private static final String TEST_STRING = "Hello, world!";
    private static final List<String> TEST_LIST = Arrays.asList("a", "b", "c");

    private byte[] serializedString;
    private byte[] serializedList;

    @Before
    public void setUp() throws Exception {
        serializedString = SerializationUtils.serialize(TEST_STRING);
        serializedList = SerializationUtils.serialize((Serializable) TEST_LIST);
    }

    // ============================
    // Test serialize and deserialize byte[] methods
    // ============================

    @Test
    public void testSerializeNonNull() {
        byte[] data = SerializationUtils.serialize(TEST_STRING);
        assertNotNull("Serialized data should not be null", data);
        assertTrue("Serialized data should have positive length", data.length > 0);
    }

    @Test(expected = NullPointerException.class)
    public void testSerializeNullObject() {
        SerializationUtils.serialize((Serializable) null);
    }

    @Test
    public void testDeserializeNonNull() {
        String deserialized = SerializationUtils.deserialize(serializedString);
        assertEquals(TEST_STRING, deserialized);
    }

    @Test(expected = NullPointerException.class)
    public void testDeserializeNullByteArray() {
        SerializationUtils.deserialize((byte[]) null);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeInvalidData() {
        SerializationUtils.deserialize(new byte[]{0, 1, 2, 3, 4});
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeTruncatedData() {
        byte[] truncated = Arrays.copyOf(serializedString, serializedString.length / 2);
        SerializationUtils.deserialize(truncated);
    }

    @Test
    public void testSerializeDeserializeRoundTrip() {
        byte[] data = SerializationUtils.serialize(TEST_LIST);
        List<?> result = SerializationUtils.deserialize(data);
        assertEquals(TEST_LIST, result);
    }

    // ============================
    // Test serialize(OutputStream) and deserialize(InputStream)
    // ============================

    @Test
    public void testSerializeToStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SerializationUtils.serialize(TEST_STRING, bos);
        byte[] data = bos.toByteArray();
        assertTrue("Serialized data should have positive length", data.length > 0);
    }

    @Test(expected = NullPointerException.class)
    public void testSerializeToNullStream() {
        SerializationUtils.serialize(TEST_STRING, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSerializeNullToStream() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SerializationUtils.serialize(null, bos);
    }

    @Test
    public void testDeserializeFromStream() {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializedString);
        String result = SerializationUtils.deserialize(bis);
        assertEquals(TEST_STRING, result);
    }

    @Test(expected = NullPointerException.class)
    public void testDeserializeFromNullStream() {
        SerializationUtils.deserialize((java.io.InputStream) null);
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeFromInvalidStream() {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[]{0, 1, 2});
        SerializationUtils.deserialize(bis);
    }

    // ============================
    // Test clone method
    // ============================

    @Test
    public void testCloneString() {
        String clone = SerializationUtils.clone(TEST_STRING);
        assertNotNull("Clone should not be null", clone);
        assertEquals(TEST_STRING, clone);
        assertNotSame("Should be a different object", TEST_STRING, clone);
    }

    @Test
    public void testCloneArrayList() {
        ArrayList<String> list = new ArrayList<>(TEST_LIST);
        @SuppressWarnings("unchecked")
        ArrayList<String> clone = SerializationUtils.clone(list);
        assertNotNull(clone);
        assertEquals(list, clone);
        assertNotSame(list, clone);
        // Modify original to ensure deep copy
        list.add("d");
        assertFalse("Clone should not be affected", clone.contains("d"));
    }

    @Test(expected = NullPointerException.class)
    public void testCloneNull() {
        SerializationUtils.clone(null);
    }

    @Test(expected = SerializationException.class)
    public void testCloneNonSerializable() {
        // Object that is not Serializable
        Object nonSerializable = new Object();
        SerializationUtils.clone(nonSerializable);
    }

    // ============================
    // Test primitive type Class serialization (Defects4J bug #13)
    // ============================

    @Test(expected = SerializationException.class)
    public void testPrimitiveTypeClassSerialization() {
        // This test reproduces the failing test from Defects4J
        // Primitive type classes (like int.class) cause ClassNotFoundException on deserialization
        Class<?> primitiveClass = int.class;
        SerializationUtils.clone(primitiveClass);
    }

    @Test(expected = SerializationException.class)
    public void testPrimitiveTypeVoidClassSerialization() {
        SerializationUtils.clone(void.class);
    }

    @Test(expected = SerializationException.class)
    public void testPrimitiveTypeArrayClassSerialization() {
        // Primitive array classes might also cause issues
        SerializationUtils.clone(int[].class);
    }

    // ============================
    // Test edge cases for Class objects (non-primitive should work)
    // ============================

    @Test
    public void testNonPrimitiveClassSerialization() {
        Class<?> clazz = String.class;
        Class<?> cloned = SerializationUtils.clone(clazz);
        assertEquals(clazz, cloned);
        assertNotSame(clazz, cloned);
    }

    @Test
    public void testClassArraySerialization() {
        Class<?> clazz = String[].class;
        Class<?> cloned = SerializationUtils.clone(clazz);
        assertEquals(clazz, cloned);
    }

    // ============================
    // Test serialization of null bytes and empty arrays
    // ============================

    @Test
    public void testRoundTripEmptyArray() {
        String[] empty = new String[0];
        byte[] data = SerializationUtils.serialize(empty);
        String[] result = SerializationUtils.deserialize(data);
        assertArrayEquals(empty, result);
    }

    @Test
    public void testRoundTripArrayWithNulls() {
        String[] withNulls = {"a", null, "c"};
        byte[] data = SerializationUtils.serialize(withNulls);
        String[] result = SerializationUtils.deserialize(data);
        assertArrayEquals(withNulls, result);
    }

    // ============================
    // Test Constructor (private, should not be accessible)
    // ============================

    @Test
    public void testConstructor() throws Exception {
        java.lang.reflect.Constructor<SerializationUtils> c = SerializationUtils.class.getDeclaredConstructor();
        assertFalse("Constructor should not be accessible", c.isAccessible());
        c.setAccessible(true);
        SerializationUtils instance = c.newInstance();
        assertNotNull(instance);
        c.setAccessible(false);
    }

    // ============================
    // Additional coverage for exception handling
    // ============================

    @Test(expected = SerializationException.class)
    public void testDeserializeFromStreamWithIOException() {
        // Simulate a stream that throws IOException during read
        ByteArrayInputStream bis = new ByteArrayInputStream(serializedString) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("Simulated read failure");
            }
        };
        SerializationUtils.deserialize(bis);
    }

    @Test(expected = SerializationException.class)
    public void testSerializeToStreamWithIOException() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream() {
            @Override
            public void write(byte[] b, int off, int len) {
                throw new RuntimeException("Simulated write failure");
            }
        };
        SerializationUtils.serialize(TEST_STRING, bos);
    }

    @Test(expected = SerializationException.class)
    public void testCloneWithNonSerializableException() {
        // An object that implements Serializable but throws NotSerializableException during serialization (unlikely in practice)
        // We use a custom class to force an error during cloning
        class CustomObject implements Serializable {
            private void writeObject(java.io.ObjectOutputStream out) throws IOException {
                throw new NotSerializableException("Forced failure");
            }
        }
        SerializationUtils.clone(new CustomObject());
    }

    // ============================
    // Test enum serialization (enums are special)
    // ============================

    @Test
    public void testEnumSerialization() {
        SampleEnum original = SampleEnum.VALUE1;
        byte[] data = SerializationUtils.serialize(original);
        SampleEnum result = SerializationUtils.deserialize(data);
        assertSame("Enum should be the same singleton", original, result);
    }

    private enum SampleEnum {
        VALUE1, VALUE2
    }
}