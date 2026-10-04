package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonToken;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonTreeReaderTest {

    @Test(expected = AssertionError.class)
    public void testNullElementThrowsAssertionError() throws IOException {
        new JsonTreeReader(null);
    }

    @Test
    public void testEmptyDocument() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(JsonNull.INSTANCE);
        assertEquals(JsonToken.NULL, reader.peek());
        assertTrue(reader.hasNext());
        reader.nextNull();
        assertFalse(reader.hasNext());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testPrimitiveTypes() throws IOException {
        // Boolean
        JsonTreeReader boolReader = new JsonTreeReader(new JsonPrimitive(true));
        assertEquals(JsonToken.BOOLEAN, boolReader.peek());
        assertTrue(boolReader.nextBoolean());

        // Number (Integer/Long/Double handled via JsonPrimitive)
        JsonTreeReader numReader = new JsonTreeReader(new JsonPrimitive(42));
        assertEquals(JsonToken.NUMBER, numReader.peek());
        assertEquals(42, numReader.nextInt());

        JsonTreeReader longReader = new JsonTreeReader(new JsonPrimitive(123456789L));
        assertEquals(JsonToken.NUMBER, longReader.peek());
        assertEquals(123456789L, longReader.nextLong());

        JsonTreeReader doubleReader = new JsonTreeReader(new JsonPrimitive(3.14));
        assertEquals(JsonToken.NUMBER, doubleReader.peek());
        assertEquals(3.14, doubleReader.nextDouble(), 0.0001);

        // String
        JsonTreeReader strReader = new JsonTreeReader(new JsonPrimitive("hello"));
        assertEquals(JsonToken.STRING, strReader.peek());
        assertEquals("hello", strReader.nextString());
    }

    @Test
    public void testJsonObjectTraversal() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("key1", "value1");
        obj.addProperty("key2", 100);

        JsonTreeReader reader = new JsonTreeReader(obj);
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();

        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("key1", reader.nextName());
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("value1", reader.nextString());

        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("key2", reader.nextName());
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(100, reader.nextInt());

        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testJsonArrayTraversal() throws IOException {
        JsonArray array = new JsonArray();
        array.add("item1");
        array.add(false);

        JsonTreeReader reader = new JsonTreeReader(array);
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();

        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("item1", reader.nextString());

        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertFalse(reader.nextBoolean());

        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test
    public void testSkipValueObject() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("a", 1);
        JsonObject nested = new JsonObject();
        nested.addProperty("b", 2);
        obj.add("nested", nested);
        obj.addProperty("c", 3);

        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());

        // Skip the "nested" object
        assertEquals("nested", reader.nextName());
        reader.skipValue();

        assertEquals("c", reader.nextName());
        assertEquals(3, reader.nextInt());
        reader.endObject();
    }

    @Test
    public void testSkipValueArray() throws IOException {
        JsonArray array = new JsonArray();
        array.add(1);
        JsonArray innerArray = new JsonArray();
        innerArray.add(2);
        array.add(innerArray);
        array.add(3);

        JsonTreeReader reader = new JsonTreeReader(array);
        reader.beginArray();
        assertEquals(1, reader.nextInt());

        reader.skipValue(); // Skips innerArray

        assertEquals(3, reader.nextInt());
        reader.endArray();
    }

    @Test
    public void testSkipValuePrimitives() throws IOException {
        JsonArray array = new JsonArray();
        array.add("string");
        array.add(true);
        array.add(JsonNull.INSTANCE);
        array.add(123);

        JsonTreeReader reader = new JsonTreeReader(array);
        reader.beginArray();
        reader.skipValue(); // string
        reader.skipValue(); // true
        reader.skipValue(); // null
        reader.skipValue(); // 123
        reader.endArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnexpectedTypePeekObjectAsPrimitive() throws IOException {
        JsonObject obj = new JsonObject();
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.nextString();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnexpectedTypeBeginArrayOnObject() throws IOException {
        JsonObject obj = new JsonObject();
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnexpectedTypeEndObjectOnArray() throws IOException {
        JsonArray arr = new JsonArray();
        JsonTreeReader reader = new JsonTreeReader(arr);
        reader.beginArray();
        reader.endObject();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnexpectedTypeNameWithoutObject() throws IOException {
        JsonArray arr = new JsonArray();
        JsonTreeReader reader = new JsonTreeReader(arr);
        reader.beginArray();
        reader.nextName();
    }

    @Test
    public void testPromoteNameToValue() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("myKey", "myVal");

        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        // Instead of nextName(), call promoteNameToValue() or peek NAME then treat as string via nextString
        assertEquals(JsonToken.NAME, reader.peek());
        reader.promoteNameToValue();
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("myKey", reader.nextString());

        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("myVal", reader.nextString());
        reader.endObject();
    }

    @Test
    public void testPathGeneration() throws IOException {
        JsonObject obj = new JsonObject();
        JsonObject inner = new JsonObject();
        inner.addProperty("leaf", 99);
        obj.add("branch", inner);

        JsonTreeReader reader = new JsonTreeReader(obj);
        assertEquals("$", reader.getPath());
        reader.beginObject();
        assertEquals("$.", reader.getPath());
        assertEquals("branch", reader.nextName());
        assertEquals("$.branch", reader.getPath());

        reader.beginObject();
        assertEquals("$.branch.", reader.getPath());
        assertEquals("leaf", reader.nextName());
        assertEquals("$.branch.leaf", reader.getPath());
        assertEquals(99, reader.nextInt());

        reader.endObject();
        reader.endObject();
        assertEquals("$.branch", reader.getPath());
    }

    @Test
    public void testArrayPathGeneration() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add("val0");
        arr.add("val1");

        JsonTreeReader reader = new JsonTreeReader(arr);
        assertEquals("$", reader.getPath());
        reader.beginArray();
        assertEquals("$[0]", reader.getPath());
        assertEquals("val0", reader.nextString());
        assertEquals("$[0]", reader.getPath());

        assertEquals("$[1]", reader.getPath());
        assertEquals("val1", reader.nextString());
        reader.endArray();
        assertEquals("$[1]", reader.getPath());
    }

    @Test
    public void testDoubleAsString() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(12.34));
        assertEquals("12.34", reader.nextString());
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidIntConversion() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("not-a-number"));
        reader.nextInt();
    }
}