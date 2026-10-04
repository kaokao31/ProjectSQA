package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.TemplateType;
import com.google.javascript.rhino.SimpleErrorReporter;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class PrototypeObjectTypeTest {

    private JSTypeRegistry registry;
    private SimpleErrorReporter errorReporter;

    @Before
    public void setUp() {
        errorReporter = new SimpleErrorReporter();
        registry = new JSTypeRegistry(errorReporter);
    }

    @Test
    public void testMinimizePropertiesBasic() {
        PrototypeObjectType proto = registry.createPrototypeObjectType(
            "TestProto", null, null, false
        );
        
        // Add some properties
        proto.defineDeclaredProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        proto.defineDeclaredProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

        assertNotNull(proto.getOwnPropertyNames());
        assertEquals(2, proto.getOwnPropertyNames().size());

        // Test minimizeProperties or similar cleanup methods if applicable
        proto.minimizeProperties();
    }

    @Test
    public void testImplicitPrototypeAndOwner() {
        ObjectType implicitProto = registry.createNativeObjectType(JSTypeNative.OBJECT_TYPE);
        PrototypeObjectType proto = new PrototypeObjectType(
            registry, "MyProto", implicitProto, false
        );

        assertSame(implicitProto, proto.getImplicitPrototype());
        assertFalse(proto.isNativeObjectType());
        assertFalse(proto.hasCachedValues());
    }

    @Test
    public void testPropertyInferenceAndCollisions() {
        PrototypeObjectType proto = registry.createPrototypeObjectType(
            "ProtoWithCollisions", null, null, false
        );

        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

        proto.defineDeclaredProperty("prop", numType, false, null);
        
        // Define or check implicit props
        assertTrue(proto.hasProperty("prop"));
        assertFalse(proto.hasProperty("nonexistent"));
        
        assertEquals(numType, proto.getPropertyType("prop"));
    }

    @Test
    public void testTemplateTypes() {
        PrototypeObjectType proto = registry.createPrototypeObjectType(
            "TemplateProto", null, null, false
        );

        TemplateType template = new TemplateType(registry, "T");
        Map<TemplateType, JSType> subMap = new HashMap<>();
        subMap.put(template, registry.getNativeType(JSTypeNative.NUMBER_TYPE));

        JSType instantiated = proto.instantiateTypeWithContext(template, subMap);
        assertNotNull(instantiated);
    }

    @Test
    public void testOwnerFunction() {
        FunctionType owner = registry.createFunctionType(
            registry.getNativeType(JSTypeNative.NO_TYPE),
            null,
            null
        );
        
        PrototypeObjectType proto = new PrototypeObjectType(
            registry, "OwnerProto", null, owner, false
        );

        assertSame(owner, proto.getOwnerFunction());
    }

    @Test
    public void testRemoveProperty() {
        PrototypeObjectType proto = registry.createPrototypeObjectType(
            "RemovableProto", null, null, false
        );

        proto.defineDeclaredProperty("tempProp", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, null);
        assertTrue(proto.hasOwnDirectProperty("tempProp"));

        boolean removed = proto.removeProperty("tempProp");
        assertTrue(removed);
        assertFalse(proto.hasOwnDirectProperty("tempProp"));
        
        // Removing non-existent property
        assertFalse(proto.removeProperty("nonexistent"));
    }

    @Test
    public void testResolve() {
        PrototypeObjectType proto = registry.createPrototypeObjectType(
            "ResolveProto", null, null, false
        );
        
        // Trigger resolve
        proto.resolve(errorReporter);
        assertNotNull(proto.getImplicitPrototype());
    }

    @Test
    public void testToDebugString() {
        PrototypeObjectType proto = registry.createPrototypeObjectType(
            "DebugProto", null, null, false
        );
        
        String debugStr = proto.toString();
        assertNotNull(debugStr);
    }

    @Test
    public void testSetOwnerFunction() {
        PrototypeObjectType proto = registry.createPrototypeObjectType(
            "SetOwnerProto", null, null, false
        );
        
        FunctionType owner = registry.createFunctionType(
            registry.getNativeType(JSTypeNative.NO_TYPE),
            null,
            null
        );
        
        proto.setOwnerFunction(owner);
        assertSame(owner, proto.getOwnerFunction());
    }
}