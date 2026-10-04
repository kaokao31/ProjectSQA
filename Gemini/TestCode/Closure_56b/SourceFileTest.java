package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

public class SourceFileTest {

    @Test
    public void testFromFile() {
        File file = new File("test.js");
        SourceFile sourceFile = SourceFile.fromFile(file);
        assertNotNull(sourceFile);
        assertEquals("test.js", sourceFile.getName());
    }

    @Test
    public void testFromFileWithCharset() {
        File file = new File("test.js");
        Charset charset = Charset.forName("UTF-8");
        SourceFile sourceFile = SourceFile.fromFile(file, charset);
        assertNotNull(sourceFile);
        assertEquals("test.js", sourceFile.getName());
    }

    @Test
    public void testFromFileString() {
        SourceFile sourceFile = SourceFile.fromFile("test.js");
        assertNotNull(sourceFile);
        assertEquals("test.js", sourceFile.getName());
    }

    @Test
    public void testFromFileStringWithCharset() {
        Charset charset = Charset.forName("UTF-8");
        SourceFile sourceFile = SourceFile.fromFile("test.js", charset);
        assertNotNull(sourceFile);
        assertEquals("test.js", sourceFile.getName());
    }

    @Test
    public void testFromCode() {
        SourceFile sourceFile = SourceFile.fromCode("fileName.js", "var x = 1;");
        assertNotNull(sourceFile);
        assertEquals("fileName.js", sourceFile.getName());
        try {
            assertEquals("var x = 1;", sourceFile.getCode());
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testFromInputStream() throws IOException {
        InputStream is = new ByteArrayInputStream("code".getBytes("UTF-8"));
        SourceFile sourceFile = SourceFile.fromInputStream("streamFile.js", is);
        assertNotNull(sourceFile);
        assertEquals("streamFile.js", sourceFile.getName());
        assertEquals("code", sourceFile.getCode());
    }

    @Test
    public void testFromInputStreamWithCharset() throws IOException {
        InputStream is = new ByteArrayInputStream("code".getBytes("UTF-8"));
        Charset charset = Charset.forName("UTF-8");
        SourceFile sourceFile = SourceFile.fromInputStream("streamFile.js", is, charset);
        assertNotNull(sourceFile);
        assertEquals("streamFile.js", sourceFile.getName());
        assertEquals("code", sourceFile.getCode());
    }

    @Test
    public void testFromReader() throws IOException {
        StringReader reader = new StringReader("reader code");
        SourceFile sourceFile = SourceFile.fromReader("readerFile.js", reader);
        assertNotNull(sourceFile);
        assertEquals("readerFile.js", sourceFile.getName());
        assertEquals("reader code", sourceFile.getCode());
    }

    @Test
    public void testLineOffsetComputationAndGetLine() {
        // Multi-line code to test line map, getLine, and getLineOffset
        String code = "line1\nline2\nline3";
        SourceFile sourceFile = SourceFile.fromCode("multiline.js", code);

        assertEquals("line1", sourceFile.getLine(1));
        assertEquals("line2", sourceFile.getLine(2));
        assertEquals("line3", sourceFile.getLine(3));
        assertNull(sourceFile.getLine(0));
        assertNull(sourceFile.getLine(4));

        // Test offset mapping (Bug 56 fix verification for line/column lookup)
        // line 1 starts at index 0
        // line 2 starts at index 6 ("line1\n" length)
        // line 3 starts at index 12 ("line1\nline2\n" length)
        assertEquals(0, sourceFile.getLineOffset(1));
        assertEquals(6, sourceFile.getLineOffset(2));
        assertEquals(12, sourceFile.getLineOffset(3));
        
        // Test locationful region
        Region region = sourceFile.getRegion(2);
        assertNotNull(region);
        assertEquals("line2", region.getSourceExcerpt());
        assertEquals(2, region.getBeginningLineNumber());
    }

    @Test
    public void testGetLocationOfLine() {
        String code = "a\nb\nc";
        SourceFile sourceFile = SourceFile.fromCode("loc.js", code);
        
        assertEquals(0, sourceFile.getLineOffset(1));
        assertEquals(2, sourceFile.getLineOffset(2));
        assertEquals(4, sourceFile.getLineOffset(3));
    }

    @Test
    public void testClearCachedFlags() {
        String code = "content";
        SourceFile sourceFile = SourceFile.fromCode("cache.js", code);
        sourceFile.clearCachedSource();
        try {
            assertEquals("content", sourceFile.getCode());
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testOriginalPath() {
        SourceFile sourceFile = SourceFile.fromCode("name", "original", "path/to/original");
        assertEquals("path/to/original", sourceFile.getOriginalPath());
    }
}