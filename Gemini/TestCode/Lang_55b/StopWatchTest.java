package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test cases for {@link StopWatch}.
 */
public class StopWatchTest {

    @Test
    public void testStopWatchSimple() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Thread.sleep(550);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.stop();
        long time = watch.getTime();
        assertEquals(time, watch.getTime());

        assertTrue(time >= 500);
        assertTrue(time < 700);

        watch.reset();
        assertEquals(0, watch.getTime());
    }

    @Test
    public void testStopWatchSimpleGet() {
        StopWatch watch = new StopWatch();
        assertEquals(0, watch.getTime());
        assertEquals("0:00:00.000", watch.toString());

        watch.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            // ignore
        }
        assertTrue(watch.getTime() < 2000);
    }

    @Test
    public void testStopWatchSplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Thread.sleep(550);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.split();
        long splitTime = watch.getSplitTime();
        String splitStr = watch.toSplitString();

        try {
            Thread.sleep(550);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.unsplit();
        try {
            Thread.sleep(550);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.stop();
        long totalTime = watch.getTime();

        assertTrue("splitTime < 500: " + splitTime, splitTime >= 500);
        assertTrue("splitTime > 700: " + splitTime, splitTime < 700);
        assertTrue("totalTime < 1500: " + totalTime, totalTime >= 1500);
        assertTrue("totalTime > 1900: " + totalTime, totalTime < 1900);
        assertNotNull(splitStr);
    }

    @Test
    public void testStopWatchSuspend() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Thread.sleep(550);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.suspend();
        long suspendTime = watch.getTime();
        try {
            Thread.sleep(550);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.resume();
        try {
            Thread.sleep(550);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.stop();
        long totalTime = watch.getTime();

        assertTrue(suspendTime >= 500);
        assertTrue(suspendTime < 700);
        assertTrue(totalTime >= 1000);
        assertTrue(totalTime < 1300);
    }

    @Test
    public void testLang315() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.suspend();
        long suspendTime = watch.getTime();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            // ignore
        }
        watch.stop();
        long totalTime = watch.getTime();
        assertTrue(totalTime == suspendTime);
    }

    @Test
    public void testBadStates() {
        StopWatch watch = new StopWatch();

        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.suspend();
            fail("Calling suspend on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.resume();
            fail("Calling resume on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.split();
            fail("Calling split on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.getSplitTime();
            fail("Calling getSplitTime on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.toSplitString();
            fail("Calling toSplitString on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        watch.start();

        try {
            watch.start();
            fail("Calling start on an already started StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.getSplitTime();
            fail("Calling getSplitTime on an unsplit StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.toSplitString();
            fail("Calling toSplitString on an unsplit StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        watch.split();

        try {
            watch.split();
            fail("Calling split on an already split StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        watch.unsplit();
        watch.suspend();

        try {
            watch.suspend();
            fail("Calling suspend on an already suspended StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.split();
            fail("Calling split on a suspended StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on a suspended StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        watch.resume();
        watch.stop();

        try {
            watch.start();
            fail("Calling start on a stopped StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.stop();
            fail("Calling stop on a stopped StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.suspend();
            fail("Calling suspend on a stopped StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.resume();
            fail("Calling resume on a stopped StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.split();
            fail("Calling split on a stopped StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on a stopped StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
        }
    }
}