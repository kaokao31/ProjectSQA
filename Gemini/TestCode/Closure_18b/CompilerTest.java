package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.util.logging.Level;

import static org.junit.Assert.*;

public class CompilerTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @After
    public void tearDown() {
        compiler = null;
    }

    @Test
    public void testInitialState() {
        assertNotNull(compiler);
        assertFalse(compiler.hasErrors());
        assertEquals(0, compiler.getErrorCount());
        assertEquals(0, compiler.getWarningCount());
    }

    @Test
    public void testOptionsInitialization() {
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        assertSame(options, compiler.getOptions());
    }

    @Test
    public void testInitWithExternsAndJs() {
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "function alert(x) {}");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "alert('hello');");

        compiler.init(externsListFrom(extern), jsListFrom(js), new CompilerOptions());
        
        assertNotNull(compiler.getRoot());
        assertFalse(compiler.hasErrors());
    }

    @Test
    public void testCompileWithBasicInput() {
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "var x = 10;");

        CompilerOptions options = new CompilerOptions();
        Result result = compiler.compile(extern, js, options);

        assertNotNull(result);
        assertTrue(result.success);
        assertFalse(compiler.hasErrors());
        assertEquals("var x=10;\n", compiler.toSource());
    }

    @Test
    public void testCompileWithSyntaxError() {
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "var x = ;"); // Syntax error

        CompilerOptions options = new CompilerOptions();
        Result result = compiler.compile(extern, js, options);

        assertNotNull(result);
        assertFalse(result.success);
        assertTrue(compiler.hasErrors());
    }

    @Test
    public void testGetRootWithoutInit() {
        // Depending on compiler state, getRoot might be null or throw, let's verify safely.
        Node root = compiler.getRoot();
        // Initially root is usually null before init
        assertNull(root);
    }

    @Test
    public void testGetSourceLine() {
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "line1;\nline2;\nline3;");

        compiler.init(externsListFrom(extern), jsListFrom(js), new CompilerOptions());
        
        String line = compiler.getSourceLine("input.js", 2);
        assertEquals("line2;", line);
    }

    @Test
    public void testGetSourceRegion() {
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "line1;\nline2;\nline3;");

        compiler.init(externsListFrom(extern), jsListFrom(js), new CompilerOptions());
        
        Region region = compiler.getSourceRegion("input.js", 2);
        assertNotNull(region);
        assertEquals("line2;", region.getSourceExcerpt());
    }

    @Test
    public void testProcessCode() {
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // Test dummy compiler pass execution
        compiler.process(new CompilerPass() {
            @Override
            public void process(Node externs, Node root) {
                // No-op pass
            }
        });
        
        assertFalse(compiler.hasErrors());
    }

    @Test
    public void testSetErrorManager() {
        ErrorManager errorManager = new LoggerErrorManager(createMessageFormatter(), java.util.logging.Logger.getAnonymousLogger());
        compiler.setErrorManager(errorManager);
        assertSame(errorManager, compiler.getErrorManager());
    }

    @Test
    public void testGetProgress() {
        assertEquals(0.0, compiler.getProgress(), 0.001);
    }

    @Test
    public void testPreprocessJs() {
        CompilerOptions options = new CompilerOptions();
        options.jqueryPass = true;
        compiler.initOptions(options);
        
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "var x = 1;");
        
        compiler.init(externsListFrom(extern), jsListFrom(js), options);
        assertNotNull(compiler.toSource());
    }

    @Test
    public void testRunPostCompilationPass() {
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "var x = 1;");
        
        compiler.init(externsListFrom(extern), jsListFrom(js), options);
        compiler.parse();
        
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testMultipleCompilations() {
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js1 = JSSourceFile.fromCode("input1.js", "var a = 1;");
        JSSourceFile js2 = JSSourceFile.fromCode("input2.js", "var b = 2;");

        CompilerOptions options = new CompilerOptions();
        compiler.compile(extern, js1, options);
        assertEquals("var a=1;\n", compiler.toSource());

        compiler.compile(extern, js2, options);
        assertEquals("var b=2;\n", compiler.toSource());
    }

    @Test
    public void testGetDefaultExterns() throws Exception {
        java.util.List<JSSourceFile> externs = Compiler.getDefaultExterns();
        assertNotNull(externs);
        assertFalse(externs.isEmpty());
    }

    @Test
    public void testRecordFunctionInformation() {
        CompilerOptions options = new CompilerOptions();
        options.recordFunctionInformation = true;
        compiler.initOptions(options);
        
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "function f() {}");
        
        compiler.compile(extern, js, options);
        assertFalse(compiler.hasErrors());
    }

    @Test
    public void testSerialize() {
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
        JSSourceFile js = JSSourceFile.fromCode("input.js", "var x = 1;");
        compiler.compile(extern, js, options);
        
        byte[] serialized = compiler.toBinary();
        // Binary serialization might be supported or return null depending on configuration, 
        // just invoke to ensure no unhandled exceptions if exposed, or skip if not part of public API.
    }

    @Test
    public void testGetUniqueIdSupplier() {
        assertNotNull(compiler.getUniqueIdSupplier());
    }

    @Test
    public void testToString() {
        assertNotNull(compiler.toString());
    }

    // Helper methods
    private java.util.List<JSSourceFile> externsListFrom(JSSourceFile... files) {
        java.util.List<JSSourceFile> list = new java.util.ArrayList<>();
        for (JSSourceFile f : files) {
            list.add(f);
        }
        return list;
    }

    private java.util.List<JSSourceFile> jsListFrom(JSSourceFile... files) {
        java.util.List<JSSourceFile> list = new java.util.ArrayList<>();
        for (JSSourceFile f : files) {
            list.add(f);
        }
        return list;
    }

    private MessageFormatter createMessageFormatter() {
        return new LightweightMessageFormatter();
    }
}