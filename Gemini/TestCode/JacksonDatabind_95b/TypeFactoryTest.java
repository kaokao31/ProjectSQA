package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;

import static org.junit.Assert.*;

public class TypeFactoryTest {

    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
    }

    @After
    public void tearDown() {
        typeFactory = null;
    }

    @Test
    public void testDefaultInstance() {
        TypeFactory tf1 = TypeFactory.defaultInstance();
        TypeFactory tf2 = TypeFactory.defaultInstance();
        assertNotNull(tf1);
        assertSame(tf1, tf2);
    }

    @Test
    public void testConstructTypeWithClass() {
        JavaType type = typeFactory.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.isTypeOrSubTypeOf(CharSequence.class));
    }

    @Test
    public void testConstructTypeWithTypeReference() {
        TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {};
        JavaType type = typeFactory.constructType(typeRef);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testConstructTypeWithJavaType() {
        JavaType baseType = typeFactory.constructType(Integer.class);
        JavaType type = typeFactory.constructType(baseType);
        assertSame(baseType, type);
    }

    @Test
    public void testConstructTypeWithGenericType() throws Exception {
        Type genericType = DummyClass.class.getField("stringList").getGenericType();
        JavaType type = typeFactory.constructType(genericType);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testConstructCollectionType() {
        CollectionType type = typeFactory.constructCollectionType(ArrayList.class, String.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionLikeType() {
        JavaType type = typeFactory.constructCollectionLikeType(ArrayList.class, Integer.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructArrayType() {
        ArrayType type = typeFactory.constructArrayType(String.class);
        assertNotNull(type);
        assertTrue(type.isArrayType());
        assertEquals(String.class, type.getContentType().getRawClass());

        JavaType baseType = typeFactory.constructType(Integer.class);
        ArrayType type2 = typeFactory.constructArrayType(baseType);
        assertNotNull(type2);
        assertEquals(Integer.class, type2.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapType() {
        MapType type = typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapLikeType() {
        JavaType type = typeFactory.constructMapLikeType(HashMap.class, String.class, Boolean.class);
        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Boolean.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructParametricType() {
        JavaType type = typeFactory.constructParametricType(Map.class, String.class, String.class);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(2, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
        assertEquals(String.class, type.containedType(1).getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametricTypeWrongArity() {
        // Map expects 2 parameters, passing only 1 should throw exception
        typeFactory.constructParametricType(Map.class, String.class);
    }

    @Test
    public void testConstructFromCanonical() {
        String canonical = "java.util.Map<java.lang.String,java.lang.Integer>";
        JavaType type = typeFactory.constructFromCanonical(canonical);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testFindTypeParameters() {
        JavaType type = typeFactory.constructType(StringList.class);
        JavaType[] params = typeFactory.findTypeParameters(type, Iterable.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(String.class, params[0].getRawClass());
    }

    @Test
    public void testMoreTypeConstructorsAndHelpers() {
        assertNotNull(typeFactory.uncheckedSimpleType(Integer.class));
        assertNotNull(typeFactory.constructSpecializedType(typeFactory.constructType(Number.class), Integer.class));
        assertNotNull(typeFactory.constructGeneralizedType(typeFactory.constructType(Integer.class), Number.class));
        assertNotNull(typeFactory.constructBlendedType(typeFactory.constructType(Integer.class), typeFactory.constructType(Number.class)));
    }

    @Test
    public void testModifiersAndCache() {
        TypeFactory modified = typeFactory.withModifier(null);
        assertNotNull(modified);

        TypeFactory cleared = typeFactory.withCache(null);
        assertNotNull(cleared);

        typeFactory.clearCache();
        assertEquals(0, typeFactory.cachedSerializersCount()); // just testing it doesn't throw
    }

    @Test
    public void testParserEdgeCases() {
        try {
            typeFactory.constructFromCanonical("invalid.canonical.Type[missing");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testConstructSimpleType() {
        JavaType type = typeFactory.constructSimpleType(List.class, new JavaType[]{typeFactory.constructType(String.class)});
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
    }

    // Helper classes for reflection and generics tests
    public static class DummyClass {
        public List<String> stringList;
    }

    public static abstract class StringList implements List<String> {
    }
}