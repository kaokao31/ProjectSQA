package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for PeepholeSubstituteAlternateSyntax.
 * Targets the bug in Closure-87 where typeof comparisons are incorrectly folded
 * for undeclared variables, leading to ReferenceError.
 */
public class PeepholeSubstituteAlternateSyntaxTest {

    private Compiler compiler;
    private PeepholeSubstituteAlternateSyntax pass;

    @Before
    public void setUp() {
        compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        pass = new PeepholeSubstituteAlternateSyntax();
    }

    /**
     * Helper: parse JavaScript source code and return the AST root node.
     */
    private Node parse(String js) {
        return com.google.javascript.jscomp.Compiler.parseSyntheticCode(js);
    }

    /**
     * Helper: get the first statement from the AST.
     */
    private Node getFirstStatement(Node root) {
        Node script = root.getLastChild();
        return script.getFirstChild();
    }

    @Test
    public void testTypeofDeclaredVariable() {
        // typeof a === 'undefined' where a is declared (var a)
        Node ast = parse("var a; typeof a === 'undefined'");
        Node expr = getFirstStatement(ast);  // should be EXPR_RESULT
        Node original = expr.getFirstChild(); // the binop
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        // After optimization, typeof a === 'undefined' should be replaced with a === undefined
        assertNotNull("Optimized node should not be null", optimized);
        assertTrue("Expected binary operator (EQ)", optimized.isToken(Token.EQ));
        Node left = optimized.getFirstChild();
        Node right = optimized.getSecondChild();
        assertTrue("Left should be NAME 'a'", left.isToken(Token.NAME) && left.getString().equals("a"));
        assertTrue("Right should be NAME 'undefined'", right.isToken(Token.NAME) && right.getString().equals("undefined"));
    }

    @Test
    public void testTypeofUndeclaredVariable() {
        // typeof x === 'undefined' where x is not declared
        Node ast = parse("typeof x === 'undefined'");
        Node expr = getFirstStatement(ast);
        Node original = expr.getFirstChild();
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        // The optimization must NOT fire because 'x' might be undeclared (Closure-87 bug)
        // It should keep the typeof comparison.
        if (original.getType() == Token.TYPEOF && original.getFirstChild().isToken(Token.NAME)) {
            // Only check if the node structure is as expected
            assertSame("typeof should not be folded for undeclared variable", original, optimized);
        } else {
            // If the parse creates a different structure, we still expect typeof to remain
            assertTrue("Result should still contain typeof", optimized.getType() == Token.TYPEOF ||
                       (optimized.isToken(Token.EQ) && optimized.getFirstChild().isToken(Token.TYPEOF)));
        }
    }

    @Test
    public void testTypeofWithNameThatLooksDefinedButIsNot() {
        // Global scope: var a; but we test without declaration
        Node ast = parse("typeof b === 'undefined'");
        Node expr = getFirstStatement(ast);
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        // Should remain typeof b
        assertTrue("Should keep typeof for undeclared variable 'b'",
                   optimized.getType() == Token.TYPEOF ||
                   (optimized.isToken(Token.EQ) && optimized.getFirstChild().isToken(Token.TYPEOF)));
    }

    @Test
    public void testTypeofWithFunctionName() {
        // function f() {} ; typeof f === 'undefined' should fold
        Node ast = parse("function f() {} typeof f === 'undefined'");
        Node script = ast.getLastChild();
        Node stmt = script.getFirstChild().getNext(); // second statement
        Node expr = stmt.getFirstChild();
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        assertTrue("typeof f should fold to f === undefined", optimized.isToken(Token.EQ));
        Node left = optimized.getFirstChild();
        assertTrue("Left should be NAME 'f'", left.isToken(Token.NAME) && left.getString().equals("f"));
        Node right = optimized.getSecondChild();
        assertTrue("Right should be NAME 'undefined'", right.isToken(Token.NAME) && right.getString().equals("undefined"));
    }

    @Test
    public void testTypeofWithTypeofUnary() {
        // typeof (typeof x) === 'undefined' – should not fold the outer typeof
        Node ast = parse("typeof (typeof x) === 'undefined'");
        Node expr = getFirstStatement(ast);
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        // The outer typeof should remain because its argument is typeof x (a unary expression)
        assertTrue("Outer typeof should remain", optimized.getType() == Token.TYPEOF ||
                   (optimized.isToken(Token.EQ) && optimized.getFirstChild().isToken(Token.TYPEOF)));
    }

    @Test
    public void testTypeofComparisonWithNull() {
        // typeof x === 'object' should not be affected
        Node ast = parse("var x = {}; typeof x === 'object'");
        Node expr = getFirstStatement(ast).getNext().getFirstChild(); // second statement
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        // This type of comparison is not handled, should remain
        assertTrue("typeof x === 'object' should not be changed", optimized.getType() == Token.EQ &&
                   optimized.getFirstChild().isToken(Token.TYPEOF));
    }

    @Test
    public void testTypeofWithStringEqualsUndefined() {
        // typeof a == 'undefined' (double equals) should also fold
        Node ast = parse("var a; typeof a == 'undefined'");
        Node expr = getFirstStatement(ast);
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        // Should become a == undefined (double equals)
        assertTrue("typeof a == 'undefined' should fold to a == undefined",
                   optimized.isToken(Token.EQ) &&
                   optimized.getFirstChild().isToken(Token.NAME) &&
                   optimized.getFirstChild().getString().equals("a") &&
                   optimized.getSecondChild().isToken(Token.NAME) &&
                   optimized.getSecondChild().getString().equals("undefined"));
    }

    @Test
    public void testTypeofWithStringNotEqualsUndefined() {
        // typeof a !== 'undefined' should fold to a !== undefined
        Node ast = parse("var a; typeof a !== 'undefined'");
        Node expr = getFirstStatement(ast);
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        assertTrue("typeof a !== 'undefined' should fold to a !== undefined",
                   optimized.isToken(Token.NE) &&
                   optimized.getFirstChild().isToken(Token.NAME) &&
                   optimized.getFirstChild().getString().equals("a") &&
                   optimized.getSecondChild().isToken(Token.NAME) &&
                   optimized.getSecondChild().getString().equals("undefined"));
    }

    @Test
    public void testTypeofWithUndeclaredVariableAndNotEquals() {
        // typeof undeclared !== 'undefined' – must not fold
        Node ast = parse("typeof undeclared !== 'undefined'");
        Node expr = getFirstStatement(ast);
        pass.optimizeSubtree(ast);
        Node optimized = expr.getFirstChild();
        // Should remain
        assertTrue("Should NOT fold undeclared variable even with !==",
                   optimized.getType() == Token.NE && optimized.getFirstChild().isToken(Token.TYPEOF));
    }

    @Test
    public void testEmptyBlock() {
        // An empty block should remain unchanged.
        Node ast = parse("{}");
        Node script = ast.getLastChild();
        Node block = script.getFirstChild();
        assertTrue("Precondition: block", block.isToken(Token.BLOCK));
        pass.optimizeSubtree(ast);
        assertSame("Empty block unchanged", block, script.getFirstChild());
    }

    @Test
    public void testNullNode() {
        // If optimizeSubtree is called on null, it should return null.
        Node result = pass.optimizeSubtree(null);
        assertNull("Optimize on null should return null", result);
    }

    @Test
    public void testNoOptimizationsForSimpleExpression() {
        // A simple number expression should remain unchanged.
        Node ast = parse("1 + 2");
        Node expr = getFirstStatement(ast);
        Node original = expr.getFirstChild();
        pass.optimizeSubtree(ast);
        assertSame("1+2 should not be modified", original, expr.getFirstChild());
    }
}