package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * JUnit 4 test suite for {@link PeepholeReplaceKnownMethods}.
 * Aimed at maximum code coverage and fault detection (including Defects4J Closure bug 50).
 */
@RunWith(JUnit4.class)
public class PeepholeReplaceKnownMethodsTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        options.setWarningLevel(DiagnosticGroups.LINT, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.PARSE_ERRORS, CheckLevel.OFF);
        // Enable peephole optimizations
        options.setPeepholeOptimizationsOn(true);
        // Ensure the pass runs
        options.setFoldConstants(true);
        options.setRemoveDeadCode(true);
        options.setInlineVariables(true);
        options.setInlineFunctions(true);
        // Disable other passes that might interfere
        options.setCheckSymbols(false);
        options.setCheckTypes(false);
        options.setGenerateExports(false);
        options.setReplaceStrings(false);
        options.setPreferReservedWords(false);
        options.setPreserveDetailedParseInfo(false);
        options.setForceLibraryInjection(false);
        options.setPrintAst(false);
        options.setTweakProcessing(CompilerOptions.TweakProcessing.OFF);
        options.setAliasStrings(false);
        options.setAliasExternals(false);
        options.setAliasKeywords(false);
        options.setCollapseProperties(false);
        options.setCollapseVariableDeclarations(false);
        options.setConvertToDottedProperties(false);
        options.setDeadAssignmentElimination(false);
        options.setDisambiguateProperties(false);
        options.setExternExports(false);
        options.setExtractPrototypeMemberDeclarations(false);
        options.setGeneratePseudoNames(false);
        options.setGroupVariableDeclarations(false);
        options.setInlineGetters(false);
        options.setLabelRenaming(false);
        options.setOptimizeArgumentsArray(false);
        options.setOptimizeParameters(false);
        options.setOptimizeReturns(false);
        options.setRemoveAbstractMethods(false);
        options.setRemoveClosureAsserts(false);
        options.setRemoveEmptyCode(false);
        options.setRemoveMemberOverloadDefs(false);
        options.setRemoveUnusedClassProperties(false);
        options.setRemoveUnusedPrototypeMethods(false);
        options.setRenameFunctions(false);
        options.setRenameLabels(false);
        options.setRenamePrefixNamespace("");
        options.setRenameProperties(false);
        options.setRenameVariables(false);
        options.setRewriteFunctionExpressions(false);
        options.setShadowVariables(false);
        options.setSmartNameRemoval(false);
        options.setSubstituteWellKnown(false); // we test this specifically
        options.setSuppressWarning(DiagnosticGroups.LINT);
        options.setSuppressWarning(DiagnosticGroups.CHECK_TYPES);
        options.setSuppressWarning(DiagnosticGroups.PARSE_ERRORS);
        options.setUseOptimizationLoop(true);
        // Allow all optimizations
        options.setAmbiguateProperties(false);
        options.setComputeFunctionSideEffects(false);
        options.setCrossModuleCodeMotion(false);
        options.setCrossModuleMethodMotion(false);
        options.setDecomposeExpressions(false);
        options.setDevirtualizePrototypeMethods(false);
        options.setExternExports(false);
        options.setExtractPrototypeMemberDeclarations(false);
        options.setGatherRawExports(false);
        options.setGatherSideEffects(false);
        options.setJsDocWarnings(false);
        options.setMarkNoSideEffectCalls(false);
        options.setMisplacedTypeAnnotations(false);
        options.setReplaceIdGenerator(false);
        options.setResolveDeclaredTypeVariables(false);
        options.setRewriteFunctionExpressions(false);
        options.setShadowVariables(false);
        options.setStrictMode(false);
        options.setTestMode(false);
        options.setUseLocalStorage(false);
        options.setUseNewCodeGenerator(true);
        options.setWarningLevel(DiagnosticGroups.CHECK_NON_STATIC_INIT, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.CHECK_REG_EXP, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.CHECK_STRING_IS, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.CHECK_SUSPICIOUS_CODE, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.CHECK_USELESS_CODE, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLE_ARGUMENTS, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.DEBUGGER_STATEMENT, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.EXTRA_REQUIRE, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.LINT, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.MISSING_GOOG, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.MISSING_RETURN, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.MISSING_SEMICOLON, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.LATE_PROVIDE, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.PRIVATE, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.STRICT_MODULE_DEP_GRAPH, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.TIGHTEN_TYPES, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.TYPE_INVALIDATION, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLES, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.UNKNOWN_DEFINES, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.VERBOSE, CheckLevel.OFF);
    }

    private String optimize(String js) {
        SourceFile input = SourceFile.fromCode("test.js", js);
        compiler.compile(
                new SourceFile[] { SourceFile.fromCode("externs.js", "// externs") },
                new SourceFile[] { input },
                options);
        return compiler.toSource();
    }

    // -------------------- substring tests --------------------

    @Test
    public void testSubstringBasic() {
        assertEquals("\"a\"", optimize("var x = \"abc\".substring(0, 1);"));
    }

    @Test
    public void testSubstringReversedArgs() {
        // substring(start, end) swaps when start > end.  This is a common bug.
        // Expected: "bc" (substring(1,3) -> "bc"); start=3, end=1 -> swap -> substring(1,3)=="bc"
        assertEquals("\"bc\"", optimize("var x = \"abc\".substring(3, 1);"));
    }

    @Test
    public void testSubstringNegativeStart() {
        // Negative start is treated as 0.
        assertEquals("\"abc\"", optimize("var x = \"abc\".substring(-1, 3);"));
    }

    @Test
    public void testSubstringNegativeEnd() {
        // Negative end is treated as 0, then start>end? start=1,end=-1 -> end=0, so substring(0,1)
        // Actually substring(1,-1) -> end=-1->0, start>end->swap->substring(0,1)=="a"
        assertEquals("\"a\"", optimize("var x = \"abc\".substring(1, -1);"));
    }

    @Test
    public void testSubstringNaN() {
        // NaN is treated as 0.
        assertEquals("\"abc\"", optimize("var x = \"abc\".substring(NaN, 3);"));
    }

    @Test
    public void testSubstringInfinity() {
        // Infinity treated as length.
        assertEquals("\"abc\"", optimize("var x = \"abc\".substring(1, Infinity);"));
    }

    @Test
    public void testSubstringEqualArgs() {
        assertEquals("\"\"", optimize("var x = \"abc\".substring(1, 1);"));
    }

    @Test
    public void testSubstringBothZero() {
        assertEquals("\"\"", optimize("var x = \"abc\".substring(0, 0);"));
    }

    @Test
    public void testSubstringLength() {
        assertEquals("\"abc\"", optimize("var x = \"abc\".substring(0, 3);"));
    }

    @Test
    public void testSubstringOutOfBounds() {
        assertEquals("\"c\"", optimize("var x = \"abc\".substring(2, 10);"));
    }

    @Test
    public void testSubstringNonStringReceiver() {
        // Should not optimize, but we test it doesn't crash and produces a call.
        String result = optimize("var x = (5).substring(0, 1);");
        // Not optimized; keep original call
        org.junit.Assert.assertNotNull(result);
        org.junit.Assert.assertTrue(result.contains("substring"));
    }

    // -------------------- substr tests --------------------

    @Test
    public void testSubstrBasic() {
        assertEquals("\"bc\"", optimize("var x = \"abc\".substr(1, 2);"));
    }

    @Test
    public void testSubstrNegativeStart() {
        // Negative start counts from end: -2 => position 1 -> "bc"
        assertEquals("\"bc\"", optimize("var x = \"abc\".substr(-2, 2);"));
    }

    @Test
    public void testSubstrNegativeLength() {
        // negative length returns empty string
        assertEquals("\"\"", optimize("var x = \"abc\".substr(1, -1);"));
    }

    @Test
    public void testSubstrZeroLength() {
        assertEquals("\"\"", optimize("var x = \"abc\".substr(1, 0);"));
    }

    @Test
    public void testSubstrOmittedLength() {
        // length omitted: go to end
        assertEquals("\"bc\"", optimize("var x = \"abc\".substr(1);"));
    }

    @Test
    public void testSubstrInfinityLength() {
        assertEquals("\"bc\"", optimize("var x = \"abc\".substr(1, Infinity);"));
    }

    @Test
    public void testSubstrStartAfterEnd() {
        assertEquals("\"\"", optimize("var x = \"abc\".substr(10, 1);"));
    }

    // -------------------- slice tests --------------------

    @Test
    public void testSliceBasic() {
        assertEquals("\"bc\"", optimize("var x = \"abc\".slice(1, 3);"));
    }

    @Test
    public void testSliceNegativeStart() {
        assertEquals("\"bc\"", optimize("var x = \"abc\".slice(-2, 3);"));
    }

    @Test
    public void testSliceNegativeEnd() {
        assertEquals("\"ab\"", optimize("var x = \"abc\".slice(0, -1);"));
    }

    @Test
    public void testSliceBothNegative() {
        assertEquals("\"b\"", optimize("var x = \"abc\".slice(-3, -1);"));
    }

    @Test
    public void testSliceStartGreaterEnd() {
        // slice with start > end returns empty string (no swapping)
        assertEquals("\"\"", optimize("var x = \"abc\".slice(2, 1);"));
    }

    // -------------------- indexOf tests --------------------
    // Note: indexOf with constant arguments may be folded.

    @Test
    public void testIndexOfFound() {
        assertEquals("1", optimize("var x = \"abcabc\".indexOf('b');"));
    }

    @Test
    public void testIndexOfNotFound() {
        assertEquals("-1", optimize("var x = \"abc\".indexOf('d');"));
    }

    @Test
    public void testIndexOfWithStartIndex() {
        assertEquals("4", optimize("var x = \"abcabc\".indexOf('b', 2);"));
    }

    @Test
    public void testIndexOfEmptyString() {
        // Empty string always matches at position 0 (or start index if given)
        assertEquals("0", optimize("var x = \"abc\".indexOf('');"));
    }

    @Test
    public void testIndexOfStartLarge() {
        assertEquals("-1", optimize("var x = \"abc\".indexOf('a', 10);"));
    }

    // -------------------- lastIndexOf tests --------------------

    @Test
    public void testLastIndexOfFound() {
        assertEquals("4", optimize("var x = \"abcabc\".lastIndexOf('b');"));
    }

    @Test
    public void testLastIndexOfNotFound() {
        assertEquals("-1", optimize("var x = \"abc\".lastIndexOf('d');"));
    }

    @Test
    public void testLastIndexOfWithStartIndex() {
        assertEquals("1", optimize("var x = \"abcabc\".lastIndexOf('b', 3);"));
    }

    // -------------------- charAt tests --------------------

    @Test
    public void testCharAtBasic() {
        assertEquals("\"b\"", optimize("var x = \"abc\".charAt(1);"));
    }

    @Test
    public void testCharAtOutOfBounds() {
        // out of bounds returns ""
        assertEquals("\"\"", optimize("var x = \"abc\".charAt(10);"));
    }

    @Test
    public void testCharAtNegative() {
        assertEquals("\"\"", optimize("var x = \"abc\".charAt(-1);"));
    }

    @Test
    public void testCharAtNonInteger() {
        // 1.5 -> index 1
        assertEquals("\"b\"", optimize("var x = \"abc\".charAt(1.5);"));
    }

    // -------------------- charCodeAt tests --------------------

    @Test
    public void testCharCodeAtBasic() {
        assertEquals("98", optimize("var x = \"abc\".charCodeAt(1);")); // 'b' ASCII 98
    }

    @Test
    public void testCharCodeAtOutOfBounds() {
        assertEquals("NaN", optimize("var x = \"abc\".charCodeAt(10);"));
    }

    @Test
    public void testCharCodeAtNegative() {
        assertEquals("NaN", optimize("var x = \"abc\".charCodeAt(-1);"));
    }

    // -------------------- toLowerCase / toUpperCase --------------------

    @Test
    public void testToLowerCaseSimple() {
        assertEquals("\"abc\"", optimize("var x = \"ABC\".toLowerCase();"));
    }

    @Test
    public void testToLowerCaseAlreadyLower() {
        assertEquals("\"abc\"", optimize("var x = \"abc\".toLowerCase();"));
    }

    @Test
    public void testUpperCaseSimple() {
        assertEquals("\"ABC\"", optimize("var x = \"abc\".toUpperCase();"));
    }

    // -------------------- trim tests --------------------

    @Test
    public void testTrimNoSpaces() {
        assertEquals("\"abc\"", optimize("var x = \"abc\".trim();"));
    }

    @Test
    public void testTrimLeadingSpaces() {
        assertEquals("\"abc\"", optimize("var x = \"   abc\".trim();"));
    }

    @Test
    public void testTrimTrailingSpaces() {
        assertEquals("\"abc\"", optimize("var x = \"abc   \".trim();"));
    }

    @Test
    public void testTrimBoth() {
        assertEquals("\"abc\"", optimize("var x = \"   abc   \".trim();"));
    }

    @Test
    public void testTrimOnlySpaces() {
        assertEquals("\"\"", optimize("var x = \"   \".trim();"));
    }

    // -------------------- replace tests (simple) --------------------

    @Test
    public void testReplaceStringPattern() {
        assertEquals("\"aXc\"", optimize("var x = \"abc\".replace('b', 'X');"));
    }

    @Test
    public void testReplaceStringPatternNotFound() {
        assertEquals("\"abc\"", optimize("var x = \"abc\".replace('d', 'X');"));
    }

    @Test
    public void testReplaceRegexPattern() {
        // Regex without g replaces first only
        assertEquals("\"Xbc\"", optimize("var x = \"abc\".replace(/a/, 'X');"));
    }

    @Test
    public void testReplaceRegexGlobal() {
        assertEquals("\"XbX\"", optimize("var x = \"aba\".replace(/a/g, 'X');"));
    }

    @Test
    public void testReplaceEmptyStringPattern() {
        // Empty string inserts replacement at beginning
        assertEquals("\"Xabc\"", optimize("var x = \"abc\".replace('', 'X');"));
    }

    // -------------------- split tests (simple) --------------------

    @Test
    public void testSplitBasic() {
        String result = optimize("var x = \"a b c\".split(' ');");
        // Might be optimized to array literal
        org.junit.Assert.assertNotNull(result);
        org.junit.Assert.assertTrue(result.contains("'a'") && result.contains("'b'") && result.contains("'c'"));
    }

    @Test
    public void testSplitEmptySeparator() {
        // split('') splits into characters
        String result = optimize("var x = \"abc\".split('');");
        org.junit.Assert.assertTrue(result.contains("'a'") && result.contains("'b'") && result.contains("'c'"));
    }

    @Test
    public void testSplitNoMatch() {
        String result = optimize("var x = \"abc\".split('d');");
        // Returns array with the whole string
        org.junit.Assert.assertTrue(result.contains("'abc'"));
    }

    // -------------------- join tests (simple) --------------------

    @Test
    public void testJoinBasic() {
        assertEquals("\"a-b\"", optimize("var x = ['a','b'].join('-'));"));
    }

    @Test
    public void testJoinDefaultSeparator() {
        // join() default separator is comma
        assertEquals("\"a,b\"", optimize("var x = ['a','b'].join();"));
    }

    @Test
    public void testJoinEmptyArray() {
        assertEquals("\"\"", optimize("var x = [].join('x');"));
    }

    // -------------------- Miscellaneous edge cases --------------------

    @Test
    public void testOptimizationOnUndefinedReceiver() {
        // Should not crash; undefined method call stays unchanged
        String result = optimize("var x = undefined.substring(0,1);");
        org.junit.Assert.assertNotNull(result);
    }

    @Test
    public void testOptimizationOnNullReceiver() {
        // Should not crash
        String result = optimize("var x = null.substring(0,1);");
        org.junit.Assert.assertNotNull(result);
    }

    @Test
    public void testOptimizationOnNumberReceiver() {
        // 'use string method on number - not really valid, but should not crash
        String result = optimize("var x = (123).substring(0,1);");
        org.junit.Assert.assertNotNull(result);
    }

    @Test
    public void testNonConstantStringArgument() {
        // If argument is not constant, the optimization should not replace or should partially fold
        String result = optimize("var y = 'a'; var x = \"abc\".substring(y, 2);");
        // Not simplified to a constant string
        org.junit.Assert.assertTrue(result.contains("substring"));
    }

    @Test
    public void testStringLengthProperty() {
        // length property might be folded
        assertEquals("3", optimize("var x = \"abc\".length;"));
    }

    @Test
    public void testEmptyStringLength() {
        assertEquals("0", optimize("var x = \"\".length;"));
    }

    @Test
    public void testCharAtWithExactCharCode() {
        // Additional coverage for single character charCodeAt
        assertEquals("97", optimize("var x = 'a'.charCodeAt(0);"));
    }

    @Test
    public void testSubstringWithUndefinedArgs() {
        // undefined is treated as 0
        assertEquals("\"abc\"", optimize("var x = \"abc\".substring(undefined, 3);"));
    }

    @Test
    public void testSubstringWithOneArg() {
        // one arg: substring(start) goes to end
        assertEquals("\"bc\"", optimize("var x = \"abc\".substring(1);"));
    }

    @Test
    public void testSubstrWithOnlyStart() {
        assertEquals("\"bc\"", optimize("var x = \"abc\".substr(1);"));
    }

    @Test
    public void testSliceWithOneArg() {
        assertEquals("\"bc\"", optimize("var x = \"abc\".slice(1);"));
    }
}