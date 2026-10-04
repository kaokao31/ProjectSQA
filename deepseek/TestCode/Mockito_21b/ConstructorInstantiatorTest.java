package org.mockito.internal.creation.instance;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConstructorInstantiatorTest {

    private ConstructorInstantiator instantiator;

    // -- Helper types used in tests ----------------------------------------

    public static class NormalClass {
        public NormalClass() {}
    }

    public static class StaticInnerClass {
        public StaticInnerClass() {}
    }

    public class InnerClass {
        public InnerClass() {}
    }

    public static class PrivateConstructorClass {
        private PrivateConstructorClass() {}
    }

    public static class NoDefaultConstructorClass {
        public NoDefaultConstructorClass(@SuppressWarnings("unused") int x) {}
    }

    public abstract static class AbstractClass {}

    public interface TestInterface {}

    // ----------------------------------------------------------------------

    @Before
    public void setUp() {
        instantiator = new ConstructorInstantiator();
    }

    @Test
    public void testInstantiateNormalClass() throws InstantiationException {
        Object instance = instantiator.newInstance(NormalClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof NormalClass);
    }

    @Test
    public void testInstantiateStaticInnerClass() throws InstantiationException {
        Object instance = instantiator.newInstance(StaticInnerClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof StaticInnerClass);
    }

    @Test
    public void testInstantiateNonStaticInnerClass() throws InstantiationException {
        // This test targets the defect: non-static inner class instantiation should succeed
        // when the enclosing instance is available. In the buggy version it will fail.
        Object instance = instantiator.newInstance(InnerClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof InnerClass);
    }

    @Test(expected = InstantiationException.class)
    public void testInstantiateAbstractClass() throws InstantiationException {
        instantiator.newInstance(AbstractClass.class);
    }

    @Test(expected = InstantiationException.class)
    public void testInstantiateInterface() throws InstantiationException {
        instantiator.newInstance(TestInterface.class);
    }

    @Test(expected = InstantiationException.class)
    public void testInstantiateClassWithPrivateConstructor() throws InstantiationException {
        instantiator.newInstance(PrivateConstructorClass.class);
    }

    @Test(expected = InstantiationException.class)
    public void testInstantiateClassWithoutDefaultConstructor() throws InstantiationException {
        instantiator.newInstance(NoDefaultConstructorClass.class);
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullClass() throws InstantiationException {
        instantiator.newInstance(null);
    }

    @Test(expected = InstantiationException.class)
    public void testInstantiatePrimitiveClass() throws InstantiationException {
        instantiator.newInstance(int.class);
    }

    @Test(expected = InstantiationException.class)
    public void testInstantiateArrayClass() throws InstantiationException {
        instantiator.newInstance(String[].class);
    }
}