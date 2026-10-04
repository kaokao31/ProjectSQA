package com.fasterxml.jackson.core;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsonPointerTest {

    // --- Helper method to access the zero-arg compiled() ---
    private JsonPointer compile(String expr) {
        return JsonPointer.compile(expr);
    }

    // --- Token list construction helpers ---
    private List<JsonPointer> tokens(JsonPointer ptr) {
        return ptr.getMatchingProperties();
    }

    // ====================== compile() tests ======================
    @Test
    public void testCompileEmptyString() {
        JsonPointer ptr = compile("");
        assertNotNull(ptr);
        assertEquals("", ptr.toString());
        assertTrue(ptr.matches());
    }

    @Test
    public void testCompileRootOnly() {
        JsonPointer ptr = compile("#");
        assertNotNull(ptr);
        assertEquals("#", ptr.toString());
    }

    @Test
    public void testCompileSimpleProperty() {
        JsonPointer ptr = compile("/foo");
        assertNotNull(ptr);
        assertEquals("/foo", ptr.toString());
    }

    @Test
    public void testCompileNestedProperty() {
        JsonPointer ptr = compile("/foo/bar");
        assertNotNull(ptr);
        assertEquals("/foo/bar", ptr.toString());
    }

    @Test
    public void testCompileEscapedSlash() {
        JsonPointer ptr = compile("/a~1b");
        assertNotNull(ptr);
        assertEquals("/a~1b", ptr.toString());
    }

    @Test
    public void testCompileEscapedTilde() {
        JsonPointer ptr = compile("/a~0b");
        assertNotNull(ptr);
        assertEquals("/a~0b", ptr.toString());
    }

    @Test
    public void testCompileEscapedMultiple() {
        JsonPointer ptr = compile("/~0~1");
        assertNotNull(ptr);
        assertEquals("/~0~1", ptr.toString());
    }

    @Test
    public void testCompilePropertyAfterEscaped() {
        JsonPointer ptr = compile("/~0abc/~1def");
        assertNotNull(ptr);
        assertEquals("/~0abc/~1def", ptr.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileIllegalChar() {
        compile("/foo|bar");
    }

    // ====================== match() tests ======================
    @Test
    public void testMatchRoot() {
        JsonPointer ptr = compile("");
        assertTrue(ptr.matches());
    }

    @Test
    public void testMatchNonRoot() {
        JsonPointer ptr = compile("/a");
        assertFalse(ptr.matches());
    }

    @Test
    public void testMatchDeep() {
        JsonPointer ptr = compile("/a/b/c");
        assertFalse(ptr.matches());
    }

    // ====================== tail() tests ======================
    @Test
    public void testTailRoot() {
        JsonPointer ptr = compile("");
        assertNull(ptr.tail());
    }

    @Test
    public void testTailSingle() {
        JsonPointer ptr = compile("/a");
        JsonPointer tail = ptr.tail();
        assertNotNull(tail);
        assertTrue(tail.matches());
        assertEquals("", tail.toString());
    }

    @Test
    public void testTailMultiple() {
        JsonPointer ptr = compile("/a/b");
        JsonPointer tail = ptr.tail();
        assertNotNull(tail);
        assertEquals("/b", tail.toString());
    }

    @Test
    public void testTailAfterProperty() {
        JsonPointer ptr = compile("/a/b/c");
        assertEquals("/b/c", ptr.tail().toString());
        assertEquals("/c", ptr.tail().tail().toString());
        assertEquals("", ptr.tail().tail().tail().toString());
        assertNull(ptr.tail().tail().tail().tail());
    }

    // ====================== last() tests ======================
    @Test
    public void testLastRoot() {
        JsonPointer ptr = compile("");
        assertTrue(ptr.last().matches());
        assertEquals("", ptr.last().toString());
    }

    @Test
    public void testLastSingle() {
        JsonPointer ptr = compile("/a");
        assertFalse(ptr.last().matches());
        assertEquals("/a", ptr.last().toString());
    }

    @Test
    public void testLastMultiple() {
        JsonPointer ptr = compile("/a/b/c");
        assertEquals("/c", ptr.last().toString());
        assertFalse(ptr.last().matches());
    }

    // ====================== append() tests ======================
    @Test
    public void testAppendToRoot() {
        JsonPointer root = compile("");
        JsonPointer child = root.append("/x");
        assertEquals("/x", child.toString());
    }

    @Test
    public void testAppendToNonRoot() {
        JsonPointer ptr = compile("/a");
        JsonPointer child = ptr.append("/b");
        assertEquals("/a/b", child.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendInvalid() {
        JsonPointer ptr = compile("/a");
        ptr.append("|");
    }

    // ====================== getMatchingProperties() tests ======================
    @Test
    public void testMatchingPropertiesRoot() {
        JsonPointer ptr = compile("");
        assertEquals(Collections.emptyList(), tokens(ptr));
    }

    @Test
    public void testMatchingPropertiesSingle() {
        JsonPointer ptr = compile("/foo");
        List<JsonPointer> props = tokens(ptr);
        assertEquals(1, props.size());
        assertEquals("/foo", props.get(0).toString());
    }

    @Test
    public void testMatchingPropertiesMultiple() {
        JsonPointer ptr = compile("/foo/bar");
        List<JsonPointer> props = tokens(ptr);
        assertEquals(2, props.size());
        assertEquals("/foo", props.get(0).toString());
        assertEquals("/bar", props.get(1).toString());
    }

    @Test
    public void testMatchingPropertiesWithEscaped() {
        JsonPointer ptr = compile("/a~0b/c~1d");
        List<JsonPointer> props = tokens(ptr);
        assertEquals(2, props.size());
        assertEquals("/a~0b", props.get(0).toString());
        assertEquals("/c~1d", props.get(1).toString());
    }

    // ====================== toString() tests ======================
    @Test
    public void testToStringRoot() {
        assertEquals("", compile("").toString());
    }

    @Test
    public void testToStringSimple() {
        assertEquals("/foo", compile("/foo").toString());
    }

    @Test
    public void testToStringNested() {
        assertEquals("/foo/bar", compile("/foo/bar").toString());
    }

    @Test
    public void testToStringWithEscapedSlash() {
        assertEquals("/a~1b", compile("/a~1b").toString());
    }

    @Test
    public void testToStringWithEscapedTilde() {
        assertEquals("/a~0b", compile("/a~0b").toString());
    }

    // ====================== equality tests ======================
    @Test
    public void testEqualsSame() {
        JsonPointer p1 = compile("/a/b");
        JsonPointer p2 = compile("/a/b");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testEqualsDifferentLength() {
        JsonPointer p1 = compile("/a");
        JsonPointer p2 = compile("/a/b");
        assertNotEquals(p1, p2);
    }

    @Test
    public void testEqualsDifferentProperty() {
        JsonPointer p1 = compile("/a");
        JsonPointer p2 = compile("/b");
        assertNotEquals(p1, p2);
    }

    @Test
    public void testEqualsEscapedVsUnescaped() {
        JsonPointer p1 = compile("/a~1b");
        JsonPointer p2 = compile("/a/b");  // different: ~1 vs /
        assertNotEquals(p1, p2);
    }

    // ====================== hashCode tests ======================
    @Test
    public void testHashCodeConsistency() {
        JsonPointer p = compile("/foo/bar");
        int h1 = p.hashCode();
        int h2 = p.hashCode();
        assertEquals(h1, h2);
    }

    // ====================== edge cases ======================
    @Test
    public void testCompileVeryLongPath() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("/a");
        }
        JsonPointer ptr = compile(sb.toString());
        assertNotNull(ptr);
    }

    @Test
    public void testCompileEmptyProperty() {
        JsonPointer ptr = compile("//");
        assertNotNull(ptr);
        assertEquals("//", ptr.toString());
        assertEquals(2, tokens(ptr).size());
    }

    @Test
    public void testTailOnEmptyProperty() {
        JsonPointer ptr = compile("//a");
        assertEquals("/a", ptr.tail().toString());
    }

    @Test
    public void testMatchingPropertiesIncludesEmpty() {
        JsonPointer ptr = compile("/ / /");
        assertEquals(3, tokens(ptr).size());
    }

    // ====================== Defects4J fault detection ======================
    // These tests target known issues in JsonPointer implementations:
    // 1. Escaped characters not properly unescaped in toString() / getMatchingProperties()
    // 2. Tail handling for multi-segment pointers
    // 3. Incorrect handling of consecutive empty segments

    @Test
    public void testDefects4JEscapedSequenceRoundTrip() {
        // The round trip compile -> toString should preserve escaped sequences
        String expr = "/~0~1abc";
        JsonPointer ptr = compile(expr);
        // If toString() re-escapes incorrectly, this will fail
        assertEquals(expr, ptr.toString());
    }

    @Test
    public void testDefects4JDeepTailConsistency() {
        JsonPointer ptr = compile("/a/b/c/d");
        JsonPointer t1 = ptr.tail().tail();
        JsonPointer t2 = ptr.tail().tail();
        // Both should be equal and have same hash
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        // Verify structure
        assertEquals("/c/d", t1.toString());
    }

    @Test
    public void testDefects4JMultipleEmptySegments() {
        // Some implementations mishandle multiple consecutive empty segments
        JsonPointer ptr = compile("///");
        assertEquals(3, tokens(ptr).size());
        // Check tail produces correct segment count
        JsonPointer tail = ptr.tail();
        assertEquals(2, tokens(tail).size());
        assertEquals("//", tail.toString());
    }

    @Test
    public void testDefects4JTrailingSlash() {
        // Edge case: pointer ending with slash
        JsonPointer ptr = compile("/a/");
        assertFalse(ptr.matches());
        assertEquals("/a/", ptr.toString());
        // tail should not throw NullPointerException
        assertNotNull(ptr.tail());
        assertEquals("/", ptr.tail().toString());
    }

    @Test
    public void testDefects4JOnlyEscapedSequence() {
        // Pointer consisting only of escaped tilde and slash
        JsonPointer ptr = compile("/~0~1");
        assertEquals("/~0~1", ptr.toString());
        List<JsonPointer> props = tokens(ptr);
        assertEquals(1, props.size());
        assertEquals("/~0~1", props.get(0).toString());
    }

    @Test
    public void testDefects4JLastOnRootWithEscaped() {
        // For root pointer with escaped characters, last() should still work
        // This can only happen if toString() is used, but root is "" so it's fine
        JsonPointer root = compile("");
        JsonPointer child = root.append("/~0");
        assertEquals("/~0", child.toString());
        assertEquals("/~0", child.last().toString());
        assertFalse(child.last().matches());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefects4JInvalidEscapeSequenceTildeNotFollowed() {
        // Tilde must be followed by 0 or 1, anything else is invalid
        compile("/~2");
    }

    @Test
    public void testDefects4JMultipleEscapedAtStart() {
        // Sequential escaped characters at start
        JsonPointer ptr = compile("/~0~0~1");
        assertEquals("/~0~0~1", ptr.toString());
        List<JsonPointer> props = tokens(ptr);
        assertEquals(1, props.size());
        assertEquals("/~0~0~1", props.get(0).toString());
    }

    @Test
    public void testDefects4JAppendMultiple() {
        // Append operation chaining
        JsonPointer root = compile("");
        JsonPointer p1 = root.append("/a");
        JsonPointer p2 = p1.append("/b");
        JsonPointer p3 = p2.append("/c");
        assertEquals("/a/b/c", p3.toString());
        assertEquals(3, tokens(p3).size());
        assertEquals("/c", p3.last().toString());
    }

    @Test
    public void testDefects4JHugeEscapedSequence() {
        // Stress test with many escaped characters
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        for (int i = 0; i < 100; i++) {
            sb.append("~0~1");
        }
        String expr = sb.toString();
        JsonPointer ptr = compile(expr);
        assertEquals(expr, ptr.toString());
        List<JsonPointer> props = tokens(ptr);
        assertEquals(1, props.size());
        assertEquals(expr, props.get(0).toString());
    }
}