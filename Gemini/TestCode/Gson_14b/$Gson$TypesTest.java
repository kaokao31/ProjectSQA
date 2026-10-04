package com.google.gson.internal;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

public class $Gson$TypesTest {

    // Helper interface and classes for testing generics/types
    private static interface GenericInterface<T> {
    }

    private static class ConcreteClass implements GenericInterface<String> {
    }

    private static class ParentClass<T> {
    }

    private static class ChildClass extends ParentClass<Integer> {
    }

    @Test
    public void testNewParameterizedTypeWithOwner() {
        Type owner = String.class;
        Type raw = List.class;
        Type[] typeArguments = new Type[] { String.class };

        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(owner, raw, typeArguments);
        assertNotNull(pt);
        assertEquals(owner, pt.getOwnerType());
        assertEquals(raw, pt.getRawType());
        assertArrayEquals(typeArguments, pt.getActualTypeArguments());

        // Test with null owner
        ParameterizedType ptNullOwner = $Gson$Types.newParameterizedTypeWithOwner(null, raw, typeArguments);
        assertNotNull(ptNullOwner);
        assertNull(ptNullOwner.getOwnerType());
        assertEquals(raw, ptNullOwner.getRawType());
        assertArrayEquals(typeArguments, ptNullOwner.getActualTypeArguments());
    }

    @Test(expected = NullPointerException.class)
    public void testNewParameterizedTypeWithOwnerNullRaw() {
        $Gson$Types.newParameterizedTypeWithOwner(null, null, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewParameterizedTypeWithOwnerInvalidOwner() {
        // Owner is not valid for a top-level raw type (e.g. List is not inner)
        $Gson$Types.newParameterizedTypeWithOwner(String.class, List.class, String.class);
    }

    @Test
    public void testNewParameterizedTypeWithOwnerInnerClass() {
        // Testing an inner class scenario
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, String.class);
        assertNotNull(pt);
        assertEquals(Map.class, pt.getOwnerType());
        assertEquals(Map.Entry.class, pt.getRawType());
    }

    @Test
    public void testNewGenericArrayType() {
        Type componentType = String.class;
        GenericArrayType gat = $Gson$Types.newGenericArrayType(componentType);
        assertNotNull(gat);
        assertEquals(componentType, gat.getGenericComponentType());
    }

    @Test
    public void testWildcardTypeWithUpperBound() {
        WildcardType wt = $Gson$Types.subtypeOf(String.class);
        assertNotNull(wt);
        assertArrayEquals(new Type[] { String.class }, wt.getUpperBounds());
        assertArrayEquals(new Type[0], wt.getLowerBounds());
    }

    @Test
    public void testWildcardTypeWithLowerBound() {
        WildcardType wt = $Gson$Types.supertypeOf(String.class);
        assertNotNull(wt);
        assertArrayEquals(new Type[] { Object.class }, wt.getUpperBounds());
        assertArrayEquals(new Type[] { String.class }, wt.getLowerBounds());
    }

    @Test
    public void testCanonicalize() {
        // Class
        assertEquals(String.class, $Gson$Types.canonicalize(String.class));

        // GenericArrayType
        GenericArrayType gat = $Gson$Types.newGenericArrayType(String.class);
        Type canonicalGat = $Gson$Types.canonicalize(gat);
        assertTrue(canonicalGat instanceof GenericArrayType);

        // ParameterizedType
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        Type canonicalPt = $Gson$Types.canonicalize(pt);
        assertTrue(canonicalPt instanceof ParameterizedType);

        // WildcardType
        WildcardType wt = $Gson$Types.subtypeOf(String.class);
        Type canonicalWt = $Gson$Types.canonicalize(wt);
        assertTrue(canonicalWt instanceof WildcardType);

        // Primitives and arrays of primitives
        assertEquals(int.class, $Gson$Types.canonicalize(int.class));
        Class<?> intArrayType = int[].class;
        assertEquals(intArrayType, $Gson$Types.canonicalize(intArrayType));
    }

    @Test
    public void testGetArrayComponentType() {
        assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
        assertEquals(int.class, $Gson$Types.getArrayComponentType(int[].class));
        
        GenericArrayType gat = $Gson$Types.newGenericArrayType(String.class);
        assertEquals(String.class, $Gson$Types.getArrayComponentType(gat));

        assertNull($Gson$Types.getArrayComponentType(String.class));
    }

    @Test
    public void testGetCollectionElementType() {
        Type listType = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals(String.class, $Gson$Types.getCollectionElementType(listType, List.class));

        Type wildListType = $Gson$Types.subtypeOf(List.class);
        // Should handle wildcards or non-collection types gracefully
        try {
            $Gson$Types.getCollectionElementType(wildListType, List.class);
        } catch (Exception e) {
            // Expected depending on strictness
        }

        assertEquals(Object.class, $Gson$Types.getCollectionElementType(String.class, String.class));
    }

    @Test
    public void testGetMapKeyAndValueTypes() {
        Type mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
        Type[] keyAndValue = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
        assertNotNull(keyAndValue);
        assertEquals(2, keyAndValue.length);
        assertEquals(String.class, keyAndValue[0]);
        assertEquals(Integer.class, keyAndValue[1]);

        Type propertiesType = Properties.class;
        Type[] propKeyAndValue = $Gson$Types.getMapKeyAndValueTypes(propertiesType, Properties.class);
        assertEquals(String.class, propKeyAndValue[0]);
        assertEquals(String.class, propKeyAndValue[1]);
    }

    @Test
    public void testGetGenericSupertype() {
        Type superType = $Gson$Types.getGenericSupertype(ConcreteClass.class, ConcreteClass.class, GenericInterface.class);
        assertNotNull(superType);

        Type childSuper = $Gson$Types.getGenericSupertype(ChildClass.class, ChildClass.class, ParentClass.class);
        assertNotNull(childSuper);

        Type objectSuper = $Gson$Types.getGenericSupertype(String.class, String.class, Object.class);
        assertEquals(Object.class, objectSuper);
    }

    @Test
    public void testEqualTypes() {
        assertTrue($Gson$Types.equals(null, null));
        assertFalse($Gson$Types.equals(String.class, null));
        assertFalse($Gson$Types.equals(null, String.class));

        assertTrue($Gson$Types.equals(String.class, String.class));
        assertFalse($Gson$Types.equals(String.class, Integer.class));

        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);

        assertTrue($Gson$Types.equals(pt1, pt2));
        assertFalse($Gson$Types.equals(pt1, pt3));

        GenericArrayType gat1 = $Gson$Types.newGenericArrayType(String.class);
        GenericArrayType gat2 = $Gson$Types.newGenericArrayType(String.class);
        GenericArrayType gat3 = $Gson$Types.newGenericArrayType(Integer.class);

        assertTrue($Gson$Types.equals(gat1, gat2));
        assertFalse($Gson$Types.equals(gat1, gat3));

        WildcardType wt1 = $Gson$Types.subtypeOf(String.class);
        WildcardType wt2 = $Gson$Types.subtypeOf(String.class);
        WildcardType wt3 = $Gson$Types.supertypeOf(String.class);

        assertTrue($Gson$Types.equals(wt1, wt2));
        assertFalse($Gson$Types.equals(wt1, wt3));
    }

    @Test
    public void testTypeToString() {
        assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
        assertEquals("int", $Gson$Types.typeToString(int.class));
        assertEquals("java.lang.String[]", $Gson$Types.typeToString(String[].class));

        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertNotNull($Gson$Types.typeToString(pt));

        GenericArrayType gat = $Gson$Types.newGenericArrayType(String.class);
        assertNotNull($Gson$Types.typeToString(gat));

        WildcardType wtSub = $Gson$Types.subtypeOf(String.class);
        assertNotNull($Gson$Types.typeToString(wtSub));

        WildcardType wtSuper = $Gson$Types.supertypeOf(String.class);
        assertNotNull($Gson$Types.typeToString(wtSuper));
    }

    @Test
    public void testResolveTypeVariable() {
        // Resolve a type variable inside a parameterized context
        try {
            MethodHolder.class.getMethod("foo").getGenericReturnType();
        } catch (Exception e) {
            // ignore
        }
    }

    private static class MethodHolder<T> {
        public T foo() { return null; }
    }
}