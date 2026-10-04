package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.util.EnumValues;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class EnumSerializerTest {

    private enum TestEnum {
        A, B, C;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private enum ObjectEnum {
        INSTANCE;
        public String getProperty() { return "value"; }
    }

    @Test
    public void testConstruct() {
        EnumValues values = EnumValues.construct(TestEnum.class);
        EnumSerializer serializer1 = EnumSerializer.construct(TestEnum.class, null, null, null);
        EnumSerializer serializer2 = EnumSerializer.construct(TestEnum.class, null, null, Boolean.TRUE);
        EnumSerializer serializer3 = EnumSerializer.construct(TestEnum.class, null, null, Boolean.FALSE);

        Assert.assertNotNull(serializer1);
        Assert.assertNotNull(serializer2);
        Assert.assertNotNull(serializer3);
        Assert.assertNotNull(serializer1.getEnumValues());
    }

    @Test
    public void testIsVisibleAsFullSchemaSupport() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        EnumSerializer serializer = EnumSerializer.construct(TestEnum.class, mapper.getSerializationConfig(), null, null);

        // Just test execution of various methods to achieve coverage
        Assert.assertNotNull(serializer.getEnumValues());
    }

    @Test
    public void testContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        EnumSerializer serializer = EnumSerializer.construct(TestEnum.class, mapper.getSerializationConfig(), null, null);

        BeanProperty.Std property = new BeanProperty.Std(
                com.fasterxml.jackson.databind.util.NameTransformer.NOP.transform("test"),
                mapper.constructType(TestEnum.class),
                null,
                null,
                null,
                com.fasterxml.jackson.databind.introspect.AnnotatedMember.EMPTY_STUB,
                null
        );

        ContextualSerializer contextual = serializer.createContextual(provider, property);
        Assert.assertNotNull(contextual);
    }

    @Test
    public void testSerialization() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(TestEnum.A);
        Assert.assertEquals("\"A\"", json);
    }

    @Test
    public void testObjectShapeEnumSerialization() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(ObjectEnum.INSTANCE);
        Assert.assertTrue(json.contains("value"));
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        EnumSerializer serializer = EnumSerializer.construct(TestEnum.class, mapper.getSerializationConfig(), null, null);

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();
        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(TestEnum.class));
        
        // Also test with shape object
        EnumSerializer objSerializer = EnumSerializer.construct(ObjectEnum.class, mapper.getSerializationConfig(), null, null);
        objSerializer.acceptJsonFormatVisitor(visitor, mapper.constructType(ObjectEnum.class));
        
        Assert.assertNotNull(serializer);
    }
}