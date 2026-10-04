package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class SourceFileTest {

    private static final String MULTILINE_SOURCE = "line1\nline2\nline3";
    private static final String EMPTY_SOURCE = "";
    private static final String SINGLE_LINE = "only line";
    private static final String WITH_TRAILING_NEWLINE = "abc\ndef\n";
    private static final String NULL_SOURCE = null;

    private SourceFile sourceFile;

    @Before
    public void setUp() throws Exception {
        // Use a valid filename and source for initial setup
        sourceFile = SourceFile.fromCode("test.js", MULTILINE_SOURCE);
    }

    @Test
    public void testGetCodeWithMultiLine() {
        assertEquals("getCode should return the original source", 
                     MULTILINE_SOURCE, sourceFile.getCode());
    }

    @Test
    public void testGetNumLinesMultiLine() {
        assertEquals(3, sourceFile.getNumLines());
    }

    @Test
    public void testGetLineFirstLine() {
        assertEquals("line1", sourceFile.getLine(1));
    }

    @Test
    public void testGetLineLastLine() {
        assertEquals("line3", sourceFile.getLine(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLineTooLow() {
        sourceFile.getLine(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLineTooHigh() {
        sourceFile.getLine(4);
    }

    @Test
    public void testGetLineOffsetFirstLine() {
        assertEquals(0, sourceFile.getLineOffset(1));
    }

    @Test
    public void testGetLineOffsetSecondLine() {
        // "line1\n" has length 6
        assertEquals(6, sourceFile.getLineOffset(2));
    }

    @Test
    public void testGetLineOffsetThirdLine() {
        // "line1\nline2\n" has length 12
        assertEquals(12, sourceFile.getLineOffset(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLineOffsetZero() {
        sourceFile.getLineOffset(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLineOffsetBeyondNumLines() {
        sourceFile.getLineOffset(4);
    }

    @Test
    public void testClearCachedSource() {
        // Ensure getCode works after clear
        sourceFile.clearCachedSource();
        assertEquals(MULTILINE_SOURCE, sourceFile.getCode());
    }

    @Test
    public void testFromCodeWithEmptySource() {
        SourceFile empty = SourceFile.fromCode("empty.js", EMPTY_SOURCE);
        assertEquals(EMPTY_SOURCE, empty.getCode());
        assertEquals(0, empty.getNumLines());
    }

    @Test(expected = NullPointerException.class)
    public void testFromCodeWithNullSource() {
        SourceFile.fromCode("null.js", NULL_SOURCE).getCode();
    }

    @Test
    public void testFromCodeSingleLine() {
        SourceFile single = SourceFile.fromCode("single.js", SINGLE_LINE);
        assertEquals(1, single.getNumLines());
        assertEquals(SINGLE_LINE, single.getLine(1));
        assertEquals(0, single.getLineOffset(1));
    }

    @Test
    public void testGetLineWithTrailingNewline() {
        // The string ends with a newline, so the second line is empty
        SourceFile trailing = SourceFile.fromCode("trail.js", WITH_TRAILING_NEWLINE);
        assertEquals(2, trailing.getNumLines());
        assertEquals("abc", trailing.getLine(1));
        assertEquals("def", trailing.getLine(2));
        // Offsets: "abc\n" length=4, so second line starts at 4
        assertEquals(4, trailing.getLineOffset(2));
    }

    @Test
    public void testGetLineOffsetTrailingNewlineLastLine() {
        SourceFile trailing = SourceFile.fromCode("trail.js", WITH_TRAILING_NEWLINE);
        // The last line starts after "abc\ndef\n" which is 8 characters? Actually "abc\ndef\n" is 8 bytes: a b c \n d e f \n -> that's 8? Let's compute: "abc" = 3 + '\n' = 4, "def" = 3 + '\n' = 4, total 8.
        assertEquals(4, trailing.getLineOffset(2));
    }

    @Test
    public void testGetLineFromCodeWithNullFileName() {
        // Should not throw NullPointerException if the file name is null? This depends on implementation
        SourceFile noName = SourceFile.fromCode(null, "source");
        assertNotNull(noName);
        assertEquals("source", noName.getCode());
    }

    @Test
    public void testGetLineWithCarriageReturn() {
        // Additional edge case: Windows-style line endings
        String crlfSource = "a\r\nb\r\nc";
        SourceFile crlf = SourceFile.fromCode("crlf.js", crlfSource);
        // Depending on implementation, it may treat \r\n as a single line break or two
        // We test that numLines is at least correct for common behavior
        // This test is for fault detection: if implementation incorrectly counts lines
        int numLines = crlf.getNumLines();
        assertTrue("Number of lines should be 3 for CRLF separated text", numLines == 3);
        assertEquals("a", crlf.getLine(1));
        assertEquals("b", crlf.getLine(2));
        assertEquals("c", crlf.getLine(3));
    }

    @Test
    public void testGetLineOffsetWithCRLF() {
        String crlfSource = "a\r\nb";
        SourceFile crlf = SourceFile.fromCode("test.js", crlfSource);
        // If \r\n is treated as one character? Actually offset after first line includes the two characters
        // This expects offset 3 (a, \r, \n) if that's how the implementation indexes
        // For fault detection, we check that the offset is consistent with the line content length
        assertEquals(3, crlf.getLineOffset(2));
    }

    @Test
    public void testGetLineWithOnlyNewlines() {
        String onlyNewlines = "\n\n\n";
        SourceFile nl = SourceFile.fromCode("nl.js", onlyNewlines);
        assertEquals(4, nl.getNumLines()); // 4 lines: empty, empty, empty, empty? Actually 3 newlines produce 4 lines.
        assertEquals("", nl.getLine(1));
        assertEquals("", nl.getLine(2));
        assertEquals("", nl.getLine(3));
        assertEquals("", nl.getLine(4));
    }
}