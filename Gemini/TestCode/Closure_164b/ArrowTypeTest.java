package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class ArrowTypeTest {

    @Test
    public void testArrowTypeBasicAndEquality() {
        SimpleErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        JSType returnType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        Node parameters = new Node(Token.PARAM_LIST);
        
        ArrowType arrow1 = new ArrowType(registry, parameters, returnType);
        ArrowType arrow2 = new ArrowType(registry, parameters, returnType);

        assertSame(returnType, arrow1.getReturnType());
        assertEquals(parameters, arrow1.getParametersNode());
        
        // Test structural equality or general methods if available
        assertNotNull(arrow1.toString());
    }

    @Test
    public void testArrowTypeWithDifferentParameters() {
        SimpleErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        JSType returnType1 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType returnType2 = registry.getNativeType(JSTypeNative.STRING_TYPE);
        
        Node param1 = new Node(Token.PARAM_LIST, Node.newString("a"));
        Node param2 = new Node(Token.PARAM_LIST, Node.newString("b"));

        ArrowType arrow1 = new ArrowType(registry, param1, returnType1);
        ArrowType arrow2 = new ArrowType(registry, param2, returnType2);

        assertNotSame(arrow1.getParametersNode(), arrow2.getParametersNode());
        assertNotSame(arrow1.getReturnType(), arrow2.getReturnType());
    }

    @Test
    public void testNullParametersOrReturnType() {
        SimpleErrorReporter errorReporter = new SimpleErrorReporter();
        JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

        ArrowType arrow = new ArrowType(registry, null, null);
        assertNull(arrow.getParametersNode());
        assertNull(arrow.getReturnType());
    }
}