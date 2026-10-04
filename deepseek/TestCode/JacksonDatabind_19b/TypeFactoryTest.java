package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.Before;
import org.junit.Test;

public class TypeFactoryTest {

    static class SimpleGeneric<T> { }

    static class Generic<T> { }

    static class Concrete extends Generic<String> { }

    static class SubGeneric<T> extends Generic<T> { }

    static class WildcardHolder {
        public List<? extends Number> list;
    }

    static class ArrayHolder {
        public List<String>[] lists;
    }

    static class Outer<T> {
        public class Inner<U> {
            public T outerValue;
            public U innerValue;
        }
    }

    private TypeFactory tf;

    @Before
    public void setUp() {
        tf = TypeFactory.defaultInstance();
    }

    @Test
    public void testConstructTypeForClass() {
        JavaType t = tf.constructType(String.class);
        assertNotNull(t);
        assertEquals(String.class, t.getRawClass());
        assertFalse(t.isContainerType());
    }

    @Test
    public void testConstructTypeParameterizedList() {
        JavaType t = tf.constructType(new TypeReference<List<String>>() { });
        assertNotNull(t);
        assertEquals(List.class, t.getRawClass());
        assertTrue(t.isContainerType());
        assertEquals(String.class, t.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeNestedMap() {
        JavaType t = tf.constructType(new TypeReference<Map<String, List<Integer>>>() { });
        assertNotNull(t);
        assertEquals(Map.class, t.getRawClass());
        assertEquals(String.class, t.getKeyType().getRawClass());
        assertEquals(List.class, t.getContentType().getRawClass());
        assertEquals(Integer.class, t.getContentType().getContentType().getRawClass());
    }

    @Test
    public void testConstructParametricType() {
        JavaType t = tf.constructParametricType(List.class, String.class);
        assertEquals(List.class, t.getRawClass());
        assertEquals(String.class, t.getContentType().getRawClass());

        JavaType map = tf.constructParametricType(Map.class, String.class, Integer.class);
        assertEquals(Map.class, map.getRawClass());
        assertEquals(String.class, map.getKeyType().getRawClass());
        assertEquals(Integer.class, map.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionType() {
        CollectionType ct = tf.constructCollectionType(List.class, String.class);
        assertEquals(List.class, ct.getRawClass());
        assertEquals(String.class, ct.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapType() {
        MapType mt = tf.constructMapType(Map.class, String.class, Integer.class);
        assertEquals(Map.class, mt.getRawClass());
        assertEquals(String.class, mt.getKeyType().getRawClass());
        assertEquals(Integer.class, mt.getContentType().getRawClass());
    }

    @Test
    public void testConstructArrayType() {
        ArrayType at = tf.constructArrayType(String.class);
        assertTrue(at.isArrayType());
        assertEquals(String.class, at.getContentType().getRawClass());

        ArrayType intArray = tf.constructArrayType(Integer.TYPE);
        assertEquals(Integer.TYPE, intArray.getContentType().getRawClass());
    }

    @Test
    public void testConstructSimpleType() {
        JavaType param = tf.constructType(String.class);
        JavaType t = tf.constructSimpleType(SimpleGeneric.class, new JavaType[] { param });
        assertEquals(SimpleGeneric.class, t.getRawClass());
        assertEquals(1, t.containedTypeCount());
        assertEquals(String.class, t.containedType(0).getRawClass());
    }

    @Test
    public void testUnknownType() {
        JavaType t = tf.unknownType();
        assertNotNull(t);
        assertEquals(Object.class, t.getRawClass());
    }

    @Test
    public void testSuperTypeResolution() {
        JavaType t = tf.constructType(Concrete.class);
        JavaType superType = t.findSuperType(Generic.class);
        assertNotNull(superType);
        assertEquals(Generic.class, superType.getRawClass());
        assertEquals(1, superType.containedTypeCount());
        assertEquals(String.class, superType.containedType(0).getRawClass());
    }

    @Test
    public void testSubclassTypeVariableResolution() {
        JavaType t = tf.constructType(new TypeReference<SubGeneric<String>>() { });
        JavaType superType = t.findSuperType(Generic.class);
        assertNotNull(superType);
        assertEquals(Generic.class, superType.getRawClass());
        assertEquals(1, superType.containedTypeCount());
        assertEquals(String.class, superType.containedType(0).getRawClass());
    }

    @Test
    public void testWildcardTypeResolution() throws Exception {
        Type type = WildcardHolder.class.getField("list").getGenericType();
        JavaType t = tf.constructType(type);
        assertEquals(List.class, t.getRawClass());
        assertEquals(Number.class, t.getContentType().getRawClass());
    }

    @Test
    public void testGenericArrayType() throws Exception {
        Type type = ArrayHolder.class.getField("lists").getGenericType();
        JavaType t = tf.constructType(type);
        assertTrue(t.isArrayType());
        assertEquals(List.class, t.getContentType().getRawClass());
        assertEquals(String.class, t.getContentType().getContentType().getRawClass());
    }

    @Test
    public void testInnerClassOwnerTypeResolution() throws Exception {
        JavaType innerType = tf.constructType(new TypeReference<Outer<String>.Inner<Integer>>() { });
        assertNotNull(innerType);
        assertEquals(Outer.Inner.class, innerType.getRawClass());

        JavaType innerValue = tf.constructType(Outer.Inner.class.getField("innerValue").getGenericType(), innerType);
        assertEquals(Integer.class, innerValue.getRawClass());

        JavaType outerValue = tf.constructType(Outer.Inner.class.getField("outerValue").getGenericType(), innerType);
        assertEquals(String.class, outerValue.getRawClass());
    }

    @Test
    public void testConstructSpecializedType() {
        JavaType list = tf.constructType(new TypeReference<List<String>>() { });
        JavaType arrayList = tf.constructSpecializedType(list, ArrayList.class);
        assertEquals(ArrayList.class, arrayList.getRawClass());
        assertEquals(String.class, arrayList.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeFromRawCollection() {
        JavaType t = tf.constructType(List.class);
        assertEquals(List.class, t.getRawClass());

        JavaType m = tf.constructType(Map.class);
        assertEquals(Map.class, m.getRawClass());
    }

    @Test
    public void testConstructTypeFromPrimitive() {
        JavaType t = tf.constructType(Integer.TYPE);
        assertEquals(Integer.TYPE, t.getRawClass());
    }

    @Test
    public void testConstructTypeFromArrayClass() {
        JavaType t = tf.constructType(String[].class);
        assertTrue(t.isArrayType());
        assertEquals(String.class, t.getContentType().getRawClass());
    }

    @Test
    public void testConstructFromCanonicalString() {
        JavaType t = tf.constructFromCanonicalString("java.lang.String");
        assertEquals(String.class, t.getRawClass());

        JavaType list = tf.constructFromCanonicalString("java.util.List<java.lang.String>");
        assertEquals(List.class, list.getRawClass());
        assertEquals(String.class, list.getContentType().getRawClass());

        JavaType map = tf.constructFromCanonicalString("java.util.Map<java.lang.String,java.lang.Integer>");
        assertEquals(Map.class, map.getRawClass());
        assertEquals(String.class, map.getKeyType().getRawClass());
        assertEquals(Integer.class, map.getContentType().getRawClass());
    }

    @Test
    public void testConstructFromCanonicalRoundTrip() {
        JavaType[] types = new JavaType[] {
            tf.constructType(String.class),
            tf.constructType(new TypeReference<List<String>>() { }),
            tf.constructType(new TypeReference<Map<String, List<Integer>>>() { }),
            tf.constructArrayType(String.class)
        };

        for (JavaType type : types) {
            String canonical = type.toCanonical();
            JavaType parsed = tf.constructFromCanonicalString(canonical);
            assertEquals("Roundtrip failed for " + canonical, canonical, parsed.toCanonical());
        }
    }
}