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
    public void testSimplexTableauConstructorAndBasicGetters() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 2.0}, 3.0);
        List<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        assertEquals(1, tableau.getNumConstraints());
        assertEquals(2, tableau.getNumVariables());
        assertEquals(3.0, tableau.getConstantTerm(), 1e-6);
        assertNotNull(tableau.getTableau());
        assertNotNull(tableau.toString());
    }

    @Test
    public void testTableauWithArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, -1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<>();
        // GEQ constraint introduces a surplus variable and an artificial variable
        // EQ constraint introduces an artificial variable
        constraints.add(new LinearConstraint(new double[]{1.0, 2.0}, Relationship.GEQ, 4.0));
        constraints.add(new LinearConstraint(new double[]{2.0, 1.0}, Relationship.EQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6);

        assertTrue(tableau.getNumArtificialVariables() > 0);
        assertFalse(tableau.getBasicVariables().isEmpty());
    }

    @Test
    public void testStrictInequalityHandling() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<>();
        // Using restrictedToNonNegative = false to trigger different variable mappings
        constraints.add(new LinearConstraint(new double[]{1.0, -1.0}, Relationship.LEQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        assertNotNull(tableau);
    }

    @Test
    public void testDropPhase1Objective() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.EQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);
        int originalWidth = tableau.getWidth();
        int originalHeight = tableau.getHeight();

        tableau.dropPhase1Objective();

        assertEquals(originalHeight - 1, tableau.getHeight());
        assertTrue(tableau.getWidth() < originalWidth);
    }

    @Test
    public void testGetEntryAndSetEntry() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        tableau.setEntry(0, 0, 99.0);
        assertEquals(99.0, tableau.getEntry(0, 0), 1e-6);
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        List<LinearConstraint> constraints1 = new ArrayList<>();
        constraints1.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 5.0));

        SimplexTableau tableau1 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, 1e-6);
        SimplexTableau tableau2 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, 1e-6);

        assertEquals(tableau1, tableau1);
        assertFalse(tableau1.equals(null));
        assertFalse(tableau1.equals(new Object()));
        assertEquals(tableau1, tableau2);
        assertEquals(tableau1.hashCode(), tableau2.hashCode());
    }

    @Test
    public void testGetSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[]{1.0, 0.0}, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[]{0.0, 1.0}, Relationship.LEQ, 1.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        RealPointValuePair solution = tableau.getSolution();
        assertNotNull(solution);
    }

    @Test
    public void testNegativeConstantsInConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<>();
        // Negative RHS should be normalized properly
        constraints.add(new LinearConstraint(new double[]{-1.0, -1.0}, Relationship.LEQ, -5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        assertNotNull(tableau);
    }
}