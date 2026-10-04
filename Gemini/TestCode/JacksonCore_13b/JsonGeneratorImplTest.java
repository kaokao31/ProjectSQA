package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class JsonGeneratorImplTest {

    private IOContext ioContext;
    private TestJsonGeneratorImpl generator;
    private StringWriter writer;

    // Concrete subclass of abstract JsonGeneratorImpl for testing purposes
    private static class TestJsonGeneratorImpl extends JsonGeneratorImpl {
        protected boolean _closed = false;

        public TestJsonGeneratorImpl(IOContext ctxt, int features, ObjectCodec codec) {
            super(ctxt, features, codec);
        }

        @Override
        public boolean isClosed() {
            return _closed;
        }

        @Override
        public void close() throws IOException {
            _closed = true;
        }

        @Override
        public JsonGenerator disable(Feature f) {
            super.disable(f);
            return this;
        }

        @Override
        public JsonGenerator enable(Feature f) {
            super.enable(f);
            return this;
        }

        @Override
        public JsonGenerator setFeatureMask(int mask) {
            return super.setFeatureMask(mask);
        }

        @Override
        public void writeStartArray() throws IOException {
            _verifyValueWrite("start an array");
            _writeContext = _writeContext.createChildArrayContext();
            if (_cfgPrettyPrinter != null) {
                _cfgPrettyPrinter.writeStartArray(this);
            } else {
                // minimal mock implementation
            }
        }

        @Override
        public void writeEndArray() throws IOException {
            if (!_writeContext.inArray()) {
                _reportError("Current context not Array");
            }
            if (_cfgPrettyPrinter != null) {
                _cfgPrettyPrinter.writeEndArray(this, _writeContext.getEntryCount());
            }
            _writeContext = _writeContext.getParent();
        }

        @Override
        public void writeStartObject() throws IOException {
            _verifyValueWrite("start an object");
            _writeContext = _writeContext.createChildObjectContext();
            if (_cfgPrettyPrinter != null) {
                _cfgPrettyPrinter.writeStartObject(this);
            }
        }

        @Override
        public void writeEndObject() throws IOException {
            if (!_writeContext.inObject()) {
                _reportError("Current context not Object");
            }
            if (_cfgPrettyPrinter != null) {
                _cfgPrettyPrinter.writeEndObject(this, _writeContext.getEntryCount());
            }
            _writeContext = _writeContext.getParent();
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            _writeContext.writeFieldName(name);
        }

        @Override
        public void writeFieldName(SerializableString name) throws IOException {
            _writeContext.writeFieldName(name.getValue());
        }

        @Override
        public void writeString(String text) throws IOException {
            _verifyValueWrite("write text");
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            _verifyValueWrite("write text");
        }

        @Override
        public void writeRaw(String text) throws IOException {}

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(char c) throws IOException {}

        @Override
        public void writeBinary(com.fasterxml.jackson.core.Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {}

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
        protected void _releaseBuffers() {}

        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {
            // basic implementation to pass checks
        }
    }

    @Before
    public void setUp() {
        BufferRecycler recycler = new BufferRecycler();
        ioContext = new IOContext(recycler, this, false);
        writer = new StringWriter();
        // Enable features like WRITE_NUMBERS_AS_STRINGS or others if necessary
        int features = JsonGenerator.Feature.collectDefaults();
        generator = new TestJsonGeneratorImpl(ioContext, features, null);
    }

    @After
    public void tearDown() throws Exception {
        if (generator != null && !generator.isClosed()) {
            generator.close();
        }
    }

    @Test
    public void testInitializationAndCodec() {
        assertNull(generator.getCodec());
        ObjectCodec codec = new com.fasterxml.jackson.databind.ObjectMapper();
        generator.setCodec(codec);
        assertSame(codec, generator.getCodec());
    }

    @Test
    public void testVersion() {
        assertNotNull(generator.version());
    }

    @Test
    public void testFeatures() {
        assertTrue(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertFalse(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        generator.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertTrue(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        generator.setFeatureMask(0);
        assertFalse(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
    }

    @Test
    public void testCharacterEscapes() {
        assertNull(generator.getCharacterEscapes());
        com.fasterxml.jackson.core.io.CharacterEscapes escapes = new com.fasterxml.jackson.core.io.CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                return new int[0];
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                return null;
            }
        };
        generator.setCharacterEscapes(escapes);
        assertSame(escapes, generator.getCharacterEscapes());
    }

    @Test
    public void testHighestNonEscapedChar() {
        assertEquals(0, generator.getHighestEscapedChar());
        generator.setHighestNonEscapedChar(127);
        assertEquals(127, generator.getHighestEscapedChar());
        
        // Edge cases for highest non-escaped char setting (Bug 13 context often relates to generator features/escaping configurations)
        generator.setHighestNonEscapedChar(0);
        assertEquals(0, generator.getHighestEscapedChar());
        generator.setHighestNonEscapedChar(-1);
        assertEquals(-1, generator.getHighestEscapedChar());
    }

    @Test
    public void testCurrentSchema() {
        assertNull(generator.getSchema());
        com.fasterxml.jackson.core.FormatSchema schema = new com.fasterxml.jackson.core.FormatSchema() {
            @Override
            public String getSchemaType() {
                return "test";
            }
        };
        generator.setSchema(schema);
        assertSame(schema, generator.getSchema());
    }

    @Test
    public void testPrettyPrinter() {
        assertNull(generator.getPrettyPrinter());
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        generator.useDefaultPrettyPrinter();
        assertNotNull(generator.getPrettyPrinter());

        generator.setPrettyPrinter(pp);
        assertSame(pp, generator.getPrettyPrinter());

        generator.setPrettyPrinter(null);
        assertNull(generator.getPrettyPrinter());
    }

    @Test
    public void testCopyCurrentEvent() throws IOException {
        // Testing methods that delegate or throw unsupported/standard exceptions
        com.fasterxml.jackson.core.JsonParser parser = new com.fasterxml.jackson.core.util.JsonParserDelegate(null) {
            // minimal mock if needed, or expect exception
        };
        try {
            generator.copyCurrentEvent(parser);
        } catch (Exception e) {
            // Expected if parser is null or not fully mocked
        }
    }

    @Test
    public void testWriteTree() throws IOException {
        ObjectCodec codec = new com.fasterxml.jackson.databind.ObjectMapper();
        generator.setCodec(codec);
        // Writing null tree
        generator.writeTree(null);
        
        com.fasterxml.jackson.databind.node.TextNode node = com.fasterxml.jackson.databind.node.TextNode.valueOf("hello");
        generator.writeTree(node);
    }
}