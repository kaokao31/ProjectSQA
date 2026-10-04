package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Modifier;

public class JavaTypeTest {

    // Concrete subclass of JavaType to allow instantiation and testing of abstract methods.
    private static class DummyJavaType extends JavaType {
        private static final long serialVersionUID = 1L;

        protected DummyJavaType(Class<?> raw) {
            super(raw, 0, null, null, false);
        }

        protected DummyJavaType(Class<?> raw, int hash, Object valueHandler, Object typeHandler, boolean asStatic) {
            super(raw, hash, valueHandler, typeHandler, asStatic);
        }

        @Override
        public JavaType withContentType(JavaType contentType) {
            return this;
        }

        @Override
        public JavaType withTyping(TypeHandlerInstantiator instantiator) {
            return this;
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withStaticTyping() {
            return this;
        }

        @Override
        public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
            return this;
        }

        @Override
        protected String buildCanonicalName() {
            return _class.getName();
        }

        @Override
        public boolean isContainerType() {
            return false;
        }
    }

    @Test
    public void testAbstractMethodsAndDefaults() {
        DummyJavaType type = new DummyJavaType(String.class);

        assertFalse(type.isAbstract());
        assertFalse(type.isConcrete());
        assertFalse(type.isThrowable());
        assertFalse(type.isArrayType());
        assertFalse(type.isCollectionLikeType());
        assertFalse(type.isMapLikeType());
        assertFalse(type.hasValueHandler());
        assertFalse(type.hasHandlers());
        assertFalse(type.isEnumType());
        assertFalse(type.isInterface());
        assertFalse(type.hasGenericTypes());

        assertNull(type.getKeyType());
        assertNull(type.getContentType());
        assertEquals(0, type.containedTypeCount());
        assertNull(type.containedType(0));
        assertNull(type.containedTypeByName("nonexistent"));
        assertNull(type.getBindings());

        // Test unsupported or default behaviors
        try {
            type.getErasedSignature();
        } catch (UnsupportedOperationException e) {
            // expected or tested
        }

        try {
            type.getGenericSignature();
        } catch (UnsupportedOperationException e) {
            // expected or tested
        }
    }

    @Test
    public void testModifiersAndFlags() {
        // Test interface raw type
        DummyJavaType interfaceType = new DummyJavaType(Runnable.class);
        assertTrue(interfaceType.isInterface());
        assertTrue(interfaceType.isAbstract());
        assertFalse(interfaceType.isConcrete());

        // Test primitive raw type
        DummyJavaType primType = new DummyJavaType(int.class);
        assertTrue(primType.isPrimitive());
        assertFalse(primType.isContainerType());

        // Test final class
        DummyJavaType finalType = new DummyJavaType(String.class);
        assertTrue(finalType.isFinal());
        assertTrue(finalType.isConcrete());
    }

    @Test
    public void testHandlersAndBuilders() {
        Object valueHandler = new Object();
        Object typeHandler = new Object();

        DummyJavaType type = new DummyJavaType(String.class, 123, valueHandler, typeHandler, true);

        assertEquals(123, type.hashCode());
        assertTrue(type.hasValueHandler());
        assertSame(valueHandler, type.getValueHandler());
        assertSame(typeHandler, type.getTypeHandler());
        assertTrue(type.useStaticType());
        assertTrue(type.hasHandlers());

        JavaType withVal = type.withValueHandler(valueHandler);
        assertNotNull(withVal);

        JavaType withType = type.withTypeHandler(typeHandler);
        assertNotNull(withType);

        JavaType bypassed = type.withHandlers(valueHandler, typeHandler);
        assertNotNull(bypassed);

        JavaType forcedStatic = type.withStaticTyping();
        assertNotNull(forcedStatic);
    }

    @Test
    public void testFulfillsMethods() {
        DummyJavaType type = new DummyJavaType(String.class);
        assertFalse(type.isJavaLangObject());

        DummyJavaType objType = new DummyJavaType(Object.class);
        assertTrue(objType.isJavaLangObject());
    }

    @Test
    public void testToStringAndEquals() {
        DummyJavaType type1 = new DummyJavaType(String.class);
        DummyJavaType type2 = new DummyJavaType(String.class);
        DummyJavaType type3 = new DummyJavaType(Integer.class);

        assertEquals(type1, type2);
        assertNotEquals(type1, type3);
        assertNotEquals(type1, null);
        assertNotEquals(type1, "some string");

        assertNotNull(type1.toString());
    }
}