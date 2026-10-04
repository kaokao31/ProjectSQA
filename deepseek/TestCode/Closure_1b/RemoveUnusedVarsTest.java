import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class RemoveUnusedVarsTest {

    private RemoveUnusedVars removeUnusedVars;

    @Before
    public void setUp() {
        removeUnusedVars = new RemoveUnusedVars();
    }

    @Test
    public void testNullInput() {
        String result = removeUnusedVars.removeUnusedVars(null);
        assertNull("Null input should return null", result);
    }

    @Test
    public void testEmptyString() {
        String result = removeUnusedVars.removeUnusedVars("");
        assertEquals("Empty input should return empty string", "", result);
    }

    @Test
    public void testNoVariables() {
        String input = "public class Test { public void method() { System.out.println(\"hello\"); } }";
        String expected = input; // unchanged
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Code with no variables should remain unchanged", expected, result);
    }

    @Test
    public void testAllVariablesUsed() {
        String input = "public class Test { public void method() { int x = 5; System.out.println(x); } }";
        String expected = input; // unchanged
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Code with all variables used should remain unchanged", expected, result);
    }

    @Test
    public void testSingleUnusedVariable() {
        String input = "public class Test { public void method() { int x = 5; } }";
        String expected = "public class Test { public void method() {  } }";
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Unused variable should be removed", expected, result);
    }

    @Test
    public void testMultipleUnusedVariables() {
        String input = "public class Test { public void method() { int a = 1; int b = 2; int c = 3; System.out.println(b); } }";
        String expected = "public class Test { public void method() { int b = 2; System.out.println(b); } }";
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Only used variable should remain", expected, result);
    }

    @Test
    public void testVariableUsedInIfCondition() {
        String input = "public class Test { public void method() { int x = 5; if (x > 0) { System.out.println(\"pos\"); } } }";
        String expected = input; // x is used in condition
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Variable used in if condition should be kept", expected, result);
    }

    @Test
    public void testVariableUsedInLoop() {
        String input = "public class Test { public void method() { int sum = 0; for (int i = 0; i < 10; i++) { sum += i; } } }";
        String expected = input; // both sum and i are used
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Variables used in loop should be kept", expected, result);
    }

    @Test
    public void testUnusedVariableInNestedBlock() {
        String input = "public class Test { public void method() { { int x = 5; } int y = 10; System.out.println(y); } }";
        String expected = "public class Test { public void method() { int y = 10; System.out.println(y); } }";
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Unused variable in nested block should be removed", expected, result);
    }

    @Test
    public void testVariableUsedAsArrayLength() {
        String input = "public class Test { public void method() { int size = 10; int[] arr = new int[size]; } }";
        String expected = input; // size used in array creation
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Variable used in array creation should be kept", expected, result);
    }

    @Test
    public void testVariableUsedInAssignment() {
        String input = "public class Test { public void method() { int a = 1; int b = a; System.out.println(b); } }";
        String expected = input; // a used in assignment to b
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Variable used in assignment should be kept", expected, result);
    }

    @Test
    public void testVariableUsedInReturn() {
        String input = "public class Test { public int method() { int x = 42; return x; } }";
        String expected = input;
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Variable used in return should be kept", expected, result);
    }

    @Test
    public void testMultipleMethodsWithUnusedVars() {
        String input = "public class Test { public void m1() { int a = 1; } public void m2() { int b = 2; System.out.println(b); } }";
        String expected = "public class Test { public void m1() {  } public void m2() { int b = 2; System.out.println(b); } }";
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Unused variable in one method should be removed", expected, result);
    }

    @Test
    public void testVariableUsedInMethodCall() {
        String input = "public class Test { public void method() { int x = 5; print(x); } private void print(int v) { System.out.println(v); } }";
        String expected = input;
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Variable used in method call should be kept", expected, result);
    }

    @Test
    public void testStringVariableUsedInConcatenation() {
        String input = "public class Test { public void method() { String name = \"John\"; System.out.println(\"Hello \" + name); } }";
        String expected = input;
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("String variable used in concatenation should be kept", expected, result);
    }

    @Test
    public void testVariableDeclaredButOnlyAssignedLater() {
        String input = "public class Test { public void method() { int x; x = 10; System.out.println(x); } }";
        String expected = input;
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Variable declared and used later should be kept", expected, result);
    }

    @Test
    public void testUnusedVariableWithInitializationOnly() {
        String input = "public class Test { public void method() { int x = 5; } }";
        String expected = "public class Test { public void method() {  } }";
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Unused variable with initialization should be removed", expected, result);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInputThrowsException() {
        // Depending on implementation, null input may throw NPE
        removeUnusedVars.removeUnusedVars(null);
    }

    @Test
    public void testCodeWithCommentsAndUnusedVars() {
        String input = "public class Test { public void method() { int x = 5; // comment \n int y = 10; System.out.println(y); } }";
        String expected = "public class Test { public void method() {  // comment \n int y = 10; System.out.println(y); } }";
        String result = removeUnusedVars.removeUnusedVars(input);
        assertEquals("Unused variable with comment should be removed", expected, result);
    }
}