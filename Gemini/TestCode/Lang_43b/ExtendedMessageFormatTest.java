package org.apache.commons.lang.text;

import org.junit.Before;
import org.junit.Test;

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
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link ExtendedMessageFormat}.
 */
public class ExtendedMessageFormatTest {

    private final Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();

    @Before
    public void setUp() {
        registry.put("lower", new LowerCaseFormatFactory());
        registry.put("upper", new UpperCaseFormatFactory());
        registry.put("dummy", new DummyFormatFactory());
    }

    /**
     * Test bug LANG-477: Escaped quote handling causing OutOfMemoryError / infinite loop.
     */
    @Test
    public void testEscapedQuote_LANG_477() {
        String pattern = "''";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("''", emf.toPattern());

        pattern = "''{0}''";
        emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("''{0}''", emf.toPattern());
        assertEquals("'foo'", emf.format(new Object[]{"foo"}));

        pattern = "'''{0}'''";
        emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("'''{0}'''", emf.toPattern());
        assertEquals("''foo''", emf.format(new Object[]{"foo"}));

        pattern = "''''";
        emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("''''", emf.toPattern());
        assertEquals("''", emf.format(new Object[]{}));
    }

    @Test
    public void testExtendedFormats() {
        String pattern = "Test: {0,lower} and {1,upper}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Test: hello and WORLD", emf.format(new Object[]{"HELLO", "world"}));
    }

    @Test
    public void testBuiltInChoiceFormat() {
        String pattern = "Choice: {0,choice,1#one|2#two|3#three}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Choice: two", emf.format(new Object[]{2}));
    }

    @Test
    public void testBuiltInDateTimeFormat() {
        Calendar cal = Calendar.getInstance();
        cal.set(2008, Calendar.JANUARY, 1, 12, 0, 0);

        String pattern = "Date: {0,date,short}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, Locale.US, registry);
        MessageFormat mf = new MessageFormat(pattern, Locale.US);

        assertEquals(mf.format(new Object[]{cal.getTime()}), emf.format(new Object[]{cal.getTime()}));
    }

    @Test
    public void testBuiltInNumberFormat() {
        String pattern = "Number: {0,number,currency}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, Locale.US, registry);
        MessageFormat mf = new MessageFormat(pattern, Locale.US);

        assertEquals(mf.format(new Object[]{1234.56}), emf.format(new Object[]{1234.56}));
    }

    @Test
    public void testCustomAndBuiltInTogether() {
        Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.JANUARY, 23, 18, 33, 5);

        String pattern = "Hi {0,lower}, today is {1,date,yyyy-MM-dd} and number is {2,number,#0.00}!";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, Locale.US, registry);
        assertEquals("Hi john, today is 2007-01-23 and number is 3.14!", emf.format(new Object[]{"JOHN", cal.getTime(), 3.14159}));
    }

    @Test
    public void testNoRegistryConstructors() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Hello {0}");
        assertEquals("Hello {0}", emf1.toPattern());
        assertEquals("Hello World", emf1.format(new Object[]{"World"}));

        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Hello {0}", Locale.US);
        assertEquals("Hello {0}", emf2.toPattern());
        assertEquals("Hello World", emf2.format(new Object[]{"World"}));
    }

    @Test
    public void testNullRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}", (Map<String, ?>) null);
        assertEquals("Hello World", emf.format(new Object[]{"World"}));

        ExtendedMessageFormat emfLocale = new ExtendedMessageFormat("Hello {0}", Locale.US, null);
        assertEquals("Hello World", emfLocale.format(new Object[]{"World"}));
    }

    @Test
    public void testApplyPatternWithNullRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}");
        emf.applyPattern("Goodbye {0}");
        assertEquals("Goodbye World", emf.format(new Object[]{"World"}));
    }

    @Test
    public void testEmptyPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("", registry);
        assertEquals("", emf.toPattern());
        assertEquals("", emf.format(new Object[]{}));
    }

    @Test
    public void testQuotedPatternWithRegistry() {
        String pattern = "Format '{0,lower}' produces {0,lower}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Format {0,lower} produces bar", emf.format(new Object[]{"BAR"}));
    }

    @Test
    public void testQuotedQuotes() {
        String pattern = "You''ll find {0,lower} here";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("You'll find it here", emf.format(new Object[]{"IT"}));
    }

    @Test
    public void testEscapedBraces() {
        String pattern = "Quotes '' and braces '{' '}' and {0,upper}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Quotes ' and braces { } and TEST", emf.format(new Object[]{"test"}));
    }

    @Test
    public void testMissingFormatArgs() {
        try {
            new ExtendedMessageFormat("Invalid {", registry);
            fail("Expected IllegalArgumentException for unclosed format element");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            new ExtendedMessageFormat("Invalid {0", registry);
            fail("Expected IllegalArgumentException for unclosed format element index");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            new ExtendedMessageFormat("Invalid {0,", registry);
            fail("Expected IllegalArgumentException for unclosed format element type");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            new ExtendedMessageFormat("Invalid {0,lower", registry);
            fail("Expected IllegalArgumentException for unclosed format element description");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testInvalidFormatElementIndex() {
        try {
            new ExtendedMessageFormat("Invalid {abc}", registry);
            fail("Expected IllegalArgumentException for non-numeric format element index");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            new ExtendedMessageFormat("Invalid {-1}", registry);
            fail("Expected IllegalArgumentException for negative format element index");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testUnterminatedQuotedString() {
        try {
            new ExtendedMessageFormat("Unterminated quote ' test", registry);
            fail("Expected IllegalArgumentException for unterminated quote");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Pattern {0,lower}", registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Pattern {0,lower}", registry);
        ExtendedMessageFormat emf3 = new ExtendedMessageFormat("Pattern {0,upper}", registry);

        assertEquals(emf1, emf2);
        assertEquals(emf1.hashCode(), emf2.hashCode());

        assertFalse(emf1.equals(emf3));
        assertFalse(emf1.equals(null));
        assertFalse(emf1.equals("Not an ExtendedMessageFormat"));
        assertTrue(emf1.equals(emf1));

        Map<String, FormatFactory> otherRegistry = new HashMap<String, FormatFactory>();
        otherRegistry.put("lower", new LowerCaseFormatFactory());
        ExtendedMessageFormat emf4 = new ExtendedMessageFormat("Pattern {0,lower}", otherRegistry);
        assertEquals(emf1, emf4);

        ExtendedMessageFormat emfNoReg1 = new ExtendedMessageFormat("Pattern {0}");
        ExtendedMessageFormat emfNoReg2 = new ExtendedMessageFormat("Pattern {0}");
        assertEquals(emfNoReg1, emfNoReg2);
        assertEquals(emfNoReg1.hashCode(), emfNoReg2.hashCode());
        assertFalse(emf1.equals(emfNoReg1));
        assertFalse(emfNoReg1.equals(emf1));
    }

    @Test
    public void testToPatternWithCustomFormats() {
        String pattern = "Custom: {0,lower} and normal {1}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals(pattern, emf.toPattern());
    }

    @Test
    public void testToPatternWithNestedQuotesAndBraces() {
        String pattern = "Nested: '{'{0,lower}'}' and ''{1,upper}''";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals(pattern, emf.toPattern());
    }

    @Test
    public void testOverrideFormatMethodsThrowException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Pattern {0,lower}", registry);

        try {
            emf.setFormat(0, new LowerCaseFormat());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        try {
            emf.setFormatByArgumentIndex(0, new LowerCaseFormat());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        try {
            emf.setFormats(new Format[]{new LowerCaseFormat()});
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        try {
            emf.setFormatsByArgumentIndex(new Format[]{new LowerCaseFormat()});
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testComplexSubFormats() {
        String pattern = "SubFormat: {0,dummy,option{a=1,b=2}}";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("SubFormat: [option{a=1,b=2}:VALUE]", emf.format(new Object[]{"VALUE"}));
    }

    @Test
    public void testWhitespaceHandlingInFormatElements() {
        String pattern = "Spaces: { 0 , lower } and { 1 , upper }";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("Spaces: abc and XYZ", emf.format(new Object[]{"ABC", "xyz"}));
    }

    // --- Helper Mock Format & Factory Classes ---

    private static class LowerCaseFormatFactory implements FormatFactory {
        public Format getFormat(String name, String arguments, Locale locale) {
            return new LowerCaseFormat();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass().equals(getClass());
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    private static class UpperCaseFormatFactory implements FormatFactory {
        public Format getFormat(String name, String arguments, Locale locale) {
            return new UpperCaseFormat();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass().equals(getClass());
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    private static class DummyFormatFactory implements FormatFactory {
        public Format getFormat(String name, String arguments, Locale locale) {
            return new DummyFormat(arguments);
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass().equals(getClass());
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    private static class LowerCaseFormat extends Format {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(obj == null ? "" : obj.toString().toLowerCase());
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass().equals(getClass());
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    private static class UpperCaseFormat extends Format {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(obj == null ? "" : obj.toString().toUpperCase());
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj.getClass().equals(getClass());
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    private static class DummyFormat extends Format {
        private static final long serialVersionUID = 1L;
        private final String args;

        public DummyFormat(String args) {
            this.args = args;
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append("[").append(args).append(":").append(obj).append("]");
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(getClass())) {
                return false;
            }
            DummyFormat other = (DummyFormat) obj;
            return (args == null && other.args == null) || (args != null && args.equals(other.args));
        }

        @Override
        public int hashCode() {
            return args == null ? 0 : args.hashCode();
        }
    }
}