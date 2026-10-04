package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JavaTypeTest {

    // Concrete subclass of JavaType for testing abstract class behavior
    private static class DummyJavaType extends JavaType {
        private static final long serialVersionUID = 1L;

        public DummyJavaType(Class<?> raw) {
            super(raw, 0, null, null, false);
        }

        public DummyJavaType(Class<?> raw, int hash, Object valueHandler, Object typeHandler, boolean asStatic) {
            super(raw, hash, valueHandler, typeHandler, asStatic);
        }

        @Override
        public JavaType upgradeTo(Class<?> subclass) {
            return this;
        }

        @Override
        public JavaType withContentType(JavaType contentType) {
            return this;
        }

        @Override
        public JavaType withTyping(Object typeHandler) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object valueHandler) {
            return this;
        }

        @Override
        public JavaType withContentValueHandler(Object valueHandler) {
            return this;
        }

        @Override
        public JavaType narrowBy(Class<?> subclass) {
            return this;
        }

        @Override
        public JavaType forcedNarrowBy(Class<?> subclass) {
            return this;
        }

        @Override
        public JavaType widenBy(Class<?> subclass) {
            return this;
        }

        @Override
        public String getGenericSignature() {
            return "Ldummy;";
        }

        @Override
        public String getErasedSignature() {
            return "Ldummy;";
        }

        @Override
        public boolean isContainerType() {
            return false;
        }
    }

    @Test
    public void testConstructorsAndGetters() {
        Object vHandler = new Object();
        Object tHandler = new Object();
        int hash = 123;
        
        DummyJavaType type = new DummyJavaType(String.class, hash, vHandler, tHandler, true);

        assertEquals(String.class, type.getRawClass());
        assertEquals(hash, type.hashCode());
        assertSame(vHandler, type.getValueHandler());
        assertSame(tHandler, type.getTypeHandler());
        assertTrue(type.useStaticType());
    }

    @Test
    public void testDefaultHandlers() {
        DummyJavaType type = new DummyJavaType(Integer.class);
        assertNull(type.getValueHandler());
        assertNull(type.getTypeHandler());
        assertNull(type.getContentValueHandler());
        assertFalse(type.useStaticType());
    }

    @Test
    public void testContainerTypeMethods() {
        DummyJavaType type = new DummyJavaType(List.class);

        assertFalse(type.isContainerType());
        assertNull(type.getKeyType());
        assertNull(type.getContentType());
        assertEquals(0, type.containedTypeCount());
        assertNull(type.containedType(0));
        assertNull(type.containedTypeName(0));
        assertNull(type.findSuperType(String.class));
        assertNull(type.forcedNarrowBy(String.class));
        assertNull(type.widenBy(String.class));
    }

    @Test
    public void testCollectionAndMapInterfaceChecks() {
        DummyJavaType type = new DummyJavaType(Object.class);

        assertFalse(type.isCollectionLikeType());
        assertFalse(type.isMapLikeType());
        assertFalse(type.isEnumType());
        assertFalse(type.isInterface());
        assertFalse(type.isPrimitive());
        assertFalse(type.isFinal());
        assertFalse(type.hasAbstractTypes());
        assertFalse(type.hasGenericTypes());
        assertFalse(type.isThrowable());
        assertFalse(type.isArrayType());
    }

    @Test
    public void testAsPrimitive() {
        DummyJavaType type = new DummyJavaType(int.class);
        assertTrue(type.isPrimitive());
    }

    @Test
    public void testAsInterface() {
        DummyJavaType type = new DummyJavaType(Runnable.class);
        assertTrue(type.isInterface());
    }

    @Test
    public void testAsCollectionLikeViaTypeFactory() {
        JavaType listType = TypeFactory.defaultInstance().constructType(List.class);
        assertTrue(listType.isContainerType());
        assertTrue(listType.isCollectionLikeType());
        assertFalse(listType.isMapLikeType());
    }

    @Test
    public void testAsMapLikeViaTypeFactory() {
        JavaType mapType = TypeFactory.defaultInstance().constructType(Map.class);
        assertTrue(mapType.isContainerType());
        assertTrue(mapType.isMapLikeType());
        assertFalse(mapType.isCollectionLikeType());
        assertNotNull(mapType.getKeyType());
        assertNotNull(mapType.getContentType());
    }

    @Test
    public void testToStringAndEquals() {
        JavaType type1 = new DummyJavaType(String.class);
        JavaType type2 = new DummyJavaType(String.class);
        JavaType type3 = new DummyJavaType(Integer.class);

        assertEquals(type1, type2);
        assertNotEquals(type1, type3);
        assertNotEquals(type1, null);
        assertNotEquals(type1, "some string");

        assertNotNull(type1.toString());
    }
}