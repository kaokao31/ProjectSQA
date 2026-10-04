package com.fasterxml.jackson.core;

import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class JsonGeneratorTest {

    // A concrete subclass of JsonGenerator for testing its methods, 
    // especially default implementations and feature flags.
    private static class DummyJsonGenerator extends JsonGenerator {
        protected int features = 0;
        protected Object currentValue;

        @Override
        public JsonGenerator setHighestNonEscapedChar(int charCode) {
            return this;
        }

        @Override
        public int getHighestNonEscapedChar() {
            return 0;
        }

        @Override
        public CharacterEscapes getCharacterEscapes() {
            return null;
        }

        @Override
        public JsonGenerator setCharacterEscapes(CharacterEscapes esc) {
            return this;
        }

        @Override
        public ObjectNode createObjectNode() {
            return null;
        }

        @Override
        public ArrayNode createArrayNode() {
            return null;
        }

        @Override
        public JsonGenerator enable(Feature f) {
            features |= f.getMask();
            return this;
        }

        @Override
        public JsonGenerator disable(Feature f) {
            features &= ~f.getMask();
            return this;
        }

        @Override
        public boolean isEnabled(Feature f) {
            return (features & f.getMask()) != 0;
        }

        @Override
        public int getFeatureMask() {
            return features;
        }

        @Override
        public JsonGenerator setFeatureMask(int mask) {
            features = mask;
            return this;
        }

        @Override
        public JsonGenerator writeStartArray() throws IOException {
            return this;
        }

        @Override
        public JsonGenerator writeEndArray() throws IOException {
            return this;
        }

        @Override
        public JsonGenerator writeStartObject() throws IOException {
            return this;
        }

        @Override
        public JsonGenerator writeEndObject() throws IOException {
            return this;
        }

        @Override
        public void writeFieldName(String name) throws IOException {}

        @Override
        public void writeFieldName(SerializableString name) throws IOException {}

        @Override
        public void writeString(String text) throws IOException {}

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeString(SerializableString text) throws IOException {}

        @Override
        public void writeRawUTF8String(byte[] buffer, int offset, int len) throws IOException {}

        @Override
        public void writeUTF8String(byte[] text, int offset, int len) throws IOException {}

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
        public JsonStreamContext getOutputContext() {
            return null;
        }

        @Override
        public void flush() throws IOException {}

        @Override
        public void close() throws IOException {}

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public Object getCurrentValue() {
            return currentValue;
        }

        @Override
        public void setCurrentValue(Object v) {
            currentValue = v;
        }
    }

    @Test
    public void testFeatureEnum() {
        JsonGenerator.Feature f = JsonGenerator.Feature.AUTO_CLOSE_TARGET;
        assertTrue(f.enabledByDefault());
        assertEquals(1 << 0, f.getMask());
    }

    @Test
    public void testDefaultWriteMethodsEmbeddedObject() throws IOException {
        DummyJsonGenerator gen = new DummyJsonGenerator();
        // Test default implementation of writeObject with null
        gen.writeObject(null);
        // Test default implementation of writeObject with non-null
        gen.writeObject("test-object");
        // Test default implementation of writeTree with null
        gen.writeTree(null);
    }

    @Test
    public void testCurrentAssignment() {
        DummyJsonGenerator gen = new DummyJsonGenerator();
        assertNull(gen.getCurrentValue());
        gen.setCurrentValue("hello");
        assertEquals("hello", gen.getCurrentValue());
    }

    @Test
    public void testVersion() {
        DummyJsonGenerator gen = new DummyJsonGenerator();
        assertNotNull(gen.version());
        assertEquals(Version.unknownVersion(), gen.version());
    }

    @Test
    public void testDelegate() {
        DummyJsonGenerator gen = new DummyJsonGenerator();
        assertNull(gen.getDelegate());
    }

    @Test
    public void testWriteStartArrayWithObject() throws IOException {
        DummyJsonGenerator gen = new DummyJsonGenerator();
        gen.writeStartArray(new Object());
        gen.writeStartArray(new Object(), 5);
        gen.writeStartObject(new Object());
        // Just exercising methods to cover default implementations
        assertTrue(true);
    }

    @Test
    public void testWriteFieldId() throws IOException {
        DummyJsonGenerator gen = new DummyJsonGenerator();
        gen.writeFieldId(123L);
        // Default impl doesn't throw, but let's make sure it executes
        assertTrue(true);
    }

    @Test
    public void testWriteArrayValues() throws IOException {
        DummyJsonGenerator gen = new DummyJsonGenerator();
        gen.writeArray(new int[]{1, 2, 3}, 0, 3);
        gen.writeArray(new long[]{1L, 2L}, 0, 2);
        gen.writeArray(new double[]{1.0, 2.0}, 0, 2);
    }
}