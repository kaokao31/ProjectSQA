package com.google.javascript.jscomp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.rhino.Node;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class DisambiguatePropertiesTest {
    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    private void runProcess(String externsSource, String source) {
        try {
            Node externs = compiler.parseTestCode(externsSource);
            Node root = compiler.parseTestCode(source);
            assertNotNull("Externs node should not be null", externs);
            assertNotNull("Root node should not be null", root);

            Map<String, CheckLevel> errorLevels = new HashMap<>();
            // Test with default error map (empty)
            DisambiguateProperties pass = new DisambiguateProperties(compiler, true, errorLevels);
            pass.process(externs, root);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    private void runProcess(String source) {
        runProcess("", source);
    }

    @Test
    public void testProcessEmptyProgram() {
        runProcess("");
    }

    @Test
    public void testProcessSimpleVarDeclaration() {
        runProcess("var a;");
    }

    @Test
    public void testProcessVarWithInitializer() {
        runProcess("var a = 1;");
    }

    @Test
    public void testProcessObjectLiteralProperty() {
        runProcess("var obj = {x: 1};");
    }

    @Test
    public void testProcessPropertyAssignment() {
        runProcess("var obj = {}; obj.x = 1;");
    }

    @Test
    public void testProcessFunctionWithThisProperty() {
        runProcess("function f() { return this.x; }");
    }

    @Test
    public void testProcessMultipleObjectsSameProperty() {
        runProcess("var a = {x: 1}; var b = {x: 2};");
    }

    @Test
    public void testProcessPropertyAccessOnNewObject() {
        runProcess("var o = new Object(); o.p = 1;");
    }

    @Test
    public void testProcessNestedObjectProperties() {
        runProcess("var a = {b: {c: 1}};");
    }

    @Test
    public void testProcessRenamingDisabled() {
        try {
            Node externs = compiler.parseTestCode("");
            Node root = compiler.parseTestCode("var a = {p: 1};");
            Map<String, CheckLevel> errorLevels = new HashMap<>();
            DisambiguateProperties pass = new DisambiguateProperties(compiler, false, errorLevels);
            pass.process(externs, root);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testProcessWithErrorMap() {
        try {
            Node externs = compiler.parseTestCode("");
            Node root = compiler.parseTestCode("var obj = {x: 1};");
            Map<String, CheckLevel> errorLevels = new HashMap<>();
            errorLevels.put("obj.x", CheckLevel.ERROR);
            DisambiguateProperties pass = new DisambiguateProperties(compiler, true, errorLevels);
            pass.process(externs, root);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testProcessWithExternsAndSourceProperties() {
        runProcess("var ext = {p: 1};", "var src = {p: 2};");
    }
}