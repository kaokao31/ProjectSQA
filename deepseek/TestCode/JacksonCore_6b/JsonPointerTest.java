package com.fasterxml.jackson.core;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class JsonPointerTest {

    private JsonPointer emptyPointer;
    private JsonPointer rootPointer;
    private JsonPointer simplePropertyPointer;
    private JsonPointer nestedPointer;
    private JsonPointer arrayIndexPointer;
    private JsonPointer escapedPointer;

    @Before
    public void setUp() {
        emptyPointer = JsonPointer.compile("");
        rootPointer = JsonPointer.compile("/");
        simplePropertyPointer = JsonPointer.compile("/a");
        nestedPointer = JsonPointer.compile("/a/b");
        arrayIndexPointer = JsonPointer.compile("/0");
        escapedPointer = JsonPointer.compile("/a~0b~1c");
    }

    // --- compile() and toString() ---

    @Test
    public void testCompileEmptyString() {
        assertEquals("", emptyPointer.toString());
        assertTrue(emptyPointer.matches());
        assertNull(emptyPointer.getMatchingProperty());
        assertEquals(-1, emptyPointer.getMatchingIndex());
    }

    @Test
    public void testCompileRoot() {
        assertEquals("/", rootPointer.toString());
        assertTrue(rootPointer.matches());
        assertEquals("", rootPointer.getMatchingProperty());
        assertEquals(-1, rootPointer.getMatchingIndex());
    }

    @Test
    public void testCompileSimpleProperty() {
        assertEquals("/a", simplePropertyPointer.toString());
        assertTrue(simplePropertyPointer.matches());
        assertEquals("a", simplePropertyPointer.getMatchingProperty());
        assertEquals(-1, simplePropertyPointer.getMatchingIndex());
    }

    @Test
    public void testCompileNested() {
        assertEquals("/a/b", nestedPointer.toString());
        assertTrue(nestedPointer.matches());
        assertEquals("b", nestedPointer.getMatchingProperty());
        assertEquals(-1, nestedPointer.getMatchingIndex());
    }

    @Test
    public void testCompileArrayIndex() {
        assertEquals("/0", arrayIndexPointer.toString());
        assertTrue(arrayIndexPointer.matches());
        assertEquals("0", arrayIndexPointer.getMatchingProperty());
        assertEquals(0, arrayIndexPointer.getMatchingIndex());
    }

    @Test
    public void testCompileEscaped() {
        assertEquals("/a~0b~1c", escapedPointer.toString());
        assertTrue(escapedPointer.matches());
        assertEquals("a~0b~1c", escapedPointer.getMatchingProperty());
        assertEquals(-1, escapedPointer.getMatchingIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileNull() {
        JsonPointer.compile(null);
    }

    // --- head() and tail() ---

    @Test
    public void testHeadEmpty() {
        assertNull(emptyPointer.head());
    }

    @Test
    public void testHeadRoot() {
        assertNull(rootPointer.head());
    }

    @Test
    public void testHeadSimple() {
        JsonPointer head = simplePropertyPointer.head();
        assertNotNull(head);
        assertEquals("", head.toString());
        assertTrue(head.matches());
    }

    @Test
    public void testHeadNested() {
        JsonPointer head = nestedPointer.head();
        assertNotNull(head);
        assertEquals("/a", head.toString());
        assertTrue(head.matches());
        assertEquals("a", head.getMatchingProperty());
    }

    @Test
    public void testTailEmpty() {
        assertNull(emptyPointer.tail());
    }

    @Test
    public void testTailRoot() {
        assertNull(rootPointer.tail());
    }

    @Test
    public void testTailSimple() {
        JsonPointer tail = simplePropertyPointer.tail();
        assertNull(tail);
    }

    @Test
    public void testTailNested() {
        JsonPointer tail = nestedPointer.tail();
        assertNotNull(tail);
        assertEquals("/b", tail.toString());
        assertTrue(tail.matches());
        assertEquals("b", tail.getMatchingProperty());
    }

    // --- matchProperty / matchElement ---

    @Test
    public void testMatchPropertyEmpty() {
        assertFalse(emptyPointer.matchProperty("a"));
    }

    @Test
    public void testMatchPropertyRoot() {
        assertTrue(rootPointer.matchProperty(""));
        assertFalse(rootPointer.matchProperty("a"));
    }

    @Test
    public void testMatchPropertySimple() {
        assertTrue(simplePropertyPointer.matchProperty("a"));
        assertFalse(simplePropertyPointer.matchProperty("b"));
    }

    @Test
    public void testMatchPropertyNested() {
        assertTrue(nestedPointer.matchProperty("b"));
        assertFalse(nestedPointer.matchProperty("a"));
    }

    @Test
    public void testMatchElementEmpty() {
        assertFalse(emptyPointer.matchElement(0));
    }

    @Test
    public void testMatchElementRoot() {
        assertFalse(rootPointer.matchElement(0));
    }

    @Test
    public void testMatchElementArrayIndex() {
        assertTrue(arrayIndexPointer.matchElement(0));
        assertFalse(arrayIndexPointer.matchElement(1));
    }

    // --- matches() ---

    @Test
    public void testMatchesEmpty() {
        assertTrue(emptyPointer.matches());
    }

    @Test
    public void testMatchesRoot() {
        assertTrue(rootPointer.matches());
    }

    @Test
    public void testMatchesSimple() {
        assertTrue(simplePropertyPointer.matches());
    }

    @Test
    public void testMatchesNested() {
        assertTrue(nestedPointer.matches());
    }

    // --- equals() and hashCode() ---

    @Test
    public void testEqualsSame() {
        assertEquals(emptyPointer, emptyPointer);
        assertEquals(rootPointer, rootPointer);
        assertEquals(simplePropertyPointer, simplePropertyPointer);
    }

    @Test
    public void testEqualsDifferent() {
        assertNotEquals(emptyPointer, rootPointer);
        assertNotEquals(simplePropertyPointer, nestedPointer);
        assertNotEquals(arrayIndexPointer, simplePropertyPointer);
    }

    @Test
    public void testEqualsNull() {
        assertNotNull(emptyPointer);
        assertFalse(emptyPointer.equals(null));
    }

    @Test
    public void testHashCodeConsistency() {
        assertEquals(emptyPointer.hashCode(), JsonPointer.compile("").hashCode());
        assertEquals(rootPointer.hashCode(), JsonPointer.compile("/").hashCode());
        assertEquals(simplePropertyPointer.hashCode(), JsonPointer.compile("/a").hashCode());
    }

    // --- Edge cases for compile ---

    @Test
    public void testCompileDoubleSlash() {
        JsonPointer ptr = JsonPointer.compile("//");
        assertEquals("//", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
        JsonPointer tail = ptr.tail();
        assertNotNull(tail);
        assertEquals("/", tail.toString());
    }

    @Test
    public void testCompileTrailingSlash() {
        JsonPointer ptr = JsonPointer.compile("/a/");
        assertEquals("/a/", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test
    public void testCompileNumericIndex() {
        JsonPointer ptr = JsonPointer.compile("/123");
        assertEquals("/123", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("123", ptr.getMatchingProperty());
        assertEquals(123, ptr.getMatchingIndex());
    }

    @Test
    public void testCompileNegativeIndex() {
        JsonPointer ptr = JsonPointer.compile("/-1");
        assertEquals("/-1", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("-1", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test
    public void testCompileLeadingZerosIndex() {
        JsonPointer ptr = JsonPointer.compile("/01");
        assertEquals("/01", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("01", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex()); // not a valid index because of leading zero
    }

    @Test
    public void testCompileEscapedTilde() {
        JsonPointer ptr = JsonPointer.compile("/~0~1");
        assertEquals("/~0~1", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("~0~1", ptr.getMatchingProperty());
        assertEquals(-1, ptr.getMatchingIndex());
    }

    @Test
    public void testCompileInvalidEscape() {
        // ~ followed by something other than 0 or 1 should be treated as literal
        JsonPointer ptr = JsonPointer.compile("/~2");
        assertEquals("/~2", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("~2", ptr.getMatchingProperty());
    }

    @Test
    public void testCompileEmptySegmentAfterSlash() {
        JsonPointer ptr = JsonPointer.compile("/a//b");
        assertEquals("/a//b", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("b", ptr.getMatchingProperty());
    }

    // --- toString() after construction ---

    @Test
    public void testToStringEmpty() {
        assertEquals("", emptyPointer.toString());
    }

    @Test
    public void testToStringRoot() {
        assertEquals("/", rootPointer.toString());
    }

    @Test
    public void testToStringSimple() {
        assertEquals("/a", simplePropertyPointer.toString());
    }

    @Test
    public void testToStringNested() {
        assertEquals("/a/b", nestedPointer.toString());
    }

    @Test
    public void testToStringArrayIndex() {
        assertEquals("/0", arrayIndexPointer.toString());
    }

    @Test
    public void testToStringEscaped() {
        assertEquals("/a~0b~1c", escapedPointer.toString());
    }

    // --- getMatchingProperty / getMatchingIndex ---

    @Test
    public void testGetMatchingPropertyEmpty() {
        assertNull(emptyPointer.getMatchingProperty());
    }

    @Test
    public void testGetMatchingPropertyRoot() {
        assertEquals("", rootPointer.getMatchingProperty());
    }

    @Test
    public void testGetMatchingPropertySimple() {
        assertEquals("a", simplePropertyPointer.getMatchingProperty());
    }

    @Test
    public void testGetMatchingIndexEmpty() {
        assertEquals(-1, emptyPointer.getMatchingIndex());
    }

    @Test
    public void testGetMatchingIndexRoot() {
        assertEquals(-1, rootPointer.getMatchingIndex());
    }

    @Test
    public void testGetMatchingIndexArray() {
        assertEquals(0, arrayIndexPointer.getMatchingIndex());
    }

    @Test
    public void testGetMatchingIndexNonNumeric() {
        assertEquals(-1, simplePropertyPointer.getMatchingIndex());
    }

    // --- Additional edge cases for coverage ---

    @Test
    public void testCompileSingleCharacterProperty() {
        JsonPointer ptr = JsonPointer.compile("/x");
        assertEquals("/x", ptr.toString());
        assertEquals("x", ptr.getMatchingProperty());
    }

    @Test
    public void testCompileLongPath() {
        JsonPointer ptr = JsonPointer.compile("/a/b/c/d/e/f");
        assertEquals("/a/b/c/d/e/f", ptr.toString());
        assertTrue(ptr.matches());
        assertEquals("f", ptr.getMatchingProperty());
    }

    @Test
    public void testHeadOfArrayIndex() {
        JsonPointer head = arrayIndexPointer.head();
        assertNotNull(head);
        assertEquals("", head.toString());
    }

    @Test
    public void testTailOfArrayIndex() {
        JsonPointer tail = arrayIndexPointer.tail();
        assertNull(tail);
    }

    @Test
    public void testMatchPropertyOnArrayIndex() {
        assertFalse(arrayIndexPointer.matchProperty("0"));
    }

    @Test
    public void testMatchElementOnProperty() {
        assertFalse(simplePropertyPointer.matchElement(0));
    }

    @Test
    public void testMatchesAfterTail() {
        JsonPointer tail = nestedPointer.tail();
        assertNotNull(tail);
        assertTrue(tail.matches());
        assertEquals("b", tail.getMatchingProperty());
    }

    @Test
    public void testEqualsWithDifferentTypes() {
        assertFalse(emptyPointer.equals(""));
    }

    @Test
    public void testHashCodeDifferent() {
        assertNotEquals(emptyPointer.hashCode(), rootPointer.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileNullString() {
        JsonPointer.compile(null);
    }
}