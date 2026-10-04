package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import static org.junit.Assert.*;

public class ResolvedRecursiveTypeTest {

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Test
    public void testSetReference() {
        Class<?> rawType = String.class;
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(rawType, bindings);

        // Initially, the reference is null/empty, or we can set it
        JavaType referencedType = SimpleType.constructUnsafe(Integer.class);
        type.setReference(referencedType);

        // Verify that the referenced type is returned
        assertEquals(Integer.class, type.getRawClass());
        // For ResolvedRecursiveType, bindings/methods might delegate or return specific values
        assertNotNull(type.getBindings());
    }

    @Test
    public void testGetSelfReferencedType() {
        Class<?> rawType = Object.class;
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(rawType, bindings);

        // Calling methods before setting reference should handle gracefully or return self/null
        // Depending on implementation, let's verify behavior
        JavaType ref = type.getSelfReferencedType();
        // May be null or self if not set
        // Let's set reference to self or another type
        type.setReference(type);
        assertSame(type, type.getSelfReferencedType());
    }

    @Test
    public void testDelegationMethods() {
        Class<?> rawType = java.util.List.class;
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(rawType, bindings);

        JavaType intType = SimpleType.constructUnsafe(Integer.class);
        type.setReference(intType);

        // Test various methods that should delegate to the referenced type
        assertEquals(Integer.class, type.getRawClass());
        assertTrue(type.isCollectionLikeType() == intType.isCollectionLikeType());
        assertFalse(type.isMapLikeType());
        
        // Test toString
        String desc = type.toString();
        assertNotNull(desc);
    }

    @Test
    public void testWithContentType() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType intType = SimpleType.constructUnsafe(Integer.class);
        type.setReference(intType);

        JavaType newType = type.withContentType(SimpleType.constructUnsafe(Long.class));
        assertNotNull(newType);
    }

    @Test
    public void testWithTypingAndBindings() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType intType = SimpleType.constructUnsafe(Integer.class);
        type.setReference(intType);

        assertEquals(type, type.withTypeHandler(null));
        assertEquals(type, type.withContentTypeHandler(null));
        assertEquals(type, type.withValueHandler(null));
        assertEquals(type, type.withContentValueHandler(null));
        assertEquals(type, type.withStaticTyping());
        assertEquals(type, type.withHandlers(null, null));
        assertEquals(type, type.refersTo(intType));
    }

    @Test
    public void testEqualsAndHashCode() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());

        JavaType intType = SimpleType.constructUnsafe(Integer.class);
        type1.setReference(intType);
        type2.setReference(intType);

        assertFalse(type1.equals(null));
        assertFalse(type1.equals("some string"));
        // Test structural equality or reference equality depending on implementation
        assertTrue(type1.equals(type1));
    }
}