package com.fasterxml.jackson.databind.cfg;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class BaseSettingsTest {

    // Dummy implementations/mocks for testing BaseSettings mutators and getters

    private static final AnnotationIntrospector MOCK_INTROSPECTOR = new AnnotationIntrospector() {
        private static final long serialVersionUID = 1L;
    };

    private static final PropertyNamingStrategy MOCK_PROPERTY_NAMING_STRATEGY = new PropertyNamingStrategy() {
        private static final long serialVersionUID = 1L;
    };

    private static final TypeResolverBuilder<?> MOCK_TYPE_RESOLVER = new TypeResolverBuilder<TypeResolverBuilder<?>>() {
        @Override public Class<?> getDefaultImpl() { return null; }
        @Override public TypeResolverBuilder<TypeResolverBuilder<?>> inclusion(JsonTypeInfo.As include) { return this; }
        @Override public TypeResolverBuilder<TypeResolverBuilder<?>> typeProperty(String propName) { return this; }
        @Override public TypeResolverBuilder<TypeResolverBuilder<?>> defaultImpl(Class<?> defaultImpl) { return this; }
        @Override public TypeResolverBuilder<TypeResolverBuilder<?>> typeIdVisibility(boolean isVisible) { return this; }
        @Override public TypeResolverBuilder<TypeResolverBuilder<?>> init(JsonTypeInfo.Id idType, com.fasterxml.jackson.databind.jsontype.TypeIdResolver idRes) { return this; }
        @Override public com.fasterxml.jackson.databind.jsontype.TypeDeserializer buildTypeDeserializer(com.fasterxml.jackson.databind.DeserializationConfig config, com.fasterxml.jackson.databind.JavaType baseType, java.util.Collection<com.fasterxml.jackson.databind.jsontype.NamedType> subtypes) { return null; }
        @Override public com.fasterxml.jackson.databind.jsontype.TypeSerializer buildTypeSerializer(com.fasterxml.jackson.databind.SerializationConfig config, com.fasterxml.jackson.databind.JavaType baseType, java.util.Collection<com.fasterxml.jackson.databind.jsontype.NamedType> subtypes) { return null; }
    };

    private static final VisibilityChecker<?> MOCK_VISIBILITY_CHECKER = VisibilityChecker.Std.defaultInstance();
    private static final DateFormat MOCK_DATE_FORMAT = DateFormat.getDateInstance();
    private static final Locale MOCK_LOCALE = Locale.GERMAN;
    private static final TimeZone MOCK_TIMEZONE = TimeZone.getTimeZone("GMT");
    private static final TypeFactory MOCK_TYPE_FACTORY = TypeFactory.defaultInstance();

    @Test
    public void testConstructorsAndGetters() {
        BaseSettings settings = new BaseSettings(
                MOCK_INTROSPECTOR,
                MOCK_VISIBILITY_CHECKER,
                MOCK_PROPERTY_NAMING_STRATEGY,
                MOCK_TYPE_FACTORY,
                MOCK_TYPE_RESOLVER,
                MOCK_DATE_FORMAT,
                MOCK_HANDLER_INSTANTIATOR(),
                MOCK_LOCALE,
                MOCK_TIMEZONE
        );

        assertSame(MOCK_INTROSPECTOR, settings.getAnnotationIntrospector());
        assertSame(MOCK_VISIBILITY_CHECKER, settings.getVisibilityChecker());
        assertSame(MOCK_PROPERTY_NAMING_STRATEGY, settings.getPropertyNamingStrategy());
        assertSame(MOCK_TYPE_FACTORY, settings.getTypeFactory());
        assertSame(MOCK_TYPE_RESOLVER, settings.getTypeResolverBuilder());
        assertSame(MOCK_DATE_FORMAT, settings.getDateFormat());
        assertNotNull(settings.getHandlerInstantiator());
        assertSame(MOCK_LOCALE, settings.getLocale());
        assertSame(MOCK_TIMEZONE, settings.getTimeZone());
    }

    @Test
    public void testWithAnnotationIntrospector() {
        BaseSettings settings = createDefaultSettings();
        AnnotationIntrospector newIntrospector = new AnnotationIntrospector() {
            private static final long serialVersionUID = 1L;
        };
        BaseSettings modified = settings.withAnnotationIntrospector(newIntrospector);
        assertNotSame(settings, modified);
        assertSame(newIntrospector, modified.getAnnotationIntrospector());

        // Test with null
        BaseSettings nullModified = settings.withAnnotationIntrospector(null);
        assertNull(nullModified.getAnnotationIntrospector());
    }

    @Test
    public void testWithVisibilityChecker() {
        BaseSettings settings = createDefaultSettings();
        VisibilityChecker<?> newChecker = VisibilityChecker.Std.defaultInstance()
                .with(JsonAutoDetect.Visibility.ANY);
        BaseSettings modified = settings.withVisibilityChecker(newChecker);
        assertNotSame(settings, modified);
        assertSame(newChecker, modified.getVisibilityChecker());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithVisibilityCheckerNull() {
        BaseSettings settings = createDefaultSettings();
        settings.withVisibilityChecker(null);
    }

    @Test
    public void testWithVisibility() {
        BaseSettings settings = createDefaultSettings();
        BaseSettings modified = settings.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        assertNotSame(settings, modified);
        assertNotNull(modified.getVisibilityChecker());
    }

    @Test
    public void testWithPropertyNamingStrategy() {
        BaseSettings settings = createDefaultSettings();
        PropertyNamingStrategy newStrategy = new PropertyNamingStrategy() {
            private static final long serialVersionUID = 1L;
        };
        BaseSettings modified = settings.withPropertyNamingStrategy(newStrategy);
        assertNotSame(settings, modified);
        assertSame(newStrategy, modified.getPropertyNamingStrategy());

        BaseSettings nullModified = settings.withPropertyNamingStrategy(null);
        assertNull(nullModified.getPropertyNamingStrategy());
    }

    @Test
    public void testWithTypeFactory() {
        BaseSettings settings = createDefaultSettings();
        TypeFactory newFactory = TypeFactory.defaultInstance();
        BaseSettings modified = settings.withTypeFactory(newFactory);
        assertNotSame(settings, modified);
        assertSame(newFactory, modified.getTypeFactory());

        // Usually TypeFactory should not be null, but let's see how it behaves or if it throws
        try {
            settings.withTypeFactory(null);
        } catch (IllegalArgumentException e) {
            // Expected if null check is enforced
        }
    }

    @Test
    public void testWithTypeResolverBuilder() {
        BaseSettings settings = createDefaultSettings();
        BaseSettings modified = settings.withTypeResolverBuilder(MOCK_TYPE_RESOLVER);
        assertNotSame(settings, modified);
        assertSame(MOCK_TYPE_RESOLVER, modified.getTypeResolverBuilder());

        BaseSettings nullModified = settings.withTypeResolverBuilder(null);
        assertNull(nullModified.getTypeResolverBuilder());
    }

    @Test
    public void testWithDateFormat() {
        BaseSettings settings = createDefaultSettings();
        DateFormat newFormat = DateFormat.getTimeInstance();
        BaseSettings modified = settings.withDateFormat(newFormat);
        assertNotSame(settings, modified);
        assertSame(newFormat, modified.getDateFormat());

        BaseSettings nullModified = settings.withDateFormat(null);
        assertNull(nullModified.getDateFormat());
    }

    @Test
    public void testWithHandlerInstantiator() {
        BaseSettings settings = createDefaultSettings();
        HandlerInstantiator newInstantiator = new HandlerInstantiator() {
            @Override
            public com.fasterxml.jackson.databind.deser.ValueInstantiator valueInstantiator(
                    com.fasterxml.jackson.databind.DeserializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> valueInstantiatorClass) {
                return null;
            }
            @Override
            public com.fasterxml.jackson.databind.JsonDeserializer<?> deserializerInstance(
                    com.fasterxml.jackson.databind.DeserializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> deserClass) {
                return null;
            }
            @Override
            public com.fasterxml.jackson.databind.JsonSerializer<?> serializerInstance(
                    com.fasterxml.jackson.databind.SerializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> serClass) {
                return null;
            }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(
                    com.fasterxml.jackson.databind.SerializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> builderClass) {
                return null;
            }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(
                    com.fasterxml.jackson.databind.DeserializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> builderClass) {
                return null;
            }
            @Override
            public AnnotationIntrospector annotationIntrospectorInstance(
                    com.fasterxml.jackson.databind.cfg.MapperConfig<?> config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> introspectorClass) {
                return null;
            }
            @Override
            public Object namingStrategyInstance(
                    com.fasterxml.jackson.databind.cfg.MapperConfig<?> config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> namingStrategyClass) {
                return null;
            }
        };

        BaseSettings modified = settings.withHandlerInstantiator(newInstantiator);
        assertNotSame(settings, modified);
        assertSame(newInstantiator, modified.getHandlerInstantiator());

        BaseSettings nullModified = settings.withHandlerInstantiator(null);
        assertNull(nullModified.getHandlerInstantiator());
    }

    @Test
    public void testWithLocale() {
        BaseSettings settings = createDefaultSettings();
        Locale newLocale = Locale.US;
        BaseSettings modified = settings.withLocale(newLocale);
        assertNotSame(settings, modified);
        assertSame(newLocale, modified.getLocale());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithLocaleNull() {
        BaseSettings settings = createDefaultSettings();
        settings.withLocale(null);
    }

    @Test
    public void testWithTimeZone() {
        BaseSettings settings = createDefaultSettings();
        TimeZone newTz = TimeZone.getTimeZone("PST");
        BaseSettings modified = settings.withTimeZone(newTz);
        assertNotSame(settings, modified);
        assertSame(newTz, modified.getTimeZone());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithTimeZoneNull() {
        BaseSettings settings = createDefaultSettings();
        settings.withTimeZone(null);
    }

    // Helper methods

    private BaseSettings createDefaultSettings() {
        return new BaseSettings(
                MOCK_INTROSPECTOR,
                MOCK_VISIBILITY_CHECKER,
                MOCK_PROPERTY_NAMING_STRATEGY,
                MOCK_TYPE_FACTORY,
                MOCK_TYPE_RESOLVER,
                MOCK_DATE_FORMAT,
                MOCK_HANDLER_INSTANTIATOR(),
                MOCK_LOCALE,
                MOCK_TIMEZONE
        );
    }

    private HandlerInstantiator MOCK_HANDLER_INSTANTIATOR() {
        return new HandlerInstantiator() {
            @Override
            public com.fasterxml.jackson.databind.deser.ValueInstantiator valueInstantiator(
                    com.fasterxml.jackson.databind.DeserializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> valueInstantiatorClass) {
                return null;
            }
            @Override
            public com.fasterxml.jackson.databind.JsonDeserializer<?> deserializerInstance(
                    com.fasterxml.jackson.databind.DeserializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> deserClass) {
                return null;
            }
            @Override
            public com.fasterxml.jackson.databind.JsonSerializer<?> serializerInstance(
                    com.fasterxml.jackson.databind.SerializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> serClass) {
                return null;
            }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(
                    com.fasterxml.jackson.databind.SerializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> builderClass) {
                return null;
            }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(
                    com.fasterxml.jackson.databind.DeserializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> builderClass) {
                return null;
            }
            @Override
            public AnnotationIntrospector annotationIntrospectorInstance(
                    com.fasterxml.jackson.databind.cfg.MapperConfig<?> config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> introspectorClass) {
                return null;
            }
            @Override
            public Object namingStrategyInstance(
                    com.fasterxml.jackson.databind.cfg.MapperConfig<?> config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated, Class<?> namingStrategyClass) {
                return null;
            }
        };
    }
}