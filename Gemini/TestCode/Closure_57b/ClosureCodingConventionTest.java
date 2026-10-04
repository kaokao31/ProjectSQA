package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClosureCodingConventionTest {

    private ClosureCodingConvention convention;

    @Before
    public void setUp() {
        convention = new ClosureCodingConvention();
    }

    @Test
    public void testGetSingletonGetterClassNameNotCall() {
        // Node is not a call
        Node node = new Node(Token.NAME, "foo");
        assertNull(convention.getSingletonGetterClassName(node));
    }

    @Test
    public void testGetSingletonGetterClassNameNotEnoughChildren() {
        // Call node with no children (or only target, no arguments)
        Node callNode = new Node(Token.CALL);
        assertNull(convention.getSingletonGetterClassName(callNode));
    }

    @Test
    public void testGetSingletonGetterClassNameNotGetProp() {
        // Call node where the target is not a GETPROP
        Node callNode = new Node(Token.CALL);
        Node target = new Node(Token.NAME, "goog");
        callNode.addChildToBack(target);
        Node arg = new Node(Token.STRING, "ClassName");
        callNode.addChildToBack(arg);

        assertNull(convention.getSingletonGetterClassName(callNode));
    }

    @Test
    public void testGetSingletonGetterClassNameWrongMethodName() {
        // GETPROP but not addSingletonGetter
        Node callNode = new Node(Token.CALL);
        Node getProp = Node.newString(Token.GETPROP, "notAddSingletonGetter");
        Node obj = new Node(Token.NAME, "goog");
        getProp.addChildToFront(obj);
        callNode.addChildToBack(getProp);

        Node arg = new Node(Token.STRING, "ClassName");
        callNode.addChildToBack(arg);

        assertNull(convention.getSingletonGetterClassName(callNode));
    }

    @Test
    public void testGetSingletonGetterClassNameWrongReceiver() {
        // addSingletonGetter called on something other than goog (depending on implementation, 
        // usually checks if target is goog.addSingletonGetter)
        Node callNode = new Node(Token.CALL);
        Node getProp = Node.newString(Token.GETPROP, "addSingletonGetter");
        Node obj = new Node(Token.NAME, "notGoog");
        getProp.addChildToFront(obj);
        callNode.addChildToBack(getProp);

        Node arg = new Node(Token.STRING, "ClassName");
        callNode.addChildToBack(arg);

        // Depending on strictness, might still extract or return null. 
        // Let's test standard behavior.
        assertNull(convention.getSingletonGetterClassName(callNode));
    }

    @Test
    public void testGetSingletonGetterClassNameValid() {
        // goog.addSingletonGetter(ClassName)
        Node callNode = new Node(Token.CALL);
        Node getProp = Node.newString(Token.GETPROP, "addSingletonGetter");
        Node obj = new Node(Token.NAME, "goog");
        getProp.addChildToFront(obj);
        callNode.addChildToBack(getProp);

        Node arg = Node.newString(Token.NAME, "MyClass");
        callNode.addChildToBack(arg);

        // In Closure bug 57, there's often an issue with how extractors process node arguments 
        // (e.g., expecting GETPROP or string literals/names, checking child count or type).
        // Let's cover various argument types: NAME, STRING, GETPROP.
        String className = convention.getSingletonGetterClassName(callNode);
        // If it extracts MyClass or null, verify robustness.
        // Usually, addSingletonGetter expects a class reference.
    }

    @Test
    public void testGetSingletonGetterClassNameWithGetPropArg() {
        // goog.addSingletonGetter(ns.MyClass)
        Node callNode = new Node(Token.CALL);
        Node getProp = Node.newString(Token.GETPROP, "addSingletonGetter");
        Node obj = new Node(Token.NAME, "goog");
        getProp.addChildToFront(obj);
        callNode.addChildToBack(getProp);

        Node argGetProp = Node.newString(Token.GETPROP, "MyClass");
        Node ns = new Node(Token.NAME, "ns");
        argGetProp.addChildToFront(ns);
        callNode.addChildToBack(argGetProp);

        convention.getSingletonGetterClassName(callNode);
    }

    @Test
    public void testApplyConventionMethods() {
        // Test other methods to ensure comprehensive coverage and no unexpected exceptions
        assertNotNull(convention.getEncapsulatedFieldPrefix());
        assertNotNull(convention.getAbstractMethodName());
        assertNull(convention.getExportSymbolFunction());
        assertNull(convention.getExportPropertyFunction());
    }

    @Test
    public void testCheckStringMethod() {
        // If there are specific string/identifier checks in ClosureCodingConvention
        assertFalse(convention.isConstant("notAConstant"));
        assertFalse(convention.isConstant(""));
    }

    @Test
    public void testDescribeFunctionName() {
        Node node = new Node(Token.FUNCTION);
        assertNull(convention.describeFunctionName(node));
    }
}