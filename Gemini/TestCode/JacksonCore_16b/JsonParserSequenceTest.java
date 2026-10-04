package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.JsonLocation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class JsonParserSequenceTest {

    // Mock parser to help test JsonParserSequence
    private static class DummyJsonParser extends com.fasterxml.jackson.core.base.ParserBase {
        private final String name;
        private JsonToken currentToken;

        public DummyJsonParser(String name, JsonToken initialToken) {
            super(null, 0);
            this.name = name;
            this.currentToken = initialToken;
        }

        @Override
        public com.fasterxml.jackson.core.JsonVersion version() {
            return com.fasterxml.jackson.core.Version.unknownVersion();
        }

        @Override
        protected void _close_input() throws IOException {}

        @Override
        protected void _finishToken() throws IOException {}

        @Override
        public JsonToken nextToken() throws IOException {
            return currentToken;
        }

        public void setCurrentToken(JsonToken t) {
            this.currentToken = t;
        }

        @Override public String getCurrentName() { return name; }
        @Override public void overrideCurrentName(String name) {}
        @Override public String getText() { return name; }
        @Override public char[] getTextCharacters() { return name.toCharArray(); }
        @Override public int getTextLength() { return name.length(); }
        @Override public int getTextOffset() { return 0; }
        @Override public boolean hasTextCharacters() { return false; }
        @Override public JsonLocation getTokenLocation() { return JsonLocation.NA; }
        @Override public JsonLocation getCurrentLocation() { return JsonLocation.NA; }
        @Override public NumberType getNumberType() { return NumberType.INT; }
        @Override public Number getNumberValue() { return 1; }
        @Override public int getIntValue() { return 1; }
        @Override public long getLongValue() { return 1L; }
        @Override public BigInteger getBigIntegerValue() { return BigInteger.ONE; }
        @Override public float getFloatValue() { return 1.0f; }
        @Override public double getDoubleValue() { return 1.0; }
        @Override public BigDecimal getDecimalValue() { return BigDecimal.ONE; }
        @Override public byte[] getBinaryValue(com.fasterxml.jackson.core.Base64Variant b64) { return new byte[0]; }
    }

    @Test
    public void testCreateFlattenedSimple() {
        DummyJsonParser p1 = new DummyJsonParser("p1", JsonToken.START_OBJECT);
        DummyJsonParser p2 = new DummyJsonParser("p2", JsonToken.END_OBJECT);

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(2, seq1.containedParsersCount());

        // Test flattening nested JsonParserSequences
        DummyJsonParser p3 = new DummyJsonParser("p3", JsonToken.VALUE_STRING);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(seq1, p3);
        // Should flatten into 3 parsers
        assertEquals(3, seq2.containedParsersCount());

        JsonParserSequence seq3 = JsonParserSequence.createFlattened(p3, seq1);
        assertEquals(3, seq3.containedParsersCount());

        JsonParserSequence seq4 = JsonParserSequence.createFlattened(seq1, seq1);
        assertEquals(4, seq4.containedParsersCount());
    }

    @Test
    public void testSingleParserSequenceBehaviors() throws IOException {
        DummyJsonParser p1 = new DummyJsonParser("p1", JsonToken.START_ARRAY);
        p1.setCurrentToken(null);

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p1);
        assertNotNull(seq);
        assertEquals(2, seq.containedParsersCount());
        
        // Test delegates
        assertFalse(seq.isClosed());
        seq.close();
        assertTrue(seq.isClosed());
    }

    @Test
    public void testSwitchBetweenParsers() throws IOException {
        DummyJsonParser p1 = new DummyJsonParser("p1", null); // will return null/switch
        DummyJsonParser p2 = new DummyJsonParser("p2", JsonToken.VALUE_TRUE);

        // If p1 returns null on nextToken(), it should advance to p2
        // Wait, let's look at standard nextToken in JsonParserSequence:
        // If current parser returns null, it switches to the next one.
        
        // Let's implement a parser that returns a token then null.
        JsonParser p_first = new DummyJsonParser("f", JsonToken.START_OBJECT) {
            private boolean called = false;
            @Override
            public JsonToken nextToken() throws IOException {
                if (!called) {
                    called = true;
                    return JsonToken.START_OBJECT;
                }
                return null;
            }
        };

        JsonParser p_second = new DummyJsonParser("s", JsonToken.END_OBJECT);

        JsonParserSequence seq = JsonParserSequence.createFlattened(p_first, p_second);
        
        assertEquals(JsonToken.START_OBJECT, seq.nextToken());
        // Now p_first returns null, should switch to p_second
        assertEquals(JsonToken.END_OBJECT, seq.nextToken());
    }

    @Test
    public void testSkipChildren() throws IOException {
        DummyJsonParser p1 = new DummyJsonParser("p1", JsonToken.START_OBJECT);
        DummyJsonParser p2 = new DummyJsonParser("p2", JsonToken.END_OBJECT);

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq.skipChildren();
        // Just verify it doesn't throw and runs through
    }

    @Test
    public void testOverlappingAndNestedFlattening() {
        // Specifically exercise the private/protected constructors and flattening logic thoroughly
        List<JsonParser> list1 = new ArrayList<>();
        DummyJsonParser p1 = new DummyJsonParser("1", JsonToken.VALUE_FALSE);
        DummyJsonParser p2 = new DummyJsonParser("2", JsonToken.VALUE_TRUE);
        list1.add(p1);
        list1.add(p2);
        
        JsonParserSequence s1 = JsonParserSequence.createFlattened(p1, p2);
        
        List<JsonParser> list2 = new ArrayList<>();
        list2.add(s1);
        list2.add(p1);
        
        // Directly invoke or use createFlattened with sequences
        JsonParserSequence s2 = JsonParserSequence.createFlattened(s1, s1);
        assertEquals(4, s2.containedParsersCount());
    }
}