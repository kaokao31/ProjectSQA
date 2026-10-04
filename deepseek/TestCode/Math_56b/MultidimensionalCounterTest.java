package org.apache.commons.math.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class MultidimensionalCounterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MultidimensionalCounter counter2D;
    private MultidimensionalCounter counter3D;
    private MultidimensionalCounter counter1D;
    private MultidimensionalCounter counterEmpty;

    @Before
    public void setUp() {
        counter2D = new MultidimensionalCounter(3, 4);
        counter3D = new MultidimensionalCounter(2, 3, 5);
        counter1D = new MultidimensionalCounter(10);
        counterEmpty = new MultidimensionalCounter(0, 0);
    }

    @Test
    public void testGetDimension() {
        Assert.assertEquals(2, counter2D.getDimension());
        Assert.assertEquals(3, counter3D.getDimension());
        Assert.assertEquals(1, counter1D.getDimension());
        Assert.assertEquals(2, counterEmpty.getDimension());
    }

    @Test
    public void testGetSizes() {
        Assert.assertArrayEquals(new int[] {3, 4}, counter2D.getSizes());
        Assert.assertArrayEquals(new int[] {2, 3, 5}, counter3D.getSizes());
        Assert.assertArrayEquals(new int[] {10}, counter1D.getSizes());
        Assert.assertArrayEquals(new int[] {0, 0}, counterEmpty.getSizes());
    }

    @Test
    public void testGetSize() {
        Assert.assertEquals(12, counter2D.getSize());
        Assert.assertEquals(30, counter3D.getSize());
        Assert.assertEquals(10, counter1D.getSize());
        Assert.assertEquals(0, counterEmpty.getSize());
    }

    @Test
    public void testEncodeDecode2D() {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                int encoded = counter2D.encode(i, j);
                Assert.assertEquals(index, encoded);
                int[] decoded = counter2D.decode(encoded);
                Assert.assertEquals(i, decoded[0]);
                Assert.assertEquals(j, decoded[1]);
                index++;
            }
        }
    }

    @Test
    public void testEncodeDecode3D() {
        int index = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 5; k++) {
                    int encoded = counter3D.encode(i, j, k);
                    Assert.assertEquals(index, encoded);
                    int[] decoded = counter3D.decode(encoded);
                    Assert.assertEquals(i, decoded[0]);
                    Assert.assertEquals(j, decoded[1]);
                    Assert.assertEquals(k, decoded[2]);
                    index++;
                }
            }
        }
    }

    @Test
    public void testEncodeDecode1D() {
        for (int i = 0; i < 10; i++) {
            int encoded = counter1D.encode(i);
            Assert.assertEquals(i, encoded);
            int[] decoded = counter1D.decode(encoded);
            Assert.assertEquals(1, decoded.length);
            Assert.assertEquals(i, decoded[0]);
        }
    }

    @Test
    public void testEncodeDecodeEmpty() {
        // For empty dimensions, encode should return 0 for valid indices
        Assert.assertEquals(0, counterEmpty.encode(0, 0));
        int[] decoded = counterEmpty.decode(0);
        Assert.assertArrayEquals(new int[] {0, 0}, decoded);
    }

    @Test
    public void testEncodeOutOfBoundsNegative() {
        thrown.expect(OutOfRangeException.class);
        counter2D.encode(-1, 0);
    }

    @Test
    public void testEncodeOutOfBoundsTooLarge() {
        thrown.expect(OutOfRangeException.class);
        counter2D.encode(3, 0);
    }

    @Test
    public void testEncodeOutOfBoundsSecondDimension() {
        thrown.expect(OutOfRangeException.class);
        counter2D.encode(0, 4);
    }

    @Test
    public void testEncodeOutOfBoundsMultipleDimensions() {
        thrown.expect(OutOfRangeException.class);
        counter3D.encode(2, 0, 0);
    }

    @Test
    public void testEncodeOutOfBoundsAllDimensions() {
        thrown.expect(OutOfRangeException.class);
        counter3D.encode(0, 3, 5);
    }

    @Test
    public void testEncodeOutOfBounds1D() {
        thrown.expect(OutOfRangeException.class);
        counter1D.encode(10);
    }

    @Test
    public void testEncodeOutOfBounds1DNegative() {
        thrown.expect(OutOfRangeException.class);
        counter1D.encode(-1);
    }

    @Test
    public void testDecodeOutOfBoundsNegative() {
        thrown.expect(OutOfRangeException.class);
        counter2D.decode(-1);
    }

    @Test
    public void testDecodeOutOfBoundsTooLarge() {
        thrown.expect(OutOfRangeException.class);
        counter2D.decode(12);
    }

    @Test
    public void testDecodeOutOfBoundsEmpty() {
        thrown.expect(OutOfRangeException.class);
        counterEmpty.decode(1);
    }

    @Test
    public void testDecodeOutOfBoundsEmptyNegative() {
        thrown.expect(OutOfRangeException.class);
        counterEmpty.decode(-1);
    }

    @Test
    public void testDecodeBoundaryValues() {
        // Test first and last indices
        int[] first = counter2D.decode(0);
        Assert.assertArrayEquals(new int[] {0, 0}, first);
        int[] last = counter2D.decode(11);
        Assert.assertArrayEquals(new int[] {2, 3}, last);
    }

    @Test
    public void testEncodeBoundaryValues() {
        Assert.assertEquals(0, counter2D.encode(0, 0));
        Assert.assertEquals(11, counter2D.encode(2, 3));
    }

    @Test
    public void testEncodeDecodeConsistency() {
        // Test all possible indices for 2D
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                int encoded = counter2D.encode(i, j);
                int[] decoded = counter2D.decode(encoded);
                Assert.assertEquals(i, decoded[0]);
                Assert.assertEquals(j, decoded[1]);
            }
        }
    }

    @Test
    public void testEncodeDecodeConsistency3D() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 5; k++) {
                    int encoded = counter3D.encode(i, j, k);
                    int[] decoded = counter3D.decode(encoded);
                    Assert.assertEquals(i, decoded[0]);
                    Assert.assertEquals(j, decoded[1]);
                    Assert.assertEquals(k, decoded[2]);
                }
            }
        }
    }

    @Test
    public void testDecodeEncodeConsistency() {
        for (int idx = 0; idx < 12; idx++) {
            int[] decoded = counter2D.decode(idx);
            int encoded = counter2D.encode(decoded[0], decoded[1]);
            Assert.assertEquals(idx, encoded);
        }
    }

    @Test
    public void testDecodeEncodeConsistency3D() {
        for (int idx = 0; idx < 30; idx++) {
            int[] decoded = counter3D.decode(idx);
            int encoded = counter3D.encode(decoded[0], decoded[1], decoded[2]);
            Assert.assertEquals(idx, encoded);
        }
    }

    @Test
    public void testDecodeEncodeConsistency1D() {
        for (int idx = 0; idx < 10; idx++) {
            int[] decoded = counter1D.decode(idx);
            int encoded = counter1D.encode(decoded[0]);
            Assert.assertEquals(idx, encoded);
        }
    }

    @Test
    public void testGetCounts() {
        int[][] expectedCounts2D = {
            {0, 0}, {0, 1}, {0, 2}, {0, 3},
            {1, 0}, {1, 1}, {1, 2}, {1, 3},
            {2, 0}, {2, 1}, {2, 2}, {2, 3}
        };
        for (int idx = 0; idx < 12; idx++) {
            Assert.assertArrayEquals(expectedCounts2D[idx], counter2D.getCounts(idx));
        }
    }

    @Test
    public void testGetCounts3D() {
        int idx = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 5; k++) {
                    int[] counts = counter3D.getCounts(idx);
                    Assert.assertEquals(i, counts[0]);
                    Assert.assertEquals(j, counts[1]);
                    Assert.assertEquals(k, counts[2]);
                    idx++;
                }
            }
        }
    }

    @Test
    public void testGetCountsOutOfBoundsNegative() {
        thrown.expect(OutOfRangeException.class);
        counter2D.getCounts(-1);
    }

    @Test
    public void testGetCountsOutOfBoundsTooLarge() {
        thrown.expect(OutOfRangeException.class);
        counter2D.getCounts(12);
    }

    @Test
    public void testGetCountsEmpty() {
        thrown.expect(OutOfRangeException.class);
        counterEmpty.getCounts(0);
    }

    @Test
    public void testConstructorWithZeroDimension() {
        thrown.expect(NotStrictlyPositiveException.class);
        new MultidimensionalCounter();
    }

    @Test
    public void testConstructorWithNegativeSize() {
        thrown.expect(NotStrictlyPositiveException.class);
        new MultidimensionalCounter(-1, 2);
    }

    @Test
    public void testConstructorWithZeroSize() {
        thrown.expect(NotStrictlyPositiveException.class);
        new MultidimensionalCounter(0, 2);
    }

    @Test
    public void testConstructorWithNullSize() {
        thrown.expect(NullArgumentException.class);
        new MultidimensionalCounter((int[]) null);
    }

    @Test
    public void testConstructorWithEmptyArray() {
        thrown.expect(NotStrictlyPositiveException.class);
        new MultidimensionalCounter(new int[0]);
    }

    @Test
    public void testEncodeWithWrongNumberOfArguments() {
        thrown.expect(OutOfRangeException.class);
        counter2D.encode(1, 2, 3);
    }

    @Test
    public void testEncodeWithTooFewArguments() {
        thrown.expect(OutOfRangeException.class);
        counter2D.encode(1);
    }

    @Test
    public void testDecodeWithEmptyCounter() {
        thrown.expect(OutOfRangeException.class);
        counterEmpty.decode(0);
    }

    @Test
    public void testGetCountsWithEmptyCounter() {
        thrown.expect(OutOfRangeException.class);
        counterEmpty.getCounts(0);
    }

    @Test
    public void testLargeDimensions() {
        MultidimensionalCounter large = new MultidimensionalCounter(100, 100, 100);
        Assert.assertEquals(1000000, large.getSize());
        int encoded = large.encode(99, 99, 99);
        Assert.assertEquals(999999, encoded);
        int[] decoded = large.decode(encoded);
        Assert.assertArrayEquals(new int[] {99, 99, 99}, decoded);
    }

    @Test
    public void testSingleElement() {
        MultidimensionalCounter single = new MultidimensionalCounter(1, 1);
        Assert.assertEquals(1, single.getSize());
        Assert.assertEquals(0, single.encode(0, 0));
        int[] decoded = single.decode(0);
        Assert.assertArrayEquals(new int[] {0, 0}, decoded);
    }

    @Test
    public void testMaxIndex() {
        MultidimensionalCounter max = new MultidimensionalCounter(Integer.MAX_VALUE, 1);
        Assert.assertEquals(Integer.MAX_VALUE, max.getSize());
        // Note: encode(0, 0) should work, but encode(Integer.MAX_VALUE-1, 0) might overflow
        // This test just ensures basic functionality
        Assert.assertEquals(0, max.encode(0, 0));
    }

    @Test(expected = OutOfRangeException.class)
    public void testEncodeOverflow() {
        // This may cause overflow in internal calculations
        MultidimensionalCounter overflow = new MultidimensionalCounter(Integer.MAX_VALUE, 2);
        overflow.encode(Integer.MAX_VALUE - 1, 1);
    }

    @Test
    public void testToString() {
        String str = counter2D.toString();
        Assert.assertTrue(str.contains("MultidimensionalCounter"));
        Assert.assertTrue(str.contains("3"));
        Assert.assertTrue(str.contains("4"));
    }

    @Test
    public void testHashCode() {
        MultidimensionalCounter same = new MultidimensionalCounter(3, 4);
        Assert.assertEquals(counter2D.hashCode(), same.hashCode());
        MultidimensionalCounter different = new MultidimensionalCounter(4, 3);
        Assert.assertNotEquals(counter2D.hashCode(), different.hashCode());
    }

    @Test
    public void testEquals() {
        MultidimensionalCounter same = new MultidimensionalCounter(3, 4);
        Assert.assertTrue(counter2D.equals(same));
        Assert.assertTrue(counter2D.equals(counter2D));
        Assert.assertFalse(counter2D.equals(null));
        Assert.assertFalse(counter2D.equals(new Object()));
        MultidimensionalCounter different = new MultidimensionalCounter(4, 3);
        Assert.assertFalse(counter2D.equals(different));
    }

    @Test
    public void testGetCountsWithInvalidIndex() {
        thrown.expect(OutOfRangeException.class);
        counter2D.getCounts(12);
    }

    @Test
    public void testGetCountsWithNegativeIndex() {
        thrown.expect(OutOfRangeException.class);
        counter2D.getCounts(-1);
    }

    @Test
    public void testEncodeWithMultipleDimensionsAndBoundary() {
        // Test edge case where index is at the boundary of each dimension
        Assert.assertEquals(0, counter3D.encode(0, 0, 0));
        Assert.assertEquals(29, counter3D.encode(1, 2, 4));
    }

    @Test
    public void testDecodeWithMultipleDimensionsAndBoundary() {
        int[] first = counter3D.decode(0);
        Assert.assertArrayEquals(new int[] {0, 0, 0}, first);
        int[] last = counter3D.decode(29);
        Assert.assertArrayEquals(new int[] {1, 2, 4}, last);
    }

    @Test
    public void testEncodeDecodeConsistencyWithLargeCounter() {
        MultidimensionalCounter large = new MultidimensionalCounter(50, 60);
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 60; j++) {
                int encoded = large.encode(i, j);
                int[] decoded = large.decode(encoded);
                Assert.assertEquals(i, decoded[0]);
                Assert.assertEquals(j, decoded[1]);
            }
        }
    }

    @Test
    public void testGetCountsWithLargeCounter() {
        MultidimensionalCounter large = new MultidimensionalCounter(50, 60);
        int idx = 0;
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 60; j++) {
                int[] counts = large.getCounts(idx);
                Assert.assertEquals(i, counts[0]);
                Assert.assertEquals(j, counts[1]);
                idx++;
            }
        }
    }

    @Test
    public void testConstructorWithMultipleSizes() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4, 5);
        Assert.assertEquals(4, counter.getDimension());
        Assert.assertEquals(120, counter.getSize());
        Assert.assertArrayEquals(new int[] {2, 3, 4, 5}, counter.getSizes());
    }

    @Test
    public void testEncodeDecodeWithFourDimensions() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4, 5);
        int idx = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 4; k++) {
                    for (int l = 0; l < 5; l++) {
                        int encoded = counter.encode(i, j, k, l);
                        Assert.assertEquals(idx, encoded);
                        int[] decoded = counter.decode(encoded);
                        Assert.assertEquals(i, decoded[0]);
                        Assert.assertEquals(j, decoded[1]);
                        Assert.assertEquals(k, decoded[2]);
                        Assert.assertEquals(l, decoded[3]);
                        idx++;
                    }
                }
            }
        }
    }

    @Test
    public void testGetCountsWithFourDimensions() {
        MultidimensionalCounter counter = new MultidimensionalCounter(2, 3, 4, 5);
        int idx = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 4; k++) {
                    for (int l = 0; l < 5; l++) {
                        int[] counts = counter.getCounts(idx);
                        Assert.assertEquals(i, counts[0]);
                        Assert.assertEquals(j, counts[1]);
                        Assert.assertEquals(k, counts[2]);
                        Assert.assertEquals(l, counts[3]);
                        idx++;
                    }
                }
            }
        }
    }
}