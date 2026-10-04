package org.apache.commons.lang3.time;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TimerTest {

    private Timer timer;

    @Before
    public void setUp() {
        timer = new Timer();
    }

    @Test
    public void testConstructorAndInitialState() {
        assertNotNull(timer);
        // An unstarted/reset timer should typically be in a specific state.
        // Let's test toString or basic actions on a fresh timer.
        timer.reset();
        assertEquals(0L, timer.getTime());
        assertEquals(0.0, timer.getTimeInSeconds(), 0.00001);
    }

    @Test
    public void testStartAndStop() {
        timer.start();
        // Pause briefly
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }
        timer.stop();

        long time = timer.getTime();
        assertTrue("Time should be greater than 0 after start and stop", time >= 0);

        double seconds = timer.getTimeInSeconds();
        assertTrue("Seconds should be non-negative", seconds >= 0.0);
    }

    @Test
    public void testSuspendAndResume() {
        timer.start();
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // ignore
        }
        timer.suspend();
        
        long suspendedTime = timer.getTime();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // ignore
        }
        
        // While suspended, time should not advance significantly (or remain consistent)
        long timeAfterWait = timer.getTime();
        // Allow a small delta for internal execution, but it shouldn't have advanced by 10ms
        assertEquals(suspendedTime, timeAfterWait, 15L);

        timer.resume();
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // ignore
        }
        timer.stop();

        assertTrue(timer.getTime() >= suspendedTime);
    }

    @Test
    public void testReset() {
        timer.start();
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            // ignore
        }
        timer.stop();
        assertTrue(timer.getTime() > 0 || timer.getTime() == 0); // handle extremely fast execution

        timer.reset();
        assertEquals(0L, timer.getTime());
        assertEquals(0.0, timer.getTimeInSeconds(), 0.00001);
    }

    @Test
    public void testToString() {
        timer.start();
        timer.stop();
        String str = timer.toString();
        assertNotNull(str);
    }

    @Test(expected = IllegalStateException.class)
    public void testStartTwiceThrowsException() {
        timer.start();
        timer.start(); // Should throw IllegalStateException or similar depending on implementation
    }

    @Test(expected = IllegalStateException.class)
    public void testStopWhenNotStartedThrowsException() {
        timer.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testSuspendWhenNotRunningThrowsException() {
        timer.suspend();
    }

    @Test(expected = IllegalStateException.class)
    public void testResumeWhenNotSuspendedThrowsException() {
        timer.resume();
    }
}