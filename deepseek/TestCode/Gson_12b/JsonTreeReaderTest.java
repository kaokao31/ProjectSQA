package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonToken;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test suite for JsonTreeReader, targeting Gson bug #12.
 * Ensures correct behavior for skipValue() on null values and other operations.
 */
public class JsonTreeReaderTest {

    private JsonTreeReader reader;

    @Before
    public void setUp() throws Exception {
        // Default setup: empty object
        reader = new JsonTreeReader(new JsonObject());
    }

    // ======================== Constructor Tests ========================

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullElement() {
        new JsonTreeReader(null);
    }

    @Test
    public void testConstructorWithNonNullElement() {
        JsonTreeReader r = new JsonTreeReader(new JsonObject());
        assertNotNull(r);
    }

    // ======================== peek() Tests ========================

    @Test
    public void testPeekOnEmptyObject() throws IOException {
        reader.beginObject();
        assertEquals(JsonToken.END_OBJECT, reader.peek());
    }

    @Test
    public void testPeekOnEmptyArray() throws IOException {
        reader = new JsonTreeReader(new JsonArray());
        reader.beginArray();
        assertEquals(JsonToken.END_ARRAY, reader.peek());
    }

    @Test
    public void testPeekOnNull() throws IOException {
        reader = new JsonTreeReader(JsonNull.INSTANCE);
        assertEquals(JsonToken.NULL, reader.peek());
    }

    @Test
    public void testPeekOnPrimitive() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive("hello"));
        assertEquals(JsonToken.STRING, reader.peek());
    }

    // ======================== nextNull() Tests ========================

    @Test
    public void testNextNull() throws IOException {
        reader = new JsonTreeReader(JsonNull.INSTANCE);
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(expected = IllegalStateException.class)
    public void testNextNullOnNonNull() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive("abc"));
        reader.nextNull();
    }

    // ======================== nextString() Tests ========================

    @Test
    public void testNextString() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive("test"));
        assertEquals("test", reader.nextString());
    }

    @Test(expected = IllegalStateException.class)
    public void testNextStringOnNull() throws IOException {
        reader = new JsonTreeReader(JsonNull.INSTANCE);
        reader.nextString();
    }

    // ======================== skipValue() Tests (Bug #12) ========================

    @Test
    public void testSkipValueOnNull() throws IOException {
        reader = new JsonTreeReader(JsonNull.INSTANCE);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testSkipValueOnPrimitive() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive(42));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testSkipValueOnEmptyObject() throws IOException {
        reader = new JsonTreeReader(new JsonObject());
        reader.beginObject();
        reader.skipValue(); // skips the entire object
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testSkipValueOnEmptyArray() throws IOException {
        reader = new JsonTreeReader(new JsonArray());
        reader.beginArray();
        reader.skipValue(); // skips the entire array
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testSkipValueOnNestedObject() throws IOException {
        JsonObject inner = new JsonObject();
        inner.add("key", new JsonPrimitive("value"));
        JsonObject outer = new JsonObject();
        outer.add("inner", inner);
        reader = new JsonTreeReader(outer);
        reader.beginObject();
        assertEquals("inner", reader.nextName());
        reader.skipValue(); // skips the inner object
        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();
    }

    @Test
    public void testSkipValueOnNestedArray() throws IOException {
        JsonArray inner = new JsonArray();
        inner.add(new JsonPrimitive(1));
        JsonArray outer = new JsonArray();
        outer.add(inner);
        reader = new JsonTreeReader(outer);
        reader.beginArray();
        reader.skipValue(); // skips the inner array
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
    }

    // ======================== hasNext() Tests ========================

    @Test
    public void testHasNextOnObjectWithMembers() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("a", new JsonPrimitive(1));
        obj.add("b", new JsonPrimitive(2));
        reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertTrue(reader.hasNext());
        reader.nextName();
        reader.nextString();
        assertTrue(reader.hasNext());
        reader.nextName();
        reader.nextString();
        assertFalse(reader.hasNext());
    }

    @Test
    public void testHasNextOnEmptyObject() throws IOException {
        reader = new JsonTreeReader(new JsonObject());
        reader.beginObject();
        assertFalse(reader.hasNext());
    }

    @Test
    public void testHasNextOnArrayWithElements() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(1));
        arr.add(new JsonPrimitive(2));
        reader = new JsonTreeReader(arr);
        reader.beginArray();
        assertTrue(reader.hasNext());
        reader.nextInt();
        assertTrue(reader.hasNext());
        reader.nextInt();
        assertFalse(reader.hasNext());
    }

    @Test
    public void testHasNextOnEmptyArray() throws IOException {
        reader = new JsonTreeReader(new JsonArray());
        reader.beginArray();
        assertFalse(reader.hasNext());
    }

    // ======================== beginObject/endObject Tests ========================

    @Test
    public void testBeginEndObject() throws IOException {
        reader = new JsonTreeReader(new JsonObject());
        reader.beginObject();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(expected = IllegalStateException.class)
    public void testBeginObjectOnNonObject() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive("x"));
        reader.beginObject();
    }

    @Test(expected = IllegalStateException.class)
    public void testEndObjectOnNonObject() throws IOException {
        reader = new JsonTreeReader(new JsonArray());
        reader.beginArray();
        reader.endObject(); // should throw
    }

    // ======================== beginArray/endArray Tests ========================

    @Test
    public void testBeginEndArray() throws IOException {
        reader = new JsonTreeReader(new JsonArray());
        reader.beginArray();
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(expected = IllegalStateException.class)
    public void testBeginArrayOnNonArray() throws IOException {
        reader = new JsonTreeReader(new JsonObject());
        reader.beginArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testEndArrayOnNonArray() throws IOException {
        reader = new JsonTreeReader(new JsonObject());
        reader.beginObject();
        reader.endArray(); // should throw
    }

    // ======================== nextName() Tests ========================

    @Test
    public void testNextName() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("key", new JsonPrimitive("val"));
        reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertEquals("key", reader.nextName());
    }

    @Test(expected = IllegalStateException.class)
    public void testNextNameOnArray() throws IOException {
        reader = new JsonTreeReader(new JsonArray());
        reader.beginArray();
        reader.nextName();
    }

    // ======================== nextBoolean() Tests ========================

    @Test
    public void testNextBooleanTrue() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive(true));
        assertTrue(reader.nextBoolean());
    }

    @Test
    public void testNextBooleanFalse() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive(false));
        assertFalse(reader.nextBoolean());
    }

    @Test(expected = IllegalStateException.class)
    public void testNextBooleanOnNull() throws IOException {
        reader = new JsonTreeReader(JsonNull.INSTANCE);
        reader.nextBoolean();
    }

    // ======================== nextInt() Tests ========================

    @Test
    public void testNextInt() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive(123));
        assertEquals(123, reader.nextInt());
    }

    @Test(expected = IllegalStateException.class)
    public void testNextIntOnString() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive("abc"));
        reader.nextInt();
    }

    // ======================== nextLong() Tests ========================

    @Test
    public void testNextLong() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive(9876543210L));
        assertEquals(9876543210L, reader.nextLong());
    }

    @Test(expected = IllegalStateException.class)
    public void testNextLongOnNull() throws IOException {
        reader = new JsonTreeReader(JsonNull.INSTANCE);
        reader.nextLong();
    }

    // ======================== nextDouble() Tests ========================

    @Test
    public void testNextDouble() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive(3.14));
        assertEquals(3.14, reader.nextDouble(), 0.0);
    }

    @Test(expected = IllegalStateException.class)
    public void testNextDoubleOnBoolean() throws IOException {
        reader = new JsonTreeReader(new JsonPrimitive(true));
        reader.nextDouble();
    }

    // ======================== close() Tests ========================

    @Test
    public void testClose() throws IOException {
        reader = new JsonTreeReader(new JsonObject());
        reader.close();
        // After close, peek should return END_DOCUMENT
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testCloseMultipleTimes() throws IOException {
        reader.close();
        reader.close(); // should not throw
    }

    // ======================== Complex Nested Structure ========================

    @Test
    public void testComplexNestedStructure() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("nullVal", JsonNull.INSTANCE);
        obj.add("str", new JsonPrimitive("hello"));
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(1));
        arr.add(new JsonPrimitive(2));
        obj.add("arr", arr);
        reader = new JsonTreeReader(obj);

        reader.beginObject();
        assertEquals("nullVal", reader.nextName());
        reader.nextNull();
        assertEquals("str", reader.nextName());
        assertEquals("hello", reader.nextString());
        assertEquals("arr", reader.nextName());
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    // ======================== Edge Cases ========================

    @Test
    public void testSkipValueOnObjectWithMultipleMembers() throws IOException {
        JsonObject obj = new JsonObject();
        obj.add("a", new JsonPrimitive(1));
        obj.add("b", new JsonPrimitive(2));
        reader = new JsonTreeReader(obj);
        reader.beginObject();
        reader.nextName(); // "a"
        reader.skipValue(); // skips value of "a"
        assertEquals("b", reader.nextName());
        reader.skipValue(); // skips value of "b"
        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();
    }

    @Test
    public void testSkipValueOnArrayWithMultipleElements() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(1));
        arr.add(new JsonPrimitive(2));
        reader = new JsonTreeReader(arr);
        reader.beginArray();
        reader.skipValue(); // skips first element
        assertEquals(JsonToken.NUMBER, reader.peek());
        reader.skipValue(); // skips second element
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testSkipValueAtEndOfDocument() throws IOException {
        reader = new JsonTreeReader(new JsonObject());
        reader.beginObject();
        reader.endObject();
        reader.skipValue(); // should throw because no more tokens
    }

    @Test
    public void testNextNullAfterSkipValueOnNull() throws IOException {
        reader = new JsonTreeReader(JsonNull.INSTANCE);
        reader.skipValue();
        // After skip, nextNull should throw because we are at END_DOCUMENT
        try {
            reader.nextNull();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }
}