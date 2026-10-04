package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import java.util.Date;
import java.io.File;
import java.net.URL;

public class TypeHandlerTest {

    @Test
    public void testCreateValueWithBooleanTrue() {
        Object result = TypeHandler.createValue("true", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testCreateValueWithBooleanFalse() {
        Object result = TypeHandler.createValue("false", Boolean.class);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testCreateValueWithBooleanLowerCase() {
        Object result = TypeHandler.createValue("true", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testCreateValueWithBooleanMixedCase() {
        Object result = TypeHandler.createValue("TRUE", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateValueWithInvalidBoolean() {
        TypeHandler.createValue("invalid", Boolean.class);
    }

    @Test
    public void testCreateValueWithInteger() {
        Object result = TypeHandler.createValue("42", Integer.class);
        assertEquals(42, result);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateValueWithInvalidInteger() {
        TypeHandler.createValue("abc", Integer.class);
    }

    @Test
    public void testCreateValueWithLong() {
        Object result = TypeHandler.createValue("1234567890123", Long.class);
        assertEquals(1234567890123L, result);
    }

    @Test
    public void testCreateValueWithDouble() {
        Object result = TypeHandler.createValue("3.14", Double.class);
        assertEquals(3.14, (Double) result, 0.001);
    }

    @Test
    public void testCreateValueWithFloat() {
        Object result = TypeHandler.createValue("2.5", Float.class);
        assertEquals(2.5f, (Float) result, 0.0001);
    }

    @Test
    public void testCreateValueWithShort() {
        Object result = TypeHandler.createValue("100", Short.class);
        assertEquals(100, (short) result);
    }

    @Test
    public void testCreateValueWithByte() {
        Object result = TypeHandler.createValue("10", Byte.class);
        assertEquals(10, (byte) result);
    }

    @Test
    public void testCreateValueWithCharacterSingleChar() {
        Object result = TypeHandler.createValue("A", Character.class);
        assertEquals('A', result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateValueWithCharacterEmptyString() {
        TypeHandler.createValue("", Character.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateValueWithCharacterLongString() {
        TypeHandler.createValue("AB", Character.class);
    }

    @Test
    public void testCreateValueWithString() {
        Object result = TypeHandler.createValue("hello", String.class);
        assertEquals("hello", result);
    }

    @Test
    public void testCreateValueWithNullString() {
        Object result = TypeHandler.createValue("null", String.class);
        assertEquals("null", result);
    }

    @Test
    public void testCreateValueWithFile() {
        Object result = TypeHandler.createValue("/tmp/test.txt", File.class);
        assertTrue(result instanceof File);
        assertEquals(new File("/tmp/test.txt"), result);
    }

    @Test
    public void testCreateValueWithURL() throws Exception {
        Object result = TypeHandler.createValue("http://example.com", URL.class);
        assertTrue(result instanceof URL);
        assertEquals(new URL("http://example.com"), result);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateValueWithMalformedURL() {
        TypeHandler.createValue("not a url", URL.class);
    }

    @Test
    public void testCreateValueWithDate() {
        // Assuming Date is handled via constructor that takes a string
        Object result = TypeHandler.createValue("2021-01-01", Date.class);
        assertNotNull(result);
        assertTrue(result instanceof Date);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateValueWithInvalidDate() {
        // If the Date constructor throws an exception
        TypeHandler.createValue("invalid date", Date.class);
    }

    @Test
    public void testCreateValueWithClassHavingStringConstructor() {
        // Class that has a constructor that takes a String
        Object result = TypeHandler.createValue("test", StringBuilder.class);
        assertNotNull(result);
        assertTrue(result instanceof StringBuilder);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateValueWithClassWithoutStringConstructor() {
        // Class that does NOT have a constructor that takes a String
        // This is the bug path for Cli-39: if the class doesn't have a String constructor,
        // it should throw a RuntimeException, but the bug might be that it throws something else
        // or fails silently.
        TypeHandler.createValue("value", Object.class);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateValueWithNullType() {
        TypeHandler.createValue("test", null);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateValueWithNullValue() {
        TypeHandler.createValue(null, String.class);
    }

    @Test
    public void testCreateValueWithEmptyStringForNonSpecialType() {
        // This test ensures that empty strings are handled correctly
        Object result = TypeHandler.createValue("", File.class);
        assertNotNull(result);
        assertEquals(new File(""), result);
    }

    @Test
    public void testCreateValueWithPrimitiveTypes() {
        // Primitive types are not expected to be passed; but if they are,
        // the method should handle them gracefully (e.g., treat as wrapper).
        Object result = TypeHandler.createValue("42", int.class);
        assertEquals(42, result);
    }

    @Test
    public void testCreateValueWithVoidType() {
        // Void.class is not a valid type for creation
        try {
            TypeHandler.createValue("test", Void.class);
            fail("Expected RuntimeException for Void.class");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testCreateValueWithNullTypeAndEmptyString() {
        // Both null type and empty value
        try {
            TypeHandler.createValue("", null);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testDefaultConstructor() {
        // Ensure TypeHandler can be instantiated (if needed)
        TypeHandler handler = new TypeHandler();
        assertNotNull(handler);
    }

    // Additional edge cases for known bugs (Cli-39)
    // The bug might be that certain classes without String constructor cause a
    // ClassNotFoundException or InstantiationException that is not properly wrapped.
    @Test(expected = RuntimeException.class)
    public void testCreateValueWithClassNotPublic() {
        // Using a package-private class that has no String constructor
        // This may trigger the bug
        TypeHandler.createValue("test", TypeHandlerTest.class); // This class is public, but has no String constructor
        // Actually TypeHandlerTest has no String constructor, so it will fail.
        // But we need a class without public String constructor; TypeHandlerTest is public but its constructor is default?
        // Use Object.class for no constructor
        TypeHandler.createValue("test", Object.class);
    }

    // Test for bug where the exception message is missing or incorrect
    @Test
    public void testCreateValueExceptionMessage() {
        try {
            TypeHandler.createValue("test", Object.class);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // Ensure the exception message is informative
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Object") || e.getMessage().contains("constructor"));
        }
    }

    // Test for bug where calendar or timezone-related classes are used
    @Test(expected = RuntimeException.class)
    public void testCreateValueWithAbstractClass() {
        TypeHandler.createValue("test", Number.class);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateValueWithInterface() {
        TypeHandler.createValue("test", Comparable.class);
    }

    // Test for bug where array class is passed
    @Test(expected = RuntimeException.class)
    public void testCreateValueWithArrayClass() {
        TypeHandler.createValue("test", String[].class);
    }

    // Test for null value with Boolean class (edge case)
    @Test(expected = RuntimeException.class)
    public void testCreateValueNullStringWithBoolean() {
        TypeHandler.createValue(null, Boolean.class);
    }
}