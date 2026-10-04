package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JSTypeRegistryTest {

    private ErrorReporter errorReporter;
    private JSTypeRegistry registry;

    @Before
    public void setUp() {
        errorReporter = new SimpleErrorReporter();
        registry = new JSTypeRegistry(errorReporter);
    }

    @Test
    public void testForwardDeclaredType() {
        // Test forward declare mechanics
        registry.forwardDeclareType("MyCustomType");
        JSType type = registry.getType("MyCustomType");
        assertNotNull("Forward declared type should be retrievable", type);
        assertTrue("Forward declared type should be an instance of PrototypeObjectType or similar proxy",
                type instanceof PrototypeObjectType || type.isUnknownType());
    }

    @Test
    public void testRegisterNativeTypesAndGetters() {
        // Verify native types are properly initialized via constructor
        assertNotNull(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
        assertNotNull(registry.getNativeType(JSTypeNative.VOID_TYPE));
        assertNotNull(registry.getNativeType(JSTypeNative.ALL_TYPE));
        assertNotNull(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        assertNotNull(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        assertNotNull(registry.getNativeType(JSTypeNative.STRING_TYPE));
        assertNotNull(registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    }

    @Test
    public void testCreateRecordType() {
        Map<String, JSType> properties = new HashMap<String, JSType>();
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        properties.put("prop1", numberType);

        JSType recordType = registry.createRecordType(properties);
        assertNotNull(recordType);
        assertTrue(recordType.isRecordType());
    }

    @Test
    public void testCreateFunctionType() {
        JSType returnType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        ArrowType arrowType = registry.createArrowType(null, null);
        FunctionType fnType = registry.createFunctionType(returnType, arrowType);
        
        assertNotNull(fnType);
        assertEquals(returnType, fnType.getReturnType());
    }

    @Test
    public void testCreateParameterizedType() {
        // Test array/template types if applicable
        ObjectType arrayType = (ObjectType) registry.getNativeType(JSTypeNative.ARRAY_TYPE);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        
        if (arrayType != null) {
            JSType paramType = registry.createParameterizedType(arrayType, numberType);
            assertNotNull(paramType);
        }
    }

    @Test
    public void testClearTypes() {
        registry.forwardDeclareType("TempType");
        assertNotNull(registry.getType("TempType"));
        
        registry.clearTypes();
        // Depending on implementation, clear might reset user types or native types
        // Let's verify standard behavior safely without violating encapsulation.
        JSType nativeType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertNotNull("Native types should generally persist or be re-established after clear", nativeType);
    }

    @Test
    public void testResolveTypesOnTheFly() {
        registry.setResolveMode(JSTypeRegistry.ResolveMode.LAZY);
        assertEquals(JSTypeRegistry.ResolveMode.LAZY, registry.getResolveMode());
        
        registry.setResolveMode(JSTypeRegistry.ResolveMode.IMMEDIATE);
        assertEquals(JSTypeRegistry.ResolveMode.IMMEDIATE, registry.getResolveMode());
    }

    @Test
    public void testGetLastGenerationId() {
        int id = registry.getLastGenerationId();
        assertTrue("Generation ID should be non-negative", id >= 0);
    }
}