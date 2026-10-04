package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for {@link CodeGenerator} focused on high code coverage and
 * detecting known faults (especially Closure bug #123 related to unary
 * operators on numbers).
 */
public class CodeGeneratorTest {

    private CodeGenerator codeGenerator;

    @Before
    public void setUp() {
        // CodeGenerator is used via static methods; no instance needed.
    }

    // ========== Basic number generation ==========

    @Test
    public void testSimpleNumber() {
        Node number = Node.newNumber(42.0);
        assertEquals("42", CodeGenerator.generateCode(number));
    }

    @Test
    public void testNegativeNumber() {
        Node negative = new Node(Token.NEG, Node.newNumber(5));
        assertEquals("-5", CodeGenerator.generateCode(negative));
    }

    @Test
    public void testPositiveUnaryNumber() {
        // Bug #123: unary plus on number may be omitted
        Node positive = new Node(Token.POS, Node.newNumber(3));
        String result = CodeGenerator.generateCode(positive);
        assertEquals("+3", result);
    }

    @Test
    public void testUnaryMinusOnNegativeNumber() {
        Node doubleNeg = new Node(Token.NEG, new Node(Token.NEG, Node.newNumber(7)));
        assertEquals("--7", CodeGenerator.generateCode(doubleNeg));
    }

    // ========== Operator precedence & parentheses ==========

    @Test
    public void testAdditionWithUnaryPlus() {
        // (+3) + 5  should be "+3+5" or "(+3)+5"? Typically +3+5.
        Node add = new Node(Token.ADD,
                new Node(Token.POS, Node.newNumber(3)),
                Node.newNumber(5));
        String code = CodeGenerator.generateCode(add);
        // Ensure parentheses are not inserted incorrectly; the +3 part is atomic
        assertTrue(code.contains("+3+5") || code.contains("(+3)+5"));
    }

    @Test
    public void testSubtractionWithUnaryMinus() {
        Node sub = new Node(Token.SUB,
                new Node(Token.NEG, Node.newNumber(8)),
                Node.newNumber(2));
        String code = CodeGenerator.generateCode(sub);
        assertTrue(code.contains("-8-2") || code.contains("(-8)-2"));
    }

    @Test
    public void testNestedUnaryOperators() {
        Node nested = new Node(Token.NEG,
                new Node(Token.POS,
                        new Node(Token.NEG, Node.newNumber(4))));
        assertEquals("-+ -4", CodeGenerator.generateCode(nested).replaceAll("\\s+", ""));
        // Expected: -+ -4  (with or without spaces)
    }

    // ========== Boolean and null literals ==========

    @Test
    public void testBooleanTrue() {
        Node bool = new Node(Token.TRUE);
        assertEquals("true", CodeGenerator.generateCode(bool));
    }

    @Test
    public void testBooleanFalse() {
        Node bool = new Node(Token.FALSE);
        assertEquals("false", CodeGenerator.generateCode(bool));
    }

    @Test
    public void testNull() {
        Node nil = new Node(Token.NULL);
        assertEquals("null", CodeGenerator.generateCode(nil));
    }

    // ========== Strings ==========

    @Test
    public void testStringLiteral() {
        Node str = Node.newString("hello");
        assertEquals("\"hello\"", CodeGenerator.generateCode(str));
    }

    @Test
    public void testStringWithEscape() {
        Node str = Node.newString("a\"b");
        assertEquals("\"a\\\"b\"", CodeGenerator.generateCode(str));
    }

    // ========== Array literals ==========

    @Test
    public void testEmptyArray() {
        Node arr = new Node(Token.ARRAYLIT);
        assertEquals("[]", CodeGenerator.generateCode(arr));
    }

    @Test
    public void testArrayWithElements() {
        Node arr = new Node(Token.ARRAYLIT,
                Node.newNumber(1),
                Node.newNumber(2));
        assertEquals("[1, 2]", CodeGenerator.generateCode(arr).replaceAll("\\s+", ""));
    }

    // ========== Object literals ==========

    @Test
    public void testEmptyObject() {
        Node obj = new Node(Token.OBJECTLIT);
        assertEquals("{}", CodeGenerator.generateCode(obj));
    }

    @Test
    public void testObjectWithProperty() {
        Node obj = new Node(Token.OBJECTLIT,
                new Node(Token.STRING_KEY, "key"),
                Node.newNumber(1));
        String code = CodeGenerator.generateCode(obj);
        assertTrue(code.contains("key"));
    }

    // ========== Function expressions ==========

    @Test
    public void testEmptyFunction() {
        Node func = new Node(Token.FUNCTION);
        // minimal function: function() {}
        assertEquals("function(){}", CodeGenerator.generateCode(func).replaceAll("\\s+", ""));
    }

    @Test
    public void testFunctionWithParam() {
        Node name = Node.newString(Token.NAME, "x");
        Node body = new Node(Token.BLOCK);
        Node func = new Node(Token.FUNCTION, name, body);
        String code = CodeGenerator.generateCode(func);
        assertTrue(code.contains("function x(){}"));
    }

    // ========== Call expressions ==========

    @Test
    public void testFunctionCall() {
        Node callee = new Node(Token.NAME, "f");
        Node call = new Node(Token.CALL, callee);
        assertEquals("f()", CodeGenerator.generateCode(call));
    }

    @Test
    public void testFunctionCallWithArg() {
        Node callee = new Node(Token.NAME, "g");
        Node arg = Node.newNumber(10);
        Node call = new Node(Token.CALL, callee, arg);
        assertEquals("g(10)", CodeGenerator.generateCode(call));
    }

    // ========== Name references ==========

    @Test
    public void testSimpleName() {
        Node name = new Node(Token.NAME, "abc");
        assertEquals("abc", CodeGenerator.generateCode(name));
    }

    @Test
    public void testThis() {
        Node thisNode = new Node(Token.THIS);
        assertEquals("this", CodeGenerator.generateCode(thisNode));
    }

    // ========== Edge cases for null / missing children ==========

    @Test(expected = IllegalStateException.class)
    public void testNullNode() {
        CodeGenerator.generateCode(null);
    }

    @Test
    public void testUnrecognizedToken() {
        // Not a real token; may cause unexpected behavior
        Node unknown = new Node(Token.DEFAULT_VALUE, Node.newNumber(1));
        // Just ensure no crash
        String code = CodeGenerator.generateCode(unknown);
        assertNotNull(code);
    }

    // ========== Complex expressions (regression for bug #123) ==========

    @Test
    public void testUnaryOnNumberInArithmetic() {
        // Expression: 1 + +2 should generate "1 + +2" or similar
        Node plusTwo = new Node(Token.POS, Node.newNumber(2));
        Node add = new Node(Token.ADD, Node.newNumber(1), plusTwo);
        String result = CodeGenerator.generateCode(add);
        assertTrue("Expected 1++2 or 1 + +2, got: " + result,
                result.contains("1+") && result.contains("+2"));
    }

    @Test
    public void testNestedNegatives() {
        // -(-5)
        Node innerNeg = new Node(Token.NEG, Node.newNumber(5));
        Node outerNeg = new Node(Token.NEG, innerNeg);
        assertEquals("--5", CodeGenerator.generateCode(outerNeg));
    }
}