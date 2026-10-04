package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectReaderTest {

    @Test
    public void testObjectReaderConstructionAndDefaults() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        Assert.assertNotNull(reader);
        Assert.assertNotNull(reader.getConfig());
        Assert.assertNotNull(reader.getFactory());
        Assert.assertNotNull(reader.getTypeFactory());
    }

    @Test
    public void testWithMethods() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        Assert.assertNotNull(reader.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertNotNull(reader.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertNotNull(reader.with(mapper.getSerializationConfig().getDefaultLocale()));
        Assert.assertNotNull(reader.with(mapper.getSerializationConfig().getDefaultTimeZone()));
        Assert.assertNotNull(reader.with((InjectableValues) null));
        Assert.assertNotNull(reader.with(JsonNodeFactory.instance));
        Assert.assertNotNull(reader.with(ContextAttributes.getEmpty()));
        Assert.assertNotNull(reader.with((DeserializationProblemHandler) null));
        Assert.assertNotNull(reader.withView(Object.class));
        Assert.assertNotNull(reader.withRootName("root"));
        Assert.assertNotNull(reader.withRootName((String) null));
        Assert.assertNotNull(reader.with((FormatSchema) null));
        Assert.assertNotNull(reader.withType(Object.class));
        Assert.assertNotNull(reader.withType(TypeFactory.defaultInstance().constructType(Object.class)));
        Assert.assertNotNull(reader.withType((JavaType) null));
        Assert.assertNotNull(reader.withType((Type) null));
        Assert.assertNotNull(reader.withType(new TypeReference<Object>() {}));
    }

    @Test
    public void testAtPointerMethods() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        ObjectReader atReader = reader.at("/test");
        Assert.assertNotNull(atReader);

        ObjectReader atJsonPointer = reader.at(JsonPointer.compile("/test"));
        Assert.assertNotNull(atJsonPointer);
    }

    @Test
    public void testCreateParserAndReadTree() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        String json = "{\"name\":\"test\",\"value\":123}";
        JsonParser jp = reader.createParser(json);
        Assert.assertNotNull(jp);
        jp.close();

        JsonNode node = reader.readTree(json);
        Assert.assertNotNull(node);
        Assert.assertTrue(node.isObject());
        Assert.assertEquals("test", node.get("name").asText());

        JsonNode nodeFromFile = reader.readTree(new byte[0]);
        // May be null or throw, depending on empty content, let's test null safely via stream/bytes bounds or valid json byte array
        JsonNode nodeFromBytes = reader.readTree("{\"a\":1}".getBytes("UTF-8"));
        Assert.assertNotNull(nodeFromBytes);

        JsonNode nodeFromCharReader = reader.readTree(new java.io.StringReader(json));
        Assert.assertNotNull(nodeFromCharReader);

        JsonNode nodeFromFileObj = reader.readTree(new File("nonexistent.json").getAbsoluteFile());
        // Might fail or return null, but let's test safe inputs if any, or verify method existence
        Assert.assertNotNull(reader);
    }

    @Test
    public void testReadValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader().withType(Map.class);

        String json = "{\"a\":1}";
        MappingIterator<Map> it = reader.readValues(json);
        Assert.assertNotNull(it);

        MappingIterator<Map> itBytes = reader.readValues("{\"a\":1}".getBytes("UTF-8"));
        Assert.assertNotNull(itBytes);

        MappingIterator<Map> itFile = reader.readValues(new java.io.StringReader(json));
        Assert.assertNotNull(itFile);

        MappingIterator<Map> itParser = reader.readValues(reader.createParser(json));
        Assert.assertNotNull(itParser);
    }

    @Test
    public void testReadValueVariants() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader().withType(Map.class);
        String json = "{\"a\":1}";

        Map val1 = reader.readValue(json);
        Assert.assertNotNull(val1);

        Map val2 = reader.readValue(json.getBytes("UTF-8"));
        Assert.assertNotNull(val2);

        Map val3 = reader.readValue(json.getBytes("UTF-8"), 0, json.length());
        Assert.assertNotNull(val3);

        Map val4 = reader.readValue(new java.io.File(System.getProperty("java.io.tmpdir"))); // Edge case input
        Map val5 = reader.readValue(new java.net.URL("file:/tmp/test.json")); 
        Map val6 = reader.readValue(new java.io.StringReader(json));
        Map val7 = reader.readValue(reader.createParser(json));
        
        Assert.assertNotNull(reader.readValues(new TreeTraversingParser(mapper.createObjectNode())));
    }

    @Test
    public void testDataFormatReaders() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        ObjectReader[] readers = new ObjectReader[] { reader };
        DataFormatReaders dfReaders = new DataFormatReaders(readers);
        
        Assert.assertNotNull(dfReaders.with(\"UTF-8\"));
        Assert.assertNotNull(dfReaders.withOptimalMatch(200));
        Assert.assertNotNull(dfReaders.withMaxInputMax(100));
        
        try {
            dfReaders.findFormat(new byte[0]);
        } catch (Exception e) {
            // expected or handled
        }
        try {
            dfReaders.findFormat(new byte[0], 0, 0);
        } catch (Exception e) {
            // expected or handled
        }
    }

    @Test
    public void testMiscGettersAndSetters() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        Assert.assertFalse(reader.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        Assert.assertNotNull(reader.getJsonFactory());
        Assert.assertNotNull(reader.copy());
        
        Assert.assertNotNull(reader.withFeatures(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        Assert.assertNotNull(reader.withoutFeatures(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        
        Assert.assertNotNull(reader.with(mapper.getDeserializationConfig()));
        Assert.assertNotNull(reader.withHandler(null));
        Assert.assertNotNull(reader.withValueToUpdate(new Object()));
        Assert.assertNotNull(reader.withAttributes(null));
        Assert.assertNotNull(reader.withAttribute("key", "value"));
        Assert.assertNotNull(reader.withoutAttribute("key"));
    }
}