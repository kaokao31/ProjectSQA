package org.jsoup;

import org.junit.Test;
import java.io.IOException;
import static org.junit.Assert.*;

public class UncheckedIOExceptionTest {

    @Test(expected = UncheckedIOException.class)
    public void testConstructorWithNullCause() {
        throw new UncheckedIOException(null);
    }

    @Test(expected = UncheckedIOException.class)
    public void testConstructorWithIOExceptionCause() {
        throw new UncheckedIOException(new IOException("test io exception"));
    }

    @Test
    public void testGetCauseReturnsIOException() {
        IOException ioException = new IOException("original");
        UncheckedIOException unchecked = new UncheckedIOException(ioException);
        assertNotNull("Cause should not be null", unchecked.getCause());
        assertTrue("Cause should be IOException", unchecked.getCause() instanceof IOException);
        assertEquals("Cause message should match", "original", unchecked.getCause().getMessage());
    }

    @Test
    public void testGetCauseReturnsNullWhenNullPassed() {
        UncheckedIOException unchecked = new UncheckedIOException(null);
        assertNull("Cause should be null", unchecked.getCause());
    }

    @Test
    public void testExceptionMessage() {
        IOException ioException = new IOException("test message");
        UncheckedIOException unchecked = new UncheckedIOException(ioException);
        assertTrue("Exception message should contain original message", 
                   unchecked.getMessage().contains("test message"));
    }

    @Test
    public void testExceptionMessageWithNullCause() {
        UncheckedIOException unchecked = new UncheckedIOException(null);
        assertNull("Exception message should be null", unchecked.getMessage());
    }

    @Test
    public void testSerialization() {
        IOException ioException = new IOException("serialization test");
        UncheckedIOException original = new UncheckedIOException(ioException);
        // Verify the exception can be thrown and caught
        try {
            throw original;
        } catch (UncheckedIOException e) {
            assertEquals("Original and caught exception should be same", original, e);
            assertTrue("Caught exception cause should be IOException", 
                      e.getCause() instanceof IOException);
            assertEquals("Caught exception cause message should match", 
                        "serialization test", e.getCause().getMessage());
        }
    }

    @Test
    public void testMultipleInstances() {
        IOException io1 = new IOException("first");
        IOException io2 = new IOException("second");
        UncheckedIOException u1 = new UncheckedIOException(io1);
        UncheckedIOException u2 = new UncheckedIOException(io2);
        
        assertNotSame("Different instances should not be same", u1, u2);
        assertEquals("First cause message", "first", u1.getCause().getMessage());
        assertEquals("Second cause message", "second", u2.getCause().getMessage());
    }

    @Test(expected = UncheckedIOException.class)
    public void testThrowWithIOExceptionSubclass() {
        throw new UncheckedIOException(new java.io.FileNotFoundException("file not found"));
    }

    @Test
    public void testCauseIsIOExceptionSubclass() {
        java.io.FileNotFoundException fnf = new java.io.FileNotFoundException("missing file");
        UncheckedIOException unchecked = new UncheckedIOException(fnf);
        assertTrue("Cause should be IOException subclass", 
                  unchecked.getCause() instanceof java.io.FileNotFoundException);
        assertEquals("Cause message should match", "missing file", unchecked.getCause().getMessage());
    }

    @Test
    public void testInitCauseReturnsSameException() {
        IOException ioException = new IOException("init cause test");
        UncheckedIOException unchecked = new UncheckedIOException(ioException);
        Throwable result = unchecked.initCause(new IOException("should not change"));
        assertSame("initCause should return same exception", unchecked, result);
        // The cause should remain the original IOException
        assertTrue("Cause should still be original IOException", 
                  unchecked.getCause() instanceof IOException);
        assertEquals("Original cause message preserved", "init cause test", 
                    unchecked.getCause().getMessage());
    }
}