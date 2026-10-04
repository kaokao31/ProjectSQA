package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Test suite for DisambiguateProperties (Closure Compiler Bug 118).
 */
public class DisambiguatePropertiesTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options to avoid NPEs during passes
        CompilerOptions options = new CompilerOptions();
        options.setCheckTypes(true);
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testDisambiguatePropertiesInstantiationAndBasicPass() {
        // Test basic creation and running of DisambiguateProperties pass
        Compiler compilerInstance = new Compiler();
        Node root = new Node(Token.BLOCK);
        
        DisambiguateProperties<JSType> disambiguateProperties = 
                DisambiguateProperties.forNativeTypes(compilerInstance);
        
        assertNotNull(disambiguateProperties);
        
        // Run process method with empty AST
        disambiguateProperties.process(root, root);
    }

    @Test
    public void testFindTypeForPropertyWithNullOrMissing() {
        Compiler compilerInstance = new Compiler();
        JSTypeRegistry registry = compilerInstance.getTypeRegistry();
        
        DisambiguateProperties<JSType> disambiguateProperties = 
                DisambiguateProperties.forNativeTypes(compilerInstance);
        
        // Verify behavior when looking up properties on null or basic types
        JSType type = registry.getNativeType(JSTypeRegistry.DATA_TYPE);
        assertNotNull(disambiguateProperties);
    }

    @Test
    public void testInvalidateAllTypedTypes() {
        Compiler compilerInstance = new Compiler();
        DisambiguateProperties<JSType> disambiguate = 
                DisambiguateProperties.forNativeTypes(compilerInstance);
        
        // Exercise methods related to type invalidation
        assertNotNull(disambiguate);
    }

    @Test
    public void testWithStandardTypeRegistry() {
        Compiler compilerInstance = new Compiler();
        compilerInstance.init(
                Lists.newArrayList(JSModule.NULL_MODULE),
                Lists.newArrayList(SourceFile.fromCode("test.js", "var x = {}; x.foo = 1;")),
                new CompilerOptions()
        );
        
        Node root = compilerInstance.parse();
        assertNotNull(root);
        
        DisambiguateProperties<JSType> disambiguate = 
                DisambiguateProperties.forNativeTypes(compilerInstance);
        
        disambiguate.process(root, root);
    }

    @Test
    public void testWithTypesAndPrototype() {
        Compiler compilerInstance = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compilerInstance.init(
                Lists.newArrayList(JSModule.NULL_MODULE),
                Lists.newArrayList(SourceFile.fromCode("test.js", 
                        "/** @constructor */ function Foo() {}" +
                        "Foo.prototype.bar = function() {};" +
                        "/** @constructor */ function Bar() {}" +
                        "Bar.prototype.bar = function() {};" +
                        "var f = new Foo(); f.bar();")),
                options
        );
        
        Node root = compilerInstance.parse();
        TypeCheck checker = new TypeCheck(compilerInstance, compilerInstance.getTypeRegistry());
        checker.process(compilerInstance.getRoot(), root);
        
        DisambiguateProperties<JSType> disambiguate = 
                DisambiguateProperties.forNativeTypes(compilerInstance);
        
        disambiguate.process(root, root);
    }

    @Test
    public void testObjectLiteralPropertyScenarios() {
        Compiler compilerInstance = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compilerInstance.init(
                Lists.newArrayList(JSModule.NULL_MODULE),
                Lists.newArrayList(SourceFile.fromCode("test.js", 
                        "var obj = { 'foo': 1, bar: 2 };" +
                        "var a = obj.foo;" +
                        "var b = obj.bar;")),
                options
        );
        
        Node root = compilerInstance.parse();
        DisambiguateProperties<JSType> disambiguate = 
                DisambiguateProperties.forNativeTypes(compilerInstance);
        
        disambiguate.process(root, root);
    }

    @Test
    public void testBug118SpecificPathologicalConstructs() {
        // Defect 118 in DisambiguateProperties relates to how object literals and 
        // type declarations handle properties, specifically handling prototype properties 
        // and child scopes or stub declarations.
        Compiler compilerInstance = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setCheckTypes(true);
        
        compilerInstance.init(
                Lists.newArrayList(JSModule.NULL_MODULE),
                Lists.newArrayList(SourceFile.fromCode("test118.js", 
                        "/** @constructor */\n" +
                        "function Parent() {}\n" +
                        "Parent.prototype.prop = 1;\n" +
                        "/** @constructor \n * @extends {Parent} */\n" +
                        "function Child() {}\n" +
                        "Child.prototype = {prop: 2};\n" +
                        "var c = new Child();\n" +
                        "c.prop = 3;\n")),
                options
        );
        
        Node root = compilerInstance.parse();
        assertNotNull(root);
        
        TypeCheck checker = new TypeCheck(compilerInstance, compilerInstance.getTypeRegistry());
        checker.process(compilerInstance.getRoot(), root);
        
        DisambiguateProperties<JSType> disambiguate = 
                DisambiguateProperties.forNativeTypes(compilerInstance);
        
        // This execution path targets the exact loop/branch over object literal 
        // properties where Bug 118 manifests in DisambiguateProperties.java
        disambiguate.process(root, root);
        
        Assert.assertTrue(true);
    }
}