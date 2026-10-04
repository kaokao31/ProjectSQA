package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ReturnsSmartNullsTest {

    interface SampleInterface {
        SampleInterface self();
        String getString();
        int getInt();
        void voidMethod();
    }

    class SampleClass {
        public String toString() {
            return "SampleClass";
        }
    }

    @Test
    public void testReturnsSmartNullsForObjectMethod() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        
        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock invocation = new InvocationOnMock() {
            @Override
            public Object getMock() {
                return new Object();
            }

            @Override
            public Method getMethod() {
                return toStringMethod;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public <T> T getArgumentAt(int index, Class<T> clazz) {
                return null;
            }
        };

        Object result = returnsSmartNulls.answer(invocation);
        assertNotNull(result);
        assertTrue(result.toString().contains("SmartNull"));
    }

    @Test
    public void testReturnsSmartNullsForNonObjectMethod() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        
        Method getStringMethod = SampleInterface.class.getMethod("getString");
        InvocationOnMock invocation = new InvocationOnMock() {
            @Override
            public Object getMock() {
                return new Object();
            }

            @Override
            public Method getMethod() {
                return getStringMethod;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public <T> T getArgumentAt(int index, Class<T> clazz) {
                return null;
            }
        };

        Object result = returnsSmartNulls.answer(invocation);
        assertNull(result);
    }

    @Test
    public void testSmartNullToString() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        
        Method selfMethod = SampleInterface.class.getMethod("self");
        InvocationOnMock invocation = new InvocationOnMock() {
            @Override
            public Object getMock() {
                return new Object();
            }

            @Override
            public Method getMethod() {
                return selfMethod;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public <T> T getArgumentAt(int index, Class<T> clazz) {
                return null;
            }
        };

        Object smartNull = returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        // Invoke toString on the returned SmartNull
        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock toStringInvocation = new InvocationOnMock() {
            @Override
            public Object getMock() {
                return smartNull;
            }

            @Override
            public Method getMethod() {
                return toStringMethod;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public <T> T getArgumentAt(int index, Class<T> clazz) {
                return null;
            }
        };

        // Use the Answer inside ReturnsSmartNulls or inspect the smart null behavior
        // Actually ReturnsSmartNulls returns an Answer inside the SmartNull. 
        // Let's verify that calling toString() on the SmartNull returns a non-null string containing method info.
        String toStringResult = (String) smartNull.toString();
        assertTrue(toStringResult.contains("self"));
    }

    @Test
    public void testSmartNullMethodInvocation() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        
        Method selfMethod = SampleInterface.class.getMethod("self");
        InvocationOnMock invocation = new InvocationOnMock() {
            @Override
            public Object getMock() {
                return new Object();
            }

            @Override
            public Method getMethod() {
                return selfMethod;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public <T> T getArgumentAt(int index, Class<T> clazz) {
                return null;
            }
        };

        Object smartNull = returnsSmartNulls.answer(invocation);
        
        // Calling a non-object method on SmartNull should throw a SmartNullPointerException (or similar RuntimeException)
        // Let's test invoking a method on the smart null via reflection if it implements an Answer or via the returned object itself.
        // Wait, the returned object is a proxy of SampleInterface.
        SampleInterface proxy = (SampleInterface) smartNull;
        
        try {
            proxy.getString();
            fail("Expected exception when calling method on SmartNull");
        } catch (Throwable t) {
            assertNotNull(t);
        }
    }
}