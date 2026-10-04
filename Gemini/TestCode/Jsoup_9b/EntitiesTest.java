package org.apache.commons.codec.language.bm;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

public class EntitiesTest {

    @Test
    public void testEntitiesConstructorAndBasicRetrieval() {
        Entities entities = Entities.BEAT_LES;
        String html = entities.escape("name");
        assertNotNull(html);
    }

    @Test
    public void testUnescapeBasic() {
        Entities entities = Entities.BEAT_LES;
        String unescaped = entities.unescape("&amp;");
        assertEquals("&", unescaped);
    }

    @Test
    public void testInstanceLoadingHelperMethods() throws Exception {
        // Test loading from XML or resource if applicable via reflection or standard constructors
        // Depending on the exact version in Defects4J, Entities might use a private constructor or a static factory method.
        // Let's test standard unescape behavior with unknown or edge case entities.
        Entities entities = Entities.HTML40;
        assertNotNull(entities);
        
        String input = "This is a test &unknown; string";
        assertEquals(input, entities.unescape(input));
    }

    @Test
    public void testEmptyAndNullInputs() {
        Entities entities = Entities.XML;
        assertEquals("", entities.escape(""));
        assertEquals("", entities.unescape(""));
    }

    @Test(expected = NullPointerException.class)
    public void testEscapeNull() {
        Entities entities = Entities.XML;
        entities.escape(null);
    }

    @Test(expected = NullPointerException.class)
    public void testUnescapeNull() {
        Entities entities = Entities.XML;
        entities.unescape(null);
    }
}