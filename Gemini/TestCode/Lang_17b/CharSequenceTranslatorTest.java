package org.apache.commons.lang3.text.translate;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CharSequenceTranslatorTest {

    @Test
    public void testTranslateNullInput() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        assertNull(translator.translate(null));

        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateEmptyInput() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        assertEquals("", translator.translate(""));

        StringWriter writer = new StringWriter();
        translator.translate("", writer);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateNullWriter() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        try {
            translator.translate("test", null);
            fail("Expected IllegalArgumentException when writer is null");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testTranslateNoOp() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        String input = "Hello World!";
        String result = translator.translate(input);
        assertEquals("Hello World!", result);
    }

    @Test
    public void testTranslateSurrogatePairNoOp() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        // Supplementary character (U+20BB7: \uD842\uDFB7) followed by 'A'
        String input = "\uD842\uDFB7A";
        String result = translator.translate(input);
        assertEquals("\uD842\uDFB7A", result);
    }

    @Test
    public void testTranslateSurrogatePairMultiple() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        // Multiple supplementary characters interspersed with regular characters
        String input = "\uD842\uDFB7_\uD83D\uDE00_test_\uD83D\uDE01";
        String result = translator.translate(input);
        assertEquals("\uD842\uDFB7_\uD83D\uDE00_test_\uD83D\uDE01", result);
    }

    @Test
    public void testTranslateSimpleSubstitution() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'a') {
                    out.write("X");
                    return 1;
                }
                return 0;
            }
        };

        String input = "banana";
        String result = translator.translate(input);
        assertEquals("bXnXnX", result);
    }

    @Test
    public void testTranslateConsumeMultipleCharacters() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index <= input.length() - 2 && input.charAt(index) == 't' && input.charAt(index + 1) == 'e') {
                    out.write("TE");
                    return 2;
                }
                return 0;
            }
        };

        String input = "tester";
        String result = translator.translate(input);
        assertEquals("TEster", result);
    }

    @Test
    public void testTranslateWithIOExceptionWrapped() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                throw new IOException("Simulated IO failure");
            }
        };

        try {
            translator.translate("abc");
            fail("Expected RuntimeException wrapping IOException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
            assertEquals("Simulated IO failure", e.getCause().getMessage());
        }
    }

    @Test
    public void testWithChain() {
        CharSequenceTranslator t1 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'a') {
                    out.write('1');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator t2 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'b') {
                    out.write('2');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator chained = t1.with(t2);
        String result = chained.translate("abc");
        assertEquals("12c", result);
    }

    @Test
    public void testWithMultipleChains() {
        CharSequenceTranslator t1 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'a') {
                    out.write('A');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator t2 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'b') {
                    out.write('B');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator t3 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'c') {
                    out.write('C');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator chained = t1.with(t2, t3);
        String result = chained.translate("abcd");
        assertEquals("ABCd", result);
    }

    @Test
    public void testHex() {
        assertEquals("0", CharSequenceTranslator.hex(0));
        assertEquals("A", CharSequenceTranslator.hex(10));
        assertEquals("F", CharSequenceTranslator.hex(15));
        assertEquals("10", CharSequenceTranslator.hex(16));
        assertEquals("FF", CharSequenceTranslator.hex(255));
        assertEquals("1000", CharSequenceTranslator.hex(4096));
        assertEquals("10FFFF", CharSequenceTranslator.hex(0x10FFFF).toUpperCase(Locale.ENGLISH));
    }
}