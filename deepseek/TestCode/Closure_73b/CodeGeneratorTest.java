package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.SourcePosition;

public class CodeGeneratorTest {

    @Test
    public void testFunctionCallNoExtraSpaceBeforeParenthesis() {
        // Build a CALL node: foo("hello")
        Node callNode = new Node(Token.CALL);
        Node functionName = new Node(Token.NAME, "foo");
        callNode.addChildToFront(functionName);
        Node argument = Node.newString(Token.STRING, "hello");
        callNode.addChildToBack(argument);

        CodeBuilder builder = new CodeBuilder();
        CodeGenerator generator = new CodeGenerator();
        generator.generate(callNode, true, builder);

        String generatedCode = builder.toString();
        // Expect no extra space before the opening parenthesis
        assertFalse("Generated code should not contain a space before '('",
                generatedCode.contains("( "));
        assertEquals("foo(\"hello\")", generatedCode);
    }

    @Test
    public void testFunctionCallMultipleArgumentsNoExtraSpaces() {
        // Build a CALL node: bar(1, "two", true)
        Node callNode = new Node(Token.CALL);
        Node functionName = new Node(Token.NAME, "bar");
        callNode.addChildToFront(functionName);
        callNode.addChildToBack(Node.newNumber(1.0));
        callNode.addChildToBack(Node.newString(Token.STRING, "two"));
        callNode.addChildToBack(new Node(Token.TRUE));

        CodeBuilder builder = new CodeBuilder();
        CodeGenerator generator = new CodeGenerator();
        generator.generate(callNode, true, builder);

        String generatedCode = builder.toString();
        assertFalse("Generated code should not contain a space before '('",
                generatedCode.contains("( "));
        assertEquals("bar(1, \"two\", true)", generatedCode);
    }

    @Test
    public void testFunctionCallNoArguments() {
        // Build a CALL node: f()
        Node callNode = new Node(Token.CALL);
        Node functionName = new Node(Token.NAME, "f");
        callNode.addChildToFront(functionName);

        CodeBuilder builder = new CodeBuilder();
        CodeGenerator generator = new CodeGenerator();
        generator.generate(callNode, true, builder);

        String generatedCode = builder.toString();
        assertEquals("f()", generatedCode);
    }

    @Test
    public void testFunctionCallWithSpecialCharacters() {
        // Build a CALL node: escape("line1\\nline2")
        Node callNode = new Node(Token.CALL);
        Node functionName = new Node(Token.NAME, "escape");
        callNode.addChildToFront(functionName);
        Node arg = Node.newString(Token.STRING, "line1\\nline2");
        callNode.addChildToBack(arg);

        CodeBuilder builder = new CodeBuilder();
        CodeGenerator generator = new CodeGenerator();
        generator.generate(callNode, true, builder);

        String generatedCode = builder.toString();
        // Ensure that the backslash and quotes are properly escaped
        assertEquals("escape(\"line1\\\\nline2\")", generatedCode);
    }

    @Test
    public void testFunctionCallWithNullSourceInfo() {
        // Test that generate runs without NullPointerException when source info is null
        Node callNode = new Node(Token.CALL);
        Node functionName = new Node(Token.NAME, "foo");
        callNode.addChildToFront(functionName);
        callNode.addChildToBack(Node.newString(Token.STRING, "test"));

        CodeBuilder builder = new CodeBuilder();
        CodeGenerator generator = new CodeGenerator();
        // Should not throw an exception even with null source position
        generator.generate(callNode, false, builder);
        String generatedCode = builder.toString();
        assertNotNull("Generated code should not be null", generatedCode);
        assertFalse("Generated code should not be empty", generatedCode.isEmpty());
    }
}