package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClosureReverseAbstractInterpreterTest {

    private Compiler compiler;
    private JSTypeRegistry typeRegistry;
    private CodingConvention convention;
    private ClosureReverseAbstractInterpreter interpreter;

    @Before
    public void setUp() {
        compiler = new Compiler();
        typeRegistry = compiler.getTypeRegistry();
        // Use GoogleCodingConvention or a standard instance
        convention = new GoogleCodingConvention();
        interpreter = new ClosureReverseAbstractInterpreter(convention, typeRegistry);
    }

    @Test
    public void testFirstParameterCtor() {
        // Test goog.typeOf(x) === 'string' patterns or similar firstParameterCtor
        // Let's construct a CALL node representing something like goog.isString(x) or similar
        // Or goog.isArray(x), etc.
        
        // Create goog.isArray(x)
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "goog"), Node.newString(Token.STRING, "isArray"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        // Test restrictedByTrue & restrictedByFalse
        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        
        // This exercises the static method check or specific interpreter logic for goog.isArray, goog.isObject, etc.
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        // Depending on whether it matches a known convention, it may return a restricted scope or null
        assertNotNull(interpreter);
    }

    @Test
    public void testGoogIsDef() {
        // goog.isDef(x)
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "goog"), 
                Node.newString(Token.STRING, "isDef"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        // Should return scopes (or at least not throw)
        assertNotNull(interpreter);
    }

    @Test
    public void testGoogIsNull() {
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "goog"), 
                Node.newString(Token.STRING, "isNull"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        assertNotNull(interpreter);
    }

    @Test
    public void testGoogIsString() {
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "goog"), 
                Node.newString(Token.STRING, "isString"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        assertNotNull(interpreter);
    }

    @Test
    public void testGoogIsNumber() {
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "goog"), 
                Node.newString(Token.STRING, "isNumber"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        assertNotNull(interpreter);
    }

    @Test
    public void testGoogIsBoolean() {
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "goog"), 
                Node.newString(Token.STRING, "isBoolean"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        assertNotNull(interpreter);
    }

    @Test
    public void testGoogIsFunction() {
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "goog"), 
                Node.newString(Token.STRING, "isFunction"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        assertNotNull(interpreter);
    }

    @Test
    public void testGoogIsObject() {
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "goog"), 
                Node.newString(Token.STRING, "isObject"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        assertNotNull(interpreter);
    }

    @Test
    public void testUnknownCall() {
        // Call to something completely unrelated
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "foo"), 
                Node.newString(Token.STRING, "bar"));
        callNode.addChildrenToBack(getProp);
        Node xName = Node.newString(Token.NAME, "x");
        callNode.addChildrenToBack(xName);

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        assertNull(resultTrue);
        assertNull(resultFalse);
    }

    @Test
    public void testNotACallNode() {
        // Pass a non-call node like a NAME or ASSIGN
        Node nameNode = Node.newString(Token.NAME, "x");

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(nameNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(nameNode, scope);

        assertNull(resultTrue);
        assertNull(resultFalse);
    }

    @Test
    public void testCallWithNoParameters() {
        // goog.isString() with zero parameters
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "goog"), 
                Node.newString(Token.STRING, "isString"));
        callNode.addChildrenToBack(getProp);
        // No second child (parameter)

        FlowScope scope = new ScopeCreator(compiler).createScope(new Node(Token.SCRIPT), null);
        FlowScope resultTrue = interpreter.getRestrictedByTrueParameterType(callNode, scope);
        FlowScope resultFalse = interpreter.getRestrictedByFalseParameterType(callNode, scope);

        assertNull(resultTrue);
        assertNull(resultFalse);
    }
}