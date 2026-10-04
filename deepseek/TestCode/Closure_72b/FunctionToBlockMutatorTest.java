package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for FunctionToBlockMutator.
 * Designed to achieve maximum code coverage and reveal faults
 * (in particular, Defects4J Closure bug #72).
 */
public class FunctionToBlockMutatorTest {

    private Compiler compiler;
    private FunctionToBlockMutator mutator;
    private Node emptyBlock;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // The mutator is constructed with a compiler and a synthetic name.
        mutator = new FunctionToBlockMutator(compiler, "__mutate_test__");
        emptyBlock = new Node(Token.BLOCK);
    }

    // ------------- Helpers to build minimal AST nodes -------------

    private Node createFunctionBody(Node... statements) {
        Node body = new Node(Token.BLOCK);
        for (Node stm : statements) {
            body.addChildToBack(stm);
        }
        return body;
    }

    private Node createFunctionNode(Node body) {
        Node name = new Node(Token.NAME, "fn");
        Node params = new Node(Token.PARAM_LIST);
        Node fn = new Node(Token.FUNCTION, name, params, body);
        fn.setSourceFileName("test.js");
        return fn;
    }

    private Node createVarDeclaration(String varName, Node initValue) {
        Node nameNode = Node.newString(Token.NAME, varName);
        if (initValue != null) {
            nameNode.addChildToBack(initValue);
        }
        return new Node(Token.VAR, nameNode);
    }

    private Node createAssignment(String targetName, Node value) {
        Node target = Node.newString(Token.NAME, targetName);
        Node assign = new Node(Token.ASSIGN, target, value);
        return new Node(Token.EXPR_RESULT, assign);
    }

    private Node createCallNode(String targetName) {
        // simulate a call to the original function
        Node callee = Node.newString(Token.NAME, targetName);
        return new Node(Token.CALL, callee);
    }

    private Node createNumberLiteral(double value) {
        return Node.newNumber(value);
    }

    private Node createStringLiteral(String value) {
        return Node.newString(value);
    }

    // ------------- Tests for basic functionality -------------

    @Test
    public void testEmptyFunctionBody() {
        Node fnNode = createFunctionNode(createFunctionBody());
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull("Mutated block must not be null", block);
        assertEquals("Block type is BLOCK", Token.BLOCK, block.getType());
        assertEquals("Empty body => empty block", 0, block.getChildCount());
    }

    @Test
    public void testSingleExpressionStatement() {
        Node expr = createAssignment("x", createNumberLiteral(42));
        Node fnNode = createFunctionNode(createFunctionBody(expr));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        assertEquals(1, block.getChildCount());
        Node firstChild = block.getFirstChild();
        assertTrue("First child must be expression result", firstChild.getType() == Token.EXPR_RESULT);
    }

    @Test
    public void testMultipleStatementsOrderPreserved() {
        Node stmt1 = createAssignment("a", createNumberLiteral(1));
        Node stmt2 = createAssignment("b", createNumberLiteral(2));
        Node stmt3 = createAssignment("c", createNumberLiteral(3));
        Node fnNode = createFunctionNode(createFunctionBody(stmt1, stmt2, stmt3));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        assertEquals("Three statements must appear in block", 3, block.getChildCount());
        assertEquals("First statement is a", "a", block.getFirstChild().getFirstChild().getString());
        assertEquals("Second statement is b", "b", block.getFirstChild().getNext().getFirstChild().getString());
        assertEquals("Third statement is c", "c", block.getLastChild().getFirstChild().getString());
    }

    @Test
    public void testVarDeclarationPreserved() {
        Node varStmt = createVarDeclaration("v", createNumberLiteral(10));
        Node fnNode = createFunctionNode(createFunctionBody(varStmt));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        assertEquals(1, block.getChildCount());
        assertEquals("Var statement remains", Token.VAR, block.getFirstChild().getType());
    }

    @Test
    public void testIfStatementTransformedCorrectly() {
        // function f() { if (true) { x=1; } else { x=2; } }
        Node cond = IR.trueNode(); // requires static import, but we use IR
        Node thenBlock = createFunctionBody(createAssignment("x", createNumberLiteral(1)));
        Node elseBlock = createFunctionBody(createAssignment("x", createNumberLiteral(2)));
        Node ifStmt = new Node(Token.IF, cond, thenBlock, elseBlock);
        Node fnNode = createFunctionNode(createFunctionBody(ifStmt));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        assertEquals(1, block.getChildCount());
        assertEquals("Must be IF node", Token.IF, block.getFirstChild().getType());
    }

    @Test
    public void testForStatementHandling() {
        // function f() { for(var i=0; i<10; i++) { total+=i; } }
        Node init = createVarDeclaration("i", createNumberLiteral(0));
        Node cond = new Node(Token.LT, Node.newString(Token.NAME, "i"), createNumberLiteral(10));
        Node incr = new Node(Token.INC, Node.newString(Token.NAME, "i"));
        Node body = createFunctionBody(createAssignment("total", new Node(Token.ADD,
                Node.newString(Token.NAME, "total"), Node.newString(Token.NAME, "i"))));
        Node forStmt = new Node(Token.FOR, init, cond, incr, body);
        Node fnNode = createFunctionNode(createFunctionBody(forStmt));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        assertEquals(1, block.getChildCount());
        assertEquals("FOR node remains", Token.FOR, block.getFirstChild().getType());
    }

    @Test
    public void testForInLoopNotCorrupted() {
        // Bug 72 related: function f() { for(var k in obj) { use(k); } }
        Node iter = Node.newString(Token.NAME, "k");
        Node object = Node.newString(Token.NAME, "obj");
        Node body = createFunctionBody(createCallNode("use"));
        Node forIn = new Node(Token.FOR_IN, iter, object, body);
        Node fnNode = createFunctionNode(createFunctionBody(forIn));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        assertEquals(1, block.getChildCount());
        // Ensure the FOR_IN structure is intact (no index out-of-bounds)
        assertEquals(Token.FOR_IN, block.getFirstChild().getType());
        assertEquals(3, block.getFirstChild().getChildCount());
    }

    @Test(expected = NullPointerException.class)
    public void testNullFunctionNodeThrowsException() {
        mutator.mutate(null, createCallNode("fn"), emptyBlock);
    }

    @Test(expected = NullPointerException.class)
    public void testNullCallNodeThrowsException() {
        Node fnNode = createFunctionNode(createFunctionBody());
        mutator.mutate(fnNode, null, emptyBlock);
    }

    @Test
    public void testReturnStatementConversion() {
        // function f() { return a + b; }
        Node returnExpr = new Node(Token.ADD,
                Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
        Node ret = new Node(Token.RETURN, returnExpr);
        Node fnNode = createFunctionNode(createFunctionBody(ret));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        // The mutator should convert the return to an assignment to a temporary.
        // Check that the temp variable is present.
        Node firstStmt = block.getFirstChild();
        assertNotNull(firstStmt);
        // Typically a return becomes a compound assignment
        assertTrue(firstStmt.getType() == Token.EXPR_RESULT || firstStmt.getType() == Token.RETURN);
    }

    @Test
    public void testNestedFunctionBodyRemainsNested() {
        // function f() { function inner() {} }
        Node innerFn = createFunctionNode(createFunctionBody()); // inner
        Node fnNode = createFunctionNode(createFunctionBody(innerFn));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        assertEquals(1, block.getChildCount());
        assertEquals("Inner function should be preserved", Token.FUNCTION, block.getFirstChild().getType());
    }

    @Test
    public void testMultipleParametersReplacedCorrectly() {
        // function f(a, b) { return a + b; }
        Node nameA = Node.newString(Token.NAME, "a");
        Node nameB = Node.newString(Token.NAME, "b");
        Node paramList = new Node(Token.PARAM_LIST, nameA, nameB);
        Node body = createFunctionBody(new Node(Token.RETURN,
                new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"))));
        Node fnNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), paramList, body);
        // Create a call node with arguments 10 and 20
        Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "f"),
                createNumberLiteral(10), createNumberLiteral(20));
        Node block = mutator.mutate(fnNode, callNode, emptyBlock);
        assertNotNull(block);
        // After mutation, references to a and b should be replaced by 10 and 20
        // Verify that the block does not contain NAME nodes for a or b.
        assertFalse("Parameter name a should not appear", containsName(block, "a"));
        assertFalse("Parameter name b should not appear", containsName(block, "b"));
    }

    private boolean containsName(Node node, String name) {
        if (node.getType() == Token.NAME && name.equals(node.getString())) {
            return true;
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (containsName(child, name)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testMutationUsesProvidedBlockParent() {
        Node fnNode = createFunctionNode(createFunctionBody(createAssignment("x", createNumberLiteral(1))));
        Node parentBlock = new Node(Token.BLOCK);
        parentBlock.addChildToBack(new Node(Token.EMPTY)); // dummy
        Node resultBlock = mutator.mutate(fnNode, createCallNode("fn"), parentBlock);
        // Should use the same parentBlock
        assertSame("Must be same parent block", parentBlock, resultBlock);
        assertEquals("Parent block should have one extra statement", 2, parentBlock.getChildCount());
    }

    @Test
    public void testLargeFunctionBodyPerformance() {
        // Build a function with 1000 simple assignments
        Node[] stmts = new Node[1000];
        for (int i = 0; i < 1000; i++) {
            stmts[i] = createAssignment("x" + i, createNumberLiteral(i));
        }
        Node fnNode = createFunctionNode(createFunctionBody(stmts));
        Node block = mutator.mutate(fnNode, createCallNode("fn"), emptyBlock);
        assertNotNull(block);
        assertEquals(1000, block.getChildCount());
    }
}