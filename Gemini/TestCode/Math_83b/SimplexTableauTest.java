package org.apache.commons.math.optimization.linear;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class SimplexTableauTest {

    @Test
    public void testTableauConstructionAndBasicGetters() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 2.0}, 3.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.LEQ, 4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        assertEquals(1, tableau.getNumConstraints());
        assertEquals(2, tableau.getNumVariables());
        assertEquals(3.0, tableau.getRHSEntry(tableau.getHeight() - 1), 1e-6);
        assertNotNull(tableau.getData());
        assertNotNull(tableau.getSymbolicMatrix());
        assertEquals(4, tableau.getWidth());
        assertEquals(3, tableau.getHeight());
    }

    @Test
    public void testTableauWithArtificialVariablesAndGeqConstraint() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, -1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // GEQ constraint requires surplus and artificial variables
        constraints.add(new LinearConstraint(new double[]{2.0, 1.0}, Relationship.GEQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6);

        assertTrue(tableau.getNumArtificialVariables() > 0);
        assertEquals(3.0, tableau.getRHSEntry(tableau.getHeight() - 1), 1e-6);
    }

    @Test
    public void testTableauWithEqConstraint() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // EQ constraint requires an artificial variable
        constraints.add(new LinearConstraint(new double[]{1.0, 2.0}, Relationship.EQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);

        assertTrue(tableau.getNumArtificialVariables() > 0);
        assertEquals(5.0, tableau.getRHSEntry(tableau.getHeight() - 1), 1e-6);
    }

    @Test
    public void testDropPhase1Objective() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 2.0}, Relationship.EQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);

        int originalWidth = tableau.getWidth();
        int originalHeight = tableau.getHeight();

        tableau.dropPhase1Objective();

        assertTrue(tableau.getWidth() < originalWidth);
        assertTrue(tableau.getHeight() < originalHeight);
    }

    @Test
    public void testGetRowIndexAndBasicMethods() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 2.0));
        constraints.add(new LinearConstraint(new double[]{2.0}, Relationship.LEQ, 4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        assertEquals(-1, tableau.getRowIndex(0.0));
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[]{1.0, 2.0}, 3.0);
        List<LinearConstraint> constraints1 = new ArrayList<LinearConstraint>();
        constraints1.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.LEQ, 4.0));

        SimplexTableau tableau1 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, 1e-6);
        SimplexTableau tableau2 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, 1e-6);

        assertEquals(tableau1, tableau1);
        assertEquals(tableau1, tableau2);
        assertEquals(tableau1.hashCode(), tableau2.hashCode());

        assertFalse(tableau1.equals(null));
        assertFalse(tableau1.equals("Some String"));

        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[]{2.0, 2.0}, 3.0);
        SimplexTableau tableau3 = new SimplexTableau(f2, constraints1, GoalType.MAXIMIZE, false, 1e-6);
        assertFalse(tableau1.equals(tableau3));
    }

    @Test
    public void testGetSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 0.0}, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[]{0.0, 1.0}, Relationship.LEQ, 1.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        RealPointValuePair solution = tableau.getSolution();

        assertNotNull(solution);
    }

    @Test
    public void testNegativeRHS() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // Negative RHS tests optimization branch for negative constants
        constraints.add(new LinearConstraint(new double[]{-1.0, -1.0}, Relationship.LEQ, -2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        assertNotNull(tableau);
    }
}