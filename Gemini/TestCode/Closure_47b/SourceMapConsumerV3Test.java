package com.google.debugging.sourcemap;

import com.google.debugging.sourcemap.util.JsonParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SourceMapConsumerV3Test {

    @Test
    public void testBasicSourceMapParsing() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        
        String json = "{\n" +
                "  \"version\": 3,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"lineCount\": 2,\n" +
                "  \"sources\": [\"foo.js\", \"bar.js\"],\n" +
                "  \"names\": [\"src\", \"maps\"],\n" +
                "  \"mappings\": \"AAAA,CAAC,EAAE,GAAI\"\n" +
                "}";

        consumer.parse(json);

        // Test mapping lookup
        MappingEntry entry = consumer.mappingForLine(1, 1);
        assertNotNull(entry);
        assertEquals(0, entry.getLineNumber());
        assertEquals(0, entry.getColumnNumber());
        assertEquals("foo.js", entry.getSourceFile());
    }

    @Test
    public void testParseWithMappingsAndNames() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        
        // A slightly more complex mapping string
        // Segments with names and source file indices
        String json = "{\n" +
                "  \"version\": 3,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sources\": [\"a.js\", \"b.js\"],\n" +
                "  \"names\": [\"print\", \"console\"],\n" +
                "  \"mappings\": \"AAAA,SAASA,OAEE\"\n" +
                "}";

        consumer.parse(json);

        MappingEntry entry = consumer.mappingForLine(1, 1);
        assertNotNull(entry);
    }

    @Test
    public void testParseWithSections() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        
        String json = "{\n" +
                "  \"version\": 3,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sections\": [\n" +
                "    {\n" +
                "      \"offset\": {\n" +
                "        \"line\": 0,\n" +
                "        \"column\": 0\n" +
                "      },\n" +
                "      \"map\": {\n" +
                "        \"version\": 3,\n" +
                "        \"file\": \"section1.js\",\n" +
                "        \"sources\": [\"s1.js\"],\n" +
                "        \"names\": [],\n" +
                "        \"mappings\": \"AAAA\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        consumer.parse(json);
        MappingEntry entry = consumer.mappingForLine(1, 1);
        assertNotNull(entry);
        assertEquals("s1.js", entry.getSourceFile());
    }

    @Test(expected = SourceMapParseException.class)
    public void testInvalidJsonStructure() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        // Not a valid JSON object map for V3
        consumer.parse("[\"invalid\"]");
    }

    @Test(expected = SourceMapParseException.class)
    public void testMissingVersion() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        String json = "{\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sources\": [\"foo.js\"],\n" +
                "  \"mappings\": \"\"\n" +
                "}";
        consumer.parse(json);
    }

    @Test(expected = SourceMapParseException.class)
    public void testWrongVersion() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        String json = "{\n" +
                "  \"version\": 2,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sources\": [\"foo.js\"],\n" +
                "  \"mappings\": \"\"\n" +
                "}";
        consumer.parse(json);
    }

    @Test
    public void testGetMappingForLineZeroOrNegative() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        String json = "{\n" +
                "  \"version\": 3,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sources\": [\"foo.js\"],\n" +
                "  \"names\": [],\n" +
                "  \"mappings\": \"AAAA\"\n" +
                "}";
        consumer.parse(json);

        // Line numbers in consumer are 1-based. 0 or negative should return null or handle gracefully.
        assertNull(consumer.mappingForLine(0, 1));
        assertNull(consumer.mappingForLine(-1, 1));
    }

    @Test
    public void testGetMappingBeyondRange() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        String json = "{\n" +
                "  \"version\": 3,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sources\": [\"foo.js\"],\n" +
                "  \"names\": [],\n" +
                "  \"mappings\": \"AAAA\"\n" +
                "}";
        consumer.parse(json);

        // Line 100 doesn't exist
        assertNull(consumer.mappingForLine(100, 1));
    }

    @Test
    public void testSourceRootHandling() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        String json = "{\n" +
                "  \"version\": 3,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sourceRoot\": \"http://example.com/\",\n" +
                "  \"sources\": [\"foo.js\"],\n" +
                "  \"names\": [],\n" +
                "  \"mappings\": \"AAAA\"\n" +
                "}";
        consumer.parse(json);

        MappingEntry entry = consumer.mappingForLine(1, 1);
        assertNotNull(entry);
        assertEquals("http://example.com/foo.js", entry.getSourceFile());
    }

    @Test
    public void testMultipleMappingsSameLine() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        String json = "{\n" +
                "  \"version\": 3,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sources\": [\"foo.js\"],\n" +
                "  \"names\": [],\n" +
                "  \"mappings\": \"AAAA,CAAC,EAAE\"\n" +
                "}";
        consumer.parse(json);

        assertNotNull(consumer.mappingForLine(1, 1));
        assertNotNull(consumer.mappingForLine(1, 3));
    }

    @Test(expected = SourceMapParseException.class)
    public void testInvalidVLQEncoding() throws Exception {
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        String json = "{\n" +
                "  \"version\": 3,\n" +
                "  \"file\": \"out.js\",\n" +
                "  \"sources\": [\"foo.js\"],\n" +
                "  \"names\": [],\n" +
                "  \"mappings\": \"A!AA\"\n" + // '!' is an invalid base64/VLQ char
                "}";
        consumer.parse(json);
    }
}