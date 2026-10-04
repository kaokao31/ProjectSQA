/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math.complex;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Test case for {@link Complex}.
 * Focused heavily on Math-46 edge cases (divide by zero, NaN, Infinity, and division safety).
 */
public class ComplexTest {

    private static final double DELTA = 1e-14;

    @Test
    public void testDivideZeroByZero() {
        Complex x = Complex.ZERO;
        Complex y = Complex.ZERO;
        Complex z = x.divide(y);
        Assert.assertTrue(z.isNaN());
    }

    @Test
    public void testDivideByZero() {
        Complex x = new Complex(1.0, 2.0);
        Complex y = Complex.ZERO;
        Complex z = x.divide(y);
        // Math-46 bug context: division of non-zero by zero used to return NaN instead of Infinity
        Assert.assertTrue(z.isInfinite());
        Assert.assertEquals(Complex.INF, z);
    }

    @Test
    public void testDivideZeroByNonZero() {
        Complex x = Complex.ZERO;
        Complex y = new Complex(1.0, 2.0);
        Complex z = x.divide(y);
        Assert.assertEquals(0.0, z.getReal(), DELTA);
        Assert.assertEquals(0.0, z.getImaginary(), DELTA);
        Assert.assertFalse(z.isNaN());
        Assert.assertFalse(z.isInfinite());
    }

    @Test
    public void testDivideScalarZeroByZero() {
        Complex x = Complex.ZERO;
        Complex z = x.divide(0.0);
        Assert.assertTrue(z.isNaN());
    }

    @Test
    public void testDivideScalarByZero() {
        Complex x = new Complex(1.0, 2.0);
        Complex z = x.divide(0.0);
        Assert.assertTrue(z.isInfinite());
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex x = new Complex(1.0, 2.0);
        Complex y = new Complex(1.0, 2.0);
        Complex z = new Complex(1.0, 3.0);
        Complex nan1 = Complex.NaN;
        Complex nan2 = new Complex(Double.NaN, Double.NaN);

        Assert.assertTrue(x.equals(x));
        Assert.assertTrue(x.equals(y));
        Assert.assertFalse(x.equals(z));
        Assert.assertFalse(x.equals(null));
        Assert.assertFalse(x.equals("some string"));

        Assert.assertTrue(nan1.equals(nan2));
        Assert.assertEquals(nan1.hashCode(), nan2.hashCode());

        Complex inf1 = Complex.INF;
        Complex inf2 = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Assert.assertTrue(inf1.equals(inf2));
        Assert.assertEquals(inf1.hashCode(), inf2.hashCode());
        
        Assert.assertEquals(x.hashCode(), y.hashCode());
        Assert.assertNotEquals(x.hashCode(), z.hashCode());
    }

    @Test
    public void testAccessorsAndBasicMath() {
        Complex c = new Complex(3.0, 4.0);
        Assert.assertEquals(3.0, c.getReal(), DELTA);
        Assert.assertEquals(4.0, c.getImaginary(), DELTA);
        Assert.assertEquals(5.0, c.abs(), DELTA);

        Complex conjugate = c.conjugate();
        Assert.assertEquals(3.0, conjugate.getReal(), DELTA);
        Assert.assertEquals(-4.0, conjugate.getImaginary(), DELTA);

        Complex negate = c.negate();
        Assert.assertEquals(-3.0, negate.getReal(), DELTA);
        Assert.assertEquals(-4.0, negate.getImaginary(), DELTA);

        Complex add = c.add(new Complex(1.0, 1.0));
        Assert.assertEquals(4.0, add.getReal(), DELTA);
        Assert.assertEquals(5.0, add.getImaginary(), DELTA);

        Complex addScalar = c.add(2.0);
        Assert.assertEquals(5.0, addScalar.getReal(), DELTA);
        Assert.assertEquals(4.0, addScalar.getImaginary(), DELTA);

        Complex subtract = c.subtract(new Complex(1.0, 1.0));
        Assert.assertEquals(2.0, subtract.getReal(), DELTA);
        Assert.assertEquals(3.0, subtract.getImaginary(), DELTA);

        Complex subtractScalar = c.subtract(1.0);
        Assert.assertEquals(2.0, subtractScalar.getReal(), DELTA);
        Assert.assertEquals(4.0, subtractScalar.getImaginary(), DELTA);

        Complex multiply = c.multiply(new Complex(2.0, 0.0));
        Assert.assertEquals(6.0, multiply.getReal(), DELTA);
        Assert.assertEquals(8.0, multiply.getImaginary(), DELTA);

        Complex multiplyScalar = c.multiply(2.0);
        Assert.assertEquals(6.0, multiplyScalar.getReal(), DELTA);
        Assert.assertEquals(8.0, multiplyScalar.getImaginary(), DELTA);

        Complex multiplyInt = c.multiply(2);
        Assert.assertEquals(6.0, multiplyInt.getReal(), DELTA);
        Assert.assertEquals(8.0, multiplyInt.getImaginary(), DELTA);
    }

    @Test
    public void testAdvancedMath() {
        Complex c = new Complex(1.0, 1.0);

        Complex sqrt = c.sqrt();
        Assert.assertNotNull(sqrt);

        Complex sqrt1 = new Complex(-1.0, 0.0).sqrt();
        Assert.assertEquals(0.0, sqrt1.getReal(), DELTA);
        Assert.assertEquals(1.0, sqrt1.getImaginary(), DELTA);

        Complex log = c.log();
        Assert.assertNotNull(log);

        Complex exp = c.exp();
        Assert.assertNotNull(exp);

        Complex pow = c.pow(new Complex(2.0, 0.0));
        Assert.assertNotNull(pow);

        Complex cos = c.cos();
        Assert.assertNotNull(cos);

        Complex cosh = c.cosh();
        Assert.assertNotNull(cosh);

        Complex sin = c.sin();
        Assert.assertNotNull(sin);

        Complex sinh = c.sinh();
        Assert.assertNotNull(sinh);

        Complex tan = c.tan();
        Assert.assertNotNull(tan);

        Complex tanh = c.tanh();
        Assert.assertNotNull(tanh);

        Complex acos = c.acos();
        Assert.assertNotNull(acos);

        Complex asin = c.asin();
        Assert.assertNotNull(asin);

        Complex atan = c.atan();
        Assert.assertNotNull(atan);

        Complex scale = c.scale(2.0);
        Assert.assertEquals(2.0, scale.getReal(), DELTA);
        Assert.assertEquals(2.0, scale.getImaginary(), DELTA);
    }

    @Test
    public void testNthRoot() {
        Complex c = new Complex(1.0, 0.0);
        List<Complex> roots = c.nthRoot(2);
        Assert.assertEquals(2, roots.size());

        try {
            c.nthRoot(0);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        Complex nanComplex = Complex.NaN;
        List<Complex> nanRoots = nanComplex.nthRoot(2);
        Assert.assertEquals(1, nanRoots.size());
        Assert.assertTrue(nanRoots.get(0).isNaN());

        Complex infComplex = Complex.INF;
        List<Complex> infRoots = infComplex.nthRoot(2);
        Assert.assertEquals(1, infRoots.size());
        Assert.assertTrue(infRoots.get(0).isNaN()); // or depending on implementation, but let's check it doesn't crash
    }

    @Test
    public void testToString() {
        Complex c = new Complex(1.0, 2.0);
        String str = c.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("1.0"));
        Assert.assertTrue(str.contains("2.0"));
    }

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        Complex.ZERO.add(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSubtractNull() {
        Complex.ZERO.subtract(null);
    }

    @Test(expected = NullPointerException.class)
    public void testMultiplyNull() {
        Complex.ZERO.multiply(null);
    }

    @Test(expected = NullPointerException.class)
    public void testDivideNull() {
        Complex.ZERO.divide(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPowNull() {
        Complex.ZERO.pow(null);
    }

    @Test
    public void testConstants() {
        Assert.assertNotNull(Complex.I);
        Assert.assertNotNull(Complex.NaN);
        Assert.assertNotNull(Complex.INF);
        Assert.assertNotNull(Complex.ONE);
        Assert.assertNotNull(Complex.ZERO);

        Assert.assertTrue(Complex.NaN.isNaN());
        Assert.assertTrue(Complex.INF.isInfinite());
        Assert.assertFalse(Complex.ONE.isNaN());
        Assert.assertFalse(Complex.ONE.isInfinite());
    }

    @Test
    public void testCheckNotNull() {
        try {
            Complex.ZERO.add(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Commons math might throw IllegalArgumentException or NullPointerException depending on version, 
            // the above test checks for NullPointerException. Let's make sure both are handled cleanly.
        } catch (NullPointerException e) {
            // Expected
        }
    }
}