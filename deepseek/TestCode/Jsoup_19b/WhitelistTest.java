package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class WhitelistTest {

    private static boolean isSafe(Whitelist w, String tagName, String attrName, String attrValue) {
        Element el = Jsoup.parse("<" + tagName + " " + attrName + "=\"" + attrValue + "\">x</" + tagName + ">")
                .select(tagName).first();
        return w.isSafeAttribute(tagName, el, el.attributes().asList().get(0));
    }

    @Test
    public void testNoneWhitelist() {
        Whitelist w = Whitelist.none();
        assertFalse(w.isSafeTag("a"));
        assertFalse(w.isSafeTag("script"));
        assertFalse(w.isSafeTag("img"));
    }

    @Test
    public void testSimpleTextWhitelist() {
        Whitelist w = Whitelist.simpleText();
        assertTrue(w.isSafeTag("b"));
        assertTrue(w.isSafeTag("em"));
        assertTrue(w.isSafeTag("i"));
        assertTrue(w.isSafeTag("strong"));
        assertTrue(w.isSafeTag("u"));
        assertFalse(w.isSafeTag("a"));
    }

    @Test
    public void testBasicWhitelist() {
        Whitelist w = Whitelist.basic();
        assertTrue(w.isSafeTag("a"));
        assertTrue(w.isSafeTag("p"));
        assertTrue(w.isSafeTag("blockquote"));
        assertFalse(w.isSafeTag("script"));
    }

    @Test
    public void testRelaxedWhitelist() {
        Whitelist w = Whitelist.relaxed();
        assertTrue(w.isSafeTag("a"));
        assertTrue(w.isSafeTag("img"));
        assertFalse(w.isSafeTag("script"));
    }

    @Test
    public void testAddTags() {
        Whitelist w = Whitelist.none();
        w.addTags("customTag1", "customTag2");
        assertTrue(w.isSafeTag("customTag1"));
        assertTrue(w.isSafeTag("customTag2"));
        assertFalse(w.isSafeTag("a"));
    }

    @Test
    public void testRemoveTags() {
        Whitelist w = Whitelist.none();
        w.addTags("a", "b");
        w.removeTags("a");
        assertFalse(w.isSafeTag("a"));
        assertTrue(w.isSafeTag("b"));

        w.removeTags("notpresent");
        assertTrue(w.isSafeTag("b"));
    }

    @Test
    public void testAddAttributes() {
        Whitelist w = Whitelist.none();
        w.addTags("a");

        assertFalse(isSafe(w, "a", "href", "http://example.com"));

        w.addAttributes("a", "href", "title");

        assertTrue(isSafe(w, "a", "href", "http://example.com"));
        assertTrue(isSafe(w, "a", "title", "Example"));
        assertFalse(isSafe(w, "a", "class", "x"));
    }

    @Test
    public void testRemoveAttributes() {
        Whitelist w = Whitelist.none();
        w.addTags("a");
        w.addAttributes("a", "href", "title");

        w.removeAttributes("a", "href");

        assertFalse(isSafe(w, "a", "href", "http://example.com"));
        assertTrue(isSafe(w, "a", "title", "Example"));

        w.removeAttributes("a", "title");
        assertFalse(isSafe(w, "a", "title", "Example"));

        w.removeAttributes("missingTag", "href");
    }

    @Test
    public void testAttributesWithNoProtocolsAreUnrestricted() {
        Whitelist w = Whitelist.none();
        w.addTags("a");
        w.addAttributes("a", "title");

        assertTrue(isSafe(w, "a", "title", "javascript:alert(1)"));
    }

    @Test
    public void testProtocols() {
        Whitelist w = Whitelist.none();
        w.addTags("a");
        w.addAttributes("a", "href");
        w.addProtocols("a", "href", "http", "https");

        assertTrue(isSafe(w, "a", "href", "http://example.com"));
        assertTrue(isSafe(w, "a", "href", "https://example.com"));
        assertFalse(isSafe(w, "a", "href", "ftp://example.com"));
        assertFalse(isSafe(w, "a", "href", "javascript:alert(1)"));
    }

    @Test
    public void testProtocolsAreCaseInsensitive() {
        Whitelist w = Whitelist.none();
        w.addTags("a");
        w.addAttributes("a", "href");
        w.addProtocols("a", "href", "http", "https", "mailto");

        assertTrue(isSafe(w, "a", "href", "HTTP://EXAMPLE.COM"));
        assertTrue(isSafe(w, "a", "href", "HttPs://example.com"));
        assertTrue(isSafe(w, "a", "href", "MAILTO:test@example.com"));
    }

    @Test
    public void testRemoveProtocols() {
        Whitelist w = Whitelist.none();
        w.addTags("a");
        w.addAttributes("a", "href");
        w.addProtocols("a", "href", "http", "https");

        assertTrue(isSafe(w, "a", "href", "http://example.com"));
        assertTrue(isSafe(w, "a", "href", "https://example.com"));

        w.removeProtocols("a", "href", "http");

        assertFalse(isSafe(w, "a", "href", "http://example.com"));
        assertTrue(isSafe(w, "a", "href", "https://example.com"));

        w.removeProtocols("missingTag", "href", "http");
        w.removeProtocols("a", "missingKey", "http");
    }

    @Test
    public void testEnforcedAttributes() {
        Whitelist w = Whitelist.none();
        w.addTags("a");
        w.addEnforcedAttribute("a", "rel", "nofollow");
        w.addEnforcedAttribute("a", "target", "_blank");

        Attributes enforced = w.getEnforcedAttributes("a");
        assertEquals(2, enforced.size());
        assertEquals("nofollow", enforced.get("rel"));
        assertEquals("_blank", enforced.get("target"));
    }

    @Test
    public void testGetEnforcedAttributesForUnknownTag() {
        Whitelist w = Whitelist.none();
        assertEquals(0, w.getEnforcedAttributes("a").size());
    }

    @Test
    public void testRemoveEnforcedAttribute() {
        Whitelist w = Whitelist.none();
        w.addTags("a");
        w.addEnforcedAttribute("a", "rel", "nofollow");
        w.addEnforcedAttribute("a", "target", "_blank");

        w.removeEnforcedAttribute("a", "rel");

        Attributes enforced = w.getEnforcedAttributes("a");
        assertEquals(1, enforced.size());
        assertEquals("_blank", enforced.get("target"));

        w.removeEnforcedAttribute("a", "target");
        assertEquals(0, w.getEnforcedAttributes("a").size());

        w.removeEnforcedAttribute("missingTag", "rel");
    }

    @Test
    public void testBasicAllowsUppercaseProtocols() {
        Whitelist w = Whitelist.basic();
        assertTrue(isSafe(w, "a", "href", "HTTP://EXAMPLE.COM"));
        assertTrue(isSafe(w, "a", "href", "https://example.com"));
        assertTrue(isSafe(w, "a", "href", "mailto:test@example.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagsRejectsNullTag() {
        Whitelist.none().addTags((String) null);
    }
}