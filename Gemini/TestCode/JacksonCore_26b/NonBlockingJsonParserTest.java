package com.fasterxml.jackson.core.json.async;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class NonBlockingJsonParserTest {

    private JsonFactory jsonFactory;
    private ByteQuadsCanonicalizer sym;
    private NonBlockingJsonParser parser;

    @Before
    public void setUp() throws Exception {
        jsonFactory = new JsonFactory();
        sym = ByteQuadsCanonicalizer.createRoot();
        // Create non-blocking parser using standard factory settings
        parser = (NonBlockingJsonParser) jsonFactory.createNonBlockingByteArrayParser();
    }

    @After
    public void tearDown() throws Exception {
        if (parser != null) {
            parser.close();
        }
    }

    @Test
    public void testBasicParsingEOF() throws IOException {
        assertEquals(JsonToken.NOT_AVAILABLE, parser.nextToken());
        
        // Feed empty or EOF
        parser.endOfInput();
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken()); // Depending on state or EOF handling, let's verify behavior
    }

    @Test
    public void testSimpleObjectParsing() throws IOException {
        byte[] input = "{\"a\": 1}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        parser.endOfInput();
        assertEquals(JsonToken.NOT_AVAILABLE, parser.nextToken());
    }

    @Test
    public void testArrayParsing() throws IOException {
        byte[] input = "[true, false, null, 1.5]".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5, parser.getDoubleValue(), 0.001);
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        parser.endOfInput();
    }

    @Test
    public void testStringEscapesAndNames() throws IOException {
        byte[] input = "{\"hello\\nworld\": \"foo\\\"bar\"}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("hello\nworld", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("foo\"bar", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        parser.endOfInput();
    }

    @Test(expected = IOException.class)
    public void testInvalidJsonThrowsException() throws IOException {
        byte[] input = "{ invalid }".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        while (parser.nextToken() != null && parser.nextToken() != JsonToken.NOT_AVAILABLE) {
            // consume until exception
        }
    }

    @Test
    public void testMultipleFeedInputs() throws IOException {
        byte[] part1 = "{\"a\":".getBytes("UTF-8");
        byte[] part2 = " 123}".getBytes("UTF-8");

        assertEquals(JsonToken.NOT_AVAILABLE, parser.nextToken());

        parser.feedInput(part1, 0, part1.length);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());

        // Now token might need more input
        assertEquals(JsonToken.NOT_AVAILABLE, parser.nextToken());

        parser.feedInput(part2, 0, part2.length);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        parser.endOfInput();
    }

    @Test
    public void testParserConfigAndFeatures() {
        assertNotNull(parser.getCodec());
        assertFalse(parser.isClosed());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());
    }

    @Test
    public void testGetTextCharactersAndLength() throws IOException {
        byte[] input = "{\"text\": \"abc\"}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        char[] chars = parser.getTextCharacters();
        assertNotNull(chars);
        assertTrue(parser.getTextLength() >= 0);
        assertTrue(parser.getTextOffset() >= 0);

        parser.endOfInput();
    }

    @Test
    public void testNumberParsingVariations() throws IOException {
        byte[] input = "[123, -456, 1.2e3, -0.5]".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-456, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1200.0, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.5, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.endOfInput();
    }

    @Test
    public void testParserClose() throws IOException {
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(expected = IOException.class)
    public void testFeedInputAfterEnd() throws IOException {
        parser.endOfInput();
        byte[] input = "{}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
    }
}