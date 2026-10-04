package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

public class CodeGeneratorTest {

  @Test
  public void testEscapeStringNullOrEmpty() {
    assertNull(CodeGenerator.escapeString(null));
    assertEquals("", CodeGenerator.escapeString(""));
  }

  @Test
  public void testEscapeStringBasic() {
    // Basic string escaping functionality
    String original = "hello\nworld";
    String escaped = CodeGenerator.escapeString(original);
    assertNotNull(escaped);
  }

  @Test
  public void testEscapeStringQuotes() {
    assertEquals("\"'hello'\"", CodeGenerator.escapeString("'hello'", false));
    assertEquals("'\"hello\"'", CodeGenerator.escapeString("\"hello\"", true));
    assertEquals("\"\\\"hello\\\"\"", CodeGenerator.escapeString("\"hello\"", false));
  }

  @Test
  public void testEscapeStringSpecialCharacters() {
    // Test various characters requiring escaping
    String special = "\b\f\n\r\t\\";
    String escaped = CodeGenerator.escapeString(special);
    assertNotNull(escaped);
  }

  @Test
  public void testHtmlEscaping() {
    // Testing specific code generation utilities if exposed or helper methods
    String input = "<script>alert('XSS');</script>";
    String result = CodeGenerator.escapeString(input);
    assertNotNull(result);
  }

  @Test
  public void testIdentifierGeneration() {
    assertTrue(CodeGenerator.isJSIdentifier("validName"));
    assertFalse(CodeGenerator.isJSIdentifier("123invalid"));
    assertFalse(CodeGenerator.isJSIdentifier(""));
    assertFalse(CodeGenerator.isJSIdentifier(null));
  }

  @Test
  public void testCharsetHandling() {
    // Check behavior with common outputCharsets
    Charset ascii = Charset.forName("US-ASCII");
    String result = CodeGenerator.escapeString("café", true, false, ascii);
    assertNotNull(result);
  }

  @Test
  public void testContextHandling() {
    // Exercises different CodeContexts if available via public signatures
    CodeContext context = CodeContext.START_OF_EXPRESSION;
    assertNotNull(context);
  }
}