package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class MaybeReachingVariableUseTest {

    @Test
    public void testConstructorAndBasicMethods() {
        // Create a minimal control flow graph using a simple script node
        Node scriptNode = new Node(Token.SCRIPT);
        Node nameNode = new Node(Token.NAME, "x");
        scriptNode.addChildToBack(nameNode);

        ControlFlowGraph<Node> cfg = new ControlFlowGraph<>(scriptNode, true, true);
        
        AbstractCompiler compiler = new Compiler();
        MaybeReachingVariableUse maybeReaching = new MaybeReachingVariableUse(cfg, null, compiler);

        // Test initial entry/exit states or basic dataflow API
        // Depending on AbstractDataFlowAnalysis structure, we can exercise entry/exit
        dataflowSanityCheck(maybeReaching, cfg);
    }

    @Test
    public void testEmptyCfg() {
        Node scriptNode = new Node(Token.SCRIPT);
        ControlFlowGraph<Node> cfg = new ControlFlowGraph<>(scriptNode, false, false);
        AbstractCompiler compiler = new Compiler();

        MaybeReachingVariableUse maybeReaching = new MaybeReachingVariableUse(cfg, null, compiler);
        assertNotNull(maybeReaching);
    }

    @Test
    public void testAnalyzeWithVariableAssignment() {
        // Create AST: 
        // SCRIPT
        //   EXPR_RESULT
        //     ASSIGN
        //       NAME x
        //       NUMBER 1
        Node scriptNode = new Node(Token.SCRIPT);
        Node expr = new Node(Token.EXPR_RESULT);
        Node assign = new Node(Token.ASSIGN);
        Node name = new Node(Token.NAME, "x");
        Node num = new Node(Token.NUMBER, 1.0);

        assign.addChildToBack(name);
        assign.addChildToBack(num);
        expr.addChildToBack(assign);
        scriptNode.addChildToBack(expr);

        ControlFlowGraph<Node> cfg = new ControlFlowGraph<>(scriptNode, true, true);
        AbstractCompiler compiler = new Compiler();

        MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, null, compiler);
        analysis.analyze();

        // Verify lattice creation and flow equations handling
        for (GraphNode<Node, ControlFlowGraph.Branch> node : cfg.getGraphNodes()) {
            FlowState<MaybeReachingVariableUse.MustDef> entryState = analysis.getEntryState(node.getValue());
            FlowState<MaybeReachingVariableUse.MustDef> exitState = analysis.getExitState(node.getValue());
            
            // Check that states are populated correctly without throwing exceptions
            if (entryState != null) {
                assertNotNull(entryState.getImplicit());
            }
            if (exitState != null) {
                assertNotNull(exitState.getImplicit());
            }
        }
    }

    @Test
    public void testFunctionWithParameters() {
        // FUNCTION
        //   NAME f
        //   PARAM_LIST
        //     NAME x
        //   BLOCK
        //     RETURN
        //       NAME x
        Node function = new Node(Token.FUNCTION);
        Node name = new Node(Token.NAME, "f");
        Node paramList = new Node(Token.PARAM_LIST);
        Node paramX = new Node(Token.NAME, "x");
        paramList.addChildToBack(paramX);
        
        Node block = new Node(Token.BLOCK);
        Node ret = new Node(Token.RETURN);
        Node useX = new Node(Token.NAME, "x");
        ret.addChildToBack(useX);
        block.addChildToBack(ret);

        function.addChildToBack(name);
        function.addChildToBack(paramList);
        function.addChildToBack(block);

        ControlFlowGraph<Node> cfg = new ControlFlowGraph<>(function, true, true);
        AbstractCompiler compiler = new Compiler();

        MaybeReachingVariableUse analysis = new MaybeReachingVariableUse(cfg, null, compiler);
        analysis.analyze();

        assertNotNull(analysis);
    }

    private void dataflowSanityCheck(MaybeReachingVariableUse analysis, ControlFlowGraph<Node> cfg) {
        analysis.analyze();
        for (GraphNode<Node, ControlFlowGraph.Branch> node : cfg.getGraphNodes()) {
            assertNotNull(analysis.getEntryState(node.getValue()));
            assertNotNull(analysis.getExitState(node.getValue()));
        }
    }
}