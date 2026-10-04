package org.apache.commons.math.optimization.linear;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SimplexSolverTest {

    @Test
    public void testSimplexSolverStandardMaximization() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);

        assertEquals(45.0, solution.getValue(), 1.0e-6);
        assertEquals(1.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(3.0, solution.getPoint()[1], 1.0e-6);
    }

    @Test
    public void testSimplexSolverStandardMinimization() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1, -1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 2));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        assertEquals(-4.0, solution.getValue(), 1.0e-6);
        assertEquals(2.0, solution.getPoint()[0], 1.0e-6);
        assertEquals(2.0, solution.getPoint()[1], 1.0e-6);
    }

    @Test
    public void testSimplexSolverUnbounded() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { -1, 1 }, Relationship.LEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        try {
            solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
            fail("Expected OptimizationException for unbounded problem");
        } catch (OptimizationException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSimplexSolverUnboundedMinimization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1, -1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -1 }, Relationship.LEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        try {
            solver.optimize(f, constraints, GoalType.MINIMIZE, false);
            fail("Expected OptimizationException for unbounded problem");
        } catch (OptimizationException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSimplexSolverNoFeasibleSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 5));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 2));

        SimplexSolver solver = new SimplexSolver();
        try {
            solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
            fail("Expected OptimizationException for no feasible solution");
        } catch (OptimizationException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSimplexSolverMaxIterationsExceeded() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 10));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        solver.setMaxIterations(0);
        try {
            solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
            fail("Expected OptimizationException due to max iterations");
        } catch (OptimizationException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testMath82BugCondition() throws OptimizationException {
        // Specific test case targeting Math-82 where a min ratio tie-breaking 
        // or degenerate pivot selection previously caused infinite loops or incorrect results.
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.75, -20, 0.5, -6 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1/4.0, -8, -1, 9 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1/2.0, -12, -1/2.0, 3 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0 }, Relationship.EQ, 1));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertNotNull(solution);
    }

    @Test
    public void testSimplexSolverWithDifferentRelationships() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 3, 1 }, Relationship.GEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 2));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        assertNotNull(solution);
        assertEquals(4.0, solution.getValue(), 1.0e-6);
    }
}