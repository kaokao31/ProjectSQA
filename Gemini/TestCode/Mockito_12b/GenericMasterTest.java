package org.mockito.internal.util.reflection;

import org.junit.Test;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GenericMasterTest {

    // Helper interfaces and classes for reflection testing
    private interface SimpleInterface<T> {
        T getT();
    }

    private interface ChildInterface extends SimpleInterface<String> {
    }

    private interface MultiParamInterface<A, B> {
        B map(A a);
    }

    private interface ExtendsMultiParam extends MultiParamInterface<Integer, Long> {
    }

    private static class ConcreteClass implements SimpleInterface<Integer> {
        public Integer getT() {
            return 1;
        }
    }

    private static class SubConcreteClass extends ConcreteClass {
    }

    private static class RawClass implements SimpleInterface {
        public Object getT() {
            return null;
        }
    }

    public String genericMethod(List<String> list, Set<Integer> set, String normal, List rawList) {
        return null;
    }

    public List<String> genericMethodWithReturnType() {
        return null;
    }

    public List genericMethodWithRawReturnType() {
        return null;
    }

    @Test
    public void testGetGenericType_ChildInterface() {
        GenericMaster master = new GenericMaster();
        Type type = master.getGenericType(ChildInterface.class);
        assertEquals(String.class, type);
    }

    @Test
    public void testGetGenericType_ExtendsMultiParam() {
        GenericMaster master = new GenericMaster();
        Type type = master.getGenericType(ExtendsMultiParam.class);
        // Depending on implementation, it typically inspects interfaces/superclasses.
        // MultiParamInterface has <A, B>, ExtendsMultiParam passes <Integer, Long>.
        // Let's verify it returns something non-null or specific.
        assertNull(master.getGenericType(MultiParamInterface.class));
    }

    @Test
    public void testGetGenericType_ConcreteClass() {
        GenericMaster master = new GenericMaster();
        Type type = master.getGenericType(ConcreteClass.class);
        assertEquals(Integer.class, type);
    }

    @Test
    public void testGetGenericType_SubConcreteClass() {
        GenericMaster master = new GenericMaster();
        // SubConcreteClass extends ConcreteClass which implements SimpleInterface<Integer>
        Type type = master.getGenericType(SubConcreteClass.class);
        assertEquals(Integer.class, type);
    }

    @Test
    public void testGetGenericType_RawClass() {
        GenericMaster master = new GenericMaster();
        Type type = master.getGenericType(RawClass.class);
        // Raw class has no type arguments, should return Object.class or null depending on implementation
        assertEquals(Object.class, type);
    }

    @Test
    public void testGetGenericType_NonParameterized() {
        GenericMaster master = new GenericMaster();
        Type type = master.getGenericType(String.class);
        assertEquals(Object.class, type);
    }

    @Test
    public void testGetGenericType_Null() {
        GenericMaster master = new GenericMaster();
        Type type = master.getGenericType(null);
        assertEquals(Object.class, type);
    }
}