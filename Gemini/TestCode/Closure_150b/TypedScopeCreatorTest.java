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

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testTypedScopeCreatorInstantiation() {
        TypedScopeCreator creator = new TypedScopeCreator(compiler);
        assertNotNull(creator);
    }

    @Test
    public void testCreateGlobalScope() {
        TypedScopeCreator creator = new TypedScopeCreator(compiler);
        Node root = new Node(Token.BLOCK);
        Scope scope = creator.createScope(root, null);
        assertNotNull(scope);
        assertTrue(scope.isGlobal());
    }

    @Test
    public void testCreateLocalScopeWithFunction() {
        TypedScopeCreator creator = new TypedScopeCreator(compiler);
        Node globalRoot = new Node(Token.BLOCK);
        Scope globalScope = creator.createScope(globalRoot, null);

        // function f(x) { var y = 1; }
        Node nameNode = Node.newString(Token.NAME, "f");
        Node argsNode = new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "x"));
        Node bodyNode = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "y")));
        Node fnNode = new Node(Token.FUNCTION, nameNode, argsNode, bodyNode);
        
        globalRoot.addChildToBack(fnNode);

        Scope localScope = creator.createScope(fnNode, globalScope);
        assertNotNull(localScope);
        assertTrue(localScope.isLocal());
        assertNotNull(localScope.getVar("x"));
        assertNotNull(localScope.getVar("y"));
    }

    @Test
    public void testObjectLiteralWithGetSet() {
        TypedScopeCreator creator = new TypedScopeCreator(compiler);
        Node globalRoot = new Node(Token.BLOCK);
        Scope globalScope = creator.createScope(globalRoot, null);

        // var obj = { get a() { return 1; }, set a(v) {} };
        Node keyGet = Node.newString(Token.STRING, "a");
        keyGet.putBooleanProp(Node.QUOTED_KEY, true);
        Node fnGet = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        keyGet.addChildToBack(fnGet);
        keyGet.setType(Token.GETTER_DEF);

        Node objNode = new Node(Token.OBJECTLIT, keyGet);
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "obj"), objNode);
        globalRoot.addChildToBack(varNode);

        Scope scope = creator.createScope(globalRoot, null);
        assertNotNull(scope);
        assertNotNull(scope.getVar("obj"));
    }

    @Test
    public void testCatchBlockScope() {
        TypedScopeCreator creator = new TypedScopeCreator(compiler);
        Node globalRoot = new Node(Token.BLOCK);
        Scope globalScope = creator.createScope(globalRoot, null);

        // try {} catch (e) { var x = e; }
        Node tryBody = new Node(Token.BLOCK);
        Node catchName = Node.newString(Token.NAME, "e");
        Node catchBody = new Node(Token.BLOCK, new Node(Token.VAR, Node.newString(Token.NAME, "x")));
        Node catchNode = new Node(Token.CATCH, catchName, catchBody);
        Node tryNode = new Node(Token.TRY, tryBody, catchNode);

        globalRoot.addChildToBack(tryNode);

        Scope scope = creator.createScope(globalRoot, null);
        assertNotNull(scope);
    }
}