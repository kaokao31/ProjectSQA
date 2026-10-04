package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FormElementTest {

    @Test
    public void testFormDataWithVariousElements() {
        String html = "<form>" +
                "<input type='text' name='text1' value='val1'>" +
                "<input type='text' name='text2'>" + // no value attribute, defaults to empty or element text
                "<input type='checkbox' name='cb1' value='cbval1' checked>" +
                "<input type='checkbox' name='cb2' value='cbval2'>" + // not checked, should be ignored
                "<input type='radio' name='rd1' value='rdval1' checked>" +
                "<input type='radio' name='rd2' value='rdval2'>" + // not checked
                "<select name='sel1'>" +
                "<option value='opt1'>One</option>" +
                "<option value='opt2' selected>Two</option>" +
                "</select>" +
                "<select name='sel2'>" +
                "<option>NoValueOpt</option>" + // option without value attribute
                "<option selected>NoValueOptSelected</option>" +
                "</select>" +
                "<textarea name='ta1'>TextAreaVal</textarea>" +
                "<button name='btn' value='btnval'>Button</button>" +
                "<input type='submit' name='sub' value='subval'>" +
                "<input type='image' name='img' value='imgval'>" +
                "<input type='file' name='file'>" +
                "<input type='password' name='pwd' value='secret'>" +
                "<input type='hidden' name='hid' value='hiddenval'>" +
                "<input type='disabled' name='dis' value='disval' disabled>" + // disabled, should be ignored
                "</form>";

        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.selectFirst("form");
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        
        boolean foundText1 = false;
        boolean foundCb1 = false;
        boolean foundCb2 = false;
        boolean foundRd1 = false;
        boolean foundSel1 = false;
        boolean foundSel2 = false;
        boolean foundTa1 = false;
        boolean foundDis = false;

        for (Connection.KeyVal kv : data) {
            if ("text1".equals(kv.key())) {
                assertEquals("val1", kv.value());
                foundText1 = true;
            } else if ("cb1".equals(kv.key())) {
                assertEquals("cbval1", kv.value());
                foundCb1 = true;
            } else if ("cb2".equals(kv.key())) {
                foundCb2 = true;
            } else if ("rd1".equals(kv.key())) {
                assertEquals("rdval1", kv.value());
                foundRd1 = true;
            } else if ("sel1".equals(kv.key())) {
                assertEquals("opt2", kv.value());
                foundSel1 = true;
            } else if ("sel2".equals(kv.key())) {
                assertEquals("NoValueOptSelected", kv.value());
                foundSel2 = true;
            } else if ("ta1".equals(kv.key())) {
                assertEquals("TextAreaVal", kv.value());
                foundTa1 = true;
            } else if ("dis".equals(kv.key())) {
                foundDis = true;
            }
        }

        assertTrue(foundText1);
        assertTrue(foundCb1);
        assertFalse(foundCb2);
        assertTrue(foundRd1);
        assertTrue(foundSel1);
        assertTrue(foundSel2);
        assertTrue(foundTa1);
        assertFalse(foundDis);
    }

    @Test
    public void testFormDataEmptyForm() {
        String html = "<form></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.selectFirst("form");
        assertNotNull(form);
        List<Connection.KeyVal> data = form.formData();
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataNoNameAttribute() {
        String html = "<form><input type='text' value='val1'><select><option selected>opt</option></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.selectFirst("form");
        assertNotNull(form);
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testAddElement() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", null);
        assertEquals(0, form.elements().size());

        Element input = new Element(Tag.valueOf("input"), "").attr("name", "test").attr("value", "val");
        FormElement returnedForm = form.addElement(input);
        
        assertSame(form, returnedForm);
        assertEquals(1, form.elements().size());
        assertEquals("test", form.elements().get(0).attr("name"));
    }

    @Test
    public void testSubmitNullAction() {
        String html = "<form><input type='text' name='foo' value='bar'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.selectFirst("form");
        
        // Ensure action attribute is missing
        assertFalse(form.hasAttr("action"));
        
        Connection conn = form.submit();
        assertNotNull(conn);
        assertEquals(form.absUrl("action"), conn.request().url().toExternalForm());
    }

    @Test
    public void testSubmitWithAction() {
        String html = "<form action='http://example.com/login'><input type='text' name='foo' value='bar'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.selectFirst("form");
        
        Connection conn = form.submit();
        assertNotNull(conn);
        assertEquals("http://example.com/login", conn.request().url().toExternalForm());
    }

    @Test
    public void testSelectChildOptionsEdgeCases() {
        // Select without any option
        String html = "<form><select name='emptySel'></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.selectFirst("form");
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testSelectMultipleOptionsFirstAsDefault() {
        // If no option is marked selected, standard HTML behavior often picks the first one or none depending on spec/implementation.
        // Let's test how Jsoup handles it.
        String html = "<form><select name='sel'><option value='1'>One</option><option value='2'>Two</option></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.selectFirst("form");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("sel", data.get(0).key());
        assertEquals("1", data.get(0).value());
    }
}