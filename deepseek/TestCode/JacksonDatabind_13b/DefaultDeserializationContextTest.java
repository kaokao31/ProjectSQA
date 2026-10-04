package com.fasterxml.jackson.databind.deser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class DefaultDeserializationContextTest {

    private DeserializationConfig config() {
        return new ObjectMapper().getDeserializationConfig();
    }

    private DefaultDeserializationContext.Impl newContext() {
        return new DefaultDeserializationContext.Impl(config(), new BasicDeserializerFactory());
    }

    private Field field(String name) throws NoSuchFieldException {
        Class<?> cls = DefaultDeserializationContext.class;
        while (cls != null) {
            try {
                Field f = cls.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private void setObjectIdResolvers(DefaultDeserializationContext ctx,
            List<ObjectIdResolver> resolvers) throws Exception {
        field("_objectIdResolvers").set(ctx, resolvers);
    }

    private Object getObjectIdResolvers(DefaultDeserializationContext ctx) throws Exception {
        return field("_objectIdResolvers").get(ctx);
    }

    @Test
    public void testCacheIsNotNull() {
        assertNotNull(newContext().getCache());
    }

    @Test
    public void testCheckUnresolvedObjectIdWithNoResolvers() throws Exception {
        newContext().checkUnresolvedObjectId();
    }

    @Test
    public void testCheckUnresolvedObjectIdWithEmptyResolvers() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        setObjectIdResolvers(ctx, new ArrayList<ObjectIdResolver>());
        ctx.checkUnresolvedObjectId();
    }

    @Test
    public void testCheckUnresolvedObjectIdWithResolvedResolver() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        SimpleObjectIdResolver resolver = new SimpleObjectIdResolver();
        ObjectIdGenerator.IdKey key = new ObjectIdGenerator.IdKey(
                ObjectIdGenerator.class, null, "id");
        resolver.bindItem(key, "value");
        setObjectIdResolvers(ctx, Collections.<ObjectIdResolver>singletonList(resolver));
        ctx.checkUnresolvedObjectId();
    }

    @Test(expected = UnresolvedForwardReference.class)
    public void testCheckUnresolvedObjectIdWithUnresolvedResolver() throws Exception {
        final ObjectIdGenerator.IdKey key = new ObjectIdGenerator.IdKey(
                ObjectIdGenerator.class, null, "id");

        ObjectIdResolver resolver = new ObjectIdResolver() {
            @Override
            public ObjectIdResolver newForDeserialization(Object context) {
                return this;
            }

            @Override
            public boolean canUseFor(ObjectIdResolver resolverType) {
                return false;
            }

            @Override
            public void bindItem(ObjectIdGenerator.IdKey idKey, Object ob) {
            }

            @Override
            public Object resolveId(ObjectIdGenerator.IdKey idKey) {
                return null;
            }

            @Override
            public List<ObjectIdGenerator.IdKey> resolveIds() {
                return Collections.singletonList(key);
            }
        };

        DefaultDeserializationContext ctx = newContext();
        setObjectIdResolvers(ctx, Collections.<ObjectIdResolver>singletonList(resolver));
        ctx.checkUnresolvedObjectId();
    }

    @Test
    public void testFindRootValueDeserializer() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = ctx.findRootValueDeserializer(type);
        assertNotNull(deser);
        assertSame(deser, ctx.findRootValueDeserializer(type));
    }

    @Test
    public void testFindValueDeserializer() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        assertNotNull(ctx.findValueDeserializer(type, null, null));
    }

    @Test
    public void testFindKeyDeserializer() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        assertNotNull(ctx.findKeyDeserializer(type, null));
    }

    @Test
    public void testDeserializerInstanceWithNullValue() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        assertNull(ctx.deserializerInstance(ctx.getConfig(), null, null));
    }

    @Test
    public void testKeyDeserializerInstanceWithNullValue() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        assertNull(ctx.keyDeserializerInstance(ctx.getConfig(), null, null));
    }

    @Test
    public void testCopy() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        DefaultDeserializationContext copy = ctx.copy();
        assertNotNull(copy);
        assertNotSame(ctx, copy);
        assertNotNull(copy.getCache());
    }

    @Test
    public void testCopyPreservesObjectIdResolvers() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        SimpleObjectIdResolver resolver = new SimpleObjectIdResolver();
        ObjectIdGenerator.IdKey key = new ObjectIdGenerator.IdKey(
                ObjectIdGenerator.class, null, "id");
        resolver.bindItem(key, "value");
        setObjectIdResolvers(ctx, Collections.<ObjectIdResolver>singletonList(resolver));

        DefaultDeserializationContext copy = ctx.copy();

        Object copied = getObjectIdResolvers(copy);
        assertNotNull("copy() must preserve registered ObjectIdResolvers", copied);
        assertTrue(copied instanceof List);
        assertEquals(1, ((List<?>) copied).size());
        assertSame(resolver, ((List<?>) copied).get(0));
    }

    @Test
    public void testCopyWithUnresolvedResolverThrows() throws Exception {
        final ObjectIdGenerator.IdKey key = new ObjectIdGenerator.IdKey(
                ObjectIdGenerator.class, null, "copy");

        ObjectIdResolver resolver = new ObjectIdResolver() {
            @Override
            public ObjectIdResolver newForDeserialization(Object context) {
                return this;
            }

            @Override
            public boolean canUseFor(ObjectIdResolver resolverType) {
                return false;
            }

            @Override
            public void bindItem(ObjectIdGenerator.IdKey idKey, Object ob) {
            }

            @Override
            public Object resolveId(ObjectIdGenerator.IdKey idKey) {
                return null;
            }

            @Override
            public List<ObjectIdGenerator.IdKey> resolveIds() {
                return Collections.singletonList(key);
            }
        };

        DefaultDeserializationContext ctx = newContext();
        setObjectIdResolvers(ctx, Collections.<ObjectIdResolver>singletonList(resolver));

        DefaultDeserializationContext copy = ctx.copy();
        assertNotNull(getObjectIdResolvers(copy));

        try {
            copy.checkUnresolvedObjectId();
            fail("copy should retain unresolved ObjectIdResolver state and throw");
        } catch (UnresolvedForwardReference e) {
            // expected
        }
    }

    @Test
    public void testCreateDummyInstance() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        DefaultDeserializationContext dummy = ctx.createDummyInstance();
        assertNotNull(dummy);
        assertNotNull(dummy.getCache());
        dummy.checkUnresolvedObjectId();
    }

    @Test
    public void testCreateInstance() throws Exception {
        DefaultDeserializationContext ctx = newContext();
        DefaultDeserializationContext created = ctx.createInstance(ctx.getConfig(), null, null);
        assertNotNull(created);
        assertNotNull(created.getCache());
        created.checkUnresolvedObjectId();
    }
}