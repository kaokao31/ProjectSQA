package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ResolvedRecursiveTypeTest {

    @Test
    public void testResolvedRecursiveTypeWorkflow() {
        // Create a base type to act as the self-reference target
        Class<?> rawType = Runnable.class;
        TypeBindings bindings = TypeBindings.emptyBindings();
        JavaType superClass = TypeFactory.unknownType();
        JavaType superInterface = TypeFactory.unknownType();

        // Initialize ResolvedRecursiveType with null/empty-ish referenced type initially
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(rawType, bindings);

        // Verify initial state
        assertNull(recursiveType.getReferencedType());
        assertEquals(rawType, recursiveType.getRawClass());
        assertTrue(recursiveType.isContainerType() == false);

        // Set the referenced type (this is usually where the recursive link is resolved)
        JavaType targetType = TypeFactory.defaultInstance().constructType(Integer.class);
        recursiveType.setReference(targetType);

        // Verify the referenced type is now set
        assertEquals(targetType, recursiveType.getReferencedType());

        // Test method delegations to the referenced type
        // getContentType(), getKeyType(), etc., should delegate to the referenced type if it is set.
        // Since Integer is not a container, getContentType should return null or delegate appropriately.
        assertNull(recursiveType.getContentType());
        assertNull(recursiveType.getKeyType());

        // Test with a container referenced type to verify delegation
        ResolvedRecursiveType containerRecursive = new ResolvedRecursiveType(Iterable.class, bindings);
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(java.util.List.class, String.class);
        containerRecursive.setReference(listType);

        assertEquals(listType, containerRecursive.getReferencedType());
        assertNotNull(containerRecursive.getContentType());
        assertEquals(TypeFactory.defaultInstance().constructType(String.class), containerRecursive.getContentType());

        // Test building methods / narrowing / widening / withTypeHandler etc.
        JavaType narrowed = containerRecursive.withContentType(TypeFactory.defaultInstance().constructType(Integer.class));
        assertNotNull(narrowed);

        JavaType withHandlers = containerRecursive.withTypeHandler("someHandler");
        assertNotNull(withHandlers);

        JavaType withContentTypeHandler = containerRecursive.withContentTypeHandler("contentHandler");
        assertNotNull(withContentTypeHandler);

        JavaType withStaticTyping = containerRecursive.withStaticTyping();
        assertNotNull(withStaticTyping);

        // Test equals and hashCode and toString
        assertFalse(containerRecursive.equals(null));
        assertTrue(containerRecursive.equals(containerRecursive));
        assertNotNull(containerRecursive.hashCode());
        assertNotNull(containerRecursive.toString());

        // Test getErasedSignature and getGenericSignature
        assertNotNull(containerRecursive.getErasedSignature());
        assertNotNull(containerRecursive.getGenericSignature());
    }

    @Test
    public void testUnresolvedRecursiveTypeBehaviors() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());

        // Operations before setReference is called (should handle null referencedType safely or throw specific exceptions)
        assertNull(recursiveType.getReferencedType());

        // Check helper methods when reference is null
        try {
            recursiveType.getErasedSignature();
        } catch (Exception e) {
            // Depending on implementation, it might throw NullPointerException if reference is not set
        }

        try {
            recursiveType.getGenericSignature();
        } catch (Exception e) {
            // Expected if uninitialized
        }

        // Now set reference and re-verify signatures
        recursiveType.setReference(TypeFactory.defaultInstance().constructType(Object.class));
        assertNotNull(recursiveType.getErasedSignature());
        assertNotNull(recursiveType.getGenericSignature());
    }
}