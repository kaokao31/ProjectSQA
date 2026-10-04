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
    private JsonParser baseParser;
    private TokenFilterContext rootContext;
    private FilteringParserDelegate parserDelegate;

    @Before
    public void setUp() throws Exception {
        jsonFactory = new JsonFactory();
        // Sample JSON string to parse
        String json = "{\"name\":\"John\",\"age\":30,\"isEmployee\":true,\"skills\":[\"Java\",\"C++\"],\"address\":{\"city\":\"New York\",\"zip\":10001},\"nullVal\":null,\"emptyArr\":[],\"emptyObj\":{}}";
        baseParser = jsonFactory.createParser(json);
    }

    @After
    public void tearDown() throws Exception {
        if (baseParser != null) {
            baseParser.close();
        }
        if (parserDelegate != null) {
            parserDelegate.close();
        }
    }

    @Test
    public void testTokenFilterAccessors() throws IOException {
        TokenFilter filter = new TokenFilter();
        parserDelegate = new FilteringParserDelegate(baseParser, filter, false, false);
        assertSame(filter, parserDelegate.getFilter());
        assertNotNull(parserDelegate.delegate());
        assertFalse(parserDelegate.inclusionAllowed());
    }

    @Test
    public void testEverythingFilteredOut() throws IOException {
        // A filter that includes nothing
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeElement(int index) { return null; }
            @Override
            public TokenFilter includeProperty(String name) { return null; }
            @Override
            public TokenFilter includeStringProperty(String name) { return null; }
        };

        parserDelegate = new FilteringParserDelegate(baseParser, filter, false, false);
        // Advancing should return null since everything is filtered out
        assertNull(parserDelegate.nextToken());
    }

    @Test
    public void testInclusionOfSpecificProperty() throws IOException {
        // Filter that includes only "name"
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("name".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        parserDelegate = new FilteringParserDelegate(baseParser, filter, true, true);
        
        assertEquals(JsonToken.START_OBJECT, parserDelegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parserDelegate.nextToken());
        assertEquals("name", parserDelegate.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parserDelegate.nextToken());
        assertEquals("John", parserDelegate.getText());
        assertEquals(JsonToken.END_OBJECT, parserDelegate.nextToken());
        assertNull(parserDelegate.nextToken());
    }

    @Test
    public void testMultipleTokensAndNestedObjects() throws IOException {
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("address".equals(name) || "city".equals(name)) {
                    return this;
                }
                if ("age".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        parserDelegate = new FilteringParserDelegate(baseParser, filter, false, false);
        while (parserDelegate.nextToken() != null) {
            // consume all
        }
    }

    @Test
    public void testValueAccessorsAndTypes() throws IOException {
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        parserDelegate = new FilteringParserDelegate(baseParser, filter, true, true);

        // START_OBJECT
        assertEquals(JsonToken.START_OBJECT, parserDelegate.nextToken());
        
        // FIELD_NAME: name
        assertEquals(JsonToken.FIELD_NAME, parserDelegate.nextToken());
        assertEquals("name", parserDelegate.getCurrentName());
        assertEquals("name", parserDelegate.getText());
        
        // VALUE_STRING: John
        assertEquals(JsonToken.VALUE_STRING, parserDelegate.nextToken());
        assertEquals("John", parserDelegate.getText());
        assertNotNull(parserDelegate.getTextCharacters());
        assertTrue(parserDelegate.getTextLength() > 0);
        assertTrue(parserDelegate.getTextOffset() >= 0);
        
        // FIELD_NAME: age
        assertEquals(JsonToken.FIELD_NAME, parserDelegate.nextToken());
        // VALUE_NUMBER_INT: 30
        assertEquals(JsonToken.VALUE_NUMBER_INT, parserDelegate.nextToken());
        assertEquals(30, parserDelegate.getIntValue());
        assertEquals(30L, parserDelegate.getLongValue());
        assertEquals(30.0f, parserDelegate.getFloatValue(), 0.001);
        assertEquals(30.0, parserDelegate.getDoubleValue(), 0.001);
        assertEquals(BigInteger.valueOf(30), parserDelegate.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(30), parserDelegate.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, parserDelegate.getNumberType());
        assertEquals(Integer.valueOf(30), parserDelegate.getNumberValue());

        // FIELD_NAME: isEmployee
        assertEquals(JsonToken.FIELD_NAME, parserDelegate.nextToken());
        // VALUE_TRUE: true
        assertEquals(JsonToken.VALUE_TRUE, parserDelegate.nextToken());
        assertTrue(parserDelegate.getBooleanValue());
        
        // Test embedded object / binary
        assertNull(parserDelegate.getEmbeddedObject());
    }

    @Test
    public void testOverriddenMethodsDelegation() throws IOException {
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        parserDelegate = new FilteringParserDelegate(baseParser, filter, true, true);

        assertNotNull(parserDelegate.getCodec());
        parserDelegate.setCodec(jsonFactory.getCodec());
        assertNotNull(parserDelegate.getInputSource());
        
        parserDelegate.nextToken(); // START_OBJECT
        assertFalse(parserDelegate.hasCurrentToken());
        assertTrue(parserDelegate.hasToken(JsonToken.START_OBJECT));
        assertTrue(parserDelegate.hasTokenId(JsonToken.START_OBJECT.id()));

        // Check helper methods
        assertNotNull(parserDelegate.getParsingContext());
        assertNotNull(parserDelegate.getTokenLocation());
        assertNotNull(parserDelegate.getCurrentLocation());
        
        parserDelegate.clearCurrentToken();
        assertNull(parserDelegate.currentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parserDelegate.currentTokenId());
    }

    @Test
    public void testOverridesBinaryAndStreamRead() throws IOException {
        String jsonWithBinary = "{\"data\":\"AQID\"}";
        JsonParser binParser = jsonFactory.createParser(jsonWithBinary);
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate binDelegate = new FilteringParserDelegate(binParser, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, binDelegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, binDelegate.nextToken());
        assertEquals(JsonToken.VALUE_STRING, binDelegate.nextToken());
        
        assertNotNull(binDelegate.getBinaryValue(Base64Variants.MIME));
        
        byte[] buffer = new byte[10];
        assertEquals(3, binDelegate.readBinaryValue(buffer));

        binDelegate.close();
        assertTrue(binDelegate.isClosed());
    }

    @Test
    public void testOverrideSkipChildren() throws IOException {
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        parserDelegate = new FilteringParserDelegate(baseParser, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, parserDelegate.nextToken());
        parserDelegate.skipChildren();
    }

    @Test
    public void testScopeAndPathContext() throws IOException {
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        parserDelegate = new FilteringParserDelegate(baseParser, filter, true, true);

        assertNull(parserDelegate.getNonEmptyChildFilter(filter));
        
        // Force various paths inside FilteringParserDelegate
        while (parserDelegate.nextToken() != null) {
            parserDelegate.getCurrentName();
        }
    }

    @Test
    public void testPathMatchingEdges() throws IOException {
        // Test with different includePath configurations
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("skills".equals(name)) {
                    return this;
                }
                if ("Java".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
            @Override
            public TokenFilter includeElement(int index) {
                return this;
            }
        };

        parserDelegate = new FilteringParserDelegate(baseParser, filter, false, true);
        while (parserDelegate.nextToken() != null) {
            // consume
        }

        FilteringParserDelegate parserDelegate2 = new FilteringParserDelegate(baseParser, filter, true, false);
        while (parserDelegate2.nextToken() != null) {
            // consume
        }
        parserDelegate2.close();
    }

    @Test
    public void testNumberTypesAndParsingFailSafes() throws IOException {
        String jsonNum = "{\"float\":10.5,\"long\":9223372036854775807}";
        JsonParser numParser = jsonFactory.createParser(jsonNum);
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate numDelegate = new FilteringParserDelegate(numParser, filter, true, true);

        while (numDelegate.nextToken() != null) {
            if (numDelegate.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
                assertEquals(10.5, numDelegate.getDoubleValue(), 0.001);
                assertEquals(10.5f, numDelegate.getFloatValue(), 0.001);
                assertEquals(BigDecimal.valueOf(10.5), numDelegate.getDecimalValue());
                assertEquals(JsonParser.NumberType.BIG_DECIMAL, numDelegate.getNumberType());
            }
            if (numDelegate.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                try {
                    numDelegate.getIntValue();
                } catch (Exception e) {
                    // expected overflow for int on max long
                }
                assertEquals(9223372036854775807L, numDelegate.getLongValue());
                assertEquals(JsonParser.NumberType.LONG, numDelegate.getNumberType());
            }
        }
        numDelegate.close();
    }
}