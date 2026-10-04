package com.google.javascript.jscomp.parsing;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.head.ast.AstNode;
import com.google.javascript.rhino.head.ast.FunctionNode;
import com.google.javascript.rhino.head.ast.Name;
import com.google.javascript.rhino.head.ast.StringLiteral;
import com.google.javascript.rhino.head.ast.ErrorCollector;
import com.google.javascript.jscomp.mozilla.rhino.tools.ToolErrorReporter;
import com.google.javascript.jscomp.Config;
import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CodingConvention;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class IRFactoryTest {

    private Config config;
    private CodingConvention codingConvention;
    private ErrorCollector errorCollector;

    @Before
    public void setUp() {
        codingConvention = new ClosureCodingConvention();
        Set<String> suppressedWarnings = new HashSet<String>();
        config = new Config(
                suppressedWarnings,
                suppressedWarnings,
                false,
                Config.LanguageMode.ECMASCRIPT5,
                false
        );
        errorCollector = new ErrorCollector();
    }

    @Test
    public void testCreateIRForSimpleFunction() {
        FunctionNode fnNode = new FunctionNode();
        Name nameNode = new Name();
        nameNode.setIdentifier("testFunc");
        fnNode.setFunctionName(nameNode);
        
        StringLiteral bodyNode = new StringLiteral();
        bodyNode.setValue("return 1;");
        fnNode.setBody(bodyNode);

        Node irNode = IRFactory.createIR(fnNode, "testSource", config, CodingConvention.class);
        Assert.assertNotNull(irNode);
    }

    @Test
    public void testStrictModeSetting() {
        Config strictConfig = new Config(
                new HashSet<String>(),
                new HashSet<String>(),
                true,
                Config.LanguageMode.ECMASCRIPT5,
                false
        );
        
        FunctionNode fnNode = new FunctionNode();
        Node irNode = IRFactory.createIR(fnNode, "testSource", strictConfig, CodingConvention.class);
        Assert.assertNotNull(irNode);
    }
}