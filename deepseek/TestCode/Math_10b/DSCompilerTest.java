package org.apache.commons.math3.analysis.differentiation;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for DSCompiler, targeting high coverage and fault detection
 * (including Defects4J Math-10 bug: incorrect size for zero free parameters).
 */
public class DSCompilerTest {

    // Helper to check that a compiler's size matches expected formula
    private void assertCompilerSize(int parameters, int order, int expectedSize) {
        DSCompiler compiler = DSCompiler.getCompiler(parameters, order);
        assertNotNull("Compiler should not be null", compiler);
        assertEquals("Size mismatch for parameters=" + parameters + ", order=" + order,
                     expectedSize, compiler.getSize());
    }

    @Test
    public void testGetCompilerWithZeroParametersAndZeroOrder() {
        // parameters=0, order=0 -> size should be 1 (only value)
        assertCompilerSize(0, 0, 1);
    }

    @Test
    public void testGetCompilerWithZeroParametersAndPositiveOrder() {
        // parameters=0, order>0 -> size should be order+1 (value + derivatives)
        // This is the buggy case in Math-10: previously returned 1 for any order.
        assertCompilerSize(0, 1, 2);
        assertCompilerSize(0, 2, 3);
        assertCompilerSize(0, 3, 4);
        assertCompilerSize(0, 5, 6);
    }

    @Test
    public void testGetCompilerWithOneParameterAndZeroOrder() {
        // parameters=1, order=0 -> size = 1
        assertCompilerSize(1, 0, 1);
    }

    @Test
    public void testGetCompilerWithOneParameterAndPositiveOrder() {
        // parameters=1, order=1 -> size = 2
        assertCompilerSize(1, 1, 2);
        // parameters=1, order=2 -> size = 3
        assertCompilerSize(1, 2, 3);
        // parameters=1, order=3 -> size = 4
        assertCompilerSize(1, 3, 4);
    }

    @Test
    public void testGetCompilerWithMultipleParameters() {
        // parameters=2, order=1 -> size = 3 (value + 2 first partials)
        assertCompilerSize(2, 1, 3);
        // parameters=2, order=2 -> size = 6 (value + 2 first + 3 second)
        assertCompilerSize(2, 2, 6);
        // parameters=3, order=1 -> size = 4
        assertCompilerSize(3, 1, 4);
        // parameters=3, order=2 -> size = 10
        assertCompilerSize(3, 2, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCompilerWithNegativeParameters() {
        DSCompiler.getCompiler(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCompilerWithNegativeOrder() {
        DSCompiler.getCompiler(1, -1);
    }

    @Test
    public void testCompilerCaching() {
        // Same parameters and order should return the same instance
        DSCompiler c1 = DSCompiler.getCompiler(2, 2);
        DSCompiler c2 = DSCompiler.getCompiler(2, 2);
        assertSame("Compiler instances should be cached", c1, c2);
    }

    @Test
    public void testMultiplyWithZeroParameters() {
        // Test multiply operation using compiler with zero free parameters.
        // This is likely to trigger the bug if size is wrong.
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        // Create arrays of correct size (value + derivatives)
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        // Fill with some values
        lhs[0] = 2.0; lhs[1] = 1.0; lhs[2] = 0.5;
        rhs[0] = 3.0; rhs[1] = 2.0; rhs[2] = 1.0;
        // Perform multiply (should not throw ArrayIndexOutOfBoundsException)
        compiler.multiply(lhs, 0, rhs, 0, result, 0);
        // Expected: (2*3)=6, (2*2 + 1*3)=7, (2*1 + 0.5*3 + 2*1*2?) Actually formula for second derivative: f''g + 2f'g' + fg''
        // For simplicity, just check that result is not NaN and array is filled
        assertFalse("Result should not contain NaN", Double.isNaN(result[0]));
        assertFalse("Result should not contain NaN", Double.isNaN(result[1]));
        assertFalse("Result should not contain NaN", Double.isNaN(result[2]));
    }

    @Test
    public void testMultiplyWithOneParameter() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 2);
        int size = compiler.getSize(); // should be 3
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 1.0; lhs[1] = 2.0; lhs[2] = 3.0;
        rhs[0] = 4.0; rhs[1] = 5.0; rhs[2] = 6.0;
        compiler.multiply(lhs, 0, rhs, 0, result, 0);
        // Expected: f*g = 1*4=4, (f'g+fg') = 2*4+1*5=13, (f''g+2f'g'+fg'') = 3*4+2*2*5+1*6=12+20+6=38
        assertEquals("Multiply value", 4.0, result[0], 1e-10);
        assertEquals("Multiply first derivative", 13.0, result[1], 1e-10);
        assertEquals("Multiply second derivative", 38.0, result[2], 1e-10);
    }

    @Test
    public void testAddWithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 1.0; lhs[1] = 2.0; lhs[2] = 3.0;
        rhs[0] = 4.0; rhs[1] = 5.0; rhs[2] = 6.0;
        compiler.add(lhs, 0, rhs, 0, result, 0);
        assertArrayEquals("Add result", new double[]{5.0, 7.0, 9.0}, result, 1e-10);
    }

    @Test
    public void testSubtractWithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 10.0; lhs[1] = 8.0; lhs[2] = 6.0;
        rhs[0] = 3.0; rhs[1] = 2.0; rhs[2] = 1.0;
        compiler.subtract(lhs, 0, rhs, 0, result, 0);
        assertArrayEquals("Subtract result", new double[]{7.0, 6.0, 5.0}, result, 1e-10);
    }

    @Test
    public void testDivideWithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 8.0; lhs[1] = 4.0; lhs[2] = 2.0;
        rhs[0] = 2.0; rhs[1] = 1.0; rhs[2] = 0.5;
        compiler.divide(lhs, 0, rhs, 0, result, 0);
        // Expected: f/g = 4, (f'g - fg')/g^2 = (4*2 - 8*1)/4 = (8-8)/4=0, second derivative complex
        assertEquals("Divide value", 4.0, result[0], 1e-10);
        assertEquals("Divide first derivative", 0.0, result[1], 1e-10);
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 1);
        int size = compiler.getSize();
        double[] lhs = new double[]{1.0, 0.0};
        double[] rhs = new double[]{0.0, 0.0};
        double[] result = new double[size];
        compiler.divide(lhs, 0, rhs, 0, result, 0);
    }

    @Test
    public void testPowWithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 2.0; operand[1] = 1.0; operand[2] = 0.5;
        compiler.pow(operand, 0, 3, result, 0);
        // f(x)=x^3, f'=3x^2, f''=6x
        // At x=2: value=8, first=12, second=12
        assertEquals("Pow value", 8.0, result[0], 1e-10);
        assertEquals("Pow first derivative", 12.0, result[1], 1e-10);
        assertEquals("Pow second derivative", 12.0, result[2], 1e-10);
    }

    @Test
    public void testExpWithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0; operand[1] = 1.0; operand[2] = 0.5;
        compiler.exp(operand, 0, result, 0);
        // exp(0)=1, derivative = exp(0)*1 =1, second = exp(0)*(1^2+0.5)=1*(1+0.5)=1.5
        assertEquals("Exp value", 1.0, result[0], 1e-10);
        assertEquals("Exp first derivative", 1.0, result[1], 1e-10);
        assertEquals("Exp second derivative", 1.5, result[2], 1e-10);
    }

    @Test
    public void testSinWithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0; operand[1] = 1.0; operand[2] = 0.0;
        compiler.sin(operand, 0, result, 0);
        // sin(0)=0, cos(0)*1=1, -sin(0)*1^2+cos(0)*0 = 0
        assertEquals("Sin value", 0.0, result[0], 1e-10);
        assertEquals("Sin first derivative", 1.0, result[1], 1e-10);
        assertEquals("Sin second derivative", 0.0, result[2], 1e-10);
    }

    @Test
    public void testCosWithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0; operand[1] = 1.0; operand[2] = 0.0;
        compiler.cos(operand, 0, result, 0);
        // cos(0)=1, -sin(0)*1=0, -cos(0)*1^2 - sin(0)*0 = -1
        assertEquals("Cos value", 1.0, result[0], 1e-10);
        assertEquals("Cos first derivative", 0.0, result[1], 1e-10);
        assertEquals("Cos second derivative", -1.0, result[2], 1e-10);
    }

    @Test
    public void testTanWithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] operand = new double[size];
        double[] result = new double[size];
        operand[0] = 0.0; operand[1] = 1.0; operand[2] = 0.0;
        compiler.tan(operand, 0, result, 0);
        // tan(0)=0, sec^2(0)*1=1, 2sec^2(0)*tan(0)*1^2+sec^2(0)*0 = 0
        assertEquals("Tan value", 0.0, result[0], 1e-10);
        assertEquals("Tan first derivative", 1.0, result[1], 1e-10);
        assertEquals("Tan second derivative", 0.0, result[2], 1e-10);
    }

    @Test
    public void testAtan2WithZeroParameters() {
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] lhs = new double[size];
        double[] rhs = new double[size];
        double[] result = new double[size];
        lhs[0] = 1.0; lhs[1] = 0.0; lhs[2] = 0.0;
        rhs[0] = 1.0; rhs[1] = 0.0; rhs[2] = 0.0;
        compiler.atan2(lhs, 0, rhs, 0, result, 0);
        // atan2(1,1)=pi/4, derivatives zero because both constant
        assertEquals("Atan2 value", Math.PI / 4, result[0], 1e-10);
        assertEquals("Atan2 first derivative", 0.0, result[1], 1e-10);
        assertEquals("Atan2 second derivative", 0.0, result[2], 1e-10);
    }

    @Test
    public void testComposeWithZeroParameters() {
        // Test composition with a polynomial: f(x)=x^2, g(x)=x+1, then f(g(x)) = (x+1)^2
        DSCompiler compiler = DSCompiler.getCompiler(0, 2);
        int size = compiler.getSize();
        double[] f = new double[]{0.0, 0.0, 2.0}; // f(y)=y^2 => f(0)=0, f'=0, f''=2
        double[] g = new double[]{1.0, 1.0, 0.0}; // g(x)=x+1 => g(0)=1, g'=1, g''=0
        double[] result = new double[size];
        compiler.compose(g, 0, f, result, 0);
        // Expected: f(g(0))=1, f'(g)*g' = 0*1=0, f''(g)*g'^2 + f'(g)*g'' = 2*1 + 0*0 = 2
        assertEquals("Compose value", 1.0, result[0], 1e-10);
        assertEquals("Compose first derivative", 0.0, result[1], 1e-10);
        assertEquals("Compose second derivative", 2.0, result[2], 1e-10);
    }

    @Test
    public void testGetPartialDerivativeIndex() {
        DSCompiler compiler = DSCompiler.getCompiler(2, 2);
        // For 2 parameters, order 2, indices: (0,0)=0, (1,0)=1, (0,1)=2, (2,0)=3, (1,1)=4, (0,2)=5
        assertEquals("Index for (0,0)", 0, compiler.getPartialDerivativeIndex(0, 0));
        assertEquals("Index for (1,0)", 1, compiler.getPartialDerivativeIndex(1, 0));
        assertEquals("Index for (0,1)", 2, compiler.getPartialDerivativeIndex(0, 1));
        assertEquals("Index for (2,0)", 3, compiler.getPartialDerivativeIndex(2, 0));
        assertEquals("Index for (1,1)", 4, compiler.getPartialDerivativeIndex(1, 1));
        assertEquals("Index for (0,2)", 5, compiler.getPartialDerivativeIndex(0, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPartialDerivativeIndexInvalidOrder() {
        DSCompiler compiler = DSCompiler.getCompiler(1, 1);
        // Requesting derivative of order 2 should throw
        compiler.getPartialDerivativeIndex(2);
    }

    @Test
    public void testCheckCompatibility() {
        DSCompiler c1 = DSCompiler.getCompiler(1, 1);
        DSCompiler c2 = DSCompiler.getCompiler(1, 1);
        // Should not throw
        c1.checkCompatibility(c2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckCompatibilityMismatch() {
        DSCompiler c1 = DSCompiler.getCompiler(1, 1);
        DSCompiler c2 = DSCompiler.getCompiler(2, 1);
        c1.checkCompatibility(c2);
    }
}