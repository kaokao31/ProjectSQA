package org.mockito.internal.util.reflection;

import static org.junit.Assert.*;
import static org.mockito.internal.util.reflection.GenericMetadataSupport.*;

import java.lang.reflect.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for GenericMetadataSupport.
 * Covers parameterized types, type variables, wildcards, raw types, and edge cases.
 */
public class GenericMetadataSupportTest {

    // Helper generic classes for testing
    private static class SimpleGeneric<T> {
        T value;
    }

    private static class BoundedGeneric<T extends Comparable<T>> {
        T value;
    }

    private static class MultiParam<K, V> {
        K key;
        V value;
    }

    private static class NestedGeneric<T> {
        List<T> list;
    }

    private static class WildcardGeneric<T extends List<?>> {
        T list;
    }

    private static class RawTypeHolder {
        SimpleGeneric rawField; // raw type
    }

    private GenericMetadataSupport support;

    @Before
    public void setUp() {
        // Initialize with a default context if needed
        support = null;
    }

    // ---------- inferFrom tests ----------

    @Test
    public void testInferFromParameterizedType() throws Exception {
        Field field = SimpleGeneric.class.getDeclaredField("value");
        Type type = field.getGenericType();
        support = inferFrom(type);
        assertNotNull(support);
        assertTrue(support instanceof GenericMetadataSupport.ParameterizedTypeImpl);
    }

    @Test
    public void testInferFromTypeVariable() throws Exception {
        Field field = SimpleGeneric.class.getDeclaredField("value");
        Type type = field.getGenericType();
        // type is TypeVariable, but inferFrom should handle it
        support = inferFrom(type);
        assertNotNull(support);
    }

    @Test
    public void testInferFromWildcardType() throws Exception {
        // Create a wildcard type via reflection
        Type wildcardType = WildcardType.class.getDeclaredField("list").getGenericType();
        support = inferFrom(wildcardType);
        assertNotNull(support);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInferFromNull() {
        inferFrom(null);
    }

    @Test
    public void testInferFromRawType() throws Exception {
        Field field = RawTypeHolder.class.getDeclaredField("rawField");
        Type type = field.getGenericType(); // raw type
        support = inferFrom(type);
        assertNotNull(support);
        // Raw type should resolve to Object
        assertEquals(Object.class, support.getRawType());
    }

    // ---------- resolveType tests ----------

    @Test
    public void testResolveTypeWithParameterizedType() throws Exception {
        // Create a parameterized type: SimpleGeneric<String>
        ParameterizedType pType = createParameterizedType(SimpleGeneric.class, String.class);
        support = inferFrom(pType);
        Type resolved = support.resolveType(pType);
        assertNotNull(resolved);
        assertEquals(String.class, resolved);
    }

    @Test
    public void testResolveTypeWithTypeVariable() throws Exception {
        // Use a class with type variable
        Field field = SimpleGeneric.class.getDeclaredField("value");
        Type type = field.getGenericType(); // T
        support = inferFrom(type);
        // Without actual type mapping, resolveType should return the variable itself
        Type resolved = support.resolveType(type);
        assertTrue(resolved instanceof TypeVariable);
    }

    @Test
    public void testResolveTypeWithWildcard() throws Exception {
        // Wildcard type
        Type wildcardType = WildcardType.class.getDeclaredField("list").getGenericType();
        support = inferFrom(wildcardType);
        Type resolved = support.resolveType(wildcardType);
        assertNotNull(resolved);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveTypeNull() {
        support = inferFrom(String.class);
        support.resolveType(null);
    }

    // ---------- readTypeVariables tests ----------

    @Test
    public void testReadTypeVariablesWithParameterizedType() throws Exception {
        ParameterizedType pType = createParameterizedType(SimpleGeneric.class, Integer.class);
        support = inferFrom(pType);
        Map<TypeVariable, Type> map = support.readTypeVariables();
        assertNotNull(map);
        assertEquals(1, map.size());
        TypeVariable tv = (TypeVariable) SimpleGeneric.class.getTypeParameters()[0];
        assertTrue(map.containsKey(tv));
        assertEquals(Integer.class, map.get(tv));
    }

    @Test
    public void testReadTypeVariablesWithBoundedType() throws Exception {
        ParameterizedType pType = createParameterizedType(BoundedGeneric.class, String.class);
        support = inferFrom(pType);
        Map<TypeVariable, Type> map = support.readTypeVariables();
        assertNotNull(map);
        assertEquals(1, map.size());
        TypeVariable tv = (TypeVariable) BoundedGeneric.class.getTypeParameters()[0];
        assertTrue(map.containsKey(tv));
        assertEquals(String.class, map.get(tv));
    }

    @Test
    public void testReadTypeVariablesWithMultipleParams() throws Exception {
        ParameterizedType pType = createParameterizedType(MultiParam.class, String.class, Integer.class);
        support = inferFrom(pType);
        Map<TypeVariable, Type> map = support.readTypeVariables();
        assertEquals(2, map.size());
        TypeVariable[] tvs = MultiParam.class.getTypeParameters();
        assertEquals(String.class, map.get(tvs[0]));
        assertEquals(Integer.class, map.get(tvs[1]));
    }

    @Test
    public void testReadTypeVariablesWithRawType() throws Exception {
        // Raw type should have empty map
        support = inferFrom(SimpleGeneric.class);
        Map<TypeVariable, Type> map = support.readTypeVariables();
        assertTrue(map.isEmpty());
    }

    // ---------- readTypeParameters tests ----------

    @Test
    public void testReadTypeParametersWithParameterizedType() throws Exception {
        ParameterizedType pType = createParameterizedType(SimpleGeneric.class, Double.class);
        support = inferFrom(pType);
        Map<TypeVariable, Type> map = support.readTypeParameters();
        assertNotNull(map);
        assertEquals(1, map.size());
        TypeVariable tv = (TypeVariable) SimpleGeneric.class.getTypeParameters()[0];
        assertTrue(map.containsKey(tv));
        assertEquals(Double.class, map.get(tv));
    }

    @Test
    public void testReadTypeParametersWithRawType() throws Exception {
        support = inferFrom(SimpleGeneric.class);
        Map<TypeVariable, Type> map = support.readTypeParameters();
        assertTrue(map.isEmpty());
    }

    // ---------- getActualTypeArguments tests ----------

    @Test
    public void testGetActualTypeArgumentsWithParameterizedType() throws Exception {
        ParameterizedType pType = createParameterizedType(MultiParam.class, Boolean.class, Float.class);
        support = inferFrom(pType);
        Type[] args = support.getActualTypeArguments();
        assertNotNull(args);
        assertEquals(2, args.length);
        assertEquals(Boolean.class, args[0]);
        assertEquals(Float.class, args[1]);
    }

    @Test
    public void testGetActualTypeArgumentsWithRawType() throws Exception {
        support = inferFrom(SimpleGeneric.class);
        Type[] args = support.getActualTypeArguments();
        assertNull(args);
    }

    // ---------- getRawType tests ----------

    @Test
    public void testGetRawTypeWithParameterizedType() throws Exception {
        ParameterizedType pType = createParameterizedType(SimpleGeneric.class, String.class);
        support = inferFrom(pType);
        assertEquals(SimpleGeneric.class, support.getRawType());
    }

    @Test
    public void testGetRawTypeWithRawType() throws Exception {
        support = inferFrom(SimpleGeneric.class);
        assertEquals(SimpleGeneric.class, support.getRawType());
    }

    @Test
    public void testGetRawTypeWithTypeVariable() throws Exception {
        Field field = SimpleGeneric.class.getDeclaredField("value");
        Type type = field.getGenericType();
        support = inferFrom(type);
        // For type variable, raw type is the bound (Object if no explicit bound)
        assertEquals(Object.class, support.getRawType());
    }

    @Test
    public void testGetRawTypeWithBoundedTypeVariable() throws Exception {
        Field field = BoundedGeneric.class.getDeclaredField("value");
        Type type = field.getGenericType();
        support = inferFrom(type);
        // Bound is Comparable<T>, raw type should be Comparable
        assertEquals(Comparable.class, support.getRawType());
    }

    // ---------- extra edge cases ----------

    @Test
    public void testInferFromClass() {
        support = inferFrom(String.class);
        assertNotNull(support);
        assertEquals(String.class, support.getRawType());
    }

    @Test
    public void testInferFromParameterizedTypeWithNested() throws Exception {
        // NestedGeneric<List<String>>
        ParameterizedType inner = createParameterizedType(ArrayList.class, String.class);
        ParameterizedType outer = createParameterizedType(NestedGeneric.class, inner);
        support = inferFrom(outer);
        assertNotNull(support);
        assertEquals(NestedGeneric.class, support.getRawType());
        Type[] args = support.getActualTypeArguments();
        assertEquals(1, args.length);
        assertTrue(args[0] instanceof ParameterizedType);
        ParameterizedType innerArg = (ParameterizedType) args[0];
        assertEquals(ArrayList.class, innerArg.getRawType());
        assertEquals(String.class, innerArg.getActualTypeArguments()[0]);
    }

    @Test
    public void testInferFromWildcardWithUpperBound() throws Exception {
        // Wildcard with upper bound: ? extends Number
        WildcardType wildcard = createWildcardType(Number.class, null);
        support = inferFrom(wildcard);
        assertNotNull(support);
        assertEquals(Number.class, support.getRawType());
    }

    @Test
    public void testInferFromWildcardWithLowerBound() throws Exception {
        // Wildcard with lower bound: ? super Integer
        WildcardType wildcard = createWildcardType(null, Integer.class);
        support = inferFrom(wildcard);
        assertNotNull(support);
        assertEquals(Object.class, support.getRawType()); // lower bound not used for raw type
    }

    @Test
    public void testInferFromArrayType() {
        Type arrayType = String[].class;
        support = inferFrom(arrayType);
        assertNotNull(support);
        assertEquals(String[].class, support.getRawType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInferFromUnsupportedType() {
        // Create a custom Type that is not supported
        Type unsupported = new Type() {
            @Override
            public String getTypeName() {
                return "custom";
            }
        };
        inferFrom(unsupported);
    }

    // ---------- helper methods ----------

    private static ParameterizedType createParameterizedType(Class<?> rawType, Type... typeArgs) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return typeArgs;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    private static WildcardType createWildcardType(Type upperBound, Type lowerBound) {
        return new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return upperBound != null ? new Type[]{upperBound} : new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return lowerBound != null ? new Type[]{lowerBound} : new Type[]{};
            }
        };
    }
}