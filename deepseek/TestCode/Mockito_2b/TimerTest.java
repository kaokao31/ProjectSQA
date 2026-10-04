package org.example; // Placeholder - replace with actual package from source

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for Timer class.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class TimerTest {

    private Timer timer;

    @Before
    public void setUp() {
        timer = new Timer();
    }

    // --- Basic start/stop and elapsed time ---

    @Test
    public void testInitialState() {
        assertFalse("Timer should not be running initially", timer.isRunning());
        assertEquals("Elapsed time should be 0 initially", 0, timer.getElapsedTime());
    }

    @Test
    public void testStartAndStop() {
        timer.start();
        assertTrue("Timer should be running after start", timer.isRunning());
        timer.stop();
        assertFalse("Timer should not be running after stop", timer.isRunning());
        assertTrue("Elapsed time should be positive after stop", timer.getElapsedTime() > 0);
    }

    @Test
    public void testElapsedTimeIncreases() throws InterruptedException {
        timer.start();
        Thread.sleep(100); // allow some time to pass
        long elapsed1 = timer.getElapsedTime();
        Thread.sleep(100);
        long elapsed2 = timer.getElapsedTime();
        assertTrue("Elapsed time should increase", elapsed2 > elapsed1);
        timer.stop();
    }

    @Test
    public void testStopWithoutStart() {
        // Should not throw exception and elapsed time remains 0
        timer.stop();
        assertEquals("Elapsed time should be 0 if never started", 0, timer.getElapsedTime());
    }

    @Test
    public void testMultipleStartStop() {
        timer.start();
        timer.stop();
        long firstElapsed = timer.getElapsedTime();
        timer.start();
        timer.stop();
        long secondElapsed = timer.getElapsedTime();
        assertTrue("Second elapsed should be >= first (accumulated)", secondElapsed >= firstElapsed);
    }

    // --- Reset ---

    @Test
    public void testReset() {
        timer.start();
        timer.stop();
        assertTrue("Elapsed time should be >0 before reset", timer.getElapsedTime() > 0);
        timer.reset();
        assertEquals("Elapsed time should be 0 after reset", 0, timer.getElapsedTime());
        assertFalse("Timer should not be running after reset", timer.isRunning());
    }

    @Test
    public void testResetWhileRunning() {
        timer.start();
        timer.reset();
        assertFalse("Timer should not be running after reset", timer.isRunning());
        assertEquals("Elapsed time should be 0 after reset", 0, timer.getElapsedTime());
    }

    // --- Edge cases and potential bugs ---

    @Test
    public void testStartTwice() {
        timer.start();
        // Starting again should not throw or reset time
        timer.start();
        assertTrue("Timer should still be running", timer.isRunning());
        timer.stop();
        assertTrue("Elapsed time should be positive", timer.getElapsedTime() > 0);
    }

    @Test
    public void testStopTwice() {
        timer.start();
        timer.stop();
        long elapsedAfterFirstStop = timer.getElapsedTime();
        timer.stop(); // second stop should be harmless
        assertEquals("Elapsed time should not change after second stop", elapsedAfterFirstStop, timer.getElapsedTime());
        assertFalse("Timer should remain stopped", timer.isRunning());
    }

    @Test
    public void testGetElapsedTimeWhileRunning() {
        timer.start();
        long elapsed = timer.getElapsedTime();
        assertTrue("Elapsed time should be >=0 while running", elapsed >= 0);
        timer.stop();
    }

    @Test
    public void testPrecisionAndOverflow() {
        // Simulate long running timer (within test limits)
        timer.start();
        // We cannot actually wait for overflow, but we can test that elapsed time is non-negative
        timer.stop();
        assertTrue("Elapsed time should be non-negative", timer.getElapsedTime() >= 0);
    }

    @Test
    public void testConcurrentModification() {
        // Basic thread safety check: start/stop from different threads
        final Timer t = new Timer();
        Thread t1 = new Thread(() -> {
            t.start();
            try { Thread.sleep(50); } catch (InterruptedException e) {}
            t.stop();
        });
        Thread t2 = new Thread(() -> {
            t.start();
            try { Thread.sleep(30); } catch (InterruptedException e) {}
            t.stop();
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }
        // After concurrent operations, timer should be in a consistent state
        assertFalse("Timer should not be running after concurrent operations", t.isRunning());
        assertTrue("Elapsed time should be non-negative", t.getElapsedTime() >= 0);
    }

    // --- Additional coverage: null/edge inputs (if any) ---
    // Timer class likely has no method parameters, so no null checks needed.

    @Test
    public void testToString() {
        // If Timer has toString, ensure it doesn't throw
        assertNotNull("toString should not return null", timer.toString());
    }

    @Test
    public void testHashCodeAndEquals() {
        // If Timer overrides equals/hashCode, test consistency
        Timer t2 = new Timer();
        assertEquals("Two new timers should be equal", timer, t2);
        assertEquals("Hash codes should be equal", timer.hashCode(), t2.hashCode());
        timer.start();
        timer.stop();
        assertFalse("Timer after use should not equal new timer", timer.equals(t2));
    }
}