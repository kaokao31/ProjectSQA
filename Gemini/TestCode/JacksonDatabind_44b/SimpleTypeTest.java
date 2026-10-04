package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class SimpleTypeTest {

    @Test
    public void testConstructUnsafe() {
        Class<?> raw = String.class;
        SimpleType type = SimpleType.constructUnsafe(raw);
        assertNotNull(type);
        assertEquals(raw, type.getRawClass());
        assertFalse(type.isContainerType());
    }

    @Test
    public void testBuildCanonicalName() {
        SimpleType type = SimpleType.constructUnsafe(Integer.class);
        String canonical = type.buildCanonicalName();
        assertNotNull(canonical);
        assertTrue(canonical.contains("Integer"));
    }

    @Test
    public void testWithContentType() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        JavaType newContentType = SimpleType.constructUnsafe(Integer.class);
        JavaType modified = type.withContentType(newContentType);
        assertNotNull(modified);
        // SimpleType overrides withContentType and throws or handles accordingly
    }

    @Test
    public void testWithTypeHandler() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object h = new Object();
        JavaType modified = type.withTypeHandler(h);
        assertNotNull(modified);
        assertSame(h, modified.getTypeHandler());
    }

    @Test
    public void testWithContentTypeHandler() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object h = new Object();
        JavaType modified = type.withContentTypeHandler(h);
        assertNotNull(modified);
        assertSame(h, modified.getContentTypeHandler());
    }

    @Test
    public void testWithHandlers() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object h1 = new Object();
        Object h2 = new Object();
        JavaType modified = type.withHandlers(h1, h2);
        assertNotNull(modified);
        assertSame(h1, modified.getTypeHandler());
        assertSame(h2, modified.getValueHandler());
    }

    @Test
    public void testWithStaticTyping() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        JavaType modified = type.withStaticTyping();
        assertNotNull(modified);
        assertTrue(modified.useStaticType());
    }

    @Test
    public void testRefine() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Class<?> raw = String.class;
        TypeBindings bindings = TypeBindings.emptyBindings();
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInterfaces = new JavaType[0];

        JavaType refined = type.refine(raw, bindings, superClass, superInterfaces);
        assertNotNull(refined);
        assertEquals(raw, refined.getRawClass());
    }

    @Test
    public void testEqualsAndHashCode() {
        SimpleType type1 = SimpleType.constructUnsafe(String.class);
        SimpleType type2 = SimpleType.constructUnsafe(String.class);
        SimpleType type3 = SimpleType.constructUnsafe(Integer.class);

        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
        assertNotEquals(type1, type3);
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("some string"));
    }

    @Test
    public void testToString() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        String desc = type.toString();
        assertNotNull(desc);
        assertTrue(desc.length() > 0);
    }
}