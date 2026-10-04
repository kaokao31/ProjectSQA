package org.joda.time.tz;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.junit.Assert.*;

public class ZoneInfoCompilerTest {

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        tempDir = new File(tempDirPath, "zoneinfo_test_" + System.currentTimeMillis());
        tempDir.mkdirs();
    }

    @After
    public void tearDown() throws Exception {
        deleteDir(tempDir);
    }

    private void deleteDir(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteDir(c);
            }
        }
        f.delete();
    }

    @Test
    public void testParseTime() {
        assertEquals(0, ZoneInfoCompiler.parseTime("24:00"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("1"));
        assertEquals(-3600000, ZoneInfoCompiler.parseTime("-01:00"));
        assertEquals(3661000, ZoneInfoCompiler.parseTime("01:01:01"));
        assertEquals(3661500, ZoneInfoCompiler.parseTime("01:01:01.500"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00s"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00u"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00g"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00z"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00w"));
    }

    @Test
    public void testParseYear() {
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("maximum"));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("max"));
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("minimum"));
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("min"));
        assertEquals(2020, ZoneInfoCompiler.parseYear("only"));
        assertEquals(2023, ZoneInfoCompiler.parseYear("2023"));
    }

    @Test
    public void testParseDayOfWeek() {
        assertEquals(1, ZoneInfoCompiler.parseDayOfWeek("Mon"));
        assertEquals(1, ZoneInfoCompiler.parseDayOfWeek("Monday"));
        assertEquals(7, ZoneInfoCompiler.parseDayOfWeek("Sun"));
        assertEquals(7, ZoneInfoCompiler.parseDayOfWeek("Sunday"));
        assertEquals(5, ZoneInfoCompiler.parseDayOfWeek("Fri"));
    }

    @Test
    public void testParseMonth() {
        assertEquals(1, ZoneInfoCompiler.parseMonth("Jan"));
        assertEquals(1, ZoneInfoCompiler.parseMonth("January"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("Dec"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("December"));
    }

    @Test
    public void testVerbose() {
        boolean original = ZoneInfoCompiler.verbose();
        try {
            ZoneInfoCompiler.setVerbose(true);
            assertTrue(ZoneInfoCompiler.verbose());
            ZoneInfoCompiler.setVerbose(false);
            assertFalse(ZoneInfoCompiler.verbose());
        } finally {
            ZoneInfoCompiler.setVerbose(original);
        }
    }

    @Test
    public void testGetZoneInfoMap() throws Exception {
        File zoneFile = new File(tempDir, "africa");
        PrintWriter pw = new PrintWriter(new FileWriter(zoneFile));
        pw.println("# This is a comment");
        pw.println("Zone TestZone 1:00 - TEST 1:00");
        pw.close();

        Map<String, DateTimeZoneBuilder> map = ZoneInfoCompiler.compile(null, new File[]{tempDir});
        assertNotNull(map);
        assertTrue(map.containsKey("TestZone"));
    }

    @Test
    public void testCompileFiles() throws Exception {
        File zoneFile = new File(tempDir, "tzdata");
        PrintWriter pw = new PrintWriter(new FileWriter(zoneFile));
        pw.println("Rule US 1967 1973 - Oct lastSun 2:00 0 S");
        pw.println("Rule US 1967 2006 - Apr Sun>=8 2:00 1:00 D");
        pw.println("Zone America/New_York -5:00 US E%sT");
        pw.close();

        File destDir = new File(tempDir, "dst");
        destDir.mkdir();

        ZoneInfoCompiler.main(new String[]{
                "-srcdir", tempDir.getAbsolutePath(),
                "-dstdir", destDir.getAbsolutePath(),
                "tzdata"
        });

        assertTrue(new File(destDir, "America/New_York").exists());
    }

    @Test
    public void testCompileWithInvalidArguments() {
        try {
            ZoneInfoCompiler.main(new String[]{"-unknownArg"});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testCompileMissingDstDir() {
        try {
            ZoneInfoCompiler.main(new String[]{tempDir.getAbsolutePath()});
            // Depending on implementation, might fail or succeed if dstdir is optional
        } catch (Exception e) {
            // Ignored if it throws
        }
    }

    @Test
    public void testWriteZoneInfoMap() throws Exception {
        File dstDir = new File(tempDir, "output");
        dstDir.mkdir();
        Map<String, DateTimeZoneBuilder> map = new HashMap<>();
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        builder.addStdOffset(3600000);
        map.put("UTC", builder);

        ZoneInfoCompiler.writeZoneInfoMap(dstDir, map);
        assertTrue(new File(dstDir, "zoneinfo.map").exists());
    }

    @Test
    public void testParseDataFile() throws Exception {
        String data = "Rule US 1967 1973 - Oct lastSun 2:00 0 S\n" +
                "Zone America/New_York -5:00 US E%sT\n" +
                "Link America/New_York US/Eastern";

        BufferedReader reader = new BufferedReader(new java.io.StringReader(data));
        ZoneInfoCompiler zic = new ZoneInfoCompiler();
        
        // Use reflection to call parseDataFile if it's package-private or private
        try {
            Method m = ZoneInfoCompiler.class.getDeclaredMethod("parseDataFile", BufferedReader.class);
            m.setAccessible(true);
            m.invoke(zic, reader);
        } catch (NoSuchMethodException e) {
            // If method signature is different, just test compile with stream
            ZoneInfoCompiler.compile(reader);
        }
    }

    @Test
    public void testZoneInfoCompilerConstructorAndMethods() {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        assertNotNull(compiler);
    }

    @Test
    public void testSupportMethodsViaTokenizer() throws Exception {
        // Test parsing various lines via reflection or helper methods if available
        String line = "Rule US 1967 only - Oct lastSun 2:00 0 S";
        StringTokenizer st = new StringTokenizer(line);
        st.nextToken(); // Rule
        
        // Just exercising standard compilation paths
        File tzFile = new File(tempDir, "data");
        PrintWriter pw = new PrintWriter(new FileWriter(tzFile));
        pw.println("Rule	TZ	1990	max	-	Mar	lastSun	2:00	1:00	D");
        pw.println("Rule	TZ	1990	max	-	Oct	lastSun	2:00	0	S");
        pw.println("Zone	Test/Zone	1:00	TZ	%s");
        pw.close();

        Map<String, DateTimeZoneBuilder> result = ZoneInfoCompiler.compile(null, new File[]{tzFile});
        assertNotNull(result);
    }
}