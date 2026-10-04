package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for StdKeyDeserializer.
 * Designed to achieve maximum code coverage and detect potential faults,
 * including the bug addressed in Defects4J JacksonDatabind bug 71.
 */
public class StdKeyDeserializerTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        // Create a minimal DeserializationContext for direct deserializeKey calls
        JsonParser jp = mapper.getFactory().createParser("{}");
        jp.nextToken(); // advance to START_OBJECT
        ctxt = mapper.getDeserializationContext(jp);
    }

    // -----------------------------------------------------------------------
    // Tests for String key deserialization
    // -----------------------------------------------------------------------
    @Test
    public void testStringKeyValid() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(String.class);
        Object result = deser.deserializeKey("hello", ctxt);
        assertEquals("hello", result);
    }

    @Test
    public void testStringKeyEmpty() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(String.class);
        Object result = deser.deserializeKey("", ctxt);
        assertEquals("", result);
    }

    @Test(expected = MismatchedInputException.class)
    public void testStringKeyNull() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(String.class);
        deser.deserializeKey(null, ctxt);
    }

    // -----------------------------------------------------------------------
    // Tests for Integer key deserialization
    // -----------------------------------------------------------------------
    @Test
    public void testIntegerKeyValid() throws Exception {
        Map<Integer, String> result = mapper.readValue(
                "{\"42\":\"value\"}",
                new TypeReference<Map<Integer, String>>() {});
        assertEquals(Integer.valueOf(42), result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testIntegerKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"abc\":\"value\"}",
                new TypeReference<Map<Integer, String>>() {});
    }

    @Test(expected = InvalidFormatException.class)
    public void testIntegerKeyEmpty() throws Exception {
        mapper.readValue(
                "{\"\":\"value\"}",
                new TypeReference<Map<Integer, String>>() {});
    }

    @Test(expected = MismatchedInputException.class)
    public void testIntegerKeyNull() throws Exception {
        // Convert a map with a null key to force deserializeKey(null, ...)
        Map<String, String> source = new HashMap<>();
        source.put(null, "value");
        mapper.convertValue(source, new TypeReference<Map<Integer, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for Long key deserialization
    // -----------------------------------------------------------------------
    @Test
    public void testLongKeyValid() throws Exception {
        Map<Long, String> result = mapper.readValue(
                "{\"1234567890123\":\"value\"}",
                new TypeReference<Map<Long, String>>() {});
        assertEquals(Long.valueOf(1234567890123L), result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testLongKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"notALong\":\"value\"}",
                new TypeReference<Map<Long, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for Double key deserialization
    // -----------------------------------------------------------------------
    @Test
    public void testDoubleKeyValid() throws Exception {
        Map<Double, String> result = mapper.readValue(
                "{\"3.14\":\"value\"}",
                new TypeReference<Map<Double, String>>() {});
        assertEquals(Double.valueOf(3.14), result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testDoubleKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"notADouble\":\"value\"}",
                new TypeReference<Map<Double, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for Boolean key deserialization
    // -----------------------------------------------------------------------
    @Test
    public void testBooleanKeyTrue() throws Exception {
        Map<Boolean, String> result = mapper.readValue(
                "{\"true\":\"value\"}",
                new TypeReference<Map<Boolean, String>>() {});
        assertEquals(Boolean.TRUE, result.keySet().iterator().next());
    }

    @Test
    public void testBooleanKeyFalse() throws Exception {
        Map<Boolean, String> result = mapper.readValue(
                "{\"false\":\"value\"}",
                new TypeReference<Map<Boolean, String>>() {});
        assertEquals(Boolean.FALSE, result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testBooleanKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"notBoolean\":\"value\"}",
                new TypeReference<Map<Boolean, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for Date key deserialization (ISO-8601 format)
    // -----------------------------------------------------------------------
    @Test
    public void testDateKeyValid() throws Exception {
        Map<Date, String> result = mapper.readValue(
                "{\"1970-01-01T00:00:00.000+00:00\":\"value\"}",
                new TypeReference<Map<Date, String>>() {});
        assertEquals(new Date(0), result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testDateKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"notADate\":\"value\"}",
                new TypeReference<Map<Date, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for Currency key deserialization (Defects4J bug 71 related)
    // -----------------------------------------------------------------------
    @Test
    public void testCurrencyKeyValid() throws Exception {
        Map<Currency, String> result = mapper.readValue(
                "{\"USD\":\"value\"}",
                new TypeReference<Map<Currency, String>>() {});
        assertEquals(Currency.getInstance("USD"), result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testCurrencyKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"XYZ\":\"value\"}",
                new TypeReference<Map<Currency, String>>() {});
    }

    @Test(expected = MismatchedInputException.class)
    public void testCurrencyKeyNull() throws Exception {
        Map<String, String> source = new HashMap<>();
        source.put(null, "value");
        mapper.convertValue(source, new TypeReference<Map<Currency, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for UUID key deserialization
    // -----------------------------------------------------------------------
    @Test
    public void testUUIDKeyValid() throws Exception {
        UUID uuid = UUID.randomUUID();
        Map<UUID, String> result = mapper.readValue(
                "{\"" + uuid.toString() + "\":\"value\"}",
                new TypeReference<Map<UUID, String>>() {});
        assertEquals(uuid, result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testUUIDKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"notAUUID\":\"value\"}",
                new TypeReference<Map<UUID, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for URL key deserialization
    // -----------------------------------------------------------------------
    @Test
    public void testURLKeyValid() throws Exception {
        Map<java.net.URL, String> result = mapper.readValue(
                "{\"http://example.com\":\"value\"}",
                new TypeReference<Map<java.net.URL, String>>() {});
        assertEquals(new java.net.URL("http://example.com"), result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testURLKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"notAURL\":\"value\"}",
                new TypeReference<Map<java.net.URL, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for URI key deserialization
    // -----------------------------------------------------------------------
    @Test
    public void testURIKeyValid() throws Exception {
        Map<java.net.URI, String> result = mapper.readValue(
                "{\"http://example.com/path\":\"value\"}",
                new TypeReference<Map<java.net.URI, String>>() {});
        assertEquals(java.net.URI.create("http://example.com/path"), result.keySet().iterator().next());
    }

    @Test(expected = InvalidFormatException.class)
    public void testURIKeyInvalid() throws Exception {
        mapper.readValue(
                "{\"notAURI\":\"value\"}",
                new TypeReference<Map<java.net.URI, String>>() {});
    }

    // -----------------------------------------------------------------------
    // Tests for getType() method
    // -----------------------------------------------------------------------
    @Test
    public void testGetTypeForString() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(String.class);
        assertEquals(String.class, deser.getType());
    }

    @Test
    public void testGetTypeForInteger() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Integer.class);
        assertEquals(Integer.class, deser.getType());
    }

    @Test
    public void testGetTypeForCurrency() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Currency.class);
        assertEquals(Currency.class, deser.getType());
    }

    // -----------------------------------------------------------------------
    // Edge case: empty JSON object (no keys)
    // -----------------------------------------------------------------------
    @Test
    public void testEmptyMap() throws Exception {
        Map<String, String> result = mapper.readValue(
                "{}",
                new TypeReference<Map<String, String>>() {});
        assertTrue(result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Edge case: multiple keys of different types (via ObjectMapper)
    // -----------------------------------------------------------------------
    @Test
    public void testMultipleIntegerKeys() throws Exception {
        Map<Integer, String> result = mapper.readValue(
                "{\"1\":\"a\",\"2\":\"b\",\"3\":\"c\"}",
                new TypeReference<Map<Integer, String>>() {});
        assertEquals(3, result.size());
        assertTrue(result.containsKey(1));
        assertTrue(result.containsKey(2));
        assertTrue(result.containsKey(3));
    }

    // -----------------------------------------------------------------------
    // Direct deserializeKey calls for types not easily tested via JSON
    // (e.g., null key, empty string for numeric types)
    // -----------------------------------------------------------------------
    @Test(expected = MismatchedInputException.class)
    public void testDirectNullKeyForInteger() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Integer.class);
        deser.deserializeKey(null, ctxt);
    }

    @Test(expected = InvalidFormatException.class)
    public void testDirectEmptyKeyForInteger() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Integer.class);
        deser.deserializeKey("", ctxt);
    }

    @Test(expected = InvalidFormatException.class)
    public void testDirectInvalidKeyForInteger() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Integer.class);
        deser.deserializeKey("12.34", ctxt);
    }

    @Test(expected = MismatchedInputException.class)
    public void testDirectNullKeyForCurrency() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Currency.class);
        deser.deserializeKey(null, ctxt);
    }

    @Test(expected = InvalidFormatException.class)
    public void testDirectEmptyKeyForCurrency() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Currency.class);
        deser.deserializeKey("", ctxt);
    }

    @Test(expected = InvalidFormatException.class)
    public void testDirectInvalidKeyForCurrency() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Currency.class);
        deser.deserializeKey("INVALID", ctxt);
    }

    // -----------------------------------------------------------------------
    // Test that deserializeKey returns correct type for all supported types
    // -----------------------------------------------------------------------
    @Test
    public void testReturnTypeForString() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(String.class);
        assertTrue(deser.deserializeKey("test", ctxt) instanceof String);
    }

    @Test
    public void testReturnTypeForInteger() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Integer.class);
        assertTrue(deser.deserializeKey("123", ctxt) instanceof Integer);
    }

    @Test
    public void testReturnTypeForLong() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Long.class);
        assertTrue(deser.deserializeKey("123", ctxt) instanceof Long);
    }

    @Test
    public void testReturnTypeForDouble() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Double.class);
        assertTrue(deser.deserializeKey("3.14", ctxt) instanceof Double);
    }

    @Test
    public void testReturnTypeForBoolean() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Boolean.class);
        assertTrue(deser.deserializeKey("true", ctxt) instanceof Boolean);
    }

    @Test
    public void testReturnTypeForDate() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Date.class);
        assertTrue(deser.deserializeKey("1970-01-01T00:00:00.000+00:00", ctxt) instanceof Date);
    }

    @Test
    public void testReturnTypeForCurrency() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Currency.class);
        assertTrue(deser.deserializeKey("USD", ctxt) instanceof Currency);
    }

    @Test
    public void testReturnTypeForUUID() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(UUID.class);
        assertTrue(deser.deserializeKey(UUID.randomUUID().toString(), ctxt) instanceof UUID);
    }

    @Test
    public void testReturnTypeForURL() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(java.net.URL.class);
        assertTrue(deser.deserializeKey("http://example.com", ctxt) instanceof java.net.URL);
    }

    @Test
    public void testReturnTypeForURI() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(java.net.URI.class);
        assertTrue(deser.deserializeKey("http://example.com", ctxt) instanceof java.net.URI);
    }
}