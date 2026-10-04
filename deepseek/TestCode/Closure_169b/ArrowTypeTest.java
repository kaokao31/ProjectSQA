package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArrowTypeTest {

    private JSTypeRegistry registry;
    private JSType returnType;
    private Node parameters;
    private ArrowType arrowType;

    @Before
    public void setUp() {
        // Create a minimal error reporter for the registry
        ErrorReporter errorReporter = new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, int lineOffset) {
                // no-op
            }

            @Override
            public void error(String message, String sourceName, int line, int lineOffset) {
                // no-op
            }
        };
        registry = new JSTypeRegistry(errorReporter);
        returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        parameters = new Node(Token.PARAM_LIST);
        Node param = Node.newString(Token.NAME, "x");
        parameters.addChildToBack(param);
        arrowType = new ArrowType(registry, parameters, returnType);
    }

    @Test
    public void testIsArrowType() {
        assertTrue(arrowType.isArrowType());
    }

    @Test
    public void testGetReturnType() {
        assertSame(returnType, arrowType.getReturnType());
    }

    @Test
    public void testGetParameters() {
        assertSame(parameters, arrowType.getParameters());
    }

    @Test
    public void testToString() {
        String str = arrowType.toString();
        assertNotNull(str);
        assertTrue(str.contains("number"));
        assertTrue(str.contains("x"));
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(arrowType.equals(arrowType));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(arrowType.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(arrowType.equals("string"));
    }

    @Test
    public void testEqualsSameValues() {
        ArrowType other = new ArrowType(registry, parameters, returnType);
        assertTrue(arrowType.equals(other));
    }

    @Test
    public void testEqualsDifferentReturnType() {
        JSType otherReturn = registry.getNativeType(JSTypeNative.STRING_TYPE);
        ArrowType other = new ArrowType(registry, parameters, otherReturn);
        assertFalse(arrowType.equals(other));
    }

    @Test
    public void testEqualsDifferentParameters() {
        Node otherParams = new Node(Token.PARAM_LIST);
        Node param = Node.newString(Token.NAME, "y");
        otherParams.addChildToBack(param);
        ArrowType other = new ArrowType(registry, otherParams, returnType);
        assertFalse(arrowType.equals(other));
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = arrowType.hashCode();
        int hash2 = arrowType.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCodeEqualObjects() {
        ArrowType other = new ArrowType(registry, parameters, returnType);
        assertEquals(arrowType.hashCode(), other.hashCode());
    }

    @Test
    public void testConstructorWithNullParameters() {
        try {
            ArrowType nullParams = new ArrowType(registry, null, returnType);
            assertNotNull(nullParams);
            assertNull(nullParams.getParameters());
        } catch (NullPointerException e) {
            fail("Constructor should handle null parameters");
        }
    }

    @Test
    public void testConstructorWithNullReturnType() {
        try {
            ArrowType nullReturn = new ArrowType(registry, parameters, null);
            assertNotNull(nullReturn);
            assertNull(nullReturn.getReturnType());
        } catch (NullPointerException e) {
            fail("Constructor should handle null return type");
        }
    }

    @Test
    public void testConstructorWithBothNull() {
        try {
            ArrowType bothNull = new ArrowType(registry, null, null);
            assertNotNull(bothNull);
            assertNull(bothNull.getParameters());
            assertNull(bothNull.getReturnType());
        } catch (NullPointerException e) {
            fail("Constructor should handle null parameters and return type");
        }
    }

    @Test
    public void testEqualsWithNullParameters() {
        ArrowType withNullParams = new ArrowType(registry, null, returnType);
        ArrowType other = new ArrowType(registry, null, returnType);
        assertTrue(withNullParams.equals(other));
    }

    @Test
    public void testEqualsWithNullReturnType() {
        ArrowType withNullReturn = new ArrowType(registry, parameters, null);
        ArrowType other = new ArrowType(registry, parameters, null);
        assertTrue(withNullReturn.equals(other));
    }

    @Test
    public void testEqualsOneNullParamOtherNotNull() {
        ArrowType withNullParams = new ArrowType(registry, null, returnType);
        ArrowType withParams = new ArrowType(registry, parameters, returnType);
        assertFalse(withNullParams.equals(withParams));
    }

    @Test
    public void testEqualsOneNullReturnOtherNotNull() {
        ArrowType withNullReturn = new ArrowType(registry, parameters, null);
        ArrowType withReturn = new ArrowType(registry, parameters, returnType);
        assertFalse(withNullReturn.equals(withReturn));
    }
}