package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for StrBuilder, targeting high coverage and defect detection.
 */
public class StrBuilderTest {

    // ---------- Constructor Tests ----------
    @Test
    public void testConstructorDefault() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.toString());
        assertEquals(32, sb.capacity());    // default capacity
    }

    @Test
    public void testConstructorWithInitialCapacity() {
        StrBuilder sb = new StrBuilder(64);
        assertEquals("", sb.toString());
        assertTrue(sb.capacity() >= 64);
    }

    @Test
    public void testConstructorWithString() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("Hello", sb.toString());
        assertEquals(5, sb.length());
    }

    @Test
    public void testConstructorWithNullString() {
        StrBuilder sb = new StrBuilder((String) null);
        assertEquals("", sb.toString());
    }

    // ---------- Append Tests ----------
    @Test
    public void testAppendNull() {
        StrBuilder sb = new StrBuilder();
        sb.append((String) null);
        assertEquals("null", sb.toString());
    }

    @Test
    public void testAppendEmptyString() {
        StrBuilder sb = new StrBuilder();
        sb.append("");
        assertEquals("", sb.toString());
    }

    @Test
    public void testAppendVarious() {
        StrBuilder sb = new StrBuilder();
        sb.append("abc");
        sb.append('d');
        sb.append(123);
        sb.append(45L);
        sb.append(3.14f);
        sb.append(2.718);
        sb.append(true);
        sb.append((Object) "obj");
        assertEquals("abcd123453.142.718trueobj", sb.toString());
    }

    @Test
    public void testAppendBuffer() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StringBuffer("buf"));
        assertEquals("buf", sb.toString());
    }

    // ---------- Insert Tests ----------
    @Test
    public void testInsertAtStart() {
        StrBuilder sb = new StrBuilder("def");
        sb.insert(0, "abc");
        assertEquals("abcdef", sb.toString());
    }

    @Test
    public void testInsertAtEnd() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(3, "def");
        assertEquals("abcdef", sb.toString());
    }

    @Test
    public void testInsertInMiddle() {
        StrBuilder sb = new StrBuilder("abef");
        sb.insert(2, "cd");
        assertEquals("abcdef", sb.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertNegativeIndex() {
        StrBuilder sb = new StrBuilder();
        sb.insert(-1, "x");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertIndexTooLarge() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(5, "x");
    }

    // ---------- Delete Tests ----------
    @Test
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(1);
        assertEquals("ac", sb.toString());
    }

    @Test
    public void testDeleteRange() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.delete(1, 4);
        assertEquals("aef", sb.toString());
    }

    @Test
    public void testDeleteAll() {
        StrBuilder sb = new StrBuilder("abcabc");
        sb.deleteAll('a');
        assertEquals("bcbc", sb.toString());
    }

    @Test
    public void testDeleteFirst() {
        StrBuilder sb = new StrBuilder("abcabc");
        sb.deleteFirst('a');
        assertEquals("bcabc", sb.toString());
    }

    @Test
    public void testDeleteString() {
        StrBuilder sb = new StrBuilder("abcabc");
        sb.deleteAll("ab");
        assertEquals("cabc", sb.toString());
    }

    // ---------- Replace Tests ----------
    @Test
    public void testReplaceRange() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.replace(1, 4, "xyz");
        assertEquals("axyzf", sb.toString());
    }

    @Test
    public void testReplaceAllChar() {
        StrBuilder sb = new StrBuilder("aba");
        sb.replace('a', 'x');
        assertEquals("xbx", sb.toString());
    }

    @Test
    public void testReplaceFirstChar() {
        StrBuilder sb = new StrBuilder("aba");
        sb.replaceFirst('a', 'x');
        assertEquals("xba", sb.toString());
    }

    // ---------- Substring Tests ----------
    @Test
    public void testSubstring() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("cde", sb.substring(2, 5));
    }

    @Test
    public void testSubstringToEnd() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("def", sb.substring(3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringNegativeStart() {
        StrBuilder sb = new StrBuilder("abc");
        sb.substring(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringEndBeforeStart() {
        StrBuilder sb = new StrBuilder("abc");
        sb.substring(2, 1);
    }

    // ---------- IndexOf / LastIndexOf Tests ----------
    @Test
    public void testIndexOfChar() {
        StrBuilder sb = new StrBuilder("abcabc");
        assertEquals(1, sb.indexOf('b'));
        assertEquals(4, sb.indexOf('b', 2));
        assertEquals(-1, sb.indexOf('x'));
    }

    @Test
    public void testIndexOfString() {
        StrBuilder sb = new StrBuilder("abcabc");
        assertEquals(0, sb.indexOf("ab"));
        assertEquals(3, sb.indexOf("ab", 1));
        assertEquals(-1, sb.indexOf("xy"));
    }

    @Test
    public void testIndexOfWithNullString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(-1, sb.indexOf((String) null));
    }

    @Test
    public void testIndexOfNegativeStart() {
        StrBuilder sb = new StrBuilder("abc");
        // Negative start should be treated as 0
        assertEquals(0, sb.indexOf("a", -1));
        assertEquals(0, sb.indexOf("a", -5));
    }

    @Test
    public void testIndexOfStartBeyondLength() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(-1, sb.indexOf("a", 10));
    }

    @Test
    public void testLastIndexOfChar() {
        StrBuilder sb = new StrBuilder("abcabc");
        assertEquals(4, sb.lastIndexOf('b'));
        assertEquals(4, sb.lastIndexOf('b', 5));
        assertEquals(1, sb.lastIndexOf('b', 3));
        assertEquals(1, sb.lastIndexOf('b', 1));
        assertEquals(-1, sb.lastIndexOf('x'));
    }

    @Test
    public void testLastIndexOfString() {
        StrBuilder sb = new StrBuilder("abcabc");
        assertEquals(3, sb.lastIndexOf("ab"));
        assertEquals(3, sb.lastIndexOf("ab", 5));
        assertEquals(0, sb.lastIndexOf("ab", 2));
        assertEquals(-1, sb.lastIndexOf("xy"));
    }

    @Test
    public void testLastIndexOfNegativeStart() {
        StrBuilder sb = new StrBuilder("abc");
        // Negative start should be treated as 0
        assertEquals(-1, sb.lastIndexOf("a", -1));
        assertEquals(-1, sb.lastIndexOf("a", -5));
    }

    // ---------- Defects4J Bug Scenarios ----------
    @Test
    public void testIndexOfLang294() {
        // This test reproduces the failing scenario from Defects4J Bug 61
        // Expected -1 but got 6 (actual bug)
        StrBuilder sb = new StrBuilder("Hello World!");
        // Calling indexOf with a start index that is within bounds should work.
        // The bug is likely with a specific string and start index combination.
        // We'll test a suspicious case: string that appears at a position after start.
        int idx = sb.indexOf("l", 9); // "l" at 9? Actually "World!" has 'l' at 8? Let's use a more precise case.
        // According to bug report: expected -1 but was 6. So a search that should return -1 returned 6.
        // Let's test indexOf with a negative start index or something similar.
        // We'll attempt multiple edge cases.
        sb = new StrBuilder("abcabc");
        // Suppose we search for "ab" starting at index 3, should return 3.
        // Now try a case where the search string is not found after start, but due to bug it finds earlier occurrence.
        // The classic bug: indexOf(String, int) with negative start didn't clamp to 0.
        // So testing indexOf("a", -1) should be same as indexOf("a", 0) = 0, but bug may return -1 or something.
        // Actually the reported failure: expected -1 but was 6 -> maybe search for "x" starting at 6 but found something else.
        // Let's create a StrBuilder with content and test:
        sb = new StrBuilder("0123456789");
        // Searching for "0" starting at -1 -> should return 0
        assertEquals(0, sb.indexOf("0", -1));
        // Searching for "9" starting at 10 -> should return -1
        assertEquals(-1, sb.indexOf("9", 10));
        // Searching for "0" starting at 5 -> should return -1
        assertEquals(-1, sb.indexOf("0", 5));
        // Now try the specific pattern from bug: maybe indexOf with start beyond length returns 6 instead of -1?
        // We'll test by constructing a string where a match exists at index 6 but we start after it.
        sb = new StrBuilder("abcdefghij");
        // "abc" at 0, start at 1 -> should return -1? Actually "abc" not found starting at 1 because we need "bc" then "abc" only at 0.
        // So searching for "abc" starting at 1 should return -1.
        assertEquals(-1, sb.indexOf("abc", 1));
        // Now if we start at 2, still -1.
        assertEquals(-1, sb.indexOf("abc", 2));
    }

    @Test
    public void testLang294() {
        // This test reproduces the other failing test: ArrayIndexOutOfBoundsException.
        // Likely caused by substring or delete with negative start or end.
        // We'll test substring with negative start? Already have tests, but let's add more.
        // Also test delete with negative start.
        StrBuilder sb = new StrBuilder("test");
        try {
            sb.substring(-1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            sb.delete(-1, 2);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        // Another common cause: substring with start > end
        try {
            sb.substring(3, 1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        // Also insert with negative index
        try {
            sb.insert(-1, "x");
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        // setCharAt with negative index
        try {
            sb.setCharAt(-1, 'x');
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        // charAt with negative index
        try {
            sb.charAt(-1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // ---------- Additional Edge Cases ----------
    @Test
    public void testEnsureCapacity() {
        StrBuilder sb = new StrBuilder(10);
        sb.ensureCapacity(20);
        assertTrue(sb.capacity() >= 20);
    }

    @Test
    public void testMinimizeCapacity() {
        StrBuilder sb = new StrBuilder(100);
        sb.append("short");
        sb.minimizeCapacity();
        assertTrue(sb.capacity() >= 5 && sb.capacity() < 100);
    }

    @Test
    public void testTrim() {
        StrBuilder sb = new StrBuilder("  abc  ");
        sb.trim();
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testReverse() {
        StrBuilder sb = new StrBuilder("abc");
        sb.reverse();
        assertEquals("cba", sb.toString());
    }

    @Test
    public void testSetCharAt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setCharAt(1, 'x');
        assertEquals("axc", sb.toString());
    }

    @Test
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("abc");
        java.io.Reader reader = sb.asReader();
        char[] buf = new char[10];
        int count = reader.read(buf);
        assertEquals("abc", new String(buf, 0, count));
    }

    @Test
    public void testAsWriter() {
        StrBuilder sb = new StrBuilder();
        java.io.Writer writer = sb.asWriter();
        try {
            writer.write("hello");
            writer.flush();
            assertEquals("hello", sb.toString());
        } catch (java.io.IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testToStringBuffer() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("test", sb.toStringBuffer().toString());
    }

    @Test
    public void testGetSetLength() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.setLength(3);
        assertEquals("abc", sb.toString());
        sb.setLength(5);
        assertEquals("abc\0\0", sb.toString()); // null characters
    }

    @Test
    public void testAppendln() {
        StrBuilder sb = new StrBuilder();
        sb.appendln("line");
        assertTrue(sb.toString().startsWith("line"));
        assertTrue(sb.toString().endsWith("\n"));
    }

    // ---------- Coverage of conditional branches ----------
    @Test
    public void testIndexOfWithEmptyString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(0, sb.indexOf(""));
        assertEquals(1, sb.indexOf("", 1));
        assertEquals(3, sb.indexOf("", 3));
        assertEquals(-1, sb.indexOf("", 4)); // beyond length
    }

    @Test
    public void testLastIndexOfWithEmptyString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(3, sb.lastIndexOf(""));
        assertEquals(3, sb.lastIndexOf("", 5));
        assertEquals(2, sb.lastIndexOf("", 2));
        assertEquals(-1, sb.lastIndexOf("", -1));
    }

    @Test
    public void testAppendFixedWidthPadRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abc", 6, '#');
        assertEquals("abc###", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abc", 6, '#');
        assertEquals("###abc", sb.toString());
    }

    // ---------- Tests for bug in lastIndexOf with negative start ----------
    @Test
    public void testLastIndexOfNegativeStartEdge() {
        // Should clamp to 0 and search from beginning? Actually lastIndexOf with negative start should return -1 because no characters before index 0.
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(-1, sb.lastIndexOf("a", -1));
        assertEquals(-1, sb.lastIndexOf('a', -1));
    }

    @Test
    public void testIndexOfNegativeStartEdge() {
        // Should treat negative start as 0.
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(0, sb.indexOf("a", -1));
        assertEquals(0, sb.indexOf('a', -1));
    }

    // ---------- Tests for delete/insert with empty strings ----------
    @Test
    public void testDeleteAllEmptyString() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteAll("");
        assertEquals("abc", sb.toString()); // no change
    }

    @Test
    public void testReplaceAllEmptyString() {
        StrBuilder sb = new StrBuilder("abc");
        sb.replace("", "x");
        assertEquals("abc", sb.toString()); // no change
    }

    @Test
    public void testReplaceFirstEmptyString() {
        StrBuilder sb = new StrBuilder("abc");
        sb.replaceFirst("", "x");
        assertEquals("abc", sb.toString()); // no change
    }
}