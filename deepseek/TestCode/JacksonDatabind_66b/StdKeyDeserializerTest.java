package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StdKeyDeserializerTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{}");
        parser.nextToken(); // advance to END_OBJECT
        ctxt = mapper.getDeserializationConfig().createContext(parser, null);
    }

    // --- String key deserializer ---
    @Test
    public void testStringKeyDeserializer() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.STRING_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("test", ctxt);
        assertEquals("test", result);
    }

    @Test
    public void testStringKeyDeserializerEmpty() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.STRING_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("", ctxt);
        assertEquals("", result);
    }

    @Test
    public void testStringKeyDeserializerNull() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.STRING_KEY_DESERIALIZER;
        Object result = deser.deserializeKey(null, ctxt);
        assertNull(result);
    }

    // --- Integer key deserializer ---
    @Test
    public void testIntegerKeyDeserializer() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.INT_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("123", ctxt);
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testIntegerKeyDeserializerNegative() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.INT_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("-456", ctxt);
        assertEquals(Integer.valueOf(-456), result);
    }

    @Test
    public void testIntegerKeyDeserializerMax() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.INT_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("2147483647", ctxt);
        assertEquals(Integer.valueOf(2147483647), result);
    }

    @Test
    public void testIntegerKeyDeserializerMin() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.INT_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("-2147483648", ctxt);
        assertEquals(Integer.valueOf(-2147483648), result);
    }

    @Test(expected = Exception.class)
    public void testIntegerKeyDeserializerOverflow() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.INT_KEY_DESERIALIZER;
        deser.deserializeKey("2147483648", ctxt);
    }

    @Test(expected = Exception.class)
    public void testIntegerKeyDeserializerInvalid() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.INT_KEY_DESERIALIZER;
        deser.deserializeKey("abc", ctxt);
    }

    // --- Long key deserializer ---
    @Test
    public void testLongKeyDeserializer() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.LONG_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("789", ctxt);
        assertEquals(Long.valueOf(789), result);
    }

    @Test
    public void testLongKeyDeserializerNegative() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.LONG_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("-123456789012345", ctxt);
        assertEquals(Long.valueOf(-123456789012345L), result);
    }

    @Test(expected = Exception.class)
    public void testLongKeyDeserializerInvalid() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.LONG_KEY_DESERIALIZER;
        deser.deserializeKey("not_a_long", ctxt);
    }

    // --- Double key deserializer ---
    @Test
    public void testDoubleKeyDeserializer() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.DOUBLE_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("3.14", ctxt);
        assertEquals(Double.valueOf(3.14), result);
    }

    @Test
    public void testDoubleKeyDeserializerNegative() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.DOUBLE_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("-2.5e10", ctxt);
        assertEquals(Double.valueOf(-2.5e10), result);
    }

    @Test(expected = Exception.class)
    public void testDoubleKeyDeserializerInvalid() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.DOUBLE_KEY_DESERIALIZER;
        deser.deserializeKey("NaN", ctxt);
    }

    // --- Boolean key deserializer ---
    @Test
    public void testBooleanKeyDeserializerTrue() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.BOOLEAN_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("true", ctxt);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testBooleanKeyDeserializerFalse() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.BOOLEAN_KEY_DESERIALIZER;
        Object result = deser.deserializeKey("false", ctxt);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(expected = Exception.class)
    public void testBooleanKeyDeserializerInvalid() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.BOOLEAN_KEY_DESERIALIZER;
        deser.deserializeKey("yes", ctxt);
    }

    // --- Type mismatch and edge cases ---
    @Test(expected = Exception.class)
    public void testTypeMismatchIntAsBoolean() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.INT_KEY_DESERIALIZER;
        deser.deserializeKey("true", ctxt);
    }

    @Test(expected = Exception.class)
    public void testTypeMismatchBooleanAsInt() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.BOOLEAN_KEY_DESERIALIZER;
        deser.deserializeKey("123", ctxt);
    }

    @Test
    public void testForTypeString() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(String.class);
        assertSame(StdKeyDeserializer.STRING_KEY_DESERIALIZER, deser);
    }

    @Test
    public void testForTypeInteger() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Integer.class);
        assertSame(StdKeyDeserializer.INT_KEY_DESERIALIZER, deser);
    }

    @Test
    public void testForTypeLong() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Long.class);
        assertSame(StdKeyDeserializer.LONG_KEY_DESERIALIZER, deser);
    }

    @Test
    public void testForTypeDouble() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Double.class);
        assertSame(StdKeyDeserializer.DOUBLE_KEY_DESERIALIZER, deser);
    }

    @Test
    public void testForTypeBoolean() throws Exception {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Boolean.class);
        assertSame(StdKeyDeserializer.BOOLEAN_KEY_DESERIALIZER, deser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTypeUnsupported() throws Exception {
        StdKeyDeserializer.forType(Float.class);
    }
}