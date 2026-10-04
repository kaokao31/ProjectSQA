package org.apache.commons.lang.time;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for {@link StopWatch}.
 * Designed to achieve high code coverage and detect faults (e.g., Lang-55).
 */
public class StopWatchTest {

    private StopWatch watch;

    @Before
    public void setUp() {
        watch = new StopWatch();
    }

    // ----- Basic start/stop -----
    @Test
    public void testStartStopTime() throws InterruptedException {
        watch.start();
        Thread.sleep(50);
        watch.stop();
        long time = watch.getTime();
        assertTrue("Elapsed time should be >= 50", time >= 50);
    }

    @Test
    public void testStartStopZeroTime() {
        watch.start();
        watch.stop();
        long time = watch.getTime();
        assertTrue("Elapsed time should be >= 0", time >= 0);
    }

    // ----- Suspend/Resume -----
    @Test
    public void testSuspendResume() throws InterruptedException {
        watch.start();
        Thread.sleep(20);
        watch.suspend();
        Thread.sleep(20); // this should not count
        watch.resume();
        Thread.sleep(20);
        watch.stop();
        long time = watch.getTime();
        assertTrue("Elapsed time should be >= 40 but not include first suspend", time >= 40);
        assertTrue("Elapsed time should be less than 60", time < 60);
    }

    @Test
    public void testMultipleSuspendResume() throws InterruptedException {
        watch.start();
        Thread.sleep(20);
        watch.suspend();
        Thread.sleep(10); // not counted
        watch.resume();
        Thread.sleep(20);
        watch.suspend();
        Thread.sleep(10); // not counted
        watch.resume();
        Thread.sleep(20);
        watch.stop();
        long time = watch.getTime();
        assertTrue("Elapsed time should be >= 60", time >= 60);
        assertTrue("Elapsed time should be < 80", time < 80);
    }

    // ----- Split/Unsplit -----
    @Test
    public void testSplitUnsplit() throws InterruptedException {
        watch.start();
        Thread.sleep(20);
        watch.split();
        long splitTime = watch.getSplitTime();
        assertTrue("Split time should be >= 20", splitTime >= 20);
        Thread.sleep(20);
        watch.unsplit();
        watch.stop();
        long totalTime = watch.getTime();
        assertTrue("Total time should be >= 40", totalTime >= 40);
    }

    @Test(expected = IllegalStateException.class)
    public void testSplitWhenNotRunning() {
        watch.split();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnsplitWhenNotSplit() {
        watch.start();
        watch.unsplit(); // not yet split
    }

    // ----- Reset -----
    @Test
    public void testReset() throws InterruptedException {
        watch.start();
        Thread.sleep(50);
        watch.stop();
        long timeBeforeReset = watch.getTime();
        assertTrue("Time before reset >= 50", timeBeforeReset >= 50);
        watch.reset();
        assertEquals("After reset time should be 0", 0, watch.getTime());
        watch.start();
        Thread.sleep(20);
        watch.stop();
        long timeAfterReset = watch.getTime();
        assertTrue("Time after second start should be >= 20", timeAfterReset >= 20);
    }

    // ----- State transition exceptions -----
    @Test(expected = IllegalStateException.class)
    public void testStopWithoutStart() {
        watch.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testSuspendWithoutStart() {
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class)
    public void testResumeWithoutSuspend() {
        watch.start();
        watch.resume(); // not suspended yet
    }

    @Test(expected = IllegalStateException.class)
    public void testSuspendWhenStopped() {
        watch.start();
        watch.stop();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class)
    public void testResumeWhenRunning() {
        watch.start();
        watch.resume(); // already running, not suspended
    }

    // ----- Bug-specific test for Lang-55 (suspension state handling) -----
    @Test
    public void testLang315() throws InterruptedException {
        // This test is designed to reproduce the failure described in Lang-55.
        // Sequence: start, suspend, resume, suspend, resume, stop.
        watch.start();
        Thread.sleep(50);
        watch.suspend();
        Thread.sleep(10); // should be ignored
        watch.resume();
        Thread.sleep(50);
        watch.suspend();
        Thread.sleep(10); // should be ignored
        watch.resume();
        Thread.sleep(50);
        watch.stop();
        long time = watch.getTime();
        // Expected elapsed: ~150ms (three active periods)
        assertTrue("Elapsed time should be >= 150", time >= 150);
        // Allow some tolerance for scheduling variations
        assertTrue("Elapsed time should be < 180", time < 180);
    }

    // ----- Additional edge cases -----
    @Test
    public void testGetStartTime() {
        watch.start();
        long startTime = watch.getStartTime();
        assertTrue("Start time should be > 0", startTime > 0);
        watch.stop();
    }

    @Test
    public void testTimeWhileRunning() throws InterruptedException {
        watch.start();
        Thread.sleep(30);
        long timeWhileRunning = watch.getTime();
        assertTrue("Time while running should be >= 30", timeWhileRunning >= 30);
        watch.stop();
    }

    @Test
    public void testSplitTimeAfterUnsplit() throws InterruptedException {
        watch.start();
        Thread.sleep(20);
        watch.split();
        watch.unsplit();
        long splitTime = watch.getSplitTime();
        // After unsplit, split time should still be available (previous split)
        assertTrue("Split time after unsplit should still be available", splitTime >= 20);
        watch.stop();
    }

    @Test
    public void testStopAfterUnsplit() throws InterruptedException {
        watch.start();
        Thread.sleep(20);
        watch.split();
        Thread.sleep(10);
        watch.unsplit();
        watch.stop();
        long total = watch.getTime();
        assertTrue("Total time after unsplit should be >= 30", total >= 30);
    }

    // ----- Concurrent use (not fully, but state consistency) -----
    @Test(expected = IllegalStateException.class)
    public void testStartTwice() {
        watch.start();
        watch.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStopTwice() {
        watch.start();
        watch.stop();
        watch.stop();
    }

    @Test
    public void testEmptyStopWatch() {
        assertEquals("Initial time should be 0", 0, watch.getTime());
        assertFalse("Initial state should not be running", watch.isStarted());
    }
}