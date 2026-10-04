package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for PrepareAst that achieves maximum coverage and detects potential faults.
 */
public class PrepareAstTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Enable all checks to ensure PrepareAst is invoked
        options.setChecksOnly(true);
        options.setIdeMode(true);
    }

    @Test
    public void testEmptyScript() {
        String code = "";
        Node root = parseAndPrepare(code);
        assertNotNull("AST should not be null", root);
        assertTrue("Root should be a script", root.isScript());
        assertEquals("Empty script should have no children", 0, root.getChildCount());
    }

    @Test
    public void testSimpleVarDeclaration() {
        String code = "var x = 1;";
        Node root = parseAndPrepare(code);
        assertNotNull(root);
        Node varNode = root.getFirstChild();
        assertNotNull("Should have a var node", varNode);
        assertTrue("First child should be VAR", varNode.isVar());
        Node nameNode = varNode.getFirstChild();
        assertNotNull("Var should have a name child", nameNode);
        assertTrue("Name child should be NAME", nameNode.isName());
        assertEquals("Variable name should be 'x'", "x", nameNode.getString());
        Node assignNode = nameNode.getFirstChild();
        assertNotNull("Should have an assignment value", assignNode);
        assertTrue("Assignment should be a number", assignNode.isNumber());
        assertEquals("Value should be 1", 1.0, assignNode.getDouble(), 0.0);
    }

    @Test
    public void testMultipleVarDeclarations() {
        String code = "var a = 1, b = 2;";
        Node root = parseAndPrepare(code);
        Node varNode = root.getFirstChild();
        assertNotNull(varNode);
        assertTrue(varNode.isVar());
        // Should have two NAME children
        int count = 0;
        for (Node child = varNode.getFirstChild(); child != null; child = child.getNext()) {
            assertTrue(child.isName());
            count++;
        }
        assertEquals("Should have two variable names", 2, count);
    }

    @Test
    public void testFunctionDeclaration() {
        String code = "function foo() { return 1; }";
        Node root = parseAndPrepare(code);
        Node funcNode = root.getFirstChild();
        assertNotNull("Should have a function node", funcNode);
        assertTrue("First child should be FUNCTION", funcNode.isFunction());
        Node nameNode = funcNode.getFirstChild();
        assertNotNull("Function should have a name", nameNode);
        assertTrue("Name should be NAME", nameNode.isName());
        assertEquals("Function name should be 'foo'", "foo", nameNode.getString());
    }

    @Test
    public void testIfStatement() {
        String code = "if (true) { var x = 1; } else { var y = 2; }";
        Node root = parseAndPrepare(code);
        Node ifNode = root.getFirstChild();
        assertNotNull(ifNode);
        assertTrue("First child should be IF", ifNode.isIf());
        // Check that both branches are present
        Node condition = ifNode.getFirstChild();
        assertNotNull("Condition should exist", condition);
        Node thenBlock = condition.getNext();
        assertNotNull("Then block should exist", thenBlock);
        assertTrue("Then block should be BLOCK", thenBlock.isBlock());
        Node elseBlock = thenBlock.getNext();
        assertNotNull("Else block should exist", elseBlock);
        assertTrue("Else block should be BLOCK", elseBlock.isBlock());
    }

    @Test
    public void testForLoop() {
        String code = "for (var i = 0; i < 10; i++) { }";
        Node root = parseAndPrepare(code);
        Node forNode = root.getFirstChild();
        assertNotNull(forNode);
        assertTrue("First child should be FOR", forNode.isFor());
        // Check structure: initializer, condition, increment, body
        Node init = forNode.getFirstChild();
        assertNotNull("Initializer should exist", init);
        Node cond = init.getNext();
        assertNotNull("Condition should exist", cond);
        Node incr = cond.getNext();
        assertNotNull("Increment should exist", incr);
        Node body = incr.getNext();
        assertNotNull("Body should exist", body);
        assertTrue("Body should be BLOCK", body.isBlock());
    }

    @Test
    public void testTryCatch() {
        String code = "try { var x = 1; } catch (e) { var y = 2; }";
        Node root = parseAndPrepare(code);
        Node tryNode = root.getFirstChild();
        assertNotNull(tryNode);
        assertTrue("First child should be TRY", tryNode.isTry());
        // Check try block, catch block, finally block (null)
        Node tryBlock = tryNode.getFirstChild();
        assertNotNull("Try block should exist", tryBlock);
        assertTrue("Try block should be BLOCK", tryBlock.isBlock());
        Node catchBlock = tryBlock.getNext();
        assertNotNull("Catch block should exist", catchBlock);
        assertTrue("Catch block should be BLOCK", catchBlock.isBlock());
        // No finally
        Node finallyBlock = catchBlock.getNext();
        assertNull("Finally block should be null", finallyBlock);
    }

    @Test
    public void testUndefinedVariable() {
        String code = "var x;";
        Node root = parseAndPrepare(code);
        Node varNode = root.getFirstChild();
        assertNotNull(varNode);
        assertTrue(varNode.isVar());
        Node nameNode = varNode.getFirstChild();
        assertNotNull(nameNode);
        assertTrue(nameNode.isName());
        // Should have no child (no assignment)
        assertNull("Undefined var should have no child", nameNode.getFirstChild());
    }

    @Test
    public void testNestedFunction() {
        String code = "function outer() { function inner() { return 1; } }";
        Node root = parseAndPrepare(code);
        Node outerFunc = root.getFirstChild();
        assertNotNull(outerFunc);
        assertTrue(outerFunc.isFunction());
        // Get body of outer function
        Node outerBody = outerFunc.getChildAfter(outerFunc.getFirstChild()); // skip name
        assertNotNull(outerBody);
        assertTrue("Outer body should be BLOCK", outerBody.isBlock());
        Node innerFunc = outerBody.getFirstChild();
        assertNotNull("Inner function should exist", innerFunc);
        assertTrue("Inner should be FUNCTION", innerFunc.isFunction());
    }

    @Test
    public void testExpressionStatement() {
        String code = "1 + 2;";
        Node root = parseAndPrepare(code);
        Node exprNode = root.getFirstChild();
        assertNotNull(exprNode);
        assertTrue("Should be EXPR_RESULT", exprNode.isExprResult());
        Node addNode = exprNode.getFirstChild();
        assertNotNull(addNode);
        assertTrue("Should be ADD", addNode.isAdd());
    }

    @Test
    public void testStringLiteral() {
        String code = "var s = 'hello';";
        Node root = parseAndPrepare(code);
        Node varNode = root.getFirstChild();
        Node nameNode = varNode.getFirstChild();
        Node stringNode = nameNode.getFirstChild();
        assertNotNull(stringNode);
        assertTrue("Should be STRING", stringNode.isString());
        assertEquals("Value should be 'hello'", "hello", stringNode.getString());
    }

    @Test
    public void testArrayLiteral() {
        String code = "var arr = [1, 2, 3];";
        Node root = parseAndPrepare(code);
        Node varNode = root.getFirstChild();
        Node nameNode = varNode.getFirstChild();
        Node arrayNode = nameNode.getFirstChild();
        assertNotNull(arrayNode);
        assertTrue("Should be ARRAYLIT", arrayNode.isArrayLit());
        // Check elements
        int count = 0;
        for (Node child = arrayNode.getFirstChild(); child != null; child = child.getNext()) {
            assertTrue("Element should be NUMBER", child.isNumber());
            count++;
        }
        assertEquals("Should have 3 elements", 3, count);
    }

    @Test
    public void testObjectLiteral() {
        String code = "var obj = {a: 1, b: 2};";
        Node root = parseAndPrepare(code);
        Node varNode = root.getFirstChild();
        Node nameNode = varNode.getFirstChild();
        Node objNode = nameNode.getFirstChild();
        assertNotNull(objNode);
        assertTrue("Should be OBJECTLIT", objNode.isObjectLit());
        // Check properties
        int count = 0;
        for (Node child = objNode.getFirstChild(); child != null; child = child.getNext()) {
            assertTrue("Property should be STRING_KEY", child.isStringKey());
            count++;
        }
        assertEquals("Should have 2 properties", 2, count);
    }

    @Test
    public void testReturnStatement() {
        String code = "function f() { return 42; }";
        Node root = parseAndPrepare(code);
        Node funcNode = root.getFirstChild();
        Node body = funcNode.getChildAfter(funcNode.getFirstChild());
        Node returnNode = body.getFirstChild();
        assertNotNull(returnNode);
        assertTrue("Should be RETURN", returnNode.isReturn());
        Node value = returnNode.getFirstChild();
        assertNotNull("Return should have a value", value);
        assertTrue("Value should be NUMBER", value.isNumber());
        assertEquals("Value should be 42", 42.0, value.getDouble(), 0.0);
    }

    @Test
    public void testThrowStatement() {
        String code = "throw new Error('msg');";
        Node root = parseAndPrepare(code);
        Node throwNode = root.getFirstChild();
        assertNotNull(throwNode);
        assertTrue("Should be THROW", throwNode.isThrow());
        Node newExpr = throwNode.getFirstChild();
        assertNotNull("Throw should have expression", newExpr);
        assertTrue("Should be NEW", newExpr.isNew());
    }

    @Test
    public void testSwitchStatement() {
        String code = "switch (x) { case 1: break; default: break; }";
        Node root = parseAndPrepare(code);
        Node switchNode = root.getFirstChild();
        assertNotNull(switchNode);
        assertTrue("Should be SWITCH", switchNode.isSwitch());
        // Check structure: switch value, case blocks
        Node switchValue = switchNode.getFirstChild();
        assertNotNull("Switch value should exist", switchValue);
        Node caseBlock = switchValue.getNext();
        assertNotNull("First case should exist", caseBlock);
        assertTrue("Should be CASE", caseBlock.isCase());
        Node defaultBlock = caseBlock.getNext();
        assertNotNull("Default case should exist", defaultBlock);
        assertTrue("Should be DEFAULT_CASE", defaultBlock.isDefaultCase());
    }

    @Test
    public void testWhileLoop() {
        String code = "while (true) { break; }";
        Node root = parseAndPrepare(code);
        Node whileNode = root.getFirstChild();
        assertNotNull(whileNode);
        assertTrue("Should be WHILE", whileNode.isWhile());
        Node condition = whileNode.getFirstChild();
        assertNotNull("Condition should exist", condition);
        Node body = condition.getNext();
        assertNotNull("Body should exist", body);
        assertTrue("Body should be BLOCK", body.isBlock());
    }

    @Test
    public void testDoLoop() {
        String code = "do { } while (false);";
        Node root = parseAndPrepare(code);
        Node doNode = root.getFirstChild();
        assertNotNull(doNode);
        assertTrue("Should be DO", doNode.isDo());
        Node body = doNode.getFirstChild();
        assertNotNull("Body should exist", body);
        assertTrue("Body should be BLOCK", body.isBlock());
        Node condition = body.getNext();
        assertNotNull("Condition should exist", condition);
    }

    @Test
    public void testLabeledStatement() {
        String code = "label: var x = 1;";
        Node root = parseAndPrepare(code);
        Node labelNode = root.getFirstChild();
        assertNotNull(labelNode);
        assertTrue("Should be LABEL", labelNode.isLabel());
        Node labelName = labelNode.getFirstChild();
        assertNotNull("Label name should exist", labelName);
        assertTrue("Label name should be LABEL_NAME", labelName.isLabelName());
        Node statement = labelName.getNext();
        assertNotNull("Statement should exist", statement);
        assertTrue("Statement should be VAR", statement.isVar());
    }

    @Test
    public void testWithStatement() {
        String code = "with (obj) { var x = 1; }";
        Node root = parseAndPrepare(code);
        Node withNode = root.getFirstChild();
        assertNotNull(withNode);
        assertTrue("Should be WITH", withNode.isWith());
        Node object = withNode.getFirstChild();
        assertNotNull("Object should exist", object);
        Node body = object.getNext();
        assertNotNull("Body should exist", body);
        assertTrue("Body should be BLOCK", body.isBlock());
    }

    @Test
    public void testDebuggerStatement() {
        String code = "debugger;";
        Node root = parseAndPrepare(code);
        Node debuggerNode = root.getFirstChild();
        assertNotNull(debuggerNode);
        assertTrue("Should be DEBUGGER", debuggerNode.isDebugger());
    }

    // Helper method to parse and run PrepareAst
    private Node parseAndPrepare(String code) {
        SourceFile input = SourceFile.fromCode("test.js", code);
        compiler.compile(
                new SourceFile[] {},
                new SourceFile[] { input },
                options);
        Node root = compiler.getRoot();
        // PrepareAst is run as part of the compilation process when checksOnly is true
        // We can also explicitly run it if needed, but the compiler already does it.
        return root;
    }
}