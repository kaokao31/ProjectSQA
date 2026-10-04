package com.fasterxml.jackson.databind.type;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

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
    public void testConstructTypeWithClass() {
        JavaType type = typeFactory.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructTypeWithType() {
        Type stringType = String.class;
        JavaType type = typeFactory.constructType(stringType);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructTypeWithBindings() {
        JavaType type = typeFactory.constructType(String.class, TypeBindings.emptyBindings());
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructParametricType() {
        JavaType type = typeFactory.constructParametricType(Map.class, String.class, Integer.class);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertTrue(type.isMapType());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametricTypeWrongArgCount() {
        // Map requires 2 type parameters, supplying only 1 should throw exception
        typeFactory.constructParametricType(Map.class, String.class);
    }

    @Test
    public void testConstructArrayType() {
        ArrayType arrayType = typeFactory.constructArrayType(String.class);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArrayType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());

        JavaType stringType = typeFactory.constructType(String.class);
        ArrayType arrayType2 = typeFactory.constructArrayType(stringType);
        assertNotNull(arrayType2);
        assertTrue(arrayType2.isArrayType());
    }

    @Test
    public void testConstructCollectionType() {
        CollectionType collectionType = typeFactory.constructCollectionType(java.util.List.class, String.class);
        assertNotNull(collectionType);
        assertTrue(collectionType.isCollectionType());
        assertEquals(String.class, collectionType.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionLikeType() {
        JavaType type = typeFactory.constructCollectionLikeType(Collection.class, String.class);
        assertNotNull(type);
        assertEquals(Collection.class, type.getRawClass());
    }

    @Test
    public void testConstructMapType() {
        MapType mapType = typeFactory.constructMapType(java.util.HashMap.class, String.class, Integer.class);
        assertNotNull(mapType);
        assertTrue(mapType.isMapType());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapLikeType() {
        JavaType type = typeFactory.constructMapLikeType(Map.class, String.class, Integer.class);
        assertNotNull(type);
        assertTrue(type.isMapLikeType());
    }

    @Test
    public void testFindSpecializedType() {
        JavaType baseType = typeFactory.constructType(Collection.class);
        JavaType specialized = typeFactory.findSpecializedType(baseType, java.util.ArrayList.class);
        assertNotNull(specialized);
        assertEquals(java.util.ArrayList.class, specialized.getRawClass());
    }

    @Test
    public void testFromClassCaching() {
        JavaType t1 = typeFactory.constructType(String.class);
        JavaType t2 = typeFactory.constructType(String.class);
        assertSame(t1, t2);
    }

    @Test
    public void testClearCache() {
        typeFactory.clearCache();
        JavaType t1 = typeFactory.constructType(String.class);
        assertNotNull(t1);
    }

    @Test
    public void testConstructFromCanonical() throws IllegalArgumentException {
        JavaType type = typeFactory.constructFromCanonical("java.lang.String");
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testWithModifier() {
        TypeModifier modifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings bindings, TypeFactory typeFactory) {
                return type;
            }
        };
        TypeFactory modifiedFactory = typeFactory.withModifier(modifier);
        assertNotNull(modifiedFactory);
        assertNotSame(typeFactory, modifiedFactory);
    }
}