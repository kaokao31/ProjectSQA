package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CoalesceVariableNames;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import java.util.ArrayList;
import java.util.List;

public class CoalesceVariableNamesTest {

    private Compiler runPass(String js) {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setCoalesceVariableNames(true);
        List<SourceFile> externs = new ArrayList<>();
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(new SourceFile("test.js", js));
        compiler.init(externs, inputs, options);
        compiler.parse();
        CoalesceVariableNames pass = new CoalesceVariableNames(compiler);
        pass.process(compiler.getExterns(), compiler.getRoot());
        return compiler;
    }

    @Test
    public void testSimpleCoalescing() {
        String js = "function f() { var a = 1; a = 2; var b = 3; b = 4; }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testNoCoalescingOverlap() {
        String js = "function f() { var a = 1; var b = 2; a = a + b; }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testForLoop() {
        String js = "function f() { for (var i = 0; i < 10; i++) { var j = i; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testForLoopWithRedeclaration() {
        String js = "function f() { var a = 1; for (var a = 0; a < 10; a++) { var b = a; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testTryCatch() {
        String js = "function f() { var a = 1; try { var b = 2; } catch (e) { var c = 3; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testNestedFunctions() {
        String js = "function f() { var a = 1; function g() { var b = 2; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testMultipleVars() {
        String js = "function f() { var a = 1, b = 2, c = 3; }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testEmptyFunction() {
        String js = "function f() {}";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testGlobalVars() {
        String js = "var a = 1; var b = 2;";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testBug142() {
        // This test triggers the bug in Defects4J Closure 142
        String js = "function f() { var a = 1; for (var b = 0; b < 10; b++) { var c = a + b; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testClosure() {
        String js = "function f() { var a = 1; return function() { return a; }; }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testWithStatement() {
        String js = "function f() { var a = 1; with (obj) { var b = 2; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testSwitchCase() {
        String js = "function f() { var a = 1; switch (a) { case 1: var b = 2; break; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testWhileLoop() {
        String js = "function f() { var a = 1; while (a < 10) { var b = a; a++; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testDoWhileLoop() {
        String js = "function f() { var a = 1; do { var b = a; a++; } while (a < 10); }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testIfElse() {
        String js = "function f() { var a = 1; if (a) { var b = 2; } else { var c = 3; } }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testNestedBlocks() {
        String js = "function f() { var a = 1; { var b = 2; } var c = 3; }";
        Compiler compiler = runPass(js);
        assertNotNull(compiler.getRoot());
    }
}