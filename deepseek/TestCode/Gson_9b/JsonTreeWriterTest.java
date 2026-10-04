package com.google.gson.internal.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class JsonTreeWriterTest {

    @Test
    public void testNewWriterReturnsJsonNull() {
        JsonTreeWriter writer = new JsonTreeWriter();
        assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testGetAfterRootString() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("hello");
        JsonElement result = writer.get();
        assertEquals("hello", result.getAsString());
    }

    @Test
    public void testGetAfterRootNullString() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((String) null);
        assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testNullValueAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.nullValue();
        assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testBooleanValueAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(true);
        assertTrue(writer.get().getAsBoolean());
        writer.value(false);
        assertFalse(writer.get().getAsBoolean());
    }

    @Test
    public void testNullBooleanAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Boolean) null);
        assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testLongValueAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, writer.get().getAsLong());
    }

    @Test
    public void testDoubleValueAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(3.25d);
        assertEquals(3.25d, writer.get().getAsDouble(), 0.0d);
    }

    @Test
    public void testNumberValueAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) 42);
        assertEquals(42, writer.get().getAsInt());
    }

    @Test
    public void testNullNumberAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) null);
        assertSame(JsonNull.INSTANCE, writer.get());
    }

    @Test
    public void testBigDecimalValue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new BigDecimal("2.5"));
        assertEquals("2.5", writer.get().getAsString());
    }

    @Test
    public void testFlushIsNoOp() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("x");
        writer.flush();
        assertEquals("x", writer.get().getAsString());
    }

    @Test
    public void testCloseCompleteDocumentDoesNotThrow() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("done");
        writer.close();
    }

    @Test
    public void testCloseIncompleteDocumentThrowsIOException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        try {
            writer.close();
            fail("Expected IOException for incomplete document");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testEmptyArrayAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.endArray();
        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(0, array.size());
    }

    @Test
    public void testArrayWithValues() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.value(1);
        writer.value("two");
        writer.value(true);
        writer.endArray();
        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(3, array.size());
        assertEquals(1, array.get(0).getAsInt());
        assertEquals("two", array.get(1).getAsString());
        assertTrue(array.get(2).getAsBoolean());
    }

    @Test
    public void testArrayWithNulls() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.nullValue();
        writer.value((String) null);
        writer.endArray();
        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(2, array.size());
        assertSame(JsonNull.INSTANCE, array.get(0));
        assertSame(JsonNull.INSTANCE, array.get(1));
    }

    @Test
    public void testEmptyObjectAtRoot() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.endObject();
        JsonObject object = writer.get().getAsJsonObject();
        assertEquals(0, object.entrySet().size());
    }

    @Test
    public void testObjectWithValues() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("int").value(1);
        writer.name("bool").value(true);
        writer.name("string").value("s");
        writer.endObject();
        JsonObject object = writer.get().getAsJsonObject();
        assertEquals(3, object.entrySet().size());
        assertEquals(1, object.get("int").getAsInt());
        assertTrue(object.get("bool").getAsBoolean());
        assertEquals("s", object.get("string").getAsString());
    }

    @Test
    public void testObjectSkipsNullPropertyByDefault() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("nullField");
        writer.nullValue();
        writer.name("kept").value(1);
        writer.endObject();
        JsonObject object = writer.get().getAsJsonObject();
        assertFalse(object.has("nullField"));
        assertEquals(1, object.get("kept").getAsInt());
    }

    @Test
    public void testObjectRetainsNullPropertyWhenSerializeNullsTrue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("nullField").nullValue();
        writer.endObject();
        JsonObject object = writer.get().getAsJsonObject();
        assertTrue(object.has("nullField"));
        assertTrue(object.get("nullField").isJsonNull());
    }

    @Test
    public void testValueInObjectWithoutNameThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        try {
            writer.value("x");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testNameAtRootThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        try {
            writer.name("x");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testNameTwiceThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("a");
        try {
            writer.name("b");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testNameInsideArrayThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        try {
            writer.name("x");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEndArrayAtRootThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        try {
            writer.endArray();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEndObjectAtRootThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        try {
            writer.endObject();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEndArrayWithPendingNameThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("a");
        try {
            writer.endArray();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEndObjectWithPendingNameThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("a");
        try {
            writer.endObject();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEndArrayWhenTopIsObjectThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        try {
            writer.endArray();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEndObjectWhenTopIsArrayThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        try {
            writer.endObject();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testGetWhileObjectOpenThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        try {
            writer.get();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testGetWhileArrayOpenThrows() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        try {
            writer.get();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testNestedObjectInsideArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.beginObject();
        writer.name("key").value("value");
        writer.endObject();
        writer.endArray();
        JsonArray array = writer.get().getAsJsonArray();
        assertEquals(1, array.size());
        JsonObject object = array.get(0).getAsJsonObject();
        assertEquals("value", object.get("key").getAsString());
    }

    @Test
    public void testNestedArrayInsideObject() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("list").beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.endObject();
        JsonObject object = writer.get().getAsJsonObject();
        JsonArray array = object.get("list").getAsJsonArray();
        assertEquals(2, array.size());
        assertEquals(1, array.get(0).getAsInt());
        assertEquals(2, array.get(1).getAsInt());
    }

    @Test
    public void testNestedObjectAsProperty() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("child").beginObject();
        writer.name("x").value(1);
        writer.endObject();
        writer.endObject();
        JsonObject root = writer.get().getAsJsonObject();
        JsonObject child = root.get("child").getAsJsonObject();
        assertEquals(1, child.get("x").getAsInt());
    }

    @Test(expected = NullPointerException.class)
    public void testNameNullThrowsNullPointerException() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name(null);
    }

    @Test
    public void testDoubleNaNRejectedWhenStrict() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testDoubleInfinityRejectedWhenStrict() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        try {
            writer.value(Double.POSITIVE_INFINITY);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testDoubleNaNAcceptedWhenLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.NaN);
        assertTrue(Double.isNaN(writer.get().getAsDouble()));
    }

    @Test
    public void testNumberNaNRejectedWhenStrict() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        try {
            writer.value(Double.valueOf(Double.NaN));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testNumberInfinityRejectedWhenStrict() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        try {
            writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testNumberNaNAcceptedWhenLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.valueOf(Double.NaN));
        assertTrue(Double.isNaN(writer.get().getAsDouble()));
    }
}