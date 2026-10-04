package org.apache.commons.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

public class EntitiesTest {

    private Entities entities;

    @Before
    public void setUp() {
        entities = new Entities();
    }

    @Test
    public void testPredefinedInstances() {
        assertNotNull(Entities.XML);
        assertNotNull(Entities.HTML32);
        assertNotNull(Entities.HTML40);

        assertEquals("quot", Entities.XML.entityName(34));
        assertEquals(34, Entities.XML.entityValue("quot"));
        assertEquals("amp", Entities.XML.entityName(38));
        assertEquals(38, Entities.XML.entityValue("amp"));
        assertEquals("lt", Entities.XML.entityName(60));
        assertEquals(60, Entities.XML.entityValue("lt"));
        assertEquals("gt", Entities.XML.entityName(62));
        assertEquals(62, Entities.XML.entityValue("gt"));
        assertEquals("apos", Entities.XML.entityName(39));
        assertEquals(39, Entities.XML.entityValue("apos"));

        assertEquals("nbsp", Entities.HTML32.entityName(160));
        assertEquals(160, Entities.HTML32.entityValue("nbsp"));
        assertEquals("copy", Entities.HTML40.entityName(169));
        assertEquals(169, Entities.HTML40.entityValue("copy"));
        assertEquals("euro", Entities.HTML40.entityName(8364));
        assertEquals(8364, Entities.HTML40.entityValue("euro"));
    }

    @Test
    public void testEscapeNullAndEmpty() {
        assertNull(entities.escape(null));
        assertEquals("", entities.escape(""));
    }

    @Test
    public void testUnescapeNullAndEmpty() {
        assertNull(entities.unescape(null));
        assertEquals("", entities.unescape(""));
    }

    @Test
    public void testEscapeWriterNullAndEmpty() throws IOException {
        StringWriter sw = new StringWriter();
        entities.escape(sw, null);
        assertEquals("", sw.toString());

        sw = new StringWriter();
        entities.escape(sw, "");
        assertEquals("", sw.toString());
    }

    @Test
    public void testUnescapeWriterNullAndEmpty() throws IOException {
        StringWriter sw = new StringWriter();
        entities.unescape(sw, null);
        assertEquals("", sw.toString());

        sw = new StringWriter();
        entities.unescape(sw, "");
        assertEquals("", sw.toString());
    }

    @Test
    public void testEscapeXmlEntities() {
        Entities xml = Entities.XML;
        assertEquals("&lt;foo&gt;&amp;\"bar\'&apos;", xml.escape("<foo>&\"bar'"));
    }

    @Test
    public void testUnescapeXmlEntities() {
        Entities xml = Entities.XML;
        assertEquals("<foo>&\"bar'", xml.unescape("&lt;foo&gt;&amp;\"bar&apos;"));
        assertEquals("&apos;", xml.unescape("&apos;")); // apos is in XML
    }

    @Test
    public void testEscapeHtml40() {
        Entities html = Entities.HTML40;
        assertEquals("&euro; 10 &amp; &copy;", html.escape("\u20AC 10 & \u00A9"));
    }

    @Test
    public void testUnescapeHtml40() {
        Entities html = Entities.HTML40;
        assertEquals("\u20AC 10 & \u00A9", html.unescape("&euro; 10 &amp; &copy;"));
    }

    @Test
    public void testEscapeWriter() throws IOException {
        StringWriter sw = new StringWriter();
        Entities.HTML40.escape(sw, "<b>hello & world</b>");
        assertEquals("&lt;b&gt;hello &amp; world&lt;/b&gt;", sw.toString());
    }

    @Test
    public void testUnescapeWriter() throws IOException {
        StringWriter sw = new StringWriter();
        Entities.HTML40.unescape(sw, "&lt;b&gt;hello &amp; world&lt;/b&gt;");
        assertEquals("<b>hello & world</b>", sw.toString());
    }

    @Test
    public void testUnescapeDecimalEntity() {
        assertEquals("A", Entities.HTML40.unescape("&#65;"));
        assertEquals("ABC", Entities.HTML40.unescape("&#65;&#66;&#67;"));
        assertEquals("foo A bar", Entities.HTML40.unescape("foo &#65; bar"));
    }

    @Test
    public void testUnescapeHexEntity() {
        assertEquals("A", Entities.HTML40.unescape("&#x41;"));
        assertEquals("A", Entities.HTML40.unescape("&#X41;"));
        assertEquals("foo A bar", Entities.HTML40.unescape("foo &#x41; bar"));
    }

    @Test
    public void testNumberOverflow() {
        // Character overflow bug (Lang-62): 12345678 > 0xFFFF
        assertEquals("&#12345678;", Entities.HTML40.unescape("&#12345678;"));
        assertEquals("&#x12345678;", Entities.HTML40.unescape("&#x12345678;"));
        assertEquals("&#999999999999999999999999999999999;", Entities.HTML40.unescape("&#999999999999999999999999999999999;"));
    }

    @Test
    public void testUnescapeIncompleteEntities() {
        assertEquals("&", Entities.HTML40.unescape("&"));
        assertEquals("foo & bar", Entities.HTML40.unescape("foo & bar"));
        assertEquals("foo &", Entities.HTML40.unescape("foo &"));
        assertEquals("foo &#", Entities.HTML40.unescape("foo &#"));
        assertEquals("foo &#;", Entities.HTML40.unescape("foo &#;"));
        assertEquals("foo &#x", Entities.HTML40.unescape("foo &#x"));
        assertEquals("foo &#x;", Entities.HTML40.unescape("foo &#x;"));
        assertEquals("foo &#xG;", Entities.HTML40.unescape("foo &#xG;"));
        assertEquals("foo &#G;", Entities.HTML40.unescape("foo &#G;"));
        assertEquals("foo &unknown; bar", Entities.HTML40.unescape("foo &unknown; bar"));
        assertEquals("&unknown", Entities.HTML40.unescape("&unknown"));
    }

    @Test
    public void testAddEntityAndAddEntities() {
        Entities custom = new Entities();
        custom.addEntity("foo", 1001);
        custom.addEntity("bar", 1002);

        assertEquals("foo", custom.entityName(1001));
        assertEquals(1001, custom.entityValue("foo"));
        assertEquals("bar", custom.entityName(1002));
        assertEquals(1002, custom.entityValue("bar"));
        assertEquals(-1, custom.entityValue("baz"));
        assertNull(custom.entityName(9999));

        String[][] array = new String[][] {
            {"baz", "1003"},
            {"qux", "1004"}
        };
        custom.addEntities(array);
        assertEquals("baz", custom.entityName(1003));
        assertEquals(1003, custom.entityValue("baz"));
        assertEquals("qux", custom.entityName(1004));
        assertEquals(1004, custom.entityValue("qux"));
    }

    @Test
    public void testArrayEntityMap() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap();
        map.add("foo", 1);
        map.add("bar", 2);
        map.add("baz", 3);
        map.add("qux", 4);
        map.add("quux", 5);

        assertEquals("foo", map.name(1));
        assertEquals(1, map.value("foo"));
        assertEquals("bar", map.name(2));
        assertEquals(2, map.value("bar"));
        assertNull(map.name(99));
        assertEquals(-1, map.value("unknown"));

        Entities.ArrayEntityMap sizedMap = new Entities.ArrayEntityMap(2);
        sizedMap.add("a", 10);
        sizedMap.add("b", 20);
        sizedMap.add("c", 30);
        assertEquals(10, sizedMap.value("a"));
        assertEquals("c", sizedMap.name(30));
    }

    @Test
    public void testBinaryEntityMap() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap();
        map.add("b", 2);
        map.add("a", 1);
        map.add("c", 3);

        assertEquals("a", map.name(1));
        assertEquals(1, map.value("a"));
        assertEquals("b", map.name(2));
        assertEquals(2, map.value("b"));
        assertEquals("c", map.name(3));
        assertEquals(3, map.value("c"));
        assertNull(map.name(99));
        assertEquals(-1, map.value("unknown"));

        Entities.BinaryEntityMap sizedMap = new Entities.BinaryEntityMap(2);
        sizedMap.add("z", 26);
        sizedMap.add("y", 25);
        sizedMap.add("x", 24);
        assertEquals(26, sizedMap.value("z"));
        assertEquals("x", sizedMap.name(24));
    }

    @Test
    public void testHashEntityMap() {
        Entities.HashEntityMap map = new Entities.HashEntityMap();
        map.add("foo", 1);
        map.add("bar", 2);

        assertEquals("foo", map.name(1));
        assertEquals(1, map.value("foo"));
        assertEquals("bar", map.name(2));
        assertEquals(2, map.value("bar"));
        assertNull(map.name(99));
        assertEquals(-1, map.value("unknown"));
    }

    @Test
    public void testTreeEntityMap() {
        Entities.TreeEntityMap map = new Entities.TreeEntityMap();
        map.add("foo", 1);
        map.add("bar", 2);

        assertEquals("foo", map.name(1));
        assertEquals(1, map.value("foo"));
        assertEquals("bar", map.name(2));
        assertEquals(2, map.value("bar"));
        assertNull(map.name(99));
        assertEquals(-1, map.value("unknown"));
    }

    @Test
    public void testMapIntMap() {
        Entities.MapIntMap map = new Entities.MapIntMap();
        map.add("foo", 1);
        map.add("bar", 2);

        assertEquals("foo", map.name(1));
        assertEquals(1, map.value("foo"));
        assertEquals("bar", map.name(2));
        assertEquals(2, map.value("bar"));
        assertNull(map.name(99));
        assertEquals(-1, map.value("unknown"));
    }

    @Test
    public void testPrimitiveEntityMap() {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        map.add("foo", 1);
        map.add("bar", 2);

        assertEquals("foo", map.name(1));
        assertEquals(1, map.value("foo"));
        assertEquals("bar", map.name(2));
        assertEquals(2, map.value("bar"));
        assertNull(map.name(99));
        assertEquals(-1, map.value("unknown"));
    }

    @Test
    public void testFillWithHtml40Entities() {
        Entities custom = new Entities();
        Entities.fillWithHtml40Entities(custom);
        assertEquals("copy", custom.entityName(169));
        assertEquals(169, custom.entityValue("copy"));
    }

    @Test
    public void testEscapeNonMappedCharacters() {
        Entities custom = new Entities();
        custom.addEntity("gt", 62);
        assertEquals("a > b", custom.unescape("a &gt; b"));
        assertEquals("a &gt; b &copy;", custom.escape("a > b \u00A9"));
    }

    @Test
    public void testEscapeWithCharArrayWriter() throws IOException {
        CharArrayWriter writer = new CharArrayWriter();
        Entities.HTML40.escape(writer, "<foo & bar>");
        assertEquals("&lt;foo &amp; bar&gt;", writer.toString());
    }

    @Test
    public void testUnescapeWithCharArrayWriter() throws IOException {
        CharArrayWriter writer = new CharArrayWriter();
        Entities.HTML40.unescape(writer, "&lt;foo &amp; bar&gt;");
        assertEquals("<foo & bar>", writer.toString());
    }
}