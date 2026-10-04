package org.apache.commons.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * Unit tests for {@link WordUtils}.
 */
public class WordUtilsTest {

    //-----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertNotNull(new WordUtils());
        Constructor<?>[] cons = WordUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(WordUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(WordUtils.class.getModifiers()));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testWrap_StringInt() {
        assertNull(WordUtils.wrap(null, 20));
        assertNull(WordUtils.wrap(null, -1));
        
        assertEquals("", WordUtils.wrap("", 20));
        assertEquals("", WordUtils.wrap("", -1));
        
        // normal cases
        assertEquals("Here is\na simple\ntext\nthat\nmust be\nwrapped",
            WordUtils.wrap("Here is a simple text that must be wrapped", 10));
        assertEquals("Here is a simple text that must be wrapped",
            WordUtils.wrap("Here is a simple text that must be wrapped", -1));
        
        // words longer than wrap length
        assertEquals("Click here to jump to the\nhttp://commons.apache.org\npage",
            WordUtils.wrap("Click here to jump to the http://commons.apache.org page", 25));
        assertEquals("Click here to jump to the\nhttp://commons.apache.org\npage",
            WordUtils.wrap("Click here to jump to the http://commons.apache.org page", 20));
    }

    @Test
    public void testWrap_StringIntStringBoolean() {
        assertNull(WordUtils.wrap(null, 20, null, true));
        assertNull(WordUtils.wrap(null, 20, "\n", true));
        assertNull(WordUtils.wrap(null, 20, "\n", false));
        assertNull(WordUtils.wrap(null, -1, "\n", true));
        assertNull(WordUtils.wrap(null, -1, "\n", false));
        
        assertEquals("", WordUtils.wrap("", 20, null, true));
        assertEquals("", WordUtils.wrap("", 20, "\n", true));
        assertEquals("", WordUtils.wrap("", 20, "\n", false));
        assertEquals("", WordUtils.wrap("", -1, "\n", true));
        assertEquals("", WordUtils.wrap("", -1, "\n", false));

        // default newLineStr = "\n" when null
        assertEquals("Here is\na simple\ntext\nthat\nmust be\nwrapped",
            WordUtils.wrap("Here is a simple text that must be wrapped", 10, null, false));
        
        // custom newLineStr
        assertEquals("Here is<br />a simple<br />text<br />that<br />must be<br />wrapped",
            WordUtils.wrap("Here is a simple text that must be wrapped", 10, "<br />", false));

        // wrapLongWords = true
        assertEquals("Click here to jump to the\nhttp://commons.apache.or\ng\npage",
            WordUtils.wrap("Click here to jump to the http://commons.apache.org page", 24, "\n", true));

        // wrapLongWords = false
        assertEquals("Click here to jump to the\nhttp://commons.apache.org\npage",
            WordUtils.wrap("Click here to jump to the http://commons.apache.org page", 24, "\n", false));
            
        // multiple spaces and wrapLongWords
        assertEquals("abc\ndef\nghi", WordUtils.wrap("abc def ghi", 3, "\n", false));
        assertEquals("abc\ndef\nghi", WordUtils.wrap("abc def ghi", 4, "\n", false));
        assertEquals("abcdef", WordUtils.wrap("abcdef", 3, "\n", false));
        assertEquals("abc\ndef", WordUtils.wrap("abcdef", 3, "\n", true));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testCapitalize_String() {
        assertNull(WordUtils.capitalize(null));
        assertEquals("", WordUtils.capitalize(""));
        assertEquals("  ", WordUtils.capitalize("  "));
        assertEquals("I", WordUtils.capitalize("I"));
        assertEquals("I", WordUtils.capitalize("i"));
        assertEquals("I Am Fine", WordUtils.capitalize("i am fine"));
        assertEquals("I Am Fine", WordUtils.capitalize("I Am Fine"));
        assertEquals("I Am FINE", WordUtils.capitalize("i am FINE"));
        assertEquals("I  Am   Fine", WordUtils.capitalize("i  am   fine"));
        assertEquals("I   Am   Fine", WordUtils.capitalize("I   Am   Fine"));
        assertEquals("I\nAm\nFine", WordUtils.capitalize("i\nam\nfine"));
        assertEquals("I\tAm\tFine", WordUtils.capitalize("i\tam\tfine"));
        assertEquals("I Am Fine.", WordUtils.capitalize("i am fine."));
    }

    @Test
    public void testCapitalizeWithDelimiters_String() {
        assertNull(WordUtils.capitalize(null, new char[0]));
        assertEquals("", WordUtils.capitalize("", new char[0]));
        assertEquals("  ", WordUtils.capitalize("  ", new char[0]));
        
        assertEquals("I", WordUtils.capitalize("I", new char[0]));
        assertEquals("i", WordUtils.capitalize("i", new char[0]));
        assertEquals("i am fine", WordUtils.capitalize("i am fine", new char[0]));
        assertEquals("I AM FINE", WordUtils.capitalize("I AM FINE", new char[0]));
        
        assertEquals("I", WordUtils.capitalize("I", new char[]{'.'}));
        assertEquals("i", WordUtils.capitalize("i", new char[]{'.'}));
        assertEquals("i am fine", WordUtils.capitalize("i am fine", new char[]{'.'}));
        assertEquals("I AM FINE", WordUtils.capitalize("I AM FINE", new char[]{'.'}));
        
        assertEquals("I.am.fine", WordUtils.capitalize("i.am.fine", new char[]{'.'}));
        assertEquals("I.Am.Fine", WordUtils.capitalize("i.am.fine", new char[]{'.'}));
        assertEquals("I..Am.Fine", WordUtils.capitalize("i..am.fine", new char[]{'.'}));
        assertEquals("I_Am_Fine", WordUtils.capitalize("i_am_fine", new char[]{'_'}));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testCapitalizeFully_String() {
        assertNull(WordUtils.capitalizeFully(null));
        assertEquals("", WordUtils.capitalizeFully(""));
        assertEquals("  ", WordUtils.capitalizeFully("  "));
        assertEquals("I", WordUtils.capitalizeFully("I"));
        assertEquals("I", WordUtils.capitalizeFully("i"));
        assertEquals("I Am Fine", WordUtils.capitalizeFully("i am fine"));
        assertEquals("I Am Fine", WordUtils.capitalizeFully("I Am Fine"));
        assertEquals("I Am Fine", WordUtils.capitalizeFully("i am FINE"));
        assertEquals("I  Am   Fine", WordUtils.capitalizeFully("i  am   fine"));
        assertEquals("I   Am   Fine", WordUtils.capitalizeFully("I   Am   Fine"));
        assertEquals("I\nAm\nFine", WordUtils.capitalizeFully("i\nam\nfine"));
        assertEquals("I\tAm\tFine", WordUtils.capitalizeFully("i\tam\tfine"));
        assertEquals("I Am Fine.", WordUtils.capitalizeFully("i am fine."));
    }

    @Test
    public void testCapitalizeFullyWithDelimiters_String() {
        assertNull(WordUtils.capitalizeFully(null, new char[0]));
        assertEquals("", WordUtils.capitalizeFully("", new char[0]));
        assertEquals("  ", WordUtils.capitalizeFully("  ", new char[0]));
        
        assertEquals("I", WordUtils.capitalizeFully("I", new char[0]));
        assertEquals("i", WordUtils.capitalizeFully("i", new char[0]));
        assertEquals("i am fine", WordUtils.capitalizeFully("i am fine", new char[0]));
        assertEquals("i am fine", WordUtils.capitalizeFully("I AM FINE", new char[0]));
        
        assertEquals("I", WordUtils.capitalizeFully("I", new char[]{'.'}));
        assertEquals("i", WordUtils.capitalizeFully("i", new char[]{'.'}));
        assertEquals("i am fine", WordUtils.capitalizeFully("i am fine", new char[]{'.'}));
        assertEquals("i am fine", WordUtils.capitalizeFully("I AM FINE", new char[]{'.'}));
        
        assertEquals("I.am.fine", WordUtils.capitalizeFully("i.am.fine", new char[0]));
        assertEquals("I.Am.Fine", WordUtils.capitalizeFully("i.am.fine", new char[]{'.'}));
        assertEquals("I..Am.Fine", WordUtils.capitalizeFully("i..am.fine", new char[]{'.'}));
        assertEquals("I_Am_Fine", WordUtils.capitalizeFully("i_am_fine", new char[]{'_'}));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testUncapitalize_String() {
        assertNull(WordUtils.uncapitalize(null));
        assertEquals("", WordUtils.uncapitalize(""));
        assertEquals("  ", WordUtils.uncapitalize("  "));
        assertEquals("i", WordUtils.uncapitalize("I"));
        assertEquals("i", WordUtils.uncapitalize("i"));
        assertEquals("i am fine", WordUtils.uncapitalize("i am fine"));
        assertEquals("i am fine", WordUtils.uncapitalize("I Am Fine"));
        assertEquals("i am fINE", WordUtils.uncapitalize("i am FINE"));
        assertEquals("i  am   fine", WordUtils.uncapitalize("i  am   fine"));
        assertEquals("i   am   fine", WordUtils.uncapitalize("I   Am   Fine"));
        assertEquals("i\nam\nfine", WordUtils.uncapitalize("i\nam\nfine"));
        assertEquals("i\tam\tfine", WordUtils.uncapitalize("i\tam\tfine"));
        assertEquals("i am fine.", WordUtils.uncapitalize("i am fine."));
    }

    @Test
    public void testUncapitalizeWithDelimiters_String() {
        assertNull(WordUtils.uncapitalize(null, new char[0]));
        assertEquals("", WordUtils.uncapitalize("", new char[0]));
        assertEquals("  ", WordUtils.uncapitalize("  ", new char[0]));
        
        assertEquals("I", WordUtils.uncapitalize("I", new char[0]));
        assertEquals("i", WordUtils.uncapitalize("i", new char[0]));
        assertEquals("i am fine", WordUtils.uncapitalize("i am fine", new char[0]));
        assertEquals("I AM FINE", WordUtils.uncapitalize("I AM FINE", new char[0]));
        
        assertEquals("I", WordUtils.uncapitalize("I", new char[]{'.'}));
        assertEquals("i", WordUtils.uncapitalize("i", new char[]{'.'}));
        assertEquals("i am fine", WordUtils.uncapitalize("i am fine", new char[]{'.'}));
        assertEquals("I AM FINE", WordUtils.uncapitalize("I AM FINE", new char[]{'.'}));
        
        assertEquals("i.am.fine", WordUtils.uncapitalize("I.Am.Fine", new char[]{'.'}));
        assertEquals("i..am.fine", WordUtils.uncapitalize("I..Am.Fine", new char[]{'.'}));
        assertEquals("i_am_fine", WordUtils.uncapitalize("I_Am_Fine", new char[]{'_'}));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testSwapCase_String() {
        assertNull(WordUtils.swapCase(null));
        assertEquals("", WordUtils.swapCase(""));
        assertEquals("  ", WordUtils.swapCase("  "));
        
        assertEquals("i", WordUtils.swapCase("I"));
        assertEquals("I", WordUtils.swapCase("i"));
        assertEquals("I AM FINE", WordUtils.swapCase("i am fine"));
        assertEquals("i aM fINE", WordUtils.swapCase("I Am Fine"));
        assertEquals("I AM fine", WordUtils.swapCase("i am FINE"));
        assertEquals("I  AM   FINE", WordUtils.swapCase("i  am   fine"));
        assertEquals("i   aM   fINE", WordUtils.swapCase("I   Am   Fine"));
        assertEquals("I\nAM\nFINE", WordUtils.swapCase("i\nam\nfine"));
        assertEquals("I\tAM\tFINE", WordUtils.swapCase("i\tam\tfine"));
        assertEquals("I AM FINE.", WordUtils.swapCase("i am fine."));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testInitials_String() {
        assertNull(WordUtils.initials(null));
        assertEquals("", WordUtils.initials(""));
        assertEquals("", WordUtils.initials("  "));
        assertEquals("I", WordUtils.initials("I"));
        assertEquals("i", WordUtils.initials("i"));
        assertEquals("iaf", WordUtils.initials("i am fine"));
        assertEquals("IAF", WordUtils.initials("I Am Fine"));
        assertEquals("iaF", WordUtils.initials("i am FINE"));
        assertEquals("iaf", WordUtils.initials("i  am   fine"));
        assertEquals("IAF", WordUtils.initials("I   Am   Fine"));
        assertEquals("iaf", WordUtils.initials("i\nam\nfine"));
        assertEquals("iaf", WordUtils.initials("i\tam\tfine"));
    }

    @Test
    public void testInitials_String_charArray() {
        assertNull(WordUtils.initials(null, new char[0]));
        assertEquals("", WordUtils.initials("", new char[0]));
        assertEquals("", WordUtils.initials("  ", new char[0]));
        assertEquals("", WordUtils.initials("I", new char[0]));
        assertEquals("", WordUtils.initials("i", new char[0]));
        assertEquals("", WordUtils.initials("i am fine", new char[0]));
        assertEquals("", WordUtils.initials("I AM FINE", new char[0]));
        
        assertEquals("I", WordUtils.initials("I", new char[]{'.'}));
        assertEquals("i", WordUtils.initials("i", new char[]{'.'}));
        assertEquals("i", WordUtils.initials("i am fine", new char[]{'.'}));
        assertEquals("I", WordUtils.initials("I AM FINE", new char[]{'.'}));
        
        assertEquals("iaf", WordUtils.initials("i.am.fine", new char[]{'.'}));
        assertEquals("iaf", WordUtils.initials("i..am.fine", new char[]{'.'}));
        assertEquals("iaf", WordUtils.initials("i_am_fine", new char[]{'_'}));
        assertEquals("IAF", WordUtils.initials("I_Am_Fine", new char[]{'_'}));
        assertEquals("iaf", WordUtils.initials("i:am:fine", new char[]{':'}));
        assertEquals("iaf", WordUtils.initials("i;am;fine", new char[]{';'}));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testAbbreviate() {
        assertNull(WordUtils.abbreviate(null, 1, -1, ""));
        assertEquals("", WordUtils.abbreviate("", 0, 2, ""));
        assertEquals("", WordUtils.abbreviate("", 2, 0, ""));
        
        assertEquals("01234", WordUtils.abbreviate("0123456789", 0, -1, null));
        assertEquals("01234", WordUtils.abbreviate("0123456789", 0, -1, ""));
        assertEquals("01234", WordUtils.abbreviate("0123456789", 2, -1, ""));
        assertEquals("01234", WordUtils.abbreviate("0123456789", 5, -1, ""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 10, -1, ""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, -1, ""));
        
        assertEquals("01234", WordUtils.abbreviate("0123456789", 0, 5, ""));
        assertEquals("01234", WordUtils.abbreviate("0123456789", 5, 2, ""));
        assertEquals("01234", WordUtils.abbreviate("0123456789", 5, 5, ""));
        assertEquals("01234", WordUtils.abbreviate("0123456789", 5, 6, ""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 0, 10, ""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 10, 5, ""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 10, 10, ""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 10, 15, ""));
        
        // Target bug Lang-45: lower > str.length() with appendToEnd
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, 20, ""));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, 20, "..."));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, 10, "..."));
        assertEquals("0123456789", WordUtils.abbreviate("0123456789", 15, -1, "..."));

        // space boundary tests
        assertEquals("upper", WordUtils.abbreviate("upper limit reached", 4, 10, ""));
        assertEquals("upper limit", WordUtils.abbreviate("upper limit reached", 11, 15, ""));
        assertEquals("upper...", WordUtils.abbreviate("upper limit reached", 4, 10, "..."));
        assertEquals("upper limit...", WordUtils.abbreviate("upper limit reached", 11, 15, "..."));
        assertEquals("upper limit reached", WordUtils.abbreviate("upper limit reached", 19, 25, "..."));
        assertEquals("upper limit reached", WordUtils.abbreviate("upper limit reached", -1, 25, "..."));
    }
}