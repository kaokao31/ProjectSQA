package com.google.gson.stream;

import org.junit.Test;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class JsonReaderTest {

    @Test(expected = NullPointerException.class)
    public void testNullIn() {
        new JsonReader(null);
    }

    @Test
    public void testEmptyDocument() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(""));
        reader.setLenient(true);
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        assertFalse(reader.hasNext());
        reader.close();
        assertTrue(!reader.isLenient()); // Check lenient default or modified state
    }

    @Test
    public void testBasicObject() throws IOException {
        String json = "{\"a\":true,\"b\":null,\"c\":123,\"d\":\"hello\"}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();

        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("a", reader.nextName());
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());

        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("b", reader.nextName());
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();

        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("c", reader.nextName());
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(123, reader.nextInt());

        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("d", reader.nextName());
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("hello", reader.nextString());

        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();

        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    @Test
    public void testBasicArray() throws IOException {
        String json = "[1, 2.5, \"test\", false, null]";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();

        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(1, reader.nextInt());

        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(2.5, reader.nextDouble(), 0.0001);

        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("test", reader.nextString());

        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertFalse(reader.nextBoolean());

        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();

        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();

        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    @Test
    public void testSkipValue() throws IOException {
        String json = "{\"a\":[1,2,{\"inner\":true}], \"b\":123}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue(); // skips the entire array

        assertEquals("b", reader.nextName());
        assertEquals(123, reader.nextInt());
        reader.endObject();
        reader.close();
    }

    @Test
    public void testSkipValueNestedObject() throws IOException {
        String json = "{\"a\":{\"b\":[10,20]}, \"c\":99}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue(); // skips nested object

        assertEquals("c", reader.nextName());
        assertEquals(99, reader.nextInt());
        reader.endObject();
        reader.close();
    }

    @Test
    public void testNumbers() throws IOException {
        String json = "[-0, 123, 45.67, 1e2, -3.4e-1]";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        assertEquals(0, reader.nextInt());
        assertEquals(123, reader.nextLong());
        assertEquals(45.67, reader.nextDouble(), 0.0001);
        assertEquals(100.0, reader.nextDouble(), 0.0001);
        assertEquals(-0.34, reader.nextDouble(), 0.0001);
        reader.close();
    }

    @Test
    public void testStringEscapes() throws IOException {
        String json = "\"\\n\\t\\r\\b\\f\\\\\\\"\\/\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        assertEquals("\n\t\r\b\f\\\"/", reader.nextString());
        reader.close();
    }

    @Test
    public void testUnicodeEscapes() throws IOException {
        String json = "\"\\u0041\\u0042\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        assertEquals("AB", reader.nextString());
        reader.close();
    }

    @Test(expected = IOException.class)
    public void testUnterminatedString() throws IOException {
        String json = "\"unterminated";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.nextString();
    }

    @Test(expected = IOException.class)
    public void testMalformedNumber() throws IOException {
        String json = "123.45.67";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(false);
        reader.nextDouble();
    }

    @Test(expected = IllegalStateException.class)
    public void testTypeMismatch() throws IOException {
        String json = "[\"notAnInt\"]";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginArray();
        reader.nextInt();
    }

    @Test
    public void testComments() throws IOException {
        String json = "/* comment */ {\n// line comment\n\"a\": 1}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
        reader.close();
    }

    @Test
    public void testPath() throws IOException {
        String json = "{\"outer\": [{\"inner\": 42}]}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        assertEquals("$.", reader.getPath());
        reader.beginObject();
        assertEquals("$.", reader.getPath());
        assertEquals("outer", reader.nextName());
        assertEquals("$.outer", reader.getPath());
        reader.beginArray();
        assertEquals("$.outer[0]", reader.getPath());
        reader.beginObject();
        assertEquals("$.outer[0]", reader.getPath());
        assertEquals("inner", reader.nextName());
        assertEquals("$.outer[0].inner", reader.getPath());
        assertEquals(42, reader.nextInt());
        reader.endObject();
        reader.endArray();
        reader.endObject();
        assertEquals("$.outer", reader.getPath());
        reader.close();
    }

    @Test
    public void testMultipleDocumentsLenient() throws IOException {
        String json = "1 2 3";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(3, reader.nextInt());
        reader.close();
    }

    @Test(expected = IOException.class)
    public void testMultipleDocumentsStrict() throws IOException {
        String json = "1 2";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(false);
        reader.nextInt();
        reader.nextInt();
    }

    @Test
    public void testPromoteNameToValue() throws IOException {
        String json = "{123: 456}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        reader.beginObject();
        // In lenient mode, non-string names might be promoted or handled
        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("123", reader.nextName());
        assertEquals(456, reader.nextInt());
        reader.endObject();
        reader.close();
    }

    @Test
    public void testEmptyArrayAndObjectSkip() throws IOException {
        String json = "{\"emptyObj\": {}, \"emptyArr\": []}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        reader.beginObject();
        assertEquals("emptyObj", reader.nextName());
        reader.skipValue();
        assertEquals("emptyArr", reader.nextName());
        reader.skipValue();
        reader.endObject();
        reader.close();
    }
}