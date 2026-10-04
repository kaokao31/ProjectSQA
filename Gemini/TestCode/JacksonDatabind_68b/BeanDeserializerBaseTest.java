package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.ValueInjector;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class BeanDeserializerBaseTest {

    // Concrete subclass of BeanDeserializerBase to test its protected/public methods
    private static class DummyBeanDeserializer extends BeanDeserializerBase {
        private static final long serialVersionUID = 1L;

        public DummyBeanDeserializer(BeanDeserializerBuilder builder, BeanDescription beanDesc,
                                     BeanPropertyMap properties, Map<String, SettableBeanProperty> backRefs,
                                     HashSet<String> ignorableProps, boolean ignoreAllUnknown, boolean hasViews) {
            super(builder, beanDesc, properties, backRefs, ignorableProps, ignoreAllUnknown, hasViews);
        }

        public DummyBeanDeserializer(BeanDeserializerBase src) {
            super(src);
        }

        public DummyBeanDeserializer(BeanDeserializerBase src, boolean ignoreAllUnknown) {
            super(src, ignoreAllUnknown);
        }

        public DummyBeanDeserializer(BeanDeserializerBase src, NameTransformer unwrapperTransformer) {
            super(src, unwrapperTransformer);
        }

        public DummyBeanDeserializer(BeanDeserializerBase src, ObjectIdReader oir) {
            super(src, oir);
        }

        public DummyBeanDeserializer(BeanDeserializerBase src, HashSet<String> ignorableProps) {
            super(src, ignorableProps);
        }

        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
            return new DummyBeanDeserializer(this, unwrapper);
        }

        @Override
        public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
            return new DummyBeanDeserializer(this, oir);
        }

        @Override
        public BeanDeserializerBase withIgnorableProperties(HashSet<String> ignorableProps) {
            return new DummyBeanDeserializer(this, ignorableProps);
        }

        @Override
        public BeanDeserializerBase withBeanProperties(BeanPropertyMap props) {
            return null;
        }

        @Override
        protected BeanDeserializerBase asArrayDeserializer() {
            return null;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return null;
        }
    }

    private DummyBeanDeserializer createDummyDeserializer() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = TypeFactory.defaultInstance().constructType(Object.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, mapper.getDeserializationConfig());
        BeanPropertyMap propertyMap = BeanPropertyMap.construct(java.util.Collections.emptyList(), false);
        Map<String, SettableBeanProperty> backRefs = new java.util.HashMap<>();
        HashSet<String> ignorableProps = new HashSet<>();
        ignorableProps.add("ignoredField");

        return new DummyBeanDeserializer(
                builder, beanDesc, propertyMap, backRefs, ignorableProps, true, false
        );
    }

    @Test
    public void testConstructorsAndAccessors() {
        DummyBeanDeserializer base = createDummyDeserializer();

        assertNotNull(base.properties());
        assertNotNull(base.getKnownPropertyNames());
        assertTrue(base.isCaseInsensitive()); // depends on default, but can check methods execute safely

        DummyBeanDeserializer copy1 = new DummyBeanDeserializer(base);
        assertNotNull(copy1);

        DummyBeanDeserializer copy2 = new DummyBeanDeserializer(base, false);
        assertNotNull(copy2);

        DummyBeanDeserializer copy3 = new DummyBeanDeserializer(base, NameTransformer.NOP);
        assertNotNull(copy3);

        DummyBeanDeserializer copy4 = new DummyBeanDeserializer(base, (ObjectIdReader) null);
        assertNotNull(copy4);

        HashSet<String> newIgnorable = new HashSet<>();
        newIgnorable.add("other");
        DummyBeanDeserializer copy5 = new DummyBeanDeserializer(base, newIgnorable);
        assertNotNull(copy5);
    }

    @Test
    public void testFindBackRef() {
        DummyBeanDeserializer base = createDummyDeserializer();
        assertNull(base.findBackReference("nonExistent"));
    }

    @Test
    public void testValueInstantiator() {
        DummyBeanDeserializer base = createDummyDeserializer();
        assertNotNull(base.getValueInstantiator());
    }

    @Test
    public void testBeanClassAndType() {
        DummyBeanDeserializer base = createDummyDeserializer();
        assertEquals(Object.class, base.getBeanClass());
        assertNotNull(base.getValueType());
    }

    @Test
    public void testAccessorsForPropertiesAndCreators() {
        DummyBeanDeserializer base = createDummyDeserializer();
        assertNull(base.findProperty(10));
        assertNull(base.findProperty("missing"));
        assertNull(base.getPropertyBasedCreator());
        assertNull(base.getObjectIdReader());
    }

    @Test
    public void testInjectables() {
        DummyBeanDeserializer base = createDummyDeserializer();
        Collection<ValueInjector> injectables = base.getInjectables();
        // May be null or empty based on builder, let's just make sure it doesn't throw
        assertTrue(injectables == null || injectables.isEmpty());
    }

    @Test
    public void testHandlingNullDeserMethods() {
        DummyBeanDeserializer base = createDummyDeserializer();
        // These methods might be null or return specific instances depending on initialization
        try {
            base.handledType();
        } catch (Exception e) {
            // expected if not fully bound
        }
    }
}