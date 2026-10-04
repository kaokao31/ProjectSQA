package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.LanguageMode;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;

import java.util.ArrayList;

import org.junit.Test;

public class InlineVariablesTest {

  private void testInline(String code, String expected) {
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    Compiler compiler = new Compiler();
    compiler.init(new ArrayList<SourceFile>(), new ArrayList<SourceFile>(), options);
    Node externs = compiler.parseSyntheticCode("");
    Node root = compiler.parseSyntheticCode(code);
    if (compiler.hasErrors()) {
      fail("Unexpected parse error: " + compiler.getErrors()[0].toString());
    }

    // Assumes constructor: InlineVariables(AbstractCompiler, boolean inlineLocals, boolean inlineConsts)
    InlineVariables pass = new InlineVariables(compiler, true, true);
    pass.process(externs, root);

    String result = compiler.toSource();
    assertEquals(expected, result);
  }

  @Test
  public void testInlineSimpleVar() {
    testInline("var x = 1; alert(x);", "alert(1);");
  }

  @Test
  public void testInlineVarWithInitializer() {
    testInline("var x = 2 + 3; alert(x);", "alert(2+3);");
  }

  @Test
  public void testNoInlineVarReassigned() {
    testInline("var x = 1; x = 2; alert(x);", "var x=1;x=2;alert(x);");
  }

  @Test
  public void testInlineVarInFunctionScope() {
    testInline("function f() { var y = 2; return y; }", "function f() { return 2; }");
  }

  @Test
  public void testNoInlineNonConstant() {
    testInline("var x = Math.random(); alert(x);", "var x=Math.random();alert(x);");
  }

  @Test
  public void testInlineVarDefinedAndUsedOnce() {
    testInline("var x = 42; alert(x);", "alert(42);");
  }

  @Test
  public void testInlineVarWithNoReference() {
    testInline("var x = 1;", "");
  }

  @Test
  public void testInlineVarUsedMultipleTimes() {
    testInline("var x = 1; alert(x); alert(x);", "alert(1);alert(1);");
  }

  @Test
  public void testNoInlineVarWithSideEffectInInit() {
    testInline("var x = foo(); alert(x);", "var x=foo();alert(x);");
  }

  @Test
  public void testInlineVarInLoop() {
    testInline("for (var i = 0; i < 10; i++) { var y = i; alert(y); }",
        "for(var i=0;i<10;i++){alert(i);}");
  }
}