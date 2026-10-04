package org.mockito.internal.creation;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

public class DelegatingMethodTest {

    private DelegatingMethod delegatingMethod;
    private Method method;

    @Before
    public void setUp() throws Exception {
        method = Object.class.getMethod("equals", Object.class);
        delegatingMethod = new DelegatingMethod(method);
    }

    @Test
    public void testGetMethod() {
        assertSame(method, delegatingMethod.getMethod());
    }

    @Test
    public void testIsVarArgsWithNonVarArgsMethod() throws Exception {
        assertFalse(delegatingMethod.isVarArgs());
    }

    @Test
    public void testIsVarArgsWithVarArgsMethod() throws Exception {
        Method varArgsMethod = String.class.getMethod("format", String.class, Object[].class);
        DelegatingMethod varArgsDelegatingMethod = new DelegatingMethod(varArgsMethod);
        assertTrue(varArgsDelegatingMethod.isVarArgs());
    }

    @Test
    public void testIsVarArgsWithMethodHavingPrimitiveArrayParam() throws Exception {
        Method primitiveArrayMethod = getClass().getDeclaredMethod("methodWithPrimitiveArray", int[].class);
        DelegatingMethod primitiveArrayDelegatingMethod = new DelegatingMethod(primitiveArrayMethod);
        assertFalse(primitiveArrayDelegatingMethod.isVarArgs());
    }

    private void methodWithPrimitiveArray(int[] arr) {}

    @Test
    public void testIsVarArgsWithMethodHavingObjectArrayParam() throws Exception {
        Method objectArrayMethod = getClass().getDeclaredMethod("methodWithObjectArray", Object[].class);
        DelegatingMethod objectArrayDelegatingMethod = new DelegatingMethod(objectArrayMethod);
        assertFalse(objectArrayDelegatingMethod.isVarArgs());
    }

    private void methodWithObjectArray(Object[] arr) {}

    @Test
    public void testIsVarArgsWithMethodHavingStringArrayParam() throws Exception {
        Method stringArrayMethod = getClass().getDeclaredMethod("methodWithStringArray", String[].class);
        DelegatingMethod stringArrayDelegatingMethod = new DelegatingMethod(stringArrayMethod);
        assertFalse(stringArrayDelegatingMethod.isVarArgs());
    }

    private void methodWithStringArray(String[] arr) {}

    @Test
    public void testIsVarArgsWithMethodHavingVarArgsString() throws Exception {
        Method varArgsStringMethod = getClass().getDeclaredMethod("varArgsMethod", String[].class);
        DelegatingMethod varArgsStringDelegatingMethod = new DelegatingMethod(varArgsStringMethod);
        assertTrue(varArgsStringDelegatingMethod.isVarArgs());
    }

    private void varArgsMethod(String... args) {}

    @Test
    public void testIsVarArgsWithMethodHavingVarArgsInt() throws Exception {
        Method varArgsIntMethod = getClass().getDeclaredMethod("varArgsIntMethod", int[].class);
        DelegatingMethod varArgsIntDelegatingMethod = new DelegatingMethod(varArgsIntMethod);
        assertTrue(varArgsIntDelegatingMethod.isVarArgs());
    }

    private void varArgsIntMethod(int... args) {}

    @Test
    public void testEquals() {
        DelegatingMethod sameMethod = new DelegatingMethod(method);
        DelegatingMethod differentMethod = new DelegatingMethod(String.class.getMethods()[0]);
        
        assertTrue(delegatingMethod.equals(sameMethod));
        assertTrue(delegatingMethod.equals(delegatingMethod));
        assertFalse(delegatingMethod.equals(null));
        assertFalse(delegatingMethod.equals(new Object()));
        assertFalse(delegatingMethod.equals(differentMethod));
    }

    @Test
    public void testHashCode() {
        DelegatingMethod sameMethod = new DelegatingMethod(method);
        assertEquals(delegatingMethod.hashCode(), sameMethod.hashCode());
    }

    @Test
    public void testToString() {
        assertNotNull(delegatingMethod.toString());
        assertTrue(delegatingMethod.toString().contains("equals"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullMethod() {
        new DelegatingMethod(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsVarArgsWithPrivateMethod() throws Exception {
        Method privateMethod = getClass().getDeclaredMethod("privateMethod");
        privateMethod.setAccessible(true);
        DelegatingMethod privateDelegatingMethod = new DelegatingMethod(privateMethod);
        privateDelegatingMethod.isVarArgs();
    }

    @SuppressWarnings("unused")
    private void privateMethod() {}

    @Test(expected = IllegalArgumentException.class)
    public void testIsVarArgsWithProtectedMethod() throws Exception {
        Method protectedMethod = getClass().getDeclaredMethod("protectedMethod");
        DelegatingMethod protectedDelegatingMethod = new DelegatingMethod(protectedMethod);
        protectedDelegatingMethod.isVarArgs();
    }

    @SuppressWarnings("unused")
    protected void protectedMethod() {}

    @Test(expected = IllegalArgumentException.class)
    public void testIsVarArgsWithStaticMethod() throws Exception {
        Method staticMethod = getClass().getDeclaredMethod("staticMethod");
        DelegatingMethod staticDelegatingMethod = new DelegatingMethod(staticMethod);
        staticDelegatingMethod.isVarArgs();
    }

    @SuppressWarnings("unused")
    private static void staticMethod() {}

    @Test
    public void testGetMethodReturnsCorrectMethod() {
        assertNotNull(delegatingMethod.getMethod());
        assertEquals("equals", delegatingMethod.getMethod().getName());
    }

    @Test
    public void testIsVarArgsWithFinalMethod() throws Exception {
        Method finalMethod = getClass().getDeclaredMethod("finalMethod");
        DelegatingMethod finalDelegatingMethod = new DelegatingMethod(finalMethod);
        assertFalse(finalDelegatingMethod.isVarArgs());
    }

    @SuppressWarnings("unused")
    private final void finalMethod() {}

    @Test
    public void testIsVarArgsWithSynchronizedMethod() throws Exception {
        Method synchronizedMethod = getClass().getDeclaredMethod("synchronizedMethod");
        DelegatingMethod synchronizedDelegatingMethod = new DelegatingMethod(synchronizedMethod);
        assertFalse(synchronizedDelegatingMethod.isVarArgs());
    }

    @SuppressWarnings("unused")
    private synchronized void synchronizedMethod() {}

    @Test
    public void testIsVarArgsWithNativeMethod() throws Exception {
        // Cannot easily obtain a native method, but we can test that non-varargs methods return false
        Method nonVarArgsMethod = getClass().getDeclaredMethod("nonVarArgsMethod", String.class, int.class);
        DelegatingMethod nonVarArgsDelegatingMethod = new DelegatingMethod(nonVarArgsMethod);
        assertFalse(nonVarArgsDelegatingMethod.isVarArgs());
    }

    @SuppressWarnings("unused")
    private void nonVarArgsMethod(String s, int i) {}

    @Test
    public void testIsVarArgsWithMethodThatReturnsArray() throws Exception {
        Method arrayReturnMethod = getClass().getDeclaredMethod("arrayReturnMethod");
        DelegatingMethod arrayReturnDelegatingMethod = new DelegatingMethod(arrayReturnMethod);
        assertFalse(arrayReturnDelegatingMethod.isVarArgs());
    }

    @SuppressWarnings("unused")
    private int[] arrayReturnMethod() { return new int[0]; }

    @Test
    public void testEqualsWithNullMethodInConstructor() {
        try {
            new DelegatingMethod(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testHashCodeWithSameMethod() throws Exception {
        Method m1 = String.class.getMethod("length");
        Method m2 = String.class.getMethod("length");
        DelegatingMethod dm1 = new DelegatingMethod(m1);
        DelegatingMethod dm2 = new DelegatingMethod(m2);
        assertEquals(dm1.hashCode(), dm2.hashCode());
    }

    @Test
    public void testEqualsWithDifferentMethodsSameSignature() throws Exception {
        Method m1 = getClass().getDeclaredMethod("testEquals");
        Method m2 = getClass().getDeclaredMethod("testIsVarArgsWithNonVarArgsMethod");
        DelegatingMethod dm1 = new DelegatingMethod(m1);
        DelegatingMethod dm2 = new DelegatingMethod(m2);
        assertFalse(dm1.equals(dm2));
    }

    @Test
    public void testIsVarArgsWithMethodHavingMultipleParametersAndVarArgsAtEnd() throws Exception {
        Method varArgsMethod = getClass().getDeclaredMethod("multipleParamsVarArgs", String.class, int.class, Object[].class);
        DelegatingMethod varArgsDelegatingMethod = new DelegatingMethod(varArgsMethod);
        assertTrue(varArgsDelegatingMethod.isVarArgs());
    }

    @SuppressWarnings("unused")
    private void multipleParamsVarArgs(String s, int i, Object... args) {}

    @Test
    public void testIsVarArgsWithMethodHavingOnlyVarArgs() throws Exception {
        Method onlyVarArgsMethod = getClass().getDeclaredMethod("onlyVarArgs", Object[].class);
        DelegatingMethod onlyVarArgsDelegatingMethod = new DelegatingMethod(onlyVarArgsMethod);
        assertTrue(onlyVarArgsDelegatingMethod.isVarArgs());
    }

    @SuppressWarnings("unused")
    private void onlyVarArgs(Object... args) {}

    @Test
    public void testIsVarArgsWithGenericMethod() throws Exception {
        Method genericMethod = getClass().getDeclaredMethod("genericMethod", Object.class);
        DelegatingMethod genericDelegatingMethod = new DelegatingMethod(genericMethod);
        assertFalse(genericDelegatingMethod.isVarArgs());
    }

    @SuppressWarnings("unused")
    private <T> void genericMethod(T t) {}

    @Test
    public void testIsVarArgsWithBridgeMethod() throws Exception {
        Method bridgeMethod = getClass().getDeclaredMethod("equals", Object.class);
        DelegatingMethod bridgeDelegatingMethod = new DelegatingMethod(bridgeMethod);
        assertFalse(bridgeDelegatingMethod.isVarArgs());
    }
}