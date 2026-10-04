package org.apache.commons.math.linear;

import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

public class BigMatrixImplTest {

    private BigMatrixImpl matrix2x3;
    private BigMatrixImpl matrix3x2;
    private BigMatrixImpl squareMatrix;
    private BigMatrixImpl rowMatrix;
    private BigMatrixImpl colMatrix;
    private BigMatrixImpl emptyMatrix;

    @Before
    public void setUp() {
        // 2x3 matrix
        BigDecimal[][] data2x3 = {
            { new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("3.0") },
            { new BigDecimal("4.0"), new BigDecimal("5.0"), new BigDecimal("6.0") }
        };
        matrix2x3 = new BigMatrixImpl(data2x3);

        // 3x2 matrix
        BigDecimal[][] data3x2 = {
            { new BigDecimal("7.0"), new BigDecimal("8.0") },
            { new BigDecimal("9.0"), new BigDecimal("10.0") },
            { new BigDecimal("11.0"), new BigDecimal("12.0") }
        };
        matrix3x2 = new BigMatrixImpl(data3x2);

        // 3x3 square matrix
        BigDecimal[][] squareData = {
            { new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("3.0") },
            { new BigDecimal("4.0"), new BigDecimal("5.0"), new BigDecimal("6.0") },
            { new BigDecimal("7.0"), new BigDecimal("8.0"), new BigDecimal("9.0") }
        };
        squareMatrix = new BigMatrixImpl(squareData);

        // 1x3 row matrix
        BigDecimal[][] rowData = {
            { new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("3.0") }
        };
        rowMatrix = new BigMatrixImpl(rowData);

        // 3x1 column matrix
        BigDecimal[][] colData = {
            { new BigDecimal("1.0") },
            { new BigDecimal("2.0") },
            { new BigDecimal("3.0") }
        };
        colMatrix = new BigMatrixImpl(colData);

        // 0x0 empty matrix (if allowed)
        BigDecimal[][] emptyData = {};
        emptyMatrix = new BigMatrixImpl(emptyData);
    }

    // ---------- Constructor tests ----------
    @Test(expected = NullPointerException.class)
    public void testConstructorNullData() {
        new BigMatrixImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNonRectangular() {
        BigDecimal[][] nonRect = {
            { new BigDecimal("1.0"), new BigDecimal("2.0") },
            { new BigDecimal("3.0") }
        };
        new BigMatrixImpl(nonRect);
    }

    @Test
    public void testConstructorCopyFlagTrue() {
        BigDecimal[][] data = { { BigDecimal.ONE, BigDecimal.TEN } };
        BigMatrixImpl m = new BigMatrixImpl(data, true);
        data[0][0] = BigDecimal.ZERO;
        assertEquals("copy should be independent", BigDecimal.ONE, m.getEntry(0, 0));
    }

    @Test
    public void testConstructorCopyFlagFalse() {
        BigDecimal[][] data = { { BigDecimal.ONE, BigDecimal.TEN } };
        BigMatrixImpl m = new BigMatrixImpl(data, false);
        data[0][0] = BigDecimal.ZERO;
        assertEquals("no copy should reflect change", BigDecimal.ZERO, m.getEntry(0, 0));
    }

    // ---------- Dimension tests ----------
    @Test
    public void testGetRowDimension() {
        assertEquals(2, matrix2x3.getRowDimension());
        assertEquals(3, matrix3x2.getRowDimension());
        assertEquals(3, squareMatrix.getRowDimension());
        assertEquals(1, rowMatrix.getRowDimension());
        assertEquals(3, colMatrix.getRowDimension());
        assertEquals(0, emptyMatrix.getRowDimension());
    }

    @Test
    public void testGetColumnDimension() {
        assertEquals(3, matrix2x3.getColumnDimension());
        assertEquals(2, matrix3x2.getColumnDimension());
        assertEquals(3, squareMatrix.getColumnDimension());
        assertEquals(3, rowMatrix.getColumnDimension());
        assertEquals(1, colMatrix.getColumnDimension());
        assertEquals(0, emptyMatrix.getColumnDimension());
    }

    // ---------- getEntry / setEntry tests ----------
    @Test
    public void testGetEntryValid() {
        assertEquals(new BigDecimal("1.0"), matrix2x3.getEntry(0, 0));
        assertEquals(new BigDecimal("6.0"), matrix2x3.getEntry(1, 2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetEntryRowNegative() {
        matrix2x3.getEntry(-1, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetEntryColNegative() {
        matrix2x3.getEntry(0, -1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetEntryRowTooHigh() {
        matrix2x3.getEntry(2, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetEntryColTooHigh() {
        matrix2x3.getEntry(0, 3);
    }

    @Test
    public void testSetEntry() {
        matrix2x3.setEntry(0, 0, new BigDecimal("99.0"));
        assertEquals(new BigDecimal("99.0"), matrix2x3.getEntry(0, 0));
        // restore for other tests
        matrix2x3.setEntry(0, 0, new BigDecimal("1.0"));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testSetEntryInvalid() {
        matrix2x3.setEntry(5, 5, BigDecimal.ONE);
    }

    // ---------- add / subtract tests ----------
    @Test
    public void testAddCompatible() {
        BigMatrixImpl other = new BigMatrixImpl(new BigDecimal[][] {
            { new BigDecimal("0.5"), new BigDecimal("1.5"), new BigDecimal("2.5") },
            { new BigDecimal("3.5"), new BigDecimal("4.5"), new BigDecimal("5.5") }
        });
        BigMatrixImpl result = (BigMatrixImpl) matrix2x3.add(other);
        assertEquals(new BigDecimal("1.5"), result.getEntry(0, 0));
        assertEquals(new BigDecimal("3.5"), result.getEntry(0, 1));
        assertEquals(new BigDecimal("5.5"), result.getEntry(0, 2));
        assertEquals(new BigDecimal("7.5"), result.getEntry(1, 0));
        assertEquals(new BigDecimal("9.5"), result.getEntry(1, 1));
        assertEquals(new BigDecimal("11.5"), result.getEntry(1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddIncompatibleDimensions() {
        matrix2x3.add(squareMatrix);
    }

    @Test
    public void testSubtractCompatible() {
        BigMatrixImpl other = new BigMatrixImpl(new BigDecimal[][] {
            { BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE },
            { BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE }
        });
        BigMatrixImpl result = (BigMatrixImpl) matrix2x3.subtract(other);
        assertEquals(new BigDecimal("0.0"), result.getEntry(0, 0));
        assertEquals(new BigDecimal("1.0"), result.getEntry(0, 1));
        assertEquals(new BigDecimal("2.0"), result.getEntry(0, 2));
        assertEquals(new BigDecimal("3.0"), result.getEntry(1, 0));
        assertEquals(new BigDecimal("4.0"), result.getEntry(1, 1));
        assertEquals(new BigDecimal("5.0"), result.getEntry(1, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractIncompatibleDimensions() {
        matrix2x3.subtract(colMatrix);
    }

    // ---------- multiply tests ----------
    @Test
    public void testMultiplyCompatible() {
        // 2x3 * 3x2 = 2x2
        BigMatrixImpl result = (BigMatrixImpl) matrix2x3.multiply(matrix3x2);
        assertEquals(2, result.getRowDimension());
        assertEquals(2, result.getColumnDimension());
        // expected: row0: 1*7+2*9+3*11 = 7+18+33=58, 1*8+2*10+3*12=8+20+36=64
        // row1: 4*7+5*9+6*11=28+45+66=139, 4*8+5*10+6*12=32+50+72=154
        assertEquals(new BigDecimal("58"), result.getEntry(0, 0));
        assertEquals(new BigDecimal("64"), result.getEntry(0, 1));
        assertEquals(new BigDecimal("139"), result.getEntry(1, 0));
        assertEquals(new BigDecimal("154"), result.getEntry(1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyIncompatibleDimensions() {
        matrix2x3.multiply(squareMatrix);
    }

    // ---------- transpose tests ----------
    @Test
    public void testTranspose() {
        BigMatrixImpl transposed = (BigMatrixImpl) matrix2x3.transpose();
        assertEquals(3, transposed.getRowDimension());
        assertEquals(2, transposed.getColumnDimension());
        assertEquals(new BigDecimal("1.0"), transposed.getEntry(0, 0));
        assertEquals(new BigDecimal("4.0"), transposed.getEntry(0, 1));
        assertEquals(new BigDecimal("2.0"), transposed.getEntry(1, 0));
        assertEquals(new BigDecimal("5.0"), transposed.getEntry(1, 1));
        assertEquals(new BigDecimal("3.0"), transposed.getEntry(2, 0));
        assertEquals(new BigDecimal("6.0"), transposed.getEntry(2, 1));
    }

    // ---------- getData tests ----------
    @Test
    public void testGetDataReturnsCopy() {
        BigDecimal[][] data = matrix2x3.getData();
        data[0][0] = BigDecimal.ZERO;
        assertEquals("original should not be modified", new BigDecimal("1.0"), matrix2x3.getEntry(0, 0));
    }

    // ---------- getRow / getColumn tests ----------
    @Test
    public void testGetRow() {
        BigDecimal[] row = matrix2x3.getRow(0);
        assertArrayEquals(new BigDecimal[] { new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("3.0") }, row);
        // modify returned row should not affect matrix
        row[0] = BigDecimal.ZERO;
        assertEquals(new BigDecimal("1.0"), matrix2x3.getEntry(0, 0));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetRowInvalidIndex() {
        matrix2x3.getRow(5);
    }

    @Test
    public void testGetColumn() {
        BigDecimal[] col = matrix2x3.getColumn(1);
        assertArrayEquals(new BigDecimal[] { new BigDecimal("2.0"), new BigDecimal("5.0") }, col);
        col[0] = BigDecimal.ZERO;
        assertEquals(new BigDecimal("2.0"), matrix2x3.getEntry(0, 1));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetColumnInvalidIndex() {
        matrix2x3.getColumn(10);
    }

    // ---------- isSquare tests ----------
    @Test
    public void testIsSquare() {
        assertTrue(squareMatrix.isSquare());
        assertFalse(matrix2x3.isSquare());
        assertFalse(rowMatrix.isSquare());
        assertFalse(colMatrix.isSquare());
        // empty matrix: 0x0 is square? Typically yes.
        assertTrue(emptyMatrix.isSquare());
    }

    // ---------- getTrace tests ----------
    @Test
    public void testGetTraceSquare() {
        assertEquals(new BigDecimal("15.0"), squareMatrix.getTrace()); // 1+5+9=15
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTraceNonSquare() {
        matrix2x3.getTrace();
    }

    // ---------- operate tests ----------
    @Test
    public void testOperateValidInput() {
        BigDecimal[] input = { new BigDecimal("1.0"), new BigDecimal("2.0"), new BigDecimal("3.0") };
        BigDecimal[] result = matrix2x3.operate(input);
        // expected: row0: 1*1 + 2*2 + 3*3 = 1+4+9=14
        // row1: 4*1 + 5*2 + 6*3 = 4+10+18=32
        assertEquals(2, result.length);
        assertEquals(new BigDecimal("14"), result[0]);
        assertEquals(new BigDecimal("32"), result[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOperateInvalidInputLength() {
        // input length != column dimension (3)
        BigDecimal[] input = { BigDecimal.ONE, BigDecimal.TEN };
        matrix2x3.operate(input);
    }

    @Test(expected = NullPointerException.class)
    public void testOperateNullInput() {
        matrix2x3.operate(null);
    }

    // ---------- preMultiply tests ----------
    @Test
    public void testPreMultiplyValidInput() {
        BigDecimal[] input = { new BigDecimal("1.0"), new BigDecimal("2.0") };
        BigDecimal[] result = matrix2x3.preMultiply(input);
        // preMultiply: v * M, where v is row vector of length rowDimension (2)
        // result length = columnDimension (3)
        // result[0] = 1*1 + 2*4 = 1+8=9
        // result[1] = 1*2 + 2*5 = 2+10=12
        // result[2] = 1*3 + 2*6 = 3+12=15
        assertEquals(3, result.length);
        assertEquals(new BigDecimal("9"), result[0]);
        assertEquals(new BigDecimal("12"), result[1]);
        assertEquals(new BigDecimal("15"), result[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPreMultiplyInvalidInputLength() {
        BigDecimal[] input = { BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE };
        matrix2x3.preMultiply(input);
    }

    @Test(expected = NullPointerException.class)
    public void testPreMultiplyNullInput() {
        matrix2x3.preMultiply(null);
    }

    // ---------- Edge cases for empty matrix ----------
    @Test
    public void testEmptyMatrixOperate() {
        BigDecimal[] input = {};
        BigDecimal[] result = emptyMatrix.operate(input);
        assertEquals(0, result.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMatrixOperateInvalidInput() {
        BigDecimal[] input = { BigDecimal.ONE };
        emptyMatrix.operate(input);
    }

    @Test
    public void testEmptyMatrixPreMultiply() {
        BigDecimal[] input = {};
        BigDecimal[] result = emptyMatrix.preMultiply(input);
        assertEquals(0, result.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMatrixPreMultiplyInvalidInput() {
        BigDecimal[] input = { BigDecimal.ONE };
        emptyMatrix.preMultiply(input);
    }

    // ---------- Additional coverage: getRowDimension / getColumnDimension on empty ----------
    @Test
    public void testEmptyMatrixDimensions() {
        assertEquals(0, emptyMatrix.getRowDimension());
        assertEquals(0, emptyMatrix.getColumnDimension());
    }

    // ---------- Test that add/subtract/multiply return new instances ----------
    @Test
    public void testAddReturnsNewInstance() {
        BigMatrixImpl other = new BigMatrixImpl(new BigDecimal[][] {
            { BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE },
            { BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE }
        });
        BigMatrixImpl result = (BigMatrixImpl) matrix2x3.add(other);
        assertNotSame(matrix2x3, result);
        assertNotSame(other, result);
    }

    @Test
    public void testSubtractReturnsNewInstance() {
        BigMatrixImpl other = new BigMatrixImpl(new BigDecimal[][] {
            { BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE },
            { BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE }
        });
        BigMatrixImpl result = (BigMatrixImpl) matrix2x3.subtract(other);
        assertNotSame(matrix2x3, result);
        assertNotSame(other, result);
    }

    @Test
    public void testMultiplyReturnsNewInstance() {
        BigMatrixImpl result = (BigMatrixImpl) matrix2x3.multiply(matrix3x2);
        assertNotSame(matrix2x3, result);
        assertNotSame(matrix3x2, result);
    }

    @Test
    public void testTransposeReturnsNewInstance() {
        BigMatrixImpl transposed = (BigMatrixImpl) matrix2x3.transpose();
        assertNotSame(matrix2x3, transposed);
    }
}