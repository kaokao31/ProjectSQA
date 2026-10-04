package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JUnit 4 test suite for CodeGenerator, targeting maximum coverage and fault detection.
 * Designed for Closure Compiler Bug 157 context.
 */
public class CodeGeneratorTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Use default options for code generation
        options.setPrettyPrint(false);
        options.setLineBreak(false);
        options.setEmitUseStrict(false);
    }

    // Helper to generate code from a node
    private String generateCode(Node node) {
        return CodeGenerator.generateCode(node, options, null);
    }

    // Helper to parse a JavaScript snippet and return the AST root
    private Node parseScript(String js) {
        SourceFile input = SourceFile.fromCode("test.js", js);
        compiler.initOptions(options);
        compiler.parse(input);
        return compiler.getRoot();
    }

    // ==================== Basic Literals ====================

    @Test
    public void testNumberLiteral() {
        Node node = IR.number(42.5);
        assertEquals("42.5", generateCode(node));
    }

    @Test
    public void testStringLiteral() {
        Node node = IR.string("hello");
        assertEquals("\"hello\"", generateCode(node));
    }

    @Test
    public void testBooleanLiteral() {
        Node node = IR.trueNode();
        assertEquals("true", generateCode(node));
        node = IR.falseNode();
        assertEquals("false", generateCode(node));
    }

    @Test
    public void testNullLiteral() {
        Node node = IR.nullNode();
        assertEquals("null", generateCode(node));
    }

    @Test
    public void testUndefinedLiteral() {
        Node node = IR.voidNode(IR.number(0));
        assertEquals("void 0", generateCode(node));
    }

    // ==================== Unary Operators ====================

    @Test
    public void testUnaryNegation() {
        Node node = IR.neg(IR.number(5));
        assertEquals("-5", generateCode(node));
    }

    @Test
    public void testUnaryNot() {
        Node node = IR.not(IR.trueNode());
        assertEquals("!true", generateCode(node));
    }

    @Test
    public void testUnaryBitwiseNot() {
        Node node = IR.bitwiseNot(IR.number(1));
        assertEquals("~1", generateCode(node));
    }

    @Test
    public void testUnaryTypeOf() {
        Node node = IR.typeOf(IR.string("test"));
        assertEquals("typeof \"test\"", generateCode(node));
    }

    @Test
    public void testUnaryVoid() {
        Node node = IR.voidNode(IR.number(0));
        assertEquals("void 0", generateCode(node));
    }

    @Test
    public void testUnaryDelete() {
        Node node = IR.delete(IR.name("x"));
        assertEquals("delete x", generateCode(node));
    }

    // ==================== Binary Operators ====================

    @Test
    public void testAddition() {
        Node node = IR.add(IR.number(1), IR.number(2));
        assertEquals("1+2", generateCode(node));
    }

    @Test
    public void testSubtraction() {
        Node node = IR.sub(IR.number(5), IR.number(3));
        assertEquals("5-3", generateCode(node));
    }

    @Test
    public void testMultiplication() {
        Node node = IR.mul(IR.number(4), IR.number(3));
        assertEquals("4*3", generateCode(node));
    }

    @Test
    public void testDivision() {
        Node node = IR.div(IR.number(10), IR.number(2));
        assertEquals("10/2", generateCode(node));
    }

    @Test
    public void testModulus() {
        Node node = IR.mod(IR.number(10), IR.number(3));
        assertEquals("10%3", generateCode(node));
    }

    @Test
    public void testExponentiation() {
        Node node = IR.exponent(IR.number(2), IR.number(3));
        assertEquals("2**3", generateCode(node));
    }

    @Test
    public void testLogicalAnd() {
        Node node = IR.and(IR.trueNode(), IR.falseNode());
        assertEquals("true&&false", generateCode(node));
    }

    @Test
    public void testLogicalOr() {
        Node node = IR.or(IR.falseNode(), IR.trueNode());
        assertEquals("false||true", generateCode(node));
    }

    @Test
    public void testBitwiseAnd() {
        Node node = IR.bitAnd(IR.number(5), IR.number(3));
        assertEquals("5&3", generateCode(node));
    }

    @Test
    public void testBitwiseOr() {
        Node node = IR.bitOr(IR.number(5), IR.number(3));
        assertEquals("5|3", generateCode(node));
    }

    @Test
    public void testBitwiseXor() {
        Node node = IR.bitXor(IR.number(5), IR.number(3));
        assertEquals("5^3", generateCode(node));
    }

    @Test
    public void testLeftShift() {
        Node node = IR.lsh(IR.number(1), IR.number(2));
        assertEquals("1<<2", generateCode(node));
    }

    @Test
    public void testRightShift() {
        Node node = IR.rsh(IR.number(8), IR.number(1));
        assertEquals("8>>1", generateCode(node));
    }

    @Test
    public void testUnsignedRightShift() {
        Node node = IR.ursh(IR.number(-1), IR.number(1));
        assertEquals("-1>>>1", generateCode(node));
    }

    @Test
    public void testEquality() {
        Node node = IR.eq(IR.number(1), IR.number(1));
        assertEquals("1==1", generateCode(node));
    }

    @Test
    public void testInequality() {
        Node node = IR.ne(IR.number(1), IR.number(2));
        assertEquals("1!=2", generateCode(node));
    }

    @Test
    public void testStrictEquality() {
        Node node = IR.sheq(IR.number(1), IR.number(1));
        assertEquals("1===1", generateCode(node));
    }

    @Test
    public void testStrictInequality() {
        Node node = IR.shne(IR.number(1), IR.number(2));
        assertEquals("1!==2", generateCode(node));
    }

    @Test
    public void testLessThan() {
        Node node = IR.lt(IR.number(1), IR.number(2));
        assertEquals("1<2", generateCode(node));
    }

    @Test
    public void testGreaterThan() {
        Node node = IR.gt(IR.number(2), IR.number(1));
        assertEquals("2>1", generateCode(node));
    }

    @Test
    public void testLessThanOrEqual() {
        Node node = IR.le(IR.number(1), IR.number(2));
        assertEquals("1<=2", generateCode(node));
    }

    @Test
    public void testGreaterThanOrEqual() {
        Node node = IR.ge(IR.number(2), IR.number(1));
        assertEquals("2>=1", generateCode(node));
    }

    @Test
    public void testInOperator() {
        Node node = IR.in(IR.string("x"), IR.objectlit());
        assertEquals("\"x\" in {}", generateCode(node));
    }

    @Test
    public void testInstanceOf() {
        Node node = IR.instanceOf(IR.name("obj"), IR.name("Array"));
        assertEquals("obj instanceof Array", generateCode(node));
    }

    // ==================== Assignment Operators ====================

    @Test
    public void testSimpleAssignment() {
        Node node = IR.assign(IR.name("x"), IR.number(5));
        assertEquals("x=5", generateCode(node));
    }

    @Test
    public void testCompoundAssignment() {
        Node node = IR.assignAdd(IR.name("x"), IR.number(1));
        assertEquals("x+=1", generateCode(node));
    }

    // ==================== Conditional (Ternary) ====================

    @Test
    public void testConditional() {
        Node node = IR.hook(IR.trueNode(), IR.number(1), IR.number(2));
        assertEquals("true?1:2", generateCode(node));
    }

    // ==================== Function Expressions ====================

    @Test
    public void testFunctionExpression() {
        Node param = IR.name("a");
        Node body = IR.block(IR.returnNode(IR.name("a")));
        Node func = IR.function(IR.name(""), IR.paramList(param), body);
        assertEquals("function(a){return a}", generateCode(func));
    }

    @Test
    public void testFunctionWithMultipleParams() {
        Node param1 = IR.name("x");
        Node param2 = IR.name("y");
        Node body = IR.block(IR.returnNode(IR.add(IR.name("x"), IR.name("y"))));
        Node func = IR.function(IR.name(""), IR.paramList(param1, param2), body);
        assertEquals("function(x,y){return x+y}", generateCode(func));
    }

    @Test
    public void testNamedFunctionExpression() {
        Node param = IR.name("n");
        Node body = IR.block(IR.returnNode(IR.mul(IR.name("n"), IR.number(2))));
        Node func = IR.function(IR.name("double"), IR.paramList(param), body);
        assertEquals("function double(n){return n*2}", generateCode(func));
    }

    // ==================== Arrow Functions (if supported) ====================

    @Test
    public void testArrowFunction() {
        Node param = IR.name("x");
        Node body = IR.add(IR.name("x"), IR.number(1));
        Node arrow = IR.arrowFunction(IR.paramList(param), body);
        assertEquals("(x)=>x+1", generateCode(arrow));
    }

    // ==================== Object Literals ====================

    @Test
    public void testEmptyObjectLiteral() {
        Node obj = IR.objectlit();
        assertEquals("{}", generateCode(obj));
    }

    @Test
    public void testObjectLiteralWithProperties() {
        Node key1 = IR.stringKey("a");
        Node val1 = IR.number(1);
        Node prop1 = IR.propDef(key1, val1);
        Node key2 = IR.stringKey("b");
        Node val2 = IR.number(2);
        Node prop2 = IR.propDef(key2, val2);
        Node obj = IR.objectlit(prop1, prop2);
        assertEquals("{a:1,b:2}", generateCode(obj));
    }

    @Test
    public void testObjectLiteralWithGetter() {
        Node getter = IR.getter(IR.stringKey("x"), IR.function(IR.name(""), IR.paramList(), IR.block(IR.returnNode(IR.number(42)))));
        Node obj = IR.objectlit(getter);
        assertEquals("{get x(){return 42}}", generateCode(obj));
    }

    @Test
    public void testObjectLiteralWithSetter() {
        Node setter = IR.setter(IR.stringKey("y"), IR.function(IR.name(""), IR.paramList(IR.name("v")), IR.block(IR.assign(IR.name("this.y"), IR.name("v")))));
        Node obj = IR.objectlit(setter);
        assertEquals("{set y(v){this.y=v}}", generateCode(obj));
    }

    // ==================== Array Literals ====================

    @Test
    public void testEmptyArrayLiteral() {
        Node arr = IR.arraylit();
        assertEquals("[]", generateCode(arr));
    }

    @Test
    public void testArrayLiteralWithElements() {
        Node arr = IR.arraylit(IR.number(1), IR.number(2), IR.number(3));
        assertEquals("[1,2,3]", generateCode(arr));
    }

    @Test
    public void testArrayLiteralWithHoles() {
        Node arr = IR.arraylit(IR.number(1), IR.empty(), IR.number(3));
        assertEquals("[1,,3]", generateCode(arr));
    }

    // ==================== Member Access ====================

    @Test
    public void testDotAccess() {
        Node node = IR.getprop(IR.name("obj"), IR.string("prop"));
        assertEquals("obj.prop", generateCode(node));
    }

    @Test
    public void testBracketAccess() {
        Node node = IR.getelem(IR.name("arr"), IR.number(0));
        assertEquals("arr[0]", generateCode(node));
    }

    // ==================== Function Calls ====================

    @Test
    public void testSimpleCall() {
        Node node = IR.call(IR.name("foo"), IR.number(1));
        assertEquals("foo(1)", generateCode(node));
    }

    @Test
    public void testMethodCall() {
        Node node = IR.call(IR.getprop(IR.name("obj"), IR.string("method")), IR.number(1));
        assertEquals("obj.method(1)", generateCode(node));
    }

    @Test
    public void testNewExpression() {
        Node node = IR.newNode(IR.name("Array"), IR.number(10));
        assertEquals("new Array(10)", generateCode(node));
    }

    // ==================== Control Flow ====================

    @Test
    public void testIfStatement() {
        Node cond = IR.trueNode();
        Node thenBlock = IR.block(IR.returnNode(IR.number(1)));
        Node ifNode = IR.ifNode(cond, thenBlock);
        assertEquals("if(true){return 1}", generateCode(ifNode));
    }

    @Test
    public void testIfElseStatement() {
        Node cond = IR.falseNode();
        Node thenBlock = IR.block(IR.returnNode(IR.number(1)));
        Node elseBlock = IR.block(IR.returnNode(IR.number(2)));
        Node ifNode = IR.ifNode(cond, thenBlock, elseBlock);
        assertEquals("if(false){return 1}else{return 2}", generateCode(ifNode));
    }

    @Test
    public void testWhileLoop() {
        Node cond = IR.trueNode();
        Node body = IR.block(IR.breakNode());
        Node whileNode = IR.whileNode(cond, body);
        assertEquals("while(true){break}", generateCode(whileNode));
    }

    @Test
    public void testDoWhileLoop() {
        Node body = IR.block(IR.breakNode());
        Node cond = IR.falseNode();
        Node doNode = IR.doNode(body, cond);
        assertEquals("do{break}while(false)", generateCode(doNode));
    }

    @Test
    public void testForLoop() {
        Node init = IR.assign(IR.name("i"), IR.number(0));
        Node cond = IR.lt(IR.name("i"), IR.number(10));
        Node incr = IR.assignAdd(IR.name("i"), IR.number(1));
        Node body = IR.block(IR.empty());
        Node forNode = IR.forNode(init, cond, incr, body);
        assertEquals("for(i=0;i<10;i+=1){}", generateCode(forNode));
    }

    @Test
    public void testForInLoop() {
        Node var = IR.name("key");
        Node obj = IR.name("obj");
        Node body = IR.block(IR.empty());
        Node forIn = IR.forInNode(var, obj, body);
        assertEquals("for(key in obj){}", generateCode(forIn));
    }

    @Test
    public void testSwitchStatement() {
        Node expr = IR.number(1);
        Node case1 = IR.caseNode(IR.number(1), IR.block(IR.breakNode()));
        Node case2 = IR.caseNode(IR.number(2), IR.block(IR.breakNode()));
        Node defaultCase = IR.defaultCase(IR.block(IR.empty()));
        Node switchNode = IR.switchNode(expr, case1, case2, defaultCase);
        assertEquals("switch(1){case 1:break;case 2:break;default:}", generateCode(switchNode));
    }

    @Test
    public void testTryCatchFinally() {
        Node tryBlock = IR.block(IR.throwNode(IR.string("error")));
        Node catchVar = IR.name("e");
        Node catchBlock = IR.block(IR.empty());
        Node catchNode = IR.catchNode(catchVar, catchBlock);
        Node finallyBlock = IR.block(IR.empty());
        Node tryNode = IR.tryNode(tryBlock, catchNode, finallyBlock);
        assertEquals("try{throw\"error\"}catch(e){}finally{}", generateCode(tryNode));
    }

    // ==================== Variable Declarations ====================

    @Test
    public void testVarDeclaration() {
        Node var = IR.var(IR.name("x"), IR.number(5));
        assertEquals("var x=5", generateCode(var));
    }

    @Test
    public void testVarDeclarationNoInit() {
        Node var = IR.var(IR.name("y"));
        assertEquals("var y", generateCode(var));
    }

    @Test
    public void testLetDeclaration() {
        Node let = IR.let(IR.name("z"), IR.number(10));
        assertEquals("let z=10", generateCode(let));
    }

    @Test
    public void testConstDeclaration() {
        Node constDecl = IR.constNode(IR.name("c"), IR.number(100));
        assertEquals("const c=100", generateCode(constDecl));
    }

    // ==================== Return, Throw, Break, Continue ====================

    @Test
    public void testReturnWithValue() {
        Node ret = IR.returnNode(IR.number(42));
        assertEquals("return 42", generateCode(ret));
    }

    @Test
    public void testReturnWithoutValue() {
        Node ret = IR.returnNode();
        assertEquals("return", generateCode(ret));
    }

    @Test
    public void testThrow() {
        Node throwNode = IR.throwNode(IR.string("error"));
        assertEquals("throw\"error\"", generateCode(throwNode));
    }

    @Test
    public void testBreak() {
        Node breakNode = IR.breakNode();
        assertEquals("break", generateCode(breakNode));
    }

    @Test
    public void testContinue() {
        Node continueNode = IR.continueNode();
        assertEquals("continue", generateCode(continueNode));
    }

    // ==================== Parenthesization Edge Cases ====================

    @Test
    public void testOperatorPrecedence() {
        // (1 + 2) * 3 should generate (1+2)*3
        Node add = IR.add(IR.number(1), IR.number(2));
        Node mul = IR.mul(add, IR.number(3));
        assertEquals("(1+2)*3", generateCode(mul));
    }

    @Test
    public void testNestedConditional() {
        // a ? b ? c : d : e
        Node inner = IR.hook(IR.name("b"), IR.name("c"), IR.name("d"));
        Node outer = IR.hook(IR.name("a"), inner, IR.name("e"));
        assertEquals("a?b?c:d:e", generateCode(outer));
    }

    @Test
    public void testUnaryPrecedence() {
        // -1 + 2 should be (-1)+2
        Node neg = IR.neg(IR.number(1));
        Node add = IR.add(neg, IR.number(2));
        assertEquals("-1+2", generateCode(add));
    }

    @Test
    public void testAssignmentInExpression() {
        // (x = 5) + 3
        Node assign = IR.assign(IR.name("x"), IR.number(5));
        Node add = IR.add(assign, IR.number(3));
        assertEquals("(x=5)+3", generateCode(add));
    }

    // ==================== Comments (if supported) ====================

    @Test
    public void testLineComment() {
        // Not directly supported via IR, but we can test via parsing
        Node script = parseScript("// comment\nvar x = 1;");
        String code = generateCode(script);
        // Comments are typically not preserved in generated code unless options specify
        // We just check that generation doesn't throw
        assertNotNull(code);
    }

    // ==================== Large/Complex Expressions ====================

    @Test
    public void testComplexExpression() {
        // (a + b) * (c - d) / e
        Node add = IR.add(IR.name("a"), IR.name("b"));
        Node sub = IR.sub(IR.name("c"), IR.name("d"));
        Node mul = IR.mul(add, sub);
        Node div = IR.div(mul, IR.name("e"));
        assertEquals("(a+b)*(c-d)/e", generateCode(div));
    }

    // ==================== Edge Cases ====================

    @Test
    public void testEmptyBlock() {
        Node block = IR.block();
        assertEquals("{}", generateCode(block));
    }

    @Test
    public void testEmptyStatement() {
        Node empty = IR.empty();
        assertEquals("", generateCode(empty));
    }

    @Test
    public void testScriptWithMultipleStatements() {
        Node script = IR.script(
            IR.var(IR.name("a"), IR.number(1)),
            IR.exprResult(IR.call(IR.name("console"), IR.string("log"), IR.name("a")))
        );
        assertEquals("var a=1;console.log(a)", generateCode(script));
    }

    @Test
    public void testNegativeNumbers() {
        Node neg = IR.neg(IR.number(5));
        assertEquals("-5", generateCode(neg));
        // Double negation
        Node doubleNeg = IR.neg(IR.neg(IR.number(3)));
        assertEquals("- -3", generateCode(doubleNeg)); // Note: space may vary
    }

    @Test
    public void testStringEscape() {
        Node str = IR.string("hello\nworld");
        assertEquals("\"hello\\nworld\"", generateCode(str));
    }

    @Test
    public void testRegexLiteral() {
        Node regex = IR.regexp(IR.string("abc"), IR.string("gi"));
        assertEquals("/abc/gi", generateCode(regex));
    }

    @Test
    public void testTemplateLiteral() {
        // Template literals may not be fully supported in older versions
        // We'll test a simple one
        Node template = IR.templateLit(IR.string("Hello "), IR.name("name"));
        assertEquals("`Hello ${name}`", generateCode(template));
    }

    // ==================== Potential Bug Triggers (Bug 157) ====================

    @Test
    public void testUnaryPlus() {
        Node unaryPlus = IR.pos(IR.number(5));
        assertEquals("+5", generateCode(unaryPlus));
    }

    @Test
    public void testUnaryMinusWithSpace() {
        // Some code generators might incorrectly add space
        Node neg = IR.neg(IR.number(3));
        assertEquals("-3", generateCode(neg));
    }

    @Test
    public void testCommaExpression() {
        Node comma = IR.comma(IR.number(1), IR.number(2));
        assertEquals("1,2", generateCode(comma));
    }

    @Test
    public void testSequenceExpressionInParentheses() {
        Node seq = IR.comma(IR.number(1), IR.number(2));
        Node paren = IR.parenthesizedExpression(seq);
        assertEquals("(1,2)", generateCode(paren));
    }

    @Test
    public void testYieldExpression() {
        Node yield = IR.yield(IR.number(42));
        assertEquals("yield 42", generateCode(yield));
    }

    @Test
    public void testSpreadOperator() {
        Node spread = IR.spread(IR.name("arr"));
        assertEquals("...arr", generateCode(spread));
    }

    @Test
    public void testAwaitExpression() {
        Node await = IR.await(IR.name("promise"));
        assertEquals("await promise", generateCode(await));
    }

    @Test
    public void testClassDeclaration() {
        Node classNode = IR.classNode(IR.name("MyClass"), null, IR.classMembers());
        assertEquals("class MyClass{}", generateCode(classNode));
    }

    @Test
    public void testClassWithExtends() {
        Node classNode = IR.classNode(IR.name("Child"), IR.name("Parent"), IR.classMembers());
        assertEquals("class Child extends Parent{}", generateCode(classNode));
    }

    @Test
    public void testImportStatement() {
        Node importNode = IR.importNode(IR.name("defaultExport"), IR.string("module"));
        assertEquals("import defaultExport from\"module\"", generateCode(importNode));
    }

    @Test
    public void testExportStatement() {
        Node exportNode = IR.exportNode(IR.var(IR.name("x"), IR.number(1)));
        assertEquals("export var x=1", generateCode(exportNode));
    }

    // ==================== Additional Coverage: Nested Functions ====================

    @Test
    public void testNestedFunction() {
        Node innerFunc = IR.function(IR.name("inner"), IR.paramList(), IR.block(IR.returnNode(IR.number(1))));
        Node outerBody = IR.block(IR.var(IR.name("f"), innerFunc), IR.returnNode(IR.call(IR.name("f"))));
        Node outerFunc = IR.function(IR.name("outer"), IR.paramList(), outerBody);
        assertEquals("function outer(){var f=function inner(){return 1};return f()}", generateCode(outerFunc));
    }

    // ==================== Test with actual parsing to ensure round-trip ====================

    @Test
    public void testRoundTripSimple() {
        String js = "var x = 1 + 2;";
        Node script = parseScript(js);
        String generated = generateCode(script);
        // The generated code may not be identical due to formatting, but should be semantically equivalent
        // We just check it's not empty and contains expected tokens
        assertNotNull(generated);
        assertEquals("var x=1+2", generated);
    }

    @Test
    public void testRoundTripWithFunction() {
        String js = "function f(a, b) { return a + b; }";
        Node script = parseScript(js);
        String generated = generateCode(script);
        assertEquals("function f(a,b){return a+b}", generated);
    }

    @Test
    public void testRoundTripWithObject() {
        String js = "var obj = {a: 1, b: 2};";
        Node script = parseScript(js);
        String generated = generateCode(script);
        assertEquals("var obj={a:1,b:2}", generated);
    }

    @Test
    public void testRoundTripWithArray() {
        String js = "var arr = [1, 2, 3];";
        Node script = parseScript(js);
        String generated = generateCode(script);
        assertEquals("var arr=[1,2,3]", generated);
    }

    @Test
    public void testRoundTripWithIfElse() {
        String js = "if (true) { return 1; } else { return 2; }";
        Node script = parseScript(js);
        String generated = generateCode(script);
        assertEquals("if(true){return 1}else{return 2}", generated);
    }

    @Test
    public void testRoundTripWithForLoop() {
        String js = "for (var i = 0; i < 10; i++) { }";
        Node script = parseScript(js);
        String generated = generateCode(script);
        assertEquals("for(var i=0;i<10;i++){}", generated);
    }

    @Test
    public void testRoundTripWithTryCatch() {
        String js = "try { throw 'error'; } catch (e) { } finally { }";
        Node script = parseScript(js);
        String generated = generateCode(script);
        assertEquals("try{throw\"error\"}catch(e){}finally{}", generated);
    }

    // ==================== Null/Edge Inputs ====================

    @Test(expected = NullPointerException.class)
    public void testNullNode() {
        generateCode(null);
    }

    @Test
    public void testEmptyScript() {
        Node script = IR.script();
        assertEquals("", generateCode(script));
    }

    @Test
    public void testScriptWithSemicolon() {
        Node expr = IR.exprResult(IR.number(1));
        Node script = IR.script(expr);
        assertEquals("1", generateCode(script));
    }
}