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
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ReaderBasedJsonParserTest {

    private IOContext ioContext;
    private CharsToNameCanonicalizer symbols;
    private BufferRecycler recycler;

    @Before
    public void setUp() {
        recycler = new BufferRecycler();
        ioContext = new IOContext(recycler, recycler, false);
        symbols = CharsToNameCanonicalizer.createRoot();
    }

    @After
    public void tearDown() {
        // cleanup if needed
    }

    private ReaderBasedJsonParser createParser(String json) {
        char[] chars = json.toCharArray();
        CharArrayReader reader = new CharArrayReader(chars);
        return new ReaderBasedJsonParser(ioContext, 0, reader, null, symbols, chars, 0, chars.length, true);
    }

    @Test
    public void testFloatParsingIntVsLong() throws IOException {
        // Specifically targeting JacksonCore Bug 19 where floating point numbers 
        // that fit into long/int or overflow are parsed.
        String json = "1234567890123.45";
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());
        assertEquals(1234567890123.45, parser.getDoubleValue(), 0.001);
        assertEquals(new BigDecimal("1234567890123.45"), parser.getDecimalValue());
        
        parser.close();
    }

    @Test
    public void testFloatParsingBigNumber() throws IOException {
        String json = "1e300";
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());
        
        parser.close();
    }

    @Test
    public void testFloatParsingDoubleOverflow() throws IOException {
        // Test very large float that overflows double
        String json = "1e99999";
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());
        
        parser.close();
    }

    @Test
    public void testIntParsing() throws IOException {
        String json = "12345";
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals(12345, parser.getIntValue());
        
        parser.close();
    }

    @Test
    public void testLongParsing() throws IOException {
        String json = "2147483648"; // Integer.MAX_VALUE + 1
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());
        assertEquals(2147483648L, parser.getLongValue());
        
        parser.close();
    }

    @Test
    public void testBigIntParsing() throws IOException {
        String json = "9223372036854775808"; // Long.MAX_VALUE + 1
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, parser.getNumberType());
        assertEquals(new BigInteger("9223372036854775808"), parser.getBigIntegerValue());
        
        parser.close();
    }

    @Test
    public void testBasicParsingStructure() throws IOException {
        String json = "{\"key\": [true, false, null, 1.5]}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        parser.close();
    }

    @Test
    public void testStringEscapesAndUnicode() throws IOException {
        String json = "\"\\n\\r\\t\\u0041\"";
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\n\r\tA", parser.getText());
        
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidJson() throws IOException {
        String json = "{\"key\": ";
        ReaderBasedJsonParser parser = createParser(json);
        try {
            parser.nextToken();
            parser.nextToken();
        } finally {
            parser.close();
        }
    }

    @Test
    public void testGetTextCharacters() throws IOException {
        String json = "\"hello world\"";
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        char[] chars = parser.getTextCharacters();
        assertNotNull(chars);
        assertTrue(parser.getTextLength() > 0);
        assertTrue(parser.getTextOffset() >= 0);
        
        parser.close();
    }

    @Test
    public void testParserFeature() throws IOException {
        String json = "{#comment}\"key\" : 1}";
        char[] chars = json.toCharArray();
        CharArrayReader reader = new CharArrayReader(chars);
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ioContext, JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask(), reader, null, symbols, chars, 0, chars.length, true);
        
        parser.enable(JsonParser.Feature.ALLOW_COMMENTS);
        assertTrue(parser.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        
        parser.close();
    }
}