package com.fasterxml.jackson.databind.jsontype.impl;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for SubTypeValidator.
 * Designed to achieve high line/branch coverage and detect potential faults
 * related to subtype validation (e.g., missing dangerous class checks).
 */
public class SubTypeValidatorTest {

    private SubTypeValidator validator;

    @Before
    public void setUp() {
        // SubTypeValidator is typically a singleton or static utility.
        // We assume it has a static validateSubType method.
        // If it's an instance, we instantiate it here.
        validator = new SubTypeValidator();
    }

    // ---------- Null and edge cases ----------

    @Test(expected = IllegalArgumentException.class)
    public void testValidateNullSubtype() {
        SubTypeValidator.validateSubType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidatePrimitiveType() {
        SubTypeValidator.validateSubType(int.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateVoidType() {
        SubTypeValidator.validateSubType(void.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateArrayType() {
        SubTypeValidator.validateSubType(Object[].class);
    }

    // ---------- Known safe types ----------

    @Test
    public void testValidateString() {
        // String is typically safe
        SubTypeValidator.validateSubType(String.class);
        // No exception expected
    }

    @Test
    public void testValidateInteger() {
        SubTypeValidator.validateSubType(Integer.class);
    }

    @Test
    public void testValidateArrayList() {
        SubTypeValidator.validateSubType(java.util.ArrayList.class);
    }

    @Test
    public void testValidateHashMap() {
        SubTypeValidator.validateSubType(java.util.HashMap.class);
    }

    @Test
    public void testValidateCustomSafeClass() {
        SubTypeValidator.validateSubType(MySafeClass.class);
    }

    // ---------- Potentially dangerous types (should be rejected) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testValidateObjectClass() {
        // Object is a known dangerous type for deserialization
        SubTypeValidator.validateSubType(Object.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateSerializable() {
        SubTypeValidator.validateSubType(java.io.Serializable.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateCloneable() {
        SubTypeValidator.validateSubType(Cloneable.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateThrowable() {
        SubTypeValidator.validateSubType(Throwable.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateException() {
        SubTypeValidator.validateSubType(Exception.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateRuntimeException() {
        SubTypeValidator.validateSubType(RuntimeException.class);
    }

    // ---------- Known dangerous classes from Jackson's blacklist ----------

    @Test(expected = IllegalArgumentException.class)
    public void testValidateJndiDataSource() {
        // javax.naming.InitialContext is a known dangerous class
        SubTypeValidator.validateSubType(javax.naming.InitialContext.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateLog4jSocketAppender() {
        // org.apache.log4j.net.SocketAppender is dangerous
        SubTypeValidator.validateSubType(org.apache.log4j.net.SocketAppender.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateJdbcRowSetImpl() {
        // com.sun.rowset.JdbcRowSetImpl is dangerous
        SubTypeValidator.validateSubType(com.sun.rowset.JdbcRowSetImpl.class);
    }

    // ---------- Edge cases with inner classes, anonymous classes ----------

    @Test(expected = IllegalArgumentException.class)
    public void testValidateAnonymousClass() {
        Object anonymous = new Object() {};
        SubTypeValidator.validateSubType(anonymous.getClass());
    }

    @Test
    public void testValidateLocalClass() {
        class LocalClass {}
        SubTypeValidator.validateSubType(LocalClass.class);
    }

    // ---------- Test with custom class that extends a dangerous type ----------

    @Test(expected = IllegalArgumentException.class)
    public void testValidateSubclassOfDangerous() {
        // If a class extends a dangerous type, it should also be rejected
        SubTypeValidator.validateSubType(MyDangerousSubclass.class);
    }

    // ---------- Helper classes ----------

    static class MySafeClass {
        // empty
    }

    static class MyDangerousSubclass extends Exception {
        // empty
    }
}