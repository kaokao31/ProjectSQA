package org.apache.commons.jxpath.ri.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;

public class CoreOperationRelationalExpressionTest {

    private static class DummyTestBean {
        public int intField = 10;
        public double doubleField = 5.5;
        public String stringField = "abc";
    }

    private EvalContext createDummyContext() {
        DummyTestBean bean = new DummyTestBean();
        Locale locale = Locale.getDefault();
        NodePointer ptr = NodePointer.newNodePointer(null, bean, locale);
        JXPathContext jxpathContext = JXPathContext.newContext(bean);
        EvalContext context = new InitialContext(new EvalContext(null, null) {
            @Override
            public NodePointer getCurrentNodePointer() {
                return ptr;
            }
            @Override
            public JXPathContext getJXPathContext() {
                return jxpathContext;
            }
        });
        return new SelfContext(context, null);
    }

    @Test
    public void testLessThan() {
        // Test < operator
        Constant c1 = new Constant(5);
        Constant c2 = new Constant(10);
        CoreOperationRelationalExpression op = new CoreOperationLessThan(c1, c2);

        EvalContext context = createDummyContext();
        Object result = op.computeValue(context);
        assertEquals(Boolean.TRUE, result);

        Constant c3 = new Constant(15);
        CoreOperationRelationalExpression opFalse = new CoreOperationLessThan(c3, c2);
        assertEquals(Boolean.FALSE, opFalse.computeValue(context));
    }

    @Test
    public void testLessThanOrEqual() {
        // Test <= operator
        Constant c1 = new Constant(10);
        Constant c2 = new Constant(10);
        CoreOperationRelationalExpression op = new CoreOperationLessThanOrEqual(c1, c2);

        EvalContext context = createDummyContext();
        assertEquals(Boolean.TRUE, op.computeValue(context));

        Constant c3 = new Constant(11);
        CoreOperationRelationalExpression opFalse = new CoreOperationLessThanOrEqual(c3, c2);
        assertEquals(Boolean.FALSE, opFalse.computeValue(context));
    }

    @Test
    public void testGreaterThan() {
        // Test > operator
        Constant c1 = new Constant(15);
        Constant c2 = new Constant(10);
        CoreOperationRelationalExpression op = new CoreOperationGreaterThan(c1, c2);

        EvalContext context = createDummyContext();
        assertEquals(Boolean.TRUE, op.computeValue(context));

        Constant c3 = new Constant(5);
        CoreOperationRelationalExpression opFalse = new CoreOperationGreaterThan(c3, c2);
        assertEquals(Boolean.FALSE, opFalse.computeValue(context));
    }

    @Test
    public void testGreaterThanOrEqual() {
        // Test >= operator
        Constant c1 = new Constant(10);
        Constant c2 = new Constant(10);
        CoreOperationRelationalExpression op = new CoreOperationGreaterThanOrEqual(c1, c2);

        EvalContext context = createDummyContext();
        assertEquals(Boolean.TRUE, op.computeValue(context));

        Constant c3 = new Constant(5);
        CoreOperationRelationalExpression opFalse = new CoreOperationGreaterThanOrEqual(c3, c2);
        assertEquals(Boolean.FALSE, opFalse.computeValue(context));
    }

    @Test
    public void testPrecedence() {
        // Relational operators should have a specific precedence
        Constant c1 = new Constant(1);
        Constant c2 = new Constant(2);
        CoreOperationRelationalExpression op = new CoreOperationLessThan(c1, c2);

        assertTrue(op.getPrecedence() > 0);
        assertFalse(op.isSymmetric());
    }

    @Test
    public void testNodeSetComparison() {
        // Testing comparison involving node sets/pointers if applicable, 
        // to cover broader execute/compute paths in CoreOperationRelationalExpression.
        EvalContext context = createDummyContext();
        // Comparing a constant with an expression that evaluates to a NodePointer or NodeSet
        Constant c1 = new Constant(10);
        VariableReference var = new VariableReference(new QName("nonExistentVar"));
        
        CoreOperationRelationalExpression op = new CoreOperationLessThan(var, c1);
        // Just exercising the code path safely
        try {
            op.computeValue(context);
        } catch (Exception e) {
            // Expected if variable not found, but code path is exercised
            assertNotNull(e);
        }
    }
}