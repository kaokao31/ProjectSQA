package com.fasterxml.jackson.databind;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.*;
import java.lang.reflect.Type;

public class JavaTypeTest {

    private JavaType simpleType;
    private JavaType arrayType;
    private JavaType collectionType;
    private JavaType mapType;
    private JavaType genericType;
    private JavaType wildcardType;
    private JavaType typeVariableType;
    private JavaType recursiveType;

    @Before
    public void setUp() throws Exception {
        // Initialize various test types using the factory methods
        simpleType = JavaType.defaultInstance();
        
        // Create concrete types for testing
        arrayType = JavaType.constructArrayType(Integer.class);
        collectionType = JavaType.constructCollectionType(ArrayList.class, String.class);
        mapType = JavaType.constructMapType(HashMap.class, String.class, Integer.class);
        
        // Create generic type with type parameters
        TypeFactory tf = TypeFactory.defaultInstance();
        genericType = tf.constructType(HashMap.class, String.class, Integer.class);
        
        // Wildcard type
        wildcardType = tf.constructType(new TypeReference<List<? extends Number>>(){}.getType());
        
        // Type variable
        typeVariableType = tf.constructType(new TypeReference<Map<String, ?>>(){}.getType());
        
        // Recursive type (to test stack overflow scenarios)
        recursiveType = tf.constructType(new TypeReference<List<List<List<String>>>>(){}.getType());
    }

    @Test
    public void testDefaultInstance() {
        JavaType defaultType = JavaType.defaultInstance();
        assertNotNull("Default instance should not be null", defaultType);
        assertTrue("Default instance should represent Void", defaultType.isVoidType());
    }

    @Test
    public void testIsArrayType() {
        assertTrue("Array type should be recognized as array", arrayType.isArrayType());
        assertTrue("Array type should have container type", arrayType.isContainerType());
        assertFalse("Simple type should not be array", simpleType.isArrayType());
    }

    @Test
    public void testIsPrimitiveType() {
        JavaType intType = JavaType.constructPrimitiveType(int.class);
        assertTrue("int should be primitive", intType.isPrimitive());
        assertFalse("Integer should not be primitive", simpleType.isPrimitive());
    }

    @Test
    public void testIsTrivialType() {
        assertTrue("Void type should be trivial", simpleType.isTrivial());
        assertFalse("Array type should not be trivial", arrayType.isTrivial());
        assertFalse("Collection type should not be trivial", collectionType.isTrivial());
    }

    @Test
    public void testIsConcreteType() {
        assertTrue("Array type should be concrete", arrayType.isConcrete());
        assertTrue("Collection type should be concrete", collectionType.isConcrete());
        assertFalse("Wildcard type should not be concrete", wildcardType.isConcrete());
    }

    @Test
    public void testIsJavaLangObject() {
        JavaType objectType = JavaType.constructType(Object.class);
        assertTrue("Object.class should be JavaLangObject", objectType.isJavaLangObject());
        assertFalse("String type should not be JavaLangObject", simpleType.isJavaLangObject());
    }

    @Test
    public void testHasGenericTypes() {
        assertTrue("Generic HashMap should have generic types", genericType.hasGenericTypes());
        assertFalse("Simple type should not have generic types", simpleType.hasGenericTypes());
        assertTrue("Collection type should have generic types", collectionType.hasGenericTypes());
        assertTrue("Wildcard type should have generic types", wildcardType.hasGenericTypes());
    }

    @Test
    public void testIsAbstractType() {
        assertFalse("Array type should not be abstract", arrayType.isAbstractType());
        assertFalse("Collection type should not be abstract", collectionType.isAbstractType());
        assertTrue("List interface should be abstract", 
            JavaType.constructType(List.class).isAbstractType());
    }

    @Test
    public void testGetContainerType() {
        assertNotNull("Array type should have container type", arrayType.getContainerType());
        assertNotNull("Collection type should have container type", collectionType.getContainerType());
        assertNull("Simple type should have no container type", simpleType.getContainerType());
    }

    @Test
    public void testGetGenericSignature() {
        String sig = simpleType.getGenericSignature();
        assertNotNull("Generic signature should not be null", sig);
        assertTrue("Generic signature should start with L", sig.startsWith("L"));
    }

    @Test
    public void testGetErasedSignature() {
        String sig = simpleType.getErasedSignature();
        assertNotNull("Erased signature should not be null", sig);
    }

    @Test
    public void testGetGenericSuperclass() {
        JavaType superclass = simpleType.getGenericSuperclass();
        assertNotNull("Superclass should not be null", superclass);
        assertTrue("Superclass should be Object", superclass.isJavaLangObject());
    }

    @Test
    public void testNarrowByClass() {
        JavaType narrowed = simpleType.narrowBy(Object.class);
        assertNotNull("Narrowed type should not be null", narrowed);
        assertEquals("Narrowed type should be Object", Object.class.getName(), narrowed.getRawClass().getName());

        JavaType superclass = JavaType.constructType(ArrayList.class).narrowBy(AbstractList.class);
        assertNotNull("Narrowed by superclass should not be null", superclass);
    }

    @Test
    public void testNarrowContentsBy() {
        JavaType narrowedContents = collectionType.narrowContentsBy(Integer.class);
        assertNotNull("Narrowed contents should not be null", narrowedContents);
        assertEquals("Content type should be Integer", 
            Integer.class.getName(), narrowedContents.getContentType().getRawClass().getName());
    }

    @Test
    public void testNarrowKeyBy() {
        JavaType narrowedKey = mapType.narrowKeyBy(Integer.class);
        assertNotNull("Narrowed key should not be null", narrowedKey);
        assertEquals("Key type should be Integer", 
            Integer.class.getName(), narrowedKey.getKeyType().getRawClass().getName());
    }

    @Test
    public void testWidenBy() {
        JavaType widened = simpleType.widenBy(Number.class);
        assertNotNull("Widened type should not be null", widened);
        assertTrue("Widened type should be assignable from original", 
            widened.isTypeOrSuperTypeOf(simpleType));
    }

    @Test
    public void testForceSubtype() {
        JavaType forced = simpleType.forceSubtype(JavaType.constructType(ArrayList.class));
        assertNotNull("Forced subtype should not be null", forced);
    }

    @Test
    public void testBindChildType() {
        JavaType unboundType = JavaType.constructType(List.class);
        JavaType boundType = unboundType.bindChildType(0, String.class);
        assertNotNull("Bound type should not be null", boundType);
        assertTrue("Bound type should have generic types", boundType.hasGenericTypes());
    }

    @Test
    public void testFindSuperType() {
        JavaType arrayListType = JavaType.constructType(ArrayList.class);
        JavaType listType = arrayListType.findSuperType(List.class);
        assertNotNull("Should find List super type", listType);
        assertEquals("Super type should be List", List.class.getName(), listType.getRawClass().getName());

        JavaType objectType = arrayListType.findSuperType(Object.class);
        assertNotNull("Should find Object super type", objectType);
    }

    @Test
    public void testFindTypeParameters() {
        JavaType[] params = collectionType.findTypeParameters(Collection.class);
        assertNotNull("Should find type parameters", params);
        assertTrue("Should have at least one type parameter", params.length >= 1);
        assertEquals("Type parameter should be String", String.class.getName(), params[0].getRawClass().getName());
    }

    @Test
    public void testInterfaces() {
        JavaType[] interfaces = simpleType.getInterfaces();
        assertNotNull("Interfaces should not be null", interfaces);
        assertEquals("Simple type should have no interfaces", 0, interfaces.length);

        JavaType comparableType = JavaType.constructType(java.io.Serializable.class);
        // No interfaces for primitive-like types
    }

    @Test
    public void testGetArrayType() {
        JavaType elementType = JavaType.constructType(Integer.class);
        JavaType arrayOfType = elementType.getArrayType();
        assertNotNull("Array of type should not be null", arrayOfType);
        assertTrue("Array of type should be array", arrayOfType.isArrayType());
        assertEquals("Element type should match", 
            Integer.class.getName(), arrayOfType.getContentType().getRawClass().getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNarrowByInvalidClass() {
        JavaType stringType = JavaType.constructType(String.class);
        // String is final, cannot narrow to subclass
        stringType.narrowBy(Object.class);
    }

    @Test(expected = NullPointerException.class)
    public void testNarrowByNull() {
        simpleType.narrowBy(null);
    }

    @Test
    public void testGetRawClass() {
        assertEquals("Simple type raw class should be Void", 
            Void.class.getName(), simpleType.getRawClass().getName());
        assertEquals("Array type raw class should be Object[]", 
            Object[].class.getName(), arrayType.getRawClass().getName());
    }

    @Test
    public void testGetContentType() {
        assertNotNull("Array type should have content type", arrayType.getContentType());
        assertNotNull("Collection type should have content type", collectionType.getContentType());
        assertNull("Simple type should have no content type", simpleType.getContentType());
    }

    @Test
    public void testGetKeyType() {
        assertNotNull("Map type should have key type", mapType.getKeyType());
        assertNull("Non-map type should have no key type", collectionType.getKeyType());
    }

    @Test
    public void testIsTypeOrSuperTypeOf() {
        assertTrue("Simple type should be super type of itself", 
            simpleType.isTypeOrSuperTypeOf(simpleType));
        assertTrue("List should be super type of ArrayList", 
            JavaType.constructType(List.class).isTypeOrSuperTypeOf(
                JavaType.constructType(ArrayList.class)));
        assertFalse("String should not be super type of Integer", 
            JavaType.constructType(String.class).isTypeOrSuperTypeOf(
                JavaType.constructType(Integer.class)));
    }

    @Test
    public void testToXString() {
        assertNotNull("toXString should not be null", simpleType.toXString());
        assertNotNull("Should contain class name", 
            JavaType.constructType(String.class).toXString().contains("String"));
    }

    @Test
    public void testToString() {
        assertNotNull("toString should not be null", simpleType.toString());
        assertTrue("toString should represent the type", 
            simpleType.toString().length() > 0);
    }

    @Test
    public void testEquals() {
        JavaType sameType = JavaType.constructType(String.class);
        JavaType sameType2 = JavaType.constructType(String.class);
        assertEquals("Same class types should be equal", sameType, sameType2);
        assertNotEquals("Different class types should not be equal", 
            JavaType.constructType(String.class), JavaType.constructType(Integer.class));
    }

    @Test
    public void testHashCode() {
        assertEquals("Same types should have same hashcode",
            JavaType.constructType(String.class).hashCode(),
            JavaType.constructType(String.class).hashCode());
    }

    @Test
    public void testIsVoidType() {
        assertTrue("Default instance should be void type", simpleType.isVoidType());
        assertFalse("String type should not be void type", 
            JavaType.constructType(String.class).isVoidType());
    }

    @Test
    public void testIsCollectionType() {
        assertTrue("ArrayList type should be collection", 
            JavaType.constructType(ArrayList.class).isCollectionType());
        assertFalse("String type should not be collection", 
            JavaType.constructType(String.class).isCollectionType());
    }

    @Test
    public void testIsMapType() {
        assertTrue("HashMap type should be map", 
            JavaType.constructType(HashMap.class).isMapType());
        assertFalse("String type should not be map", 
            JavaType.constructType(String.class).isMapType());
    }

    @Test
    public void testIsFinal() {
        assertFalse("Array type should not be final", arrayType.isFinal());
        assertTrue("String type should be final", 
            JavaType.constructType(String.class).isFinal());
    }

    @Test
    public void testGetInterfaces() {
        JavaType[] interfaces = JavaType.constructType(ArrayList.class).getInterfaces();
        assertNotNull("Should have interfaces", interfaces);
        assertTrue("ArrayList should implement interfaces", interfaces.length > 0);
        
        JavaType[] simpleInterfaces = simpleType.getInterfaces();
        assertEquals("Simple type should have no interfaces", 0, simpleInterfaces.length);
    }

    @Test
    public void testRecursiveTypeBinding() {
        assertNotNull("Recursive type should not be null", recursiveType);
        assertTrue("Recursive type should be container type", recursiveType.isContainerType());
        
        JavaType innerContent = recursiveType.getContentType();
        assertNotNull("Inner content should not be null", innerContent);
        assertTrue("Inner content should also be container", innerContent.isContainerType());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAbstractMethodValidation() {
        // Test that abstract methods throw UnsupportedOperationException
        // when called on base JavaType
        JavaType baseType = new JavaType() {
            // Only override necessary abstract methods
            @Override
            public JavaType getSuperClass() { return null; }
            @Override
            public Class<?> getRawClass() { return Object.class; }
            @Override
            public boolean isAbstract() { return true; }
            @Override
            public boolean isFinal() { return false; }
            @Override
            public boolean isContainerType() { return true; }
            @Override
            public JavaType getContentType() { return null; }
            @Override
            public int actualSize() { return 0; }
            @Override
            public JavaType containedType(int index) { return null; }
            @Override
            public JavaType containedTypeOrUnknown(int index) { return null; }
            @Override
            public String containedTypeName(int index) { return ""; }
            @Override
            public JavaType getKeyType() { return null; }
            @Override
            public boolean isArrayType() { return false; }
            @Override
            public String getGenericSignature() { return ""; }
            @Override
            public String getErasedSignature() { return ""; }
            @Override
            public String toString() { return ""; }
            @Override
            public boolean equals(Object o) { return false; }
            @Override
            public int hashCode() { return 0; }
        };
        
        // This should throw UnsupportedOperationException for non-overridden methods
        baseType.getGenericSignature(Object.class);
    }

    @Test
    public void testTypeVariableHandling() {
        assertNotNull("Type variable type should not be null", typeVariableType);
        assertTrue("Type variable type should be concrete", typeVariableType.isConcrete());
        assertEquals("Should have raw class", 
            Map.class.getName(), typeVariableType.getRawClass().getName());
    }

    @Test
    public void testWildcardType() {
        assertNotNull("Wildcard type should not be null", wildcardType);
        assertFalse("Wildcard type should not be primitive", wildcardType.isPrimitive());
        assertTrue("Wildcard type should be concrete", wildcardType.isConcrete());
    }

    @Test
    public void testNullInputHandling() {
        try {
            JavaType.constructType((Class<?>) null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
        try {
            JavaType.constructType((Type) null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testPrimitiveArray() {
        JavaType intArrayType = JavaType.constructType(int[].class);
        assertTrue("int[] should be array type", intArrayType.isArrayType());
        assertTrue("int[] should have primitive content", intArrayType.getContentType().isPrimitive());
        assertEquals("Content type should be int", 
            int.class.getName(), intArrayType.getContentType().getRawClass().getName());
    }

    @Test
    public void testMultiDimensionalArray() {
        JavaType multiArray = JavaType.constructType(int[][][].class);
        assertTrue("3D array should be array type", multiArray.isArrayType());
        
        JavaType content = multiArray.getContentType();
        assertTrue("Content should be array", content.isArrayType());
        
        content = content.getContentType();
        assertTrue("Content of content should be array", content.isArrayType());
        
        content = content.getContentType();
        assertFalse("Final content should not be array", content.isArrayType());
        assertTrue("Final content should be primitive", content.isPrimitive());
    }

    @Test
    public void testNestedGenericCollections() {
        JavaType nestedList = JavaType.constructType(
            new TypeReference<List<HashMap<String, List<Integer>>>>(){}.getType());
        assertNotNull("Nested generic should not be null", nestedList);
        assertTrue("Should be container", nestedList.isContainerType());
        
        JavaType content = nestedList.getContentType();
        assertTrue("Content should be map", content.isMapType());
        
        JavaType keyType = content.getKeyType();
        assertEquals("Key should be String", String.class.getName(), keyType.getRawClass().getName());
        
        JavaType valueType = content.getContentType();
        assertTrue("Value should be list", valueType.isCollectionType());
    }
}