package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.GlobalNamespace;
import com.google.javascript.jscomp.GlobalNamespace.Name;
import com.google.javascript.jscomp.GlobalNamespace.Ref;

import java.util.List;
import java.util.Set;

public class GlobalNamespaceTest {

    private Compiler compiler;
    private GlobalNamespace globalNamespace;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        // Initialize the compiler with default options
        compiler.initOptions(options);
    }

    @Test
    public void testEmptySource() {
        // Test with empty source code
        String source = "";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        assertNotNull(globalNamespace);
        assertTrue(globalNamespace.getNames().isEmpty());
    }

    @Test
    public void testSimpleGlobalVariable() {
        String source = "var x = 1;";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(1, names.size());
        Name name = names.get(0);
        assertEquals("x", name.getFullName());
        assertTrue(name.isDeclaredType());
        assertFalse(name.isFunctionNamespace());
    }

    @Test
    public void testGlobalFunction() {
        String source = "function foo() {}";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(1, names.size());
        Name name = names.get(0);
        assertEquals("foo", name.getFullName());
        assertTrue(name.isFunctionNamespace());
        assertTrue(name.isDeclaredType());
    }

    @Test
    public void testNestedGlobalScopes() {
        String source = "var a = {}; a.b = 1;";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
        Name a = names.get(0);
        Name ab = names.get(1);
        assertEquals("a", a.getFullName());
        assertEquals("a.b", ab.getFullName());
    }

    @Test
    public void testMultipleGlobalNames() {
        String source = "var x = 1; var y = 2; function z() {}";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(3, names.size());
    }

    @Test
    public void testReferenceCounting() {
        String source = "var x = 1; x = 2; var y = x;";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        Name x = findName(globalNamespace.getNames(), "x");
        assertNotNull(x);
        List<Ref> refs = x.getRefs();
        assertEquals(3, refs.size()); // declaration, assign, read
    }

    @Test
    public void testNamespaceAliasing() {
        String source = "var goog = {}; goog.ui = {};";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
        Name goog = findName(names, "goog");
        assertNotNull(goog);
        assertTrue(goog.isNamespaceObject());
    }

    @Test
    public void testPropertyAccessOnLiteral() {
        String source = "var a = {b: 1};";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
        Name a = findName(names, "a");
        assertNotNull(a);
        assertTrue(a.isDeclaredType());
    }

    @Test
    public void testFunctionWithPrototype() {
        String source = "function Foo() {} Foo.prototype.bar = function() {};";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(3, names.size());
        Name foo = findName(names, "Foo");
        assertNotNull(foo);
        assertTrue(foo.isFunctionNamespace());
        Name fooPrototypeBar = findName(names, "Foo.prototype.bar");
        assertNotNull(fooPrototypeBar);
        assertTrue(fooPrototypeBar.isFunctionNamespace());
    }

    @Test
    public void testGlobalNamesInSeparateFiles() {
        String source1 = "var a = 1;";
        String source2 = "var b = 2;";
        Node jsRoot1 = compiler.parseCode(source1);
        Node jsRoot2 = compiler.parseCode(source2);
        // Create a synthetic script node
        Node script1 = new Node(Token.SCRIPT);
        script1.addChildToBack(jsRoot1);
        Node script2 = new Node(Token.SCRIPT);
        script2.addChildToBack(jsRoot2);
        Node root = new Node(Token.BLOCK);
        root.addChildToBack(script1);
        root.addChildToBack(script2);
        globalNamespace = new GlobalNamespace(compiler, root);
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
    }

    @Test
    public void testExternsWithGlobalNames() {
        String externs = "var window;";
        String source = "var x = window;";
        Compiler compilerWithExterns = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compilerWithExterns.initOptions(options);
        Node externNode = compilerWithExterns.parseCode(externs);
        Node sourceNode = compilerWithExterns.parseCode(source);
        Node scriptNode = new Node(Token.SCRIPT);
        scriptNode.addChildToBack(sourceNode);
        globalNamespace = new GlobalNamespace(compilerWithExterns, scriptNode);
        List<Name> names = globalNamespace.getNames();
        assertEquals(1, names.size()); // Only 'x' should be in global namespace
        Name windowName = findName(names, "window");
        assertNull(windowName); // window should not be in global names as it's from externs
    }

    @Test
    public void testGetterAndSetterProperties() {
        String source = "var a = {}; Object.defineProperty(a, 'b', {get: function() {return 1;}, set: function(v) {}});";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        // This is complex; just verify it doesn't crash and finds the global 'a'
        List<Name> names = globalNamespace.getNames();
        Name a = findName(names, "a");
        assertNotNull(a);
    }

    @Test
    public void testConditionalAliasing() {
        String source = "var a = {}; if (true) { a.b = 1; }";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        Name a = findName(names, "a");
        assertNotNull(a);
        Name ab = findName(names, "a.b");
        assertNotNull(ab);
    }

    @Test
    public void testEscapingNames() {
        String source = "var $ = 1; var _foo = 2;";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
        Name dollar = findName(names, "$");
        assertNotNull(dollar);
        Name underscoreFoo = findName(names, "_foo");
        assertNotNull(underscoreFoo);
    }

    @Test
    public void testNamesWithSamePrefix() {
        String source = "var abc = 1; var abcd = 2;";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
        Name abc = findName(names, "abc");
        assertNotNull(abc);
        Name abcd = findName(names, "abcd");
        assertNotNull(abcd);
    }

    @Test
    public void testGlobalNamesInTryCatch() {
        String source = "try { var x = 1; } catch(e) { var y = 2; }";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
    }

    @Test
    public void testForwardReference() {
        String source = "var x = y; var y = 1;";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
        Name x = findName(names, "x");
        assertNotNull(x);
        Name y = findName(names, "y");
        assertNotNull(y);
        // Check that x has a reference to y before y is defined
        List<Ref> xRefs = x.getRefs();
        boolean foundForwardRef = false;
        for (Ref ref : xRefs) {
            if (ref.isReadWrite()) {
                foundForwardRef = true;
                break;
            }
        }
        assertTrue(foundForwardRef);
    }

    @Test
    public void testComplexNestedPropertyDefinition() {
        String source = "var a = {}; a.b = {}; a.b.c = function() {};";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(4, names.size());
        Name a = findName(names, "a");
        assertNotNull(a);
        Name ab = findName(names, "a.b");
        assertNotNull(ab);
        Name abc = findName(names, "a.b.c");
        assertNotNull(abc);
        assertTrue(abc.isFunctionNamespace());
    }

    @Test
    public void testRedeclaredGlobalVariable() {
        String source = "var x = 1; var x = 2;";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(1, names.size());
        Name x = names.get(0);
        List<Ref> refs = x.getRefs();
        assertEquals(2, refs.size()); // Should have two declarations
    }

    @Test
    public void testGlobalVarWithImplicitGlobal() {
        String source = "x = 1;"; // Implicit global without 'var'
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertFalse(names.isEmpty()); // 'x' should be treated as global in some cases
    }

    @Test
    public void testMultipleObjectsAssignmentOnSameLine() {
        String source = "var a = {}, b = {};";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(2, names.size());
    }

    @Test
    public void testEnumPattern() {
        String source = "var Color = {RED: 1, GREEN: 2, BLUE: 3};";
        globalNamespace = new GlobalNamespace(compiler, compiler.parseCode(source));
        List<Name> names = globalNamespace.getNames();
        assertEquals(4, names.size());
        Name color = findName(names, "Color");
        assertNotNull(color);
        assertTrue(color.isNamespaceObject());
        Name colorRed = findName(names, "Color.RED");
        assertNotNull(colorRed);
    }

    // Helper method to find a Name by full name in the list
    private Name findName(List<Name> names, String fullName) {
        for (Name name : names) {
            if (name.getFullName().equals(fullName)) {
                return name;
            }
        }
        return null;
    }
}