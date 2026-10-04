package com.fasterxml.jackson.core.base;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.VersionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class GeneratorBaseTest {

    private ConcreteGenerator generator;
    private IOContext ioContext;

    @Before
    public void setUp() {
        BufferRecycler rc = new BufferRecycler();
        ioContext = new IOContext(rc, rc, false);
        generator = new ConcreteGenerator(0, null);
    }

    @After
    public void tearDown() throws Exception {
        if (!generator.isClosed()) {
            generator.close();
        }
    }

    @Test
    public void testInitialState() {
        assertFalse(generator.isClosed());
        assertNotNull(generator.getCodec());
        assertNotNull(generator.getOutputContext());
        assertTrue(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        assertTrue(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT));
    }

    @Test
    public void testFeatures() {
        generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertFalse(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        generator.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertTrue(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        generator.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        assertFalse(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        generator.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, true);
        assertTrue(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        JsonGenerator configured = generator.useDefaultPrettyPrinter();
        assertNotNull(configured);
    }

    @Test
    public void testVersion() {
        Version v = generator.version();
        assertNotNull(v);
        assertEquals(VersionUtil.unknownVersion(), v);
    }

    @Test
    public void testCurrent() throws IOException {
        generator.writeStartObject();
        assertEquals("{}", generator.getCurrentName()); // Default impl or custom
        generator.writeEndObject();
    }

    @Test
    public void testCopyCurrentEvent() throws IOException {
        JsonParser p = new DummyParser();
        generator.copyCurrentEvent(p);
        generator.copyCurrentStructure(p);
    }

    @Test
    public void testWriteFields() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("testField");
        generator.writeString("testValue");
        
        generator.writeBooleanField("bField", true);
        generator.writeNullField("nField");
        generator.writeNumberField("iField", 123);
        generator.writeNumberField("lField", 123L);
        generator.writeNumberField("dField", 1.23);
        generator.writeNumberField("fField", 1.23f);
        generator.writeNumberField("biField", BigInteger.TEN);
        generator.writeNumberField("bdField", BigDecimal.ONE);
        
        generator.writeObjectField("oField", "obj");
        generator.writeTree(null);
        
        generator.writeEndObject();
    }

    @Test
    public void testWriteRawVarious() throws IOException {
        generator.writeStartArray();
        generator.writeRawValue("raw");
        generator.writeRawValue("raw", 0, 3);
        generator.writeRawValue(new char[]{'r', 'a', 'w'}, 0, 3);
        
        generator.writeBinary(null, new byte[10], 0, 10);
        
        generator.writeString(new char[]{'a', 'b'}, 0, 2);
        generator.writeString(null);
        
        generator.writeEndArray();
    }

    @Test
    public void testDelegateMethods() throws IOException {
        assertEquals(generator, generator.setHighestNonEscapedChar(127));
        assertEquals(127, generator.getHighestNonEscapedChar());
        assertNull(generator.getCharacterEscapes());
        assertEquals(generator, generator.setCharacterEscapes(null));
        assertNull(generator.getOutputTarget());
        assertEquals(0, generator.getBufferRecycledSize());
    }

    @Test(expected = JsonGenerationException.class)
    public void testDefaultReportError() throws IOException {
        generator.publicReportError("Some error");
    }

    @Test
    public void testWriteMethodsWithVariousTypes() throws IOException {
        generator.writeStartArray();
        generator.writeBoolean(false);
        generator.writeNull();
        generator.writeNumber((short) 1);
        generator.writeNumber(2);
        generator.writeNumber(3L);
        generator.writeNumber(4.0f);
        generator.writeNumber(5.0);
        generator.writeNumber(BigInteger.valueOf(6));
        generator.writeNumber(BigDecimal.TEN);
        generator.writeNumber("123");
        generator.writeNumber("123", 0, 3);
        generator.writeEndArray();
    }

    @Test
    public void testWriteArrayField() throws IOException {
        generator.writeStartObject();
        generator.writeArrayFieldStart("arrayField");
        generator.writeNumber(1);
        generator.writeEndArray();
        
        generator.writeObjectFieldStart("objField");
        generator.writeEndObject();
        generator.writeEndObject();
    }

    // --- Helper concrete implementation of GeneratorBase ---
    private static class ConcreteGenerator extends GeneratorBase {
        protected boolean _closed = false;
        protected JsonWriteContext _ctxt;

        public ConcreteGenerator(int features, ObjectCodec codec) {
            super(features, codec);
            DupDetector dups = JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.enabledIn(features) ? DupDetector.rootDetector(this) : null;
            _ctxt = JsonWriteContext.createRootContext(dups);
        }

        @Override
        public JsonVersioned version() {
            return VersionUtil.unknownVersion();
        }

        @Override
        public JsonWriteContext getOutputContext() {
            return _ctxt;
        }

        @Override
        public void writeStartArray() throws IOException {
            _ctxt = _ctxt.createChildArrayContext();
        }

        @Override
        public void writeEndArray() throws IOException {
            _ctxt = _ctxt.getParent();
        }

        @Override
        public void writeStartObject() throws IOException {
            _ctxt = _ctxt.createChildObjectContext();
        }

        @Override
        public void writeEndObject() throws IOException {
            _ctxt = _ctxt.getParent();
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            _ctxt.writeFieldName(name);
        }

        @Override
        public void writeString(String text) throws IOException {}

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeRawUTF8String(byte[] buffer, int offset, int len) throws IOException {}

        @Override
        public void writeUTF8String(byte[] buffer, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(String text) throws IOException {}

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {}

        @Override
        public void writeRaw(char c) throws IOException {}

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
        public void flush() throws IOException {}

        @Override
        public void close() throws IOException {
            _closed = true;
        }

        @Override
        public boolean isClosed() {
            return _closed;
        }

        public void publicReportError(String msg) throws IOException {
            _reportError(msg);
        }
    }

    private static class DummyParser extends JsonParser {
        @Override public ObjectCodec getCodec() { return null; }
        @Override public void setCodec(ObjectCodec c) {}
        @Override public Version version() { return VersionUtil.unknownVersion(); }
        @Override public void close() throws IOException {}
        @Override public boolean isClosed() { return false; }
        @Override public JsonStreamContext getParsingContext() { return null; }
        @Override public void clearCurrentToken() {}
        @Override public JsonToken getLastClearedToken() { return null; }
        @Override public JsonToken getCurrentToken() { return JsonToken.START_OBJECT; }
        @Override public int getCurrentTokenId() { return JsonToken.START_OBJECT.id(); }
        @Override public boolean hasCurrentToken() { return true; }
        @Override public boolean hasTokenId(int id) { return true; }
        @Override public boolean hasToken(JsonToken t) { return true; }
        @Override public String getCurrentName() throws IOException { return "dummy"; }
        @Override public void overrideCurrentName(String name) {}
        @Override public String getText() throws IOException { return "dummy"; }
        @Override public char[] getTextCharacters() throws IOException { return new char[0]; }
        @Override public int getTextLength() throws IOException { return 0; }
        @Override public int getTextOffset() throws IOException { return 0; }
        @Override public boolean NebenTextCharactersAreCopyable() { return false; }
        @Override public byte[] getBinaryValue(Base64Variant b64variant) throws IOException { return new byte[0]; }
        @Override public int readBinaryValue(Base64Variant b64variant, OutputStream out) throws IOException { return 0; }
        @Override public JsonToken nextToken() throws IOException { return null; }
        @Override public JsonParser skipChildren() throws IOException { return this; }
        @Override public JsonLocation getTokenLocation() { return null; }
        @Override public JsonLocation getCurrentLocation() { return null; }
        @Override public Number getNumberValue() throws IOException { return 0; }
        @Override public NumberType getNumberType() throws IOException { return NumberType.INT; }
        @Override public int getIntValue() throws IOException { return 0; }
        @Override public long getLongValue() throws IOException { return 0L; }
        @Override public BigInteger getBigIntegerValue() throws IOException { return BigInteger.ZERO; }
        @Override public float getFloatValue() throws IOException { return 0.0f; }
        @Override public double getDoubleValue() throws IOException { return 0.0; }
        @Override public BigDecimal getDecimalValue() throws IOException { return BigDecimal.ZERO; }
        @Override public Object getEmbeddedObject() throws IOException { return null; }
    }
}