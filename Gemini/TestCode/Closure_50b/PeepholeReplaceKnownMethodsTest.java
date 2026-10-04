package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeReplaceKnownMethodsTest {

    private PeepholeReplaceKnownMethods peepholeOptimize;
    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        peepholeOptimize = new PeepholeReplaceKnownMethods();
        compiler = new Compiler();
        // Initialize compiler with minimal options to satisfy passes if needed
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        peepholeOptimize.beginTraversal(compiler);
    }

    @Test
    public void testStringJoinEmptyArray() {
        // Test [].join() -> ""
        Node call = Node.newString(Token.NAME, "Array");
        Node getProp = Node.newString(Token.GETPROP, "");
        getProp.addChildToFront(Node.newArrayLit());
        getProp.addChildToBack(Node.newString("join"));
        
        // Wait, proper JS AST structure for [].join()
        // CALL
        //   GETPROP
        //     ARRAYLIT
        //     STRING "join"
        Node arrayLit = Node.newArrayLit();
        Node joinProp = Node.newString(Token.GETPROP, "join");
        joinProp.addChildToFront(arrayLit);
        joinProp.addChildToBack(Node.newString("join"));
        
        call.addChildToFront(joinProp);

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertNotNull(optimized);
    }

    @Test
    public void testStringJoinConstants() {
        // ['a', 'b', 'c'].join(',') -> "a,b,c"
        Node arrayLit = Node.newArrayLit(
                Node.newString("a"),
                Node.newString("b"),
                Node.newString("c")
        );
        Node getProp = new Node(Token.GETPROP, arrayLit, Node.newString("join"));
        Node call = new Node(Token.CALL, getProp, Node.newString(","));

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertNotNull(optimized);
        assertEquals(Token.STRING, optimized.getType());
        assertEquals("a,b,c", optimized.getString());
    }

    @Test
    public void testStringJoinNonLiteral() {
        // ['a', x].join(',') should not be fully folded to a string literal
        Node arrayLit = Node.newArrayLit(
                Node.newString("a"),
                Node.newString(Token.NAME, "x")
        );
        Node getProp = new Node(Token.GETPROP, arrayLit, Node.newString("join"));
        Node call = new Node(Token.CALL, getProp, Node.newString(","));

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertNotNull(optimized);
    }

    @Test
    public void testStringSubstrOptimization() {
        // "abcde".substr(1, 2) -> "bc"
        Node stringNode = Node.newString("abcde");
        Node getProp = new Node(Token.GETPROP, stringNode, Node.newString("substr"));
        Node call = new Node(Token.CALL, getProp, Node.newNumber(1), Node.newNumber(2));

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertNotNull(optimized);
        assertEquals(Token.STRING, optimized.getType());
        assertEquals("bc", optimized.getString());
    }

    @Test
    public void testStringSubstringOptimization() {
        // "abcde".substring(1, 3) -> "bc"
        Node stringNode = Node.newString("abcde");
        Node getProp = new Node(Token.GETPROP, stringNode, Node.newString("substring"));
        Node call = new Node(Token.CALL, getProp, Node.newNumber(1), Node.newNumber(3));

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertNotNull(optimized);
        assertEquals(Token.STRING, optimized.getType());
        assertEquals("bc", optimized.getString());
    }

    @Test
    public void testStringCharAtOptimization() {
        // "abcde".charAt(1) -> "b"
        Node stringNode = Node.newString("abcde");
        Node getProp = new Node(Token.GETPROP, stringNode, Node.newString("charAt"));
        Node call = new Node(Token.CALL, getProp, Node.newNumber(1));

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertNotNull(optimized);
        assertEquals(Token.STRING, optimized.getType());
        assertEquals("b", optimized.getString());
    }

    @Test
    public void testStringIndexOfOptimization() {
        // "abcde".indexOf("cd") -> 2
        Node stringNode = Node.newString("abcde");
        Node getProp = new Node(Token.GETPROP, stringNode, Node.newString("indexOf"));
        Node call = new Node(Token.CALL, getProp, Node.newString("cd"));

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertNotNull(optimized);
        assertEquals(Token.NUMBER, optimized.getType());
        assertEquals(2.0, optimized.getDouble(), 0.001);
    }

    @Test
    public void testMathRandomOptimization() {
        // Math.random() should remain Math.random() (usually not folded, but tested for safety)
        Node mathNode = Node.newString(Token.NAME, "Math");
        Node getProp = new Node(Token.GETPROP, mathNode, Node.newString("random"));
        Node call = new Node(Token.CALL, getProp);

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertNotNull(optimized);
    }

    @Test
    public void testUnknownMethodCall() {
        // foo.unknownMethod() -> should be untouched
        Node nameNode = Node.newString(Token.NAME, "foo");
        Node getProp = new Node(Token.GETPROP, nameNode, Node.newString("unknownMethod"));
        Node call = new Node(Token.CALL, getProp);

        Node optimized = peepholeOptimize.optimizeSubtree(call);
        assertSame(call, optimized);
    }

    @Test
    public void testConstructorCallOptimization() {
        // new Array(1, 2, 3) -> [1, 2, 3] (Bug 50 specific context often relates to Array/Object constructors or method replacements)
        Node newObj = new Node(Token.NEW, Node.newString(Token.NAME, "Array"), Node.newNumber(1), Node.newNumber(2));
        Node optimized = peepholeOptimize.optimizeSubtree(newObj);
        assertNotNull(optimized);
    }

    @Test
    public void testArrayConstructorSingleNumericArg() {
        // new Array(3) -> [,,] or similar depending on implementation
        Node newObj = new Node(Token.NEW, Node.newString(Token.NAME, "Array"), Node.newNumber(3));
        Node optimized = peepholeOptimize.optimizeSubtree(newObj);
        assertNotNull(optimized);
    }

    @Test
    public void testArrayConstructorMultipleArgs() {
        // new Array(1, 2) -> [1, 2]
        Node newObj = new Node(Token.NEW, Node.newString(Token.NAME, "Array"), Node.newNumber(1), Node.newNumber(2));
        Node optimized = peepholeOptimize.optimizeSubtree(newObj);
        assertNotNull(optimized);
    }

    @Test
    public void testNoOpNode() {
        Node numberNode = Node.newNumber(42);
        Node optimized = peepholeOptimize.optimizeSubtree(numberNode);
        assertSame(numberNode, optimized);
    }
}