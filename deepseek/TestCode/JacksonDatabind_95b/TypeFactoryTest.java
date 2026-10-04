package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class TypeFactoryTest {

    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
    }

    // --- constructType(Class<?>) ---

    @Test
    public void testConstructTypeSimpleClass() {
        JavaType type = typeFactory.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.isReferenceType() || !type.isContainerType());
    }

    @Test
    public void testConstructTypePrimitive() {
        JavaType type = typeFactory.constructType(int.class);
        assertNotNull(type);
        assertEquals(Integer.class, type.getRawClass()); // primitive mapped to wrapper
    }

    @Test
    public void testConstructTypeArray() {
        JavaType type = typeFactory.constructType(int[].class);
        assertNotNull(type);
        assertTrue(type.isArrayType());
        assertEquals(int.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeObjectArray() {
        JavaType type = typeFactory.constructType(String[].class);
        assertNotNull(type);
        assertTrue(type.isArrayType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeGenericList() {
        // Use a parameterized type: List<String>
        Type genericType = new TypeReference<List<String>>() {}.getType();
        JavaType type = typeFactory.constructType(genericType);
        assertNotNull(type);
        assertTrue(type.isCollectionLikeType());
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testConstructTypeGenericMap() {
        Type genericType = new TypeReference<Map<String, Integer>>() {}.getType();
        JavaType type = typeFactory.constructType(genericType);
        assertNotNull(type);
        assertTrue(type.isMapLikeType());
        assertEquals(Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructTypeNullClass() {
        typeFactory.constructType((Class<?>) null);
    }

    // --- constructArrayType ---

    @Test
    public void testConstructArrayTypeFromClass() {
        JavaType elementType = typeFactory.constructType(String.class);
        JavaType arrayType = typeFactory.constructArrayType(elementType);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArrayType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructArrayTypeNullElement() {
        typeFactory.constructArrayType((JavaType) null);
    }

    // --- constructCollectionType ---

    @Test
    public void testConstructCollectionType() {
        JavaType elementType = typeFactory.constructType(String.class);
        JavaType collectionType = typeFactory.constructCollectionType(ArrayList.class, elementType);
        assertNotNull(collectionType);
        assertTrue(collectionType.isCollectionLikeType());
        assertEquals(ArrayList.class, collectionType.getRawClass());
        assertEquals(String.class, collectionType.containedType(0).getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructCollectionTypeNonCollection() {
        JavaType elementType = typeFactory.constructType(String.class);
        typeFactory.constructCollectionType(String.class, elementType);
    }

    // --- constructMapType ---

    @Test
    public void testConstructMapType() {
        JavaType keyType = typeFactory.constructType(String.class);
        JavaType valueType = typeFactory.constructType(Integer.class);
        JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);
        assertNotNull(mapType);
        assertTrue(mapType.isMapLikeType());
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.containedType(0).getRawClass());
        assertEquals(Integer.class, mapType.containedType(1).getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructMapTypeNonMap() {
        JavaType keyType = typeFactory.constructType(String.class);
        JavaType valueType = typeFactory.constructType(Integer.class);
        typeFactory.constructMapType(String.class, keyType, valueType);
    }

    // --- constructParametricType ---

    @Test
    public void testConstructParametricTypeSimple() {
        JavaType paramType = typeFactory.constructParametricType(List.class, String.class);
        assertNotNull(paramType);
        assertTrue(paramType.isCollectionLikeType());
        assertEquals(List.class, paramType.getRawClass());
        assertEquals(String.class, paramType.containedType(0).getRawClass());
    }

    @Test
    public void testConstructParametricTypeNested() {
        JavaType paramType = typeFactory.constructParametricType(Map.class, String.class, List.class);
        assertNotNull(paramType);
        assertTrue(paramType.isMapLikeType());
        assertEquals(Map.class, paramType.getRawClass());
        assertEquals(String.class, paramType.containedType(0).getRawClass());
        assertEquals(List.class, paramType.containedType(1).getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametricTypeInvalid() {
        typeFactory.constructParametricType(String.class, Integer.class);
    }

    // --- constructSimpleType ---

    @Test
    public void testConstructSimpleType() {
        JavaType simpleType = typeFactory.constructSimpleType(String.class, null);
        assertNotNull(simpleType);
        assertEquals(String.class, simpleType.getRawClass());
        assertFalse(simpleType.isContainerType());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructSimpleTypeNullClass() {
        typeFactory.constructSimpleType(null, null);
    }

    // --- constructRawCollectionType ---

    @Test
    public void testConstructRawCollectionType() {
        JavaType rawCollType = typeFactory.constructRawCollectionType(ArrayList.class);
        assertNotNull(rawCollType);
        assertTrue(rawCollType.isCollectionLikeType());
        assertEquals(ArrayList.class, rawCollType.getRawClass());
        // raw type should have no type parameters resolved
        assertEquals(0, rawCollType.containedTypeCount());
    }

    // --- constructRawMapType ---

    @Test
    public void testConstructRawMapType() {
        JavaType rawMapType = typeFactory.constructRawMapType(HashMap.class);
        assertNotNull(rawMapType);
        assertTrue(rawMapType.isMapLikeType());
        assertEquals(HashMap.class, rawMapType.getRawClass());
        assertEquals(0, rawMapType.containedTypeCount());
    }

    // --- constructSpecializedType ---

    @Test
    public void testConstructSpecializedType() {
        JavaType baseType = typeFactory.constructType(List.class);
        JavaType specialized = typeFactory.constructSpecializedType(baseType, ArrayList.class);
        assertNotNull(specialized);
        assertEquals(ArrayList.class, specialized.getRawClass());
        // should preserve type parameters if any
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeIncompatible() {
        JavaType baseType = typeFactory.constructType(List.class);
        typeFactory.constructSpecializedType(baseType, String.class);
    }

    // --- _resolveType (via constructType with Type) ---

    @Test
    public void testResolveTypeWithTypeVariable() {
        // Use a class with type variable
        Type genericType = new TypeReference<Comparable<String>>() {}.getType();
        JavaType type = typeFactory.constructType(genericType);
        assertNotNull(type);
        assertEquals(Comparable.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testResolveTypeWithWildcard() {
        Type genericType = new TypeReference<List<? extends Number>>() {}.getType();
        JavaType type = typeFactory.constructType(genericType);
        assertNotNull(type);
        assertTrue(type.isCollectionLikeType());
        JavaType elementType = type.containedType(0);
        assertTrue(elementType.isTypeOrSubTypeOf(Number.class));
    }

    // --- Edge cases: empty arrays, null types, etc. ---

    @Test
    public void testConstructTypeVoid() {
        JavaType type = typeFactory.constructType(void.class);
        assertNotNull(type);
        assertEquals(Void.class, type.getRawClass());
    }

    @Test
    public void testConstructTypeFromJavaType() {
        JavaType original = typeFactory.constructType(String.class);
        JavaType copy = typeFactory.constructType(original);
        assertSame(original, copy); // should return same instance
    }

    @Test
    public void testConstructTypeFromTypeBindings() {
        // Simulate a parameterized type with bindings
        // This may require more setup; we'll test basic path
        JavaType stringType = typeFactory.constructType(String.class);
        TypeBindings bindings = TypeBindings.create(stringType);
        JavaType type = typeFactory.constructType(List.class, bindings);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    // --- Test for potential bug: raw types and type resolution ---

    @Test
    public void testRawTypeResolution() {
        // Construct raw List type and then try to resolve with generics
        JavaType rawList = typeFactory.constructRawCollectionType(List.class);
        JavaType resolved = typeFactory.constructSpecializedType(rawList, ArrayList.class);
        // Should produce ArrayList with no type parameters (raw)
        assertEquals(ArrayList.class, resolved.getRawClass());
        assertEquals(0, resolved.containedTypeCount());
    }

    @Test
    public void testGenericArrayType() {
        // List<String>[] 
        Type genericArrayType = new TypeReference<List<String>[]>() {}.getType();
        JavaType type = typeFactory.constructType(genericArrayType);
        assertNotNull(type);
        assertTrue(type.isArrayType());
        JavaType componentType = type.getContentType();
        assertTrue(componentType.isCollectionLikeType());
        assertEquals(List.class, componentType.getRawClass());
        assertEquals(String.class, componentType.containedType(0).getRawClass());
    }

    // --- Test for bug 95: likely issue with type resolution for certain patterns ---
    // This test is designed to trigger the specific bug if present

    @Test
    public void testBug95Trigger() {
        // Bug 95 might involve resolving types with multiple levels of generics
        // or handling of raw types in certain contexts.
        // We'll create a complex generic type and verify resolution.
        Type complexType = new TypeReference<Map<String, List<Integer>>>() {}.getType();
        JavaType type = typeFactory.constructType(complexType);
        assertNotNull(type);
        assertTrue(type.isMapLikeType());
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
        JavaType valueType = type.containedType(1);
        assertTrue(valueType.isCollectionLikeType());
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.containedType(0).getRawClass());
    }

    // --- Helper inner class for TypeReference ---
    private static abstract class TypeReference<T> {
        private final Type type;

        protected TypeReference() {
            java.lang.reflect.Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof java.lang.reflect.ParameterizedType) {
                this.type = ((java.lang.reflect.ParameterizedType) superClass).getActualTypeArguments()[0];
            } else {
                throw new RuntimeException("TypeReference constructed without type parameter");
            }
        }

        public Type getType() {
            return type;
        }
    }
}