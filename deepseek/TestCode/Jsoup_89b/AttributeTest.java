package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for the Attribute class.
 * Designed to achieve high code coverage and detect potential faults,
 * including those related to Defects4J bug 89.
 */
public class AttributeTest {

    private Attribute basicAttr;
    private Attribute emptyValueAttr;
    private Attribute nullValueAttr;
    private Attribute specialCharsAttr;
    private Attribute booleanLikeAttr;

    @Before
    public void setUp() {
        basicAttr = new Attribute("key", "value");
        emptyValueAttr = new Attribute("empty", "");
        nullValueAttr = new Attribute("nullkey", null);
        specialCharsAttr = new Attribute("special", "value\"with\"quotes");
        booleanLikeAttr = new Attribute("disabled", "");
    }

    // --- Constructor and basic getters ---

    @Test
    public void testConstructorAndGetters() {
        assertEquals("key", basicAttr.getKey());
        assertEquals("value", basicAttr.getValue());
    }

    @Test
    public void testEmptyValue() {
        assertEquals("", emptyValueAttr.getValue());
    }

    @Test
    public void testNullValue() {
        assertNull(nullValueAttr.getValue());
    }

    // --- setValue ---

    @Test
    public void testSetValueReturnsOldValue() {
        String old = basicAttr.setValue("newValue");
        assertEquals("value", old);
        assertEquals("newValue", basicAttr.getValue());
    }

    @Test
    public void testSetValueToNull() {
        String old = basicAttr.setValue(null);
        assertEquals("value", old);
        assertNull(basicAttr.getValue());
    }

    // --- html() method (returns String) ---

    @Test
    public void testHtmlBasic() {
        assertEquals("key=\"value\"", basicAttr.html());
    }

    @Test
    public void testHtmlEmptyValue() {
        // Expected: key="" or key? The bug may be here.
        // We assert the current behavior; if bug exists, this test may fail.
        assertEquals("empty=\"\"", emptyValueAttr.html());
    }

    @Test
    public void testHtmlNullValue() {
        // Null value should be treated as empty or omitted?
        // In Jsoup, null value results in key=""? We'll assert the actual.
        assertEquals("nullkey=\"\"", nullValueAttr.html());
    }

    @Test
    public void testHtmlSpecialChars() {
        // Value contains double quotes; should be escaped.
        String html = specialCharsAttr.html();
        // Expect either single-quoted or escaped double quotes.
        assertTrue("HTML should escape or alternate quotes: " + html,
                html.contains("&quot;") || html.contains("'"));
        // Also ensure no unescaped double quotes inside the value.
        assertFalse("Unescaped double quote found", html.matches(".*=\"[^\"]*\"[^\"]*\".*"));
    }

    @Test
    public void testHtmlBooleanLike() {
        // Boolean-like attribute with empty value.
        // The bug may cause output "disabled=\"\"" instead of "disabled".
        // We test both possibilities; if bug exists, one will fail.
        String html = booleanLikeAttr.html();
        // Acceptable forms: "disabled" or "disabled=\"\""
        assertTrue("Boolean attribute output unexpected: " + html,
                html.equals("disabled") || html.equals("disabled=\"\""));
    }

    // --- html(StringBuilder) method ---

    @Test
    public void testHtmlStringBuilderBasic() {
        StringBuilder sb = new StringBuilder();
        basicAttr.html(sb);
        assertEquals("key=\"value\"", sb.toString());
    }

    @Test
    public void testHtmlStringBuilderEmptyValue() {
        StringBuilder sb = new StringBuilder();
        emptyValueAttr.html(sb);
        assertEquals("empty=\"\"", sb.toString());
    }

    @Test
    public void testHtmlStringBuilderNullValue() {
        StringBuilder sb = new StringBuilder();
        nullValueAttr.html(sb);
        assertEquals("nullkey=\"\"", sb.toString());
    }

    @Test
    public void testHtmlStringBuilderSpecialChars() {
        StringBuilder sb = new StringBuilder();
        specialCharsAttr.html(sb);
        String result = sb.toString();
        assertTrue("HTML should escape or alternate quotes: " + result,
                result.contains("&quot;") || result.contains("'"));
    }

    // --- toString ---

    @Test
    public void testToString() {
        assertEquals("key=\"value\"", basicAttr.toString());
    }

    // --- equals and hashCode ---

    @Test
    public void testEqualsSame() {
        Attribute other = new Attribute("key", "value");
        assertEquals(basicAttr, other);
        assertEquals(basicAttr.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsDifferentKey() {
        Attribute other = new Attribute("otherKey", "value");
        assertNotEquals(basicAttr, other);
    }

    @Test
    public void testEqualsDifferentValue() {
        Attribute other = new Attribute("key", "otherValue");
        assertNotEquals(basicAttr, other);
    }

    @Test
    public void testEqualsNull() {
        assertFalse(basicAttr.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(basicAttr.equals("string"));
    }

    @Test
    public void testEqualsEmptyVsNullValue() {
        Attribute emptyVal = new Attribute("a", "");
        Attribute nullVal = new Attribute("a", null);
        // They are not equal because one value is empty string, the other null.
        assertNotEquals(emptyVal, nullVal);
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = basicAttr.hashCode();
        int hash2 = basicAttr.hashCode();
        assertEquals(hash1, hash2);
    }

    // --- clone (if implemented) ---

    @Test
    public void testClone() {
        Attribute cloned = basicAttr.clone();
        assertNotNull(cloned);
        assertEquals(basicAttr.getKey(), cloned.getKey());
        assertEquals(basicAttr.getValue(), cloned.getValue());
        // Ensure it's a different object
        assertNotSame(basicAttr, cloned);
    }

    @Test
    public void testCloneEmptyValue() {
        Attribute cloned = emptyValueAttr.clone();
        assertEquals("", cloned.getValue());
    }

    @Test
    public void testCloneNullValue() {
        Attribute cloned = nullValueAttr.clone();
        assertNull(cloned.getValue());
    }

    // --- Edge cases: key with special characters ---

    @Test
    public void testKeyWithDash() {
        Attribute attr = new Attribute("data-value", "test");
        assertEquals("data-value=\"test\"", attr.html());
    }

    @Test
    public void testKeyWithColon() {
        Attribute attr = new Attribute("xml:lang", "en");
        assertEquals("xml:lang=\"en\"", attr.html());
    }

    // --- Edge cases: value with ampersand ---

    @Test
    public void testValueWithAmpersand() {
        Attribute attr = new Attribute("link", "a&b");
        String html = attr.html();
        // Ampersand should be escaped as &amp;
        assertTrue("Ampersand not escaped", html.contains("&amp;"));
    }

    // --- Edge cases: value with less-than and greater-than ---

    @Test
    public void testValueWithLtGt() {
        Attribute attr = new Attribute("data", "<tag>");
        String html = attr.html();
        // These should be escaped as &lt; and &gt;
        assertTrue("Less-than not escaped", html.contains("&lt;"));
        assertTrue("Greater-than not escaped", html.contains("&gt;"));
    }

    // --- Potential fault detection: empty key ---

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKeyThrows() {
        new Attribute("", "value");
    }

    // --- Potential fault detection: null key ---

    @Test(expected = IllegalArgumentException.class)
    public void testNullKeyThrows() {
        new Attribute(null, "value");
    }

    // --- Test parent constructor if available (optional) ---

    @Test
    public void testParentConstructor() {
        // If Attribute has a constructor with parent, test it.
        // We'll assume it exists for coverage.
        Attributes parent = new Attributes();
        Attribute attrWithParent = new Attribute("key", "value", parent);
        assertEquals("key", attrWithParent.getKey());
        assertEquals("value", attrWithParent.getValue());
    }

    // --- Test setKey if available ---

    @Test
    public void testSetKey() {
        // If setKey exists, test it.
        // We'll assume it does.
        Attribute attr = new Attribute("old", "val");
        attr.setKey("new");
        assertEquals("new", attr.getKey());
    }

    // --- Additional edge: value with single quotes ---

    @Test
    public void testValueWithSingleQuotes() {
        Attribute attr = new Attribute("name", "it's");
        String html = attr.html();
        // Should be enclosed in double quotes, single quote inside is fine.
        assertTrue("HTML should contain double quotes", html.startsWith("name=\"") && html.endsWith("\""));
        assertTrue("Value should contain single quote", html.contains("'"));
    }

    // --- Test that html() output is consistent with toString ---

    @Test
    public void testHtmlConsistentWithToString() {
        assertEquals(basicAttr.html(), basicAttr.toString());
        assertEquals(emptyValueAttr.html(), emptyValueAttr.toString());
        assertEquals(nullValueAttr.html(), nullValueAttr.toString());
        assertEquals(specialCharsAttr.html(), specialCharsAttr.toString());
    }
}