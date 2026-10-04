package org.apache.commons.lang.text;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive test suite for StrBuilder, targeting high coverage and fault detection
 * including the known bug #59 (ArrayIndexOutOfBoundsException in appendFixedWidthPadRight).
 */
public class StrBuilderTest {

    private StrBuilder sb;

    @Before
    public void setUp() {
        sb = new StrBuilder();
    }

    // -------- Constructors ----------
    @Test
    public void testConstructorDefault() {
        StrBuilder s = new StrBuilder();
        assertEquals(0, s.length());
        assertTrue(s.capacity() >= 32);  // default initial capacity
    }

    @Test
    public void testConstructorCapacity() {
        StrBuilder s = new StrBuilder(10);
        assertTrue(s.capacity() >= 10);
        assertEquals(0, s.length());
    }

    @Test
    public void testConstructorString() {
        StrBuilder s = new StrBuilder("abc");
        assertEquals("abc", s.toString());
        assertEquals(3, s.length());
    }

    @Test
    public void testConstructorStringNull() {
        StrBuilder s = new StrBuilder((String) null);
        assertEquals(0, s.length());
    }

    @Test
    public void testConstructorCopy() {
        StrBuilder original = new StrBuilder("test");
        StrBuilder copy = new StrBuilder(original);
        assertEquals("test", copy.toString());
    }

    // -------- Length & Capacity ----------
    @Test
    public void testLength() {
        sb.append("hello");
        assertEquals(5, sb.length());
    }

    @Test
    public void testCapacity() {
        assertTrue(sb.capacity() > 0);
    }

    @Test
    public void testEnsureCapacity() {
        sb.ensureCapacity(200);
        assertTrue(sb.capacity() >= 200);
    }

    @Test
    public void testMinimizeCapacity() {
        sb.append("short");
        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());
        assertEquals(5, sb.length());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(sb.isEmpty());
        sb.append('a');
        assertFalse(sb.isEmpty());
        sb.clear();
        assertTrue(sb.isEmpty());
    }

    // -------- Append (various types) ----------
    @Test
    public void testAppendString() {
        sb.append("abc");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testAppendNullString() {
        sb.append((String) null);
        assertEquals("null", sb.toString());  // StrBuilder appends "null"
    }

    @Test
    public void testAppendEmptyString() {
        sb.append("");
        assertEquals("", sb.toString());
    }

    @Test
    public void testAppendStringBuffer() {
        StringBuffer buf = new StringBuffer("xyz");
        sb.append(buf);
        assertEquals("xyz", sb.toString());
    }

    @Test
    public void testAppendNullStringBuffer() {
        sb.append((StringBuffer) null);
        assertEquals("null", sb.toString());
    }

    @Test
    public void testAppendCharSequence() {
        sb.append((CharSequence) "seq");
        assertEquals("seq", sb.toString());
    }

    @Test
    public void testAppendNullCharSequence() {
        sb.append((CharSequence) null);
        assertEquals("null", sb.toString());
    }

    @Test
    public void testAppendChar() {
        sb.append('x');
        assertEquals("x", sb.toString());
    }

    @Test
    public void testAppendCharArray() {
        char[] chars = {'a', 'b', 'c'};
        sb.append(chars);
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testAppendCharArraySub() {
        char[] chars = {'a', 'b', 'c', 'd'};
        sb.append(chars, 1, 2);
        assertEquals("bc", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayBadOffset() {
        char[] chars = {'a', 'b'};
        sb.append(chars, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayBadLength() {
        char[] chars = {'a', 'b'};
        sb.append(chars, 0, 3);
    }

    @Test
    public void testAppendBoolean() {
        sb.append(true);
        assertEquals("true", sb.toString());
        sb.append(false);
        assertEquals("truefalse", sb.toString());
    }

    @Test
    public void testAppendInt() {
        sb.append(123);
        assertEquals("123", sb.toString());
    }

    @Test
    public void testAppendLong() {
        sb.append(9876543210L);
        assertEquals("9876543210", sb.toString());
    }

    @Test
    public void testAppendFloat() {
        sb.append(3.14f);
        assertTrue(sb.toString().startsWith("3.14"));
    }

    @Test
    public void testAppendDouble() {
        sb.append(2.71828);
        assertTrue(sb.toString().startsWith("2.71828"));
    }

    @Test
    public void testAppendObject() {
        sb.append((Object) "obj");
        assertEquals("obj", sb.toString());
    }

    @Test
    public void testAppendNullObject() {
        sb.append((Object) null);
        assertEquals("null", sb.toString());
    }

    @Test
    public void testAppendStrBuilder() {
        StrBuilder other = new StrBuilder("inner");
        sb.append(other);
        assertEquals("inner", sb.toString());
    }

    @Test
    public void testAppendNullStrBuilder() {
        sb.append((StrBuilder) null);
        assertEquals("null", sb.toString());
    }

    // -------- FixedWidthPad (Bug #59 related) ----------
    @Test
    public void testAppendFixedWidthPadRight() {
        sb.appendFixedWidthPadRight("foo", 5, '*');
        assertEquals("foo**", sb.toString());
        assertEquals(5, sb.length());
    }

    @Test
    public void testAppendFixedWidthPadRightTruncate() {
        sb.appendFixedWidthPadRight("foobar", 3, '-');
        assertEquals("foo", sb.toString());
        assertEquals(3, sb.length());
    }

    @Test
    public void testAppendFixedWidthPadRightNullObject() {
        sb.appendFixedWidthPadRight((Object) null, 5, '-');
        assertEquals("null-", sb.toString()); // "null" padded to length 5 with '-'
        assertEquals(5, sb.length());
    }

    @Test
    public void testAppendFixedWidthPadRightWithObject() {
        sb.appendFixedWidthPadRight((Object) Integer.valueOf(42), 5, ' ');
        String val = Integer.toString(42);
        int padding = 5 - val.length();
        StringBuilder expected = new StringBuilder();
        expected.append(val);
        for (int i = 0; i < padding; i++) expected.append(' ');
        assertEquals(expected.toString(), sb.toString());
        assertEquals(5, sb.length());
    }

    // This test reproduces the ArrayIndexOutOfBoundsException from bug #59
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAppendFixedWidthPadRightBug59() {
        // Using a very small initial capacity and then appending fixed width
        StrBuilder s = new StrBuilder(1);
        s.appendFixedWidthPadRight("foo", 5, '-');
        // If bug exists, an ArrayIndexOutOfBoundsException is thrown during append.
        // After fix, no exception, and we can check length.
        assertEquals(5, s.length());
    }

    @Test
    public void testAppendFixedWidthPadLeft() {
        sb.appendFixedWidthPadLeft("bar", 6, '#');
        assertEquals("###bar", sb.toString());
        assertEquals(6, sb.length());
    }

    @Test
    public void testAppendFixedWidthPadLeftTruncate() {
        sb.appendFixedWidthPadLeft("barbaz", 3, '#');
        assertEquals("bar", sb.toString());
        assertEquals(3, sb.length());
    }

    @Test
    public void testAppendFixedWidthPadLeftNullObject() {
        sb.appendFixedWidthPadLeft((Object) null, 5, '*');
        assertEquals("*null", sb.toString());  // padded to length 5
        assertEquals(5, sb.length());
    }

    // -------- Insert ----------
    @Test
    public void testInsertString() {
        sb.append("abcd");
        sb.insert(2, "XY");
        assertEquals("abXYcd", sb.toString());
    }

    @Test
    public void testInsertNullString() {
        sb.append("abcd");
        sb.insert(2, (String) null);
        assertEquals("abnullcd", sb.toString());
    }

    @Test
    public void testInsertChar() {
        sb.append("abcd");
        sb.insert(2, 'Z');
        assertEquals("abZcd", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertNegativeIndex() {
        sb.insert(-1, 'x');
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertIndexTooLarge() {
        sb.insert(10, 'x');
    }

    @Test
    public void testInsertCharArray() {
        sb.append("abcd");
        sb.insert(2, new char[]{'X', 'Y'});
        assertEquals("abXYcd", sb.toString());
    }

    @Test
    public void testInsertBoolean() {
        sb.append("test");
        sb.insert(2, true);
        assertEquals("tetruest", sb.toString());
    }

    // -------- Delete & Replace ----------
    @Test
    public void testDelete() {
        sb.append("abcdef");
        sb.delete(2, 4);
        assertEquals("abef", sb.toString());
    }

    @Test
    public void testDeleteAll() {
        sb.append("abcbcd");
        sb.deleteAll("bc");
        assertEquals("abcd", sb.toString());
    }

    @Test
    public void testDeleteFirst() {
        sb.append("abcbcd");
        sb.deleteFirst("bc");
        assertEquals("abcbcd".replaceFirst("bc", ""), sb.toString());
    }

    @Test
    public void testReplaceString() {
        sb.append("abcabc");
        sb.replace(1, 3, "XY");
        assertEquals("aXYabc", sb.toString());
    }

    @Test
    public void testReplaceAll() {
        sb.append("aba");
        sb.replaceAll("a", "X");
        assertEquals("XbX", sb.toString());
    }

    @Test
    public void testReplaceFirst() {
        sb.append("aba");
        sb.replaceFirst("a", "X");
        assertEquals("Xba", sb.toString());
    }

    // -------- Substring & CharAt ----------
    @Test
    public void testSubstring() {
        sb.append("hello");
        assertEquals("ell", sb.substring(1, 4));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubstringBadRange() {
        sb.substring(2, 1);
    }

    @Test
    public void testCharAt() {
        sb.append("abc");
        assertEquals('b', sb.charAt(1));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtNegative() {
        sb.charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtTooLarge() {
        sb.charAt(100);
    }

    @Test
    public void testSetCharAt() {
        sb.append("abc");
        sb.setCharAt(1, 'Z');
        assertEquals("aZc", sb.toString());
    }

    // -------- IndexOf / LastIndexOf ----------
    @Test
    public void testIndexOfString() {
        sb.append("hello world");
        assertEquals(2, sb.indexOf("llo"));
        assertEquals(-1, sb.indexOf("xyz"));
    }

    @Test
    public void testIndexOfNull() {
        assertEquals(-1, sb.indexOf((String) null));
    }

    @Test
    public void testIndexOfFromIndex() {
        sb.append("aba");
        assertEquals(2, sb.indexOf("a", 1));
    }

    @Test
    public void testLastIndexOfString() {
        sb.append("aba");
        assertEquals(2, sb.lastIndexOf("a"));
        assertEquals(-1, sb.lastIndexOf("x"));
    }

    @Test
    public void testLastIndexOfNull() {
        assertEquals(-1, sb.lastIndexOf((String) null));
    }

    // -------- Trim & Clear ----------
    @Test
    public void testTrim() {
        sb.append("  hello  ");
        sb.trim();
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testClear() {
        sb.append("data");
        sb.clear();
        assertEquals("", sb.toString());
        assertEquals(0, sb.length());
    }

    // -------- Conversion methods ----------
    @Test
    public void testToString() {
        sb.append("test");
        assertEquals("test", sb.toString());
    }

    @Test
    public void testToStringBuffer() {
        sb.append("buf");
        StringBuffer buf = sb.toStringBuffer();
        assertEquals("buf", buf.toString());
    }

    @Test
    public void testToCharArray() {
        sb.append("abc");
        char[] chars = sb.toCharArray();
        assertArrayEquals(new char[]{'a', 'b', 'c'}, chars);
    }

    @Test
    public void testGetChars() {
        sb.append("abcdef");
        char[] dest = new char[4];
        sb.getChars(1, 5, dest, 0);
        assertArrayEquals(new char[]{'b', 'c', 'd', 'e'}, dest);
    }

    // -------- Left / Right / Mid ----------
    @Test
    public void testLeftString() {
        sb.append("hello");
        assertEquals("hel", sb.leftString(3));
    }

    @Test
    public void testRightString() {
        sb.append("hello");
        assertEquals("llo", sb.rightString(3));
    }

    @Test
    public void testMidString() {
        sb.append("hello");
        assertEquals("ell", sb.midString(1, 3));
    }

    // -------- asReader / asWriter / asTokenizer ----------
    @Test
    public void testAsReader() throws Exception {
        sb.append("readme");
        java.io.Reader reader = sb.asReader();
        char[] buf = new char[6];
        int read = reader.read(buf);
        assertEquals(6, read);
        assertEquals("readme", new String(buf));
    }

    @Test
    public void testAsWriter() throws Exception {
        java.io.Writer writer = sb.asWriter();
        writer.write("written");
        writer.flush();
        assertEquals("written", sb.toString());
    }

    @Test
    public void testAsTokenizer() {
        sb.append("one two three");
        StrTokenizer tok = sb.asTokenizer();
        assertNotNull(tok);
        // just ensure no exception
        assertEquals(3, tok.getTokenArray().length);
    }

    // -------- Edge Cases and Bugs ----------
    @Test
    public void testAppendWithLargeCapacity() {
        StrBuilder s = new StrBuilder(5);
        s.append("large");
        assertEquals(5, s.length());
        // Appending more should trigger expansion
        s.append(" extension");
        assertEquals(14, s.length());
    }

    @Test
    public void testInsertAtEnd() {
        sb.append("abc");
        sb.insert(3, "XYZ");
        assertEquals("abcXYZ", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteInvalidRange() {
        sb.delete(1, 0);
    }

    @Test
    public void testDeleteAllEmptyString() {
        sb.append("abc");
        sb.deleteAll("");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testReplaceAllEmptyString() {
        sb.append("abc");
        sb.replaceAll("", "X");
        assertEquals("abc", sb.toString());
    }

    // -------- Additional: ensure capacity + append operations ----------
    @Test
    public void testAppendAfterEnsureCapacity() {
        sb.ensureCapacity(100);
        sb.append("test");
        assertEquals("test", sb.toString());
    }

    @Test
    public void testMinimizeThenAppend() {
        sb.append("short");
        sb.minimizeCapacity();
        sb.append(" extended");
        assertEquals("short extended", sb.toString());
        assertTrue(sb.capacity() >= sb.length());
    }

    // -------- Bug #59 alternative triggers ----------
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAppendFixedWidthPadRightBug59Alt() {
        StrBuilder s = new StrBuilder(2);
        s.appendFixedWidthPadRight("test", 10, '.');
        // This should not throw with fix; but if bug exists, may throw.
        assertEquals(10, s.length());
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAppendFixedWidthPadLeftBug59() {
        StrBuilder s = new StrBuilder(1);
        s.appendFixedWidthPadLeft("bar", 10, '-');
        assertEquals(10, s.length());
    }
}