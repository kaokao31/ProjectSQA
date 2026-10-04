package org.apache.commons.math.optimization.linear;

import org.junit.Test;
import org.junit.Assert;
import java.util.ArrayList;
import java.util.Collection;

public class SimplexSolverTest {

    private static final double EPSILON = 1e-6;

    @Test
    public void testBasicFeasibleSolution() {
        // maximize x + y subject to x + y <= 2, x >= 0, y >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(2.0, solution.getValue(), EPSILON);
        Assert.assertArrayEquals(new double[] {2, 0}, solution.getPoint(), EPSILON);
    }

    @Test
    public void testUnboundedSolution() {
        // maximize x + y subject to x >= 0, y >= 0 (no upper bound)
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        try {
            solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
            Assert.fail("Expected UnboundedSolutionException");
        } catch (UnboundedSolutionException e) {
            // expected
        }
    }

    @Test
    public void testInfeasibleSolution() {
        // maximize x subject to x <= -1, x >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1}, Relationship.LEQ, -1));
        constraints.add(new LinearConstraint(new double[] {1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        try {
            solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
            Assert.fail("Expected NoFeasibleSolutionException");
        } catch (NoFeasibleSolutionException e) {
            // expected
        }
    }

    @Test
    public void testEqualityConstraint() {
        // maximize x + y subject to x + y = 1, x >= 0, y >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(1.0, solution.getValue(), EPSILON);
        Assert.assertArrayEquals(new double[] {1, 0}, solution.getPoint(), EPSILON);
    }

    @Test
    public void testMinimization() {
        // minimize x + y subject to x + y >= 2, x >= 0, y >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.GEQ, 2));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(2.0, solution.getValue(), EPSILON);
        Assert.assertArrayEquals(new double[] {2, 0}, solution.getPoint(), EPSILON);
    }

    @Test
    public void testDegenerateProblem() {
        // This is a known degenerate case that triggered bug Math-82
        // maximize 0.5*x + 0.5*y subject to x + y <= 2, x <= 1, y <= 1, x >= 0, y >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {0.5, 0.5}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(1.0, solution.getValue(), EPSILON);
        // The optimal solution is (1,1) but due to degeneracy, the solver might return (1,0) or (0,1)
        // The value should be 1.0 regardless
        double x = solution.getPoint()[0];
        double y = solution.getPoint()[1];
        Assert.assertTrue(x >= 0 && x <= 1 + EPSILON);
        Assert.assertTrue(y >= 0 && y <= 1 + EPSILON);
        Assert.assertEquals(1.0, x + y, EPSILON);
    }

    @Test
    public void testZeroObjectiveCoefficients() {
        // maximize 0*x + 0*y subject to x + y <= 1, x >= 0, y >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {0, 0}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(0.0, solution.getValue(), EPSILON);
        // Any feasible point is optimal
        double x = solution.getPoint()[0];
        double y = solution.getPoint()[1];
        Assert.assertTrue(x >= 0 && y >= 0 && x + y <= 1 + EPSILON);
    }

    @Test
    public void testNegativeObjectiveCoefficients() {
        // maximize -x - y subject to x + y <= 2, x >= 0, y >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {-1, -1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(0.0, solution.getValue(), EPSILON);
        Assert.assertArrayEquals(new double[] {0, 0}, solution.getPoint(), EPSILON);
    }

    @Test
    public void testLargeCoefficients() {
        // maximize 1000*x + 1000*y subject to x + y <= 1, x >= 0, y >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1000, 1000}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(1000.0, solution.getValue(), EPSILON);
        Assert.assertArrayEquals(new double[] {1, 0}, solution.getPoint(), EPSILON);
    }

    @Test
    public void testMultipleOptimalSolutions() {
        // maximize x + y subject to x + y <= 1, x >= 0, y >= 0
        // All points on the line x+y=1 are optimal
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(1.0, solution.getValue(), EPSILON);
        double x = solution.getPoint()[0];
        double y = solution.getPoint()[1];
        Assert.assertTrue(x >= 0 && y >= 0);
        Assert.assertEquals(1.0, x + y, EPSILON);
    }

    @Test
    public void testSingleVariable() {
        // maximize x subject to x <= 5, x >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1}, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] {1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(5.0, solution.getValue(), EPSILON);
        Assert.assertArrayEquals(new double[] {5}, solution.getPoint(), EPSILON);
    }

    @Test
    public void testWithEpsilon() {
        // Use a custom epsilon to test sensitivity
        SimplexSolver solver = new SimplexSolver(1e-10);
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.GEQ, 0));

        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(2.0, solution.getValue(), 1e-9);
    }

    @Test(expected = NoFeasibleSolutionException.class)
    public void testInfeasibleWithEquality() {
        // maximize x subject to x = 1, x = 2 (contradictory)
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1}, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] {1}, Relationship.EQ, 2));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(expected = UnboundedSolutionException.class)
    public void testUnboundedWithNegativeCoefficient() {
        // maximize -x subject to x >= 0 (unbounded in negative direction? Actually maximize -x is bounded above by 0)
        // To get unbounded, we need a variable that can increase without bound while improving objective
        // maximize x subject to x >= 0 (no upper bound)
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[] {1}, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }
}