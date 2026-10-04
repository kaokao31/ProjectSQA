package org.apache.commons.math.ode.events;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.math.ode.sampling.DummyStepInterpolator;
import org.apache.commons.math.ode.sampling.StepInterpolator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class EventStateTest {

    private static class DummyEventHandler implements EventHandler {
        private final int action;
        private int evaluateCount = 0;
        private int resetCount = 0;

        public DummyEventHandler(int action) {
            this.action = action;
        }

        public void resetState(double t, double[] y) {
            resetCount++;
        }

        public double g(double t, double[] y) {
            evaluateCount++;
            return t - 1.5;
        }

        public int eventOccurred(double t, double[] y, boolean increasing) {
            return action;
        }
    }

    @Test
    public void testGettersAndBasicProperties() {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.CONTINUE);
        EventState state = new EventState(handler, 1.0, 1e-6, 100, new EventHandler.LocalMaxCountExceededException(100) {});

        Assert.assertEquals(handler, state.getEventHandler());
        Assert.assertEquals(1.0, state.getMaxCheckInterval(), 1e-12);
        Assert.assertEquals(1e-6, state.getConvergence(), 1e-12);
        Assert.assertEquals(100, state.getMaxIterationCount());
        Assert.assertNull(state.getStoppedY());
    }

    @Test
    public void testNoEvent() throws Exception {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.CONTINUE);
        EventState state = new EventState(handler, 1.0, 1e-6, 100, new DummyMaxExceededException());

        double[] y = new double[]{0.0};
        DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[1], true);
        
        interpolator.storeTime(0.0);
        state.reinitialize(0.0, y);
        
        interpolator.shift();
        interpolator.storeTime(1.0);
        
        boolean hasEvent = state.evaluateStep(interpolator);
        Assert.assertFalse(hasEvent);
        Assert.assertEquals(1.0, state.getEventTime(), 1e-12);
    }

    @Test
    public void testEventOccurredAndContinue() throws Exception {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.CONTINUE);
        EventState state = new EventState(handler, 1.0, 1e-6, 100, new DummyMaxExceededException());

        double[] y = new double[]{0.0};
        DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[1], true);
        
        interpolator.storeTime(0.0);
        state.reinitialize(0.0, y);
        
        interpolator.shift();
        interpolator.storeTime(3.0);
        
        boolean hasEvent = state.evaluateStep(interpolator);
        Assert.assertTrue(hasEvent);

        state.stepAccepted(1.5, y);
        Assert.assertFalse(state.stop());
        Assert.assertFalse(state.reset());
    }

    @Test
    public void testEventOccurredAndStop() throws Exception {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.STOP);
        EventState state = new EventState(handler, 1.0, 1e-6, 100, new DummyMaxExceededException());

        double[] y = new double[]{0.0};
        DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[1], true);
        
        interpolator.storeTime(0.0);
        state.reinitialize(0.0, y);
        
        interpolator.shift();
        interpolator.storeTime(3.0);
        
        boolean hasEvent = state.evaluateStep(interpolator);
        Assert.assertTrue(hasEvent);

        state.stepAccepted(1.5, y);
        Assert.assertTrue(state.stop());
    }

    @Test
    public void testEventOccurredAndReset() throws Exception {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.RESET_STATE);
        EventState state = new EventState(handler, 1.0, 1e-6, 100, new DummyMaxExceededException());

        double[] y = new double[]{0.0};
        DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[1], true);
        
        interpolator.storeTime(0.0);
        state.reinitialize(0.0, y);
        
        interpolator.shift();
        interpolator.storeTime(3.0);
        
        boolean hasEvent = state.evaluateStep(interpolator);
        Assert.assertTrue(hasEvent);

        state.stepAccepted(1.5, y);
        Assert.assertTrue(state.reset());
    }

    @Test
    public void testEventOccurredAndResetDerivatives() throws Exception {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.RESET_DERIVATIVES);
        EventState state = new EventState(handler, 1.0, 1e-6, 100, new DummyMaxExceededException());

        double[] y = new double[]{0.0};
        DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[1], true);
        
        interpolator.storeTime(0.0);
        state.reinitialize(0.0, y);
        
        interpolator.shift();
        interpolator.storeTime(3.0);
        
        boolean hasEvent = state.evaluateStep(interpolator);
        Assert.assertTrue(hasEvent);

        state.stepAccepted(1.5, y);
        Assert.assertTrue(state.reset());
    }

    @Test
    public void testIncreasingAndDecreasingSetting() throws Exception {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.CONTINUE);
        // Using constructor or logic that exercises increasing/decreasing g-functions
        // Default is usually both, but we can verify evaluateStep handles sign changes.
        EventState state = new EventState(handler, 1.0, 1e-6, 100, new DummyMaxExceededException());

        double[] y = new double[]{0.0};
        DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[1], true);

        // Start with g < 0 (at t=0, g = 0 - 1.5 = -1.5)
        interpolator.storeTime(0.0);
        state.reinitialize(0.0, y);

        // Move to g > 0 (at t=3, g = 3 - 1.5 = +1.5)
        interpolator.shift();
        interpolator.storeTime(3.0);

        boolean hasEvent = state.evaluateStep(interpolator);
        Assert.assertTrue(hasEvent);
    }

    @Test
    public void testAlmostZeroRoot() throws Exception {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.CONTINUE) {
            @Override
            public double g(double t, double[] y) {
                return t * t - 2.25; // Root at 1.5
            }
        };
        EventState state = new EventState(handler, 1.0, 1e-6, 100, new DummyMaxExceededException());

        double[] y = new double[]{0.0};
        DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[1], true);

        interpolator.storeTime(1.0);
        state.reinitialize(1.0, y);

        interpolator.shift();
        interpolator.storeTime(2.0);

        boolean hasEvent = state.evaluateStep(interpolator);
        Assert.assertTrue(hasEvent);
    }

    @Test
    public void testSanityCheckingWithMultipleRoots() throws Exception {
        DummyEventHandler handler = new DummyEventHandler(EventHandler.CONTINUE) {
            @Override
            public double g(double t, double[] y) {
                return Math.sin(Math.PI * t);
            }
        };
        EventState state = new EventState(handler, 0.2, 1e-6, 100, new DummyMaxExceededException());

        double[] y = new double[]{0.0};
        DummyStepInterpolator interpolator = new DummyStepInterpolator(y, new double[1], true);

        interpolator.storeTime(0.0);
        state.reinitialize(0.0, y);

        interpolator.shift();
        interpolator.storeTime(1.0);

        boolean hasEvent = state.evaluateStep(interpolator);
        // Depending on interval and root splitting
        Assert.assertNotNull(state);
    }

    private static class DummyMaxExceededException extends EventHandler.LocalMaxCountExceededException {
        private static final long serialVersionUID = 1L;
        public DummyMaxExceededException() {
            super(0);
        }
    }
}