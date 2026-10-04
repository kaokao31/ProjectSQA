package org.apache.commons.lang.text;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for StrBuilder, targeting bug 60 (contains(char) beyond end).
 */
public class StrBuilderTest {

    private StrBuilder builder;

    @Before
    public void setUp() {
        builder = new StrBuilder();
    }

    // ---------- contains(char) tests ----------

    @Test
    public void testContainsCharEmpty() {
        assertFalse("Empty builder should not contain any char", builder.contains('a'));
    }

    @Test
    public void testContainsCharSingle() {
        builder.append("hello");
        assertTrue(builder.contains('h'));
        assertTrue(builder.contains('o'));
        assertFalse(builder.contains('x'));
    }

    @Test
    public void testContainsCharMultiple() {
        builder.append("abcabc");
        assertTrue(builder.contains('a'));
        assertTrue(builder.contains('c'));
        assertFalse(builder.contains('d'));
    }

    @Test
    public void testContainsCharAfterDelete() {
        // Simulate buffer with leftover data beyond size
        builder.append("abcdef");
        builder.delete(0, 3); // now content is "def", buffer may still have 'a','b','c' beyond size
        assertFalse("Should not find deleted char 'a'", builder.contains('a'));
        assertFalse("Should not find deleted char 'b'", builder.contains('b'));
        assertFalse("Should not find deleted char 'c'", builder.contains('c'));
        assertTrue(builder.contains('d'));
        assertTrue(builder.contains('e'));
        assertTrue(builder.contains('f'));
    }

    @Test
    public void testContainsCharAfterInsert() {
        builder.append("xyz");
        builder.insert(0, "abc");
        assertTrue(builder.contains('a'));
        assertTrue(builder.contains('z'));
        assertFalse(builder.contains('w'));
    }

    @Test
    public void testContainsCharAfterReplace() {
        builder.append("hello");
        builder.replace(0, 2, "ab");
        assertTrue(builder.contains('a'));
        assertTrue(builder.contains('b'));
        assertFalse(builder.contains('h'));
    }

    @Test
    public void testContainsCharAfterSetLength() {
        builder.append("abcdef");
        builder.setLength(3); // content becomes "abc", buffer may still have 'd','e','f'
        assertFalse("Should not find char beyond new length", builder.contains('d'));
        assertFalse(builder.contains('e'));
        assertFalse(builder.contains('f'));
        assertTrue(builder.contains('a'));
        assertTrue(builder.contains('c'));
    }

    @Test
    public void testContainsCharAfterEnsureCapacity() {
        builder.append("abc");
        builder.ensureCapacity(20);
        // buffer is now larger, but size is 3
        assertFalse(builder.contains('d'));
        assertTrue(builder.contains('a'));
    }

    // ---------- testLang295 (reproduce the bug) ----------
    @Test
    public void testLang295() {
        // The failing test from Defects4J: contains(char) looking beyond end
        StrBuilder sb = new StrBuilder("abcdef");
        sb.delete(0, 3); // now "def"
        // The bug: contains('a') returns true because it scans beyond size
        assertFalse("contains(char) should not find deleted character", sb.contains('a'));
        assertFalse(sb.contains('b'));
        assertFalse(sb.contains('c'));
        assertTrue(sb.contains('d'));
        assertTrue(sb.contains('e'));
        assertTrue(sb.contains('f'));
    }

    // ---------- Other method tests for coverage ----------

    @Test
    public void testAppend() {
        builder.append("test");
        assertEquals("test", builder.toString());
        builder.append(123);
        assertEquals("test123", builder.toString());
        builder.append(true);
        assertEquals("test123true", builder.toString());
    }

    @Test
    public void testInsert() {
        builder.append("world");
        builder.insert(0, "hello ");
        assertEquals("hello world", builder.toString());
        builder.insert(6, "beautiful ");
        assertEquals("hello beautiful world", builder.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertNegativeIndex() {
        builder.insert(-1, "x");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertIndexTooLarge() {
        builder.insert(1, "x");
    }

    @Test
    public void testDelete() {
        builder.append("abcdef");
        builder.delete(2, 4);
        assertEquals("abef", builder.toString());
        builder.delete(0, 0);
        assertEquals("abef", builder.toString());
        builder.delete(0, 4);
        assertEquals("", builder.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteNegativeStart() {
        builder.append("abc");
        builder.delete(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteEndBeforeStart() {
        builder.append("abc");
        builder.delete(2, 1);
    }

    @Test
    public void testReplace() {
        builder.append("hello world");
        builder.replace(6, 11, "there");
        assertEquals("hello there", builder.toString());
        builder.replace(0, 5, "hi");
        assertEquals("hi there", builder.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReplaceNegativeStart() {
        builder.append("abc");
        builder.replace(-1, 2, "x");
    }

    @Test
    public void testSubstring() {
        builder.append("abcdef");
        assertEquals("abc", builder.substring(0, 3));
        assertEquals("def", builder.substring(3));
        assertEquals("", builder.substring(3, 3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringNegativeStart() {
        builder.append("abc");
        builder.substring(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringEndBeforeStart() {
        builder.append("abc");
        builder.substring(2, 1);
    }

    @Test
    public void testIndexOf() {
        builder.append("abcabc");
        assertEquals(0, builder.indexOf('a'));
        assertEquals(3, builder.indexOf('a', 1));
        assertEquals(-1, builder.indexOf('d'));
        assertEquals(-1, builder.indexOf('a', 10));
    }

    @Test
    public void testLastIndexOf() {
        builder.append("abcabc");
        assertEquals(3, builder.lastIndexOf('a'));
        assertEquals(5, builder.lastIndexOf('c'));
        assertEquals(-1, builder.lastIndexOf('d'));
        assertEquals(-1, builder.lastIndexOf('a', -1));
    }

    @Test
    public void testLength() {
        assertEquals(0, builder.length());
        builder.append("hello");
        assertEquals(5, builder.length());
        builder.delete(0, 2);
        assertEquals(3, builder.length());
    }

    @Test
    public void testCapacity() {
        int initial = builder.capacity();
        assertTrue(initial >= 0);
        builder.ensureCapacity(initial + 100);
        assertTrue(builder.capacity() >= initial + 100);
    }

    @Test
    public void testEnsureCapacity() {
        builder.ensureCapacity(10);
        assertTrue(builder.capacity() >= 10);
        builder.ensureCapacity(0);
        // should not decrease
        assertTrue(builder.capacity() >= 10);
    }

    @Test
    public void testSetLength() {
        builder.append("abcdef");
        builder.setLength(3);
        assertEquals("abc", builder.toString());
        builder.setLength(6);
        // extended with null characters
        assertEquals("abc\0\0\0", builder.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetLengthNegative() {
        builder.setLength(-1);
    }

    @Test
    public void testCharAt() {
        builder.append("hello");
        assertEquals('h', builder.charAt(0));
        assertEquals('o', builder.charAt(4));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAtNegativeIndex() {
        builder.append("abc");
        builder.charAt(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAtIndexTooLarge() {
        builder.append("abc");
        builder.charAt(3);
    }

    @Test
    public void testSetCharAt() {
        builder.append("hello");
        builder.setCharAt(0, 'H');
        assertEquals("Hello", builder.toString());
        builder.setCharAt(4, '!');
        assertEquals("Hell!", builder.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetCharAtNegativeIndex() {
        builder.append("abc");
        builder.setCharAt(-1, 'x');
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetCharAtIndexTooLarge() {
        builder.append("abc");
        builder.setCharAt(3, 'x');
    }

    @Test
    public void testAppendNull() {
        builder.append((String) null);
        assertEquals("null", builder.toString());
    }

    @Test
    public void testAppendObject() {
        builder.append((Object) "test");
        assertEquals("test", builder.toString());
        builder.append((Object) null);
        assertEquals("testnull", builder.toString());
    }

    @Test
    public void testAppendStrBuilder() {
        StrBuilder other = new StrBuilder("world");
        builder.append(other);
        assertEquals("world", builder.toString());
        builder.append((StrBuilder) null);
        assertEquals("worldnull", builder.toString());
    }

    @Test
    public void testAppendCharArray() {
        builder.append(new char[]{'a', 'b', 'c'});
        assertEquals("abc", builder.toString());
        builder.append((char[]) null);
        assertEquals("abcnull", builder.toString());
    }

    @Test
    public void testAppendCharArrayWithOffset() {
        builder.append(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        assertEquals("bc", builder.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAppendCharArrayInvalidOffset() {
        builder.append(new char[]{'a', 'b'}, -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAppendCharArrayInvalidLength() {
        builder.append(new char[]{'a', 'b'}, 0, 3);
    }

    @Test
    public void testAppendBoolean() {
        builder.append(true);
        assertEquals("true", builder.toString());
        builder.append(false);
        assertEquals("truefalse", builder.toString());
    }

    @Test
    public void testAppendInt() {
        builder.append(42);
        assertEquals("42", builder.toString());
        builder.append(-1);
        assertEquals("42-1", builder.toString());
    }

    @Test
    public void testAppendLong() {
        builder.append(1234567890123L);
        assertEquals("1234567890123", builder.toString());
    }

    @Test
    public void testAppendFloat() {
        builder.append(3.14f);
        assertTrue(builder.toString().startsWith("3.14"));
    }

    @Test
    public void testAppendDouble() {
        builder.append(2.71828);
        assertTrue(builder.toString().startsWith("2.71828"));
    }

    @Test
    public void testClear() {
        builder.append("test");
        builder.clear();
        assertEquals("", builder.toString());
        assertEquals(0, builder.length());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(builder.isEmpty());
        builder.append("a");
        assertFalse(builder.isEmpty());
        builder.clear();
        assertTrue(builder.isEmpty());
    }

    @Test
    public void testTrim() {
        builder.append("  hello  ");
        builder.trim();
        assertEquals("hello", builder.toString());
        builder.clear();
        builder.append("   ");
        builder.trim();
        assertEquals("", builder.toString());
    }

    @Test
    public void testReverse() {
        builder.append("abc");
        builder.reverse();
        assertEquals("cba", builder.toString());
        builder.reverse();
        assertEquals("abc", builder.toString());
        builder.clear();
        builder.reverse();
        assertEquals("", builder.toString());
    }

    @Test
    public void testToString() {
        assertEquals("", builder.toString());
        builder.append("test");
        assertEquals("test", builder.toString());
    }

    @Test
    public void testHashCode() {
        builder.append("abc");
        int hash1 = builder.hashCode();
        builder.append("d");
        int hash2 = builder.hashCode();
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testEquals() {
        StrBuilder other = new StrBuilder();
        assertTrue(builder.equals(other));
        builder.append("abc");
        assertFalse(builder.equals(other));
        other.append("abc");
        assertTrue(builder.equals(other));
        assertFalse(builder.equals(null));
        assertFalse(builder.equals("abc"));
    }

    @Test
    public void testMinimizeCapacity() {
        builder.append("a long string that will be trimmed");
        int capBefore = builder.capacity();
        builder.minimizeCapacity();
        assertTrue(builder.capacity() <= capBefore);
        assertEquals(builder.length(), builder.capacity());
    }

    @Test
    public void testGetChars() {
        builder.append("abcdef");
        char[] dst = new char[6];
        builder.getChars(0, 6, dst, 0);
        assertArrayEquals(new char[]{'a','b','c','d','e','f'}, dst);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetCharsInvalidSrcBegin() {
        builder.append("abc");
        builder.getChars(-1, 2, new char[2], 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetCharsInvalidSrcEnd() {
        builder.append("abc");
        builder.getChars(0, 4, new char[4], 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetCharsInvalidDstIndex() {
        builder.append("abc");
        builder.getChars(0, 3, new char[2], 0);
    }
}