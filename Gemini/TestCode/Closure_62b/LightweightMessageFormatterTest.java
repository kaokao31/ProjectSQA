package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for LightweightMessageFormatter in Closure Compiler (Bug 62 context).
 * Designed for maximum coverage and edge cases.
 */
public class LightweightMessageFormatterTest {

  @Test
  public void testNullSourceExcerpt() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("test.js", 10, 5, DiagnosticType.warning("JSC_TEST_WARNING", "Test warning"));
    
    String formatted = formatter.formatError(error);
    assertNotNull(formatted);
    assertTrue(formatted.contains("test.js:10"));
    assertTrue(formatted.contains("Test warning"));
  }

  @Test
  public void testFormatErrorWithSource() {
    SourceExcerptProvider source = new SourceExcerptProvider() {
      @Override
      public StringgetSource(String sourceName, int lineNumber) {
        if (lineNumber == 5) {
          return "var x = 10;";
        }
        return null;
      }

      @Override
      public Region excerptRegion(String sourceName, int lineNumber, SourceExcerpt excerptType) {
        return null;
      }
    };

    LightweightMessageFormatter formatter = new LightweightMessageFormatter(source);
    JSError error = JSError.make("test.js", 5, 8, DiagnosticType.warning("JSC_TEST_WARNING", "Test warning"));
    
    String formatted = formatter.formatError(error);
    assertNotNull(formatted);
    assertTrue(formatted.contains("test.js:5"));
    assertTrue(formatted.contains("var x = 10;"));
    assertTrue(formatted.contains("^"));
  }

  @Test
  public void testFormatWarningWithSource() {
    SourceExcerptProvider source = new SourceExcerptProvider() {
      @Override
      public String getSource(String sourceName, int lineNumber) {
        if (lineNumber == 1) {
          return "foo();";
        }
        return null;
      }

      @Override
      public Region excerptRegion(String sourceName, int lineNumber, SourceExcerpt excerptType) {
        return null;
      }
    };

    LightweightMessageFormatter formatter = new LightweightMessageFormatter(source);
    JSError warning = JSError.make("test.js", 1, 1, CheckLevel.WARNING, DiagnosticType.warning("JSC_TEST_WARNING", "Warning message"));
    
    String formatted = formatter.formatError(warning);
    assertNotNull(formatted);
    assertTrue(formatted.contains("WARNING"));
    assertTrue(formatted.contains("foo();"));
  }

  @Test
  public void testFormatWithNoSourceAndZeroLineNumber() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("test.js", 0, 0, DiagnosticType.warning("JSC_TEST_WARNING", "Zero line"));
    
    String formatted = formatter.formatError(error);
    assertNotNull(formatted);
    assertTrue(formatted.contains("test.js"));
  }

  @Test
  public void testPointToColumnEdgeCases() {
    SourceExcerptProvider source = new SourceExcerptProvider() {
      @Override
      public String getSource(String sourceName, int lineNumber) {
        return "  ab";
      }

      @Override
      public Region excerptRegion(String sourceName, int lineNumber, SourceExcerpt excerptType) {
        return null;
      }
    };

    LightweightMessageFormatter formatter = new LightweightMessageFormatter(source);
    
    // Column 0 or negative
    JSError error1 = JSError.make("test.js", 1, 0, DiagnosticType.warning("JSC_TEST_WARNING", "Col 0"));
    assertNotNull(formatter.formatError(error1));

    // Column way beyond line length
    JSError error2 = JSError.make("test.js", 1, 100, DiagnosticType.warning("JSC_TEST_WARNING", "Col 100"));
    assertNotNull(formatter.formatError(error2));
  }
}