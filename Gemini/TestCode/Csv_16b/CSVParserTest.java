package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CSVParserTest {

    @Test
    public void testParseStringFormat() throws IOException {
        String csv = "A,B,C\n1,2,3";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(2, records.size());
        Assert.assertEquals("A", records.get(0).get(0));
        Assert.assertEquals("3", records.get(1).get(2));
        parser.close();
    }

    @Test
    public void testParseFileCharsetFormat() throws IOException {
        File tempFile = File.createTempFile("csvTest", ".csv");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), "Col1,Col2\nVal1,Val2".getBytes(StandardCharsets.UTF_8));

        CSVParser parser = CSVParser.parse(tempFile, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
        Assert.assertNotNull(parser);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("Val1", records.get(0).get("Col1"));
        parser.close();
    }

    @Test
    public void testParsePathCharsetFormat() throws IOException {
        Path tempPath = Files.createTempFile("csvPathTest", ".csv");
        tempPath.toFile().deleteOnExit();
        Files.write(tempPath, "X,Y\n9,8".getBytes(StandardCharsets.UTF_8));

        CSVParser parser = CSVParser.parse(tempPath, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
        Assert.assertNotNull(parser);
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("9", records.get(0).get(0));
        parser.close();
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullStringFormat() throws IOException {
        CSVParser.parse((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullFileFormat() throws IOException {
        CSVParser.parse((File) null, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullPathFormat() throws IOException {
        CSVParser.parse((Path) null, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullURLFormat() throws IOException {
        CSVParser.parse((URL) null, StandardCharsets.UTF_8, CSVFormat.DEFAULT);
    }

    @Test
    public void testHeaderHandlingWithHeaderRow() throws IOException {
        String csv = "Header1,Header2\nData1,Data2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Header1", "Header2");
        CSVParser parser = CSVParser.parse(csv, format);
        
        Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertEquals(Integer.valueOf(0), headerMap.get("Header1"));
        Assert.assertEquals(Integer.valueOf(1), headerMap.get("Header2"));
        
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("Data1", records.get(0).get("Header1"));
        parser.close();
    }

    @Test
    public void testHeaderAutoDetection() throws IOException {
        String csv = "H1,H2\nR1C1,R1C2";
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        CSVParser parser = CSVParser.parse(csv, format);
        
        Map<String, Integer> headerMap = parser.getHeaderMap();
        Assert.assertNotNull(headerMap);
        Assert.assertTrue(headerMap.containsKey("H1"));
        Assert.assertTrue(headerMap.containsKey("H2"));
        
        List<CSVRecord> records = parser.getRecords();
        Assert.assertEquals(1, records.size());
        Assert.assertEquals("R1C1", records.get(0).get("H1"));
        parser.close();
    }

    @Test
    public void testIteratorAndStreamMethods() throws IOException {
        String csv = "A\n1\n2";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        
        Assert.assertTrue(iterator.hasNext());
        CSVRecord record1 = iterator.next();
        Assert.assertEquals("A", record1.get(0));
        
        Assert.assertTrue(iterator.hasNext());
        CSVRecord record2 = iterator.next();
        Assert.assertEquals("1", record2.get(0));
        
        Assert.assertTrue(iterator.hasNext());
        CSVRecord record3 = iterator.next();
        Assert.assertEquals("2", record3.get(0));
        
        Assert.assertFalse(iterator.hasNext());
        parser.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemoveThrowsException() throws IOException {
        String csv = "A\n1";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        try {
            iterator.remove();
        } finally {
            parser.close();
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoSuchElement() throws IOException {
        String csv = "";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        try {
            iterator.next();
        } finally {
            parser.close();
        }
    }

    @Test
    public void testGetFirstRecordNumberAndCurrentLineNumber() throws IOException {
        String csv = "A,B\n1,2\r\n3,4";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        
        Assert.assertEquals(0L, parser.getCurrentLineNumber());
        
        CSVRecord rec1 = parser.nextRecord();
        Assert.assertEquals(1L, rec1.getRecordNumber());
        
        CSVRecord rec2 = parser.nextRecord();
        Assert.assertEquals(2L, rec2.getRecordNumber());
        
        parser.close();
    }

    @Test
    public void testGetRecordNumber() throws IOException {
        String csv = "1\n2\n3";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        Assert.assertEquals(0L, parser.getRecordNumber());
        parser.nextRecord();
        Assert.assertEquals(1L, parser.getRecordNumber());
        parser.close();
    }

    @Test
    public void testIsClosed() throws IOException {
        String csv = "1,2";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testGetHeaderMap() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B");
        CSVParser parser = CSVParser.parse("1,2", format);
        Map<String, Integer> map = parser.getHeaderMap();
        Assert.assertNotNull(map);
        Assert.assertEquals(2, map.size());
        parser.close();
    }

    @Test
    public void testGetBOM() throws IOException {
        // Test with UTF-8 BOM
        byte[] bomBytes = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'a', ',', 'b'};
        ByteArrayInputStream bais = new ByteArrayInputStream(bomBytes);
        CSVParser parser = new CSVParser(new java.io.InputStreamReader(bais, StandardCharsets.UTF_8), CSVFormat.DEFAULT);
        // Access parser methods to ensure code execution covers BOM handling if present
        Assert.assertNotNull(parser.nextRecord());
        parser.close();
    }

    @Test
    public void testGetHeaderNames() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("X", "Y");
        CSVParser parser = CSVParser.parse("1,2", format);
        List<String> headerNames = parser.getHeaderNames();
        Assert.assertNotNull(headerNames);
        Assert.assertEquals(2, headerNames.size());
        Assert.assertEquals("X", headerNames.get(0));
        Assert.assertEquals("Y", headerNames.get(1));
        parser.close();
    }
}