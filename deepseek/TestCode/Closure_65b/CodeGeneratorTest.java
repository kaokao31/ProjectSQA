package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for CodeGenerator targeting Defects4J Closure-65.
 * Focuses on break/continue statements inside switch and loop constructs.
 */
public class CodeGeneratorTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Disable all optimizations to preserve original AST structure
        options.setChecksOnly(true);
        options.setWarningLevel(CheckLevel.OFF);
    }

    /**
     * Parses JavaScript source code and returns the root AST node.
     */
    private Node parse(String code) {
        SourceFile input = SourceFile.fromCode("test.js", code);
        compiler.compile(
                SourceFile.fromCode("externs.js", ""),
                input,
                options);
        // The root is the compilation unit; the script node is its first child
        return compiler.getRoot().getFirstChild();
    }

    /**
     * Tests that a break statement inside a switch that is inside a for loop
     * is correctly generated with a semicolon. This targets the known bug
     * where the semicolon is omitted in such nested contexts.
     */
    @Test
    public void testBreakInSwitchInsideForLoop() {
        String code = "for(var i=0;i<10;i++){switch(i){case 1: break;}}";
        Node root = parse(code);
        String generated = CodeGenerator.generateCode(root, true, false);
        assertTrue("Generated code should contain 'break;'", generated.contains("break;"));
    }

    /**
     * Tests a simple break inside a switch (no loop nesting).
     */
    @Test
    public void testBreakInSwitch() {
        String code = "switch(x){case 1: break;}";
        Node root = parse(code);
        String generated = CodeGenerator.generateCode(root, true, false);
        assertTrue("Generated code should contain 'break;'", generated.contains("break;"));
    }

    /**
     * Tests a break inside a for loop (no switch).
     */
    @Test
    public void testBreakInLoop() {
        String code = "for(;;){break;}";
        Node root = parse(code);
        String generated = CodeGenerator.generateCode(root, true, false);
        assertTrue("Generated code should contain 'break;'", generated.contains("break;"));
    }

    /**
     * Tests a continue statement inside a for loop.
     */
    @Test
    public void testContinueInLoop() {
        String code = "for(;;){continue;}";
        Node root = parse(code);
        String generated = CodeGenerator.generateCode(root, true, false);
        assertTrue("Generated code should contain 'continue;'", generated.contains("continue;"));
    }

    /**
     * Tests a continue inside a switch inside a for loop (edge case).
     */
    @Test
    public void testContinueInSwitchInsideForLoop() {
        String code = "for(var i=0;i<10;i++){switch(i){case 1: continue;}}";
        Node root = parse(code);
        String generated = CodeGenerator.generateCode(root, true, false);
        assertTrue("Generated code should contain 'continue;'", generated.contains("continue;"));
    }

    /**
     * Tests that a break statement without a semicolon in source is still
     * generated with a semicolon (normalization).
     */
    @Test
    public void testBreakWithoutSemicolon() {
        String code = "for(;;){break}";
        Node root = parse(code);
        String generated = CodeGenerator.generateCode(root, true, false);
        assertTrue("Generated code should contain 'break;'", generated.contains("break;"));
    }

    /**
     * Tests that a return statement inside a switch inside a loop is correct.
     */
    @Test
    public void testReturnInSwitchInsideLoop() {
        String code = "for(;;){switch(x){case 1: return;}}";
        Node root = parse(code);
        String generated = CodeGenerator.generateCode(root, true, false);
        assertTrue("Generated code should contain 'return;'", generated.contains("return;"));
    }

    /**
     * Tests that a throw statement inside a switch inside a loop is correct.
     */
    @Test
    public void testThrowInSwitchInsideLoop() {
        String code = "for(;;){switch(x){case 1: throw new Error();}}";
        Node root = parse(code);
        String generated = CodeGenerator.generateCode(root, true, false);
        assertTrue("Generated code should contain 'throw'", generated.contains("throw"));
    }
}