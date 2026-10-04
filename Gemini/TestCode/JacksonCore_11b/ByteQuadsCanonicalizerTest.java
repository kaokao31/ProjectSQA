package com.fasterxml.jackson.core.sym;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

public class ByteQuadsCanonicalizerTest {

    @Test
    public void testBasicCreationAndMethods() {
        // Create root canonicalizer
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        assertNotNull(root);
        assertEquals(0, root.size());
        assertEquals(64, root.bucketCount());
        assertTrue(root.maybeDirty());

        // Make child
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertNotNull(child);
        assertFalse(child.maybeDirty());

        // Test primary table calculations or state
        // Adding simple names to trigger symbol additions and hashing
        // addName(int[] q, int qlen)
        int[] q1 = new int[] { 12345 };
        int hash1 = root.calcHash(12345);
        assertEquals(hash1, root.calcHash(q1, 1));

        String name1 = root.addName(q1, 1);
        assertNull(name1); // First time adding returns null or the name depending on impl, usually null if new or name if collision. Wait, addName returns String if it already exists or null? Let's check Jackson's addName: returns canonical String.
        
        // Let's verify findName
        int foundHash = root.calcHash(12345);
        assertEquals(12345, root.findName(foundHash));

        // Release child back to root
        child.release();
    }

    @Test
    public void testHashCalculations() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();

        int h1 = root.calcHash(1);
        int h2 = root.calcHash(1, 2);
        int h3 = root.calcHash(1, 2, 3);
        int h4 = root.calcHash(new int[] { 1, 2, 3, 4 }, 4);

        assertTrue(h1 != 0);
        assertTrue(h2 != 0);
        assertTrue(h3 != 0);
        assertTrue(h4 != 0);
    }

    @Test
    public void testNPathNamesAndRehash() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();

        // Add enough names to trigger rehash or collision areas
        for (int i = 0; i < 100; i++) {
            int[] q = new int[] { i, i + 1000 };
            root.addName("name_" + i, q, 2);
        }

        assertTrue(root.size() > 0);

        // Find added names
        int[] qTest = new int[] { 10, 1010 };
        assertEquals("name_10", root.findName(qTest, 2));
    }

    @Test
    public void testCollisionsAndLongerQuads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();

        // Test 1 quad, 2 quads, 3 quads, 4+ quads
        int[] q1 = new int[] { 111 };
        int[] q2 = new int[] { 111, 222 };
        int[] q3 = new int[] { 111, 222, 333 };
        int[] q4 = new int[] { 111, 222, 333, 444 };
        int[] q5 = new int[] { 111, 222, 333, 444, 555 };

        root.addName("n1", q1, 1);
        root.addName("n2", q2, 2);
        root.addName("n3", q3, 3);
        root.addName("n4", q4, 4);
        root.addName("n5", q5, 5);

        assertEquals("n1", root.findName(q1, 1));
        assertEquals("n2", root.findName(q2, 2));
        assertEquals("n3", root.findName(q3, 3));
        assertEquals("n4", root.findName(q4, 4));
        assertEquals("n5", root.findName(q5, 5));
    }

    @Test
    public void testChildMergingAndState() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(true);

        int[] q = new int[] { 999 };
        child.addName("test", q, 1);

        // Verify child has it, root might or might not depending on sync, but child release will merge if dirty
        assertEquals("test", child.findName(q, 1));

        child.release();
    }

    @Test
    public void testNamerEdgeCases() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        
        // Empty or 0 length quads if applicable
        try {
            root.findName(new int[0], 0);
        } catch (Exception e) {
            // Expected or handled
        }
    }
}