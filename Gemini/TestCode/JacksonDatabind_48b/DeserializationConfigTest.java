package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DeserializationConfigTest {

    private DeserializationConfig config;

    @Before
    public void setUp() {
        BaseSettings base = new BaseSettings(
                (ClassIntrospector) null,
                (AnnotationIntrospector) null,
                (VisibilityChecker<?>) null,
                (PropertyNamingStrategy) null,
                (TypeFactory) null,
                (TypeResolverBuilder<?>) null,
                Locale.getDefault(),
                TimeZone.getDefault(),
                (Base64Variant) null
        );
        SubtypeResolver str = new SubtypeResolver() {
        };
        RootNameLookup rnl = new RootNameLookup();
        config = new DeserializationConfig(base, str, null, null, rnl);
    }

    @Test
    public void testWithDES() {
        DeserializationConfig newConfig = config.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertNotNull(newConfig);
        assertNotSame(config, newConfig);
        assertTrue(newConfig.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
    }

    @Test
    public void testWithoutDES() {
        DeserializationConfig enabledConfig = config.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        DeserializationConfig disabledConfig = enabledConfig.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertNotNull(disabledConfig);
        assertFalse(disabledConfig.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
    }

    @Test
    public void testWithFeatures() {
        DeserializationConfig newConfig = config.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertNotNull(newConfig);
        assertTrue(newConfig.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertTrue(newConfig.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testWithoutFeatures() {
        DeserializationConfig enabledConfig = config.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        DeserializationConfig newConfig = enabledConfig.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertNotNull(newConfig);
        assertFalse(newConfig.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(newConfig.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testWithRootName() {
        DeserializationConfig newConfig = config.withRootName("rootName");
        assertNotNull(newConfig);
        assertEquals("rootName", newConfig.getRootName());
    }

    @Test
    public void testWithRootNameProperty() {
        PropertyName propName = PropertyName.construct("propName");
        DeserializationConfig newConfig = config.withRootName(propName);
        assertNotNull(newConfig);
        assertEquals(propName, newConfig.getRootNameObject());
    }

    @Test
    public void testWithSubtypeResolver() {
        SubtypeResolver str = new SubtypeResolver() {
        };
        DeserializationConfig newConfig = config.with(str);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithView() {
        DeserializationConfig newConfig = config.withView(Object.class);
        assertNotNull(newConfig);
        assertEquals(Object.class, newConfig.getActiveView());
    }

    @Test
    public void testWithHandler() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {
        };
        DeserializationConfig newConfig = config.withHandler(handler);
        assertNotNull(newConfig);
        assertNotNull(newConfig.getProblemHandlers());
    }

    @Test
    public void testWithoutHandler() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {
        };
        DeserializationConfig withH = config.withHandler(handler);
        DeserializationConfig withoutH = withH.withoutHandler(handler);
        assertNotNull(withoutH);
    }

    @Test
    public void testWithClassIntrospector() {
        ClassIntrospector ci = new ClassIntrospector() {
            @Override
            public BeanDescription forClass(MapperConfig<?> config, JavaType type, MixInResolver r) {
                return null;
            }

            @Override
            public BeanDescription forCreation(DeserializationConfig config, JavaType type, MixInResolver r) {
                return null;
            }

            @Override
            public BeanDescription forDeserialization(DeserializationConfig config, JavaType type, MixInResolver r) {
                return null;
            }

            @Override
            public BeanDescription forDeserializationWithBuilder(DeserializationConfig config, JavaType type, MixInResolver r) {
                return null;
            }

            @Override
            public BeanDescription forSerialization(SerializationConfig config, JavaType type, MixInResolver r) {
                return null;
            }

            @Override
            public BeanDescription forDirectClassAnnotations(MapperConfig<?> config, JavaType type, MixInResolver r) {
                return null;
            }
        };
        DeserializationConfig newConfig = config.with(ci);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithAnnotationIntrospector() {
        AnnotationIntrospector ai = AnnotationIntrospector.nopInstance();
        DeserializationConfig newConfig = config.with(ai);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithAppendedAnnotationIntrospector() {
        AnnotationIntrospector ai = AnnotationIntrospector.nopInstance();
        DeserializationConfig newConfig = config.withAppendedAnnotationIntrospector(ai);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithInsertedAnnotationIntrospector() {
        AnnotationIntrospector ai = AnnotationIntrospector.nopInstance();
        DeserializationConfig newConfig = config.withInsertedAnnotationIntrospector(ai);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithVisibilityChecker() {
        VisibilityChecker<?> vc = VisibilityChecker.Std.defaultInstance();
        DeserializationConfig newConfig = config.with(vc);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithVisibility() {
        DeserializationConfig newConfig = config.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithPropertyNamingStrategy() {
        PropertyNamingStrategy pns = new PropertyNamingStrategy.PropertyNamingStrategyBase() {
            @Override
            public String translate(String propertyName) {
                return propertyName;
            }
        };
        DeserializationConfig newConfig = config.with(pns);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithTypeFactory() {
        TypeFactory tf = TypeFactory.defaultInstance();
        DeserializationConfig newConfig = config.with(tf);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithTypeResolverBuilder() {
        TypeResolverBuilder<?> trb = new DefaultTypeResolverBuilder(DefaultTyping.NON_FINAL);
        DeserializationConfig newConfig = config.with(trb);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithDefaultTyper() {
        TypeResolverBuilder<?> trb = new DefaultTypeResolverBuilder(DefaultTyping.NON_FINAL);
        DeserializationConfig newConfig = config.withDefaultTyper(trb);
        assertNotNull(newConfig);
    }

    @Test
    public void testWithTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        DeserializationConfig newConfig = config.with(tz);
        assertNotNull(newConfig);
        assertEquals(tz, newConfig.getTimeZone());
    }

    @Test
    public void testWithLocale() {
        Locale loc = Locale.CANADA;
        DeserializationConfig newConfig = config.with(loc);
        assertNotNull(newConfig);
        assertEquals(loc, newConfig.getLocale());
    }

    @Test
    public void testWithBase64Variant() {
        Base64Variant b64 = Base64Variants.MIME;
        DeserializationConfig newConfig = config.with(b64);
        assertNotNull(newConfig);
    }

    @Test
    public void testInitialization() {
        assertNotNull(config.getBase64Variant());
        assertNotNull(config.getClassIntrospector());
        assertNotNull(config.getAnnotationIntrospector());
        assertNotNull(config.getPropertyNamingStrategy());
        assertNotNull(config.getTypeFactory());
        assertNotNull(config.getSubtypeResolver());
    }

    @Test
    public void testIntrospectClassAnnotations() {
        JavaType type = SimpleType.construct(Object.class);
        BeanDescription bd = config.introspectClassAnnotations(type);
        assertNotNull(bd);
    }

    @Test
    public void testIntrospectDirectClassAnnotations() {
        JavaType type = SimpleType.construct(Object.class);
        BeanDescription bd = config.introspectDirectClassAnnotations(type);
        assertNotNull(bd);
    }

    @Test
    public void testNodeFactory() {
        assertNotNull(config.getNodeFactory());
        DeserializationConfig newConfig = config.with(JsonNodeFactory.instance);
        assertNotNull(newConfig);
    }
}