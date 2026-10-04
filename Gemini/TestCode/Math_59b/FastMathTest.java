package org.apache.commons.math.util;

import org.junit.Assert;
import org.junit.Test;

public class FastMathTest {

    @Test
    public void testMinMaxFloatDoubleEdgeCases() {
        // Test min and max methods for float and double to cover branches and edge cases
        
        // float min/max
        Assert.assertEquals(1.0f, FastMath.min(1.0f, 2.0f), 0.0f);
        Assert.assertEquals(1.0f, FastMath.min(2.0f, 1.0f), 0.0f);
        Assert.assertEquals(2.0f, FastMath.max(1.0f, 2.0f), 0.0f);
        Assert.assertEquals(2.0f, FastMath.max(2.0f, 1.0f), 0.0f);

        // Negative zero and zero cases for float
        float minZeroFloat = FastMath.min(0.0f, -0.0f);
        Assert.assertTrue(Float.isNaN(minZeroFloat) || (minZeroFloat == 0.0f && Float.floatToIntBits(minZeroFloat) == Float.floatToIntBits(-0.0f)));

        float maxZeroFloat = FastMath.max(0.0f, -0.0f);
        Assert.assertEquals(0.0f, maxZeroFloat, 0.0f);
        Assert.assertEquals(0.0f, maxZeroFloat, 0.0f);

        // NaN cases for float
        Assert.assertTrue(Float.isNaN(FastMath.min(Float.NaN, 1.0f)));
        Assert.assertTrue(Float.isNaN(FastMath.min(1.0f, Float.NaN)));
        Assert.assertTrue(Float.isNaN(FastMath.max(Float.NaN, 1.0f)));
        Assert.assertTrue(Float.isNaN(FastMath.max(1.0f, Float.NaN)));

        // double min/max
        Assert.assertEquals(1.0, FastMath.min(1.0, 2.0), 0.0);
        Assert.assertEquals(1.0, FastMath.min(2.0, 1.0), 0.0);
        Assert.assertEquals(2.0, FastMath.max(1.0, 2.0), 0.0);
        Assert.assertEquals(2.0, FastMath.max(2.0, 1.0), 0.0);

        // Negative zero and zero cases for double
        double minZeroDouble = FastMath.min(0.0, -0.0);
        Assert.assertTrue(minZeroDouble == 0.0 && Double.doubleToLongBits(minZeroDouble) == Double.doubleToLongBits(-0.0));

        double maxZeroDouble = FastMath.max(0.0, -0.0);
        Assert.assertEquals(0.0, maxZeroDouble, 0.0);

        // NaN cases for double
        Assert.assertTrue(Double.isNaN(FastMath.min(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.min(1.0, Double.NaN)));
        Assert.assertTrue(Double.isNaN(FastMath.max(Double.NaN, 1.0)));
        Assert.assertTrue(Double.isNaN(FastMath.max(1.0, Double.NaN)));
    }

    @Test
    public void testAbsMethods() {
        Assert.assertEquals(5, FastMath.abs(-5));
        Assert.assertEquals(5, FastMath.abs(5));
        Assert.assertEquals(Integer.MIN_VALUE, FastMath.abs(Integer.MIN_VALUE));

        Assert.assertEquals(5L, FastMath.abs(-5L));
        Assert.assertEquals(5L, FastMath.abs(5L));
        Assert.assertEquals(Long.MIN_VALUE, FastMath.abs(Long.MIN_VALUE));

        Assert.assertEquals(5.0f, FastMath.abs(-5.0f), 0.0f);
        Assert.assertEquals(5.0f, FastMath.abs(5.0f), 0.0f);
        Assert.assertTrue(Float.isNaN(FastMath.abs(Float.NaN)));
        Assert.assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);

        Assert.assertEquals(5.0, FastMath.abs(-5.0), 0.0);
        Assert.assertEquals(5.0, FastMath.abs(5.0), 0.0);
        Assert.assertTrue(Double.isNaN(FastMath.abs(Double.NaN)));
        Assert.assertEquals(0.0, FastMath.abs(-0.0), 0.0);
    }

    @Test
    public void testTrigFunctions() {
        double delta = 1e-9;
        Assert.assertEquals(0.0, FastMath.sin(0.0), delta);
        Assert.assertEquals(1.0, FastMath.cos(0.0), delta);
        Assert.assertEquals(0.0, FastMath.tan(0.0), delta);

        Assert.assertEquals(0.0, FastMath.asin(0.0), delta);
        Assert.assertEquals(Math.PI / 2, FastMath.acos(0.0), delta);
        Assert.assertEquals(0.0, FastMath.atan(0.0), delta);
    }

    @Test
    public void testExpLogPow() {
        double delta = 1e-9;
        Assert.assertEquals(1.0, FastMath.exp(0.0), delta);
        Assert.assertEquals(0.0, FastMath.log(1.0), delta);
        Assert.assertEquals(8.0, FastMath.pow(2.0, 3.0), delta);
        Assert.assertEquals(1.0, FastMath.pow(5.0, 0.0), delta);
    }

    @Test
    public void testSqrtAndCbrt() {
        double delta = 1e-9;
        Assert.assertEquals(3.0, FastMath.sqrt(9.0), delta);
        Assert.assertEquals(2.0, FastMath.cbrt(8.0), delta);
        Assert.assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
    }

    @Test
    public void testRoundFloorCeil() {
        Assert.assertEquals(3.0, FastMath.floor(3.7), 0.0);
        Assert.assertEquals(-4.0, FastMath.floor(-3.7), 0.0);

        Assert.assertEquals(4.0, FastMath.ceil(3.2), 0.0);
        Assert.assertEquals(-3.0, FastMath.ceil(-3.7), 0.0);

        Assert.assertEquals(4L, FastMath.round(3.7));
        Assert.assertEquals(4, FastMath.round(3.7f));
    }
}