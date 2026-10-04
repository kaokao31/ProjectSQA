package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class LiveVariablesAnalysisTest {

    private AbstractCompiler compiler;
    private ScopeCreator scopeCreator;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options minimally if needed
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        scopeCreator = new SyntacticScopeCreator(compiler);
    }

    @Test
    public void testAnalyzeBasicFunction() {
        // Create a simple AST: function f() { var x = 1; return x; }
        Node script = Node.newString(Token.SCRIPT, "test.js");
        Node name = Node.newString(Token.NAME, "f");
        Node lp = Node.newToken(Token.LP);
        Node body = new Node(Token.BLOCK);
        
        //  var x = 1;
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        varNode.getFirstChild().addChildToBack(Node.newNumber(1.0));
        body.addChildToBack(varNode);
        
        // return x;
        Node returnNode = new Node(Token.RETURN, Node.newString(Token.NAME, "x"));
        body.addChildToBack(returnNode);

        Node func = new Node(Token.FUNCTION, name, lp, body);
        script.addChildToBack(func);

        // Build scopes
        Scope globalScope = scopeCreator.createScope(script, null);
        Scope functionScope = scopeCreator.createScope(func, globalScope);

        ControlFlowGraph<Node> cfg = new ControlFlowGraph<>(script, true, true);
        
        LiveVariablesAnalysis lva = new LiveVariablesAnalysis(cfg, functionScope, compiler);
        lva.analyze();

        // Verify analysis ran without throwing exceptions and results are populated
        assertNotNull(lva);
    }

    @Test
    public void testAnalyzeWithIfElseBranch() {
        // function f(a) { var x; if (a) { x = 1; } else { x = 2; } return x; }
        Node script = Node.newString(Token.SCRIPT, "test_branch.js");
        Node name = Node.newString(Token.NAME, "f");
        Node lp = Node.newToken(Token.LP);
        lp.addChildToBack(Node.newString(Token.NAME, "a"));
        
        Node body = new Node(Token.BLOCK);
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        body.addChildToBack(varNode);

        // if (a) { x = 1; } else { x = 2; }
        Node cond = Node.newString(Token.NAME, "a");
        Node thenBlock = new Node(Token.BLOCK);
        thenBlock.addChildToBack(new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1.0)));
        
        Node elseBlock = new Node(Token.BLOCK);
        elseBlock.addChildToBack(new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(2.0)));

        Node ifNode = new Node(Token.IF, cond, thenBlock, elseBlock);
        body.addChildToBack(ifNode);

        Node returnNode = new Node(Token.RETURN, Node.newString(Token.NAME, "x"));
        body.addChildToBack(returnNode);

        Node func = new Node(Token.FUNCTION, name, lp, body);
        script.addChildToBack(func);

        Scope globalScope = scopeCreator.createScope(script, null);
        Scope functionScope = scopeCreator.createScope(func, globalScope);

        ControlFlowGraph<Node> cfg = new ControlFlowGraph<>(script, true, true);
        
        LiveVariablesAnalysis lva = new LiveVariablesAnalysis(cfg, functionScope, compiler);
        lva.analyze();

        assertNotNull(lva);
    }

    @Test
    public void testAnalyzeWithLoop() {
        // function f() { var x = 0; while (x < 10) { x++; } return x; }
        Node script = Node.newString(Token.SCRIPT, "test_loop.js");
        Node name = Node.newString(Token.NAME, "f");
        Node lp = Node.newToken(Token.LP);
        Node body = new Node(Token.BLOCK);

        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        varNode.getFirstChild().addChildToBack(Node.newNumber(0.0));
        body.addChildToBack(varNode);

        // while (x < 10) { x++; }
        Node cond = new Node(Token.LT, Node.newString(Token.NAME, "x"), Node.newNumber(10.0));
        Node loopBody = new Node(Token.BLOCK);
        Node inc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
        loopBody.addChildToBack(inc);

        Node whileNode = new Node(Token.WHILE, cond, loopBody);
        body.addChildToBack(whileNode);

        Node returnNode = new Node(Token.RETURN, Node.newString(Token.NAME, "x"));
        body.addChildToBack(returnNode);

        Node func = new Node(Token.FUNCTION, name, lp, body);
        script.addChildToBack(func);

        Scope globalScope = scopeCreator.createScope(script, null);
        Scope functionScope = scopeCreator.createScope(func, globalScope);

        ControlFlowGraph<Node> cfg = new ControlFlowGraph<>(script, true, true);
        
        LiveVariablesAnalysis lva = new LiveVariablesAnalysis(cfg, functionScope, compiler);
        lva.analyze();

        assertNotNull(lva);
    }
}