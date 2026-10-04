package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.AnalyzePrototypeProperties.PrototypeProperty;
import com.google.javascript.rhino.Node;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for AnalyzePrototypeProperties.
 * Designed to achieve high line/branch coverage and detect the bug
 * where prototype property reads are incorrectly recorded as assignments.
 */
public class AnalyzePrototypePropertiesTest {

  private Compiler compiler;
  private AnalyzePrototypeProperties pass;
  private Set<PrototypeProperty> collectedProperties;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new DefaultCodingConvention());
    // Keep source information for precise checks
    options.setPreserveDetailedSourceInfo(true);
    compiler.disableThreadSafetyAssertions();
    collectedProperties = null;
  }

  private void compileAndRun(String code) {
    SourceFile source = SourceFile.fromCode("test.js", code);
    compiler.compile(SourceFile.fromCode("externs.js", ""), source, options);
    pass = new AnalyzePrototypeProperties(compiler);
    pass.process(null, compiler.getRoot());
    collectedProperties = pass.getPrototypeProperties();
  }

  // Helper to check presence of a property with given name and owner
  private boolean containsProperty(Set<PrototypeProperty> properties, String name, String owner) {
    for (PrototypeProperty pp : properties) {
      if (pp.getName().equals(name) && pp.getOwnerName().equals(owner)) {
        return true;
      }
    }
    return false;
  }

  // ==============================================================
  //   Basic Prototype Property Assignments
  // ==============================================================

  @Test
  public void testSimplePropertyAssignment() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = 1;\n");
    assertNotNull("properties should not be null", collectedProperties);
    assertFalse("should contain at least one property", collectedProperties.isEmpty());
    assertTrue("should contain 'bar' on Foo",
               containsProperty(collectedProperties, "bar", "Foo"));
  }

  @Test
  public void testMultiplePropertiesSameConstructor() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = 1;\n" +
        "Foo.prototype.baz = 'a';\n");
    assertNotNull(collectedProperties);
    assertEquals(2, collectedProperties.size());
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
    assertTrue(containsProperty(collectedProperties, "baz", "Foo"));
  }

  @Test
  public void testPropertyOnDifferentConstructors() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "/** @constructor */ function Bar() {}\n" +
        "Foo.prototype.x = 1;\n" +
        "Bar.prototype.y = 2;\n");
    assertNotNull(collectedProperties);
    assertEquals(2, collectedProperties.size());
    assertTrue(containsProperty(collectedProperties, "x", "Foo"));
    assertTrue(containsProperty(collectedProperties, "y", "Bar"));
  }

  // ==============================================================
  //   Property Read (get) – should NOT be recorded
  // ==============================================================

  @Test
  public void testPropertyReadNotRecorded() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = 1;\n" +
        "var x = Foo.prototype.bar;\n");
    // Only the assignment should be recorded, not the read
    assertNotNull(collectedProperties);
    assertEquals(1, collectedProperties.size());
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
  }

  @Test
  public void testOnlyReadNoAssignment() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "var x = Foo.prototype.bar;\n");
    assertNotNull(collectedProperties);
    assertTrue("no assignments, so properties set should be empty", collectedProperties.isEmpty());
  }

  // ==============================================================
  //   Edge Cases: null, undefined, nested
  // ==============================================================

  @Test
  public void testPropertyWithNullInitializer() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = null;\n");
    assertNotNull(collectedProperties);
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
  }

  @Test
  public void testPropertyWithUndefinedInitializer() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = undefined;\n");
    assertNotNull(collectedProperties);
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
  }

  @Test
  public void testChainedPrototype() {
    // e.g., Foo.prototype.bar.prototype.baz = 1;
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "/** @constructor */ function Goo() {}\n" +
        "Foo.prototype.bar = Goo;\n" +
        "Foo.prototype.bar.prototype.baz = 1;\n");
    // This should record two prototype properties: 'bar' on Foo (with value Goo) and 'baz' on Goo
    assertNotNull(collectedProperties);
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
    assertTrue(containsProperty(collectedProperties, "baz", "Goo"));
  }

  // ==============================================================
  //   Property Deletion (delete) – not an assignment
  // ==============================================================

  @Test
  public void testDeletePropertyNotRecorded() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = 1;\n" +
        "delete Foo.prototype.bar;\n");
    // Delete does not count as assignment; if previously assigned, property should still be present
    // (The analysis records assignments, not deletion; deletion may affect lifetime, but this test
    //  checks that delete itself does not create a new property record.)
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
  }

  // ==============================================================
  //   Non-prototype properties (not on .prototype)
  // ==============================================================

  @Test
  public void testRegularPropertyNotRecorded() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.bar = 1;\n");
    assertNotNull(collectedProperties);
    assertTrue("non-prototype properties should not be included", collectedProperties.isEmpty());
  }

  @Test
  public void testInstancePropertyNotRecorded() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "var f = new Foo();\n" +
        "f.bar = 1;\n");
    assertTrue("instance property not on prototype", collectedProperties.isEmpty());
  }

  // ==============================================================
  //   Multiple assignment to same property (should not duplicate)
  // ==============================================================

  @Test
  public void testDuplicateAssignmentNotDoubleCounted() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = 1;\n" +
        "Foo.prototype.bar = 2;\n");
    assertNotNull(collectedProperties);
    assertEquals("duplicate assignments to same property should still be one record",
                 1, collectedProperties.size());
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
  }

  // ==============================================================
  //   Property via function expression assignment
  // ==============================================================

  @Test
  public void testPropertyAssignedFunction() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = function() {};\n");
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
  }

  // ==============================================================
  //   No source (empty input)
  // ==============================================================

  @Test
  public void testEmptyInput() {
    compileAndRun("");
    assertNotNull(collectedProperties);
    assertTrue("empty input should yield empty set", collectedProperties.isEmpty());
  }

  // ==============================================================
  //   Prototype property accessed via variable (not literal)
  // ==============================================================

  @Test
  public void testPropertyOnPrototypeVariable() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "var proto = Foo.prototype;\n" +
        "proto.bar = 1;\n");
    // The analysis should follow the variable reference and recognize this as Foo.prototype.bar
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
  }

  // ==============================================================
  //   Computed property name (e.g., Foo.prototype['baz'] = 1)
  // ==============================================================

  @Test
  public void testComputedPropertyName() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype['baz'] = 1;\n");
    // Computed names are often not tracked because name can be dynamic.
    // For static strings, some implementations capture them.
    // This test ensures no exception and property is captured if possible.
    // At minimum, the pass should not crash.
    assertNotNull(collectedProperties);
    // May or may not contain 'baz' depending on implementation; we only assert no exception.
  }

  // ==============================================================
  //   Multiple prototype chains (inheritance)
  // ==============================================================

  @Test
  public void testInheritedPrototypeProperty() {
    compileAndRun(
        "/** @constructor */ function Parent() {}\n" +
        "/** @constructor @extends {Parent} */ function Child() {}\n" +
        "Child.prototype = new Parent();\n" +
        "Parent.prototype.prop = 1;\n");
    // Parent's prototype property should be recorded; Child prototype assignment is a different case.
    assertTrue(containsProperty(collectedProperties, "prop", "Parent"));
  }

  // ==============================================================
  //   Bug-specific: Read after assignment should not remove the property
  // ==============================================================

  @Test
  public void testBug67PropertyReadNotModified() {
    // This is the core bug: a read of a prototype property should not cause
    // the pass to think the property is modified. If the pass incorrectly
    // marks the property as modified, it may be removed erroneously.
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = 1;\n" +
        "var x = Foo.prototype.bar;\n" +
        "Foo.prototype.baz = x;\n");
    // Both 'bar' and 'baz' should be present
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
    assertTrue(containsProperty(collectedProperties, "baz", "Foo"));
    assertEquals(2, collectedProperties.size());
  }

  // ==============================================================
  //   Assignment in a function declaration
  // ==============================================================

  @Test
  public void testPropertyAssignedInFunction() {
    compileAndRun(
        "/** @constructor */ function Foo() {}\n" +
        "function initProto() {\n" +
        "  Foo.prototype.bar = 1;\n" +
        "}\n" +
        "initProto();\n");
    // The assignment is inside a function, but analysis should still capture it.
    assertTrue(containsProperty(collectedProperties, "bar", "Foo"));
  }

  // ==============================================================
  //   Property on an anonymous prototype (e.g., obj.prototype)
  // ==============================================================

  @Test
  public void testAnonymousPrototype() {
    compileAndRun(
        "var obj = {prototype: {}};\n" +
        "obj.prototype.foo = 1;\n");
    // This is not a constructor's prototype; analysis may ignore it.
    // Ensure no crash and set is empty (since not a real constructor prototype).
    assertNotNull(collectedProperties);
    assertTrue("anonymous objects are not tracked", collectedProperties.isEmpty());
  }

  // ==============================================================
  //   Null/undefined references (should not throw NPE)
  // ==============================================================

  @Test(expected = NullPointerException.class)
  public void testNullCompiler() {
    new AnalyzePrototypeProperties(null);
  }

  @Test(expected = NullPointerException.class)
  public void testProcessNullRoot() {
    // set up
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    c.compile(SourceFile.fromCode("empty.js", ""), SourceFile.fromCode("test.js", ""), opts);
    AnalyzePrototypeProperties p = new AnalyzePrototypeProperties(c);
    p.process(null, null);
  }
}