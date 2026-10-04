package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class FilteringParserDelegateTest {

    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        jsonFactory = new JsonFactory();
    }

    @After
    public void tearDown() {
        // Cleanup if needed
    }

    @Test
    public void testBasicFilteringAndTokenDelegation() throws IOException {
        String json = "{\"a\":1, \"b\":2, \"c\":[3, 4]}";
        JsonParser p = jsonFactory.createParser(json);
        
        TokenFilter filter = new NameFilter("a");
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, false);

        assertNotNull(fp.getDelegate());
        assertNotNull(fp.getFilter());

        // Test tokens
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("a", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(1, fp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertNull(fp.nextToken());

        fp.close();
        assertTrue(fp.isClosed());
    }

    @Test
    public void testIncludePathOption() throws IOException {
        String json = "{\"a\":{\"b\":2}}";
        JsonParser p = jsonFactory.createParser(json);
        
        TokenFilter filter = new NameFilter("b");
        // includePath = true
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, false);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("a", fp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("b", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(2, fp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        assertEquals(JsonToken.END_OBJECT, fp.nextToken());

        fp.close();
    }

    @Test
    public void testSkipChildrenAndValueMethods() throws IOException {
        String json = "{\"a\":[1,2,3], \"b\":4}";
        JsonParser p = jsonFactory.createParser(json);
        
        TokenFilter filter = new NameFilter("b");
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, false);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals("b", fp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        
        // Test various value retrieval methods
        assertEquals(4, fp.getIntValue());
        assertEquals(4L, fp.getLongValue());
        assertEquals(4.0f, fp.getFloatValue(), 0.001f);
        assertEquals(4.0, fp.getDoubleValue(), 0.001);
        assertEquals(BigInteger.valueOf(4), fp.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(4), fp.getDecimalValue());
        assertEquals("4", fp.getText());
        assertNotNull(fp.getTextCharacters());
        assertEquals(0, fp.getTextOffset());
        assertEquals(1, fp.getTextLength());

        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        fp.close();
    }

    @Test
    public void testOverriddenParserMethods() throws IOException {
        String json = "{\"a\": \"hello\"}";
        JsonParser p = jsonFactory.createParser(json);
        
        TokenFilter filter = new NameFilter("a");
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        assertNotNull(fp.getCodec());
        fp.setCodec(jsonFactory.getCodec());

        assertEquals(JsonStreamContext.class, fp.getParsingContext().getClass());
        assertEquals(JsonLocation.class, fp.getTokenLocation().getClass());
        assertEquals(JsonLocation.class, fp.getCurrentLocation().getClass());

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_STRING, fp.nextToken());
        assertEquals("hello", fp.getValueAsString());

        fp.clearCurrentToken();
        assertNull(fp.currentToken());
        assertNull(fp.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, fp.getCurrentTokenId());
        assertFalse(fp.hasCurrentToken());
        assertTrue(fp.hasTokenId(JsonTokenId.ID_NO_TOKEN));

        fp.close();
    }

    @Test
    public void testBinaryAndEmbeddedData() throws IOException {
        String json = "{\"a\": \"aGVsbG8=\"}"; // base64 for hello
        JsonParser p = jsonFactory.createParser(json);
        
        TokenFilter filter = new NameFilter("a");
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, false);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_STRING, fp.nextToken());

        byte[] binary = fp.getBinaryValue(Base64Variants.MIME);
        assertNotNull(binary);

        assertNull(fp.getEmbeddedObject());

        fp.close();
    }

    @Test
    public void testOverrideCurrentToken() throws IOException {
        String json = "{\"a\": 1}";
        JsonParser p = jsonFactory.createParser(json);
        
        TokenFilter filter = new NameFilter("a");
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, false);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        
        fp.overrideCurrentToken(JsonToken.VALUE_STRING);
        assertEquals(JsonToken.VALUE_STRING, fp.currentToken());

        fp.close();
    }

    @Test
    public void testNumberTypeAndParsingBoundaries() throws IOException {
        String json = "{\"a\": 123, \"b\": 123.45}";
        JsonParser p = jsonFactory.createParser(json);
        
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        assertEquals(JsonParser.NumberType.INT, fp.getNumberType());
        assertEquals(Number.class, fp.getNumberValue().getClass());

        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, fp.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, fp.getNumberType());

        assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        fp.close();
    }

    @Test
    public void testReadBinaryValueWithOutputStream() throws IOException {
        String json = "{\"a\": \"aGVsbG8=\"}";
        JsonParser p = jsonFactory.createParser(json);
        
        TokenFilter filter = new NameFilter("a");
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, false);

        assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        assertEquals(JsonToken.VALUE_STRING, fp.nextToken());

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        int bytesRead = fp.readBinaryValue(Base64Variants.MIME, bos);
        assertTrue(bytesRead > 0);

        fp.close();
    }

    @Test
    public void testOverriddenVersionAndFeatureMethods() throws IOException {
        String json = "{}";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        assertNotNull(fp.version());
        
        fp.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertFalse(fp.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));

        fp.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        assertTrue(fp.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));

        fp.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        assertFalse(fp.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));

        fp.overrideStdFeatures(0, 0);
        
        fp.close();
    }
}