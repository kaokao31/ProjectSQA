package com.fasterxml.jackson.core.sym;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;

public class ByteQuadsCanonicalizerTest {

    @Test
    public void testRootCanonicalizerCreation() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        assertNotNull(root);
        assertEquals(0, root.size());
        assertFalse(root.maybeDirty());
        
        // Test making child
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertNotNull(child);
        assertNotSame(root, child);
    }

    @Test
    public void testAddNameSingleQuad() {
        ByteQuadsCanonicalizer canonicalizer = ByteQuadsCanonicalizer.createRoot();
        int[] q = new int[] { 12345 };
        
        // Add name with 1 quad
        String name = canonicalizer.addName("testName1", q, 1);
        assertEquals("testName1", name);
        assertEquals(1, canonicalizer.size());
        
        // Find existing name
        String found = canonicalizer.findName(12345);
        assertEquals("testName1", found);
    }

    @Test
    public void testAddNameTwoQuads() {
        ByteQuadsCanonicalizer canonicalizer = ByteQuadsCanonicalizer.createRoot();
        int[] q = new int[] { 111, 222 };
        
        String name = canonicalizer.addName("testName2", q, 2);
        assertEquals("testName2", name);
        assertEquals(1, canonicalizer.size());
        
        String found = canonicalizer.findName(111, 222);
        assertEquals("testName2", found);
    }

    @Test
    public void testAddNameThreeQuads() {
        ByteQuadsCanonicalizer canonicalizer = ByteQuadsCanonicalizer.createRoot();
        int[] q = new int[] { 11, 22, 33 };
        
        String name = canonicalizer.addName("testName3", q, 3);
        assertEquals("testName3", name);
        assertEquals(1, canonicalizer.size());
        
        String found = canonicalizer.findName(11, 22, 33);
        assertEquals("testName3", found);
    }

    @Test
    public void testAddNameNQuads() {
        ByteQuadsCanonicalizer canonicalizer = ByteQuadsCanonicalizer.createRoot();
        int[] q = new int[] { 1, 2, 3, 4, 5 };
        
        String name = canonicalizer.addName("testNameN", q, 5);
        assertEquals("testNameN", name);
        assertEquals(1, canonicalizer.size());
        
        String found = canonicalizer.findName(q, 5);
        assertEquals("testNameN", found);
    }

    @Test
    public void testRehashAndCollisionHandling() {
        ByteQuadsCanonicalizer canonicalizer = ByteQuadsCanonicalizer.createRoot();
        
        // Add multiple names to trigger rehash and potential collision chains (Bug 10 context usually involves rehash / sizing)
        for (int i = 0; i < 100; i++) {
            int[] q = new int[] { i, i * 31, i * 37 };
            canonicalizer.addName("name" + i, q, 3);
        }
        
        assertEquals(100, canonicalizer.size());
        
        // Verify we can find them back
        for (int i = 0; i < 100; i++) {
            String found = canonicalizer.findName(i, i * 31, i * 37);
            assertEquals("name" + i, found);
        }
    }

    @Test
    public void testChildCanonicalizerNPEOrSyncBack() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(123);
        
        int[] q = new int[] { 999 };
        child.addName("childName", q, 1);
        assertEquals(1, child.size());
        
        // Trigger child release / sync back if method exists
        child.release();
    }

    @Test
    public void testNulAndEmptyFinds() {
        ByteQuadsCanonicalizer canonicalizer = ByteQuadsCanonicalizer.createRoot();
        assertNull(canonicalizer.findName(99999));
        assertNull(canonicalizer.findName(111, 222));
        assertNull(canonicalizer.findName(11, 22, 33));
        assertNull(canonicalizer.findName(new int[] { 1, 2, 3 }, 3));
    }
}