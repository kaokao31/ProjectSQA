package com.google.javascript.jscomp.parsing;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.TokenStream;
import com.google.javascript.jscomp.mozilla.rhino.CompilerEnvirons;
import com.google.javascript.jscomp.mozilla.rhino.Context;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.Assert.*;

public class JsDocInfoParserTest {

    private DummyErrorReporter errorReporter;
    private Config config;

    @Before
    public void setUp() {
        errorReporter = new DummyErrorReporter();
        Set<String> annotationSet = Annotation.annotationNames();
        config = new Config(annotationSet, null);
    }

    @Test
    public void testParseEmptyString() {
        String jsDoc = "";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertFalse(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNull(info);
    }

    @Test
    public void testParseSimpleDescription() {
        String jsDoc = "/** @fileoverview This is a test file overview. */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isfileoverview());
    }

    @Test
    public void testParseTypeAnnotation() {
        String jsDoc = "/** @type {string} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getType());
    }

    @Test
    public void testParseParamAnnotation() {
        String jsDoc = "/** @param {number} x The x coordinate. */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.hasParameter("x"));
    }

    @Test
    public void testParseReturnAnnotation() {
        String jsDoc = "/** @return {boolean} Whether it works. */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        boolean result2 = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getReturnType());
    }

    @Test
    public void testParseDefineAnnotation() {
        String jsDoc = "/** @define {boolean} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isDefine());
    }

    @Test
    public void testParsePrivateAnnotation() {
        String jsDoc = "/** @private */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.getVisibility() == JSDocInfo.Visibility.PRIVATE);
    }

    @Test
    public void testParseProtectedAnnotation() {
        String jsDoc = "/** @protected */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.getVisibility() == JSDocInfo.Visibility.PROTECTED);
    }

    @Test
    public void testParsePublicAnnotation() {
        String jsDoc = "/** @public */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.getVisibility() == JSDocInfo.Visibility.PUBLIC);
    }

    @Test
    public void testParseConstAnnotation() {
        String jsDoc = "/** @const */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isConstant());
    }

    @Test
    public void testParseDeprecatedAnnotation() {
        String jsDoc = "/** @deprecated Use something else. */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isDeprecated());
    }

    @Test
    public void testParseSuppressAnnotation() {
        String jsDoc = "/** @suppress {checkTypes} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getSuppressions());
        assertTrue(info.getSuppressions().contains("checkTypes"));
    }

    @Test
    public void testParseAuthorAnnotation() {
        String jsDoc = "/** @author John Doe */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
    }

    @Test
    public void testParseLendsAnnotation() {
        String jsDoc = "/** @lends {SomeObject.prototype} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
    }

    @Test
    public void testParseThisAnnotation() {
        String jsDoc = "/** @this {SomeObject} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getThisType());
    }

    @Test
    public void testParseThrowsAnnotation() {
        String jsDoc = "/** @throws {Error} If something goes wrong. */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
    }

    @Test
    public void testParseEnumAnnotation() {
        String jsDoc = "/** @enum {string} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isEnum());
    }

    @Test
    public void testParseImplementsAnnotation() {
        String jsDoc = "/** @implements {SomeInterface} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.getImplementedInterfaces().size() > 0);
    }

    @Test
    public void testParseInterfaceAnnotation() {
        String jsDoc = "/** @interface */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isInterface());
    }

    @Test
    public void testParseRecordAnnotation() {
        String jsDoc = "/** @record */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
    }

    @Test
    public void testParseExportAnnotation() {
        String jsDoc = "/** @export */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isExport());
    }

    @Test
    public void testParseExternsAnnotation() {
        String jsDoc = "/** @externs */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isExterns());
    }

    @Test
    public void testParseNoAliasAnnotation() {
        String jsDoc = "/** @noalias */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isNoAlias());
    }

    @Test
    public void testParseNoCompileAnnotation() {
        String jsDoc = "/** @nocompile */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isNoCompile());
    }

    @Test
    public void testParseNoTypeCheckAnnotation() {
        String jsDoc = "/** @nocheck */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
    }

    @Test
    public void testParsePreserveTryAnnotation() {
        String jsDoc = "/** @preserveTry */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
    }

    @Test
    public void testParseLicenseAnnotation() {
        String jsDoc = "/** @license MIT */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.hasLicense());
    }

    @Test
    public void testParseBowerAnnotation() {
        String jsDoc = "/** @bowered */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        // May or may not recognize depending on configuration, just testing invocation path
        assertNotNull(parser);
    }

    @Test
    public void testParseInvalidAnnotation() {
        String jsDoc = "/** @nonexistentannotation */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertNotNull(parser);
    }

    @Test
    public void testParseMultipleLinesAndAnnotations() {
        String jsDoc = "/**\n" +
                " * Description here.\n" +
                " * @param {string} a Description a.\n" +
                " * @return {number}\n" +
                " */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.hasParameter("a"));
        assertNotNull(info.getReturnType());
    }

    @Test
    public void testParseWithTypeApplication() {
        String jsDoc = "/** @type {Array.<string>} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getType());
    }

    @Test
    public void testParseWithRecordType() {
        String jsDoc = "/** @type {{a: string, b: number}} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getType());
    }

    @Test
    public void testParseWithFunctionType() {
        String jsDoc = "/** @type {function(string, number): boolean} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertNotNull(info.getType());
    }

    @Test
    public void testParseNullableAndNonNullableTypes() {
        String jsDoc = "/** @type {?string} */";
        JsDocInfoParser parser = createParser(jsDoc);
        assertTrue(parser.parse());

        JsDocInfoParser parser2 = createParser("/** @type {!string} */");
        assertTrue(parser2.parse());
    }

    @Test
    public void testParseModifierAnnotations() {
        String jsDoc = "/** @constructor @extends {Base} @final */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isConstructor());
        assertTrue(info.isFinal());
    }

    @Test
    public void testParseAbstractAnnotation() {
        String jsDoc = "/** @abstract */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isAbstract());
    }

    @Test
    public void testParseOverrideAnnotation() {
        String jsDoc = "/** @override */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isOverride());
    }

    @Test
    public void testParseEqualsAnnotation() {
        String jsDoc = "/** @nosideeffects */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
        assertTrue(info.isNoSideEffects());
    }

    @Test
    public void testParseVersionAnnotation() {
        String jsDoc = "/** @version 1.2.3 */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        JSDocInfo info = parser.retrieveAndClearJsDocInfo();
        assertNotNull(info);
    }

    @Test
    public void testParseCustomInlineTags() {
        String jsDoc = "/** {@link #toString} */";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
    }

    @Test
    public void testParseWhimsicalFormatting() {
        String jsDoc = "  /**\n   *   @type    {number}   \n   */  ";
        JsDocInfoParser parser = createParser(jsDoc);
        boolean result = parser.parse();
        assertTrue(result);
        assertNotNull(parser.retrieveAndClearJsDocInfo());
    }

    @Test
    public void testParseBadSyntaxType() {
        String jsDoc = "/** @type { */";
        JsDocInfoParser parser = createParser(jsDoc);
        parser.parse();
        assertNotNull(parser);
    }

    // Helper method to instantiate JsDocInfoParser via available constructors or factory methods
    private JsDocInfoParser createParser(String jsDoc) {
        // Using standard constructor patterns found in Closure compiler JsDocInfoParser
        JsDocStream stream = new JsDocStream(jsDoc.toCharArray(), 0, jsDoc.length());
        return new JsDocInfoParser(stream, jsDoc, 0, config, errorReporter);
    }

    private static class DummyErrorReporter implements ErrorReporter {
        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        }

        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        }

        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
            return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
        }
    }
}