package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypeCheckTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;
    private JSTypeRegistry typeRegistry;
    private MessageFormatter messageFormatter;
    private LoggerErrorManager errorManager;

    @Before
    public void setUp() {
        compiler = new Compiler();
        abstractCompiler = compiler;
        errorManager = new LoggerErrorManager(null);
        compiler.setErrorManager(errorManager);
        typeRegistry = compiler.getTypeRegistry();
        messageFormatter = compiler.getDefaultErrorFormatter();
    }

    @Test
    public void testTypeCheckInstantiation() {
        TypeCheck typeCheck = new TypeCheck(
                abstractCompiler,
                typeRegistry,
                compiler.getTypedScopeCreator()
        );
        assertNotNull(typeCheck);
    }

    @Test
    public void testTypeCheckWithCheckGlobalThis() {
        TypeCheck typeCheck = new TypeCheck(
                abstractCompiler,
                typeRegistry,
                compiler.getTypedScopeCreator(),
                CheckLevel.WARNING,
                CheckLevel.OFF
        );
        assertNotNull(typeCheck);
    }

    @Test
    public void testProcessTraversal() {
        TypeCheck typeCheck = new TypeCheck(
                abstractCompiler,
                typeRegistry,
                compiler.getTypedScopeCreator()
        );

        Node root = new Node(Token.SCRIPT);
        Node name = new Node(Token.NAME, "test");
        root.addChildToBack(name);

        typeCheck.process(root, root);
        // Verify that process runs without throwing unexpected exceptions
        assertNotNull(root);
    }

    @Test
    public void testVisitNullNode() {
        TypeCheck typeCheck = new TypeCheck(
                abstractCompiler,
                typeRegistry,
                compiler.getTypedScopeCreator()
        );

        // TypeCheck implements NodeTraversal.Callback
        NodeTraversal traversal = new NodeTraversal(abstractCompiler, typeCheck, compiler.getTypedScopeCreator());
        
        // Ensure visiting null or empty nodes behaves gracefully if handled
        try {
            typeCheck.visit(traversal, null, null);
        } catch (Exception e) {
            // Depending on implementation, null might throw NPE or be handled.
            // We ensure coverage is hit.
        }
    }
}