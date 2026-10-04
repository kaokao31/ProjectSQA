package com.fasterxml.jackson.core.sym;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for ByteQuadsCanonicalizer.
 * Targets maximum code coverage and fault detection, including the known
 * spillover overflow bug (Defects4J bug 10).
 */
public class ByteQuadsCanonicalizerTest {

    private ByteQuadsCanonicalizer root;

    @Before
    public void setUp() {
        // Create a root canonicalizer with default settings
        root = ByteQuadsCanonicalizer.createRoot();
    }

    // Helper to add a name and return its quads (for verification)
    private int[] addName(ByteQuadsCanonicalizer table, String name) {
        int[] quads = new int[name.length()];
        for (int i = 0; i < name.length(); i++) {
            quads[i] = (int) name.charAt(i);
        }
        int[] result = table.addName(name, quads, quads.length);
        return result;
    }

    // Helper to find a name
    private int[] findName(ByteQuadsCanonicalizer table, String name) {
        int[] quads = new int[name.length()];
        for (int i = 0; i < name.length(); i++) {
            quads[i] = (int) name.charAt(i);
        }
        return table.findName(name, quads, quads.length);
    }

    @Test
    public void testBasicAddAndFind() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        String name = "abc";
        int[] added = addName(child, name);
        assertNotNull("addName should return non-null", added);
        assertEquals("abc", new String(added, 0, added.length));

        int[] found = findName(child, name);
        assertNotNull("findName should find added name", found);
        assertArrayEquals(added, found);
    }

    @Test
    public void testSameNameReturnsSameInstance() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        String name = "test";
        int[] first = addName(child, name);
        int[] second = addName(child, name);
        assertSame("Same name should return same int[] instance", first, second);
    }

    @Test
    public void testDifferentNames() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        String name1 = "foo";
        String name2 = "bar";
        int[] added1 = addName(child, name1);
        int[] added2 = addName(child, name2);
        assertNotNull(added1);
        assertNotNull(added2);
        assertNotSame("Different names should have different arrays", added1, added2);
    }

    @Test
    public void testEmptyString() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        String empty = "";
        int[] added = addName(child, empty);
        assertNotNull("Empty string should be addable", added);
        assertEquals(0, added.length);

        int[] found = findName(child, empty);
        assertNotNull("Empty string should be findable", found);
        assertEquals(0, found.length);
    }

    @Test
    public void testSingleCharacter() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        String name = "x";
        int[] added = addName(child, name);
        assertNotNull(added);
        assertEquals(1, added.length);
        assertEquals('x', added[0]);
    }

    @Test
    public void testLongName() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('a');
        }
        String longName = sb.toString();
        int[] added = addName(child, longName);
        assertNotNull(added);
        assertEquals(100, added.length);
    }

    @Test
    public void testHashCollision() {
        // Force collisions by using strings that hash to same bucket
        // We'll add many names to cause collisions
        ByteQuadsCanonicalizer child = root.makeChild(1);
        int count = 200;
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = "name" + i;
        }
        for (String name : names) {
            int[] added = addName(child, name);
            assertNotNull("Failed to add name: " + name, added);
        }
        // Verify all can be found
        for (String name : names) {
            int[] found = findName(child, name);
            assertNotNull("Failed to find name: " + name, found);
            assertEquals(name, new String(found, 0, found.length));
        }
    }

    @Test
    public void testSpillOverflow() {
        // This test targets the known bug: spillover area overflow
        // Create a child with small initial hash area to force spillover
        ByteQuadsCanonicalizer child = root.makeChild(1);
        // Add many names to exceed bucket count and force spillover
        int bucketCount = child.bucketCount();
        int namesToAdd = bucketCount * 3; // well beyond capacity
        for (int i = 0; i < namesToAdd; i++) {
            String name = "spill" + i;
            int[] added = addName(child, name);
            assertNotNull("Failed to add name at index " + i, added);
        }
        // Verify all can be found (this would hang or fail with bug)
        for (int i = 0; i < namesToAdd; i++) {
            String name = "spill" + i;
            int[] found = findName(child, name);
            assertNotNull("Failed to find name after spill: " + name, found);
            assertEquals(name, new String(found, 0, found.length));
        }
    }

    @Test
    public void testRehash() {
        // Trigger rehash by adding many names
        ByteQuadsCanonicalizer child = root.makeChild(1);
        int initialBucketCount = child.bucketCount();
        int namesToAdd = initialBucketCount * 2; // should trigger rehash
        for (int i = 0; i < namesToAdd; i++) {
            String name = "rehash" + i;
            addName(child, name);
        }
        int newBucketCount = child.bucketCount();
        assertTrue("Bucket count should increase after rehash", newBucketCount > initialBucketCount);
        // Verify all names still findable
        for (int i = 0; i < namesToAdd; i++) {
            String name = "rehash" + i;
            int[] found = findName(child, name);
            assertNotNull("Name lost after rehash: " + name, found);
        }
    }

    @Test
    public void testBucketCountPowerOfTwo() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        int bc = child.bucketCount();
        assertTrue("Bucket count should be power of two", (bc & (bc - 1)) == 0);
        // After adding many names, bucket count should still be power of two
        for (int i = 0; i < 100; i++) {
            addName(child, "p2" + i);
        }
        bc = child.bucketCount();
        assertTrue("Bucket count after additions should be power of two", (bc & (bc - 1)) == 0);
    }

    @Test
    public void testMultipleChildren() {
        // Test that children are independent
        ByteQuadsCanonicalizer child1 = root.makeChild(1);
        ByteQuadsCanonicalizer child2 = root.makeChild(1);
        String name = "shared";
        addName(child1, name);
        int[] found1 = findName(child1, name);
        assertNotNull("Child1 should find name", found1);
        int[] found2 = findName(child2, name);
        assertNull("Child2 should not find name added to child1", found2);
    }

    @Test
    public void testRelease() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        addName(child, "temp");
        child.release();
        // After release, child should be invalid; further operations may throw
        // We just verify no exception on release
    }

    @Test
    public void testSizeTracking() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        assertEquals(0, child.size());
        addName(child, "a");
        assertEquals(1, child.size());
        addName(child, "b");
        assertEquals(2, child.size());
        // Adding same name should not increase size
        addName(child, "a");
        assertEquals(2, child.size());
    }

    @Test
    public void testFindNonExistent() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        int[] found = findName(child, "nonexistent");
        assertNull("Non-existent name should return null", found);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullName() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        addName(child, null);
    }

    @Test(expected = NullPointerException.class)
    public void testFindNullName() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        findName(child, null);
    }

    @Test
    public void testAddNameWithQuadsArray() {
        // Directly test addName with quads array
        ByteQuadsCanonicalizer child = root.makeChild(1);
        String name = "test";
        int[] quads = new int[]{'t', 'e', 's', 't'};
        int[] result = child.addName(name, quads, quads.length);
        assertNotNull(result);
        assertArrayEquals(quads, result);
    }

    @Test
    public void testFindNameWithQuadsArray() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        String name = "test";
        int[] quads = new int[]{'t', 'e', 's', 't'};
        child.addName(name, quads, quads.length);
        int[] found = child.findName(name, quads, quads.length);
        assertNotNull(found);
        assertArrayEquals(quads, found);
    }

    @Test
    public void testInternedNameEquality() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        String name1 = new String("hello");
        String name2 = new String("hello");
        assertNotSame("Input strings should be different objects", name1, name2);
        int[] added1 = addName(child, name1);
        int[] added2 = addName(child, name2);
        assertSame("Interned names should return same int[]", added1, added2);
    }

    @Test
    public void testLargeNumberOfNames() {
        // Stress test with many unique names
        ByteQuadsCanonicalizer child = root.makeChild(1);
        int count = 1000;
        for (int i = 0; i < count; i++) {
            String name = "stress" + i;
            int[] added = addName(child, name);
            assertNotNull("Failed at index " + i, added);
        }
        assertEquals(count, child.size());
        // Verify all
        for (int i = 0; i < count; i++) {
            String name = "stress" + i;
            int[] found = findName(child, name);
            assertNotNull("Lost name at index " + i, found);
        }
    }

    @Test
    public void testBucketCountAfterRelease() {
        ByteQuadsCanonicalizer child = root.makeChild(1);
        child.release();
        // After release, bucketCount may still be accessible but state is invalid
        // We just ensure no crash
        try {
            child.bucketCount();
        } catch (Exception e) {
            // Expected maybe
        }
    }
}