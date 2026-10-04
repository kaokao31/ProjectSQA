package org.mockito.internal.configuration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.mockito.internal.configuration.SpyAnnotationEngine;
import org.mockito.internal.util.MockUtil;
import org.mockito.plugins.MockMaker;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for SpyAnnotationEngine, targeting high coverage and fault detection.
 * Covers normal spy creation, null fields, uninitialized fields, and edge cases.
 */
public class SpyAnnotationEngineTest {

    private SpyAnnotationEngine engine;

    @Before
    public void setUp() {
        engine = new SpyAnnotationEngine();
    }

    // --- Helper classes with @Spy fields ---

    static class WithInitializedSpy {
        @Spy
        private List<String> list = new LinkedList<>();
    }

    static class WithNullSpy {
        @Spy
        private List<String> list = null;
    }

    static class WithUninitializedSpy {
        @Spy
        private List<String> list;
    }

    static class WithMultipleSpies {
        @Spy
        private List<String> list1 = new LinkedList<>();
        @Spy
        private List<String> list2 = new LinkedList<>();
    }

    static class WithNonSpyField {
        private String name = "test";
    }

    static class WithFinalSpy {
        @Spy
        private final List<String> list = new LinkedList<>();
    }

    static class WithAbstractSpy {
        @Spy
        private abstractClass obj = null;
    }

    abstract static class abstractClass {
        public abstract void doSomething();
    }

    // --- Tests ---

    @Test
    public void shouldCreateSpyForInitializedField() throws Exception {
        WithInitializedSpy instance = new WithInitializedSpy();
        engine.process(instance.getClass(), instance);
        Field field = WithInitializedSpy.class.getDeclaredField("list");
        field.setAccessible(true);
        Object spy = field.get(instance);
        assertNotNull("Spy should not be null", spy);
        assertTrue("Field should be a Mockito spy", MockUtil.isMock(spy));
    }

    @Test
    public void shouldHandleNullFieldGracefully() throws Exception {
        // Bug 32: previously threw NPE when field was null
        WithNullSpy instance = new WithNullSpy();
        try {
            engine.process(instance.getClass(), instance);
            // After fix, should not throw; field remains null or becomes spy?
            // Depending on implementation, it might create a spy from default constructor.
            // We just verify no exception.
        } catch (NullPointerException e) {
            fail("SpyAnnotationEngine should not throw NPE for null field");
        }
    }

    @Test
    public void shouldCreateSpyForUninitializedFieldWithDefaultConstructor() throws Exception {
        WithUninitializedSpy instance = new WithUninitializedSpy();
        engine.process(instance.getClass(), instance);
        Field field = WithUninitializedSpy.class.getDeclaredField("list");
        field.setAccessible(true);
        Object spy = field.get(instance);
        assertNotNull("Spy should be created for uninitialized field", spy);
        assertTrue("Field should be a Mockito spy", MockUtil.isMock(spy));
    }

    @Test
    public void shouldProcessMultipleSpyFields() throws Exception {
        WithMultipleSpies instance = new WithMultipleSpies();
        engine.process(instance.getClass(), instance);
        Field field1 = WithMultipleSpies.class.getDeclaredField("list1");
        field1.setAccessible(true);
        Field field2 = WithMultipleSpies.class.getDeclaredField("list2");
        field2.setAccessible(true);
        assertTrue("list1 should be spy", MockUtil.isMock(field1.get(instance)));
        assertTrue("list2 should be spy", MockUtil.isMock(field2.get(instance)));
    }

    @Test
    public void shouldNotProcessNonSpyFields() throws Exception {
        WithNonSpyField instance = new WithNonSpyField();
        engine.process(instance.getClass(), instance);
        Field field = WithNonSpyField.class.getDeclaredField("name");
        field.setAccessible(true);
        assertEquals("Non-spy field should remain unchanged", "test", field.get(instance));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForNullTestInstance() throws Exception {
        engine.process(WithInitializedSpy.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForNullContextClass() throws Exception {
        engine.process(null, new WithInitializedSpy());
    }

    @Test
    public void shouldHandleFinalSpyField() throws Exception {
        WithFinalSpy instance = new WithFinalSpy();
        engine.process(instance.getClass(), instance);
        Field field = WithFinalSpy.class.getDeclaredField("list");
        field.setAccessible(true);
        Object spy = field.get(instance);
        assertNotNull("Final field spy should not be null", spy);
        assertTrue("Final field should be spy", MockUtil.isMock(spy));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenCannotCreateSpyForAbstractClass() throws Exception {
        WithAbstractSpy instance = new WithAbstractSpy();
        engine.process(instance.getClass(), instance);
    }

    @Test
    public void shouldNotModifyFieldIfAlreadySpy() throws Exception {
        WithInitializedSpy instance = new WithInitializedSpy();
        // First process
        engine.process(instance.getClass(), instance);
        Field field = WithInitializedSpy.class.getDeclaredField("list");
        field.setAccessible(true);
        Object firstSpy = field.get(instance);
        // Process again
        engine.process(instance.getClass(), instance);
        Object secondSpy = field.get(instance);
        assertSame("Spy should not be replaced", firstSpy, secondSpy);
    }

    @Test
    public void shouldProcessFieldsFromSuperclass() throws Exception {
        class Parent {
            @Spy
            protected List<String> parentList = new LinkedList<>();
        }
        class Child extends Parent {
            @Spy
            private List<String> childList = new LinkedList<>();
        }
        Child instance = new Child();
        engine.process(Child.class, instance);
        Field parentField = Parent.class.getDeclaredField("parentList");
        parentField.setAccessible(true);
        Field childField = Child.class.getDeclaredField("childList");
        childField.setAccessible(true);
        assertTrue("Parent field should be spy", MockUtil.isMock(parentField.get(instance)));
        assertTrue("Child field should be spy", MockUtil.isMock(childField.get(instance)));
    }

    @Test
    public void shouldNotProcessStaticFields() throws Exception {
        class WithStaticSpy {
            @Spy
            private static List<String> staticList = new LinkedList<>();
        }
        WithStaticSpy instance = new WithStaticSpy();
        engine.process(WithStaticSpy.class, instance);
        Field field = WithStaticSpy.class.getDeclaredField("staticList");
        field.setAccessible(true);
        // Static fields should not be spied; value remains original
        assertFalse("Static field should not be spy", MockUtil.isMock(field.get(null)));
    }
}