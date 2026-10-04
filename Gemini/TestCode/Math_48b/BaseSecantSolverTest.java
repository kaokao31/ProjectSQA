package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class BaseSecantSolverTest {

    // Dummy concrete implementation of BaseSecantSolver to test its methods
    private static class DummySecantSolver extends BaseSecantSolver {
        protected DummySecantSolver(double absoluteAccuracy) {
            super(absoluteAccuracy);
        }

        protected DummySecantSolver(double relativeAccuracy, double absoluteAccuracy) {
            super(relativeAccuracy, absoluteAccuracy);
        }

        protected DummySecantSolver(double relativeAccuracy, double absoluteAccuracy, double functionValueAccuracy) {
            super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy);
        }

        @Override
        protected final double doSolve() {
            // Using REGULA_FALSI as default for testing method dispatch
            returnprimSolve();
            return 0.0;
        }

        public double primSolve() {
            return super.solve(100, new UnivariateFunction() {
                public double value(double x) {
                    return x;
                }
            }, -1.0, 1.0, AllowedSolution.ANY_SIDE);
        }
    }

    @Test
    public void testConstructors() {
        BaseSecantSolver solver1 = new RegulaFalsiSolver();
        Assert.assertEquals(1e-6, solver1.getAbsoluteAccuracy(), 1e-15);

        BaseSecantSolver solver2 = new RegulaFalsiSolver(1e-8);
        Assert.assertEquals(1e-8, solver2.getAbsoluteAccuracy(), 1e-15);

        BaseSecantSolver solver3 = new RegulaFalsiSolver(1e-8, 1e-9);
        Assert.assertEquals(1e-8, solver3.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-9, solver3.getAbsoluteAccuracy(), 1e-15);

        BaseSecantSolver solver4 = new RegulaFalsiSolver(1e-8, 1e-9, 1e-10);
        Assert.assertEquals(1e-8, solver4.getRelativeAccuracy(), 1e-15);
        Assert.assertEquals(1e-9, solver4.getAbsoluteAccuracy(), 1e-15);
        Assert.assertEquals(1e-10, solver4.getFunctionValueAccuracy(), 1e-15);
    }

    @Test
    public void testSolveWithRegulaFalsi() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BaseSecantSolver solver = new RegulaFalsiSolver();
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testSolveWithIllinois() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BaseSecantSolver solver = new IllinoisSolver();
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testSolveWithPegasus() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        BaseSecantSolver solver = new PegasusSolver();
        double root = solver.solve(100, f, 1.0, 3.0, AllowedSolution.ANY_SIDE);
        Assert.assertEquals(2.0, root, 1e-6);
    }

    @Test
    public void testAllowedSolutions() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };
        
        for (BaseSecantSolver solver : new BaseSecantSolver[] {
            new RegulaFalsiSolver(),
            new IllinoisSolver(),
            new PegasusSolver()
        }) {
            for (AllowedSolution allowed : AllowedSolution.values()) {
                double root = solver.solve(100, f, 1.0, 3.0, allowed);
                Assert.assertTrue(root >= 2.0 - 1e-3 && root <= 2.0 + 1e-3);
            }
        }
    }

    @Test
    public void testFunctionValueAccuracyBranch() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return x - 2.0;
            }
        };
        // Set exact root match conditions
        BaseSecantSolver solver = new RegulaFalsiSolver(1e-6, 1e-6, 1e-2);
        double root = solver.solve(100, f, 1.9, 2.1, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(FastMath.abs(f.value(root)) <= 1e-2);
    }

    @Test
    public void testConvergenceOnEdgeValues() {
        UnivariateFunction f = new UnivariateFunction() {
            public double value(double x) {
                return Math.sin(x);
            }
        };
        BaseSecantSolver solver = new IllinoisSolver();
        double root = solver.solve(100, f, -0.1, 0.1, AllowedSolution.RIGHT_SIDE);
        Assert.assertEquals(0.0, root, 1e-4);
    }

    @Test(expected = org.apache.commons.math.exception.NullArgumentException.class)
    public void testNullFunction() {
        BaseSecantSolver solver = new RegulaFalsiSolver();
        solver.solve(100, null, 1.0, 2.0);
    }
}