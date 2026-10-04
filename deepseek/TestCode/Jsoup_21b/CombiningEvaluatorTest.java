package org.jsoup.select;

import org.junit.Before;
import org.junit.Test;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import static org.junit.Assert.*;

public class CombiningEvaluatorTest {

    private Element element;
    private Elements elements;

    @Before
    public void setUp() {
        element = new Element(Tag.valueOf("div"), "");
        elements = new Elements();
    }

    // Helper evaluator that returns a fixed boolean
    private static class FixedEvaluator extends Evaluator {
        private final boolean result;

        FixedEvaluator(boolean result) {
            this.result = result;
        }

        @Override
        public boolean evaluate(Element element, Elements elements) {
            return result;
        }
    }

    // ---------- And evaluator tests ----------

    @Test
    public void testAndEmptyList() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        // Bug: empty And should return false, but buggy version returns true
        assertFalse("Empty And should return false", and.evaluate(element, elements));
    }

    @Test
    public void testAndSingleTrue() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        and.updateEvaluators(new FixedEvaluator(true));
        assertTrue("Single true evaluator should return true", and.evaluate(element, elements));
    }

    @Test
    public void testAndSingleFalse() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        and.updateEvaluators(new FixedEvaluator(false));
        assertFalse("Single false evaluator should return false", and.evaluate(element, elements));
    }

    @Test
    public void testAndAllTrue() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        and.updateEvaluators(new FixedEvaluator(true));
        and.updateEvaluators(new FixedEvaluator(true));
        assertTrue("All true evaluators should return true", and.evaluate(element, elements));
    }

    @Test
    public void testAndOneFalseAmongTrue() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        and.updateEvaluators(new FixedEvaluator(true));
        and.updateEvaluators(new FixedEvaluator(false));
        and.updateEvaluators(new FixedEvaluator(true));
        assertFalse("One false among true should return false", and.evaluate(element, elements));
    }

    // ---------- Or evaluator tests ----------

    @Test
    public void testOrEmptyList() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        // Empty Or should return false (no condition satisfied)
        assertFalse("Empty Or should return false", or.evaluate(element, elements));
    }

    @Test
    public void testOrSingleTrue() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        or.updateEvaluators(new FixedEvaluator(true));
        assertTrue("Single true evaluator should return true", or.evaluate(element, elements));
    }

    @Test
    public void testOrSingleFalse() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        or.updateEvaluators(new FixedEvaluator(false));
        assertFalse("Single false evaluator should return false", or.evaluate(element, elements));
    }

    @Test
    public void testOrAllFalse() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        or.updateEvaluators(new FixedEvaluator(false));
        or.updateEvaluators(new FixedEvaluator(false));
        assertFalse("All false evaluators should return false", or.evaluate(element, elements));
    }

    @Test
    public void testOrAtLeastOneTrue() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        or.updateEvaluators(new FixedEvaluator(false));
        or.updateEvaluators(new FixedEvaluator(true));
        or.updateEvaluators(new FixedEvaluator(false));
        assertTrue("At least one true evaluator should return true", or.evaluate(element, elements));
    }

    // ---------- rightMostEvaluator tests ----------

    @Test
    public void testRightMostEvaluatorEmpty() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        assertNull("Empty list should return null", and.rightMostEvaluator());
    }

    @Test
    public void testRightMostEvaluatorSingle() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        Evaluator eval = new FixedEvaluator(true);
        and.updateEvaluators(eval);
        assertSame("Single evaluator should be returned", eval, and.rightMostEvaluator());
    }

    @Test
    public void testRightMostEvaluatorMultiple() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        Evaluator first = new FixedEvaluator(true);
        Evaluator second = new FixedEvaluator(false);
        and.updateEvaluators(first);
        and.updateEvaluators(second);
        assertSame("Last evaluator should be returned", second, and.rightMostEvaluator());
    }

    // ---------- updateEvaluators tests ----------

    @Test
    public void testUpdateEvaluatorsAddsToEmptyList() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        Evaluator eval = new FixedEvaluator(true);
        and.updateEvaluators(eval);
        assertEquals("Should contain one evaluator", 1, and.evaluators.size());
        assertSame("Evaluator should be the added one", eval, and.evaluators.get(0));
    }

    @Test
    public void testUpdateEvaluatorsAddsMultiple() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        Evaluator eval1 = new FixedEvaluator(true);
        Evaluator eval2 = new FixedEvaluator(false);
        and.updateEvaluators(eval1);
        and.updateEvaluators(eval2);
        assertEquals("Should contain two evaluators", 2, and.evaluators.size());
        assertSame("First evaluator should be eval1", eval1, and.evaluators.get(0));
        assertSame("Second evaluator should be eval2", eval2, and.evaluators.get(1));
    }

    // ---------- Edge case: null element/elements ----------

    @Test(expected = NullPointerException.class)
    public void testAndEvaluateNullElement() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        and.updateEvaluators(new FixedEvaluator(true));
        and.evaluate(null, elements);
    }

    @Test(expected = NullPointerException.class)
    public void testAndEvaluateNullElements() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        and.updateEvaluators(new FixedEvaluator(true));
        and.evaluate(element, null);
    }

    @Test(expected = NullPointerException.class)
    public void testOrEvaluateNullElement() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        or.updateEvaluators(new FixedEvaluator(true));
        or.evaluate(null, elements);
    }

    @Test(expected = NullPointerException.class)
    public void testOrEvaluateNullElements() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        or.updateEvaluators(new FixedEvaluator(true));
        or.evaluate(element, null);
    }
}