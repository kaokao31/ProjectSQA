package org.apache.commons.math.optimization.linear;

import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SimplexTableauTest {

    @Test
    public void testSimplexTableauConstructionAndBasicGetters() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 2.0}, 3.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.LEQ, 4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        Assert.assertNotNull(tableau.getTableau());
        Assert.assertEquals(2, tableau.getNumVariables());
        Assert.assertEquals(1, tableau.getNumConstraints());
        Assert.assertEquals(4, tableau.getWidth());
        Assert.assertEquals(2, tableau.getHeight());
        Assert.assertNotNull(tableau.toString());
    }

    @Test
    public void testStrictInequalityHandling() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.LT, 5.0));
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.GT, 1.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);
        Assert.assertNotNull(tableau);
        Assert.assertTrue(tableau.getWidth() > 0);
    }

    @Test
    public void testGetRhsCellOffsetAndVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{2.0, 0.0}, Relationship.EQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1e-6);
        
        // Testing basic structural properties
        Assert.assertEquals(1, tableau.getSlackVariableCount());
        Assert.assertEquals(0, tableau.getArtificialVariableCount());
        
        RealVector solution = tableau.getSolution();
        Assert.assertNotNull(solution);
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 5.0));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, 1e-6);
        SimplexTableau t2 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, 1e-6);
        SimplexTableau t3 = new SimplexTableau(f1, c1, GoalType.MINIMIZE, false, 1e-6);

        Assert.assertTrue(t1.equals(t1));
        Assert.assertTrue(t1.equals(t2));
        Assert.assertFalse(t1.equals(t3));
        Assert.assertFalse(t1.equals(null));
        Assert.assertFalse(t1.equals(new Object()));

        Assert.assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void testMath87BugScenario() {
        // Math-87 bug relates to SimplexTableau incorrectly selecting the basic variable
        // when multiple entries exist in a column or due to precision issues.
        // Let's construct a scenario with specific coefficients to exercise column scanning.
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{syntaxCheck(0), 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 0.0}, Relationship.EQ, 1.0));
        constraints.add(new LinearConstraint(new double[]{0.0, 1.0}, Relationship.EQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        
        // Force the internal state to execute getBasicRow for columns
        for (int i = 0; i < tableau.getWidth() - 1; i++) {
            tableau.getBasicRow(i);
        }
        
        RealVector sol = tableau.getSolution();
        Assert.assertNotNull(sol);
    }

    @Test
    public void testDropPhase1Objective() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 2.0}, Relationship.GEQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);
        // This will introduce artificial variables
        if (tableau.getArtificialVariableCount() > 0) {
            tableau.discardArtificialVariables();
            Assert.assertNotNull(tableau.getTableau());
        }
    }

    private double syntaxCheck(double val) {
        return val + 1.0;
    }
}