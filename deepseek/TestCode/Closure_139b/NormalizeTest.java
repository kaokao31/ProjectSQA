package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import java.util.Arrays;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

public class NormalizeTest {

  private Compiler compiler;
  private Node externs;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    externs = compiler.parseTestCode("");
    assertNotNull("Empty externs should parse", externs);
    assertTrue("Unexpected setup errors: " + formatErrors(compiler),
        compiler.getErrors().length == 0);
  }

  private String normalize(String js) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode(js);
    assertNotNull("Failed to parse input: " + js, root);
    assertTrue("Parse errors for input: " + js + " -> " + formatErrors(compiler),
        compiler.getErrors().length == 0);

    new Normalize(compiler, false).process(externs, root);

    assertTrue("Normalize introduced errors for input: " + js + " -> " + formatErrors(compiler),
        compiler.getErrors().length == 0);
    return new CodeGenerator(compiler).generate(root);
  }

  private String formatErrors(Compiler compiler) {
    return Arrays.toString(compiler.getErrors());
  }

  private int countOccurrences(String source, String target) {
    int count = 0;
    int index = 0;
    while ((index = source.indexOf(target, index)) != -1) {
      count++;
      index += target.length();
    }
    return count;
  }

  @Test
  public void testEmptyScript() {
    assertEquals("", normalize(""));
  }

  @Test
  public void testSimpleProgramsDoNotThrow() {
    String[] sources = {
        "var x = 1;",
        "function f() { return 1; }",
        "var f = function() { return 1; };",
        "var f = function g() { return 1; };",
        "var o = {a: 1, 'b': 2};",
        "var o = {foo: 1};",
        "var o = {default: 1};",
        "var o = {get x() { return 1; }, set x(v) { this.x = v; }};",
        "for (var i = 0; i < 10; i++) { foo(i); }",
        "for (var p in obj) { foo(p); }",
        "try { foo(); } catch (e) { bar(e); } finally { baz(); }",
        "switch (x) { case 1: break; default: break; }",
        "label: for (;;) { break label; }",
        "if (true) { function f() {} }",
    };

    for (String source : sources) {
      assertNotNull("Should normalize without crashing: " + source, normalize(source));
    }
  }

  @Test
  public void testDuplicateVarDeclarationsAreMerged() {
    String result = normalize("var x = 1; var x = 2;");
    assertEquals("Duplicate var declarations should be merged: " + result,
        1, countOccurrences(result, "var x"));
    assertTrue("Second initializer should become an assignment: " + result,
        result.contains("x = 2"));
  }

  @Test
  public void testDuplicateVarWithoutInitializerIsRemoved() {
    String result = normalize("var x; var x;");
    assertEquals("Duplicate empty var declarations should be removed: " + result,
        1, countOccurrences(result, "var x"));
  }

  @Test
  public void testReservedWordObjectKeyIsQuoted() {
    String result = normalize("var o = {default: 1};");
    assertTrue("Expected default key to be quoted: " + result,
        result.contains("'default'") || result.contains("\"default\""));
  }

  @Test
  public void testValidObjectKeyIsNotQuoted() {
    String result = normalize("var o = {foo: 1};");
    assertFalse("Expected foo key to remain unquoted: " + result,
        result.contains("'foo'") || result.contains("\"foo\""));
  }

  @Test
  public void testGetterSetterDoesNotCrash() {
    String source = "var o = {get x() { return 1; }, set x(v) { this.x = v; }};";
    assertNotNull(normalize(source));
  }

  @Test
  public void testForInLoopDoesNotCrash() {
    assertNotNull(normalize("for (var p in obj) { foo(p); }"));
  }

  @Test
  public void testTryCatchFinallyDoesNotCrash() {
    assertNotNull(normalize("try { foo(); } catch (e) { bar(e); } finally { baz(); }"));
  }

  @Test
  public void testSwitchAndLabelDoNotCrash() {
    assertNotNull(normalize("switch (x) { case 1: break; default: break; }"));
    assertNotNull(normalize("label: for (;;) { break label; }"));
  }

  @Test
  public void testNormalizationIsIdempotent() {
    String[] sources = {
        "var x = 1;",
        "function f() { return 1; }",
        "var f = function g() { return 1; };",
        "var o = {a: 1, 'b': 2};",
        "var o = {foo: 1};",
        "var o = {default: 1};",
        "var o = {get x() { return 1; }, set x(v) { this.x = v; }};",
        "for (var i = 0; i < 10; i++) { foo(i); }",
        "for (var p in obj) { foo(p); }",
        "try { foo(); } catch (e) { bar(e); } finally { baz(); }",
        "switch (x) { case 1: break; default: break; }",
        "label: for (;;) { break label; }",
        "if (true) { function f() {} }",
        "var x = 1; var x = 2;",
        "var x; var x;"
    };

    for (String source : sources) {
      String once = normalize(source);
      String twice = normalize(once);
      assertEquals("Normalization is not idempotent for source: " + source
          + "\nonce=" + once
          + "\ntwice=" + twice,
          once, twice);
    }
  }

  @Test
  public void testNormalizedCodeIsValidJavaScript() {
    String[] sources = {
        "var x = 1; var x = 2;",
        "var o = {default: 1};",
        "if (true) { function f() {} }",
        "var o = {get x() { return 1; }, set x(v) { this.x = v; }};"
    };

    for (String source : sources) {
      String normalized = normalize(source);
      Compiler compiler = new Compiler();
      compiler.initOptions(new CompilerOptions());
      Node reparsed = compiler.parseTestCode(normalized);
      assertNotNull("Normalized output failed to parse for source: " + source
          + "\nnormalized=" + normalized, reparsed);
      assertTrue("Normalized output had parse errors: " + normalized
          + " -> " + formatErrors(compiler),
          compiler.getErrors().length == 0);
    }
  }

  @Test
  public void testExternsWithObjectLiteralAreNormalized() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node externs = compiler.parseTestCode("var externsObj = {foo: 1};");
    Node root = compiler.parseTestCode("var x = {bar: 2};");
    assertNotNull(externs);
    assertNotNull(root);
    assertTrue("Unexpected parse errors: " + formatErrors(compiler),
        compiler.getErrors().length == 0);

    new Normalize(compiler, false).process(externs, root);

    assertTrue("Normalize errored on externs: " + formatErrors(compiler),
        compiler.getErrors().length == 0);
  }

  @Test
  public void testExternsWithReservedWordPropertyAreNormalized() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node externs = compiler.parseTestCode("var externsObj = {default: 1};");
    Node root = compiler.parseTestCode("var x = {default: 2};");
    assertNotNull(externs);
    assertNotNull(root);
    assertTrue("Unexpected parse errors: " + formatErrors(compiler),
        compiler.getErrors().length == 0);

    new Normalize(compiler, false).process(externs, root);

    assertTrue("Normalize errored on reserved-word extern property: " + formatErrors(compiler),
        compiler.getErrors().length == 0);
  }
}