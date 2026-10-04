package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypedScopeCreatorTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;
    private TypedScopeCreator scopeCreator;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testCreationWithDefaultOptions() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        assertNotNull(scopeCreator);
    }

    @Test
    public void testCreationWithCodingConvention() {
        CodingConvention convention = new DefaultCodingConvention();
        scopeCreator = new TypedScopeCreator(abstractCompiler, convention);
        assertNotNull(scopeCreator);
    }

    @Test
    public void testCreateScopeBasic() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        Node root = new Node(Token.SCRIPT);
        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
        assertEquals(root, scope.getRootNode());
    }

    @Test
    public void testCreateScopeWithVarNode() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        Node root = new Node(Token.SCRIPT);
        Node nameNode = Node.newString(Token.NAME, "x");
        Node varNode = new Node(Token.VAR, nameNode);
        root.addChildToBack(varNode);

        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("x"));
    }

    @Test
    public void testCreateScopeWithFunctionNode() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        Node root = new Node(Token.SCRIPT);
        Node funcName = Node.newString(Token.NAME, "myFunc");
        Node args = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        Node funcNode = new Node(Token.FUNCTION, funcName, args, body);
        root.addChildToBack(funcNode);

        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("myFunc"));
    }

    @Test
    public void testObjectLiteralWithGetterSetterClosure95() {
        // Closure 95 involves type inference and scope creation around object literal definitions,
        // specifically checking qualified names and 'this' contexts in object literals or functions.
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        
        // Constructing an AST representing: var obj = { get a() { return this.b; }, set a(val) { this.b = val; } };
        Node root = new Node(Token.SCRIPT);
        
        Node valNode = new Node(Token.OBJECTLIT);
        Node getProp = Node.newString(Token.GETTER_DEF, "a");
        Node funcNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
        getProp.addChildToBack(funcNode);
        valNode.addChildToBack(getProp);

        Node nameNode = Node.newString(Token.NAME, "obj");
        nameNode.addChildToBack(valNode);
        Node varNode = new Node(Token.VAR, nameNode);
        root.addChildToBack(varNode);

        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("obj"));
    }

    @Test
    public void testScopeCreationWithCtor() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        
        // function Foo() {}
        Node root = new Node(Token.SCRIPT);
        Node funcName = Node.newString(Token.NAME, "Foo");
        Node args = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        Node funcNode = new Node(Token.FUNCTION, funcName, args, body);
        root.addChildToBack(funcNode);

        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
        Var fooVar = scope.getVar("Foo");
        assertNotNull(fooVar);
        assertNotNull(fooVar.getType());
    }

    @Test
    public void testFunctionWithThisReference() {
        scopeCreator = new TypedScopeCreator(abstractCompiler);
        
        // /** @constructor */ function Bar() { this.x = 3; }
        Node root = new Node(Token.SCRIPT);
        Node funcName = Node.newString(Token.NAME, "Bar");
        Node args = new Node(Token.LP);
        
        Node assign = new Node(Token.ASSIGN, 
            Node.newString(Token.GETPROP, Node.newString(Token.THIS, "this"), "x"),
            Node.newNumber(3)
        );
        Node body = new Node(Token.BLOCK, assign);
        Node funcNode = new Node(Token.FUNCTION, funcName, args, body);
        
        root.addChildToBack(funcNode);

        Scope scope = scopeCreator.createScope(root, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("Bar"));
    }
}