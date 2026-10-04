package org.apache.commons.cli;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Date;

/**
 * Test suite for TypeHandler class (Cli-40 bug context).
 * Achieves high coverage and targets potential faults.
 */
public class TypeHandlerTest {

    private TypeHandler handler;

    @Before
    public void setUp() {
        handler = new TypeHandler();
    }

    // --- createValue(String, Class) ---

    @Test
    public void testCreateValueWithStringClass() throws Exception {
        Object result = TypeHandler.createValue("hello", String.class);
        assertEquals("hello", result);
    }

    @Test
    public void testCreateValueWithIntegerClass() throws Exception {
        Object result = TypeHandler.createValue("123", Integer.class);
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testCreateValueWithLongClass() throws Exception {
        Object result = TypeHandler.createValue("456", Long.class);
        assertEquals(Long.valueOf(456L), result);
    }

    @Test
    public void testCreateValueWithFloatClass() throws Exception {
        Object result = TypeHandler.createValue("3.14", Float.class);
        assertEquals(Float.valueOf(3.14f), result);
    }

    @Test
    public void testCreateValueWithDoubleClass() throws Exception {
        Object result = TypeHandler.createValue("2.718", Double.class);
        assertEquals(Double.valueOf(2.718), result);
    }

    @Test
    public void testCreateValueWithNumberClass() throws Exception {
        // Bug context: Number.class should be handled (e.g., return a Long or Integer)
        Object result = TypeHandler.createValue("42", Number.class);
        assertNotNull(result);
        assertTrue(result instanceof Number);
        // Typically returns a Long
        assertEquals(Long.valueOf(42L), result);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateValueWithIntegerClassInvalid() throws Exception {
        TypeHandler.createValue("notanumber", Integer.class);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateValueWithLongClassInvalid() throws Exception {
        TypeHandler.createValue("12.5", Long.class);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateValueWithFloatClassInvalid() throws Exception {
        TypeHandler.createValue("abc", Float.class);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateValueWithDoubleClassInvalid() throws Exception {
        TypeHandler.createValue("", Double.class);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateValueWithNumberClassInvalid() throws Exception {
        TypeHandler.createValue("NaN", Number.class);
    }

    @Test
    public void testCreateValueWithBooleanClassTrue() throws Exception {
        Object result = TypeHandler.createValue("true", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testCreateValueWithBooleanClassFalse() throws Exception {
        Object result = TypeHandler.createValue("false", Boolean.class);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testCreateValueWithBooleanClassCaseInsensitive() throws Exception {
        Object result = TypeHandler.createValue("True", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateValueWithBooleanClassInvalid() throws Exception {
        TypeHandler.createValue("yes", Boolean.class);
    }

    @Test
    public void testCreateValueWithFileClass() throws Exception {
        Object result = TypeHandler.createValue("/tmp/test.txt", File.class);
        assertEquals(new File("/tmp/test.txt"), result);
    }

    @Test
    public void testCreateValueWithClassClass() throws Exception {
        Object result = TypeHandler.createValue("java.lang.String", Class.class);
        assertEquals(String.class, result);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testCreateValueWithClassClassNotFound() throws Exception {
        TypeHandler.createValue("NonExistentClass", Class.class);
    }

    @Test
    public void testCreateValueWithDateClass() throws Exception {
        // Date parsing may be locale-dependent; use a known format
        Object result = TypeHandler.createValue("2020-01-01", Date.class);
        assertNotNull(result);
        assertTrue(result instanceof Date);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateValueWithDateClassInvalid() throws Exception {
        TypeHandler.createValue("notadate", Date.class);
    }

    @Test
    public void testCreateValueWithURLClass() throws Exception {
        Object result = TypeHandler.createValue("http://example.com", URL.class);
        assertEquals(new URL("http://example.com"), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateValueWithURLClassInvalid() throws Exception {
        TypeHandler.createValue("not a url", URL.class);
    }

    @Test
    public void testCreateValueWithObjectClass() throws Exception {
        // Object.class should return the string itself
        Object result = TypeHandler.createValue("any", Object.class);
        assertEquals("any", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateValueWithUnsupportedClass() throws Exception {
        TypeHandler.createValue("test", StringBuilder.class);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateValueNullString() throws Exception {
        TypeHandler.createValue(null, String.class);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateValueNullClass() throws Exception {
        TypeHandler.createValue("test", null);
    }

    // --- createNumber(String) ---

    @Test
    public void testCreateNumberInteger() throws Exception {
        Number result = TypeHandler.createNumber("123");
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testCreateNumberLong() throws Exception {
        Number result = TypeHandler.createNumber("1234567890123");
        assertEquals(Long.valueOf(1234567890123L), result);
    }

    @Test
    public void testCreateNumberFloat() throws Exception {
        Number result = TypeHandler.createNumber("3.14");
        assertEquals(Float.valueOf(3.14f), result);
    }

    @Test
    public void testCreateNumberDouble() throws Exception {
        Number result = TypeHandler.createNumber("2.71828");
        assertEquals(Double.valueOf(2.71828), result);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmpty() throws Exception {
        TypeHandler.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberNull() throws Exception {
        TypeHandler.createNumber(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalid() throws Exception {
        TypeHandler.createNumber("abc");
    }

    // --- createObject(String) ---

    @Test
    public void testCreateObjectValidClass() throws Exception {
        Object result = TypeHandler.createObject("java.lang.String");
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testCreateObjectInvalidClass() throws Exception {
        TypeHandler.createObject("com.example.NonExistent");
    }

    @Test(expected = ClassNotFoundException.class)
    public void testCreateObjectEmptyString() throws Exception {
        TypeHandler.createObject("");
    }

    @Test(expected = ClassNotFoundException.class)
    public void testCreateObjectNull() throws Exception {
        TypeHandler.createObject(null);
    }

    // --- createClass(String) ---

    @Test
    public void testCreateClassValid() throws Exception {
        Class<?> result = TypeHandler.createClass("java.lang.Integer");
        assertEquals(Integer.class, result);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testCreateClassInvalid() throws Exception {
        TypeHandler.createClass("some.bogus.Class");
    }

    @Test(expected = ClassNotFoundException.class)
    public void testCreateClassEmpty() throws Exception {
        TypeHandler.createClass("");
    }

    @Test(expected = ClassNotFoundException.class)
    public void testCreateClassNull() throws Exception {
        TypeHandler.createClass(null);
    }

    // --- createFile(String) ---

    @Test
    public void testCreateFile() throws Exception {
        File result = TypeHandler.createFile("/path/to/file.txt");
        assertEquals(new File("/path/to/file.txt"), result);
    }

    @Test
    public void testCreateFileRelative() throws Exception {
        File result = TypeHandler.createFile("relative/path");
        assertEquals(new File("relative/path"), result);
    }

    @Test
    public void testCreateFileEmpty() throws Exception {
        File result = TypeHandler.createFile("");
        assertEquals(new File(""), result);
    }

    @Test
    public void testCreateFileNull() throws Exception {
        // Assuming null returns null or throws? Let's test behavior.
        try {
            File result = TypeHandler.createFile(null);
            assertNull(result);
        } catch (NullPointerException e) {
            // acceptable
        }
    }

    // --- createURL(String) ---

    @Test
    public void testCreateURLValid() throws Exception {
        URL result = TypeHandler.createURL("http://apache.org");
        assertEquals(new URL("http://apache.org"), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateURLInvalid() throws Exception {
        TypeHandler.createURL("not a url");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateURLEmpty() throws Exception {
        TypeHandler.createURL("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateURLNull() throws Exception {
        TypeHandler.createURL(null);
    }

    // --- createDate(String) ---

    @Test
    public void testCreateDateValid() throws Exception {
        Date result = TypeHandler.createDate("2021-12-25");
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDateInvalid() throws Exception {
        TypeHandler.createDate("invalid-date");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDateEmpty() throws Exception {
        TypeHandler.createDate("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDateNull() throws Exception {
        TypeHandler.createDate(null);
    }

    // --- Additional edge cases for createValue ---

    @Test
    public void testCreateValueWithIntegerClassNegative() throws Exception {
        Object result = TypeHandler.createValue("-100", Integer.class);
        assertEquals(Integer.valueOf(-100), result);
    }

    @Test
    public void testCreateValueWithLongClassMax() throws Exception {
        Object result = TypeHandler.createValue("9223372036854775807", Long.class);
        assertEquals(Long.MAX_VALUE, result);
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateValueWithLongClassOverflow() throws Exception {
        TypeHandler.createValue("9223372036854775808", Long.class);
    }

    @Test
    public void testCreateValueWithFloatClassInfinity() throws Exception {
        Object result = TypeHandler.createValue("Infinity", Float.class);
        assertEquals(Float.POSITIVE_INFINITY, result);
    }

    @Test
    public void testCreateValueWithDoubleClassNaN() throws Exception {
        Object result = TypeHandler.createValue("NaN", Double.class);
        assertEquals(Double.NaN, result);
    }

    @Test
    public void testCreateValueWithNumberClassNegative() throws Exception {
        Object result = TypeHandler.createValue("-1", Number.class);
        assertNotNull(result);
        assertTrue(result instanceof Number);
        assertEquals(Long.valueOf(-1L), result);
    }

    @Test
    public void testCreateValueWithNumberClassFloatString() throws Exception {
        // Number.class with a decimal string should return a Double or Float?
        Object result = TypeHandler.createValue("3.14", Number.class);
        assertNotNull(result);
        assertTrue(result instanceof Number);
        // Typically returns a Double
        assertEquals(Double.valueOf(3.14), result);
    }

    @Test
    public void testCreateValueWithBooleanClassTrueMixedCase() throws Exception {
        Object result = TypeHandler.createValue("tRuE", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateValueWithBooleanClassNullString() throws Exception {
        TypeHandler.createValue(null, Boolean.class);
    }

    // --- Test static methods via instance (if applicable) ---

    @Test
    public void testStaticMethodsViaInstance() throws Exception {
        // Ensure instance methods delegate to static ones
        assertEquals("test", handler.createValue("test", String.class));
    }

    // --- Test default constructor ---

    @Test
    public void testDefaultConstructor() {
        assertNotNull(new TypeHandler());
    }
}