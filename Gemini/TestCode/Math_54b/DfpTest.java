package org.apache.commons.math.dfp;

import org.junit.Assert;
import org.junit.Test;

public class DfpTest {

    @Test
    public void testZero() {
        DfpField field = new DfpField(10);
        Dfp zero = field.newDfp(0);
        Assert.assertEquals(0, zero.classify());
        Assert.assertTrue(zero.isZero());
        Assert.assertFalse(zero.isNaN());
    }

    @Test
    public void testEqualsAndCompare() {
        DfpField field = new DfpField(10);
        Dfp a = field.newDfp(1);
        Dfp b = field.newDfp(1);
        Dfp c = field.newDfp(2);

        Assert.assertTrue(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertEquals(0, a.compareTo(b));
        Assert.assertEquals(-1, a.compareTo(c));
        Assert.assertEquals(1, c.compareTo(a));
    }

    @Test
    public void testAddAndSubtract() {
        DfpField field = new DfpField(10);
        Dfp a = field.newDfp(5);
        Dfp b = field.newDfp(3);

        Dfp sum = a.add(b);
        Assert.assertEquals(8.0, sum.toDouble(), 1e-12);

        Dfp diff = a.subtract(b);
        Assert.assertEquals(2.0, diff.toDouble(), 1e-12);
    }

    @Test
    public void testMultiplyAndDivide() {
        DfpField field = new DfpField(10);
        Dfp a = field.newDfp(4);
        Dfp b = field.newDfp(2);

        Dfp prod = a.multiply(b);
        Assert.assertEquals(8.0, prod.toDouble(), 1e-12);

        Dfp quot = a.divide(b);
        Assert.assertEquals(2.0, quot.toDouble(), 1e-12);
    }

    @Test
    public void testNegateAndAbs() {
        DfpField field = new DfpField(10);
        Dfp a = field.newDfp(-5);

        Assert.assertEquals(5.0, a.abs().toDouble(), 1e-12);
        Assert.assertEquals(5.0, a.negate().toDouble(), 1e-12);
    }

    @Test
    public void testSpecialNumbers() {
        DfpField field = newDfpFieldWithDefaults();
        Dfp nan = field.newDfp(DfpField.FLAG_INVALID); // or similar via string/factory
        // Let's use getZero, getOne, etc.
        Dfp zero = field.getZero();
        Dfp one = field.getOne();
        Dfp two = field.getTwo();

        Assert.assertNotNull(zero);
        Assert.assertNotNull(one);
        Assert.assertNotNull(two);
    }

    private DfpField newDfpFieldWithDefaults() {
        return new DfpField(20);
    }
}