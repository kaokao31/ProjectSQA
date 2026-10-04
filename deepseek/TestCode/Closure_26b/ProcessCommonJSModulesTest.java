package com.google.javascript.jscomp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ProcessCommonJSModulesTest {

  private String process(String filename, String code) {
    return process(Arrays.asList(SourceFile.fromCode(filename, code)), "", false);
  }

  private String process(List<SourceFile> inputs, String prefix, boolean reportDependencies) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = Arrays.asList(SourceFile.fromCode("externs.js", ""));

    compiler.init(externs, inputs, options);
    Node root = compiler.parseInputs();
    assertNotNull("Unable to parse test inputs", root);

    ProcessCommonJSModules pass =
        new ProcessCommonJSModules(compiler, prefix, reportDependencies);
    pass.process(compiler.getExternsRoot(), root);

    return compiler.toSource();
  }

  @Test
  public void testSimpleModuleIsProvidedAndHasNamespaceObject() {
    String out = process("foo.js", "var x = 1;");

    assertTrue(out, out.contains("goog.provide"));
    assertTrue(out, out.contains("module$foo"));
  }

  @Test
  public void testEmptyModuleIsStillProcessed() {
    String out = process("empty.js", "");

    assertTrue(out, out.contains("module$empty"));
  }

  @Test
  public void testDashInFileNameIsNormalizedToUnderscore() {
    String out = process("foo-bar.js", "var x = 1;");

    assertTrue(out, out.contains("module$foo_bar"));
    assertFalse(out, out.contains("module$foo-bar"));
  }

  @Test
  public void testDotInFileNameIsNormalizedToUnderscore() {
    String out = process("foo.bar.js", "var x = 1;");

    assertTrue(out, out.contains("module$foo_bar"));
    assertFalse(out, out.contains("module$foo.bar"));
  }

  @Test
  public void testJsExtensionIsStrippedBeforeNormalization() {
    String out = process("foo.js", "var x = 1;");

    assertTrue(out, out.contains("module$foo"));
    assertFalse("Module name should not keep the JS extension", out.contains("module$foo.js"));
    assertFalse("Normalization must not turn foo.js into foo_", out.contains("module$foo_"));

    String dottedOut = process("foo.bar.js", "var x = 1;");

    assertTrue(dottedOut, dottedOut.contains("module$foo_bar"));
    assertFalse(
        "Module name should not keep the JS extension",
        dottedOut.contains("module$foo.bar.js"));
    assertFalse(
        "Normalization must not turn foo.bar.js into foo_bar_",
        dottedOut.contains("module$foo_bar_"));
  }

  @Test
  public void testExportsPropertyIsRewrittenToModuleObject() {
    String out = process("foo.js", "exports.answer = 42;");

    assertTrue(out, out.contains("module$foo"));
    assertTrue(out, out.contains("answer"));
    assertFalse("exports should be rewritten to the generated module object", out.contains("exports.answer"));
  }

  @Test
  public void testModuleExportsIsRewrittenToModuleObject() {
    String out = process("foo.js", "module.exports = function() {};");

    assertTrue(out, out.contains("module$foo"));
    assertFalse("module should be rewritten to the generated module object", out.contains("module.exports"));
  }

  @Test
  public void testRequireIsRewrittenToGoogRequireAndModuleReference() {
    String out = process("foo.js", "var x = require('bar');");

    assertTrue(out, out.contains("goog.require"));
    assertTrue(out, out.contains("module$bar"));
    assertFalse("require call should be rewritten", out.contains("require('bar')"));
  }

  @Test
  public void testMultipleRequiresAreProcessed() {
    String out = process("foo.js", "var a = require('a'); var b = require('b');");

    assertTrue(out, out.contains("module$a"));
    assertTrue(out, out.contains("module$b"));
  }

  @Test
  public void testMultipleScriptsAreProcessedIndependently() {
    String out =
        process(
            Arrays.asList(
                SourceFile.fromCode("a.js", "var a = 1;"),
                SourceFile.fromCode("b.js", "var b = 2;")),
            "",
            false);

    assertTrue(out, out.contains("module$a"));
    assertTrue(out, out.contains("module$b"));
  }

  @Test
  public void testReportDependenciesConstructorDoesNotCrash() {
    String out = process("foo.js", "var x = require('bar'); var y = 1;", "", true);

    assertNotNull(out);
    assertTrue(out, out.contains("module$foo"));
    assertTrue(out, out.contains("module$bar"));
  }

  @Test
  public void testNullSourceFileNameDoesNotCrash() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = Arrays.asList(SourceFile.fromCode("externs.js", ""));
    List<SourceFile> inputs = Arrays.asList(SourceFile.fromCode("foo.js", "var x = 1;"));

    compiler.init(externs, inputs, options);
    Node root = compiler.parseInputs();
    assertNotNull("Unable to parse test inputs", root);

    Node script = root.getFirstChild();
    assertNotNull("Expected a parsed script node", script);
    script.setSourceFileName(null);

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "", false);
    pass.process(compiler.getExternsRoot(), root);

    assertNotNull(root);
  }
}