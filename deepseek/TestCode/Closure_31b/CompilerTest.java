package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
  }

  private Result compile(String code) {
    return compiler.compile(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(SourceFile.fromCode("test.js", code)),
        options);
  }

  @Test
  public void testCompileNoErrors() {
    Result result = compile("var x = 1; var y = 2;");
    assertTrue(result.success);
    assertEquals(0, compiler.getErrors().length);
    assertEquals(0, compiler.getWarnings().length);
    assertNotNull(compiler.getRoot());
    assertNotNull(compiler.getJsRoot());
    assertNotNull(compiler.getExternsRoot());
  }

  @Test
  public void testCompileSyntaxError() {
    Result result = compile("var x = ;");
    assertFalse(result.success);
    assertTrue(compiler.getErrors().length > 0);
  }

  @Test
  public void testCompileErrorLineNumber() {
    Result result = compile("var a = 1;\nvar b = ;");
    assertFalse(result.success);
    assertTrue(compiler.getErrors().length > 0);
    JSError error = compiler.getErrors()[0];
    assertEquals(2, error.getLineNumber());
  }

  @Test
  public void testCompileEmptyInput() {
    Result result = compile("");
    assertTrue(result.success);
    assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testCompileResetsErrors() {
    compile("var x = ;");
    assertTrue(compiler.getErrors().length > 0);

    Result result = compile("var y = 1;");
    assertTrue(result.success);
    assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testCompileWithExterns() {
    List<SourceFile> externs = Collections.singletonList(
        SourceFile.fromCode("externs.js", "var External = {};"));
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("input.js", "External.foo();"));
    Result result = compiler.compile(externs, inputs, options);
    assertTrue(result.success);
    assertEquals(0, compiler.getErrors().length);
  }

  @Test
  public void testParse() {
    Node root = compiler.parse(
        SourceFile.fromCode("test.js", "var a = 1;"));
    assertNotNull(root);
    assertNotNull(root.getFirstChild());
  }

  @Test
  public void testParseEmptySourceFile() {
    Node root = compiler.parse(SourceFile.fromCode("empty.js", ""));
    assertNotNull(root);
  }

  @Test
  public void testParseSyntheticCode() {
    Node root = compiler.parseSyntheticCode("var synthetic = 1;");
    assertNotNull(root);
  }

  @Test
  public void testParseMultipleFiles() {
    SourceFile[] files = new SourceFile[] {
        SourceFile.fromCode("a.js", "var a;"),
        SourceFile.fromCode("b.js", "var b;")
    };
    Node root = compiler.parse(files);
    assertNotNull(root);
  }

  @Test
  public void testGetSourceFileByName() {
    compile("var a = 1;");
    assertNotNull(compiler.getSourceFileByName("test.js"));
  }

  @Test
  public void testGetSourceFileByNameForUnknownFile() {
    assertNull(compiler.getSourceFileByName("unknown.js"));
  }

  @Test
  public void testToSource() {
    compile("var a = 1;");
    assertTrue(compiler.toSource().contains("var a"));
  }
}