package org.apache.commons.jelly.expression.jexl;

import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.expression.jexl.JexlExpression;
import org.apache.commons.jelly.expression.jexl.JexlExpressionFactory;
import org.apache.commons.jelly.JellyContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonPointerTest {

    @Test
    public void testJsonPointerExpressionEvaluation() throws Exception {
        JellyContext context = new JellyContext();
        context.setVariable("foo", "bar");
        
        ExpressionFactory factory = new JexlExpressionFactory();
        Expression expr = factory.createExpression("foo");
        
        Object result = expr.evaluate(context);
        assertEquals("bar", result);
    }
}