package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.List;

import static org.junit.Assert.*;

public class FormElementTest {

    @Test
    public void testFormElementCreationAndBasicProperties() {
        String html = "<form action=\"/submit\"><input name=\"user\" value=\"john\"></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        assertNotNull(form);
        assertEquals("/submit", form.absUrl("action"));
        
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("user", data.get(0).key());
        assertEquals("john", data.get(0).value());
    }

    @Test
    public void testAddElement() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        assertEquals(0, form.elements().size());

        Element input = new Element(Tag.valueOf("input"), "").attr("name", "foo").attr("value", "bar");
        FormElement returnedForm = form.addElement(input);
        
        assertSame(form, returnedForm);
        assertEquals(1, form.elements().size());
        assertEquals(input, form.elements().get(0));
    }

    @Test
    public void testSubmit() {
        String html = "<form action=\"/login\"><input name=\"user\" value=\"admin\"></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();
        assertNotNull(con);
        assertEquals(Connection.Method.GET, con.request().method());
        assertEquals("http://localhost/login", con.request().url().toExternalForm());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitNoAction() {
        String html = "<form><input name=\"user\" value=\"admin\"></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        form.submit();
    }

    @Test
    public void testFormDataWithVariousInputTypes() {
        String html = "<form action=\"/test\">" +
                "<input name=\"text1\" value=\"val1\">" +
                "<input type=\"checkbox\" name=\"cb1\" value=\"cbVal1\" checked>" +
                "<input type=\"checkbox\" name=\"cb2\" value=\"cbVal2\">" + // unchecked, should be ignored
                "<input type=\"radio\" name=\"r1\" value=\"rVal1\" checked>" +
                "<radio name=\"r1\" value=\"rVal2\" checked>" + // another checked radio
                "<select name=\"sel1\"><option value=\"opt1\" selected>Opt1</option><option value=\"opt2\">Opt2</option></select>" +
                "<textarea name=\"ta1\">TextareaVal</textarea>" +
                "<button name=\"btn\" value=\"btnVal\">Button</button>" +
                "</form>";

        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        boolean foundCb2 = false;
        for (Connection.KeyVal kv : data) {
            if (kv.key().equals("cb2")) {
                foundCb2 = true;
            }
        }
        assertFalse("Unchecked checkbox should not be in form data", foundCb2);

        // Check specifics
        assertTrue(data.size() >= 5);
    }

    @Test
    public void testFormDataDisabledElements() {
        String html = "<form action=\"/test\">" +
                "<input name=\"text1\" value=\"val1\" disabled>" +
                "<input name=\"text2\" value=\"val2\">" +
                "</form>";

        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("text2", data.get(0).key());
        assertEquals("val2", data.get(0).value());
    }

    @Test
    public void testFormDataSubmitButtonWithoutName() {
        String html = "<form action=\"/test\">" +
                "<input type=\"submit\" value=\"Submit\">" +
                "<input name=\"text1\" value=\"val1\">" +
                "</form>";

        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("text1", data.get(0).key());
    }

    @Test
    public void testNodeRemovalCleansUpFormElements() {
        String html = "<form action=\"/test\"><input name=\"user\" value=\"john\"></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        Element input = doc.select("input").first();

        assertEquals(1, form.elements().size());
        input.remove();
        assertEquals(0, form.elements().size());
    }
}