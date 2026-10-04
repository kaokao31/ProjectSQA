package com.google.gson.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

public class $Gson$TypesTest {

  private static class TestBean<T> {
    public List<String> stringList;
    public T genericField;
    public Map<? extends Number, ? super String> wildcardMap;
    public List<?> unboundedWildcardList;
  }

  @Test
  public void testNewParameterizedTypeWithOwner() {
    ParameterizedType type = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertNotNull(type);
    assertEquals(List.class, type.getRawType());
    assertNull(type.getOwnerType());
    assertEquals(1, type.getActualTypeArguments().length);
    assertEquals(String.class, type.getActualTypeArguments()[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithOwnerInvalidEnclosing() {
    // Owner type supplied for a top-level class (List is top-level)
    $Gson$Types.newParameterizedTypeWithOwner(String.class, List.class, String.class);
  }

  @Test
  public void testNewParameterizedTypeWithOwnerNested() {
    ParameterizedType pt = (ParameterizedType) TestBean.class.getGenericSuperclass();
    ParameterizedType type = $Gson$Types.newParameterizedTypeWithOwner(Map.class, Map.Entry.class, String.class, String.class);
    assertNotNull(type);
    assertEquals(Map.Entry.class, type.getRawType());
    assertEquals(Map.class, type.getOwnerType());
  }

  @Test
  public void testNewArrayType() {
    GenericArrayType arrayType = $Gson$Types.newArrayType(String.class);
    assertNotNull(arrayType);
    assertEquals(String.class, arrayType.getComponentType());
  }

  @Test
  public void testSubtypeOf() {
    WildcardType wildcard = $Gson$Types.subtypeOf(Number.class);
    assertNotNull(wildcard);
    assertEquals(1, wildcard.getUpperBounds().length);
    assertEquals(Number.class, wildcard.getUpperBounds()[0]);
    assertEquals(0, wildcard.getLowerBounds().length);
  }

  @Test
  public void testSupertypeOf() {
    WildcardType wildcard = $Gson$Types.supertypeOf(Number.class);
    assertNotNull(wildcard);
    assertEquals(1, wildcard.getLowerBounds().length);
    assertEquals(Number.class, wildcard.getLowerBounds()[0]);
    assertEquals(1, wildcard.getUpperBounds().length);
    assertEquals(Object.class, wildcard.getUpperBounds()[0]);
  }

  @Test
  public void testCanonicalizeNull() {
    assertNull($Gson$Types.canonicalize(null));
  }

  @Test
  public void testCanonicalizeClass() {
    Type type = $Gson$Types.canonicalize(String.class);
    assertEquals(String.class, type);

    Class<int[]> intArrayClass = int[].class;
    assertEquals(intArrayClass, $Gson$Types.canonicalize(intArrayClass));
  }

  @Test
  public void testCanonicalizeParameterizedType() throws Exception {
    Type fieldType = TestBean.class.getField("stringList").getGenericType();
    Type canonicalized = $Gson$Types.canonicalize(fieldType);
    assertNotNull(canonicalized);
    assertTrue(canonicalized instanceof ParameterizedType);
  }

  @Test
  public void testCanonicalizeGenericArrayType() throws Exception {
    Type fieldType = TestBean.class.getField("stringList").getGenericType();
    GenericArrayType arrayType = $Gson$Types.newArrayType(fieldType);
    Type canonicalized = $Gson$Types.canonicalize(arrayType);
    assertNotNull(canonicalized);
    assertTrue(canonicalized instanceof GenericArrayType);
  }

  @Test
  public void testCanonicalizeWildcardType() throws Exception {
    Type fieldType = TestBean.class.getField("wildcardMap").getGenericType();
    ParameterizedType pt = (ParameterizedType) fieldType;
    Type[] actualArgs = pt.getActualTypeArguments();
    
    Type canonicalizedUpper = $Gson$Types.canonicalize(actualArgs[0]);
    assertTrue(canonicalizedUpper instanceof WildcardType);

    Type canonicalizedLower = $Gson$Types.canonicalize(actualArgs[1]);
    assertTrue(canonicalizedLower instanceof WildcardType);
  }

  @Test
  public void testGetArrayComponentType() {
    assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    assertEquals(Integer.TYPE, $Gson$Types.getArrayComponentType(int[].class));
    
    GenericArrayType gat = $Gson$Types.newArrayType(String.class);
    assertEquals(String.class, $Gson$Types.getArrayComponentType(gat));

    assertNull($Gson$Types.getArrayComponentType(String.class));
  }

  @Test
  public void testGetCollectionElementType() {
    Type collectionType = $Gson$Types.newParameterizedTypeWithOwner(null, Collection.class, String.class);
    assertEquals(String.class, $Gson$Types.getCollectionElementType(collectionType, Collection.class));

    Type rawCollection = Collection.class;
    assertEquals(Object.class, $Gson$Types.getCollectionElementType(rawCollection, Collection.class));

    Type stringClass = String.class;
    assertEquals(Object.class, $Gson$Types.getCollectionElementType(stringClass, Collection.class));
  }

  @Test
  public void testGetMapKeyAndValueTypes() throws Exception {
    Type mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    Type[] mapKeyAndValueTypes = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
    assertEquals(2, mapKeyAndValueTypes.length);
    assertEquals(String.class, mapKeyAndValueTypes[0]);
    assertEquals(Integer.class, mapKeyAndValueTypes[1]);

    Type rawMap = Map.class;
    Type[] rawMapTypes = $Gson$Types.getMapKeyAndValueTypes(rawMap, Map.class);
    assertEquals(String.class, rawMapTypes[0]);
    assertEquals(Object.class, rawMapTypes[1]);
  }

  @Test
  public void testGetRawType() {
    assertEquals(String.class, $Gson$Types.getRawType(String.class));

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(List.class, $Gson$Types.getRawType(pt));

    GenericArrayType gat = $Gson$Types.newArrayType(String.class);
    assertEquals(String[].class, $Gson$Types.getRawType(gat));

    WildcardType wt = $Gson$Types.subtypeOf(String.class);
    assertEquals(String.class, $Gson$Types.getRawType(wt));

    TypeVariable<?> tv = TestBean.class.getTypeParameters()[0];
    assertEquals(Object.class, $Gson$Types.getRawType(tv));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeIllegalArgument() {
    Type unknownType = new Type() {
      @Override
      public String toString() {
        return "UnknownType";
      }
    };
    $Gson$Types.getRawType(unknownType);
  }

  @Test
  public void testEqualsAndHashCode() {
    ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);

    assertTrue($Gson$Types.equals(pt1, pt2));
    assertFalse($Gson$Types.equals(pt1, pt3));
    assertFalse($Gson$Types.equals(pt1, null));
    assertFalse($Gson$Types.equals(pt1, String.class));
    assertEquals(pt1.hashCode(), pt2.hashCode());

    GenericArrayType gat1 = $Gson$Types.newArrayType(String.class);
    GenericArrayType gat2 = $Gson$Types.newArrayType(String.class);
    GenericArrayType gat3 = $Gson$Types.newArrayType(Integer.class);

    assertTrue($Gson$Types.equals(gat1, gat2));
    assertFalse($Gson$Types.equals(gat1, gat3));
    assertFalse($Gson$Types.equals(gat1, null));
    assertFalse($Gson$Types.equals(gat1, String.class));
    assertEquals(gat1.hashCode(), gat2.hashCode());

    WildcardType wt1 = $Gson$Types.subtypeOf(String.class);
    WildcardType wt2 = $Gson$Types.subtypeOf(String.class);
    WildcardType wt3 = $Gson$Types.subtypeOf(Integer.class);

    assertTrue($Gson$Types.equals(wt1, wt2));
    assertFalse($Gson$Types.equals(wt1, wt3));
    assertFalse($Gson$Types.equals(wt1, null));
    assertFalse($Gson$Types.equals(wt1, String.class));
    assertEquals(wt1.hashCode(), wt2.hashCode());

    TypeVariable<?> tv1 = TestBean.class.getTypeParameters()[0];
    TypeVariable<?> tv2 = TestBean.class.getTypeParameters()[0];
    assertTrue($Gson$Types.equals(tv1, tv2));
    assertFalse($Gson$Types.equals(tv1, null));
    assertFalse($Gson$Types.equals(tv1, String.class));
    assertEquals(tv1.hashCode(), tv2.hashCode());

    assertTrue($Gson$Types.equals(String.class, String.class));
    assertFalse($Gson$Types.equals(String.class, Integer.class));
  }

  @Test
  public void testTypeToString() {
    assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    assertEquals("java.lang.String[]", $Gson$Types.typeToString(String[].class));
    
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertNotNull($Gson$Types.typeToString(pt));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    assertNotNull($Gson$Types.typeToString(wt));

    WildcardType wtSuper = $Gson$Types.supertypeOf(Number.class);
    assertNotNull($Gson$Types.typeToString(wtSuper));
  }

  @Test
  public void testGetGenericSupertype() {
    Type superType = $Gson$Types.getGenericSupertype(ArrayListWithNoGeneric.class, List.class, Collection.class);
    assertNotNull(superType);
  }

  private static class ArrayListWithNoGeneric extends java.util.ArrayList<Object> {
    private static final long serialVersionUID = 1L;
  }

  @Test
  public void testResolveTypeVariable() {
    Type resolved = $Gson$Types.resolve(String[].class, String.class, TestBean.class.getTypeParameters()[0]);
    assertNotNull(resolved);
  }

  @Test
  public void testResolveParameterizedType() throws Exception {
    Type fieldType = TestBean.class.getField("stringList").getGenericType();
    Type resolved = $Gson$Types.resolve(TestBean.class, TestBean.class, fieldType);
    assertNotNull(resolved);
  }
}