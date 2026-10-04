package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for NumberSerializer.
 * Designed to achieve high coverage and detect potential faults (e.g., handling of special values, null, edge cases).
 */
public class NumberSerializerTest {

    private NumberSerializer serializer;
    private JsonGenerator jgen;
    private SerializerProvider provider;
    private StringWriter stringWriter;

    @Before
    public void setUp() throws Exception {
        serializer = new NumberSerializer();
        stringWriter = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        jgen = mapper.getFactory().createGenerator(stringWriter);
        provider = mapper.getSerializerProvider();
    }

    // --- Basic number types ---

    @Test
    public void testSerializeInteger() throws IOException {
        serializer.serialize(42, jgen, provider);
        jgen.flush();
        assertEquals("42", stringWriter.toString());
    }

    @Test
    public void testSerializeLong() throws IOException {
        serializer.serialize(123456789012345L, jgen, provider);
        jgen.flush();
        assertEquals("123456789012345", stringWriter.toString());
    }

    @Test
    public void testSerializeDouble() throws IOException {
        serializer.serialize(3.14159, jgen, provider);
        jgen.flush();
        assertEquals("3.14159", stringWriter.toString());
    }

    @Test
    public void testSerializeFloat() throws IOException {
        serializer.serialize(2.5f, jgen, provider);
        jgen.flush();
        assertEquals("2.5", stringWriter.toString());
    }

    @Test
    public void testSerializeBigInteger() throws IOException {
        BigInteger big = new BigInteger("999999999999999999999999999999");
        serializer.serialize(big, jgen, provider);
        jgen.flush();
        assertEquals("999999999999999999999999999999", stringWriter.toString());
    }

    @Test
    public void testSerializeBigDecimal() throws IOException {
        BigDecimal bd = new BigDecimal("12345.67890123456789");
        serializer.serialize(bd, jgen, provider);
        jgen.flush();
        assertEquals("12345.67890123456789", stringWriter.toString());
    }

    // --- Edge cases: zero, negative, min/max ---

    @Test
    public void testSerializeZero() throws IOException {
        serializer.serialize(0, jgen, provider);
        jgen.flush();
        assertEquals("0", stringWriter.toString());
    }

    @Test
    public void testSerializeNegativeInteger() throws IOException {
        serializer.serialize(-100, jgen, provider);
        jgen.flush();
        assertEquals("-100", stringWriter.toString());
    }

    @Test
    public void testSerializeMaxInteger() throws IOException {
        serializer.serialize(Integer.MAX_VALUE, jgen, provider);
        jgen.flush();
        assertEquals(String.valueOf(Integer.MAX_VALUE), stringWriter.toString());
    }

    @Test
    public void testSerializeMinInteger() throws IOException {
        serializer.serialize(Integer.MIN_VALUE, jgen, provider);
        jgen.flush();
        assertEquals(String.valueOf(Integer.MIN_VALUE), stringWriter.toString());
    }

    @Test
    public void testSerializeMaxLong() throws IOException {
        serializer.serialize(Long.MAX_VALUE, jgen, provider);
        jgen.flush();
        assertEquals(String.valueOf(Long.MAX_VALUE), stringWriter.toString());
    }

    @Test
    public void testSerializeMinLong() throws IOException {
        serializer.serialize(Long.MIN_VALUE, jgen, provider);
        jgen.flush();
        assertEquals(String.valueOf(Long.MIN_VALUE), stringWriter.toString());
    }

    // --- Special double/float values (potential fault triggers) ---

    @Test
    public void testSerializeDoubleNaN() throws IOException {
        serializer.serialize(Double.NaN, jgen, provider);
        jgen.flush();
        // Jackson typically writes NaN as "NaN" (not quoted)
        assertEquals("NaN", stringWriter.toString());
    }

    @Test
    public void testSerializeDoublePositiveInfinity() throws IOException {
        serializer.serialize(Double.POSITIVE_INFINITY, jgen, provider);
        jgen.flush();
        assertEquals("Infinity", stringWriter.toString());
    }

    @Test
    public void testSerializeDoubleNegativeInfinity() throws IOException {
        serializer.serialize(Double.NEGATIVE_INFINITY, jgen, provider);
        jgen.flush();
        assertEquals("-Infinity", stringWriter.toString());
    }

    @Test
    public void testSerializeFloatNaN() throws IOException {
        serializer.serialize(Float.NaN, jgen, provider);
        jgen.flush();
        assertEquals("NaN", stringWriter.toString());
    }

    @Test
    public void testSerializeFloatPositiveInfinity() throws IOException {
        serializer.serialize(Float.POSITIVE_INFINITY, jgen, provider);
        jgen.flush();
        assertEquals("Infinity", stringWriter.toString());
    }

    @Test
    public void testSerializeFloatNegativeInfinity() throws IOException {
        serializer.serialize(Float.NEGATIVE_INFINITY, jgen, provider);
        jgen.flush();
        assertEquals("-Infinity", stringWriter.toString());
    }

    // --- Null handling ---

    @Test(expected = NullPointerException.class)
    public void testSerializeNullValue() throws IOException {
        // NumberSerializer may throw NPE if value is null (depends on implementation)
        serializer.serialize(null, jgen, provider);
    }

    // --- Subtypes of Number (e.g., AtomicInteger, AtomicLong, etc.) ---

    @Test
    public void testSerializeAtomicInteger() throws IOException {
        java.util.concurrent.atomic.AtomicInteger ai = new java.util.concurrent.atomic.AtomicInteger(77);
        serializer.serialize(ai, jgen, provider);
        jgen.flush();
        assertEquals("77", stringWriter.toString());
    }

    @Test
    public void testSerializeAtomicLong() throws IOException {
        java.util.concurrent.atomic.AtomicLong al = new java.util.concurrent.atomic.AtomicLong(8888888888L);
        serializer.serialize(al, jgen, provider);
        jgen.flush();
        assertEquals("8888888888", stringWriter.toString());
    }

    // --- BigDecimal with different scales ---

    @Test
    public void testSerializeBigDecimalWithScale() throws IOException {
        BigDecimal bd = new BigDecimal("0.001");
        serializer.serialize(bd, jgen, provider);
        jgen.flush();
        assertEquals("0.001", stringWriter.toString());
    }

    @Test
    public void testSerializeBigDecimalNegative() throws IOException {
        BigDecimal bd = new BigDecimal("-123.456");
        serializer.serialize(bd, jgen, provider);
        jgen.flush();
        assertEquals("-123.456", stringWriter.toString());
    }

    // --- Very large/small numbers ---

    @Test
    public void testSerializeVeryLargeDouble() throws IOException {
        double large = 1e308;
        serializer.serialize(large, jgen, provider);
        jgen.flush();
        assertEquals("1.0E308", stringWriter.toString());
    }

    @Test
    public void testSerializeVerySmallDouble() throws IOException {
        double small = 1e-308;
        serializer.serialize(small, jgen, provider);
        jgen.flush();
        assertEquals("1.0E-308", stringWriter.toString());
    }

    // --- Byte and Short (should be handled as integers) ---

    @Test
    public void testSerializeByte() throws IOException {
        byte b = 127;
        serializer.serialize(b, jgen, provider);
        jgen.flush();
        assertEquals("127", stringWriter.toString());
    }

    @Test
    public void testSerializeShort() throws IOException {
        short s = -32768;
        serializer.serialize(s, jgen, provider);
        jgen.flush();
        assertEquals("-32768", stringWriter.toString());
    }

    // --- Test with custom Number subclass (if any) ---

    @Test
    public void testSerializeCustomNumberSubclass() throws IOException {
        Number custom = new Number() {
            @Override
            public int intValue() { return 999; }
            @Override
            public long longValue() { return 999L; }
            @Override
            public float floatValue() { return 999.0f; }
            @Override
            public double doubleValue() { return 999.0; }
            @Override
            public String toString() { return "custom"; }
        };
        serializer.serialize(custom, jgen, provider);
        jgen.flush();
        // Typically Jackson uses doubleValue() for unknown Number subclasses
        assertEquals("999.0", stringWriter.toString());
    }

    // --- Test with ObjectMapper integration (to ensure full serialization path) ---

    @Test
    public void testIntegrationWithObjectMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(12345);
        assertEquals("12345", json);
    }

    @Test
    public void testIntegrationWithObjectMapperNaN() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Double.NaN);
        // Jackson may throw exception for NaN by default; but if not, check output
        // This test is to trigger potential fault
        assertNotNull(json);
    }

    // --- Additional edge: serialize with JsonGenerator in different state ---

    @Test
    public void testSerializeWithObjectWriteStartObject() throws IOException {
        // Simulate writing inside an object
        jgen.writeStartObject();
        jgen.writeFieldName("value");
        serializer.serialize(42, jgen, provider);
        jgen.writeEndObject();
        jgen.flush();
        assertEquals("{\"value\":42}", stringWriter.toString());
    }

    @Test
    public void testSerializeWithArrayContext() throws IOException {
        jgen.writeStartArray();
        serializer.serialize(1, jgen, provider);
        serializer.serialize(2, jgen, provider);
        jgen.writeEndArray();
        jgen.flush();
        assertEquals("[1,2]", stringWriter.toString());
    }
}