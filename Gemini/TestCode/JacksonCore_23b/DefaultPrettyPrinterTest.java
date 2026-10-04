package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class DefaultPrettyPrinterTest {

    @Test
    public void testDefaultConstructorAndConstants() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertNotNull(pp);
        assertNotNull(pp._arrayIndenter);
        assertNotNull(pp._objectIndenter);
        assertNotNull(pp._rootSeparator);
    }

    @Test
    public void testStringConstructor() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("CUSTOM_ROOT");
        assertEquals("CUSTOM_ROOT", pp._rootSeparator.getValue());
    }

    @Test
    public void testCopyConstructor() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("ROOT");
        DefaultPrettyPrinter copy = new DefaultPrettyPrinter(pp);
        assertNotNull(copy);
        assertEquals(pp._rootSeparator.getValue(), copy._rootSeparator.getValue());
    }

    @Test
    public void testCreateInstance() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter newInstance = pp.createInstance();
        assertNotNull(newInstance);
        assertNotSame(pp, newInstance);
        assertEquals(pp.getClass(), newInstance.getClass());
    }

    @Test
    public void testIndenterConfiguration() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        
        pp.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        pp.indentObjectsWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        
        assertSame(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE, pp.arrayIndenter());
        assertSame(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE, pp.objectIndenter());
    }

    @Test
    public void testNopIndenterConfiguration() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        
        pp.indentArraysWith(DefaultIndenter.NopIndenter.instance);
        pp.indentObjectsWith(DefaultIndenter.NopIndenter.instance);
        
        assertSame(DefaultIndenter.NopIndenter.instance, pp.arrayIndenter());
        assertSame(DefaultIndenter.NopIndenter.instance, pp.objectIndenter());
    }

    @Test
    public void testSpacesInObjectEntriesConfiguration() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertTrue(pp._spacesInObjectEntries);
        
        pp.withoutSpacesInObjectEntries();
        assertFalse(pp._spacesInObjectEntries);
        
        pp.withSpacesInObjectEntries();
        assertTrue(pp._spacesInObjectEntries);
    }

    @Test
    public void testRootSeparatorConfiguration() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        
        pp.withRootSeparator((String) null);
        assertNull(pp._rootSeparator);

        pp.withRootSeparator("NEW_ROOT");
        assertEquals("NEW_ROOT", pp._rootSeparator.getValue());

        pp.withRootSeparator(new SerializedString("SER_ROOT"));
        assertEquals("SER_ROOT", pp._rootSeparator.getValue());
    }

    @Test
    public void testWriteRootValueSeparator() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(" ");
        pp.writeRootValueSeparator(gen);
        gen.flush();
        assertEquals(" ", out.toString());
        gen.close();
    }

    @Test
    public void testWriteRootValueSeparatorNull() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter((SerializedString) null);
        // Should not throw NPE when root separator is null
        pp.writeRootValueSeparator(gen);
        gen.close();
    }

    @Test
    public void testWriteStartObject() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.writeStartObject(gen);
        gen.flush();
        assertEquals("{", out.toString());
        gen.close();
    }

    @Test
    public void testWriteEndObject() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.writeEndObject(gen, 2);
        gen.flush();
        assertTrue(out.toString().contains("}"));
        gen.close();
    }

    @Test
    public void testWriteObjectEntrySeparator() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.writeObjectEntrySeparator(gen);
        gen.flush();
        assertEquals(",", out.toString());
        gen.close();
    }

    @Test
    public void testWriteObjectFieldValueSeparator() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.writeObjectFieldValueSeparator(gen);
        gen.flush();
        assertEquals(" : ", out.toString());

        // Test without spaces in object entries
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        JsonGenerator gen2 = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out2, false),
                0, null, new java.io.OutputStreamWriter(out2, "UTF-8")
        );
        pp.withoutSpacesInObjectEntries();
        pp.writeObjectFieldValueSeparator(gen2);
        gen2.flush();
        assertEquals(":", out2.toString());
        
        gen.close();
        gen2.close();
    }

    @Test
    public void testWriteStartArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.writeStartArray(gen);
        gen.flush();
        assertEquals("[", out.toString());
        gen.close();
    }

    @Test
    public void testWriteEndArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.writeEndArray(gen, 1);
        gen.flush();
        assertTrue(out.toString().contains("]"));
        gen.close();
    }

    @Test
    public void testWriteArrayValueSeparator() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.writeArrayValueSeparator(gen);
        gen.flush();
        assertEquals(",", out.toString());
        gen.close();
    }

    @Test
    public void testBeforeArrayValues() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.beforeArrayValues(gen);
        gen.close();
    }

    @Test
    public void testBeforeObjectEntries() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.beforeObjectEntries(gen);
        gen.close();
    }

    @Test
    public void testNopIndenterBehavior() throws IOException {
        DefaultIndenter.NopIndenter nop = DefaultIndenter.NopIndenter.instance;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = new com.fasterxml.jackson.core.json.WriterBasedJsonGenerator(
                new com.fasterxml.jackson.core.io.IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), out, false),
                0, null, new java.io.OutputStreamWriter(out, "UTF-8")
        );
        nop.writeIndentation(gen, 0);
        assertFalse(nop.isInline());
        gen.close();
    }
}