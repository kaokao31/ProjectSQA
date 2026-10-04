package org.apache.commons.math.optimization.linear;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SimplexTableauTest {

    private static final double EPSILON = 1.0e-6;

    @Test
    public void testInitialTableauWithOnlyLeqConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        assertEquals(2, tableau.getNumVariables());
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(2, tableau.getHeight());
        assertEquals(4, tableau.getWidth());

        // Objective row (maximization -> negative coefficients)
        assertEquals(-1.0, tableau.getEntry(0, 0), EPSILON);
        assertEquals(-1.0, tableau.getEntry(0, 1), EPSILON);
        assertEquals(0.0, tableau.getEntry(0, 2), EPSILON);
        assertEquals(0.0, tableau.getEntry(0, 3), EPSILON);

        // Constraint row
        assertEquals(1.0, tableau.getEntry(1, 0), EPSILON);
        assertEquals(1.0, tableau.getEntry(1, 1), EPSILON);
        assertEquals(1.0, tableau.getEntry(1, 2), EPSILON);
        assertEquals(6.0, tableau.getEntry(1, 3), EPSILON);

        // Slack variable is basic in row 1
        assertNotNull(tableau.getBasicRow(2));
        assertEquals(1, tableau.getBasicRow(2).intValue());
        assertEquals(2, tableau.getBasicVariable(1));

        // Decision variables are not basic initially
        assertNull(tableau.getBasicRow(0));
        assertNull(tableau.getBasicRow(1));
    }

    @Test
    public void testEqualityConstraintAddsArtificialVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {2, 1}, Relationship.EQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        assertEquals(0, tableau.getNumSlackVariables());
        assertEquals(1, tableau.getNumArtificialVariables());
        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(3, tableau.getHeight());
        assertEquals(4, tableau.getWidth());

        // Phase 1 objective row: artificial variable coefficient is 1
        assertEquals(0.0, tableau.getEntry(0, 0), EPSILON);
        assertEquals(0.0, tableau.getEntry(0, 1), EPSILON);
        assertEquals(1.0, tableau.getEntry(0, 2), EPSILON);
        assertEquals(0.0, tableau.getEntry(0, 3), EPSILON);

        // Original objective row (maximization)
        assertEquals(-1.0, tableau.getEntry(1, 0), EPSILON);
        assertEquals(-1.0, tableau.getEntry(1, 1), EPSILON);
        assertEquals(0.0, tableau.getEntry(1, 2), EPSILON);
        assertEquals(0.0, tableau.getEntry(1, 3), EPSILON);

        // Constraint row
        assertEquals(2.0, tableau.getEntry(2, 0), EPSILON);
        assertEquals(1.0, tableau.getEntry(2, 1), EPSILON);
        assertEquals(1.0, tableau.getEntry(2, 2), EPSILON);
        assertEquals(4.0, tableau.getEntry(2, 3), EPSILON);

        // Artificial variable is basic in the constraint row
        assertNotNull(tableau.getBasicRow(2));
        assertEquals(2, tableau.getBasicRow(2).intValue());
        assertEquals(2, tableau.getBasicVariable(2));
    }

    @Test
    public void testNegativeRHSConstraintIsNormalized() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {-1, -1}, Relationship.LEQ, -5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        // After normalization: x1 + x2 >= 5 -> slack (surplus) and artificial variable
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(1, tableau.getNumArtificialVariables());
        assertEquals(2, tableau.getNumObjectiveFunctions());
        assertEquals(3, tableau.getHeight());
        assertEquals(5, tableau.getWidth());

        // Constraint row: [1, 1, -1, 1, 5] (slack coefficient -1 for >=)
        assertEquals(1.0, tableau.getEntry(2, 0), EPSILON);
        assertEquals(1.0, tableau.getEntry(2, 1), EPSILON);
        assertEquals(-1.0, tableau.getEntry(2, 2), EPSILON);
        assertEquals(1.0, tableau.getEntry(2, 3), EPSILON);
        assertEquals(5.0, tableau.getEntry(2, 4), EPSILON);

        // Artificial variable is basic in the constraint row
        assertNotNull(tableau.getBasicRow(3));
        assertEquals(2, tableau.getBasicRow(3).intValue());
    }

    @Test
    public void testPivotOperation() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        // Pivot on column 0 (x1) and row 1 (slack row)
        tableau.pivot(0, 1);

        // x1 becomes basic in row 1
        assertEquals(0, tableau.getBasicVariable(1));
        assertNotNull(tableau.getBasicRow(0));
        assertEquals(1, tableau.getBasicRow(0).intValue());

        // Updated objective row
        assertEquals(0.0, tableau.getEntry(0, 0), EPSILON);
        assertEquals(0.0, tableau.getEntry(0, 1), EPSILON);
        assertEquals(1.0, tableau.getEntry(0, 2), EPSILON);
        assertEquals(6.0, tableau.getEntry(0, 3), EPSILON);

        // Updated constraint row (pivot row remains unchanged)
        assertEquals(1.0, tableau.getEntry(1, 0), EPSILON);
        assertEquals(1.0, tableau.getEntry(1, 1), EPSILON);
        assertEquals(1.0, tableau.getEntry(1, 2), EPSILON);
        assertEquals(6.0, tableau.getEntry(1, 3), EPSILON);

        // Slack column is no longer basic
        assertNull(tableau.getBasicRow(2));
    }

    @Test
    public void testDropPhase1ObjectiveRemovesArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {2, 1}, Relationship.EQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
        tableau.dropPhase1Objective();

        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(2, tableau.getHeight());
        assertEquals(3, tableau.getWidth()); // 2 variables + RHS
    }

    @Test
    public void testUnrestrictedVariablesCreatePair() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1}, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, EPSILON);

        assertEquals(2, tableau.getNumVariables()); // x+ and x-
        assertEquals(1, tableau.getNumSlackVariables());
        assertEquals(0, tableau.getNumArtificialVariables());
        assertEquals(1, tableau.getNumObjectiveFunctions());
        assertEquals(2, tableau.getHeight());
        assertEquals(4, tableau.getWidth());
    }

    @Test
    public void testGetRhs() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        assertEquals(0.0, tableau.getRhs(0), EPSILON);
        assertEquals(6.0, tableau.getRhs(1), EPSILON);
    }

    @Test
    public void testGetOffsets() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 6));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);

        assertEquals(2, tableau.getSlackVariableOffset());
        assertEquals(3, tableau.getArtificialVariableOffset());
        assertEquals(tableau.getWidth() - 1, tableau.getRhsOffset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncompatibleConstraintThrows() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1, 1}, Relationship.LEQ, 6));

        new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullConstraintsThrows() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1}, 0);

        new SimplexTableau(f, null, GoalType.MAXIMIZE, true, EPSILON);
    }
}