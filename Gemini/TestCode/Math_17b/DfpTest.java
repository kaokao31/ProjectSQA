package org.apache.commons.math3.dfp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DfpTest {

    private DfpField field;

    @Before
    public void setUp() {
        field = new DfpField(20);
    }

    @Test
    public void testMultiplyAndDivideByInteger() {
        Dfp a = field.newDfp("3.0");
        Dfp b = a.multiply(2);
        Assert.assertEquals(6.0, b.toDouble(), 1e-15);

        Dfp c = b.divide(2);
        Assert.assertEquals(3.0, c.toDouble(), 1e-15);
    }

    @Test
    public void testMultiplyAndDivideByDfp() {
        Dfp a = field.newDfp("3.5");
        Dfp b = field.newDfp("2.0");
        Dfp c = a.multiply(b);
        Assert.assertEquals(7.0, c.toDouble(), 1e-15);

        Dfp d = c.divide(b);
        Assert.assertEquals(3.5, d.toDouble(), 1e-15);
    }

    @Test
    public void testAddAndSubtract() {
        Dfp a = field.newDfp("10.5");
        Dfp b = field.newDfp("2.5");
        Dfp sum = a.add(b);
        Assert.assertEquals(13.0, sum.toDouble(), 1e-15);

        Dfp diff = a.subtract(b);
        Assert.assertEquals(8.0, diff.toDouble(), 1e-15);
    }

    @Test
    public void testRemainder() {
        Dfp a = field.newDfp("10.0");
        Dfp b = field.newDfp("3.0");
        Dfp rem = a.remainder(b);
        Assert.assertEquals(1.0, rem.toDouble(), 1e-15);
    }

    @Test
    public void testNegateAndAbs() {
        Dfp a = field.newDfp("-5.25");
        Assert.assertEquals(5.25, a.abs().toDouble(), 1e-15);
        Assert.assertEquals(5.25, a.negate().toDouble(), 1e-15);
    }

    @Test
    public void testComparisons() {
        Dfp a = field.newDfp("1.0");
        Dfp b = field.newDfp("2.0");
        Dfp c = field.newDfp("1.0");

        Assert.assertTrue(a.lessThan(b));
        Assert.assertTrue(b.greaterThan(a));
        Assert.assertEquals(0, a.compareTo(c));
        Assert.assertTrue(a.equals(c));
        Assert.assertFalse(a.equals(b));
    }

    @Test
    public void testSquareRoot() {
        Dfp a = field.newDfp("9.0");
        Dfp sqrt = a.sqrt();
        Assert.assertEquals(3.0, sqrt.toDouble(), 1e-15);
    }

    @Test
    public void testSpecialValues() {
        Dfp zero = field.getZero();
        Dfp one = field.getOne();
        Dfp two = field.getTwo();

        Assert.assertTrue(zero.isZero());
        Assert.assertEquals(1.0, one.toDouble(), 1e-15);
        Assert.assertEquals(2.0, two.toDouble(), 1e-15);

        Dfp inf = field.newDfp("1").divide(field.newDfp("0"));
        Assert.assertTrue(inf.isInfinite());

        Dfp nan = field.newDfp("0").divide(field.newDfp("0"));
        Assert.assertTrue(nan.isNaN());
    }

    @Test
    public void testPower() {
        Dfp a = field.newDfp("2.0");
        Dfp pow = a.power(3);
        Assert.assertEquals(8.0, pow.toDouble(), 1e-15);
    }

    @Test
    public void testNextAfter() {
        Dfp a = field.newDfp("1.0");
        Dfp next = a.nextAfter(field.newDfp("2.0"));
        Assert.assertTrue(next.greaterThan(a));
        
        Dfp prev = a.nextAfter(field.newDfp("0.0"));
        Assert.assertTrue(prev.lessThan(a));
    }

    @Test
    public void testConversions() {
        Dfp a = field.newDfp("123.456");
        Assert.assertEquals(123, a.toInt());
        Assert.assertEquals(123.456, a.toDouble(), 1e-3);
        Assert.assertEquals("123.456", a.toString());
    }
}