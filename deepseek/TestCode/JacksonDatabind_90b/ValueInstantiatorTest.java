package com.fasterxml.jackson.databind.deser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for ValueInstantiator.
 * Covers all public methods, edge cases, and potential fault paths.
 */
public class ValueInstantiatorTest {

    private ValueInstantiator instantiator;

    @Before
    public void setUp() {
        // Use a concrete subclass that exposes default behavior
        instantiator = new ValueInstantiator() {
            @Override
            public String getValueTypeDesc() {
                return "TestType";
            }

            @Override
            public Class<?> getValueClass() {
                return Object.class;
            }
        };
    }

    // --- canCreateUsingDefault / createUsingDefault ---
    @Test
    public void testCanCreateUsingDefault_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateUsingDefault());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateUsingDefault_throwsException() {
        instantiator.createUsingDefault(null);
    }

    // --- canCreateFromString / createFromString ---
    @Test
    public void testCanCreateFromString_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateFromString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromString_throwsException() {
        instantiator.createFromString(null, "test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromString_nullString_throwsException() {
        instantiator.createFromString(null, null);
    }

    // --- canCreateFromInt / createFromInt ---
    @Test
    public void testCanCreateFromInt_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateFromInt());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromInt_throwsException() {
        instantiator.createFromInt(null, 42);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromInt_zero_throwsException() {
        instantiator.createFromInt(null, 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromInt_negative_throwsException() {
        instantiator.createFromInt(null, -1);
    }

    // --- canCreateFromLong / createFromLong ---
    @Test
    public void testCanCreateFromLong_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateFromLong());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromLong_throwsException() {
        instantiator.createFromLong(null, 100L);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromLong_zero_throwsException() {
        instantiator.createFromLong(null, 0L);
    }

    // --- canCreateFromDouble / createFromDouble ---
    @Test
    public void testCanCreateFromDouble_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateFromDouble());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromDouble_throwsException() {
        instantiator.createFromDouble(null, 3.14);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromDouble_NaN_throwsException() {
        instantiator.createFromDouble(null, Double.NaN);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromDouble_infinity_throwsException() {
        instantiator.createFromDouble(null, Double.POSITIVE_INFINITY);
    }

    // --- canCreateFromBoolean / createFromBoolean ---
    @Test
    public void testCanCreateFromBoolean_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateFromBoolean());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromBoolean_true_throwsException() {
        instantiator.createFromBoolean(null, true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromBoolean_false_throwsException() {
        instantiator.createFromBoolean(null, false);
    }

    // --- canCreateFromObjectWith / createFromObjectWith ---
    @Test
    public void testCanCreateFromObjectWith_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateFromObjectWith());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromObjectWith_throwsException() {
        instantiator.createFromObjectWith(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromObjectWith_emptyArgs_throwsException() {
        instantiator.createFromObjectWith(null, new Object[0], new Object[0]);
    }

    // --- canCreateFromDelegate / createFromDelegate ---
    @Test
    public void testCanCreateFromDelegate_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateFromDelegate());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromDelegate_throwsException() {
        instantiator.createFromDelegate(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromDelegate_nullValue_throwsException() {
        instantiator.createFromDelegate(null, null, "value");
    }

    // --- canCreateFromArrayDelegate / createFromArrayDelegate ---
    @Test
    public void testCanCreateFromArrayDelegate_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateFromArrayDelegate());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromArrayDelegate_throwsException() {
        instantiator.createFromArrayDelegate(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromArrayDelegate_nullValue_throwsException() {
        instantiator.createFromArrayDelegate(null, null, "array");
    }

    // --- getValueTypeDesc ---
    @Test
    public void testGetValueTypeDesc_returnsConfigured() {
        assertEquals("TestType", instantiator.getValueTypeDesc());
    }

    // --- getValueClass ---
    @Test
    public void testGetValueClass_returnsConfigured() {
        assertEquals(Object.class, instantiator.getValueClass());
    }

    // --- getDelegateType ---
    @Test
    public void testGetDelegateType_returnsNull() {
        assertNull("Default implementation should return null", instantiator.getDelegateType(null));
    }

    @Test
    public void testGetDelegateType_withNullConfig_returnsNull() {
        assertNull("Should return null even with null config", instantiator.getDelegateType(null));
    }

    // --- getArrayDelegateType ---
    @Test
    public void testGetArrayDelegateType_returnsNull() {
        assertNull("Default implementation should return null", instantiator.getArrayDelegateType(null));
    }

    @Test
    public void testGetArrayDelegateType_withNullConfig_returnsNull() {
        assertNull("Should return null even with null config", instantiator.getArrayDelegateType(null));
    }

    // --- getFromObjectArguments ---
    @Test
    public void testGetFromObjectArguments_returnsNull() {
        assertNull("Default implementation should return null", instantiator.getFromObjectArguments(null));
    }

    @Test
    public void testGetFromObjectArguments_withNullConfig_returnsNull() {
        assertNull("Should return null even with null config", instantiator.getFromObjectArguments(null));
    }

    // --- canCreateUsingArrayDelegate ---
    @Test
    public void testCanCreateUsingArrayDelegate_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateUsingArrayDelegate());
    }

    // --- canCreateUsingDelegate ---
    @Test
    public void testCanCreateUsingDelegate_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canCreateUsingDelegate());
    }

    // --- canInstantiate ---
    @Test
    public void testCanInstantiate_returnsFalse() {
        assertFalse("Default implementation should return false", instantiator.canInstantiate());
    }

    // --- completeConstruction ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCompleteConstruction_throwsException() {
        instantiator.completeConstruction(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCompleteConstruction_nullValue_throwsException() {
        instantiator.completeConstruction(null, null);
    }

    // --- createUsingDefault with null deserialization context ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateUsingDefault_nullContext_throwsException() {
        instantiator.createUsingDefault(null);
    }

    // --- createFromString with empty string ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromString_emptyString_throwsException() {
        instantiator.createFromString(null, "");
    }

    // --- createFromInt with Integer.MAX_VALUE ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromInt_maxValue_throwsException() {
        instantiator.createFromInt(null, Integer.MAX_VALUE);
    }

    // --- createFromLong with Long.MIN_VALUE ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromLong_minValue_throwsException() {
        instantiator.createFromLong(null, Long.MIN_VALUE);
    }

    // --- createFromDouble with negative zero ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromDouble_negativeZero_throwsException() {
        instantiator.createFromDouble(null, -0.0);
    }

    // --- createFromBoolean with null context ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromBoolean_nullContext_throwsException() {
        instantiator.createFromBoolean(null, true);
    }

    // --- createFromObjectWith with null arguments ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromObjectWith_nullArgs_throwsException() {
        instantiator.createFromObjectWith(null, null, null);
    }

    // --- createFromDelegate with null delegate ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromDelegate_nullDelegate_throwsException() {
        instantiator.createFromDelegate(null, null, null);
    }

    // --- createFromArrayDelegate with null delegate ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFromArrayDelegate_nullDelegate_throwsException() {
        instantiator.createFromArrayDelegate(null, null, null);
    }

    // --- completeConstruction with null value ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCompleteConstruction_nullValue_throwsException2() {
        instantiator.completeConstruction(null, null);
    }

    // --- Additional edge: getValueTypeDesc on anonymous subclass ---
    @Test
    public void testGetValueTypeDesc_anonymous() {
        ValueInstantiator anon = new ValueInstantiator() {
            @Override
            public String getValueTypeDesc() {
                return "Anonymous";
            }

            @Override
            public Class<?> getValueClass() {
                return String.class;
            }
        };
        assertEquals("Anonymous", anon.getValueTypeDesc());
        assertEquals(String.class, anon.getValueClass());
    }

    // --- Test that default implementations do not throw for can* methods ---
    @Test
    public void testAllCanMethodsReturnFalseWithoutException() {
        assertFalse(instantiator.canCreateUsingDefault());
        assertFalse(instantiator.canCreateFromString());
        assertFalse(instantiator.canCreateFromInt());
        assertFalse(instantiator.canCreateFromLong());
        assertFalse(instantiator.canCreateFromDouble());
        assertFalse(instantiator.canCreateFromBoolean());
        assertFalse(instantiator.canCreateFromObjectWith());
        assertFalse(instantiator.canCreateUsingDelegate());
        assertFalse(instantiator.canCreateUsingArrayDelegate());
        assertFalse(instantiator.canInstantiate());
    }

    // --- Test that get*Type methods return null without exception ---
    @Test
    public void testGetTypeMethodsReturnNullWithoutException() {
        assertNull(instantiator.getDelegateType(null));
        assertNull(instantiator.getArrayDelegateType(null));
        assertNull(instantiator.getFromObjectArguments(null));
    }

    // --- Test that create* methods throw UnsupportedOperationException ---
    @Test(expected = UnsupportedOperationException.class)
    public void testAllCreateMethodsThrow() {
        instantiator.createUsingDefault(null);
    }

    // --- Test that completeConstruction throws ---
    @Test(expected = UnsupportedOperationException.class)
    public void testCompleteConstructionThrows() {
        instantiator.completeConstruction(null, null);
    }
}