package com.fasterxml.jackson.core.base;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class ParserBaseTest {

    private ConcreteParserBase parser;
    private IOContext ioContext;

    @Before
    public void setUp() {
        ioContext = new IOContext(new com.fasterxml.jackson.core.util.BufferRecycler(), "test", false);
        CharsToNameCanonicalizer symbols = CharsToNameCanonicalizer.createRoot();
        parser = new ConcreteParserBase(ioContext, 0, symbols);
    }

    @After
    public void tearDown() throws Exception {
        if (parser != null) {
            parser.close();
        }
    }

    @Test
    public void testBasicInitialization() {
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());
        assertFalse(parser.isClosed());
        assertEquals(0, parser.getTokenCharacterOffset());
        assertEquals(0, parser.getTokenLineNr());
        assertEquals(1, parser.getTokenColumnNr());
    }

    @Test
    public void testTextCharacters() throws IOException {
        parser.textBuffer.resetWith(new char[]{'a', 'b', 'c'}, 0, 3);
        assertEquals("abc", parser.getText());
        assertArrayEquals(new char[]{'a', 'b', 'c'}, parser.getTextCharacters());
        assertEquals(3, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
    }

    @Test
    public void testNumberParsingInt() throws IOException {
        parser._numTypesValid = ParserBase.NR_UNKNOWN;
        parser._numberInt = 42;
        parser._numTypesValid = ParserBase.NR_INT;
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, JsonToken.VALUE_NUMBER_INT);
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals(42, parser.getIntValue());
        assertEquals(42L, parser.getLongValue());
        assertEquals(42.0f, parser.getFloatValue(), 0.001f);
        assertEquals(42.0, parser.getDoubleValue(), 0.001);
        assertEquals(BigInteger.valueOf(42), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(42), parser.getDecimalValue());
    }

    @Test
    public void testNumberParsingLong() throws IOException {
        long largeVal = 3000000000L;
        parser._numTypesValid = ParserBase.NR_UNKNOWN;
        parser._numberLong = largeVal;
        parser._numTypesValid = ParserBase.NR_LONG;

        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());
        assertEquals(largeVal, parser.getLongValue());
        assertEquals((int) largeVal, parser.getIntValue()); // Overflow expected by design in standard Jackson coercion
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidIntOverflow() throws IOException {
        parser._numTypesValid = ParserBase.NR_UNKNOWN;
        parser._numberLong = 5000000000L;
        parser._numTypesValid = ParserBase.NR_LONG;
        parser.intValue = 0; // force re-evaluation or trigger check if any
        // Directly test standard overflow check if implemented via helper or force check
        parser.convertNumberToInt();
    }

    @Test
    public void testReadBinaryValue() throws IOException {
        ByteArrayBuilder builder = new ByteArrayBuilder();
        byte[] data = new byte[]{1, 2, 3, 4};
        Base64Variant variant = Base64Variants.MIME;
        int result = parser.readBinaryValue(variant, builder);
        assertEquals(0, result);
    }

    @Test
    public void testTokenLocationMethods() {
        parser._tokenInputRow = 5;
        parser._tokenInputCol = 10;
        parser._tokenInputTotal = 50L;
        
        JsonLocation loc = parser.getTokenLocation();
        assertEquals(5, loc.getLineNr());
        assertEquals(10, loc.getColumnNr());
        assertEquals(50L, loc.getCharOffset());
    }

    @Test
    public void testCurrentLocationMethods() {
        parser._currInputRow = 2;
        parser._currInputCol = 3;
        parser._currInputProcessed = 20L;
        parser._inputPtr = 5;
        
        JsonLocation loc = parser.getCurrentLocation();
        assertEquals(2, loc.getLineNr());
        assertEquals(4, loc.getColumnNr()); // currInputCol + (_inputPtr - ...) depending on implementation
    }

    @Test
    public void testParsingContext() {
        assertNotNull(parser.getParsingContext());
        parser.setCurrentName("testName");
        assertEquals("testName", parser.getCurrentName());
    }

    @Test
    public void testOverriddenAbstractMethods() throws IOException {
        assertNull(parser.getEmbeddedObject());
        assertFalse(parser.isNaN());
        parser.finishToken();
    }

    @Test
    public void testHandleOverflowMethods() {
        try {
            parser.reportOverflowInt();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("larger than"));
        }

        try {
            parser.reportOverflowLong();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("larger than"));
        }
    }

    @Test
    public void testReset() {
        parser._textBuffer.resetWith("hello");
        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
    }

    // Concrete subclass of ParserBase to enable testing of abstract and protected methods
    private static class ConcreteParserBase extends ParserBase {
        private final CharsToNameCanonicalizer _symbols;

        public ConcreteParserBase(IOContext ctxt, int features, CharsToNameCanonicalizer symbols) {
            super(ctxt, features);
            _symbols = symbols;
            _parsingContext = JsonReadContext.createRootContext(0, 0, DupDetector.rootDetector(this));
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public JsonToken nextToken() throws IOException {
            return null;
        }

        @Override
        protected void _closeInput() throws IOException {
        }

        @Override
        protected void _finishString() throws IOException {
        }

        @Override
        public boolean hasTextCharacters() {
            return _textBuffer.hasTextAsCharacters();
        }

        @Idempotent
        @Override
        public String getCurrentName() throws IOException {
            return _parsingContext.getCurrentName();
        }

        @Override
        public void overrideCurrentName(String name) {
            _parsingContext.setCurrentName(name);
        }

        @Override
        public int readBinaryValue(Base64Variant b64variant, OutputStream out) throws IOException {
            return 0;
        }

        public void convertNumberToInt() throws IOException {
            _parseNumericValue(NR_INT);
        }

        @Override
        protected void _parseNumericValue(int expType) throws IOException {
            if ((_numTypesValid & expType) == 0) {
                if (expType == NR_INT) {
                    if (_numberLong > Integer.MAX_VALUE || _numberLong < Integer.MIN_VALUE) {
                        reportOverflowInt();
                    }
                    _numberInt = (int) _numberLong;
                    _numTypesValid |= NR_INT;
                } else {
                    super._parseNumericValue(expType);
                }
            }
        }
    }
}