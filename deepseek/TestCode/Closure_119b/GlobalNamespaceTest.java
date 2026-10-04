package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.GlobalNamespace;
import com.google.javascript.jscomp.GlobalNamespace.Name;
import com.google.javascript.jscomp.Node;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for GlobalNamespace.
 * Designed to achieve maximum coverage and detect underlying faults (Defects4J Closure bug 119).
 */
public class GlobalNamespaceTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setIdeMode(true);
  }

  private Node compileAndGetRoot(String js) {
    compiler.compile(
        SourceFile.fromCode("test.js", js),
        SourceFile.fromCode("externs.js", ""),
        options);
    return compiler.getRoot();
  }

  @Test
  public void testEmptyProgram() {
    Node root = compileAndGetRoot("");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    assertNotNull(namespace);
    assertTrue(namespace.getAllNames().isEmpty());
  }

  @Test
  public void testGlobalVarDeclaration() {
    Node root = compileAndGetRoot("var x = 10;");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    Name xName = namespace.getName("x");
    assertNotNull(xName);
    assertEquals("x", xName.getBaseName());
    assertTrue(xName.getGlobalSets() == 1);
    assertNotNull(xName.getDeclaration());
  }

  @Test
  public void testGlobalFunctionDeclaration() {
    Node root = compileAndGetRoot("function f() {}");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    Name fName = namespace.getName("f");
    assertNotNull(fName);
    assertTrue(fName.getGlobalSets() == 1);
    assertTrue(fName.getDeclarations() > 0);
    assertNotNull(fName.getDeclaration());
  }

  @Test
  public void testUndeclaredGlobalAssignment() {
    // Assignment without explicit declaration creates a global name with no declaration node.
    Node root = compileAndGetRoot("a = 5;");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    Name aName = namespace.getName("a");
    assertNotNull(aName);
    assertTrue(aName.getGlobalSets() == 1);
    assertTrue(aName.getDeclarations() == 0); // no declaration
    // The bug (Closure 119) caused NPE when accessing getDeclaration() for such names.
    assertNull(aName.getDeclaration()); // should be null, not throw NPE
  }

  @Test
  public void testMultipleGlobalNames() {
    Node root = compileAndGetRoot("var x = 1; var y = 2; function z() {}");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    assertEquals(3, namespace.getAllNames().size());
    assertNotNull(namespace.getName("x"));
    assertNotNull(namespace.getName("y"));
    assertNotNull(namespace.getName("z"));
  }

  @Test
  public void testGlobalNameWithModule() {
    // Simulate a namespace that is a module type (empty for brevity)
    // This test verifies isModule() path
    Node root = compileAndGetRoot("var goog = {}; goog.module = function() {};");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    Name googName = namespace.getName("goog");
    assertNotNull(googName);
    // goog is not a module by itself
    assertTrue(namespace.isModule(googName) == false);
  }

  @Test
  public void testNameWithMultipleReferences() {
    Node root = compileAndGetRoot("var c = 0; c = c + 1; function g() { c = 2; }");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    Name cName = namespace.getName("c");
    assertNotNull(cName);
    assertTrue(cName.getGlobalSets() >= 1);
    // c is referenced multiple times
    assertTrue(cName.getGlobalRefs() >= 1);
  }

  @Test(expected = NullPointerException.class)
  public void testNullCompiler() {
    // This should trigger a null pointer if GlobalNamespace does not check for null compiler.
    new GlobalNamespace(null, new Node(0));
  }

  @Test
  public void testNameSlot() {
    Node root = compileAndGetRoot("var obj = {a: 1};");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    Name objName = namespace.getName("obj");
    assertNotNull(objName);
    // Slot 'a' should exist
    Name aSlot = namespace.getSlot("obj.a");
    assertNotNull(aSlot);
    assertEquals("a", aSlot.getBaseName());
  }

  @Test
  public void testAllNamesIteration() {
    Node root = compileAndGetRoot("var a; var b; var c;");
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    assertEquals(3, namespace.getAllNames().size());
    for (Name n : namespace.getAllNames()) {
      assertTrue(n.getGlobalSets() >= 0);
    }
  }

  @Test
  public void testNameWithExterns() {
    // Ensure externs are properly ignored
    options.setExternExportsPath("/dev/null");
    Node root = compileAndGetRoot("/** @externs */ var window;"); // Not actually a real externs
    GlobalNamespace namespace = new GlobalNamespace(compiler, root);
    Name windowName = namespace.getName("window");
    // window might be considered global if parsed
    assertNull(windowName);
  }
}