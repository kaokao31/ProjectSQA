package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SimpleTypeTest {

    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
    }

    // Basic construction and identity
    @Test
    public void testConstructSimpleType() {
        JavaType stringType = typeFactory.constructType(String.class);
        assertNotNull(stringType);
        assertTrue(stringType.isConcrete());
        assertFalse(stringType.isContainerType());
        assertEquals(String.class, stringType.getRawClass());
    }

    // Equality with itself
    @Test
    public void testEqualsSelf() {
        JavaType type = typeFactory.constructType(Integer.class);
        assertTrue(type.equals(type));
    }

    // Equality with null
    @Test
    public void testEqualsNull() {
        JavaType type = typeFactory.constructType(Double.class);
        assertFalse(type.equals(null));
    }

    // Equality with different type
    @Test
    public void testEqualsDifferentType() {
        JavaType type1 = typeFactory.constructType(String.class);
        JavaType type2 = typeFactory.constructType(Integer.class);
        assertFalse(type1.equals(type2));
    }

    // Equality with same type but different type parameters (if applicable)
    @Test
    public void testEqualsWithTypeParameters() {
        // Create a parameterized type: List<String>
        JavaType listString = typeFactory.constructParametricType(List.class, String.class);
        // Create another List<String>
        JavaType listString2 = typeFactory.constructParametricType(List.class, String.class);
        assertEquals(listString, listString2);

        // Create List<Integer>
        JavaType listInteger = typeFactory.constructParametricType(List.class, Integer.class);
        assertFalse(listString.equals(listInteger));
    }

    // HashCode consistency with equals
    @Test
    public void testHashCodeConsistency() {
        JavaType type1 = typeFactory.constructType(Boolean.class);
        JavaType type2 = typeFactory.constructType(Boolean.class);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    // HashCode for parameterized types
    @Test
    public void testHashCodeParameterized() {
        JavaType type1 = typeFactory.constructParametricType(Map.class, String.class, Integer.class);
        JavaType type2 = typeFactory.constructParametricType(Map.class, String.class, Integer.class);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    // Test isContainerType returns false for SimpleType
    @Test
    public void testIsContainerType() {
        JavaType type = typeFactory.constructType(Long.class);
        assertFalse(type.isContainerType());
    }

    // Test isAbstract for concrete class
    @Test
    public void testIsAbstract() {
        JavaType type = typeFactory.constructType(Float.class);
        assertFalse(type.isAbstract());
    }

    // Test isConcrete for concrete class
    @Test
    public void testIsConcrete() {
        JavaType type = typeFactory.constructType(Character.class);
        assertTrue(type.isConcrete());
    }

    // Test toString
    @Test
    public void testToString() {
        JavaType type = typeFactory.constructType(Short.class);
        String str = type.toString();
        assertNotNull(str);
        assertTrue(str.contains("Short"));
    }

    // Test equals with null type parameters (simulating bug scenario)
    // This test attempts to trigger the NullPointerException in equals when type parameters are null.
    // We create a SimpleType via reflection to set null type parameters.
    @Test
    public void testEqualsWithNullTypeParameters() throws Exception {
        // Obtain a SimpleType instance for String (no type parameters)
        SimpleType baseType = (SimpleType) typeFactory.constructType(String.class);
        // Create a copy with null type parameters using reflection
        SimpleType nullParamType = createSimpleTypeWithNullTypeParams(baseType);
        // The equals method should handle null type parameters gracefully
        assertFalse(baseType.equals(nullParamType));
        assertFalse(nullParamType.equals(baseType));
        // Also test hashCode (should not throw)
        nullParamType.hashCode(); // should not throw NPE
    }

    // Helper method to create a SimpleType with null type parameters via reflection
    private SimpleType createSimpleTypeWithNullTypeParams(SimpleType original) throws Exception {
        java.lang.reflect.Field typeParamsField = SimpleType.class.getDeclaredField("_typeParameters");
        typeParamsField.setAccessible(true);
        // Create a new SimpleType instance using the same raw class and type handler
        // We can use the protected constructor or clone via reflection
        java.lang.reflect.Constructor<SimpleType> constructor = SimpleType.class.getDeclaredConstructor(
                Class.class, com.fasterxml.jackson.databind.type.TypeBindings.class,
                com.fasterxml.jackson.databind.JavaType.class, JavaType[].class,
                Object.class, Object.class, boolean.class);
        constructor.setAccessible(true);
        // Get the bindings from original
        com.fasterxml.jackson.databind.type.TypeBindings bindings = original.getBindings();
        // Create a new instance with null type parameters
        SimpleType newType = constructor.newInstance(
                original.getRawClass(),
                bindings,
                original.getSuperClass(),
                null, // type parameters set to null
                original.getValueHandler(),
                original.getTypeHandler(),
                original.isFinal()
        );
        return newType;
    }

    // Test equals with empty type parameters array
    @Test
    public void testEqualsWithEmptyTypeParameters() throws Exception {
        SimpleType baseType = (SimpleType) typeFactory.constructType(String.class);
        SimpleType emptyParamType = createSimpleTypeWithEmptyTypeParams(baseType);
        // Both have no type parameters, should be equal
        assertEquals(baseType, emptyParamType);
        assertEquals(baseType.hashCode(), emptyParamType.hashCode());
    }

    private SimpleType createSimpleTypeWithEmptyTypeParams(SimpleType original) throws Exception {
        java.lang.reflect.Constructor<SimpleType> constructor = SimpleType.class.getDeclaredConstructor(
                Class.class, com.fasterxml.jackson.databind.type.TypeBindings.class,
                com.fasterxml.jackson.databind.JavaType.class, JavaType[].class,
                Object.class, Object.class, boolean.class);
        constructor.setAccessible(true);
        com.fasterxml.jackson.databind.type.TypeBindings bindings = original.getBindings();
        return constructor.newInstance(
                original.getRawClass(),
                bindings,
                original.getSuperClass(),
                new JavaType[0], // empty array
                original.getValueHandler(),
                original.getTypeHandler(),
                original.isFinal()
        );
    }

    // Test equals when one type has null type parameters and the other has empty array
    @Test
    public void testEqualsNullVsEmptyTypeParams() throws Exception {
        SimpleType baseType = (SimpleType) typeFactory.constructType(String.class);
        SimpleType nullParamType = createSimpleTypeWithNullTypeParams(baseType);
        SimpleType emptyParamType = createSimpleTypeWithEmptyTypeParams(baseType);
        // They should not be equal because null and empty array are different
        assertFalse(nullParamType.equals(emptyParamType));
        assertFalse(emptyParamType.equals(nullParamType));
    }

    // Test serialization/deserialization if applicable (SimpleType is serializable)
    @Test
    public void testSerialization() throws Exception {
        SimpleType original = (SimpleType) typeFactory.constructType(Boolean.class);
        // Serialize to byte array
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        // Deserialize
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        SimpleType deserialized = (SimpleType) ois.readObject();
        ois.close();
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
    }

    // Test equals with different raw class but same type parameters
    @Test
    public void testEqualsDifferentRawClass() {
        JavaType type1 = typeFactory.constructType(Number.class);
        JavaType type2 = typeFactory.constructType(Integer.class);
        assertFalse(type1.equals(type2));
    }

    // Test equals with same raw class but different type parameters (parameterized)
    @Test
    public void testEqualsDifferentTypeParams() {
        JavaType listString = typeFactory.constructParametricType(List.class, String.class);
        JavaType listObject = typeFactory.constructParametricType(List.class, Object.class);
        assertFalse(listString.equals(listObject));
    }

    // Test equals with same raw class and same type parameters (parameterized)
    @Test
    public void testEqualsSameTypeParams() {
        JavaType map1 = typeFactory.constructParametricType(Map.class, String.class, Integer.class);
        JavaType map2 = typeFactory.constructParametricType(Map.class, String.class, Integer.class);
        assertEquals(map1, map2);
    }

    // Test hashCode for simple types
    @Test
    public void testHashCodeSimple() {
        JavaType type1 = typeFactory.constructType(Byte.class);
        JavaType type2 = typeFactory.constructType(Byte.class);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    // Test hashCode for different simple types
    @Test
    public void testHashCodeDifferentSimple() {
        JavaType type1 = typeFactory.constructType(Byte.class);
        JavaType type2 = typeFactory.constructType(Short.class);
        assertNotEquals(type1.hashCode(), type2.hashCode());
    }
}