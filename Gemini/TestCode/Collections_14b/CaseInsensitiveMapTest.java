package org.apache.commons.collections.map;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class CaseInsensitiveMapTest {

    @Test
    public void testConstructorAndBasicOperations() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        assertTrue(map.isEmpty());
        
        map.put("One", 1);
        map.put("TWO", 2);
        
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(1), map.get("one"));
        assertEquals(Integer.valueOf(1), map.get("ONE"));
        assertEquals(Integer.valueOf(1), map.get("One"));
        
        assertEquals(Integer.valueOf(2), map.get("two"));
        assertEquals(Integer.valueOf(2), map.get("Two"));
        
        assertTrue(map.containsKey("oNe"));
        assertTrue(map.containsKey("TwO"));
        assertFalse(map.containsKey("three"));

        assertTrue(map.containsValue(1));
        assertFalse(map.containsValue(99));

        assertEquals(Integer.valueOf(1), map.remove("ONE"));
        assertNull(map.remove("nonexistent"));
        assertEquals(1, map.size());

        map.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testNullKeysAndValues() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        
        // Null key mapping
        map.put(null, "NullValue");
        assertEquals(1, map.size());
        assertEquals("NullValue", map.get(null));
        assertTrue(map.containsKey(null));
        
        // Overwrite null key
        map.put(null, "NewNullValue");
        assertEquals("NewNullValue", map.get(null));
        
        // Null value with non-null key
        map.put("Key", null);
        assertNull(map.get("key"));
        assertTrue(map.containsKey("KEY"));
        assertTrue(map.containsValue(null));
        
        assertEquals("NewNullValue", map.remove(null));
        assertNull(map.get(null));
    }

    @Test
    public void testMapConstructor() {
        Map<String, String> initialMap = new HashMap<String, String>();
        initialMap.put("KeyOne", "ValueOne");
        initialMap.put("KEYTWO", "ValueTwo");
        
        CaseInsensitiveMap map = new CaseInsensitiveMap(initialMap);
        assertEquals(2, map.size());
        assertEquals("ValueOne", map.get("keyone"));
        assertEquals("ValueTwo", map.get("keyTwo"));
    }

    @Test
    public void testCapacityAndLoadFactorConstructors() {
        CaseInsensitiveMap map1 = new CaseInsensitiveMap(10);
        assertTrue(map1.isEmpty());

        CaseInsensitiveMap map2 = new CaseInsensitiveMap(16, 0.75f);
        assertTrue(map2.isEmpty());
        
        try {
            new CaseInsensitiveMap(-1, 0.75f);
            fail("Expected IllegalArgumentException for negative capacity");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testClone() {
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        map.put("A", 100);
        map.put("B", 200);

        CaseInsensitiveMap cloned = (CaseInsensitiveMap) map.clone();
        assertEquals(map.size(), cloned.size());
        assertEquals(Integer.valueOf(100), cloned.get("a"));
        assertEquals(Integer.valueOf(200), cloned.get("b"));

        // Mutating original shouldn't affect clone
        map.put("A", 300);
        assertEquals(Integer.valueOf(100), cloned.get("a"));
    }

    @Test
    public void testLocaleSensitivityBugTrigger() {
        // Defects4J Collections 14 deals with lowercase conversion Locale issues
        // (e.g., Turkish dotless i or general lowercasing logic).
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        
        // Standard strings
        map.put("ABC", "val");
        assertEquals("val", map.get("abc"));
        
        // Mixed casing and non-ascii / locale edge cases
        map.put("İ", "dot-i"); // Turkish capital dotted I
        // Depending on locale, String.toLowerCase() can behave differently.
        // We verify that get works with the expected converted key or standard ASCII.
        map.put("I", "ascii-i");
        assertNotNull(map.get("i"));
    }
}