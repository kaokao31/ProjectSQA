package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Collection;

public class StdSubtypeResolverTest {

    private StdSubtypeResolver resolver;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        resolver = new StdSubtypeResolver();
        typeFactory = TypeFactory.defaultInstance();
    }

    @Test
    public void testCollectAndResolveSubtypesWithAnnotatedBase() {
        JavaType baseType = typeFactory.constructType(AnnotatedBase.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypes(baseType, null, typeFactory, null);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        boolean hasSub1 = false, hasSub2 = false;
        for (NamedType nt : subtypes) {
            if (nt.getType() == Sub1.class) hasSub1 = true;
            if (nt.getType() == Sub2.class) hasSub2 = true;
        }
        assertTrue("Should contain Sub1", hasSub1);
        assertTrue("Should contain Sub2", hasSub2);
    }

    @Test
    public void testCollectAndResolveSubtypesInheritedFromSupertype() {
        JavaType intermediateType = typeFactory.constructType(Intermediate.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypes(intermediateType, null, typeFactory, null);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        boolean hasSub1 = false, hasSub2 = false;
        for (NamedType nt : subtypes) {
            if (nt.getType() == Sub1.class) hasSub1 = true;
            if (nt.getType() == Sub2.class) hasSub2 = true;
        }
        assertTrue("Should contain Sub1", hasSub1);
        assertTrue("Should contain Sub2", hasSub2);
    }

    @Test
    public void testCollectAndResolveSubtypesDeepInheritance() {
        JavaType concreteType = typeFactory.constructType(Concrete.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypes(concreteType, null, typeFactory, null);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
    }

    @Test
    public void testCollectAndResolveSubtypesByTypeIdWithAnnotatedBase() {
        JavaType baseType = typeFactory.constructType(AnnotatedBase.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByTypeId(baseType, null, typeFactory, null);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        boolean hasSub1 = false, hasSub2 = false;
        for (NamedType nt : subtypes) {
            if (nt.getType() == Sub1.class) hasSub1 = true;
            if (nt.getType() == Sub2.class) hasSub2 = true;
        }
        assertTrue("Should contain Sub1", hasSub1);
        assertTrue("Should contain Sub2", hasSub2);
    }

    @Test
    public void testCollectAndResolveSubtypesByTypeIdInheritedFromSupertype() {
        JavaType intermediateType = typeFactory.constructType(Intermediate.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByTypeId(intermediateType, null, typeFactory, null);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
    }

    @Test
    public void testCollectAndResolveSubtypesByTypeIdDeepInheritance() {
        JavaType concreteType = typeFactory.constructType(Concrete.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByTypeId(concreteType, null, typeFactory, null);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
    }

    @Test
    public void testCollectAndResolveSubtypesWithNoSubtypes() {
        JavaType stringType = typeFactory.constructType(String.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypes(stringType, null, typeFactory, null);
        assertNotNull(subtypes);
        assertTrue(subtypes.isEmpty());
    }

    @Test
    public void testRegisterSubtypes() {
        resolver.registerSubtypes(new NamedType(RegisteredSub1.class, "reg1"),
                                  new NamedType(RegisteredSub2.class, "reg2"));
        JavaType baseType = typeFactory.constructType(RegisteredBase.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypes(baseType, null, typeFactory, null);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        boolean hasReg1 = false, hasReg2 = false;
        for (NamedType nt : subtypes) {
            if (nt.getType() == RegisteredSub1.class) hasReg1 = true;
            if (nt.getType() == RegisteredSub2.class) hasReg2 = true;
        }
        assertTrue("Should contain RegisteredSub1", hasReg1);
        assertTrue("Should contain RegisteredSub2", hasReg2);
    }

    // Test classes

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Sub1.class, name = "sub1"),
        @JsonSubTypes.Type(value = Sub2.class, name = "sub2")
    })
    static class AnnotatedBase { }

    static class Sub1 extends AnnotatedBase { }
    static class Sub2 extends AnnotatedBase { }

    static class Intermediate extends AnnotatedBase { }
    static class Concrete extends Intermediate { }

    static class RegisteredBase { }
    static class RegisteredSub1 extends RegisteredBase { }
    static class RegisteredSub2 extends RegisteredBase { }
}