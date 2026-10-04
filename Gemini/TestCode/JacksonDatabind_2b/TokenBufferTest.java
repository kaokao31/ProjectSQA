package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
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

    @Test
    public void testEmptyBuffer() throws IOException {
        JsonParser jp = tokenBuffer.asParser();
        assertNull(jp.nextToken());
        jp.close();
    }

    @Test
    public void testBasicWritingAndParsing() throws IOException {
        tokenBuffer.writeStartObject();
        tokenBuffer.writeStringField("name", "Jackson");
        tokenBuffer.writeNumberField("age", 10);
        tokenBuffer.writeBooleanField("active", true);
        tokenBuffer.writeNullField("nullField");
        tokenBuffer.writeEndObject();

        JsonParser jp = tokenBuffer.asParser();

        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("name", jp.getCurrentName());
        assertEquals("Jackson", jp.getText());

        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("Jackson", jp.getText());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("age", jp.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(10, jp.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("active", jp.getCurrentName());

        assertEquals(JsonToken.VALUE_TRUE, jp.nextToken());
        assertTrue(jp.getBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("nullField", jp.getCurrentName());

        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());

        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        assertNull(jp.nextToken());

        jp.close();
    }

    @Test
    public void testArrayWritingAndParsing() throws IOException {
        tokenBuffer.writeStartArray();
        tokenBuffer.writeNumber(1);
        tokenBuffer.writeNumber(2L);
        tokenBuffer.writeNumber(3.0f);
        tokenBuffer.writeNumber(4.0d);
        tokenBuffer.writeNumber(BigInteger.valueOf(5L));
        tokenBuffer.writeNumber(BigDecimal.valueOf(6.0));
        tokenBuffer.writeEndArray();

        JsonParser jp = tokenBuffer.asParser();

        assertEquals(JsonToken.START_ARRAY, jp.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(1, jp.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(2L, jp.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertEquals(3.0f, jp.getFloatValue(), 0.001f);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertEquals(4.0d, jp.getDoubleValue(), 0.001d);

        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(BigInteger.valueOf(5L), jp.getBigIntegerValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertEquals(BigDecimal.valueOf(6.0), jp.getDecimalValue());

        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        assertNull(jp.nextToken());

        jp.close();
    }

    @Test
    public void testBinaryAndEmbeddedObject() throws IOException {
        tokenBuffer.writeStartObject();
        tokenBuffer.writeFieldName("binary");
        byte[] data = new byte[]{1, 2, 3, 4};
        tokenBuffer.writeBinary(data);

        tokenBuffer.writeFieldName("embedded");
        tokenBuffer.writeEmbeddedObject("customObject");
        tokenBuffer.writeEndObject();

        JsonParser jp = tokenBuffer.asParser();
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("binary", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, jp.nextToken());
        assertArrayEquals(data, jp.getBinaryValue());

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("embedded", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, jp.nextToken());
        assertEquals("customObject", jp.getEmbeddedObject());

        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

    @Test
    public void testCopyCurrentEvent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.createParser("{\"a\":1}");
        
        tokenBuffer.writeStartObject();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        tokenBuffer.copyCurrentEvent(parser);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        tokenBuffer.copyCurrentEvent(parser);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        tokenBuffer.copyCurrentEvent(parser);

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        tokenBuffer.copyCurrentEvent(parser);

        tokenBuffer.writeEndObject();
        parser.close();

        JsonParser jp = tokenBuffer.asParser();
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("a", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(1, jp.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(1, jp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

    @Test
    public void testCopyCurrentStructure() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.createParser("{\"arr\":[1,2], \"obj\":{\"b\":true}}");
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        tokenBuffer.copyCurrentStructure(parser);
        parser.close();

        JsonParser jp = tokenBuffer.asParser();
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("arr", jp.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("obj", jp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("b", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, jp.nextToken());
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

    @Test
    public void testSerialize() throws IOException {
        tokenBuffer.writeStartObject();
        tokenBuffer.writeStringField("hello", "world");
        tokenBuffer.writeEndObject();

        TokenBuffer tb2 = new TokenBuffer(objectMapper, false);
        tb2.writeStartArray();
        tokenBuffer.serialize(tb2);
        tb2.writeEndArray();

        JsonParser jp = tb2.asParser();
        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("hello", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("world", jp.getText());
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        assertEquals(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

    @Test
    public void testParserMethodsEdgeCases() throws IOException {
        tokenBuffer.writeBoolean(false);
        tokenBuffer.writeNull();
        tokenBuffer.writeRaw("raw");
        tokenBuffer.writeRawValue("rawVal");
        tokenBuffer.writeString("str");

        JsonParser jp = tokenBuffer.asParser();

        assertEquals(JsonToken.VALUE_FALSE, jp.nextToken());
        assertFalse(jp.getBooleanValue());
        assertEquals("false", jp.getText());

        assertEquals(JsonToken.VALUE_NULL, jp.nextToken());
        assertNull(jp.getText());

        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, jp.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, jp.nextToken());
        
        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("str", jp.getText());
        char[] chars = new char[3];
        assertEquals(3, jp.getTextCharacters(chars, 0, 3));
        assertEquals(3, jp.getTextLength());
        assertEquals(0, jp.getTextOffset());
        assertNotNull(jp.getSourceLocation());
        assertNotNull(jp.getParsingContext());

        jp.close();
    }

    @Test
    public void testParserNumberParsingVariations() throws IOException {
        tokenBuffer.writeNumber(100L);
        tokenBuffer.writeNumber(10.5);
        tokenBuffer.writeNumber(Short.valueOf((short) 5));
        tokenBuffer.writeNumber(Byte.valueOf((byte) 2));
        tokenBuffer.writeNumber(Float.valueOf(1.1f));

        JsonParser jp = tokenBuffer.asParser();

        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(JsonParser.NumberType.LONG, jp.getNumberType());
        assertEquals(100L, jp.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, jp.getNumberType());
        assertEquals(10.5, jp.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(5, jp.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(2, jp.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
        assertEquals(1.1f, jp.getFloatValue(), 0.001f);

        jp.close();
    }

    @Test
    public void testAppend() throws IOException {
        tokenBuffer.writeStartObject();
        TokenBuffer tb2 = new TokenBuffer(objectMapper, false);
        tb2.writeStringField("key", "val");
        
        tokenBuffer.append(tb2);
        tokenBuffer.writeEndObject();

        JsonParser jp = tokenBuffer.asParser();
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        assertEquals("key", jp.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        assertEquals("val", jp.getText());
        assertEquals(JsonToken.END_OBJECT, jp.nextToken());
        jp.close();
    }

    @Test
    public void testVersionAndCodec() {
        assertNotNull(tokenBuffer.version());
        assertNotNull(tokenBuffer.getCodec());
        tokenBuffer.setCodec(objectMapper);
        assertSame(objectMapper, tokenBuffer.getCodec());
    }

    @Test
    public void testDelegateTypeSerializer() throws IOException {
        tokenBuffer.writeStartObject();
        tokenBuffer.writeStringField("a", "b");
        tokenBuffer.writeEndObject();

        JsonGenerator delegate = objectMapper.getFactory().createGenerator(new java.io.StringWriter());
        tokenBuffer.serialize(delegate);
        delegate.close();
    }
}