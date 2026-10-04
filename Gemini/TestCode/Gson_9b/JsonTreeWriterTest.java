package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class JsonTreeWriterTest {

    private JsonTreeWriter writer;

    @Before
    public void setUp() {
        writer = new JsonTreeWriter();
    }

    @After
    public void tearDown() {
        writer = null;
    }

    @Test
    public void testInitialState() {
        assertNotNull(writer.get());
        assertTrue(writer.get().isJsonNull());
    }

    @Test
    public void testWriteNull() throws IOException {
        writer.nullValue();
        JsonElement element = writer.get();
        assertNotNull(element);
        assertTrue(element.isJsonNull());
    }

    @Test
    public void testWriteBoolean() throws IOException {
        writer.beginArray();
        writer.value(true);
        writer.value(false);
        writer.endArray();

        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(2, array.size());
        assertTrue(array.get(0).getAsBoolean());
        assertFalse(array.get(1).getAsBoolean());
    }

    @Test
    public void testWriteBooleanPrimitive() throws IOException {
        writer.beginArray();
        writer.value(Boolean.TRUE);
        writer.value((Boolean) null);
        writer.endArray();

        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(2, array.size());
        assertTrue(array.get(0).getAsBoolean());
        assertTrue(array.get(1).isJsonNull());
    }

    @Test
    public void testWriteNumberPrimitives() throws IOException {
        writer.beginArray();
        writer.value(10L);
        writer.value(20.5D);
        writer.value(30.4F);
        writer.value(new BigDecimal("40.56"));
        writer.value(new BigInteger("50"));
        writer.endArray();

        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(5, array.size());
        assertEquals(10L, array.get(0).getAsLong());
        assertEquals(20.5D, array.get(1).getAsDouble(), 0.001);
        assertEquals(30.4F, array.get(2).getAsFloat(), 0.001);
        assertEquals(new BigDecimal("40.56"), array.get(3).getAsBigDecimal());
        assertEquals(new BigInteger("50"), array.get(4).getAsBigInteger());
    }

    @Test
    public void testWriteNumberStringAndInt() throws IOException {
        writer.beginArray();
        writer.value(123);
        writer.value("custom-number-string");
        writer.endArray();

        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(2, array.size());
        assertEquals(123, array.get(0).getAsInt());
        assertEquals("custom-number-string", array.get(1).getAsString());
    }

    @Test
    public void testWriteNullNumber() throws IOException {
        writer.beginArray();
        writer.value((Number) null);
        writer.endArray();

        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(1, array.size());
        assertTrue(array.get(0).isJsonNull());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteNaN() throws IOException {
        writer.value(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteInfinity() throws IOException {
        writer.value(Double.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteNegativeInfinity() throws IOException {
        writer.value(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testWriteJsonString() throws IOException {
        writer.value("hello");
        JsonElement element = writer.get();
        assertEquals("hello", element.getAsString());
    }

    @Test(expected = IllegalStateException.class)
    public void testTopLevelScalarThenValue() throws IOException {
        writer.value("first");
        writer.value("second"); // Should throw IllegalStateException because top-level is already a complete primitive
    }

    @Test
    public void testNestedObjectsAndArrays() throws IOException {
        writer.beginObject();
        writer.name("stringKey");
        writer.value("stringValue");
        writer.name("arrayKey");
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.name("objectKey");
        writer.beginObject();
        writer.name("nestedKey");
        writer.value(true);
        writer.endObject();
        writer.endObject();

        JsonObject obj = writer.get().getAsJsonObject();
        assertEquals(3, obj.size());
        assertEquals("stringValue", obj.get("stringKey").getAsString());

        JsonArray array = obj.get("arrayKey").getAsJsonArray();
        assertEquals(2, array.size());
        assertEquals(1, array.get(0).getAsInt());
        assertEquals(2, array.get(1).getAsInt());

        JsonObject nestedObj = obj.get("objectKey").getAsJsonObject();
        assertTrue(nestedObj.get("nestedKey").getAsBoolean());
    }

    @Test(expected = IllegalStateException.class)
    public void testNameWithoutObject() throws IOException {
        writer.name("orphanName");
    }

    @Test(expected = IllegalStateException.class)
    public void testObjectValueWithoutName() throws IOException {
        writer.beginObject();
        writer.value("orphanValue");
    }

    @Test(expected = IllegalStateException.class)
    public void testEndArrayWithoutBegin() throws IOException {
        writer.endArray();
    }

    @Test(expected = IllegalStateException.class)
    public void testEndObjectWithoutBegin() throws IOException {
        writer.endObject();
    }

    @Test(expected = IOException.class)
    public void testMethodsAfterClose() throws IOException {
        writer.close();
        writer.value("test");
    }

    @Test
    public void testFlushDoesNothing() throws IOException {
        writer.beginArray();
        writer.flush();
        writer.value(1);
        writer.flush();
        writer.endArray();
        assertEquals(1, writer.get().getAsJsonArray().size());
    }
}