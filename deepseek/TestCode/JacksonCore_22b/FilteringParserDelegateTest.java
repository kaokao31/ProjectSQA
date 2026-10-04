package com.fasterxml.jackson.core.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class FilteringParserDelegateTest {

    private static class IncludeOnlyFieldsFilter extends TokenFilter {
        private final Set<String> include;

        IncludeOnlyFieldsFilter(String... fields) {
            include = new HashSet<String>(Arrays.asList(fields));
        }

        @Override
        public TokenFilter filterStartObject() {
            return this;
        }

        @Override
        public TokenFilter filterStartArray() {
            return this;
        }

        @Override
        public TokenFilter filterFieldName(String name) {
            return include.contains(name) ? TokenFilter.INCLUDE_ALL : null;
        }

        @Override
        public TokenFilter filterValue(JsonToken t) {
            return TokenFilter.INCLUDE_ALL;
        }
    }

    private static class SkipObjectElementsFilter extends TokenFilter {
        @Override
        public TokenFilter filterStartArray() {
            return this;
        }

        @Override
        public TokenFilter filterStartObject() {
            return null;
        }

        @Override
        public TokenFilter filterFieldName(String name) {
            return null;
        }

        @Override
        public TokenFilter filterValue(JsonToken t) {
            return TokenFilter.INCLUDE_ALL;
        }
    }

    private static class StringValuesOnlyFilter extends TokenFilter {
        @Override
        public TokenFilter filterStartArray() {
            return this;
        }

        @Override
        public TokenFilter filterStartObject() {
            return this;
        }

        @Override
        public TokenFilter filterFieldName(String name) {
            return null;
        }

        @Override
        public TokenFilter filterString(String value) {
            return TokenFilter.INCLUDE_ALL;
        }

        @Override
        public TokenFilter filterValue(JsonToken t) {
            return (t == JsonToken.VALUE_STRING) ? TokenFilter.INCLUDE_ALL : null;
        }
    }

    private static class NullFilter extends TokenFilter { }

    private void assertSequence(String json, TokenFilter filter, boolean includePath,
            boolean allowMultipleMatches, Object... expected) throws Exception {
        JsonParser underlying = new JsonFactory().createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(underlying, filter,
                includePath, allowMultipleMatches);
        try {
            for (Object exp : expected) {
                JsonToken actual = parser.nextToken();
                assertNotNull("Expected token for " + exp + " but got null", actual);
                if (exp instanceof JsonToken) {
                    assertSame(exp, actual);
                } else if (exp instanceof String) {
                    if (actual == JsonToken.FIELD_NAME) {
                        assertEquals(exp, parser.getCurrentName());
                    } else {
                        assertEquals(JsonToken.VALUE_STRING, actual);
                        assertEquals(exp, parser.getText());
                    }
                } else if (exp instanceof Integer) {
                    assertEquals(JsonToken.VALUE_NUMBER_INT, actual);
                    assertEquals(((Integer) exp).intValue(), parser.getIntValue());
                } else if (exp instanceof Long) {
                    assertEquals(JsonToken.VALUE_NUMBER_INT, actual);
                    assertEquals(((Long) exp).longValue(), parser.getLongValue());
                } else if (exp instanceof Double) {
                    assertEquals(JsonToken.VALUE_NUMBER_FLOAT, actual);
                    assertEquals(((Double) exp).doubleValue(), parser.getDoubleValue(), 0.0);
                } else if (exp instanceof Boolean) {
                    if (((Boolean) exp).booleanValue()) {
                        assertEquals(JsonToken.VALUE_TRUE, actual);
                    } else {
                        assertEquals(JsonToken.VALUE_FALSE, actual);
                    }
                } else if (exp == null) {
                    assertEquals(JsonToken.VALUE_NULL, actual);
                } else {
                    fail("Unsupported expected value: " + exp + " (" + exp.getClass().getName() + ")");
                }
            }
            assertNull("Expected end of stream", parser.nextToken());
        } finally {
            underlying.close();
        }
    }

    @Test
    public void testIncludeAllObject() throws Exception {
        assertSequence("{\"a\":1,\"b\":\"two\",\"c\":true,\"d\":null}",
                TokenFilter.INCLUDE_ALL, false, false,
                JsonToken.START_OBJECT,
                "a", JsonToken.VALUE_NUMBER_INT,
                "b", "two",
                "c", JsonToken.VALUE_TRUE,
                "d", JsonToken.VALUE_NULL,
                JsonToken.END_OBJECT);
    }

    @Test
    public void testIncludeAllArray() throws Exception {
        assertSequence("[1,\"x\",true,null]",
                TokenFilter.INCLUDE_ALL, false, false,
                JsonToken.START_ARRAY,
                JsonToken.VALUE_NUMBER_INT,
                "x",
                JsonToken.VALUE_TRUE,
                JsonToken.VALUE_NULL,
                JsonToken.END_ARRAY);
    }

    @Test
    public void testRootScalarsWithIncludeAll() throws Exception {
        assertSequence("123", TokenFilter.INCLUDE_ALL, false, false,
                JsonToken.VALUE_NUMBER_INT);
        assertSequence("true", TokenFilter.INCLUDE_ALL, false, false,
                JsonToken.VALUE_TRUE);
        assertSequence("null", TokenFilter.INCLUDE_ALL, false, false,
                JsonToken.VALUE_NULL);
        assertSequence("\"root\"", TokenFilter.INCLUDE_ALL, false, false,
                "root");
    }

    @Test
    public void testFilteredScalarFieldsAreSkipped() throws Exception {
        assertSequence("{\"keep\":1,\"skip\":2,\"keep2\":3}",
                new IncludeOnlyFieldsFilter("keep", "keep2"), false, false,
                JsonToken.START_OBJECT,
                "keep", JsonToken.VALUE_NUMBER_INT,
                "keep2", JsonToken.VALUE_NUMBER_INT,
                JsonToken.END_OBJECT);
    }

    @Test
    public void testFilteredFieldNestedObjectIsSkipped() throws Exception {
        assertSequence("{\"keep\":1,\"skip\":{\"a\":[1,2]},\"keep2\":2}",
                new IncludeOnlyFieldsFilter("keep", "keep2"), false, false,
                JsonToken.START_OBJECT,
                "keep", JsonToken.VALUE_NUMBER_INT,
                "keep2", JsonToken.VALUE_NUMBER_INT,
                JsonToken.END_OBJECT);
    }

    @Test
    public void testFilteredFieldNestedArrayIsSkipped() throws Exception {
        assertSequence("{\"a\":1,\"skip\":[1,{\"x\":2}],\"b\":2}",
                new IncludeOnlyFieldsFilter("a", "b"), false, false,
                JsonToken.START_OBJECT,
                "a", JsonToken.VALUE_NUMBER_INT,
                "b", JsonToken.VALUE_NUMBER_INT,
                JsonToken.END_OBJECT);
    }

    @Test
    public void testFilteredObjectInsideArrayIsSkipped() throws Exception {
        assertSequence("[1,{\"a\":2},3]",
                new SkipObjectElementsFilter(), false, false,
                JsonToken.START_ARRAY,
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.END_ARRAY);
    }

    @Test
    public void testArrayElementsFilteredByStringValueType() throws Exception {
        assertSequence("[\"keep\",123,\"more\",true]",
                new StringValuesOnlyFilter(), false, false,
                JsonToken.START_ARRAY,
                "keep",
                "more",
                JsonToken.END_ARRAY);
    }

    @Test
    public void testRootObjectFilteredOut() throws Exception {
        JsonParser underlying = new JsonFactory().createParser("{\"a\":1}");
        FilteringParserDelegate parser = new FilteringParserDelegate(underlying, new NullFilter(),
                false, false);
        try {
            assertNull(parser.nextToken());
        } finally {
            underlying.close();
        }
    }

    @Test
    public void testRootArrayFilteredOut() throws Exception {
        JsonParser underlying = new JsonFactory().createParser("[1,2]");
        FilteringParserDelegate parser = new FilteringParserDelegate(underlying, new NullFilter(),
                false, false);
        try {
            assertNull(parser.nextToken());
        } finally {
            underlying.close();
        }
    }

    @Test
    public void testRootScalarFilteredOut() throws Exception {
        JsonParser underlying = new JsonFactory().createParser("123");
        FilteringParserDelegate parser = new FilteringParserDelegate(underlying, new NullFilter(),
                false, false);
        try {
            assertNull(parser.nextToken());
        } finally {
            underlying.close();
        }
    }

    @Test
    public void testSkipChildrenOnArray() throws Exception {
        JsonParser underlying = new JsonFactory().createParser("{\"a\":[1,{\"b\":2}],\"c\":3}");
        FilteringParserDelegate parser = new FilteringParserDelegate(underlying,
                TokenFilter.INCLUDE_ALL, false, false);
        try {
            assertSame(JsonToken.START_OBJECT, parser.nextToken());
            assertSame(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("a", parser.getCurrentName());
            assertSame(JsonToken.START_ARRAY, parser.nextToken());
            assertSame(JsonToken.END_ARRAY, parser.skipChildren());
            assertSame(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("c", parser.getCurrentName());
            assertSame(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(3, parser.getIntValue());
            assertSame(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        } finally {
            underlying.close();
        }
    }

    @Test
    public void testNumberAccessors() throws Exception {
        String json = "{\"small\":1,\"big\":1234567890123,\"dec\":1.25,"
                + "\"bigint\":123456789012345678901234567890}";
        JsonParser underlying = new JsonFactory().createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(underlying,
                TokenFilter.INCLUDE_ALL, false, false);
        try {
            assertSame(JsonToken.START_OBJECT, parser.nextToken());

            assertSame(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("small", parser.getCurrentName());
            assertSame(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(1, parser.getIntValue());
            assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

            assertSame(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("big", parser.getCurrentName());
            assertSame(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(1234567890123L, parser.getLongValue());

            assertSame(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("dec", parser.getCurrentName());
            assertSame(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(1.25, parser.getDoubleValue(), 0.0);
            assertEquals(new BigDecimal("1.25"), parser.getDecimalValue());

            assertSame(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("bigint", parser.getCurrentName());
            assertSame(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(new BigInteger("123456789012345678901234567890"),
                    parser.getBigIntegerValue());

            assertSame(JsonToken.END_OBJECT, parser.nextToken());
        } finally {
            underlying.close();
        }
    }

    @Test
    public void testCurrentTokenAndTextAccessors() throws Exception {
        JsonParser underlying = new JsonFactory().createParser("{\"text\":\"hello\"}");
        FilteringParserDelegate parser = new FilteringParserDelegate(underlying,
                TokenFilter.INCLUDE_ALL, false, false);
        try {
            assertNull(parser.getCurrentToken());
            assertSame(JsonToken.START_OBJECT, parser.nextToken());
            assertSame(JsonToken.START_OBJECT, parser.getCurrentToken());
            assertSame(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("text", parser.getCurrentName());
            assertEquals("text", parser.getText());
            assertSame(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("hello", parser.getText());
            assertSame(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        } finally {
            underlying.close();
        }
    }

    @Test
    public void testCloseDelegatesToUnderlyingParser() throws Exception {
        JsonParser underlying = new JsonFactory().createParser("[1,2]");
        FilteringParserDelegate parser = new FilteringParserDelegate(underlying,
                TokenFilter.INCLUDE_ALL, false, false);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        assertTrue(underlying.isClosed());
    }
}