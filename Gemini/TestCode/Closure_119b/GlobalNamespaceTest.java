package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.jscomp.GlobalNamespace.Name;
import com.google.javascript.jscomp.GlobalNamespace.Ref;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class GlobalNamespaceTest {

    private AbstractCompiler compiler;
    private AbstractCompiler mockingCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler with default options if needed for basic tree processing
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
    }

    @Test
    public void testEmptyGlobalNamespace() {
        Node root = new Node(Token.BLOCK);
        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        
        assertEquals(0, namespace.getNameForest().size());
        assertNull(namespace.getGlobalTable());
        assertNull(namespace.getName("nonExistent"));
    }

    @Test
    public void testSimpleVariableDeclaration() {
        // var x = 1;
        Node nameNode = Node.newString(Token.NAME, "x");
        Node numberNode = Node.newNumber(1.0);
        nameNode.addChildToFront(numberNode);
        
        Node varNode = new Node(Token.VAR, nameNode);
        Node root = new Node(Token.SCRIPT, varNode);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        
        Name xName = namespace.getName("x");
        assertNotNull(xName);
        assertEquals("x", xName.name);
        assertEquals(Name.Type.OTHER, xName.type);
    }

    @Test
    public void testFunctionDeclaration() {
        // function foo() {}
        Node funcNameNode = Node.newString(Token.NAME, "foo");
        Node paramList = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        Node funcNode = new Node(Token.FUNCTION, funcNameNode, paramList, body);
        Node root = new Node(Token.SCRIPT, funcNode);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        
        Name fooName = namespace.getName("foo");
        assertNotNull(fooName);
        assertEquals("foo", fooName.name);
    }

    @Test
    public void testObjectLiteralAndAssignment() {
        // var ns = {}; ns.foo = 1;
        Node nsName1 = Node.newString(Token.NAME, "ns");
        Node objLit = new Node(Token.OBJECTLIT);
        nsName1.addChildToFront(objLit);
        Node varNode = new Node(Token.VAR, nsName1);

        Node getProp = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "ns"), 
                Node.newString(Token.STRING, "foo"));
        Node assign = new Node(Token.ASSIGN, getProp, Node.newNumber(10.0));
        Node expr = new Node(Token.EXPR_RESULT, assign);

        Node root = newNode(Token.SCRIPT, varNode, expr);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        
        Name ns = namespace.getName("ns");
        assertNotNull(ns);
        
        Name foo = namespace.getName("ns.foo");
        assertNotNull(foo);
    }

    @Test
    public void testNamespaceScanWithExterns() {
        Node root = new Node(Token.BLOCK);
        Node externs = new Node(Token.BLOCK);
        
        GlobalNamespace namespace = new GlobalNamespace(compiler, externs, root);
        assertNotNull(namespace);
    }

    @Test
    public void testNameGetTypeAndProperties() {
        Node nameNode = Node.newString(Token.NAME, "a");
        Node varNode = new Node(Token.VAR, nameNode);
        Node root = new Node(Token.SCRIPT, varNode);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        Name a = namespace.getName("a");
        
        assertNotNull(a.getDeclarations());
        assertFalse(a.isDelete());
        assertFalse(a.isConstant());
        assertFalse(a.inExterns());
    }

    @Test
    public void testGlobalNamespaceIterator() {
        Node nameNode = Node.newString(Token.NAME, "testVar");
        Node varNode = new Node(Token.VAR, nameNode);
        Node root = new Node(Token.SCRIPT, varNode);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        Iterator<Name> it = namespace.iterator();
        
        boolean found = false;
        while (it.hasNext()) {
            Name n = it.next();
            if ("testVar".equals(n.name)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testNestedGlobalAssignments() {
        // a = { b: { c: 1 } };
        Node cVal = Node.newNumber(1.0);
        Node bObj = new Node(Token.OBJECTLIT, Node.newString(Token.STRING, "c"), cVal);
        Node aObj = new Node(Token.OBJECTLIT, Node.newString(Token.STRING, "b"), bObj);
        
        Node nameA = Node.newString(Token.NAME, "a");
        nameA.addChildToFront(aObj);
        Node varNode = new Node(Token.VAR, nameA);
        Node root = new Node(Token.SCRIPT, varNode);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        
        assertNotNull(namespace.getName("a"));
        assertNotNull(namespace.getName("a.b"));
        assertNotNull(namespace.getName("a.b.c"));
    }

    @Test
    public void testGetSlotAndScan() {
        Node root = new Node(Token.SCRIPT);
        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        
        // Exercise internal structures if possible or robust checks
        assertNull(namespace.getSlot("nonExistentSlot"));
    }

    @Test
    public void testRefTypes() {
        Node nameNode = Node.newString(Token.NAME, "x");
        Node varNode = new Node(Token.VAR, nameNode);
        Node root = new Node(Token.SCRIPT, varNode);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        Name x = namespace.getName("x");
        if (x != null && !x.getDeclarations().isEmpty()) {
            Ref ref = x.getDeclarations().get(0);
            assertNotNull(ref.getNode());
            assertNotNull(ref.getSourceName());
        }
    }

    private Node newNode(int type, Node... children) {
        Node node = new Node(type);
        for (Node child : children) {
            node.addChildToBack(child);
        }
        return node;
    }
}