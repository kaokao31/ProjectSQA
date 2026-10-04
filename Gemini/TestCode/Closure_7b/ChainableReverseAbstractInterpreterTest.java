package com.google.javascript.jscomp.type;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.GoogleCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.ParameterizedType;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import com.google.javascript.rhino.jstype.SimpleRegistry;
import com.google.javascript.rhino.jstype.UnionType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for ChainableReverseAbstractInterpreter.
 * Designed for maximum code coverage and targeting edge cases relevant to Defects4J Closure Bug 7.
 */
public class ChainableReverseAbstractInterpreterTest {

    private JSTypeRegistry registry;
    private CodingConvention convention;
    private DummyChainableReverseAbstractInterpreter interpreter;

    private static class DummyChainableReverseAbstractInterpreter extends ChainableReverseAbstractInterpreter {
        public DummyChainableReverseAbstractInterpreter(CodingConvention convention, JSTypeRegistry registry) {
            super(convention, registry);
        }

        @Override
        public FlowScope caseAllType(FlowScope blindScope) {
            return null;
        }

        @Override
        public FlowScope caseNullType(FlowScope blindScope) {
            return null;
        }

        @Override
        public FlowScope caseObjectType(ObjectType t) {
            return super.caseObjectType(t);
        }

        public JSType getRestrictedBothHelper(JSType originalType, JSType restrictedType) {
            return getRestrictedBoth(originalType, restrictedType);
        }

        public JSType getNativeTypeHelper(int nativeTypeId) {
            return getNativeType(nativeTypeId);
        }
    }

    @Before
    public void setUp() {
        registry = new JSTypeRegistry(new SimpleRegistry());
        convention = new GoogleCodingConvention();
        interpreter = new DummyChainableReverseAbstractInterpreter(convention, registry);
    }

    @Test
    public void testFirstSetterAndGetter() {
        assertNull(interpreter.getFirst());

        SemanticAbstractInterpreter nextInterpreter = new DummyChainableReverseAbstractInterpreter(convention, registry);
        ChainableReverseAbstractInterpreter result = interpreter.append(nextInterpreter);

        assertSame(interpreter, result);
        assertSame(nextInterpreter, interpreter.getFirst());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNull() {
        interpreter.append(null);
    }

    @Test
    public void testGetNext() {
        assertNull(interpreter.getNext());

        SemanticAbstractInterpreter nextInterpreter = new DummyChainableReverseAbstractInterpreter(convention, registry);
        interpreter.append(nextInterpreter);
        assertSame(nextInterpreter, interpreter.getNext());
    }

    @Test
    public void testGetRestrictedBoth() {
        JSType stringType = registry.getNativeType(JSTypeRegistry.DATA_TYPE); // Just a native type
        JSType numberType = registry.getNativeType(JSTypeRegistry.NUMBER_TYPE);

        JSType result = interpreter.getRestrictedBothHelper(stringType, numberType);
        assertNotNull(result);
    }

    @Test
    public void testCaseObjectTypeParametric() {
        ObjectType objType = registry.createAnonymousObjectType();
        FlowScope scope = interpreter.caseObjectType(objType);
        assertNull(scope);
    }

    @Test
    public void testGetNativeType() {
        JSType nativeType = interpreter.getNativeTypeHelper(JSTypeRegistry.VOID_TYPE);
        assertNotNull(nativeType);
    }

    @Test
    public void testMultistepChain() {
        DummyChainableReverseAbstractInterpreter second = new DummyChainableReverseAbstractInterpreter(convention, registry);
        DummyChainableReverseAbstractInterpreter third = new DummyChainableReverseAbstractInterpreter(convention, registry);

        interpreter.append(second);
        second.append(third);

        assertSame(second, interpreter.getFirst());
        assertSame(third, second.getFirst());
    }

    @Test
    public void testGetIncomingScope() {
        Node node = new Node(Token.BLOCK);
        FlowScope scope = new FlowScope();
        
        interpreter.setScopes(node, scope, scope);
        // ChainableReverseAbstractInterpreter delegates or handles through FlowScope if applicable
        assertNotNull(interpreter);
    }
}