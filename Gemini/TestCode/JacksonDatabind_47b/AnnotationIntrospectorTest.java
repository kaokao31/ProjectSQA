package com.fasterxml.jackson.databind;

import com .fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class AnnotationIntrospectorTest {

    static class DummyIntrospector extends AnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public Version version() {
            return Version.unknownVersion();
        }
    }

    static class DummyBean {
        public String value;
    }

    @Test
    public void testNopIntrospectorConstants() {
        assertNotNull(NopAnnotationIntrospector.instance);
    }

    @Test
    public void testFindNullSerializer() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findNullSerializer(ac));
    }

    @Test
    public void testFindAutoDetectVisibility() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findAutoDetectVisibility(ac, null));
    }

    @Test
    public void testFindTypeResolverBuilder() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findTypeResolverBuilder(null, ac, null));
    }

    @Test
    public void testFindPropertyIgnorals() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findPropertyIgnorals(ac));
        assertNull(ai.findPropertiesToIgnore(ac, true));
    }

    @Test
    public void testIsIgnorableType() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.isIgnorableType(ac));
    }

    @Test
    public void testFindFilterId() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findFilterId(ac));
    }

    @Test
    public void testFindNamingStrategy() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findNamingStrategy(ac));
    }

    @Test
    public void testFindVisibility() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findAutoDetectVisibility(ac, null));
    }

    @Test
    public void testFindCreatorBinding() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findCreatorBinding(ac));
    }

    @Test
    public void testFindCreatorStyle() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findCreatorStyle(ac));
    }

    @Test
    public void testFindViews() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findViews(ac));
    }

    @Test
    public void testFindObjectIdInfo() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findObjectIdInfo(ac));
        assertNull(ai.findObjectReferenceInfo(ac, null));
    }

    @Test
    public void testFindSerializationName() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertNull(ai.findNameForSerialization(ac));
        assertNull(ai.findNameForDeserialization(ac));
    }

    @Test
    public void testHasIgnoreMarker() {
        AnnotationIntrospector ai = new DummyIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DummyBean.class, ai, null);
        assertFalse(ai.hasIgnoreMarker(ac));
    }

    @Test
    public void testFindEnumValues() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertNull(ai.findEnumValues(DummyBean.class, new Enum[0], new String[0]));
        assertNull(ai.findEnumName(null));
    }

    @Test
    public void testFindImplicitPropertyName() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertNull(ai.findImplicitPropertyName(null));
    }

    @Test
    public void testFindPropertyAccess() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertNull(ai.findPropertyAccess(null));
    }

    @Test
    public void testFindReferenceType() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertNull(ai.findReferenceType(null));
    }

    @Test
    public void testFindUnwrappingNameTransformer() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertNull(ai.findUnwrappingNameTransformer(null));
    }

    @Test
    public void testHasAsValue() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertFalse(ai.hasAsValue(null));
        assertFalse(ai.hasAnySetter(null));
        assertFalse(ai.hasAnyGetter(null));
        assertFalse(ai.hasCreatorAnnotation(null));
    }

    @Test
    public void testFindInjectableValueId() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertNull(ai.findInjectableValueId(null));
    }

    @Test
    public void testGetTypeResolverBuilder() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertNull(ai.findPropertyTypeResolver(null, null, null));
        assertNull(ai.findAsPropertyTypeResolver(null, null, null));
    }

    @Test
    public void testRefinementMethods() {
        AnnotationIntrospector ai = new DummyIntrospector();
        assertNull(ai.refineDeserializationType(null, null, null));
        assertNull(ai.refineSerializationType(null, null, null));
    }

    @Test
    public void testFindAndAddIgnored() {
        AnnotationIntrospector ai = new DummyIntrospector();
        Collection<String> result = ai.findbrowableIgnoredForSerialization(null);
        assertNull(result);
    }

    @Test
    public void testPair() {
        AnnotationIntrospector ai1 = new DummyIntrospector();
        AnnotationIntrospector ai2 = new DummyIntrospector();
        AnnotationIntrospector pair = AnnotationIntrospector.pair(ai1, ai2);
        assertNotNull(pair);
        
        Collection<AnnotationIntrospector> all = pair.allIntrospectors();
        assertEquals(2, all.size());
    }
}