package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrototypeObjectTypeTest {

    private JSTypeRegistry registry;
    private PrototypeObjectType prototypeObjectType;

    @Before
    public void setUp() {
        registry = new JSTypeRegistry(new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, int charno) {}

            @Override
            public void error(String message, String sourceName, int line, int charno) {}
        });
        
        // Creating a concrete instance of PrototypeObjectType using the registry's native object or similar
        ObjectType unknownType = registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE);
        prototypeObjectType = new PrototypeObjectType(registry, "TestPrototype", unknownType);
    }

    @Test
    public void testBasicProperties() {
        assertNotNull(prototypeObjectType);
        assertEquals("TestPrototype", prototypeObjectType.getReferenceName());
        assertFalse(prototypeObjectType.isNativeObjectType());
    }

    @Test
    public void testGetConstructor() {
        // Test constructor relationship
        assertNotNull(prototypeObjectType.getConstructor());
    }

    @Test
    public void testIsObject() {
        assertTrue(prototypeObjectType.isObject());
        assertTrue(prototypeObjectType.isInstanceType());
    }

    @Test
    public void testHasProperty() {
        assertFalse(prototypeObjectType.hasProperty("nonExistentProperty"));
        
        // Define a property and check
        prototypeObjectType.defineDeclaredProperty("testProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        assertTrue(prototypeObjectType.hasProperty("testProp"));
        assertTrue(prototypeObjectType.hasOwnDirectProperty("testProp"));
    }

    @Test
    public void testGetProperties() {
        assertNotNull(prototypeObjectType.getPropertyNames());
        assertTrue(prototypeObjectType.getPropertyNames().isEmpty());

        prototypeObjectType.defineDeclaredProperty("prop1", registry.getNativeType(JSTypeNative.STRING_TYPE), true, null);
        assertEquals(1, prototypeObjectType.getPropertyNames().size());
        assertTrue(prototypeObjectType.getPropertyNames().contains("prop1"));
    }

    @Test
    public void testRemoveProperty() {
        assertFalse(prototypeObjectType.removeProperty("nonExistent"));

        prototypeObjectType.defineDeclaredProperty("propToRemove", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, null);
        assertTrue(prototypeObjectType.hasProperty("propToRemove"));
        
        assertTrue(prototypeObjectType.removeProperty("propToRemove"));
        assertFalse(prototypeObjectType.hasProperty("propToRemove"));
    }

    @Test
    public void testToDebugString() {
        String debugStr = prototypeObjectType.toDebugString();
        assertNotNull(debugStr);
    }

    @Test
    public void testImplicitPrototype() {
        ObjectType implicitProto = prototypeObjectType.getImplicitPrototype();
        // Depending on setup, it could be null or the unknown type passed in constructor
        assertNotNull(implicitProto);
    }

    @Test
    public void testMatchObjectType() {
        assertNotNull(prototypeObjectType.toObjectType());
        assertSame(prototypeObjectType, prototypeObjectType.toObjectType());
    }

    @Test
    public void testGetPropertyType() {
        prototypeObjectType.defineDeclaredProperty("typedProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        JSType propType = prototypeObjectType.getPropertyType("typedProp");
        assertNotNull(propType);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), propType);
    }

    @Test
    public void testGetPropertyNode() {
        prototypeObjectType.defineDeclaredProperty("nodeProp", registry.getNativeType(JSTypeNative.VOID_TYPE), false, null);
        // Checking property node retrieval
        assertNull(prototypeObjectType.getPropertyNode("nodeProp"));
    }

    @Test
    public void testIsStructural() {
        assertFalse(prototypeObjectType.isStructural());
    }
}