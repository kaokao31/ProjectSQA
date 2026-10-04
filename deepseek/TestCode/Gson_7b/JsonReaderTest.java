package com.google.gson.stream;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class JsonReaderTest {

    private JsonReader reader(String json) {
        return new JsonReader(new StringReader(json));
    }

    private void assertStrictNumberRejected(String json) throws IOException {
        JsonReader reader = reader(json);
        try {
            JsonToken token = reader.peek();
            if (token == JsonToken.NUMBER) {
                reader.nextString();
            } else {
                reader.skipValue();
            }
            fail("Expected IOException for malformed number: " + json);
        } catch (IOException expected) {
            // expected
        }
    }

    @Test
    public void testEmptyDocument() throws IOException {
        JsonReader reader = reader("");
        assertFalse(reader.hasNext());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testWhitespaceOnlyDocument() throws IOException {
        JsonReader reader = reader(" \t\r\n");
        assertFalse(reader.hasNext());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testLenientFlag() throws IOException {
        JsonReader reader = reader("[]");
        assertFalse(reader.isLenient());
        reader.setLenient(true);
        assertTrue(reader.isLenient());
    }

    @Test
    public void testStringValue() throws IOException {
        JsonReader reader = reader("\"hello\"");
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("hello", reader.nextString());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testStringEscapes() throws IOException {
        JsonReader reader = reader("\"a\\nb\\t\\\"c\"");
        assertEquals("a\nb\t\"c", reader.nextString());
    }

    @Test
    public void testIntegerValue() throws IOException {
        JsonReader reader = reader("42");
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(42, reader.nextInt());
    }

    @Test
    public void testLongValue() throws IOException {
        JsonReader reader = reader("123456789012345");
        assertEquals(123456789012345L, reader.nextLong());
    }

    @Test
    public void testDoubleValue() throws IOException {
        JsonReader reader = reader("3.25");
        assertEquals(3.25, reader.nextDouble(), 0.0);
    }

    @Test
    public void testBooleanValues() throws IOException {
        JsonReader reader = reader("true false");
        assertTrue(reader.nextBoolean());
        assertFalse(reader.nextBoolean());
    }

    @Test
    public void testNullValue() throws IOException {
        JsonReader reader = reader("null");
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
    }

    @Test
    public void testEmptyArray() throws IOException {
        JsonReader reader = reader("[]");
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();
        assertFalse(reader.hasNext());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testArrayWithValues() throws IOException {
        JsonReader reader = reader("[1,2,3]");
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(3, reader.nextInt());
        assertFalse(reader.hasNext());
        reader.endArray();
    }

    @Test
    public void testNestedArrays() throws IOException {
        JsonReader reader = reader("[[1],[2,3]]");
        reader.beginArray();
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
        reader.beginArray();
        assertEquals(2, reader.nextInt());
        assertEquals(3, reader.nextInt());
        reader.endArray();
        reader.endArray();
    }

    @Test
    public void testEmptyObject() throws IOException {
        JsonReader reader = reader("{}");
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertFalse(reader.hasNext());
        reader.endObject();
    }

    @Test
    public void testObjectWithValues() throws IOException {
        JsonReader reader = reader("{\"a\":1,\"b\":\"two\"}");
        reader.beginObject();
        assertTrue(reader.hasNext());
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        assertTrue(reader.hasNext());
        assertEquals("b", reader.nextName());
        assertEquals("two", reader.nextString());
        assertFalse(reader.hasNext());
        reader.endObject();
    }

    @Test
    public void testNestedObjects() throws IOException {
        JsonReader reader = reader("{\"outer\":{\"inner\":true}}");
        reader.beginObject();
        assertEquals("outer", reader.nextName());
        reader.beginObject();
        assertEquals("inner", reader.nextName());
        assertTrue(reader.nextBoolean());
        reader.endObject();
        reader.endObject();
    }

    @Test
    public void testNextNameAndGetCurrentName() throws IOException {
        JsonReader reader = reader("{\"key\":123}");
        reader.beginObject();
        assertNull(reader.getCurrentName());
        assertEquals("key", reader.nextName());
        assertEquals("key", reader.getCurrentName());
        assertEquals(123, reader.nextInt());
        assertEquals("key", reader.getCurrentName());
        reader.endObject();
    }

    @Test
    public void testStrictRejectsComments() throws IOException {
        JsonReader reader = reader("// comment\n[1]");
        try {
            reader.peek();
            fail("Expected IOException for comment in strict mode");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testLenientAcceptsComments() throws IOException {
        JsonReader reader = reader("// comment\n[1]");
        reader.setLenient(true);
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test
    public void testStrictRejectsSingleQuotedString() throws IOException {
        JsonReader reader = reader("['a']");
        try {
            reader.peek();
            fail("Expected IOException for single-quoted string in strict mode");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testLenientAcceptsSingleQuotedString() throws IOException {
        JsonReader reader = reader("['a']");
        reader.setLenient(true);
        reader.beginArray();
        assertEquals("a", reader.nextString());
        reader.endArray();
    }

    @Test
    public void testStrictRejectsUnquotedString() throws IOException {
        JsonReader reader = reader("[a]");
        try {
            reader.peek();
            fail("Expected IOException for unquoted string in strict mode");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testLenientAcceptsUnquotedString() throws IOException {
        JsonReader reader = reader("[a]");
        reader.setLenient(true);
        reader.beginArray();
        assertEquals("a", reader.nextString());
        reader.endArray();
    }

    @Test
    public void testStrictRejectsTrailingComma() throws IOException {
        JsonReader reader = reader("[1,]");
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        try {
            reader.hasNext();
            fail("Expected IOException for trailing comma in strict mode");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testLenientAcceptsTrailingComma() throws IOException {
        JsonReader reader = reader("[1,]");
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertFalse(reader.hasNext());
        reader.endArray();
    }

    @Test
    public void testStrictRejectsUnterminatedArray() throws IOException {
        JsonReader reader = reader("[1");
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        try {
            reader.peek();
            fail("Expected IOException for unterminated array");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testStrictRejectsUnterminatedObject() throws IOException {
        JsonReader reader = reader("{\"a\":1");
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        try {
            reader.peek();
            fail("Expected IOException for unterminated object");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testStrictRejectsUnterminatedString() throws IOException {
        JsonReader reader = reader("\"abc");
        try {
            reader.peek();
            fail("Expected IOException for unterminated string");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testValidNumbers() throws IOException {
        String[] validNumbers = {
            "0", "-0", "1", "-1", "1.0", "-1.0", "1e2", "1E2",
            "1e+2", "1e-2", "0.5", "-0.5", "1234567890123456789"
        };
        for (String json : validNumbers) {
            JsonReader reader = reader(json);
            assertEquals("Expected NUMBER token for: " + json, JsonToken.NUMBER, reader.peek());
            reader.nextString();
        }
    }

    @Test
    public void testStrictRejectsMalformedNumbers() throws IOException {
        String[] malformedNumbers = {
            "1.", ".5", "+1", "01", "-01", "00", "1.e2", "1e", "1e+",
            "1e-", "--1", "-+1", "NaN", "Infinity", "-Infinity", "0x1", "1.2.3", "."
        };
        for (String json : malformedNumbers) {
            assertStrictNumberRejected(json);
        }
    }

    @Test
    public void testLenientAcceptsNaN() throws IOException {
        JsonReader reader = reader("NaN");
        reader.setLenient(true);
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertTrue(Double.isNaN(reader.nextDouble()));
    }

    @Test
    public void testLenientAcceptsInfinity() throws IOException {
        JsonReader reader = reader("Infinity");
        reader.setLenient(true);
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble(), 0.0);
    }

    @Test
    public void testNonExecutePrefix() throws IOException {
        JsonReader reader = reader(")]}'\n[1]");
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testNonExecutePrefixWithLeadingWhitespace() throws IOException {
        JsonReader reader = reader(" \t)]}'\n[1]");
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test
    public void testLenientAcceptsMultipleTopLevelValues() throws IOException {
        JsonReader reader = reader("1 2");
        reader.setLenient(true);
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testSkipValueInArray() throws IOException {
        JsonReader reader = reader("[1,2,3]");
        reader.beginArray();
        reader.skipValue();
        assertEquals(2, reader.nextInt());
        reader.skipValue();
        assertFalse(reader.hasNext());
        reader.endArray();
    }

    @Test
    public void testSkipValueInObject() throws IOException {
        JsonReader reader = reader("{\"a\":1,\"b\":2}");
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue();
        assertEquals("b", reader.nextName());
        assertEquals(2, reader.nextInt());
        reader.endObject();
    }

    @Test
    public void testSkipValueNested() throws IOException {
        JsonReader reader = reader("[1,[2,3],4]");
        reader.beginArray();
        reader.skipValue();
        reader.beginArray();
        reader.endArray();
        reader.skipValue();
        assertFalse(reader.hasNext());
        reader.endArray();
    }

    @Test
    public void testHasNextAfterTopLevelValue() throws IOException {
        JsonReader reader = reader("true");
        assertTrue(reader.hasNext());
        assertTrue(reader.nextBoolean());
        assertFalse(reader.hasNext());
    }

    @Test
    public void testPeekAfterNextString() throws IOException {
        JsonReader reader = reader("\"value\"");
        assertEquals("value", reader.nextString());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testInvalidLiteral() throws IOException {
        JsonReader reader = reader("tru");
        try {
            reader.peek();
            fail("Expected IOException for invalid literal");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testClosePreventsFurtherReads() throws IOException {
        JsonReader reader = reader("[]");
        reader.close();
        try {
            reader.peek();
            fail("Expected IOException after close");
        } catch (IOException expected) {
        }
    }
}