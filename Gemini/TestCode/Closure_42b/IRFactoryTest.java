package com.google.javascript.jscomp.parsing;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.head.ast.AstRoot;
import com.google.javascript.rhino.head.ast.FunctionNode;
import com.google.javascript.rhino.head.ast.Name;
import com.google.javascript.rhino.head.ast.ObjectProperty;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IRFactoryTest {

    private Config config;

    @Before
    public void setUp() {
        // Initialize a default configuration for parsing/transforming
        config = new Config(
                null, // pushion
                null, // transformation
                true, // languageFeatures
                Config.LanguageMode.ECMASCRIPT5,
                true  // acceptEs6
        );
    }

    @Test
    public void testIRFactoryCreation() {
        assertNotNull(config);
    }

    @Test
    public void testTransformBasicAstRoot() {
        AstRoot astRoot = new AstRoot();
        astRoot.setSourceName("testcode.js");

        // Transform the AST root using IRFactory via ParserRunner or direct if accessible,
        // Since IRFactory package-private methods are accessed via parsing package:
        // Let's invoke transform through standard parsing or direct class instantiation if possible.
        // IRFactory has package-private parse/transform methods or similar depending on the exact version.
        // Let's test basic AST transformation handling.
        
        Node node = IRFactory.transform(astRoot, "var x = 1;", config, null);
        assertNotNull(node);
    }

    @Test
    public void testFunctionNodeTransform() {
        FunctionNode fn = new FunctionNode();
        Name name = new Name();
        name.setIdentifier("testFunc");
        fn.setFunctionName(name);
        
        AstRoot astRoot = new AstRoot();
        astRoot.addChild(fn);
        astRoot.setSourceName("testcode.js");

        Node node = IRFactory.transform(astRoot, "function testFunc() {}", config, null);
        assertNotNull(node);
    }

    @Test
    public void testObjectPropertyTransform() {
        ObjectProperty prop = new ObjectProperty();
        Name key = new Name();
        key.setIdentifier("key");
        prop.setLeft(key);
        
        Name value = new Name();
        value.setIdentifier("val");
        prop.setRight(value);

        AstRoot astRoot = new AstRoot();
        astRoot.addChild(prop);
        astRoot.setSourceName("testcode.js");

        Node node = IRFactory.transform(astRoot, "var obj = {key: val};", config, null);
        assertNotNull(node);
    }

    @Test(expected = NullPointerException.class)
    public void testTransformNullAstRoot() {
        IRFactory.transform(null, "code", config, null);
    }
}