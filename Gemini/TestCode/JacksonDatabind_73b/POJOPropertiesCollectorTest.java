package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class POJOPropertiesCollectorTest {

    static class DummyPOJO {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    static class DummyConfig extends MapperConfig<DummyConfig> {
        protected DummyConfig(BaseSettings base, SubtypeResolver str) {
            super(base, str);
        }

        @Override
        public boolean isEnabled(MapperFeature f) {
            return true;
        }

        @Override
        public boolean isAnnotationProcessingEnabled() {
            return false;
        }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() {
            return AnnotationIntrospector.nopInstance();
        }

        @Override
        public VisibilityChecker<?> getDefaultVisibilityChecker() {
            return VisibilityChecker.Std.defaultInstance();
        }

        @Override
        public JavaType constructType(Class<?> cls) {
            return TypeFactory.defaultInstance().constructType(cls);
        }

        @Override
        public BeanDescription introspectClassAnnotations(Class<?> cls) {
            return null;
        }

        @Override
        public BeanDescription introspectDirectClassAnnotations(Class<?> cls) {
            return null;
        }
    }

    @Test
    public void testPOJOPropertiesCollectorCreationAndBasicGetters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(DummyPOJO.class);
        
        AnnotatedClass ac = AnnotatedClass.construct(DummyPOJO.class, AnnotationIntrospector.nopInstance(), null);
        
        // Construct a base settings and config
        BaseSettings base = new BaseSettings(null, AnnotationIntrospector.nopInstance(), null, tf, null, null, null, null);
        SubtypeResolver str = new SimpleSubtypeResolver();
        DummyConfig config = new DummyConfig(base, str);

        POJOPropertiesCollector collector = new POJOPropertiesCollector(
                config, true, type, ac, SerializationConfig.Feature.WRAP_ROOT_VALUE
        );

        Assert.assertNotNull(collector);
        Assert.assertEquals(type, collector.getType());
        Assert.assertNotNull(collector.getClassDef());
        Assert.assertNotNull(collector.getConfig());
    }

    @Test
    public void testCollectAll() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(DummyPOJO.class);
        AnnotatedClass ac = AnnotatedClass.construct(DummyPOJO.class, AnnotationIntrospector.nopInstance(), null);
        
        BaseSettings base = new BaseSettings(null, AnnotationIntrospector.nopInstance(), null, tf, null, null, null, null);
        SubtypeResolver str = new SimpleSubtypeResolver();
        DummyConfig config = new DummyConfig(base, str);

        POJOPropertiesCollector collector = new POJOPropertiesCollector(
                config, true, type, ac, SerializationConfig.Feature.WRAP_ROOT_VALUE
        );

        List<BeanPropertyDefinition> props = collector.getProperties();
        Assert.assertNotNull(props);
    }
}