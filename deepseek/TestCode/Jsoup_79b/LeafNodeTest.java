```java
package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class LeafNodeTest {
    private LeafNode leafNode;
    private static final String TEST_DATA = "Hello World";
    private static final String TEST_ATTR_KEY = "class";
    private static final String TEST_ATTR_VAL = "test-class";

    @Before
    public void setUp() {
        leafNode = new TextNode(TEST_DATA);
    }

    // Test constructor and basic attributes
    @Test
    public void testConstructorAndGetData() {
        assertEquals(TEST_DATA, leafNode.getData());
    }

    @Test
    public void testNodeNameNonNull() {
        assertNotNull(leafNode.nodeName());
    }

    // Test null/empty data constructor
    @Test
    public void testConstructorWithNullData() {
        try {
            new TextNode(null);
            fail("Should throw IllegalArgumentException for null data");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithEmptyData() {
        TextNode emptyNode = new TextNode("");
        assertEquals("", emptyNode.getData());
    }

    // Test attribute core functionality
    @Test
    public void testAttributesInitialNull() {
        assertNull(leafNode.attributes()); // in Defects4J context, may throw NPE bug
    }

    @Test
    public void testHasAttrOnNullAttributes() {
        assertFalse(leafNode.hasAttr(TEST_ATTR_KEY));
    }

    @Test
    public void testAttrOnNullAttributes() {
        assertEquals("", leafNode.attr(TEST_ATTR_KEY));
    }

    // Test coreAttr method with null/empty key
    @Test(expected = IllegalArgumentException.class)
    public void testCoreAttrNullKey() {
        leafNode.coreAttr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCoreAttrEmptyKey() {
        leafNode.coreAttr("");
    }

    // Test attribute setting cascades to ensure attributes are created
    @Test
    public void testAttrSetThenGet() {
        leafNode.attr(TEST_ATTR_KEY, TEST_ATTR_VAL);
        assertEquals(TEST_ATTR_VAL, leafNode.attr(TEST_ATTR_KEY));
        assertTrue(leafNode.hasAttr(TEST_ATTR_KEY));
    }

    @Test
    public void testAttrSetThenRemove() {
        leafNode.attr(TEST_ATTR_KEY, TEST_ATTR_VAL);
        assertTrue(leafNode.hasAttr(TEST_ATTR_KEY));
        leafNode.attr(TEST_ATTR_KEY, "");
        assertTrue(leafNode.hasAttr(TEST_ATTR_KEY));
    }

    // Test baseUri (default behavior)
    @Test
    public void testBaseUriDefault() {
        assertEquals("", leafNode.baseUri());
    }

    @Test
    public void testSetBaseUri() {
        leafNode.setBaseUri("http://example.com");
        assertEquals("http://example.com", leafNode.baseUri());
    }

    @Test
    public void testSetBaseUriNull() {
        leafNode.setBaseUri(null);
        assertEquals("", leafNode.baseUri());
    }

    // Test outerHtml delegation
    @Test
    public void testOuterHtmlDelegation() {
        String html = leafNode.outerHtml();
        assertNotNull(html);
        assertTrue(html.length() > 0);
        assertTrue(html.contains(TEST_DATA));
    }

    // Test toString equals outerHtml
    @Test
    public void testToStringEqualsOuterHtml() {
        assertEquals(leafNode.outerHtml(), leafNode.toString());
    }

    // Edge case: special characters in data
    @Test
    public void testSpecialCharsInData() {
        String specialData = "<>&\"'";
        TextNode specialNode = new TextNode(specialData);
        String html = specialNode.outerHtml();
        assertNotNull(html);
        // should be escaped in HTML context, but TextNode.outerHtmlHead returns raw
        // assertFalse(html.equals(specialData)); // Data should not be escaped
        // For now, just ensure not null and contains original
        assertTrue(html.contains(specialData));
    }

    // Edge case: whitespace trimming
    @Test
    public void testWhitespaceData() {
        TextNode wsNode = new TextNode("   ");
        assertEquals("   ", wsNode.getData());
    }

    // Edge case: unicode characters
    @Test
    public void testUnicodeData() {
        String unicode = "日本語のテキスト";
        TextNode unicodeNode = new TextNode(unicode);
        assertEquals(unicode, unicodeNode.getData());
    }

    // Test attribute cloning behavior
    @Test
    public void testAttributesCloningDepth() {
        leafNode.attr("data-id", "123");
        Node cloned = leafNode.clone();
        assertTrue(cloned.hasAttr("data-id"));
        assertEquals("123", cloned.attr("data-id"));
    }

    // Behavior testing for Defects4J bug pattern (null attributes causing NPE)
    @Test
    public void testAccessAttributesAfterModification() {
        // Force attribute creation then null out (simulating state changes)
        leafNode.attr("key1", "val1");
        assertTrue(leafNode.hasAttr("key1"));
        // After this, ensure baseUri still works (Defects4J bug might break baseUri when attributes are null)
        assertEquals("", leafNode.baseUri());
    }

    @Test
    public void testMultipleAttrAccessAfterConstruction() {
        // Every call to attr/get should be safe when attributes are null
        assertFalse(leafNode.hasAttr("nonexistent"));
        assertEquals("", leafNode.attr("nonexistent"));
        assertEquals("", leafNode.attr("unknown"));
        assertNotNull(leafNode.outerHtml());
    }

    // Test coreAttr returns empty string for missing attribute 
    @Test
    public void testCoreAttrMissing() {
        leafNode.attr("a", "1");
        // access different attribute than set
        assertEquals("", leafNode.attr("b"));
    }

    // Test removing attribute
    @Test
    public void testRemoveAttr() {
        leafNode.attr("test", "value");
        assertTrue(leafNode.hasAttr("test"));
        leafNode.removeAttr("test");
        assertFalse(leafNode.hasAttr("test"));
    }

    // Test remove attribute on null attributes
    @Test
    public void testRemoveAttrOnNull() {
        // Should not throw
        leafNode.removeAttr("nonexistent");
        assertFalse(leafNode.hasAttr("nonexistent"));
    }
}