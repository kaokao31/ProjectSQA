package org.apache.commons.math3.optimization.linear;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SimplexTableauTest {

    @Test
    public void testSimplexTableauConstructionAndBasicMethods() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0, 0.0}, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[]{0.0, 1.0}, Relationship.LEQ, 1.0));

        SimplexTableau tableau = new SimplexTableau(
                f, constraints, GoalType.MAXIMIZE, false, 1e-6
        );

        Assert.assertNotNull(tableau);
        Assert.assertEquals(3, tableau.getWidth());
        Assert.assertEquals(3, tableau.getHeight()); // 2 constraints + objective function
        Assert.assertEquals(1.0, tableau.getEntry(0, 0), 1e-6);
        
        // Test toString
        Assert.assertNotNull(tableau.toString());
    }

    @Test
    public void testArtificialVariablesAndDrop() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0, 2.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        // Equality constraint forces an artificial variable
        constraints.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.EQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(
                f, constraints, GoalType.MINIMIZE, true, 1e-6
        );

        Assert.assertTrue(tableau.getNumArtificialVariables() > 0);
        
        // Test drop phase 1 method indirectly or directly if accessible
        // Let's invoke a getSolution check or similar operations
        PointValuePair solution = tableau.getSolution();
        Assert.assertNotNull(solution);
    }

    @Test
    public void testGetSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{3.0, 2.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{2.0, 1.0}, Relationship.LEQ, 100.0));
        constraints.add(new LinearConstraint(new double[]{1.0, 1.0}, Relationship.LEQ, 80.0));

        SimplexTableau tableau = new SimplexTableau(
                f, constraints, GoalType.MAXIMIZE, false, 1e-6
        );

        // Manually set some basic variables or run iterations if needed, 
        // but here we just check getSolution on initial state.
        PointValuePair solution = tableau.getSolution();
        Assert.assertNotNull(solution);
    }

    @Test
    public void testRestrictedToNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 5.0));

        SimplexTableau tableauNegative = new SimplexTableau(
                f, constraints, GoalType.MAXIMIZE, false, 1e-6
        );
        SimplexTableau tableauNonNegative = new SimplexTableau(
                f, constraints, GoalType.MAXIMIZE, true, 1e-6
        );

        Assert.assertNotNull(tableauNegative);
        Assert.assertNotNull(tableauNonNegative);
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[]{1.0, 1.0}, 0.0);
        List<LinearConstraint> constraints1 = new ArrayList<LinearConstraint>();
        constraints1.add(new LinearConstraint(new double[]{1.0, 0.0}, Relationship.LEQ, 1.0));

        SimplexTableau t1 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, 1e-6);
        SimplexTableau t2 = new SimplexTableau(f1, constraints1, GoalType.MAXIMIZE, false, 1e-6);
        SimplexTableau t3 = new SimplexTableau(f1, constraints1, GoalType.MINIMIZE, false, 1e-6);

        Assert.assertEquals(t1, t2);
        Assert.assertEquals(t1.hashCode(), t2.hashCode());
        Assert.assertNotEquals(t1, t3);
        Assert.assertNotEquals(t1, null);
        Assert.assertNotEquals(t1, "SomeString");
    }

    @Test
    public void testColumnOperations() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{1.0}, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[]{1.0}, Relationship.LEQ, 1.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        // Test basic structural accessors
        Assert.assertTrue(tableau.getNumVariables() >= 1);
        Assert.assertTrue(tableau.getNumSlacks() >= 1);
        
        RealVector column = tableau.getColumn(0);
        Assert.assertNotNull(column);
    }
}