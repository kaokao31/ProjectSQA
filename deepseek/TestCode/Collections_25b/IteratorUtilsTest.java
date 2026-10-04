package org.apache.commons.collections4;

import static org.junit.Assert.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

public class IteratorUtilsTest {

    private List<String> list;
    private Iterator<String> emptyIterator;
    private Iterator<String> singleIterator;
    private Iterator<String> multiIterator;

    @Before
    public void setUp() {
        list = Arrays.asList("a", "b", "c", "d", "e");
        emptyIterator = Collections.<String>emptyIterator();
        singleIterator = Collections.singleton("x").iterator();
        multiIterator = list.iterator();
    }

    // ========== toList ==========
    @Test
    public void testToListWithEmptyIterator() {
        List<String> result = IteratorUtils.toList(emptyIterator);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testToListWithSingleElement() {
        List<String> result = IteratorUtils.toList(singleIterator);
        assertEquals(1, result.size());
        assertEquals("x", result.get(0));
    }

    @Test
    public void testToListWithMultipleElements() {
        List<String> result = IteratorUtils.toList(multiIterator);
        assertEquals(list, result);
    }

    @Test(expected = NullPointerException.class)
    public void testToListWithNullIterator() {
        IteratorUtils.toList(null);
    }

    // ========== toArray ==========
    @Test
    public void testToArrayWithEmptyIterator() {
        String[] result = IteratorUtils.toArray(emptyIterator, String.class);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testToArrayWithSingleElement() {
        String[] result = IteratorUtils.toArray(singleIterator, String.class);
        assertEquals(1, result.length);
        assertEquals("x", result[0]);
    }

    @Test
    public void testToArrayWithMultipleElements() {
        String[] result = IteratorUtils.toArray(multiIterator, String.class);
        assertArrayEquals(list.toArray(new String[0]), result);
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayWithNullIterator() {
        IteratorUtils.toArray(null, String.class);
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayWithNullClass() {
        IteratorUtils.toArray(multiIterator, null);
    }

    // ========== toSet ==========
    @Test
    public void testToSetWithEmptyIterator() {
        Set<String> result = IteratorUtils.toSet(emptyIterator);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testToSetWithSingleElement() {
        Set<String> result = IteratorUtils.toSet(singleIterator);
        assertEquals(1, result.size());
        assertTrue(result.contains("x"));
    }

    @Test
    public void testToSetWithDuplicates() {
        Iterator<String> dupIter = Arrays.asList("a", "a", "b", "b").iterator();
        Set<String> result = IteratorUtils.toSet(dupIter);
        assertEquals(2, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    @Test(expected = NullPointerException.class)
    public void testToSetWithNullIterator() {
        IteratorUtils.toSet(null);
    }

    // ========== toMap ==========
    @Test
    public void testToMapWithEmptyIterator() {
        Map<String, String> result = IteratorUtils.toMap(emptyIterator, s -> s, s -> s);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testToMapWithSingleElement() {
        Map<String, String> result = IteratorUtils.toMap(singleIterator, s -> s, s -> s.toUpperCase());
        assertEquals(1, result.size());
        assertEquals("X", result.get("x"));
    }

    @Test(expected = NullPointerException.class)
    public void testToMapWithNullIterator() {
        IteratorUtils.toMap(null, s -> s, s -> s);
    }

    @Test(expected = NullPointerException.class)
    public void testToMapWithNullKeyMapper() {
        IteratorUtils.toMap(multiIterator, null, s -> s);
    }

    @Test(expected = NullPointerException.class)
    public void testToMapWithNullValueMapper() {
        IteratorUtils.toMap(multiIterator, s -> s, null);
    }

    // ========== filteredIterator ==========
    @Test
    public void testFilteredIteratorWithEmpty() {
        Iterator<String> filtered = IteratorUtils.filteredIterator(emptyIterator, s -> true);
        assertFalse(filtered.hasNext());
    }

    @Test
    public void testFilteredIteratorWithAllPass() {
        Iterator<String> filtered = IteratorUtils.filteredIterator(multiIterator, s -> true);
        List<String> result = IteratorUtils.toList(filtered);
        assertEquals(list, result);
    }

    @Test
    public void testFilteredIteratorWithNonePass() {
        Iterator<String> filtered = IteratorUtils.filteredIterator(multiIterator, s -> false);
        assertFalse(filtered.hasNext());
    }

    @Test
    public void testFilteredIteratorWithSomePass() {
        Iterator<String> filtered = IteratorUtils.filteredIterator(multiIterator, s -> s.equals("a") || s.equals("c"));
        List<String> result = IteratorUtils.toList(filtered);
        assertEquals(Arrays.asList("a", "c"), result);
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredIteratorWithNullIterator() {
        IteratorUtils.filteredIterator(null, s -> true);
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredIteratorWithNullPredicate() {
        IteratorUtils.filteredIterator(multiIterator, null);
    }

    // ========== transformedIterator ==========
    @Test
    public void testTransformedIteratorWithEmpty() {
        Iterator<String> transformed = IteratorUtils.transformedIterator(emptyIterator, s -> s + "!");
        assertFalse(transformed.hasNext());
    }

    @Test
    public void testTransformedIteratorWithSingle() {
        Iterator<String> transformed = IteratorUtils.transformedIterator(singleIterator, s -> s + "!");
        assertEquals("x!", transformed.next());
    }

    @Test
    public void testTransformedIteratorWithMultiple() {
        Iterator<String> transformed = IteratorUtils.transformedIterator(multiIterator, s -> s.toUpperCase());
        List<String> result = IteratorUtils.toList(transformed);
        assertEquals(Arrays.asList("A", "B", "C", "D", "E"), result);
    }

    @Test(expected = NullPointerException.class)
    public void testTransformedIteratorWithNullIterator() {
        IteratorUtils.transformedIterator(null, s -> s);
    }

    @Test(expected = NullPointerException.class)
    public void testTransformedIteratorWithNullTransformer() {
        IteratorUtils.transformedIterator(multiIterator, null);
    }

    // ========== emptyIterator ==========
    @Test
    public void testEmptyIterator() {
        Iterator<String> empty = IteratorUtils.emptyIterator();
        assertNotNull(empty);
        assertFalse(empty.hasNext());
    }

    // ========== singletonIterator ==========
    @Test
    public void testSingletonIterator() {
        Iterator<String> single = IteratorUtils.singletonIterator("test");
        assertTrue(single.hasNext());
        assertEquals("test", single.next());
        assertFalse(single.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testSingletonIteratorWithNull() {
        IteratorUtils.singletonIterator(null);
    }

    // ========== arrayIterator ==========
    @Test
    public void testArrayIteratorWithEmptyArray() {
        Iterator<String> iter = IteratorUtils.arrayIterator(new String[0]);
        assertFalse(iter.hasNext());
    }

    @Test
    public void testArrayIteratorWithSingle() {
        Iterator<String> iter = IteratorUtils.arrayIterator(new String[]{"only"});
        assertTrue(iter.hasNext());
        assertEquals("only", iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testArrayIteratorWithMultiple() {
        Iterator<String> iter = IteratorUtils.arrayIterator(new String[]{"a", "b", "c"});
        List<String> result = IteratorUtils.toList(iter);
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test(expected = NullPointerException.class)
    public void testArrayIteratorWithNullArray() {
        IteratorUtils.arrayIterator(null);
    }

    // ========== asIterable ==========
    @Test
    public void testAsIterableWithEmptyIterator() {
        Iterable<String> iterable = IteratorUtils.asIterable(emptyIterator);
        Iterator<String> iter = iterable.iterator();
        assertFalse(iter.hasNext());
    }

    @Test
    public void testAsIterableWithMultiple() {
        Iterable<String> iterable = IteratorUtils.asIterable(multiIterator);
        List<String> result = new ArrayList<>();
        for (String s : iterable) {
            result.add(s);
        }
        assertEquals(list, result);
    }

    @Test(expected = NullPointerException.class)
    public void testAsIterableWithNullIterator() {
        IteratorUtils.asIterable(null);
    }

    // ========== asEnumeration ==========
    @Test
    public void testAsEnumerationWithEmpty() {
        Enumeration<String> enumeration = IteratorUtils.asEnumeration(emptyIterator);
        assertFalse(enumeration.hasMoreElements());
    }

    @Test
    public void testAsEnumerationWithMultiple() {
        Enumeration<String> enumeration = IteratorUtils.asEnumeration(multiIterator);
        List<String> result = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            result.add(enumeration.nextElement());
        }
        assertEquals(list, result);
    }

    @Test(expected = NullPointerException.class)
    public void testAsEnumerationWithNullIterator() {
        IteratorUtils.asEnumeration(null);
    }

    // ========== forEach ==========
    @Test
    public void testForEachWithEmpty() {
        final List<String> collector = new ArrayList<>();
        IteratorUtils.forEach(emptyIterator, collector::add);
        assertTrue(collector.isEmpty());
    }

    @Test
    public void testForEachWithMultiple() {
        final List<String> collector = new ArrayList<>();
        IteratorUtils.forEach(multiIterator, collector::add);
        assertEquals(list, collector);
    }

    @Test(expected = NullPointerException.class)
    public void testForEachWithNullIterator() {
        IteratorUtils.forEach(null, s -> {});
    }

    @Test(expected = NullPointerException.class)
    public void testForEachWithNullConsumer() {
        IteratorUtils.forEach(multiIterator, null);
    }

    // ========== forEachButLast ==========
    @Test
    public void testForEachButLastWithEmpty() {
        final List<String> collector = new ArrayList<>();
        IteratorUtils.forEachButLast(emptyIterator, collector::add);
        assertTrue(collector.isEmpty());
    }

    @Test
    public void testForEachButLastWithSingle() {
        final List<String> collector = new ArrayList<>();
        IteratorUtils.forEachButLast(singleIterator, collector::add);
        assertTrue(collector.isEmpty());
    }

    @Test
    public void testForEachButLastWithMultiple() {
        final List<String> collector = new ArrayList<>();
        IteratorUtils.forEachButLast(multiIterator, collector::add);
        assertEquals(Arrays.asList("a", "b", "c", "d"), collector);
    }

    @Test(expected = NullPointerException.class)
    public void testForEachButLastWithNullIterator() {
        IteratorUtils.forEachButLast(null, s -> {});
    }

    @Test(expected = NullPointerException.class)
    public void testForEachButLastWithNullConsumer() {
        IteratorUtils.forEachButLast(multiIterator, null);
    }

    // ========== getIterator ==========
    @Test
    public void testGetIteratorFromIterable() {
        Iterable<String> iterable = list;
        Iterator<String> iter = IteratorUtils.getIterator(iterable);
        assertNotNull(iter);
        assertTrue(iter.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testGetIteratorWithNull() {
        IteratorUtils.getIterator(null);
    }

    // ========== loopingIterator ==========
    @Test
    public void testLoopingIteratorWithEmpty() {
        Iterator<String> looping = IteratorUtils.loopingIterator(emptyIterator);
        assertFalse(looping.hasNext());
    }

    @Test
    public void testLoopingIteratorWithSingle() {
        Iterator<String> looping = IteratorUtils.loopingIterator(singleIterator);
        assertTrue(looping.hasNext());
        assertEquals("x", looping.next());
        assertTrue(looping.hasNext());
        assertEquals("x", looping.next());
    }

    @Test
    public void testLoopingIteratorWithMultiple() {
        Iterator<String> looping = IteratorUtils.loopingIterator(multiIterator);
        List<String> firstRound = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            firstRound.add(looping.next());
        }
        assertEquals(list, firstRound);
        List<String> secondRound = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            secondRound.add(looping.next());
        }
        assertEquals(list, secondRound);
    }

    @Test(expected = NullPointerException.class)
    public void testLoopingIteratorWithNullIterator() {
        IteratorUtils.loopingIterator(null);
    }

    // ========== peekingIterator ==========
    @Test
    public void testPeekingIteratorWithEmpty() {
        Iterator<String> peeking = IteratorUtils.peekingIterator(emptyIterator);
        assertFalse(peeking.hasNext());
    }

    @Test
    public void testPeekingIteratorWithSingle() {
        Iterator<String> peeking = IteratorUtils.peekingIterator(singleIterator);
        assertTrue(peeking.hasNext());
        assertEquals("x", peeking.peek());
        assertEquals("x", peeking.next());
        assertFalse(peeking.hasNext());
    }

    @Test
    public void testPeekingIteratorWithMultiple() {
        Iterator<String> peeking = IteratorUtils.peekingIterator(multiIterator);
        assertEquals("a", peeking.peek());
        assertEquals("a", peeking.next());
        assertEquals("b", peeking.peek());
        assertEquals("b", peeking.next());
    }

    @Test(expected = NullPointerException.class)
    public void testPeekingIteratorWithNullIterator() {
        IteratorUtils.peekingIterator(null);
    }

    // ========== pushbackIterator ==========
    @Test
    public void testPushbackIteratorWithEmpty() {
        Iterator<String> pushback = IteratorUtils.pushbackIterator(emptyIterator);
        assertFalse(pushback.hasNext());
    }

    @Test
    public void testPushbackIteratorWithSingle() {
        Iterator<String> pushback = IteratorUtils.pushbackIterator(singleIterator);
        assertTrue(pushback.hasNext());
        String val = pushback.next();
        assertEquals("x", val);
        pushback.pushback(val);
        assertTrue(pushback.hasNext());
        assertEquals("x", pushback.next());
    }

    @Test
    public void testPushbackIteratorWithMultiple() {
        Iterator<String> pushback = IteratorUtils.pushbackIterator(multiIterator);
        assertEquals("a", pushback.next());
        pushback.pushback("a");
        assertEquals("a", pushback.next());
        assertEquals("b", pushback.next());
    }

    @Test(expected = NullPointerException.class)
    public void testPushbackIteratorWithNullIterator() {
        IteratorUtils.pushbackIterator(null);
    }

    // ========== unmodifiableIterator ==========
    @Test
    public void testUnmodifiableIteratorWithEmpty() {
        Iterator<String> unmod = IteratorUtils.unmodifiableIterator(emptyIterator);
        assertFalse(unmod.hasNext());
    }

    @Test
    public void testUnmodifiableIteratorWithSingle() {
        Iterator<String> unmod = IteratorUtils.unmodifiableIterator(singleIterator);
        assertTrue(unmod.hasNext());
        assertEquals("x", unmod.next());
        assertFalse(unmod.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableIteratorRemove() {
        Iterator<String> unmod = IteratorUtils.unmodifiableIterator(multiIterator);
        unmod.next();
        unmod.remove();
    }

    @Test(expected = NullPointerException.class)
    public void testUnmodifiableIteratorWithNullIterator() {
        IteratorUtils.unmodifiableIterator(null);
    }

    // ========== collatedIterator ==========
    @Test
    public void testCollatedIteratorWithEmpty() {
        Iterator<String> collated = IteratorUtils.collatedIterator(null, emptyIterator, emptyIterator);
        assertFalse(collated.hasNext());
    }

    @Test
    public void testCollatedIteratorWithSingle() {
        Iterator<String> collated = IteratorUtils.collatedIterator(null, singleIterator, emptyIterator);
        assertEquals("x", collated.next());
        assertFalse(collated.hasNext());
    }

    @Test
    public void testCollatedIteratorWithTwoSorted() {
        Iterator<String> a = Arrays.asList("a", "c", "e").iterator();
        Iterator<String> b = Arrays.asList("b", "d", "f").iterator();
        Iterator<String> collated = IteratorUtils.collatedIterator(null, a, b);
        List<String> result = IteratorUtils.toList(collated);
        assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f"), result);
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorWithNullIterator() {
        IteratorUtils.collatedIterator(null, null, emptyIterator);
    }

    // ========== nodeListIterator (if applicable) ==========
    // Not implemented in standard IteratorUtils? Skipping.

    // ========== Additional edge cases ==========
    @Test
    public void testToArrayListWithEmpty() {
        ArrayList<String> result = IteratorUtils.toArrayList(emptyIterator);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testToArrayListWithMultiple() {
        ArrayList<String> result = IteratorUtils.toArrayList(multiIterator);
        assertEquals(list, result);
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayListWithNull() {
        IteratorUtils.toArrayList(null);
    }

    // ========== Test for bug 25 specific (if known) ==========
    // Bug 25: toList() returns an unmodifiable list? Let's test modifiability.
    @Test
    public void testToListModifiability() {
        List<String> result = IteratorUtils.toList(multiIterator);
        // Should be modifiable
        result.add("f");
        assertEquals(6, result.size());
        assertTrue(result.contains("f"));
    }

    // Also test that toList returns a list that is serializable? Not required.
}