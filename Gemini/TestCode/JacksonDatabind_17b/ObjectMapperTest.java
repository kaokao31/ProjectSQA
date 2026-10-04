package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectMapperTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testDefaultConstructorAndBasicGetters() {
        assertNotNull(objectMapper);
        assertNotNull(objectMapper.getSerializationConfig());
        assertNotNull(objectMapper.getDeserializationConfig());
        assertNotNull(objectMapper.tokenStreamFactory());
        assertNotNull(objectMapper.getSubtypeResolver());
        assertNotNull(objectMapper.getTypeFactory());
        assertNotNull(objectMapper.getVisibilityChecker());
        assertNotNull(objectMapper.getPropertyNamingStrategy());
        assertNotNull(objectMapper.getInjectableValues());
        assertNotNull(objectMapper.getFactory());
    }

    @Test
    public void testSubtypeResolverConfiguration() {
        SubtypeResolver customResolver = new StdSubtypeResolver();
        ObjectMapper configured = objectMapper.setSubtypeResolver(customResolver);
        assertSame(objectMapper, configured);
        assertEquals(customResolver, objectMapper.getSubtypeResolver());
    }

    @Test
    public void testTypeFactoryConfiguration() {
        TypeFactory customFactory = TypeFactory.defaultInstance();
        ObjectMapper configured = objectMapper.setTypeFactory(customFactory);
        assertSame(objectMapper, configured);
        assertEquals(customFactory, objectMapper.getTypeFactory());
    }

    @Test
    public void testReadValueWithInvalidJsonReturnsNullOrThrows() {
        // Just exercising various readValue overloads with empty or invalid inputs to check robustness
        try {
            objectMapper.readValue("", Object.class);
            fail("Expected exception for empty JSON string");
        } catch (Exception e) {
            // Expected
        }

        try {
            objectMapper.readValue((String) null, Object.class);
            fail("Expected exception for null JSON string");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testWriteValueAsStringWithNull() {
        try {
            String json = objectMapper.writeValueAsString(null);
            // null usually serializes to "null"
            assertEquals("null", json);
        } catch (Exception e) {
            fail("Should have serialized null: " + e.getMessage());
        }
    }

    @Test
    public void testMapperWithBean() {
        class SimpleBean {
            public int x = 42;
            public String name = "test";
        }

        SimpleBean bean = new SimpleBean();
        try {
            String json = objectMapper.writeValueAsString(bean);
            assertNotNull(json);
            assertTrue(json.contains("42"));
            assertTrue(json.contains("test"));

            SimpleBean readBack = objectMapper.readValue(json, SimpleBean.class);
            assertNotNull(readBack);
            assertEquals(42, readBack.x);
            assertEquals("test", readBack.name);
        } catch (Exception e) {
            fail("Bean serialization/deserialization failed: " + e.getMessage());
        }
    }

    @Test
    public void testJsonIncludeDefaultMapping() {
        // Test targeting specific methods or behaviors relevant to Jackson Databind ObjectMapper
        assertNotNull(objectMapper.configOverride(Object.class));
    }
}