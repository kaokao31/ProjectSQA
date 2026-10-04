package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.jscomp.TypeInferencePass;
import com.google.javascript.jscomp.parsing.parser.util.SourcePosition;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.UnionType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypeInferenceTest {
    private Compiler compiler;

    @Before
    public void setUp() throws Exception {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setChecksOnly(true);
        options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.OFF);
        options.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.OFF);
        compiler.compile(
            ImmutableList.of(
                SourceFile.fromCode("test",
                    "/** @param {?number|string} x */\n" +
                    "function f(x) {\n" +
                    "  if (typeof x === 'number') {\n" +
                    "    return x + 1;\n" +
                    "  }\n" +
                    "  return 0;\n" +
                    "}"
                )
            ),
            options
        );
    }

    @Test
    public void testNarrowTypeInsideTypeGuard() {
        Node root = compiler.getRoot();
        Node script = root.getFirstChild();
        Node function = script.getFirstChild();
        Node body = function.getLastChild();
        Node ifNode = body.getFirstChild(); // assuming no other statements
        Node condition = ifNode.getFirstChild();
        Node thenBlock = condition.getNext();
        Node returnStmt = thenBlock.getFirstChild();
        Node addNode = returnStmt.getFirstChild(); // x + 1
        Node xRef = addNode.getFirstChild();
        JSType typeOfX = xRef.getJSType();
        assertNotNull("Type of x inside the if block should be inferred", typeOfX);
        assertTrue("Inside type guard 'typeof x === \\'number\\'', x should be a number",
            typeOfX.isNumberType());
    }

    @Test
    public void testUnionTypeRestrictByNotNullOrUndefined() {
        JSType numberType = compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE);
        JSType unionType = compiler.getTypeRegistry().createUnionType(numberType, stringType);
        assertTrue("Union type should be union type", unionType.isUnionType());
        
        // Test that restrictByNotNullOrUndefined works correctly.
        // This method is often involved in type narrowing bugs.
        JSType narrowed = unionType.restrictByNotNullOrUndefined();
        assertNotNull("Narrowed union should not be null", narrowed);
        assertTrue("Narrowed union should still be a union", narrowed.isUnionType());
        assertFalse("Narrowed union should not contain null/undefined",
            narrowed.isNullType() || narrowed.isVoidType());
    }

    @Test
    public void testTypeInferenceForSimpleAssignment() {
        String code = "/** @type {?number} */ var x = 5; if (x != null) { var y = x; }";
        compiler.compile(
            ImmutableList.of(SourceFile.fromCode("test2", code)),
            new CompilerOptions()
        );
        Node root = compiler.getRoot();
        // The variable y should have a non-nullable number type.
        // We can check the type of y
        Node script = root.getFirstChild();
        Node varY = script.getLastChild(); // the assignment to y
        JSType yType = varY.getJSType();
        assertNotNull("Type of y should be inferred", yType);
        assertTrue("y should be a number type, not nullable", yType.isNumberType());
        assertFalse("y should not be nullable", yType.isNullable());
    }

    @Test
    public void testNullCheckOnFunctionReturn() {
        String code = "/** @return {?string} */ function f() { return null; } " +
                      "var x = f(); if (x) { var y = x.length; }";
        compiler.compile(
            ImmutableList.of(SourceFile.fromCode("test3", code)),
            new CompilerOptions()
        );
        Node root = compiler.getRoot();
        // The property access x.length inside the if should be on non-null string
        Node script = root.getFirstChild();
        Node assignmentY = script.getLastChild(); // var y = x.length;
        Node lengthAccess = assignmentY.getFirstChild().getFirstChild(); // x.length
        JSType typeOfX = lengthAccess.getFirstChild().getJSType();
        assertNotNull("Type of x inside the if should be inferred", typeOfX);
        assertTrue("Inside truthy check, x should be a string (non-null)", typeOfX.isStringType());
    }
}