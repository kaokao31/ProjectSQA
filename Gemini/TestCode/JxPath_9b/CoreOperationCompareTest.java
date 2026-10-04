package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Test;

public class CoreOperationCompareTest {

    @Test
    public void testEqualComparison() {
        Constant c1 = new Constant("test");
        Constant c2 = new Constant("test");
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);

        EvalContext context = new InitialContext(new SelfContext(null, null));
        Object result = op.computeValue(context);
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testNotEqualComparison() {
        Constant c1 = new Constant("test1");
        Constant c2 = new Constant("test2");
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);

        EvalContext context = new InitialContext(new SelfContext(null, null));
        Object result = op.computeValue(context);
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testLessThanComparison() {
        Constant c1 = new Constant(new Double(1.0));
        Constant c2 = new Constant(new Double(2.0));
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);

        EvalContext context = new InitialContext(new SelfContext(null, null));
        Object result = op.computeValue(context);
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testLessThanOrEqualComparison() {
        Constant c1 = new Constant(new Double(2.0));
        Constant c2 = new Constant(new Double(2.0));
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);

        EvalContext context = new InitialContext(new SelfContext(null, null));
        Object result = op.computeValue(context);
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testGreaterThanComparison() {
        Constant c1 = new Constant(new Double(3.0));
        Constant c2 = new Constant(new Double(2.0));
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(c1, c2);

        EvalContext context = new InitialContext(new SelfContext(null, null));
        Object result = op.computeValue(context);
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testGreaterThanOrEqualComparison() {
        Constant c1 = new Constant(new Double(2.0));
        Constant c2 = new Constant(new Double(2.0));
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(c1, c2);

        EvalContext context = new InitialContext(new SelfContext(null, null));
        Object result = op.computeValue(context);
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testGetSymbol() {
        Constant c1 = new Constant(1);
        Constant c2 = new Constant(2);

        assertEquals("=", new CoreOperationEqual(c1, c2).getSymbol());
        assertEquals("!=", new CoreOperationNotEqual(c1, c2).getSymbol());
        assertEquals("<", new CoreOperationLessThan(c1, c2).getSymbol());
        assertEquals("<=", new CoreOperationLessThanOrEqual(c1, c2).getSymbol());
        assertEquals(">", new CoreOperationGreaterThan(c1, c2).getSymbol());
        assertEquals(">=", new CoreOperationGreaterThanOrEqual(c1, c2).getSymbol());
    }

    @Test
    public void testIsSymmetric() {
        Constant c1 = new Constant(1);
        Constant c2 = new Constant(2);

        CoreOperationEqual eq = new CoreOperationEqual(c1, c2);
        CoreOperationNotEqual neq = new CoreOperationNotEqual(c1, c2);
        CoreOperationLessThan lt = new CoreOperationLessThan(c1, c2);

        assertEquals(true, eq.isSymmetric());
        assertEquals(true, neq.isSymmetric());
        assertEquals(false, lt.isSymmetric());
    }

    @Test
    public void testGetPrecedence() {
        Constant c1 = new Constant(1);
        Constant c2 = new Constant(2);
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);
        
        // Comparing precedence values against expected expression constants
        assertEquals(op.getPrecedence(), new CoreOperationNotEqual(c1, c2).getPrecedence());
    }
}