package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class ObjectReaderTest {

    @Test
    public void testReadValuesWithByteArrayAndOffset() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);
        
        byte[] input = "\"testValue\"".getBytes("UTF-8");
        MappingIterator<String> it = reader.readValues(input, 0, input.length);
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("testValue", it.next());
        it.close();
    }

    @Test
    public void testAtPointer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);
        
        ObjectReader pointerReader = reader.at("/some/path");
        assertNotNull(pointerReader);
        assertNotSame(reader, pointerReader);

        ObjectReader pointerReaderStr = reader._at((String) null);
        assertNotNull(pointerReaderStr);
    }

    @Test
    public void testWithMethodsAndProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);

        assertNotNull(reader.with(Locale.US));
        assertNotNull(reader.with(TimeZone.getDefault()));
        assertNotNull(reader.with(Base64Variants.MIME));
        assertNotNull(reader.with(ContextAttributes.getEmpty()));
        assertNotNull(reader.with(JsonNodeFactory.instance));
        assertNotNull(reader.withHandler(new DeserializationProblemHandler() {}));
        assertNotNull(reader.withValueToUpdate("default"));
        assertNotNull(reader.withView(Object.class));
        assertNotNull(reader.withRootName("root"));
        assertNotNull(reader.withRootName((PropertyName) null));
    }

    @Test
    public void testWithoutMethods() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);

        assertNotNull(reader.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertNotNull(reader.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));
        assertNotNull(reader.without(JsonParser.Feature.ALLOW_COMMENTS));
        assertNotNull(reader.withoutFeatures(JsonParser.Feature.ALLOW_COMMENTS));
        assertNotNull(reader.withoutRootName());
    }

    @Test
    public void testWithFeatures() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);

        assertNotNull(reader.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertNotNull(reader.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));
        assertNotNull(reader.with(JsonParser.Feature.ALLOW_COMMENTS));
        assertNotNull(reader.withFeatures(JsonParser.Feature.ALLOW_COMMENTS));
        assertNotNull(reader.with(FormatSchema.NOOP));
    }

    @Test
    public void testConstructorsAndFactories() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);

        assertNotNull(reader.forType(Integer.class));
        assertNotNull(reader.forType(new TypeReference<Integer>() {}));
        assertNotNull(reader.forType(mapper.constructType(Integer.class)));
        assertNotNull(reader.readTree("\"hello\""));
    }

    @Test
    public void testGetDataFormatReaders() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);
        DataFormatReaders dfReaders = reader.withFormatReaders(new ObjectReader[0]);
        assertNotNull(dfReaders);
    }

    @Test
    public void testReadValuesVariousSources() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);

        // Just test that methods exist and handle basic inputs or throw expected exceptions
        try {
            reader.readValues(new File("non-existent-file.json"));
        } catch (Exception e) {
            // expected
        }

        try {
            reader.readValues(new URL("http://localhost/non-existent"));
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testMissingNodeAndNullNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(String.class);
        
        TokenBuffer tb = new TokenBuffer(mapper, false);
        JsonParser p = tb.asParser();
        assertNotNull(reader.readTree(p));
        p.close();
        tb.close();
    }
}