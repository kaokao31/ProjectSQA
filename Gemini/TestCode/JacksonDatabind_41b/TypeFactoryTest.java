package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.util.LRUMap;

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

        JavaType boolType = typeFactory.constructFromCanonical("boolean");
        assertTrue(boolType.isPrimitive());
        assertEquals(boolean.class, boolType.getRawClass());
    }

    @Test
    public void testConstructFromCanonicalArray() {
        JavaType t = typeFactory.constructFromCanonical("int[]");
        assertNotNull(t);
        assertTrue(t.isArrayType());
        assertEquals(int[].class, t.getRawClass());
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

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonicalInvalid() {
        typeFactory.constructFromCanonical("not.a.ValidType[invalid");
    }

    @Test
    public void testFindClass() throws ClassNotFoundException {
        Class<?> cls = typeFactory.findClass("java.lang.String");
        assertEquals(String.class, cls);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testFindClassNotFound() throws ClassNotFoundException {
        typeFactory.findClass("com.nonexistent.Class123456");
    }

    @Test
    public void testConstructTypeBasic() {
        JavaType type = typeFactory.constructType(String.class);
        assertEquals(String.class, type.getRawClass());

        Type typeRef = String.class;
        JavaType type2 = typeFactory.constructType(typeRef);
        assertEquals(String.class, type2.getRawClass());
    }

    @Test
    public void testConstructTypeReference() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        JavaType type = typeFactory.constructType(String.class, bindings);
        assertEquals(String.class, type.getRawClass());

        JavaType typeWithClass = typeFactory.constructType(String.class, String.class);
        assertEquals(String.class, typeWithClass.getRawClass());
    }

    @Test
    public void testConstructArrayType() {
        JavaType componentType = typeFactory.constructType(String.class);
        ArrayType arrayType = typeFactory.constructArrayType(componentType);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArrayType());
        assertEquals(String[].class, arrayType.getRawClass());

        ArrayType arrayTypeByClass = typeFactory.constructArrayType(String.class);
        assertNotNull(arrayTypeByClass);
        assertEquals(String[].class, arrayTypeByClass.getRawClass());
    }

    @Test
    public void testConstructCollectionType() {
        CollectionType colType = typeFactory.constructCollectionType(ArrayList.class, String.class);
        assertNotNull(colType);
        assertEquals(ArrayList.class, colType.getRawClass());
        assertEquals(String.class, colType.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionLikeType() {
        JavaType elementType = typeFactory.constructType(String.class);
        JavaType colLike = typeFactory.constructCollectionLikeType(ArrayList.class, elementType);
        assertNotNull(colLike);
        assertEquals(ArrayList.class, colLike.getRawClass());

        JavaType colLikeByClass = typeFactory.constructCollectionLikeType(ArrayList.class, String.class);
        assertNotNull(colLikeByClass);
        assertEquals(ArrayList.class, colLikeByClass.getRawClass());
    }

    @Test
    public void testConstructMapType() {
        MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        assertNotNull(mapType);
        assertEquals(HashMap.class, mapType.getRawClass());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapLikeType() {
        JavaType keyType = typeFactory.constructType(String.class);
        JavaType valType = typeFactory.constructType(Integer.class);
        JavaType mapLike = typeFactory.constructMapLikeType(HashMap.class, keyType, valType);
        assertNotNull(mapLike);
        assertEquals(HashMap.class, mapLike.getRawClass());

        JavaType mapLikeByClass = typeFactory.constructMapLikeType(HashMap.class, String.class, Integer.class);
        assertNotNull(mapLikeByClass);
        assertEquals(HashMap.class, mapLikeByClass.getRawClass());
    }

    @Test
    public void testConstructParametricType() {
        JavaType t = typeFactory.constructParametricType(List.class, String.class);
        assertNotNull(t);
        assertEquals(List.class, t.getRawClass());
        assertEquals(1, t.containedTypeCount());
        assertEquals(String.class, t.containedType(0).getRawClass());

        JavaType javaTypeArg = typeFactory.constructType(String.class);
        JavaType t2 = typeFactory.constructParametricType(List.class, javaTypeArg);
        assertNotNull(t2);
        assertEquals(List.class, t2.getRawClass());

        JavaType tMulti = typeFactory.constructParametricType(Map.class, new JavaType[] { javaTypeArg, javaTypeArg });
        assertNotNull(tMulti);
        assertEquals(Map.class, tMulti.getRawClass());
    }

    @Test
    public void testConstructSpecializedType() {
        JavaType baseType = typeFactory.constructType(Collection.class);
        JavaType specialized = typeFactory.constructSpecializedType(baseType, ArrayList.class);
        assertNotNull(specialized);
        assertEquals(ArrayList.class, specialized.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeInvalid() {
        JavaType baseType = typeFactory.constructType(ArrayList.class);
        // String is not a subclass of ArrayList
        typeFactory.constructSpecializedType(baseType, String.class);
    }

    @Test
    public void testGeneralizedType() {
        JavaType srcType = typeFactory.constructType(ArrayList.class);
        JavaType general = typeFactory.constructGeneralizedType(srcType, Collection.class);
        assertNotNull(general);
        assertEquals(Collection.class, general.getRawClass());
    }

    @Test
    public void testFromClass() {
        JavaType t = typeFactory.constructType(int.class);
        assertNotNull(t);
        assertTrue(t.isPrimitive());
        
        JavaType voidType = typeFactory.constructType(void.class);
        assertNotNull(voidType);
    }

    @Test
    public void testClearCache() {
        typeFactory.clearCache();
        // Just verify it doesn't throw and functions after clear
        JavaType t = typeFactory.constructType(String.class);
        assertNotNull(t);
    }

    @Test
    public void testWithModifier() {
        TypeModifier dummyModifier = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings bindings, TypeFactory typeFactory) {
                return type;
            }
            @Override
            public String toString() {
                return "DummyModifier";
            }
        };
        TypeFactory modified = typeFactory.withModifier(dummyModifier);
        assertNotNull(modified);
        assertNotSame(typeFactory, modified);
    }

    @Test
    public void testWithCache() {
        LRUMap<Object, JavaType> cache = new LRUMap<Object, JavaType>(16, 200);
        TypeFactory cached = typeFactory.withCache(cache);
        assertNotNull(cached);
        assertNotSame(typeFactory, cached);
    }

    @Test
    public void testResolveMemberType() {
        JavaType t = typeFactory.constructType(String.class);
        TypeBindings bindings = TypeBindings.emptyBindings();
        JavaType resolved = typeFactory.resolveMemberType(String.class, bindings);
        assertNotNull(resolved);
    }
}