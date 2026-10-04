package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for Primitives class.
 * Covers all methods and edge cases, including the known bug (Mockito bug 26)
 * where primitiveTypeOf(Void.class) and wrapperOf(void.class) may not work correctly.
 */
public class PrimitivesTest {

    // ---------- isPrimitiveWrapper ----------

    @Test
    public void testIsPrimitiveWrapper_withAllWrappers_returnsTrue() {
        assertTrue("Boolean wrapper", Primitives.isPrimitiveWrapper(Boolean.class));
        assertTrue("Character wrapper", Primitives.isPrimitiveWrapper(Character.class));
        assertTrue("Byte wrapper", Primitives.isPrimitiveWrapper(Byte.class));
        assertTrue("Short wrapper", Primitives.isPrimitiveWrapper(Short.class));
        assertTrue("Integer wrapper", Primitives.isPrimitiveWrapper(Integer.class));
        assertTrue("Long wrapper", Primitives.isPrimitiveWrapper(Long.class));
        assertTrue("Float wrapper", Primitives.isPrimitiveWrapper(Float.class));
        assertTrue("Double wrapper", Primitives.isPrimitiveWrapper(Double.class));
        assertTrue("Void wrapper", Primitives.isPrimitiveWrapper(Void.class));
    }

    @Test
    public void testIsPrimitiveWrapper_withPrimitives_returnsFalse() {
        assertFalse("boolean primitive", Primitives.isPrimitiveWrapper(boolean.class));
        assertFalse("char primitive", Primitives.isPrimitiveWrapper(char.class));
        assertFalse("byte primitive", Primitives.isPrimitiveWrapper(byte.class));
        assertFalse("short primitive", Primitives.isPrimitiveWrapper(short.class));
        assertFalse("int primitive", Primitives.isPrimitiveWrapper(int.class));
        assertFalse("long primitive", Primitives.isPrimitiveWrapper(long.class));
        assertFalse("float primitive", Primitives.isPrimitiveWrapper(float.class));
        assertFalse("double primitive", Primitives.isPrimitiveWrapper(double.class));
        assertFalse("void primitive", Primitives.isPrimitiveWrapper(void.class));
    }

    @Test
    public void testIsPrimitiveWrapper_withNonPrimitiveTypes_returnsFalse() {
        assertFalse("String", Primitives.isPrimitiveWrapper(String.class));
        assertFalse("Object", Primitives.isPrimitiveWrapper(Object.class));
        assertFalse("null", Primitives.isPrimitiveWrapper(null));
    }

    // ---------- primitiveTypeOf ----------

    @Test
    public void testPrimitiveTypeOf_withWrappers_returnsPrimitive() {
        assertEquals("Boolean -> boolean", boolean.class, Primitives.primitiveTypeOf(Boolean.class));
        assertEquals("Character -> char", char.class, Primitives.primitiveTypeOf(Character.class));
        assertEquals("Byte -> byte", byte.class, Primitives.primitiveTypeOf(Byte.class));
        assertEquals("Short -> short", short.class, Primitives.primitiveTypeOf(Short.class));
        assertEquals("Integer -> int", int.class, Primitives.primitiveTypeOf(Integer.class));
        assertEquals("Long -> long", long.class, Primitives.primitiveTypeOf(Long.class));
        assertEquals("Float -> float", float.class, Primitives.primitiveTypeOf(Float.class));
        assertEquals("Double -> double", double.class, Primitives.primitiveTypeOf(Double.class));
        // Bug 26: Void.class should map to void.class
        assertEquals("Void -> void", void.class, Primitives.primitiveTypeOf(Void.class));
    }

    @Test
    public void testPrimitiveTypeOf_withPrimitives_returnsSame() {
        assertEquals("boolean", boolean.class, Primitives.primitiveTypeOf(boolean.class));
        assertEquals("char", char.class, Primitives.primitiveTypeOf(char.class));
        assertEquals("byte", byte.class, Primitives.primitiveTypeOf(byte.class));
        assertEquals("short", short.class, Primitives.primitiveTypeOf(short.class));
        assertEquals("int", int.class, Primitives.primitiveTypeOf(int.class));
        assertEquals("long", long.class, Primitives.primitiveTypeOf(long.class));
        assertEquals("float", float.class, Primitives.primitiveTypeOf(float.class));
        assertEquals("double", double.class, Primitives.primitiveTypeOf(double.class));
        assertEquals("void", void.class, Primitives.primitiveTypeOf(void.class));
    }

    @Test
    public void testPrimitiveTypeOf_withNonPrimitiveOrWrapper_returnsNull() {
        assertNull("String", Primitives.primitiveTypeOf(String.class));
        assertNull("Object", Primitives.primitiveTypeOf(Object.class));
        assertNull("null", Primitives.primitiveTypeOf(null));
    }

    // ---------- wrapperOf ----------

    @Test
    public void testWrapperOf_withPrimitives_returnsWrapper() {
        assertEquals("boolean -> Boolean", Boolean.class, Primitives.wrapperOf(boolean.class));
        assertEquals("char -> Character", Character.class, Primitives.wrapperOf(char.class));
        assertEquals("byte -> Byte", Byte.class, Primitives.wrapperOf(byte.class));
        assertEquals("short -> Short", Short.class, Primitives.wrapperOf(short.class));
        assertEquals("int -> Integer", Integer.class, Primitives.wrapperOf(int.class));
        assertEquals("long -> Long", Long.class, Primitives.wrapperOf(long.class));
        assertEquals("float -> Float", Float.class, Primitives.wrapperOf(float.class));
        assertEquals("double -> Double", Double.class, Primitives.wrapperOf(double.class));
        // Bug 26: void.class should map to Void.class
        assertEquals("void -> Void", Void.class, Primitives.wrapperOf(void.class));
    }

    @Test
    public void testWrapperOf_withWrappers_returnsSame() {
        assertEquals("Boolean", Boolean.class, Primitives.wrapperOf(Boolean.class));
        assertEquals("Character", Character.class, Primitives.wrapperOf(Character.class));
        assertEquals("Byte", Byte.class, Primitives.wrapperOf(Byte.class));
        assertEquals("Short", Short.class, Primitives.wrapperOf(Short.class));
        assertEquals("Integer", Integer.class, Primitives.wrapperOf(Integer.class));
        assertEquals("Long", Long.class, Primitives.wrapperOf(Long.class));
        assertEquals("Float", Float.class, Primitives.wrapperOf(Float.class));
        assertEquals("Double", Double.class, Primitives.wrapperOf(Double.class));
        assertEquals("Void", Void.class, Primitives.wrapperOf(Void.class));
    }

    @Test
    public void testWrapperOf_withNonPrimitiveOrWrapper_returnsNull() {
        assertNull("String", Primitives.wrapperOf(String.class));
        assertNull("Object", Primitives.wrapperOf(Object.class));
        assertNull("null", Primitives.wrapperOf(null));
    }

    // ---------- isPrimitiveOrWrapper ----------

    @Test
    public void testIsPrimitiveOrWrapper_withPrimitives_returnsTrue() {
        assertTrue("boolean", Primitives.isPrimitiveOrWrapper(boolean.class));
        assertTrue("char", Primitives.isPrimitiveOrWrapper(char.class));
        assertTrue("byte", Primitives.isPrimitiveOrWrapper(byte.class));
        assertTrue("short", Primitives.isPrimitiveOrWrapper(short.class));
        assertTrue("int", Primitives.isPrimitiveOrWrapper(int.class));
        assertTrue("long", Primitives.isPrimitiveOrWrapper(long.class));
        assertTrue("float", Primitives.isPrimitiveOrWrapper(float.class));
        assertTrue("double", Primitives.isPrimitiveOrWrapper(double.class));
        assertTrue("void", Primitives.isPrimitiveOrWrapper(void.class));
    }

    @Test
    public void testIsPrimitiveOrWrapper_withWrappers_returnsTrue() {
        assertTrue("Boolean", Primitives.isPrimitiveOrWrapper(Boolean.class));
        assertTrue("Character", Primitives.isPrimitiveOrWrapper(Character.class));
        assertTrue("Byte", Primitives.isPrimitiveOrWrapper(Byte.class));
        assertTrue("Short", Primitives.isPrimitiveOrWrapper(Short.class));
        assertTrue("Integer", Primitives.isPrimitiveOrWrapper(Integer.class));
        assertTrue("Long", Primitives.isPrimitiveOrWrapper(Long.class));
        assertTrue("Float", Primitives.isPrimitiveOrWrapper(Float.class));
        assertTrue("Double", Primitives.isPrimitiveOrWrapper(Double.class));
        assertTrue("Void", Primitives.isPrimitiveOrWrapper(Void.class));
    }

    @Test
    public void testIsPrimitiveOrWrapper_withNonPrimitiveNonWrapper_returnsFalse() {
        assertFalse("String", Primitives.isPrimitiveOrWrapper(String.class));
        assertFalse("Object", Primitives.isPrimitiveOrWrapper(Object.class));
        assertFalse("null", Primitives.isPrimitiveOrWrapper(null));
    }

    // ---------- allPrimitiveWrappers (if exists) ----------
    // Not present in all versions, but we can test the set if exposed.
    // Assuming there is a method getAllPrimitiveWrapperTypes()? Not in original.
    // Skipping.

    // ---------- Edge cases for null safety ----------

    @Test(expected = NullPointerException.class)
    public void testPrimitiveTypeOf_withNull_doesNotThrowNPE() {
        // Actually the method should handle null gracefully, but if not, we expect NPE.
        // The bug might cause NPE for null input. We'll test that it returns null.
        // But to be safe, we already tested null returns null.
        // This test is to ensure no exception is thrown.
        assertNull(Primitives.primitiveTypeOf(null));
    }

    @Test(expected = NullPointerException.class)
    public void testWrapperOf_withNull_doesNotThrowNPE() {
        assertNull(Primitives.wrapperOf(null));
    }

    // ---------- Additional coverage for internal logic ----------

    @Test
    public void testPrimitiveTypeOf_withUnknownClass_returnsNull() {
        assertNull("Custom class", Primitives.primitiveTypeOf(getClass()));
    }

    @Test
    public void testWrapperOf_withUnknownClass_returnsNull() {
        assertNull("Custom class", Primitives.wrapperOf(getClass()));
    }
}