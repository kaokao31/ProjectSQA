package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 4 test suite for StdKeySerializer.
 * Designed to achieve maximum line/branch coverage and detect potential faults.
 */
public class StdKeySerializerTest {

    private StdKeySerializer serializer;
    private JsonGenerator gen;
    private SerializerProvider provider;

    @Before
    public void setUp() {
        serializer = new StdKeySerializer();
        gen = mock(JsonGenerator.class);
        provider = mock(SerializerProvider.class);
    }

    // ========== serialize method tests ==========

    @Test
    public void testSerializeNullKey() throws IOException {
        // Should write null for null key (potential fault: some implementations throw NPE)
        serializer.serialize(null, gen, provider);
        verify(gen).writeNull();
    }

    @Test
    public void testSerializeStringKey() throws IOException {
        String key = "testKey";
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName(key);
    }

    @Test
    public void testSerializeEmptyStringKey() throws IOException {
        String key = "";
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName("");
    }

    @Test
    public void testSerializeIntegerKey() throws IOException {
        Integer key = 42;
        serializer.serialize(key, gen, provider);
        // StdKeySerializer converts non-String keys via toString()
        verify(gen).writeFieldName("42");
    }

    @Test
    public void testSerializeLongKey() throws IOException {
        Long key = 123456789L;
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName("123456789");
    }

    @Test
    public void testSerializeBooleanKey() throws IOException {
        Boolean key = true;
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName("true");
    }

    @Test
    public void testSerializeDoubleKey() throws IOException {
        Double key = 3.14;
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName("3.14");
    }

    @Test
    public void testSerializeObjectKey() throws IOException {
        Object key = new Object() {
            @Override
            public String toString() {
                return "customObject";
            }
        };
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName("customObject");
    }

    @Test(expected = IOException.class)
    public void testSerializeThrowsIOException() throws IOException {
        // Simulate IOException from generator
        doThrow(new IOException("Generator error")).when(gen).writeFieldName(anyString());
        serializer.serialize("fail", gen, provider);
    }

    // ========== Edge cases and boundary tests ==========

    @Test
    public void testSerializeKeyWithSpecialCharacters() throws IOException {
        String key = "key with spaces and \n newline";
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName(key);
    }

    @Test
    public void testSerializeKeyWithUnicode() throws IOException {
        String key = "\u00e9\u00f1\u00fc";
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName(key);
    }

    @Test
    public void testSerializeVeryLongStringKey() throws IOException {
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 10000; i++) {
            sb.append('a');
        }
        String key = sb.toString();
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName(key);
    }

    @Test
    public void testSerializeNullProvider() throws IOException {
        // Provider is not used in StdKeySerializer, but test null safety
        serializer.serialize("key", gen, null);
        verify(gen).writeFieldName("key");
    }

    @Test(expected = NullPointerException.class)
    public void testSerializeNullGenerator() throws IOException {
        // Should throw NPE if generator is null
        serializer.serialize("key", null, provider);
    }

    // ========== Tests for potential fault: handling of null key ==========
    // Some versions of StdKeySerializer might throw NullPointerException for null key
    // This test verifies that null is handled correctly (writeNull)
    @Test
    public void testNullKeyDoesNotThrow() {
        try {
            serializer.serialize(null, gen, provider);
            verify(gen).writeNull();
        } catch (IOException e) {
            fail("IOException should not be thrown for null key");
        } catch (NullPointerException e) {
            fail("NullPointerException should not be thrown for null key");
        }
    }

    // ========== Additional coverage for any internal branches ==========
    @Test
    public void testSerializeByteArrayKey() throws IOException {
        // StdKeySerializer may handle byte[] by converting to String? Unlikely but test
        byte[] key = new byte[]{1, 2, 3};
        serializer.serialize(key, gen, provider);
        // toString() of byte[] is not meaningful, but it should still call writeFieldName
        verify(gen).writeFieldName(key.toString());
    }

    @Test
    public void testSerializeArrayKey() throws IOException {
        int[] key = new int[]{1, 2, 3};
        serializer.serialize(key, gen, provider);
        verify(gen).writeFieldName(key.toString());
    }
}