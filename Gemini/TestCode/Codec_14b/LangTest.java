package org.apache.commons.codec.language.bm;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.*;

public class LangTest {

    @Test
    public void testInstanceLoadingAndBasicLanguages() {
        // Test standard instances for different name types and rules
        for (NameType nameType : NameType.values()) {
            for (RuleType ruleType : RuleType.values()) {
                Lang lang = Lang.instance(nameType, ruleType);
                assertNotNull(lang);
                assertNotNull(lang.getLanguages());
            }
        }
    }

    @Test
    public void testLanguageGuessing() {
        // Hebrew name guessing with Ashkenazi rules
        Lang lang = Lang.instance(NameType.ASHKENAZI, RuleType.RULES);
        String languages = lang.guessLanguages("Cohen");
        assertNotNull(languages);
        
        // Test empty or null inputs if handled safely, or basic string matching
        String emptyGuess = lang.guessLanguages("");
        assertNotNull(emptyGuess);
    }

    @Test
    public void testLoadFromResource() {
        // Directly test private/protected or helper logic if accessible through public API,
        // such as creating an instance and ensuring language rules are parsed correctly.
        Lang lang = Lang.instance(NameType.GENERIC, RuleType.RULES);
        // "smith" in generic rules
        assertTrue(lang.guessLanguages("smith").length() > 0);
    }
}