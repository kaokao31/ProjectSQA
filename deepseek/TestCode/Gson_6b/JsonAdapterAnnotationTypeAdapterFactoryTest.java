package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.internal.ConstructorConstructor;
import org.junit.Before;
import org.junit.Test;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import static org.junit.Assert.*;

/**
 * Test suite for JsonAdapterAnnotationTypeAdapterFactory.
 * Designed to achieve maximum coverage and detect faults (e.g., Defects4J bug 6).
 */
public class JsonAdapterAnnotationTypeAdapterFactoryTest {

    private JsonAdapterAnnotationTypeAdapterFactory factory;
    private Gson gson;

    // --- Helper classes for testing ---

    // A simple TypeAdapter for String
    public static class StringTypeAdapter extends TypeAdapter<String> {
        @Override
        public String read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
            return in.nextString();
        }
        @Override
        public void write(com.google.gson.stream.JsonWriter out, String value) throws java.io.IOException {
            out.value(value);
        }
    }

    // A TypeAdapterFactory that creates StringTypeAdapter
    public static class StringTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() == String.class) {
                return (TypeAdapter<T>) new StringTypeAdapter();
            }
            return null;
        }
    }

    // A class annotated with @JsonAdapter pointing to a TypeAdapter
    @JsonAdapter(StringTypeAdapter.class)
    public static class AnnotatedWithAdapter {}

    // A class annotated with @JsonAdapter pointing to a TypeAdapterFactory
    @JsonAdapter(StringTypeAdapterFactory.class)
    public static class AnnotatedWithFactory {}

    // A class annotated with @JsonAdapter pointing to a class that itself has @JsonAdapter (recursive)
    @JsonAdapter(RecursiveAdapter.class)
    public static class AnnotatedWithRecursive {}

    // A TypeAdapter that is annotated with @JsonAdapter (recursive case)
    @JsonAdapter(StringTypeAdapter.class)
    public static class RecursiveAdapter extends TypeAdapter<String> {
        @Override
        public String read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
            return in.nextString();
        }
        @Override
        public void write(com.google.gson.stream.JsonWriter out, String value) throws java.io.IOException {
            out.value(value);
        }
    }

    // A class that is neither TypeAdapter nor TypeAdapterFactory
    public static class InvalidAdapterClass {}

    // A class annotated with @JsonAdapter pointing to InvalidAdapterClass
    @JsonAdapter(InvalidAdapterClass.class)
    public static class AnnotatedWithInvalid {}

    @Before
    public void setUp() {
        // ConstructorConstructor with no custom instance creators
        ConstructorConstructor constructorConstructor = new ConstructorConstructor(Collections.<Class<?>, com.google.gson.internal.InstanceCreator<?>>emptyMap());
        factory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        gson = new Gson();
    }

    // --- Test cases ---

    @Test
    public void testNoAnnotationReturnsNull() {
        // Empty annotations array
        Annotation[] annotations = new Annotation[0];
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class), annotations);
        assertNull("Expected null when no @JsonAdapter annotation present", adapter);
    }

    @Test
    public void testAnnotationWithTypeAdapter() {
        // Get annotations from a class that has @JsonAdapter with a TypeAdapter
        Annotation[] annotations = AnnotatedWithAdapter.class.getAnnotations();
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class), annotations);
        assertNotNull("Expected a TypeAdapter when @JsonAdapter specifies a TypeAdapter", adapter);
        assertTrue("Adapter should be instance of StringTypeAdapter", adapter instanceof StringTypeAdapter);
    }

    @Test
    public void testAnnotationWithTypeAdapterFactory() {
        Annotation[] annotations = AnnotatedWithFactory.class.getAnnotations();
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class), annotations);
        assertNotNull("Expected a TypeAdapter when @JsonAdapter specifies a TypeAdapterFactory", adapter);
        assertTrue("Adapter should be instance of StringTypeAdapter", adapter instanceof StringTypeAdapter);
    }

    @Test(expected = NullPointerException.class)
    public void testNullAnnotationsArray() {
        // Passing null for annotations should throw NullPointerException (bug 6)
        factory.create(gson, TypeToken.get(String.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullElementInAnnotationsArray() {
        // Array containing a null element should also throw NullPointerException
        Annotation[] annotations = new Annotation[] { null };
        factory.create(gson, TypeToken.get(String.class), annotations);
    }

    @Test
    public void testAnnotationWithInvalidValue() {
        // @JsonAdapter pointing to a class that is neither TypeAdapter nor TypeAdapterFactory
        Annotation[] annotations = AnnotatedWithInvalid.class.getAnnotations();
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class), annotations);
        assertNull("Expected null when annotation value is invalid", adapter);
    }

    @Test
    public void testAnnotationWithRecursiveAnnotation() {
        // Annotation value is a class that itself has @JsonAdapter
        Annotation[] annotations = AnnotatedWithRecursive.class.getAnnotations();
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class), annotations);
        assertNotNull("Expected a TypeAdapter even with recursive @JsonAdapter", adapter);
        // The adapter should be the one from the inner annotation (StringTypeAdapter)
        assertTrue("Adapter should be instance of StringTypeAdapter", adapter instanceof StringTypeAdapter);
    }

    @Test
    public void testMultipleAnnotationsOneWithJsonAdapter() {
        // Simulate multiple annotations, only one is @JsonAdapter
        Annotation[] annotations = new Annotation[] {
            new Annotation() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Deprecated.class;
                }
            },
            AnnotatedWithAdapter.class.getAnnotations()[0] // the @JsonAdapter annotation
        };
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class), annotations);
        assertNotNull("Should find @JsonAdapter among multiple annotations", adapter);
    }

    @Test
    public void testAnnotationOnTypeWithDifferentTarget() {
        // Test that the factory works when the annotation is on a type (class)
        // Already covered by previous tests, but ensure no regression
        Annotation[] annotations = AnnotatedWithAdapter.class.getAnnotations();
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class), annotations);
        assertNotNull(adapter);
    }

    @Test
    public void testNullGson() {
        // Passing null Gson should throw NullPointerException (defensive check)
        Annotation[] annotations = AnnotatedWithAdapter.class.getAnnotations();
        try {
            factory.create(null, TypeToken.get(String.class), annotations);
            fail("Expected NullPointerException for null Gson");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testNullTypeToken() {
        // Passing null TypeToken should throw NullPointerException
        Annotation[] annotations = AnnotatedWithAdapter.class.getAnnotations();
        try {
            factory.create(gson, null, annotations);
            fail("Expected NullPointerException for null TypeToken");
        } catch (NullPointerException e) {
            // expected
        }
    }
}