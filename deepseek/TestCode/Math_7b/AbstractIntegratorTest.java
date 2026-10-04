package org.apache.commons.math3.ode;

import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Collection;

/**
 * JUnit 4 test suite for AbstractIntegrator, targeting maximum coverage and fault detection.
 * Focuses on event handling, step acceptance, and sanity checks.
 */
public class AbstractIntegratorTest {

    private static final double EPS = 1e-12;

    // Concrete subclass for testing abstract methods
    private static class TestIntegrator extends AbstractIntegrator {
        private static final long serialVersionUID = 1L;

        public TestIntegrator() {
            super("test");
        }

        @Override
        public void integrate(ExpandableStatefulODE equations, double t) {
            // minimal implementation for testing
        }

        @Override
        protected double getCurrentStepStart() {
            return 0.0;
        }

        @Override
        protected double getCurrentSignedStepsize() {
            return 0.1;
        }

        @Override
        protected void setStepSize(double stepSize) {
            // no-op
        }

        @Override
        protected void setStateInitialized(boolean isInitialized) {
            // no-op
        }

        @Override
        protected boolean isStateInitialized() {
            return true;
        }

        @Override
        protected void computeDerivatives(double t, double[] y, double[] yDot) {
            // no-op
        }

        @Override
        protected void acceptStep(ExpandableStatefulODE equations, double tEnd, double[] y, double[] yDot, double[] yDotTmp)
                throws MathIllegalStateException {
            // call super to test the actual implementation
            super.acceptStep(equations, tEnd, y, yDot, yDotTmp);
        }

        @Override
        protected void sanityChecks(ExpandableStatefulODE equations, double t) throws MathIllegalArgumentException {
            super.sanityChecks(equations, t);
        }
    }

    private TestIntegrator integrator;
    private ExpandableStatefulODE equations;

    @Before
    public void setUp() {
        integrator = new TestIntegrator();
        // Create a simple ODE: dy/dt = 1, y(0)=0
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };
        equations = new ExpandableStatefulODE(ode);
        equations.setTime(0.0);
        equations.setPrimaryState(new double[]{0.0});
    }

    // ---------- Event Handler Tests ----------

    @Test
    public void testAddAndClearEventHandlers() {
        assertEquals(0, integrator.getEventHandlers().size());
        EventHandler handler = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

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
        };
        integrator.addEventHandler(handler, 0.1, 1e-9, 100);
        assertEquals(1, integrator.getEventHandlers().size());
        assertTrue(integrator.getEventHandlers().contains(handler));

        integrator.clearEventHandlers();
        assertEquals(0, integrator.getEventHandlers().size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullEventHandler() {
        integrator.addEventHandler(null, 0.1, 1e-9, 100);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testAddEventHandlerNegativeMaxCheck() {
        EventHandler handler = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

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
        };
        integrator.addEventHandler(handler, 0.1, 1e-9, -1);
    }

    // ---------- Step Handler Tests ----------

    @Test
    public void testAddAndClearStepHandlers() {
        assertEquals(0, integrator.getStepHandlers().size());
        StepHandler handler = new StepHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
            }
        };
        integrator.addStepHandler(handler);
        assertEquals(1, integrator.getStepHandlers().size());
        assertTrue(integrator.getStepHandlers().contains(handler));

        integrator.clearStepHandlers();
        assertEquals(0, integrator.getStepHandlers().size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullStepHandler() {
        integrator.addStepHandler(null);
    }

    // ---------- acceptStep Tests (bug-prone area) ----------

    @Test
    public void testAcceptStepNoEvent() {
        // No event handlers, step should be accepted normally
        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        // tEnd = 1.0, step size = 0.1 (from getCurrentSignedStepsize)
        // The method will try to accept the step, but since no events, it should just return
        // We call it and expect no exception
        integrator.acceptStep(equations, 1.0, y, yDot, yDotTmp);
        // After acceptStep, the state should be updated (but our subclass doesn't implement integrate)
        // We can check that the step handler was called if any, but we have none.
        // Just ensure no exception.
    }

    @Test
    public void testAcceptStepWithEventAtEnd() {
        // This is the bug scenario: event occurs exactly at the end of the step
        EventHandler handler = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public double g(double t, double[] y) {
                // Event at t = 0.1 (end of first step)
                return t - 0.1;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {
            }
        };
        integrator.addEventHandler(handler, 0.1, 1e-9, 100);
        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        // tEnd = 0.1, step size = 0.1, event at exactly tEnd
        // The bug in Defects4J Math-7 is that acceptStep may loop infinitely or mishandle this case.
        // We expect it to stop and not throw.
        integrator.acceptStep(equations, 0.1, y, yDot, yDotTmp);
        // After acceptStep, the event should have been triggered and the step accepted.
        // We can check that the event handler's eventOccurred was called (but we cannot easily verify without mocking).
        // At least no exception.
    }

    @Test(expected = MathIllegalStateException.class)
    public void testAcceptStepWithEventBeforeEndButNotReached() {
        // Event at t=0.05, but step size is 0.1, so event should be found and step shrunk.
        // However, if the event is not found due to convergence issues, it may throw.
        // We'll set a very tight convergence to force failure.
        EventHandler handler = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.05;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {
            }
        };
        integrator.addEventHandler(handler, 0.1, 1e-12, 100); // very small convergence
        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        // This may cause a MathIllegalStateException if event cannot be found within maxIterations
        integrator.acceptStep(equations, 0.1, y, yDot, yDotTmp);
    }

    // ---------- sanityChecks Tests ----------

    @Test(expected = MathIllegalArgumentException.class)
    public void testSanityChecksNullEquations() {
        integrator.sanityChecks(null, 0.0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testSanityChecksNegativeTime() {
        // ODE dimension is 1, time negative should be fine? Actually sanityChecks may check for NaN or infinite.
        // We'll test with a valid equations but time = Double.NaN
        integrator.sanityChecks(equations, Double.NaN);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testSanityChecksInfiniteTime() {
        integrator.sanityChecks(equations, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testSanityChecksValid() {
        // Should not throw
        integrator.sanityChecks(equations, 0.0);
    }

    // ---------- Other Methods ----------

    @Test
    public void testGetName() {
        assertEquals("test", integrator.getName());
    }

    @Test
    public void testSetAndGetMaxEvaluations() {
        integrator.setMaxEvaluations(100);
        assertEquals(100, integrator.getMaxEvaluations());
        assertEquals(0, integrator.getEvaluations()); // not started
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testSetMaxEvaluationsNegative() {
        integrator.setMaxEvaluations(-1);
    }

    @Test
    public void testSetAndGetInterpolator() {
        // Not directly accessible, but we can test via step handler
        // This is a placeholder
    }

    @Test
    public void testResetEvaluations() {
        integrator.setMaxEvaluations(10);
        integrator.resetEvaluations();
        assertEquals(0, integrator.getEvaluations());
    }

    // ---------- Edge Cases for Event Handling ----------

    @Test
    public void testMultipleEventHandlers() {
        EventHandler handler1 = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.2;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {
            }
        };
        EventHandler handler2 = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.3;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }

            @Override
            public void resetState(double t, double[] y) {
                y[0] = 0.5;
            }
        };
        integrator.addEventHandler(handler1, 0.1, 1e-9, 100);
        integrator.addEventHandler(handler2, 0.1, 1e-9, 100);
        assertEquals(2, integrator.getEventHandlers().size());

        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        // tEnd = 0.5, step size = 0.1, events at 0.2 and 0.3
        // The acceptStep should handle both events sequentially
        integrator.acceptStep(equations, 0.5, y, yDot, yDotTmp);
        // After processing, the state may have been reset by handler2
        // We cannot easily verify without more infrastructure, but no exception expected.
    }

    @Test
    public void testEventWithActionContinue() {
        EventHandler handler = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.1;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }

            @Override
            public void resetState(double t, double[] y) {
            }
        };
        integrator.addEventHandler(handler, 0.1, 1e-9, 100);
        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        integrator.acceptStep(equations, 0.2, y, yDot, yDotTmp);
        // Event at 0.1 should be triggered but integration continues
        // No exception expected
    }

    @Test
    public void testEventWithActionResetDerivatives() {
        EventHandler handler = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.1;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_DERIVATIVES;
            }

            @Override
            public void resetState(double t, double[] y) {
            }
        };
        integrator.addEventHandler(handler, 0.1, 1e-9, 100);
        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        integrator.acceptStep(equations, 0.2, y, yDot, yDotTmp);
        // Derivatives should be recomputed
    }

    // ---------- Test for infinite loop bug (Math-7) ----------

    @Test(timeout = 1000)
    public void testAcceptStepNoInfiniteLoop() {
        // This test ensures that acceptStep does not loop infinitely when event is at step end
        EventHandler handler = new EventHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.1; // event exactly at step end
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {
            }
        };
        integrator.addEventHandler(handler, 0.1, 1e-9, 100);
        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        // This should not hang
        integrator.acceptStep(equations, 0.1, y, yDot, yDotTmp);
    }

    // ---------- Test for NullPointerException in acceptStep ----------

    @Test(expected = NullPointerException.class)
    public void testAcceptStepNullEquations() {
        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        integrator.acceptStep(null, 0.1, y, yDot, yDotTmp);
    }

    @Test(expected = NullPointerException.class)
    public void testAcceptStepNullY() {
        double[] yDot = new double[]{1.0};
        double[] yDotTmp = new double[]{1.0};
        integrator.acceptStep(equations, 0.1, null, yDot, yDotTmp);
    }

    @Test(expected = NullPointerException.class)
    public void testAcceptStepNullYDot() {
        double[] y = new double[]{0.0};
        double[] yDotTmp = new double[]{1.0};
        integrator.acceptStep(equations, 0.1, y, null, yDotTmp);
    }

    @Test(expected = NullPointerException.class)
    public void testAcceptStepNullYDotTmp() {
        double[] y = new double[]{0.0};
        double[] yDot = new double[]{1.0};
        integrator.acceptStep(equations, 0.1, y, yDot, null);
    }
}