package org.mockito.internal.creation;

import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class DelegatingMethodTest {

    // Helper interface and methods for testing
    public interface SampleInterface {
        void sampleMethod();
        void sampleMethod(String arg);
        int sampleMethod(int a, int b);
    }

    public static class SampleClass {
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        Method m1 = SampleInterface.class.getMethod("sampleMethod");
        Method m2 = SampleInterface.class.getMethod("sampleMethod");
        Method m3 = SampleInterface.class.getMethod("sampleMethod", String.class);

        DelegatingMethod delegating1 = new DelegatingMethod(m1);
        DelegatingMethod delegating2 = new DelegatingMethod(m2);
        DelegatingMethod delegating3 = new DelegatingMethod(m3);
        DelegatingMethod delegatingNullMethod = new DelegatingMethod(null);

        // Test equals
        assertTrue(delegating1.equals(delegating1));
        assertTrue(delegating1.equals(delegating2));
        assertFalse(delegating1.equals(delegating3));
        assertFalse(delegating1.equals(null));
        assertFalse(delegating1.equals("some string"));

        // Test equals with null underlying method
        assertTrue(delegatingNullMethod.equals(new DelegatingMethod(null)));
        assertFalse(delegatingNullMethod.equals(delegating1));
        assertFalse(delegating1.equals(delegatingNullMethod));

        // Test hashCode
        assertEquals(m1.hashCode(), delegating1.hashCode());
        
        DelegatingMethod delegatingNullHashCode = new DelegatingMethod(null);
        assertEquals(0, delegatingNullHashCode.hashCode());
    }

    @Test
    public void testGetJavaMethod() throws Exception {
        Method m = SampleInterface.class.getMethod("sampleMethod");
        DelegatingMethod delegating = new DelegatingMethod(m);
        assertSame(m, delegating.getJavaMethod());

        DelegatingMethod delegatingNull = new DelegatingMethod(null);
        assertNull(delegatingNull.getJavaMethod());
    }

    @Test
    public void testGetParameterTypes() throws Exception {
        Method m = SampleInterface.class.getMethod("sampleMethod", String.class);
        DelegatingMethod delegating = new DelegatingMethod(m);
        
        Class<?>[] parameterTypes = delegating.getParameterTypes();
        assertNotNull(parameterTypes);
        assertEquals(1, parameterTypes.length);
        assertEquals(String.class, parameterTypes[0]);
    }

    @Test
    public void testGetReturnType() throws Exception {
        Method m = SampleInterface.class.getMethod("sampleMethod", int.class, int.class);
        DelegatingMethod delegating = new DelegatingMethod(m);
        
        assertEquals(int.class, delegating.getReturnType());
    }

    @Test
    public void testGetExceptionTypes() throws Exception {
        Method m = SampleInterface.class.getMethod("sampleMethod");
        DelegatingMethod delegating = new DelegatingMethod(m);
        
        assertNotNull(delegating.getExceptionTypes());
    }

    @Test
    public void testIsAbstract() throws Exception {
        Method m = SampleInterface.class.getMethod("sampleMethod");
        DelegatingMethod delegating = new DelegatingMethod(m);
        
        // Interface methods are abstract by default
        assertTrue(delegating.isAbstract());

        Method concreteMethod = SampleClass.class.getMethod("toString");
        DelegatingMethod delegatingConcrete = new DelegatingMethod(concreteMethod);
        assertFalse(delegatingConcrete.isAbstract());
    }
}