package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class TokenBufferTest {

    private ObjectMapper mapper;
    private TokenBuffer buffer;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        buffer = new TokenBuffer(mapper, false);
    }

    // ============================================================
    // Basic token writing and reading
    // ============================================================

    @Test
    public void testEmptyBuffer() throws IOException {
        JsonParser p = buffer.asParser();
        assertNull("Empty buffer should have no tokens", p.nextToken());
        p.close();
    }

    @Test
    public void testWriteStartObjectEndObject() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testWriteStartArrayEndArray() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testWriteFieldName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("name");
        buffer.writeString("value");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteString() throws IOException {
        buffer.writeString("hello");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        p.close();
    }

    @Test
    public void testWriteNumberInt() throws IOException {
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        p.close();
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        buffer.writeNumber(1234567890123L);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1234567890123L, p.getLongValue());
        p.close();
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        buffer.writeNumber(3.14f);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14f, p.getFloatValue(), 0.0001);
        p.close();
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        buffer.writeNumber(2.71828);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.71828, p.getDoubleValue(), 0.00001);
        p.close();
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        BigDecimal bd = new BigDecimal("12345678901234567890.123456789");
        buffer.writeNumber(bd);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(bd, p.getDecimalValue());
        p.close();
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        BigInteger bi = new BigInteger("123456789012345678901234567890");
        buffer.writeNumber(bi);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(bi, p.getBigIntegerValue());
        p.close();
    }

    @Test
    public void testWriteBooleanTrue() throws IOException {
        buffer.writeBoolean(true);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
        p.close();
    }

    @Test
    public void testWriteBooleanFalse() throws IOException {
        buffer.writeBoolean(false);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
        p.close();
    }

    @Test
    public void testWriteNull() throws IOException {
        buffer.writeNull();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteStringArray() throws IOException {
        buffer.writeStartArray();
        buffer.writeString("a");
        buffer.writeString("b");
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("b", p.getText());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteNestedObject() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("outer");
        buffer.writeStartObject();
        buffer.writeFieldName("inner");
        buffer.writeString("value");
        buffer.writeEndObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("outer", p.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("inner", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // ============================================================
    // WriteObject / WriteTree
    // ============================================================

    @Test
    public void testWriteObjectPojo() throws IOException {
        SimplePojo pojo = new SimplePojo("test", 123);
        buffer.writeObject(pojo);
        JsonParser p = buffer.asParser();
        // Should produce a JSON object with fields "name" and "value"
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("test", p.getText());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("value", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteObjectNull() throws IOException {
        buffer.writeObject(null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteTree() throws IOException {
        ObjectNode root = mapper.createObjectNode();
        root.put("key", "value");
        buffer.writeTree(root);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testWriteTreeNull() throws IOException {
        buffer.writeTree(null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    // ============================================================
    // Copy operations
    // ============================================================

    @Test
    public void testCopyCurrentEvent() throws IOException {
        // Write a simple structure, then copy from a parser
        String json = "{\"a\":1}";
        JsonParser source = mapper.getFactory().createParser(json);
        source.nextToken(); // START_OBJECT
        buffer.copyCurrentEvent(source);
        source.nextToken(); // FIELD_NAME
        buffer.copyCurrentEvent(source);
        source.nextToken(); // VALUE_NUMBER_INT
        buffer.copyCurrentEvent(source);
        source.nextToken(); // END_OBJECT
        buffer.copyCurrentEvent(source);
        source.close();

        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testCopyCurrentStructure() throws IOException {
        String json = "{\"arr\":[1,2]}";
        JsonParser source = mapper.getFactory().createParser(json);
        source.nextToken(); // START_OBJECT
        buffer.copyCurrentStructure(source);
        source.close();

        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arr", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // ============================================================
    // Serialization round-trip
    // ============================================================

    @Test
    public void testSerializeTokenBuffer() throws IOException {
        buffer.writeStartObject();
        buffer.writeStringField("x", "y");
        buffer.writeEndObject();
        String json = mapper.writeValueAsString(buffer);
        assertEquals("{\"x\":\"y\"}", json);
    }

    @Test
    public void testDeserializeTokenBuffer() throws IOException {
        buffer.writeStartObject();
        buffer.writeStringField("a", "b");
        buffer.writeEndObject();
        String json = mapper.writeValueAsString(buffer);
        TokenBuffer result = mapper.readValue(json, TokenBuffer.class);
        JsonParser p = result.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("b", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // ============================================================
    // Edge cases and special values
    // ============================================================

    @Test
    public void testWriteEmptyString() throws IOException {
        buffer.writeString("");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        p.close();
    }

    @Test
    public void testWriteNaN() throws IOException {
        buffer.writeNumber(Double.NaN);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
        p.close();
    }

    @Test
    public void testWriteInfinity() throws IOException {
        buffer.writeNumber(Double.POSITIVE_INFINITY);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isInfinite(p.getDoubleValue()));
        assertTrue(p.getDoubleValue() > 0);
        p.close();
    }

    @Test
    public void testWriteNegativeInfinity() throws IOException {
        buffer.writeNumber(Double.NEGATIVE_INFINITY);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isInfinite(p.getDoubleValue()));
        assertTrue(p.getDoubleValue() < 0);
        p.close();
    }

    @Test
    public void testWriteVeryLargeNumber() throws IOException {
        BigInteger huge = new BigInteger("1" + "0".repeat(100));
        buffer.writeNumber(huge);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(huge, p.getBigIntegerValue());
        p.close();
    }

    @Test
    public void testWriteVerySmallDouble() throws IOException {
        double tiny = Double.MIN_VALUE;
        buffer.writeNumber(tiny);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(tiny, p.getDoubleValue(), 0.0);
        p.close();
    }

    // ============================================================
    // Exception handling
    // ============================================================

    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldNameWithoutObject() throws IOException {
        buffer.writeFieldName("shouldFail");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteEndObjectWithoutStart() throws IOException {
        buffer.writeEndObject();
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteEndArrayWithoutStart() throws IOException {
        buffer.writeEndArray();
    }

    // ============================================================
    // Parser navigation
    // ============================================================

    @Test
    public void testParserSkipChildren() throws IOException {
        buffer.writeStartObject();
        buffer.writeStringField("a", "b");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.skipChildren();
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test
    public void testParserGetCurrentToken() throws IOException {
        buffer.writeString("test");
        JsonParser p = buffer.asParser();
        assertNull(p.getCurrentToken());
        p.nextToken();
        assertEquals(JsonToken.VALUE_STRING, p.getCurrentToken());
        p.close();
    }

    // ============================================================
    // Helper class
    // ============================================================

    static class SimplePojo {
        public String name;
        public int value;

        public SimplePojo() {}

        public SimplePojo(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}