package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ReaderBasedJsonParserTest {

    private IOContext ioContext;
    private BufferRecycler bufferRecycler;

    @Before
    public void setUp() {
        bufferRecycler = new BufferRecycler();
        ioContext = new IOContext(bufferRecycler, "test", false);
    }

    @After
    public void tearDown() {
        // Cleanup if needed
    }

    private ReaderBasedJsonParser createParser(String json) {
        Reader reader = new StringReader(json);
        CharsToNameCanonicalizer cnc = CharsToNameCanonicalizer.createRoot();
        return new ReaderBasedJsonParser(ioContext, 0, reader, null, cnc);
    }

    @Test
    public void testParserInitialization() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\": 1}");
        assertNotNull(parser);
        assertNull(parser.getCurrentToken());
        parser.close();
    }

    @Test
    public void testGetInputSource() throws IOException {
        String json = "[1, 2, 3]";
        Reader reader = new StringReader(json);
        CharsToNameCanonicalizer cnc = CharsToNameCanonicalizer.createRoot();
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ioContext, 0, reader, null, cnc);
        assertSame(reader, parser.getInputSource());
        parser.close();
    }

    @Test
    public void testGetTextCharactersAndLength() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello world\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        char[] chars = parser.getTextCharacters();
        assertNotNull(chars);
        assertTrue(parser.getTextLength() > 0);
        assertTrue(parser.getTextOffset() >= 0);
        parser.close();
    }

    @Test
    public void testGetTextMoreBranches() throws IOException {
        // Test various token types for getText() / getTextCharacters()
        ReaderBasedJsonParser parser = createParser("{\"key\": 123}");
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("", parser.getText());
        assertNull(parser.getTextCharacters());
        assertEquals(0, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getText());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("123", parser.getText());

        parser.close();
    }

    @Test
    public void testGetBinaryValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"QUJD\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] bytes = parser.getBinaryValue(Base64Variant.getDefaultVariant());
        assertNotNull(bytes);
        assertEquals("ABC", new String(bytes, "UTF-8"));
        parser.close();
    }

    @Test
    public void testLoadMore() throws IOException {
        // Indirectly test loadMore through parsing a longer string
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 2000; i++) {
            sb.append(i);
            if (i < 1999) sb.append(",");
        }
        sb.append("]");

        ReaderBasedJsonParser parser = createParser(sb.toString());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        for (int i = 0; i < 2000; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(i, parser.getIntValue());
        }
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextTokenBasic() throws IOException {
        ReaderBasedJsonParser parser = createParser("  \n\r\t {\"trueVal\": true, \"falseVal\": false, \"nullVal\": null, \"num\": -123.45}  ");
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("trueVal", parser.getCurrentName());
        
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("falseVal", parser.getCurrentName());
        
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nullVal", parser.getCurrentName());
        
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("num", parser.getCurrentName());
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-123.45, parser.getDoubleValue(), 0.001);
        
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        
        parser.close();
    }

    @Test
    public void testNumberParsingVarieties() throws IOException {
        ReaderBasedJsonParser parser = createParser("[123, -456, 123.45, 1e2, -2E-3, 0]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(123L, parser.getLongValue());
        assertEquals(BigInteger.valueOf(123), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(123), parser.getDecimalValue());
        assertEquals(123.0f, parser.getFloatValue(), 0.0f);
        assertEquals(123.0, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-456, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(123.45, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(100.0, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.002, parser.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testCommentsFeature() throws IOException {
        ReaderBasedJsonParser parser = createParser("/* comment */ {\n // line comment\n \"a\": 1}");
        // Enable comments feature if applicable, or parse directly if configured
        // By default JSON parser might fail on comments unless configured. Let's test standard parsing or configure.
        parser.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testReleaseBuffered() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        parser.nextToken();
        parser.releaseBuffered(new StringReader(""));
        parser.close();
    }

    @Test
    public void testOverflowIntLong() throws IOException {
        ReaderBasedJsonParser parser = createParser("[2147483650, -2147483655, 9223372036854775807, -9223372036854775808]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2147483650L, parser.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-2147483655L, parser.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(9223372036854775807L, parser.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-9223372036854775808L, parser.getLongValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testStringEscapesAndQuotes() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\"\\/\b\f\n\r\tA", parser.getText());
        parser.close();
    }

    @Test
    public void testUnquotedFieldNames() throws IOException {
        ReaderBasedJsonParser parser = createParser("{a: 1}");
        parser.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testSingleQuotes() throws IOException {
        ReaderBasedJsonParser parser = createParser("{'a': 'value'}");
        parser.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testGetEmbeddedObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        assertNull(parser.getEmbeddedObject());
        parser.close();
    }

    @Test
    public void testOverrideCurrentName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\": 1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        parser.overrideCurrentName("overridden");
        assertEquals("overridden", parser.getCurrentName());
        parser.close();
    }
}