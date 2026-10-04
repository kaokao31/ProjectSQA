package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for {@link ObjectReader}.
 * Designed to achieve high code coverage and detect potential faults,
 * including those related to Defects4J bug #14 (JsonNode handling).
 */
public class ObjectReaderTest {

    // ----- Helper types -----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Person {
        @JsonProperty("name")
        public String name;
        @JsonProperty("age")
        public int age;

        public Person() { }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    public static class Wrapper<T> {
        @JsonProperty("value")
        public T value;

        public Wrapper() { }

        public Wrapper(T value) { this.value = value; }
    }

    // ----- Test infrastructure -----

    private ObjectMapper mapper;
    private ObjectReader reader;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        reader = mapper.reader();
    }

    // ==================== Basic readValue tests ====================

    @Test
    public void testReadValueStringToPerson() throws Exception {
        String json = "{\"name\":\"John\",\"age\":30}";
        Person p = reader.readValue(json, Person.class);
        assertEquals("John", p.name);
        assertEquals(30, p.age);
    }

    @Test
    public void testReadValueBytesToPerson() throws Exception {
        byte[] json = "{\"name\":\"Jane\",\"age\":25}".getBytes("UTF-8");
        Person p = reader.readValue(json, Person.class);
        assertEquals("Jane", p.name);
        assertEquals(25, p.age);
    }

    @Test
    public void testReadValueBytesOffsetLengthToPerson() throws Exception {
        byte[] json = "  {\"name\":\"Bob\",\"age\":40}  ".getBytes("UTF-8");
        Person p = reader.readValue(json, 2, json.length - 4, Person.class);
        assertEquals("Bob", p.name);
        assertEquals(40, p.age);
    }

    @Test
    public void testReadValueInputStreamToPerson() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":35}";
        InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"));
        Person p = reader.readValue(is, Person.class);
        assertEquals("Alice", p.name);
        assertEquals(35, p.age);
    }

    @Test
    public void testReadValueReaderToPerson() throws Exception {
        String json = "{\"name\":\"Charlie\",\"age\":28}";
        Reader r = new StringReader(json);
        Person p = reader.readValue(r, Person.class);
        assertEquals("Charlie", p.name);
        assertEquals(28, p.age);
    }

    @Test
    public void testReadValueFileToPerson() throws Exception {
        File tmp = File.createTempFile("test", ".json");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write("{\"name\":\"FileTest\",\"age\":99}");
        }
        Person p = reader.readValue(tmp, Person.class);
        assertEquals("FileTest", p.name);
        assertEquals(99, p.age);
    }

    @Test
    public void testReadValueURLToPerson() throws Exception {
        File tmp = File.createTempFile("test", ".json");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write("{\"name\":\"URLTest\",\"age\":77}");
        }
        URL url = tmp.toURI().toURL();
        Person p = reader.readValue(url, Person.class);
        assertEquals("URLTest", p.name);
        assertEquals(77, p.age);
    }

    @Test
    public void testReadValueStringSubstringToPerson() throws Exception {
        String json = "prefix{\"name\":\"Sub\",\"age\":1}suffix";
        Person p = reader.readValue(json, 6, json.length() - 12, Person.class);
        assertEquals("Sub", p.name);
        assertEquals(1, p.age);
    }

    // ==================== JsonNode tests (targeting bug #14) ====================

    @Test
    public void testReadValueStringToJsonNodeObject() throws Exception {
        String json = "{\"key\":\"value\"}";
        JsonNode node = reader.readValue(json, JsonNode.class);
        assertTrue(node instanceof ObjectNode);
        assertEquals("value", node.get("key").asText());
    }

    @Test
    public void testReadValueStringToJsonNodeArray() throws Exception {
        String json = "[1,2,3]";
        JsonNode node = reader.readValue(json, JsonNode.class);
        assertTrue(node instanceof ArrayNode);
        assertEquals(3, node.size());
        assertEquals(1, node.get(0).asInt());
    }

    @Test
    public void testReadValueStringToJsonNodeString() throws Exception {
        String json = "\"hello\"";
        JsonNode node = reader.readValue(json, JsonNode.class);
        assertTrue(node.isTextual());
        assertEquals("hello", node.asText());
    }

    @Test
    public void testReadValueStringToJsonNodeNumber() throws Exception {
        String json = "42";
        JsonNode node = reader.readValue(json, JsonNode.class);
        assertTrue(node.isNumber());
        assertEquals(42, node.asInt());
    }

    @Test
    public void testReadValueBytesToJsonNode() throws Exception {
        byte[] json = "{\"a\":1}".getBytes("UTF-8");
        JsonNode node = reader.readValue(json, JsonNode.class);
        assertTrue(node instanceof ObjectNode);
        assertEquals(1, node.get("a").asInt());
    }

    @Test
    public void testReadValueInputStreamToJsonNode() throws Exception {
        String json = "[null, true, false]";
        InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"));
        JsonNode node = reader.readValue(is, JsonNode.class);
        assertTrue(node instanceof ArrayNode);
        assertTrue(node.get(1).asBoolean());
        assertFalse(node.get(2).asBoolean());
    }

    // ==================== TypeReference and JavaType tests ====================

    @Test
    public void testReadValueWithTypeReferenceList() throws Exception {
        String json = "[{\"name\":\"A\",\"age\":1},{\"name\":\"B\",\"age\":2}]";
        List<Person> list = reader.readValue(json, new TypeReference<List<Person>>() {});
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).name);
        assertEquals(2, list.get(1).age);
    }

    @Test
    public void testReadValueWithTypeReferenceMap() throws Exception {
        String json = "{\"x\":{\"name\":\"X\",\"age\":10}}";
        Map<String, Person> map = reader.readValue(json, new TypeReference<Map<String, Person>>() {});
        assertEquals("X", map.get("x").name);
    }

    @Test
    public void testReadValueWithJavaType() throws Exception {
        JavaType jt = TypeFactory.defaultInstance().constructType(Person.class);
        String json = "{\"name\":\"JavaType\",\"age\":50}";
        Person p = reader.readValue(json, jt);
        assertEquals("JavaType", p.name);
        assertEquals(50, p.age);
    }

    @Test
    public void testReadValueWithJavaTypeForJsonNode() throws Exception {
        JavaType jt = TypeFactory.defaultInstance().constructType(JsonNode.class);
        String json = "{\"x\":1}";
        JsonNode node = reader.readValue(json, jt);
        assertTrue(node instanceof ObjectNode);
    }

    // ==================== readValues (streaming) tests ====================

    @Test
    public void testReadValuesStringToPerson() throws Exception {
        String json = "{\"name\":\"A\",\"age\":1}\n{\"name\":\"B\",\"age\":2}";
        Iterator<Person> it = reader.readValues(json, Person.class);
        List<Person> list = new ArrayList<>();
        it.forEachRemaining(list::add);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).name);
    }

    @Test
    public void testReadValuesBytesToPerson() throws Exception {
        byte[] json = "{\"name\":\"X\",\"age\":10}{\"name\":\"Y\",\"age\":20}".getBytes("UTF-8");
        Iterator<Person> it = reader.readValues(json, Person.class);
        assertTrue(it.hasNext());
        Person p1 = it.next();
        assertEquals("X", p1.name);
        assertTrue(it.hasNext());
        Person p2 = it.next();
        assertEquals("Y", p2.name);
        assertFalse(it.hasNext());
    }

    @Test
    public void testReadValuesInputStreamToPerson() throws Exception {
        String json = "{\"name\":\"I1\",\"age\":1}\n{\"name\":\"I2\",\"age\":2}";
        InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"));
        Iterator<Person> it = reader.readValues(is, Person.class);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testReadValuesReaderToPerson() throws Exception {
        String json = "{\"name\":\"R1\",\"age\":11}{\"name\":\"R2\",\"age\":22}";
        Reader r = new StringReader(json);
        Iterator<Person> it = reader.readValues(r, Person.class);
        List<Person> list = new ArrayList<>();
        it.forEachRemaining(list::add);
        assertEquals(2, list.size());
    }

    @Test
    public void testReadValuesFileToPerson() throws Exception {
        File tmp = File.createTempFile("test", ".json");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write("{\"name\":\"F1\",\"age\":1}{\"name\":\"F2\",\"age\":2}");
        }
        Iterator<Person> it = reader.readValues(tmp, Person.class);
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(2, count);
    }

    @Test
    public void testReadValuesURLToPerson() throws Exception {
        File tmp = File.createTempFile("test", ".json");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write("{\"name\":\"U1\",\"age\":10}{\"name\":\"U2\",\"age\":20}");
        }
        URL url = tmp.toURI().toURL();
        Iterator<Person> it = reader.readValues(url, Person.class);
        assertTrue(it.hasNext());
        assertEquals("U1", it.next().name);
        assertTrue(it.hasNext());
        assertEquals("U2", it.next().name);
        assertFalse(it.hasNext());
    }

    @Test
    public void testReadValuesStringToJsonNode() throws Exception {
        String json = "{\"a\":1}{\"b\":2}";
        Iterator<JsonNode> it = reader.readValues(json, JsonNode.class);
        assertTrue(it.hasNext());
        JsonNode n1 = it.next();
        assertTrue(n1 instanceof ObjectNode);
        assertEquals(1, n1.get("a").asInt());
        assertTrue(it.hasNext());
        JsonNode n2 = it.next();
        assertEquals(2, n2.get("b").asInt());
        assertFalse(it.hasNext());
    }

    @Test
    public void testReadValuesEmptyInput() throws Exception {
        Iterator<Person> it = reader.readValues("", Person.class);
        assertFalse(it.hasNext());
    }

    // ==================== forType tests ====================

    @Test
    public void testForTypePerson() throws Exception {
        ObjectReader typedReader = reader.forType(Person.class);
        assertNotNull(typedReader);
        Person p = typedReader.readValue("{\"name\":\"ForType\",\"age\":33}");
        assertEquals("ForType", p.name);
        assertEquals(33, p.age);
    }

    @Test
    public void testForTypeJsonNode() throws Exception {
        ObjectReader typedReader = reader.forType(JsonNode.class);
        JsonNode node = typedReader.readValue("[1,2,3]");
        assertTrue(node instanceof ArrayNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTypeNull() throws Exception {
        reader.forType((Class<?>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTypeNullJavaType() throws Exception {
        reader.forType((JavaType) null);
    }

    // ==================== Configuration tests ====================

    @Test
    public void testWithConfigIgnoreUnknown() throws Exception {
        // By default, unknown properties cause failure
        ObjectReader configured = reader.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = "{\"name\":\"Config\",\"unknown\":\"x\"}";
        Person p = configured.readValue(json, Person.class);
        assertEquals("Config", p.name);
    }

    @Test(expected = JsonProcessingException.class)
    public void testWithConfigFailOnNullForPrimitives() throws Exception {
        ObjectReader configured = reader.with(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        String json = "{\"name\":\"NullAge\",\"age\":null}";
        configured.readValue(json, Person.class);
    }

    @Test
    public void testWithConfigAcceptSingleValueAsArray() throws Exception {
        ObjectReader configured = reader.with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        String json = "{\"name\":\"Single\",\"age\":5}";
        List<Person> list = configured.readValue(json, new TypeReference<List<Person>>() {});
        assertEquals(1, list.size());
        assertEquals("Single", list.get(0).name);
    }

    @Test
    public void testWithConfigUnwrapSingleValueArrays() throws Exception {
        ObjectReader configured = reader.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
        String json = "[{\"name\":\"Unwrapped\",\"age\":7}]";
        Person p = configured.readValue(json, Person.class);
        assertEquals("Unwrapped", p.name);
    }

    @Test
    public void testWithReturnsNewInstance() throws Exception {
        ObjectReader configured = reader.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertNotSame(reader, configured);
        // Original reader should still fail on unknown properties
        String json = "{\"name\":\"Orig\",\"unknown\":\"x\"}";
        try {
            reader.readValue(json, Person.class);
            fail("Original reader should have thrown exception");
        } catch (JsonProcessingException e) {
            // expected
        }
    }

    // ==================== Error handling tests ====================

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullString() throws Exception {
        reader.readValue((String) null, Person.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullBytes() throws Exception {
        reader.readValue((byte[]) null, Person.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullInputStream() throws Exception {
        reader.readValue((InputStream) null, Person.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullReader() throws Exception {
        reader.readValue((Reader) null, Person.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullFile() throws Exception {
        reader.readValue((File) null, Person.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullURL() throws Exception {
        reader.readValue((URL) null, Person.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullClass() throws Exception {
        reader.readValue("{}", (Class<?>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullTypeReference() throws Exception {
        reader.readValue("{}", (TypeReference<?>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValueNullJavaType() throws Exception {
        reader.readValue("{}", (JavaType) null);
    }

    @Test(expected = JsonProcessingException.class)
    public void testReadValueEmptyString() throws Exception {
        reader.readValue("", Person.class);
    }

    @Test(expected = JsonProcessingException.class)
    public void testReadValueMalformedJson() throws Exception {
        reader.readValue("{invalid}", Person.class);
    }

    @Test(expected = JsonProcessingException.class)
    public void testReadValueTypeMismatch() throws Exception {
        // Expecting Person but given an array
        reader.readValue("[1,2,3]", Person.class);
    }

    @Test(expected = JsonProcessingException.class)
    public void testReadValueTruncatedJson() throws Exception {
        reader.readValue("{\"name\":\"Truncated\"", Person.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValuesNullString() throws Exception {
        reader.readValues((String) null, Person.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadValuesNullClass() throws Exception {
        reader.readValues("{}", (Class<?>) null);
    }

    // ==================== Reusability tests ====================

    @Test
    public void testReaderReusableAfterSuccessfulRead() throws Exception {
        Person p1 = reader.readValue("{\"name\":\"First\",\"age\":1}", Person.class);
        assertEquals("First", p1.name);
        Person p2 = reader.readValue("{\"name\":\"Second\",\"age\":2}", Person.class);
        assertEquals("Second", p2.name);
    }

    @Test
    public void testReaderReusableAfterFailedRead() throws Exception {
        try {
            reader.readValue("invalid", Person.class);
            fail("Should have thrown");
        } catch (JsonProcessingException e) {
            // expected
        }
        // Now a valid read should still work
        Person p = reader.readValue("{\"name\":\"Recover\",\"age\":0}", Person.class);
        assertEquals("Recover", p.name);
    }

    // ==================== Edge cases ====================

    @Test
    public void testReadValueNullFieldInJson() throws Exception {
        String json = "{\"name\":null,\"age\":10}";
        Person p = reader.readValue(json, Person.class);
        assertNull(p.name);
        assertEquals(10, p.age);
    }

    @Test
    public void testReadValueEmptyObject() throws Exception {
        Person p = reader.readValue("{}", Person.class);
        assertNull(p.name);
        assertEquals(0, p.age);
    }

    @Test
    public void testReadValueEmptyArray() throws Exception {
        List<Person> list = reader.readValue("[]", new TypeReference<List<Person>>() {});
        assertTrue(list.isEmpty());
    }

    @Test
    public void testReadValueLargeNumbers() throws Exception {
        String json = "{\"name\":\"Big\",\"age\":2147483647}";
        Person p = reader.readValue(json, Person.class);
        assertEquals(2147483647, p.age);
    }

    @Test
    public void testReadValueSpecialCharacters() throws Exception {
        String json = "{\"name\":\"\\u00e9\\n\\t\",\"age\":0}";
        Person p = reader.readValue(json, Person.class);
        assertEquals("é\n\t", p.name);
    }

    @Test
    public void testReadValueDeepNested() throws Exception {
        String json = "{\"value\":{\"name\":\"Nested\",\"age\":3}}";
        Wrapper<Person> w = reader.readValue(json, new TypeReference<Wrapper<Person>>() {});
        assertEquals("Nested", w.value.name);
        assertEquals(3, w.value.age);
    }

    // ==================== Additional readValues edge cases ====================

    @Test
    public void testReadValuesWithWhitespace() throws Exception {
        String json = "  {\"a\":1}  {\"b\":2}  ";
        Iterator<JsonNode> it = reader.readValues(json, JsonNode.class);
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(2, count);
    }

    @Test(expected = JsonProcessingException.class)
    public void testReadValuesMalformedInMiddle() throws Exception {
        String json = "{\"a\":1} broken {\"b\":2}";
        Iterator<JsonNode> it = reader.readValues(json, JsonNode.class);
        it.next(); // first valid
        it.next(); // should throw
    }

    @Test
    public void testReadValuesWithFileAndURL() throws Exception {
        File tmp = File.createTempFile("test", ".json");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write("1 2 3");
        }
        // read from File
        Iterator<Integer> itFile = reader.readValues(tmp, Integer.class);
        List<Integer> listFile = new ArrayList<>();
        itFile.forEachRemaining(listFile::add);
        assertEquals(Arrays.asList(1,2,3), listFile);

        // read from URL
        Iterator<Integer> itURL = reader.readValues(tmp.toURI().toURL(), Integer.class);
        List<Integer> listURL = new ArrayList<>();
        itURL.forEachRemaining(listURL::add);
        assertEquals(Arrays.asList(1,2,3), listURL);
    }

    // ==================== Thread safety (basic) ====================

    @Test(timeout = 5000)
    public void testReaderThreadSafety() throws Exception {
        // Simple concurrent reads from same reader
        final int THREADS = 4;
        final int ITERATIONS = 100;
        List<Thread> threads = new ArrayList<>();
        final boolean[] failed = {false};
        for (int i = 0; i < THREADS; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < ITERATIONS; j++) {
                    try {
                        Person p = reader.readValue("{\"name\":\"T\",\"age\":" + j + "}", Person.class);
                        if (p.age != j) {
                            failed[0] = true;
                        }
                    } catch (Exception e) {
                        failed[0] = true;
                    }
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        assertFalse("Thread safety test failed", failed[0]);
    }
}