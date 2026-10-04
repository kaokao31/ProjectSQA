package org.apache.commons.lang3.text.translate;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class CharSequenceTranslatorTest {

    @Test
    public void testTranslateNullCharSequence() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        assertNull(translator.translate(null));
    }

    @Test
    public void testTranslateEmptyCharSequence() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        assertEquals("", translator.translate(""));
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
            fail("Expected IllegalArgumentException on null writer");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testTranslateNullInputWithWriter() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateNoOp() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0; // indicates character was not consumed/translated
            }
        };

        String input = "Hello World!";
        String output = translator.translate(input);
        assertEquals(input, output);
    }

    @Test
    public void testTranslateCustomReplacement() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'a') {
                    out.write("A");
                    return 1;
                }
                return 0;
            }
        };

        assertEquals("bAnAnA", translator.translate("banana"));
    }

    @Test
    public void testTranslateMultipleCharactersConsumed() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index <= input.length() - 2 && input.charAt(index) == 't' && input.charAt(index + 1) == 'h') {
                    out.write("TH");
                    return 2;
                }
                return 0;
            }
        };

        assertEquals("THat THing", translator.translate("that thing"));
    }

    @Test
    public void testTranslateSurrogatePairs() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                int cp = Character.codePointAt(input, index);
                if (cp > 0xFFFF) {
                    out.write(String.format("\\u%04X\\u%04X",
                            (int) input.charAt(index), (int) input.charAt(index + 1)));
                    return 2;
                }
                return 0;
            }
        };

        // Supplementary character (surrogate pair)
        // Code point U+1D11E (MUSICAL SYMBOL G CLEF) = "\uD834\uDD1E"
        String input = "a\uD834\uDD1Eb";
        String expected = "a\\uD834\\uDD1Eb";
        assertEquals(expected, translator.translate(input));
    }

    @Test
    public void testTranslateSurrogatePairsPassThrough() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0; // pass through unmodified
            }
        };

        String input = "\uD834\uDD1E";
        assertEquals(input, translator.translate(input));
    }

    @Test
    public void testTranslateSurrogatePairAtEndOfString() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                int cp = Character.codePointAt(input, index);
                if (cp > 0xFFFF) {
                    out.write("SURROGATE");
                    return 2;
                }
                return 0;
            }
        };

        String input = "test\uD83D\uDE00";
        assertEquals("testSURROGATE", translator.translate(input));
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

        CharSequenceTranslator combined = t1.with(t2);
        assertEquals("12c", combined.translate("abc"));
    }

    @Test
    public void testHex() {
        assertEquals("0", CharSequenceTranslator.hex(0));
        assertEquals("A", CharSequenceTranslator.hex(10));
        assertEquals("F", CharSequenceTranslator.hex(15));
        assertEquals("10", CharSequenceTranslator.hex(16));
        assertEquals("FFFF", CharSequenceTranslator.hex(65535));
        assertEquals("1D11E", CharSequenceTranslator.hex(0x1D11E));
    }

    @Test
    public void testTranslateThrowsRuntimeExceptionOnIOException() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                throw new IOException("Simulated IO Exception");
            }
        };

        try {
            translator.translate("test");
            fail("Expected RuntimeException wrapping IOException");
        } catch (RuntimeException e) {
            assertEquals("java.io.IOException: Simulated IO Exception", e.getMessage());
        }
    }
}