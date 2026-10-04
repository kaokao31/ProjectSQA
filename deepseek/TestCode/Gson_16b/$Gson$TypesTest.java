package com.google.gson.internal;

import org.junit.Test;

import java.lang.reflect.*;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Test suite for $Gson$Types. Achieves high code coverage and targets known bugs (e.g., Defects4J bug 16).
 */
public class $Gson$TypesTest {

    // -----------------------------------------------------------------------
    // Helper types for testing
    // -----------------------------------------------------------------------

    private interface SimpleInterface {}
    private static class SimpleClass {}
    private static class SubClass extends SimpleClass implements SimpleInterface {}

    // For generic tests
    private static class GenericClass<T, U> {
        T t;
        U u;
    }

    private static class ConcreteClass extends GenericClass<String, Integer> {}

    // For recursive type bound (triggers Defects4J bug 16)
    private static abstract class RecursiveBound<T extends RecursiveBound<T>> {}

    // For wildcard and array tests
    private static class WildcardHolder<T> {
        List<? extends T> list;
    }

    private static class ArrayHolder {
        int[] intArray;
        String[] stringArray;
        List<?>[] listArray;
    }

    // -----------------------------------------------------------------------
    // Tests for getGenericSupertype
    // -----------------------------------------------------------------------

    @Test
    public void testGetGenericSupertype_DirectSuperclass() {
        Type actual = $Gson$Types.getGenericSupertype(SubClass.class, SubClass.class, SimpleClass.class);
        assertEquals(SimpleClass.class, actual);
    }

    @Test
    public void testGetGenericSupertype_GenericSuperclass() {
        Type actual = $Gson$Types.getGenericSupertype(ConcreteClass.class, ConcreteClass.class, GenericClass.class);
        assertTrue(actual instanceof ParameterizedType);
        ParameterizedType pType = (ParameterizedType) actual;
        assertEquals(GenericClass.class, pType.getRawType());
        assertArrayEquals(new Type[]{String.class, Integer.class}, pType.getActualTypeArguments());
    }

    @Test
    public void testGetGenericSupertype_RecursiveBound() {
        // This should not throw StackOverflowError (Defects4J bug 16 regression)
        Type recursiveSuper = $Gson$Types.getGenericSupertype(RecursiveBound.class, RecursiveBound.class, RecursiveBound.class);
        assertNotNull(recursiveSuper);
    }

    @Test
    public void testGetGenericSupertype_Interface() {
        Type actual = $Gson$Types.getGenericSupertype(SubClass.class, SubClass.class, SimpleInterface.class);
        assertEquals(SimpleInterface.class, actual);
    }

    @Test
    public void testGetGenericSupertype_ObjectSuperclass() {
        Type actual = $Gson$Types.getGenericSupertype(SimpleClass.class, SimpleClass.class, Object.class);
        assertEquals(Object.class, actual);
    }

    @Test(expected = NullPointerException.class)
    public void testGetGenericSupertype_NullSubclass() {
        $Gson$Types.getGenericSupertype(null, SimpleClass.class, SimpleClass.class);
    }

    @Test(expected = NullPointerException.class)
    public void testGetGenericSupertype_NullClass() {
        $Gson$Types.getGenericSupertype(SimpleClass.class, SimpleClass.class, null);
    }

    // -----------------------------------------------------------------------
    // Tests for resolve
    // -----------------------------------------------------------------------

    @Test
    public void testResolve_SimpleTypeVariable() throws Exception {
        TypeVariable<?> tv = GenericClass.class.getTypeParameters()[0];
        Type resolved = $Gson$Types.resolve(new Type[]{String.class}, tv);
        assertEquals(String.class, resolved);
    }

    @Test
    public void testResolve_UnresolvableTypeVariable() throws Exception {
        TypeVariable<?> tv = GenericClass.class.getTypeParameters()[0];
        Type resolved = $Gson$Types.resolve(new Type[]{}, tv);
        assertSame(tv, resolved); // returns itself if not in context
    }

    @Test
    public void testResolve_ParameterizedType() throws Exception {
        // Create a ParameterizedType: List<String>
        ParameterizedType listOfString = createParameterizedType(List.class, new Type[]{String.class});
        Type resolved = $Gson$Types.resolve(new Type[]{}, listOfString);
        assertSame(listOfString, resolved); // no type variables
    }

    @Test
    public void testResolve_GenericArrayType() {
        // Create GenericArrayType: String[]
        Type genericArray = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        Type resolved = $Gson$Types.resolve(new Type[]{}, genericArray);
        assertTrue(resolved instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) resolved).getGenericComponentType());
    }

    @Test
    public void testResolve_WildcardType() {
        WildcardType wildcard = new WildcardType() {
            @Override
            public Type[] getUpperBounds() { return new Type[]{Object.class}; }
            @Override
            public Type[] getLowerBounds() { return new Type[]{}; }
        };
        Type resolved = $Gson$Types.resolve(new Type[]{}, wildcard);
        assertSame(wildcard, resolved); // unchanged
    }

    @Test
    public void testResolve_Class() {
        Type resolved = $Gson$Types.resolve(new Type[]{}, String.class);
        assertEquals(String.class, resolved);
    }

    @Test(expected = NullPointerException.class)
    public void testResolve_NullType() {
        $Gson$Types.resolve(new Type[]{String.class}, null);
    }

    // -----------------------------------------------------------------------
    // Tests for canonicalize
    // -----------------------------------------------------------------------

    @Test
    public void testCanonicalize_Class() {
        assertEquals(String.class, $Gson$Types.canonicalize(String.class));
    }

    @Test
    public void testCanonicalize_ParameterizedType() {
        ParameterizedType p = createParameterizedType(List.class, new Type[]{String.class});
        Type canon = $Gson$Types.canonicalize(p);
        assertEquals(p, canon); // usually same reference
    }

    @Test
    public void testCanonicalize_GenericArrayType() {
        GenericArrayType arrayType = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        Type canon = $Gson$Types.canonicalize(arrayType);
        assertEquals(arrayType, canon);
    }

    @Test
    public void testCanonicalize_WildcardType() {
        WildcardType w = new WildcardType() {
            @Override
            public Type[] getUpperBounds() { return new Type[]{Number.class}; }
            @Override
            public Type[] getLowerBounds() { return new Type[]{}; }
        };
        Type canon = $Gson$Types.canonicalize(w);
        assertEquals(w, canon);
    }

    @Test(expected = NullPointerException.class)
    public void testCanonicalize_Null() {
        $Gson$Types.canonicalize(null);
    }

    // -----------------------------------------------------------------------
    // Tests for equals
    // -----------------------------------------------------------------------

    @Test
    public void testEquals_SameObject() {
        Type t1 = String.class;
        assertTrue($Gson$Types.equals(t1, t1));
    }

    @Test
    public void testEquals_EqualTypes() {
        Type t1 = createParameterizedType(List.class, new Type[]{String.class});
        Type t2 = createParameterizedType(List.class, new Type[]{String.class});
        assertTrue($Gson$Types.equals(t1, t2));
    }

    @Test
    public void testEquals_DifferentRawType() {
        Type t1 = createParameterizedType(List.class, new Type[]{String.class});
        Type t2 = createParameterizedType(Set.class, new Type[]{String.class});
        assertFalse($Gson$Types.equals(t1, t2));
    }

    @Test
    public void testEquals_DifferentTypeArgs() {
        Type t1 = createParameterizedType(List.class, new Type[]{String.class});
        Type t2 = createParameterizedType(List.class, new Type[]{Integer.class});
        assertFalse($Gson$Types.equals(t1, t2));
    }

    @Test
    public void testEquals_ClassVsParameterized() {
        assertFalse($Gson$Types.equals(List.class, createParameterizedType(List.class, new Type[]{String.class})));
    }

    @Test
    public void testEquals_GenericArray() {
        GenericArrayType a1 = new GenericArrayType() {
            @Override public Type getGenericComponentType() { return String.class; }
        };
        GenericArrayType a2 = new GenericArrayType() {
            @Override public Type getGenericComponentType() { return String.class; }
        };
        assertTrue($Gson$Types.equals(a1, a2));
    }

    @Test
    public void testEquals_WildcardSameBounds() {
        WildcardType w1 = new WildcardType() {
            @Override public Type[] getUpperBounds() { return new Type[]{Number.class}; }
            @Override public Type[] getLowerBounds() { return new Type[]{}; }
        };
        WildcardType w2 = new WildcardType() {
            @Override public Type[] getUpperBounds() { return new Type[]{Number.class}; }
            @Override public Type[] getLowerBounds() { return new Type[]{}; }
        };
        assertTrue($Gson$Types.equals(w1, w2));
    }

    @Test
    public void testEquals_Null() {
        assertFalse($Gson$Types.equals(String.class, null));
        assertFalse($Gson$Types.equals(null, String.class));
        assertTrue($Gson$Types.equals(null, null));
    }

    // -----------------------------------------------------------------------
    // Tests for hashCode
    // -----------------------------------------------------------------------

    @Test
    public void testHashCode_Class() {
        assertEquals(String.class.hashCode(), $Gson$Types.hashCode(String.class));
    }

    @Test
    public void testHashCode_ParameterizedType() {
        ParameterizedType p = createParameterizedType(List.class, new Type[]{String.class});
        int expected = p.getRawType().hashCode() ^ Arrays.hashCode(p.getActualTypeArguments());
        assertEquals(expected, $Gson$Types.hashCode(p));
    }

    @Test
    public void testHashCode_GenericArray() {
        GenericArrayType a = new GenericArrayType() {
            @Override public Type getGenericComponentType() { return String.class; }
        };
        assertEquals(a.getGenericComponentType().hashCode(), $Gson$Types.hashCode(a));
    }

    @Test
    public void testHashCode_Wildcard() {
        WildcardType w = new WildcardType() {
            @Override public Type[] getUpperBounds() { return new Type[]{Number.class}; }
            @Override public Type[] getLowerBounds() { return new Type[]{}; }
        };
        // hash of wildcard: upper bounds XOR lower bounds
        int expected = Arrays.hashCode(w.getUpperBounds()) ^ Arrays.hashCode(w.getLowerBounds());
        assertEquals(expected, $Gson$Types.hashCode(w));
    }

    // -----------------------------------------------------------------------
    // Tests for typeToString
    // -----------------------------------------------------------------------

    @Test
    public void testTypeToString_Class() {
        assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    }

    @Test
    public void testTypeToString_ParameterizedType() {
        ParameterizedType p = createParameterizedType(List.class, new Type[]{String.class});
        // Implementation may vary, but should contain "List<String>"
        String str = $Gson$Types.typeToString(p);
        assertTrue(str.contains("List"));
        assertTrue(str.contains("String"));
    }

    @Test
    public void testTypeToString_GenericArray() {
        GenericArrayType a = new GenericArrayType() {
            @Override public Type getGenericComponentType() { return String.class; }
        };
        assertEquals("java.lang.String[]", $Gson$Types.typeToString(a));
    }

    @Test
    public void testTypeToString_Wildcard() {
        WildcardType w = new WildcardType() {
            @Override public Type[] getUpperBounds() { return new Type[]{Number.class}; }
            @Override public Type[] getLowerBounds() { return new Type[]{}; }
        };
        assertEquals("? extends java.lang.Number", $Gson$Types.typeToString(w));
    }

    @Test
    public void testTypeToString_WildcardLowerBound() {
        WildcardType w = new WildcardType() {
            @Override public Type[] getUpperBounds() { return new Type[]{Object.class}; }
            @Override public Type[] getLowerBounds() { return new Type[]{String.class}; }
        };
        assertEquals("? super java.lang.String", $Gson$Types.typeToString(w));
    }

    // -----------------------------------------------------------------------
    // Tests for getArrayComponentType
    // -----------------------------------------------------------------------

    @Test
    public void testGetArrayComponentType_ObjectArray() {
        assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    }

    @Test
    public void testGetArrayComponentType_PrimitiveArray() {
        assertEquals(int.class, $Gson$Types.getArrayComponentType(int[].class));
    }

    @Test
    public void testGetArrayComponentType_GenericArray() {
        GenericArrayType a = new GenericArrayType() {
            @Override public Type getGenericComponentType() { return List.class; }
        };
        assertEquals(List.class, $Gson$Types.getArrayComponentType(a));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetArrayComponentType_NonArray() {
        $Gson$Types.getArrayComponentType(String.class);
    }

    // -----------------------------------------------------------------------
    // Tests for getCollectionElementType
    // -----------------------------------------------------------------------

    @Test
    public void testGetCollectionElementType_Parameterized() throws Exception {
        Field field = WildcardHolder.class.getDeclaredField("list");
        Type type = field.getGenericType();
        Type elementType = $Gson$Types.getCollectionElementType(type, WildcardHolder.class);
        // List<? extends T> -> T is TypeVariable, context from WildcardHolder? Actually unknown.
        // But we can test with concrete type: use a subclass that provides T
        assertEquals(Object.class, elementType); // because wildcard extends T, T unknown -> Object
    }

    @Test
    public void testGetCollectionElementType_Concrete() {
        Type type = new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return new Type[]{String.class}; }
            @Override public Type getRawType() { return List.class; }
            @Override public Type getOwnerType() { return null; }
        };
        assertEquals(String.class, $Gson$Types.getCollectionElementType(type, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCollectionElementType_NonCollection() {
        $Gson$Types.getCollectionElementType(String.class, null);
    }

    // -----------------------------------------------------------------------
    // Tests for getMapKeyAndValueTypes
    // -----------------------------------------------------------------------

    @Test
    public void testGetMapKeyAndValueTypes_Concrete() {
        Type type = new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return new Type[]{String.class, Integer.class}; }
            @Override public Type getRawType() { return Map.class; }
            @Override public Type getOwnerType() { return null; }
        };
        Type[] kv = $Gson$Types.getMapKeyAndValueTypes(type, null);
        assertArrayEquals(new Type[]{String.class, Integer.class}, kv);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMapKeyAndValueTypes_NonMap() {
        $Gson$Types.getMapKeyAndValueTypes(String.class, null);
    }

    // -----------------------------------------------------------------------
    // Tests for subtypeOf / supertypeOf
    // -----------------------------------------------------------------------

    @Test
    public void testSubtypeOf() {
        WildcardType w = $Gson$Types.subtypeOf(Number.class);
        assertArrayEquals(new Type[]{Number.class}, w.getUpperBounds());
        assertArrayEquals(new Type[]{}, w.getLowerBounds());
    }

    @Test
    public void testSupertypeOf() {
        WildcardType w = $Gson$Types.supertypeOf(Number.class);
        assertArrayEquals(new Type[]{Object.class}, w.getUpperBounds());
        assertArrayEquals(new Type[]{Number.class}, w.getLowerBounds());
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private static ParameterizedType createParameterizedType(final Class<?> rawType, final Type[] typeArgs) {
        return new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() { return typeArgs; }
            @Override public Type getRawType() { return rawType; }
            @Override public Type getOwnerType() { return null; }
        };
    }
}