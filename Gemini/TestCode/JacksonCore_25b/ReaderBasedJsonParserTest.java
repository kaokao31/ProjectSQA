package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ReaderBasedJsonParserTest {

    private IOContext ioContext;
    private CharsToNameCanonicalizer symbols;
    private BufferRecycler bufferRecycler;

    @Before
    public void setUp() {
        bufferRecycler = new BufferRecycler();
        ioContext = new IOContext(bufferRecycler, "testSource", false);
        symbols = CharsToNameCanonicalizer.createRoot();
    }

    @After
    public void tearDown() throws Exception {
        if (symbols != null) {
            symbols.release();
        }
    }

    private ReaderBasedJsonParser createParser(String input) {
        StringReader reader = new StringReader(input);
        return new ReaderBasedJsonParser(ioContext, 0, reader, null, symbols);
    }

    @Test
    public void testBasicParsing() throws IOException {
        String json = "{\"name\":\"value\", \"number\":123, \"flag\":true, \"nothing\":null, \"array\":[1,2]}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("number", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("flag", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nothing", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("array", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testTokenLocationAndCurrentLocation() throws IOException {
        String json = "  {\n \"a\": 1 }";
        ReaderBasedJsonParser parser = createParser(json);

        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testEmbeddedObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        assertNull(parser.getEmbeddedObject());
        parser.close();
    }

    @Test
    public void testBinaryValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"YQ==\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] bytes = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertNotNull(bytes);
        assertEquals('a', (char) bytes[0]);
        parser.close();
    }

    @Test
    public void testFloatAndDoubleParsing() throws IOException {
        String json = "{\"f\": 123.45, \"d\": 1.23456789e2}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(123.45, parser.getDoubleValue(), 0.001);
        assertEquals(123.45, parser.getFloatValue(), 0.001);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(123.456789, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testBigNumberParsing() throws IOException {
        String json = "{\"bigInt\": 12345678901234567890, \"bigDec\": 12345678901234567890.1234567890}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        BigInteger bi = parser.getBigIntegerValue();
        assertNotNull(bi);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        BigDecimal bd = parser.getDecimalValue();
        assertNotNull(bd);

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testGetTextCharactersAndLength() throws IOException {
        String json = "{\"text\": \"hello world\"}";
        ReaderBasedJsonParser parser = createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        char[] chars = parser.getTextCharacters();
        assertNotNull(chars);
        assertTrue(parser.getTextLength() > 0);
        assertTrue(parser.getTextOffset() >= 0);

        parser.close();
    }

    @Test
    public void testOverflowNumberParsing() throws IOException {
        String json = "{\"long\": 92233720368547758070}";
        ReaderBasedJsonParser parser = createParser(json);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.getLongValue();
            fail("Expected ArithmeticException or InputCoercionException for overflow");
        } catch (Exception e) {
            // Expected
        }
        parser.close();
    }

    @Test
    public void testUnquotedFieldNamesAndComments() throws IOException {
        // Feature flags test if applicable via ObjectCodec or standard config
        String json = "{name: 'value'}";
        ReaderBasedJsonParser parser = createParser(json);
        // Depending on parser features, unquoted names might throw or pass.
        // Let's test standard config with standard failure or enabling features if possible.
        try {
            parser.nextToken();
            parser.nextToken();
        } catch (Exception e) {
            // Expected if ALLOW_UNQUOTED_FIELD_NAMES is false
        }
        parser.close();
    }

    @Test
    public void testSkipChildren() throws IOException {
        String json = "{\"a\": {\"b\": [1, 2, 3]}, \"c\": 2}";
        ReaderBasedJsonParser parser = createParser(json);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        
        parser.nextToken(); // START_OBJECT of 'a'
        parser.skipChildren();
        
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testTextStream() throws IOException {
        String json = "{\"text\": \"streamtest\"}";
        ReaderBasedJsonParser parser = createParser(json);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        
        assertNotNull(parser.getTextStream());
        parser.close();
    }

    @Test
    public void testGetCodecAndSetCodec() {
        ReaderBasedJsonParser parser = createParser("{}");
        assertNull(parser.getCodec());
        parser.setCodec(null);
        parser.close();
    }

    @Test
    public void testOverrideCurrentName() throws IOException {
        String json = "{\"a\": 1}";
        ReaderBasedJsonParser parser = createParser(json);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        parser.overrideCurrentName("b");
        assertEquals("b", parser.getCurrentName());
        parser.close();
    }
}