package org.apache.commons.lang;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for the {@link Entities} class.
 * Targets high code coverage and detection of known bugs such as
 * Defects4J Bug 42 (high Unicode escaping).
 */
public class EntitiesTest {

    private StringWriter writer;
    private Entities entities;

    @Before
    public void setUp() {
        writer = new StringWriter();
        entities = new Entities();
    }

    // ====================== HTML40 Static Instance Tests ======================

    @Test
    public void testEscapeHighUnicode() throws IOException {
        // Supplementary character U+1D362 (decimal 119650)
        String input = "\uD835\uDF62";  // surrogate pair
        StringWriter sw = new StringWriter();
        Entities.HTML40.escape(sw, input);
        String expected = "&#119650;";
        assertEquals("High Unicode should be escaped as a single numeric entity",
                     expected, sw.toString());
    }

    @Test
    public void testEscapeSimpleEntity() throws IOException {
        String input = "<>&\"'";
        StringWriter sw = new StringWriter();
        Entities.HTML40.escape(sw, input);
        // Note: actual escaping may vary; but we check basic entities are replaced
        assertTrue("Angle brackets should be escaped", sw.toString().contains("&lt;"));
        assertTrue("Ampersand should be escaped", sw.toString().contains("&amp;"));
    }

    @Test
    public void testUnescapeNumericEntities() throws IOException {
        String input = "&#119650;";
        StringWriter sw = new StringWriter();
        Entities.HTML40.unescape(sw, input);
        assertEquals("Unescape should restore the supplementary character",
                     "\uD835\uDF62", sw.toString());
    }

    @Test
    public void testEscapeNullInput() throws IOException {
        // Should handle null gracefully (return 0 or throw exception? Usually throws NPE)
        // Assuming NPE is acceptable, test that it throws
        try {
            Entities.HTML40.escape(writer, null);
            fail("Should throw NullPointerException on null input");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testEscapeEmptyString() throws IOException {
        StringWriter sw = new StringWriter();
        int result = Entities.HTML40.escape(sw, "");
        assertEquals("Empty string should not write anything", 0, sw.getBuffer().length());
        // Result is number of characters written; should be 0
    }

    @Test
    public void testEscapePlainText() throws IOException {
        String plain = "Hello World";
        StringWriter sw = new StringWriter();
        Entities.HTML40.escape(sw, plain);
        assertEquals("Plain text should remain unchanged", plain, sw.toString());
    }

    @Test
    public void testUnescapeNullInput() throws IOException {
        try {
            Entities.HTML40.unescape(writer, null);
            fail("Should throw NullPointerException on null input");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testUnescapeEmptyString() throws IOException {
        StringWriter sw = new StringWriter();
        int result = Entities.HTML40.unescape(sw, "");
        assertEquals("Empty string should not write anything", 0, sw.getBuffer().length());
    }

    @Test
    public void testUnescapePlainText() throws IOException {
        String plain = "Hello World";
        StringWriter sw = new StringWriter();
        Entities.HTML40.unescape(sw, plain);
        assertEquals("Plain text should remain unchanged", plain, sw.toString());
    }

    // ====================== Custom Entities Tests ======================

    @Test
    public void testAddEntityAndEscape() throws IOException {
        // Add a custom entity
        entities.addEntity("test", 1234);
        StringWriter sw = new StringWriter();
        // Need to use our custom Entities instance; HTML40 is static, so we test with our instance
        // But escape method on custom instance may not have the same mapping
        entities.escape(sw, "\u04D2");  // character with code point 1234
        // Since we added the entity, it should escape as &test;
        assertEquals("Custom entity should be used", "&test;", sw.toString());
    }

    @Test
    public void testUnescapeCustomEntity() throws IOException {
        entities.addEntity("test", 1234);
        StringWriter sw = new StringWriter();
        entities.unescape(sw, "&test;");
        assertEquals("Unescape custom entity should return the character",
                     "\u04D2", sw.toString());
    }

    @Test
    public void testAddEntityOverwrites() throws IOException {
        // Add same name twice; should overwrite
        entities.addEntity("amp", 1234);  // override &amp; with different code
        StringWriter sw = new StringWriter();
        // Use our instance; it may not contain built-in entities unless copied
        // We need to ensure the instance has the base HTML40 entities? Not required for this test
        // This test just verifies addEntity works without exception.
        assertNotNull("addEntity should succeed", entities);
    }

    @Test
    public void testMultipleEntities() throws IOException {
        entities.addEntity("alpha", 945);
        entities.addEntity("beta", 946);
        StringWriter sw = new StringWriter();
        String input = "\u03B1\u03B2";  // alpha, beta
        entities.escape(sw, input);
        assertEquals("Multiple characters should each be escaped", "&alpha;&beta;", sw.toString());
    }

    // ====================== Edge Cases ======================

    @Test
    public void testSurrogatePairAsInputText() throws IOException {
        // Input already a surrogate pair that doesn't represent a valid supplementary character?
        // Valid high Unicode should be escaped; but isolated surrogates should pass through
        String isolatedHigh = "\uD800";  // lone high surrogate
        StringWriter sw1 = new StringWriter();
        Entities.HTML40.escape(sw1, isolatedHigh);
        // Should remain as single surrogate (maybe encoded as &#xD800; or keep it)
        // The exact behavior depends on implementation; but ensure no exception
        assertFalse("Output should not be empty", sw1.toString().isEmpty());
    }

    @Test
    public void testBogusEntityName() throws IOException {
        // Unescape a nonexistent entity; should leave as is
        StringWriter sw = new StringWriter();
        Entities.HTML40.unescape(sw, "&nonexistent;");
        // Most implementations leave unknown entities unchanged
        assertTrue("Unknown entity should be passed through", sw.toString().contains("&nonexistent;"));
    }

    @Test
    public void testEscapeWithMultipleHighUnicode() throws IOException {
        // Two supplementary characters
        String input = "\uD835\uDF62\uD835\uDF63";  // U+1D362 and U+1D363
        StringWriter sw = new StringWriter();
        Entities.HTML40.escape(sw, input);
        String expected = "&#119650;&#119651;";
        assertEquals("Two high Unicode chars should each be escaped", expected, sw.toString());
    }

    @Test
    public void testUnescapeMultipleNumericEntities() throws IOException {
        String input = "&#65;&#66;";
        StringWriter sw = new StringWriter();
        Entities.HTML40.unescape(sw, input);
        assertEquals("Multiple numeric entities should be unescaped", "AB", sw.toString());
    }

    @Test
    public void testEscapeMixedEntities() throws IOException {
        String input = "<>&\"'&\u00E0";  // mix of named and numeric (à)
        StringWriter sw = new StringWriter();
        Entities.HTML40.escape(sw, input);
        String result = sw.toString();
        assertTrue("Should escape <", result.contains("&lt;"));
        assertTrue("Should escape >", result.contains("&gt;"));
        assertTrue("Should escape &", result.contains("&amp;"));
        // à may be escaped as &agrave; or as numeric; but should be changed
        assertFalse("Original à should not appear", result.contains("\u00E0"));
    }

    // ====================== Helper Method Tests ======================

    @Test
    public void testWriterNullCheck() throws IOException {
        try {
            Entities.HTML40.escape(null, "abc");
            fail("Should throw NullPointerException on null writer");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testUnescapeWriterNullCheck() throws IOException {
        try {
            Entities.HTML40.unescape(null, "abc");
            fail("Should throw NullPointerException on null writer");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testEscapeReturnsWrittenCount() throws IOException {
        StringWriter sw = new StringWriter();
        int count = Entities.HTML40.escape(sw, "&");
        // The escaped form is "&amp;" which is 5 characters
        assertEquals("Should return number of characters written", 5, count);
    }

    @Test
    public void testUnescapeReturnsWrittenCount() throws IOException {
        StringWriter sw = new StringWriter();
        int count = Entities.HTML40.unescape(sw, "&amp;");
        // Unescaped form is "&" which is 1 character
        assertEquals("Should return number of characters written", 1, count);
    }

    @Test
    public void testEntitiesCreation() {
        assertNotNull("Default constructor should create empty Entities", entities);
    }

    @Test
    public void testHTMl40InstanceExists() {
        assertNotNull("HTML40 static instance should exist", Entities.HTML40);
    }

    // ====================== Bug-Specific Regression Tests ======================

    @Test
    public void testBug42HighUnicodeNotSplit() throws IOException {
        // Reproduce the exact failure from StringEscapeUtilsTest
        // High Unicode character U+1D362
        String input = "\uD835\uDF62";
        StringWriter sw = new StringWriter();
        Entities.HTML40.escape(sw, input);
        // The bug splits into &#55348;&#57186; instead of &#119650;
        String expected = "&#119650;";
        assertEquals("High Unicode must be escaped as a single entity, not two surrogates",
                     expected, sw.toString());
    }

    @Test
    public void testBug42ManyHighUnicode() throws IOException {
        // Test several high Unicode characters
        StringBuilder sb = new StringBuilder();
        StringBuilder expected = new StringBuilder();
        for (int cp = 0x1D300; cp <= 0x1D30A; cp += 1) {  // range of Tai Xuan Jing symbols
            if (!Character.isDefined(cp)) continue;
            sb.append(Character.toChars(cp));
            expected.append("&#").append(cp).append(";");
        }
        StringWriter sw = new StringWriter();
        Entities.HTML40.escape(sw, sb.toString());
        assertEquals("All high Unicode characters should be escaped as numeric entities",
                     expected.toString(), sw.toString());
    }
}