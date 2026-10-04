package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrimitivesTest {

    @Test
    public void testPrimitiveWrapperDefaults() {
        assertEquals(Boolean.FALSE, Primitives.primitiveWrapperOf(boolean.class));
        assertEquals(Character.valueOf('\0'), Primitives.primitiveWrapperOf(char.class));
        assertEquals(Byte.valueOf((byte) 0), Primitives.primitiveWrapperOf(byte.class));
        assertEquals(Short.valueOf((short) 0), Primitives.primitiveWrapperOf(short.class));
        assertEquals(Integer.valueOf(0), Primitives.primitiveWrapperOf(int.class));
        assertEquals(Long.valueOf(0L), Primitives.primitiveWrapperOf(long.class));
        assertEquals(Float.valueOf(0.0f), Primitives.primitiveWrapperOf(float.class));
        assertEquals(Double.valueOf(0.0d), Primitives.primitiveWrapperOf(double.class));
    }

    @Test
    public void testNonPrimitiveWrapper() {
        assertNull(Primitives.primitiveWrapperOf(String.class));
        assertNull(Primitives.primitiveWrapperOf(Object.class));
        assertNull(Primitives.primitiveWrapperOf(null));
    }

    @Test
    public void testIsPrimitiveWrapper() {
        assertTrue(Primitives.isPrimitiveWrapper(Boolean.TRUE));
        assertTrue(Primitives.isPrimitiveWrapper(Character.valueOf('a')));
        assertTrue(Primitives.isPrimitiveWrapper(Byte.valueOf((byte) 1)));
        assertTrue(Primitives.isPrimitiveWrapper(Short.valueOf((short) 1)));
        assertTrue(Primitives.isPrimitiveWrapper(Integer.valueOf(1)));
        assertTrue(Primitives.isPrimitiveWrapper(Long.valueOf(1L)));
        assertTrue(Primitives.isPrimitiveWrapper(Float.valueOf(1.0f)));
        assertTrue(Primitives.isPrimitiveWrapper(Double.valueOf(1.0d)));

        assertFalse(Primitives.isPrimitiveWrapper("test"));
        assertFalse(Primitives.isPrimitiveWrapper(null));
    }

    @Test
    public void testPrimitiveTypeOf() {
        assertEquals(boolean.class, Primitives.primitiveTypeOf(Boolean.class));
        assertEquals(char.class, Primitives.primitiveTypeOf(Character.class));
        assertEquals(byte.class, Primitives.primitiveTypeOf(Byte.class));
        assertEquals(short.class, Primitives.primitiveTypeOf(Short.class));
        assertEquals(int.class, Primitives.primitiveTypeOf(Integer.class));
        assertEquals(long.class, Primitives.primitiveTypeOf(Long.class));
        assertEquals(float.class, Primitives.primitiveTypeOf(Float.class));
        assertEquals(double.class, Primitives.primitiveTypeOf(Double.class));

        assertNull(Primitives.primitiveTypeOf(String.class));
        assertNull(Primitives.primitiveTypeOf(Object.class));
        assertNull(Primitives.primitiveTypeOf(null));
    }
}