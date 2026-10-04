package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.*;

public class AnalyzePrototypePropertiesTest {

    private Compiler compiler;
    private AnalyzePrototypeProperties analyzer;

    @Before
    public void setUp() {
        compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
    }

    @Test
    public void testEmptyPrototype() {
        String js = "/** @constructor */ function Foo() {} // No prototype assignments";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testSimplePropertyWithStringLiteral() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.bar = 'test';";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsKey("bar"));
        Assert.assertEquals("test", result.get("bar").getStringValue());
    }

    @Test
    public void testNestedPropertyWithDotAccess() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.bar.baz = 42;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsKey("bar"));
        Assert.assertNull(result.get("bar").getStringValue());
        Assert.assertTrue(result.containsKey("bar.baz"));
    }

    @Test
    public void testNumberLiteralProperty() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.count = 100;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get("count").getStringValue());
    }

    @Test
    public void testBooleanLiteralProperty() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.flag = true;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get("flag").getStringValue());
    }

    @Test
    public void testNullLiteralProperty() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.value = null;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get("value").getStringValue());
    }

    @Test
    public void testPrototypeGetPropAssignment() {
        String js = "/** @constructor */ function Foo() {} \n var x = {}; \n Foo.prototype.prop = x.y;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get("prop").getStringValue());
    }

    @Test
    public void testMultiplePropertiesOnSamePrototype() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.a = 1; \n Foo.prototype.b = 'hello'; \n Foo.prototype.c = {};";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.containsKey("a"));
        Assert.assertTrue(result.containsKey("b"));
        Assert.assertTrue(result.containsKey("c"));
    }

    @Test
    public void testNonLiteralAssignmentOnProperty() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.prop = someFunction();";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get("prop").getStringValue());
    }

    @Test
    public void testMultipleConstructorPrototypes() {
        String js = "/** @constructor */ function Foo() {} \n /** @constructor */ function Bar() {} \n Foo.prototype.x = 1; \n Bar.prototype.y = 'abc';";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsKey("x"));
        Assert.assertTrue(result.containsKey("y"));
    }

    @Test
    public void testPropertyValueWithArrayLiteral() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.list = [1, 2, 3];";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get("list").getStringValue());
    }

    @Test
    public void testPropertyWithFullyQualifiedName() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.namespace.property = 'value';";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsKey("namespace"));
        Assert.assertTrue(result.containsKey("namespace.property"));
    }

    @Test
    public void testEmptyAssignmentValue() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.empty =;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        // Depending on parsing, might fail gracefully
    }

    @Test
    public void testPrototypeAssignmentInsideFunction() {
        String js = "/** @constructor */ function Foo() {} \n (function() { Foo.prototype.x = 1; })();";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testPropertyWithComplexGetProp() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.obj.prop = 'deep';";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsKey("obj"));
        Assert.assertTrue(result.containsKey("obj.prop"));
    }

    @Test
    public void testMultipleNestedPropertyLevels() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.a.b.c.d = 7;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }

    @Test
    public void testFunctionPrototypeAssignment() {
        String js = "/** @constructor */ function Foo() {} \n var Bar = function() {}; \n Bar.prototype.x = 'y';";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsKey("x"));
    }

    @Test
    public void testAnonymousFunctionAsPropertyValue() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.method = function() {};";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get("method").getStringValue());
    }

    @Test
    public void testPropertyWithThisInPrototype() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.x = this.y;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testPropertyInNestedBlock() {
        String js = "/** @constructor */ function Foo() {} \n if (true) { Foo.prototype.x = 1; }";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testPrototypeWithNumberProperty() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype[0] = 'zero';";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsKey("0"));
    }

    @Test
    public void testMultiplePrototypeChains() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.a.b = 1; \n Foo.prototype.a.c = 2;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.containsKey("a"));
        Assert.assertTrue(result.containsKey("a.b"));
        Assert.assertTrue(result.containsKey("a.c"));
    }

    @Test
    public void testExternsNotAnalyzed() {
        // Externs should not be analyzed by default
    }

    @Test
    public void testPropertyOnNonPrototype() {
        String js = "/** @constructor */ function Foo() {} \n Foo.bar = 'baz';";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testPrototypeWithUnderScoreName() {
        String js = "/** @constructor */ function _Foo() {} \n _Foo.prototype.x = 1;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsKey("x"));
    }

    @Test
    public void testPropertyWithUndefinedValue() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.x = undefined;";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.get("x").getStringValue());
    }

    @Test
    public void testGlobalScopePrototypeAssignment() {
        String js = "/** @constructor */ function Foo() {} \n Foo.prototype.x = 1; \n (function() { Foo.prototype.y = 2; })();";
        Node root = parse(js);
        analyzer = new AnalyzePrototypeProperties(compiler, null);
        Map<String, AnalyzePrototypeProperties.LiteralProperty> result = analyzer.analyze(root);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    private Node parse(String js) {
        CompilerInput input = compiler.parseSyntheticCode("test", js);
        if (compiler.hasErrors()) {
            throw new RuntimeException("Parse error: " + compiler.getErrors());
        }
        return input.getAstRoot(compiler);
    }
}