package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

public class StrBuilderTest {

    // Methods that reproduce the known NullPointerException bugs
    @Test
    public void testLang412Left() {
        StrBuilder sb = new StrBuilder("abc");
        sb.clear();
        // leftString should not throw NullPointerException on empty builder
        assertEquals("", sb.leftString(0));
    }

    @Test
    public void testLang412Right() {
        StrBuilder sb = new StrBuilder("abc");
        sb.clear();
        // rightString should not throw NullPointerException on empty builder
        assertEquals("", sb.rightString(0));
    }

    // Additional tests to cover edge cases and increase coverage
    @Test
    public void testSubstringOnEmpty() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.substring(0, 0));
    }

    @Test
    public void testSubstringAfterClear() {
        StrBuilder sb = new StrBuilder("test");
        sb.clear();
        assertEquals("", sb.substring(0, 0));
    }

    @Test
    public void testSubstringAfterSetLengthZero() {
        StrBuilder sb = new StrBuilder("test");
        sb.setLength(0);
        assertEquals("", sb.substring(0, 0));
    }

    @Test
    public void testMidStringOnEmpty() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.midString(0, 0));
    }

    @Test
    public void testMidStringAfterClear() {
        StrBuilder sb = new StrBuilder("test");
        sb.clear();
        assertEquals("", sb.midString(0, 0));
    }

    @Test
    public void testLeftStringWithPositiveLength() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("ab", sb.leftString(2));
        assertEquals("abcdef", sb.leftString(10));
    }

    @Test
    public void testRightStringWithPositiveLength() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("ef", sb.rightString(2));
        assertEquals("abcdef", sb.rightString(10));
    }

    @Test
    public void testMidStringWithPositiveIndex() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("cd", sb.midString(2, 2));
        assertEquals("", sb.midString(10, 2));
    }

    @Test
    public void testNullStringConstructor() {
        StrBuilder sb = new StrBuilder((String) null);
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
        assertEquals("", sb.substring(0, 0));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.midString(0, 0));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testLeftStringNegativeLength() {
        StrBuilder sb = new StrBuilder("abc");
        sb.leftString(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testRightStringNegativeLength() {
        StrBuilder sb = new StrBuilder("abc");
        sb.rightString(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testMidStringNegativeStart() {
        StrBuilder sb = new StrBuilder("abc");
        sb.midString(-1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubstringStartNegative() {
        StrBuilder sb = new StrBuilder("abc");
        sb.substring(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubstringEndBeyondLength() {
        StrBuilder sb = new StrBuilder("abc");
        sb.substring(1, 10);
    }

    @Test
    public void testClearAndLength() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals(4, sb.length());
        sb.clear();
        assertEquals(0, sb.length());
        assertEquals("", sb.toString());
    }

    @Test
    public void testSetLengthLargerThanBuffer() {
        StrBuilder sb = new StrBuilder("test");
        sb.setLength(20);
        assertEquals(20, sb.length());
        // Ensure no NPE on subsequent substring
        assertEquals("", sb.substring(0, 0));
    }

    @Test
    public void testSetLengthZeroAndSubstring() {
        StrBuilder sb = new StrBuilder("test");
        sb.setLength(0);
        assertEquals("", sb.substring(0, 0));
    }

    @Test
    public void testDeleteAllAndSubstring() {
        StrBuilder sb = new StrBuilder("test");
        sb.deleteAll('t');
        assertEquals("es", sb.toString());
        sb.clear();
        assertEquals("", sb.substring(0, 0));
    }

    @Test
    public void testReplaceAndSubstring() {
        StrBuilder sb = new StrBuilder("test");
        sb.replace(0, 4, "");
        assertEquals("", sb.substring(0, 0));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.midString(0, 0));
    }

    @Test
    public void testSubstringAfterEnsureCapacity() {
        StrBuilder sb = new StrBuilder(0);
        sb.ensureCapacity(10);
        assertEquals("", sb.substring(0, 0));
    }

    @Test
    public void testAppendEmptyString() {
        StrBuilder sb = new StrBuilder("a");
        sb.append("");
        assertEquals("a", sb.toString());
        assertEquals("a", sb.leftString(1));
    }

    @Test
    public void testToString() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testLengthAfterMultipleOperations() {
        StrBuilder sb = new StrBuilder();
        sb.append("abc");
        assertEquals(3, sb.length());
        sb.append("def");
        assertEquals(6, sb.length());
        sb.delete(0, 3);
        assertEquals(3, sb.length());
        assertEquals("def", sb.toString());
    }

    @Test
    public void testNullAppend() {
        StrBuilder sb = new StrBuilder("abc");
        sb.append((String) null);
        assertEquals("abcnull", sb.toString());
    }
}