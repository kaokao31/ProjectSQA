package com.fasterxml.jackson.core.filter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class FilteringParserDelegateTest {

    private final JsonFactory factory = new JsonFactory();

    private JsonParser filteredParser(String json, TokenFilter filter,
            boolean includePath, boolean allowMultipleMatches) throws IOException {
        return new FilteringParserDelegate(factory.createParser(json), filter,
                includePath, allowMultipleMatches);
    }

    private static List<JsonToken> readTokens(JsonParser p) throws IOException {
        List<JsonToken> tokens = new ArrayList<JsonToken>();
        JsonToken t;
        while ((t = p.nextToken()) != null) {
            tokens.add(t);
        }
        return tokens;
    }

    @Test
    public void testIncludeAllTokens() throws Exception {
        String json = "{\"a\":1,\"b\":[true,false],\"c\":\"text\",\"d\":null}";
        try (JsonParser p = filteredParser(json, new TokenFilter(), true, true)) {
            assertEquals(Arrays.asList(
                    JsonToken.START_OBJECT,
                    JsonToken.FIELD_NAME,
                    JsonToken.VALUE_NUMBER_INT,
                    JsonToken.FIELD_NAME,
                    JsonToken.START_ARRAY,
                    JsonToken.VALUE_TRUE,
                    JsonToken.VALUE_FALSE,
                    JsonToken.END_ARRAY,
                    JsonToken.FIELD_NAME,
                    JsonToken.VALUE_STRING,
                    JsonToken.FIELD_NAME,
                    JsonToken.VALUE_NULL,
                    JsonToken.END_OBJECT),
                    readTokens(p));
        }
    }

    @Test
    public void testFilterObjectIncludePathTrue() throws Exception {
        String json = "{\"a\":1,\"b\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "a".equals(name) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, true, true)) {
            assertEquals(Arrays.asList(
                    JsonToken.START_OBJECT,
                    JsonToken.FIELD_NAME,
                    JsonToken.VALUE_NUMBER_INT,
                    JsonToken.END_OBJECT),
                    readTokens(p));
        }
    }

    @Test
    public void testFilterObjectIncludePathFalse() throws Exception {
        String json = "{\"a\":1,\"b\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "a".equals(name) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, false, true)) {
            assertEquals(Collections.singletonList(JsonToken.VALUE_NUMBER_INT), readTokens(p));
        }
    }

    @Test
    public void testFilterObjectMultipleMatchesIncludePathFalse() throws Exception {
        String json = "{\"a\":1,\"b\":2,\"c\":3}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return ("a".equals(name) || "c".equals(name)) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, false, true)) {
            assertEquals(Arrays.asList(
                    JsonToken.VALUE_NUMBER_INT,
                    JsonToken.VALUE_NUMBER_INT),
                    readTokens(p));
        }
    }

    @Test
    public void testFilterArrayIncludePathFalse() throws Exception {
        String json = "[1,2,3,4]";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeElement(int index) {
                return (index % 2 == 0) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, false, true)) {
            assertEquals(Arrays.asList(
                    JsonToken.VALUE_NUMBER_INT,
                    JsonToken.VALUE_NUMBER_INT),
                    readTokens(p));
        }
    }

    @Test
    public void testFilterArrayIncludePathTrue() throws Exception {
        String json = "[1,2]";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeElement(int index) {
                return (index == 0) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, true, true)) {
            assertEquals(Arrays.asList(
                    JsonToken.START_ARRAY,
                    JsonToken.VALUE_NUMBER_INT,
                    JsonToken.END_ARRAY),
                    readTokens(p));
        }
    }

    @Test
    public void testAllowMultipleMatchesFalseStopsAfterFirstValue() throws Exception {
        String json = "[1,2,3]";
        try (JsonParser p = filteredParser(json, new TokenFilter(), false, false)) {
            assertEquals(Collections.singletonList(JsonToken.VALUE_NUMBER_INT), readTokens(p));
        }
    }

    @Test
    public void testRootValueWithIncludeAll() throws Exception {
        String json = "true";
        try (JsonParser p = filteredParser(json, new TokenFilter(), false, true)) {
            assertEquals(Collections.singletonList(JsonToken.VALUE_TRUE), readTokens(p));
        }
    }

    @Test
    public void testNestedPathIncluded() throws Exception {
        String json = "{\"root\":{\"a\":1},\"skip\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return ("root".equals(name) || "a".equals(name)) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, true, true)) {
            assertEquals(Arrays.asList(
                    JsonToken.START_OBJECT,
                    JsonToken.FIELD_NAME,
                    JsonToken.START_OBJECT,
                    JsonToken.FIELD_NAME,
                    JsonToken.VALUE_NUMBER_INT,
                    JsonToken.END_OBJECT,
                    JsonToken.END_OBJECT),
                    readTokens(p));
        }
    }

    @Test
    public void testSkipNonMatchingNestedValue() throws Exception {
        String json = "{\"a\":{\"x\":1,\"y\":2},\"b\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "b".equals(name) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, false, true)) {
            assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2, p.getIntValue());
            assertNull(p.nextToken());
        }
    }

    @Test
    public void testNextValueWithFiltering() throws Exception {
        String json = "{\"a\":1,\"b\":2,\"c\":3}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return ("a".equals(name) || "c".equals(name)) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, false, true)) {
            assertSame(JsonToken.VALUE_NUMBER_INT, p.nextValue());
            assertEquals(1, p.getIntValue());
            assertSame(JsonToken.VALUE_NUMBER_INT, p.nextValue());
            assertEquals(3, p.getIntValue());
            assertNull(p.nextValue());
        }
    }

    @Test
    public void testNextValueWithIncludePathTrue() throws Exception {
        String json = "{\"a\":1}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "a".equals(name) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, true, true)) {
            assertSame(JsonToken.START_OBJECT, p.nextValue());
            assertSame(JsonToken.VALUE_NUMBER_INT, p.nextValue());
            assertEquals(1, p.getIntValue());
            assertSame(JsonToken.END_OBJECT, p.nextValue());
            assertNull(p.nextValue());
        }
    }

    @Test
    public void testPropertyWithValueFilter() throws Exception {
        String json = "{\"a\":4,\"b\":2}";
        final TokenFilter valueFilter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() > 2;
            }
        };
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "a".equals(name) ? valueFilter : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, false, true)) {
            assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(4, p.getIntValue());
            assertNull(p.nextToken());
        }
    }

    @Test
    public void testValueFilterRejectsAll() throws Exception {
        String json = "{\"a\":4}";
        final TokenFilter reject = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return false;
            }
        };
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "a".equals(name) ? reject : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, false, true)) {
            assertEquals(Collections.<JsonToken>emptyList(), readTokens(p));
        }
    }

    @Test
    public void testCurrentNameWhenPathNotIncluded() throws Exception {
        String json = "{\"a\":1,\"b\":2}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return ("a".equals(name) || "b".equals(name)) ? this : null;
            }
        };
        try (JsonParser p = filteredParser(json, filter, false, true)) {
            assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals("a", p.getCurrentName());
            assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals("b", p.getCurrentName());
            assertNull(p.nextToken());
        }
    }
}