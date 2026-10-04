package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

public class BaseSecantSolverTest {

    private static final double RELATIVE_ACCURACY = 1e-9;
    private static final double ABSOLUTE_ACCURACY = 1e-9;
    private static final double FUNCTION_VALUE_ACCURACY = 1e-9;

    @Test
    public void testRegulaFalsiSolver() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        BaseSecantSolver solver = new RegulaFalsiSolver(ABSOLUTE_ACCURACY, RELATIVE_ACCURACY);
        double result = solver.solve(100, f, 0.0, 3.0);
        Assert.assertEquals(2.0, result, 1e-6);
    }

    @Test
    public void testIllinoisSolver() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        BaseSecantSolver solver = new IllinoisSolver(ABSOLUTE_ACCURACY, RELATIVE_ACCURACY, FUNCTION_VALUE_ACCURACY);
        double result = solver.solve(100, f, 0.0, 3.0);
        Assert.assertEquals(2.0, result, 1e-6);
    }

    @Test
    public void testPegasusSolver() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        BaseSecantSolver solver = new PegasusSolver(ABSOLUTE_ACCURACY, RELATIVE_ACCURACY);
        double result = solver.solve(100, f, 0.0, 3.0);
        Assert.assertEquals(2.0, result, 1e-6);
    }

    @Test
    public void testCubicFunction() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x * x - x - 2.0;
            }
        };

        BaseSecantSolver solver = new PegasusSolver();
        double result = solver.solve(100, f, 1.0, 2.0);
        // Root is approx 1.5213797
        Assert.assertEquals(1.5213797, result, 1e-5);
    }

    @Test
    public void testFunctionValueAccuracy() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };

        BaseSecantSolver solver = new RegulaFalsiSolver();
        // Hit the function value accuracy condition directly
        double result = solver.solve(100, f, -0.1, 0.1);
        Assert.assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void testAllowedMethods() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x * x - 4.0;
            }
        };

        for (AllowedSolution allowed : AllowedSolution.values()) {
            BaseSecantSolver solver = new IllinoisSolver();
            double result = solver.solve(100, f, 0.0, 3.0, allowed);
            Assert.assertTrue(result >= 0.0 && result <= 3.0);
        }
    }

    @Test
    public void testPegasusMethodBranchCoverage() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.sin(x);
            }
        };

        BaseSecantSolver solver = new PegasusSolver(ABSOLUTE_ACCURACY, RELATIVE_ACCURACY);
        double result = solver.solve(100, f, -1.0, 2.0);
        Assert.assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void testIllinoisMethodBranchCoverage() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return FastMath.cos(x);
            }
        };

        BaseSecantSolver solver = new IllinoisSolver(ABSOLUTE_ACCURACY, RELATIVE_ACCURACY);
        double result = solver.solve(100, f, 0.0, 2.0);
        Assert.assertEquals(Math.PI / 2.0, result, 1e-4);
    }

    @Test
    public void testRegulaFalsiMethodBranchCoverage() {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x - 1.5;
            }
        };

        BaseSecantSolver solver = new RegulaFalsiSolver(ABSOLUTE_ACCURACY, RELATIVE_ACCURACY);
        double result = solver.solve(100, f, 0.0, 3.0);
        Assert.assertEquals(1.5, result, 1e-6);
    }
}