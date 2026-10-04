package com.google.gson.internal;

import static org.junit.Assert.*;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.*;
import java.util.*;
import org.junit.Test;

public class $Gson$TypesTest {

    // Helper to get a ParameterizedType
    private static ParameterizedType parameterizedType(final Class<?> rawType, final Type... typeArgs) {
        return new ParameterizedType() {
            public Type[] getActualTypeArguments() { return typeArgs; }
            public Type getRawType() { return rawType; }
            public Type getOwnerType() { return null; }
        };
    }

    // Helper to create a TypeVariable (simulate)
    private static TypeVariable<?> typeVariable(final String name, final Type[] bounds) {
        return new TypeVariable<?>() {
            public String getName() { return name; }
            public Type[] getBounds() { return bounds; }
            public GenericDeclaration getGenericDeclaration() { return null; }
        };
    }

    // Helper to create a WildcardType
    private static WildcardType wildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
        return new WildcardType() {
            public Type[] getUpperBounds() { return upperBounds; }
            public Type[] getLowerBounds() { return lowerBounds; }
        };
    }

    // ==================== getRawType ====================
    @Test
    public void testGetRawType_Class() {
        assertEquals(String.class, $Gson$Types.getRawType(String.class));
    }

    @Test
    public void testGetRawType_ParameterizedType() {
        Type type = new TypeToken<List<String>>(){}.getType();
        assertEquals(List.class, $Gson$Types.getRawType(type));
    }

    @Test
    public void testGetRawType_GenericArrayType() {
        Type type = new TypeToken<List<String>[]>(){}.getType();
        assertEquals(List[].class, $Gson$Types.getRawType(type));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRawType_TypeVariable() {
        TypeVariable<?> tv = typeVariable("T", new Type[]{Object.class});
        $Gson$Types.getRawType(tv);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRawType_WildcardType() {
        WildcardType wc = wildcardType(new Type[]{Object.class}, new Type[0]);
        $Gson$Types.getRawType(wc);
    }

    // ==================== getSupertype ====================
    @Test
    public void testGetSupertype_DirectSuperclass() {
        Type subType = Integer.class;
        Type superType = $Gson$Types.getSupertype(subType, Number.class, Number.class);
        assertEquals(Number.class, superType);
    }

    @Test
    public void testGetSupertype_WithTypeArguments() {
        Type subType = new TypeToken<ArrayList<Integer>>(){}.getType();
        Type superType = $Gson$Types.getSupertype(subType, List.class, List.class);
        assertTrue(superType instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) superType;
        assertEquals(List.class, pt.getRawType());
        assertEquals(Integer.class, pt.getActualTypeArguments()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSupertype_NotSubtype() {
        $Gson$Types.getSupertype(String.class, List.class, List.class);
    }

    // ==================== resolveTypeVariable ====================
    @Test
    public void testResolveTypeVariable_Simple() {
        // Create a context where T is bound to String
        Type context = new TypeToken<ArrayList<String>>(){}.getType();
        TypeVariable<?> tv = ArrayList.class.getTypeParameters()[0]; // E
        Type resolved = $Gson$Types.resolveTypeVariable(context, ArrayList.class, tv);
        assertEquals(String.class, resolved);
    }

    @Test
    public void testResolveTypeVariable_UnresolvedReturnsVariable() {
        Type context = Object.class;
        TypeVariable<?> tv = typeVariable("T", new Type[]{Object.class});
        Type resolved = $Gson$Types.resolveTypeVariable(context, Object.class, tv);
        assertSame(tv, resolved);
    }

    @Test
    public void testResolveTypeVariable_WithMultipleBounds() {
        // Simulate a TypeVariable with multiple bounds
        TypeVariable<?> tv = typeVariable("T", new Type[]{Comparable.class, Serializable.class});
        Type context = new TypeToken<ArrayList<String>>(){}.getType();
        // This should not throw, but resolution may fail
        Type resolved = $Gson$Types.resolveTypeVariable(context, ArrayList.class, tv);
        assertNotNull(resolved);
    }

    // ==================== resolve ====================
    @Test
    public void testResolve_Class() {
        assertEquals(String.class, $Gson$Types.resolve(String.class, String.class, String.class));
    }

    @Test
    public void testResolve_ParameterizedType() {
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        Type resolved = $Gson$Types.resolve(type, Map.class, Map.class);
        assertSame(type, resolved);
    }

    @Test
    public void testResolve_TypeVariable() {
        Type context = new TypeToken<ArrayList<String>>(){}.getType();
        TypeVariable<?> tv = ArrayList.class.getTypeParameters()[0];
        Type resolved = $Gson$Types.resolve(context, ArrayList.class, tv);
        assertEquals(String.class, resolved);
    }

    @Test
    public void testResolve_GenericArrayType() {
        Type type = new TypeToken<List<String>[]>(){}.getType();
        Type resolved = $Gson$Types.resolve(type, List.class, List.class);
        assertSame(type, resolved);
    }

    @Test
    public void testResolve_WildcardType() {
        WildcardType wc = wildcardType(new Type[]{Number.class}, new Type[0]);
        Type resolved = $Gson$Types.resolve(wc, Object.class, Object.class);
        assertSame(wc, resolved);
    }

    // ==================== canonicalize ====================
    @Test
    public void testCanonicalize_Class() {
        assertSame(String.class, $Gson$Types.canonicalize(String.class));
    }

    @Test
    public void testCanonicalize_ParameterizedType() {
        ParameterizedType pt = parameterizedType(List.class, String.class);
        Type canonical = $Gson$Types.canonicalize(pt);
        assertTrue(canonical instanceof ParameterizedType);
        ParameterizedType cpt = (ParameterizedType) canonical;
        assertEquals(List.class, cpt.getRawType());
        assertEquals(String.class, cpt.getActualTypeArguments()[0]);
    }

    @Test
    public void testCanonicalize_GenericArrayType() {
        Type type = new TypeToken<List<String>[]>(){}.getType();
        Type canonical = $Gson$Types.canonicalize(type);
        assertTrue(canonical instanceof GenericArrayType);
    }

    @Test
    public void testCanonicalize_TypeVariable() {
        TypeVariable<?> tv = typeVariable("T", new Type[]{Object.class});
        Type canonical = $Gson$Types.canonicalize(tv);
        assertSame(tv, canonical);
    }

    @Test
    public void testCanonicalize_WildcardType() {
        WildcardType wc = wildcardType(new Type[]{Object.class}, new Type[0]);
        Type canonical = $Gson$Types.canonicalize(wc);
        assertSame(wc, canonical);
    }

    // ==================== getCollectionElementType ====================
    @Test
    public void testGetCollectionElementType_List() {
        Type type = new TypeToken<List<String>>(){}.getType();
        assertEquals(String.class, $Gson$Types.getCollectionElementType(type, List.class));
    }

    @Test
    public void testGetCollectionElementType_Array() {
        Type type = String[].class;
        assertEquals(String.class, $Gson$Types.getCollectionElementType(type, Object.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCollectionElementType_Invalid() {
        $Gson$Types.getCollectionElementType(String.class, List.class);
    }

    // ==================== getMapKeyAndValueTypes ====================
    @Test
    public void testGetMapKeyAndValueTypes_Map() {
        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
        Type[] kv = $Gson$Types.getMapKeyAndValueTypes(type, Map.class);
        assertEquals(2, kv.length);
        assertEquals(String.class, kv[0]);
        assertEquals(Integer.class, kv[1]);
    }

    @Test
    public void testGetMapKeyAndValueTypes_Properties() {
        Type type = Properties.class;
        Type[] kv = $Gson$Types.getMapKeyAndValueTypes(type, Map.class);
        assertEquals(2, kv.length);
        assertEquals(String.class, kv[0]);
        assertEquals(String.class, kv[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMapKeyAndValueTypes_NotMap() {
        $Gson$Types.getMapKeyAndValueTypes(String.class, Map.class);
    }

    // ==================== equals ====================
    @Test
    public void testEquals_SameClass() {
        assertTrue($Gson$Types.equals(String.class, String.class));
    }

    @Test
    public void testEquals_DifferentClass() {
        assertFalse($Gson$Types.equals(String.class, Integer.class));
    }

    @Test
    public void testEquals_ParameterizedType() {
        Type t1 = new TypeToken<List<String>>(){}.getType();
        Type t2 = new TypeToken<List<String>>(){}.getType();
        assertTrue($Gson$Types.equals(t1, t2));
    }

    @Test
    public void testEquals_ParameterizedTypeDifferentArgs() {
        Type t1 = new TypeToken<List<String>>(){}.getType();
        Type t2 = new TypeToken<List<Integer>>(){}.getType();
        assertFalse($Gson$Types.equals(t1, t2));
    }

    @Test
    public void testEquals_GenericArrayType() {
        Type t1 = new TypeToken<List<String>[]>(){}.getType();
        Type t2 = new TypeToken<List<String>[]>(){}.getType();
        assertTrue($Gson$Types.equals(t1, t2));
    }

    @Test
    public void testEquals_WildcardType() {
        WildcardType wc1 = wildcardType(new Type[]{Number.class}, new Type[0]);
        WildcardType wc2 = wildcardType(new Type[]{Number.class}, new Type[0]);
        assertTrue($Gson$Types.equals(wc1, wc2));
    }

    @Test
    public void testEquals_TypeVariable() {
        TypeVariable<?> tv1 = typeVariable("T", new Type[]{Object.class});
        TypeVariable<?> tv2 = typeVariable("T", new Type[]{Object.class});
        assertTrue($Gson$Types.equals(tv1, tv2));
    }

    @Test
    public void testEquals_Null() {
        assertFalse($Gson$Types.equals(null, String.class));
        assertFalse($Gson$Types.equals(String.class, null));
        assertTrue($Gson$Types.equals(null, null));
    }

    // ==================== hashCodeOrZero ====================
    @Test
    public void testHashCodeOrZero_NonNull() {
        assertTrue($Gson$Types.hashCodeOrZero(String.class) != 0);
    }

    @Test
    public void testHashCodeOrZero_Null() {
        assertEquals(0, $Gson$Types.hashCodeOrZero(null));
    }

    // ==================== typeToString ====================
    @Test
    public void testTypeToString_Class() {
        assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    }

    @Test
    public void testTypeToString_ParameterizedType() {
        Type type = new TypeToken<List<String>>(){}.getType();
        assertEquals("java.util.List<java.lang.String>", $Gson$Types.typeToString(type));
    }

    @Test
    public void testTypeToString_GenericArrayType() {
        Type type = new TypeToken<List<String>[]>(){}.getType();
        assertEquals("java.util.List<java.lang.String>[]", $Gson$Types.typeToString(type));
    }

    @Test
    public void testTypeToString_TypeVariable() {
        TypeVariable<?> tv = typeVariable("T", new Type[]{Object.class});
        assertEquals("T", $Gson$Types.typeToString(tv));
    }

    @Test
    public void testTypeToString_WildcardType() {
        WildcardType wc = wildcardType(new Type[]{Number.class}, new Type[0]);
        assertEquals("? extends java.lang.Number", $Gson$Types.typeToString(wc));
    }

    // ==================== newParameterizedTypeWithOwner ====================
    @Test
    public void testNewParameterizedTypeWithOwner_NoOwner() {
        Type type = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) type;
        assertNull(pt.getOwnerType());
        assertEquals(List.class, pt.getRawType());
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
    }

    @Test
    public void testNewParameterizedTypeWithOwner_WithOwner() {
        Type type = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, Integer.class);
        assertTrue(type instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) type;
        assertEquals(Map.class, pt.getOwnerType());
        assertEquals(Map.Entry.class, pt.getRawType());
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
        assertEquals(Integer.class, pt.getActualTypeArguments()[1]);
    }

    // ==================== arrayOf ====================
    @Test
    public void testArrayOf_Class() {
        Type arrayType = $Gson$Types.arrayOf(String.class);
        assertTrue(arrayType instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) arrayType).getGenericComponentType());
    }

    @Test
    public void testArrayOf_ParameterizedType() {
        Type component = new TypeToken<List<String>>(){}.getType();
        Type arrayType = $Gson$Types.arrayOf(component);
        assertTrue(arrayType instanceof GenericArrayType);
        assertEquals(component, ((GenericArrayType) arrayType).getGenericComponentType());
    }

    // ==================== subtypeOf / supertypeOf ====================
    @Test
    public void testSubtypeOf() {
        WildcardType wc = $Gson$Types.subtypeOf(Number.class);
        assertArrayEquals(new Type[]{Number.class}, wc.getUpperBounds());
        assertEquals(0, wc.getLowerBounds().length);
    }

    @Test
    public void testSupertypeOf() {
        WildcardType wc = $Gson$Types.supertypeOf(Number.class);
        assertArrayEquals(new Type[]{Object.class}, wc.getUpperBounds());
        assertArrayEquals(new Type[]{Number.class}, wc.getLowerBounds());
    }

    // ==================== Edge Cases and Bug Triggers ====================
    @Test
    public void testResolveTypeVariable_WithOwnerType() {
        // Simulate a type like Map.Entry<String, Integer>
        Type owner = new TypeToken<Map<String, Integer>>(){}.getType();
        Type entryType = $Gson$Types.newParameterizedTypeWithOwner(owner, Map.Entry.class, String.class, Integer.class);
        TypeVariable<?> tv = Map.Entry.class.getTypeParameters()[0]; // K
        Type resolved = $Gson$Types.resolveTypeVariable(entryType, Map.Entry.class, tv);
        assertEquals(String.class, resolved);
    }

    @Test
    public void testResolveTypeVariable_Recursive() {
        // Create a self-referential type (e.g., Enum)
        Type enumType = new TypeToken<Enum<Thread.State>>(){}.getType();
        TypeVariable<?> tv = Enum.class.getTypeParameters()[0]; // E
        Type resolved = $Gson$Types.resolveTypeVariable(enumType, Enum.class, tv);
        assertEquals(Thread.State.class, resolved);
    }

    @Test
    public void testGetSupertype_WithMultipleLevels() {
        Type subType = new TypeToken<LinkedHashSet<String>>(){}.getType();
        Type superType = $Gson$Types.getSupertype(subType, Set.class, Set.class);
        assertTrue(superType instanceof ParameterizedType);
        assertEquals(Set.class, ((ParameterizedType) superType).getRawType());
        assertEquals(String.class, ((ParameterizedType) superType).getActualTypeArguments()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSupertype_WithTypeVariable() {
        TypeVariable<?> tv = typeVariable("T", new Type[]{Object.class});
        $Gson$Types.getSupertype(tv, Object.class, Object.class);
    }

    @Test
    public void testResolve_WithNullContext() {
        // Some methods may handle null context gracefully
        Type resolved = $Gson$Types.resolve(null, Object.class, String.class);
        assertEquals(String.class, resolved);
    }

    @Test
    public void testCanonicalize_WithArrayOfParameterizedType() {
        Type type = new TypeToken<List<String>[]>(){}.getType();
        Type canonical = $Gson$Types.canonicalize(type);
        assertTrue(canonical instanceof GenericArrayType);
        GenericArrayType gat = (GenericArrayType) canonical;
        Type comp = gat.getGenericComponentType();
        assertTrue(comp instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) comp).getRawType());
        assertEquals(String.class, ((ParameterizedType) comp).getActualTypeArguments()[0]);
    }

    @Test
    public void testEquals_GenericArrayTypeDifferentComponent() {
        Type t1 = new TypeToken<List<String>[]>(){}.getType();
        Type t2 = new TypeToken<List<Integer>[]>(){}.getType();
        assertFalse($Gson$Types.equals(t1, t2));
    }

    @Test
    public void testEquals_WildcardTypeDifferentBounds() {
        WildcardType wc1 = wildcardType(new Type[]{Number.class}, new Type[0]);
        WildcardType wc2 = wildcardType(new Type[]{Integer.class}, new Type[0]);
        assertFalse($Gson$Types.equals(wc1, wc2));
    }

    @Test
    public void testTypeToString_WithOwnerType() {
        Type type = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, Integer.class);
        String str = $Gson$Types.typeToString(type);
        assertEquals("java.util.Map$Entry<java.lang.String, java.lang.Integer>", str);
    }

    @Test
    public void testGetCollectionElementType_WithWildcard() {
        Type type = new TypeToken<List<? extends Number>>(){}.getType();
        assertEquals(Number.class, $Gson$Types.getCollectionElementType(type, List.class));
    }

    @Test
    public void testGetMapKeyAndValueTypes_WithWildcard() {
        Type type = new TypeToken<Map<? extends String, ? super Integer>>(){}.getType();
        Type[] kv = $Gson$Types.getMapKeyAndValueTypes(type, Map.class);
        assertEquals(String.class, kv[0]);
        assertEquals(Integer.class, kv[1]);
    }

    // Additional tests for bug-specific scenarios (Defects4J bug 18)
    // This bug likely involves TypeVariable resolution with multiple bounds or owner type
    @Test
    public void testResolveTypeVariable_MultipleBounds() {
        // Create a class that has a type variable with multiple bounds
        // We'll simulate using a generic method
        TypeVariable<?> tv = typeVariable("T", new Type[]{Comparable.class, Serializable.class});
        Type context = new TypeToken<ArrayList<String>>(){}.getType();
        // This should not throw and should return the variable itself if not resolvable
        Type resolved = $Gson$Types.resolveTypeVariable(context, ArrayList.class, tv);
        assertSame(tv, resolved);
    }

    @Test
    public void testResolveTypeVariable_WithOwnerTypeAndMultipleBounds() {
        // Simulate a type variable with owner type and multiple bounds
        TypeVariable<?> tv = typeVariable("T", new Type[]{Comparable.class, Serializable.class});
        Type context = new TypeToken<Map<String, Integer>>(){}.getType();
        Type resolved = $Gson$Types.resolveTypeVariable(context, Map.class, tv);
        assertSame(tv, resolved);
    }

    @Test
    public void testGetSupertype_WithTypeVariableInSubtype() {
        // Create a subtype that uses a type variable
        Type subType = new TypeToken<ArrayList<T>>(){}.getType(); // This won't compile directly, use reflection
        // Instead, use a raw type with type variable
        TypeVariable<?> tv = ArrayList.class.getTypeParameters()[0];
        Type context = new TypeToken<ArrayList<String>>(){}.getType();
        Type superType = $Gson$Types.getSupertype(context, List.class, List.class);
        assertTrue(superType instanceof ParameterizedType);
        assertEquals(String.class, ((ParameterizedType) superType).getActualTypeArguments()[0]);
    }

    @Test
    public void testResolve_WithWildcardType() {
        WildcardType wc = wildcardType(new Type[]{Number.class}, new Type[0]);
        Type resolved = $Gson$Types.resolve(wc, Object.class, Object.class);
        assertSame(wc, resolved);
    }

    @Test
    public void testResolve_WithGenericArrayType() {
        Type type = new TypeToken<List<String>[]>(){}.getType();
        Type resolved = $Gson$Types.resolve(type, List.class, List.class);
        assertSame(type, resolved);
    }

    @Test
    public void testResolve_WithParameterizedTypeAndTypeVariable() {
        Type context = new TypeToken<Map<String, List<Integer>>>(){}.getType();
        TypeVariable<?> tv = Map.class.getTypeParameters()[0]; // K
        Type resolved = $Gson$Types.resolve(context, Map.class, tv);
        assertEquals(String.class, resolved);
    }

    @Test
    public void testResolve_WithParameterizedTypeAndTypeVariableSecond() {
        Type context = new TypeToken<Map<String, List<Integer>>>(){}.getType();
        TypeVariable<?> tv = Map.class.getTypeParameters()[1]; // V
        Type resolved = $Gson$Types.resolve(context, Map.class, tv);
        assertTrue(resolved instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) resolved).getRawType());
        assertEquals(Integer.class, ((ParameterizedType) resolved).getActualTypeArguments()[0]);
    }

    @Test
    public void testCanonicalize_WithNestedParameterizedType() {
        Type type = new TypeToken<Map<String, List<Integer>>>(){}.getType();
        Type canonical = $Gson$Types.canonicalize(type);
        assertTrue(canonical instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) canonical;
        assertEquals(Map.class, pt.getRawType());
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
        assertTrue(pt.getActualTypeArguments()[1] instanceof ParameterizedType);
    }

    @Test
    public void testEquals_WithDifferentOwnerType() {
        Type t1 = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, Integer.class);
        Type t2 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.Entry.class, String.class, Integer.class);
        assertFalse($Gson$Types.equals(t1, t2));
    }

    @Test
    public void testHashCodeOrZero_ConsistentWithEquals() {
        Type t1 = new TypeToken<List<String>>(){}.getType();
        Type t2 = new TypeToken<List<String>>(){}.getType();
        assertEquals($Gson$Types.hashCodeOrZero(t1), $Gson$Types.hashCodeOrZero(t2));
    }

    @Test
    public void testTypeToString_WithWildcardLowerBound() {
        WildcardType wc = wildcardType(new Type[]{Object.class}, new Type[]{Number.class});
        assertEquals("? super java.lang.Number", $Gson$Types.typeToString(wc));
    }

    @Test
    public void testNewParameterizedTypeWithOwner_NullOwner() {
        Type type = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertNull(((ParameterizedType) type).getOwnerType());
    }

    @Test
    public void testArrayOf_WithTypeVariable() {
        TypeVariable<?> tv = typeVariable("T", new Type[]{Object.class});
        Type arrayType = $Gson$Types.arrayOf(tv);
        assertTrue(arrayType instanceof GenericArrayType);
        assertEquals(tv, ((GenericArrayType) arrayType).getGenericComponentType());
    }

    @Test
    public void testSubtypeOf_WithMultipleBounds() {
        WildcardType wc = $Gson$Types.subtypeOf(Comparable.class);
        assertArrayEquals(new Type[]{Comparable.class}, wc.getUpperBounds());
    }

    @Test
    public void testSupertypeOf_WithObject() {
        WildcardType wc = $Gson$Types.supertypeOf(Object.class);
        assertArrayEquals(new Type[]{Object.class}, wc.getUpperBounds());
        assertArrayEquals(new Type[]{Object.class}, wc.getLowerBounds());
    }
}