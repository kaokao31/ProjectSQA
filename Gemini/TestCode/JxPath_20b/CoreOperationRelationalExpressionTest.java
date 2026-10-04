package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionTest {

    // Concrete implementation of CoreOperationRelationalExpression for testing purposes
    private static class ConcreteRelationalExpression extends CoreOperationRelationalExpression {
        public ConcreteRelationalExpression(Expression arg1, Expression arg2) {
            super(arg1, arg2);
        }

        @Override
        protected boolean evaluateCompare(int compare) {
            return compare < 0;
        }
    }

    private static class DummyExpression extends Expression {
        private final Object value;

        public DummyExpression(Object value) {
            this.value = value;
        }

        @Override
        public Object compute(EvalContext context) {
            return value;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return value;
        }

        @Override
        public boolean computeContextDependent() {
            return false;
        }
    }

    @Test
    public void testBothNodesAreNodePointers() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));

        // Create two node pointers with comparable values
        NodePointer ptr1 = NodePointer.newNodePointer(null, "b", null);
        NodePointer ptr2 = NodePointer.newNodePointer(null, "a", null);

        DummyExpression expr1 = new DummyExpression(ptr1);
        DummyExpression expr2 = new DummyExpression(ptr2);

        ConcreteRelationalExpression op = new ConcreteRelationalExpression(expr1, expr2);
        // "b" < "a" is false
        assertFalse(op.computeValue(rootContext));
    }

    @Test
    public void testOneNodePointerLeft() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));

        NodePointer ptr1 = NodePointer.newNodePointer(null, "a", null);
        DummyExpression expr1 = new DummyExpression(ptr1);
        DummyExpression expr2 = new DummyExpression("b");

        ConcreteRelationalExpression op = new ConcreteRelationalExpression(expr1, expr2);
        // "a" < "b" is true
        assertTrue(op.computeValue(rootContext));
    }

    @Test
    public void testOneNodePointerRight() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));

        DummyExpression expr1 = new DummyExpression("b");
        NodePointer ptr2 = NodePointer.newNodePointer(null, "a", null);
        DummyExpression expr2 = new DummyExpression(ptr2);

        ConcreteRelationalExpression op = new ConcreteRelationalExpression(expr1, expr2);
        // "b" < "a" is false
        assertFalse(op.computeValue(rootContext));
    }

    @Test
    public void testIteratorOnLeft() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));

        // EvalContext acting as an iterator yielding a value
        EvalContext evalContext = new InitialContext(rootContext);
        DummyExpression expr1 = new DummyExpression(evalContext);
        DummyExpression expr2 = new DummyExpression("a");

        ConcreteRelationalExpression op = new ConcreteRelationalExpression(expr1, expr2);
        // InitialContext contains root, compute evaluates to root pointer or similar.
        // Just checking it executes without exception and returns boolean.
        assertNotNull(op.computeValue(rootContext));
    }

    @Test
    public void testIteratorOnRight() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));

        EvalContext evalContext = new InitialContext(rootContext);
        DummyExpression expr1 = new DummyExpression("a");
        DummyExpression expr2 = new DummyExpression(evalContext);

        ConcreteRelationalExpression op = new ConcreteRelationalExpression(expr1, expr2);
        assertNotNull(op.computeValue(rootContext));
    }

    @Test
    public void testFindValueIteratorsBoth() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));

        EvalContext evalContext1 = new InitialContext(rootContext);
        EvalContext evalContext2 = new InitialContext(rootContext);

        DummyExpression expr1 = new DummyExpression(evalContext1);
        DummyExpression expr2 = new DummyExpression(evalContext2);

        ConcreteRelationalExpression op = new ConcreteRelationalExpression(expr1, expr2);
        assertNotNull(op.computeValue(rootContext));
    }

    @Test
    public void testPrimitiveComparisons() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));

        ConcreteRelationalExpression op = new ConcreteRelationalExpression(
                new DummyExpression(1),
                new DummyExpression(2)
        );
        assertTrue(op.computeValue(rootContext));

        ConcreteRelationalExpression op2 = new ConcreteRelationalExpression(
                new DummyExpression(2),
                new DummyExpression(1)
        );
        assertFalse(op2.computeValue(rootContext));
    }

    @Test
    public void testContainsCollectionOrNodeSet() {
        JXPathContext context = JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(context, NodePointer.newNodePointer(null, new Object(), null));

        // Test with a collection/array on one side
        Object[] array = new Object[] { "a", "c" };
        ConcreteRelationalExpression op = new ConcreteRelationalExpression(
                new DummyExpression(array),
                new DummyExpression("b")
        );
        // Should find "a" in array, compare "a" < "b" -> true
        assertTrue(op.computeValue(rootContext));
    }
}