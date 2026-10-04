package org.apache.commons.collections.functors;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.NullIsExceptionPredicate;
import org.apache.commons.collections.functors.NullIsFalsePredicate;
import org.apache.commons.collections.functors.NullIsTruePredicate;

public class EqualPredicateTest {

    @Test
    public void testEqualPredicateFactoryWithNull() {
        Predicate predicate = EqualPredicate.equalPredicate(null);
        assertTrue(predicate instanceof NullIsExceptionPredicate);
    }

    @Test
    public void testEqualPredicateFactoryWithNonNull() {
        String testValue = "test";
        Predicate predicate = EqualPredicate.equalPredicate(testValue);
        assertTrue(predicate instanceof EqualPredicate);
        
        EqualPredicate eqPred = (EqualPredicate) predicate;
        assertNotNull(eqPred.getValue());
        assertEquals(testValue, eqPred.getValue());
    }

    @Test
    public void testEqualPredicateFactoryWithNullAndEquator() {
        Predicate predicate = EqualPredicate.equalPredicate(null, null);
        assertTrue(predicate instanceof NullIsExceptionPredicate);
    }

    @Test
    public void testEqualPredicateWithEquator() {
        String testValue = "test";
        org.apache.commons.collections.Equator<String> equator = new org.apache.commons.collections.Equator<String>() {
            public boolean equate(String o1, String o2) {
                if (o1 == null || o2 == null) {
                    return o1 == o2;
                }
                return o1.equalsIgnoreCase(o2);
            }
            public int hash(String o) {
                return o == null ? 0 : o.toLowerCase().hashCode();
            }
        };

        Predicate predicate = EqualPredicate.equalPredicate(testValue, equator);
        assertTrue(predicate instanceof EqualPredicate);
        
        assertTrue(predicate.evaluate("TEST"));
        assertFalse(predicate.evaluate("other"));
        assertFalse(predicate.evaluate(null));
    }

    @Test
    public void testEvaluateWithNullValueAndStandardEquator() {
        // If value is null, factory returns NullIsExceptionPredicate, 
        // but we can test EqualPredicate directly if we bypass the factory or test evaluate logic.
        // Let's test standard EqualPredicate evaluation with null values.
        EqualPredicate pred = new EqualPredicate(null);
        
        // Note: Standard Equator uses standard .equals(). If value is null, null.equals(input) might throw NPE 
        // or return false depending on implementation. Let's check how DefaultEquator handles it.
        // Actually, DefaultEquator.INSTANCE.equate(null, null) returns true.
        assertFalse(pred.evaluate("notNull"));
    }

    @Test
    public void testEvaluateStandard() {
        EqualPredicate pred = new EqualPredicate("hello");
        assertTrue(pred.evaluate("hello"));
        assertFalse(pred.evaluate("world"));
        assertFalse(pred.evaluate(null));
    }

    @Test
    public void testEvaluateWithCustomEquator() {
        org.apache.commons.collections.Equator<Integer> equator = new org.apache.commons.collections.Equator<Integer>() {
            public boolean equate(Integer o1, Integer o2) {
                if (o1 == null || o2 == null) return false;
                return o1.intValue() % 2 == o2.intValue() % 2;
            }
            public int hash(Integer o) {
                return o == null ? 0 : o % 2;
            }
        };

        EqualPredicate pred = new EqualPredicate(2, equator);
        assertTrue(pred.evaluate(4)); // Both even
        assertFalse(pred.evaluate(3)); // Even vs Odd
    }
}