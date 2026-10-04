package org.apache.commons.math3.optimization.linear;

import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.RealPointValuePair;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class SimplexSolverTest {

    @Test
    public void testSimplexSolverStandardMaximization() {
        // Maximize 3x + 5y subject to:
        // x <= 4
        // 2y <= 12
        // 3x + 2y <= 18
        // x, y >= 0
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 18));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);

        Assert.assertEquals(2.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(6.0, solution.getPoint()[1], 1e-6);
        Assert.assertEquals(36.0, solution.getValue(), 1e-6);
    }

    @Test
    public void testSimplexSolverStandardMinimization() {
        // Minimize -3x - 5y subject to same constraints
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -3, -5 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 18));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        Assert.assertEquals(2.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(6.0, solution.getPoint()[1], 1e-6);
        Assert.assertEquals(-36.0, solution.getValue(), 1e-6);
    }

    @Test
    public void testSimplexSolverWithRestrictedNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertTrue(solution.getPoint()[0] >= 0.0);
        Assert.assertTrue(solution.getPoint()[1] >= 0.0);
    }

    @Test
    public void testSimplexSolverDegeneracyHandling() {
        // Test case specifically targeting potential cycling or tie-breaking in Math-28
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 10, 5, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 1 }, Relationship.LEQ, 10));
        constraints.add(new LinearConstraint(new double[] { 2, 0.5, 0 }, Relationship.LEQ, 8));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 2 }, Relationship.LEQ, 12));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);

        Assert.assertNotNull(solution);
        Assert.assertTrue(solution.getValue() >= 0.0);
    }

    @Test(expected = TooManyIterationsException.class)
    public void testMaxIterationsExceeded() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexSolver solver = new SimplexSolver(1, 1e-6);
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

    @Test
    public void testMath28SpecificDegenerateCase() {
        // Constructing a scenario with multiple ratios leading to degenerate pivots
        // This targets the dropPhase1Objective / tableau row selection logic often buggy in Math 28
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 2.0, 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 1.0 }, Relationship.LEQ, 3.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        
        Assert.assertNotNull(solution);
        Assert.assertTrue(solution.getValue() >= 0.0);
    }
}