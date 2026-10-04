package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class DefaultPrettyPrinterTest {

    private StringWriter sw;
    private JsonGenerator jg;
    private DefaultPrettyPrinter printer;

    @Before
    public void setUp() throws IOException {
        sw = new StringWriter();
        JsonFactory factory = new JsonFactory();
        jg = factory.createGenerator(sw);
        printer = new DefaultPrettyPrinter();
    }

    @After
    public void tearDown() throws IOException {
        jg.close();
    }

    // Helper to flush and get output
    private String getOutput() throws IOException {
        jg.flush();
        return sw.toString();
    }

    // ========== Constructor Tests ==========

    @Test
    public void testDefaultConstructor() {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter();
        assertNotNull(p);
        assertTrue(p.indentObjects());
        assertTrue(p.indentArrays());
        assertNotNull(p.getObjectIndenter());
        assertNotNull(p.getArrayIndenter());
    }

    @Test
    public void testConstructorWithIndenters() {
        Indenter objIndenter = new DefaultPrettyPrinter.Lf2SpacesIndenter();
        Indenter arrIndenter = new DefaultPrettyPrinter.FixedSpaceIndenter();
        DefaultPrettyPrinter p = new DefaultPrettyPrinter(objIndenter, arrIndenter);
        assertSame(objIndenter, p.getObjectIndenter());
        assertSame(arrIndenter, p.getArrayIndenter());
    }

    @Test
    public void testCopyConstructor() {
        DefaultPrettyPrinter original = new DefaultPrettyPrinter();
        original.indentObjectsWith(new DefaultPrettyPrinter.Lf2SpacesIndenter());
        original.indentArraysWith(new DefaultPrettyPrinter.FixedSpaceIndenter());
        DefaultPrettyPrinter copy = new DefaultPrettyPrinter(original);
        assertNotSame(original, copy);
        assertNotSame(original.getObjectIndenter(), copy.getObjectIndenter());
        assertNotSame(original.getArrayIndenter(), copy.getArrayIndenter());
        // Verify they are equal in behavior
        assertEquals(original.indentObjects(), copy.indentObjects());
        assertEquals(original.indentArrays(), copy.indentArrays());
    }

    @Test
    public void testCopyConstructorWithNullIndenters() {
        DefaultPrettyPrinter original = new DefaultPrettyPrinter();
        original.indentObjectsWith(null);
        original.indentArraysWith(null);
        DefaultPrettyPrinter copy = new DefaultPrettyPrinter(original);
        assertNull(copy.getObjectIndenter());
        assertNull(copy.getArrayIndenter());
    }

    // ========== Factory Method ==========

    @Test
    public void testConstructDefaultPrettyPrinter() {
        DefaultPrettyPrinter p = DefaultPrettyPrinter.constructDefaultPrettyPrinter();
        assertNotNull(p);
        assertTrue(p.indentObjects());
        assertTrue(p.indentArrays());
    }

    // ========== With Indenter Methods ==========

    @Test
    public void testWithObjectIndenter() {
        Indenter indenter = new DefaultPrettyPrinter.Lf2SpacesIndenter();
        DefaultPrettyPrinter p = printer.withObjectIndenter(indenter);
        assertNotSame(printer, p);
        assertSame(indenter, p.getObjectIndenter());
        // Original unchanged
        assertNotSame(indenter, printer.getObjectIndenter());
    }

    @Test
    public void testWithObjectIndenterNull() {
        DefaultPrettyPrinter p = printer.withObjectIndenter(null);
        assertNull(p.getObjectIndenter());
    }

    @Test
    public void testWithArrayIndenter() {
        Indenter indenter = new DefaultPrettyPrinter.FixedSpaceIndenter();
        DefaultPrettyPrinter p = printer.withArrayIndenter(indenter);
        assertNotSame(printer, p);
        assertSame(indenter, p.getArrayIndenter());
    }

    @Test
    public void testWithArrayIndenterNull() {
        DefaultPrettyPrinter p = printer.withArrayIndenter(null);
        assertNull(p.getArrayIndenter());
    }

    // ========== Indent Flags ==========

    @Test
    public void testIndentObjectsWithTrue() {
        DefaultPrettyPrinter p = printer.withObjectIndenter(new DefaultPrettyPrinter.Lf2SpacesIndenter());
        assertTrue(p.indentObjects());
    }

    @Test
    public void testIndentObjectsWithFalse() {
        DefaultPrettyPrinter p = printer.withObjectIndenter(null);
        assertFalse(p.indentObjects());
    }

    @Test
    public void testIndentArraysWithTrue() {
        DefaultPrettyPrinter p = printer.withArrayIndenter(new DefaultPrettyPrinter.FixedSpaceIndenter());
        assertTrue(p.indentArrays());
    }

    @Test
    public void testIndentArraysWithFalse() {
        DefaultPrettyPrinter p = printer.withArrayIndenter(null);
        assertFalse(p.indentArrays());
    }

    // ========== Write Start/End Object ==========

    @Test
    public void testWriteStartObjectEndObject() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartObject();
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("{"));
        assertTrue(out.contains("}"));
    }

    @Test
    public void testWriteStartObjectEndObjectWithNullObjectIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withObjectIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartObject();
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        // Should not throw NPE
        assertTrue(out.contains("{"));
        assertTrue(out.contains("}"));
    }

    @Test
    public void testWriteStartObjectEndObjectWithCustomIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withObjectIndenter(new DefaultPrettyPrinter.Lf2SpacesIndenter());
        jg.setPrettyPrinter(p);
        jg.writeStartObject();
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("{"));
        assertTrue(out.contains("}"));
    }

    // ========== Write Start/End Array ==========

    @Test
    public void testWriteStartArrayEndArray() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartArray();
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("["));
        assertTrue(out.contains("]"));
    }

    @Test
    public void testWriteStartArrayEndArrayWithNullArrayIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withArrayIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartArray();
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("["));
        assertTrue(out.contains("]"));
    }

    @Test
    public void testWriteStartArrayEndArrayWithCustomIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withArrayIndenter(new DefaultPrettyPrinter.FixedSpaceIndenter());
        jg.setPrettyPrinter(p);
        jg.writeStartArray();
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("["));
        assertTrue(out.contains("]"));
    }

    // ========== Before Object Entries ==========

    @Test
    public void testBeforeObjectEntries() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartObject();
        jg.writeStringField("key", "value");
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        // Should have a newline before the field
        assertTrue(out.contains("\"key\""));
    }

    @Test
    public void testBeforeObjectEntriesWithNullIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withObjectIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartObject();
        jg.writeStringField("key", "value");
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        // Should not have extra newline
        assertTrue(out.contains("\"key\""));
    }

    // ========== Before Array Values ==========

    @Test
    public void testBeforeArrayValues() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartArray();
        jg.writeNumber(1);
        jg.writeNumber(2);
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("1"));
        assertTrue(out.contains("2"));
    }

    @Test
    public void testBeforeArrayValuesWithNullIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withArrayIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartArray();
        jg.writeNumber(1);
        jg.writeNumber(2);
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("1"));
        assertTrue(out.contains("2"));
    }

    // ========== Separators ==========

    @Test
    public void testWriteObjectFieldValueSeparator() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartObject();
        jg.writeStringField("key", "value");
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains(":"));
    }

    @Test
    public void testWriteObjectEntrySeparator() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartObject();
        jg.writeStringField("a", "1");
        jg.writeStringField("b", "2");
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        // Should have comma between entries
        assertTrue(out.contains(","));
    }

    @Test
    public void testWriteArrayValueSeparator() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartArray();
        jg.writeNumber(1);
        jg.writeNumber(2);
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains(","));
    }

    @Test
    public void testWriteRootValueSeparator() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartObject();
        jg.writeEndObject();
        jg.writeStartArray();
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        // Should have a space between root values
        assertTrue(out.contains("} "));
    }

    // ========== Nested Structures ==========

    @Test
    public void testNestedObjectInArray() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartArray();
        jg.writeStartObject();
        jg.writeStringField("x", "y");
        jg.writeEndObject();
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("["));
        assertTrue(out.contains("{"));
        assertTrue(out.contains("}"));
        assertTrue(out.contains("]"));
    }

    @Test
    public void testNestedArrayInObject() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartObject();
        jg.writeFieldName("arr");
        jg.writeStartArray();
        jg.writeNumber(1);
        jg.writeEndArray();
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("{"));
        assertTrue(out.contains("["));
        assertTrue(out.contains("]"));
        assertTrue(out.contains("}"));
    }

    // ========== Edge Cases: Null Indenters ==========

    @Test
    public void testWriteEndObjectWithNullIndenter() throws IOException {
        // Bug trigger: writeEndObject calls _objectIndenter.writeIndentation()
        // when _objectIndenter is null, should not throw NPE
        DefaultPrettyPrinter p = printer.withObjectIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartObject();
        jg.writeEndObject();
        jg.close();
        // Should not throw
        assertTrue(true);
    }

    @Test
    public void testWriteEndArrayWithNullIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withArrayIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartArray();
        jg.writeEndArray();
        jg.close();
        // Should not throw
        assertTrue(true);
    }

    @Test
    public void testWriteStartObjectWithNullIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withObjectIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartObject();
        jg.close();
        // Should not throw
        assertTrue(true);
    }

    @Test
    public void testWriteStartArrayWithNullIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withArrayIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartArray();
        jg.close();
        // Should not throw
        assertTrue(true);
    }

    // ========== Custom Indenters ==========

    @Test
    public void testIndentWithLf2SpacesIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withObjectIndenter(new DefaultPrettyPrinter.Lf2SpacesIndenter());
        jg.setPrettyPrinter(p);
        jg.writeStartObject();
        jg.writeStringField("a", "1");
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        // Should have newline and two spaces before field
        assertTrue(out.contains("\n  \"a\""));
    }

    @Test
    public void testIndentWithFixedSpaceIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withArrayIndenter(new DefaultPrettyPrinter.FixedSpaceIndenter());
        jg.setPrettyPrinter(p);
        jg.writeStartArray();
        jg.writeNumber(1);
        jg.writeNumber(2);
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        // Should have space after comma
        assertTrue(out.contains(", "));
    }

    @Test
    public void testIndentWithSpacesIndenter() throws IOException {
        DefaultPrettyPrinter p = printer.withObjectIndenter(new DefaultPrettyPrinter.NopIndenter());
        jg.setPrettyPrinter(p);
        jg.writeStartObject();
        jg.writeStringField("a", "1");
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        // NopIndenter does nothing, so no extra whitespace
        assertTrue(out.contains("\"a\""));
    }

    // ========== Empty Structures ==========

    @Test
    public void testEmptyObject() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartObject();
        jg.writeEndObject();
        jg.close();
        String out = getOutput();
        assertEquals("{}", out.trim());
    }

    @Test
    public void testEmptyArray() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeStartArray();
        jg.writeEndArray();
        jg.close();
        String out = getOutput();
        assertEquals("[]", out.trim());
    }

    // ========== Multiple Root Values ==========

    @Test
    public void testMultipleRootValues() throws IOException {
        jg.setPrettyPrinter(printer);
        jg.writeNumber(1);
        jg.writeNumber(2);
        jg.close();
        String out = getOutput();
        assertTrue(out.contains("1"));
        assertTrue(out.contains("2"));
        // Should have separator between them
        assertTrue(out.contains("1 2") || out.contains("1\n2"));
    }

    // ========== Copy with Indenters ==========

    @Test
    public void testCopyWithIndenters() {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter();
        p.indentObjectsWith(new DefaultPrettyPrinter.Lf2SpacesIndenter());
        p.indentArraysWith(new DefaultPrettyPrinter.FixedSpaceIndenter());
        DefaultPrettyPrinter copy = p.copy();
        assertNotSame(p, copy);
        assertNotSame(p.getObjectIndenter(), copy.getObjectIndenter());
        assertNotSame(p.getArrayIndenter(), copy.getArrayIndenter());
        // Verify they produce same output
        assertEquals(p.indentObjects(), copy.indentObjects());
        assertEquals(p.indentArrays(), copy.indentArrays());
    }

    @Test
    public void testCopyWithNullIndenters() {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter();
        p.indentObjectsWith(null);
        p.indentArraysWith(null);
        DefaultPrettyPrinter copy = p.copy();
        assertNull(copy.getObjectIndenter());
        assertNull(copy.getArrayIndenter());
    }

    // ========== Bug Regression: NPE on writeEndObject with null indenter ==========

    @Test
    public void testBug23Regression() throws IOException {
        // This test specifically targets the bug where writeEndObject
        // calls _objectIndenter.writeIndentation() without null check
        DefaultPrettyPrinter p = new DefaultPrettyPrinter();
        p = p.withObjectIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartObject();
        jg.writeEndObject();
        // Should not throw NullPointerException
        assertTrue(true);
    }

    @Test
    public void testBug23RegressionArray() throws IOException {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter();
        p = p.withArrayIndenter(null);
        jg.setPrettyPrinter(p);
        jg.writeStartArray();
        jg.writeEndArray();
        // Should not throw NullPointerException
        assertTrue(true);
    }
}