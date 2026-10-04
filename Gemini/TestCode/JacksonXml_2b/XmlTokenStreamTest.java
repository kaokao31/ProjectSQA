package org.codehaus.plexus.util.xml.pull;

import org.junit.Test;
import java.io.StringReader;
import static org.junit.Assert.*;

public class XmlTokenStreamTest {

    @Test(expected = NullPointerException.class)
    public void testConstructorNullReader() throws Exception {
        new XmlTokenStream(null);
    }

    @Test
    public void testEmptyStream() throws Exception {
        XmlTokenStream stream = new XmlTokenStream(new StringReader(""));
        assertFalse(stream.hasNext());
    }

    @Test
    public void testSimpleElement() throws Exception {
        XmlTokenStream stream = new XmlTokenStream(new StringReader("<root/>"));
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.START_TAG, stream.next());
        assertEquals("root", stream.getName());
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.END_TAG, stream.next());
        
        assertFalse(stream.hasNext());
    }

    @Test
    public void testElementWithText() throws Exception {
        XmlTokenStream stream = new XmlTokenStream(new StringReader("<root>text</root>"));
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.START_TAG, stream.next());
        assertEquals("root", stream.getName());
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.TEXT, stream.next());
        assertEquals("text", stream.getText());
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.END_TAG, stream.next());
        
        assertFalse(stream.hasNext());
    }

    @Test
    public void testAttributes() throws Exception {
        XmlTokenStream stream = new XmlTokenStream(new StringReader("<root attr=\"value\"></root>"));
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.START_TAG, stream.next());
        assertEquals("root", stream.getName());
        assertEquals(1, stream.getAttributeCount());
        assertEquals("attr", stream.getAttributeName(0));
        assertEquals("value", stream.getAttributeValue(0));
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.END_TAG, stream.next());
        
        assertFalse(stream.hasNext());
    }

    @Test
    public void testCommentsAndProcessingInstructions() throws Exception {
        String xml = "<?xml version=\"1.0\"?><!-- comment --><root><!-- another comment --></root>";
        XmlTokenStream stream = new XmlTokenStream(new StringReader(xml));
        
        while (stream.hasNext()) {
            stream.next();
        }
        assertFalse(stream.hasNext());
    }

    @Test
    public void testCdataSection() throws Exception {
        String xml = "<root><![CDATA[some cdata content]]></root>";
        XmlTokenStream stream = new XmlTokenStream(new StringReader(xml));
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.START_TAG, stream.next());
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.TEXT, stream.next());
        assertEquals("some cdata content", stream.getText());
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.END_TAG, stream.next());
        
        assertFalse(stream.hasNext());
    }

    @Test
    public void testEntityReference() throws Exception {
        String xml = "<root>&amp;</root>";
        XmlTokenStream stream = new XmlTokenStream(new StringReader(xml));
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.START_TAG, stream.next());
        
        assertTrue(stream.hasNext());
        assertEquals( XmlTokenStream.TEXT, stream.next());
        
        assertTrue(stream.hasNext());
        assertEquals(XmlTokenStream.END_TAG, stream.next());
    }

    @Test(expected = XmlPullParserException.class)
    public void testMalformedXml() throws Exception {
        String xml = "<root><unclosed>";
        XmlTokenStream stream = new XmlTokenStream(new StringReader(xml));
        while (stream.hasNext()) {
            stream.next();
        }
    }

    @Test
    public void testInvalidGettersBeforeNextOrOutOfBound() throws Exception {
        XmlTokenStream stream = new XmlTokenStream(new StringReader("<root/>"));
        assertNull(stream.getName());
        assertNull(stream.getText());
        assertEquals(-1, stream.getAttributeCount());
        
        stream.next(); // START_TAG
        assertEquals("root", stream.getName());
        assertNull(stream.getAttributeName(0));
        assertNull(stream.getAttributeValue(0));
    }
}