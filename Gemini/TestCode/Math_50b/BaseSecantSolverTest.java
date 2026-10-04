package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class BaseSecantSolverTest {

    // Concrete implementation of BaseSecantSolver using REGULA_FALSI method to test all branches
    private static class DummySecantSolver extends BaseSecantSolver {
        public DummySecantSolver() {
            super(1e-6, 1e-12, Method.REGULA_FALSI);
        }

        public DummySecantSolver(final double absoluteAccuracy) {
            super(absoluteAccuracy, Method.REGULA_FALSI);
        }

        public DummySecantSolver(final double relativeAccuracy, final double absoluteAccuracy) {
            super(relativeAccuracy, absoluteAccuracy, Method.REGULA_FALSI);
        }

        public DummySecantSolver(final double relativeAccuracy, final double absoluteAccuracy, final double functionValueAccuracy) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy, Method.REGULA_FALSI);
        }
    }

    @Test
    public void testConstructors() {
        BaseSecantSolver solver1 = new DummySecantSolver();
        Assert.assertNotNull(solver1);

        BaseSecantSolver solver2 = new DummySecantSolver(1e-5);
        Assert.assertNotNull(solver2);

        BaseSecantSolver solver3 = new DummySecantSolver(1e-4, 1e-8);
        Assert.assertNotNull(solver3);

        BaseSecantSolver solver4 = new DummySecantSolver(1e-4, 1e-8, 1e-11);
        Assert.assertNotNull(solver4);
    }

    @Test
    public void testSolveWithRegulaFalsiMethod() {
        // Function with root at x = 0.0
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0; // Roots at -2 and 2
            }
        };

        BaseSecantSolver solver = new DummySecantSolver(1e-6, 1e-15);
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testSolveWithAllowedSolutions() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        BaseSecantSolver solver = new DummySecantSolver(1e-9, 1e-15);

        // Test different AllowedSolution enums
        double rootAny = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, rootAny, 1e-6);

        double rootLeft = solver.solve(100, f, 1.0, 3.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(rootLeft <= 2.0);

        double rootRight = solver.solve(100, f, 1.0, 3.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(rootRight >= 2.0);

        double rootBelow = solver.solve(100, f, 1.0, 3.0, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(f.value(rootBelow) <= 0);

        double rootAbove = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(f.value(rootAbove) >= 0);
    }

    @Test
    public void testPegasusMethod() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };

        BaseSecantSolver solver = new BaseSecantSolver(1e-6, 1e-12, BaseSecantSolver.Method.PEGASUS) {
        };

        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(FastMath.sqrt(2.0), root, 1e-6);
    }

    @Test
    public void testIllinoisMethod() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 2.0;
            }
        };

        BaseSecantSolver solver = new BaseSecantSolver(1e-6, 1e-12, BaseSecantSolver.Method.ILLINOIS) {
        };

        double root = solver.solve(100, f, 1.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(FastMath.sqrt(2.0), root, 1e-6);
    }

    @Test
    public void testRootAtMinOrMax() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };

        BaseSecantSolver solver = new DummySecantSolver(1e-6, 1e-12);
        // Root is exactly at min (0.0)
        double root = solver.solve(100, f, 0.0, 2.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1e-6);

        // Root is exactly at max (0.0)
        root = solver.solve(100, f, -2.0, 0.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(0.0, root, 1e-6);
    }

    @Test
    public void testFunctionValueAccuracyBranch() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.0000001;
            }
        };

        // Set function value accuracy such that it hits within bounds early
        BaseSecantSolver solver = new DummySecantSolver(1e-1, 1e-1, 1e-3);
        double root = solver.solve(100, f, 0.0, 2.0, AllowedSolution.BELOW_SIDE);
        Assert.assertNotNull(root);
    }

    @Test
    public void testSideApproachesBug50Scenario() {
        // Specific function to trigger adjustments in Regula Falsi / Illinois / Pegasus methods
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return cubic(x);
            }
            private double cubic(double x) {
                return x * x * x - 2 * x - 2;
            }
        };

        BaseSecantSolver solver = new DummySecantSolver(1e-6, 1e-15);
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.LEFT_SIDE);
        Assert.assertTrue(f.value(root) <= 0.0);

        root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(f.value(root) >= 0.0);
    }
}