package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;

public class JsonAdapterAnnotationTypeAdapterFactoryTest {

    private ConstructorConstructor constructorConstructor;
    private JsonAdapterAnnotationTypeAdapterFactory factory;

    @Before
    public void setUp() {
        constructorConstructor = new ConstructorConstructor(Collections.emptyMap());
        factory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
    }

    @JsonAdapter(DummyTypeAdapter.class)
    private static class AnnotatedClass {
    }

    private static class DummyTypeAdapter extends TypeAdapter<AnnotatedClass> {
        @Override
        public void write(JsonWriter out, AnnotatedClass value) throws IOException {
            out.beginObject();
            out.endObject();
        }

        @Override
        public AnnotatedClass read(JsonReader in) throws IOException {
            in.beginObject();
            in.endObject();
            return new AnnotatedClass();
        }
    }

    @JsonAdapter(DummyFactory.class)
    private static class AnnotatedClassWithFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return null;
        }
    }

    private static class DummyFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return new DummyTypeAdapter();
        }
    }

    @JsonAdapter(InvalidAdapter.class)
    private static class AnnotatedClassWithInvalidAdapter {
    }

    private static class InvalidAdapter {
        // Not a TypeAdapter and not a TypeAdapterFactory
    }

    @Test
    public void testNullAnnotation() {
        Gson gson = new Gson();
        TypeToken<String> typeToken = TypeToken.get(String.class);
        TypeAdapter<String> adapter = factory.create(gson, typeToken);
        Assert.assertNull(adapter);
    }

    @Test
    public void testAnnotatedClassWithTypeAdapter() {
        Gson gson = new Gson();
        TypeToken<AnnotatedClass> typeToken = TypeToken.get(AnnotatedClass.class);
        TypeAdapter<AnnotatedClass> adapter = factory.create(gson, typeToken);
        Assert.assertNotNull(adapter);
        Assert.assertTrue(adapter instanceof DummyTypeAdapter);
    }

    @Test
    public void testAnnotatedClassWithTypeAdapterFactory() {
        Gson gson = new Gson();
        TypeToken<AnnotatedClassWithFactory> typeToken = TypeToken.get(AnnotatedClassWithFactory.class);
        TypeAdapter<AnnotatedClassWithFactory> adapter = factory.create(gson, typeToken);
        Assert.assertNotNull(adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnnotatedClassWithInvalidAdapterThrowsException() {
        Gson gson = new Gson();
        TypeToken<AnnotatedClassWithInvalidAdapter> typeToken = TypeToken.get(AnnotatedClassWithInvalidAdapter.class);
        factory.create(gson, typeToken);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetTypeAdapterDirect() throws Exception {
        java.lang.reflect.Method method = JsonAdapterAnnotationTypeAdapterFactory.class.getDeclaredMethod(
                "getTypeAdapter", ConstructorConstructor.class, Gson.class, TypeToken.class, JsonAdapter.class);
        method.setAccessible(true);

        JsonAdapter annotation = AnnotatedClass.class.getAnnotation(JsonAdapter.class);
        Gson gson = new Gson();
        TypeToken<AnnotatedClass> typeToken = TypeToken.get(AnnotatedClass.class);

        TypeAdapter<?> adapter = (TypeAdapter<?>) method.invoke(null, constructorConstructor, gson, typeToken, annotation);
        Assert.assertNotNull(adapter);
    }
}