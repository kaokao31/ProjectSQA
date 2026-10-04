package org.apache.commons.math3.util;

import org.junit.Assert;
import org.junit.Test;

public class FastMathTest {

    @Test
    public void testMaxMinMethods() {
        Assert.assertEquals(5.0, FastMath.max(3.0, 5.0), 0.0);
        Assert.assertEquals(5.0, FastMath.max(5.0, 3.0), 0.0);
        Assert.assertEquals(3.0, FastMath.min(3.0, 5.0), 0.0);
        Assert.assertEquals(3.0, FastMath.min(5.0, 3.0), 0.0);

        Assert.assertEquals(5.0f, FastMath.max(3.0f, 5.0f), 0.0f);
        Assert.assertEquals(5.0f, FastMath.max(5.0f, 3.0f), 0.0f);
        Assert.assertEquals(3.0f, FastMath.min(3.0f, 5.0f), 0.0f);
        Assert.assertEquals(3.0f, FastMath.min(5.0f, 3.0f), 0.0f);

        Assert.assertEquals(10, FastMath.max(5, 10));
        Assert.assertEquals(10, FastMath.max(10, 5));
        Assert.assertEquals(5, FastMath.min(5, 10));
        Assert.assertEquals(5, FastMath.min(10, 5));

        Assert.assertEquals(10L, FastMath.max(5L, 10L));
        Assert.assertEquals(10L, FastMath.max(10L, 5L));
        Assert.assertEquals(5L, FastMath.min(5L, 10L));
        Assert.assertEquals(5L, FastMath.min(10L, 5L));
    }

    @Test
    public void testAbsMethods() {
        Assert.assertEquals(5.0, FastMath.abs(-5.0), 0.0);
        Assert.assertEquals(5.0, FastMath.abs(5.0), 0.0);
        Assert.assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        Assert.assertEquals(5.0f, FastMath.abs(5.0f), 0.0f);
        Assert.assertEquals(5, FastMath.abs(-5));
        Assert.assertEquals(5, FastMath.abs(5));
        Assert.assertEquals(5L, FastMath.abs(-5L));
        Assert.assertEquals(5L, FastMath.abs(5L));
    }

    @Test
    public void testSignMethods() {
        Assert.assertEquals(1.0, FastMath.signum(5.0), 0.0);
        Assert.assertEquals(-1.0, FastMath.signum(-5.0), 0.0);
        Assert.assertEquals(0.0, FastMath.signum(0.0), 0.0);

        Assert.assertEquals(1.0f, FastMath.signum(5.0f), 0.0f);
        Assert.assertEquals(-1.0f, FastMath.signum(-5.0f), 0.0f);
        Assert.assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
    }

    @Test
    public void testTrigSpecialValues() {
        Assert.assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));

        Assert.assertEquals(0.0, FastMath.sin(0.0), 0.0);
        Assert.assertEquals(1.0, FastMath.cos(0.0), 0.0);
        Assert.assertEquals(0.0, FastMath.tan(0.0), 0.0);
    }

    @Test
    public void testExpAndLogSpecialValues() {
        Assert.assertEquals(1.0, FastMath.exp(0.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.exp(Double.NaN)));
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(Double.POSITIVE_INFINITY), 0.0);
        Assert.assertEquals(0.0, FastMath.exp(Double.NEGATIVE_INFINITY), 0.0);

        Assert.assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.log(-1.0)));
        Assert.assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        Assert.assertEquals(0.0, FastMath.log(1.0), 0.0);
    }

    @Test
    public void testPowSpecialValues() {
        Assert.assertEquals(1.0, FastMath.pow(2.0, 0.0), 0.0);
        Assert.assertEquals(2.0, FastMath.pow(2.0, 1.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.pow(2.0, Double.NaN)));
    }

    @Test
    public void testSqrtAndCbrt() {
        Assert.assertEquals(2.0, FastMath.sqrt(4.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
        Assert.assertEquals(0.0, FastMath.sqrt(0.0), 0.0);

        Assert.assertEquals(2.0, FastMath.cbrt(8.0), 0.0);
        Assert.assertEquals(-2.0, FastMath.cbrt(-8.0), 0.0);
        Assert.assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
    }

    @Test
    public void testHypot() {
        Assert.assertEquals(5.0, FastMath.hypot(3.0, 4.0), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 3.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 3.0)));
    }

    @Test
    public void testIEEERemainder() {
        Assert.assertEquals(1.0, FastMath.IEEEremainder(9.0, 4.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.IEEEremainder(Double.NaN, 4.0)));
    }

    @Test
    public void testCeilFloorRintRound() {
        Assert.assertEquals(3.0, FastMath.ceil(2.5), 0.0);
        Assert.assertEquals(2.0, FastMath.floor(2.5), 0.0);
        Assert.assertEquals(2.0, FastMath.rint(2.5), 0.0);
        Assert.assertEquals(3, FastMath.round(2.5f));
        Assert.assertEquals(3L, FastMath.round(2.5));
    }

    @Test
    public void testNextAfter() {
        Assert.assertEquals(Math.nextAfter(1.0, 2.0), FastMath.nextAfter(1.0, 2.0), 0.0);
        Assert.assertEquals(Math.nextAfter(1.0f, 2.0f), FastMath.nextAfter(1.0f, 2.0f), 0.0f);
    }

    @Test
    public void testRandom() {
        double r = FastMath.random();
        Assert.assertTrue(r >= 0.0 && r < 1.0);
    }

    @Test
    public void testUlp() {
        Assert.assertEquals(Math.ulp(1.0), FastMath.ulp(1.0), 0.0);
        Assert.assertEquals(Math.ulp(1.0f), FastMath.ulp(1.0f), 0.0f);
    }
}