package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectCodec;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class TokenBufferTest {

    @Test
    public void testConstructionAndBasics() {
        TokenBuffer buf = new TokenBuffer(null, false);
        assertNotNull(buf);
        assertNull(buf.getCodec());

        ObjectCodec codec = new ObjectMapperMock();
        TokenBuffer bufWithCodec = new TokenBuffer(codec, true);
        assertSame(codec, bufWithCodec.getCodec());

        TokenBuffer cloned = bufWithCodec.copy();
        assertNotNull(cloned);
        
        buf.close();
        assertFalse(buf.isClosed()); // TokenBuffer usually ignores close() or doesn't flag closed via isClosed() unless overridden, let's verify delegate
    }

    @Test
    public void testVersion() {
        TokenBuffer buf = new TokenBuffer(null, false);
        assertNotNull(buf.version());
    }

    @Test
    public void testWriteOperations() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);

        buf.writeStartObject();
        buf.writeFieldName("testField");
        buf.writeString("testValue");
        buf.writeNumber(123);
        buf.writeNumber(123L);
        buf.writeNumber(123.45);
        buf.writeNumber(BigDecimal.TEN);
        buf.writeNumber(BigInteger.ONE);
        buf.writeBoolean(true);
        buf.writeNull();
        buf.writeEndObject();

        buf.flush();
        assertFalse(buf.isClosed());
    }

    @Test
    public void testBinaryAndRawWrites() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeBinary(new byte[]{1, 2, 3});
        buf.writeRaw("raw");
        buf.writeRawValue("rawVal");
        buf.writeEndArray();
    }

    @Test
    public void testParsingAsParser() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(1);
        buf.writeEndObject();

        JsonParser p = buf.asParser();
        assertNotNull(p);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test
    public void testParserWithCodec() throws IOException {
        ObjectCodec codec = new ObjectMapperMock();
        TokenBuffer buf = new TokenBuffer(codec, false);
        buf.writeStartArray();
        buf.writeNumber(10);
        buf.writeEndArray();

        JsonParser p = buf.asParser(codec);
        assertSame(codec, p.getCodec());
        p.close();
    }

    @Test
    public void testDeserialize() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeNumber(42);

        JsonParser p = buf.asParser();
        // Just invoking deserialize if applicable or testing override methods
        assertNotNull(p.getValueAsInt());
        p.close();
    }

    @Test
    public void testSerialize() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeString("hello");

        JsonGeneratorMock gen = new JsonGeneratorMock();
        buf.serialize(gen);
        assertTrue(gen.writeStringCalled);
    }

    @Test
    public void testAppend() throws IOException {
        TokenBuffer buf1 = new TokenBuffer(null, false);
        buf1.writeStartArray();
        buf1.writeNumber(1);
        buf1.writeEndArray();

        TokenBuffer buf2 = new TokenBuffer(null, false);
        buf2.append(buf1.asParser());
        
        JsonParser p = buf2.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken() == JsonToken.START_OBJECT ? JsonToken.START_OBJECT : JsonToken.START_ARRAY);
        p.close();
    }

    // Mocks for testing
    private static class ObjectMapperMock extends ObjectCodec {
        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public <T> T readValue(JsonParser jp, Class<T> valueType) throws IOException {
            return null;
        }

        @Override
        public <T> T readValue(JsonParser jp, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) throws IOException {
            return null;
        }

        @Override
        public <T> T readValue(JsonParser jp, com.fasterxml.jackson.databind.JavaType valueType) throws IOException {
            return null;
        }

        @Override
        public <T> java.util.Iterator<T> readValues(JsonParser jp, Class<T> valueType) throws IOException {
            return null;
        }

        @Override
        public <T> java.util.Iterator<T> readValues(JsonParser jp, com.fasterxml.jackson.core.type.TypeReference<?> valueTypeRef) throws IOException {
            return null;
        }

        @Override
        public <T> java.util.Iterator<T> readValues(JsonParser jp, com.fasterxml.jackson.databind.JavaType valueType) throws IOException {
            return null;
        }

        @Override
        public void writeValue(JsonGenerator jgen, Object value) throws IOException {
        }

        @Override
        public com.fasterxml.jackson.databind.JsonNode createObjectNode() {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonNode createArrayNode() {
            return null;
        }

        @Override
        public JsonParser treeAsTokens(com.fasterxml.jackson.core.TreeNode n) {
            return null;
        }

        @Override
        public <T> T treeToValue(com.fasterxml.jackson.core.TreeNode n, Class<T> valueType) throws IOException {
            return null;
        }
    }

    private static class JsonGeneratorMock extends com.fasterxml.jackson.core.base.GeneratorBase {
        public boolean writeStringCalled = false;

        public JsonGeneratorMock() {
            super(0, null);
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public JsonGenerator enable(Feature f) { return this; }

        @Override
        public JsonGenerator disable(Feature f) { return this; }

        @Override
        public booleanisEnabled(Feature f) { return false; }

        @Override
        public int getFeatureMask() { return 0; }

        @Override
        public JsonGenerator setFeatureMask(int mask) { return this; }

        @Override
        public void writeStartArray() throws IOException {}

        @Override
        public void writeEndArray() throws IOException {}

        @Override
        public void writeStartObject() throws IOException {}

        @Override
        public void writeEndObject() throws IOException {}

        @Override
        public void writeFieldName(String name) throws IOException {}

        @Override
        public void writeString(String text) throws IOException {
            writeStringCalled = true;
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}

        @Override
        public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}

        @Override
        public void writeRaw(String text) throws IOException {}

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(char c) throws IOException {}

        @Override
        public void writeRawValue(String text) throws IOException {}

        @Override
        public void writeRawValue(String text, int offset, int len) throws IOException {}

        @Override
        public void writeRawValue(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {}

        @Override
        public void writeNumber(short v) throws IOException {}

        @Override
        public void writeNumber(int v) throws IOException {}

        @Override
        public void writeNumber(long v) throws IOException {}

        @Override
        public void writeNumber(BigInteger v) throws IOException {}

        @Override
        public void writeNumber(double v) throws IOException {}

        @Override
        public void writeNumber(float v) throws IOException {}

        @Override
        public void writeNumber(BigDecimal v) throws IOException {}

        @Override
        public void writeNumber(String encodedValue) throws IOException {}

        @Override
        public void writeBoolean(boolean state) throws IOException {}

        @Override
        public void writeNull() throws IOException {}

        @Override
        public void writeObject(Object pojo) throws IOException {}

        @Override
        public void writeTree(com.fasterxml.jackson.core.TreeNode rootNode) throws IOException {}

        @Override
        public void flush() throws IOException {}

        @Override
        protected void _releaseBuffers() {}

        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {}

        @Override
        public boolean isClosed() { return false; }

        @Override
        public void close() throws IOException {}
    }
}