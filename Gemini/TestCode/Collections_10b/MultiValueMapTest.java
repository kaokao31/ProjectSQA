package org.apache.commons.collections.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.functors.ConstantFactory;
import org.junit.Assert;
import org.junit.Test;

public class MultiValueMapTest {

    @Test
    public void testMultiValueMapConstructor() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertNotNull(map);
    }

    @Test
    public void testMultiValueMapWithMapAndFactory() {
        Map<Object, Object> inner = new HashMap<Object, Object>();
        Factory factory = ConstantFactory.getInstance(new ArrayList());
        MultiValueMap map = new MultiValueMap(inner, factory);
        Assert.assertNotNull(map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiValueMapNullFactory() {
        Map<Object, Object> inner = new HashMap<Object, Object>();
        new MultiValueMap(inner, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiValueMapInvalidFactoryType() {
        Map<Object, Object> inner = new HashMap<Object, Object>();
        // Factory must return a Collection
        Factory factory = ConstantFactory.getInstance("NotACollection");
        new MultiValueMap(inner, factory);
    }

    @Test
    public void testFactoryCreationValid() {
        MultiValueMap map = new MultiValueMap();
        Collection coll = map.createCollection(1);
        Assert.assertNotNull(coll);
        Assert.assertTrue(coll instanceof ArrayList);
    }

    @Test
    public void testPutSingle() {
        MultiValueMap map = new MultiValueMap();
        Object result = map.put("key1", "value1");
        Assert.assertNull(result); // First put for key returns null (no previous collection or empty)
        Assert.assertTrue(map.containsKey("key1"));
        Assert.assertEquals(1, map.size("key1"));
        Assert.assertEquals(1, map.totalSize());
    }

    @Test
    public void testPutMultipleValuesSameKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        Object result = map.put("key1", "value2");
        Assert.assertNotNull(result);
        Assert.assertEquals(2, map.size("key1"));
        Assert.assertTrue(map.containsValue("key1", "value1"));
        Assert.assertTrue(map.containsValue("key1", "value2"));
    }

    @Test
    public void testPutAllMap() {
        MultiValueMap map = new MultiValueMap();
        Map<String, String> source = new HashMap<String, String>();
        source.put("k1", "v1");
        source.put("k2", "v2");

        map.putAll(source);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("v1", map.get("k1"));
    }

    @Test
    public void testPutAllMultiValueMap() {
        MultiValueMap source = new MultiValueMap();
        source.put("k1", "v1");
        source.put("k1", "v2");

        MultiValueMap map = new MultiValueMap();
        map.putAll(source);
        Assert.assertEquals(2, map.size("k1"));
    }

    @Test
    public void testPutAllWithCollection() {
        MultiValueMap map = new MultiValueMap();
        Collection<String> values = Arrays.asList("v1", "v2", "v3");
        boolean changed = map.putAll("key1", values);
        Assert.assertTrue(changed);
        Assert.assertEquals(3, map.size("key1"));

        // Put empty collection
        boolean changedEmpty = map.putAll("key1", new ArrayList<String>());
        Assert.assertFalse(changedEmpty);

        // Put null collection
        boolean changedNull = map.putAll("key1", null);
        Assert.assertFalse(changedNull);
    }

    @Test
    public void testContainsValueKeySpecific() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");

        Assert.assertTrue(map.containsValue("key1", "value1"));
        Assert.assertFalse(map.containsValue("key1", "value2"));
        Assert.assertFalse(map.containsValue("nonexistent", "value1"));
    }

    @Test
    public void testContainsValueGlobal() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key2", "value2");

        Assert.assertTrue(map.containsValue("value1"));
        Assert.assertTrue(map.containsValue("value2"));
        Assert.assertFalse(map.containsValue("value3"));
    }

    @Test
    public void testGetCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");

        Object col = map.getCollection("key1");
        Assert.assertNotNull(col);
        Assert.assertTrue(col instanceof Collection);
        Assert.assertEquals(2, ((Collection) col).size());

        Assert.assertNull(map.getCollection("nonexistent"));
    }

    @Test
    public void testIterator() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");

        Iterator it = map.iterator("key1");
        Assert.assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(2, count);

        Iterator emptyIt = map.iterator("nonexistent");
        Assert.assertNotNull(emptyIt);
        Assert.assertFalse(emptyIt.hasNext());
    }

    @Test
    public void testSizeOperations() {
        MultiValueMap map = new MultiValueMap();
        Assert.assertEquals(0, map.totalSize());

        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");

        Assert.assertEquals(2, map.size("key1"));
        Assert.assertEquals(1, map.size("key2"));
        Assert.assertEquals(0, map.size("nonexistent"));
        Assert.assertEquals(3, map.totalSize());
    }

    @Test
    public void testRemoveKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");

        Object removed = map.remove("key1", "value1");
        Assert.assertEquals(true, removed);
        Assert.assertEquals(1, map.size("key1"));

        Object removedNonExistent = map.remove("key1", "nonexistentValue");
        Assert.assertEquals(false, removedNonExistent);

        Object removedNonExistentKey = map.remove("nonexistentKey", "value1");
        Assert.assertEquals(false, removedNonExistentKey);

        // Remove last element should clear key mapping completely
        map.remove("key1", "value2");
        Assert.assertFalse(map.containsKey("key1"));
    }

    @Test
    public void testClear() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key2", "value2");

        map.clear();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.totalSize());
    }

    @Test
    public void testValuesCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value1");

        Collection values = map.values();
        Assert.assertNotNull(values);
        // Total values across all keys
        Assert.assertEquals(3, values.size());
    }

    @Test
    public void testStaticMultiValueMap() {
        Map<Object, Object> normalMap = new HashMap<Object, Object>();
        normalMap.put("key1", "val1");
        
        MultiValueMap mvMap = MultiValueMap.multiValueMap(normalMap);
        Assert.assertNotNull(mvMap);
        Assert.assertEquals("val1", mvMap.get("key1"));

        Factory factory = ConstantFactory.getInstance(new LinkedList());
        MultiValueMap mvMapWithFactory = MultiValueMap.multiValueMap(normalMap, factory);
        Assert.assertNotNull(mvMapWithFactory);
    }
}