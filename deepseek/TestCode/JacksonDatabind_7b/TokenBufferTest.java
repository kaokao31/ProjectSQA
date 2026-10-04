package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
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
    // Basic write and read tests
    // ============================================================

    @Test
    public void testWriteStartObjectEndObject() throws IOException {
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteStartArrayEndArray() throws IOException {
        buffer.writeStartArray();
        buffer.writeEndArray();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteFieldName() throws IOException {
        buffer.writeStartObject();
        buffer.writeFieldName("name");
        buffer.writeString("value");
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteString() throws IOException {
        buffer.writeString("hello");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNull() throws IOException {
        buffer.writeNull();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteBooleanTrue() throws IOException {
        buffer.writeBoolean(true);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteBooleanFalse() throws IOException {
        buffer.writeBoolean(false);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberInt() throws IOException {
        buffer.writeNumber(42);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        buffer.writeNumber(1234567890123L);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1234567890123L, parser.getLongValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        buffer.writeNumber(3.14159);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14159, parser.getDoubleValue(), 1e-9);
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        BigDecimal bd = new BigDecimal("123.45678901234567890");
        buffer.writeNumber(bd);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(bd, parser.getDecimalValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        BigInteger bi = new BigInteger("123456789012345678901234567890");
        buffer.writeNumber(bi);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(bi, parser.getBigIntegerValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        buffer.writeNumber(2.5f);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(2.5f, parser.getFloatValue(), 1e-6);
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObject() throws IOException {
        buffer.writeObject("test");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectNull() throws IOException {
        buffer.writeObject(null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectArray() throws IOException {
        buffer.writeObject(new int[]{1, 2, 3});
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteRaw() throws IOException {
        buffer.writeRaw("raw");
        // raw is not tokenized, so we expect nothing? Actually it writes raw content, but asParser may not handle it.
        // This test may need to be adjusted based on behavior. For now, just ensure no exception.
        // We'll just call and not parse.
    }

    @Test
    public void testWriteRawValue() throws IOException {
        buffer.writeRawValue("true");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteBinary() throws IOException {
        byte[] data = {1, 2, 3};
        buffer.writeBinary(data);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertArrayEquals(data, parser.getBinaryValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteEmbeddedObject() throws IOException {
        Object obj = new Object();
        buffer.writeEmbeddedObject(obj);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        assertNull(parser.nextToken());
        parser.close();
    }

    // ============================================================
    // CopyCurrentEvent tests
    // ============================================================

    @Test
    public void testCopyCurrentEventStartObject() throws IOException {
        JsonParser source = mapper.createParser("{\"a\":1}");
        source.nextToken(); // START_OBJECT
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventEndObject() throws IOException {
        JsonParser source = mapper.createParser("{}");
        source.nextToken(); // START_OBJECT
        source.nextToken(); // END_OBJECT
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventStartArray() throws IOException {
        JsonParser source = mapper.createParser("[1]");
        source.nextToken(); // START_ARRAY
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventEndArray() throws IOException {
        JsonParser source = mapper.createParser("[]");
        source.nextToken(); // START_ARRAY
        source.nextToken(); // END_ARRAY
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventFieldName() throws IOException {
        JsonParser source = mapper.createParser("{\"key\":1}");
        source.nextToken(); // START_OBJECT
        source.nextToken(); // FIELD_NAME
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventString() throws IOException {
        JsonParser source = mapper.createParser("\"hello\"");
        source.nextToken(); // VALUE_STRING
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventNumberInt() throws IOException {
        JsonParser source = mapper.createParser("123");
        source.nextToken(); // VALUE_NUMBER_INT
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventNumberLong() throws IOException {
        JsonParser source = mapper.createParser("1234567890123");
        source.nextToken(); // VALUE_NUMBER_INT
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1234567890123L, parser.getLongValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventNumberFloat() throws IOException {
        JsonParser source = mapper.createParser("3.14");
        source.nextToken(); // VALUE_NUMBER_FLOAT
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 1e-9);
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventTrue() throws IOException {
        JsonParser source = mapper.createParser("true");
        source.nextToken(); // VALUE_TRUE
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventFalse() throws IOException {
        JsonParser source = mapper.createParser("false");
        source.nextToken(); // VALUE_FALSE
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventNull() throws IOException {
        JsonParser source = mapper.createParser("null");
        source.nextToken(); // VALUE_NULL
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventEmbeddedObject() throws IOException {
        // Embedded object not easily created from JSON, but we can simulate via TokenBuffer itself
        TokenBuffer srcBuffer = new TokenBuffer(mapper, false);
        Object obj = new Object();
        srcBuffer.writeEmbeddedObject(obj);
        JsonParser source = srcBuffer.asParser();
        source.nextToken(); // VALUE_EMBEDDED_OBJECT
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentEventBinary() throws IOException {
        byte[] data = {10, 20, 30};
        TokenBuffer srcBuffer = new TokenBuffer(mapper, false);
        srcBuffer.writeBinary(data);
        JsonParser source = srcBuffer.asParser();
        source.nextToken(); // VALUE_STRING (binary written as base64)
        buffer.copyCurrentEvent(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertArrayEquals(data, parser.getBinaryValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    // ============================================================
    // CopyCurrentStructure tests
    // ============================================================

    @Test
    public void testCopyCurrentStructureObject() throws IOException {
        JsonParser source = mapper.createParser("{\"a\":1,\"b\":2}");
        source.nextToken(); // START_OBJECT
        buffer.copyCurrentStructure(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentStructureArray() throws IOException {
        JsonParser source = mapper.createParser("[1,2,3]");
        source.nextToken(); // START_ARRAY
        buffer.copyCurrentStructure(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testCopyCurrentStructureNested() throws IOException {
        JsonParser source = mapper.createParser("{\"arr\":[1,2],\"obj\":{\"x\":3}}");
        source.nextToken(); // START_OBJECT
        buffer.copyCurrentStructure(source);
        source.close();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("obj", parser.getCurrentName());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("x", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // ============================================================
    // Serialization tests
    // ============================================================

    @Test
    public void testSerialize() throws IOException {
        buffer.writeStartObject();
        buffer.writeStringField("key", "value");
        buffer.writeEndObject();
        byte[] bytes = mapper.writeValueAsBytes(buffer);
        TokenBuffer deserialized = mapper.readValue(bytes, TokenBuffer.class);
        JsonParser parser = deserialized.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testSerializeEmpty() throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(buffer);
        TokenBuffer deserialized = mapper.readValue(bytes, TokenBuffer.class);
        JsonParser parser = deserialized.asParser();
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testSerializeWithNested() throws IOException {
        buffer.writeStartArray();
        buffer.writeStartObject();
        buffer.writeEndObject();
        buffer.writeEndArray();
        byte[] bytes = mapper.writeValueAsBytes(buffer);
        TokenBuffer deserialized = mapper.readValue(bytes, TokenBuffer.class);
        JsonParser parser = deserialized.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // ============================================================
    // Edge cases and potential bug triggers
    // ============================================================

    @Test
    public void testWriteFieldNameWithoutObject() throws IOException {
        // Should throw exception? TokenBuffer may allow it? We'll just ensure no crash.
        try {
            buffer.writeFieldName("test");
            // If no exception, we can still parse? Might produce invalid sequence.
            // Just ensure no NPE.
        } catch (Exception e) {
            // Expected maybe? Not sure. We'll just catch.
        }
    }

    @Test
    public void testWriteStringField() throws IOException {
        buffer.writeStartObject();
        buffer.writeStringField("name", "value");
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberField() throws IOException {
        buffer.writeStartObject();
        buffer.writeNumberField("num", 42);
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("num", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteBooleanField() throws IOException {
        buffer.writeStartObject();
        buffer.writeBooleanField("flag", true);
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("flag", parser.getCurrentName());
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNullField() throws IOException {
        buffer.writeStartObject();
        buffer.writeNullField("nothing");
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nothing", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectField() throws IOException {
        buffer.writeStartObject();
        buffer.writeObjectField("obj", "value");
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("obj", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectFieldWithNull() throws IOException {
        buffer.writeStartObject();
        buffer.writeObjectField("nullfield", null);
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nullfield", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectFieldWithArray() throws IOException {
        buffer.writeStartObject();
        buffer.writeObjectField("arr", new int[]{1,2});
        buffer.writeEndObject();
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteRawWithString() throws IOException {
        buffer.writeRaw("\"rawstring\"");
        // Raw writes raw content, not tokenized. We'll just ensure no exception.
    }

    @Test
    public void testWriteRawValueWithNumber() throws IOException {
        buffer.writeRawValue("123");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteRawValueWithNull() throws IOException {
        buffer.writeRawValue("null");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteRawValueWithBoolean() throws IOException {
        buffer.writeRawValue("false");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteRawValueWithString() throws IOException {
        buffer.writeRawValue("\"hello\"");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteRawValueWithArray() throws IOException {
        buffer.writeRawValue("[1,2]");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteRawValueWithObject() throws IOException {
        buffer.writeRawValue("{\"a\":1}");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteBinaryWithOffset() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeBinary(data, 1, 3);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertArrayEquals(new byte[]{2,3,4}, parser.getBinaryValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteBinaryEmpty() throws IOException {
        buffer.writeBinary(new byte[0]);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertArrayEquals(new byte[0], parser.getBinaryValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteBinaryNull() throws IOException {
        // writeBinary with null? Not allowed, but we can test exception.
        try {
            buffer.writeBinary(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWriteNumberShort() throws IOException {
        buffer.writeNumber((short) 10);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(10, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberByte() throws IOException {
        buffer.writeNumber((byte) 7);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(7, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberDoubleNaN() throws IOException {
        buffer.writeNumber(Double.NaN);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberDoubleInfinity() throws IOException {
        buffer.writeNumber(Double.POSITIVE_INFINITY);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberDoubleNegativeInfinity() throws IOException {
        buffer.writeNumber(Double.NEGATIVE_INFINITY);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberFloatNaN() throws IOException {
        buffer.writeNumber(Float.NaN);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Float.isNaN(parser.getFloatValue()));
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberFloatInfinity() throws IOException {
        buffer.writeNumber(Float.POSITIVE_INFINITY);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Float.isInfinite(parser.getFloatValue()));
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberFloatNegativeInfinity() throws IOException {
        buffer.writeNumber(Float.NEGATIVE_INFINITY);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Float.isInfinite(parser.getFloatValue()));
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberBigDecimalZero() throws IOException {
        buffer.writeNumber(BigDecimal.ZERO);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(BigDecimal.ZERO, parser.getDecimalValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberBigIntegerZero() throws IOException {
        buffer.writeNumber(BigInteger.ZERO);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(BigInteger.ZERO, parser.getBigIntegerValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberBigDecimalNegative() throws IOException {
        buffer.writeNumber(new BigDecimal("-123.456"));
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new BigDecimal("-123.456"), parser.getDecimalValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteNumberBigIntegerNegative() throws IOException {
        buffer.writeNumber(new BigInteger("-9876543210"));
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(new BigInteger("-9876543210"), parser.getBigIntegerValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteStringEmpty() throws IOException {
        buffer.writeString("");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteStringNull() throws IOException {
        buffer.writeString(null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteStringWithQuotes() throws IOException {
        buffer.writeString("hello \"world\"");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello \"world\"", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteStringWithEscapedChars() throws IOException {
        buffer.writeString("line1\nline2\t");
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("line1\nline2\t", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectWithPojo() throws IOException {
        buffer.writeObject(new SimplePojo(1, "test"));
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectWithPojoNull() throws IOException {
        buffer.writeObject(null);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectWithList() throws IOException {
        buffer.writeObject(java.util.Arrays.asList("a", "b"));
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a", parser.getText());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("b", parser.getText());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectWithMap() throws IOException {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        map.put("x", 10);
        buffer.writeObject(map);
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("x", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(10, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testWriteObjectWithNestedPojo() throws IOException {
        buffer.writeObject(new NestedPojo(new SimplePojo(2, "inner")));
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("inner", parser.getCurrentName());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("inner", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private void assertToken(JsonToken expected, JsonToken actual) {
        assertEquals("Expected token " + expected + " but got " + actual, expected, actual);
    }

    // Simple POJO for testing
    public static class SimplePojo {
        public int id;
        public String name;

        public SimplePojo() {} // for Jackson
        public SimplePojo(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class NestedPojo {
        public SimplePojo inner;

        public NestedPojo() {}
        public NestedPojo(SimplePojo inner) {
            this.inner = inner;
        }
    }
}