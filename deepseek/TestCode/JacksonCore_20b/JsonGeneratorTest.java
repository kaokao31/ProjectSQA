package com.fasterxml.jackson.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.CharacterEscapes;

public class JsonGeneratorTest {

    private TestJsonGenerator generator;

    @Before
    public void setUp() {
        generator = new TestJsonGenerator();
    }

    // ------------------------------------------------------------
    // Tests for Feature enum
    // ------------------------------------------------------------
    @Test
    public void testFeatureValues() {
        for (JsonGenerator.Feature f : JsonGenerator.Feature.values()) {
            assertNotNull(f);
            assertTrue(f.getMask() > 0);
        }
    }

    @Test
    public void testFeatureEnabledByDefault() {
        for (JsonGenerator.Feature f : JsonGenerator.Feature.values()) {
            // Just ensure no exception
            f.enabledByDefault();
        }
    }

    @Test
    public void testFeatureEnabledIn() {
        int flags = JsonGenerator.Feature.collectDefaults();
        for (JsonGenerator.Feature f : JsonGenerator.Feature.values()) {
            boolean enabled = f.enabledIn(flags);
            assertEquals(f.enabledByDefault(), enabled);
        }
    }

    @Test
    public void testFeatureCollectDefaults() {
        int defaults = JsonGenerator.Feature.collectDefaults();
        assertTrue(defaults >= 0);
    }

    // ------------------------------------------------------------
    // Tests for isEnabled / enable / disable / configure
    // ------------------------------------------------------------
    @Test
    public void testEnableDisableFeature() {
        for (JsonGenerator.Feature f : JsonGenerator.Feature.values()) {
            generator.disable(f);
            assertFalse(generator.isEnabled(f));
            generator.enable(f);
            assertTrue(generator.isEnabled(f));
            generator.configure(f, false);
            assertFalse(generator.isEnabled(f));
            generator.configure(f, true);
            assertTrue(generator.isEnabled(f));
        }
    }

    @Test
    public void testSetAndGetFeatureValue() {
        int flags = 0;
        for (JsonGenerator.Feature f : JsonGenerator.Feature.values()) {
            generator.setFeatureValue(f.getMask(), true);
            assertTrue(generator.getFeatureValue(f.getMask()));
            generator.setFeatureValue(f.getMask(), false);
            assertFalse(generator.getFeatureValue(f.getMask()));
        }
    }

    // ------------------------------------------------------------
    // Tests for output context
    // ------------------------------------------------------------
    @Test
    public void testGetOutputContext() {
        assertNotNull(generator.getOutputContext());
    }

    // ------------------------------------------------------------
    // Tests for highest escaped char
    // ------------------------------------------------------------
    @Test
    public void testHighestEscapedChar() {
        assertEquals(0, generator.getHighestEscapedChar());
        generator.setHighestEscapedChar(127);
        assertEquals(127, generator.getHighestEscapedChar());
        generator.setHighestEscapedChar(0);
        assertEquals(0, generator.getHighestEscapedChar());
    }

    // ------------------------------------------------------------
    // Tests for character escapes
    // ------------------------------------------------------------
    @Test
    public void testCharacterEscapes() {
        assertNull(generator.getCharacterEscapes());
        CharacterEscapes escapes = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                return new int[128];
            }
            @Override
            public SerializableString getEscapeSequence(int ch) {
                return null;
            }
        };
        generator.setCharacterEscapes(escapes);
        assertSame(escapes, generator.getCharacterEscapes());
        generator.setCharacterEscapes(null);
        assertNull(generator.getCharacterEscapes());
    }

    // ------------------------------------------------------------
    // Tests for root value separator
    // ------------------------------------------------------------
    @Test
    public void testRootValueSeparator() {
        assertNull(generator.getRootValueSeparator());
        SerializableString sep = new SerializableString() {
            @Override
            public String getValue() { return ","; }
            @Override
            public int charLength() { return 1; }
            @Override
            public char[] asQuotedChars() { return new char[]{','}; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[]{','}; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[]{','}; }
        };
        generator.setRootValueSeparator(sep);
        assertSame(sep, generator.getRootValueSeparator());
        generator.setRootValueSeparator(null);
        assertNull(generator.getRootValueSeparator());
    }

    // ------------------------------------------------------------
    // Tests for pretty printer
    // ------------------------------------------------------------
    @Test
    public void testPrettyPrinter() {
        assertNull(generator.getPrettyPrinter());
        PrettyPrinter pp = new PrettyPrinter() {
            @Override public void writeRootValueSeparator(JsonGenerator gen) {}
            @Override public void writeStartObject(JsonGenerator gen) {}
            @Override public void writeEndObject(JsonGenerator gen, int nrOfEntries) {}
            @Override public void writeObjectEntrySeparator(JsonGenerator gen) {}
            @Override public void writeObjectFieldValueSeparator(JsonGenerator gen) {}
            @Override public void writeStartArray(JsonGenerator gen) {}
            @Override public void writeEndArray(JsonGenerator gen, int nrOfValues) {}
            @Override public void writeArrayValueSeparator(JsonGenerator gen) {}
            @Override public void beforeArrayValues(JsonGenerator gen) {}
            @Override public void beforeObjectEntries(JsonGenerator gen) {}
        };
        generator.setPrettyPrinter(pp);
        assertSame(pp, generator.getPrettyPrinter());
        generator.setPrettyPrinter(null);
        assertNull(generator.getPrettyPrinter());
    }

    // ------------------------------------------------------------
    // Tests for codec
    // ------------------------------------------------------------
    @Test
    public void testCodec() {
        assertNull(generator.getCodec());
        ObjectCodec codec = new ObjectCodec() {
            @Override public JsonFactory getFactory() { return null; }
            @Override public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override public TreeNode readTree(JsonParser p) { return null; }
            @Override public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override public JsonParser readValues(JsonParser p, Class<?> valueType) { return null; }
            @Override public JsonParser readValues(JsonParser p, TypeReference<?> valueTypeRef) { return null; }
            @Override public <T> T readValue(JsonParser p, TypeReference<?> valueTypeRef) { return null; }
        };
        generator.setCodec(codec);
        assertSame(codec, generator.getCodec());
        generator.setCodec(null);
        assertNull(generator.getCodec());
    }

    // ------------------------------------------------------------
    // Tests for schema
    // ------------------------------------------------------------
    @Test
    public void testSchema() {
        assertNull(generator.getSchema());
        FormatSchema schema = new FormatSchema() {
            @Override public String getSchemaType() { return "test"; }
        };
        generator.setSchema(schema);
        assertSame(schema, generator.getSchema());
        generator.setSchema(null);
        assertNull(generator.getSchema());
    }

    // ------------------------------------------------------------
    // Tests for output state
    // ------------------------------------------------------------
    @Test
    public void testOutputState() {
        assertEquals(JsonGenerator.STATE_ROOT, generator.getOutputState());
        generator.setOutputState(JsonGenerator.STATE_ARRAY);
        assertEquals(JsonGenerator.STATE_ARRAY, generator.getOutputState());
        generator.setOutputState(JsonGenerator.STATE_ROOT);
        assertEquals(JsonGenerator.STATE_ROOT, generator.getOutputState());
    }

    // ------------------------------------------------------------
    // Tests for writeString (null handling)
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteStringNull() throws IOException {
        generator.writeString((String) null);
    }

    @Test
    public void testWriteStringEmpty() throws IOException {
        generator.writeString("");
        // no exception expected
    }

    @Test
    public void testWriteStringNormal() throws IOException {
        generator.writeString("hello");
    }

    // ------------------------------------------------------------
    // Tests for writeString with char array
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteStringCharArrayNull() throws IOException {
        generator.writeString((char[]) null, 0, 0);
    }

    @Test
    public void testWriteStringCharArrayEmpty() throws IOException {
        generator.writeString(new char[0], 0, 0);
    }

    @Test
    public void testWriteStringCharArrayNormal() throws IOException {
        generator.writeString(new char[]{'a','b','c'}, 0, 3);
    }

    // ------------------------------------------------------------
    // Tests for writeString with SerializableString
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteSerializableStringNull() throws IOException {
        generator.writeString((SerializableString) null);
    }

    @Test
    public void testWriteSerializableStringNormal() throws IOException {
        SerializableString ss = new SerializableString() {
            @Override public String getValue() { return "test"; }
            @Override public int charLength() { return 4; }
            @Override public char[] asQuotedChars() { return new char[]{'t','e','s','t'}; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{'t','e','s','t'}; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{'t','e','s','t'}; }
        };
        generator.writeString(ss);
    }

    // ------------------------------------------------------------
    // Tests for writeNumber
    // ------------------------------------------------------------
    @Test
    public void testWriteNumberInt() throws IOException {
        generator.writeNumber(42);
        generator.writeNumber(0);
        generator.writeNumber(-1);
        generator.writeNumber(Integer.MAX_VALUE);
        generator.writeNumber(Integer.MIN_VALUE);
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        generator.writeNumber(42L);
        generator.writeNumber(0L);
        generator.writeNumber(-1L);
        generator.writeNumber(Long.MAX_VALUE);
        generator.writeNumber(Long.MIN_VALUE);
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        generator.writeNumber(3.14);
        generator.writeNumber(0.0);
        generator.writeNumber(-1.5);
        generator.writeNumber(Double.MAX_VALUE);
        generator.writeNumber(Double.MIN_VALUE);
        generator.writeNumber(Double.NaN);
        generator.writeNumber(Double.POSITIVE_INFINITY);
        generator.writeNumber(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        generator.writeNumber(3.14f);
        generator.writeNumber(0.0f);
        generator.writeNumber(-1.5f);
        generator.writeNumber(Float.MAX_VALUE);
        generator.writeNumber(Float.MIN_VALUE);
        generator.writeNumber(Float.NaN);
        generator.writeNumber(Float.POSITIVE_INFINITY);
        generator.writeNumber(Float.NEGATIVE_INFINITY);
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        generator.writeNumber(BigDecimal.TEN);
        generator.writeNumber(BigDecimal.ZERO);
        generator.writeNumber(BigDecimal.valueOf(123.456));
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteNumberBigDecimalNull() throws IOException {
        generator.writeNumber((BigDecimal) null);
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        generator.writeNumber(BigInteger.TEN);
        generator.writeNumber(BigInteger.ZERO);
        generator.writeNumber(BigInteger.valueOf(-123));
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteNumberBigIntegerNull() throws IOException {
        generator.writeNumber((BigInteger) null);
    }

    @Test
    public void testWriteNumberString() throws IOException {
        generator.writeNumber("123");
        generator.writeNumber("0");
        generator.writeNumber("-456.789");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteNumberStringNull() throws IOException {
        generator.writeNumber((String) null);
    }

    // ------------------------------------------------------------
    // Tests for writeBoolean
    // ------------------------------------------------------------
    @Test
    public void testWriteBoolean() throws IOException {
        generator.writeBoolean(true);
        generator.writeBoolean(false);
    }

    // ------------------------------------------------------------
    // Tests for writeNull
    // ------------------------------------------------------------
    @Test
    public void testWriteNull() throws IOException {
        generator.writeNull();
    }

    // ------------------------------------------------------------
    // Tests for writeObject (null handling)
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteObjectNull() throws IOException {
        generator.writeObject(null);
    }

    @Test
    public void testWriteObjectString() throws IOException {
        generator.writeObject("test");
    }

    @Test
    public void testWriteObjectNumber() throws IOException {
        generator.writeObject(42);
    }

    // ------------------------------------------------------------
    // Tests for writeTree
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteTreeNull() throws IOException {
        generator.writeTree(null);
    }

    @Test
    public void testWriteTreeNonNull() throws IOException {
        TreeNode tree = new TreeNode() {
            @Override public JsonToken asToken() { return JsonToken.VALUE_NULL; }
            @Override public JsonParser.NumberType numberType() { return null; }
            @Override public int size() { return 0; }
            @Override public boolean isValueNode() { return true; }
            @Override public boolean isContainerNode() { return false; }
            @Override public boolean isMissingNode() { return false; }
            @Override public boolean isArray() { return false; }
            @Override public boolean isObject() { return false; }
            @Override public TreeNode get(String fieldName) { return null; }
            @Override public TreeNode get(int index) { return null; }
            @Override public TreeNode path(String fieldName) { return null; }
            @Override public TreeNode path(int index) { return null; }
            @Override public Iterator<String> fieldNames() { return null; }
            @Override public TreeNode get(int index) { return null; }
            @Override public String toString() { return "null"; }
        };
        generator.writeTree(tree);
    }

    // ------------------------------------------------------------
    // Tests for writeStartObject / writeEndObject
    // ------------------------------------------------------------
    @Test
    public void testWriteStartEndObject() throws IOException {
        generator.writeStartObject();
        generator.writeEndObject();
    }

    // ------------------------------------------------------------
    // Tests for writeStartArray / writeEndArray
    // ------------------------------------------------------------
    @Test
    public void testWriteStartEndArray() throws IOException {
        generator.writeStartArray();
        generator.writeEndArray();
    }

    // ------------------------------------------------------------
    // Tests for writeFieldName
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldNameNull() throws IOException {
        generator.writeFieldName((String) null);
    }

    @Test
    public void testWriteFieldNameEmpty() throws IOException {
        generator.writeFieldName("");
    }

    @Test
    public void testWriteFieldNameNormal() throws IOException {
        generator.writeFieldName("name");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldNameSerializableNull() throws IOException {
        generator.writeFieldName((SerializableString) null);
    }

    @Test
    public void testWriteFieldNameSerializableNormal() throws IOException {
        SerializableString ss = new SerializableString() {
            @Override public String getValue() { return "field"; }
            @Override public int charLength() { return 5; }
            @Override public char[] asQuotedChars() { return new char[]{'f','i','e','l','d'}; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{'f','i','e','l','d'}; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{'f','i','e','l','d'}; }
        };
        generator.writeFieldName(ss);
    }

    // ------------------------------------------------------------
    // Tests for writeRaw
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteRawStringNull() throws IOException {
        generator.writeRaw((String) null);
    }

    @Test
    public void testWriteRawStringEmpty() throws IOException {
        generator.writeRaw("");
    }

    @Test
    public void testWriteRawStringNormal() throws IOException {
        generator.writeRaw("raw");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteRawStringOffsetNull() throws IOException {
        generator.writeRaw((String) null, 0, 0);
    }

    @Test
    public void testWriteRawStringOffsetNormal() throws IOException {
        generator.writeRaw("raw", 0, 3);
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteRawCharArrayNull() throws IOException {
        generator.writeRaw((char[]) null, 0, 0);
    }

    @Test
    public void testWriteRawCharArrayEmpty() throws IOException {
        generator.writeRaw(new char[0], 0, 0);
    }

    @Test
    public void testWriteRawCharArrayNormal() throws IOException {
        generator.writeRaw(new char[]{'r','a','w'}, 0, 3);
    }

    @Test
    public void testWriteRawChar() throws IOException {
        generator.writeRaw('x');
    }

    // ------------------------------------------------------------
    // Tests for writeRawValue
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteRawValueStringNull() throws IOException {
        generator.writeRawValue((String) null);
    }

    @Test
    public void testWriteRawValueStringEmpty() throws IOException {
        generator.writeRawValue("");
    }

    @Test
    public void testWriteRawValueStringNormal() throws IOException {
        generator.writeRawValue("123");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteRawValueStringOffsetNull() throws IOException {
        generator.writeRawValue((String) null, 0, 0);
    }

    @Test
    public void testWriteRawValueStringOffsetNormal() throws IOException {
        generator.writeRawValue("123", 0, 3);
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteRawValueCharArrayNull() throws IOException {
        generator.writeRawValue((char[]) null, 0, 0);
    }

    @Test
    public void testWriteRawValueCharArrayEmpty() throws IOException {
        generator.writeRawValue(new char[0], 0, 0);
    }

    @Test
    public void testWriteRawValueCharArrayNormal() throws IOException {
        generator.writeRawValue(new char[]{'1','2','3'}, 0, 3);
    }

    // ------------------------------------------------------------
    // Tests for writeBinary
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteBinaryNull() throws IOException {
        generator.writeBinary(Base64Variants.MIME, null, 0, 0);
    }

    @Test
    public void testWriteBinaryEmpty() throws IOException {
        generator.writeBinary(Base64Variants.MIME, new byte[0], 0, 0);
    }

    @Test
    public void testWriteBinaryNormal() throws IOException {
        generator.writeBinary(Base64Variants.MIME, new byte[]{1,2,3}, 0, 3);
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteBinarySimpleNull() throws IOException {
        generator.writeBinary(null, 0, 0);
    }

    @Test
    public void testWriteBinarySimpleEmpty() throws IOException {
        generator.writeBinary(new byte[0], 0, 0);
    }

    @Test
    public void testWriteBinarySimpleNormal() throws IOException {
        generator.writeBinary(new byte[]{1,2,3}, 0, 3);
    }

    // ------------------------------------------------------------
    // Tests for writeUTF8String / writeRawUTF8String
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWriteUTF8StringNull() throws IOException {
        generator.writeUTF8String(null, 0, 0);
    }

    @Test
    public void testWriteUTF8StringEmpty() throws IOException {
        generator.writeUTF8String(new byte[0], 0, 0);
    }

    @Test
    public void testWriteUTF8StringNormal() throws IOException {
        generator.writeUTF8String(new byte[]{'h','e','l','l','o'}, 0, 5);
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteRawUTF8StringNull() throws IOException {
        generator.writeRawUTF8String(null, 0, 0);
    }

    @Test
    public void testWriteRawUTF8StringEmpty() throws IOException {
        generator.writeRawUTF8String(new byte[0], 0, 0);
    }

    @Test
    public void testWriteRawUTF8StringNormal() throws IOException {
        generator.writeRawUTF8String(new byte[]{'h','e','l','l','o'}, 0, 5);
    }

    // ------------------------------------------------------------
    // Tests for copyCurrentEvent / copyCurrentStructure
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testCopyCurrentEventNullParser() throws IOException {
        generator.copyCurrentEvent(null);
    }

    @Test(expected = JsonGenerationException.class)
    public void testCopyCurrentStructureNullParser() throws IOException {
        generator.copyCurrentStructure(null);
    }

    // ------------------------------------------------------------
    // Tests for flush and close
    // ------------------------------------------------------------
    @Test
    public void testFlush() throws IOException {
        generator.flush();
    }

    @Test
    public void testClose() throws IOException {
        generator.close();
    }

    // ------------------------------------------------------------
    // Tests for _reportError (via protected method exposure)
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testReportError() throws IOException {
        generator.callReportError("test error");
    }

    // ------------------------------------------------------------
    // Tests for _wrapIOException
    // ------------------------------------------------------------
    @Test(expected = JsonGenerationException.class)
    public void testWrapIOException() throws IOException {
        generator.callWrapIOException(new IOException("test"));
    }

    // ------------------------------------------------------------
    // Tests for _throwInternal
    // ------------------------------------------------------------
    @Test(expected = RuntimeException.class)
    public void testThrowInternal() {
        generator.callThrowInternal();
    }

    // ------------------------------------------------------------
    // Helper class: minimal concrete JsonGenerator
    // ------------------------------------------------------------
    private static class TestJsonGenerator extends JsonGenerator {

        private int outputState = STATE_ROOT;
        private int highestEscapedChar = 0;
        private CharacterEscapes characterEscapes;
        private SerializableString rootValueSeparator;
        private PrettyPrinter prettyPrinter;
        private ObjectCodec codec;
        private FormatSchema schema;
        private int featureFlags = JsonGenerator.Feature.collectDefaults();

        @Override
        public JsonStreamContext getOutputContext() {
            return new JsonStreamContext() {
                @Override
                public JsonToken getCurrentToken() { return null; }
                @Override
                public String getCurrentName() { return null; }
                @Override
                public Object getCurrentValue() { return null; }
                @Override
                public boolean hasCurrentName() { return false; }
                @Override
                public boolean hasCurrentValue() { return false; }
                @Override
                public int getEntryCount() { return 0; }
                @Override
                public int getCurrentIndex() { return 0; }
            };
        }

        @Override
        public boolean isEnabled(JsonGenerator.Feature f) {
            return (featureFlags & f.getMask()) != 0;
        }

        @Override
        public JsonGenerator enable(JsonGenerator.Feature f) {
            featureFlags |= f.getMask();
            return this;
        }

        @Override
        public JsonGenerator disable(JsonGenerator.Feature f) {
            featureFlags &= ~f.getMask();
            return this;
        }

        @Override
        public JsonGenerator configure(JsonGenerator.Feature f, boolean state) {
            if (state) enable(f); else disable(f);
            return this;
        }

        @Override
        public void setFeatureValue(int mask, boolean enabled) {
            if (enabled) featureFlags |= mask; else featureFlags &= ~mask;
        }

        @Override
        public boolean getFeatureValue(int mask) {
            return (featureFlags & mask) != 0;
        }

        @Override
        public int getOutputState() {
            return outputState;
        }

        @Override
        public void setOutputState(int state) {
            outputState = state;
        }

        @Override
        public int getHighestEscapedChar() {
            return highestEscapedChar;
        }

        @Override
        public void setHighestEscapedChar(int ch) {
            highestEscapedChar = ch;
        }

        @Override
        public CharacterEscapes getCharacterEscapes() {
            return characterEscapes;
        }

        @Override
        public void setCharacterEscapes(CharacterEscaces esc) {
            this.characterEscapes = esc;
        }

        @Override
        public SerializableString getRootValueSeparator() {
            return rootValueSeparator;
        }

        @Override
        public void setRootValueSeparator(SerializableString sep) {
            this.rootValueSeparator = sep;
        }

        @Override
        public PrettyPrinter getPrettyPrinter() {
            return prettyPrinter;
        }

        @Override
        public void setPrettyPrinter(PrettyPrinter pp) {
            this.prettyPrinter = pp;
        }

        @Override
        public ObjectCodec getCodec() {
            return codec;
        }

        @Override
        public void setCodec(ObjectCodec oc) {
            this.codec = oc;
        }

        @Override
        public FormatSchema getSchema() {
            return schema;
        }

        @Override
        public void setSchema(FormatSchema schema) {
            this.schema = schema;
        }

        // Abstract methods – minimal implementations
        @Override
        public void writeStartArray() throws IOException { }
        @Override
        public void writeEndArray() throws IOException { }
        @Override
        public void writeStartObject() throws IOException { }
        @Override
        public void writeEndObject() throws IOException { }
        @Override
        public void writeFieldName(String name) throws IOException {
            if (name == null) _reportError("Field name is null");
        }
        @Override
        public void writeFieldName(SerializableString name) throws IOException {
            if (name == null) _reportError("Field name is null");
        }
        @Override
        public void writeString(String text) throws IOException {
            if (text == null) _reportError("String value is null");
        }
        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            if (text == null) _reportError("char[] is null");
        }
        @Override
        public void writeString(SerializableString text) throws IOException {
            if (text == null) _reportError("SerializableString is null");
        }
        @Override
        public void writeRawUTF8String(byte[] text, int offset, int len) throws IOException {
            if (text == null) _reportError("byte[] is null");
        }
        @Override
        public void writeUTF8String(byte[] text, int offset, int len) throws IOException {
            if (text == null) _reportError("byte[] is null");
        }
        @Override
        public void writeRaw(String text) throws IOException {
            if (text == null) _reportError("raw string is null");
        }
        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {
            if (text == null) _reportError("raw string is null");
        }
        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {
            if (text == null) _reportError("raw char[] is null");
        }
        @Override
        public void writeRaw(char c) throws IOException { }
        @Override
        public void writeRawValue(String text) throws IOException {
            if (text == null) _reportError("raw value string is null");
        }
        @Override
        public void writeRawValue(String text, int offset, int len) throws IOException {
            if (text == null) _reportError("raw value string is null");
        }
        @Override
        public void writeRawValue(char[] text, int offset, int len) throws IOException {
            if (text == null) _reportError("raw value char[] is null");
        }
        @Override
        public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
            if (data == null) _reportError("binary data is null");
        }
        @Override
        public void writeBinary(byte[] data, int offset, int len) throws IOException {
            if (data == null) _reportError("binary data is null");
        }
        @Override
        public void writeNumber(int v) throws IOException { }
        @Override
        public void writeNumber(long v) throws IOException { }
        @Override
        public void writeNumber(double v) throws IOException { }
        @Override
        public void writeNumber(float v) throws IOException { }
        @Override
        public void writeNumber(BigDecimal dec) throws IOException {
            if (dec == null) _reportError("BigDecimal is null");
        }
        @Override
        public void writeNumber(BigInteger v) throws IOException {
            if (v == null) _reportError("BigInteger is null");
        }
        @Override
        public void writeNumber(String encodedValue) throws IOException {
            if (encodedValue == null) _reportError("encoded number string is null");
        }
        @Override
        public void writeBoolean(boolean state) throws IOException { }
        @Override
        public void writeNull() throws IOException { }
        @Override
        public void writeObject(Object pojo) throws IOException {
            if (pojo == null) _reportError("Object value is null");
        }
        @Override
        public void writeTree(TreeNode rootNode) throws IOException {
            if (rootNode == null) _reportError("TreeNode is null");
        }
        @Override
        public void copyCurrentEvent(JsonParser jp) throws IOException {
            if (jp == null) _reportError("JsonParser is null");
        }
        @Override
        public void copyCurrentStructure(JsonParser jp) throws IOException {
            if (jp == null) _reportError("JsonParser is null");
        }
        @Override
        public void flush() throws IOException { }
        @Override
        public void close() throws IOException { }

        // Expose protected methods for testing
        public void callReportError(String msg) throws IOException {
            _reportError(msg);
        }

        public void callWrapIOException(IOException e) throws IOException {
            _wrapIOException(e);
        }

        public void callThrowInternal() {
            _throwInternal();
        }
    }
}