package org.apache.commons.lang3.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ExtendedMessageFormatTest {

    private final Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();

    @Before
    public void setUp() {
        registry.put("lower", new LowerCaseFormatFactory());
        registry.put("upper", new UpperCaseFormatFactory());
    }

    @Test
    public void testExtendedFormats() {
        final String pattern = "Hi {0,lower} {1,upper}!";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Hi {0,lower} {1,upper}!", emf.toPattern());
        assertEquals("Hi world TEST!", emf.format(new Object[] {"WORLD", "test"}));
    }

    @Test
    public void testEscapedChars() {
        final String pattern = "''{0,lower}'' '{1,upper}'";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("''{0,lower}'' '{1,upper}'", emf.toPattern());
        assertEquals("'world' {1,upper}", emf.format(new Object[] {"WORLD", "test"}));
    }

    @Test
    public void testBuiltInChoiceFormat() {
        final String pattern = "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}.";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("There are no files.", emf.format(new Object[] {0}));
        assertEquals("There is one file.", emf.format(new Object[] {1}));
        assertEquals("There are 2 files.", emf.format(new Object[] {2}));
    }

    @Test
    public void testBuiltInDateTimeFormat() {
        final Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 15, 10, 30, 0);

        final ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,date,short} {0,time,short}", Locale.US, registry);
        final DateFormat dateDefault = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        final DateFormat timeDefault = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        final String expected = dateDefault.format(cal.getTime()) + " " + timeDefault.format(cal.getTime());
        assertEquals(expected, emf.format(new Object[] {cal.getTime()}));
    }

    @Test
    public void testBuiltInNumberFormat() {
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,number,currency}", Locale.US, registry);
        final NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        assertEquals(nf.format(12.34), emf.format(new Object[] {12.34}));
    }

    @Test
    public void testConstructors() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}");
        assertEquals("Hello {0}", emf.toPattern());
        assertEquals(Locale.getDefault(), emf.getLocale());

        emf = new ExtendedMessageFormat("Hello {0}", Locale.GERMAN);
        assertEquals("Hello {0}", emf.toPattern());
        assertEquals(Locale.GERMAN, emf.getLocale());

        emf = new ExtendedMessageFormat("Hello {0,lower}", registry);
        assertEquals("Hello {0,lower}", emf.toPattern());
        assertEquals(Locale.getDefault(), emf.getLocale());

        emf = new ExtendedMessageFormat("Hello {0,lower}", Locale.FRENCH, registry);
        assertEquals("Hello {0,lower}", emf.toPattern());
        assertEquals(Locale.FRENCH, emf.getLocale());
    }

    @Test
    public void testEqualsHashcode() {
        final Map<String, ? extends FormatFactory> registryEmpty = new HashMap<String, FormatFactory>();
        final Map<String, ? extends FormatFactory> registryAlt = new HashMap<String, FormatFactory>();
        registryAlt.put("other", new LowerCaseFormatFactory());

        final String pattern = "Pattern: {0,lower}";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, Locale.US, registry);

        final ExtendedMessageFormat other = new ExtendedMessageFormat(pattern, Locale.US, registry);
        assertEquals("emf.equals(other)", emf, other);
        assertEquals("other.equals(emf)", other, emf);
        assertEquals("same hashcode", emf.hashCode(), other.hashCode());

        assertFalse("emf.equals(null)", emf.equals(null));
        assertFalse("emf.equals(String)", emf.equals(""));
        assertTrue("emf.equals(emf)", emf.equals(emf));

        assertFalse("different pattern", emf.equals(new ExtendedMessageFormat("Different: {0,lower}", Locale.US, registry)));
        assertFalse("different locale", emf.equals(new ExtendedMessageFormat(pattern, Locale.FRANCE, registry)));
        assertFalse("different registry", emf.equals(new ExtendedMessageFormat(pattern, Locale.US, registryAlt)));
        assertFalse("empty registry vs full registry", emf.equals(new ExtendedMessageFormat(pattern, Locale.US, registryEmpty)));
        assertFalse("null registry vs full registry", emf.equals(new ExtendedMessageFormat(pattern, Locale.US, null)));

        final ExtendedMessageFormat emfNoReg1 = new ExtendedMessageFormat("Pattern: {0}", Locale.US);
        final ExtendedMessageFormat emfNoReg2 = new ExtendedMessageFormat("Pattern: {0}", Locale.US);
        assertEquals("no reg equals", emfNoReg1, emfNoReg2);
        assertEquals("no reg hashCode", emfNoReg1.hashCode(), emfNoReg2.hashCode());
    }

    @Test
    public void testApplyPatternWithNullRegistry() {
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("Test {0}", (Map<String, ? extends FormatFactory>) null);
        assertEquals("Test {0}", emf.toPattern());
        emf.applyPattern("New {0}");
        assertEquals("New {0}", emf.toPattern());
    }

    @Test
    public void testApplyPatternWithoutFormatElement() {
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("No format element", registry);
        assertEquals("No format element", emf.toPattern());
        assertEquals("No format element", emf.format(new Object[] {}));
    }

    @Test
    public void testApplyPatternWithSubformatPattern() {
        final String pattern = "Prefix {0,lower,subpattern} Suffix";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Prefix {0,lower,subpattern} Suffix", emf.toPattern());
        assertEquals("Prefix test Suffix", emf.format(new Object[] {"TEST"}));
    }

    @Test
    public void testApplyPatternWithQuotesAndBraces() {
        final String pattern = "Prefix '{0}' '{' {0,upper} '}' Suffix";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Prefix '{0}' '{' {0,upper} '}' Suffix", emf.toPattern());
        assertEquals("Prefix {0} { ABC } Suffix", emf.format(new Object[] {"abc"}));
    }

    @Test
    public void testApplyPatternWithDoubleQuotesInPattern() {
        final String pattern = "Prefix ''{0,lower}'' Suffix";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Prefix ''{0,lower}'' Suffix", emf.toPattern());
        assertEquals("Prefix 'abc' Suffix", emf.format(new Object[] {"ABC"}));
    }

    @Test
    public void testOverriddenFormatHandling() {
        final ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,lower}", registry);
        final Format[] originalFormats = emf.getFormats();
        assertNotNull(originalFormats);
        assertEquals(1, originalFormats.length);
        assertTrue(originalFormats[0] instanceof LowerCaseFormat);

        try {
            emf.setFormat(0, new UpperCaseFormat());
            fail("setFormat should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
        }

        try {
            emf.setFormatByArgumentIndex(0, new UpperCaseFormat());
            fail("setFormatByArgumentIndex should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
        }

        try {
            emf.setFormats(new Format[] {new UpperCaseFormat()});
            fail("setFormats should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
        }

        try {
            emf.setFormatsByArgumentIndex(new Format[] {new UpperCaseFormat()});
            fail("setFormatsByArgumentIndex should throw UnsupportedOperationException");
        } catch (final UnsupportedOperationException expected) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedQuotedString() {
        new ExtendedMessageFormat("Unterminated 'quote", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedFormatElement() {
        new ExtendedMessageFormat("Unterminated {0", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnterminatedFormatDescription() {
        new ExtendedMessageFormat("Unterminated {0,lower", registry);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgumentIndex() {
        new ExtendedMessageFormat("Invalid {not_a_number}", registry);
    }

    @Test
    public void testNestedSubformatBraces() {
        final String pattern = "Test {0,choice,0#zero|1#one: {1,lower}|2#two: {1,upper}}";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Test one: foo", emf.format(new Object[] {1, "FOO"}));
        assertEquals("Test two: BAR", emf.format(new Object[] {2, "bar"}));
    }

    @Test
    public void testWhitespaceHandling() {
        final String pattern = "Test { 0 , lower , sub } End";
        final ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Test foo End", emf.format(new Object[] {"FOO"}));
    }

    // Helper format factories and formats for testing
    private static class LowerCaseFormatFactory implements FormatFactory {
        @Override
        public Format getFormat(final String name, final String arguments, final Locale locale) {
            return new LowerCaseFormat();
        }
    }

    private static class UpperCaseFormatFactory implements FormatFactory {
        @Override
        public Format getFormat(final String name, final String arguments, final Locale locale) {
            return new UpperCaseFormat();
        }
    }

    private static class LowerCaseFormat extends Format {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
            return toAppendTo.append(obj == null ? "" : obj.toString().toLowerCase());
        }

        @Override
        public Object parseObject(final String source, final ParsePosition pos) {
            return source;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof LowerCaseFormat;
        }

        @Override
        public int hashCode() {
            return LowerCaseFormat.class.hashCode();
        }
    }

    private static class UpperCaseFormat extends Format {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos) {
            return toAppendTo.append(obj == null ? "" : obj.toString().toUpperCase());
        }

        @Override
        public Object parseObject(final String source, final ParsePosition pos) {
            return source;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof UpperCaseFormat;
        }

        @Override
        public int hashCode() {
            return UpperCaseFormat.class.hashCode();
        }
    }
}