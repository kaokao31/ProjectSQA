package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for org.jsoup.nodes.Attribute.
 * Designed to achieve high code coverage and detect faults,
 * specifically targeting the bug in Defects4J Jsoup-85.
 */
public class AttributeTest {

    private Attribute attrWithValue;
    private Attribute attrEmptyValue;
    private Attribute attrNullValue;
    private Document.OutputSettings settings;

    @Before
    public void setUp() {
        attrWithValue = new Attribute("key", "value");
        attrEmptyValue = new Attribute("key", "");
        attrNullValue = new Attribute("key", null);
        settings = new Document("").outputSettings();
    }

    // --- Constructor tests ---

    @Test(expected = IllegalArgumentException.class)
    public void constructorNullKeyThrows() {
        new Attribute(null, "value");
    }

    @Test
    public void constructorTrimsAndLowercasesKey() {
        Attribute attr = new Attribute("  KEY  ", "val");
        assertEquals("key", attr.getKey());
    }

    @Test
    public void constructorAllowsEmptyKey() {
        Attribute attr = new Attribute("", "val");
        assertEquals("", attr.getKey());
    }

    @Test
    public void constructorAllowsNullValue() {
        Attribute attr = new Attribute("key", null);
        assertNull(attr.getValue());
    }

    @Test
    public void constructorAllowsEmptyValue() {
        Attribute attr = new Attribute("key", "");
        assertEquals("", attr.getValue());
    }

    // --- getKey / getValue ---

    @Test
    public void getKeyReturnsKey() {
        assertEquals("key", attrWithValue.getKey());
    }

    @Test
    public void getValueReturnsValue() {
        assertEquals("value", attrWithValue.getValue());
    }

    // --- setValue ---

    @Test
    public void setValueReturnsOldValue() {
        String old = attrWithValue.setValue("newValue");
        assertEquals("value", old);
    }

    @Test
    public void setValueUpdatesValue() {
        attrWithValue.setValue("newValue");
        assertEquals("newValue", attrWithValue.getValue());
    }

    @Test
    public void setValueAllowsNull() {
        attrWithValue.setValue(null);
        assertNull(attrWithValue.getValue());
    }

    @Test
    public void setValueAllowsEmpty() {
        attrWithValue.setValue("");
        assertEquals("", attrWithValue.getValue());
    }

    // --- html() method (bug target) ---

    @Test
    public void htmlWithValueIncludesEqualsAndQuotes() {
        assertEquals("key=\"value\"", attrWithValue.html());
    }

    @Test
    public void htmlWithEmptyValueOmitsEqualsAndQuotes() {
        // This test targets the bug in Jsoup-85: empty value should not produce key=""
        assertEquals("key", attrEmptyValue.html());
    }

    @Test
    public void htmlWithNullValueOmitsEqualsAndQuotes() {
        // Null value should also omit equals and quotes (consistent with empty)
        assertEquals("key", attrNullValue.html());
    }

    @Test
    public void htmlWithSpecialCharactersEscapes() {
        Attribute attr = new Attribute("key", "a\"b&c<d>e");
        assertEquals("key=\"a\\\"b&amp;c&lt;d&gt;e\"", attr.html());
    }

    @Test
    public void htmlWithSpacesInValue() {
        Attribute attr = new Attribute("key", "hello world");
        assertEquals("key=\"hello world\"", attr.html());
    }

    // --- html(Appendable, OutputSettings) ---

    @Test
    public void htmlAppendableWithValue() throws IOException {
        StringBuilder sb = new StringBuilder();
        attrWithValue.html(sb, settings);
        assertEquals("key=\"value\"", sb.toString());
    }

    @Test
    public void htmlAppendableWithEmptyValue() throws IOException {
        StringBuilder sb = new StringBuilder();
        attrEmptyValue.html(sb, settings);
        assertEquals("key", sb.toString());
    }

    @Test
    public void htmlAppendableWithNullValue() throws IOException {
        StringBuilder sb = new StringBuilder();
        attrNullValue.html(sb, settings);
        assertEquals("key", sb.toString());
    }

    // --- toString() ---

    @Test
    public void toStringEqualsHtml() {
        assertEquals(attrWithValue.html(), attrWithValue.toString());
        assertEquals(attrEmptyValue.html(), attrEmptyValue.toString());
        assertEquals(attrNullValue.html(), attrNullValue.toString());
    }

    // --- equals and hashCode ---

    @Test
    public void equalsSameObject() {
        assertTrue(attrWithValue.equals(attrWithValue));
    }

    @Test
    public void equalsNullReturnsFalse() {
        assertFalse(attrWithValue.equals(null));
    }

    @Test
    public void equalsDifferentTypeReturnsFalse() {
        assertFalse(attrWithValue.equals("string"));
    }

    @Test
    public void equalsSameKeyAndValueReturnsTrue() {
        Attribute other = new Attribute("key", "value");
        assertTrue(attrWithValue.equals(other));
    }

    @Test
    public void equalsDifferentKeyReturnsFalse() {
        Attribute other = new Attribute("otherKey", "value");
        assertFalse(attrWithValue.equals(other));
    }

    @Test
    public void equalsDifferentValueReturnsFalse() {
        Attribute other = new Attribute("key", "otherValue");
        assertFalse(attrWithValue.equals(other));
    }

    @Test
    public void equalsNullValueVsEmptyValue() {
        // null and empty are considered different
        assertFalse(attrNullValue.equals(attrEmptyValue));
    }

    @Test
    public void hashCodeConsistentWithEquals() {
        Attribute other = new Attribute("key", "value");
        assertEquals(attrWithValue.hashCode(), other.hashCode());
    }

    @Test
    public void hashCodeDifferentForDifferentKey() {
        Attribute other = new Attribute("otherKey", "value");
        assertNotEquals(attrWithValue.hashCode(), other.hashCode());
    }

    // --- clone ---

    @Test
    public void cloneProducesEqualButNotSame() {
        Attribute clone = attrWithValue.clone();
        assertNotSame(attrWithValue, clone);
        assertEquals(attrWithValue, clone);
    }

    @Test
    public void cloneWithEmptyValue() {
        Attribute clone = attrEmptyValue.clone();
        assertEquals(attrEmptyValue, clone);
    }

    @Test
    public void cloneWithNullValue() {
        Attribute clone = attrNullValue.clone();
        assertEquals(attrNullValue, clone);
    }

    // --- Edge cases for key trimming and lowercasing ---

    @Test
    public void keyTrimmedAndLowercasedInEquals() {
        Attribute attr1 = new Attribute("  Key  ", "val");
        Attribute attr2 = new Attribute("key", "val");
        assertEquals(attr1, attr2);
    }

    @Test
    public void keyCaseInsensitiveInHashCode() {
        Attribute attr1 = new Attribute("KEY", "val");
        Attribute attr2 = new Attribute("key", "val");
        assertEquals(attr1.hashCode(), attr2.hashCode());
    }
}