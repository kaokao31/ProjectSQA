package org.apache.commons.math.linear;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OpenMapRealMatrixTest {

    private static final double EPSILON = 1.0e-12;

    // Common matrix used across tests
    private OpenMapRealMatrix testMatrix;

    @Before
    public void setUp() {
        testMatrix = createBasicMatrix();
    }

    // Helper to create a 3x3 matrix with known entries
    private OpenMapRealMatrix createBasicMatrix() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.setEntry(0, 0, 1.0);
        m.setEntry(0, 1, 2.0);
        m.setEntry(0, 2, 3.0);
        m.setEntry(1, 0, 4.0);
        m.setEntry(1, 1, 5.0);
        m.setEntry(1, 2, 6.0);
        m.setEntry(2, 0, 7.0);
        m.setEntry(2, 1, 8.0);
        m.setEntry(2, 2, 9.0);
        return m;
    }

    // ========== Constructor Tests ==========

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeRow() {
        new OpenMapRealMatrix(-1, 10);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeColumn() {
        new OpenMapRealMatrix(10, -1);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroRow() {
        new OpenMapRealMatrix(0, 10);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroColumn() {
        new OpenMapRealMatrix(10, 0);
    }

    @Test
    public void testConstructorValid() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(5, 5);
        assertEquals(5, m.getRowDimension());
        assertEquals(5, m.getColumnDimension());
        // all entries should be zero
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                assertEquals(0.0, m.getEntry(i, j), EPSILON);
            }
        }
    }

    // ========== Dimension Tests ==========

    @Test
    public void testGetRowDimension() {
        assertEquals(3, testMatrix.getRowDimension());
    }

    @Test
    public void testGetColumnDimension() {
        assertEquals(3, testMatrix.getColumnDimension());
    }

    // ========== getEntry / setEntry Tests ==========

    @Test
    public void testGetEntry() {
        assertEquals(1.0, testMatrix.getEntry(0, 0), EPSILON);
        assertEquals(5.0, testMatrix.getEntry(1, 1), EPSILON);
        assertEquals(9.0, testMatrix.getEntry(2, 2), EPSILON);
    }

    @Test
    public void testGetEntryZeroDefault() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(5, 5);
        assertEquals(0.0, m.getEntry(2, 3), EPSILON);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetEntryRowNegative() {
        testMatrix.getEntry(-1, 0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetEntryRowTooHigh() {
        testMatrix.getEntry(3, 0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetEntryColumnNegative() {
        testMatrix.getEntry(0, -1);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetEntryColumnTooHigh() {
        testMatrix.getEntry(0, 3);
    }

    @Test
    public void testSetEntry() {
        testMatrix.setEntry(1, 2, 99.0);
        assertEquals(99.0, testMatrix.getEntry(1, 2), EPSILON);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntryOutOfBoundsRowLow() {
        testMatrix.setEntry(-1, 0, 5.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntryOutOfBoundsRowHigh() {
        testMatrix.setEntry(3, 0, 5.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntryOutOfBoundsColumnLow() {
        testMatrix.setEntry(0, -1, 5.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testSetEntryOutOfBoundsColumnHigh() {
        testMatrix.setEntry(0, 3, 5.0);
    }

    @Test
    public void testSetEntryOverwrite() {
        testMatrix.setEntry(0, 0, -1.0);
        assertEquals(-1.0, testMatrix.getEntry(0, 0), EPSILON);
    }

    @Test
    public void testSetEntryZeroValueRemoves() {
        testMatrix.setEntry(0, 0, 0.0);
        assertEquals(0.0, testMatrix.getEntry(0, 0), EPSILON);
        // sparse matrix should not store zero entries; should revert to default
        // No direct way to verify internal storage, but getEntry works.
    }

    // ========== addToEntry Tests ==========

    @Test
    public void testAddToEntryExisting() {
        testMatrix.addToEntry(0, 0, 10.0);
        assertEquals(11.0, testMatrix.getEntry(0, 0), EPSILON);
    }

    @Test
    public void testAddToEntryNonExisting() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.addToEntry(1, 1, 7.0);
        assertEquals(7.0, m.getEntry(1, 1), EPSILON);
    }

    @Test
    public void testAddToEntryMultipleTimes() {
        testMatrix.addToEntry(2, 2, 1.0);
        testMatrix.addToEntry(2, 2, 1.0);
        assertEquals(11.0, testMatrix.getEntry(2, 2), EPSILON);
    }

    @Test(expected = MatrixIndexException.class)
    public void testAddToEntryOutOfBoundsRow() {
        testMatrix.addToEntry(-1, 0, 5.0);
    }

    @Test(expected = MatrixIndexException.class)
    public void testAddToEntryOutOfBoundsColumn() {
        testMatrix.addToEntry(0, 5, 5.0);
    }

    // ========== multiply Tests ==========

    @Test
    public void testMultiplyByIdentity() {
        RealMatrix identity = new OpenMapRealMatrix(3, 3);
        // set identity
        identity.setEntry(0, 0, 1.0);
        identity.setEntry(1, 1, 1.0);
        identity.setEntry(2, 2, 1.0);
        RealMatrix product = testMatrix.multiply(identity);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(testMatrix.getEntry(i, j), product.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test
    public void testMultiplyWithDifferentDimensions() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 3);
        a.setEntry(0, 0, 1);
        a.setEntry(0, 1, 2);
        a.setEntry(0, 2, 3);
        a.setEntry(1, 0, 4);
        a.setEntry(1, 1, 5);
        a.setEntry(1, 2, 6);

        OpenMapRealMatrix b = new OpenMapRealMatrix(3, 2);
        b.setEntry(0, 0, 7);
        b.setEntry(0, 1, 8);
        b.setEntry(1, 0, 9);
        b.setEntry(1, 1, 10);
        b.setEntry(2, 0, 11);
        b.setEntry(2, 1, 12);

        RealMatrix product = a.multiply(b);
        assertEquals(2, product.getRowDimension());
        assertEquals(2, product.getColumnDimension());
        assertEquals(58.0, product.getEntry(0, 0), EPSILON);  // 1*7 + 2*9 + 3*11 = 7+18+33=58
        assertEquals(64.0, product.getEntry(0, 1), EPSILON);  // 1*8 + 2*10 + 3*12 = 8+20+36=64
        assertEquals(139.0, product.getEntry(1, 0), EPSILON); // 4*7 + 5*9 + 6*11 = 28+45+66=139
        assertEquals(154.0, product.getEntry(1, 1), EPSILON); // 4*8 + 5*10 + 6*12 = 32+50+72=154
    }

    @Test(expected = MatrixDimensionMismatchException.class)
    public void testMultiplyIncompatibleDimensions() {
        OpenMapRealMatrix a = new OpenMapRealMatrix(2, 3);
        OpenMapRealMatrix b = new OpenMapRealMatrix(2, 2);
        a.multiply(b);
    }

    // ========== copy Tests ==========

    @Test
    public void testCopy() {
        OpenMapRealMatrix copy = (OpenMapRealMatrix) testMatrix.copy();
        assertNotNull(copy);
        assertEquals(testMatrix.getRowDimension(), copy.getRowDimension());
        assertEquals(testMatrix.getColumnDimension(), copy.getColumnDimension());
        for (int i = 0; i < testMatrix.getRowDimension(); i++) {
            for (int j = 0; j < testMatrix.getColumnDimension(); j++) {
                assertEquals(testMatrix.getEntry(i, j), copy.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test
    public void testCopyIsIndependent() {
        OpenMapRealMatrix copy = (OpenMapRealMatrix) testMatrix.copy();
        copy.setEntry(0, 0, 100.0);
        assertEquals(1.0, testMatrix.getEntry(0, 0), EPSILON);
    }

    // ========== createMatrix Tests ==========

    @Test
    public void testCreateMatrix() {
        RealMatrix created = testMatrix.createMatrix(4, 5);
        assertEquals(4, created.getRowDimension());
        assertEquals(5, created.getColumnDimension());
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                assertEquals(0.0, created.getEntry(i, j), EPSILON);
            }
        }
    }

    // ========== Walking Tests (basic coverage) ==========

    @Test
    public void testWalkInRowOrder() {
        double[] values = new double[9];
        testMatrix.walkInRowOrder(new DefaultRealMatrixChangingVisitor() {
            int index = 0;
            @Override
            public double visit(int row, int col, double value) {
                values[index++] = value;
                return value;
            }
        });
        assertArrayEquals(new double[]{1,2,3,4,5,6,7,8,9}, values, EPSILON);
    }

    @Test
    public void testWalkInColumnOrder() {
        double[] values = new double[9];
        testMatrix.walkInColumnOrder(new DefaultRealMatrixChangingVisitor() {
            int index = 0;
            @Override
            public double visit(int row, int col, double value) {
                values[index++] = value;
                return value;
            }
        });
        assertArrayEquals(new double[]{1,4,7,2,5,8,3,6,9}, values, EPSILON);
    }

    @Test
    public void testWalkInOptimizedOrder() {
        double[] values = new double[9];
        testMatrix.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            int index = 0;
            @Override
            public double visit(int row, int col, double value) {
                values[index++] = value;
                return value;
            }
        });
        // Optimized order should be row-order for this implementation
        assertArrayEquals(new double[]{1,2,3,4,5,6,7,8,9}, values, EPSILON);
    }

    // ========== hashCode and equals Tests ==========

    @Test
    public void testEqualsSameMatrix() {
        assertTrue(testMatrix.equals(testMatrix));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(testMatrix.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(testMatrix.equals("string"));
    }

    @Test
    public void testEqualsDifferentDimensions() {
        OpenMapRealMatrix other = new OpenMapRealMatrix(2, 3);
        assertFalse(testMatrix.equals(other));
    }

    @Test
    public void testEqualsDifferentEntries() {
        OpenMapRealMatrix other = createBasicMatrix();
        other.setEntry(0, 0, 0.0);
        assertFalse(testMatrix.equals(other));
    }

    @Test
    public void testEqualsSameEntries() {
        OpenMapRealMatrix other = createBasicMatrix();
        assertTrue(testMatrix.equals(other));
    }

    @Test
    public void testHashCodeConsistency() {
        OpenMapRealMatrix other = createBasicMatrix();
        assertEquals(testMatrix.hashCode(), other.hashCode());
    }

    // ========== Edge Cases for Sparse Matrices ==========

    @Test
    public void testLargeSparseMatrix() {
        OpenMapRealMatrix large = new OpenMapRealMatrix(1000, 1000);
        large.setEntry(0, 0, 1.0);
        large.setEntry(500, 500, 2.0);
        large.setEntry(999, 999, 3.0);
        assertEquals(1.0, large.getEntry(0, 0), EPSILON);
        assertEquals(2.0, large.getEntry(500, 500), EPSILON);
        assertEquals(3.0, large.getEntry(999, 999), EPSILON);
        // other entries should be zero
        assertEquals(0.0, large.getEntry(100, 200), EPSILON);
    }

    @Test
    public void testSetEntryZeroThenNonZero() {
        testMatrix.setEntry(1, 1, 0.0);
        assertEquals(0.0, testMatrix.getEntry(1, 1), EPSILON);
        testMatrix.setEntry(1, 1, 5.0);
        assertEquals(5.0, testMatrix.getEntry(1, 1), EPSILON);
    }

    @Test
    public void testAddToEntryResultZero() {
        testMatrix.setEntry(1, 1, 5.0);
        testMatrix.addToEntry(1, 1, -5.0);
        assertEquals(0.0, testMatrix.getEntry(1, 1), EPSILON);
    }

    // ========== Submatrix (if present, but OpenMapRealMatrix only inherits from AbstractRealMatrix which has getSubMatrix) ==========

    @Test
    public void testGetSubMatrix() {
        RealMatrix sub = testMatrix.getSubMatrix(0, 1, 0, 1);
        assertEquals(2, sub.getRowDimension());
        assertEquals(2, sub.getColumnDimension());
        assertEquals(1.0, sub.getEntry(0, 0), EPSILON);
        assertEquals(2.0, sub.getEntry(0, 1), EPSILON);
        assertEquals(4.0, sub.getEntry(1, 0), EPSILON);
        assertEquals(5.0, sub.getEntry(1, 1), EPSILON);
    }

    // ========== Serialization (optional, but useful) ==========

    @Test
    public void testSerializationRoundTrip() throws Exception {
        OpenMapRealMatrix original = createBasicMatrix();
        // serialize to byte array
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        byte[] bytes = baos.toByteArray();

        // deserialize
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        OpenMapRealMatrix deserialized = (OpenMapRealMatrix) ois.readObject();
        ois.close();

        assertEquals(original.getRowDimension(), deserialized.getRowDimension());
        assertEquals(original.getColumnDimension(), deserialized.getColumnDimension());
        for (int i = 0; i < original.getRowDimension(); i++) {
            for (int j = 0; j < original.getColumnDimension(); j++) {
                assertEquals(original.getEntry(i, j), deserialized.getEntry(i, j), EPSILON);
            }
        }
    }

    // ========== Additional Coverage: getRow / getColumn ==========

    @Test
    public void testGetRow() {
        double[] row = testMatrix.getRow(1);
        assertArrayEquals(new double[]{4.0, 5.0, 6.0}, row, EPSILON);
    }

    @Test
    public void testGetColumn() {
        double[] col = testMatrix.getColumn(1);
        assertArrayEquals(new double[]{2.0, 5.0, 8.0}, col, EPSILON);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetRowOutOfBounds() {
        testMatrix.getRow(4);
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetColumnOutOfBounds() {
        testMatrix.getColumn(-1);
    }

    // ========== Test for bug conditions (Defects4J #45) - possibly related to entry removal or iteration ==========

    @Test
    public void testSetEntryAfterRemoval() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(2, 2);
        m.setEntry(0, 0, 10.0);
        m.setEntry(0, 0, 0.0);
        // After setting to zero, the entry should be removed from sparse storage
        // Then set again
        m.setEntry(0, 0, 20.0);
        assertEquals(20.0, m.getEntry(0, 0), EPSILON);
        // Walk to ensure no internal state corruption
        double sum = 0.0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                sum += m.getEntry(i, j);
            }
        }
        assertEquals(20.0, sum, EPSILON);
    }

    @Test
    public void testWalkWithRemovedEntries() {
        OpenMapRealMatrix m = new OpenMapRealMatrix(3, 3);
        m.setEntry(0, 0, 1.0);
        m.setEntry(0, 1, 2.0);
        m.setEntry(0, 2, 3.0);
        m.setEntry(0, 1, 0.0); // remove middle entry
        // walk should only see two entries?
        // Actually walk visits all indices, but value will be zero for removed entry.
        // We'll just check walk doesn't throw.
        double[] values = new double[9];
        m.walkInRowOrder(new DefaultRealMatrixPreservingVisitor() {
            int idx = 0;
            @Override
            public void visit(int row, int col, double value) {
                values[idx++] = value;
            }
        });
        assertEquals(1.0, values[0], EPSILON);
        assertEquals(0.0, values[1], EPSILON); // removed entry becomes zero
        assertEquals(3.0, values[2], EPSILON);
    }
}