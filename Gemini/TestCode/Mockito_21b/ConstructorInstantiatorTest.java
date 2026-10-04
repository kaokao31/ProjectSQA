package org.mockito.internal.creation.instance;

import org.junit.Test;
import org.mockito.internal.exceptions.util.Pluralizer;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collections;

import static org.junit.Assert.*;

public class ConstructorInstantiatorTest {

    static class SimpleClass {
        private final String value;

        public SimpleClass() {
            this.value = "default";
        }

        public SimpleClass(String value) {
            this.value = value;
        }

        public SimpleClass(String s, Integer i) {
            this.value = s + i;
        }

        public String getValue() {
            return value;
        }
    }

    static class PrivateConstructorClass {
        private PrivateConstructorClass() {}
    }

    static class ExceptionThrowingConstructorClass {
        public ExceptionThrowingConstructorClass() {
            throw new RuntimeException("Expected exception");
        }
    }

    static abstract class AbstractClass {
        public AbstractClass() {}
    }

    @Test
    public void should_instantiate_with_default_constructor() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        SimpleClass obj = instantiator.newInstance(SimpleClass.class);
        assertNotNull(obj);
        assertEquals("default", obj.getValue());
    }

    @Test
    public void should_instantiate_with_matching_constructor() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator("test-arg");
        SimpleClass obj = instantiator.newInstance(SimpleClass.class);
        assertNotNull(obj);
        assertEquals("test-arg", obj.getValue());
    }

    @Test
    public void should_instantiate_with_multiple_args() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator("arg", 123);
        SimpleClass obj = instantiator.newInstance(SimpleClass.class);
        assertNotNull(obj);
        assertEquals("arg123", obj.getValue());
    }

    @Test(expected = InstantationException.class)
    public void should_fail_when_no_constructor_matches() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(12345);
        instantiator.newInstance(SimpleClass.class);
    }

    @Test(expected = InstantationException.class)
    public void should_fail_when_constructor_is_private() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator();
        instantiator.newInstance(PrivateConstructorClass.class);
    }

    @Test(expected = InstantationException.class)
    public void should_fail_when_class_is_abstract() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator();
        instantiator.newInstance(AbstractClass.class);
    }

    @Test(expected = InstantationException.class)
    public void should_fail_when_constructor_throws_exception() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator();
        instantiator.newInstance(ExceptionThrowingConstructorClass.class);
    }

    @Test(expected = InstantationException.class)
    public void should_fail_when_class_does_not_exist_or_cannot_be_instantiated() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator();
        // Passing an interface which has no constructors
        instantiator.newInstance(Serializable.class);
    }
}