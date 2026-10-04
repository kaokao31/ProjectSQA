package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.List;

/**
 * JUnit 4 test suite for JsonMappingException.
 * Designed to achieve maximum coverage and detect the bug (Defects4J bug 63)
 * where getPathReference() throws NullPointerException when path is null.
 */
public class JsonMappingExceptionTest {

    // ============================================================
    // Tests for constructors
    // ============================================================

    @Test
    public void testConstructorWithMessage() {
        JsonMappingException e = new JsonMappingException("test message");
        assertEquals("test message", e.getMessage());
        assertNull(e.getLocation());
        assertNull(e.getCause());
    }

    @Test
    public void testConstructorWithMessageAndLocation() {
        // Using null location for simplicity; location is not the focus
        JsonMappingException e = new JsonMappingException("test", (com.fasterxml.jackson.core.JsonLocation) null);
        assertEquals("test", e.getMessage());
        assertNull(e.getLocation());
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        JsonMappingException e = new JsonMappingException("test", null, cause);
        assertEquals("test", e.getMessage());
        assertSame(cause, e.getCause());
    }

    // ============================================================
    // Tests for getPathReference() – bug detection
    // ============================================================

    @Test
    public void testGetPathReferenceWhenPathIsNull() {
        // Bug: In the original version, this throws NullPointerException.
        // The test expects no exception and a null return.
        JsonMappingException e = new JsonMappingException("test");
        String pathRef = e.getPathReference();
        assertNull("Path reference should be null when no path has been prepended", pathRef);
    }

    @Test
    public void testGetPathReferenceWithSingleField() {
        JsonMappingException e = new JsonMappingException("test");
        e.prependPath("referrer", "fieldName");
        String pathRef = e.getPathReference();
        assertNotNull(pathRef);
        assertEquals("[\"fieldName\"]", pathRef);
    }

    @Test
    public void testGetPathReferenceWithSingleIndex() {
        JsonMappingException e = new JsonMappingException("test");
        e.prependPath("referrer", 0);
        String pathRef = e.getPathReference();
        assertNotNull(pathRef);
        assertEquals("[0]", pathRef);
    }

    @Test
    public void testGetPathReferenceWithMultipleFields() {
        JsonMappingException e = new JsonMappingException("test");
        e.prependPath("referrer1", "field1");
        e.prependPath("referrer2", "field2");
        // prepend adds to front, so order is field2 -> field1
        String pathRef = e.getPathReference();
        assertNotNull(pathRef);
        assertEquals("[\"field2\"]->[\"field1\"]", pathRef);
    }

    @Test
    public void testGetPathReferenceWithMultipleIndices() {
        JsonMappingException e = new JsonMappingException("test");
        e.prependPath("referrer1", 0);
        e.prependPath("referrer2", 1);
        String pathRef = e.getPathReference();
        assertNotNull(pathRef);
        assertEquals("[1]->[0]", pathRef);
    }

    @Test
    public void testGetPathReferenceWithMixedFieldAndIndex() {
        JsonMappingException e = new JsonMappingException("test");
        e.prependPath("referrer1", "field");
        e.prependPath("referrer2", 42);
        String pathRef = e.getPathReference();
        assertNotNull(pathRef);
        assertEquals("[42]->[\"field\"]", pathRef);
    }

    // ============================================================
    // Tests for getPath()
    // ============================================================

    @Test
    public void testGetPathWhenNull() {
        JsonMappingException e = new JsonMappingException("test");
        assertNull("Path list should be null initially", e.getPath());
    }

    @Test
    public void testGetPathAfterPrepend() {
        JsonMappingException e = new JsonMappingException("test");
        e.prependPath("referrer", "field");
        List<JsonMappingException.Reference> path = e.getPath();
        assertNotNull(path);
        assertEquals(1, path.size());
        assertEquals("field", path.get(0).getFieldName());
    }

    // ============================================================
    // Tests for prependPath() edge cases
    // ============================================================

    @Test
    public void testPrependPathWithNullReferrer() {
        JsonMappingException e = new JsonMappingException("test");
        // Should not throw; referrer may be null
        e.prependPath(null, "field");
        assertNotNull(e.getPath());
        assertEquals(1, e.getPath().size());
    }

    @Test
    public void testPrependPathWithNullFieldName() {
        JsonMappingException e = new JsonMappingException("test");
        // Should not throw; fieldName may be null
        e.prependPath("referrer", (String) null);
        assertNotNull(e.getPath());
        assertEquals(1, e.getPath().size());
        assertNull(e.getPath().get(0).getFieldName());
    }

    @Test
    public void testPrependPathWithNullReferrerAndIndex() {
        JsonMappingException e = new JsonMappingException("test");
        e.prependPath(null, 0);
        assertNotNull(e.getPath());
        assertEquals(1, e.getPath().size());
    }

    // ============================================================
    // Tests for getMessage() with and without path
    // ============================================================

    @Test
    public void testGetMessageWithoutPath() {
        JsonMappingException e = new JsonMappingException("simple message");
        String msg = e.getMessage();
        assertTrue(msg.contains("simple message"));
        // Should not contain path info
        assertFalse(msg.contains("->"));
    }

    @Test
    public void testGetMessageWithPath() {
        JsonMappingException e = new JsonMappingException("msg with path");
        e.prependPath("obj", "field");
        String msg = e.getMessage();
        assertTrue(msg.contains("msg with path"));
        assertTrue(msg.contains("[\"field\"]"));
    }

    // ============================================================
    // Tests for getLocation() and getCause()
    // ============================================================

    @Test
    public void testGetLocation() {
        // Using null location; we trust the constructor stores it
        JsonMappingException e = new JsonMappingException("test", (com.fasterxml.jackson.core.JsonLocation) null);
        assertNull(e.getLocation());
    }

    @Test
    public void testGetCause() {
        Throwable cause = new RuntimeException("cause");
        JsonMappingException e = new JsonMappingException("test", null, cause);
        assertSame(cause, e.getCause());
    }

    // ============================================================
    // Additional coverage: prependPath with both overloads
    // ============================================================

    @Test
    public void testPrependPathMultipleCalls() {
        JsonMappingException e = new JsonMappingException("test");
        e.prependPath("a", "first");
        e.prependPath("b", 1);
        e.prependPath("c", "second");
        List<JsonMappingException.Reference> path = e.getPath();
        assertEquals(3, path.size());
        // Order: last prepended is first in list
        assertEquals("second", path.get(0).getFieldName());
        assertEquals(Integer.valueOf(1), path.get(1).getIndex());
        assertEquals("first", path.get(2).getFieldName());
    }

    // ============================================================
    // Test that getPathReference() does not throw for empty path
    // (path is null, already covered, but also after prepend and then
    //  if path becomes empty? Not possible via public API, but we can
    //  test that it handles null internal list gracefully)
    // ============================================================

    @Test
    public void testGetPathReferenceAfterPrependThenNoPath() {
        // This is not possible via public API, but we can test that
        // if path is null, it returns null (already tested).
        // Additional: ensure that getPathReference() does not modify state.
        JsonMappingException e = new JsonMappingException("test");
        assertNull(e.getPathReference());
        // Still no path
        assertNull(e.getPath());
    }
}