package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class TokenBufferTest {

    private ObjectMapper objectMapper;
    private TokenBuffer tokenBuffer;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        tokenBuffer = new TokenBuffer(objectMapper, false);
    }

    @After
    public void tearDown() throws IOException {
        if (tokenBuffer != null) {
            tokenBuffer.close();
        }
    }

    @Test
    public void testVersion() {
        assertNotNull(tokenBuffer.version());
    }

    @Test
    public void testDelegate() {
        assertNull(tokenBuffer.getCodec());
        ObjectCodec codec = new ObjectMapper();
        TokenBuffer tbWithCodec = new TokenBuffer(codec, false);
        assertSame(codec, tbWithCodec.getCodec());
    }

    @Test
    public void testCreateWithSerializationConfig() {
        SerializationConfig config = objectMapper.getSerializationConfig();
        TokenBuffer tb = new TokenBuffer(config, true);
        assertTrue(tb.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        
        TokenBuffer tb2 = new TokenBuffer(config, false);
        assertFalse(tb2.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
    }

    @Test
    public void testOverrideParentNames() {
        tokenBuffer.overrideCurrentName("parentName");
        // Just ensuring it doesn't throw and covers the method
    }

    @Test
    public void testCopyCurrentEvent() throws IOException {
        JsonParser p = objectMapper.getFactory().createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        
        tokenBuffer.copyCurrentEvent(p);
        assertEquals(JsonToken.START_OBJECT, tokenBuffer.asParser().nextToken());
        
        p.close();
    }

    @Test
    public void testCopyCurrentStructure() throws IOException {
        JsonParser p = objectMapper.getFactory().createParser("{\"a\":1}");
        tokenBuffer.copyCurrentStructure(p);
        
        JsonParser bp = tokenBuffer.asParser();
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("a", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, bp.nextToken());
        assertEquals(1, bp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        
        p.close();
    }

    @Test
    public void testWriteBasicTypes() throws IOException {
        tokenBuffer.writeStartObject();
        tokenBuffer.writeFieldName("stringField");
        tokenBuffer.writeString("testValue");
        tokenBuffer.writeFieldName("intField");
        tokenBuffer.writeNumber(123);
        tokenBuffer.writeFieldName("longField");
        tokenBuffer.writeNumber(123L);
        tokenBuffer.writeFieldName("doubleField");
        tokenBuffer.writeNumber(123.45);
        tokenBuffer.writeFieldName("floatField");
        tokenBuffer.writeNumber(123.45f);
        tokenBuffer.writeFieldName("bigDecimalField");
        tokenBuffer.writeNumber(BigDecimal.TEN);
        tokenBuffer.writeFieldName("bigIntegerField");
        tokenBuffer.writeNumber(BigInteger.ONE);
        tokenBuffer.writeFieldName("nullField");
        tokenBuffer.writeNull();
        tokenBuffer.writeFieldName("boolTrue");
        tokenBuffer.writeBoolean(true);
        tokenBuffer.writeFieldName("boolFalse");
        tokenBuffer.writeBoolean(false);
        tokenBuffer.writeEndObject();

        JsonParser p = tokenBuffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("stringField", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("testValue", p.getText());
        
        // Skip through rest
        while (p.nextToken() != null) {
            // just consume
        }
    }

    @Test
    public void testWriteBinary() throws IOException {
        tokenBuffer.writeStartArray();
        byte[] data = new byte[]{1, 2, 3, 4};
        tokenBuffer.writeBinary(data);
        tokenBuffer.writeEndArray();

        JsonParser p = tokenBuffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, p.getBinaryValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testWriteRawValues() throws IOException {
        tokenBuffer.writeStartObject();
        tokenBuffer.writeFieldName("raw");
        tokenBuffer.writeRaw("123");
        tokenBuffer.writeFieldName("rawWithOffset");
        tokenBuffer.writeRaw("abc", 0, 3);
        tokenBuffer.writeFieldName("rawChar");
        tokenBuffer.writeRaw('X');
        tokenBuffer.writeFieldName("rawValue");
        tokenBuffer.writeRawValue("456");
        tokenBuffer.writeFieldName("rawValueOffset");
        tokenBuffer.writeRawValue("789abc", 0, 3);
        tokenBuffer.writeFieldName("rawValueChar");
        tokenBuffer.writeRawValue('Y');
        tokenBuffer.writeEndObject();

        JsonParser p = tokenBuffer.asParser();
        assertNotNull(p);
    }

    @Test
    public void testAppend() throws IOException {
        tokenBuffer.writeNumber(42);
        
        TokenBuffer tb2 = new TokenBuffer(objectMapper, false);
        tb2.append(tokenBuffer);
        
        JsonParser p = tb2.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        tb2.close();
    }

    @Test
    public void testDeserialize() throws IOException {
        tokenBuffer.writeNumber(100);
        DeserializationContext ctxt = objectMapper.getDeserializationContext();
        Integer val = tokenBuffer.deserialize(ctxt, Integer.class);
        assertEquals(Integer.valueOf(100), val);
    }

    @Test
    public void testParserMethods() throws IOException {
        tokenBuffer.writeStartObject();
        tokenBuffer.writeFieldName("name");
        tokenBuffer.writeString("Jackson");
        tokenBuffer.writeEndObject();

        JsonParser p = tokenBuffer.asParser();
        assertNull(p.getCurrentLocation());
        assertNull(p.getTokenLocation());
        assertNull(p.getEmbeddedObject());
        
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals("name", p.getText());
        
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Jackson", p.getText());
        assertEquals(7, p.getTextCharacters().length);
        assertEquals(0, p.getTextOffset());
        assertEquals(7, p.getTextLength());
        
        assertTrue(p.hasTextCharacters());
        
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        
        p.close();
        assertTrue(p.isClosed());
    }

    @Test
    public void testParserNumberParsing() throws IOException {
        tokenBuffer.writeStartArray();
        tokenBuffer.writeNumber(10);
        tokenBuffer.writeNumber(10L);
        tokenBuffer.writeNumber(10.5f);
        tokenBuffer.writeNumber(10.5);
        tokenBuffer.writeNumber(BigDecimal.valueOf(20.5));
        tokenBuffer.writeNumber(BigInteger.valueOf(30));
        tokenBuffer.writeEndArray();

        JsonParser p = tokenBuffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        assertEquals(10, p.getIntValue());
        assertEquals(10L, p.getLongValue());
        assertEquals(10.5f, p.getFloatValue(), 0.001f);
        assertEquals(10.5, p.getDoubleValue(), 0.001);
        assertEquals(BigDecimal.TEN, p.getDecimalValue());
        assertEquals(10, p.getNumberValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testParserScope() throws IOException {
        tokenBuffer.writeStartArray();
        tokenBuffer.writeStartObject();
        tokenBuffer.writeEndObject();
        tokenBuffer.writeEndArray();

        JsonParser p = tokenBuffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertTrue(p.getParsingContext().inArray());
        
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertTrue(p.getParsingContext().inObject());
        
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testForceUseOfBigDecimal() throws IOException {
        TokenBuffer tb = new TokenBuffer(objectMapper, true);
        tb.writeNumber(10.5);
        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());
        tb.close();
    }
}