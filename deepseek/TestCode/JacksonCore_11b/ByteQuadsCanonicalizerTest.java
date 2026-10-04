package com.fasterxml.jackson.core.sym;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for ByteQuadsCanonicalizer.
 * Designed to achieve high coverage and trigger potential faults,
 * especially related to hash table expansion and collision handling.
 */
public class ByteQuadsCanonicalizerTest {

    private ByteQuadsCanonicalizer symbolTable;

    @Before
    public void setUp() {
        // Create a fresh symbol table with default initial size (likely 64)
        symbolTable = ByteQuadsCanonicalizer.createRoot();
    }

    // Helper to convert a string to a byte array of quads (4 bytes per quad)
    private byte[] toQuads(String str) {
        byte[] bytes = str.getBytes();
        // Pad to multiple of 4
        int len = (bytes.length + 3) & ~3;
        byte[] quads = new byte[len];
        System.arraycopy(bytes, 0, quads, 0, bytes.length);
        return quads;
    }

    // Helper to add a name and return its internal index
    private int addName(String name) {
        byte[] quads = toQuads(name);
        // The method signature: addName(String name, byte[] quads, int qlen)
        // qlen is number of quads (bytes.length / 4)
        int qlen = quads.length / 4;
        return symbolTable.addName(name, quads, qlen);
    }

    // Helper to find a name and return its index (or -1 if not found)
    private int findName(String name) {
        byte[] quads = toQuads(name);
        int qlen = quads.length / 4;
        return symbolTable.findName(name, quads, qlen);
    }

    @Test
    public void testEmptyTable() {
        // Initially no names should be found
        assertEquals(-1, findName("test"));
        assertEquals(-1, findName(""));
    }

    @Test
    public void testAddAndFindSingle() {
        int index = addName("hello");
        assertTrue("Index should be non-negative", index >= 0);
        assertEquals(index, findName("hello"));
        // Duplicate should return same index
        assertEquals(index, addName("hello"));
    }

    @Test
    public void testAddAndFindMultiple() {
        String[] names = {"a", "b", "c", "d", "e"};
        int[] indices = new int[names.length];
        for (int i = 0; i < names.length; i++) {
            indices[i] = addName(names[i]);
            assertTrue("Index for " + names[i] + " should be non-negative", indices[i] >= 0);
        }
        // Verify each can be found
        for (int i = 0; i < names.length; i++) {
            assertEquals("Find " + names[i], indices[i], findName(names[i]));
        }
    }

    @Test
    public void testCollisionAndExpansion() {
        // Add many names that share the same initial quad to force collisions
        // Use names of length 4 (single quad) with same first byte but different second byte
        // This will cause hash collisions and trigger expansion
        int count = 200;
        String[] names = new String[count];
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            // Create a name with first byte fixed to 0x41 ('A') and second byte varying
            names[i] = "A" + (char)('a' + (i % 26)) + "xx"; // length 4
            indices[i] = addName(names[i]);
            assertTrue("Index for " + names[i] + " should be non-negative", indices[i] >= 0);
        }
        // Verify all can be found
        for (int i = 0; i < count; i++) {
            assertEquals("Find " + names[i], indices[i], findName(names[i]));
        }
    }

    @Test
    public void testExpansionWithLongNames() {
        // Add names with multiple quads to exercise different code paths
        int count = 100;
        String[] names = new String[count];
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            // Names of varying lengths (1 to 4 quads)
            StringBuilder sb = new StringBuilder();
            int quadCount = (i % 4) + 1;
            for (int j = 0; j < quadCount; j++) {
                sb.append((char)('a' + (i + j) % 26));
            }
            names[i] = sb.toString();
            indices[i] = addName(names[i]);
            assertTrue("Index for " + names[i] + " should be non-negative", indices[i] >= 0);
        }
        // Verify all can be found
        for (int i = 0; i < count; i++) {
            assertEquals("Find " + names[i], indices[i], findName(names[i]));
        }
    }

    @Test
    public void testDuplicateNamesReturnSameIndex() {
        int idx1 = addName("duplicate");
        int idx2 = addName("duplicate");
        assertEquals("Duplicate names should return same index", idx1, idx2);
        assertEquals(idx1, findName("duplicate"));
    }

    @Test
    public void testEmptyString() {
        int index = addName("");
        assertTrue("Empty string should be added", index >= 0);
        assertEquals(index, findName(""));
        // Duplicate empty string
        assertEquals(index, addName(""));
    }

    @Test
    public void testSingleCharacterNames() {
        for (char c = 'a'; c <= 'z'; c++) {
            String name = String.valueOf(c);
            int idx = addName(name);
            assertTrue("Index for " + name + " should be non-negative", idx >= 0);
            assertEquals(idx, findName(name));
        }
    }

    @Test
    public void testNamesWithAllBytes() {
        // Test names that cover all byte values (0-255) in the first quad
        for (int b = 0; b < 256; b++) {
            byte[] quads = new byte[4];
            quads[0] = (byte) b;
            String name = new String(quads, 0, 4);
            int idx = addName(name);
            assertTrue("Index for byte " + b + " should be non-negative", idx >= 0);
            assertEquals(idx, findName(name));
        }
    }

    @Test
    public void testLargeNumberOfNames() {
        // Add a large number of names to force multiple expansions
        int count = 1000;
        String[] names = new String[count];
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            names[i] = "name" + i;
            indices[i] = addName(names[i]);
            assertTrue("Index for " + names[i] + " should be non-negative", indices[i] >= 0);
        }
        // Verify all can be found
        for (int i = 0; i < count; i++) {
            assertEquals("Find " + names[i], indices[i], findName(names[i]));
        }
    }

    @Test
    public void testFindNonExistent() {
        addName("existing");
        assertEquals(-1, findName("nonexistent"));
        assertEquals(-1, findName(""));
    }

    @Test
    public void testBucketOverflow() {
        // Add many names that hash to the same bucket (by using same first quad)
        // This will cause bucket overflow and trigger rehashing
        int count = 300;
        String[] names = new String[count];
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            // Use same first quad (0x41414141) but different second quad
            byte[] quads = new byte[8];
            quads[0] = 0x41; quads[1] = 0x41; quads[2] = 0x41; quads[3] = 0x41;
            quads[4] = (byte)(i >> 24);
            quads[5] = (byte)(i >> 16);
            quads[6] = (byte)(i >> 8);
            quads[7] = (byte)i;
            String name = new String(quads);
            indices[i] = symbolTable.addName(name, quads, 2);
            assertTrue("Index for name " + i + " should be non-negative", indices[i] >= 0);
        }
        // Verify all can be found
        for (int i = 0; i < count; i++) {
            byte[] quads = new byte[8];
            quads[0] = 0x41; quads[1] = 0x41; quads[2] = 0x41; quads[3] = 0x41;
            quads[4] = (byte)(i >> 24);
            quads[5] = (byte)(i >> 16);
            quads[6] = (byte)(i >> 8);
            quads[7] = (byte)i;
            String name = new String(quads);
            assertEquals("Find name " + i, indices[i], symbolTable.findName(name, quads, 2));
        }
    }

    @Test
    public void testShrinkAfterExpansion() {
        // Add many names, then remove? Not supported. But we can test that
        // after expansion, the table still works correctly.
        // This test just ensures no exceptions.
        for (int i = 0; i < 500; i++) {
            addName("test" + i);
        }
        // Verify a few
        assertTrue(findName("test0") >= 0);
        assertTrue(findName("test499") >= 0);
    }

    @Test
    public void testHashCollisionWithSameName() {
        // Add same name multiple times, should return same index
        int idx1 = addName("collision");
        int idx2 = addName("collision");
        assertEquals(idx1, idx2);
    }

    @Test
    public void testNameWithZeroBytes() {
        // Names containing zero bytes (null character)
        byte[] quads = new byte[4];
        quads[0] = 0; quads[1] = 0; quads[2] = 0; quads[3] = 0;
        String name = new String(quads);
        int idx = symbolTable.addName(name, quads, 1);
        assertTrue("Index for zero bytes should be non-negative", idx >= 0);
        assertEquals(idx, symbolTable.findName(name, quads, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidQuadLength() {
        // Quad length must be at least 1; if 0, should throw
        byte[] quads = new byte[0];
        symbolTable.addName("", quads, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testNullName() {
        symbolTable.addName(null, null, 1);
    }
}