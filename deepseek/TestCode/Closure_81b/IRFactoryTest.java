package com.google.javascript.jscomp.parsing;

import com.google.javascript.jscomp.parsing.Config;
import com.google.javascript.jscomp.parsing.IRFactory;
import com.google.javascript.jscomp.parsing.ParserRunner;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.parsing.parser.util.SourcePosition;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IRFactoryTest {

    private Config config;
    private IRFactory irFactory;

    @Before
    public void setUp() {
        config = Config.getDefault();
        irFactory = new IRFactory(config);
    }

    @Test
    public void testGoogGetMsgWithOneArg() {
        String source = "goog.getMsg('hello');";
        Node ast = parse(source);
        Node transformed = irFactory.transformTree(ast);
        Node exprResult = transformed.getFirstChild();
        assertNotNull(exprResult);
        Node call = exprResult.getFirstChild();
        assertNotNull(call);
        assertEquals("Expected CALL node for goog.getMsg", Token.CALL, call.getType());
        Node function = call.getFirstChild();
        assertTrue("Function should be a qualified name", function.isQualifiedName());
        assertEquals("goog.getMsg", function.getQualifiedName());
    }

    @Test
    public void testGoogGetMsgWithTwoArgs() {
        String source = "goog.getMsg('hello', {description: 'world'});";
        Node ast = parse(source);
        Node transformed = irFactory.transformTree(ast);
        Node exprResult = transformed.getFirstChild();
        Node call = exprResult.getFirstChild();
        assertEquals(Token.CALL, call.getType());
        Node secondArg = call.getLastChild();
        assertEquals(Token.OBJECTLIT, secondArg.getType());
        Node prop = secondArg.getFirstChild();
        assertEquals("description", prop.getString());
    }

    @Test
    public void testGoogGetMsgWithThreeArgs() {
        String source = "goog.getMsg('hello', {description: 'world'}, {html: true});";
        Node ast = parse(source);
        Node transformed = irFactory.transformTree(ast);
        Node exprResult = transformed.getFirstChild();
        Node call = exprResult.getFirstChild();
        assertEquals(Token.CALL, call.getType());
        // Verify three arguments
        int argCount = 0;
        for (Node arg = call.getFirstChild().getNext(); arg != null; arg = arg.getNext()) {
            argCount++;
        }
        assertEquals(3, argCount);
    }

    @Test
    public void testRegularFunctionCall() {
        String source = "foo();";
        Node ast = parse(source);
        Node transformed = irFactory.transformTree(ast);
        Node exprResult = transformed.getFirstChild();
        Node call = exprResult.getFirstChild();
        assertEquals(Token.CALL, call.getType());
    }

    @Test
    public void testConstructorCall() {
        String source = "new Foo();";
        Node ast = parse(source);
        Node transformed = irFactory.transformTree(ast);
        Node exprResult = transformed.getFirstChild();
        Node newNode = exprResult.getFirstChild();
        assertEquals(Token.NEW, newNode.getType());
    }

    @Test
    public void testGoogGetMsgNotTreatedAsNew() {
        // This is the core bug: goog.getMsg should not become a NEW node
        String source = "goog.getMsg('test');";
        Node ast = parse(source);
        Node transformed = irFactory.transformTree(ast);
        Node exprResult = transformed.getFirstChild();
        Node call = exprResult.getFirstChild();
        assertNotEquals("goog.getMsg should not be a NEW node", Token.NEW, call.getType());
        assertEquals(Token.CALL, call.getType());
    }

    private Node parse(String source) {
        try {
            SourceFile file = SourceFile.fromCode("test.js", source);
            return ParserRunner.parse(file, config, null);
        } catch (Exception e) {
            throw new RuntimeException("Parsing failed", e);
        }
    }
}