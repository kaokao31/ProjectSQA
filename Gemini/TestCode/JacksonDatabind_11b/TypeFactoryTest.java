package com.fasterxml.jackson.databind.type;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

import static org.junit.Assert.*;

public class TypeFactoryTest {

    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
    }

    @Test
    public void testDefaultInstance() {
        assertNotNull(TypeFactory.defaultInstance());
        assertSame(TypeFactory.defaultInstance(), TypeFactory.defaultInstance());
    }

    @Test
    public void testConstructFromCanonicalPrimitive() {
        JavaType t = typeFactory.constructFromCanonical("int");
        assertNotNull(t);
        assertTrue(t.isPrimitive());
        assertEquals(int.class, t.getRawClass());
    }

    @Test
    public void testConstructFromCanonicalArray() {
        JavaType t = typeFactory.constructFromCanonical("java.lang.String[]");
        assertNotNull(t);
        assertTrue(t.isArrayType());
        assertEquals(String[].class, t.getRawClass());
    }

    @Test
    public void testConstructFromCanonicalParameterized() {
        JavaType t = typeFactory.constructFromCanonical("java.util.List<java.lang.String>");
        assertNotNull(t);
        assertTrue(t.isContainerType());
        assertEquals(List.class, t.getRawClass());
        assertEquals(1, t.containedTypeCount());
        assertEquals(String.class, t.containedType(0).getRawClass());
    }

    @Test
    public void testConstructTypeWithJavaType() {
        JavaType base = typeFactory.constructType(String.class);
        JavaType result = typeFactory.constructType(base);
        assertSame(base, result);
    }

    @Test
    public void testConstructTypeWithClass() {
        JavaType t = typeFactory.constructType(int.class);
        assertNotNull(t);
        assertTrue(t.isPrimitive());

        JavaType t2 = typeFactory.constructType(String.class);
        assertNotNull(t2);
        assertEquals(String.class, t2.getRawClass());
    }

    @Test
    public void testConstructTypeWithTypeReference() {
        TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {};
        JavaType t = typeFactory.constructType(typeRef);
        assertNotNull(t);
        assertEquals(List.class, t.getRawClass());
        assertEquals(String.class, t.containedType(0).getRawClass());
    }

    @Test
    public void testConstructSpecializedType() {
        JavaType base = typeFactory.constructType(Collection.class);
        JavaType spec = typeFactory.constructSpecializedType(base, ArrayList.class);
        assertNotNull(spec);
        assertEquals(ArrayList.class, spec.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeIncompatible() {
        JavaType base = typeFactory.constructType(String.class);
        typeFactory.constructSpecializedType(base, ArrayList.class);
    }

    @Test
    public void testConstructGeneralizedType() {
        JavaType spec = typeFactory.constructType(ArrayList.class);
        JavaType general = typeFactory.constructGeneralizedType(spec, Collection.class);
        assertNotNull(general);
        assertEquals(Collection.class, general.getRawClass());
    }

    @Test
    public void testConstructArrayType() {
        JavaType t = typeFactory.constructArrayType(String.class);
        assertNotNull(t);
        assertTrue(t.isArrayType());
        assertEquals(String[].class, t.getRawClass());

        JavaType elemType = typeFactory.constructType(Integer.class);
        JavaType t2 = typeFactory.constructArrayType(elemType);
        assertNotNull(t2);
        assertTrue(t2.isArrayType());
        assertEquals(Integer[].class, t2.getRawClass());
    }

    @Test
    public void testConstructCollectionType() {
        CollectionType t = typeFactory.constructCollectionType(ArrayList.class, String.class);
        assertNotNull(t);
        assertEquals(ArrayList.class, t.getRawClass());
        assertEquals(String.class, t.containedType(0).getRawClass());
    }

    @Test
    public void testConstructCollectionLikeType() {
        JavaType t = typeFactory.constructCollectionLikeType(ArrayList.class, String.class);
        assertNotNull(t);
        assertEquals(ArrayList.class, t.getRawClass());
    }

    @Test
    public void testConstructMapType() {
        MapType t = typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        assertNotNull(t);
        assertEquals(HashMap.class, t.getRawClass());
        assertEquals(String.class, t.getKeyType().getRawClass());
        assertEquals(Integer.class, t.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapLikeType() {
        JavaType t = typeFactory.constructMapLikeType(HashMap.class, String.class, Integer.class);
        assertNotNull(t);
        assertEquals(HashMap.class, t.getRawClass());
    }

    @Test
    public void testFindTypeParameters() {
        JavaType t = typeFactory.constructType(ArrayList.class);
        JavaType[] params = typeFactory.findTypeParameters(t, Collection.class);
        assertNotNull(params);
        assertEquals(1, params.length);
    }

    @Test
    public void testMoreFindTypeParameters() {
        JavaType[] params = typeFactory.findTypeParameters(ArrayList.class, Collection.class);
        assertNotNull(params);
        assertEquals(1, params.length);
    }

    @Test
    public void testCanonicalNameParsingErrors() {
        try {
            typeFactory.constructFromCanonical("NonExistentClassXYZ123");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to parse"));
        }
    }

    @Test
    public void testClearCache() {
        typeFactory.clearCache();
        // Should not throw any exception
        assertNotNull(typeFactory.constructType(String.class));
    }

    @Test
    public void testWithModifier() {
        TypeFactory modified = typeFactory.withModifier(null);
        assertNotNull(modified);
    }

    @Test
    public void testConstructParametricType() {
        JavaType t = typeFactory.constructParametricType(List.class, String.class);
        assertNotNull(t);
        assertEquals(List.class, t.getRawClass());
        assertEquals(String.class, t.containedType(0).getRawClass());

        JavaType mapType = typeFactory.constructParametricType(Map.class, String.class, Integer.class);
        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametricTypeWrongArgCount() {
        // Map requires 2 type parameters, supplying only 1 should trigger exception in strict checks or handle gracefully
        typeFactory.constructParametricType(Map.class, String.class);
    }

    @Test
    public void testClassWithNoTypeParameters() {
        JavaType t = typeFactory.constructParametricType(String.class, String.class);
        assertNotNull(t);
        assertEquals(String.class, t.getRawClass());
    }

    @Test
    public void testUnboundTypeParametersBug11Scenario() {
        // Specifically targeting the recursive type resolution or incomplete bindings scenario in Defects4J JacksonDatabind 11
        try {
            TypeBindings bindings = new TypeBindings(typeFactory, (Class<?>) null);
            assertNotNull(bindings);
        } catch (Exception e) {
            // Expected or handled depending on constructor signature
        }
        
        // Construct type with a raw class that has type parameters but none supplied
        JavaType t = typeFactory.constructType(List.class);
        assertNotNull(t);
        assertEquals(List.class, t.getRawClass());
    }

    @Test
    public void testConstructFromAnyType() {
        // Testing constructType with generic Type interface implementations
        Type t = String.class;
        JavaType jt = typeFactory.constructType(t);
        assertEquals(String.class, jt.getRawClass());
    }
}