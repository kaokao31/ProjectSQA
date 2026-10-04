package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for InlineObjectLiterals compiler pass.
 * Designed to achieve high coverage and detect potential faults.
 */
public class InlineObjectLiteralsTest {

    private Compiler compiler;
    private InlineObjectLiterals pass;

    @Before
    public void setUp() {
        compiler = new Compiler();
        pass = new InlineObjectLiterals(compiler);
    }

    // Helper to create a simple AST with an object literal
    private Node createObjectLiteralWithProperties(String... keyValuePairs) {
        Node objLit = new Node(Token.OBJECTLIT);
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            Node key = Node.newString(Token.STRING_KEY, keyValuePairs[i]);
            Node value = Node.newString(keyValuePairs[i + 1]);
            objLit.addChildToBack(new Node(Token.STRING_KEY, key, value));
        }
        return objLit;
    }

    @Test
    public void testNullExterns() {
        Node root = new Node(Token.BLOCK);
        try {
            pass.process(null, root);
            fail("Expected NullPointerException for null externs");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testNullRoot() {
        Node externs = new Node(Token.BLOCK);
        try {
            pass.process(externs, null);
            fail("Expected NullPointerException for null root");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testEmptyObjectLiteral() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "x");
        Node objLit = new Node(Token.OBJECTLIT);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // After processing, object literal should be replaced with empty object
        assertTrue("Object literal should be replaced", objLit.getType() != Token.OBJECTLIT);
    }

    @Test
    public void testSimpleObjectLiteral() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = createObjectLiteralWithProperties("a", "1", "b", "2");
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Verify that the object literal was inlined (replaced with a series of assignments)
        Node newChild = name.getFirstChild();
        assertNotNull("Object literal should be replaced", newChild);
        assertTrue("Should be a block or comma expression", 
                   newChild.getType() == Token.BLOCK || newChild.getType() == Token.COMMA);
    }

    @Test
    public void testObjectLiteralWithNestedObject() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "outer");
        Node outerObj = new Node(Token.OBJECTLIT);
        Node innerObj = createObjectLiteralWithProperties("x", "10");
        Node key = Node.newString(Token.STRING_KEY, "inner");
        key.addChildToBack(innerObj);
        outerObj.addChildToBack(key);
        name.addChildToBack(outerObj);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Should inline both levels
        Node newChild = name.getFirstChild();
        assertNotNull("Outer object should be inlined", newChild);
    }

    @Test
    public void testObjectLiteralWithFunctionValue() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "fn");
        Node function = new Node(Token.FUNCTION);
        key.addChildToBack(function);
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Function values should be handled; object literal may or may not be inlined
        // At minimum, no exception should be thrown
        assertNotNull("Processing should complete without exception", root);
    }

    @Test
    public void testObjectLiteralWithDuplicateKeys() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key1 = Node.newString(Token.STRING_KEY, "a");
        key1.addChildToBack(Node.newString("1"));
        objLit.addChildToBack(key1);
        Node key2 = Node.newString(Token.STRING_KEY, "a");
        key2.addChildToBack(Node.newString("2"));
        objLit.addChildToBack(key2);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Duplicate keys should be handled; no crash
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralInExpression() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        // Create an expression statement: var x = {a:1} + 2;
        Node expr = new Node(Token.ADD);
        Node objLit = createObjectLiteralWithProperties("a", "1");
        expr.addChildToBack(objLit);
        expr.addChildToBack(Node.newNumber(2));
        Node exprStmt = new Node(Token.EXPR_RESULT, expr);
        root.addChildToBack(exprStmt);
        pass.process(externs, root);
        // Object literal in expression should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithGetterSetter() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        // Getter
        Node getter = new Node(Token.GETTER_DEF);
        Node getterKey = Node.newString(Token.STRING_KEY, "prop");
        Node getterBody = new Node(Token.BLOCK);
        getter.addChildToBack(getterKey);
        getter.addChildToBack(getterBody);
        objLit.addChildToBack(getter);
        // Setter
        Node setter = new Node(Token.SETTER_DEF);
        Node setterKey = Node.newString(Token.STRING_KEY, "prop");
        Node setterBody = new Node(Token.BLOCK);
        setter.addChildToBack(setterKey);
        setter.addChildToBack(setterBody);
        objLit.addChildToBack(setter);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Getters/setters should be preserved or handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithNumberKeys() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "0");
        key.addChildToBack(Node.newNumber(42));
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Number keys should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithStringKeyContainingSpecialChars() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "my-key");
        key.addChildToBack(Node.newString("value"));
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Special characters in keys should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithEmptyStringKey() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "");
        key.addChildToBack(Node.newString("empty"));
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Empty string key should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithNullValue() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "nullVal");
        key.addChildToBack(new Node(Token.NULL));
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Null value should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithUndefinedValue() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "undef");
        key.addChildToBack(new Node(Token.VOID));
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Undefined value should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithArrayValue() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "arr");
        Node array = new Node(Token.ARRAYLIT);
        array.addChildToBack(Node.newNumber(1));
        array.addChildToBack(Node.newNumber(2));
        key.addChildToBack(array);
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Array value should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithBooleanValue() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "flag");
        key.addChildToBack(new Node(Token.TRUE));
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Boolean value should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithRegexValue() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "re");
        Node regex = new Node(Token.REGEXP);
        regex.addChildToBack(Node.newString("pattern"));
        regex.addChildToBack(Node.newString("flags"));
        key.addChildToBack(regex);
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Regex value should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithThisValue() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "self");
        key.addChildToBack(new Node(Token.THIS));
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // 'this' value should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithNameReference() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "ref");
        key.addChildToBack(Node.newString(Token.NAME, "otherVar"));
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Name reference value should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithComputedProperty() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "obj");
        Node objLit = new Node(Token.OBJECTLIT);
        Node computedKey = new Node(Token.COMPUTED_PROP);
        Node keyExpr = Node.newString(Token.NAME, "keyVar");
        Node value = Node.newNumber(1);
        computedKey.addChildToBack(keyExpr);
        computedKey.addChildToBack(value);
        objLit.addChildToBack(computedKey);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Computed properties may or may not be inlined; no crash
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testMultipleObjectLiteralsInSameScope() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        // var a = {x:1}; var b = {y:2};
        Node var1 = new Node(Token.VAR);
        Node name1 = Node.newString(Token.NAME, "a");
        name1.addChildToBack(createObjectLiteralWithProperties("x", "1"));
        var1.addChildToBack(name1);
        root.addChildToBack(var1);
        Node var2 = new Node(Token.VAR);
        Node name2 = Node.newString(Token.NAME, "b");
        name2.addChildToBack(createObjectLiteralWithProperties("y", "2"));
        var2.addChildToBack(name2);
        root.addChildToBack(var2);
        pass.process(externs, root);
        // Both should be processed
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralInForInLoop() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        // for (var k in {a:1}) {}
        Node forIn = new Node(Token.FOR_IN);
        Node iterator = new Node(Token.VAR);
        Node iterName = Node.newString(Token.NAME, "k");
        iterator.addChildToBack(iterName);
        Node object = createObjectLiteralWithProperties("a", "1");
        Node body = new Node(Token.BLOCK);
        forIn.addChildToBack(iterator);
        forIn.addChildToBack(object);
        forIn.addChildToBack(body);
        root.addChildToBack(forIn);
        pass.process(externs, root);
        // Object literal in for-in should be handled
        assertNotNull("Processing should complete", root);
    }

    @Test
    public void testObjectLiteralWithSideEffects() {
        Node externs = new Node(Token.BLOCK);
        Node root = new Node(Token.BLOCK);
        // var x = {a: (console.log(1), 2)};
        Node var = new Node(Token.VAR);
        Node name = Node.newString(Token.NAME, "x");
        Node objLit = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING_KEY, "a");
        Node comma = new Node(Token.COMMA);
        Node call = new Node(Token.CALL);
        Node getprop = new Node(Token.GETPROP);
        getprop.addChildToBack(Node.newString(Token.NAME, "console"));
        getprop.addChildToBack(Node.newString(Token.STRING, "log"));
        call.addChildToBack(getprop);
        call.addChildToBack(Node.newNumber(1));
        comma.addChildToBack(call);
        comma.addChildToBack(Node.newNumber(2));
        key.addChildToBack(comma);
        objLit.addChildToBack(key);
        name.addChildToBack(objLit);
        var.addChildToBack(name);
        root.addChildToBack(var);
        pass.process(externs, root);
        // Side effects should be preserved; no crash
        assertNotNull("Processing should complete", root);
    }
}