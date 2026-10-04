package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ValueInstantiatorTest {

    // Concrete implementation of abstract ValueInstantiator to test default method behaviors
    private static class DummyValueInstantiator extends ValueInstantiator {
        @Override
        public String getValueTypeDesc() {
            return "dummy-type";
        }
    }

    @Test
    public void testDefaultMethods() {
        ValueInstantiator inst = new DummyValueInstantiator();

        assertEquals("dummy-type", inst.getValueTypeDesc());
        assertFalse(inst.canCreateUsingDefault());
        assertFalse(inst.canCreateUsingDelegate());
        assertFalse(inst.canCreateUsingArrayDelegate());
        assertFalse(inst.canCreateFromObjectWith());
        assertFalse(inst.canCreateFromString());
        assertFalse(inst.canCreateFromInt());
        assertFalse(inst.canCreateFromLong());
        assertFalse(inst.canCreateFromDouble());
        assertFalse(inst.canCreateFromBoolean());

        assertNull(inst.getDelegateType(null));
        assertNull(inst.getArrayDelegateType(null));
        assertNull(inst.getFromObjectArguments(null));
        assertNull(inst.getDefaultCreator());
        assertNull(inst.getWithCreator());
        assertNull(inst.getArrayDelegateCreator());
        assertNull(inst.getDelegateCreator());

        JavaType type = TypeFactory.defaultInstance().constructType(Object.class);
        try {
            inst.createUsingDefault(null);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        try {
            inst.createUsingDelegate(null, "value");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        try {
            inst.createUsingArrayDelegate(null, "value");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        try {
            inst.createFromObjectWith(null, new Object[0]);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        try {
            inst.createFromString(null, "test");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        try {
            inst.createFromInt(null, 123);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        try {
            inst.createFromLong(null, 123L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        try {
            inst.createFromDouble(null, 1.23);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        try {
            inst.createFromBoolean(null, true);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }

        assertNull(inst.getCompleteNotNullables());
        assertNull(inst.getMissingProperties());
        assertNull(inst.mapProperties(null));
        assertNull(inst.createProcessedProperties(null, null, null, null));
    }

    @Test
    public void testGetIncompleteParameter() {
        ValueInstantiator inst = new DummyValueInstantiator();
        assertNull(inst.getIncompleteParameter());
    }
}