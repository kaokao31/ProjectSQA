package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

public class CodeGeneratorTest {

    @Test
    public void testIsSimpleizesToTrueForValidIdentifiers() {
        assertTrue(CodeGenerator.isSimpleName("a"));
        assertTrue(CodeGenerator.isSimpleName("foo"));
        assertTrue(CodeGenerator.isSimpleName("foo$bar"));
        assertTrue(CodeGenerator.isSimpleName("_baz"));
        assertTrue(CodeGenerator.isSimpleName("foo123"));
    }

    @Test
    public void testIsSimpleizesToFalseForNonIdentifiers() {
        assertFalse(CodeGenerator.isSimpleName(null));
        assertFalse(CodeGenerator.isSimpleName(""));
        assertFalse(CodeGenerator.isSimpleName("123foo"));
        assertFalse(CodeGenerator.isSimpleName("foo.bar"));
        assertFalse(CodeGenerator.isSimpleName("foo-bar"));
        assertFalse(CodeGenerator.isSimpleName("foo bar"));
    }

    @Test
    public void testCostCheck() {
        // Basic tests to exercise CodeGenerator helper utilities and methods if accessible
        assertNotNull(CodeGenerator.jsString("test", null));
        assertNotNull(CodeGenerator.identifierEscape("test"));
    }
}