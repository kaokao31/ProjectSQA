package org.jsoup.safety;

import org.junit.Before;
import org.junit.Test;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.*;

public class WhitelistTest {

    private Whitelist whitelist;

    @Before
    public void setUp() {
        whitelist = new Whitelist();
    }

    @Test
    public void testNone() {
        Whitelist w = Whitelist.none();
        assertFalse(w.isSafeTag("a"));
        assertFalse(w.isSafeAttribute("a", new Element(Tag.valueOf("a"), ""), new Attribute("href", "http://example.com")));
    }

    @Test
    public void testBasic() {
        Whitelist w = Whitelist.basic();
        assertTrue(w.isSafeTag("a"));
        assertTrue(w.isSafeTag("p"));
        assertTrue(w.isSafeTag("img"));
        assertFalse(w.isSafeTag("script"));

        Element a = new Element(Tag.valueOf("a"), "");
        Attribute safeHref = new Attribute("href", "http://example.com");
        Attribute unsafeHref = new Attribute("href", "javascript:alert(1)");
        Attribute title = new Attribute("title", "example");

        assertTrue(w.isSafeAttribute("a", a, safeHref));
        assertFalse(w.isSafeAttribute("a", a, unsafeHref));
        assertTrue(w.isSafeAttribute("a", a, title));
    }

    @Test
    public void testBasicWithImages() {
        Whitelist w = Whitelist.basicWithImages();
        assertTrue(w.isSafeTag("img"));
        Element img = new Element(Tag.valueOf("img"), "");
        assertTrue(w.isSafeAttribute("img", img, new Attribute("src", "http://example.com/img.png")));
        assertFalse(w.isSafeAttribute("img", img, new Attribute("src", "javascript:alert(1)")));
    }

    @Test
    public void testRelaxed() {
        Whitelist w = Whitelist.relaxed();
        assertTrue(w.isSafeTag("table"));
        assertTrue(w.isSafeTag("tr"));
        assertTrue(w.isSafeTag("td"));
        assertTrue(w.isSafeTag("th"));
    }

    @Test
    public void testSimpleText() {
        Whitelist w = Whitelist.simpleText();
        assertTrue(w.isSafeTag("b"));
        assertTrue(w.isSafeTag("i"));
        assertFalse(w.isSafeTag("a"));
    }

    @Test
    public void testAddTag() {
        assertFalse(whitelist.isSafeTag("custom"));
        whitelist.addTag("custom");
        assertTrue(whitelist.isSafeTag("custom"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagNull() {
        whitelist.addTag(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagEmpty() {
        whitelist.addTag("");
    }

    @Test
    public void testRemoveTag() {
        whitelist.addTag("custom");
        assertTrue(whitelist.isSafeTag("custom"));
        whitelist.removeTag("custom");
        assertFalse(whitelist.isSafeTag("custom"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTagNull() {
        whitelist.removeTag(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveTagEmpty() {
        whitelist.removeTag("");
    }

    @Test
    public void testAddAttributes() {
        whitelist.addTag("p");
        whitelist.addAttributes("p", "class", "style", "id");

        Element p = new Element(Tag.valueOf("p"), "");
        assertTrue(whitelist.isSafeAttribute("p", p, new Attribute("class", "my-class")));
        assertTrue(whitelist.isSafeAttribute("p", p, new Attribute("style", "color:red")));
        assertTrue(whitelist.isSafeAttribute("p", p, new Attribute("id", "main")));
        assertFalse(whitelist.isSafeAttribute("p", p, new Attribute("onclick", "alert(1)")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesNullTag() {
        whitelist.addAttributes(null, "class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyTag() {
        whitelist.addAttributes("", "class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesNullAttributes() {
        whitelist.addAttributes("p", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyAttributes() {
        whitelist.addAttributes("p");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesNullAttributeItem() {
        whitelist.addAttributes("p", "class", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyAttributeItem() {
        whitelist.addAttributes("p", "class", "");
    }

    @Test
    public void testRemoveAttributes() {
        whitelist.addTag("p");
        whitelist.addAttributes("p", "class", "style");
        Element p = new Element(Tag.valueOf("p"), "");
        assertTrue(whitelist.isSafeAttribute("p", p, new Attribute("class", "test")));

        whitelist.removeAttributes("p", "class");
        assertFalse(whitelist.isSafeAttribute("p", p, new Attribute("class", "test")));
        assertTrue(whitelist.isSafeAttribute("p", p, new Attribute("style", "test")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttributesNullTag() {
        whitelist.removeAttributes(null, "class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttributesEmptyTag() {
        whitelist.removeAttributes("", "class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttributesNullAttributes() {
        whitelist.removeAttributes("p", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttributesEmptyAttributes() {
        whitelist.removeAttributes("p");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttributesNullAttributeItem() {
        whitelist.removeAttributes("p", "class", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttributesEmptyAttributeItem() {
        whitelist.removeAttributes("p", "class", "");
    }

    @Test
    public void testAddEnforcedAttribute() {
        whitelist.addTag("p");
        whitelist.addEnforcedAttribute("p", "rel", "nofollow");

        Attributes enforced = whitelist.getEnforcedAttributes("p");
        assertNotNull(enforced);
        assertEquals("nofollow", enforced.get("rel"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullTag() {
        whitelist.addEnforcedAttribute(null, "rel", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyTag() {
        whitelist.addEnforcedAttribute("", "rel", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullKey() {
        whitelist.addEnforcedAttribute("p", null, "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyKey() {
        whitelist.addEnforcedAttribute("p", "", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullValue() {
        whitelist.addEnforcedAttribute("p", "rel", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyValue() {
        whitelist.addEnforcedAttribute("p", "rel", "");
    }

    @Test
    public void testRemoveEnforcedAttribute() {
        whitelist.addTag("p");
        whitelist.addEnforcedAttribute("p", "rel", "nofollow");
        assertNotNull(whitelist.getEnforcedAttributes("p").get("rel"));

        whitelist.removeEnforcedAttribute("p", "rel");
        assertEquals("", whitelist.getEnforcedAttributes("p").get("rel"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEnforcedAttributeNullTag() {
        whitelist.removeEnforcedAttribute(null, "rel");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEnforcedAttributeEmptyTag() {
        whitelist.removeEnforcedAttribute("", "rel");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEnforcedAttributeNullKey() {
        whitelist.removeEnforcedAttribute("p", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEnforcedAttributeEmptyKey() {
        whitelist.removeEnforcedAttribute("p", "");
    }

    @Test
    public void testPreserveRelativeLinks() {
        whitelist.preserveRelativeLinks(true);
        whitelist.addTag("a");
        whitelist.addAttributes("a", "href");

        Element a = new Element(Tag.valueOf("a"), "");
        Attribute relHref = new Attribute("href", "/path/to/page");
        assertTrue(whitelist.isSafeAttribute("a", a, relHref));

        whitelist.preserveRelativeLinks(false);
        assertFalse(whitelist.isSafeAttribute("a", a, relHref));
    }

    @Test
    public void testAddProtocols() {
        whitelist.addTag("a");
        whitelist.addAttributes("a", "href");
        whitelist.addProtocols("a", "href", "ftp", "http");

        Element a = new Element(Tag.valueOf("a"), "");
        Attribute ftpHref = new Attribute("href", "ftp://example.com");
        assertTrue(whitelist.isSafeAttribute("a", a, ftpHref));

        Attribute customScheme = new Attribute("href", "custom://example.com");
        assertFalse(whitelist.isSafeAttribute("a", a, customScheme));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullTag() {
        whitelist.addProtocols(null, "href", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyTag() {
        whitelist.addProtocols("", "href", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullKey() {
        whitelist.addProtocols("a", null, "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyKey() {
        whitelist.addProtocols("a", "", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullProtocols() {
        whitelist.addProtocols("a", "href", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyProtocols() {
        whitelist.addProtocols("a", "href");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullProtocolItem() {
        whitelist.addProtocols("a", "href", "http", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyProtocolItem() {
        whitelist.addProtocols("a", "href", "http", "");
    }

    @Test
    public void testRemoveProtocols() {
        whitelist.addTag("a");
        whitelist.addAttributes("a", "href");
        whitelist.addProtocols("a", "href", "ftp", "http");

        Element a = new Element(Tag.valueOf("a"), "");
        Attribute ftpHref = new Attribute("href", "ftp://example.com");
        assertTrue(whitelist.isSafeAttribute("a", a, ftpHref));

        whitelist.removeProtocols("a", "href", "ftp");
        assertFalse(whitelist.isSafeAttribute("a", a, ftpHref));
        assertTrue(whitelist.isSafeAttribute("a", a, new Attribute("href", "http://example.com")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProtocolsNullTag() {
        whitelist.removeProtocols(null, "href", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProtocolsEmptyTag() {
        whitelist.removeProtocols("", "href", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProtocolsNullKey() {
        whitelist.removeProtocols("a", null, "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProtocolsEmptyKey() {
        whitelist.removeProtocols("a", "", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProtocolsNullProtocols() {
        whitelist.removeProtocols("a", "href", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProtocolsEmptyProtocols() {
        whitelist.removeProtocols("a", "href");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProtocolsNullProtocolItem() {
        whitelist.removeProtocols("a", "href", "http", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveProtocolsEmptyProtocolItem() {
        whitelist.removeProtocols("a", "href", "http", "");
    }

    @Test
    public void testIsSafeAttributeEdgeCases() {
        // Tag not allowed
        assertFalse(whitelist.isSafeAttribute("unknown", new Element(Tag.valueOf("unknown"), ""), new Attribute("class", "foo")));

        // Tag allowed, but attribute not allowed
        whitelist.addTag("p");
        assertFalse(whitelist.isSafeAttribute("p", new Element(Tag.valueOf("p"), ""), new Attribute("class", "foo")));

        // Tag allowed, attribute allowed, but test protocol validation branch where protocol isn't required/matched or is empty
        whitelist.addAttributes("p", "data-val");
        assertTrue(whitelist.isSafeAttribute("p", new Element(Tag.valueOf("p"), ""), new Attribute("data-val", "123")));
    }

    @Test
    public void testProtocolValidationWithoutProtocolsConfigured() {
        whitelist.addTag("a");
        whitelist.addAttributes("a", "href");
        // No protocols added for href, test isSafeAttribute with various URLs
        Element a = new Element(Tag.valueOf("a"), "");
        assertTrue(whitelist.isSafeAttribute("a", a, new Attribute("href", "http://example.com")));
        assertTrue(whitelist.isSafeAttribute("a", a, new Attribute("href", "javascript:alert(1)"))); // If no protocols specified, might behave differently or test specific branching in Jsoup 19
    }
}