package org.jsoup.select;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CombiningEvaluatorTest {

    @Test
    public void testAndEvaluatorEmpty() {
        CombiningEvaluator.And and = new CombiningEvaluator.And();
        assertEquals(":andr", and.toString());
        // Evaluating empty should handle gracefully (typically true or depending on implementation)
        // Let's test with a dummy element.
        assertFalse(and.matches(null, null));
    }

    @Test
    public void testAndEvaluatorWithEvaluators() {
        Evaluator eval1 = new Evaluator.Tag("div");
        Evaluator eval2 = new Evaluator.Id("test");
        
        CombiningEvaluator.And and = new CombiningEvaluator.And(Arrays.asList(eval1, eval2));
        assertEquals(":div#test", and.toString());

        CombiningEvaluator.And andVarargs = new CombiningEvaluator.And(eval1, eval2);
        assertEquals(":div#test", andVarargs.toString());
    }

    @Test
    public void testOrEvaluatorEmpty() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        assertEquals(":or", or.toString());
        assertFalse(or.matches(null, null));
    }

    @Test
    public void testOrEvaluatorWithEvaluators() {
        Evaluator eval1 = new Evaluator.Tag("div");
        Evaluator eval2 = new Evaluator.Tag("span");

        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(eval1, eval2));
        // toString check or behavior check
        assertNotNull(or.toString());
    }

    @Test
    public void testOrEvaluatorAdd() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or();
        Evaluator eval1 = new Evaluator.Tag("div");
        or.add(eval1);
        
        List<Evaluator> evaluators = new ArrayList<Evaluator>();
        evaluators.add(new Evaluator.Tag("span"));
        
        CombiningEvaluator.Or or2 = new CombiningEvaluator.Or(evaluators);
        // Exercise right-most evaluator retrieval if applicable
        Evaluator rightMost = or2.rightMostEvaluator();
        assertNotNull(rightMost);
    }
}