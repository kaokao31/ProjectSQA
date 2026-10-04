package org.mockito.internal.invocation;

import org.junit.Test;
import org.mockito.internal.invocation.realmethod.RealMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class InvocationTest {

    public static class SampleClass {
        public void myMethod(String arg) {}
        public void myMethod(int arg) {}
        public void varargsMethod(String... args) {}
        public int returnInt() { return 42; }
    }

    private static class DummyRealMethod implements RealMethod {
        private final Object result;
        private final Throwable throwable;

        public DummyRealMethod(Object result, Throwable throwable) {
            this.result = result;
            this.throwable = throwable;
        }

        @Override
        public Object invoke(Object target, Object[] args) throws Throwable {
            if (throwable != null) {
                throw throwable;
            }
            return result;
        }
    }

    @Test
    public void testInvocationCreationAndGetters() throws Exception {
        Method method = SampleClass.class.getMethod("myMethod", String.class);
        Object[] args = new Object[] { "test" };
        RealMethod realMethod = new DummyRealMethod(null, null);

        Invocation invocation = new Invocation(new SampleClass(), method, args, 1, realMethod);

        assertEquals("test", invocation.getMethod().getName());
        assertArrayEquals(args, invocation.getArguments());
        assertEquals(1, invocation.getSequenceNumber());
        assertEquals("myMethod", invocation.getMethod().getName());
        assertSame(realMethod, invocation.getRealMethod());
        assertNotNull(invocation.toString());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        Method method1 = SampleClass.class.getMethod("myMethod", String.class);
        Method method2 = SampleClass.class.getMethod("myMethod", int.class);

        SampleClass mock1 = new SampleClass();
        SampleClass mock2 = new SampleClass();

        Invocation inv1 = new Invocation(mock1, method1, new Object[] { "a" }, 1, new DummyRealMethod(null, null));
        Invocation inv2 = new Invocation(mock1, method1, new Object[] { "a" }, 1, new DummyRealMethod(null, null));
        Invocation inv3 = new Invocation(mock1, method1, new Object[] { "b" }, 1, new DummyRealMethod(null, null));
        Invocation inv4 = new Invocation(mock2, method1, new Object[] { "a" }, 1, new DummyRealMethod(null, null));
        Invocation inv5 = new Invocation(mock1, method2, new Object[] { 1 }, 1, new DummyRealMethod(null, null));

        assertTrue(inv1.equals(inv1));
        assertTrue(inv1.equals(inv2));
        assertTrue(inv2.equals(inv1));

        assertFalse(inv1.equals(null));
        assertFalse(inv1.equals("some string"));
        assertFalse(inv1.equals(inv3)); // different args
        assertFalse(inv1.equals(inv4)); // different mock
        assertFalse(inv1.equals(inv5)); // different method

        assertEquals(inv1.hashCode(), inv2.hashCode());
    }

    @Test
    public void testCompareTo() throws Exception {
        Method method = SampleClass.class.getMethod("myMethod", String.class);
        Invocation inv1 = new Invocation(new SampleClass(), method, new Object[] { "a" }, 1, new DummyRealMethod(null, null));
        Invocation inv2 = new Invocation(new SampleClass(), method, new Object[] { "a" }, 2, new DummyRealMethod(null, null));
        Invocation inv3 = new Invocation(new SampleClass(), method, new Object[] { "a" }, 1, new DummyRealMethod(null, null));

        assertTrue(inv1.compareTo(inv2) < 0);
        assertTrue(inv2.compareTo(inv1) > 0);
        assertEquals(0, inv1.compareTo(inv3));
    }

    @Test
    public void testCallRealMethod() throws Throwable {
        Method method = SampleClass.class.getMethod("returnInt");
        RealMethod realMethod = new DummyRealMethod(100, null);
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[0], 1, realMethod);

        Object result = invocation.callRealMethod();
        assertEquals(100, result);
    }

    @Test(expected = RuntimeException.class)
    public void testCallRealMethodThrowsException() throws Throwable {
        Method method = SampleClass.class.getMethod("returnInt");
        RealMethod realMethod = new DummyRealMethod(null, new RuntimeException("fail"));
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[0], 1, realMethod);

        invocation.callRealMethod();
    }

    @Test
    public void testFormattingArguments() throws Exception {
        Method method = SampleClass.class.getMethod("myMethod", String.class);
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[] { "hello" }, 1, new DummyRealMethod(null, null));

        assertEquals("[hello]", invocation.getArgumentsAsPrintable());
    }

    @Test
    public void testExpandVarArgs() throws Exception {
        Method method = SampleClass.class.getMethod("varargsMethod", String[].class);
        // Passing an array as varargs
        String[] varargs = new String[] { "a", "b" };
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[] { varargs }, 1, new DummyRealMethod(null, null));

        assertNotNull(invocation.getArguments());
    }

    @Test
    public void testMarkVerified() throws Exception {
        Method method = SampleClass.class.getMethod("returnInt");
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[0], 1, new DummyRealMethod(null, null));

        assertFalse(invocation.isVerified());
        invocation.markVerified();
        assertTrue(invocation.isVerified());
    }

    @Test
    public void testLocation() throws Exception {
        Method method = SampleClass.class.getMethod("returnInt");
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[0], 1, new DummyRealMethod(null, null));

        assertNotNull(invocation.getLocation());
    }
}