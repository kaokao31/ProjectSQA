package org.apache.commons.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Entities}.
 */
public class EntitiesTest {

    private Entities entities;

    @Before
    public void setUp() {
        entities = new Entities();
    }

    @Test
    public void testEmptyEntities() {
        assertNull(entities.entityName(1));
        assertEquals(-1, entities.entityValue("foo"));
        assertEquals("foo", entities.escape("foo"));
        assertEquals("foo", entities.unescape("foo"));
    }

    @Test
    public void testAddEntity() {
        entities.addEntity("foo", 1);
        assertEquals("foo", entities.entityName(1));
        assertEquals(1, entities.entityValue("foo"));
    }

    @Test
    public void testAddEntitiesArray() {
        String[][] array = {
            {"foo", "1"},
            {"bar", "2"}
        };
        entities.addEntities(array);
        assertEquals("foo", entities.entityName(1));
        assertEquals(1, entities.entityValue("foo"));
        assertEquals("bar", entities.entityName(2));
        assertEquals(2, entities.entityValue("bar"));
    }

    @Test
    public void testXmlEntities() {
        assertEquals("&quot;", Entities.XML.escape("\""));
        assertEquals("&amp;", Entities.XML.escape("&"));
        assertEquals("&lt;", Entities.XML.escape("<"));
        assertEquals("&gt;", Entities.XML.escape(">"));
        assertEquals("&apos;", Entities.XML.escape("'"));

        assertEquals("\"", Entities.XML.unescape("&quot;"));
        assertEquals("&", Entities.XML.unescape("&amp;"));
        assertEquals("<", Entities.XML.unescape("&lt;"));
        assertEquals(">", Entities.XML.unescape("&gt;"));
        assertEquals("'", Entities.XML.unescape("&apos;"));
    }

    @Test
    public void testHtml32Entities() {
        assertEquals("&nbsp;", Entities.HTML32.escape("\u00A0"));
        assertEquals("\u00A0", Entities.HTML32.unescape("&nbsp;"));
        assertEquals("&copy;", Entities.HTML32.escape("\u00A9"));
        assertEquals("\u00A9", Entities.HTML32.unescape("&copy;"));
    }

    @Test
    public void testHtml40Entities() {
        assertEquals("&euro;", Entities.HTML40.escape("\u20AC"));
        assertEquals("\u20AC", Entities.HTML40.unescape("&euro;"));
        assertEquals("&trade;", Entities.HTML40.escape("\u2122"));
        assertEquals("\u2122", Entities.HTML40.unescape("&trade;"));
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
    public void testEscapeWriter() throws IOException {
        entities.addEntity("amp", '&');
        StringWriter writer = new StringWriter();
        entities.escape(writer, "a & b");
        assertEquals("a &amp; b", writer.toString());
    }

    @Test
    public void testEscapeWriterNull() throws IOException {
        StringWriter writer = new StringWriter();
        entities.escape(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    public void testUnescapeWriter() throws IOException {
        entities.addEntity("amp", '&');
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "a &amp; b");
        assertEquals("a & b", writer.toString());
    }

    @Test
    public void testUnescapeWriterNull() throws IOException {
        StringWriter writer = new StringWriter();
        entities.unescape(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    public void testUnescapeUnknownEntity() {
        assertEquals("&unknown;", Entities.HTML40.unescape("&unknown;"));
        assertEquals("&;", Entities.HTML40.unescape("&;"));
        assertEquals("a & b", Entities.HTML40.unescape("a & b"));
        assertEquals("&amp", Entities.HTML40.unescape("&amp"));
    }

    @Test
    public void testUnescapeNumericalEntityDecimal() {
        assertEquals("\u00A0", Entities.HTML40.unescape("&#160;"));
        assertEquals("\u00A0", Entities.HTML40.unescape("&#0160;"));
        assertEquals("A", Entities.HTML40.unescape("&#65;"));
        assertEquals("&#;", Entities.HTML40.unescape("&#;"));
        assertEquals("&#abc;", Entities.HTML40.unescape("&#abc;"));
    }

    @Test
    public void testUnescapeNumericalEntityHex() {
        assertEquals("\u00A0", Entities.HTML40.unescape("&#xA0;"));
        assertEquals("\u00A0", Entities.HTML40.unescape("&#xa0;"));
        assertEquals("A", Entities.HTML40.unescape("&#x41;"));
        assertEquals("&#x;", Entities.HTML40.unescape("&#x;"));
        assertEquals("&#xG1;", Entities.HTML40.unescape("&#xG1;"));
    }

    @Test
    public void testUnescapeMalformedNumericalEntities() {
        assertEquals("&#99999999999999999999999999999;", Entities.HTML40.unescape("&#99999999999999999999999999999;"));
        assertEquals("&#xFFFFFFFFFFFFFFFFFFFFFFFFFFFFF;", Entities.HTML40.unescape("&#xFFFFFFFFFFFFFFFFFFFFFFFFFFFFF;"));
    }

    @Test
    public void testUnescapeGeneralStrings() {
        assertEquals("&nbsp; & &amp; &nbsp;", Entities.HTML40.unescape("&amp;nbsp; &amp; &amp;amp; &amp;nbsp;"));
        assertEquals("Test & and &amp and &#", Entities.HTML40.unescape("Test & and &amp and &#"));
        assertEquals("&#X41;", Entities.HTML40.unescape("&#X41;"));
    }

    @Test
    public void testEscapeNonAscii() {
        Entities e = new Entities();
        e.addEntity("euro", 0x20AC);
        assertEquals("&euro;", e.escape("\u20AC"));
        assertEquals("&#8482;", e.escape("\u2122"));
    }

    @Test
    public void testEscapeHighUnicodeSupplementaryCharacters() {
        // High Unicode character above 0xFFFF, e.g. code point 0x1D362 (119650)
        // UTF-16 surrogate pair: \uD834\uDF62
        String highUnicode = "\uD834\uDF62";
        String escaped = Entities.HTML40.escape(highUnicode);
        assertEquals("&#119650;", escaped);
        assertEquals(highUnicode, Entities.HTML40.unescape("&#119650;"));
    }

    @Test
    public void testEscapeHighUnicodeWithWriter() throws IOException {
        String highUnicode = "A\uD834\uDF62B";
        StringWriter writer = new StringWriter();
        Entities.HTML40.escape(writer, highUnicode);
        assertEquals("A&#119650;B", writer.toString());
    }

    @Test
    public void testMapImplementations() {
        Entities.PrimitiveEntityMap primMap = new Entities.PrimitiveEntityMap();
        primMap.add("foo", 1);
        assertEquals("foo", primMap.name(1));
        assertEquals(1, primMap.value("foo"));
        assertNull(primMap.name(2));
        assertEquals(-1, primMap.value("bar"));

        Entities.MapIntMap mapInt = new Entities.MapIntMap();
        mapInt.add("foo", 1);
        assertEquals("foo", mapInt.name(1));
        assertEquals(1, mapInt.value("foo"));

        Entities.HashEntityMap hashMap = new Entities.HashEntityMap();
        hashMap.add("foo", 1);
        assertEquals("foo", hashMap.name(1));
        assertEquals(1, hashMap.value("foo"));

        Entities.TreeEntityMap treeMap = new Entities.TreeEntityMap();
        treeMap.add("foo", 1);
        assertEquals("foo", treeMap.name(1));
        assertEquals(1, treeMap.value("foo"));

        Entities.ArrayEntityMap arrayMap = new Entities.ArrayEntityMap();
        arrayMap.add("foo", 1);
        arrayMap.add("bar", 2);
        assertEquals("foo", arrayMap.name(1));
        assertEquals(1, arrayMap.value("foo"));
        assertEquals("bar", arrayMap.name(2));
        assertEquals(2, arrayMap.value("bar"));
        assertNull(arrayMap.name(3));
        assertEquals(-1, arrayMap.value("baz"));

        Entities.BinaryEntityMap binaryMap = new Entities.BinaryEntityMap();
        binaryMap.add("b", 2);
        binaryMap.add("a", 1);
        binaryMap.add("c", 3);
        assertEquals("a", binaryMap.name(1));
        assertEquals("b", binaryMap.name(2));
        assertEquals("c", binaryMap.name(3));
        assertEquals(1, binaryMap.value("a"));
        assertEquals(2, binaryMap.value("b"));
        assertEquals(3, binaryMap.value("c"));
        assertNull(binaryMap.name(4));
        assertEquals(-1, binaryMap.value("d"));

        Entities.LookupEntityMap lookupMap = new Entities.LookupEntityMap();
        lookupMap.add("foo", 1);
        lookupMap.add("large", 500);
        assertEquals("foo", lookupMap.name(1));
        assertEquals("large", lookupMap.name(500));
        assertEquals(1, lookupMap.value("foo"));
        assertEquals(500, lookupMap.value("large"));
        assertNull(lookupMap.name(2));
        assertNull(lookupMap.name(600));
        assertEquals(-1, lookupMap.value("unknown"));
    }

    @Test
    public void testBinaryEntityMapGrowth() {
        Entities.BinaryEntityMap binaryMap = new Entities.BinaryEntityMap(2);
        binaryMap.add("d", 4);
        binaryMap.add("b", 2);
        binaryMap.add("a", 1);
        binaryMap.add("c", 3);
        binaryMap.add("e", 5);

        assertEquals("a", binaryMap.name(1));
        assertEquals("b", binaryMap.name(2));
        assertEquals("c", binaryMap.name(3));
        assertEquals("d", binaryMap.name(4));
        assertEquals("e", binaryMap.name(5));
        assertEquals(1, binaryMap.value("a"));
        assertEquals(2, binaryMap.value("b"));
        assertEquals(3, binaryMap.value("c"));
        assertEquals(4, binaryMap.value("d"));
        assertEquals(5, binaryMap.value("e"));
    }

    @Test
    public void testArrayEntityMapGrowth() {
        Entities.ArrayEntityMap arrayMap = new Entities.ArrayEntityMap(2);
        arrayMap.add("a", 1);
        arrayMap.add("b", 2);
        arrayMap.add("c", 3);
        arrayMap.add("d", 4);

        assertEquals("a", arrayMap.name(1));
        assertEquals("b", arrayMap.name(2));
        assertEquals("c", arrayMap.name(3));
        assertEquals("d", arrayMap.name(4));
        assertEquals(1, arrayMap.value("a"));
        assertEquals(2, arrayMap.value("b"));
        assertEquals(3, arrayMap.value("c"));
        assertEquals(4, arrayMap.value("d"));
    }

    @Test
    public void testLookupEntityMapBoundary() {
        Entities.LookupEntityMap lookupMap = new Entities.LookupEntityMap();
        lookupMap.add("zero", 0);
        lookupMap.add("maxLookup", 255);
        lookupMap.add("overLookup", 256);

        assertEquals("zero", lookupMap.name(0));
        assertEquals("maxLookup", lookupMap.name(255));
        assertEquals("overLookup", lookupMap.name(256));
        assertEquals(0, lookupMap.value("zero"));
        assertEquals(255, lookupMap.value("maxLookup"));
        assertEquals(256, lookupMap.value("overLookup"));
    }

    @Test
    public void testEntitiesFillMethods() {
        Entities custom = new Entities();
        Entities.fillWithHtml40Entities(custom);
        assertNotNull(custom.entityName(0x20AC));
        assertEquals("euro", custom.entityName(0x20AC));
        assertEquals(0x20AC, custom.entityValue("euro"));
    }
}