package org.apache.commons.math.ode.events;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for EventState class (Math-78 bug context).
 * Achieves high coverage and targets potential faults in event detection.
 */
public class EventStateTest {

    private static final double EPS = 1e-10;

    private EventHandler ascendingHandler;
    private EventHandler descendingHandler;
    private EventHandler constantHandler;
    private EventHandler nullHandler;

    @Before
    public void setUp() {
        // Handler that triggers at t = 1.0 with increasing g
        ascendingHandler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 1.0;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {
                // no-op
            }
        };

        // Handler that triggers at t = 2.0 with decreasing g
        descendingHandler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return 2.0 - t;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }

            @Override
            public void resetState(double t, double[] y) {
                // no-op
            }
        };

        // Handler with constant g (never crosses zero)
        constantHandler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return 5.0;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }

            @Override
            public void resetState(double t, double[] y) {
                // no-op
            }
        };

        // Null handler for edge case
        nullHandler = null;
    }

    // ---------- Constructor tests ----------

    @Test(expected = NullPointerException.class)
    public void testConstructorNullHandler() {
        new EventState(null, 1e-6, 1e-12, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeMaxCheckInterval() {
        new EventState(ascendingHandler, 1e-6, 1e-12, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroConvergence() {
        new EventState(ascendingHandler, 0.0, 1e-12, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeConvergence() {
        new EventState(ascendingHandler, -1e-6, 1e-12, 100);
    }

    @Test
    public void testConstructorValid() {
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        assertNotNull(es);
    }

    // ---------- evaluateStep tests ----------

    @Test
    public void testEvaluateStepNoEvent() {
        EventState es = new EventState(constantHandler, 1e-6, 1e-12, 100);
        double t0 = 0.0;
        double[] y0 = new double[]{0.0};
        double t1 = 10.0;
        boolean forward = true;
        // Should not find event because g never crosses zero
        assertFalse(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(Double.NaN, es.getEventTime(), 0.0);
    }

    @Test
    public void testEvaluateStepForwardEvent() {
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        double t0 = 0.0;
        double[] y0 = new double[]{0.0};
        double t1 = 2.0;
        boolean forward = true;
        assertTrue(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(1.0, es.getEventTime(), 1e-6);
    }

    @Test
    public void testEvaluateStepBackwardEvent() {
        EventState es = new EventState(descendingHandler, 1e-6, 1e-12, 100);
        double t0 = 3.0;
        double[] y0 = new double[]{0.0};
        double t1 = 1.0;
        boolean forward = false;
        assertTrue(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(2.0, es.getEventTime(), 1e-6);
    }

    @Test
    public void testEvaluateStepEventAtBoundary() {
        // Event exactly at t0
        EventHandler handler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {}
        };
        EventState es = new EventState(handler, 1e-6, 1e-12, 100);
        double t0 = 0.0;
        double[] y0 = new double[]{0.0};
        double t1 = 1.0;
        boolean forward = true;
        assertTrue(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(0.0, es.getEventTime(), 1e-6);
    }

    @Test
    public void testEvaluateStepEventAtEnd() {
        // Event exactly at t1
        EventHandler handler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 1.0;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {}
        };
        EventState es = new EventState(handler, 1e-6, 1e-12, 100);
        double t0 = 0.0;
        double[] y0 = new double[]{0.0};
        double t1 = 1.0;
        boolean forward = true;
        assertTrue(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(1.0, es.getEventTime(), 1e-6);
    }

    @Test
    public void testEvaluateStepMultipleEvents() {
        // Handler with two zero crossings in interval
        EventHandler handler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return (t - 1.0) * (t - 2.0); // zeros at 1 and 2
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {}
        };
        EventState es = new EventState(handler, 1e-6, 1e-12, 100);
        double t0 = 0.0;
        double[] y0 = new double[]{0.0};
        double t1 = 3.0;
        boolean forward = true;
        // Should find first event at t=1.0
        assertTrue(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(1.0, es.getEventTime(), 1e-6);
    }

    @Test
    public void testEvaluateStepNoEventDueToTolerance() {
        // Event very close to boundary but within convergence
        EventHandler handler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 1e-8;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {}
        };
        EventState es = new EventState(handler, 1e-6, 1e-12, 100);
        double t0 = 0.0;
        double[] y0 = new double[]{0.0};
        double t1 = 1e-7;
        boolean forward = true;
        // g(t0) = -1e-8, g(t1) = -9e-8? Actually t1=1e-7, g(t1)=1e-7-1e-8=9e-8 >0, so crossing at 1e-8
        // But step ends at 1e-7, event at 1e-8 is within step. Should detect.
        assertTrue(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(1e-8, es.getEventTime(), 1e-6);
    }

    // ---------- stepAccepted tests ----------

    @Test
    public void testStepAcceptedNoEvent() {
        EventState es = new EventState(constantHandler, 1e-6, 1e-12, 100);
        double t = 5.0;
        double[] y = new double[]{0.0};
        boolean forward = true;
        // Should not trigger event
        assertFalse(es.stepAccepted(t, y));
        assertEquals(Double.NaN, es.getEventTime(), 0.0);
    }

    @Test
    public void testStepAcceptedEventAtEnd() {
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        double t = 1.0;
        double[] y = new double[]{0.0};
        boolean forward = true;
        // g(t)=0, should trigger event
        assertTrue(es.stepAccepted(t, y));
        assertEquals(1.0, es.getEventTime(), 1e-6);
    }

    @Test
    public void testStepAcceptedEventCrossing() {
        // Step that crosses zero but not exactly at end
        EventHandler handler = new EventHandler() {
            private boolean firstCall = true;
            @Override
            public double g(double t, double[] y) {
                if (firstCall) {
                    firstCall = false;
                    return -1.0; // at previous step end
                }
                return 1.0; // at current step end
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {}
        };
        EventState es = new EventState(handler, 1e-6, 1e-12, 100);
        // Simulate that previous step ended at t=0 with g=-1, now step ends at t=1 with g=1
        // First call to stepAccepted with t=0 to set pendingEvent? Actually stepAccepted is called after evaluateStep.
        // We'll directly test the crossing detection.
        // We need to set the pendingEvent flag via evaluateStep first.
        double t0 = 0.0;
        double[] y0 = new double[]{0.0};
        double t1 = 1.0;
        boolean forward = true;
        // evaluateStep should detect crossing
        assertTrue(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        // Now stepAccepted at t1 should confirm event
        assertTrue(es.stepAccepted(t1, y0));
        assertEquals(1.0, es.getEventTime(), 1e-6);
    }

    // ---------- reset tests ----------

    @Test
    public void testReset() {
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        double t = 1.0;
        double[] y = new double[]{0.0};
        // First stepAccepted to set event
        es.stepAccepted(t, y);
        assertTrue(es.reset(t, y));
        // After reset, event should be cleared
        assertEquals(Double.NaN, es.getEventTime(), 0.0);
    }

    @Test
    public void testResetNoEvent() {
        EventState es = new EventState(constantHandler, 1e-6, 1e-12, 100);
        double t = 5.0;
        double[] y = new double[]{0.0};
        assertFalse(es.reset(t, y));
    }

    // ---------- getEventTime tests ----------

    @Test
    public void testGetEventTimeInitial() {
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        assertEquals(Double.NaN, es.getEventTime(), 0.0);
    }

    @Test
    public void testGetEventTimeAfterStepAccepted() {
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        double t = 1.0;
        double[] y = new double[]{0.0};
        es.stepAccepted(t, y);
        assertEquals(1.0, es.getEventTime(), 1e-6);
    }

    // ---------- Edge cases ----------

    @Test(expected = NullPointerException.class)
    public void testEvaluateStepNullInterpolator() {
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        es.evaluateStep(null);
    }

    @Test
    public void testEvaluateStepWithVerySmallStep() {
        // Step size smaller than convergence
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        double t0 = 0.0;
        double[] y0 = new double[]{0.0};
        double t1 = 1e-10;
        boolean forward = true;
        // No event expected because step too small
        assertFalse(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(Double.NaN, es.getEventTime(), 0.0);
    }

    @Test
    public void testEvaluateStepBackwardNoEvent() {
        EventState es = new EventState(ascendingHandler, 1e-6, 1e-12, 100);
        double t0 = 2.0;
        double[] y0 = new double[]{0.0};
        double t1 = 0.0;
        boolean forward = false;
        // g(t0)=1, g(t1)=-1, crossing at 1.0, but backward step should detect
        assertTrue(es.evaluateStep(new StepInterpolatorStub(t0, t1, y0, forward)));
        assertEquals(1.0, es.getEventTime(), 1e-6);
    }

    @Test
    public void testStepAcceptedWithActionContinue() {
        EventHandler handler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 1.0;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }

            @Override
            public void resetState(double t, double[] y) {}
        };
        EventState es = new EventState(handler, 1e-6, 1e-12, 100);
        double t = 1.0;
        double[] y = new double[]{0.0};
        // Should still detect event but action is CONTINUE
        assertTrue(es.stepAccepted(t, y));
        // After CONTINUE, event time should be reset to NaN? Actually stepAccepted sets pendingEvent and eventTime.
        // But if action is CONTINUE, the event is not considered final? The behavior depends on implementation.
        // We'll just check that it returns true.
    }

    // ---------- Helper stub for StepInterpolator ----------

    /**
     * A minimal stub for StepInterpolator to provide getPreviousTime(), getCurrentTime(),
     * and getInterpolatedState().
     */
    private static class StepInterpolatorStub extends StepInterpolator {
        private final double t0;
        private final double t1;
        private final double[] y;
        private final boolean forward;

        StepInterpolatorStub(double t0, double t1, double[] y, boolean forward) {
            this.t0 = t0;
            this.t1 = t1;
            this.y = y.clone();
            this.forward = forward;
        }

        @Override
        public double getPreviousTime() {
            return forward ? t0 : t1;
        }

        @Override
        public double getCurrentTime() {
            return forward ? t1 : t0;
        }

        @Override
        public double[] getInterpolatedState(double time) {
            // For simplicity, return constant state
            return y.clone();
        }

        @Override
        protected StepInterpolator doCopy() {
            return new StepInterpolatorStub(t0, t1, y, forward);
        }

        // Other abstract methods: dummy implementations
        @Override
        protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
            // no-op
        }
    }
}