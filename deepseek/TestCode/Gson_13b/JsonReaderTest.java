package com.google.gson.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class JsonReaderTest {

  private JsonReader reader(String json) {
    return new JsonReader(new StringReader(json));
  }

  @Test
  public void testStrictRejectsMultipleTopLevelValues_ArrayThenArray() throws IOException {
    JsonReader reader = reader("[1] [2]");
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.endArray();

    try {
      reader.peek();
      fail("Expected MalformedJsonException for multiple top-level values");
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testStrictRejectsMultipleTopLevelValues_ArrayThenObject() throws IOException {
    JsonReader reader = reader("[1] {\"a\":2}");
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.endArray();

    try {
      reader.peek();
      fail("Expected MalformedJsonException for multiple top-level values");
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testStrictRejectsMultipleTopLevelValues_ObjectThenArray() throws IOException {
    JsonReader reader = reader("{\"a\":1} [2]");
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(1, reader.nextInt());
    reader.endObject();

    try {
      reader.peek();
      fail("Expected MalformedJsonException for multiple top-level values");
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testStrictRejectsMultipleTopLevelValues_ArrayThenScalar() throws IOException {
    JsonReader reader = reader("[1] true");
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.endArray();

    try {
      reader.peek();
      fail("Expected MalformedJsonException for multiple top-level values");
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testStrictRejectsMultipleTopLevelValues_WithWhitespace() throws IOException {
    JsonReader reader = reader("[1]   \n\t [2]");
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.endArray();

    try {
      reader.peek();
      fail("Expected MalformedJsonException for multiple top-level values");
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testLenientAllowsMultipleTopLevelValues() throws IOException {
    JsonReader reader = reader("[1] [2]");
    reader.setLenient(true);

    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.endArray();

    assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
    reader.beginArray();
    assertEquals(2, reader.nextInt());
    reader.endArray();

    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testStrictAcceptsSingleTopLevelArray() throws IOException {
    JsonReader reader = reader("[1]");
    assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.endArray();

    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testStrictAcceptsSingleTopLevelObject() throws IOException {
    JsonReader reader = reader("{\"a\":true}");
    assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertTrue(reader.nextBoolean());
    reader.endObject();

    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
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
  public void testEmptyObject() throws IOException {
    JsonReader reader = reader("{}");
    assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    reader.beginObject();
    assertFalse(reader.hasNext());
    reader.endObject();

    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testArrayPrimitives() throws IOException {
    JsonReader reader = reader("[null,true,false,\"hello\",123,-45,6.5]");
    reader.beginArray();

    reader.nextNull();
    assertTrue(reader.nextBoolean());
    assertFalse(reader.nextBoolean());
    assertEquals("hello", reader.nextString());
    assertEquals(123, reader.nextInt());
    assertEquals(-45, reader.nextInt());
    assertEquals(6.5, reader.nextDouble(), 0.0);

    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testObjectPrimitives() throws IOException {
    JsonReader reader = reader("{\"null\":null,\"bool\":true,\"str\":\"x\",\"num\":42}");
    reader.beginObject();

    assertEquals("null", reader.nextName());
    reader.nextNull();

    assertEquals("bool", reader.nextName());
    assertTrue(reader.nextBoolean());

    assertEquals("str", reader.nextName());
    assertEquals("x", reader.nextString());

    assertEquals("num", reader.nextName());
    assertEquals(42, reader.nextInt());

    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testNestedStructures() throws IOException {
    JsonReader reader = reader("{\"a\":[1,{\"b\":2}],\"c\":{\"d\":[true]}}");
    reader.beginObject();

    assertEquals("a", reader.nextName());
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.beginObject();
    assertEquals("b", reader.nextName());
    assertEquals(2, reader.nextInt());
    reader.endObject();
    reader.endArray();

    assertEquals("c", reader.nextName());
    reader.beginObject();
    assertEquals("d", reader.nextName());
    reader.beginArray();
    assertTrue(reader.nextBoolean());
    reader.endArray();
    reader.endObject();

    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testSkipValue() throws IOException {
    JsonReader reader = reader("{\"a\":[1,{\"b\":2}],\"c\":3}");
    reader.beginObject();

    assertEquals("a", reader.nextName());
    reader.skipValue();

    assertEquals("c", reader.nextName());
    assertEquals(3, reader.nextInt());

    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testStrictRejectsTopLevelPrimitive() throws IOException {
    JsonReader reader = reader("true");

    try {
      reader.peek();
      fail("Expected MalformedJsonException for top-level primitive in strict mode");
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testLenientAllowsTopLevelPrimitive() throws IOException {
    JsonReader reader = reader("true");
    reader.setLenient(true);

    assertEquals(JsonToken.BOOLEAN, reader.peek());
    assertTrue(reader.nextBoolean());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testLenientAllowsTopLevelString() throws IOException {
    JsonReader reader = reader("\"hello\"");
    reader.setLenient(true);

    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("hello", reader.nextString());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testStrictRejectsSingleQuotedString() throws IOException {
    JsonReader reader = reader("['foo']");
    reader.beginArray();

    try {
      reader.peek();
      fail("Expected MalformedJsonException for single quoted string in strict mode");
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testLenientAcceptsSingleQuotedString() throws IOException {
    JsonReader reader = reader("['foo']");
    reader.setLenient(true);

    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("foo", reader.nextString());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testLenientAcceptsUnquotedString() throws IOException {
    JsonReader reader = reader("[foo]");
    reader.setLenient(true);

    reader.beginArray();
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("foo", reader.nextString());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testStrictRejectsBlockComment() throws IOException {
    JsonReader reader = reader("[/* comment */1]");
    reader.beginArray();

    try {
      reader.peek();
      fail("Expected MalformedJsonException for comment in strict mode");
    } catch (MalformedJsonException expected) {
    }
  }

  @Test
  public void testLenientAcceptsComments() throws IOException {
    JsonReader reader = reader("[// line comment\n1, /* block comment */ 2]");
    reader.setLenient(true);

    reader.beginArray();
    assertEquals(1, reader.nextInt());
    assertEquals(2, reader.nextInt());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testNextIntOverflowFails() throws IOException {
    JsonReader reader = reader("[2147483648]");
    reader.beginArray();

    try {
      reader.nextInt();
      fail("Expected NumberFormatException for int overflow");
    } catch (NumberFormatException expected) {
    }
  }

  @Test
  public void testNextLongAcceptsLargeValue() throws IOException {
    JsonReader reader = reader("[9223372036854775807]");
    reader.beginArray();
    assertEquals(9223372036854775807L, reader.nextLong());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test
  public void testNextDoubleAcceptsFractionAndExponent() throws IOException {
    JsonReader reader = reader("[3.14,1e10]");
    reader.beginArray();
    assertEquals(3.14, reader.nextDouble(), 0.0);
    assertEquals(1e10, reader.nextDouble(), 0.0);
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }
}