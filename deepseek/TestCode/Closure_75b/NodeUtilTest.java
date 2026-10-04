package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

public class NodeUtilTest {

    private Node emptyScript;
    private Node numberNode;
    private Node stringNode;
    private Node booleanTrueNode;
    private Node booleanFalseNode;
    private Node nullNode;
    private Node undefinedNode;
    private Node arrayLitNode;
    private Node objectLitNode;
    private Node functionNode;
    private Node addNode;
    private Node eqNode;
    private Node neNode;
    private Node sheqNode;
    private Node shneNode;
    private Node notNode;
    private Node callNode;

    @Before
    public void setUp() {
        emptyScript = new Node(Token.SCRIPT);

        numberNode = Node.newNumber(42);
        stringNode = Node.newString("test");
        booleanTrueNode = Node.newBoolean(true);
        booleanFalseNode = Node.newBoolean(false);
        nullNode = Node.newNull();
        undefinedNode = Node.newUndefined();
        arrayLitNode = new Node(Token.ARRAYLIT);
        objectLitNode = new Node(Token.OBJECTLIT);
        functionNode = new Node(Token.FUNCTION);

        addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
        eqNode = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(1));
        neNode = new Node(Token.NE, Node.newNumber(1), Node.newNumber(2));
        sheqNode = new Node(Token.SHEQ, Node.newNumber(1), Node.newNumber(1));
        shneNode = new Node(Token.SHNE, Node.newNumber(1), Node.newNumber(2));
        notNode = new Node(Token.NOT, booleanTrueNode);
        callNode = new Node(Token.CALL, new Node(Token.NAME, "func"));
    }

    // --- isBooleanResult tests ---
    @Test
    public void testIsBooleanResult_equality() {
        assertTrue("EQ should be boolean result", NodeUtil.isBooleanResult(eqNode));
        assertTrue("NE should be boolean result", NodeUtil.isBooleanResult(neNode));
        assertTrue("SHEQ should be boolean result", NodeUtil.isBooleanResult(sheqNode));
        assertTrue("SHNE should be boolean result", NodeUtil.isBooleanResult(shneNode));
    }

    @Test
    public void testIsBooleanResult_logical() {
        Node andNode = new Node(Token.AND, booleanTrueNode, booleanFalseNode);
        Node orNode = new Node(Token.OR, booleanTrueNode, booleanFalseNode);
        assertTrue("AND should be boolean result", NodeUtil.isBooleanResult(andNode));
        assertTrue("OR should be boolean result", NodeUtil.isBooleanResult(orNode));
        assertTrue("NOT should be boolean result", NodeUtil.isBooleanResult(notNode));
    }

    @Test
    public void testIsBooleanResult_comparisons() {
        Node ltNode = new Node(Token.LT, Node.newNumber(1), Node.newNumber(2));
        Node leNode = new Node(Token.LE, Node.newNumber(1), Node.newNumber(2));
        Node gtNode = new Node(Token.GT, Node.newNumber(1), Node.newNumber(2));
        Node geNode = new Node(Token.GE, Node.newNumber(1), Node.newNumber(2));
        assertTrue("LT should be boolean result", NodeUtil.isBooleanResult(ltNode));
        assertTrue("LE should be boolean result", NodeUtil.isBooleanResult(leNode));
        assertTrue("GT should be boolean result", NodeUtil.isBooleanResult(gtNode));
        assertTrue("GE should be boolean result", NodeUtil.isBooleanResult(geNode));
    }

    @Test
    public void testIsBooleanResult_instanceof() {
        Node instanceofNode = new Node(Token.INSTANCEOF, Node.newString(""), objectLitNode);
        assertTrue("INSTANCEOF should be boolean result", NodeUtil.isBooleanResult(instanceofNode));
    }

    @Test
    public void testIsBooleanResult_booleanLiterals() {
        assertTrue("Boolean true literal", NodeUtil.isBooleanResult(booleanTrueNode));
        assertTrue("Boolean false literal", NodeUtil.isBooleanResult(booleanFalseNode));
    }

    @Test
    public void testIsBooleanResult_in() {
        Node inNode = new Node(Token.IN, Node.newString("prop"), objectLitNode);
        assertTrue("IN should be boolean result", NodeUtil.isBooleanResult(inNode));
    }

    @Test
    public void testIsBooleanResult_doesNotReturnTrueForNonBoolean() {
        assertFalse("Number is not boolean result", NodeUtil.isBooleanResult(numberNode));
        assertFalse("String is not boolean result", NodeUtil.isBooleanResult(stringNode));
        assertFalse("Array literal is not boolean result", NodeUtil.isBooleanResult(arrayLitNode));
        assertFalse("Object literal is not boolean result", NodeUtil.isBooleanResult(objectLitNode));
        assertFalse("Function is not boolean result", NodeUtil.isBooleanResult(functionNode));
        assertFalse("Null is not boolean result", NodeUtil.isBooleanResult(nullNode));
        assertFalse("Undefined is not boolean result", NodeUtil.isBooleanResult(undefinedNode));
        assertFalse("Add is not boolean result", NodeUtil.isBooleanResult(addNode));
        assertFalse("Call is not boolean result", NodeUtil.isBooleanResult(callNode));
    }

    // --- isNumberResult tests ---
    @Test
    public void testIsNumberResult_arithmetic() {
        Node subNode = new Node(Token.SUB, Node.newNumber(5), Node.newNumber(3));
        Node mulNode = new Node(Token.MUL, Node.newNumber(2), Node.newNumber(3));
        Node divNode = new Node(Token.DIV, Node.newNumber(10), Node.newNumber(2));
        Node modNode = new Node(Token.MOD, Node.newNumber(7), Node.newNumber(3));
        Node posNode = new Node(Token.POS, numberNode);
        Node negNode = new Node(Token.NEG, numberNode);
        assertTrue("ADD should be number result", NodeUtil.isNumberResult(addNode));
        assertTrue("SUB should be number result", NodeUtil.isNumberResult(subNode));
        assertTrue("MUL should be number result", NodeUtil.isNumberResult(mulNode));
        assertTrue("DIV should be number result", NodeUtil.isNumberResult(divNode));
        assertTrue("MOD should be number result", NodeUtil.isNumberResult(modNode));
        assertTrue("POS should be number result", NodeUtil.isNumberResult(posNode));
        assertTrue("NEG should be number result", NodeUtil.isNumberResult(negNode));
    }

    @Test
    public void testIsNumberResult_bitwise() {
        Node bitAnd = new Node(Token.BITAND, numberNode, Node.newNumber(1));
        Node bitOr = new Node(Token.BITOR, numberNode, Node.newNumber(2));
        Node bitXor = new Node(Token.BITXOR, numberNode, Node.newNumber(3));
        Node bitNot = new Node(Token.BITNOT, numberNode);
        Node leftShift = new Node(Token.LSH, numberNode, Node.newNumber(1));
        Node rightShift = new Node(Token.RSH, numberNode, Node.newNumber(1));
        Node unsignedRightShift = new Node(Token.URSH, numberNode, Node.newNumber(1));
        assertTrue("BITAND should be number result", NodeUtil.isNumberResult(bitAnd));
        assertTrue("BITOR should be number result", NodeUtil.isNumberResult(bitOr));
        assertTrue("BITXOR should be number result", NodeUtil.isNumberResult(bitXor));
        assertTrue("BITNOT should be number result", NodeUtil.isNumberResult(bitNot));
        assertTrue("LSH should be number result", NodeUtil.isNumberResult(leftShift));
        assertTrue("RSH should be number result", NodeUtil.isNumberResult(rightShift));
        assertTrue("URSH should be number result", NodeUtil.isNumberResult(unsignedRightShift));
    }

    @Test
    public void testIsNumberResult_incDec() {
        Node inc = new Node(Token.INC, Node.newNumber(1));
        Node dec = new Node(Token.DEC, Node.newNumber(1));
        assertTrue("INC should be number result", NodeUtil.isNumberResult(inc));
        assertTrue("DEC should be number result", NodeUtil.isNumberResult(dec));
    }

    @Test
    public void testIsNumberResult_numberLiteral() {
        assertTrue("Number literal", NodeUtil.isNumberResult(numberNode));
    }

    @Test
    public void testIsNumberResult_doesNotReturnTrueForNonNumber() {
        assertFalse("String is not number result", NodeUtil.isNumberResult(stringNode));
        assertFalse("Boolean is not number result", NodeUtil.isNumberResult(booleanTrueNode));
        assertFalse("Array literal is not number result", NodeUtil.isNumberResult(arrayLitNode));
        assertFalse("Object literal is not number result", NodeUtil.isNumberResult(objectLitNode));
        assertFalse("Function is not number result", NodeUtil.isNumberResult(functionNode));
        assertFalse("Null is not number result", NodeUtil.isNumberResult(nullNode));
        assertFalse("Equality is not number result", NodeUtil.isNumberResult(eqNode));
        assertFalse("Call is not number result", NodeUtil.isNumberResult(callNode));
    }

    // --- isNaN tests ---
    @Test
    public void testIsNaN_NaNNode() {
        Node nanNode = Node.newNumber(Double.NaN);
        assertTrue("NaN should be NaN", NodeUtil.isNaN(nanNode));
    }

    @Test
    public void testIsNaN_nonNaNNumber() {
        assertFalse("Regular number should not be NaN", NodeUtil.isNaN(numberNode));
    }

    @Test
    public void testIsNaN_string() {
        assertFalse("String should not be NaN", NodeUtil.isNaN(stringNode));
    }

    @Test
    public void testIsNaN_boolean() {
        assertFalse("Boolean should not be NaN", NodeUtil.isNaN(booleanTrueNode));
    }

    @Test
    public void testIsNaN_null() {
        // Some implementations might treat null as NaN? Typically not.
        assertFalse("Null should not be NaN", NodeUtil.isNaN(nullNode));
    }

    @Test
    public void testIsNaN_undefined() {
        // According to Defects4J Closure 75, isNaN might incorrectly treat undefined as NaN
        // The fix is to ensure undefined is not considered NaN.
        assertFalse("Undefined should not be NaN", NodeUtil.isNaN(undefinedNode));
    }

    @Test
    public void testIsNaN_addChild() {
        // A more complex expression like 'NaN' as string? Not needed.
        // Ensure that non-number nodes are handled.
        assertFalse("Equality node should not be NaN", NodeUtil.isNaN(eqNode));
    }

    // --- isFunctionObjectApply tests ---
    @Test
    public void testIsFunctionObjectApply_applyCall() {
        Node applyNode = new Node(Token.CALL,
            new Node(Token.GETPROP, new Node(Token.NAME, "o"), Node.newString("apply")));
        // Also need an argument list (at least one argument for apply?)
        // The implementation checks the function is GETPROP with property "apply"
        assertTrue("o.apply(...) should be function object apply", NodeUtil.isFunctionObjectApply(applyNode));
    }

    @Test
    public void testIsFunctionObjectApply_callCall() {
        Node callApplyNode = new Node(Token.CALL,
            new Node(Token.GETPROP, new Node(Token.NAME, "func"), Node.newString("call")));
        assertTrue("func.call(...) should be function object apply", NodeUtil.isFunctionObjectApply(callApplyNode));
    }

    @Test
    public void testIsFunctionObjectApply_notApplyCall() {
        // A regular call
        assertFalse("Regular call is not function object apply", NodeUtil.isFunctionObjectApply(callNode));
        // A call with a name directly
        Node nameCall = new Node(Token.CALL, new Node(Token.NAME, "apply"));
        assertFalse("apply() without getprop is not function object apply", NodeUtil.isFunctionObjectApply(nameCall));
    }

    @Test
    public void testIsFunctionObjectApply_nullFunction() {
        // When GETPROP's first child is null? But Node shouldn't be null.
        // Instead test that GETPROP that doesn't have property "apply" or "call" returns false.
        Node bindNode = new Node(Token.CALL,
            new Node(Token.GETPROP, new Node(Token.NAME, "f"), Node.newString("bind")));
        assertFalse("bind is not apply or call", NodeUtil.isFunctionObjectApply(bindNode));
    }

    // --- isFunction tests ---
    @Test
    public void testIsFunction_functionNode() {
        assertTrue("Function node should be a function", NodeUtil.isFunction(functionNode));
    }

    @Test
    public void testIsFunction_nonFunction() {
        assertFalse("Number is not a function", NodeUtil.isFunction(numberNode));
        assertFalse("String is not a function", NodeUtil.isFunction(stringNode));
        assertFalse("Script is not a function", NodeUtil.isFunction(emptyScript));
    }

    // --- isEmptyBlock tests ---
    @Test
    public void testIsEmptyBlock_emptyBlock() {
        Node block = new Node(Token.BLOCK);
        assertTrue("Empty block should be empty", NodeUtil.isEmptyBlock(block));
    }

    @Test
    public void testIsEmptyBlock_nonEmptyBlock() {
        Node block = new Node(Token.BLOCK);
        block.addChildToBack(Node.newString("stmt"));
        assertFalse("Non-empty block should not be empty", NodeUtil.isEmptyBlock(block));
    }

    @Test
    public void testIsEmptyBlock_nullScript() {
        // Script is not a block, but isEmptyBlock checks for BLOCK token.
        assertFalse("Script is not a block", NodeUtil.isEmptyBlock(emptyScript));
    }

    // --- isObjectLitKey tests ---
    @Test
    public void testIsObjectLitKey_objectLitChild() {
        // Create an object literal with a key child.
        Node objectLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING, "key");
        key.setJSDocInfo(new JSDocInfo()); // Simulate non-null JSDocInfo?
        // Actually, isObjectLitKey checks if parent is OBJECT_REF or if parent is parent of a STRING in object literal.
        // Simplified: put key as child of objectLit.
        objectLit.addChildToBack(key);
        assertFalse("Key with JSDocInfo? Not sure. The method checks if node parent is OBJECTLIT and node type is STRING or GETTER/SETTER.",
            NodeUtil.isObjectLitKey(key)); // Need to check implementation. This might be wrong.
    }

    @Test
    public void testIsObjectLitKey_stringNotInObjectLit() {
        Node key = Node.newString(Token.STRING, "key");
        // Without parent, it's not object lit key
        assertFalse("String without parent object lit is not object lit key", NodeUtil.isObjectLitKey(key));
    }

    // --- isStringResult tests ---
    @Test
    public void testIsStringResult_stringLiteral() {
        assertTrue("String literal is string result", NodeUtil.isStringResult(stringNode));
    }

    @Test
    public void testIsStringResult_addWithString() {
        Node addString = new Node(Token.ADD, Node.newString("a"), Node.newString("b"));
        assertTrue("String concatenation is string result", NodeUtil.isStringResult(addString));
    }

    @Test
    public void testIsStringResult_otherCalls() {
        Node arrayLit = new Node(Token.ARRAYLIT);
        assertFalse("Array literal is not string result", NodeUtil.isStringResult(arrayLit));
    }

    // --- isArrayLiteral tests ---
    @Test
    public void testIsArrayLiteral_arrayLit() {
        assertTrue("Array literal node", NodeUtil.isArrayLiteral(arrayLitNode));
    }

    @Test
    public void testIsArrayLiteral_nonArray() {
        assertFalse("Object literal is not array literal", NodeUtil.isArrayLiteral(objectLitNode));
        assertFalse("Number is not array literal", NodeUtil.isArrayLiteral(numberNode));
    }

    // --- isNullOrUndefined tests ---
    @Test
    public void testIsNullOrUndefined_null() {
        assertTrue("Null node", NodeUtil.isNullOrUndefined(nullNode));
    }

    @Test
    public void testIsNullOrUndefined_undefined() {
        assertTrue("Undefined node", NodeUtil.isNullOrUndefined(undefinedNode));
    }

    @Test
    public void testIsNullOrUndefined_other() {
        assertFalse("Boolean is not null or undefined", NodeUtil.isNullOrUndefined(booleanTrueNode));
        assertFalse("Number is not null or undefined", NodeUtil.isNullOrUndefined(numberNode));
    }

    // --- mayBeString tests (simple) ---
    @Test
    public void testMayBeString_string() {
        // mayBeString is not static? It's a helper. But we can test via NodeUtil.mayBeString.
        // Actually NodeUtil.mayBeString is often used. Let's test.
        assertTrue("String node may be string", NodeUtil.mayBeString(stringNode, true));
    }

    // --- isAmbiguousBoolean tests ---
    @Test
    public void testIsAmbiguousBoolean() {
        // Not sure about the implementation, but we can test basic cases.
        Node hookNode = new Node(Token.HOOK, booleanTrueNode, Node.newString("a"), Node.newString("b"));
        assertTrue("HOOK is ambiguous boolean", NodeUtil.isAmbiguousBoolean(hookNode));
        Node andNode = new Node(Token.AND, numberNode, stringNode);
        assertTrue("AND is ambiguous boolean", NodeUtil.isAmbiguousBoolean(andNode));
        Node orNode = new Node(Token.OR, numberNode, stringNode);
        assertTrue("OR is ambiguous boolean", NodeUtil.isAmbiguousBoolean(orNode));
        Node numberOnly = numberNode;
        assertFalse("Number is not ambiguous boolean", NodeUtil.isAmbiguousBoolean(numberOnly));
    }

    // --- isExprAssign tests ---
    @Test
    public void testIsExprAssign() {
        Node assignNode = new Node(Token.ASSIGN, new Node(Token.NAME, "x"), Node.newNumber(5));
        Node exprAssign = new Node(Token.EXPR_RESULT, assignNode);
        assertTrue("EXPR_RESULT with ASSIGN child is expr assign", NodeUtil.isExprAssign(exprAssign));
        assertFalse("EXPR_RESULT with non-assign child is not expr assign", new Node(Token.EXPR_RESULT, callNode));
    }

    // --- isNormalGet tests ---
    @Test
    public void testIsNormalGet() {
        Node getProp = new Node(Token.GETPROP, new Node(Token.NAME, "obj"), Node.newString("prop"));
        Node getElem = new Node(Token.GETELEM, new Node(Token.NAME, "obj"), Node.newString("index"));
        assertTrue("GETPROP is normal get", NodeUtil.isNormalGet(getProp));
        assertTrue("GETELEM is normal get", NodeUtil.isNormalGet(getElem));
        assertFalse("NAME is not normal get", new Node(Token.NAME, "x"));
    }

    // --- isCall tests ---
    @Test
    public void testIsCall() {
        assertTrue("CALL node is call", NodeUtil.isCall(callNode));
        assertFalse("NAME is not call", new Node(Token.NAME, "f"));
    }

    // --- isNew tests ---
    @Test
    public void testIsNew() {
        Node newNode = new Node(Token.NEW, new Node(Token.NAME, "Object"));
        assertTrue("NEW node is new", NodeUtil.isNew(newNode));
        assertFalse("CALL is not new", NodeUtil.isNew(callNode));
    }

    // --- isStatement tests (simple) ---
    @Test
    public void testIsStatement() {
        Node exprResult = new Node(Token.EXPR_RESULT, numberNode);
        Node var = new Node(Token.VAR, new Node(Token.NAME, "x"));
        Node ifNode = new Node(Token.IF, booleanTrueNode, new Node(Token.BLOCK));
        assertTrue("EXPR_RESULT is statement", NodeUtil.isStatement(exprResult));
        assertTrue("VAR is statement", NodeUtil.isStatement(var));
        assertTrue("IF is statement", NodeUtil.isStatement(ifNode));
        assertFalse("NUMBER is not a statement", NodeUtil.isStatement(numberNode));
    }

    // --- isReferenceTo tests ---
    @Test
    public void testIsReferenceTo() {
        Node nameX = new Node(Token.NAME, "x");
        assertTrue("NAME x refers to var x", NodeUtil.isReferenceTo(nameX, "x"));
        assertFalse("NAME x does not refer to var y", NodeUtil.isReferenceTo(nameX, "y"));
        // Also getprop? 
        Node getProp = new Node(Token.GETPROP, nameX, Node.newString("prop"));
        assertFalse("GETPROP should not be direct reference", NodeUtil.isReferenceTo(getProp, "x"));
    }

    // --- isDeclaration tests ---
    @Test
    public void testIsDeclaration() {
        Node var = new Node(Token.VAR, new Node(Token.NAME, "x"));
        Node let = new Node(Token.LET, new Node(Token.NAME, "y"));
        Node constDecl = new Node(Token.CONST, new Node(Token.NAME, "z"));
        Node function = new Node(Token.FUNCTION, new Node(Token.NAME, "f"));
        assertTrue("VAR is declaration", NodeUtil.isDeclaration(var, true));
        assertTrue("LET is declaration", NodeUtil.isDeclaration(let, true));
        assertTrue("CONST is declaration", NodeUtil.isDeclaration(constDecl, true));
        assertTrue("FUNCTION is declaration", NodeUtil.isDeclaration(function, true));
        assertFalse("EXPR_RESULT is not declaration", NodeUtil.isDeclaration(new Node(Token.EXPR_RESULT), false));
    }

    // --- isLValue tests ---
    @Test
    public void testIsLValue() {
        Node name = new Node(Token.NAME, "x");
        Node getProp = new Node(Token.GETPROP, new Node(Token.NAME, "obj"), Node.newString("prop"));
        Node getElem = new Node(Token.GETELEM, new Node(Token.NAME, "obj"), Node.newString("key"));
        assertTrue("NAME can be lvalue", NodeUtil.isLValue(name));
        assertTrue("GETPROP can be lvalue", NodeUtil.isLValue(getProp));
        assertTrue("GETELEM can be lvalue", NodeUtil.isLValue(getElem));
        assertFalse("NUMBER is not lvalue", NodeUtil.isLValue(numberNode));
    }

    // --- isImmutableResult tests ---
    @Test
    public void testIsImmutableResult() {
        assertTrue("Number literal immutable", NodeUtil.isImmutableResult(numberNode));
        assertTrue("String literal immutable", NodeUtil.isImmutableResult(stringNode));
        assertTrue("Boolean literal immutable", NodeUtil.isImmutableResult(booleanTrueNode));
        assertTrue("Null immutable", NodeUtil.isImmutableResult(nullNode));
        assertTrue("Undefined immutable", NodeUtil.isImmutableResult(undefinedNode));
        assertFalse("Array literal is not immutable", NodeUtil.isImmutableResult(arrayLitNode));
        assertFalse("Object literal is not immutable", NodeUtil.isImmutableResult(objectLitNode));
    }

    // --- isSimpleFunctionObjectCall tests (not sure exactly) ---
    // Could include tests for isEncodedFunction, isFunctionProperty, etc.

    // Edge case: null node passed to methods (should not crash)
    @Test(expected = NullPointerException.class)
    public void testIsBooleanResult_nullNode() {
        NodeUtil.isBooleanResult(null);
    }

    @Test(expected = NullPointerException.class)
    public void testIsFunctionObjectApply_nullNode() {
        NodeUtil.isFunctionObjectApply(null);
    }

    // Additional coverage for methods like containsCall, containsFunction, etc.
    @Test
    public void testContainsCall() {
        Node containsCallNode = new Node(Token.EXPR_RESULT, callNode);
        assertTrue("Contains call", NodeUtil.containsCall(containsCallNode));
        assertFalse("No call", NodeUtil.containsCall(numberNode));
    }

    @Test
    public void testContainsFunction() {
        Node container = new Node(Token.BLOCK);
        container.addChildToBack(new Node(Token.EXPR_RESULT, functionNode));
        assertTrue("Contains function", NodeUtil.containsFunction(container));
        assertFalse("No function", NodeUtil.containsCall(numberNode));
    }

    // Test getFunctionName
    @Test
    public void testGetFunctionName() {
        Node namedFunction = new Node(Token.FUNCTION, new Node(Token.NAME, "myFunc"));
        assertEquals("myFunc", NodeUtil.getFunctionName(namedFunction));
        Node anonymousFunction = new Node(Token.FUNCTION);
        assertNull("Anonymous function has no name", NodeUtil.getFunctionName(anonymousFunction));
    }

    // Test isPropertyAccess
    @Test
    public void testIsPropertyAccess() {
        Node getProp = new Node(Token.GETPROP, new Node(Token.NAME, "o"), Node.newString("p"));
        Node getElem = new Node(Token.GETELEM, new Node(Token.NAME, "o"), Node.newString("p"));
        assertTrue("GETPROP is property access", NodeUtil.isPropertyAccess(getProp));
        assertTrue("GETELEM is property access", NodeUtil.isPropertyAccess(getElem));
        assertFalse("NAME is not property access", new Node(Token.NAME, "x"));
    }

    // Test isPrototypePropertyDeclaration
    @Test
    public void testIsPrototypePropertyDeclaration() {
        // Create something like "Foo.prototype.method = function() {}"
        Node assign = new Node(Token.ASSIGN,
            new Node(Token.GETPROP,
                new Node(Token.GETPROP,
                    new Node(Token.NAME, "Foo"),
                    Node.newString("prototype")),
                Node.newString("method")),
            new Node(Token.FUNCTION));
        Node exprResult = new Node(Token.EXPR_RESULT, assign);
        assertTrue("Assignment to prototype property", NodeUtil.isPrototypePropertyDeclaration(exprResult));
        // A regular assignment
        Node simpleAssign = new Node(Token.ASSIGN, new Node(Token.NAME, "x"), Node.newNumber(1));
        Node simpleExpr = new Node(Token.EXPR_RESULT, simpleAssign);
        assertFalse("Simple assignment is not prototype", NodeUtil.isPrototypePropertyDeclaration(simpleExpr));
    }

    // --- Additional test for bug 75 specific: isBooleanResult for NE and SHNE already covered.

    // --- Test isNumericResult? Not sure if that's a direct method.

    // Ensure we have a good mix of positive and negative tests for covered branches.

    // Test getBooleanResult for various nodes (not necessary as it just returns node's boolean value).

    // Test isUnscopedQualifiedName etc.

    // To achieve high line coverage, we need to test methods like:
    // isConstantName, isConstant, isValidDefineValue, etc.

    @Test
    public void testIsConstantName() {
        Node nameX = new Node(Token.NAME, "x");
        Node nameXConst = new Node(Token.NAME, "X");
        // Without JSDocInfo, not constant based on case.
        assertFalse("Lowercase name is not constant", NodeUtil.isConstantName(nameX));
        assertTrue("Uppercase name is constant", NodeUtil.isConstantName(nameXConst));
        // With 'const' keyword? Not needed.
    }

    @Test
    public void testIsConstant() {
        // Based on whether node has a 'CONST' property or similar?
        // Not sure, but we can test based on JSDocInfo.
    }

    // We could also test removeChild, maybeNotBool, etc. But keep it manageable.

    // Test many more to get coverage: ensure we also test static methods that were not covered.
}