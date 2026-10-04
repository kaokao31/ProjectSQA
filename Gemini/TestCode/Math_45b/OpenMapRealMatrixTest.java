package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class OpenMapRealMatrixTest {

    @Test
    public void testConstructorsAndBasicOperations() {
        // Test dimensions constructor
        OpenMapRealMatrix matrix1 = new OpenMapRealMatrix(3, 4);
        assertEquals(3, matrix1.getRowDimension());
        assertEquals(4, matrix1.getColumnDimension());

        // Test invalid dimensions (should throw NotStrictlyPositiveException or NumberIsTooLargeException)
        try {
            new OpenMapRealMatrix(0, 4);
            fail("Expected exception for non-positive row dimension");
        } catch (Exception e) {
            // Expected
        }

        try {
            new OpenMapRealMatrix(3, 0);
            fail("Expected exception for non-positive column dimension");
        } catch (Exception e) {
            // Expected
        }

        // Test maximum allowed size or large size constructor if applicable
        // Math-45 specifically deals with row/column dimension limits (Integer.MAX_VALUE / long conversion issues)
        try {
            // This might trigger NumberIsTooLargeException in Commons Math 45 if dimensions exceed capacity
            int largeDim = Integer.MAX_VALUE;
            new OpenMapRealMatrix(largeDim, largeDim);
        } catch (Exception e) {
            // Expected for huge dimensions
        }

        // Test copy constructor
        OpenMapRealMatrix matrix2 = new OpenMapRealMatrix(matrix1);
        assertEquals(3, matrix2.getRowDimension());
        assertEquals(4, matrix2.getColumnDimension());

        // Test createMatrix
        RealMatrix matrix3 = matrix1.createMatrix(2, 2);
        assertEquals(2, matrix3.getRowDimension());
        assertEquals(2, matrix3.getColumnDimension());

        try {
            matrix1.createMatrix(0, 2);
            fail("Expected exception");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testCopy() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(0, 0, 5.0);
        OpenMapRealMatrix copy = matrix.copy();
        assertEquals(5.0, copy.getEntry(0, 0), 1.0e-12);
        assertNotSame(matrix, copy);
    }

    @Test
    public void testAdd() {
        OpenMapRealMatrix matrix1 = new OpenMapRealMatrix(2, 2);
        matrix1.setEntry(0, 0, 1.0);

        OpenMapRealMatrix matrix2 = new OpenMapRealMatrix(2, 2);
        matrix2.setEntry(0, 0, 2.0);

        RealMatrix sum = matrix1.add(matrix2);
        assertEquals(3.0, sum.getEntry(0, 0), 1.0e-12);

        // Dimension mismatch
        OpenMapRealMatrix matrix3 = new OpenMapRealMatrix(3, 3);
        try {
            matrix1.add(matrix3);
            fail("Expected MatrixDimensionMismatchException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testSubtract() {
        OpenMapRealMatrix matrix1 = new OpenMapRealMatrix(2, 2);
        matrix1.setEntry(0, 0, 3.0);

        OpenMapRealMatrix matrix2 = new OpenMapRealMatrix(2, 2);
        matrix2.setEntry(0, 0, 1.0);

        RealMatrix diff = matrix1.subtract(matrix2);
        assertEquals(2.0, diff.getEntry(0, 0), 1.0e-12);

        // Dimension mismatch
        OpenMapRealMatrix matrix3 = new OpenMapRealMatrix(3, 3);
        try {
            matrix1.subtract(matrix3);
            fail("Expected MatrixDimensionMismatchException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testMultiply() {
        OpenMapRealMatrix matrix1 = new OpenMapRealMatrix(2, 2);
        matrix1.setEntry(0, 0, 2.0);
        matrix1.setEntry(1, 1, 3.0);

        OpenMapRealMatrix matrix2 = new OpenMapRealMatrix(2, 2);
        matrix2.setEntry(0, 0, 4.0);
        matrix2.setEntry(1, 1, 5.0);

        RealMatrix prod = matrix1.multiply(matrix2);
        assertEquals(8.0, prod.getEntry(0, 0), 1.0e-12);
        assertEquals(15.0, prod.getEntry(1, 1), 1.0e-12);

        // Dimension mismatch for multiplication
        OpenMapRealMatrix matrix3 = new OpenMapRealMatrix(3, 3);
        try {
            matrix1.multiply(matrix3);
            fail("Expected DimensionMismatchException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testMultiplyOpenMapRealMatrix() {
        OpenMapRealMatrix matrix1 = new OpenMapRealMatrix(2, 2);
        matrix1.setEntry(0, 0, 2.0);

        OpenMapRealMatrix matrix2 = new OpenMapRealMatrix(2, 2);
        matrix2.setEntry(0, 0, 4.0);

        OpenMapRealMatrix prod = matrix1.multiply(matrix2);
        assertEquals(8.0, prod.getEntry(0, 0), 1.0e-12);
    }

    @Test
    public void testSetAndGetEntry() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(3, 3);
        matrix.setEntry(1, 2, 42.0);
        assertEquals(42.0, matrix.getEntry(1, 2), 1.0e-12);
        assertEquals(0.0, matrix.getEntry(0, 0), 1.0e-12);

        // Test setting zero (should remove or keep sparse)
        matrix.setEntry(1, 2, 0.0);
        assertEquals(0.0, matrix.getEntry(1, 2), 1.0e-12);

        // Out of bounds
        try {
            matrix.getEntry(-1, 0);
            fail("Expected OutOfRangeException");
        } catch (Exception e) {
            // Expected
        }

        try {
            matrix.setEntry(3, 0, 1.0);
            fail("Expected OutOfRangeException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testAddToEntryAndMultiplyEntry() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.addToEntry(0, 0, 5.0);
        assertEquals(5.0, matrix.getEntry(0, 0), 1.0e-12);

        matrix.addToEntry(0, 0, 2.0);
        assertEquals(7.0, matrix.getEntry(0, 0), 1.0e-12);

        matrix.multiplyEntry(0, 0, 3.0);
        assertEquals(21.0, matrix.getEntry(0, 0), 1.0e-12);

        // Out of bounds checks
        try {
            matrix.addToEntry(5, 5, 1.0);
            fail("Expected OutOfRangeException");
        } catch (Exception e) {
            // Expected
        }

        try {
            matrix.multiplyEntry(5, 5, 2.0);
            fail("Expected OutOfRangeException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testGetRowAndColumnVector() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(0, 0, 1.0);
        matrix.setEntry(0, 1, 2.0);

        RealVector row = matrix.getRowVector(0);
        assertEquals(2, row.getDimension());
        assertEquals(1.0, row.getEntry(0), 1.0e-12);
        assertEquals(2.0, row.getEntry(1), 1.0e-12);

        RealVector col = matrix.getColumnVector(1);
        assertEquals(2, col.getDimension());
        assertEquals(2.0, col.getEntry(0), 1.0e-12);
        assertEquals(0.0, col.getEntry(1), 1.0e-12);

        // Out of bounds
        try {
            matrix.getRowVector(5);
            fail("Expected OutOfRangeException");
        } catch (Exception e) {
            // Expected
        }

        try {
            matrix.getColumnVector(5);
            fail("Expected OutOfRangeException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testGetRowAndColumnArray() {
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(2, 2);
        matrix.setEntry(1, 0, 3.0);
        matrix.setEntry(1, 1, 4.0);

        double[] row = matrix.getRow(1);
        assertEquals(2, row.length);
        assertEquals(3.0, row[0], 1.0e-12);
        assertEquals(4.0, row[1], 1.0e-12);

        double[] col = matrix.getColumn(0);
        assertEquals(2, col.length);
        assertEquals(0.0, col[0], 1.0e-12);
        assertEquals(3.0, col[1], 1.0e-12);

        // Out of bounds
        try {
            matrix.getRow(10);
            fail("Expected OutOfRangeException");
        } catch (Exception e) {
            // Expected
        }

        try {
            matrix.getColumn(10);
            fail("Expected OutOfRangeException");
        } catch (Exception e) {
            // Expected
        }
    }
}