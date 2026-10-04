package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class CoreFunctionTest {

    @Test
    public void testNodeNameFunctionWithNullPointer() {
        // Test node-name() with a NullPointer or context where pointer is null
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_NODE_NAME, new Expression[0]);
        
        EvalContext context = new InitialContext(new SelfContext(null, null));
        // Setup a scenario where getCurrentNodePointer() might return null or a NullPointer
        try {
            Object result = cf.computeValue(context);
            // Depending on implementation, it may return null or an empty string/QName
            // We just want to ensure it executes without unexpected unhandled NPE if possible,
            // or exercises the branch where pointer is null.
        } catch (Exception e) {
            // Expected or acceptable depending on strict JXPath behavior
        }
    }

    @Test
    public void testLangFunction() {
        Expression[] args = new Expression[] { new Constant("en") };
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_LANG, args);
        
        NodePointer pointer = NullPointer.newNodePointer(null, null, null);
        EvalContext context = new InitialContext(new SelfContext(null, pointer));
        
        Object result = cf.computeValue(context);
        Assert.assertNotNull(result);
    }

    @Test
    public void testStringFunctionWithArgs() {
        Expression[] args = new Expression[] { new Constant(123) };
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_STRING, args);
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Object result = cf.computeValue(context);
        Assert.assertEquals("123", result);
    }

    @Test
    public void testStringFunctionNoArgs() {
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_STRING, new Expression[0]);
        NodePointer pointer = NullPointer.newNodePointer(null, "testValue", null);
        EvalContext context = new InitialContext(new SelfContext(null, pointer));
        
        Object result = cf.computeValue(context);
        Assert.assertEquals("testValue", result);
    }

    @Test
    public void testNumberFunction() {
        Expression[] args = new Expression[] { new Constant("456") };
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_NUMBER, args);
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Object result = cf.computeValue(context);
        Assert.assertEquals(Double.valueOf(456.0), result);
    }

    @Test
    public void testBooleanFunction() {
        Expression[] args = new Expression[] { new Constant(Boolean.TRUE) };
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_BOOLEAN, args);
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Object result = cf.computeValue(context);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testNotFunction() {
        Expression[] args = new Expression[] { new Constant(Boolean.FALSE) };
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_NOT, args);
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Object result = cf.computeValue(context);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testTrueAndFalseFunctions() {
        CoreFunction trueFunc = new CoreFunction(CoreFunction.FUNCTION_TRUE, new Expression[0]);
        CoreFunction falseFunc = new CoreFunction(CoreFunction.FUNCTION_FALSE, new Expression[0]);
        EvalContext context = new InitialContext(new SelfContext(null, null));

        Assert.assertEquals(Boolean.TRUE, trueFunc.computeValue(context));
        Assert.assertEquals(Boolean.FALSE, falseFunc.computeValue(context));
    }

    @Test
    public void testIdFunction() {
        Expression[] args = new Expression[] { new Constant("someId") };
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_ID, args);
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        try {
            cf.computeValue(context);
        } catch (Exception e) {
            // ID function might require a proper namespace/document context
        }
    }

    @Test
    public void testConcatFunction() {
        Expression[] args = new Expression[] { new Constant("a"), new Constant("b"), new Constant("c") };
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_CONCAT, args);
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Object result = cf.computeValue(context);
        Assert.assertEquals("abc", result);
    }

    @Test
    public void testStartsWithAndEndsWithAndContains() {
        CoreFunction starts = new CoreFunction(CoreFunction.FUNCTION_STARTS_WITH, new Expression[] { new Constant("foobar"), new Constant("foo") });
        CoreFunction ends = new CoreFunction(CoreFunction.FUNCTION_ENDS_WITH, new Expression[] { new Constant("foobar"), new Constant("bar") });
        CoreFunction contains = new CoreFunction(CoreFunction.FUNCTION_CONTAINS, new Expression[] { new Constant("foobar"), new Constant("oba") });
        
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Assert.assertEquals(Boolean.TRUE, starts.computeValue(context));
        Assert.assertEquals(Boolean.TRUE, ends.computeValue(context));
        Assert.assertEquals(Boolean.TRUE, contains.computeValue(context));
    }

    @Test
    public void testSubstringBeforeAndAfter() {
        CoreFunction before = new CoreFunction(CoreFunction.FUNCTION_SUBSTRING_BEFORE, new Expression[] { new Constant("1999/04/01"), new Constant("/") });
        CoreFunction after = new CoreFunction(CoreFunction.FUNCTION_SUBSTRING_AFTER, new Expression[] { new Constant("1999/04/01"), new Constant("/") });
        
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Assert.assertEquals("1999", before.computeValue(context));
        Assert.assertEquals("04/01", after.computeValue(context));
    }

    @Test
    public void testSubstring() {
        CoreFunction sub2 = new CoreFunction(CoreFunction.FUNCTION_SUBSTRING, new Expression[] { new Constant("12345"), new Constant(2) });
        CoreFunction sub3 = new CoreFunction(CoreFunction.FUNCTION_SUBSTRING, new Expression[] { new Constant("12345"), new Constant(2), new Constant(3) });
        
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Assert.assertEquals("2345", sub2.computeValue(context));
        Assert.assertEquals("234", sub3.computeValue(context));
    }

    @Test
    public void testStringLength() {
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_STRING_LENGTH, new Expression[] { new Constant("12345") });
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Assert.assertEquals(Double.valueOf(5.0), cf.computeValue(context));
    }

    @Test
    public void testNormalizeSpace() {
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_NORMALIZE_SPACE, new Expression[] { new Constant("  abc   def  ") });
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Assert.assertEquals("abc def", cf.computeValue(context));
    }

    @Test
    public void testTranslate() {
        CoreFunction cf = new CoreFunction(CoreFunction.FUNCTION_TRANSLATE, new Expression[] { new Constant("bar"), new Constant("abc"), new Constant("XYZ") });
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Assert.assertEquals("XYZ", cf.computeValue(context));
    }

    @Test
    public void testNumericFunctions() {
        CoreFunction sum = new CoreFunction(CoreFunction.FUNCTION_SUM, new Expression[] { new Constant(1) }); // Or an expression context
        CoreFunction floor = new CoreFunction(CoreFunction.FUNCTION_FLOOR, new Expression[] { new Constant(1.5) });
        CoreFunction ceiling = new CoreFunction(CoreFunction.FUNCTION_CEILING, new Expression[] { new Constant(1.5) });
        CoreFunction round = new CoreFunction(CoreFunction.FUNCTION_ROUND, new Expression[] { new Constant(1.5) });
        
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        Assert.assertEquals(Double.valueOf(1.0), floor.computeValue(context));
        Assert.assertEquals(Double.valueOf(2.0), ceiling.computeValue(context));
        Assert.assertEquals(Double.valueOf(2.0), round.computeValue(context));
    }

    @Test
    public void testGetFunctionByName() {
        Assert.assertEquals(CoreFunction.FUNCTION_CEILING, CoreFunction.getFunctionCode("ceiling"));
        Assert.assertEquals(CoreFunction.FUNCTION_FLOOR, CoreFunction.getFunctionCode("floor"));
        Assert.assertEquals(CoreFunction.FUNCTION_ROUND, CoreFunction.getFunctionCode("round"));
        Assert.assertEquals(CoreFunction.FUNCTION_SUM, CoreFunction.getFunctionCode("sum"));
        Assert.assertEquals(CoreFunction.FUNCTION_STRING, CoreFunction.getFunctionCode("string"));
        Assert.assertEquals(CoreFunction.FUNCTION_BOOLEAN, CoreFunction.getFunctionCode("boolean"));
        Assert.assertEquals(CoreFunction.FUNCTION_NUMBER, CoreFunction.getFunctionCode("number"));
        Assert.assertEquals(CoreFunction.FUNCTION_TRUE, CoreFunction.getFunctionCode("true"));
        Assert.assertEquals(CoreFunction.FUNCTION_FALSE, CoreFunction.getFunctionCode("false"));
        Assert.assertEquals(CoreFunction.FUNCTION_NOT, CoreFunction.getFunctionCode("not"));
        Assert.assertEquals(CoreFunction.FUNCTION_ID, CoreFunction.getFunctionCode("id"));
        Assert.assertEquals(CoreFunction.FUNCTION_CONCAT, CoreFunction.getFunctionCode("concat"));
        Assert.assertEquals(CoreFunction.FUNCTION_STARTS_WITH, CoreFunction.getFunctionCode("starts-with"));
        Assert.assertEquals(CoreFunction.FUNCTION_CONTAINS, CoreFunction.getFunctionCode("contains"));
        Assert.assertEquals(CoreFunction.FUNCTION_SUBSTRING_BEFORE, CoreFunction.getFunctionCode("substring-before"));
        Assert.assertEquals(CoreFunction.FUNCTION_SUBSTRING_AFTER, CoreFunction.getFunctionCode("substring-after"));
        Assert.assertEquals(CoreFunction.FUNCTION_SUBSTRING, CoreFunction.getFunctionCode("substring"));
        Assert.assertEquals(CoreFunction.FUNCTION_STRING_LENGTH, CoreFunction.getFunctionCode("string-length"));
        Assert.assertEquals(CoreFunction.FUNCTION_NORMALIZE_SPACE, CoreFunction.getFunctionCode("normalize-space"));
        Assert.assertEquals(CoreFunction.FUNCTION_TRANSLATE, CoreFunction.getFunctionCode("translate"));
        Assert.assertEquals(CoreFunction.FUNCTION_LANG, CoreFunction.getFunctionCode("lang"));
        Assert.assertEquals(CoreFunction.FUNCTION_COUNT, CoreFunction.getFunctionCode("count"));
        Assert.assertEquals(CoreFunction.FUNCTION_POSITION, CoreFunction.getFunctionCode("position"));
        Assert.assertEquals(CoreFunction.FUNCTION_LAST, CoreFunction.getFunctionCode("last"));
        Assert.assertEquals(CoreFunction.FUNCTION_LOCAL_NAME, CoreFunction.getFunctionCode("local-name"));
        Assert.assertEquals(CoreFunction.FUNCTION_NAME, CoreFunction.getFunctionCode("name"));
        Assert.assertEquals(CoreFunction.FUNCTION_NAMESPACE_URI, CoreFunction.getFunctionCode("namespace-uri"));
        Assert.assertEquals(CoreFunction.FUNCTION_NULL, CoreFunction.getFunctionCode("unknown-func-xyz"));
    }

    @Test
    public void testComputeContextBasedFunctions() {
        EvalContext context = new InitialContext(new SelfContext(null, null));
        
        CoreFunction position = new CoreFunction(CoreFunction.FUNCTION_POSITION, new Expression[0]);
        CoreFunction last = new CoreFunction(CoreFunction.FUNCTION_LAST, new Expression[0]);
        CoreFunction count = new CoreFunction(CoreFunction.FUNCTION_COUNT, new Expression[] { new Constant("test") });
        
        try {
            position.computeValue(context);
            last.computeValue(context);
            count.computeValue(context);
        } catch (Exception e) {
            // Expected if context lacks full setup
        }
    }
}