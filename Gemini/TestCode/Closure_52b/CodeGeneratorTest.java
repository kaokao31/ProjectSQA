package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for CodeGenerator (specifically targeting hex/decimal formatting logic common in Closure 52).
 */
public class CodeGeneratorTest {

    @Test
    public void testIsSimpleNumber() {
        // Test various string inputs to check simple number classification
        assertTrue(CodeGenerator.isSimpleNumber("0"));
        assertTrue(CodeGenerator.isSimpleNumber("9"));
        assertTrue(CodeGenerator.isSimpleNumber("123"));
        
        assertFalse(CodeGenerator.isSimpleNumber(""));
        assertFalse(CodeGenerator.isSimpleNumber("012")); // Octal-like in JS or leading zero
        assertFalse(CodeGenerator.isSimpleNumber("12.3"));
        assertFalse(CodeGenerator.isSimpleNumber("1e3"));
        assertFalse(CodeGenerator.isSimpleNumber("a123"));
        assertFalse(CodeGenerator.isSimpleNumber("-123"));
    }

    @Test
    public void testIsHexDigit() {
        assertTrue(CodeGenerator.isHexDigit('0'));
        assertTrue(CodeGenerator.isHexDigit('9'));
        assertTrue(CodeGenerator.isHexDigit('a'));
        assertTrue(CodeGenerator.isHexDigit('f'));
        assertTrue(CodeGenerator.isHexDigit('A'));
        assertTrue(CodeGenerator.isHexDigit('F'));

        assertFalse(CodeGenerator.isHexDigit('g'));
        assertFalse(CodeGenerator.isHexDigit('/'));
        assertFalse(CodeGenerator.isHexDigit(':'));
        assertFalse(CodeGenerator.isHexDigit('`'));
        assertFalse(CodeGenerator.isHexDigit('G'));
    }

    @Test
    public void testIdentifier() {
        // Basic identifier escaping/validation tests if exposed or through simple generation
        // CodeGenerator often handles escaping strings and identifiers.
        // Let's verify safe identifier checks or escaping methods if available.
        boolean isIdentifier = CodeGenerator.isIdentifier("validIdent");
        assertTrue(isIdentifier);

        boolean isInvalidIdentifier = CodeGenerator.isIdentifier("invalid-ident");
        assertFalse(isInvalidIdentifier);
    }
}