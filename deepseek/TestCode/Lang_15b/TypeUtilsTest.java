package org.apache.commons.lang3.reflect;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TypeUtilsTest {

    // Inner classes to reproduce the failing tests
    public static class Thing implements This<String, String> {
        @Override
        public String getKey() { return "key"; }
        @Override
        public String getValue() { return "value"; }
    }

    public interface This<K, V> {
        K getKey();
        V getValue();
    }

    // Additional inner classes for coverage
    public static class SimpleClass {}
    public static class GenericClass<T> {}
    public static class MultiBound<T extends Comparable<T> & List<String>> {}
    public static class WildcardHolder<T> { T value; }

    // Test for the known failing testGetTypeArguments
    @Test
    public void testGetTypeArguments() {
        // This test should return 2 type arguments for Thing implementing This<String,String>
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(Thing.class, This.class);
        assertNotNull("Type arguments should not be null", typeArgs);
        assertEquals("Expected 2 type arguments", 2, typeArgs.size());
        // Verify the actual types
        TypeVariable<?>[] params = This.class.getTypeParameters();
        assertEquals("First param should be String", String.class, typeArgs.get(params[0]));
        assertEquals("Second param should be String", String.class, typeArgs.get(params[1]));
    }

    // Test for the known failing testIsAssignable
    @Test
    public void testIsAssignable() {
        // Thing implements This<String,String>, so it should be assignable
        assertTrue("Thing should be assignable to This<String,String>",
                TypeUtils.isAssignable(Thing.class, This.class));
        // More specific: check with parameterized type
        // We need to construct a ParameterizedType for This<String,String>
        java.lang.reflect.ParameterizedType paramType = createParameterizedType(This.class, String.class, String.class);
        assertTrue("Thing should be assignable to This<String,String> parameterized",
                TypeUtils.isAssignable(Thing.class, paramType));
    }

    // Helper to create a ParameterizedType for testing
    private java.lang.reflect.ParameterizedType createParameterizedType(final Class<?> rawType, final Type... typeArgs) {
        return new java.lang.reflect.ParameterizedType() {
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

    // Edge case: null inputs
    @Test(expected = NullPointerException.class)
    public void testGetTypeArgumentsNullClass() {
        TypeUtils.getTypeArguments(null, This.class);
    }

    @Test(expected = NullPointerException.class)
    public void testGetTypeArgumentsNullToClass() {
        TypeUtils.getTypeArguments(Thing.class, null);
    }

    @Test(expected = NullPointerException.class)
    public void testIsAssignableNullFrom() {
        TypeUtils.isAssignable(null, This.class);
    }

    @Test(expected = NullPointerException.class)
    public void testIsAssignableNullTo() {
        TypeUtils.isAssignable(Thing.class, null);
    }

    // Test with simple classes (no generics)
    @Test
    public void testGetTypeArgumentsSimple() {
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(SimpleClass.class, Object.class);
        assertNotNull(typeArgs);
        assertTrue("No type arguments expected for simple class", typeArgs.isEmpty());
    }

    // Test with generic class and concrete type arguments
    @Test
    public void testGetTypeArgumentsGenericConcrete() {
        // GenericClass<String> should have one type argument String
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(GenericClass.class, GenericClass.class);
        assertNotNull(typeArgs);
        assertEquals(1, typeArgs.size());
        TypeVariable<?> param = GenericClass.class.getTypeParameters()[0];
        assertEquals(Object.class, typeArgs.get(param)); // raw type uses Object bound
    }

    // Test with wildcards
    @Test
    public void testGetTypeArgumentsWithWildcard() {
        // Use a parameterized type with wildcard
        java.lang.reflect.ParameterizedType wildcardType = createParameterizedType(GenericClass.class, new java.lang.reflect.WildcardType() {
            @Override
            public Type[] getUpperBounds() { return new Type[]{Object.class}; }
            @Override
            public Type[] getLowerBounds() { return new Type[]{}; }
        });
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(wildcardType, GenericClass.class);
        assertNotNull(typeArgs);
        assertEquals(1, typeArgs.size());
    }

    // Test isAssignable with various types
    @Test
    public void testIsAssignableSameClass() {
        assertTrue(TypeUtils.isAssignable(String.class, String.class));
        assertTrue(TypeUtils.isAssignable(Integer.class, Integer.class));
    }

    @Test
    public void testIsAssignableSuperclass() {
        assertTrue(TypeUtils.isAssignable(Thing.class, Object.class));
        assertTrue(TypeUtils.isAssignable(SimpleClass.class, Object.class));
    }

    @Test
    public void testIsAssignableNotAssignable() {
        assertFalse(TypeUtils.isAssignable(String.class, Integer.class));
        assertFalse(TypeUtils.isAssignable(SimpleClass.class, This.class));
    }

    // Test with parameterized types
    @Test
    public void testIsAssignableParameterized() {
        java.lang.reflect.ParameterizedType listString = createParameterizedType(List.class, String.class);
        assertTrue(TypeUtils.isAssignable(listString, List.class));
        // ArrayList<String> is assignable to List<String>
        java.lang.reflect.ParameterizedType arrayListString = createParameterizedType(java.util.ArrayList.class, String.class);
        assertTrue(TypeUtils.isAssignable(arrayListString, listString));
    }

    // Test with multiple bounds
    @Test
    public void testGetTypeArgumentsMultipleBounds() {
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(MultiBound.class, MultiBound.class);
        assertNotNull(typeArgs);
        assertEquals(1, typeArgs.size());
    }

    // Test with raw types
    @Test
    public void testIsAssignableRawType() {
        assertTrue(TypeUtils.isAssignable(Thing.class, This.class));
        // Raw This should be assignable from Thing
        assertTrue(TypeUtils.isAssignable(Thing.class, This.class));
    }

    // Additional coverage for getTypeArguments with interfaces
    @Test
    public void testGetTypeArgumentsInterface() {
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.getTypeArguments(Thing.class, This.class);
        assertNotNull(typeArgs);
        assertEquals(2, typeArgs.size());
    }

    // Test with array types
    @Test
    public void testIsAssignableArray() {
        assertTrue(TypeUtils.isAssignable(String[].class, Object[].class));
        assertFalse(TypeUtils.isAssignable(String[].class, Integer[].class));
    }

    // Test with primitive types
    @Test
    public void testIsAssignablePrimitive() {
        assertTrue(TypeUtils.isAssignable(int.class, int.class));
        assertTrue(TypeUtils.isAssignable(int.class, Integer.class));
        assertTrue(TypeUtils.isAssignable(Integer.class, int.class));
    }

    // Test with null type arguments
    @Test(expected = NullPointerException.class)
    public void testGetTypeArgumentsNullMap() {
        TypeUtils.getTypeArguments(Thing.class, This.class, null);
    }

    // Test with empty map
    @Test
    public void testGetTypeArgumentsWithEmptyMap() {
        java.util.Map<TypeVariable<?>, Type> map = new java.util.HashMap<>();
        Map<TypeVariable<?>, Type> result = TypeUtils.getTypeArguments(Thing.class, This.class, map);
        assertSame(map, result);
        assertEquals(2, result.size());
    }
}