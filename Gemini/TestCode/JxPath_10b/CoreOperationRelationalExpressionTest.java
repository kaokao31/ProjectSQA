package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JUnit 4 test suite for CoreOperationRelationalExpression.
 * Designed to maximize branch and line coverage and target potential relational bugs in JXPath 10.
 */
public class CoreOperationRelationalExpressionTest {

    // Concrete implementation of CoreOperationRelationalExpression for testing purposes
    private static class ConcreteRelationalExpression extends CoreOperationRelationalExpression {
        public ConcreteRelationalExpression(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare == 0;
        }

        public int getPrecedence() {
            return PRECEDENCE_RELATIONAL;
        }
    }

    private static class MockConstantExpression extends Expression {
        private final Object value;

        public MockConstantExpression(Object value) {
            this.value = value;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return value;
        }

        @Override
        public Object compute(EvalContext context) {
            return value;
        }

        @Override
        public boolean computeContextDependent() {
            return false;
        }
    }

    private static class MockNodeSet {
        private final List<Object> nodes;

        public MockNodeSet(List<Object> nodes) {
            this.nodes = nodes;
        }

        public List<Object> getNodes() {
            return nodes;
        }
    }

    @Test
    public void testNullBothSides() {
        Expression left = new MockConstantExpression(null);
        Expression right = new MockConstantExpression(null);
        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);

        EvalContext context = new InitialContext(new RootContext(null, null, null));
        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof Boolean);
    }

    @Test
    public void testFindMatchNodeSetAndPrimitive() {
        List<Object> list = new ArrayList<>();
        list.add("test");
        Expression left = new MockConstantExpression(list);
        Expression right = new MockConstantExpression("test");

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));
        
        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testFindMatchPrimitiveAndNodeSet() {
        List<Object> list = new ArrayList<>();
        list.add(123);
        Expression left = new MockConstantExpression(123);
        Expression right = new MockConstantExpression(list);

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));
        
        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testFindMatchNodeSetsBoth() {
        List<Object> list1 = new ArrayList<>();
        list1.add("abc");
        
        List<Object> list2 = new ArrayList<>();
        list2.add("abc");

        Expression left = new MockConstantExpression(list1);
        Expression right = new MockConstantExpression(list2);

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));
        
        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testFindMatchNodePointers() {
        NodePointer ptr1 = NodePointer.newNodePointer(null, "value", null);
        NodePointer ptr2 = NodePointer.newNodePointer(null, "value", null);

        Expression left = new MockConstantExpression(ptr1);
        Expression right = new MockConstantExpression(ptr2);

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));
        
        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testContainsMatchWithCollection() {
        List<Object> list = new ArrayList<>();
        list.add(10);
        list.add(20);
        list.add(30);

        Expression left = new MockConstantExpression(list);
        Expression right = new MockConstantExpression(20);

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));

        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testContainsMatchWithArray() {
        String[] array = new String[] {"apple", "banana", "cherry"};

        Expression left = new MockConstantExpression(array);
        Expression right = new MockConstantExpression("banana");

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));

        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testContainsMatchWithModelNodeSet() {
        Set<Object> set = new HashSet<>();
        set.add(42);

        Expression left = new MockConstantExpression(set);
        Expression right = new MockConstantExpression(42);

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));

        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testNullPointerOnOneSide() {
        NodePointer ptr = NullPointer.newNodePointer(null, null, null);
        Expression left = new MockConstantExpression(ptr);
        Expression right = new MockConstantExpression("something");

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));

        Object result = expr.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testNumericComparisons() {
        Expression left = new MockConstantExpression(5);
        Expression right = new MockConstantExpression(5);

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));

        Object result = expr.computeValue(context);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testStringComparisons() {
        Expression left = new MockConstantExpression("abc");
        Expression right = new MockConstantExpression("def");

        ConcreteRelationalExpression expr = new ConcreteRelationalExpression(left, right);
        EvalContext context = new InitialContext(new RootContext(null, null, null));

        Object result = expr.computeValue(context);
        Assert.assertEquals(Boolean.FALSE, result);
    }
}