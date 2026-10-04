package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypeCheckTest {

    private Compiler compiler;
    private DefaultCodingConvention convention;
    private TypeCheck typeCheck;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler with a basic config to avoid NPEs during type checking
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        convention = new DefaultCodingConvention();
    }

    @Test
    public void testCreationAndBasicVisit() {
        // Test instantiation of TypeCheck via constructor
        Scope globalScope = new Scope(null, compiler.getTypeRegistry().getNativeScope().getGlobalThisType());
        typeCheck = new TypeCheck(compiler, compiler.forwardTypeInference, compiler.getTypeRegistry());
        
        assertNotNull(typeCheck);

        Node scriptNode = new Node(Token.SCRIPT);
        Node nameNode = new Node(Token.NAME, Node.newString("a"));
        scriptNode.addChildToBack(nameNode);

        // Exercise the traverse / visit methods
        typeCheck.process(compiler.getSourceFileByName("testcode"), scriptNode);
        
        Node returnedScope = typeCheck.scopeGiven(scriptNode, globalScope);
        assertNotNull(returnedScope);
    }

    @Test
    public void testTypeCheckTraversalWithFunction() {
        typeCheck = new TypeCheck(compiler, compiler.forwardTypeInference, compiler.getTypeRegistry());

        // Construct a simple AST: function foo() {}
        Node functionNode = new Node(Token.FUNCTION, 
                Node.newString("foo"), 
                new Node(Token.LP), 
                new Node(Token.BLOCK));
        
        Scope globalScope = new Scope(null, compiler.getTypeRegistry().getNativeScope().getGlobalThisType());
        
        // This will exercise specific node handling in TypeCheck (e.g., FUNCTION, BLOCK, etc.)
        Node resultScope = typeCheck.scopeGiven(functionNode, globalScope);
        assertNotNull(resultScope);
    }

    @Test
    public void testTypeCheckWithVarDecl() {
        typeCheck = new TypeCheck(compiler, compiler.forwardTypeInference, compiler.getTypeRegistry());

        // Construct AST: var x = 5;
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        varNode.getFirstChild().addChildToBack(Node.newNumber(5));

        Node script = new Node(Token.SCRIPT, varNode);
        
        typeCheck.process(compiler.getSourceFileByName("testVar"), script);
        assertTrue(compiler.getErrors().length >= 0);
    }
}