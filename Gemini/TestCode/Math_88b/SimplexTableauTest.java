package org.apache.commons.math.optimization.linear;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimplexTableauTest {

    @Test
    public void testSimplexTableauBasicConstruction() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 2.0}, 3.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.LEQ, 4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        Assert.assertNotNull(tableau);
        Assert.assertEquals(2, tableau.getNumVariables());
        Assert.assertEquals(1, tableau.getNumConstraints());
        Assert.assertEquals(5, tableau.getWidth());
        Assert.assertEquals(3, tableau.getHeight());
    }

    @Test
    public void testSimplexTableauWithArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // GEQ constraint introduces a surplus and an artificial variable
        constraints.add(new LinearConstraint(new double[]{1.0, 2.0}, Relationship.GEQ, 5.0));
        // EQ constraint introduces an artificial variable
        constraints.add(new LinearConstraint(new double[]{2.0, 1.0}, Relationship.EQ, 4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6);

        Assert.assertNotNull(tableau);
        // Should contain slack/surplus/artificial variables
        Assert.assertTrue(tableau.getWidth() > 0);
        Assert.assertTrue(tableau.getHeight() > 0);
    }

    @Test
    public void testGetRowConstraint() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        // Test restricted/unrestricted and row accessors if any
        Assert.assertNotNull(tableau.gettableau());
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[]{1.0, 2.0}, 3.0);
        Collection<LinearConstraint> constraints1 = new ArrayList<LinearConstraint>();
        constraints1.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.LEQ, 4.0));

        SimplexTableau tableau1 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, 1e-6);
        SimplexTableau tableau2 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, 1e-6);

        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[]{2.0, 1.0}, 3.0);
        SimplexTableau tableau3 = new SimplexTableau(f2, constraints1, GoalType.MAXIMIZE, false, 1e-6);

        Assert.assertTrue(tableau1.equals(tableau1));
        Assert.assertTrue(tableau1.equals(tableau2));
        Assert.assertFalse(tableau1.equals(null));
        Assert.assertFalse(tableau1.equals(new Object()));
        Assert.assertFalse(tableau1.equals(tableau3));

        Assert.assertEquals(tableau1.hashCode(), tableau2.hashCode());
    }

    @Test
    public void testGetSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 0.0}, Relationship.LEQ, 2.0));
        constraints.add(new LinearConstraint(new double[]{0.0, 1.0}, Relationship.LEQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        
        RealPointValuePair solution = tableau.getSolution();
        Assert.assertNotNull(solution);
    }

    @Test
    public void testNegativeConstantsInConstraints() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // Negative RHS value to test normalization logic
        constraints.add(new LinearConstraint(new double[]{1.0, -2.0}, Relationship.LEQ, -3.0));
        constraints.add(new LinearConstraint(new double[]{-1.0, 1.0}, Relationship.GEQ, -2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        Assert.assertNotNull(tableau);
    }

    @Test
    public void testColumnAndRowCounts() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 2.0, 3.0}, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 1.0, 1.0}, Relationship.LEQ, 10.0));
        constraints.add(new LinearConstraint(new double[]{2.0, 1.0, -1.0}, Relationship.EQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6);
        
        Assert.assertEquals(3, tableau.getNumVariables());
        Assert.assertEquals(2, tableau.getNumConstraints());
        Assert.assertTrue(tableau.getWidth() > 0);
        Assert.assertTrue(tableau.getHeight() > 0);
    }
}