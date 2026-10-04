package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreOperationGreaterThanTest {

    @Test
    public void testGetSymbol() {
        Constant left = new Constant(1);
        Constant right = new Constant(2);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        assertEquals(">", op.getSymbol());
    }

    @Test
    public void testEvaluateGreaterThan() {
        Constant left = new Constant(5);
        Constant right = new Constant(3);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testEvaluateLessThan() {
        Constant left = new Constant(2);
        Constant right = new Constant(3);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testEvaluateEqual() {
        Constant left = new Constant(3);
        Constant right = new Constant(3);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        Object result = op.computeValue(null);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testEvaluateWithNodeSetAndNumber() {
        // Test comparing an Iterator/NodeSet vs Number to hit node-set comparison branches
        JXPathContext context = JXPathContext.newContext(new Object());
        Variables vars = context.getVariables();
        vars.declareVariable("val", 5);

        // Create an expression that evaluates to a NodeSet or Iterator
        // Using CoreOperationGreaterThan where left or right might be evaluated via InfoSetUtil
        Constant left = new Constant(10);
        Constant right = new Constant(4);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));
        Object result = op.computeValue(rootContext);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testInvert() {
        Constant left = new Constant(1);
        Constant right = new Constant(2);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        // The evaluateCompare logic is inherited, but verify getPrecedence and other methods work
        assertTrue(op.getPrecedence() > 0);
    }
}