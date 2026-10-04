package com.google.gson.stream;

import org.junit.Test;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

public class JsonReaderTest {

  @Test(expected = NullPointerException.class)
  public void testConstructorNullPointer() {
    new JsonReader(null);
  }

  @Test
  public void testEmptyDocument() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(""));
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    assertFalse(reader.hasNext());
    reader.close();
  }

  @Test
  public void testBasicObject() throws IOException {
    String json = "{\"a\":true,\"b\":123,\"c\":\"hello\",\"d\":null}";
    JsonReader reader = new JsonReader(new StringReader(json));

    assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    reader.beginObject();

    assertEquals(JsonToken.NAME, reader.peek());
    assertEquals("a", reader.nextName());
    assertEquals(JsonToken.BOOLEAN, reader.peek());
    assertTrue(reader.nextBoolean());

    assertEquals(JsonToken.NAME, reader.peek());
    assertEquals("b", reader.nextName());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(123, reader.nextInt());

    assertEquals(JsonToken.NAME, reader.peek());
    assertEquals("c", reader.nextName());
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("hello", reader.nextString());

    assertEquals(JsonToken.NAME, reader.peek());
    assertEquals("d", reader.nextName());
    assertEquals(JsonToken.NULL, reader.peek());
    reader.nextNull();

    assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();

    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    reader.close();
  }

  @Test
  public void testBasicArray() throws IOException {
    String json = "[1, 2.5, \"test\", false]";
    JsonReader reader = new JsonReader(new StringReader(json));

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

    assertEquals(JsonToken.END_ARRAY, reader.peek());
    reader.endArray();

    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    reader.close();
  }

  @Test
  public void testLenientParsing() throws IOException {
    String json = "{a: 'single', b: 123}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    assertTrue(reader.isLenient());

    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals("single", reader.nextString());
    assertEquals("b", reader.nextName());
    assertEquals(123, reader.nextInt());
    reader.endObject();
    reader.close();
  }

  @Test(expected = IOException.class)
  public void testStrictParsingFailure() throws IOException {
    String json = "{a: 123}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(false);
    reader.beginObject();
    // Should fail because unquoted name 'a' is not allowed in strict mode
    reader.nextName();
  }

  @Test
  public void testSkipValueObject() throws IOException {
    String json = "{\"skip\":{\"nested\":1}, \"keep\":2}";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginObject();
    assertEquals("skip", reader.nextName());
    reader.skipValue();

    assertEquals("keep", reader.nextName());
    assertEquals(2, reader.nextInt());
    reader.endObject();
    reader.close();
  }

  @Test
  public void testSkipValueArray() throws IOException {
    String json = "[ [1, 2], 3]";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginArray();
    reader.skipValue();
    assertEquals(3, reader.nextInt());
    reader.endArray();
    reader.close();
  }

  @Test
  public void testSkipValuePrimitives() throws IOException {
    String json = "[\"stringVal\", 123, true, null]";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginArray();
    reader.skipValue(); // string
    reader.skipValue(); // number
    reader.skipValue(); // boolean
    reader.skipValue(); // null
    reader.endArray();
    reader.close();
  }

  @Test
  public void testNumbers() throws IOException {
    String json = "[0, -123, 123.456, 1e2, -2E-3]";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginArray();
    assertEquals(0, reader.nextInt());
    assertEquals(-123, reader.nextLong());
    assertEquals(123.456, reader.nextDouble(), 0.0001);
    assertEquals(100.0, reader.nextDouble(), 0.0001);
    assertEquals(-0.002, reader.nextDouble(), 0.00001);
    reader.endArray();
    reader.close();
  }

  @Test(expected = IllegalStateException.class)
  public void testTypeMismatchPeek() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123"));
    reader.nextString(); // should fail because peek is NUMBER, not STRING
  }

  @Test(expected = IllegalStateException.class)
  public void testUnexpectedEnd() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":"));
    reader.beginObject();
    reader.nextName();
    reader.endObject();
  }

  @Test
  public void testPath() throws IOException {
    String json = "{\"a\":[{\"b\":42}]}";
    JsonReader reader = new JsonReader(new StringReader(json));

    assertEquals("$.", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPath());

    assertEquals("a", reader.nextName());
    assertEquals("$.a", reader.getPath());

    reader.beginArray();
    assertEquals("$.a[0]", reader.getPath());

    reader.beginObject();
    assertEquals("$.a[0]", reader.getPath());

    assertEquals("b", reader.nextName());
    assertEquals("$.a[0].b", reader.getPath());

    assertEquals(42, reader.nextInt());
    reader.endObject();
    assertEquals("$.a[0]", reader.getPath());

    reader.endArray();
    assertEquals("$.a", reader.getPath());

    reader.endObject();
    assertEquals("$.", reader.getPath());
    reader.close();
  }

  @Test
  public void testStringEscapes() throws IOException {
    String json = "\"\\n\\t\\r\\b\\f\\\\\\\"\\/\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    assertEquals("\n\t\r\b\f\\\"/", reader.nextString());
    reader.close();
  }

  @Test
  public void testUnicodeEscapes() throws IOException {
    String json = "\"\\u0041\\u0042\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    assertEquals("AB", reader.nextString());
    reader.close();
  }

  @Test
  public void testComments() throws IOException {
    String json = "/* comment */ {// comment\n \"a\": 1 // line comment\n}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(1, reader.nextInt());
    reader.endObject();
    reader.close();
  }

  @Test(expected = IOException.class)
  public void testMalformedJsonNumber() throws IOException {
    String json = "[1.2.3]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    reader.nextDouble();
  }

  @Test
  public void testHasNextFalseAtEnd() throws IOException {
    String json = "{}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginObject();
    assertFalse(reader.hasNext());
    reader.endObject();
    assertFalse(reader.hasNext());
    reader.close();
  }
}