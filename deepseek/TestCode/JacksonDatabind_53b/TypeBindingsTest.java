package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive JUnit 4 test suite for TypeBindings.
 * Designed to achieve high line/branch coverage and detect potential faults
 * (e.g., JacksonDatabind bug #53 related to unresolved type variables).
 */
public class TypeBindingsTest {

    private TypeBindings emptyBindings;
    private TypeBindings singleBindings;
    private TypeBindings multiBindings;
    private TypeBindings withUnresolved;

    // Helper to create a TypeVariable for testing
    private static class TestTypeVariable implements TypeVariable<?> {
        private final String name;
        private final Type[] bounds;

        TestTypeVariable(String name, Type... bounds) {
            this.name = name;
            this.bounds = bounds.length == 0 ? new Type[]{Object.class} : bounds;
        }

        @Override
        public Type[] getBounds() {
            return bounds;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<?> getGenericDeclaration() {
            return Object.class;
        }
    }

    @Before
    public void setUp() {
        // Empty bindings
        emptyBindings = TypeBindings.emptyBindings();

        // Single binding: T -> String
        TypeVariable<?> tv1 = new TestTypeVariable("T");
        singleBindings = TypeBindings.create(tv1, String.class);

        // Multiple bindings: K -> Integer, V -> Double
        TypeVariable<?> tvK = new TestTypeVariable("K");
        TypeVariable<?> tvV = new TestTypeVariable("V");
        multiBindings = TypeBindings.create(
            new TypeVariable<?>[]{tvK, tvV},
            new Type[]{Integer.class, Double.class}
        );

        // Bindings with unresolved type variable (self-reference or missing)
        TypeVariable<?> tvUnresolved = new TestTypeVariable("X");
        withUnresolved = TypeBindings.create(tvUnresolved, tvUnresolved); // bound to itself
    }

    // --- Basic construction and size ---

    @Test
    public void testEmptyBindingsSize() {
        assertEquals(0, emptyBindings.size());
    }

    @Test
    public void testSingleBindingsSize() {
        assertEquals(1, singleBindings.size());
    }

    @Test
    public void testMultiBindingsSize() {
        assertEquals(2, multiBindings.size());
    }

    @Test
    public void testWithUnresolvedSize() {
        assertEquals(1, withUnresolved.size());
    }

    // --- getBoundType ---

    @Test
    public void testGetBoundTypeOnEmpty() {
        assertNull(emptyBindings.getBoundType(0));
        assertNull(emptyBindings.getBoundType(-1));
        assertNull(emptyBindings.getBoundType(1));
    }

    @Test
    public void testGetBoundTypeSingle() {
        assertEquals(String.class, singleBindings.getBoundType(0));
        assertNull(singleBindings.getBoundType(1));
        assertNull(singleBindings.getBoundType(-1));
    }

    @Test
    public void testGetBoundTypeMulti() {
        assertEquals(Integer.class, multiBindings.getBoundType(0));
        assertEquals(Double.class, multiBindings.getBoundType(1));
        assertNull(multiBindings.getBoundType(2));
        assertNull(multiBindings.getBoundType(-1));
    }

    @Test
    public void testGetBoundTypeUnresolved() {
        // When bound is a TypeVariable, getBoundType should return the variable itself
        Type bound = withUnresolved.getBoundType(0);
        assertNotNull(bound);
        assertTrue(bound instanceof TypeVariable);
        assertEquals("X", ((TypeVariable<?>) bound).getName());
    }

    // --- findType ---

    @Test
    public void testFindTypeWithMatchingName() {
        Type result = singleBindings.findType("T");
        assertEquals(String.class, result);
    }

    @Test
    public void testFindTypeWithNonMatchingName() {
        Type result = singleBindings.findType("U");
        assertNull(result);
    }

    @Test
    public void testFindTypeWithNullName() {
        Type result = singleBindings.findType(null);
        assertNull(result);
    }

    @Test
    public void testFindTypeOnEmpty() {
        assertNull(emptyBindings.findType("T"));
    }

    @Test
    public void testFindTypeMulti() {
        assertEquals(Integer.class, multiBindings.findType("K"));
        assertEquals(Double.class, multiBindings.findType("V"));
        assertNull(multiBindings.findType("X"));
    }

    @Test
    public void testFindTypeUnresolved() {
        Type result = withUnresolved.findType("X");
        assertNotNull(result);
        assertTrue(result instanceof TypeVariable);
    }

    // --- hasUnbound ---

    @Test
    public void testHasUnboundEmpty() {
        assertFalse(emptyBindings.hasUnbound());
    }

    @Test
    public void testHasUnboundSingleResolved() {
        assertFalse(singleBindings.hasUnbound());
    }

    @Test
    public void testHasUnboundMultiResolved() {
        assertFalse(multiBindings.hasUnbound());
    }

    @Test
    public void testHasUnboundWithUnresolved() {
        assertTrue(withUnresolved.hasUnbound());
    }

    // --- toString ---

    @Test
    public void testToStringEmpty() {
        assertEquals("<>", emptyBindings.toString());
    }

    @Test
    public void testToStringSingle() {
        String s = singleBindings.toString();
        assertTrue(s.contains("T"));
        assertTrue(s.contains("String"));
    }

    @Test
    public void testToStringMulti() {
        String s = multiBindings.toString();
        assertTrue(s.contains("K"));
        assertTrue(s.contains("Integer"));
        assertTrue(s.contains("V"));
        assertTrue(s.contains("Double"));
    }

    @Test
    public void testToStringUnresolved() {
        String s = withUnresolved.toString();
        assertTrue(s.contains("X"));
        // Should show that X is bound to itself (type variable)
        assertTrue(s.contains("X"));
    }

    // --- equals and hashCode ---

    @Test
    public void testEqualsSameInstance() {
        assertEquals(emptyBindings, emptyBindings);
        assertEquals(singleBindings, singleBindings);
    }

    @Test
    public void testEqualsDifferentEmpty() {
        TypeBindings otherEmpty = TypeBindings.emptyBindings();
        assertEquals(emptyBindings, otherEmpty);
        assertEquals(emptyBindings.hashCode(), otherEmpty.hashCode());
    }

    @Test
    public void testEqualsDifferentContent() {
        assertNotEquals(emptyBindings, singleBindings);
        assertNotEquals(singleBindings, multiBindings);
    }

    @Test
    public void testEqualsNull() {
        assertFalse(emptyBindings.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(emptyBindings.equals("some string"));
    }

    @Test
    public void testEqualsSameContent() {
        TypeVariable<?> tv1 = new TestTypeVariable("T");
        TypeBindings otherSingle = TypeBindings.create(tv1, String.class);
        assertEquals(singleBindings, otherSingle);
        assertEquals(singleBindings.hashCode(), otherSingle.hashCode());
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = emptyBindings.hashCode();
        int hash2 = emptyBindings.hashCode();
        assertEquals(hash1, hash2);
    }

    // --- Edge cases: null type variable or bound ---

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullTypeVar() {
        TypeBindings.create((TypeVariable<?>) null, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullBound() {
        TypeVariable<?> tv = new TestTypeVariable("T");
        TypeBindings.create(tv, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithMismatchedArrays() {
        TypeVariable<?>[] tvars = {new TestTypeVariable("A"), new TestTypeVariable("B")};
        Type[] bounds = {String.class}; // only one bound
        TypeBindings.create(tvars, bounds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullArray() {
        TypeBindings.create((TypeVariable<?>[]) null, new Type[]{String.class});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullBoundsArray() {
        TypeVariable<?>[] tvars = {new TestTypeVariable("A")};
        TypeBindings.create(tvars, null);
    }

    // --- TypeVariable with multiple bounds ---

    @Test
    public void testTypeVariableWithMultipleBounds() {
        TypeVariable<?> tv = new TestTypeVariable("M", Comparable.class, Serializable.class);
        TypeBindings bindings = TypeBindings.create(tv, String.class);
        assertEquals(String.class, bindings.getBoundType(0));
        assertFalse(bindings.hasUnbound());
    }

    // --- Binding to primitive types ---

    @Test
    public void testBindingToPrimitive() {
        TypeVariable<?> tv = new TestTypeVariable("P");
        TypeBindings bindings = TypeBindings.create(tv, int.class);
        assertEquals(int.class, bindings.getBoundType(0));
    }

    // --- Binding to array types ---

    @Test
    public void testBindingToArray() {
        TypeVariable<?> tv = new TestTypeVariable("A");
        TypeBindings bindings = TypeBindings.create(tv, String[].class);
        assertEquals(String[].class, bindings.getBoundType(0));
    }

    // --- Binding to parameterized type (simulate) ---
    // We'll use a simple generic class to create a parameterized type

    @Test
    public void testBindingToParameterizedType() throws Exception {
        // Create a parameterized type: List<String>
        java.lang.reflect.ParameterizedType listOfString = (java.lang.reflect.ParameterizedType)
            new TypeBindingsTestHelper().getListOfStringType();
        TypeVariable<?> tv = new TestTypeVariable("L");
        TypeBindings bindings = TypeBindings.create(tv, listOfString);
        assertEquals(listOfString, bindings.getBoundType(0));
    }

    // Helper class to obtain a parameterized type
    static class TypeBindingsTestHelper {
        public List<String> dummyField;
        public java.lang.reflect.Type getListOfStringType() throws Exception {
            java.lang.reflect.Field field = TypeBindingsTestHelper.class.getField("dummyField");
            return field.getGenericType();
        }
    }

    // --- Stress test: many bindings ---

    @Test
    public void testManyBindings() {
        int count = 100;
        TypeVariable<?>[] tvars = new TypeVariable<?>[count];
        Type[] bounds = new Type[count];
        for (int i = 0; i < count; i++) {
            tvars[i] = new TestTypeVariable("V" + i);
            bounds[i] = Integer.class;
        }
        TypeBindings many = TypeBindings.create(tvars, bounds);
        assertEquals(count, many.size());
        for (int i = 0; i < count; i++) {
            assertEquals(Integer.class, many.getBoundType(i));
        }
        assertFalse(many.hasUnbound());
    }

    // --- Test that findType returns null for non-existent name even with many ---

    @Test
    public void testFindTypeNonExistentInMany() {
        int count = 10;
        TypeVariable<?>[] tvars = new TypeVariable<?>[count];
        Type[] bounds = new Type[count];
        for (int i = 0; i < count; i++) {
            tvars[i] = new TestTypeVariable("K" + i);
            bounds[i] = String.class;
        }
        TypeBindings many = TypeBindings.create(tvars, bounds);
        assertNull(many.findType("Z"));
    }

    // --- Test that hasUnbound returns false when all bounds are resolved ---

    @Test
    public void testHasUnboundAllResolved() {
        TypeVariable<?> tv1 = new TestTypeVariable("A");
        TypeVariable<?> tv2 = new TestTypeVariable("B");
        TypeBindings bindings = TypeBindings.create(
            new TypeVariable<?>[]{tv1, tv2},
            new Type[]{Boolean.class, Character.class}
        );
        assertFalse(bindings.hasUnbound());
    }

    // --- Test that hasUnbound returns true if any bound is a TypeVariable ---

    @Test
    public void testHasUnboundOneUnresolved() {
        TypeVariable<?> tv1 = new TestTypeVariable("A");
        TypeVariable<?> tv2 = new TestTypeVariable("B");
        TypeBindings bindings = TypeBindings.create(
            new TypeVariable<?>[]{tv1, tv2},
            new Type[]{String.class, tv2} // tv2 bound to itself
        );
        assertTrue(bindings.hasUnbound());
    }

    // --- Test that getBoundType returns null for negative index ---

    @Test
    public void testGetBoundTypeNegativeIndex() {
        assertNull(singleBindings.getBoundType(-5));
    }

    // --- Test that getBoundType returns null for index >= size ---

    @Test
    public void testGetBoundTypeOutOfBounds() {
        assertNull(singleBindings.getBoundType(1));
        assertNull(multiBindings.getBoundType(2));
    }

    // --- Test that findType is case-sensitive ---

    @Test
    public void testFindTypeCaseSensitive() {
        TypeVariable<?> tv = new TestTypeVariable("t");
        TypeBindings bindings = TypeBindings.create(tv, Long.class);
        assertNull(bindings.findType("T")); // uppercase T not found
        assertEquals(Long.class, bindings.findType("t"));
    }

    // --- Test that toString handles special characters in names ---

    @Test
    public void testToStringWithSpecialNames() {
        TypeVariable<?> tv = new TestTypeVariable("T$");
        TypeBindings bindings = TypeBindings.create(tv, Float.class);
        String s = bindings.toString();
        assertTrue(s.contains("T$"));
        assertTrue(s.contains("Float"));
    }

    // --- Test that equals handles different order of bindings? (if order matters) ---
    // Assuming order matters (as per typical implementation)

    @Test
    public void testEqualsDifferentOrder() {
        TypeVariable<?> tvA = new TestTypeVariable("A");
        TypeVariable<?> tvB = new TestTypeVariable("B");
        TypeBindings order1 = TypeBindings.create(
            new TypeVariable<?>[]{tvA, tvB},
            new Type[]{Integer.class, String.class}
        );
        TypeBindings order2 = TypeBindings.create(
            new TypeVariable<?>[]{tvB, tvA},
            new Type[]{String.class, Integer.class}
        );
        // They are not equal because order differs
        assertNotEquals(order1, order2);
    }

    // --- Test that hashCode is consistent with equals ---

    @Test
    public void testHashCodeConsistentWithEquals() {
        TypeVariable<?> tv1 = new TestTypeVariable("X");
        TypeBindings b1 = TypeBindings.create(tv1, Long.class);
        TypeVariable<?> tv2 = new TestTypeVariable("X");
        TypeBindings b2 = TypeBindings.create(tv2, Long.class);
        assertEquals(b1, b2);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    // --- Test that emptyBindings is a singleton ---

    @Test
    public void testEmptyBindingsSingleton() {
        TypeBindings anotherEmpty = TypeBindings.emptyBindings();
        assertSame(emptyBindings, anotherEmpty);
    }

    // --- Test that create with single var returns a new instance ---

    @Test
    public void testCreateSingleReturnsNewInstance() {
        TypeVariable<?> tv = new TestTypeVariable("T");
        TypeBindings b1 = TypeBindings.create(tv, String.class);
        TypeBindings b2 = TypeBindings.create(tv, String.class);
        assertNotSame(b1, b2); // should be different objects
    }

    // --- Test that create with array returns a new instance ---

    @Test
    public void testCreateArrayReturnsNewInstance() {
        TypeVariable<?> tv = new TestTypeVariable("T");
        TypeBindings b1 = TypeBindings.create(new TypeVariable<?>[]{tv}, new Type[]{String.class});
        TypeBindings b2 = TypeBindings.create(new TypeVariable<?>[]{tv}, new Type[]{String.class});
        assertNotSame(b1, b2);
    }
}