package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for MinimizeExitPoints.
 * Targets high coverage and fault detection, especially for bug 126.
 */
public class MinimizeExitPointsTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT3);
        options.setIdeMode(false);
        // Ensure MinimizeExitPoints is enabled
        options.setCodingConvention(new DefaultCodingConvention());
        // Disable other passes to isolate MinimizeExitPoints
        options.setChecksOnly(false);
        options.setWarningLevel(CheckLevel.OFF);
    }

    /**
     * Helper: parse JavaScript and return the AST root (script node).
     */
    private Node parseScript(String js) {
        SourceFile input = SourceFile.fromCode("test.js", js);
        compiler.initCompilerOptionsIfTesting();
        Result result = compiler.compile(
                new SourceFile[] {},
                new SourceFile[] { input },
                options);
        assertTrue("Compilation failed: " + compiler.getErrors(), result.success);
        return compiler.getRoot().getLastChild(); // Script node
    }

    /**
     * Helper: run MinimizeExitPoints on the script node.
     */
    private void applyPass(Node scriptRoot) {
        MinimizeExitPoints pass = new MinimizeExitPoints(compiler);
        pass.process(null, scriptRoot);
    }

    // ---- Basic function with single return (no transform needed) ----
    @Test
    public void testSimpleReturn() {
        String js = "function f() { return 1; }";
        Node script = parseScript(js);
        Node func = script.getFirstChild();
        Node body = func.getLastChild(); // BLOCK
        assertNotNull(body);
        applyPass(script);
        // The function body should still contain a single return
        assertTrue(body.getLastChild().isReturn());
    }

    // ---- Try-finally with return (Bug 126 scenario) ----
    @Test
    public void testTryFinallyWithReturn() {
        String js = "function f() { try { return 1; } finally { x = 2; } }";
        Node script = parseScript(js);
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        applyPass(script);
        // After pass: the finally code should be duplicated before the return
        // We check that the body block has more than one statement
        // (the return and the finally code, possibly restructured)
        assertTrue(body.getChildCount() >= 2);
        // The first stmt should be the finally block assignment
        Node firstStmt = body.getFirstChild();
        // It should be an EXPR_RESULT or similar for "x = 2"
        assertNotNull(firstStmt);
        // The last statement should be the return
        assertTrue(body.getLastChild().isReturn());
    }

    // ---- Try-catch-finally with throw ----
    @Test
    public void testTryCatchFinallyWithThrow() {
        String js = "function f() { try { throw new Error(); } catch(e) { return e; } finally { cleanup(); } }";
        Node script = parseScript(js);
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        applyPass(script);
        // Should still have a catch and finally handling
        // The finally should be duplicated into catch and throw paths
        assertTrue(body.getChildCount() >= 1);
        // Check there is no longer a TRY node directly? Actually the pass might transform
        // We just check no exception during pass
    }

    // ---- Try-finally without any abrupt exit ----
    @Test
    public void testTryFinallyNoExit() {
        String js = "function f() { try { x = 1; } finally { y = 2; } }";
        Node script = parseScript(js);
        applyPass(script);
        // The try-finally should remain structurally similar
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        // After pass, there should still be a try-finally or equivalent
        // (The pass only minimizes exit points, so if no exit, it may keep try)
        assertTrue(body.hasChildren());
    }

    // ---- Nested try-finally with returns ----
    @Test
    public void testNestedTryFinally() {
        String js = "function f() { try { try { return 1; } finally { inner(); } } finally { outer(); } }";
        Node script = parseScript(js);
        applyPass(script);
        // Should not crash, and the code should be transformed
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        // At least 2 statements: outer finally duplicated, etc.
        assertTrue(body.getChildCount() >= 2);
    }

    // ---- Multiple returns in try ----
    @Test
    public void testMultipleReturnsInTry() {
        String js = "function f(a) { try { if (a) return 1; else return 2; } finally { cleanup(); } }";
        Node script = parseScript(js);
        applyPass(script);
        // Should handle both returns
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        // Should have at least 3 statements (two assignments of finally? maybe more)
        assertTrue(body.getChildCount() >= 2);
    }

    // ---- Throw in try without catch ----
    @Test
    public void testThrowInTryWithoutCatch() {
        String js = "function f() { try { throw new Error(); } finally { cleanup(); } }";
        Node script = parseScript(js);
        applyPass(script);
        // Should duplicate finally before throw
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        // The body should have at least 1 statement (the finally code before throw)
        assertTrue(body.getChildCount() >= 1);
    }

    // ---- Empty finally block ----
    @Test
    public void testEmptyFinally() {
        String js = "function f() { try { return 1; } finally { } }";
        Node script = parseScript(js);
        applyPass(script);
        // Should not add unnecessary statements
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        // Should still be a single return (since finally empty)
        assertEquals(1, body.getChildCount());
        assertTrue(body.getLastChild().isReturn());
    }

    // ---- Try-catch with return in both ----
    @Test
    public void testTryCatchReturnBoth() {
        String js = "function f() { try { return 1; } catch(e) { return 2; } }";
        Node script = parseScript(js);
        applyPass(script);
        // Should not remove anything, but may transform if catch has its own exit
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        // The try-catch should still be present
        assertTrue(body.hasChildren());
    }

    // ---- Try-finally with break inside loop ----
    @Test
    public void testTryFinallyWithBreak() {
        String js = "function f() { while(true) { try { break; } finally { x = 1; } } }";
        Node script = parseScript(js);
        applyPass(script);
        // Should duplicate finally before break
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        // Loop body should have at least the finally code
        assertTrue(body.hasChildren());
    }

    // ---- Try-finally with continue inside loop ----
    @Test
    public void testTryFinallyWithContinue() {
        String js = "function f() { for(;;) { try { continue; } finally { x = 1; } } }";
        Node script = parseScript(js);
        applyPass(script);
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        assertTrue(body.hasChildren());
    }

    // ---- No abrupt exit in try ----
    @Test
    public void testNoAbruptExit() {
        String js = "function f() { try { ordinary(); } finally { cleanup(); } }";
        Node script = parseScript(js);
        applyPass(script);
        // Should keep the try-finally as is
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        // The body should still contain a TRY node
        Node first = body.getFirstChild();
        assertTrue(first.isTry());
    }

    // ---- Null script body (edge case) ----
    @Test
    public void testNullBody() {
        // Empty script
        String js = "";
        Node script = parseScript(js);
        // Script should have no children
        assertFalse(script.hasChildren());
        applyPass(script);
        // Should not crash
        assertFalse(script.hasChildren());
    }

    // ---- Very large number of statements in try ----
    @Test
    public void testLargeTryBlock() {
        StringBuilder sb = new StringBuilder();
        sb.append("function f() { try { ");
        for (int i = 0; i < 100; i++) {
            sb.append("x = ").append(i).append("; ");
        }
        sb.append("return 1; } finally { cleanup(); } }");
        String js = sb.toString();
        Node script = parseScript(js);
        applyPass(script);
        // Should not crash and should transform
        Node func = script.getFirstChild();
        Node body = func.getLastChild();
        assertTrue(body.getChildCount() >= 100); // The 100 assignments may be duplicated? Actually the pass duplicates only finally code, not all
        // Better to just check it runs
    }
}