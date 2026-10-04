package com.google.javascript.jscomp.parsing;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.head.ast.AstRoot;
import com.google.javascript.rhino.head.ast.Comment;
import com.google.javascript.rhino.head.ast.Name;
import com.google.javascript.rhino.head.ast.StringLiteral;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IRFactoryTest {

    private Config config;

    @Before
    public void setUp() {
        // Initialize a standard Config instance using the factory method or strict mode flags
        config = Config.getConfig(
                CompilerEnvirons.ideMode ? CompilerEnvirons.Initializer.class : null,
                null,
                false,
                Config.LanguageMode.ECMASCRIPT5,
                false
        );
    }

    @Test
    public void testParseSimpleAstRoot() {
        AstRoot astRoot = new AstRoot();
        astRoot.setSourceName("test.js");

        // Parse using IRFactory transform
        Node node = IRFactory.transform(astRoot, "var x = 1;", config, null);
        assertNotNull(node);
    }

    @Test
    public void testStringLiteralJsDocAttachment() {
        // Specifically targeting JSDoc comment parsing logic on string literals or statements
        // which has historically been a source of AST transformation issues in Closure 122.
        String source = "/** @fileoverview description */\n 'use strict';";
        AstRoot astRoot = new AstRoot();
        astRoot.setSourceName("testDoc.js");
        
        Comment comment = new Comment(0, 32, org.google.javascript.rhino.head.Token.BlockComment, "/** @fileoverview description */");
        astRoot.addComment(comment);

        StringLiteral strLiteral = new StringLiteral();
        strLiteral.setValue("use strict");
        strLiteral.setQuoteChar('\'');
        astRoot.addChild(strLiteral);

        Node node = IRFactory.transform(astRoot, source, config, null);
        assertNotNull(node);
    }

    @Test
    public void testParseWithWarnings() {
        AstRoot astRoot = new AstRoot();
        astRoot.setSourceName("warningTest.js");

        // Triggering parsing with a reporter to capture parse warnings/errors
        EmptyErrorReporter reporter = new EmptyErrorReporter();
        Node node = IRFactory.transform(astRoot, "function f() { return; }", config, reporter);
        assertNotNull(node);
    }

    @Test
    public void testCommentAttachmentBug122() {
        // Closure 122 specifically involves handling of comments attached to nodes,
        // particularly block/jsdoc comments where lines or positions might be mismatched.
        String source = "/* multiline \n * comment \n */\n var a = 1;";
        
        AstRoot astRoot = new AstRoot();
        astRoot.setSourceName("commentTest.js");
        
        Comment comment = new Comment(0, 25, org.google.javascript.rhino.head.Token.BlockComment, "/* multiline \n * comment \n */");
        astRoot.addComment(comment);
        
        Name name = new Name();
        name.setIdentifier("a");
        astRoot.addChild(name);

        Node node = IRFactory.transform(astRoot, source, config, null);
        assertNotNull(node);
    }

    private static class EmptyErrorReporter implements com.google.javascript.jscomp.parsing.ParserRunner.ParseErrorReporter {
        @Override
        public void warning(String message, String sourceName, int line, int lineOffset) {
            // no-op
        }

        @Override
        public void error(String message, String sourceName, int line, int lineOffset) {
            // no-op
        }
    }
}