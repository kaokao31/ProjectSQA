package com.fasterxml.jackson.databind.jsontype.impl;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for SubTypeValidator, focusing on the
 * bug fix for JacksonDatabind issue #94 (security vulnerability
 * related to unchecked dangerous types).
 */
public class SubTypeValidatorTest {

    private SubTypeValidator validator;

    @org.junit.Before
    public void setUp() throws Exception {
        // Obtain the singleton instance (common pattern in Jackson)
        validator = SubTypeValidator.instance();
    }

    // ------ Positive tests: safe types must not throw ------

    @Test
    public void testSafeTypeString() throws Exception {
        validator.validateBaseType(String.class);
    }

    @Test
    public void testSafeTypeInteger() throws Exception {
        validator.validateBaseType(Integer.class);
    }

    @Test
    public void testSafeTypeArrayList() throws Exception {
        validator.validateBaseType(java.util.ArrayList.class);
    }

    @Test
    public void testSafeTypeObject() throws Exception {
        validator.validateBaseType(Object.class);
    }

    @Test
    public void testSafeTypeAbstractList() throws Exception {
        validator.validateBaseType(java.util.AbstractList.class);
    }

    @Test
    public void testSafeTypeArray() throws Exception {
        validator.validateBaseType(String[].class);
    }

    @Test
    public void testSafeTypePrimitive() throws Exception {
        validator.validateBaseType(int.class);
    }

    // ------ Negative tests: dangerous types must be rejected ------

    @Test(expected = IllegalArgumentException.class)
    public void testBlockedProcessBuilder() throws Exception {
        validator.validateBaseType(ProcessBuilder.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockedRuntime() throws Exception {
        validator.validateBaseType(Runtime.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockedSystem() throws Exception {
        validator.validateBaseType(System.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockedClass() throws Exception {
        validator.validateBaseType(Class.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockedThread() throws Exception {
        validator.validateBaseType(Thread.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockedProcess() throws Exception {
        validator.validateBaseType(Process.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockedRuntimePermission() throws Exception {
        validator.validateBaseType(java.security.AllPermission.class);
    }

    // ------ Edge cases ------

    @Test(expected = NullPointerException.class)
    public void testNullClass() throws Exception {
        // The implementation might throw NPE on null class reference
        validator.validateBaseType(null);
    }

    // ------ Test that multiple calls do not change state ------
    @Test
    public void testMultipleSafeCalls() throws Exception {
        validator.validateBaseType(String.class);
        validator.validateBaseType(Integer.class);
        validator.validateBaseType(Object.class);
    }

    // ------ Test that a blocked call after a safe call still works ------
    @Test(expected = IllegalArgumentException.class)
    public void testSafeThenBlocked() throws Exception {
        validator.validateBaseType(String.class);
        validator.validateBaseType(ProcessBuilder.class);
    }
}