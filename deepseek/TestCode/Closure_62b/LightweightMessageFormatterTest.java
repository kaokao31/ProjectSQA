package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LightweightMessageFormatterTest {

  private LightweightMessageFormatter formatter;
  private TestSourceExcerptProvider provider;

  private static final DiagnosticType ERROR_TYPE =
      DiagnosticType.error("TEST_ERROR", "Test error message");
  private static final DiagnosticType WARNING_TYPE =
      DiagnosticType.warning("TEST_WARNING", "Test warning message");

  @Before
  public void setUp() {
    provider = new TestSourceExcerptProvider("test.js", 1, "var x = 1;");
    formatter = new LightweightMessageFormatter(provider);
  }

  // ---------------------------------------------------------------------
  // Test formatSourceLine
  // ---------------------------------------------------------------------

  @Test
  public void testFormatSourceLine_basic() {
    String result = formatter.formatSourceLine("test.js", 1);
    assertNotNull("Should not be null", result);
    assertEquals("Incorrect line", "var x = 1;", result);
  }

  @Test
  public void testFormatSourceLine_missingLine() {
    String result = formatter.formatSourceLine("test.js", 2);
    assertNotNull("Should return null or empty", result);
  }

  @Test
  public void testFormatSourceLine_lineNumberZero() {
    String result = formatter.formatSourceLine("test.js", 0);
    assertNull("Should return null for line 0", result);
  }

  // ---------------------------------------------------------------------
  // Test formatSourcePoint
  // ---------------------------------------------------------------------

  @Test
  public void testFormatSourcePoint_basic() {
    String result = formatter.formatSourcePoint("test.js", 1, 4);
    assertNotNull("Should not be null", result);
    String[] lines = result.split("\n");
    assertEquals("Should have two lines", 2, lines.length);
    assertEquals("var x = 1;", lines[0]);
    assertEquals("    ^", lines[1]);
  }

  @Test
  public void testFormatSourcePoint_atColumnZero() {
    String result = formatter.formatSourcePoint("test.js", 1, 0);
    String[] lines = result.split("\n");
    assertEquals("var x = 1;", lines[0]);
    assertEquals("^", lines[1]);
  }

  @Test
  public void testFormatSourcePoint_columnBeyondLineLength() {
    String result = formatter.formatSourcePoint("test.js", 1, 20);
    assertNotNull("Should not throw", result);
    String[] lines = result.split("\n");
    assertEquals("var x = 1;", lines[0]);
    String caretLine = lines[1];
    assertTrue("Caret line should contain caret", caretLine.contains("^"));
    assertTrue("Caret index should be >= line length", caretLine.indexOf('^') >= 10);
  }

  @Test
  public void testFormatSourcePoint_negativeColumn() {
    String result = formatter.formatSourcePoint("test.js", 1, -1);
    assertNotNull("Should not throw", result);
    String[] lines = result.split("\n");
    assertEquals("var x = 1;", lines[0]);
    // Should not crash; caret position unspecified, but typically it appears at column 0.
  }

  @Test
  public void testFormatSourcePoint_emptyLine() {
    TestSourceExcerptProvider emptyProvider =
        new TestSourceExcerptProvider("test.js", 1, "");
    LightweightMessageFormatter formatterEmpty =
        new LightweightMessageFormatter(emptyProvider);
    String result = formatterEmpty.formatSourcePoint("test.js", 1, 0);
    assertNotNull("Should not be null", result);
    String[] lines = result.split("\n");
    assertEquals("Empty line should exist", "", lines[0]);
    assertTrue("Should have caret line", lines.length > 1);
  }

  @Test
  public void testFormatSourcePoint_missingSource() {
    String result = formatter.formatSourcePoint("missing.js", 1, 4);
    assertNull("Should return null if source line is missing", result);
  }

  // ---------------------------------------------------------------------
  // Test formatError / formatWarning
  // ---------------------------------------------------------------------

  @Test
  public void testFormatError_basic() {
    JSError error = JSError.make("test.js", 1, 4, ERROR_TYPE);
    String result = formatter.formatError(error);
    assertNotNull(result);
    assertTrue("Should contain error message", result.contains("Test error message"));
    assertTrue("Should contain source line", result.contains("var x = 1;"));
    assertTrue("Should have caret line", hasCaret(result.split("\n")));
    assertCaretPosition(result, 4);
  }

  @Test
  public void testFormatWarning_basic() {
    JSError error = JSError.make("test.js", 1, 4, WARNING_TYPE);
    String result = formatter.formatWarning(error);
    assertNotNull(result);
    assertTrue("Should contain warning message", result.contains("Test warning message"));
    assertTrue("Should contain source line", result.contains("var x = 1;"));
    assertTrue("Should have caret line", hasCaret(result.split("\n")));
    assertCaretPosition(result, 4);
  }

  @Test
  public void testFormatError_noSourceLine() {
    TestSourceExcerptProvider nullProvider =
        new TestSourceExcerptProvider("nonexistent.js", 1, null);
    LightweightMessageFormatter formatter2 = new LightweightMessageFormatter(nullProvider);
    JSError error = JSError.make("test.js", 1, 4, ERROR_TYPE);
    String result = formatter2.formatError(error);
    assertNotNull(result);
    assertFalse(result.contains("var x = 1;"));
  }

  @Test
  public void testFormatError_lineNumberZero() {
    JSError error = JSError.make("test.js", 0, -1, ERROR_TYPE);
    String result = formatter.formatError(error);
    assertNotNull(result);
    assertTrue(result.contains("Test error message"));
    assertFalse("Should not contain source line", result.contains("var x = 1;"));
  }

  @Test
  public void testFormatError_nullSourceName() {
    JSError error = JSError.make(null, 1, 4, ERROR_TYPE);
    String result = formatter.formatError(error);
    assertNotNull(result);
    assertTrue(result.contains("Test error message"));
    assertFalse("Should not contain source line", result.contains("var x = 1;"));
  }

  @Test
  public void testFormatError_columnBeyondLine() {
    JSError error = JSError.make("test.js", 1, 20, ERROR_TYPE);
    String result = formatter.formatError(error);
    assertNotNull("Should not throw", result);
    assertTrue("Should still contain source line", result.contains("var x = 1;"));
    assertTrue("Should have caret line", hasCaret(result.split("\n")));
  }

  // ---------------------------------------------------------------------
  // Helper methods
  // ---------------------------------------------------------------------

  private boolean hasCaret(String[] lines) {
    for (String line : lines) {
      if (line.contains("^")) {
        return true;
      }
    }
    return false;
  }

  private void assertCaretPosition(String result, int column) {
    String[] lines = result.split("\n");
    for (String line : lines) {
      if (line.contains("^")) {
        int caretIndex = line.indexOf('^');
        assertEquals("Incorrect caret column", column, caretIndex);
        return;
      }
    }
    fail("No caret found in result");
  }

  // ---------------------------------------------------------------------
  // Inner stub for SourceExcerptProvider
  // ---------------------------------------------------------------------

  private static class TestSourceExcerptProvider implements SourceExcerptProvider {
    private final String sourceName;
    private final int lineNumber;
    private final String line;

    TestSourceExcerptProvider(String sourceName, int lineNumber, String line) {
      this.sourceName = sourceName;
      this.lineNumber = lineNumber;
      this.line = line;
    }

    @Override
    public String getSourceLine(String sourceName, int lineNumber) {
      if (sourceName.equals(this.sourceName) && lineNumber == this.lineNumber) {
        return line;
      }
      return null;
    }

    @Override
    public String getSourceLines(String sourceName, int lineNumber, int maxLines) {
      // Not used in tests
      return null;
    }

    @Override
    public String getSourceName(String sourceName) {
      return sourceName;
    }
  }
}