package com.example; // TODO: Replace with actual package from source

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for CreatorCollector.
 * Designed to achieve high line/branch coverage and detect potential faults.
 */
public class CreatorCollectorTest {
    private CreatorCollector creatorCollector;

    @Before
    public void setUp() {
        creatorCollector = new CreatorCollector();
    }

    @After
    public void tearDown() {
        creatorCollector = null;
    }

    // --- Constructor and initial state ---
    @Test
    public void testInitialState() {
        assertTrue("Should be empty initially", creatorCollector.isEmpty());
        assertNull("Created should be null initially", creatorCollector.getCreated());
        assertNull("Collected should be null initially", creatorCollector.getCollected());
    }

    // --- create() method tests ---
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNull() {
        creatorCollector.create(null);
    }

    @Test
    public void testCreateWithEmptyString() {
        creatorCollector.create("");
        assertEquals("Created should be empty string", "", creatorCollector.getCreated());
        assertFalse("Should not be empty after create", creatorCollector.isEmpty());
    }

    @Test
    public void testCreateWithValidString() {
        creatorCollector.create("item1");
        assertEquals("Created should be 'item1'", "item1", creatorCollector.getCreated());
        assertFalse("Should not be empty after create", creatorCollector.isEmpty());
    }

    @Test
    public void testCreateOverwritesPrevious() {
        creatorCollector.create("first");
        creatorCollector.create("second");
        assertEquals("Created should be 'second' after overwrite", "second", creatorCollector.getCreated());
    }

    @Test
    public void testCreateWithSpecialCharacters() {
        String special = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        creatorCollector.create(special);
        assertEquals("Created should handle special characters", special, creatorCollector.getCreated());
    }

    @Test
    public void testCreateWithVeryLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        String longString = sb.toString();
        creatorCollector.create(longString);
        assertEquals("Created should handle long strings", longString, creatorCollector.getCreated());
    }

    // --- collect() method tests ---
    @Test(expected = IllegalArgumentException.class)
    public void testCollectWithNull() {
        creatorCollector.collect(null);
    }

    @Test
    public void testCollectWithEmptyString() {
        creatorCollector.collect("");
        assertEquals("Collected should be empty string", "", creatorCollector.getCollected());
        assertFalse("Should not be empty after collect", creatorCollector.isEmpty());
    }

    @Test
    public void testCollectWithValidString() {
        creatorCollector.collect("item1");
        assertEquals("Collected should be 'item1'", "item1", creatorCollector.getCollected());
        assertFalse("Should not be empty after collect", creatorCollector.isEmpty());
    }

    @Test
    public void testCollectOverwritesPrevious() {
        creatorCollector.collect("first");
        creatorCollector.collect("second");
        assertEquals("Collected should be 'second' after overwrite", "second", creatorCollector.getCollected());
    }

    @Test
    public void testCollectWithSpecialCharacters() {
        String special = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        creatorCollector.collect(special);
        assertEquals("Collected should handle special characters", special, creatorCollector.getCollected());
    }

    @Test
    public void testCollectWithVeryLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("b");
        }
        String longString = sb.toString();
        creatorCollector.collect(longString);
        assertEquals("Collected should handle long strings", longString, creatorCollector.getCollected());
    }

    // --- Combined create/collect tests ---
    @Test
    public void testCreateAndCollectSameItem() {
        creatorCollector.create("item");
        creatorCollector.collect("item");
        assertEquals("Created should be 'item'", "item", creatorCollector.getCreated());
        assertEquals("Collected should be 'item'", "item", creatorCollector.getCollected());
    }

    @Test
    public void testCreateAndCollectDifferentItems() {
        creatorCollector.create("createItem");
        creatorCollector.collect("collectItem");
        assertEquals("Created should be 'createItem'", "createItem", creatorCollector.getCreated());
        assertEquals("Collected should be 'collectItem'", "collectItem", creatorCollector.getCollected());
    }

    // --- clear() method tests ---
    @Test
    public void testClearAfterCreate() {
        creatorCollector.create("item");
        creatorCollector.clear();
        assertNull("Created should be null after clear", creatorCollector.getCreated());
        assertNull("Collected should be null after clear", creatorCollector.getCollected());
        assertTrue("Should be empty after clear", creatorCollector.isEmpty());
    }

    @Test
    public void testClearAfterCollect() {
        creatorCollector.collect("item");
        creatorCollector.clear();
        assertNull("Created should be null after clear", creatorCollector.getCreated());
        assertNull("Collected should be null after clear", creatorCollector.getCollected());
        assertTrue("Should be empty after clear", creatorCollector.isEmpty());
    }

    @Test
    public void testClearAfterBoth() {
        creatorCollector.create("a");
        creatorCollector.collect("b");
        creatorCollector.clear();
        assertNull("Created should be null after clear", creatorCollector.getCreated());
        assertNull("Collected should be null after clear", creatorCollector.getCollected());
        assertTrue("Should be empty after clear", creatorCollector.isEmpty());
    }

    @Test
    public void testClearOnEmpty() {
        creatorCollector.clear();
        assertNull("Created should be null after clear on empty", creatorCollector.getCreated());
        assertNull("Collected should be null after clear on empty", creatorCollector.getCollected());
        assertTrue("Should be empty after clear on empty", creatorCollector.isEmpty());
    }

    // --- isEmpty() method tests ---
    @Test
    public void testIsEmptyAfterCreateAndClear() {
        creatorCollector.create("item");
        assertFalse("Should not be empty after create", creatorCollector.isEmpty());
        creatorCollector.clear();
        assertTrue("Should be empty after clear", creatorCollector.isEmpty());
    }

    @Test
    public void testIsEmptyAfterCollectAndClear() {
        creatorCollector.collect("item");
        assertFalse("Should not be empty after collect", creatorCollector.isEmpty());
        creatorCollector.clear();
        assertTrue("Should be empty after clear", creatorCollector.isEmpty());
    }

    // --- Edge cases and potential fault triggers ---
    @Test
    public void testCreateThenCollectThenCreate() {
        creatorCollector.create("first");
        creatorCollector.collect("first");
        creatorCollector.create("second");
        assertEquals("Created should be 'second'", "second", creatorCollector.getCreated());
        assertEquals("Collected should still be 'first'", "first", creatorCollector.getCollected());
    }

    @Test
    public void testCollectThenCreateThenCollect() {
        creatorCollector.collect("first");
        creatorCollector.create("second");
        creatorCollector.collect("third");
        assertEquals("Created should be 'second'", "second", creatorCollector.getCreated());
        assertEquals("Collected should be 'third'", "third", creatorCollector.getCollected());
    }

    @Test
    public void testNullAfterClearThenCreate() {
        creatorCollector.clear();
        creatorCollector.create("newItem");
        assertEquals("Created should be 'newItem' after clear and create", "newItem", creatorCollector.getCreated());
        assertNull("Collected should be null after clear and create", creatorCollector.getCollected());
    }

    @Test
    public void testNullAfterClearThenCollect() {
        creatorCollector.clear();
        creatorCollector.collect("newItem");
        assertNull("Created should be null after clear and collect", creatorCollector.getCreated());
        assertEquals("Collected should be 'newItem' after clear and collect", "newItem", creatorCollector.getCollected());
    }

    // --- Test that methods are idempotent when called multiple times ---
    @Test
    public void testMultipleCreatesSameValue() {
        creatorCollector.create("same");
        creatorCollector.create("same");
        assertEquals("Created should be 'same'", "same", creatorCollector.getCreated());
    }

    @Test
    public void testMultipleCollectsSameValue() {
        creatorCollector.collect("same");
        creatorCollector.collect("same");
        assertEquals("Collected should be 'same'", "same", creatorCollector.getCollected());
    }

    // --- Test with whitespace strings ---
    @Test
    public void testCreateWithWhitespace() {
        creatorCollector.create("   ");
        assertEquals("Created should be whitespace string", "   ", creatorCollector.getCreated());
    }

    @Test
    public void testCollectWithWhitespace() {
        creatorCollector.collect("   ");
        assertEquals("Collected should be whitespace string", "   ", creatorCollector.getCollected());
    }

    // --- Test that getCreated and getCollected return references (not copies) if applicable ---
    // This test assumes the class stores the exact reference; if it copies, adjust accordingly.
    @Test
    public void testReferenceIntegrity() {
        String original = "test";
        creatorCollector.create(original);
        assertSame("getCreated should return the same reference", original, creatorCollector.getCreated());
        creatorCollector.collect(original);
        assertSame("getCollected should return the same reference", original, creatorCollector.getCollected());
    }
}