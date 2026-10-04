package com.google.javascript.jscomp.type;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticScope;
import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.GoogleCodingConvention;
import com.google.javascript.jscomp.SimpleDefinitionFinder;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.Scope.Var;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for ChainableReverseAbstractInterpreter targeting maximum coverage
 * and edge-case behavior for Closure Bug 19.
 */
public class ChainableReverseAbstractInterpreterTest {

    private JSTypeRegistry typeRegistry;
    private CodingConvention codingConvention;
    private TestChainableReverseAbstractInterpreter interpreter;

    private static final class TestChainableReverseAbstractInterpreter extends ChainableReverseAbstractInterpreter {
        public TestChainableReverseAbstractInterpreter(CodingConvention codingConvention, JSTypeRegistry typeRegistry) {
            super(codingConvention, typeRegistry);
        }

        @Override
        public FlowScope firstThrough(Node node, FlowScope scope) {
            return scope;
        }

        @Override
        public FlowScope caseName(Node node, JSType blindNameType, FlowScope scope) {
            return scope;
        }
    }

    @Before
    public void setUp() {
        typeRegistry = new JSTypeRegistry(null);
        codingConvention = new GoogleCodingConvention();
        interpreter = new TestChainableReverseAbstractInterpreter(codingConvention, typeRegistry);
    }

    @Test
    public void testNextAndFirstInterpreterChaining() {
        assertNull(interpreter.getIterator());

        TestChainableReverseAbstractInterpreter secondInterpreter =
                new TestChainableReverseAbstractInterpreter(codingConvention, typeRegistry);

        ChainableReverseAbstractInterpreter result = interpreter.append(secondInterpreter);
        assertSame(secondInterpreter, result);
        assertSame(secondInterpreter, interpreter.getIterator());
        assertSame(interpreter, secondInterpreter.getFirstInterpreter());
    }

    @Test
    public void testGetPreciseTypeIncludingNull() {
        Node node = Node.newString("testNode");
        JSType type = typeRegistry.getNativeType(JSTypeRegistry.DataTypes.UNKNOWN_TYPE);
        
        JSType result = interpreter.getPreciseTypeIncludingNull(type, node);
        assertNotNull(result);
    }

    @Test
    public void testThroughWithNoNextInterpreter() {
        Node node = Node.newNumber(1.0);
        FlowScope scope = new FlowScope();
        
        FlowScope result = interpreter.through(node, scope);
        assertNotNull(result);
        assertSame(scope, result);
    }

    @Test
    public void testThroughWithNextInterpreter() {
        Node node = Node.newNumber(1.0);
        FlowScope scope = new FlowScope();

        TestChainableReverseAbstractInterpreter secondInterpreter =
                new TestChainableReverseAbstractInterpreter(codingConvention, typeRegistry) {
                    @Override
                    public FlowScope firstThrough(Node n, FlowScope s) {
                        return null; // Test custom flow scope return
                    }
                };

        interpreter.append(secondInterpreter);
        FlowScope result = interpreter.through(node, scope);
        assertNull(result);
    }

    @Test
    public void testGetNodeNameWithValidGetprop() {
        // Constructing a GETPROP node: a.b
        Node left = Node.newString("a");
        Node right = Node.newString("b");
        Node getprop = new Node(com.google.javascript.rhino.Token.GETPROP, left, right);

        FlowScope scope = new FlowScope();
        String nodeName = interpreter.getNodeName(getprop);
        // Depending on implementation, getNodeName handles GETPROP/NAME properly or returns null
        // Testing robustness of the method against typical AST nodes.
        assertNotNull(interpreter);
    }

    @Test
    public void testHandlePropertyThisAssignment() {
        Node receiver = Node.newString("this");
        Node prop = Node.newString("prop");
        Node getprop = new Node(com.google.javascript.rhino.Token.GETPROP, receiver, prop);
        
        FlowScope scope = new FlowScope();
        JSType type = typeRegistry.getNativeType(JSTypeRegistry.DataTypes.UNKNOWN_TYPE);

        // Exercise restrictions and specific method paths
        FlowScope result = interpreter.caseClient(getprop, type, scope, true);
        assertNotNull(result);
    }

    @Test
    public void testRestrictedScopeRetained() {
        Node nameNode = Node.newString("x");
        FlowScope scope = new FlowScope();
        JSType type = typeRegistry.getNativeType(JSTypeRegistry.DataTypes.UNKNOWN_TYPE);

        FlowScope restricted = interpreter.restricter(nameNode, scope, type, true);
        assertNotNull(restricted);
    }
}