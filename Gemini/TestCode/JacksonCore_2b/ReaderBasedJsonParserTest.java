package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.*;

public class ReaderBasedJsonParserTest {

    private IOContext ioContext;
    private CharsToNameCanonicalizer symbols;

    @Before
    public void setUp() {
        BufferRecycler br = new BufferRecycler();
        ioContext = new IOContext(br, br, false);
        symbols = CharsToNameCanonicalizer.createRoot();
    }

    @After
    public void tearDown() throws Exception {
        // Nothing specific needed
    }

    private ReaderBasedJsonParser createParser(String json) {
        Reader reader = new StringReader(json);
        // default features
        int features = JsonFactory.DEFAULT_PARSER_FEATURE_FLAGS;
        return new ReaderBasedJsonParser(ioContext, features, reader, null, symbols);
    }

    private ReaderBasedJsonParser createParser(Reader reader, int features) {
        return new ReaderBasedJsonParser(ioContext, features, reader, null, symbols);
    }

    @Test
    public void testBasicParsing() throws IOException {
        String json = "{\"a\": 1, \"b\": [true, false, null], \"c\": \"hello\"}";
        ReaderBasedJsonParser parser = createParser(json);

        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("a", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());

        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());

        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testNumberParsingVarieties() throws IOException {
        // Test integers, longs, floats, doubles, exponents, negative values
        String json = "[-123, 0, 123, 123.45, 1e10, -2E-5, 3.14e+2]";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-123, parser.getIntValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(123.45, parser.getDoubleValue(), 0.001);
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1e10, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-2E-5, parser.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14e+2, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testLongAndBigNumberParsing() throws IOException {
        String json = "[9223372036854775807, -9223372036854775808]";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(9223372036854775807L, parser.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-9223372036854775808L, parser.getLongValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    @Test
    public void testEscapedCharactersInString() throws IOException {
        String json = "\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0020\"";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\"\\/\b\f\n\r\t ", parser.getText());
        parser.close();
    }

    @Test(expected = IOException.class)
    public void testInvalidStringEscape() throws IOException {
        String json = "\"\\x\"";
        ReaderBasedJsonParser parser = createParser(json);
        parser.nextToken();
    }

    @Test(expected = IOException.class)
    public void testUnterminatedString() throws IOException {
        String json = "\"unterminated";
        ReaderBasedJsonParser parser = createParser(json);
        parser.nextToken();
    }

    @Test(expected = IOException.class)
    public void testUnexpectedEndInObject() throws IOException {
        String json = "{\"a\": 1";
        ReaderBasedJsonParser parser = createParser(json);
        while (parser.nextToken() != null) {
            // consume
        }
    }

    @Test(expected = IOException.class)
    public void testInvalidToken() throws IOException {
        String json = "invalid";
        ReaderBasedJsonParser parser = createParser(json);
        parser.nextToken();
    }

    @Test
    public void testCommentsFeature() throws IOException {
        // Enable ALLOW_COMMENTS
        int features = JsonFactory.DEFAULT_PARSER_FEATURE_FLAGS | JsonParser.Feature.ALLOW_COMMENTS.getMask();
        String json = "{\n" +
                "  // line comment\n" +
                "  \"a\": 1 /* block comment */,\n" +
                "  \"b\": 2\n" +
                "}";
        ReaderBasedJsonParser parser = createParser(new StringReader(json), features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testSingleQuotesFeature() throws IOException {
        int features = JsonFactory.DEFAULT_PARSER_FEATURE_FLAGS | JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        String json = "{'a': 'value', \"b\": 2}";
        ReaderBasedJsonParser parser = createParser(new StringReader(json), features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testUnquotedFieldNamesFeature() throws IOException {
        int features = JsonFactory.DEFAULT_PARSER_FEATURE_FLAGS | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{a: 1, b: [2, 3]}";
        ReaderBasedJsonParser parser = createParser(new StringReader(json), features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testStreamReadMethods() throws IOException {
        String json = "{\"name\":\"Jackson\",\"val\":true,\"num\":123.45,\"nullVal\":null}";
        ReaderBasedJsonParser parser = createParser(json);

        assertNotNull(parser.getCurrentLocation());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCodec());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals("name", parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Jackson", parser.getText());
        char[] chars = parser.getTextCharacters();
        assertNotNull(chars);
        assertTrue(parser.getTextLength() > 0);
        assertTrue(parser.getTextOffset() >= 0);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(123.45, parser.getFloatValue(), 0.001);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testBufferRecyclingAndEmptyInput() throws IOException {
        String json = "";
        ReaderBasedJsonParser parser = createParser(json);
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testParserFeatureConfig() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        parser.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(parser.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        parser.close();
    }

    @Test
    public void testGetBinaryValue() throws IOException {
        // Base64 encoded string test
        String json = "\"TWFuaXN0ZXI=\""; // "Manister" in base64
        ReaderBasedJsonParser parser = createParser(json);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] bytes = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(bytes);
        assertEquals("Manister", new String(bytes, "UTF-8"));
        parser.close();
    }

    @Test
    public void testOvertheEdgeBufferCrossing() throws IOException {
        // Create a JSON string long enough to force buffer reloads (> 500 chars typically)
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < 200; i++) {
            sb.append("{\"index\":").append(i).append("},\n");
        }
        sb.append("{\"index\":200}\n]");

        ReaderBasedJsonParser parser = createParser(sb.toString());
        int count = 0;
        while (parser.nextToken() != null) {
            count++;
        }
        assertTrue(count > 500);
        parser.close();
    }
}