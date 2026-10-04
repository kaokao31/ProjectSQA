package org.apache.commons.lang3.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

@SuppressWarnings("unused")
public class TypeUtilsTest<B> {

    public interface This<K, V> {
    }

    public class That<K, V> implements This<K, V> {
    }

    public interface And<K, V> extends This<Number, Number> {
    }

    public class The<K, V> extends That<Number, Number> implements And<String, String> {
    }

    public class Other<T> implements This<String, T> {
    }

    public class Thing extends TheOther<String> {
    }

    public class TheOther<B> extends That<String, B> {
    }

    public class AA<T> {
    }

    public class BB<T> extends AA<T> {
    }

    public class CC<T> extends BB<T> {
    }

    public class DD<T> extends CC<T> {
    }

    public class EE<T> extends DD<Integer> {
    }

    public class AClass extends AA<List<?>> {
    }

    public class BClass extends AA<List<? extends String>> {
    }

    public class CClass extends AA<List<? super String>> {
    }

    public class DClass extends AA<List<String>> {
    }

    public class EClass extends AA<String[]> {
    }

    public interface IA {
    }

    public interface IB {
    }

    public interface IC extends IA, IB {
    }

    public static class Stub {
        public static final String FIELD1 = "1";
        public static int FIELD2 = 2;
    }

    public List<String> stringList;
    public List<?> wildList;
    public List<? extends String> extendsStringList;
    public List<? super String> superStringList;
    public List<? extends Number> extendsNumberList;
    public List<? super Number> superNumberList;
    public List<? extends Comparable<?>> extendsComparableList;
    public List<List<String>> listOfStringList;
    public String[] stringArray;
    public List<String>[] stringListArray;
    public List<? extends String>[] extendsStringListArray;
    public B[] bArray;
    public B b;
    public List<B> bList;
    public List<? extends B> extendsBList;

    public <U extends Comparable<U>> void dummyMethod(U u) {
    }

    @Test
    public void testConstructor() {
        assertNotNull(new TypeUtils());
        Constructor<?>[] cons = TypeUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
    }

    @Test
    public void testIsAssignable() throws SecurityException, NoSuchFieldException {
        Field stringListField = getClass().getField("stringList");
        Field wildListField = getClass().getField("wildList");
        Field extendsStringListField = getClass().getField("extendsStringList");
        Field superStringListField = getClass().getField("superStringList");
        Field extendsNumberListField = getClass().getField("extendsNumberList");
        Field superNumberListField = getClass().getField("superNumberList");
        Field extendsComparableListField = getClass().getField("extendsComparableList");
        Field listOfStringListField = getClass().getField("listOfStringList");
        Field stringArrayField = getClass().getField("stringArray");
        Field stringListArrayField = getClass().getField("stringListArray");
        Field extendsStringListArrayField = getClass().getField("extendsStringListArray");
        Field bArrayField = getClass().getField("bArray");
        Field bField = getClass().getField("b");
        Field bListField = getClass().getField("bList");
        Field extendsBListField = getClass().getField("extendsBList");

        Type stringListType = stringListField.getGenericType();
        Type wildListType = wildListField.getGenericType();
        Type extendsStringListType = extendsStringListField.getGenericType();
        Type superStringListType = superStringListField.getGenericType();
        Type extendsNumberListType = extendsNumberListField.getGenericType();
        Type superNumberListType = superNumberListField.getGenericType();
        Type extendsComparableListType = extendsComparableListField.getGenericType();
        Type listOfStringListType = listOfStringListField.getGenericType();
        Type stringArrayType = stringArrayField.getGenericType();
        Type stringListArrayType = stringListArrayField.getGenericType();
        Type extendsStringListArrayType = extendsStringListArrayField.getGenericType();
        Type bArrayType = bArrayField.getGenericType();
        Type bType = bField.getGenericType();
        Type bListType = bListField.getGenericType();
        Type extendsBListType = extendsBListField.getGenericType();

        assertTrue(TypeUtils.isAssignable(CharSequence.class, Object.class));
        assertFalse(TypeUtils.isAssignable(Object.class, CharSequence.class));
        assertTrue(TypeUtils.isAssignable(String.class, CharSequence.class));
        assertFalse(TypeUtils.isAssignable(CharSequence.class, String.class));
        assertTrue(TypeUtils.isAssignable(String[].class, CharSequence[].class));
        assertFalse(TypeUtils.isAssignable(CharSequence[].class, String[].class));
        assertTrue(TypeUtils.isAssignable(String[][].class, CharSequence[][].class));
        assertFalse(TypeUtils.isAssignable(CharSequence[][].class, String[][].class));

        assertTrue(TypeUtils.isAssignable(stringListType, Collection.class));
        assertTrue(TypeUtils.isAssignable(stringListType, List.class));
        assertTrue(TypeUtils.isAssignable(stringListType, stringListType));
        assertTrue(TypeUtils.isAssignable(stringListType, wildListType));
        assertTrue(TypeUtils.isAssignable(stringListType, extendsStringListType));
        assertTrue(TypeUtils.isAssignable(stringListType, superStringListType));
        assertFalse(TypeUtils.isAssignable(stringListType, extendsNumberListType));
        assertFalse(TypeUtils.isAssignable(stringListType, superNumberListType));
        assertTrue(TypeUtils.isAssignable(stringListType, extendsComparableListType));
        assertFalse(TypeUtils.isAssignable(stringListType, listOfStringListType));

        assertTrue(TypeUtils.isAssignable(extendsStringListType, wildListType));
        assertFalse(TypeUtils.isAssignable(extendsStringListType, stringListType));
        assertTrue(TypeUtils.isAssignable(extendsStringListType, extendsComparableListType));
        assertFalse(TypeUtils.isAssignable(extendsStringListType, extendsNumberListType));

        assertTrue(TypeUtils.isAssignable(superStringListType, wildListType));
        assertFalse(TypeUtils.isAssignable(superStringListType, stringListType));

        assertTrue(TypeUtils.isAssignable(stringListArrayType, List[].class));
        assertTrue(TypeUtils.isAssignable(stringListArrayType, extendsStringListArrayType));
        assertFalse(TypeUtils.isAssignable(extendsStringListArrayType, stringListArrayType));

        assertFalse(TypeUtils.isAssignable(null, String.class));
        assertTrue(TypeUtils.isAssignable(String.class, (Type) null));
        assertTrue(TypeUtils.isAssignable((Type) null, (Type) null));

        assertTrue(TypeUtils.isAssignable(AA.class, AA.class));
        assertTrue(TypeUtils.isAssignable(BB.class, AA.class));
        assertTrue(TypeUtils.isAssignable(CC.class, AA.class));
        assertTrue(TypeUtils.isAssignable(DD.class, AA.class));
        assertTrue(TypeUtils.isAssignable(EE.class, AA.class));

        assertTrue(TypeUtils.isAssignable(AClass.class, AA.class));
        assertTrue(TypeUtils.isAssignable(BClass.class, AA.class));
        assertTrue(TypeUtils.isAssignable(CClass.class, AA.class));
        assertTrue(TypeUtils.isAssignable(DClass.class, AA.class));
        assertTrue(TypeUtils.isAssignable(EClass.class, AA.class));

        // Testing hierarchy bug triggers (Lang-15)
        ParameterizedType thisStringStringType = (ParameterizedType) That.class.getGenericInterfaces()[0];
        ParameterizedType thatStringStringType = (ParameterizedType) TheOther.class.getGenericSuperclass();
        assertTrue(TypeUtils.isAssignable(Thing.class, thisStringStringType));
        assertTrue(TypeUtils.isAssignable(Thing.class, thatStringStringType));
        assertTrue(TypeUtils.isAssignable(TheOther.class, thisStringStringType));
        assertTrue(TypeUtils.isAssignable(TheOther.class, That.class));
        assertTrue(TypeUtils.isAssignable(Other.class, This.class));

        // Primitive and array checks
        assertTrue(TypeUtils.isAssignable(int.class, Integer.class));
        assertTrue(TypeUtils.isAssignable(Integer.class, int.class));
        assertTrue(TypeUtils.isAssignable(int[].class, Object.class));
        assertTrue(TypeUtils.isAssignable(int[].class, Cloneable.class));
        assertTrue(TypeUtils.isAssignable(int[].class, Serializable.class));
        assertFalse(TypeUtils.isAssignable(int[].class, Object[].class));
        assertTrue(TypeUtils.isAssignable(Integer[].class, Object[].class));
        assertTrue(TypeUtils.isAssignable(Integer[].class, Number[].class));

        // Wildcard and TypeVariable assignability
        WildcardType wildType = (WildcardType) ((ParameterizedType) wildListType).getActualTypeArguments()[0];
        WildcardType extendsStringType = (WildcardType) ((ParameterizedType) extendsStringListType).getActualTypeArguments()[0];
        WildcardType superStringType = (WildcardType) ((ParameterizedType) superStringListType).getActualTypeArguments()[0];

        assertTrue(TypeUtils.isAssignable(String.class, wildType));
        assertTrue(TypeUtils.isAssignable(String.class, extendsStringType));
        assertTrue(TypeUtils.isAssignable(String.class, superStringType));
        assertTrue(TypeUtils.isAssignable(Object.class, superStringType));
        assertFalse(TypeUtils.isAssignable(Object.class, extendsStringType));

        assertTrue(TypeUtils.isAssignable(extendsStringType, wildType));
        assertTrue(TypeUtils.isAssignable(superStringType, wildType));
        assertFalse(TypeUtils.isAssignable(wildType, extendsStringType));
        assertFalse(TypeUtils.isAssignable(wildType, superStringType));
    }

    @Test
    public void testGetTypeArguments() {
        Map<TypeVariable<?>, Type> typeVarAssigns;

        typeVarAssigns = TypeUtils.getTypeArguments(String.class, CharSequence.class);
        assertNotNull(typeVarAssigns);
        assertEquals(0, typeVarAssigns.size());

        typeVarAssigns = TypeUtils.getTypeArguments(Stub.class, Stub.class);
        assertNotNull(typeVarAssigns);
        assertEquals(0, typeVarAssigns.size());

        typeVarAssigns = TypeUtils.getTypeArguments(ArrayList.class, List.class);
        assertNotNull(typeVarAssigns);
        assertEquals(1, typeVarAssigns.size());

        TypeVariable<?> listT = List.class.getTypeParameters()[0];
        TypeVariable<?> arrayListE = ArrayList.class.getTypeParameters()[0];
        assertEquals(arrayListE, typeVarAssigns.get(listT));

        // Test Lang-15 bug: Thing -> This<String, String>
        ParameterizedType thisStringStringType = (ParameterizedType) That.class.getGenericInterfaces()[0];
        typeVarAssigns = TypeUtils.getTypeArguments(Thing.class, This.class);
        assertNotNull(typeVarAssigns);
        assertEquals(2, typeVarAssigns.size());
        assertEquals(String.class, typeVarAssigns.get(This.class.getTypeParameters()[0]));
        assertEquals(String.class, typeVarAssigns.get(This.class.getTypeParameters()[1]));

        typeVarAssigns = TypeUtils.getTypeArguments(TheOther.class, This.class);
        assertNotNull(typeVarAssigns);
        assertEquals(2, typeVarAssigns.size());
        assertEquals(String.class, typeVarAssigns.get(This.class.getTypeParameters()[0]));
        assertEquals(TheOther.class.getTypeParameters()[0], typeVarAssigns.get(This.class.getTypeParameters()[1]));

        typeVarAssigns = TypeUtils.getTypeArguments(The.class, This.class);
        assertNotNull(typeVarAssigns);
        assertEquals(2, typeVarAssigns.size());
        assertEquals(Number.class, typeVarAssigns.get(This.class.getTypeParameters()[0]));
        assertEquals(Number.class, typeVarAssigns.get(This.class.getTypeParameters()[1]));

        typeVarAssigns = TypeUtils.getTypeArguments(The.class, That.class);
        assertNotNull(typeVarAssigns);
        assertEquals(2, typeVarAssigns.size());
        assertEquals(Number.class, typeVarAssigns.get(That.class.getTypeParameters()[0]));
        assertEquals(Number.class, typeVarAssigns.get(That.class.getTypeParameters()[1]));

        typeVarAssigns = TypeUtils.getTypeArguments(The.class, And.class);
        assertNotNull(typeVarAssigns);
        assertEquals(2, typeVarAssigns.size());
        assertEquals(The.class.getTypeParameters()[0], typeVarAssigns.get(And.class.getTypeParameters()[0]));
        assertEquals(The.class.getTypeParameters()[1], typeVarAssigns.get(And.class.getTypeParameters()[1]));

        typeVarAssigns = TypeUtils.getTypeArguments(EE.class, AA.class);
        assertNotNull(typeVarAssigns);
        assertEquals(1, typeVarAssigns.size());
        assertEquals(Integer.class, typeVarAssigns.get(AA.class.getTypeParameters()[0]));

        assertNull(TypeUtils.getTypeArguments(List.class, Set.class));
        assertNull(TypeUtils.getTypeArguments((Type) null, List.class));
        assertNull(TypeUtils.getTypeArguments(List.class, (Class<?>) null));
    }

    @Test
    public void testDetermineTypeArguments() throws NoSuchFieldException {
        Field stringListField = getClass().getField("stringList");
        ParameterizedType stringListType = (ParameterizedType) stringListField.getGenericType();
        Map<TypeVariable<?>, Type> typeArgs = TypeUtils.determineTypeArguments(ArrayList.class, stringListType);
        assertNotNull(typeArgs);
        assertEquals(1, typeArgs.size());
        assertEquals(String.class, typeArgs.get(ArrayList.class.getTypeParameters()[0]));

        assertNull(TypeUtils.determineTypeArguments(null, stringListType));
        assertNull(TypeUtils.determineTypeArguments(ArrayList.class, null));
        assertNull(TypeUtils.determineTypeArguments(TreeSet.class, stringListType));
    }

    @Test
    public void testGetArrayComponentType() throws NoSuchFieldException {
        Field stringArrayField = getClass().getField("stringArray");
        Field stringListArrayField = getClass().getField("stringListArray");
        Field stringListField = getClass().getField("stringList");

        assertEquals(String.class, TypeUtils.getArrayComponentType(stringArrayField.getGenericType()));
        assertEquals(stringListField.getGenericType(), TypeUtils.getArrayComponentType(stringListArrayField.getGenericType()));
        assertNull(TypeUtils.getArrayComponentType(stringListField.getGenericType()));
        assertNull(TypeUtils.getArrayComponentType(null));
        assertEquals(int.class, TypeUtils.getArrayComponentType(int[].class));
        assertEquals(int[].class, TypeUtils.getArrayComponentType(int[][].class));
    }

    @Test
    public void testGetRawType() throws NoSuchFieldException {
        Field stringListField = getClass().getField("stringList");
        Field stringListArrayField = getClass().getField("stringListArray");
        Field bField = getClass().getField("b");
        Field bArrayField = getClass().getField("bArray");

        assertEquals(List.class, TypeUtils.getRawType(stringListField.getGenericType(), null));
        assertEquals(List[].class, TypeUtils.getRawType(stringListArrayField.getGenericType(), null));
        assertEquals(String.class, TypeUtils.getRawType(String.class, null));
        assertEquals(String[].class, TypeUtils.getRawType(String[].class, null));
        assertNull(TypeUtils.getRawType(null, null));

        assertEquals(Object.class, TypeUtils.getRawType(bField.getGenericType(), getClass()));
        assertEquals(Object[].class, TypeUtils.getRawType(bArrayField.getGenericType(), getClass()));
    }

    @Test
    public void testNormalizeUpperBounds() {
        Type[] bounds = new Type[] { Object.class, CharSequence.class, String.class };
        Type[] normalized = TypeUtils.normalizeUpperBounds(bounds);
        assertEquals(1, normalized.length);
        assertEquals(String.class, normalized[0]);

        bounds = new Type[] { IA.class, IB.class };
        normalized = TypeUtils.normalizeUpperBounds(bounds);
        assertEquals(2, normalized.length);
        assertTrue(Arrays.asList(normalized).contains(IA.class));
        assertTrue(Arrays.asList(normalized).contains(IB.class));

        bounds = new Type[] { IA.class, IC.class };
        normalized = TypeUtils.normalizeUpperBounds(bounds);
        assertEquals(1, normalized.length);
        assertEquals(IC.class, normalized[0]);

        assertEquals(0, TypeUtils.normalizeUpperBounds(new Type[0]).length);
        assertEquals(0, TypeUtils.normalizeUpperBounds(null).length);
    }

    @Test
    public void testGetImplicitUpperBounds() throws NoSuchFieldException {
        Field wildListField = getClass().getField("wildList");
        Field extendsStringListField = getClass().getField("extendsStringList");
        Field superStringListField = getClass().getField("superStringList");

        WildcardType wildType = (WildcardType) ((ParameterizedType) wildListField.getGenericType()).getActualTypeArguments()[0];
        WildcardType extendsStringType = (WildcardType) ((ParameterizedType) extendsStringListField.getGenericType()).getActualTypeArguments()[0];
        WildcardType superStringType = (WildcardType) ((ParameterizedType) superStringListField.getGenericType()).getActualTypeArguments()[0];

        Type[] wildBounds = TypeUtils.getImplicitUpperBounds(wildType);
        assertEquals(1, wildBounds.length);
        assertEquals(Object.class, wildBounds[0]);

        Type[] extendsStringBounds = TypeUtils.getImplicitUpperBounds(extendsStringType);
        assertEquals(1, extendsStringBounds.length);
        assertEquals(String.class, extendsStringBounds[0]);

        Type[] superStringBounds = TypeUtils.getImplicitUpperBounds(superStringType);
        assertEquals(1, superStringBounds.length);
        assertEquals(Object.class, superStringBounds[0]);

        assertEquals(0, TypeUtils.getImplicitUpperBounds(null).length);
    }

    @Test
    public void testGetImplicitLowerBounds() throws NoSuchFieldException {
        Field wildListField = getClass().getField("wildList");
        Field extendsStringListField = getClass().getField("extendsStringList");
        Field superStringListField = getClass().getField("superStringList");

        WildcardType wildType = (WildcardType) ((ParameterizedType) wildListField.getGenericType()).getActualTypeArguments()[0];
        WildcardType extendsStringType = (WildcardType) ((ParameterizedType) extendsStringListField.getGenericType()).getActualTypeArguments()[0];
        WildcardType superStringType = (WildcardType) ((ParameterizedType) superStringListField.getGenericType()).getActualTypeArguments()[0];

        Type[] wildBounds = TypeUtils.getImplicitLowerBounds(wildType);
        assertEquals(1, wildBounds.length);
        assertNull(wildBounds[0]);

        Type[] extendsStringBounds = TypeUtils.getImplicitLowerBounds(extendsStringType);
        assertEquals(1, extendsStringBounds.length);
        assertNull(extendsStringBounds[0]);

        Type[] superStringBounds = TypeUtils.getImplicitLowerBounds(superStringType);
        assertEquals(1, superStringBounds.length);
        assertEquals(String.class, superStringBounds[0]);

        assertEquals(0, TypeUtils.getImplicitLowerBounds(null).length);
    }

    @Test
    public void testTypesSatisfyVariables() throws NoSuchMethodException {
        Method method = getClass().getMethod("dummyMethod", Comparable.class);
        TypeVariable<?>[] typeParams = method.getTypeParameters();
        Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<TypeVariable<?>, Type>();

        typeVarAssigns.put(typeParams[0], String.class);
        assertTrue(TypeUtils.typesSatisfyVariables(typeVarAssigns));

        typeVarAssigns.put(typeParams[0], Object.class);
        assertFalse(TypeUtils.typesSatisfyVariables(typeVarAssigns));

        assertFalse(TypeUtils.typesSatisfyVariables(null));
        assertTrue(TypeUtils.typesSatisfyVariables(Collections.<TypeVariable<?>, Type>emptyMap()));
    }

    @Test
    public void testIsInstance() throws NoSuchFieldException {
        Field stringListField = getClass().getField("stringList");
        List<String> list = new ArrayList<String>();
        list.add("hello");

        assertTrue(TypeUtils.isInstance(list, stringListField.getGenericType()));
        assertTrue(TypeUtils.isInstance("hello", String.class));
        assertFalse(TypeUtils.isInstance("hello", Integer.class));
        assertFalse(TypeUtils.isInstance(null, String.class));
        assertFalse(TypeUtils.isInstance("hello", null));
        assertFalse(TypeUtils.isInstance(null, null));
    }

    @Test
    public void testToStringAndToLongString() throws NoSuchFieldException {
        Field stringListField = getClass().getField("stringList");
        Field wildListField = getClass().getField("wildList");

        assertNotNull(TypeUtils.toString(stringListField.getGenericType()));
        assertNotNull(TypeUtils.toLongString(stringListField.getGenericType()));
        assertNotNull(TypeUtils.toString(wildListField.getGenericType()));
        assertNotNull(TypeUtils.toLongString(wildListField.getGenericType()));
        assertNotNull(TypeUtils.toString(String.class));
        assertNotNull(TypeUtils.toLongString(String.class));
        assertNotNull(TypeUtils.toString(int[].class));
        assertNotNull(TypeUtils.toLongString(int[].class));
        assertNull(TypeUtils.toString(null));
        assertNull(TypeUtils.toLongString(null));
    }

    @Test
    public void testWrap() throws NoSuchFieldException {
        Field stringListField = getClass().getField("stringList");
        Type type = stringListField.getGenericType();
        assertNotNull(TypeUtils.wrap(type));
        assertSame(String.class, TypeUtils.wrap(String.class).getType());
    }
}