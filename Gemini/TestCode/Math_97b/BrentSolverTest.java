package org.apache.commons.math.analysis;

import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.FunctionEvaluationException;
import org.junit.Assert;
import org.junit.Test;

public class BrentSolverTest {

    @Test
    public void testRootAlreadyExact() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double root = solver.solve(2.0, 5.0);
        Assert.assertEquals(2.0, root, 1E-12);
    }

    @Test
    public void testRootAlreadyExactLower() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double root = solver.solve(2.0, 0.0, 2.0);
        Assert.assertEquals(2.0, root, 1E-12);
    }

    @Test
    public void testFunctionValuesSignTheSame() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        try {
            solver.solve(0.0, 2.0);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testFunctionValuesSignTheSameWithInitial() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x + 1.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        try {
            solver.solve(0.0, 2.0, 1.0);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testStandardSolveLinear() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.5;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double root = solver.solve(0.0, 3.0);
        Assert.assertEquals(1.5, root, 1E-6);
    }

    @Test
    public void testStandardSolveWithInitial() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double root = solver.solve(0.0, 3.0, 2.1);
        Assert.assertEquals(2.0, root, 1E-6);
    }

    @Test
    public void testInitialIsRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double root = solver.solve(0.0, 3.0, 2.0);
        Assert.assertEquals(2.0, root, 1E-12);
    }

    @Test
    public void testInitialOutsideBounds() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        try {
            solver.solve(0.0, 3.0, 4.0);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testConvergenceOnFunction() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return Math.cos(x) - x;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        double root = solver.solve(0.0, Math.PI / 2);
        Assert.assertEquals(0.7390851332, root, 1E-6);
    }

    @Test
    public void testSolveWithAbsoluteAccuracy() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        solver.setAbsoluteAccuracy(1E-4);
        double root = solver.solve(0.0, 2.0);
        Assert.assertEquals(1.0, root, 1E-3);
    }

    @Test
    public void testMath97Trigger() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f);
        // In Math 97, if min and max bracket a root where f(min)*f(max) == 0 or similar boundary conditions,
        // or check for non-bracketing when roots are at boundaries.
        // Let's test endpoints that evaluate to zero closely or verify the exact condition of Math 97.
        double root = solver.solve(0.0, 1.0);
        Assert.assertEquals(0.0, root, 1E-12);
    }
    
    @Test
    public void testEndpointIsRoot() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return exp(x) - 1.0; // placeholder, or simply x
            }
            private double exp(double x) { return Math.exp(x); }
        };
        // Let's use a simpler polynomial
        UnivariateRealFunction f2 = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        BrentSolver solver = new BrentSolver(f2);
        // min = 0.0 -> f(min) = 0.0
        double root = solver.solve(0.0, 1.0);
        Assert.assertEquals(0.0, root, 1E-12);

        // max = 0.0 -> f(max) = 0.0
        double root2 = solver.solve(-1.0, 0.0);
        Assert.assertEquals(0.0, root2, 1E-12);
    }
}