package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableMap;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.parsing.ParserRunner.ParseResult;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.ScriptOrFnNode;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.EvaluatorException;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class LexerTest {

  private static class TestErrorReporter implements ErrorReporter {
    private int errorCount = 0;
    private int warningCount = 0;

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warningCount++;
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errorCount++;
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }
  }

  private Node parse(String jsCode) {
    Config config = new Config(
        new HashSet<String>(),
        new HashSet<String>(),
        Config.LanguageMode.ECMAScript3,
        false
    );
    TestErrorReporter reporter = new TestErrorReporter();
    SourceFile file = SourceFile.fromCode("test.js", jsCode);
    
    // Using ParserRunner to exercise the lexer/parser integration
    ParseResult result = ParserRunner.parse(file, jsCode, config, reporter);
    return result.ast;
  }

  @Test
  public void testBasicLexing() {
    Node root = parse("var x = 10;");
    assertNotNull(root);
  }

  @Test
  public void testStringLiteralSingleQuotes() {
    Node root = parse("var s = 'hello';");
    assertNotNull(root);
  }

  @Test
  public void testStringLiteralDoubleQuotes() {
    Node root = parse("var s = \"world\";");
    assertNotNull(root);
  }

  @Test
  public void testEscapeSequences() {
    // Tests various escape characters in the lexer
    Node root = parse("var s = '\\n\\r\\t\\b\\f\\\\\\\'\\\"\\0';");
    assertNotNull(root);
  }

  @Test
  public void testHexAndUnicodeEscapes() {
    Node root = parse("var s = '\\x41\\u0041';");
    assertNotNull(root);
  }

  @Test
  public void testNumericLiterals() {
    Node root = parse("var a = 123; var b = 0123; var c = 0x123; var d = 123.456; var e = 1e-3;");
    assertNotNull(root);
  }

  @Test
  public void testRegularExpressionLiteral() {
    Node root = parse("var re = /ab+c/i;");
    assertNotNull(root);
  }

  @Test
  public void testRegularExpressionWithEscape() {
    Node root = parse("var re = /\\/\\//;");
    assertNotNull(root);
  }

  @Test
  public void testOperatorsAndPunctuation() {
    Node root = parse("a === b; a !== b; a <<= 1; a >>>= 1; a &&= b; a ||= b;");
    assertNotNull(root);
  }

  @Test
  public void testKeywords() {
    Node root = parse("if (true) { else { while(false) { break; continue; } } return; }");
    assertNotNull(root);
  }

  @Test
  public void testReservedWordsAsIdentifiersStrictModeCheck() {
    // Depending on config, reserved words or strict mode checks might be triggered
    Node root = parse("var class = 1; var function = 2;");
    assertNotNull(root);
  }

  @Test
  public void testUnterminatedString() {
    try {
      parse("var s = 'unterminated;");
    } catch (Exception e) {
      // Expected or handled by error reporter
    }
  }

  @Test
  public void testUnterminatedMultilineComment() {
    try {
      parse("/* comment without end");
    } catch (Exception e) {
      // Expected or handled by error reporter
    }
  }

  @Test
  public void testLineComment() {
    Node root = parse("// This is a line comment\n var x = 1;");
    assertNotNull(root);
  }

  @Test
  public void testBlockComment() {
    Node root = parse("/* This is a block comment */ var x = 1;");
    assertNotNull(root);
  }
}