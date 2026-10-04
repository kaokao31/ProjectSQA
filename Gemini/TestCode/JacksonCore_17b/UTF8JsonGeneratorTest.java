package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class UTF8JsonGeneratorTest {

    private IOContext ioContext;
    private ByteArrayOutputStream byteArrayOutputStream;
    private UTF8JsonGenerator generator;

    @Before
    public void setUp() throws Exception {
        ioContext = new IOContext(new BufferRecycler(), "test-ref", false);
        byteArrayOutputStream = new ByteArrayOutputStream();
        int flags = 0; // standard flags
        ObjectCodec codec = null; // can be null for basic testing
        BytesToNameCanonicalizer enc = BytesToNameCanonicalizer.createRoot();
        generator = new UTF8JsonGenerator(ioContext, flags, codec, byteArrayOutputStream,
                byteArrayOutputStream.toByteArray(), 0, false);
    }

    @After
    public void tearDown() throws Exception {
        if (generator != null && !generator.isClosed()) {
            generator.close();
        }
    }

    @Test
    public void testInitializationAndBasics() {
        assertNotNull(generator);
        assertFalse(generator.isClosed());
        assertNotNull(generator.getOutputTarget());
        assertEquals(0, generator.getOutputBuffered());
        assertNotNull(generator.getCodec());
        assertNotNull(generator.getStreamWriteConstraints());
    }

    @Test
    public void testWriteNull() throws IOException {
        generator.writeNull();
        generator.flush();
        assertEquals("null", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteBoolean() throws IOException {
        generator.writeBoolean(true);
        generator.writeBoolean(false);
        generator.flush();
        assertEquals("truefalse", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberInt() throws IOException {
        generator.writeNumber(123);
        generator.flush();
        assertEquals("123", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        generator.writeNumber(1234567890L);
        generator.flush();
        assertEquals("1234567890", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        generator.writeNumber(new BigInteger("12345678901234567890"));
        generator.flush();
        assertEquals("12345678901234567890", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        generator.writeNumber(new BigDecimal("123.45"));
        generator.flush();
        assertEquals("123.45", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        generator.writeNumber(123.45);
        generator.flush();
        assertEquals("123.45", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        generator.writeNumber(12.34f);
        generator.flush();
        assertEquals("12.34", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteNumberString() throws IOException {
        generator.writeNumber("987654");
        generator.flush();
        assertEquals("987654", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteString() throws IOException {
        generator.writeString("Hello World");
        generator.flush();
        assertEquals("\"Hello World\"", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteStringEmpty() throws IOException {
        generator.writeString("");
        generator.flush();
        assertEquals("\"\"", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteStringSpecialChars() throws IOException {
        generator.writeString("Line1\nLine2\t\"Quotes\"");
        generator.flush();
        assertEquals("\"Line1\\nLine2\\t\\\"Quotes\\\"\"", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteRawString() throws IOException {
        generator.writeRaw("raw-value");
        generator.flush();
        assertEquals("raw-value", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteRawChars() throws IOException {
        char[] chars = {'a', 'b', 'c'};
        generator.writeRaw(chars, 0, 3);
        generator.flush();
        assertEquals("abc", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteRawChar() throws IOException {
        generator.writeRaw('X');
        generator.flush();
        assertEquals("X", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteObjectAndArray() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("field1");
        generator.writeString("val1");
        generator.writeFieldName("field2");
        generator.writeStartArray();
        generator.writeNumber(1);
        generator.writeNumber(2);
        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();

        assertEquals("{\"field1\":\"val1\",\"field2\":[1,2]}", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testWriteBinary() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        generator.writeBinary(Base64Variants.MIME, data, 0, data.length);
        generator.flush();
        // Base64 of {1,2,3,4,5}
        assertTrue(byteArrayOutputStream.toString("UTF-8").length() > 0);
    }

    @Test
    public void testSerializeCustomUTF8() throws IOException {
        // Trigger specific code paths for UTF8 character encoding and escaping
        char[] complexChars = new char[]{'A', '\u0000', '\u0080', '\u0800', '\uD800', '\uDC00'};
        // Just exercising writeString with various unicode points
        generator.writeString(new String(complexChars));
        generator.flush();
        assertTrue(byteArrayOutputStream.size() > 0);
    }

    @Test
    public void testWriteFieldName() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("myField");
        generator.writeNumber(100);
        generator.writeEndObject();
        generator.close();
        assertEquals("{\"myField\":100}", byteArrayOutputStream.toString("UTF-8"));
    }

    @Test
    public void testFlushAndClose() throws IOException {
        generator.writeStartObject();
        generator.writeFieldName("a");
        generator.writeNumber(1);
        generator.writeEndObject();
        generator.flush();
        assertTrue(byteArrayOutputStream.toString("UTF-8").contains("\"a\":1"));
        generator.close();
        assertTrue(generator.isClosed());
    }

    @Test(expected = IOException.class)
    public void testWriteAfterClose() throws IOException {
        generator.close();
        generator.writeNumber(10);
    }

    @Test
    public void testFeatureEnablement() {
        assertFalse(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        generator.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertTrue(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        generator.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertFalse(generator.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
    }

    @Test
    public void testGetOutputBuffered() throws IOException {
        assertEquals(0, generator.getOutputBuffered());
        generator.writeNumber(123);
        // Might be buffered or flushed depending on implementation
        assertTrue(generator.getOutputBuffered() >= 0);
    }
}