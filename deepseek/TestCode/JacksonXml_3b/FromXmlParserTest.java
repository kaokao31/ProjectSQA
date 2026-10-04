package com.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for FromXmlParser.
 * NOTE: Since the original source was not provided, this test suite assumes a typical XML parser
 * with methods: parse(String), isValid(), getError(), getElementCount(), etc.
 * Adjust package, imports, and method names to match the actual class under test.
 */
public class FromXmlParserTest {

    private FromXmlParser parser;

    @Before
    public void setUp() {
        parser = new FromXmlParser();
    }

    // ---------- Constructor and initial state ----------
    @Test
    public void testInitialState() {
        assertFalse("Parser should not be valid before parsing", parser.isValid());
        assertNull("Error message should be null initially", parser.getError());
        assertEquals("Element count should be 0 initially", 0, parser.getElementCount());
    }

    // ---------- parse() with null input ----------
    @Test(expected = IllegalArgumentException.class)
    public void testParseNullInput() {
        parser.parse(null);
    }

    // ---------- parse() with empty string ----------
    @Test
    public void testParseEmptyString() {
        parser.parse("");
        assertFalse("Empty string should result in invalid parse", parser.isValid());
        assertNotNull("Error should be set for empty input", parser.getError());
        assertEquals("Element count should be 0 after empty parse", 0, parser.getElementCount());
    }

    // ---------- parse() with well-formed XML ----------
    @Test
    public void testParseValidXml() {
        String validXml = "<root><child>text</child></root>";
        parser.parse(validXml);
        assertTrue("Well-formed XML should parse successfully", parser.isValid());
        assertNull("No error should be present for valid XML", parser.getError());
        assertEquals("Should have exactly 2 elements (root, child)", 2, parser.getElementCount());
    }

    // ---------- parse() with malformed XML ----------
    @Test
    public void testParseMalformedXml() {
        String malformedXml = "<root><child>text</root>"; // missing closing tag
        parser.parse(malformedXml);
        assertFalse("Malformed XML should result in invalid parse", parser.isValid());
        assertNotNull("Error should be set for malformed XML", parser.getError());
    }

    // ---------- parse() with empty element ----------
    @Test
    public void testParseEmptyElement() {
        String emptyElementXml = "<root></root>";
        parser.parse(emptyElementXml);
        assertTrue("Empty element should be valid", parser.isValid());
        assertEquals("Should have 1 element (root)", 1, parser.getElementCount());
    }

    // ---------- parse() with attributes ----------
    @Test
    public void testParseWithAttributes() {
        String attrXml = "<root id=\"1\" name=\"test\"><child/></root>";
        parser.parse(attrXml);
        assertTrue("XML with attributes should be valid", parser.isValid());
        assertEquals("Should have 2 elements (root, child)", 2, parser.getElementCount());
    }

    // ---------- parse() with CDATA ----------
    @Test
    public void testParseWithCdata() {
        String cdataXml = "<root><![CDATA[some <data>]]></root>";
        parser.parse(cdataXml);
        assertTrue("XML with CDATA should be valid", parser.isValid());
        assertEquals("Should have 1 element (root)", 1, parser.getElementCount());
    }

    // ---------- parse() with comments ----------
    @Test
    public void testParseWithComments() {
        String commentXml = "<root><!-- comment --><child/></root>";
        parser.parse(commentXml);
        assertTrue("XML with comments should be valid", parser.isValid());
        assertEquals("Comments should not count as elements", 2, parser.getElementCount());
    }

    // ---------- parse() with deep nesting ----------
    @Test
    public void testParseDeepNesting() {
        StringBuilder deepXml = new StringBuilder("<root>");
        for (int i = 0; i < 100; i++) {
            deepXml.append("<level" + i + ">");
        }
        for (int i = 99; i >= 0; i--) {
            deepXml.append("</level" + i + ">");
        }
        deepXml.append("</root>");
        parser.parse(deepXml.toString());
        assertTrue("Deep nesting should be valid", parser.isValid());
        assertEquals("Element count should be 101 (root + 100 levels)", 101, parser.getElementCount());
    }

    // ---------- parse() with special characters ----------
    @Test
    public void testParseSpecialCharacters() {
        String specialXml = "<root>&amp;&lt;&gt;&quot;&apos;</root>";
        parser.parse(specialXml);
        assertTrue("XML with entities should be valid", parser.isValid());
        assertEquals("Should have 1 element (root)", 1, parser.getElementCount());
    }

    // ---------- parse() without root element ----------
    @Test
    public void testParseNoRoot() {
        String noRootXml = "just text";
        parser.parse(noRootXml);
        assertFalse("Non-XML input should be invalid", parser.isValid());
        assertNotNull("Error should be set", parser.getError());
    }

    // ---------- parse() with multiple root attempts ----------
    @Test
    public void testParseMultipleRoots() {
        String multipleRoots = "<a></a><b></b>";
        parser.parse(multipleRoots);
        assertFalse("Multiple roots should be invalid", parser.isValid());
        assertNotNull("Error should be set", parser.getError());
    }

    // ---------- parse() whitespace-only input ----------
    @Test
    public void testParseWhitespace() {
        parser.parse("   ");
        // Depending on implementation, whitespace may be treated as empty or as text
        // Assume it is invalid because no XML structure
        assertFalse("Whitespace-only input should be invalid", parser.isValid());
        assertNotNull("Error should be set", parser.getError());
    }

    // ---------- parse() with BOM ----------
    @Test
    public void testParseWithBom() {
        String bomXml = "\uFEFF<?xml version=\"1.0\"?><root/>";
        parser.parse(bomXml);
        // BOM handling is implementation-specific, but many parsers accept it
        // Assume valid, but may not be; test both branches
        boolean result = parser.isValid();
        if (result) {
            assertEquals("Should have 1 element if BOM accepted", 1, parser.getElementCount());
        } else {
            assertNotNull("Error should be set if BOM rejected", parser.getError());
        }
    }

    // ---------- Multiple parses (reset state) ----------
    @Test
    public void testMultipleParses() {
        parser.parse("<root/>");
        assertTrue(parser.isValid());
        assertEquals(1, parser.getElementCount());

        // Second parse with invalid input should reset state
        parser.parse("invalid");
        assertFalse(parser.isValid());
        assertNull("Error should be null if not set? Actually depends on design", parser.getError());
        assertEquals("State should reset to 0 elements after invalid parse", 0, parser.getElementCount());
    }

    // ---------- parse() with namespace ----------
    @Test
    public void testParseWithNamespace() {
        String nsXml = "<ns:root xmlns:ns=\"http://example.com\"><ns:child/></ns:root>";
        parser.parse(nsXml);
        assertTrue("XML with namespace should be valid", parser.isValid());
        assertEquals("Should have 2 elements", 2, parser.getElementCount());
    }

    // ---------- Edge: extremely long text node ----------
    @Test(timeout = 1000)
    public void testParseLargeTextNode() {
        StringBuilder largeText = new StringBuilder("<root>");
        for (int i = 0; i < 10000; i++) {
            largeText.append("a");
        }
        largeText.append("</root>");
        parser.parse(largeText.toString());
        assertTrue("Large text node should be valid", parser.isValid());
        assertEquals("Should have 1 element", 1, parser.getElementCount());
    }

    // ---------- Edge: invalid XML encoding ----------
    @Test
    public void testParseInvalidEncodingDeclaration() {
        String badEnc = "<?xml version=\"1.0\" encoding=\"invalid-encoding\"?><root/>";
        parser.parse(badEnc);
        // Some parsers reject unknown encodings, some ignore
        // We just check it doesn't throw and returns some state
        boolean valid = parser.isValid();
        if (valid) {
            assertEquals(1, parser.getElementCount());
        } else {
            assertNotNull(parser.getError());
        }
    }

    // ---------- Null after valid parse (coverage of error path) ----------
    @Test
    public void testGetErrorAfterValidParse() {
        parser.parse("<root/>");
        assertTrue(parser.isValid());
        assertNull("getError() should return null after successful parse", parser.getError());
    }

    // ---------- Element count after mixed content ----------
    @Test
    public void testMixedContent() {
        String mixed = "<root>text<child/>more text</root>";
        parser.parse(mixed);
        // Elements: root, child -> 2 (text nodes are not counted as elements)
        assertTrue(parser.isValid());
        assertEquals("Should have 2 elements (root and child)", 2, parser.getElementCount());
    }

    // ---------- Reset after error ----------
    @Test
    public void testResetBehaviour() {
        parser.parse("<root>"); // missing closing tag
        assertFalse(parser.isValid());
        assertNotNull(parser.getError());

        parser.parse("<root/>");
        assertTrue(parser.isValid());
        assertNull(parser.getError());
        assertEquals(1, parser.getElementCount());
    }
}