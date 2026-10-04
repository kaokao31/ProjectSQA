package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class InlineCostEstimatorTest {

    @Test
    public void testGetCost() {
        // Test estimating cost for a simple node tree
        Node root = Node.newNumber(10.0);
        int cost = InlineCostEstimator.getCost(root);
        assertTrue(cost > 0);
    }

    @Test
    public void testGetCostWithThreshold() {
        Node root = Node.newString("test");
        // Test with a threshold lower than actual cost
        int threshold = 1;
        int cost = InlineCostEstimator.getCost(root, threshold);
        assertTrue(cost >= threshold);
    }

    @Test
    public void testGetCostExceedingThreshold() {
        Node root = Node.newString("veryLongIdentifierToExceedThreshold");
        int threshold = 2;
        int cost = InlineCostEstimator.getCost(root, threshold);
        assertTrue(cost >= threshold);
    }

    @Test
    public void testInstantiation() {
        // Exercise the constructor for coverage if any exists (or default constructor)
        InlineCostEstimator estimator = new InlineCostEstimator();
        assertNotNull(estimator);
    }
}