package org.apache.commons.math3.random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class BitsStreamGeneratorTest {

    private TestBitsStreamGenerator generator;

    /**
     * Concrete subclass of BitsStreamGenerator for testing abstract methods.
     */
    private static class TestBitsStreamGenerator extends BitsStreamGenerator {
        private int nextIntReturn = 0;
        private long nextLongReturn = 0L;
        private boolean nextBooleanReturn = false;
        private float nextFloatReturn = 0.0f;
        private double nextDoubleReturn = 0.0d;
        private byte[] nextBytesByteArray = null;

        @Override
        public void setSeed(int seed) {
            // no-op
        }

        @Override
        public void setSeed(int[] seed) {
            // no-op
        }

        @Override
        public void setSeed(long seed) {
            // no-op
        }

        @Override
        protected int next(int bits) {
            return nextIntReturn;
        }

        @Override
        public int nextInt() {
            return nextIntReturn;
        }

        @Override
        public long nextLong() {
            return nextLongReturn;
        }

        @Override
        public boolean nextBoolean() {
            return nextBooleanReturn;
        }

        @Override
        public float nextFloat() {
            return nextFloatReturn;
        }

        @Override
        public double nextDouble() {
            return nextDoubleReturn;
        }

        @Override
        public void nextBytes(byte[] bytes) {
            if (nextBytesByteArray != null && bytes != null) {
                System.arraycopy(nextBytesByteArray, 0, bytes, 0, Math.min(bytes.length, nextBytesByteArray.length));
            }
        }
    }

    @Before
    public void setUp() {
        generator = new TestBitsStreamGenerator();
    }

    @Test
    public void testNextBoolean() {
        generator.nextBooleanReturn = true;
        Assert.assertTrue(generator.nextBoolean());

        generator.nextBooleanReturn = false;
        Assert.assertFalse(generator.nextBoolean());
    }

    @Test
    public void testNextIntLimit() {
        // Test standard positive n
        generator.nextIntReturn = 10;
        int val = generator.nextInt(50);
        Assert.assertTrue(val >= 0);
        Assert.assertTrue(val < 50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNextIntLimitNonPositive() {
        // n <= 0 should throw IllegalArgumentException
        generator.nextInt(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNextIntLimitNegative() {
        generator.nextInt(-5);
    }

    @Test
    public void testNextLongBounds() {
        // Test normal range
        generator.nextLongReturn = 100L;
        long val = generator.nextLong(1000L);
        Assert.assertTrue(val >= 0L);
        Assert.assertTrue(val < 1000L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNextLongLimitNonPositive() {
        generator.nextLong(0L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNextLongLimitNegative() {
        generator.nextLong(-100L);
    }

    @Test
    public void testClear() {
        // Just verify clear doesn't throw and can be called
        generator.clear();
    }

    @Test
    public void testNextFloat() {
        generator.nextFloatReturn = 0.5f;
        Assert.assertEquals(0.5f, generator.nextFloat(), 0.0001f);
    }

    @Test
    public void testNextDouble() {
        generator.nextDoubleReturn = 0.75;
        Assert.assertEquals(0.75, generator.nextDouble(), 0.0001);
    }

    @Test
    public void testNextBytes() {
        byte[] bytes = new byte[10];
        generator.nextBytesByteArray = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        generator.nextBytes(bytes);
        Assert.assertEquals(1, bytes[0]);
        Assert.assertEquals(10, bytes[9]);
    }
}