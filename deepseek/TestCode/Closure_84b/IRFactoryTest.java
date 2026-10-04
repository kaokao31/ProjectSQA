package com.google.javascript.jscomp.parsing;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class IRFactoryTest {
    private Object irFactory;
    private Method transformMethod;

    @Before
    public void setUp() throws Exception {
        Class<?> clazz = Class.forName("com.google.javascript.jscomp.parsing.IRFactory");
        irFactory = clazz.getDeclaredConstructor().newInstance();
        transformMethod = clazz.getMethod("transform", Node.class);
    }

    @Test
    public void testTransformNullInput() throws Exception {
        try {
            transformMethod.invoke(irFactory, new Object[]{null});
            fail("Expected exception on null input");
        } catch (InvocationTargetException e) {
            // expected
        }
    }

    @Test
    public void testTransformScript() throws Exception {
        Node script = new Node(1); // SCRIPT
        Object result = transformMethod.invoke(irFactory, script);
        assertNotNull("Transform should return a non-null result for a script", result);
    }

    @Test
    public void testTransformScriptWithFunction() throws Exception {
        Node script = new Node(1);
        Node function = new Node(2); // FUNCTION
        script.addChildToBack(function);
        Object result = transformMethod.invoke(irFactory, script);
        assertNotNull("Transform should handle scripts containing functions", result);
    }

    @Test
    public void testTransformScriptWithMultipleChildren() throws Exception {
        Node script = new Node(1);
        script.addChildToBack(new Node(3)); // NAME
        script.addChildToBack(new Node(4)); // NUMBER
        Object result = transformMethod.invoke(irFactory, script);
        assertNotNull("Transform should handle scripts with multiple children", result);
    }
}