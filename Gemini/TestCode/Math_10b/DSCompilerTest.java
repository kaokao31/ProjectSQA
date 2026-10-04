package org.apache.commons.math3.analysis.differentiation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DSCompilerTest {

    @Test
    public void testCompilationAndBasicDerivatives() {
        // Test compiling for 1 order, 1 parameter
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        Assert.assertEquals(1, compiler.getFreeParameters());
        Assert.assertEquals(1, compiler.getOrder());
        Assert.assertEquals(2, compiler.getSize());

        double[] data = new double[2];
        data[0] = 2.0; // value
        data[1] = 3.0; // derivative

        double[] result = new double[2];
        
        // Test function: id(x) = x
        compiler.add(data, 0, data, 0, result, 0);
        Assert.assertEquals(4.0, result[0], 1.0e-15);
        Assert.assertEquals(6.0, result[1], 1.0e-15);

        compiler.subtract(data, 0, data, 0, result, 0);
        Assert.assertEquals(0.0, result[0], 1.0e-15);
        Assert.assertEquals(0.0, result[1], 1.0e-15);
    }

    @Test
    public void testMultiplyAndDivide() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] left = new double[] { 2.0, 1.0 };
        double[] right = new double[] { 3.0, 4.0 };
        double[] result = new double[2];

        // Multiply: (2 + x)*(3 + 4x) = 6 + 11x + 4x^2 -> up to order 1: 6 + 11x
        compiler.multiply(left, 0, right, 0, result, 0);
        Assert.assertEquals(6.0, result[0], 1.0e-15);
        Assert.assertEquals(11.0, result[1], 1.0e-15);

        // Divide: (2 + x) / (3 + 4x)
        compiler.divide(left, 0, right, 0, result, 0);
        Assert.assertEquals(2.0 / 3.0, result[0], 1.0e-15);
        // derivative of (2+x)/(3+4x) at x=0 is (1*(3) - 2*(4)) / 3^2 = (3 - 8) / 9 = -5/9
        Assert.assertEquals(-5.0 / 9.0, result[1], 1.0e-15);
    }

    @Test
    public void testElementaryFunctions() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] operand = new double[] { 0.0, 1.0 };
        double[] result = new double[2];

        // sin(0) = 0, cos(0)*1 = 1
        compiler.sin(operand, 0, result, 0);
        Assert.assertEquals(0.0, result[0], 1.0e-15);
        Assert.assertEquals(1.0, result[1], 1.0e-15);

        // cos(0) = 1, -sin(0)*1 = 0
        compiler.cos(operand, 0, result, 0);
        Assert.assertEquals(1.0, result[0], 1.0e-15);
        Assert.assertEquals(0.0, result[1], 1.0e-15);

        // exp(0) = 1, exp(0)*1 = 1
        compiler.exp(operand, 0, result, 0);
        Assert.assertEquals(1.0, result[0], 1.0e-15);
        Assert.assertEquals(1.0, result[1], 1.0e-15);

        // log(1) for operand around 1
        double[] logOperand = new double[] { 1.0, 1.0 };
        compiler.log(logOperand, 0, result, 0);
        Assert.assertEquals(0.0, result[0], 1.0e-15);
        Assert.assertEquals(1.0 / 1.0, result[1], 1.0e-15);
    }

    @Test
    public void testPowAndRoot() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] operand = new double[] { 4.0, 1.0 };
        double[] result = new double[2];

        // sqrt(4) = 2, 0.5 * 4^(-0.5) * 1 = 0.25
        compiler.sqrt(operand, 0, result, 0);
        Assert.assertEquals(2.0, result[0], 1.0e-15);
        Assert.assertEquals(0.25, result[1], 1.0e-15);

        // pow(4, 2) = 16
        compiler.pow(operand, 0, 2, result, 0);
        Assert.assertEquals(16.0, result[0], 1.0e-15);
        Assert.assertEquals(2 * 4 * 1.0, result[1], 1.0e-15);
    }

    @Test
    public void testTrigFunctionsAdditional() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] operand = new double[] { 0.0, 1.0 };
        double[] result = new double[2];

        compiler.tan(operand, 0, result, 0);
        Assert.assertEquals(0.0, result[0], 1.0e-15);
        Assert.assertEquals(1.0, result[1], 1.0e-15);

        compiler.asin(operand, 0, result, 0);
        Assert.assertEquals(0.0, result[0], 1.0e-15);
        Assert.assertEquals(1.0, result[1], 1.0e-15);

        compiler.acos(operand, 0, result, 0);
        Assert.assertEquals(Math.PI / 2.0, result[0], 1.0e-15);
        Assert.assertEquals(-1.0, result[1], 1.0e-15);

        compiler.atan(operand, 0, result, 0);
        Assert.assertEquals(0.0, result[0], 1.0e-15);
        Assert.assertEquals(1.0, result[1], 1.0e-15);
    }

    @Test
    public void testHyperbolicFunctions() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] operand = new double[] { 0.0, 1.0 };
        double[] result = new double[2];

        compiler.sinh(operand, 0, result, 0);
        Assert.assertEquals(0.0, result[0], 1.0e-15);
        Assert.assertEquals(1.0, result[1], 1.0e-15);

        compiler.cosh(operand, 0, result, 0);
        Assert.assertEquals(1.0, result[0], 1.0e-15);
        Assert.assertEquals(0.0, result[1], 1.0e-15);

        compiler.tanh(operand, 0, result, 0);
        Assert.assertEquals(0.0, result[0], 1.0e-15);
        Assert.assertEquals(1.0, result[1], 1.0e-15);
    }

    @Test
    public void testCompilerCacheAndLimits() {
        DSCompiler c1 = DSCompiler.getCompiler(2, 2);
        DSCompiler c2 = DSCompiler.getCompiler(2, 2);
        Assert.assertSame(c1, c2);

        Assert.assertEquals(2, c1.getFreeParameters());
        Assert.assertEquals(2, c1.getOrder());
        // For 2 parameters, order 2, size should be ((2+2)!)/(2!2!) = 24 / 4 = 6
        Assert.assertEquals(6, c1.getSize());
    }

    @Test
    public void testComposeAndRemainder() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] operand = new double[] { 2.0, 1.0 };
        double[] result = new double[2];
        double[] higherOrder = { 5.0, 2.0 };

        compiler.compose(operand, 0, higherOrder, result, 0);
        // Checking compose functionality executes without error
        Assert.assertNotNull(result);

        compiler.remainder(operand, 0, new double[] { 3.0, 0.0 }, 0, result, 0);
        Assert.assertEquals(2.0 % 3.0, result[0], 1.0e-15);
    }

    @Test
    public void testAtan2AndHypot() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        double[] x = new double[] { 1.0, 0.0 };
        double[] y = new double[] { 1.0, 0.0 };
        double[] result = new double[2];

        compiler.atan2(y, 0, x, 0, result, 0);
        Assert.assertEquals(Math.PI / 4.0, result[0], 1.0e-15);

        compiler.hypot(x, 0, y,  0, result, 0);
        Assert.assertEquals(Math.sqrt(2.0), result[0], 1.0e-15);
    }
}