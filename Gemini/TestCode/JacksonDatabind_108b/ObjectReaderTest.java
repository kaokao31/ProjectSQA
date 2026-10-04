package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class ObjectReaderTest {

    private ObjectMapper objectMapper;
    private ObjectReader objectReader;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectReader = objectMapper.reader();
    }

    @After
    public void tearDown() {
        objectMapper = null;
        objectReader = null;
    }

    @Test
    public void testWithMethodsCoverage() {
        assertNotNull(objectReader.with(objectMapper.getSerializationConfig()));
        assertNotNull(objectReader.with(objectMapper.getDeserializationConfig()));
        assertNotNull(objectReader.with(JsonNodeFactory.instance));
        assertNotNull(objectReader.with(TimeZone.getDefault()));
        assertNotNull(objectReader.with(Locale.getDefault()));
        assertNotNull(objectReader.with(ContextAttributes.getEmpty()));
        assertNotNull(objectReader.with(new JsonFactory()));
        assertNotNull(objectReader.withRootName("testRoot"));
        assertNotNull(objectReader.withRootName((String) null));
    }

    @Test
    public void testWithFeatureMethods() {
        assertNotNull(objectReader.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertNotNull(objectReader.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertNotNull(objectReader.with(JsonParser.Feature.ALLOW_COMMENTS));
        assertNotNull(objectReader.without(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test
    public void testAtPointerAndPath() {
        assertNotNull(objectReader.at("/test"));
        assertNotNull(objectReader.at(JsonPointer.compile("/test")));
    }

    @Test
    public void testForTypeMethods() {
        assertNotNull(objectReader.forType(Object.class));
        assertNotNull(objectReader.forType(new TypeReference<Map<String, Object>>() {}));
        assertNotNull(objectReader.forType(objectMapper.constructType(Object.class)));
        assertNotNull(objectReader.forType((JavaType) null));
    }

    @Test
    public void testValueToTreeAndReadTree() throws Exception {
        String json = "{\"key\":\"value\"}";
        JsonNode node = objectReader.readTree(json);
        assertNotNull(node);
        assertTrue(node.isObject());

        JsonNode nodeFromBytes = objectReader.readTree(json.getBytes("UTF-8"));
        assertNotNull(nodeFromBytes);

        JsonNode nodeFromFile = objectReader.readTree(new File("nonexistent.json").getAbsoluteFile());
        assertNotNull(nodeFromFile);

        JsonNode nodeFromUrl = objectReader.readTree(new URL("http://localhost/nonexistent"));
        assertNotNull(nodeFromUrl);

        JsonNode nodeFromReader = objectReader.readTree(new StringReader(json));
        assertNotNull(nodeFromReader);

        JsonNode nodeFromStream = objectReader.readTree(new ByteArrayInputStream(json.getBytes("UTF-8")));
        assertNotNull(nodeFromStream);
    }

    @Test
    public void testReadValuesAs() throws Exception {
        String json = "[{\"key\":\"value1\"},{\"key\":\"value2\"}]";
        MappingIterator<Map> iterator = objectReader.forType(Map.class).readValues(json);
        assertNotNull(iterator);
        
        MappingIterator<Map> iteratorBytes = objectReader.forType(Map.class).readValues(json.getBytes("UTF-8"));
        assertNotNull(iteratorBytes);

        MappingIterator<Map> iteratorFile = objectReader.forType(Map.class).readValues(new File("nonexistent.json").getAbsoluteFile());
        assertNotNull(iteratorFile);

        MappingIterator<Map> iteratorUrl = objectReader.forType(Map.class).readValues(new URL("http://localhost/nonexistent"));
        assertNotNull(iteratorUrl);

        MappingIterator<Map> iteratorReader = objectReader.forType(Map.class).readValues(new StringReader(json));
        assertNotNull(iteratorReader);

        MappingIterator<Map> iteratorStream = objectReader.forType(Map.class).readValues(new ByteArrayInputStream(json.getBytes("UTF-8")));
        assertNotNull(iteratorStream);
        
        JsonParser parser = objectMapper.getFactory().createParser(json);
        MappingIterator<Map> iteratorParser = objectReader.forType(Map.class).readValues(parser);
        assertNotNull(iteratorParser);
    }

    @Test
    public void testReadValueVariations() throws Exception {
        String json = "{\"key\":\"value\"}";
        
        Map result1 = objectReader.forType(Map.class).readValue(json);
        assertNotNull(result1);

        Map result2 = objectReader.forType(Map.class).readValue(json.getBytes("UTF-8"));
        assertNotNull(result2);

        Map result3 = objectReader.forType(Map.class).readValue(new File("nonexistent.json").getAbsoluteFile());
        assertNotNull(result3);

        Map result4 = objectReader.forType(Map.class).readValue(new URL("http://localhost/nonexistent"));
        assertNotNull(result4);

        Map result5 = objectReader.forType(Map.class).readValue(new StringReader(json));
        assertNotNull(result5);

        Map result6 = objectReader.forType(Map.class).readValue(new ByteArrayInputStream(json.getBytes("UTF-8")));
        assertNotNull(result6);
        
        JsonParser parser = objectMapper.getFactory().createParser(json);
        Map result7 = objectReader.forType(Map.class).readValue(parser);
        assertNotNull(result7);

        TokenBuffer tb = new TokenBuffer(objectMapper, false);
        tb.writeString("test");
        Map result8 = objectReader.forType(Map.class).readValue(tb.asParser());
        assertNotNull(result8);
    }

    @Test
    public void testWithHandlerAndIncludeds() {
        assertNotNull(objectReader.with((DeserializationConfig) null));
        assertNotNull(objectReader.withHandler(null));
        assertNotNull(objectReader.withValueToUpdate(new Object()));
        assertNotNull(objectReader.withView(Object.class));
        assertNotNull(objectReader.with((Locale) null));
        assertNotNull(objectReader.with((TimeZone) null));
        assertNotNull(objectReader.with((ContextAttributes) null));
    }

    @Test
    public void testDataFormatReaders() {
        ObjectReader[] readers = new ObjectReader[] { objectReader };
        DataFormatReaders dfr = new DataFormatReaders(readers);
        assertNotNull(objectReader.with(dfr));
    }

    @Test
    public void testGettersAndConfig() {
        assertNotNull(objectReader.getConfig());
        assertNotNull(objectReader.getFactory());
        assertNotNull(objectReader.getTypeFactory());
        assertNotNull(objectReader.getInjectableValues());
        assertEquals(Object.class, objectReader.getValueType().getRawClass());
    }

    @Test
    public void testMissingNodeAndAttribute() {
        assertNotNull(objectReader.getAttribute("test"));
        assertNotNull(objectReader.withAttribute("key", "val"));
        assertNotNull(objectReader.withoutAttribute("key"));
    }
}