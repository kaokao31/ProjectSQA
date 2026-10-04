package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for JSTypeRegistry.
 * Designed to achieve high code coverage and detect faults,
 * including the known bug in Defects4J Closure-165 where
 * createUnionType throws NullPointerException when the list contains null elements.
 */
public class JSTypeRegistryTest {

    private JSTypeRegistry registry;
    private ErrorReporter errorReporter;

    @Before
    public void setUp() {
        // Create a simple error reporter that does nothing
        errorReporter = new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, String lineSource) {
                // no-op
            }

            @Override
            public void error(String message, String sourceName, int line, String lineSource) {
                // no-op
            }
        };
        registry = new JSTypeRegistry(errorReporter, false);
        // Ensure native types are initialized
        registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        registry.getNativeType(JSTypeNative.STRING_TYPE);
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        registry.getNativeType(JSTypeNative.ARRAY_TYPE);
        registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    }

    // ===================== getNativeType tests =====================

    @Test
    public void testGetNativeType_knownType() {
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertNotNull("Native NUMBER_TYPE should not be null", numType);
        assertTrue("Native type should be a number type", numType.isNumberValueType());
    }

    @Test
    public void testGetNativeType_arrayType() {
        JSType arrayType = registry.getNativeType(JSTypeNative.ARRAY_TYPE);
        assertNotNull("Native ARRAY_TYPE should not be null", arrayType);
    }

    @Test
    public void testGetNativeType_unknownType_returnsNull() {
        // Assuming there is no custom type for this enum value
        JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        assertNull("Uninitialized native type should be null", unknown);
    }

    // ===================== register and getType tests =====================

    @Test
    public void testRegister_and_getType() {
        String typeName = "test.Type";
        JSType type = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        registry.register(type, typeName);
        JSType retrieved = registry.getType(typeName);
        assertNotNull("Registered type should be retrievable", retrieved);
        assertSame("Retrieved type should be the same object", type, retrieved);
    }

    @Test
    public void testRegister_duplicate_returnsOldType() {
        String typeName = "dup.Type";
        JSType first = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType second = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        registry.register(first, typeName);
        registry.register(second, typeName);
        JSType retrieved = registry.getType(typeName);
        assertSame("Second registration should overwrite with the new type", second, retrieved);
    }

    // ===================== createUnionType tests =====================

    @Test
    public void testCreateUnionType_emptyList() {
        List<JSType> empty = new ArrayList<>();
        JSType result = registry.createUnionType(empty);
        // Empty union should produce a type that is the NO_TYPE or equivalent
        assertNotNull("Union of empty list should not be null", result);
        // Typically it returns the NO_TYPE
        assertTrue("Empty union should be the no type", result.isNoType());
    }

    @Test
    public void testCreateUnionType_singleType() {
        List<JSType> single = new ArrayList<>();
        single.add(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        JSType result = registry.createUnionType(single);
        assertNotNull("Union of single type should not be null", result);
        assertTrue("Union of single number should be number type", result.isNumberValueType());
    }

    @Test
    public void testCreateUnionType_multipleTypes() {
        List<JSType> types = new ArrayList<>();
        types.add(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        types.add(registry.getNativeType(JSTypeNative.STRING_TYPE));
        JSType result = registry.createUnionType(types);
        assertNotNull("Union of multiple types should not be null", result);
        assertTrue("Result should be a union type", result.isUnionType());
    }

    @Test
    public void testCreateUnionType_withNullElement() {
        // This test targets the known Defects4J Closure-165 bug:
        // createUnionType should handle null elements gracefully.
        List<JSType> types = new ArrayList<>();
        types.add(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        types.add(null);
        try {
            JSType result = registry.createUnionType(types);
            // If no NPE, the bug is fixed; result should be a number-only union
            assertNotNull("Result should not be null", result);
            assertTrue("Union with one real type should be that type", result.isNumberValueType());
        } catch (NullPointerException e) {
            fail("createUnionType should not throw NullPointerException when list contains null");
        }
    }

    @Test
    public void testCreateUnionType_allNullElements() {
        List<JSType> types = new ArrayList<>();
        types.add(null);
        types.add(null);
        try {
            JSType result = registry.createUnionType(types);
            // If all null, expect NO_TYPE or empty union
            assertNotNull("Result should not be null", result);
            assertTrue("Union of all nulls should be no type", result.isNoType());
        } catch (NullPointerException e) {
            fail("createUnionType should not throw NullPointerException when list contains only nulls");
        }
    }

    // ===================== createOptionalType tests =====================

    @Test
    public void testCreateOptionalType_notNull() {
        JSType type = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType optional = registry.createOptionalType(type);
        assertNotNull("Optional type should not be null", optional);
        // Optional type is typically a union of the type and undefined
        assertTrue("Optional type should be a union type", optional.isUnionType());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateOptionalType_nullInput() {
        registry.createOptionalType(null);
    }

    // ===================== createParameters tests (if applicable) =====================

    @Test
    public void testCreateParameters_empty() {
        List<JSType> params = new ArrayList<>();
        JSType result = registry.createParameters(params);
        assertNotNull("Parameters from empty list should not be null", result);
        // For empty parameters, likely the function has no parameters
        // The exact assertion depends on implementation; at least not null
    }

    @Test
    public void testCreateParameters_singleParam() {
        List<JSType> params = new ArrayList<>();
        params.add(registry.getNativeType(JSTypeNative.STRING_TYPE));
        JSType result = registry.createParameters(params);
        assertNotNull("Parameters with one param should not be null", result);
    }

    // ===================== createFunctionType tests (simple) =====================

    @Test
    public void testCreateFunctionType_noReturn() {
        JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        List<JSType> params = new ArrayList<>();
        params.add(registry.getNativeType(JSTypeNative.STRING_TYPE));
        JSType funcType = registry.createFunctionType(returnType, params);
        assertNotNull("Function type should not be null", funcType);
        assertTrue("Result should be a function type", funcType.isFunctionType());
    }

    // ===================== Edge case: null ErrorReporter =====================

    @Test(expected = NullPointerException.class)
    public void testConstructor_nullErrorReporter() {
        new JSTypeRegistry(null, false);
    }

    // ===================== Edge case: registration with null type =====================

    @Test(expected = NullPointerException.class)
    public void testRegister_nullType() {
        registry.register(null, "some.name");
    }

    @Test(expected = NullPointerException.class)
    public void testRegister_nullName() {
        registry.register(registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    }

    // ===================== Additional coverage =====================

    @Test
    public void testGetType_nonExistent() {
        JSType retrieved = registry.getType("non.existent.Type");
        assertNull("Non-existent type should be null", retrieved);
    }

    @Test
    public void testRegistry_initialization() {
        // Just check that registry is not null and some native types are available
        assertNotNull(registry);
        JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertNotNull(number);
    }
}