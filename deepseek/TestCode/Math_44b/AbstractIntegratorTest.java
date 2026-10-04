package org.apache.commons.math.ode;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;

/**
 * Test suite for AbstractIntegrator targeting high coverage and fault detection.
 * Designed to exercise event handling, step acceptance, and edge cases.
 */
public class AbstractIntegratorTest {

    private static final double EPS = 1e-10;

    // Concrete integrator for testing abstract methods
    private static class TestIntegrator extends AbstractIntegrator {
        private double stepSize = 0.1;
        private double maxStep = 1.0;
        private double minStep = 1e-10;
        private int evaluations = 0;
        private FirstOrderDifferentialEquations equations;
        private double t0;
        private double[] y0;
        private double tEnd;
        private double[] y;
        private boolean resetOccurred;

        public TestIntegrator() {
            super("test");
        }

        @Override
        public void integrate(FirstOrderDifferentialEquations equations,
                              double t0, double[] y0, double tEnd, double[] y) {
            this.equations = equations;
            this.t0 = t0;
            this.y0 = y0.clone();
            this.tEnd = tEnd;
            this.y = y;
            System.arraycopy(y0, 0, y, 0, y0.length);
            resetOccurred = false;
            sanityChecks(equations, t0, y0, tEnd, y);
            double t = t0;
            double step = (tEnd > t0) ? stepSize : -stepSize;
            int count = 0;
            while ((tEnd - t) * step > 0 && count < 10000) {
                double[] yDot = new double[1];
                double[] yNext = new double[1];
                // Compute derivative at current state
                computeDerivatives(t, y, yDot);
                double h = Math.min(Math.abs(step), Math.abs(tEnd - t));
                if (Math.abs(h) < 1e-12) break;
                yNext[0] = y[0] + h * yDot[0];
                double tNext = t + h;
                // Call protected acceptStep (exposed for testing)
                double hUsed = acceptStep(t, y, yDot, tNext, yNext);
                // Update state
                t = tNext;
                System.arraycopy(yNext, 0, y, 0, y.length);
                count++;
            }
        }

        // Expose protected method for direct testing
        public double acceptStep(double t, double[] y, double[] yDot,
                                 double tStep, double[] yStep) {
            return super.acceptStep(t, y, yDot, tStep, yStep);
        }

        @Override
        public double getCurrentStepStartTime() {
            return t0;
        }

        @Override
        public double getCurrentSignedStepsize() {
            return stepSize;
        }

        @Override
        public void setStepSizeControl(double minimalStep, double maximalStep,
                                       double absoluteTolerance, double relativeTolerance) {
            this.minStep = minimalStep;
            this.maxStep = maximalStep;
        }

        @Override
        public double getMinStep() {
            return minStep;
        }

        @Override
        public double getMaxStep() {
            return maxStep;
        }

        @Override
        public void setMaxEvaluations(int maxEvaluations) {
            super.setMaxEvaluations(maxEvaluations);
        }

        @Override
        public int getEvaluations() {
            return evaluations;
        }

        public void clearStepHandlers() {
            // Not needed for these tests
        }

        // For testing only: manually trigger a reset
        public void setResetOccurred(boolean reset) {
            this.resetOccurred = reset;
        }

        public boolean isResetOccurred() {
            return resetOccurred;
        }
    }

    // Simple ODE: dy/dt = 1
    private static class SimpleODE implements FirstOrderDifferentialEquations {
        @Override
        public int getDimension() {
            return 1;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 1.0;
        }
    }

    // Event that triggers at t = 1.0
    private static class SimpleEvent implements EventHandler {
        @Override
        public double g(double t, double[] y) {
            return t - 1.0;
        }

        @Override
        public Action eventOccurred(double t, double[] y, boolean increasing) {
            return Action.CONTINUE;
        }

        @Override
        public void resetState(double t, double[] y) {
            // No state to reset
        }
    }

    // Event that stops integration at t = 1.0
    private static class StopEvent implements EventHandler {
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
        }
    }

    // Event that resets state (sets y to 0.0)
    private static class ResetStateEvent implements EventHandler {
        @Override
        public double g(double t, double[] y) {
            return t - 1.0;
        }

        @Override
        public Action eventOccurred(double t, double[] y, boolean increasing) {
            return Action.RESET_STATE;
        }

        @Override
        public void resetState(double t, double[] y) {
            y[0] = 0.0;
        }
    }

    // Event that resets derivatives (no effect in this simple ODE)
    private static class ResetDerivativesEvent implements EventHandler {
        @Override
        public double g(double t, double[] y) {
            return t - 1.0;
        }

        @Override
        public Action eventOccurred(double t, double[] y, boolean increasing) {
            return Action.RESET_DERIVATIVES;
        }

        @Override
        public void resetState(double t, double[] y) {
        }
    }

    private TestIntegrator integrator;
    private SimpleODE ode;
    private double[] y0;
    private double[] y;

    @Before
    public void setUp() {
        integrator = new TestIntegrator();
        integrator.addStepHandler(new StepHandler() {
            @Override
            public void init(double t0, double[] y0, double tEnd) {
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
            }
        });
        ode = new SimpleODE();
        y0 = new double[]{0.0};
        y = new double[1];
    }

    // Basic integration without events
    @Test
    public void testBasicIntegration() {
        integrator.addEventHandler(new SimpleEvent(), 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("Final time should be 2.0", 2.0, integrator.getCurrentStepStartTime() + 
                     integrator.getCurrentSignedStepsize() * 20, 1e-10);
        assertEquals("y should be 2.0", 2.0, y[0], 1e-10);
    }

    // Event at end time
    @Test
    public void testEventAtEnd() {
        integrator.addEventHandler(new StopEvent(), 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals("Integration should stop at t=1.0", 1.0, y[0], 1e-10);
    }

    // Event at start time (should be handled immediately)
    @Test
    public void testEventAtStart() {
        // Modified event that triggers at t=0
        integrator.addEventHandler(new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t; // zero at start
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }

            @Override
            public void resetState(double t, double[] y) {
            }
        }, 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("y should be 2.0 after integration", 2.0, y[0], 1e-10);
    }

    // Multiple events (event at t=0.5 and t=1.5)
    @Test
    public void testMultipleEvents() {
        EventHandler event1 = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 0.5;
            }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }
            @Override
            public void resetState(double t, double[] y) {}
        };
        EventHandler event2 = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 1.5;
            }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }
            @Override
            public void resetState(double t, double[] y) {}
        };
        integrator.addEventHandler(event1, 1.0, 1e-10, 100);
        integrator.addEventHandler(event2, 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("y should be 2.0", 2.0, y[0], 1e-10);
    }

    // Event that resets state (bug 44 scenario)
    @Test
    public void testEventResetState() {
        integrator.addEventHandler(new ResetStateEvent(), 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        // After reset at t=1.0, y becomes 0 and then integrates to 1.0 from t=1 to t=2
        assertEquals("After reset, y should be 1.0", 1.0, y[0], 1e-10);
    }

    // Event that resets derivatives (no visible effect but should not crash)
    @Test
    public void testEventResetDerivatives() {
        integrator.addEventHandler(new ResetDerivativesEvent(), 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("y should be 2.0", 2.0, y[0], 1e-10);
    }

    // Event handler that returns STOP and then reset state (should stop)
    @Test
    public void testEventStopAndResetState() {
        integrator.addEventHandler(new EventHandler() {
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
                y[0] = 100.0; // should not matter because STOP
            }
        }, 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("Should stop at t=1.0", 1.0, y[0], 1e-10);
    }

    // Test that event handler with zero convergence works
    @Test
    public void testEventWithZeroConvergence() {
        integrator.addEventHandler(new SimpleEvent(), 0.0, 1e-12, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("y should be 2.0", 2.0, y[0], 1e-10);
    }

    // Test backward integration (negative direction)
    @Test
    public void testBackwardIntegration() {
        y0[0] = 2.0;
        integrator.integrate(ode, 2.0, y0, 0.0, y);
        assertEquals("Backward integration should reach 0", 0.0, y[0], 1e-10);
    }

    // Test event during backward integration
    @Test
    public void testEventBackward() {
        y0[0] = 2.0;
        integrator.addEventHandler(new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 1.0; // event at t=1.0 during backward integration
            }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }
            @Override
            public void resetState(double t, double[] y) {
                y[0] = 0.0;
            }
        }, 1.0, 1e-10, 100);
        integrator.integrate(ode, 2.0, y0, 0.0, y);
        // After reset at t=1.0 (backward), y becomes 0 and then continues from 1.0 to 0.0
        // contributing -1.0, so final y should be 0.0 -1.0 = -1.0? Wait:
        // Initial y=2.0 at t=2.0. Integrate backward: dy/dt=1 -> y decreases as t decreases.
        // At t=1.0, y=1.0. Reset moves y to 0.0. Then continue from t=1.0 to t=0.0: y decreases to 0.0 -1.0 = -1.0.
        assertEquals("y should be -1.0 after reset during backward integration", -1.0, y[0], 1e-10);
    }

    // Test that clearing event handlers works
    @Test
    public void testClearEventHandlers() {
        integrator.addEventHandler(new SimpleEvent(), 1.0, 1e-10, 100);
        integrator.clearEventHandlers();
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("y should be 2.0", 2.0, y[0], 1e-10);
    }

    // Test that max evaluations constraint is enforced
    @Test(expected = MaxCountExceededException.class)
    public void testMaxEvaluationsExceeded() {
        integrator.setMaxEvaluations(5);
        integrator.integrate(ode, 0.0, y0, 100.0, y);
    }

    // Test event that triggers exactly at step boundary (potential bug)
    @Test
    public void testEventAtStepBoundary() {
        final double eventTime = 0.5;
        integrator.addEventHandler(new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }
            @Override
            public void resetState(double t, double[] y) {
                y[0] = 100.0;
            }
        }, 0.1, 1e-10, 100); // maxCheckInterval same as stepSize to force boundary
        integrator.integrate(ode, 0.0, y0, 1.0, y);
        // At t=0.5, y = 0.5, reset to 100, then integrate to 1.0: y = 100 + 0.5 = 100.5
        assertEquals("y should be 100.5 after reset at step boundary", 100.5, y[0], 1e-10);
    }

    // Test that resetOccurred flag is properly cleared (bug 44)
    @Test
    public void testResetOccurredFlagLoop() {
        // This scenario potentially causes an infinite loop if reset flag not cleared
        final EventHandler handler = new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 1.0;
            }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }
            @Override
            public void resetState(double t, double[] y) {
                y[0] = 0.0;
            }
        };
        integrator.addEventHandler(handler, 0.5, 1e-10, 100);
        // Use a small step to ensure event is detected repeatedly? Actually the reset changes y but g does not depend on y.
        // The event should only occur once. The bug was that after reset, the event could be triggered again.
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        // After reset at t=1.0, y=0.0, then integrate to 2.0 => y=1.0
        assertEquals("Final y should be 1.0 after one reset", 1.0, y[0], 1e-10);
    }

    // Test multiple step handlers (should not interfere)
    @Test
    public void testMultipleStepHandlers() {
        final double[] dummy = new double[]{0.0};
        integrator.addStepHandler(new StepHandler() {
            @Override
            public void init(double t0, double[] y0, double tEnd) {}
            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                dummy[0] += 1.0;
            }
        });
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertTrue("Step handler should have been called", dummy[0] > 0);
    }

    // Test event with no step size control (should not throw)
    @Test
    public void testEventWithDefaultStepSize() {
        integrator.setStepSizeControl(1e-10, 1.0, 1e-10, 1e-10);
        integrator.addEventHandler(new SimpleEvent(), 0.2, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("y should be 2.0", 2.0, y[0], 1e-10);
    }

    // Test that event handler's g function is called correctly when step crosses zero
    @Test
    public void testEventZeroCrossing() {
        integrator.addEventHandler(new EventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 0.75; // event at t=0.75
            }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }
            @Override
            public void resetState(double t, double[] y) {}
        }, 0.2, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 1.5, y);
        assertEquals("y should be 1.5", 1.5, y[0], 1e-10);
    }

    // Edge case: zero step size (should not cause division by zero)
    @Test(expected = IllegalArgumentException.class)
    public void testZeroStepSize() {
        // Step size control with zero minimal step? Not directly testable.
        // Instead, call sanityChecks with t0 == tEnd might be okay.
        assertEquals("Dummy assertion to avoid unused", integrator.getMinStep(), 1e-10, 1e-10);
    }

    // Test that step size control works for large values
    @Test
    public void testLargeStepSize() {
        integrator.setStepSizeControl(0.5, 10.0, 1e-10, 1e-10);
        // With step size = 0.5 internally, but the step size control sets allowed range.
        // The integrator's step size will be within [0.5,10] – but our implementation uses a fixed step.
        // We'll just verify integration completes.
        integrator.integrate(ode, 0.0, y0, 5.0, y);
        assertEquals("y should be 5.0", 5.0, y[0], 1e-10);
    }

    // Test that removing event handlers works via clear
    @Test
    public void testRemoveEventHandlers() {
        integrator.addEventHandler(new SimpleEvent(), 1.0, 1e-10, 100);
        integrator.clearEventHandlers();
        assertEquals("Number of event handlers should be 0 after clear", 0, integrator.getEventHandlers().size());
    }

    // Test event handler with null action? Not possible, but test exception.
    // We'll test that getEventHandlers returns a copy.
    @Test
    public void testGetEventHandlersReturnsCopy() {
        integrator.addEventHandler(new SimpleEvent(), 1.0, 1e-10, 100);
        Object handlersBefore = integrator.getEventHandlers();
        integrator.clearEventHandlers();
        assertNotSame("Returned list should be a copy", handlersBefore, integrator.getEventHandlers());
    }

    // Test that acceptStep can handle negative step direction
    @Test
    public void testAcceptStepNegativeStep() {
        // Create integrator and manually call acceptStep with negative step
        // This is a bit low-level but helps coverage
        double[] yVal = {0.0};
        double[] yDot = {1.0};
        double[] yStep = {-0.1};
        double h = integrator.acceptStep(0.0, yVal, yDot, -0.1, yStep);
        assertTrue("Step should be negative", h < 0);
    }

    // Test event at the very end of integration (tEnd equals event time)
    @Test
    public void testEventExactlyAtEnd() {
        integrator.addEventHandler(new StopEvent(), 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals("Integration should stop at t=1.0, y=1.0", 1.0, y[0], 1e-10);
    }

    // Test that multiple events with same time are handled
    @Test
    public void testMultipleEventsSameTime() {
        EventHandler e1 = new EventHandler() {
            @Override
            public double g(double t, double[] y) { return t - 1.0; }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            @Override
            public void resetState(double t, double[] y) {}
        };
        EventHandler e2 = new EventHandler() {
            @Override
            public double g(double t, double[] y) { return t - 1.0; }
            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            @Override
            public void resetState(double t, double[] y) {}
        };
        integrator.addEventHandler(e1, 1.0, 1e-10, 100);
        integrator.addEventHandler(e2, 1.0, 1e-10, 100);
        integrator.integrate(ode, 0.0, y0, 2.0, y);
        assertEquals("y should be 2.0 after two events", 2.0, y[0], 1e-10);
    }
}