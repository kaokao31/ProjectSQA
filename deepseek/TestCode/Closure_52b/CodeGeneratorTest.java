package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for CodeGenerator focusing on edge cases,
 * branch coverage, and fault detection matching Defects4J Closure-52.
 */
public class CodeGeneratorTest {

    private CompilerOptions options;
    private Compiler compiler;

    @Before
    public void setUp() {
        options = new CompilerOptions();
        compiler = new Compiler();
        compiler.initOptions(options);
    }

    private String generate(Node node) {
        return new CodeGenerator(compiler).generate(node);
    }

    @Test
    public void testEmptyScript() {
        Node script = new Node(Token.SCRIPT);
        assertEquals("", generate(script));
    }

    @Test
    public void testNumberZero() {
        Node num = Node.newNumber(0);
        assertEquals("0", generate(num));
    }

    @Test
    public void testNumberNegativeZero() {
        Node num = Node.newNumber(-0.0);
        assertEquals("0", generate(num));
    }

    @Test
    public void testNumberNaN() {
        Node num = Node.newNumber(Double.NaN);
        assertEquals("NaN", generate(num));
    }

    @Test
    public void testNumberInfinity() {
        Node num = Node.newNumber(Double.POSITIVE_INFINITY);
        assertEquals("Infinity", generate(num));
    }

    @Test
    public void testNumberNegativeInfinity() {
        Node num = Node.newNumber(Double.NEGATIVE_INFINITY);
        assertEquals("-Infinity", generate(num));
    }

    @Test
    public void testNumberLarge() {
        Node num = Node.newNumber(1e21);
        assertEquals("1e+21", generate(num));
    }

    @Test
    public void testStringEmpty() {
        Node str = Node.newString("");
        assertEquals("\"\"", generate(str));
    }

    @Test
    public void testStringWithQuotes() {
        Node str = Node.newString("hello\"world");
        assertEquals("\"hello\\\"world\"", generate(str));
    }

    @Test
    public void testStringWithBackslash() {
        Node str = Node.newString("a\\b");
        assertEquals("\"a\\\\b\"", generate(str));
    }

    @Test
    public void testStringWithNewline() {
        Node str = Node.newString("line1\nline2");
        assertEquals("\"line1\\nline2\"", generate(str));
    }

    @Test
    public void testBooleanTrue() {
        Node bool = new Node(Token.TRUE);
        assertEquals("true", generate(bool));
    }

    @Test
    public void testBooleanFalse() {
        Node bool = new Node(Token.FALSE);
        assertEquals("false", generate(bool));
    }

    @Test
    public void testNull() {
        Node nullNode = new Node(Token.NULL);
        assertEquals("null", generate(nullNode));
    }

    @Test
    public void testArrayLiteralEmpty() {
        Node arr = new Node(Token.ARRAYLIT);
        assertEquals("[]", generate(arr));
    }

    @Test
    public void testArrayLiteralWithHoles() {
        Node arr = new Node(Token.ARRAYLIT);
        arr.addChildToBack(new Node(Token.EMPTY));
        arr.addChildToBack(new Node(Token.EMPTY));
        assertEquals("[,]", generate(arr));
    }

    @Test
    public void testArrayLiteralTrailingComma() {
        Node arr = new Node(Token.ARRAYLIT);
        arr.addChildToBack(Node.newNumber(1));
        arr.addChildToBack(Node.newNumber(2));
        arr.addChildToBack(new Node(Token.EMPTY));
        assertEquals("[1,2,]", generate(arr));
    }

    @Test
    public void testObjectLiteralEmpty() {
        Node obj = new Node(Token.OBJECTLIT);
        assertEquals("{}", generate(obj));
    }

    @Test
    public void testObjectLiteralWithProperty() {
        Node obj = new Node(Token.OBJECTLIT);
        Node key = Node.newString("key");
        Node value = Node.newString("value");
        obj.addChildToBack(Node.newString("key")); // string key
        // Actually, for object literal we need a Prop node
        // Simplified: we use STRING_KEY with child
        Node prop = new Node(Token.STRING_KEY, value);
        prop.putProp(Node.STRING_PROP, "key");
        obj.addChildToBack(prop);
        String result = generate(obj);
        assertTrue(result.contains("\"key\"") || result.contains("key"));
    }

    @Test
    public void testForInLoopWithVar() {
        // Build: for (var x in expr) { body }
        Node forIn = new Node(Token.FOR_IN);
        Node varDecl = new Node(Token.VAR);
        Node nameX = Node.newString(Token.NAME, "x");
        varDecl.addChildToBack(nameX);
        Node objExpr = Node.newString("obj");
        Node body = new Node(Token.BLOCK);
        forIn.addChildToBack(varDecl);
        forIn.addChildToBack(objExpr);
        forIn.addChildToBack(body);
        String result = generate(forIn);
        assertTrue("Expected 'for (var x in obj)' but got: " + result,
                result.contains("for (var x in obj)"));
    }

    @Test
    public void testForInLoopWithoutVar() {
        Node forIn = new Node(Token.FOR_IN);
        Node nameX = Node.newString(Token.NAME, "x");
        Node objExpr = Node.newString("obj");
        Node body = new Node(Token.BLOCK);
        forIn.addChildToBack(nameX);
        forIn.addChildToBack(objExpr);
        forIn.addChildToBack(body);
        String result = generate(forIn);
        assertTrue(result.contains("for (x in obj)"));
    }

    @Test
    public void testForLoop() {
        Node forNode = new Node(Token.FOR);
        // init: var i = 0
        Node init = new Node(Token.VAR);
        Node varI = Node.newString(Token.NAME, "i");
        Node initVal = Node.newNumber(0);
        varI.addChildToBack(initVal);
        init.addChildToBack(varI);
        // condition: i < 10
        Node cond = new Node(Token.LT, Node.newString(Token.NAME, "i"), Node.newNumber(10));
        // increment: i++
        Node inc = new Node(Token.INC, Node.newString(Token.NAME, "i"));
        // body: empty
        Node body = new Node(Token.BLOCK);
        forNode.addChildToBack(init);
        forNode.addChildToBack(cond);
        forNode.addChildToBack(inc);
        forNode.addChildToBack(body);
        String result = generate(forNode);
        assertTrue(result.contains("for") && result.contains("var i=0;i<10;i++"));
    }

    @Test
    public void testWhileLoop() {
        Node whileNode = new Node(Token.WHILE);
        Node cond = new Node(Token.TRUE);
        Node body = new Node(Token.BLOCK);
        whileNode.addChildToBack(cond);
        whileNode.addChildToBack(body);
        assertEquals("while(true){}", generate(whileNode).replaceAll("\\s+", ""));
    }

    @Test
    public void testDoLoop() {
        Node doNode = new Node(Token.DO);
        Node cond = new Node(Token.FALSE);
        Node body = new Node(Token.BLOCK);
        doNode.addChildToBack(body);
        doNode.addChildToBack(cond);
        String result = generate(doNode);
        assertTrue(result.contains("do") && result.contains("while(false)"));
    }

    @Test
    public void testFunctionDeclaration() {
        Node func = new Node(Token.FUNCTION);
        Node name = Node.newString(Token.NAME, "f");
        Node params = new Node(Token.PARAM_LIST);
        Node body = new Node(Token.BLOCK);
        func.addChildToBack(name);
        func.addChildToBack(params);
        func.addChildToBack(body);
        String result = generate(func);
        assertTrue(result.contains("function f(){}") || result.contains("function f() {\n}"));
    }

    @Test
    public void testTryCatchFinally() {
        Node tryNode = new Node(Token.TRY);
        Node tryBody = new Node(Token.BLOCK);
        Node catchNode = new Node(Token.CATCH);
        Node catchVar = Node.newString(Token.NAME, "e");
        Node catchBody = new Node(Token.BLOCK);
        catchNode.addChildToBack(catchVar);
        catchNode.addChildToBack(catchBody);
        Node finallyBody = new Node(Token.BLOCK);
        tryNode.addChildToBack(tryBody);
        tryNode.addChildToBack(catchNode);
        tryNode.addChildToBack(finallyBody);
        String result = generate(tryNode);
        assertTrue(result.contains("try") && result.contains("catch(e)") && result.contains("finally"));
    }

    @Test
    public void testConditionalExpression() {
        Node cond = new Node(Token.HOOK);
        cond.addChildToBack(new Node(Token.TRUE));
        cond.addChildToBack(Node.newNumber(1));
        cond.addChildToBack(Node.newNumber(2));
        assertEquals("true?1:2", generate(cond));
    }

    @Test
    public void testSequenceExpression() {
        Node seq = new Node(Token.COMMA);
        seq.addChildToBack(Node.newNumber(1));
        seq.addChildToBack(Node.newNumber(2));
        assertEquals("1,2", generate(seq));
    }

    // Additional tests to cover more branches and potential faults
    @Test
    public void testUnaryOperators() {
        Node not = new Node(Token.NOT, new Node(Token.TRUE));
        assertEquals("!true", generate(not));

        Node typeof = new Node(Token.TYPEOF, Node.newString("a"));
        assertEquals("typeof a", generate(typeof));
    }

    @Test
    public void testBinaryOperators() {
        Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
        assertEquals("1+2", generate(add));
    }

    @Test
    public void testAssignment() {
        Node assign = new Node(Token.ASSIGN);
        assign.addChildToBack(Node.newString(Token.NAME, "x"));
        assign.addChildToBack(Node.newNumber(5));
        assertEquals("x=5", generate(assign));
    }

    @Test
    public void testRegExp() {
        Node regexp = new Node(Token.REGEXP);
        // Need to set proper props
        regexp.putProp(Node.STRING_PROP, "abc");
        regexp.putProp(Node.REGEXP_FLAGS, "g");
        String result = generate(regexp);
        assertEquals("/abc/g", result);
    }

    // This test specifically targets Defects4J Closure-52 fault
    @Test
    public void testForInVarLoopWithArrayLiteral() {
        // Build: for (var x in [1,2]) {}
        Node forIn = new Node(Token.FOR_IN);
        Node varDecl = new Node(Token.VAR);
        Node nameX = Node.newString(Token.NAME, "x");
        varDecl.addChildToBack(nameX);

        Node arrLit = new Node(Token.ARRAYLIT);
        arrLit.addChildToBack(Node.newNumber(1));
        arrLit.addChildToBack(Node.newNumber(2));

        Node body = new Node(Token.BLOCK);
        forIn.addChildToBack(varDecl);
        forIn.addChildToBack(arrLit);
        forIn.addChildToBack(body);

        String result = generate(forIn);
        // The known bug produced "for (x in [1,2])" instead of "for (var x in [1,2])"
        assertTrue("Expected var in for-in loop: " + result,
                result.contains("for (var x in [1,2])"));
    }

    // Test for object literal key generation
    @Test
    public void testObjectLiteralWithGetter() {
        Node obj = new Node(Token.OBJECTLIT);
        Node getter = new Node(Token.GETTER_DEF);
        Node getterName = Node.newString(Token.STRING, "prop");
        Node getterBody = new Node(Token.BLOCK);
        getter.addChildToBack(getterName);
        getter.addChildToBack(getterBody);
        obj.addChildToBack(getter);
        String result = generate(obj);
        assertTrue(result.contains("get prop") || result.contains("get \"prop\""));
    }
}