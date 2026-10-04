package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.RecordType;
import com.google.javascript.rhino.jstype.StaticScope;
import com.google.javascript.rhino.jstype.UnionType;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RecordTypeTest {

    private JSTypeRegistry registry;
    private ErrorReporter errorReporter;

    @Before
    public void setUp() {
        errorReporter = new SimpleErrorReporter();
        registry = new JSTypeRegistry(errorReporter);
    }

    @Test
    public void testRecordTypeCreationAndProperties() {
        Map<String, JSType> properties = new HashMap<String, JSType>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

        properties.put("a", stringType);
        properties.put("b", numberType);

        RecordType recordType = new RecordType(registry, properties);

        assertTrue(recordType.hasProperty("a"));
        assertTrue(recordType.hasProperty("b"));
        assertFalse(recordType.hasProperty("c"));

        assertEquals(stringType, recordType.getPropertyType("a"));
        assertEquals(numberType, recordType.getPropertyType("b"));
        assertNull(recordType.getPropertyType("c"));

        assertTrue(recordType.isRecordType());
        assertFalse(recordType.isNominalType());
    }

    @Test
    public void testRecordTypeEqualityAndDiff() {
        Map<String, JSType> props1 = new HashMap<String, JSType>();
        Map<String, JSType> props2 = new HashMap<String, JSType>();
        Map<String, JSType> props3 = new HashMap<String, JSType>();

        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

        props1.put("a", stringType);
        props2.put("a", stringType);

        props3.put("a", stringType);
        props3.put("b", numberType);

        RecordType record1 = new RecordType(registry, props1);
        RecordType record2 = new RecordType(registry, props2);
        RecordType record3 = new RecordType(registry, props3);

        assertTrue(record1.isEquivalentTo(record2));
        assertFalse(record1.isEquivalentTo(record3));

        // Test diff
        DisputeTypeDiff diff = new DisputeTypeDiff();
        // RecordType relies on JSType registry and object mechanisms
        assertNotNull(record1.toString());
        assertNotNull(record3.toString());
    }

    @Test
    public void testRecordTypeWithEmptyProperties() {
        Map<String, JSType> properties = new HashMap<String, JSType>();
        RecordType recordType = new RecordType(registry, properties);

        assertTrue(recordType.isRecordType());
        assertFalse(recordType.hasProperty("any"));
        assertEquals("{  }", recordType.toString());
    }

    @Test
    public void testRecordTypeResolve() {
        Map<String, JSType> properties = new HashMap<String, JSType>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        properties.put("prop", stringType);

        RecordType recordType = new RecordType(registry, properties);
        JSType resolved = recordType.resolve(errorReporter, null);
        assertNotNull(resolved);
        assertTrue(resolved.isRecordType());
    }

    @Test
    public void testGetLeastSupertype() {
        Map<String, JSType> props1 = new HashMap<String, JSType>();
        Map<String, JSType> props2 = new HashMap<String, JSType>();

        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

        props1.put("a", stringType);
        props2.put("a", numberType);

        RecordType record1 = new RecordType(registry, props1);
        RecordType record2 = new RecordType(registry, props2);

        JSType superType = record1.getLeastSupertype(record2);
        assertNotNull(superType);
    }

    @Test
    public void testGetPropertyNode() {
        Map<String, JSType> properties = new HashMap<String, JSType>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        properties.put("a", stringType);

        RecordType recordType = new RecordType(registry, properties);
        assertNull(recordType.getPropertyNode("a"));
    }

    @Test
    public void testGetPropertyOwner() {
        Map<String, JSType> properties = new HashMap<String, JSType>();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        properties.put("a", stringType);

        RecordType recordType = new RecordType(registry, properties);
        assertEquals(recordType, recordType.getPropertyOwner("a"));
        assertNull(recordType.getPropertyOwner("nonexistent"));
    }

    @Test
    public void testIsSubtype() {
        Map<String, JSType> props1 = new HashMap<String, JSType>();
        Map<String, JSType> props2 = new HashMap<String, JSType>();

        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        props1.put("a", stringType);
        props2.put("a", stringType);
        props2.put("b", stringType);

        RecordType record1 = new RecordType(registry, props1);
        RecordType record2 = new RecordType(registry, props2);

        // record2 has more properties, so record2 is a subtype of record1 (has at least all properties of record1)
        assertTrue(record2.isSubtype(record1));
        assertFalse(record1.isSubtype(record2));
    }

    // Helper class for checking diffs if needed
    private static class DisputeTypeDiff {
        // Placeholder for type diff expectations
    }
}