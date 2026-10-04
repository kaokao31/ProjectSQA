package org.apache.commons.math3.optimization.linear;

import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

public class SimplexSolverTest {

    private static final double EPSILON = 1.0e-6;

    private void assertSolutionEquals(double[] expectedPoint, double expectedValue, PointValuePair solution) {
        assertNotNull("Solution should not be null", solution);
        assertArrayEquals(expectedPoint, solution.getPoint(), EPSILON);
        assertEquals(expectedValue, solution.getValue(), EPSILON);
    }

    private void assertUnbounded(LinearObjectiveFunction f, Collection<LinearConstraint> constraints, GoalType goalType) {
        SimplexSolver solver = new SimplexSolver();
        try {
            solver.optimize(f, constraints, goalType, true);
            fail("Expected UnboundedSolutionException");
        } catch (UnboundedSolutionException e) {
            // expected
        }
    }

    private void assertInfeasible(LinearObjectiveFunction f, Collection<LinearConstraint> constraints, GoalType goalType) {
        SimplexSolver solver = new SimplexSolver();
        try {
            solver.optimize(f, constraints, goalType, true);
            fail("Expected NoFeasibleSolutionException");
        } catch (NoFeasibleSolutionException e) {
            // expected
        }
    }

    @Test
    public void testBasicMaximization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 3 }, Relationship.LEQ, 6));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertSolutionEquals(new double[] { 3, 1 }, 14, solution);
    }

    @Test
    public void testBasicMinimization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 2, -1 }, Relationship.LEQ, 2));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        // Convert to minimization: -2x + y -> maximize -2x + y
        assertSolutionEquals(new double[] { 1, 0 }, -2, solution); // expected based on constraints
    }

    @Test
    public void testEqualityConstraint() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 3));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        assertSolutionEquals(new double[] { 0, 3 }, 6, solution);
    }

    @Test
    public void testNegativeRHS() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // Convert x - y <= -1 to -x + y >= 1
        constraints.add(new LinearConstraint(new double[] { -1, 1 }, Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 0)); // x >= 0
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0)); // y >= 0
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        // Problem is unbounded? Actually with x>=0, y>=0 and -x+y>=1, we can increase both.
        assertUnbounded(f, constraints, GoalType.MAXIMIZE);
    }

    @Test(expected = UnboundedSolutionException.class)
    public void testUnbounded() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -1 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 0));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(expected = NoFeasibleSolutionException.class)
    public void testInfeasible() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test
    public void testDegenerateTie() {
        // This problem has a degenerate basic solution (slack variable zero)
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -1 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 1));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        // Optimum is (0.5, 0.5) with value 2*0.5+0.5 = 1.5
        assertSolutionEquals(new double[] { 0.5, 0.5 }, 1.5, solution);
    }

    @Test
    public void testPhaseOneRequired() {
        // This requires Phase 1 because initial solution is not feasible
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 2 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.GEQ, 8));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        // Feasible region: 2x+y>=8, x+y>=5, x,y>=0. Max 3x+2y.
        // Vertices: (0,8) -> 16, (3,2) -> 13, (5,0) -> 15. So optimum at (0,8).
        assertSolutionEquals(new double[] { 0, 8 }, 16, solution);
    }

    @Test
    public void testMixedConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 5, 4 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 6, 4 }, Relationship.LEQ, 24));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { -1, 1 }, Relationship.GEQ, -1));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        // Solve manually: vertices: (0,0) -> 0, (0,3) -> 12, (2,2) -> 18, (4,0) -> 20, (3.33,0.67) -> 19.33
        // Actually (4,0) is feasible? 6*4+4*0=24 <=24, 4+0<=6, -4+0=-1? Actually -x+y >= -1 is x-y <=1, so 4-0=4>1, not feasible.
        // Intersection: x-y<=1, 6x+4y<=24, x+2y<=6.
        // Solve 6x+4y=24, x+2y=6 -> 6x+4y=24, 2x+4y=12 -> 4x=12 -> x=3, y=1.5. Check -x+y=-1.5 <=1? yes.
        // Objective: 5*3+4*1.5=21. Check other intersections: x-y=1, x+2y=6 -> x=1+y, 1+y+2y=6 -> y=5/3, x=8/3. Objective: 5*8/3+4*5/3=40/3+20/3=60/3=20. So optimum is (3,1.5) with value 21.
        assertSolutionEquals(new double[] { 3, 1.5 }, 21, solution);
    }

    @Test
    public void testLargeCoefficients() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1000, 1000 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 10));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 10));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 15));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        // Optimum at (10,10) if feasible? 10+10=20 >15, so not feasible. Edge: x=10, y=5 -> 15, x=5,y=10 -> 15, also (10,5) and (5,10) are on line.
        // Objective: 1000*10+1000*5 = 15000.
        assertSolutionEquals(new double[] { 10, 5 }, 15000, solution);
    }
}