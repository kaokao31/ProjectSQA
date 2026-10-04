package org.apache.commons.lang3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TypeCheckTest {

    @Test
    public void testIsInstanceWithNullType() {
        assertFalse(TypeCheck.isInstance(null, new Object()));
    }

    @Test
    public void testIsInstanceWithNullObject() {
        assertFalse(TypeCheck.isInstance(String.class, null));
    }

    @Test
    public void testIsInstanceWithMatchingType() {
        assertTrue(TypeCheck.isInstance(String.class, "Hello"));
    }

    @Test
    public void testIsInstanceWithNonMatchingType() {
        assertFalse(TypeCheck.isInstance(String.class, 123));
    }

    @Test
    public void testIsInstanceWithSubclass() {
        assertTrue(TypeCheck.isInstance(Object.class, "Hello"));
    }

    @Test
    public void testIsInstanceWithInterface() {
        assertTrue(TypeCheck.isInstance(CharSequence.class, "Hello"));
    }

    @Test
    public void testIsInstanceWithPrimitiveType() {
        assertTrue(TypeCheck.isInstance(int.class, 123));
    }

    @Test
    public void testIsInstanceWithPrimitiveWrapperBoxedValue() {
        assertTrue(TypeCheck.isInstance(Integer.class, 123));
    }

    @Test
    public void testIsInstanceWithNullTypeAndNullObject() {
        assertFalse(TypeCheck.isInstance(null, null));
    }

    @Test
    public void testIsInstanceWithArrayType() {
        assertTrue(TypeCheck.isInstance(Object[].class, new Object[0]));
    }

    @Test
    public void testIsInstanceWithPrimitiveArray() {
        assertTrue(TypeCheck.isInstance(int[].class, new int[0]));
    }

    @Test
    public void testIsInstanceWithNullWithUnboxing() {
        assertFalse(TypeCheck.isInstance(Integer.class, null));
    }

    @Test
    public void testIsInstanceWithBoxedAndPrimitiveCrossCheck() {
        assertFalse(TypeCheck.isInstance(int.class, (Object) null));
    }

    @Test
    public void testIsInstanceWithGenericType() {
        assertTrue(TypeCheck.isInstance(java.util.List.class, new java.util.ArrayList<>()));
    }

    @Test
    public void testIsAssignableWithNullType() {
        assertFalse(TypeCheck.isAssignable(null, String.class));
    }

    @Test
    public void testIsAssignableWithNullTargetType() {
        assertFalse(TypeCheck.isAssignable(String.class, null));
    }

    @Test
    public void testIsAssignableWithBothNull() {
        assertFalse(TypeCheck.isAssignable(null, null));
    }

    @Test
    public void testIsAssignableWithSameType() {
        assertTrue(TypeCheck.isAssignable(String.class, String.class));
    }

    @Test
    public void testIsAssignableWithSuperclass() {
        assertTrue(TypeCheck.isAssignable(String.class, Object.class));
    }

    @Test
    public void testIsAssignableWithSubclass() {
        assertFalse(TypeCheck.isAssignable(Object.class, String.class));
    }

    @Test
    public void testIsAssignableWithInterface() {
        assertTrue(TypeCheck.isAssignable(String.class, CharSequence.class));
    }

    @Test
    public void testIsAssignableWithPrimitiveToWrapper() {
        assertTrue(TypeCheck.isAssignable(int.class, Integer.class));
    }

    @Test
    public void testIsAssignableWithWrapperToPrimitive() {
        assertTrue(TypeCheck.isAssignable(Integer.class, int.class));
    }

    @Test
    public void testIsAssignableWithDifferentPrimitives() {
        assertFalse(TypeCheck.isAssignable(int.class, long.class));
    }

    @Test
    public void testIsAssignableWithPrimitiveToSuperWrapper() {
        assertFalse(TypeCheck.isAssignable(int.class, Number.class));
    }

    @Test
    public void testIsAssignableWithDoublePrecision() {
        assertFalse(TypeCheck.isAssignable(float.class, double.class));
    }

    @Test
    public void testIsAssignableWithVoidType() {
        assertTrue(TypeCheck.isAssignable(void.class, Void.class));
    }

    @Test
    public void testIsAssignableWithAutoboxedEquivalence() {
        assertTrue(TypeCheck.isAssignable(double.class, Double.class));
    }

    @Test
    public void testIsAssignableWithWideningPrimitiveConversionCheck() {
        assertTrue(TypeCheck.isAssignable(Integer.class, int.class));
    }

    @Test
    public void testIsAssignableWithArrayTypes() {
        // Array types are not directly assignable to non-array types
        assertFalse(TypeCheck.isAssignable(String[].class, Object.class));
        assertTrue(TypeCheck.isAssignable(String[].class, Object[].class));
        assertFalse(TypeCheck.isAssignable(int[].class, Object[].class));
        assertTrue(TypeCheck.isAssignable(int[].class, int[].class));
    }

    @Test
    public void testIsAssignableWithUnrelatedTypes() {
        assertFalse(TypeCheck.isAssignable(String.class, Integer.class));
    }

    @Test
    public void testIsAssignableWithNullValues() {
        assertFalse(TypeCheck.isAssignable(null, Integer.class));
        assertFalse(TypeCheck.isAssignable(Integer.class, null));
    }

    @Test(expected = NullPointerException.class)
    public void testIsAssignableNullParameterException() {
        // Intentional to check null safety
        TypeCheck.isAssignable(null, null);
    }
}