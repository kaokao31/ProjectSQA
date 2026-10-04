package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

public class NumericEntityUnescaperTest {

    private final NumericEntityUnescaper unescaper = new NumericEntityUnescaper();

    // Expected behavior: normal numeric entity
    @Test
    public void testNormalDecimalEntity() throws IOException {
        String input = "&#65;";
        String expected = "A";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
        assertEquals("Decimal entity should unescape to 'A'", expected, writer.toString());
    }

    @Test
    public void testNormalHexEntity() throws IOException {
        String input = "&#x41;";
        String expected = "A";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
        assertEquals("Hex entity should unescape to 'A'", expected, writer.toString());
    }

    @Test
    public void testZeroLengthEntity() throws IOException {
        String input = "&#;";
        String expected = "&#;";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
        assertEquals("Entity without digits should be unchanged", expected, writer.toString());
    }

    @Test
    public void testNoEntity() throws IOException {
        String input = "Hello World";
        String expected = "Hello World";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
        assertEquals("Input without entities should pass through", expected, writer.toString());
    }

    @Test
    public void testEmptyString() throws IOException {
        String input = "";
        String expected = "";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
        assertEquals("Empty input should produce empty output", expected, writer.toString());
    }

    @Test
    public void testMultipleEntities() throws IOException {
        String input = "&#65;&#x42;";
        String expected = "AB";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
        assertEquals("Multiple entities should unescape correctly", expected, writer.toString());
    }

    // -----------------------------------------------------------------------
    // Bug-reproducing tests (Defects4J #19)
    // These tests are expected to throw StringIndexOutOfBoundsException
    // due to missing bounds checks in the buggy implementation.
    // -----------------------------------------------------------------------

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testUnfinishedEntity() throws IOException {
        // Input ends with "&#" – no digits, no semicolon
        String input = "&#";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testOutOfBounds() throws IOException {
        // Input ends with "&#x" – hex marker but no digits, no semicolon
        String input = "&#x";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
    }

    // Additional edge cases that might trigger the bug

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testUnfinishedHexEntityAtEnd() throws IOException {
        // Input ends with incomplete hex entity
        String input = "&#x1";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testUnfinishedDecimalEntityAtEnd() throws IOException {
        // Input ends with incomplete decimal entity
        String input = "&#12";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
    }

    // Test that a proper entity after an unfinished one still works? (coverage)
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testUnfinishedEntityThenNormal() throws IOException {
        // Proper entity is never reached because the first unfinished entity crashes
        String input = "&#&#65;";
        StringWriter writer = new StringWriter();
        unescaper.translate(input, 0, writer);
    }

}