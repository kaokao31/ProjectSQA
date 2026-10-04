package org.apache.commons.compress.archivers.sevenz;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class SevenZFileTest {

    @Test(expected = IOException.class)
    public void testConstructorWithNullFile() throws IOException {
        File nullFile = null;
        new SevenZFile(nullFile);
    }

    @Test(expected = IOException.class)
    public void testConstructorWithNonExistentFile() throws IOException {
        File nonExistent = new File("non_existent_file_123456789.7z");
        new SevenZFile(nonExistent);
    }

    @Test(expected = IOException.class)
    public void testConstructorWithNullFileAndCharset() throws IOException {
        new SevenZFile(null, "UTF-8");
    }

    @Test(expected = IOException.class)
    public void testConstructorWithNonExistentFileAndCharset() throws IOException {
        File nonExistent = new File("non_existent_file_123456789.7z");
        new SevenZFile(nonExistent, "UTF-8");
    }

    @Test
    public void testMatchesWithNullBuffer() {
        byte[] buffer = null;
        boolean matches = SevenZFile.matches(buffer, 0);
        assertFalse(matches);
    }

    @Test
    public void testMatchesWithShortBuffer() {
        byte[] buffer = new byte[2];
        boolean matches = SevenZFile.matches(buffer, 2);
        assertFalse(matches);
    }

    @Test
    public void testMatchesWithCorrectSignature() {
        // SevenZ signature: '7', 'z', 0xBC, 0xAF, 0x27, 0x1C
        byte[] buffer = new byte[] {
            (byte)'7', (byte)'z', (byte)0xBC, (byte)0xAF, (byte)0x27, (byte)0x1C, 
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        boolean matches = SevenZFile.matches(buffer, buffer.length);
        assertTrue(matches);
    }

    @Test
    public void testMatchesWithIncorrectSignature() {
        byte[] buffer = new byte[] {
            (byte)'P', (byte)'K', 0x03, 0x04, 0x14, 0x00, 
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        boolean matches = SevenZFile.matches(buffer, buffer.length);
        assertFalse(matches);
    }
}