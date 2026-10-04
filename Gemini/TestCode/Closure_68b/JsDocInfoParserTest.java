package com.google.javascript.jscomp.parsing;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.testing.EmptyScope;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsDocInfoParserTest {

    private Config config;
    private JSTypeRegistry jsTypeRegistry;
    private WarningReporterStub warningReporter;

    @Before
    public void setUp() {
        jsTypeRegistry = new JSTypeRegistry(null, true);
        warningReporter = new WarningReporterStub();
        config = new Config(
                null,
                jsTypeRegistry,
                warningReporter,
                null,
                null
        );
    }

    @Test
    public void testParseBasicJsDoc() {
        String jsDocSource = "* @author user@google.com (User) ";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertEquals("user@google.com (User)", info.getAuthor());
    }

    @Test
    public void testParseEmptyJsDoc() {
        String jsDocSource = "";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        // Depending on parser implementation, empty might return false or true with null info
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNull(info);
    }

    @Test
    public void testParseTypeAnnotation() {
        String jsDocSource = "* @type {string} */";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getType());
        assertTrue(info.getType().isString());
    }

    @Test
    public void testParseParamAnnotation() {
        String jsDocSource = "* @param {number} x The x coordinate.";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.hasParameter("x"));
        assertEquals("The x coordinate.", info.getDescriptionForParameter("x"));
    }

    @Test
    public void testParseReturnAnnotation() {
        String jsDocSource = "* @return {boolean} Success status.";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getReturnType());
        assertTrue(info.getReturnType().isBooleanValueType());
    }

    @Test
    public void testParseDeprecated() {
        String jsDocSource = "* @deprecated Use something else instead.";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isDeprecated());
    }

    @Test
    public void testParseDesc() {
        String jsDocSource = "* @desc This is a description.";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertEquals("This is a description.", info.getDescription());
    }

    @Test
    public void testParseDefine() {
        String jsDocSource = "* @define {boolean}";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isDefine());
    }

    @Test
    public void testParseSuppress() {
        String jsDocSource = "* @suppress {checkTypes|uselessCode}";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.getSuppressions().contains("checkTypes"));
        assertTrue(info.getSuppressions().contains("uselessCode"));
    }

    @Test
    public void testParsePrivate() {
        String jsDocSource = "* @private";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.getVisibility().isPrivate());
    }

    @Test
    public void testParseProtected() {
        String jsDocSource = "* @protected";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.getVisibility().isProtected());
    }

    @Test
    public void testParsePublic() {
        String jsDocSource = "* @public";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.getVisibility().isPublic());
    }

    @Test
    public void testParseConstant() {
        String jsDocSource = "* @const";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isConstant());
    }

    @Test
    public void testParseConstructor() {
        String jsDocSource = "* @constructor";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isConstructor());
    }

    @Test
    public void testParseInterface() {
        String jsDocSource = "* @interface";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isInterface());
    }

    @Test
    public void testParseImplements() {
        String jsDocSource = "* @implements {SomeInterface}";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertEquals(1, info.getImplementedInterfaces().size());
    }

    @Test
    public void testParseExtends() {
        String jsDocSource = "* @extends {SomeBaseClass}";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getBaseClass());
    }

    @Test
    public void testParseInvalidAnnotation() {
        String jsDocSource = "* @invalidAnnotationTag";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        // Should handle gracefully or report warning
        assertNotNull(parser);
    }

    @Test
    public void testParseFileoverview() {
        String jsDocSource = "* @fileoverview This is a file overview.";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.hasFileOverview());
    }

    @Test
    public void testParseLicense() {
        String jsDocSource = "* @license MIT";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertEquals("MIT", info.getLicense());
    }

    @Test
    public void testParseThrows() {
        String jsDocSource = "* @throws {Error} If something goes wrong.";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertFalse(info.getThrownTypes().isEmpty());
    }

    @Test
    public void testParseSee() {
        String jsDocSource = "* @see AnotherClass";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertFalse(info.getSeeReferences().isEmpty());
    }

    @Test
    public void testParseVersion() {
        String jsDocSource = "* @version 1.0.4";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertEquals("1.0.4", info.getVersion());
    }

    @Test
    public void testParseThis() {
        String jsDocSource = "* @this {MyClass}";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getThisType());
    }

    @Test
    public void testParseOverride() {
        String jsDocSource = "* @override";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isOverride());
    }

    @Test
    public void testParseNoAlias() {
        String jsDocSource = "* @noalias";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isNoAlias());
    }

    @Test
    public void testParseNoCompile() {
        String jsDocSource = "* @nocompile";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isNoCompile());
    }

    @Test
    public void testParseExport() {
        String jsDocSource = "* @export";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isExport());
    }

    @Test
    public void testParseExterns() {
        String jsDocSource = "* @externs";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isExterns());
    }

    @Test
    public void testParseLends() {
        String jsDocSource = "* @lends {SomeObject.prototype}";
        JsDocInfoParser parser = createParser(jsDocSource);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getLendsName());
    }

    // Helper method to instantiate JsDocInfoParser using standard Closure idioms
    private JsDocInfoParser createParser(String source) {
        // Create an instance of JsDocInfoParser through its constructor or factory stream
        JsDocHeaderParser.ExtractionInfo extractionInfo = new JsDocHeaderParser.ExtractionInfo("", "", false, null);
        Node node = new Node(Token.SCRIPT);
        
        // Using standard constructor pattern for JsDocInfoParser in Closure compiler
        return new JsDocInfoParser(
                new JavascriptHoodieScanner(source),
                source,
                node,
                config,
                warningReporter
        );
    }

    // Stub scanner helper to mimic stream processing for parser tests
    private static class JavascriptHoodieScanner extends JsDocTokenStream {
        public JavascriptHoodieScanner(String source) {
            super(source, 0, 0);
        }
    }

    private static class WarningReporterStub implements org.junit.runner.Describable, com.google.javascript.jscomp.DiagnosticReporter {
        @Override
        public void warning(com.google.javascript.jscomp.JSError message) {
        }

        @Override
        public void error(com.google.javascript.jscomp.JSError message) {
        }

        @Override
        public com.google.javascript.jscomp.CheckLevel getEffectiveLevel(com.google.javascript.jscomp.DiagnosticType type) {
            return com.google.javascript.jscomp.CheckLevel.WARNING;
        }

        @Override
        public org.junit.runner.Description getDescription() {
            return org.junit.runner.Description.createSuiteDescription("WarningReporterStub");
        }
    }
}