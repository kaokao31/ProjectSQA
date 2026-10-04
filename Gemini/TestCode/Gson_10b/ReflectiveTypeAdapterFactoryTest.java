package com.google.gson.internal.bind;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ReflectiveTypeAdapterFactoryTest {

    private ReflectiveTypeAdapterFactory factory;
    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(Collections.emptyMap());
        FieldNamingStrategy fieldNamingPolicy = gson.fieldNamingStrategy();
        Excluder excluder = gson.excluder();
        JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        
        factory = new ReflectiveTypeAdapterFactory(
                constructorConstructor, fieldNamingPolicy, excluder, jsonAdapterFactory);
    }

    @Test
    public void testCreateWithPrimitiveOrUnsupportedType() {
        TypeAdapter<Integer> adapter = factory.create(gson, TypeToken.get(int.class));
        assertNull(adapter);

        TypeAdapter<String> stringAdapter = factory.create(gson, TypeToken.get(String.class));
        assertNull(stringAdapter);
    }

    @Test
    public void testCreateWithValidClass() {
        TypeAdapter<SimpleModel> adapter = factory.create(gson, TypeToken.get(SimpleModel.class));
        assertNotNull(adapter);
    }

    @Test
    public void testFieldExclusion() {
        TypeAdapter<ModelWithExcludedFields> adapter = factory.create(gson, TypeToken.get(ModelWithExcludedFields.class));
        assertNotNull(adapter);
    }

    @Test
    public void testSerializedNameAnnotation() {
        TypeAdapter<ModelWithSerializedName> adapter = factory.create(gson, TypeToken.get(ModelWithSerializedName.class));
        assertNotNull(adapter);
    }

    @Test
    public void testJsonAdapterFieldAnnotation() {
        TypeAdapter<ModelWithJsonAdapterField> adapter = factory.create(gson, TypeToken.get(ModelWithJsonAdapterField.class));
        assertNotNull(adapter);
    }

    @Test
    public void testGetFieldNamesWithSerializedName() throws Exception {
        Field field = ModelWithSerializedName.class.getDeclaredField("renamedField");
        List<String> fieldNames = ReflectiveTypeAdapterFactory.getFieldNames(gson.fieldNamingStrategy(), field);
        assertTrue(fieldNames.contains("custom_name"));
        assertTrue(fieldNames.contains("alternate_name"));
    }

    @Test
    public void testGetFieldNamesWithoutSerializedName() throws Exception {
        Field field = SimpleModel.class.getDeclaredField("name");
        List<String> fieldNames = ReflectiveTypeAdapterFactory.getFieldNames(gson.fieldNamingStrategy(), field);
        assertTrue(fieldNames.contains("name"));
    }

    @Test
    public void testExcludeFieldStatic() {
        Excluder excluder = new Excluder();
        boolean exclude = ReflectiveTypeAdapterFactory.excludeField(SimpleModel.class.getDeclaredField("STATIC_FIELD"), true, excluder);
        assertTrue(exclude);
    }

    @Test
    public void testAdapterSerializationAndDeserialization() {
        TypeAdapter<SimpleModel> adapter = factory.create(gson, TypeToken.get(SimpleModel.class));
        assertNotNull(adapter);
        
        SimpleModel model = new SimpleModel();
        model.name = "test";
        model.value = 123;
        
        String json = gson.toJson(model);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("123"));

        SimpleModel parsed = gson.fromJson(json, SimpleModel.class);
        assertNotNull(parsed);
        assertEquals("test", parsed.name);
        assertEquals(123, parsed.value);
    }

    // Helper classes for testing
    private static class SimpleModel {
        public static String STATIC_FIELD = "static";
        public String name;
        public int value;
    }

    private static class ModelWithExcludedFields {
        public transient int transientField;
        public static int staticField;
    }

    private static class ModelWithSerializedName {
        @SerializedName(value = "custom_name", alternate = {"alternate_name"})
        public String renamedField;
    }

    private static class CustomAnnotationAdapter extends TypeAdapter<String> {
        @Override
        public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            return in.nextString();
        }
    }

    private static class ModelWithJsonAdapterField {
        @JsonAdapter(CustomAnnotationAdapter.class)
        public String customAdapterField;
    }
}