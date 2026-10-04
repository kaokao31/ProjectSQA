package org.apache.commons.math.optimization.linear;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.PointValuePair;
import org.junit.Test;

public class SimplexTableauTest {

    private SimplexTableau createBasicTableau() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1.0, 0.0}, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 1.0}, Relationship.LEQ, 1.0));
        return new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-10);
    }

    @Test
    public void testConstructorAndGetters() {
        SimplexTableau tableau = createBasicTableau();

        assertEquals(5, tableau.getWidth());
        assertEquals(3, tableau.getHeight());
        assertEquals(4, tableau.getNumVariables());
        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(2, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(2, tableau.getSlackVariableOffset());
        assertEquals(4, tableau.getArtificialVariableOffset());
        assertEquals(4, tableau.getRhsOffset());
        assertEquals(2, tableau.getOriginalNumDecisionVariables());
    }

    @Test
    public void testInitialTableauEntries() {
        SimplexTableau tableau = createBasicTableau();

        assertEquals(1.0, tableau.getEntry(1, 0), 0.0);
        assertEquals(0.0, tableau.getEntry(1, 1), 0.0);
        assertEquals(1.0, tableau.getEntry(1, 2), 0.0);
        assertEquals(0.0, tableau.getEntry(1, 3), 0.0);
        assertEquals(1.0, tableau.getEntry(1, 4), 0.0);

        assertEquals(0.0, tableau.getEntry(2, 0), 0.0);
        assertEquals(1.0, tableau.getEntry(2, 1), 0.0);
        assertEquals(0.0, tableau.getEntry(2, 2), 0.0);
        assertEquals(1.0, tableau.getEntry(2, 3), 0.0);
        assertEquals(1.0, tableau.getEntry(2, 4), 0.0);
    }

    @Test
    public void testGetBasicRow() {
        SimplexTableau tableau = createBasicTableau();

        assertEquals(1, tableau.getBasicRow(2));
        assertEquals(2, tableau.getBasicRow(3));
        assertEquals(-1, tableau.getBasicRow(0));
        assertEquals(-1, tableau.getBasicRow(1));
    }

    @Test
    public void testGetBasicRowIgnoresTinyNonZeroEntries() {
        SimplexTableau tableau = createBasicTableau();
        int col = tableau.getSlackVariableOffset();
        int expectedRow = 1;

        assertEquals(expectedRow, tableau.getBasicRow(col));

        tableau.setEntry(0, col, 1e-12);
        tableau.setEntry(2, col, 1e-13);

        assertEquals(expectedRow, tableau.getBasicRow(col));
    }

    @Test
    public void testGetBasicRowForSolutionIgnoresTinyNonZeroEntries() {
        SimplexTableau tableau = createBasicTableau();
        int col = tableau.getSlackVariableOffset();

        tableau.setEntry(0, col, 1e-12);
        tableau.setEntry(2, col, 1e-13);

        assertEquals(1, tableau.getBasicRowForSolution(col));
    }

    @Test
    public void testRowOperations() {
        SimplexTableau tableau = createBasicTableau();

        tableau.multiplyRow(1, 2.0);
        assertEquals(2.0, tableau.getEntry(1, 0), 0.0);
        assertEquals(2.0, tableau.getEntry(1, 2), 0.0);
        assertEquals(2.0, tableau.getEntry(1, 4), 0.0);

        tableau.divideRow(1, 2.0);
        assertEquals(1.0, tableau.getEntry(1, 0), 0.0);

        tableau.subtractRow(1, 2);
        assertEquals(1.0, tableau.getEntry(1, 0), 0.0);
        assertEquals(-1.0, tableau.getEntry(1, 1), 0.0);
        assertEquals(1.0, tableau.getEntry(1, 2), 0.0);
        assertEquals(-1.0, tableau.getEntry(1, 3), 0.0);
        assertEquals(0.0, tableau.getEntry(1, 4), 0.0);
    }

    @Test
    public void testPivot() {
        SimplexTableau tableau = createBasicTableau();

        tableau.pivot(0, 1);

        assertEquals(0.0, tableau.getEntry(0, 0), 1e-12);
        assertEquals(1.0, tableau.getEntry(1, 0), 1e-12);
        assertEquals(0.0, tableau.getEntry(2, 0), 1e-12);
    }

    @Test
    public void testGetSolutionInitial() {
        SimplexTableau tableau = createBasicTableau();
        PointValuePair solution = tableau.getSolution();

        assertNotNull(solution);
        assertEquals(0.0, solution.getValue(), 1e-10);
        assertArrayEquals(new double[] {0.0, 0.0}, solution.getPoint(), 1e-10);
    }

    @Test
    public void testNormalizeNegativeRHS() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0}, 0);
        LinearConstraint constraint = new LinearConstraint(new double[] {1.0}, Relationship.GEQ, -5.0);

        SimplexTableau tableau =
            new SimplexTableau(f, Collections.singletonList(constraint), GoalType.MINIMIZE, true, 1e-10);

        assertEquals(-1.0, tableau.getEntry(1, 0), 1e-12);
        assertEquals(1.0, tableau.getEntry(1, 1), 1e-12);
        assertEquals(5.0, tableau.getEntry(1, tableau.getRhsOffset()), 1e-12);
    }

    @Test
    public void testUnrestrictedVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0}, 0);
        LinearConstraint constraint = new LinearConstraint(new double[] {1.0}, Relationship.LEQ, 1.0);

        SimplexTableau tableau =
            new SimplexTableau(f, Collections.singletonList(constraint), GoalType.MINIMIZE, false, 1e-10);

        assertEquals(2, tableau.getNumDecisionVariables());
        assertEquals(1, tableau.getOriginalNumDecisionVariables());
        assertEquals(2, tableau.getSlackVariableOffset());
        assertEquals(3, tableau.getArtificialVariableOffset());
    }

    @Test
    public void testSolveSimpleLP() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {2.0, 3.0}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1.0, 2.0}, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] {2.0, 1.0}, Relationship.LEQ, 5.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        assertNotNull(solution);
        assertEquals(7.0, solution.getValue(), 1e-6);
        assertArrayEquals(new double[] {2.0, 1.0}, solution.getPoint(), 1e-6);
    }

    @Test
    public void testSolveWithNegativeRHSAndUnrestrictedVariables() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1.0}, Relationship.GEQ, -5.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        assertNotNull(solution);
        assertEquals(-5.0, solution.getValue(), 1e-6);
        assertArrayEquals(new double[] {-5.0}, solution.getPoint(), 1e-6);
    }

    @Test
    public void testSolveWithEqualityConstraint() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0}, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1.0, 0.0}, Relationship.EQ, 1.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 1.0}, Relationship.EQ, 2.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        assertNotNull(solution);
        assertEquals(3.0, solution.getValue(), 1e-6);
        assertArrayEquals(new double[] {1.0, 2.0}, solution.getPoint(), 1e-6);
    }
}