package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonPointerTest {

    @Test
    public void testEmptyPointer() {
        JsonPointer ptr = JsonPointer.empty();
        assertNotNull(ptr);
        assertTrue(ptr.matches());
        assertTrue(ptr.mayMatches());
        assertNull(ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        assertNull(ptr.head());
        assertNull(ptr.tail());
        assertEquals("", ptr.toString());
        assertSame(JsonPointer.empty(), JsonPointer.compile(""));
    }

    @Test
    public void testCompileValid() {
        JsonPointer ptr = JsonPointer.compile("/foo/0/bar");
        assertNotNull(ptr);
        assertFalse(ptr.matches());
        assertTrue(ptr.mayMatches());
        assertEquals("foo", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());

        JsonPointer next = ptr.tail();
        assertNotNull(next);
        assertEquals("0", next.getMatchingProperty());
        assertEquals(0, next.getMatchingIndex());

        JsonPointer next2 = next.tail();
        assertNotNull(next2);
        assertEquals("bar", next2.getMatchingProperty());
        assertEquals(-1, next2.getMatchingIndex());

        assertNull(next2.tail());
        assertEquals("/foo/0/bar", ptr.toString());
    }

    @Test
    public void testCompileIntegerParsingBugFix() {
        // Specifically targeting Bug ID 6 where leading zeros or specific number formats in pointers might be misparsed
        JsonPointer ptr = JsonPointer.compile("/0");
        assertEquals(0, ptr.getMatchingIndex());
        assertEquals("0", ptr.getMatchingProperty());

        // Leading zero per RFC 6901: "0" is valid, but "01" is NOT a valid index (should be treated as property, or parse error depending on strictness, Jackson treats invalid/leading zeros as non-numeric property or parses strictly).
        JsonPointer ptrLeadingZero = JsonPointer.compile("/01");
        // In Jackson, parsing index with leading zero like "01" might fail or be considered non-index
        assertEquals(-1, ptrLeadingZero.getMatchingIndex());
        assertEquals("01", ptrLeadingZero.getMatchingProperty());

        // Large index test
        JsonPointer ptrLarge = JsonPointer.compile("/2147483647");
        assertEquals(2147483647, ptrLarge.getMatchingIndex());

        // Overflow index test (should not throw NumberFormatException, but fallback to property or max)
        JsonPointer ptrOverflow = JsonPointer.compile("/2147483648");
        assertEquals(-1, ptrOverflow.getMatchingIndex());
        assertEquals("2147483648", ptrOverflow.getMatchingProperty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileInvalidNoSlash() {
        JsonPointer.compile("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileInvalidEscape() {
        JsonPointer.compile("/foo~");
    }

    @Test
    public void testEscaping() {
        // ~0 -> ~, ~1 -> /
        JsonPointer ptr = JsonPointer.compile("/a~1b/c~0d");
        assertEquals("a/b", ptr.getMatchingProperty());
        JsonPointer next = ptr.tail();
        assertEquals("c~d", next.getMatchingProperty());
        assertEquals("/a~1b/c~0d", ptr.toString());
    }

    @Test
    public void testMatchAndTailMethods() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar");
        
        JsonPointer matchFoo = ptr.matchProperty("foo");
        assertNotNull(matchFoo);
        assertEquals("bar", matchFoo.getMatchingProperty());

        assertNull(ptr.matchProperty("bar"));

        JsonPointer matchElement = JsonPointer.compile("/0/bar").matchElement(0);
        assertNotNull(matchElement);
        assertEquals("bar", matchElement.getMatchingProperty());

        assertNull(JsonPointer.compile("/1/bar").matchElement(0));
    }

    @Test
    public void testHeadMethod() {
        JsonPointer ptr = JsonPointer.compile("/foo/bar/baz");
        JsonPointer head = ptr.head();
        assertEquals("/foo/bar", head.toString());
        assertEquals("/foo", head.head().toString());
        assertEquals("", head.head().head().toString());
    }

    @Test
    public void testAppend() {
        JsonPointer ptr1 = JsonPointer.compile("/foo");
        JsonPointer ptr2 = JsonPointer.compile("/bar");
        JsonPointer combined = ptr1.append(ptr2);
        assertEquals("/foo/bar", combined.toString());

        JsonPointer empty = JsonPointer.empty();
        assertSame(ptr1, ptr1.append(empty));
        assertSame(ptr2, empty.append(ptr2));
        assertSame(empty, empty.append(null));
    }

    @Test
    public void testEqualsAndHashCode() {
        JsonPointer ptr1 = JsonPointer.compile("/foo/1");
        JsonPointer ptr2 = JsonPointer.compile("/foo/1");
        JsonPointer ptr3 = JsonPointer.compile("/foo/2");

        assertEquals(ptr1, ptr1);
        assertEquals(ptr1, ptr2);
        assertEquals(ptr1.hashCode(), ptr2.hashCode());
        assertNotEquals(ptr1, ptr3);
        assertNotEquals(ptr1, null);
        assertNotEquals(ptr1, "string");
    }
}