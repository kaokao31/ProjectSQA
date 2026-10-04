package org.apache.commons.math3.stat.inference;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.apache.commons.math3.exception.MathIllegalArgumentException;

public class MannWhitneyUTestTest {

    private MannWhitneyUTest test;

    @Before
    public void setUp() {
        test = new MannWhitneyUTest();
    }

    // ---------- mannWhitneyU ----------

    @Test
    public void testMannWhitneyUSimple() {
        double[] x = {1, 2, 3};
        double[] y = {4, 5, 6};
        double u = test.mannWhitneyU(x, y);
        // All x < y => U = 0 (or 9 depending on definition)
        // Typically U = sum of ranks of x minus n1*(n1+1)/2
        // For this data, ranks: x:1,2,3; y:4,5,6 => sum ranks x = 1+2+3=6, n1=3 => U = 6 - 3*4/2 = 6-6=0
        assertEquals(0.0, u, 1e-10);
    }

    @Test
    public void testMannWhitneyUReversed() {
        double[] x = {4, 5, 6};
        double[] y = {1, 2, 3};
        double u = test.mannWhitneyU(x, y);
        // All x > y => U = n1*n2 = 9
        assertEquals(9.0, u, 1e-10);
    }

    @Test
    public void testMannWhitneyUWithTies() {
        double[] x = {1, 2, 2, 3};
        double[] y = {2, 3, 4};
        double u = test.mannWhitneyU(x, y);
        // Compute expected: combined sorted: [1(x),2(x),2(x),2(y),3(x),3(y),4(y)]
        // Ranks: 1->1, 2->(2+3+4)/3=3, 3->(5+6)/2=5.5, 4->7
        // Sum ranks x: 1 + 3 + 3 + 5.5 = 12.5
        // n1=4 => U = 12.5 - 4*5/2 = 12.5 - 10 = 2.5
        assertEquals(2.5, u, 1e-10);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUNaNInX() {
        double[] x = {1, Double.NaN, 3};
        double[] y = {4, 5, 6};
        test.mannWhitneyU(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUNaNInY() {
        double[] x = {1, 2, 3};
        double[] y = {4, Double.NaN, 6};
        test.mannWhitneyU(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUEmptyX() {
        double[] x = {};
        double[] y = {1, 2, 3};
        test.mannWhitneyU(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUEmptyY() {
        double[] x = {1, 2, 3};
        double[] y = {};
        test.mannWhitneyU(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUNullX() {
        double[] x = null;
        double[] y = {1, 2, 3};
        test.mannWhitneyU(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUNullY() {
        double[] x = {1, 2, 3};
        double[] y = null;
        test.mannWhitneyU(x, y);
    }

    // ---------- mannWhitneyUTest ----------

    @Test
    public void testMannWhitneyUTestSimple() {
        double[] x = {1, 2, 3};
        double[] y = {4, 5, 6};
        double p = test.mannWhitneyUTest(x, y);
        // For U=0, n1=3,n2=3, exact p-value = 0.05 (one-sided? two-sided? Usually two-sided)
        // In Commons Math, it returns two-sided p-value. For this data, p should be 0.05 (exact)
        assertEquals(0.05, p, 1e-10);
    }

    @Test
    public void testMannWhitneyUTestReversed() {
        double[] x = {4, 5, 6};
        double[] y = {1, 2, 3};
        double p = test.mannWhitneyUTest(x, y);
        // U=9, same as above, p=0.05
        assertEquals(0.05, p, 1e-10);
    }

    @Test
    public void testMannWhitneyUTestWithTies() {
        double[] x = {1, 2, 2, 3};
        double[] y = {2, 3, 4};
        double p = test.mannWhitneyUTest(x, y);
        // With ties, exact p-value is not trivial; we just check it's between 0 and 1
        assertTrue(p >= 0 && p <= 1);
    }

    @Test
    public void testMannWhitneyUTestLargeSample() {
        // Use normal approximation
        double[] x = new double[20];
        double[] y = new double[20];
        for (int i = 0; i < 20; i++) {
            x[i] = i;
            y[i] = i + 10; // shift
        }
        double p = test.mannWhitneyUTest(x, y);
        assertTrue(p >= 0 && p <= 1);
        // Should be very small because distributions are separated
        assertTrue(p < 0.05);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUTestNaNX() {
        double[] x = {1, Double.NaN, 3};
        double[] y = {4, 5, 6};
        test.mannWhitneyUTest(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUTestNaNY() {
        double[] x = {1, 2, 3};
        double[] y = {4, Double.NaN, 6};
        test.mannWhitneyUTest(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUTestEmptyX() {
        double[] x = {};
        double[] y = {1, 2, 3};
        test.mannWhitneyUTest(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUTestEmptyY() {
        double[] x = {1, 2, 3};
        double[] y = {};
        test.mannWhitneyUTest(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUTestNullX() {
        double[] x = null;
        double[] y = {1, 2, 3};
        test.mannWhitneyUTest(x, y);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMannWhitneyUTestNullY() {
        double[] x = {1, 2, 3};
        double[] y = null;
        test.mannWhitneyUTest(x, y);
    }

    // ---------- Edge cases ----------

    @Test
    public void testMannWhitneyUAllEqual() {
        double[] x = {1, 1, 1};
        double[] y = {1, 1, 1};
        double u = test.mannWhitneyU(x, y);
        // All equal: ranks all tied, sum ranks x = (1+2+3+4+5+6)/2 = 10.5? Actually combined size=6, ranks 1..6, all tied => each gets average rank 3.5
        // Sum ranks x = 3*3.5 = 10.5, n1=3 => U = 10.5 - 3*4/2 = 10.5 - 6 = 4.5
        assertEquals(4.5, u, 1e-10);
    }

    @Test
    public void testMannWhitneyUTestAllEqual() {
        double[] x = {1, 1, 1};
        double[] y = {1, 1, 1};
        double p = test.mannWhitneyUTest(x, y);
        // With ties, p-value should be 1.0 (exact? approximation?)
        // For identical samples, p=1
        assertEquals(1.0, p, 1e-10);
    }

    @Test
    public void testMannWhitneyUOneElementEach() {
        double[] x = {5};
        double[] y = {3};
        double u = test.mannWhitneyU(x, y);
        // x > y => U = 1
        assertEquals(1.0, u, 1e-10);
    }

    @Test
    public void testMannWhitneyUTestOneElementEach() {
        double[] x = {5};
        double[] y = {3};
        double p = test.mannWhitneyUTest(x, y);
        // n1=1,n2=1, U=1 => two-sided p = 1.0? Actually exact: possible U values 0 or 1, each probability 0.5, two-sided p=1
        assertEquals(1.0, p, 1e-10);
    }

    @Test
    public void testMannWhitneyUWithNegativeValues() {
        double[] x = {-5, -3, -1};
        double[] y = {0, 2, 4};
        double u = test.mannWhitneyU(x, y);
        // All x < y => U = 0
        assertEquals(0.0, u, 1e-10);
    }

    @Test
    public void testMannWhitneyUTestWithNegativeValues() {
        double[] x = {-5, -3, -1};
        double[] y = {0, 2, 4};
        double p = test.mannWhitneyUTest(x, y);
        assertEquals(0.05, p, 1e-10);
    }

    // ---------- Additional coverage: large values to test overflow ----------

    @Test
    public void testMannWhitneyULargeValues() {
        double[] x = {1e10, 2e10, 3e10};
        double[] y = {4e10, 5e10, 6e10};
        double u = test.mannWhitneyU(x, y);
        assertEquals(0.0, u, 1e-10);
    }

    @Test
    public void testMannWhitneyUTestLargeValues() {
        double[] x = {1e10, 2e10, 3e10};
        double[] y = {4e10, 5e10, 6e10};
        double p = test.mannWhitneyUTest(x, y);
        assertEquals(0.05, p, 1e-10);
    }

    // ---------- Test that the statistic is symmetric ----------

    @Test
    public void testMannWhitneyUSymmetric() {
        double[] x = {1, 4, 6};
        double[] y = {2, 3, 5};
        double uxy = test.mannWhitneyU(x, y);
        double uyx = test.mannWhitneyU(y, x);
        assertEquals(uxy + uyx, x.length * y.length, 1e-10);
    }
}