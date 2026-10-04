package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for CodeGenerator, targeting Defects4J Closure bug 145.
 * Focuses on for-in loops and label/break/continue generation.
 */
public class CodeGeneratorTest {

    private Compiler compiler;
    private CodeGenerator codeGenerator;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
        compiler.initOptions(options);
        codeGenerator = new CodeGenerator(compiler);
    }

    // Helper: parse a JavaScript snippet and return the AST root
    private Node parse(String js) {
        SourceFile source = SourceFile.fromCode("test", js);
        Node root = compiler.parse(source);
        assertNotNull("Parsing failed", root);
        return root;
    }

    // Helper: generate code for a given node using the CodeGenerator
    private String generateCode(Node node) {
        StringBuilder sb = new StringBuilder();
        codeGenerator.add(node, sb, CodeGenerator.Context.STATEMENT);
        return sb.toString();
    }

    // ==================== Bug 145 focused tests ====================

    /**
     * Bug 145: CodeGenerator incorrectly handles 'var' in for-in loops.
     * Test: for (var x in obj) should generate 'var x' in output.
     */
    @Test
    public void testForInWithVar() {
        Node script = parse("for (var x in obj) {}");
        Node forNode = script.getFirstChild(); // FOR node
        assertNotNull(forNode);
        String code = generateCode(forNode);
        assertTrue("Generated code must contain 'var x'", code.contains("var x"));
    }

    /**
     * Test: for (x in obj) without var should not add var.
     */
    @Test
    public void testForInWithoutVar() {
        Node script = parse("for (x in obj) {}");
        Node forNode = script.getFirstChild();
        String code = generateCode(forNode);
        assertFalse("Generated code should not contain 'var'", code.contains("var"));
        assertTrue(code.contains("x in obj"));
    }

    /**
     * Test: for-in with let (ES6) should generate 'let x' (if supported).
     * (Assumes language mode allows let)
     */
    @Test
    public void testForInWithLet() {
        CompilerOptions opts = new CompilerOptions();
        opts.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT6);
        compiler.initOptions(opts);
        Node script = parse("for (let x in obj) {}");
        Node forNode = script.getFirstChild();
        String code = generateCode(forNode);
        assertTrue("Generated code must contain 'let x'", code.contains("let x"));
    }

    /**
     * Test: break with label inside a for loop.
     * Tests that the label is correctly emitted.
     */
    @Test
    public void testBreakWithLabel() {
        Node script = parse("outer: for(;;) { break outer; }");
        Node forNode = script.getFirstChild(); // LABEL node wrapping FOR
        assertEquals("First child should be LABEL", Token.LABEL, forNode.getToken());
        String code = generateCode(forNode);
        assertTrue("Generated code should contain 'break outer'", code.contains("break outer"));
        assertTrue("Generated code should contain the label 'outer:'", code.contains("outer:"));
    }

    /**
     * Test: continue with label inside a while loop.
     */
    @Test
    public void testContinueWithLabel() {
        Node script = parse("label1: while (true) { continue label1; }");
        Node labelNode = script.getFirstChild();
        assertEquals("Should be LABEL", Token.LABEL, labelNode.getToken());
        String code = generateCode(labelNode);
        assertTrue("Generated code should contain 'continue label1'", code.contains("continue label1"));
        assertTrue("Generated code should contain label 'label1:'", code.contains("label1:"));
    }

    /**
     * Test: multiple nested loops with labels to ensure correct scoping.
     */
    @Test
    public void testNestedLabels() {
        String js = "a: for(;;) { b: while(true) { break a; } }";
        Node script = parse(js);
        Node outerLabel = script.getFirstChild();
        String code = generateCode(outerLabel);
        assertTrue("Should contain 'break a'", code.contains("break a"));
        assertTrue("Should contain label 'a:'", code.contains("a:"));
        assertTrue("Should contain label 'b:'", code.contains("b:"));
    }

    /**
     * Test: label on a block (not loop) with break.
     */
    @Test
    public void testLabelBlockBreak() {
        Node script = parse("a: { break a; }");
        Node labelNode = script.getFirstChild();
        String code = generateCode(labelNode);
        assertTrue("Should contain 'break a'", code.contains("break a"));
        assertTrue("Should contain label 'a:'", code.contains("a:"));
    }

    // ==================== Edge cases ====================

    @Test
    public void testEmptyForIn() {
        Node script = parse("for (var x in obj) {}");
        Node forNode = script.getFirstChild();
        String code = generateCode(forNode);
        assertTrue(code.contains("{}") || code.contains("{ }"));
    }

    @Test
    public void testForInWithEmptyBody() {
        Node script = parse("for (var x in obj) ;");
        Node forNode = script.getFirstChild();
        String code = generateCode(forNode);
        assertTrue(code.contains(";") || code.contains("{}"));
    }

    @Test
    public void testUnaryExpressionInForIn() {
        // for-in with !isNaN should not affect generation of var
        Node script = parse("for (var x in obj) { !isNaN(x); }");
        Node forNode = script.getFirstChild();
        String code = generateCode(forNode);
        assertTrue("Should contain 'var x'", code.contains("var x"));
        assertTrue("Should contain '!isNaN'", code.contains("!isNaN"));
    }

    @Test
    public void testForInWithStringKeyword() {
        // Ensure 'in' operator is not confused with for-in for non-variable cases
        Node script = parse("var a = 'x' in obj;");
        Node varNode = script.getFirstChild();
        String code = generateCode(varNode);
        assertTrue("Should contain 'x' in obj", code.contains("'x' in obj"));
        assertFalse("Should not contain 'var x'", code.contains("var x"));
    }

    // ==================== Additional coverage ====================

    @Test
    public void testStringEscaping() {
        // Test that string literals are properly escaped
        Node script = parse("var s = \"hello\\nworld\";");
        Node varNode = script.getFirstChild();
        String code = generateCode(varNode);
        assertTrue("String should include escape sequence", code.contains("\\n"));
    }

    @Test
    public void testObjectLiteralWithGetter() {
        // ES5 getter syntax
        Node script = parse("var obj = { get x() { return 1; } };");
        Node varNode = script.getFirstChild();
        String code = generateCode(varNode);
        assertTrue("Should contain 'get x'", code.contains("get x"));
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullNode() {
        codeGenerator.add(null, new StringBuilder(), CodeGenerator.Context.STATEMENT);
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullAppendable() {
        Node node = new Node(Token.SCRIPT);
        codeGenerator.add(node, null, CodeGenerator.Context.STATEMENT);
    }
}