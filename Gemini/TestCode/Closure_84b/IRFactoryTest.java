package com.google.javascript.jscomp.parsing;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.Context;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.ast.AstRoot;
import com.google.javascript.rhino.head.ast.FunctionNode;
import com.google.javascript.rhino.head.ast.Name;
import com.google.javascript.rhino.head.ast.ObjectLiteral;
import com.google.javascript.rhino.head.ast.PropertyGet;
import com.google.javascript.rhino.head.ast.StringLiteral;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class IRFactoryTest {

    private Config config;
    private ErrorReporter errorReporter;

    @Before
    public void setUp() {
        config = new Config(new HashSet<String>(), new HashSet<String>(), Config.LanguageMode.ECMASCRIPT3, false);
        errorReporter = new EmptyErrorReporter();
    }

    @Test
    public void testSimpleVariableDeclaration() {
        String source = "var x = 10;";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testFunctionNodeTransform() {
        String source = "function foo(a, b) { return a + b; }";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testObjectLiteralTransform() {
        String source = "var obj = { 'a': 1, b: 2 };";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testGetPropTransform() {
        String source = "a.b;";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testIncDecTransform() {
        String source = "var x = 0; x++; ++x; x--; --x;";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testThrowStatement() {
        String source = "throw new Error('test');";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testTryCatchFinally() {
        String source = "try { foo(); } catch (e) { bar(); } finally { baz(); }";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testSwitchStatement() {
        String source = "switch(x) { case 1: break; default:; }";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testConditionalExpression() {
        String source = "var x = a ? b : c;";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testLabelAndBreakContinue() {
        String source = "outer: for(;;) { break outer; }";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testRegExpLiteral() {
        String source = "var re = /ab+c/i;";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testArrayLiteral() {
        String source = "var arr = [1, , 3];";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testGetterSetter() {
        String source = "var obj = { get a() { return 1; }, set a(v) { } };";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testStrictModeDirective() {
        String source = "'use strict'; function f() {}";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, config, errorReporter);
        assertNotNull(node);
    }

    @Test
    public void testCommentAttachment() {
        Config configWithComments = new Config(new HashSet<String>(), new HashSet<String>(), Config.LanguageMode.ECMASCRIPT3, true);
        String source = "// comment\nvar x = 1;";
        AstRoot astRoot = parseSource(source);
        Node node = IRFactory.transform(astRoot, configWithComments, errorReporter);
        assertNotNull(node);
    }

    private AstRoot parseSource(String source) {
        CompilerEnvirons environment = new CompilerEnvirons();
        environment.setRecordingComments(true);
        environment.setRecordingLocalJsDocComments(true);
        environment.setLanguageVersion(Context.VERSION_1_5);
        Parser parser = new Parser(environment, errorReporter);
        return parser.parse(source, null, 1);
    }
}