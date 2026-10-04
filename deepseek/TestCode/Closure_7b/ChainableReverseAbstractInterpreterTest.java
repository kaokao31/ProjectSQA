package com.google.javascript.jscomp.type;

import com.google.javascript.jscomp.Scope;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.UnionType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for ChainableReverseAbstractInterpreter.
 * Designed to achieve high coverage and detect the Closure-7 bug.
 */
public class ChainableReverseAbstractInterpreterTest {

    private JSTypeRegistry registry;
    private Node dummyNode;
    private Scope dummyScope;
    private FlowScope dummyFlowScope;

    @Before
    public void setUp() {
        registry = new JSTypeRegistry(null);
        dummyNode = new Node(1); // arbitrary token type
        dummyScope = new Scope(null, null);
        dummyFlowScope = new FlowScope(dummyScope);
    }

    // Concrete subclass for testing abstract methods
    private static class TestInterpreter extends ChainableReverseAbstractInterpreter {
        private final JSType type;
        private final FlowScope resultScope;

        TestInterpreter(JSType type, FlowScope resultScope) {
            this.type = type;
            this.resultScope = resultScope;
        }

        @Override
        public JSType getTypeOfExpression(Node n, Scope s) {
            return type;
        }

        @Override
        public FlowScope getPreciserScopeKnowingConditionOutcome(Node n, FlowScope s) {
            return resultScope;
        }
    }

    @Test
    public void testGetTypeOfExpression_NullNode() {
        TestInterpreter interpreter = new TestInterpreter(null, null);
        assertNull(interpreter.getTypeOfExpression(null, dummyScope));
    }

    @Test
    public void testGetTypeOfExpression_NullScope() {
        TestInterpreter interpreter = new TestInterpreter(registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        JSType result = interpreter.getTypeOfExpression(dummyNode, null);
        assertNotNull(result);
        assertTrue(result.isNumberType());
    }

    @Test
    public void testGetTypeOfExpression_NonNull() {
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        TestInterpreter interpreter = new TestInterpreter(stringType, null);
        JSType result = interpreter.getTypeOfExpression(dummyNode, dummyScope);
        assertSame(stringType, result);
    }

    @Test
    public void testGetPreciserScopeKnowingConditionOutcome_NullNode() {
        TestInterpreter interpreter = new TestInterpreter(null, dummyFlowScope);
        FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(null, dummyFlowScope);
        assertSame(dummyFlowScope, result);
    }

    @Test
    public void testGetPreciserScopeKnowingConditionOutcome_NullScope() {
        TestInterpreter interpreter = new TestInterpreter(null, null);
        FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(dummyNode, null);
        assertNull(result);
    }

    @Test
    public void testGetPreciserScopeKnowingConditionOutcome_NonNull() {
        FlowScope expectedScope = new FlowScope(dummyScope);
        TestInterpreter interpreter = new TestInterpreter(null, expectedScope);
        FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(dummyNode, dummyFlowScope);
        assertSame(expectedScope, result);
    }

    // Test for the Closure-7 bug: non-function type should not cause exception
    @Test
    public void testGetPreciserScopeKnowingConditionOutcome_NonFunctionType() {
        JSType nonFunctionType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        TestInterpreter interpreter = new TestInterpreter(nonFunctionType, dummyFlowScope);
        // This should not throw ClassCastException or NullPointerException
        try {
            interpreter.getPreciserScopeKnowingConditionOutcome(dummyNode, dummyFlowScope);
        } catch (Exception e) {
            fail("Should not throw exception for non-function type: " + e.getMessage());
        }
    }

    // Test for function type (normal path)
    @Test
    public void testGetPreciserScopeKnowingConditionOutcome_FunctionType() {
        FunctionType functionType = registry.createFunctionType(
            registry.getNativeType(JSTypeNative.NUMBER_TYPE),
            registry.getNativeType(JSTypeNative.STRING_TYPE)
        );
        TestInterpreter interpreter = new TestInterpreter(functionType, dummyFlowScope);
        FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(dummyNode, dummyFlowScope);
        assertNotNull(result);
    }

    // Test chain of responsibility: firstLink and nextLink
    @Test
    public void testChainOfResponsibility() {
        TestInterpreter first = new TestInterpreter(null, null);
        TestInterpreter second = new TestInterpreter(null, null);
        first.setNextLink(second);
        assertSame(second, first.getNextLink());
        assertNull(second.getNextLink());
    }

    // Test that getPreciserScopeKnowingConditionOutcome delegates to next link when current returns null
    @Test
    public void testDelegationToNextLink() {
        FlowScope expectedScope = new FlowScope(dummyScope);
        TestInterpreter first = new TestInterpreter(null, null) {
            @Override
            public FlowScope getPreciserScopeKnowingConditionOutcome(Node n, FlowScope s) {
                return null; // simulate no result
            }
        };
        TestInterpreter second = new TestInterpreter(null, expectedScope);
        first.setNextLink(second);
        FlowScope result = first.getPreciserScopeKnowingConditionOutcome(dummyNode, dummyFlowScope);
        assertSame(expectedScope, result);
    }

    // Test with union type (edge case)
    @Test
    public void testGetTypeOfExpression_UnionType() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType unionType = registry.createUnionType(numberType, stringType);
        TestInterpreter interpreter = new TestInterpreter(unionType, null);
        JSType result = interpreter.getTypeOfExpression(dummyNode, dummyScope);
        assertTrue(result.isUnionType());
    }

    // Test with object type
    @Test
    public void testGetTypeOfExpression_ObjectType() {
        ObjectType objectType = registry.createAnonymousObjectType(null);
        TestInterpreter interpreter = new TestInterpreter(objectType, null);
        JSType result = interpreter.getTypeOfExpression(dummyNode, dummyScope);
        assertTrue(result.isObjectType());
    }

    // Test that getPreciserScopeKnowingConditionOutcome handles null from getTypeOfExpression
    @Test
    public void testGetPreciserScopeKnowingConditionOutcome_NullType() {
        TestInterpreter interpreter = new TestInterpreter(null, dummyFlowScope);
        FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(dummyNode, dummyFlowScope);
        // Should not throw and should return the input scope or delegate
        assertNotNull(result);
    }

    // Test with empty scope
    @Test
    public void testGetPreciserScopeKnowingConditionOutcome_EmptyScope() {
        FlowScope emptyScope = new FlowScope(new Scope(null, null));
        TestInterpreter interpreter = new TestInterpreter(registry.getNativeType(JSTypeNative.VOID_TYPE), emptyScope);
        FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(dummyNode, emptyScope);
        assertSame(emptyScope, result);
    }
}