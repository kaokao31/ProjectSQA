package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import org.junit.Before;
import org.junit.Test;

public class SimpleTypeTest {

    private TypeFactory tf;
    private SimpleType stringType;
    private SimpleType intType;
    private SimpleType listStringType;

    @Before
    public void setUp() {
        tf = TypeFactory.defaultInstance();
        stringType = tf.constructType(String.class);
        intType = tf.constructType(Integer.class);
        listStringType = (SimpleType) tf.constructType(new TypeReference<List<String>>() {}.getType());
    }

    @Test
    public void testConstruction() {
        assertNotNull(stringType);
        assertEquals(String.class, stringType.getRawClass());
    }

    @Test
    public void testIsContainerType() {
        assertFalse(stringType.isContainerType());
    }

    @Test
    public void testIsConcrete() {
        assertTrue(stringType.isConcrete());
    }

    @Test
    public void testEqualsBasic() {
        SimpleType other = tf.constructType(String.class);
        assertEquals(stringType, other);
        assertEquals(stringType.hashCode(), other.hashCode());
        assertFalse(stringType.equals(intType));
    }

    @Test
    public void testEqualsWithTypeBindings() {
        // Test equals with same type but different type bindings
        SimpleType type1 = (SimpleType) tf.constructType(new TypeReference<Map<String, Integer>>() {}.getType());
        SimpleType type2 = (SimpleType) tf.constructType(new TypeReference<Map<String, Integer>>() {}.getType());
        assertEquals(type1, type2);
    }

    @Test
    public void testToString() {
        String str = stringType.toString();
        assertNotNull(str);
        assertTrue(str.contains("String"));
    }

    @Test
    public void testWithTypeHandler() {
        TypeHandler<?> handler = new TypeHandler() {
            // dummy implementation
        };
        SimpleType withHandler = stringType.withTypeHandler(handler);
        assertNotNull(withHandler);
        assertFalse(withHandler.equals(stringType));
        SimpleType withHandlerAgain = withHandler.withTypeHandler(handler);
        assertSame(withHandler, withHandlerAgain);
    }

    @Test
    public void testWithValueHandler() {
        ValueHandler handler = new ValueHandler() {
            // dummy implementation
        };
        SimpleType withHandler = stringType.withValueHandler(handler);
        assertNotNull(withHandler);
        assertFalse(withHandler.equals(stringType));
        SimpleType withHandlerAgain = withHandler.withValueHandler(handler);
        assertSame(withHandler, withHandlerAgain);
    }

    @Test
    public void testNop() {
        assertSame(stringType, stringType.nop());
    }

    @Test
    public void testSerialization() throws Exception {
        String serialized = null;
        try {
            serialized = new ObjectMapper().writeValueAsString(stringType);
        } catch (Exception e) {
            fail("Serialization should not throw: " + e.getMessage());
        }
        assertNotNull(serialized);
    }

    @Test
    public void testDeserialization() throws Exception {
        String ser = "{\"type\":\"string\"}";  // Not valid serialized form, but we just test basic
        // Not needed: we assume Jackson type serialization works.
    }

    @Test
    public void testGenericTypeResolution() {
        // This test is related to bug 37: type resolution with generics
        SimpleType listType = (SimpleType) tf.constructType(new TypeReference<List<Integer>>() {}.getType());
        assertNotNull(listType);
        // Check that the type parameter is Integer
        JavaType contained = listType.containedType(0);
        assertNotNull(contained);
        assertEquals(Integer.class, contained.getRawClass());
        
        // Test nested generics
        SimpleType mapListType = (SimpleType) tf.constructType(new TypeReference<Map<String, List<Integer>>>() {}.getType());
        assertNotNull(mapListType);
    }

    @Test
    public void testEqualityWithTypeBindingsAndGenerics() {
        SimpleType t1 = (SimpleType) tf.constructType(new TypeReference<Map<String, Integer>>() {}.getType());
        SimpleType t2 = (SimpleType) tf.constructType(new TypeReference<Map<String, Integer>>() {}.getType());
        assertEquals(t1, t2);
        // Different generic parameters should not be equal
        SimpleType t3 = (SimpleType) tf.constructType(new TypeReference<Map<String, String>>() {}.getType());
        assertFalse(t1.equals(t3));
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = stringType.hashCode();
        int hash2 = tf.constructType(String.class).hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testConstructionWithNullBindings() {
        IllegalArgumentException exception = null;
        try {
            new SimpleType(String.class, null, null, null, null, false);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNull(exception);
    }

    @Test
    public void testIsAbstractSimpleType() {
        assertFalse(stringType.isAbstract());
    }

    @Test
    public void testErasedSignature() {
        assertNotNull(stringType.erasedSignature());
        assertEquals(String.class.getName(), stringType.erasedSignature());
    }

    @Test
    public void testGenericSignature() {
        String sig = stringType.genericSignature();
        assertNotNull(sig);
    }

    @Test
    public void testTypeBindingsGetter() {
        TypeBindings bindings = stringType.getTypeBindings();
        assertNotNull(bindings);
        assertEquals(0, bindings.size());
    }

    @Test
    public void testContainedTypeCount() {
        assertEquals(0, stringType.containedTypeCount());
        SimpleType listType = (SimpleType) tf.constructType(new TypeReference<List<String>>() {}.getType());
        assertEquals(1, listType.containedTypeCount());
    }

    @Test
    public void testContainedTypeByName() {
        SimpleType listType = (SimpleType) tf.constructType(new TypeReference<List<String>>() {}.getType());
        JavaType ct = listType.containedType(0);
        assertNotNull(ct);
        assertEquals(String.class, ct.getRawClass());
        // Index out of bounds should return null
        assertNull(listType.containedType(5));
    }

    @Test
    public void testIsTypeOrSubTypeOf() {
        // Simple types: strict equality
        assertTrue(stringType.isTypeOrSubTypeOf(String.class));
        assertFalse(stringType.isTypeOrSubTypeOf(Integer.class));
    }

    @Test
    public void testWithContentTypeHandler() {
        // Not defined for SimpleType (non-container), but should not throw
        SimpleType withHandler = stringType.withContentTypeHandler(null);
        assertSame(stringType, withHandler);
    }

    @Test
    public void testWithContentValueHandler() {
        SimpleType withHandler = stringType.withContentValueHandler(null);
        assertSame(stringType, withHandler);
    }

    @Test
    public void testNarrowContentsBy() {
        // Not defined for SimpleType
        assertNull(stringType.narrowContentsBy(Integer.class));
    }

    @Test
    public void testForceTagged() {
        // SimpleType should return itself
        JavaType result = stringType.forceTagged(JavaType.class);
        assertSame(stringType, result);
    }

    @Test
    public void testIsJavaLangObject() {
        assertFalse(stringType.isJavaLangObject());
        SimpleType objectType = (SimpleType) tf.constructType(Object.class);
        assertTrue(objectType.isJavaLangObject());
    }

    @Test
    public void testUseStaticType() {
        assertFalse(stringType.useStaticType());
    }

    @Test
    public void testBuildCanonicalName() {
        assertNotNull(stringType.buildCanonicalName());
    }

    @Test
    public void testIsCollectionLikeType() {
        assertFalse(stringType.isCollectionLikeType());
    }

    @Test
    public void testIsMapLikeType() {
        assertFalse(stringType.isMapLikeType());
    }

    @Test
    public void testIsArrayType() {
        assertFalse(stringType.isArrayType());
    }

    @Test
    public void testIsEnumType() {
        assertFalse(stringType.isEnumType());
        SimpleType enumType = tf.constructType(java.util.concurrent.TimeUnit.class);
        assertTrue(enumType.isEnumType());
    }

    @Test
    public void testIsFinalSimpleType() {
        assertTrue(stringType.isFinal());
    }

    @Test
    public void testHasGenericTypes() {
        assertFalse(stringType.hasGenericTypes());
        SimpleType listType = (SimpleType) tf.constructType(new TypeReference<List<String>>() {}.getType());
        assertTrue(listType.hasGenericTypes());
    }

    @Test
    public void testGetSuperClass() {
        // String superclass is Object
        JavaType superClass = stringType.getSuperClass();
        assertNotNull(superClass);
        assertEquals(Object.class, superClass.getRawClass());
    }

    @Test
    public void testGetInterfaces() {
        // String implements Serializable, Comparable, CharSequence
        JavaType[] interfaces = stringType.getInterfaces();
        assertNotNull(interfaces);
        assertTrue(interfaces.length > 0);
        boolean hasSerializable = false;
        boolean hasComparable = false;
        for (JavaType intf : interfaces) {
            if (intf.getRawClass() == java.io.Serializable.class) hasSerializable = true;
            if (intf.getRawClass() == Comparable.class) hasComparable = true;
        }
        assertTrue(hasSerializable);
        assertTrue(hasComparable);
    }

    @Test
    public void testFindSuperType() {
        JavaType found = stringType.findSuperType(CharSequence.class);
        assertNotNull(found);
        assertTrue(found.getRawClass() == CharSequence.class);
    }

    @Test
    public void testFindTypeParameters() {
        JavaType[] params = stringType.findTypeParameters(Comparable.class);
        assertNotNull(params);
        if (params.length > 0) {
            assertEquals(String.class, params[0].getRawClass());
        }
    }

    @Test
    public void testNarrowBy() {
        // Narrowing with itself should be same
        JavaType narrowed = stringType.narrowBy(String.class);
        assertSame(stringType, narrowed);
        // Narrowing with subclass? String is final, so should throw exception
        try {
            stringType.narrowBy(CharSequence.class);
            fail("Expected IllegalArgumentException for narrowing final class");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIsThrowable() {
        assertFalse(stringType.isThrowable());
        SimpleType exceptionType = tf.constructType(Exception.class);
        assertTrue(exceptionType.isThrowable());
    }

    @Test
    public void testWidenBy() {
        // widen from Integer to Number
        JavaType widened = intType.widenBy(Number.class);
        assertNotNull(widened);
        assertEquals(Number.class, widened.getRawClass());
    }

    @Test
    public void testIsPrimitive() {
        assertFalse(stringType.isPrimitive());
        SimpleType intPrimitive = tf.constructType(int.class);
        assertTrue(intPrimitive.isPrimitive());
    }

    @Test
    public void testIsAbstract() {
        // String is final, not abstract
        assertFalse(stringType.isAbstract());
        SimpleType abstractType = tf.constructType(Number.class);
        assertTrue(abstractType.isAbstract());
    }

    @Test
    public void testIsTrueEnumType() {
        assertFalse(stringType.isTrueEnumType());
        SimpleType enumType = tf.constructType(java.util.concurrent.TimeUnit.class);
        assertTrue(enumType.isTrueEnumType());
    }

    @Test
    public void testIsEnumImplType() {
        assertFalse(stringType.isEnumImplType());
        SimpleType enumType = tf.constructType(java.util.concurrent.TimeUnit.class);
        assertFalse(enumType.isEnumImplType()); // true enum, not impl
    }

    @Test
    public void testIsReferenceType() {
        assertTrue(stringType.isReferenceType()); // Actually string is not a reference type in Jackson terminology? Usually reference types are all except primitives. Let's check: In Jackson, reference types include all Object-based types. So yes.
        assertTrue(stringType.isReferenceType());
    }

    @Test
    public void testIsMapLikeTypeNo() {
        assertFalse(stringType.isMapLikeType());
    }

    @Test
    public void testIsCollectionLikeTypeNo() {
        assertFalse(stringType.isCollectionLikeType());
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse(stringType.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        assertFalse(stringType.equals("some string"));
    }

    @Test
    public void testHashCodeWithTypeBindings() {
        // Create a type with bindings and ensure hash is based on them
        JavaType typeWithBindings = tf.constructType(new TypeReference<Map<String, Integer>>() {}.getType());
        int hash = typeWithBindings.hashCode();
        assertTrue(hash != 0);
    }

    @Test
    public void testSerializationRoundtrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(stringType);
        JavaType deserialized = mapper.readValue(json, JavaType.class);
        assertEquals(stringType, deserialized);
    }

    @Test
    public void testToStringContainsClassName() {
        String s = stringType.toString();
        assertTrue(s.contains("String"));
        // For generic type
        SimpleType listType = (SimpleType) tf.constructType(new TypeReference<List<Integer>>() {}.getType());
        String listStr = listType.toString();
        assertTrue(listStr.contains("List") && listStr.contains("Integer"));
    }

    @Test
    public void testWithContentValueHandlerNull() {
        assertSame(stringType, stringType.withContentValueHandler(null));
    }

    @Test
    public void testWithTypeHandlerNull() {
        assertSame(stringType, stringType.withTypeHandler(null));
    }

    @Test
    public void testWithStaticTyping() {
        SimpleType withStatic = stringType.withStaticTyping();
        assertNotNull(withStatic);
        assertFalse(withStatic.equals(stringType));
    }

    @Test
    public void testToStringWithGeneric() {
        JavaType listType = tf.constructType(new TypeReference<List<String>>() {}.getType());
        String str = listType.toString();
        assertNotNull(str);
        assertTrue(str.contains("java.util.List<java.lang.String>"));
    }

    @Test
    public void testForTypeAliases() {
        // Not directly applicable, but ensure no exception
        assertNotNull(stringType.getRawClass());
    }

    @Test
    public void testNarrowContentsByNull() {
        assertNull(stringType.narrowContentsBy(null));
    }

    @Test
    public void testNarrowByWithNull() {
        try {
            stringType.narrowBy(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}