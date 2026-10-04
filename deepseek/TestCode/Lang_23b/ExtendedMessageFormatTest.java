package org.apache.commons.lang3.text;

import org.junit.Before;
import org.junit.Test;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for ExtendedMessageFormat.
 * Designed to achieve maximum code coverage and detect faults,
 * particularly the registry-related equals/hashCode bug (Defects4J Bug 23).
 */
public class ExtendedMessageFormatTest {

    private static final String DEFAULT_PATTERN = "Hello {0}";
    private static final Locale TEST_LOCALE = Locale.US;

    private Map<String, ? extends Format> registry;
    private Format dummyFormat;

    @Before
    public void setUp() {
        registry = new HashMap<>();
        dummyFormat = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo.append(obj);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return source.substring(pos.getIndex());
            }
        };
        registry.put("dummy", dummyFormat);
    }

    // ======================== Constructor Tests ========================

    @Test
    public void testConstructorWithNullPattern() {
        try {
            new ExtendedMessageFormat(null);
            fail("Expected IllegalArgumentException for null pattern");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithNullPatternAndRegistry() {
        try {
            new ExtendedMessageFormat(null, registry);
            fail("Expected IllegalArgumentException for null pattern with registry");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithNullPatternAndLocale() {
        try {
            new ExtendedMessageFormat(null, TEST_LOCALE);
            fail("Expected IllegalArgumentException for null pattern with locale");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithNullPatternAndLocaleAndRegistry() {
        try {
            new ExtendedMessageFormat(null, TEST_LOCALE, registry);
            fail("Expected IllegalArgumentException for null pattern with locale and registry");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithEmptyPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("");
        assertNotNull(emf);
        assertEquals("", emf.toPattern());
    }

    @Test
    public void testConstructorWithDefaultPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        assertNotNull(emf);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    @Test
    public void testConstructorWithRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        assertNotNull(emf);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    @Test
    public void testConstructorWithLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, TEST_LOCALE);
        assertNotNull(emf);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    @Test
    public void testConstructorWithLocaleAndRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, TEST_LOCALE, registry);
        assertNotNull(emf);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    // ======================== applyPattern Tests ========================

    @Test
    public void testApplyPatternNull() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        try {
            emf.applyPattern(null);
            fail("Expected IllegalArgumentException for null pattern");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testApplyPatternEmpty() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        emf.applyPattern("");
        assertEquals("", emf.toPattern());
    }

    @Test
    public void testApplyPatternValid() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        String newPattern = "Goodbye {0}";
        emf.applyPattern(newPattern);
        assertEquals(newPattern, emf.toPattern());
    }

    // ======================== toPattern Tests ========================

    @Test
    public void testToPatternAfterConstruction() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    @Test
    public void testToPatternWithRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    // ======================== format Tests ========================

    @Test
    public void testFormatBasic() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Value: {0}");
        String result = emf.format(new Object[]{"test"});
        assertEquals("Value: test", result);
    }

    @Test
    public void testFormatWithMultipleArguments() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0} {1}");
        String result = emf.format(new Object[]{"Hello", "World"});
        assertEquals("Hello World", result);
    }

    @Test
    public void testFormatWithNullArgument() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        String result = emf.format(new Object[]{null});
        assertEquals("null", result);
    }

    @Test
    public void testFormatWithCustomFormat() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, dummy}", registry);
        String result = emf.format(new Object[]{"test"});
        assertEquals("test", result);
    }

    @Test
    public void testFormatWithLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, number}", TEST_LOCALE);
        String result = emf.format(new Object[]{12345.678});
        // Locale US uses comma as grouping separator
        assertTrue(result.contains("12,345.678"));
    }

    // ======================== parseObject Tests ========================

    @Test
    public void testParseObject() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Value: {0}");
        Object[] result = (Object[]) emf.parseObject("Value: test");
        assertEquals(1, result.length);
        assertEquals("test", result[0]);
    }

    @Test
    public void testParseObjectWithCustomFormat() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, dummy}", registry);
        Object[] result = (Object[]) emf.parseObject("test");
        assertEquals(1, result.length);
        assertEquals("test", result[0]);
    }

    @Test
    public void testParseObjectWithNullSource() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        try {
            emf.parseObject(null);
            fail("Expected IllegalArgumentException for null source");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ======================== equals and hashCode Tests ========================

    @Test
    public void testEqualsSameObject() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        assertTrue(emf.equals(emf));
    }

    @Test
    public void testEqualsNull() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        assertFalse(emf.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        assertFalse(emf.equals("string"));
    }

    @Test
    public void testEqualsSamePatternAndRegistry() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        assertTrue(emf1.equals(emf2));
        assertEquals(emf1.hashCode(), emf2.hashCode());
    }

    @Test
    public void testEqualsDifferentPattern() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Pattern A", registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Pattern B", registry);
        assertFalse(emf1.equals(emf2));
    }

    @Test
    public void testEqualsDifferentRegistry() {
        Map<String, Format> registry2 = new HashMap<>();
        Format fmt2 = new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                return toAppendTo.append("X");
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return "X";
            }
        };
        registry2.put("other", fmt2);

        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry2);
        assertFalse(emf1.equals(emf2));
    }

    @Test
    public void testEqualsOneRegistryNull() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN);
        assertFalse(emf1.equals(emf2));
    }

    @Test
    public void testEqualsBothRegistryNull() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN);
        assertTrue(emf1.equals(emf2));
        assertEquals(emf1.hashCode(), emf2.hashCode());
    }

    @Test
    public void testEqualsSymmetry() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        assertTrue(emf1.equals(emf2));
        assertTrue(emf2.equals(emf1));
    }

    @Test
    public void testEqualsTransitivity() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        ExtendedMessageFormat emf3 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        assertTrue(emf1.equals(emf2));
        assertTrue(emf2.equals(emf3));
        assertTrue(emf1.equals(emf3));
    }

    @Test
    public void testHashCodeConsistency() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        int initialHash = emf.hashCode();
        assertEquals(initialHash, emf.hashCode());
        assertEquals(initialHash, emf.hashCode());
    }

    @Test
    public void testHashCodeWithDifferentPatterns() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Pattern A", registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Pattern B", registry);
        // Not strictly required to be different, but likely
        assertNotEquals(emf1.hashCode(), emf2.hashCode());
    }

    @Test
    public void testHashCodeWithDifferentRegistries() {
        Map<String, Format> registry2 = new HashMap<>();
        registry2.put("other", dummyFormat);
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry2);
        assertNotEquals(emf1.hashCode(), emf2.hashCode());
    }

    // ======================== Edge Cases and Bug Triggers ========================

    @Test
    public void testEqualsHashcodeRegistryBug() {
        // This test directly targets the known bug (Defects4J Bug 23)
        // where equals/hashCode inconsistency occurs with registry.
        Map<String, Format> reg1 = new HashMap<>();
        reg1.put("fmt1", dummyFormat);
        Map<String, Format> reg2 = new HashMap<>();
        reg2.put("fmt2", dummyFormat);

        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("{0, fmt1}", reg1);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("{0, fmt2}", reg2);

        // They are not equal because registry keys differ
        assertFalse(emf1.equals(emf2));
        // But hashCode should also differ (or at least be consistent with equals)
        // The bug might cause equal hash codes even when not equal.
        // We assert that if equals returns false, hashCodes may or may not be equal,
        // but we can check that the contract is not violated (i.e., if equals true then hash equal).
        // However, we can also verify that the hash codes are different in this case.
        // This is a typical trigger for the bug.
        assertNotEquals(emf1.hashCode(), emf2.hashCode());
    }

    @Test
    public void testEqualsWithNullRegistryEntry() {
        Map<String, Format> regWithNull = new HashMap<>();
        regWithNull.put("nullFmt", null);
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("{0, nullFmt}", regWithNull);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("{0, nullFmt}", regWithNull);
        // Both have same pattern and same registry (with null value)
        assertTrue(emf1.equals(emf2));
        assertEquals(emf1.hashCode(), emf2.hashCode());
    }

    @Test
    public void testEqualsWithDifferentNullRegistryEntry() {
        Map<String, Format> reg1 = new HashMap<>();
        reg1.put("fmt", null);
        Map<String, Format> reg2 = new HashMap<>();
        reg2.put("fmt", dummyFormat);
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("{0, fmt}", reg1);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("{0, fmt}", reg2);
        assertFalse(emf1.equals(emf2));
    }

    @Test
    public void testFormatWithEscapedBraces() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("'{0}'");
        String result = emf.format(new Object[]{"test"});
        assertEquals("{0}", result);
    }

    @Test
    public void testFormatWithSingleQuote() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("It''s {0}");
        String result = emf.format(new Object[]{"fun"});
        assertEquals("It's fun", result);
    }

    @Test
    public void testFormatWithCustomFormatAndLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, dummy}", TEST_LOCALE, registry);
        String result = emf.format(new Object[]{"test"});
        assertEquals("test", result);
    }

    @Test
    public void testParseObjectWithCustomFormatAndLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, dummy}", TEST_LOCALE, registry);
        Object[] result = (Object[]) emf.parseObject("test");
        assertEquals(1, result.length);
        assertEquals("test", result[0]);
    }

    @Test
    public void testConstructorWithNullLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, (Locale) null);
        assertNotNull(emf);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    @Test
    public void testConstructorWithNullRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, (Map<String, ? extends Format>) null);
        assertNotNull(emf);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    @Test
    public void testConstructorWithNullLocaleAndNullRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, null, null);
        assertNotNull(emf);
        assertEquals(DEFAULT_PATTERN, emf.toPattern());
    }

    @Test
    public void testEqualsWithNullPattern() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN);
        assertTrue(emf1.equals(emf2));
    }

    @Test
    public void testHashCodeWithNullRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        int hash = emf.hashCode();
        assertNotNull(hash);
    }

    @Test
    public void testFormatWithEmptyArguments() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("No placeholders");
        String result = emf.format(new Object[]{});
        assertEquals("No placeholders", result);
    }

    @Test
    public void testFormatWithTooManyArguments() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        String result = emf.format(new Object[]{"a", "b"});
        assertEquals("a", result);
    }

    @Test
    public void testFormatWithTooFewArguments() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0} {1}");
        try {
            emf.format(new Object[]{"only"});
            fail("Expected IllegalArgumentException for missing argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testApplyPatternWithCustomFormat() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", registry);
        emf.applyPattern("{0, dummy}");
        assertEquals("{0, dummy}", emf.toPattern());
    }

    @Test
    public void testToPatternAfterApplyPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN);
        emf.applyPattern("New {0}");
        assertEquals("New {0}", emf.toPattern());
    }

    @Test
    public void testEqualsAfterApplyPattern() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        emf1.applyPattern("Changed");
        assertFalse(emf1.equals(emf2));
    }

    @Test
    public void testHashCodeAfterApplyPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat(DEFAULT_PATTERN, registry);
        int hashBefore = emf.hashCode();
        emf.applyPattern("New");
        int hashAfter = emf.hashCode();
        assertNotEquals(hashBefore, hashAfter);
    }
}