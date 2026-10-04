package com.google.gson.stream;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class JsonReaderTest {

    @Test
    public void testEmptyDocument() {
        StringReader reader = new StringReader("");
        JsonReader jsonReader = new JsonReader(reader);
        try {
            jsonReader.beginObject();
            Assert.fail("Expected IOException or EOFException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testNullReader() {
        try {
            new JsonReader(null);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testSimpleObject() throws IOException {
        String json = "{\"a\": true}";
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        
        jsonReader.beginObject();
        Assert.assertEquals("a", jsonReader.nextName());
        Assert.assertTrue(jsonReader.nextBoolean());
        jsonReader.endObject();
        jsonReader.close();
    }

    @Test
    public void testSimpleArray() throws IOException {
        String json = "[1, 2, 3]";
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        
        jsonReader.beginArray();
        Assert.assertEquals(1.0, jsonReader.nextDouble(), 0.0001);
        Assert.assertEquals(2, jsonReader.nextInt());
        Assert.assertEquals("3", jsonReader.nextString());
        jsonReader.endArray();
        jsonReader.close();
    }

    @Test
    public void testNestedStructures() throws IOException {
        String json = "{\"a\": [1, {\"b\": null}]}";
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        
        jsonReader.beginObject();
        Assert.assertEquals("a", jsonReader.nextName());
        jsonReader.beginArray();
        Assert.assertEquals(1, jsonReader.nextInt());
        jsonReader.beginObject();
        Assert.assertEquals("b", jsonReader.nextName());
        jsonReader.nextNull();
        jsonReader.endObject();
        jsonReader.endArray();
        jsonReader.endObject();
        jsonReader.close();
    }

    @Test
    public void testLenientMode() throws IOException {
        String json = "{a: 1}"; // Unquoted key, which might need lenient mode
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        jsonReader.setLenient(true);
        Assert.assertTrue(jsonReader.isLenient());

        jsonReader.beginObject();
        Assert.assertEquals("a", jsonReader.nextName());
        Assert.assertEquals(1, jsonReader.nextInt());
        jsonReader.endObject();
        jsonReader.close();
    }

    @Test
    public void testSkipValue() throws IOException {
        String json = "{\"a\": [1, 2], \"b\": 2}";
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        
        jsonReader.beginObject();
        Assert.assertEquals("a", jsonReader.nextName());
        jsonReader.skipValue(); // skips the array
        Assert.assertEquals("b", jsonReader.nextName());
        Assert.assertEquals(2, jsonReader.nextInt());
        jsonReader.endObject();
        jsonReader.close();
    }

    @Test
    public void testPath() throws IOException {
        String json = "{\"a\":[{\"b\":1}]}";
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        
        Assert.assertEquals("$.", jsonReader.getPath());
        jsonReader.beginObject();
        Assert.assertEquals("$.", jsonReader.getPath());
        Assert.assertEquals("a", jsonReader.nextName());
        Assert.assertEquals("$.a", jsonReader.getPath());
        jsonReader.beginArray();
        Assert.assertEquals("$.a[0]", jsonReader.getPath());
        jsonReader.beginObject();
        Assert.assertEquals("$.a[0]", jsonReader.getPath());
        Assert.assertEquals("b", jsonReader.nextName());
        Assert.assertEquals("$.a[0].b", jsonReader.getPath());
        Assert.assertEquals(1, jsonReader.nextInt());
        jsonReader.endObject();
        jsonReader.endArray();
        jsonReader.endObject();
        Assert.assertEquals("$", jsonReader.getPath());
        jsonReader.close();
    }

    @Test
    public void testNumbers() throws IOException {
        String json = "{\"int\": 42, \"long\": 4200000000, \"double\": 3.14}";
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        
        jsonReader.beginObject();
        Assert.assertEquals("int", jsonReader.nextName());
        Assert.assertEquals(42, jsonReader.nextInt());
        Assert.assertEquals("long", jsonReader.nextName());
        Assert.assertEquals(4200000000L, jsonReader.nextLong());
        Assert.assertEquals("double", jsonReader.nextName());
        Assert.assertEquals(3.14, jsonReader.nextDouble(), 0.0001);
        jsonReader.endObject();
        jsonReader.close();
    }

    @Test
    public void testPeek() throws IOException {
        String json = "{\"a\": 1}";
        JsonReader jsonReader = new JsonReader(new StringReader(json));
        
        Assert.assertEquals(JsonToken.BEGIN_OBJECT, jsonReader.peek());
        jsonReader.beginObject();
        Assert.assertEquals(JsonToken.NAME, jsonReader.peek());
        Assert.assertEquals("a", jsonReader.nextName());
        Assert.assertEquals(JsonToken.NUMBER, jsonReader.peek());
        Assert.assertEquals(1, jsonReader.nextInt());
        Assert.assertEquals(JsonToken.END_OBJECT, jsonReader.peek());
        jsonReader.endObject();
        Assert.assertEquals(JsonToken.END_DOCUMENT, jsonReader.peek());
        jsonReader.close();
    }

    @Test
    public void testToStringOutput() {
        StringReader reader = new StringReader("[]");
        JsonReader jsonReader = new JsonReader(reader);
        assert jsonReader.toString() != null;
    }
}