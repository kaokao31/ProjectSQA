package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class FormElementTest {

    private FormElement createForm() {
        return new FormElement(Tag.valueOf("form"), "http://example.com/", new Attributes());
    }

    private Element createInput(String name, String value) {
        Element input = new Element(Tag.valueOf("input"), "http://example.com/", new Attributes());
        input.attr("name", name);
        input.attr("value", value);
        return input;
    }

    @Test
    public void testConstructorInitializesEmptyElements() {
        FormElement form = createForm();

        assertNotNull(form.elements());
        assertTrue(form.elements().isEmpty());
    }

    @Test
    public void testAddElementAddsToElementsAndReturnsThis() {
        FormElement form = createForm();
        Element input = createInput("q", "jsoup");

        assertSame(form, form.addElement(input));
        assertEquals(1, form.elements().size());
        assertSame(input, form.elements().get(0));
    }

    @Test
    public void testElementsReturnsLiveList() {
        FormElement form = createForm();
        Elements elements = form.elements();
        Element input = createInput("q", "jsoup");

        form.addElement(input);

        assertSame(elements, form.elements());
        assertEquals(1, elements.size());
    }

    @Test
    public void testAddElementDoesNotAppendChildNode() {
        FormElement form = createForm();
        Element input = createInput("q", "jsoup");

        form.addElement(input);

        assertEquals(0, form.childNodeSize());
    }

    @Test
    public void testRemoveChildRemovesFromElementsUsingNodeRemove() {
        FormElement form = createForm();
        Element input = createInput("q", "jsoup");
        form.appendChild(input);
        form.addElement(input);

        assertEquals(1, form.elements().size());

        input.remove();

        assertEquals(0, form.elements().size());
        assertEquals(0, form.childNodeSize());
    }

    @Test
    public void testRemoveChildKeepsRemainingElements() {
        FormElement form = createForm();
        Element input1 = createInput("one", "1");
        Element input2 = createInput("two", "2");
        form.appendChild(input1);
        form.appendChild(input2);
        form.addElement(input1);
        form.addElement(input2);

        input1.remove();

        assertEquals(1, form.elements().size());
        assertSame(input2, form.elements().get(0));
    }

    @Test
    public void testRemoveChildDirectlyUpdatesElements() {
        FormElement form = createForm();
        Element input = createInput("q", "jsoup");
        form.appendChild(input);
        form.addElement(input);

        form.removeChild(input);

        assertEquals(0, form.elements().size());
        assertEquals(0, form.childNodeSize());
    }

    @Test
    public void testFormDataIncludesSubmittableElement() {
        FormElement form = createForm();
        Element input = createInput("q", "jsoup");
        form.appendChild(input);
        form.addElement(input);

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("q", data.get(0).key());
        assertEquals("jsoup", data.get(0).value());
    }

    @Test
    public void testFormDataExcludesNonSubmittableElement() {
        FormElement form = createForm();
        Element div = new Element(Tag.valueOf("div"), "http://example.com/", new Attributes());
        div.attr("name", "ignored");
        div.attr("value", "ignored");
        form.addElement(div);

        assertTrue(form.formData().isEmpty());
    }

    @Test
    public void testFormDataExcludesRemovedElement() {
        FormElement form = createForm();
        Element input = createInput("q", "jsoup");
        form.appendChild(input);
        form.addElement(input);

        input.remove();

        assertTrue(form.formData().isEmpty());
    }

    @Test
    public void testFormDataSkipsEmptyName() {
        FormElement form = createForm();
        Element input = createInput("", "v");
        form.appendChild(input);
        form.addElement(input);

        assertTrue(form.formData().isEmpty());
    }

    @Test
    public void testFormDataSelectsSelectedOption() {
        FormElement form = createForm();
        Element select = new Element(Tag.valueOf("select"), "http://example.com/", new Attributes());
        select.attr("name", "color");
        Element option = new Element(Tag.valueOf("option"), "http://example.com/", new Attributes());
        option.attr("value", "blue");
        option.attr("selected", "");
        select.appendChild(option);
        form.appendChild(select);
        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("color", data.get(0).key());
        assertEquals("blue", data.get(0).value());
    }

    @Test
    public void testFormDataIncludesCheckedCheckbox() {
        FormElement form = createForm();
        Element checkbox = new Element(Tag.valueOf("input"), "http://example.com/", new Attributes());
        checkbox.attr("type", "checkbox");
        checkbox.attr("name", "agree");
        checkbox.attr("value", "yes");
        checkbox.attr("checked", "");
        form.appendChild(checkbox);
        form.addElement(checkbox);

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("yes", data.get(0).value());
    }

    @Test
    public void testFormDataIncludesCheckedRadio() {
        FormElement form = createForm();
        Element radio = new Element(Tag.valueOf("input"), "http://example.com/", new Attributes());
        radio.attr("type", "radio");
        radio.attr("name", "gender");
        radio.attr("value", "female");
        radio.attr("checked", "");
        form.appendChild(radio);
        form.addElement(radio);

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("gender", data.get(0).key());
        assertEquals("female", data.get(0).value());
    }

    @Test
    public void testFormDataSkipsUncheckedCheckbox() {
        FormElement form = createForm();
        Element checkbox = new Element(Tag.valueOf("input"), "http://example.com/", new Attributes());
        checkbox.attr("type", "checkbox");
        checkbox.attr("name", "opt");
        checkbox.attr("value", "yes");
        form.appendChild(checkbox);
        form.addElement(checkbox);

        assertTrue(form.formData().isEmpty());
    }
}