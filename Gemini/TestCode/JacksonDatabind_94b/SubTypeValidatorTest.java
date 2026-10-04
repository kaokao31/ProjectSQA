package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class SubTypeValidatorTest {

    private SubTypeValidator validator;

    @Before
    public void setUp() {
        validator = SubTypeValidator.instance();
    }

    @Test
    public void testInstance() {
        assertNotNull(validator);
        assertSame(validator, SubTypeValidator.instance());
    }

    @Test
    public void testValidateSubTypeSafeClasses() {
        DeserializationContext ctxt = null;
        
        // Basic safe JDK types
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType integerType = TypeFactory.defaultInstance().constructType(Integer.class);
        JavaType objectType = TypeFactory.defaultInstance().constructType(Object.class);
        JavaType booleanType = TypeFactory.defaultInstance().constructType(Boolean.class);

        // Should not throw any exception
        validator.validateSubType(ctxt, stringType);
        validator.validateSubType(ctxt, integerType);
        validator.validateSubType(ctxt, objectType);
        validator.validateSubType(ctxt, booleanType);
    }

    @Test
    public void testValidateSubTypeBlockedClasses() {
        DeserializationContext ctxt = null;

        // Construct types that are typically blocked or match blacklist patterns in Jackson 94
        // e.g., Spring framework or other known dangerous gadgets if present, 
        // or check generic class names matched by SubTypeValidator.
        // Let's test a class known to be on standard blocklists or similar pattern.
        
        // Common dangerous class patterns often checked:
        // org.springframework.context.support.ClassPathXmlApplicationContext
        // com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl
        
        try {
            Class<?> clazz = Class.forName("org.springframework.context.support.ClassPathXmlApplicationContext");
            JavaType badType = TypeFactory.defaultInstance().constructType(clazz);
            validator.validateSubType(ctxt, badType);
            fail("Expected exception for blacklisted subtype");
        } catch (ClassNotFoundException e) {
            // Spring not on classpath, try another or ignore
        } catch (Exception e) {
            // Expected validation failure (JsonMappingException)
            assertNotNull(e.getMessage());
        }

        try {
            Class<?> clazz = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl");
            JavaType badType = TypeFactory.defaultInstance().constructType(clazz);
            validator.validateSubType(ctxt, badType);
            fail("Expected exception for TemplatesImpl subtype");
        } catch (ClassNotFoundException e) {
            // Not on classpath in some JDK environments
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCustomSubTypesAndEdgeCases() {
        DeserializationContext ctxt = null;
        
        // Test validating an ordinary user-defined safe class
        JavaType safeUserType = TypeFactory.defaultInstance().constructType(SafeUserClass.class);
        validator.validateSubType(ctxt, safeUserType);
        
        // Array types
        JavaType arrayType = TypeFactory.defaultInstance().constructArrayType(String.class);
        validator.validateSubType(ctxt, arrayType);
        
        // Collection types
        JavaType collType = TypeFactory.defaultInstance().constructCollectionType(java.util.ArrayList.class, String.class);
        validator.validateSubType(ctxt, collType);
    }

    // Dummy safe class for testing
    public static class SafeUserClass {
        public String name;
    }
}