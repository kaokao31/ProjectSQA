package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;

public class SimpleTypeTest {

    @Test
    public void testConstructUnparameterized() {
        SimpleType type = SimpleType.constructUnparameterized(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertFalse(type.isContainerType());
        assertFalse(type.isCollectionLikeType());
        assertFalse(type.isMapLikeType());
        assertFalse(type.hasGenericTypes());
        assertEquals(0, type.containedTypeCount());
        assertNull(type.containedType(0));
        assertNull(type.containedTypeName(0));
    }

    @Test
    public void testConstructWithClassAndParameters() {
        // Test constructing with type parameters (or simulating the bug fix area in Jackson 37)
        Class<?> raw = Map.class;
        JavaType keyType = SimpleType.constructUnparameterized(String.class);
        JavaType valueType = SimpleType.constructUnparameterized(Integer.class);
        
        TypeBindings bindings = TypeBindings.create(raw, new JavaType[] { keyType, valueType });
        SimpleType type = SimpleType.construct(raw, bindings, null, null);
        
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
        assertTrue(type.hasGenericTypes());
        assertEquals(2, type.containedTypeCount());
        assertEquals(keyType, type.containedType(0));
        assertEquals(valueType, type.containedType(1));
    }

    @Test
    public void testWithContentType() {
        SimpleType type = SimpleType.constructUnparameterized(String.class);
        JavaType newContentType = SimpleType.constructUnparameterized(Integer.class);
        JavaType modified = type.withContentType(newContentType);
        assertNotNull(modified);
        // SimpleType does not really have content type modifications in the same way collections do,
        // but it should return this or a new instance safely without crashing.
    }

    @Test
    public void testWithTypingAndRefining() {
        SimpleType type = SimpleType.constructUnparameterized(String.class);
        
        JavaType narrowed = type.withStaticTyping();
        assertTrue(narrowed.useStaticType());

        JavaType valueHandlerType = type.withValueHandler("valueHandler");
        assertEquals("valueHandler", valueHandlerType.getValueHandler());

        JavaType typeHandlerType = type.withTypeHandler("typeHandler");
        assertEquals("typeHandler", typeHandlerType.getTypeHandler());
    }

    @Test
    public void testBuildCanonicalName() {
        SimpleType type = SimpleType.constructUnparameterized(String.class);
        String canonical = type.toCanonical();
        assertNotNull(canonical);
        assertTrue(canonical.contains("String"));
    }

    @Test
    public void testGetErasedSignature() {
        SimpleType type = SimpleType.constructUnparameterized(String.class);
        StringBuilder sb = new StringBuilder();
        type.getErasedSignature(sb);
        assertTrue(sb.length() > 0);
    }

    @Test
    public void testGetGenericSignature() {
        SimpleType type = SimpleType.constructUnparameterized(String.class);
        StringBuilder sb = new StringBuilder();
        type.getGenericSignature(sb);
        assertTrue(sb.length() > 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        SimpleType type1 = SimpleType.constructUnparameterized(String.class);
        SimpleType type2 = SimpleType.constructUnparameterized(String.class);
        SimpleType type3 = SimpleType.constructUnparameterized(Integer.class);

        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
        assertNotEquals(type1, type3);
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("some string"));
    }

    @Test
    public void testToString() {
        SimpleType type = SimpleType.constructUnparameterized(String.class);
        String desc = type.toString();
        assertNotNull(desc);
        assertTrue(desc.contains("String"));
    }
}