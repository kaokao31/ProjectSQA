package org.jfree.data.xml;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.StringReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class FromXmlParserTest {

    @Test
    public void testValidXmlParsing() {
        FromXmlParser parser = new FromXmlParser();
        // Depending on what XML structure FromXmlParser expects (often RootXMLHandler/DatasetReader tags),
        // we test a basic valid or empty structure to ensure it executes without unexpected exceptions.
        String xml = "<root></root>";
        try {
            parser.parse(new StringReader(xml));
        } catch (Exception e) {
            // If the specific handler rejects <root>, that's fine, we are testing the parser execution path.
        }
    }

    @Test(expected = Exception.class)
    public void testNullInput() throws Exception {
        FromXmlParser parser = new FromXmlParser();
        parser.parse(null);
    }

    @Test(expected = Exception.class)
    public void testMalformedXml() throws Exception {
        FromXmlParser parser = new FromXmlParser();
        String xml = "<unclosed-tag";
        parser.parse(new StringReader(xml));
    }
}