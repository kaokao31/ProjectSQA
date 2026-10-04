package com.google.gson.stream;

import org.junit.Test;
import org.junit.Assert;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonWriterTest {

    @Test
    public void testBasicDocument() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        jsonWriter.beginObject();
        jsonWriter.name("hello").value("world");
        jsonWriter.name("answer").value(42);
        jsonWriter.name("pi").value(3.14);
        jsonWriter.name("check").value(true);
        jsonWriter.name("nil").nullValue();
        jsonWriter.endObject();

        jsonWriter.close();

        String expected = "{\"hello\":\"world\",\"answer\":42,\"pi\":3.14,\"check\":true,\"nil\":null}";
        Assert.assertEquals(expected, stringWriter.toString());
    }

    @Test
    public void testNestedArraysAndObjects() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        jsonWriter.beginArray();
        jsonWriter.beginObject();
        jsonWriter.name("arr");
        jsonWriter.beginArray();
        jsonWriter.value(1);
        jsonWriter.value(2);
        jsonWriter.endArray();
        jsonWriter.endObject();
        jsonWriter.endArray();

        Assert.assertEquals("[{\"arr\":[1,2]}]", stringWriter.toString());
    }

    @Test
    public void testLenientSetting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        Assert.assertFalse(jsonWriter.isLenient());
        jsonWriter.setLenient(true);
        Assert.assertTrue(jsonWriter.isLenient());

        jsonWriter.beginArray();
        // Infinite and NaN values are allowed only in lenient mode
        jsonWriter.value(Double.NaN);
        jsonWriter.value(Double.NEGATIVE_INFINITY);
        jsonWriter.value(Double.POSITIVE_INFINITY);
        jsonWriter.endArray();

        Assert.assertEquals("[NaN,-Infinity,Infinity]", stringWriter.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictRejectNaN() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setLenient(false);

        jsonWriter.beginArray();
        jsonWriter.value(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictRejectNegativeInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setLenient(false);

        jsonWriter.beginArray();
        jsonWriter.value(Double.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStrictRejectPositiveInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setLenient(false);

        jsonWriter.beginArray();
        jsonWriter.value(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testHtmlSafeSetting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        Assert.assertTrue(jsonWriter.isHtmlSafe());
        jsonWriter.setHtmlSafe(false);
        Assert.assertFalse(jsonWriter.isHtmlSafe());

        jsonWriter.value("<script>alert('Hello')</script>");
        // When htmlSafe is false, <, >, & etc. are not escaped as unicode
        Assert.assertEquals("\"<script>alert('Hello')</script>\"", stringWriter.toString());
    }

    @Test
    public void testHtmlSafeEscaping() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setHtmlSafe(true);

        jsonWriter.value("<script>alert('Hello')</script>");
        // Default htmlSafe escapes <, >, =, &, '
        Assert.assertEquals("\"\\u003cscript\\u003ealert(\\'Hello\\')\\u003c/script\\u003e\"", stringWriter.toString());
    }

    @Test
    public void testFormattingIndentation() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent("  ");

        jsonWriter.beginObject();
        jsonWriter.name("a").value(1);
        jsonWriter.endObject();

        String expected = "{\n  \"a\": 1\n}";
        Assert.assertEquals(expected, stringWriter.toString());
    }

    @Test
    public void testFormattingIndentationEmpty() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setIndent("");

        jsonWriter.beginObject();
        jsonWriter.name("a").value(1);
        jsonWriter.endObject();

        Assert.assertEquals("{\"a\":1}", stringWriter.toString());
    }

    @Test
    public void testSerializeNullsSetting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        Assert.assertFalse(jsonWriter.getSerializeNulls());
        jsonWriter.setSerializeNulls(true);
        Assert.assertTrue(jsonWriter.getSerializeNulls());

        jsonWriter.beginObject();
        jsonWriter.name("nil").nullValue();
        jsonWriter.endObject();

        Assert.assertEquals("{\"nil\":null}", stringWriter.toString());

        // Test when serializeNulls is false (default)
        StringWriter stringWriter2 = new StringWriter();
        JsonWriter jsonWriter2 = new JsonWriter(stringWriter2);
        jsonWriter2.beginObject();
        jsonWriter2.name("nil").nullValue();
        jsonWriter2.endObject();

        Assert.assertEquals("{}", stringWriter2.toString());
    }

    @Test
    public void testNumberTypes() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        jsonWriter.beginArray();
        jsonWriter.value(Long.MAX_VALUE);
        jsonWriter.value(new BigInteger("12345678901234567890"));
        jsonWriter.value(new BigDecimal("123.456"));
        jsonWriter.value((Number) 100);
        jsonWriter.endArray();

        Assert.assertEquals("[" + Long.MAX_VALUE + ",12345678901234567890,123.456,100]", stringWriter.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testJsonWriterWithoutRootValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.value("too many values");
    }

    @Test(expected = IllegalStateException.class)
    public void testNestedValuesWithoutName() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginObject();
        jsonWriter.value("missingName");
    }

    @Test(expected = IllegalStateException.class)
    public void testNameWithoutObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.name("orphanName");
    }

    @Test(expected = IllegalStateException.class)
    public void testPrematureClose() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginArray();
        jsonWriter.close();
    }

    @Test
    public void testFlushAndClose() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginArray();
        jsonWriter.value(1);
        jsonWriter.flush();
        jsonWriter.endArray();
        jsonWriter.close();
        
        // Calling close multiple times should be safe
        jsonWriter.close();

        Assert.assertEquals("[1]", stringWriter.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testDeepNestingCheckOrNestedCloseMismatch() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.beginArray();
        jsonWriter.endObject(); // Mismatch array/object
    }
}