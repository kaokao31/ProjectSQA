package com.google.gson.internal.bind;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ReflectiveTypeAdapterFactory}.
 * Targets high branch coverage and potential fault detection
 * (especially for defect Gson-10 related to primitive defaults and @SerializedName).
 */
public class ReflectiveTypeAdapterFactoryTest {

    private Gson gson;
    private ReflectiveTypeAdapterFactory factory;

    @Before
    public void setUp() throws Exception {
        // Build a default Gson to extract the factory (package-private access)
        gson = new GsonBuilder().create();
        // Access the factory via reflection (since it's internal)
        java.lang.reflect.Field factoryField = Gson.class.getDeclaredField("constructorConstructor");
        factoryField.setAccessible(true);
        Object constructorConstructor = factoryField.get(gson);
        factoryField = Gson.class.getDeclaredField("fieldNamingPolicy");
        factoryField.setAccessible(true);
        Object fieldNamingPolicy = factoryField.get(gson);
        factoryField = Gson.class.getDeclaredField("excluder");
        factoryField.setAccessible(true);
        Object excluder = factoryField.get(gson);
        factoryField = Gson.class.getDeclaredField("jsonAdapterFactory");
        factoryField.setAccessible(true);
        Object jsonAdapterFactory = factoryField.get(gson);
        // Note: ReflectiveTypeAdapterFactory constructor may have changed; we use a workaround
        // In Gson 2.8.x, it takes (ConstructorConstructor, FieldNamingPolicy, Excluder, JsonAdapterAnnotationTypeAdapterFactory)
        // For simplicity, we test indirectly through Gson serialization/deserialization.
        // Direct instantiation is fragile; instead, test via Gson public API.
    }

    // ----- Helper classes for testing -----

    static class SimpleBean {
        @SerializedName("custom_name")
        String name;
        int value;
        Boolean flag;
    }

    static class WithDefaults {
        int intField;
        long longField;
        boolean boolField;
        String strField;
    }

    static class NullableFields {
        String nullString;
        Integer nullInteger;
    }

    // ----- Tests for basic creation and serialization -----

    @Test
    public void testSimpleSerialization() {
        SimpleBean bean = new SimpleBean();
        bean.name = "test";
        bean.value = 42;
        bean.flag = true;

        String json = gson.toJson(bean);
        assertTrue(json.contains("\"custom_name\":\"test\""));
        assertTrue(json.contains("\"value\":42"));
        assertTrue(json.contains("\"flag\":true"));
    }

    @Test
    public void testSimpleDeserialization() {
        String json = "{\"custom_name\":\"hello\",\"value\":99,\"flag\":false}";
        SimpleBean bean = gson.fromJson(json, SimpleBean.class);
        assertEquals("hello", bean.name);
        assertEquals(99, bean.value);
        assertEquals(Boolean.FALSE, bean.flag);
    }

    // ----- Edge cases: default primitive values -----

    @Test
    public void testPrimitiveDefaults() {
        // When not present in JSON, primitives get default values (0, false)
        String json = "{}";
        WithDefaults obj = gson.fromJson(json, WithDefaults.class);
        assertEquals(0, obj.intField);
        assertEquals(0L, obj.longField);
        assertEquals(false, obj.boolField);
        assertNull(obj.strField); // object type defaults to null
    }

    @Test
    public void testPrimitiveExplicitDefaultsInJson() {
        String json = "{\"intField\":0,\"longField\":0,\"boolField\":false,\"strField\":null}";
        WithDefaults obj = gson.fromJson(json, WithDefaults.class);
        assertEquals(0, obj.intField);
        assertEquals(0L, obj.longField);
        assertEquals(false, obj.boolField);
        assertNull(obj.strField);
    }

    // ----- Edge cases: null handling -----

    @Test
    public void testNullFieldSerialization() {
        NullableFields obj = new NullableFields();
        obj.nullString = null;
        obj.nullInteger = null;
        String json = gson.toJson(obj);
        // Default Gson excludes null fields; so they should not appear
        assertFalse(json.contains("nullString"));
        assertFalse(json.contains("nullInteger"));
    }

    @Test
    public void testNullFieldDeserialization() {
        String json = "{\"nullString\":null,\"nullInteger\":null}";
        NullableFields obj = gson.fromJson(json, NullableFields.class);
        assertNull(obj.nullString);
        assertNull(obj.nullInteger);
    }

    // ----- Tests for @SerializedName (potential fault trigger) -----

    @Test
    public void testSerializedNameDeserialization() {
        String json = "{\"custom_name\":\"value\"}";
        SimpleBean bean = gson.fromJson(json, SimpleBean.class);
        assertEquals("value", bean.name);
    }

    @Test(expected = com.google.gson.JsonSyntaxException.class)
    public void testInvalidTypeForPrimitive() {
        // Should throw JsonSyntaxException if a non-integer is provided for int field
        String json = "{\"value\":\"notanumber\"}";
        gson.fromJson(json, SimpleBean.class);
    }

    // ----- Tests for TypeAdapter creation via factory (indirect) -----

    @Test
    public void testTypeAdapterNotNull() {
        TypeAdapter<SimpleBean> adapter = gson.getAdapter(SimpleBean.class);
        assertNotNull(adapter);
    }

    @Test
    public void testTypeAdapterRoundTrip() throws IOException {
        SimpleBean original = new SimpleBean();
        original.name = "round";
        original.value = 100;
        original.flag = true;

        TypeAdapter<SimpleBean> adapter = gson.getAdapter(SimpleBean.class);
        StringWriter sw = new StringWriter();
        JsonWriter jw = gson.newJsonWriter(sw);
        adapter.write(jw, original);
        jw.close();
        String json = sw.toString();

        JsonReader jr = gson.newJsonReader(new StringReader(json));
        SimpleBean deserialized = adapter.read(jr);
        assertEquals(original.name, deserialized.name);
        assertEquals(original.value, deserialized.value);
        assertEquals(original.flag, deserialized.flag);
    }

    // ----- Tests for edge case: empty object -----

    @Test
    public void testEmptyObject() {
        String json = "{}";
        SimpleBean bean = gson.fromJson(json, SimpleBean.class);
        assertNull(bean.name);
        assertEquals(0, bean.value);
        assertNull(bean.flag);
    }

    // ----- Tests for field with @SerializedName that is a Java keyword? -----

    static class WithSpecialNames {
        @SerializedName("class")
        String className;
    }

    @Test
    public void testSpecialSerializedName() {
        WithSpecialNames obj = new WithSpecialNames();
        obj.className = "MyClass";
        String json = gson.toJson(obj);
        assertTrue(json.contains("\"class\":\"MyClass\""));

        String jsonIn = "{\"class\":\"TestClass\"}";
        WithSpecialNames des = gson.fromJson(jsonIn, WithSpecialNames.class);
        assertEquals("TestClass", des.className);
    }

    // ----- Test for boolean primitive default -----

    @Test
    public void testBooleanPrimitiveDefault() {
        String json = "{\"boolField\":false}";
        WithDefaults obj = gson.fromJson(json, WithDefaults.class);
        assertFalse(obj.boolField);
    }

    // ----- Test for missing fields in JSON -----

    @Test
    public void testMissingFieldDeserialization() {
        String json = "{\"name\":\"onlyName\"}";
        SimpleBean bean = gson.fromJson(json, SimpleBean.class);
        assertEquals("onlyName", bean.name);
        assertEquals(0, bean.value); // int default
        assertNull(bean.flag); // object default
    }

    // ----- Test for extra fields in JSON (should be ignored) -----

    @Test
    public void testExtraFieldsInJson() {
        String json = "{\"name\":\"x\",\"value\":1,\"flag\":true,\"extra\":\"ignore\"}";
        SimpleBean bean = gson.fromJson(json, SimpleBean.class);
        assertEquals("x", bean.name);
        assertEquals(1, bean.value);
        assertEquals(Boolean.TRUE, bean.flag);
    }

    // ----- Test for null literal in JSON for object fields -----

    @Test
    public void testNullLiteralDeserialization() {
        String json = "{\"name\":null,\"flag\":null}";
        SimpleBean bean = gson.fromJson(json, SimpleBean.class);
        assertNull(bean.name);
        assertNull(bean.flag);
        // primitive 'value' will default to 0 because it's not present
        assertEquals(0, bean.value);
    }

    // ----- Test for @SerializedName with empty string (edge case) -----

    static class WithEmptySerializedName {
        @SerializedName("")
        String emptyKey;
    }

    @Test
    public void testEmptySerializedName() {
        WithEmptySerializedName obj = new WithEmptySerializedName();
        obj.emptyKey = "value";
        String json = gson.toJson(obj);
        assertTrue(json.contains("\"\":\"value\""));

        String jsonIn = "{\"\":\"data\"}";
        WithEmptySerializedName des = gson.fromJson(jsonIn, WithEmptySerializedName.class);
        assertEquals("data", des.emptyKey);
    }

    // ----- Test for double field (if present in factory) -----

    static class WithDouble {
        double d;
    }

    @Test
    public void testDoubleField() {
        WithDouble obj = new WithDouble();
        obj.d = 3.14;
        String json = gson.toJson(obj);
        assertTrue(json.contains("3.14"));

        WithDouble des = gson.fromJson("{\"d\":2.718}", WithDouble.class);
        assertEquals(2.718, des.d, 1e-9);
    }

    // ----- Test for array field (indirect) -----

    static class WithArray {
        int[] values;
    }

    @Test
    public void testArrayField() {
        WithArray obj = new WithArray();
        obj.values = new int[]{1,2,3};
        String json = gson.toJson(obj);
        assertTrue(json.contains("[1,2,3]"));

        WithArray des = gson.fromJson("{\"values\":[4,5,6]}", WithArray.class);
        assertArrayEquals(new int[]{4,5,6}, des.values);
    }

    // ----- Additional edge case: object field with @SerializedName that differs -----

    static class NestedBean {
        @SerializedName("inner")
        SimpleBean innerField;
    }

    @Test
    public void testNestedSerializedName() {
        NestedBean obj = new NestedBean();
        obj.innerField = new SimpleBean();
        obj.innerField.name = "nested";
        obj.innerField.value = 7;
        String json = gson.toJson(obj);
        assertTrue(json.contains("\"inner\""));
        assertTrue(json.contains("\"custom_name\":\"nested\""));

        String jsonIn = "{\"inner\":{\"custom_name\":\"test\",\"value\":1,\"flag\":true}}";
        NestedBean des = gson.fromJson(jsonIn, NestedBean.class);
        assertEquals("test", des.innerField.name);
        assertEquals(1, des.innerField.value);
        assertEquals(Boolean.TRUE, des.innerField.flag);
    }
}