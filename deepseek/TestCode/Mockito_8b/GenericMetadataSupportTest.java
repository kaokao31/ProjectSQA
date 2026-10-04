package org.springframework.core;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GenericMetadataSupportTest {

    private GenericMetadataSupport support;

    // Helper class with generic parameters
    private static class GenericClass<T, U extends Number> {
        private T field1;
        private U field2;
        private List<String> field3;
        private Map<?, ?> field4;
    }

    // Helper class without generics
    private static class SimpleClass {
        private String name;
        private int value;
    }

    @Before
    public void setUp() {
        // Default setup for a class with generics
        support = new GenericMetadataSupport(GenericClass.class);
    }

    // Test constructor with null source class
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullSource() {
        new GenericMetadataSupport(null);
    }

    // Test constructor with non-generic class
    @Test
    public void testConstructorWithSimpleClass() {
        GenericMetadataSupport simpleSupport = new GenericMetadataSupport(SimpleClass.class);
        assertNotNull(simpleSupport);
        assertEquals(SimpleClass.class, simpleSupport.getSource());
        assertEquals(0, simpleSupport.getGenericTypeCount());
        assertFalse(simpleSupport.hasGenericType());
    }

    // Test getSource returns the correct class
    @Test
    public void testGetSource() {
        assertEquals(GenericClass.class, support.getSource());
    }

    // Test getType returns the raw type
    @Test
    public void testGetType() {
        assertEquals(GenericClass.class, support.getType());
    }

    // Test getGenericTypeCount for a class with two type parameters
    @Test
    public void testGetGenericTypeCount() {
        assertEquals(2, support.getGenericTypeCount());
    }

    // Test hasGenericType returns true for generic class
    @Test
    public void testHasGenericType() {
        assertTrue(support.hasGenericType());
    }

    // Test getGenericType with valid index returns correct Type
    @Test
    public void testGetGenericTypeValidIndex() {
        Type type0 = support.getGenericType(0);
        assertNotNull(type0);
        // The first type parameter is T (unbounded)
        assertTrue(type0 instanceof java.lang.reflect.TypeVariable);
        assertEquals("T", ((java.lang.reflect.TypeVariable) type0).getName());

        Type type1 = support.getGenericType(1);
        assertNotNull(type1);
        assertTrue(type1 instanceof java.lang.reflect.TypeVariable);
        assertEquals("U", ((java.lang.reflect.TypeVariable) type1).getName());
    }

    // Test getGenericType with index out of bounds (negative)
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetGenericTypeNegativeIndex() {
        support.getGenericType(-1);
    }

    // Test getGenericType with index out of bounds (too large)
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetGenericTypeIndexTooLarge() {
        support.getGenericType(5);
    }

    // Test getGenericType for non-generic class returns null
    @Test
    public void testGetGenericTypeForNonGenericClass() {
        GenericMetadataSupport simpleSupport = new GenericMetadataSupport(SimpleClass.class);
        assertNull(simpleSupport.getGenericType(0));
    }

    // Test resolveType with a concrete type (e.g., String)
    @Test
    public void testResolveTypeConcrete() {
        Type stringType = String.class;
        Class<?> resolved = support.resolveType(stringType);
        assertEquals(String.class, resolved);
    }

    // Test resolveType with a type variable (should resolve to Object if unbounded)
    @Test
    public void testResolveTypeVariableUnbounded() {
        Type typeVar = support.getGenericType(0); // T
        Class<?> resolved = support.resolveType(typeVar);
        // Unbounded type variable resolves to Object
        assertEquals(Object.class, resolved);
    }

    // Test resolveType with a bounded type variable (U extends Number)
    @Test
    public void testResolveTypeVariableBounded() {
        Type typeVar = support.getGenericType(1); // U extends Number
        Class<?> resolved = support.resolveType(typeVar);
        // Should resolve to the bound: Number
        assertEquals(Number.class, resolved);
    }

    // Test resolveType with parameterized type (List<String>)
    @Test
    public void testResolveTypeParameterized() {
        // We need to get the type of field3 which is List<String>
        // This requires reflection on the class fields, but we assume support has a method to get field types
        // For simplicity, we create a parameterized type manually
        // In real scenario, support might have getFieldGenericType method
        // We'll just test with a known parameterized type from the class
        // Actually, we can use the field's generic type via reflection
        try {
            java.lang.reflect.Field field = GenericClass.class.getDeclaredField("field3");
            Type fieldType = field.getGenericType();
            Class<?> resolved = support.resolveType(fieldType);
            // List<String> resolves to List.class (raw type)
            assertEquals(List.class, resolved);
        } catch (NoSuchFieldException e) {
            fail("Field not found");
        }
    }

    // Test resolveType with wildcard type (Map<?, ?>)
    @Test
    public void testResolveTypeWildcard() {
        try {
            java.lang.reflect.Field field = GenericClass.class.getDeclaredField("field4");
            Type fieldType = field.getGenericType();
            Class<?> resolved = support.resolveType(fieldType);
            // Map<?, ?> resolves to Map.class
            assertEquals(Map.class, resolved);
        } catch (NoSuchFieldException e) {
            fail("Field not found");
        }
    }

    // Test resolveType with null
    @Test(expected = IllegalArgumentException.class)
    public void testResolveTypeNull() {
        support.resolveType(null);
    }

    // Test that getGenericType returns the same Type object for same index (caching)
    @Test
    public void testGetGenericTypeCaching() {
        Type first = support.getGenericType(0);
        Type second = support.getGenericType(0);
        assertSame("Should return the same Type instance", first, second);
    }

    // Edge case: class with multiple bounds (e.g., T extends Comparable & Serializable)
    // We'll create a separate inner class for this
    private static class MultiBoundClass<T extends Comparable<T> & java.io.Serializable> {
    }

    @Test
    public void testResolveTypeWithMultipleBounds() {
        GenericMetadataSupport multiSupport = new GenericMetadataSupport(MultiBoundClass.class);
        Type typeVar = multiSupport.getGenericType(0);
        Class<?> resolved = multiSupport.resolveType(typeVar);
        // Should resolve to the first bound: Comparable
        assertEquals(Comparable.class, resolved);
    }

    // Test for a class that extends a generic superclass (e.g., List<String> as superclass)
    private static class SubClass extends GenericClass<String, Integer> {
    }

    @Test
    public void testSubclassResolvedTypes() {
        GenericMetadataSupport subSupport = new GenericMetadataSupport(SubClass.class);
        // The type parameters should be resolved to String and Integer
        Type type0 = subSupport.getGenericType(0);
        // Since SubClass extends GenericClass<String, Integer>, the type variable T is bound to String
        // But getGenericType returns the type variable from the class definition, not the resolved value
        // Actually, in Spring's GenericMetadataSupport, getGenericType returns the actual type argument from the superclass
        // We'll assume it returns the resolved type
        // For this test, we expect String.class
        assertEquals(String.class, type0);
        Type type1 = subSupport.getGenericType(1);
        assertEquals(Integer.class, type1);
    }

    // Test for a class with no type parameters but with generic superclass
    private static class SimpleSub extends GenericClass<String, Integer> {
    }

    @Test
    public void testSimpleSubclass() {
        GenericMetadataSupport simpleSubSupport = new GenericMetadataSupport(SimpleSub.class);
        // Should have 2 generic types resolved from superclass
        assertEquals(2, simpleSubSupport.getGenericTypeCount());
        assertTrue(simpleSubSupport.hasGenericType());
        assertEquals(String.class, simpleSubSupport.getGenericType(0));
        assertEquals(Integer.class, simpleSubSupport.getGenericType(1));
    }

    // Test for a class that is itself generic and extends another generic class
    private static class MiddleClass<A, B> extends GenericClass<A, B> {
    }

    @Test
    public void testMiddleClass() {
        GenericMetadataSupport middleSupport = new GenericMetadataSupport(MiddleClass.class);
        // Should have 2 type parameters from MiddleClass itself
        assertEquals(2, middleSupport.getGenericTypeCount());
        // The type variables should be A and B
        Type type0 = middleSupport.getGenericType(0);
        assertTrue(type0 instanceof java.lang.reflect.TypeVariable);
        assertEquals("A", ((java.lang.reflect.TypeVariable) type0).getName());
        Type type1 = middleSupport.getGenericType(1);
        assertTrue(type1 instanceof java.lang.reflect.TypeVariable);
        assertEquals("B", ((java.lang.reflect.TypeVariable) type1).getName());
    }

    // Test for a class with a generic method (not directly supported by class-level metadata)
    // But we can test that the class handles it gracefully
    private static class MethodGenericClass {
        public <T> T getValue(T input) { return input; }
    }

    @Test
    public void testClassWithGenericMethod() {
        GenericMetadataSupport methodSupport = new GenericMetadataSupport(MethodGenericClass.class);
        // Class has no type parameters
        assertEquals(0, methodSupport.getGenericTypeCount());
        assertFalse(methodSupport.hasGenericType());
    }
}