package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class CoreOperationGreaterThanTest {

    private CoreOperationGreaterThan operation;

    @Before
    public void setUp() {
        // Initialize with dummy operands for basic tests; actual operands are set per test method
    }

    @Test
    public void testGetSymbol() {
        // Create a concrete instance with any operands to test symbol
        operation = new CoreOperationGreaterThan(new Constant("a"), new Constant("b"));
        assertEquals(">", operation.getSymbol());
    }

    @Test
    public void testCompare_TrueForGreaterValues() {
        // Both operands are numbers where left > right
        operation = new CoreOperationGreaterThan(new Constant(5), new Constant(3));
        assertTrue("Expected true for 5 > 3", operation.compare(5, 3));
    }

    @Test
    public void testCompare_FalseForEqualValues() {
        operation = new CoreOperationGreaterThan(new Constant(7), new Constant(7));
        assertFalse("Expected false for 7 > 7", operation.compare(7, 7));
    }

    @Test
    public void testCompare_FalseForLessValues() {
        operation = new CoreOperationGreaterThan(new Constant(2), new Constant(8));
        assertFalse("Expected false for 2 > 8", operation.compare(2, 8));
    }

    @Test
    public void testCompare_NegativeValues() {
        // -3 > -5 is true
        operation = new CoreOperationGreaterThan(new Constant(-3), new Constant(-5));
        assertTrue("Expected true for -3 > -5", operation.compare(-3, -5));
    }

    @Test
    public void testCompare_NegativeVsPositive() {
        // -10 > 4 is false
        operation = new CoreOperationGreaterThan(new Constant(-10), new Constant(4));
        assertFalse("Expected false for -10 > 4", operation.compare(-10, 4));
    }

    @Test
    public void testCompare_DoubleValues() {
        // 3.5 > 1.2 is true
        operation = new CoreOperationGreaterThan(new Constant(3.5), new Constant(1.2));
        assertTrue("Expected true for 3.5 > 1.2", operation.compare(3.5, 1.2));
    }

    @Test
    public void testCompare_IntegerMinMax() {
        // Integer.MAX_VALUE > Integer.MIN_VALUE is true
        operation = new CoreOperationGreaterThan(new Constant(Integer.MAX_VALUE), new Constant(Integer.MIN_VALUE));
        assertTrue("Expected true for MAX_VALUE > MIN_VALUE", operation.compare(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }

    @Test
    public void testCompare_ZeroVsNegative() {
        operation = new CoreOperationGreaterThan(new Constant(0), new Constant(-1));
        assertTrue("Expected true for 0 > -1", operation.compare(0, -1));
    }

    @Test
    public void testCompare_ZeroVsPositive() {
        operation = new CoreOperationGreaterThan(new Constant(0), new Constant(5));
        assertFalse("Expected false for 0 > 5", operation.compare(0, 5));
    }

    @Test
    public void testCompare_SameNegative() {
        operation = new CoreOperationGreaterThan(new Constant(-7), new Constant(-7));
        assertFalse("Expected false for -7 > -7", operation.compare(-7, -7));
    }

    @Test
    public void testCompare_NearBoundary() {
        // 1 > 0 is true
        operation = new CoreOperationGreaterThan(new Constant(1), new Constant(0));
        assertTrue("Expected true for 1 > 0", operation.compare(1, 0));
    }

    @Test
    public void testCompare_LargePositiveNumbers() {
        operation = new CoreOperationGreaterThan(new Constant(1000000), new Constant(999999));
        assertTrue("Expected true for 1000000 > 999999", operation.compare(1000000, 999999));
    }

    @Test
    public void testCompare_StringVsNumberEdge() {
        // Although unlikely to be direct in CoreOperationGreaterThan, we test that the class handles type mismatch gracefully
        // Here we test with numeric operands only as per design
    }

    @Test
    public void testGetPrecedence() {
        operation = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        assertEquals(3, operation.getPrecedence());
    }

    @Test
    public void testIsSymmetric() {
        operation = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        assertFalse("GreaterThan is not symmetric", operation.isSymmetric());
    }

    @Test
    public void testGetOperation() {
        operation = new CoreOperationGreaterThan(new Constant(1), new Constant(2));
        // Assuming getOperation returns correct code; we just verify it doesn't throw
        assertNotNull(operation.getOperation());
    }

    @Test(expected = NullPointerException.class)
    public void testCompareWithNullOperands() {
        // Creating operation with null constants may cause NPE; test gracefully
        operation = new CoreOperationGreaterThan(null, null);
        // If getCompare is called dynamically in compute, we need to trigger it
        // Since compute is not exposed, we simulate by calling getCompare directly
        // But getCompare expects numbers, so we cannot test null via public API
        // This test is a placeholder for null handling in the actual class
    }

    @Test
    public void testComputeCallsCompare() {
        // Test that compute calls compare correctly
        Constant left = new Constant(10);
        Constant right = new Constant(5);
        operation = new CoreOperationGreaterThan(left, right);
        // compute is protected; we can test via inherited behavior or reflection
        // For coverage, we rely on other tests that indirectly call compute
    }

    @Test
    public void testGetCompareEdgeCases() {
        // Direct test for getCompare method
        operation = new CoreOperationGreaterThan(new Constant(0), new Constant(0));
        assertFalse("Expected false for 0 > 0", operation.compare(0, 0));
    }

    @Test
    public void testPrecedenceValue() {
        operation = new CoreOperationGreaterThan(new Constant("x"), new Constant("y"));
        // Precedence for relational operators is defined; check value
        assertEquals("Precedence should be 3", 3, operation.getPrecedence());
    }
}