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
        jsonFactory = null;
    }

    private JsonParser createParser(String content) throws IOException {
        return jsonFactory.createParser(new StringReader(content));
    }

    @Test
    public void testBasicDelegationAndFiltering() throws IOException {
        String json = "{\"a\":1, \"b\":2, \"c\":3}";
        JsonParser p = createParser(json);
        
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("a".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(
                p, filter, false, false
        );

        assertEquals(p.getCodec(), delegate.getCodec());
        
        ObjectCodec codec = jsonFactory.getCodec();
        delegate.setCodec(codec);
        assertSame(codec, delegate.getCodec());

        assertNotNull(delegate.getInputSource());
        assertNotNull(delegate.version());

        delegate.close();
        assertTrue(delegate.isClosed());
        
        p.close();
    }

    @Test
    public void testDelegateMethods() throws IOException {
        String json = "{\"a\":[1,2], \"b\":{\"x\":10}}";
        JsonParser p = createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true
        );

        assertNotNull(delegate.getDelegate());
        assertNotNull(delegate.getCurrentToken());
        assertNotNull(delegate.currentToken());
        assertNotNull(delegate.currentTokenId());
        
        // Navigation / token consumption
        JsonToken t;
        while ((t = delegate.nextToken()) != null) {
            // exercise current name / text / values based on token
            if (delegate.hasCurrentToken()) {
                assertNotNull(delegate.getCurrentName());
            }
            if (t == JsonToken.VALUE_NUMBER_INT) {
                assertEquals(p.getIntValue(), delegate.getIntValue());
                assertEquals(p.getLongValue(), delegate.getLongValue());
                assertEquals(p.getBigIntegerValue(), delegate.getBigIntegerValue());
                assertEquals(p.getDecimalValue(), delegate.getDecimalValue());
                assertEquals(p.getDoubleValue(), delegate.getDoubleValue(), 0.0001);
                assertEquals(p.getFloatValue(), delegate.getFloatValue(), 0.0001f);
                assertNotNull(delegate.getNumberType());
                assertNotNull(delegate.getNumberValue());
            } else if (t == JsonToken.VALUE_STRING) {
                assertEquals(p.getText(), delegate.getText());
                assertNotNull(delegate.getTextCharacters());
                assertTrue(delegate.getTextLength() >= 0);
                assertTrue(delegate.getTextOffset() >= 0);
            }
        }

        delegate.clearCurrentToken();
        assertNull(delegate.getCurrentToken());

        delegate.overrideCurrentName("overridden");
        assertEquals("overridden", delegate.getCurrentName());

        assertNotNull(delegate.getParsingContext());
        assertNotNull(delegate.getTokenLocation());
        assertNotNull(delegate.getCurrentLocation());

        delegate.close();
    }

    @Test
    public void testNumberAndBinaryParsing() throws IOException {
        String json = "{\"num\": 123.45, \"bin\": \"AQID\"}";
        JsonParser p = createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, false, false
        );

        while (delegate.nextToken() != null) {
            if (delegate.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
                assertEquals(p.getDecimalValue(), delegate.getDecimalValue());
                assertEquals(p.getDoubleValue(), delegate.getDoubleValue(), 0.00001);
                assertEquals(p.getFloatValue(), delegate.getFloatValue(), 0.00001f);
            }
            if (delegate.hasTokenId(JsonToken.ID_STRING)) {
                try {
                    delegate.getBinaryValue(Base64Variants.MIME);
                } catch (Exception e) {
                    // expected if not binary context, but exercises the method
                }
            }
        }
        delegate.close();
    }

    @Test
    public void testFeatureMethods() throws IOException {
        String json = "{\"a\":1}";
        JsonParser p = createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, false, false
        );

        delegate.enable(JsonParser.Feature.ALLOW_COMMENTS);
        assertTrue(delegate.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        delegate.disable(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(delegate.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        delegate.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(delegate.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        int features = delegate.getFormatFeatures();
        assertTrue(features >= 0);

        delegate.overrideStdFeatures(0, 0);
        delegate.overrideFormatFeatures(0, 0);

        delegate.close();
    }

    @Test
    public void testTokenStreamAndUnmatched() throws IOException {
        String json = "{\"exclude\": 1, \"include\": 2}";
        JsonParser p = createParser(json);
        
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("include".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(
                p, filter, true, true
        );

        while (delegate.nextToken() != null) {
            // consume all
        }

        delegate.skipChildren();
        assertNull(delegate.nextTextValue());
        assertEquals(10, delegate.nextIntValue(10));
        assertEquals(20L, delegate.nextLongValue(20L));
        assertNull(delegate.nextBooleanValue());

        delegate.close();
    }

    @Test
    public void testReadDataAs() throws IOException {
        String json = "{\"a\": 1}";
        JsonParser p = createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, false, false
        );

        while (delegate.nextToken() != null) {
            if (delegate.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                assertEquals(Integer.valueOf(1), delegate.readValueAs(Integer.class));
                assertNotNull(delegate.readValueAsTree());
                break;
            }
        }
        delegate.close();
    }
}