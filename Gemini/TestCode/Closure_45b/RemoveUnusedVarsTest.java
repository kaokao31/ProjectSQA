package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RemoveUnusedVarsTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testRemoveUnusedVarsBasic() {
        // Simple test to exercise the RemoveUnusedVars pass initialization and execution
        CompilerOptions options = new CompilerOptions();
        options.removeUnusedLocalVars = true;
        compiler.initOptions(options);

        Node root = compiler.parseSyntheticCode("var x = 1; var y = 2; print(y);");
        assertNotNull(root);

        AbstractCompiler astValidator = compiler;
        RemoveUnusedVars pass = new RemoveUnusedVars(astValidator, true, true, true);
        pass.process(compiler.getRoot(), root);
    }

    @Test
    public void testRemoveUnusedVarsWithAssignedGetProp() {
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        // Code that triggers properties / assigned values checking logic
        Node root = compiler.parseSyntheticCode("var ns = {}; ns.foo = 1;");
        assertNotNull(root);

        RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
        pass.process(compiler.getRoot(), root);
    }
}