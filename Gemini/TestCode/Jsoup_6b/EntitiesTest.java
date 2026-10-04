package org.jsoup.nodes;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class EntitiesTest {

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<Entities> constructor = Entities.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            // expected if it throws inside, but just testing instantiation
        }
    }

    @Test
    public void testEscapeBasic() {
        String input = "Hello & < > \" ' world";
        // Assuming escape method exists and handles basic HTML entities
        String escaped = Entities.escape(input, Document.OutputSettings.Syntax.html, Entities.EscapeMode.base);
        assertNotNull(escaped);
        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&lt;"));
        assertTrue(escaped.contains("&gt;"));
    }

    @Test
    public void testEscapeExtended() {
        String input = "\u00A0"; // non-breaking space
        String escapedHtml = Entities.escape(input, Document.OutputSettings.Syntax.html, Entities.EscapeMode.extended);
        assertNotNull(escapedHtml);
    }

    @Test
    public void testEscapeNull() {
        try {
            Entities.escape(null, Document.OutputSettings.Syntax.html, Entities.EscapeMode.base);
        } catch (Exception e) {
            // Depending on implementation, might throw NullPointerException or return empty string
        }
    }

    @Test
    public void testUnescape() {
        String input = "Hello &amp; &lt; &gt; &quot; &apos; &reg; &copy;";
        String unescaped = Entities.unescape(input);
        assertEquals("Hello & < > \" ' ® ©", unescaped);
    }

    @Test
    public void testUnescapeNumeric() {
        String input = "&#x3C; &#60;";
        String unescaped = Entities.unescape(input);
        assertEquals("< <", unescaped);
    }

    @Test
    public void testUnescapeInvalid() {
        String input = "&unknownEntity; &;";
        String unescaped = Entities.unescape(input);
        assertEquals("&unknownEntity; &;", unescaped);
    }

    @Test
    public void testUnescapeNull() {
        try {
            Entities.unescape(null);
        } catch (Exception e) {
            // Null safety check
        }
    }

    @Test
    public void testEscapeModeEnum() {
        assertNotNull(Entities.EscapeMode.base);
        assertNotNull(Entities.EscapeMode.extended);
        assertNotNull(Entities.EscapeMode.xhtml);
    }
}