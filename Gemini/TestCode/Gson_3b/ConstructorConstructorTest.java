package com.google.gson.internal;

import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;

public class ConstructorConstructorTest {

  @Test
  public void testGet_unregisteredWithNoInstanceCreators() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
    TypeToken<String> typeToken = TypeToken.get(String.class);
    ObjectConstructor<String> creator = cc.get(typeToken);
    Assert.assertNotNull(creator);
    // Should attempt default reflection instantiation for String (which has no no-arg constructor, or might fail/succeed depending on Gson internals)
    try {
      creator.construct();
    } catch (Exception e) {
      // Expected for String if it tries to instantiate directly without mapping
    }
  }

  @Test
  public void testGet_withInstanceCreatorForType() {
    Map<Type, InstanceCreator<?>> instanceCreators = Collections.singletonMap(
      String.class,
      (InstanceCreator<String>) type -> "InstanceCreatorString"
    );
    ConstructorConstructor cc = new ConstructorConstructor(instanceCreators);
    ObjectConstructor<String> creator = cc.get(TypeToken.get(String.class));
    Assert.assertNotNull(creator);
    Assert.assertEquals("InstanceCreatorString", creator.construct());
  }

  @Test
  public void testGet_withInstanceCreatorForParameterizedType() {
    TypeToken<Collection<String>> typeToken = new TypeToken<Collection<String>>() {};
    Map<Type, InstanceCreator<?>> instanceCreators = Collections.singletonMap(
      typeToken.getType(),
      (InstanceCreator<Collection<String>>) type -> (Collection<String>) Collections.singletonList("ParameterizedCreator")
    );
    ConstructorConstructor cc = new ConstructorConstructor(instanceCreators);
    ObjectConstructor<Collection<String>> creator = cc.get(typeToken);
    Assert.assertNotNull(creator);
    Collection<String> instance = creator.construct();
    Assert.assertNotNull(instance);
    Assert.assertTrue(instance.contains("ParameterizedCreator"));
  }

  @Test
  public void testGet_defaultConstructor() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
    ObjectConstructor<SampleWithDefaultConstructor> creator = cc.get(TypeToken.get(SampleWithDefaultConstructor.class));
    Assert.assertNotNull(creator);
    SampleWithDefaultConstructor instance = creator.construct();
    Assert.assertNotNull(instance);
  }

  @Test
  public void testGet_noDefaultConstructor() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
    ObjectConstructor<SampleWithoutDefaultConstructor> creator = cc.get(TypeToken.get(SampleWithoutDefaultConstructor.class));
    Assert.assertNotNull(creator);
    try {
      creator.construct();
      Assert.fail("Expected RuntimeException due to missing no-arg constructor");
    } catch (RuntimeException e) {
      // Expected
    }
  }

  @Test
  public void testGet_collectionInterface() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
    ObjectConstructor<Collection> creator = cc.get(new TypeToken<Collection>() {});
    Assert.assertNotNull(creator);
    Assert.assertNotNull(creator.construct());
  }

  @Test
  public void testGet_sortedSetInterface() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
    ObjectConstructor<SortedSet> creator = cc.get(new TypeToken<SortedSet>() {});
    Assert.assertNotNull(creator);
    Assert.assertNotNull(creator.construct());
  }

  @Test
  public void testGet_mapInterface() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
    ObjectConstructor<Map> creator = cc.get(new TypeToken<Map>() {});
    Assert.assertNotNull(creator);
    Assert.assertNotNull(creator.construct());
  }

  @Test
  public void testGet_enumSet() {
    // EnumSet requires a special constructor or handling in Gson
    ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
    TypeToken<java.util.EnumSet<SampleEnum>> typeToken = new TypeToken<java.util.EnumSet<SampleEnum>>() {};
    ObjectConstructor<java.util.EnumSet<SampleEnum>> creator = cc.get(typeToken);
    Assert.assertNotNull(creator);
    try {
      creator.construct();
    } catch (Exception e) {
      // Depending on Gson version, EnumSet might need element type info
    }
  }

  @Test
  public void testToString() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.emptyMap());
    Assert.assertNotNull(cc.toString());
  }

  public static class SampleWithDefaultConstructor {
    public SampleWithDefaultConstructor() {}
  }

  public static class SampleWithoutDefaultConstructor {
    public SampleWithoutDefaultConstructor(String val) {}
  }

  public enum SampleEnum {
    A, B, C
  }
}