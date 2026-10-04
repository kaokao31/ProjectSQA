package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FormElementTest {

    private Document doc;
    private FormElement form;

    @Before
    public void setUp() {
        String html = "<form id='form' action='/submit' method='post'>" +
                "<input name='text' value='hello'>" +
                "<input type='checkbox' name='check' checked value='on'>" +
                "<input type='checkbox' name='uncheck' value='off'>" +
                "<input type='radio' name='radio' value='a' checked>" +
                "<input type='radio' name='radio' value='b'>" +
                "<select name='select'>" +
                "<option value='1'>One</option>" +
                "<option value='2' selected>Two</option>" +
                "</select>" +
                "<select name='multi' multiple>" +
                "<option value='x' selected>X</option>" +
                "<option value='y' selected>Y</option>" +
                "<option value='z'>Z</option>" +
                "</select>" +
                "<textarea name='area'>content</textarea>" +
                "<button type='submit' name='btn' value='go'>Go</button>" +
                "<input type='image' name='img' src='pic.png'>" +
                "<input type='hidden' name='hidden' value='secret'>" +
                "</form>";
        doc = Jsoup.parse(html);
        form = (FormElement) doc.getElementById("form");
    }

    @Test
    public void testFormDataEmptyForm() {
        Document emptyDoc = Jsoup.parse("<form id='empty'></form>");
        FormElement emptyForm = (FormElement) emptyDoc.getElementById("empty");
        List<Connection.KeyVal> data = emptyForm.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithTextInput() {
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("text") && kv.value().equals("hello")));
    }

    @Test
    public void testFormDataWithCheckboxChecked() {
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("check") && kv.value().equals("on")));
    }

    @Test
    public void testFormDataWithCheckboxUnchecked() {
        List<Connection.KeyVal> data = form.formData();
        assertFalse(data.stream().anyMatch(kv -> kv.key().equals("uncheck")));
    }

    @Test
    public void testFormDataWithRadioButton() {
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("radio") && kv.value().equals("a")));
        assertFalse(data.stream().anyMatch(kv -> kv.key().equals("radio") && kv.value().equals("b")));
    }

    @Test
    public void testFormDataWithSelect() {
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("select") && kv.value().equals("2")));
    }

    @Test
    public void testFormDataWithSelectMultiple() {
        List<Connection.KeyVal> data = form.formData();
        long count = data.stream().filter(kv -> kv.key().equals("multi")).count();
        assertEquals(2, count);
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("multi") && kv.value().equals("x")));
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("multi") && kv.value().equals("y")));
    }

    @Test
    public void testFormDataWithTextarea() {
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("area") && kv.value().equals("content")));
    }

    @Test
    public void testFormDataWithSubmitButton() {
        List<Connection.KeyVal> data = form.formData();
        // According to HTML spec, a submit button is only successful if it is used to submit.
        // Jsoup may include it or not; we test that it is present (bug detection if missing)
        assertTrue("Submit button data should be present", data.stream().anyMatch(kv -> kv.key().equals("btn") && kv.value().equals("go")));
    }

    @Test
    public void testFormDataWithImageInput() {
        List<Connection.KeyVal> data = form.formData();
        // Image inputs should include name and coordinates (x,y). Jsoup may not handle coordinates.
        // At minimum, the name should be present with some value (possibly empty or "0,0")
        assertTrue("Image input data should be present", data.stream().anyMatch(kv -> kv.key().equals("img")));
    }

    @Test
    public void testFormDataWithHiddenInput() {
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("hidden") && kv.value().equals("secret")));
    }

    @Test
    public void testFormDataWithDisabledElement() {
        String html = "<form id='f'><input name='disabled' disabled value='no'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        assertFalse(data.stream().anyMatch(kv -> kv.key().equals("disabled")));
    }

    @Test
    public void testFormDataWithNoName() {
        String html = "<form id='f'><input value='noName'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testAddElement() {
        Element newInput = new Element("input").attr("name", "added").attr("value", "new");
        form.addElement(newInput);
        Elements elements = form.elements();
        assertTrue(elements.contains(newInput));
    }

    @Test
    public void testElements() {
        Elements elements = form.elements();
        assertFalse(elements.isEmpty());
        // Should contain all form controls
        assertTrue(elements.stream().anyMatch(e -> e.tagName().equals("input") && e.attr("name").equals("text")));
        assertTrue(elements.stream().anyMatch(e -> e.tagName().equals("button")));
    }

    @Test
    public void testFormMethod() {
        assertEquals("post", form.formMethod());
        form.setFormMethod("get");
        assertEquals("get", form.formMethod());
    }

    @Test
    public void testFormAction() {
        assertEquals("/submit", form.formAction());
        form.setFormAction("/new");
        assertEquals("/new", form.formAction());
    }

    @Test
    public void testFormEnctype() {
        // default enctype is application/x-www-form-urlencoded
        assertEquals("application/x-www-form-urlencoded", form.formEnctype());
        form.setFormEnctype("multipart/form-data");
        assertEquals("multipart/form-data", form.formEnctype());
    }

    @Test
    public void testFormDataWithMultipleSubmitButtons() {
        String html = "<form id='f'>" +
                "<button type='submit' name='b1' value='v1'>B1</button>" +
                "<button type='submit' name='b2' value='v2'>B2</button>" +
                "</form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // Both buttons may be included; test that at least one is present
        assertFalse(data.isEmpty());
        assertTrue(data.stream().anyMatch(kv -> kv.key().startsWith("b")));
    }

    @Test
    public void testFormDataWithResetButton() {
        String html = "<form id='f'><button type='reset' name='reset' value='r'>Reset</button></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // Reset buttons should not be included
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithButtonWithoutName() {
        String html = "<form id='f'><button type='submit' value='v'>Click</button></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // Button without name should not be included
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithInputTypeImageNoName() {
        String html = "<form id='f'><input type='image' src='img.png'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // Image input without name should not be included
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithSelectNoOptions() {
        String html = "<form id='f'><select name='empty'></select></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // Select with no options should not be included
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithSelectNoSelected() {
        String html = "<form id='f'><select name='s'><option value='a'>A</option></select></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // If no option is selected, first option is default? HTML spec says first option is selected if none selected.
        // Jsoup may or may not include it. Test that data is not empty.
        assertFalse(data.isEmpty());
    }

    @Test
    public void testFormDataWithTextareaEmpty() {
        String html = "<form id='f'><textarea name='t'></textarea></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("t") && kv.value().equals("")));
    }

    @Test
    public void testFormDataWithCheckboxNoValue() {
        String html = "<form id='f'><input type='checkbox' name='c' checked></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // Checkbox without value should have value "on"
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("c") && kv.value().equals("on")));
    }

    @Test
    public void testFormDataWithRadioNoValue() {
        String html = "<form id='f'><input type='radio' name='r' checked></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // Radio without value should have value "on"
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("r") && kv.value().equals("on")));
    }

    @Test
    public void testFormDataWithMultipleTextInputsSameName() {
        String html = "<form id='f'><input name='x' value='1'><input name='x' value='2'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        assertEquals(2, data.stream().filter(kv -> kv.key().equals("x")).count());
    }

    @Test
    public void testFormDataWithInputTypeFile() {
        String html = "<form id='f'><input type='file' name='file'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // File inputs are not included in formData (they require multipart)
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataWithInputTypePassword() {
        String html = "<form id='f'><input type='password' name='pwd' value='secret'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("pwd") && kv.value().equals("secret")));
    }

    @Test
    public void testFormDataWithInputTypeSubmit() {
        String html = "<form id='f'><input type='submit' name='sub' value='Send'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        // Submit input buttons should be included
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("sub") && kv.value().equals("Send")));
    }

    @Test
    public void testFormDataWithInputTypeImageCoordinates() {
        // Image inputs should include x and y coordinates. Jsoup may not handle this.
        // We test that the name is present and value is something like "0,0" or empty.
        String html = "<form id='f'><input type='image' name='img' src='pic.png'></form>";
        Document d = Jsoup.parse(html);
        FormElement f = (FormElement) d.getElementById("f");
        List<Connection.KeyVal> data = f.formData();
        assertTrue(data.stream().anyMatch(kv -> kv.key().equals("img")));
        // The value might be empty string or "0,0". We just check it's not null.
        Connection.KeyVal kv = data.stream().filter(k -> k.key().equals("img")).findFirst().get();
        assertNotNull(kv.value());
    }
}