package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;

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
            public void warning(String message, String sourceName, int line, int characterPosition) {}

            @Override
            public void error(String message, String sourceName, int line, int characterPosition) {}
        });
        prototypeObjectType = new PrototypeObjectType(registry, "TestPrototype", null);
    }

    @Test
    public void testBasicProperties() {
        assertNotNull(prototypeObjectType);
        assertEquals("TestPrototype", prototypeObjectType.getReferenceName());
        assertFalse(prototypeObjectType.hasReferenceName());
    }

    @Test
    public void testConstructor() {
        FunctionType owner = registry.createFunctionType(registry.getNativeType(JSTypeNative.NO_TYPE), null, null);
        PrototypeObjectType protoWithCtor = new PrototypeObjectType(registry, "CtorProto", owner);
        assertEquals("CtorProto", protoWithCtor.getReferenceName());
        assertSame(owner, protoWithCtor.getConstructor());
    }

    @Test
    public void testPropertiesMethods() {
        assertFalse(prototypeObjectType.hasOwnDeclaredProperty("nonExistent"));
        
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        prototypeObjectType.defineDeclaredProperty("someProp", stringType, false, null);
        
        assertTrue(prototypeObjectType.hasOwnDeclaredProperty("someProp"));
        assertSame(stringType, prototypeObjectType.getPropertyType("someProp"));
    }

    @Test
    public void testImplementsInterface() {
        assertFalse(prototypeObjectType.isInterface());
        assertFalse(prototypeObjectType.isInstanceType());
    }

    @Test
    public void testToString() {
        String str = prototypeObjectType.toString();
        assertNotNull(str);
    }
}