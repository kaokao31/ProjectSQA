package org.apache.commons.lang3;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test suite for StringUtils with emphasis on join methods (Defects4J bug 20).
 */
public class StringUtilsTest {

    // ==================== join(Object[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_ArrayChar_NullArray() {
        StringUtils.join((Object[]) null, ',');
    }

    @Test
    public void testJoin_ArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new Object[0], ','));
    }

    @Test
    public void testJoin_ArrayChar_SingleElement() {
        assertEquals("a", StringUtils.join(new Object[] {"a"}, ','));
    }

    @Test
    public void testJoin_ArrayChar_MultipleElements() {
        assertEquals("a,b,c", StringUtils.join(new Object[] {"a", "b", "c"}, ','));
    }

    @Test
    public void testJoin_ArrayChar_WithNullElement() {
        assertEquals("a,,c", StringUtils.join(new Object[] {"a", null, "c"}, ','));
    }

    @Test
    public void testJoin_ArrayChar_AllNullElements() {
        assertEquals(",,", StringUtils.join(new Object[] {null, null, null}, ','));
    }

    @Test
    public void testJoin_ArrayChar_MixedTypes() {
        assertEquals("1,2,3", StringUtils.join(new Object[] {1, 2, 3}, ','));
    }

    // ==================== join(Object[], String) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_ArrayString_NullArray() {
        StringUtils.join((Object[]) null, ",");
    }

    @Test
    public void testJoin_ArrayString_EmptyArray() {
        assertEquals("", StringUtils.join(new Object[0], ","));
    }

    @Test
    public void testJoin_ArrayString_SingleElement() {
        assertEquals("a", StringUtils.join(new Object[] {"a"}, ","));
    }

    @Test
    public void testJoin_ArrayString_MultipleElements() {
        assertEquals("a,b,c", StringUtils.join(new Object[] {"a", "b", "c"}, ","));
    }

    @Test
    public void testJoin_ArrayString_WithNullElement() {
        assertEquals("a,,c", StringUtils.join(new Object[] {"a", null, "c"}, ","));
    }

    @Test
    public void testJoin_ArrayString_AllNullElements() {
        assertEquals(",,", StringUtils.join(new Object[] {null, null, null}, ","));
    }

    @Test
    public void testJoin_ArrayString_NullSeparator() {
        assertEquals("abc", StringUtils.join(new Object[] {"a", "b", "c"}, (String) null));
    }

    // ==================== join(Object[]) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_Array_NullArray() {
        StringUtils.join((Object[]) null);
    }

    @Test
    public void testJoin_Array_EmptyArray() {
        assertEquals("", StringUtils.join(new Object[0]));
    }

    @Test
    public void testJoin_Array_SingleElement() {
        assertEquals("a", StringUtils.join(new Object[] {"a"}));
    }

    @Test
    public void testJoin_Array_MultipleElements() {
        assertEquals("abc", StringUtils.join(new Object[] {"a", "b", "c"}));
    }

    @Test
    public void testJoin_Array_WithNullElement() {
        assertEquals("ac", StringUtils.join(new Object[] {"a", null, "c"}));
    }

    // ==================== join(long[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_LongArrayChar_NullArray() {
        StringUtils.join((long[]) null, ',');
    }

    @Test
    public void testJoin_LongArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new long[0], ','));
    }

    @Test
    public void testJoin_LongArrayChar_SingleElement() {
        assertEquals("1", StringUtils.join(new long[] {1L}, ','));
    }

    @Test
    public void testJoin_LongArrayChar_MultipleElements() {
        assertEquals("1,2,3", StringUtils.join(new long[] {1L, 2L, 3L}, ','));
    }

    // ==================== join(int[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_IntArrayChar_NullArray() {
        StringUtils.join((int[]) null, ',');
    }

    @Test
    public void testJoin_IntArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new int[0], ','));
    }

    @Test
    public void testJoin_IntArrayChar_SingleElement() {
        assertEquals("1", StringUtils.join(new int[] {1}, ','));
    }

    @Test
    public void testJoin_IntArrayChar_MultipleElements() {
        assertEquals("1,2,3", StringUtils.join(new int[] {1, 2, 3}, ','));
    }

    // ==================== join(short[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_ShortArrayChar_NullArray() {
        StringUtils.join((short[]) null, ',');
    }

    @Test
    public void testJoin_ShortArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new short[0], ','));
    }

    // ==================== join(byte[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_ByteArrayChar_NullArray() {
        StringUtils.join((byte[]) null, ',');
    }

    @Test
    public void testJoin_ByteArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new byte[0], ','));
    }

    // ==================== join(char[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_CharArrayChar_NullArray() {
        StringUtils.join((char[]) null, ',');
    }

    @Test
    public void testJoin_CharArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new char[0], ','));
    }

    @Test
    public void testJoin_CharArrayChar_SingleElement() {
        assertEquals("a", StringUtils.join(new char[] {'a'}, ','));
    }

    @Test
    public void testJoin_CharArrayChar_MultipleElements() {
        assertEquals("a,b,c", StringUtils.join(new char[] {'a', 'b', 'c'}, ','));
    }

    // ==================== join(float[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_FloatArrayChar_NullArray() {
        StringUtils.join((float[]) null, ',');
    }

    @Test
    public void testJoin_FloatArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new float[0], ','));
    }

    // ==================== join(double[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_DoubleArrayChar_NullArray() {
        StringUtils.join((double[]) null, ',');
    }

    @Test
    public void testJoin_DoubleArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new double[0], ','));
    }

    // ==================== join(boolean[], char) ====================

    @Test(expected = NullPointerException.class)
    public void testJoin_BooleanArrayChar_NullArray() {
        StringUtils.join((boolean[]) null, ',');
    }

    @Test
    public void testJoin_BooleanArrayChar_EmptyArray() {
        assertEquals("", StringUtils.join(new boolean[0], ','));
    }

    // Additional coverage: other common methods (non-join) to increase coverage
    @Test
    public void testIsNotEmpty() {
        assertTrue(StringUtils.isNotEmpty(" "));
        assertFalse(StringUtils.isNotEmpty(""));
        assertFalse(StringUtils.isNotEmpty(null));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(null));
        assertFalse(StringUtils.isEmpty(" "));
    }

    @Test
    public void testTrimToEmpty() {
        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(" "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
    }

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("", StringUtils.defaultString(""));
        assertEquals("abc", StringUtils.defaultString("abc"));
    }
}