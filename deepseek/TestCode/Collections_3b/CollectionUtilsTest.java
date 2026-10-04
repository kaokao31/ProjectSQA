package org.apache.commons.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CollectionUtilsTest {

    @Test
    public void testIsEmpty() {
        assertTrue(CollectionUtils.isEmpty(null));
        assertTrue(CollectionUtils.isEmpty(new ArrayList()));
        assertTrue(CollectionUtils.isEmpty(Collections.emptyList()));
        assertFalse(CollectionUtils.isEmpty(Arrays.asList("x")));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(CollectionUtils.isNotEmpty(null));
        assertFalse(CollectionUtils.isNotEmpty(new ArrayList()));
        assertTrue(CollectionUtils.isNotEmpty(Arrays.asList("x")));
    }

    @Test
    public void testGetCardinalityMap() {
        List coll = new ArrayList();
        coll.add("A");
        coll.add("B");
        coll.add("A");
        coll.add(null);
        coll.add(null);

        Map card = CollectionUtils.getCardinalityMap(coll);
        assertEquals(Integer.valueOf(2), card.get("A"));
        assertEquals(Integer.valueOf(1), card.get("B"));
        assertEquals(Integer.valueOf(2), card.get(null));
        assertEquals(3, card.size());
    }

    @Test
    public void testGetCardinalityMapEmpty() {
        Map card = CollectionUtils.getCardinalityMap(new ArrayList());
        assertNotNull(card);
        assertTrue(card.isEmpty());
    }

    @Test
    public void testIntersectionBasic() {
        Collection a = Arrays.asList("A", "B", "B", "C");
        Collection b = Arrays.asList("B", "B", "B", "D");
        Collection result = CollectionUtils.intersection(a, b);
        assertCardinalityEquals(Arrays.asList("B", "B"), result);
    }

    @Test
    public void testIntersectionWithEmpty() {
        Collection a = Arrays.asList("A", "B");
        assertTrue(CollectionUtils.intersection(a, Collections.emptyList()).isEmpty());
        assertTrue(CollectionUtils.intersection(Collections.emptyList(), a).isEmpty());
    }

    @Test
    public void testIntersectionDisjoint() {
        Collection result = CollectionUtils.intersection(Arrays.asList("A"), Arrays.asList("B"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testIntersectionWithNullElements() {
        List a = new ArrayList();
        a.add(null);
        a.add("A");
        a.add(null);

        List b = new ArrayList();
        b.add(null);
        b.add("B");

        Collection result = CollectionUtils.intersection(a, b);
        assertCardinalityEquals(Collections.singleton(null), result);
    }

    @Test
    public void testUnionWithDuplicates() {
        Collection a = Arrays.asList("A", "B", "B", "C");
        Collection b = Arrays.asList("B", "B", "B", "D");
        Collection result = CollectionUtils.union(a, b);
        assertCardinalityEquals(Arrays.asList("A", "B", "B", "B", "C", "D"), result);
    }

    @Test
    public void testUnionWithEmpty() {
        Collection a = Arrays.asList("A", "B");
        assertCardinalityEquals(a, CollectionUtils.union(a, Collections.emptyList()));
    }

    @Test
    public void testDisjunctionWithDuplicates() {
        Collection a = Arrays.asList("A", "B", "B", "C");
        Collection b = Arrays.asList("B", "B", "B", "D");
        Collection result = CollectionUtils.disjunction(a, b);
        assertCardinalityEquals(Arrays.asList("A", "B", "C", "D"), result);
    }

    @Test
    public void testDisjunctionWithEmpty() {
        Collection a = Arrays.asList("A", "A", "B");
        assertCardinalityEquals(a, CollectionUtils.disjunction(a, Collections.emptyList()));
        assertTrue(CollectionUtils.disjunction(Collections.emptyList(), a).isEmpty() == false);
        assertCardinalityEquals(a, CollectionUtils.disjunction(Collections.emptyList(), a));
    }

    @Test
    public void testIsEqualCollection() {
        assertTrue(CollectionUtils.isEqualCollection(new ArrayList(), new ArrayList()));
        assertTrue(CollectionUtils.isEqualCollection(
                Arrays.asList("A", "B", "C"), Arrays.asList("C", "B", "A")));
        assertTrue(CollectionUtils.isEqualCollection(
                Arrays.asList("A", "A", "B"), Arrays.asList("A", "B", "A")));

        List a = new ArrayList();
        a.add("A");
        a.add(null);
        List b = new ArrayList();
        b.add(null);
        b.add("A");
        assertTrue(CollectionUtils.isEqualCollection(a, b));

        assertFalse(CollectionUtils.isEqualCollection(
                Arrays.asList("A"), Arrays.asList("A", "A")));
        assertFalse(CollectionUtils.isEqualCollection(
                Arrays.asList("A", "A"), Arrays.asList("A")));
        assertFalse(CollectionUtils.isEqualCollection(
                Arrays.asList("A", "B"), Arrays.asList("A", "C")));
        assertFalse(CollectionUtils.isEqualCollection(
                new ArrayList(), Arrays.asList("A")));
    }

    @Test
    public void testIsSubCollection() {
        assertTrue(CollectionUtils.isSubCollection(
                Collections.emptyList(), Arrays.asList("A", "B")));
        assertTrue(CollectionUtils.isSubCollection(
                Arrays.asList("A", "B"), Arrays.asList("A", "B", "C")));
        assertTrue(CollectionUtils.isSubCollection(
                Arrays.asList("A", "A", "B"), Arrays.asList("A", "A", "A", "B")));

        assertFalse(CollectionUtils.isSubCollection(
                Arrays.asList("A", "A", "B"), Arrays.asList("A", "B")));
        assertFalse(CollectionUtils.isSubCollection(
                Arrays.asList("A", "C"), Arrays.asList("A", "B")));
    }

    @Test
    public void testIsProperSubCollection() {
        assertTrue(CollectionUtils.isProperSubCollection(
                Collections.emptyList(), Arrays.asList("A")));
        assertTrue(CollectionUtils.isProperSubCollection(
                Arrays.asList("A"), Arrays.asList("A", "A")));
        assertFalse(CollectionUtils.isProperSubCollection(
                Arrays.asList("A"), Arrays.asList("A")));
        assertFalse(CollectionUtils.isProperSubCollection(
                Arrays.asList("A", "A"), Arrays.asList("A", "B")));
    }

    @Test
    public void testRetainAllWithDuplicates() {
        Collection base = new ArrayList();
        base.add("A");
        base.add("A");
        base.add("B");
        base.add("C");
        base.add(null);
        base.add(null);

        Collection retain = Arrays.asList("A", "C", null);
        Collection result = CollectionUtils.retainAll(base, retain);
        assertCardinalityEquals(Arrays.asList("A", "A", "C", null, null), result);
    }

    @Test
    public void testRetainAllDisjoint() {
        Collection result = CollectionUtils.retainAll(
                Arrays.asList("A", "B"), Arrays.asList("C"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testRemoveAllWithDuplicates() {
        Collection base = new ArrayList();
        base.add("A");
        base.add("A");
        base.add("B");
        base.add("C");
        base.add(null);
        base.add(null);

        Collection remove = Arrays.asList("A", "C", null);
        Collection result = CollectionUtils.removeAll(base, remove);
        assertCardinalityEquals(Arrays.asList("B"), result);
    }

    @Test
    public void testRemoveAllRemoveEmpty() {
        Collection result = CollectionUtils.removeAll(
                Arrays.asList("A", "B"), Collections.emptyList());
        assertCardinalityEquals(Arrays.asList("A", "B"), result);
    }

    @Test
    public void testSubtractWithDuplicates() {
        Collection a = new ArrayList();
        a.add("A");
        a.add("A");
        a.add("B");
        a.add("B");
        a.add("C");
        a.add(null);
        a.add(null);

        Collection b = Arrays.asList("A", "B", "C", null);
        Collection result = CollectionUtils.subtract(a, b);
        assertCardinalityEquals(Arrays.asList("A", "B", null), result);
    }

    @Test
    public void testSubtractWhenRemoveHasMoreOccurrences() {
        Collection result = CollectionUtils.subtract(
                Arrays.asList("A", "A"), Arrays.asList("A", "A", "A"));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSubtractWithEmpty() {
        Collection a = Arrays.asList("A", "A", "B");
        assertCardinalityEquals(a, CollectionUtils.subtract(a, Collections.emptyList()));
        assertTrue(CollectionUtils.subtract(Collections.emptyList(), a).isEmpty());
    }

    @Test
    public void testAddIgnoreNull() {
        Collection coll = new ArrayList();
        CollectionUtils.addIgnoreNull(coll, "x");
        CollectionUtils.addIgnoreNull(coll, null);
        assertEquals(1, coll.size());
        assertEquals("x", coll.iterator().next());
    }

    @Test
    public void testTransform() {
        List list = new ArrayList();
        list.add("a");
        list.add("b");

        CollectionUtils.transform(list, new Transformer() {
            public Object transform(Object input) {
                return ((String) input).toUpperCase();
            }
        });

        assertEquals(Arrays.asList("A", "B"), list);
    }

    @Test
    public void testFilter() {
        List list = new ArrayList();
        list.add("a");
        list.add("bb");
        list.add("c");

        boolean changed = CollectionUtils.filter(list, new Predicate() {
            public boolean evaluate(Object input) {
                return ((String) input).length() > 1;
            }
        });

        assertTrue(changed);
        assertEquals(Arrays.asList("bb"), list);
    }

    @Test
    public void testFilterNoChange() {
        List list = new ArrayList();
        list.add("aaa");
        list.add("bb");

        boolean changed = CollectionUtils.filter(list, new Predicate() {
            public boolean evaluate(Object input) {
                return ((String) input).length() > 1;
            }
        });

        assertFalse(changed);
        assertEquals(Arrays.asList("aaa", "bb"), list);
    }

    private static void assertCardinalityEquals(Collection expected, Collection actual) {
        assertNotNull("Actual collection should not be null", actual);
        assertEquals("Collection sizes differ", expected.size(), actual.size());
        Map<Object, Integer> expectedCounts = entryCounts(expected);
        Map<Object, Integer> actualCounts = entryCounts(actual);
        assertEquals("Cardinalities differ", expectedCounts, actualCounts);
    }

    private static Map<Object, Integer> entryCounts(Collection coll) {
        Map<Object, Integer> counts = new HashMap<Object, Integer>();
        if (coll != null) {
            for (Object o : coll) {
                Integer current = counts.get(o);
                counts.put(o, current == null ? Integer.valueOf(1) : Integer.valueOf(current.intValue() + 1));
            }
        }
        return counts;
    }
}