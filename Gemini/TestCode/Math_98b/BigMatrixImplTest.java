package org.apache.commons.math.linear;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class BigMatrixImplTest {

    @Test
    public void testBug98MultiplyDimensionMismatch() {
        // Defects4J Math 98 bug: operate(BigDecimal[]) uses rows instead of cols,
        // or multiply(BigMatrixImpl) dimension checks have flaws.
        // Specifically, check operate with wrong dimension array.
        BigMatrixImpl m = new BigMatrixImpl(new double[][]{
                {1.0, 2.0},
                {3.0, 4.0}
        });

        try {
            m.operate(new double[]{1.0}); // Mismatched dimension
            Assert.fail("Expecting IllegalArgumentException due to dimension mismatch");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testBug98MultiplyMatrixDimension() {
        BigMatrixImpl a = new BigMatrixImpl(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });
        
        BigMatrixImpl b = new BigMatrixImpl(new double[][]{
                {1.0, 2.0},
                {3.0, 4.0}
        }); // 2x2, whereas a is 2x3. b should have 3 rows to multiply a * b, or a.multiply(b) where a is 2x3 and b is 3x2.
        
        // Let's test a (2x3) * b (3x2) -> result 2x2
        BigMatrixImpl bCorrect = new BigMatrixImpl(new double[][]{
                {1.0, 2.0},
                {3.0, 4.0},
                {5.0, 6.0}
        });
        
        BigMatrix result = a.multiply(bCorrect);
        Assert.assertEquals(2, result.getRowDimension());
        Assert.assertEquals(2, result.getColumnDimension());

        // Now test incompatible dimensions for multiplication
        try {
            a.multiply(b); // 2x3 multiplied by 2x2 should fail
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testConstructorsAndBasics() {
        BigMatrixImpl m1 = new BigMatrixImpl();
        Assert.assertEquals(0, m1.getRowDimension());
        Assert.assertEquals(0, m1.getColumnDimension());

        BigDecimal[][] data = new BigDecimal[][]{
                {BigDecimal.valueOf(1), BigDecimal.valueOf(2)},
                {BigDecimal.valueOf(3), BigDecimal.valueOf(4)}
        };
        BigMatrixImpl m2 = new BigMatrixImpl(data);
        Assert.assertEquals(2, m2.getRowDimension());
        Assert.assertEquals(2, m2.getColumnDimension());

        // Copy constructor / array input
        double[] l1 = {1.0, 2.0};
        double[] l2 = {3.0, 4.0};
        BigMatrixImpl m3 = new BigMatrixImpl(new double[][]{l1, l2});
        Assert.assertEquals(2, m3.getRowDimension());
        Assert.assertEquals(2, m3.getColumnDimension());

        // Test copy-in / copy-out flags
        BigMatrixImpl m4 = new BigMatrixImpl(data, false);
        Assert.assertEquals(2, m4.getRowDimension());

        // Test column/row vectors
        BigDecimal[] v = new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(2)};
        BigMatrixImpl m5 = new BigMatrixImpl(v);
        Assert.assertEquals(2, m5.getRowDimension());
        Assert.assertEquals(1, m5.getColumnDimension());
    }

    @Test
    public void testOperations() {
        BigMatrixImpl m = new BigMatrixImpl(new double[][]{
                {1.0, 2.0},
                {3.0, 4.0}
        });

        BigMatrix copy = m.copy();
        Assert.assertEquals(m, copy);

        BigMatrix addMat = m.add(m);
        Assert.assertEquals(BigDecimal.valueOf(2.0), addMat.getEntry(0, 0));

        BigMatrix subMat = addMat.subtract(m);
        Assert.assertEquals(m.getEntry(0, 0), subMat.getEntry(0, 0));

        BigMatrix scalarAdd = m.scalarAdd(BigDecimal.valueOf(1.0));
        Assert.assertEquals(BigDecimal.valueOf(2.0), scalarAdd.getEntry(0, 0));

        BigMatrix scalarMul = m.scalarMultiply(BigDecimal.valueOf(2.0));
        Assert.assertEquals(BigDecimal.valueOf(2.0), scalarMul.getEntry(0, 0));

        BigDecimal[] opVec = m.operate(new double[]{1.0, 1.0});
        Assert.assertEquals(2, opVec.length);

        BigDecimal[] opVecBd = m.operate(new BigDecimal[]{BigDecimal.ONE, BigDecimal.ONE});
        Assert.assertEquals(2, opVecBd.length);

        BigDecimal[] preOp = m.preMultiply(new double[]{1.0, 1.0});
        Assert.assertEquals(2, preOp.length);

        BigMatrix transpose = m.transpose();
        Assert.assertEquals(2, transpose.getRowDimension());
        Assert.assertEquals(2, transpose.getColumnDimension());

        String str = m.toString();
        Assert.assertNotNull(str);

        int[] rWind = {0, 1};
        int[] cWind = {0, 1};
        BigMatrix sub = m.getSubMatrix(rWind, cWind);
        Assert.assertEquals(2, sub.getRowDimension());

        BigMatrix sub2 = m.getSubMatrix(0, 1, 0, 1);
        Assert.assertEquals(2, sub2.getRowDimension());

        BigMatrix rowMat = m.getRowMatrix(0);
        Assert.assertEquals(1, rowMat.getRowDimension());

        BigMatrix colMat = m.getColumnMatrix(0);
        Assert.assertEquals(2, colMat.getRowDimension());

        double[] rArr = m.getRow(0);
        Assert.assertEquals(2, rArr.length);

        BigDecimal[] rArrBd = m.getRowBigDecimal(0);
        Assert.assertEquals(2, rArrBd.length);

        double[] cArr = m.getColumn(0);
        Assert.assertEquals(2, cArr.length);

        BigDecimal[] cArrBd = m.getColumnBigDecimal(0);
        Assert.assertEquals(2, cArrBd.length);

        BigDecimal entry = m.getEntry(0, 0);
        Assert.assertEquals(BigDecimal.valueOf(1.0), entry);

        m.setEntry(0, 0, BigDecimal.valueOf(10.0));
        Assert.assertEquals(BigDecimal.valueOf(10.0), m.getEntry(0, 0));

        m.setRow(0, new double[]{5.0, 6.0});
        Assert.assertEquals(BigDecimal.valueOf(5.0), m.getEntry(0, 0));

        m.setRow(0, new BigDecimal[]{BigDecimal.valueOf(7.0), BigDecimal.valueOf(8.0)});
        Assert.assertEquals(BigDecimal.valueOf(7.0), m.getEntry(0, 0));

        m.setColumn(0, new double[]{9.0, 10.0});
        Assert.assertEquals(BigDecimal.valueOf(9.0), m.getEntry(0, 0));

        m.setColumn(0, new BigDecimal[]{BigDecimal.valueOf(11.0), BigDecimal.valueOf(12.0)});
        Assert.assertEquals(BigDecimal.valueOf(11.0), m.getEntry(0, 0));

        BigMatrix setSub = new BigMatrixImpl(new double[][]{{99.0}});
        m.setSubMatrix(setSub.getData(), 0, 0);
        Assert.assertEquals(BigDecimal.valueOf(99.0), m.getEntry(0, 0));

        BigMatrix augmented = m.cumSum();
        Assert.assertNotNull(augmented);
    }

    @Test
    public void testLUDecomposition() {
        BigMatrixImpl m = new BigMatrixImpl(new double[][]{
                {2.0, -1.0, 0.0},
                {-1.0, 2.0, -1.0},
                {0.0, -1.0, 2.0}
        });

        BigDecimal det = m.getDeterminant();
        Assert.assertNotNull(det);

        BigDecimal[] sol = m.solve(new double[]{1.0, 0.0, 1.0});
        Assert.assertEquals(3, sol.length);

        BigDecimal[] solBd = m.solve(new BigDecimal[]{BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE});
        Assert.assertEquals(3, solBd.length);

        BigMatrix solMat = m.solve(new BigMatrixImpl(new double[][]{{1.0}, {0.0}, {1.0}}));
        Assert.assertEquals(3, solMat.getRowDimension());
    }

    @Test
    public void testEqualsAndHashCode() {
        BigMatrixImpl m1 = new BigMatrixImpl(new double[][]{{1.0}});
        BigMatrixImpl m2 = new BigMatrixImpl(new double[][]{{1.0}});
        BigMatrixImpl m3 = new BigMatrixImpl(new double[][]{{2.0}});

        Assert.assertTrue(m1.equals(m1));
        Assert.assertTrue(m1.equals(m2));
        Assert.assertFalse(m1.equals(m3));
        Assert.assertFalse(m1.equals(null));
        Assert.assertFalse(m1.equals("SomeString"));

        Assert.assertEquals(m1.hashCode(), m2.hashCode());
        
        BigMatrixImpl empty1 = new BigMatrixImpl();
        BigMatrixImpl empty2 = new BigMatrixImpl();
        Assert.assertTrue(empty1.equals(empty2));
        Assert.assertEquals(empty1.hashCode(), empty2.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        BigMatrixImpl m1 = new BigMatrixImpl(new double[][]{{1.0}});
        BigMatrixImpl m2 = new BigMatrixImpl(new double[][]{{1.0, 2.0}});
        m1.add(m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractDimensionMismatch() {
        BigMatrixImpl m1 = new BigMatrixImpl(new double[][]{{1.0}});
        BigMatrixImpl m2 = new BigMatrixImpl(new double[][]{{1.0, 2.0}});
        m1.subtract(m2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLUDecompositionNonSquare() {
        BigMatrixImpl m1 = new BigMatrixImpl(new double[][]{{1.0, 2.0}});
        m1.getDeterminant();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSolveDimensionMismatch() {
        BigMatrixImpl m1 = new BigMatrixImpl(new double[][]{
                {1.0, 2.0},
                {3.0, 4.0}
        });
        m1.solve(new double[]{1.0});
    }

    @Test(expected = MatrixIndexException.class)
    public void testGetEntryOutOfBounds() {
        BigMatrixImpl m1 = new BigMatrixImpl(new double[][]{{1.0}});
        m1.getEntry(5, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullData() {
        new BigMatrixImpl((BigDecimal[][]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyRow() {
        new BigMatrixImpl(new BigDecimal[][]{{}});
    }
}