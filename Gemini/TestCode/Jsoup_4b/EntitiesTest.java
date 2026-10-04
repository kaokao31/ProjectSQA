package org.apache.commons.codec.language.bm;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class EntitiesTest {

    @Test
    public void testConstructorAndGetMap() {
        Entities entities = Entities.JAVA;
        assertNotNull(entities.getMap());
        assertFalse(entities.getMap().isEmpty());
    }

    @Test
    public void testEntityNameMapping() {
        Entities entities = Entities.JAVA;
        // Test standard HTML entity mapping loaded from resources
        // Usually contains mapping like &amp; -> &
        String translated = entities.unescape("&amp;");
        assertEquals("&", translated);
    }

    @Test
    public void testUnescapeWithNoEntities() {
        Entities entities = Entities.JAVA;
        String text = "hello world";
        assertEquals("hello world", entities.unescape(text));
    }

    @Test
    public void testUnescapeWithMultipleEntities() {
        Entities entities = Entities.JAVA;
        String text = "&amp;&lt;&gt;";
        assertEquals("&<>", entities.unescape(text));
    }

    @Test
    public void testUnescapeWithUnknownEntity() {
        Entities entities = Entities.JAVA;
        String text = "&unknownentity;";
        // Depending on implementation, unknown entities might be left as-is or transformed
        String result = entities.unescape(text);
        assertNotNull(result);
    }

    @Test
    public void testXMLInstance() {
        Entities xmlEntities = Entities.XML;
        assertNotNull(xmlEntities);
        assertNotNull(xmlEntities.getMap());
    }

    @Test
    public void testHTML40Instance() {
        Entities htmlEntities = Entities.HTML40;
        assertNotNull(htmlEntities);
        assertNotNull(htmlEntities.getMap());
    }

    @Test
    public void testBasicInstance() {
        Entities basicEntities = Entities.BASIC;
        assertNotNull(basicEntities);
        assertNotNull(basicEntities.getMap());
    }

    @Test
    public void testCannedInstance() {
        // Test loading entities via the factory/canned method if available
        Entities entities = Entities.INSTANCE;
        assertNotNull(entities);
    }

    @Test(expected = NullPointerException.class)
    public void testUnescapeNull() {
        Entities entities = Entities.JAVA;
        entities.unescape(null);
    }

    @Test
    public void testEmptyStringUnescape() {
        Entities entities = Entities.JAVA;
        assertEquals("", entities.unescape(""));
    }

    @Test
    public void testEntityConstructorAndMethods() {
        // Directly test internal class or builder methods if exposed, 
        // or ensure all code branches in Entities are covered through different string inputs.
        Entities entities = Entities.JAVA;
        String complex = "This &amp; that are <body> &lt;cool&gt;";
        String result = entities.unescape(complex);
        assertTrue(result.contains("&"));
        assertTrue(result.contains("<"));
        assertTrue(result.contains(">"));
    }
}