package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;

public class FunctionTypeTest {

    private static class DummyJSTypeRegistry extends JSTypeRegistry {
        public DummyJSTypeRegistry() {
            super(new ErrorReporter() {
                @Override public void warning(String message, String sourceName, int line, int character) {}
                @Override public void error(String message, String sourceName, int line, int character) {}
            });
        }
    }

    @Test
    public void testFunctionTypeConstructionAndBasicProperties() {
        JSTypeRegistry registry = new DummyJSTypeRegistry();
        
        // Test standard constructor or factory methods via registry if available,
        // or directly test methods on FunctionType if we can instantiate or mock/subclass.
        // Since FunctionType is abstract or has specific constructors, let's exercise what's accessible.
        
        // Let's create an instance using standard Rhino compiler patterns if possible.
        // FunctionType(JSTypeRegistry registry, String name, Node sourceNode,
        //              ArrowType type, JSType evaluatedType, boolean isConstructor,
        //              boolean isInterface, JSType prototype)
        
        ArrowType arrowType = new ArrowType(registry, null, null);
        FunctionType fnType = new FunctionType(
                registry,
                "dummyFn",
                null,
                arrowType,
                null,
                false,
                false,
                null
        );

        assertNotNull(fnType);
        assertFalse(fnType.isConstructor());
        assertFalse(fnType.isInterface());
        assertTrue(fnType.isFunctionType());
        assertFalse(fnType.isNominalType());
        
        // Test call/apply or type relation checks
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        assertNotNull(unknownType);
        
        // Test call with various parameters
        fnType.setPrototype(null);
        assertNull(fnType.getPrototype());
        
        JSType instanceType = fnType.getInstanceType();
        assertNotNull(instanceType);
    }

    @Test
    public void testConstructorAndInterfaceFlags() {
        JSTypeRegistry registry = new DummyJSTypeRegistry();
        ArrowType arrowType = new ArrowType(registry, null, null);
        
        FunctionType ctorFn = new FunctionType(
                registry,
                "Ctor",
                null,
                arrowType,
                null,
                true, // isConstructor
                false,
                null
        );
        
        assertTrue(ctorFn.isConstructor());
        assertFalse(ctorFn.isInterface());
        assertNotNull(ctorFn.getInstanceType());

        FunctionType interfaceFn = new FunctionType(
                registry,
                "Interface",
                null,
                arrowType,
                null,
                false,
                true, // isInterface
                null
        );
        
        assertFalse(interfaceFn.isConstructor());
        assertTrue(interfaceFn.isInterface());
    }

    @Test
    public void testGetCallSignaturesAndParameters() {
        JSTypeRegistry registry = new DummyJSTypeRegistry();
        ArrowType arrowType = new ArrowType(registry, null, null);
        
        FunctionType fnType = new FunctionType(
                registry,
                "fn",
                null,
                arrowType,
                null,
                false,
                false,
                null
        );

        assertNotNull(fnType.getCallSignature());
        assertEquals(arrowType, fnType.getCallSignature());
    }
}