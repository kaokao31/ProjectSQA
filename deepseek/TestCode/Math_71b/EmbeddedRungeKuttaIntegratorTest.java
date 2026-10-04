package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.events.EventState;
import org.apache.commons.math.ode.sampling.FixedStepHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for EmbeddedRungeKuttaIntegrator (abstract).
 * Uses DormandPrince853Integrator as concrete implementation.
 * Designed to achieve high coverage and detect potential faults.
 */
public class EmbeddedRungeKuttaIntegratorTest {

    private static final double EPS = 1e-12;
    private static final double TOL = 1e-8;

    private FirstOrderDifferentialEquations simpleODE;
    private FirstOrderDifferentialEquations stiffODE;
    private FirstOrderDifferentialEquations linearODE;

    @Before
    public void setUp() {
        // Simple ODE: dy/dt = 1, y(0)=0 => y(t)=t
        simpleODE = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        // Stiff-ish ODE: dy/dt = -100*y, y(0)=1 => y(t)=exp(-100*t)
        stiffODE = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -100.0 * y[0];
            }
        };

        // Linear ODE: dy/dt = y, y(0)=1 => y(t)=exp(t)
        linearODE = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };
    }

    // ========== Basic integration tests ==========

    @Test
    public void testSimpleIntegration() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        double[] y = new double[] { 0.0 };
        double tEnd = 5.0;
        integrator.integrate(simpleODE, 0.0, y, tEnd, y);
        assertEquals("y(t) should be t", tEnd, y[0], TOL);
    }

    @Test
    public void testStiffIntegration() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        double[] y = new double[] { 1.0 };
        double tEnd = 0.1;
        integrator.integrate(stiffODE, 0.0, y, tEnd, y);
        double expected = Math.exp(-100.0 * tEnd);
        assertEquals("Stiff ODE result", expected, y[0], 1e-6);
    }

    @Test
    public void testLinearIntegration() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-8, 1e-8);
        double[] y = new double[] { 1.0 };
        double tEnd = 1.0;
        integrator.integrate(linearODE, 0.0, y, tEnd, y);
        double expected = Math.exp(1.0);
        assertEquals("Linear ODE result", expected, y[0], 1e-6);
    }

    // ========== Event handling tests ==========

    @Test
    public void testEventStopsIntegration() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        double[] y = new double[] { 0.0 };
        // Event at t=2.5, stop integration
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
            public double g(double t, double[] y) {
                return t - 2.5;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-6, 100);
        double tEnd = 10.0;
        integrator.integrate(simpleODE, 0.0, y, tEnd, y);
        // Should have stopped at t=2.5
        assertEquals("Integration should stop at event time", 2.5, y[0], TOL);
    }

    @Test
    public void testEventResetsState() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        final double[] y = new double[] { 0.0 };
        // Event at t=1.0, reset state to 0.0 (no change)
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return RESET_STATE;
            }
            public double g(double t, double[] y) {
                return t - 1.0;
            }
            public void resetState(double t, double[] y) {
                y[0] = 0.0; // reset to initial condition
            }
        }, 0.1, 1e-6, 100);
        double tEnd = 2.0;
        integrator.integrate(simpleODE, 0.0, y, tEnd, y);
        // After reset at t=1, y should be 0 again, then integrate from 1 to 2 => y=1
        assertEquals("After reset, y should be 1.0", 1.0, y[0], TOL);
    }

    @Test
    public void testEventResetsDerivatives() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        final double[] y = new double[] { 0.0 };
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return RESET_DERIVATIVES;
            }
            public double g(double t, double[] y) {
                return t - 1.5;
            }
            public void resetState(double t, double[] y) {
                // no state change
            }
        }, 0.1, 1e-6, 100);
        double tEnd = 3.0;
        integrator.integrate(simpleODE, 0.0, y, tEnd, y);
        // Should continue integration after reset
        assertEquals("Integration after reset derivatives", 3.0, y[0], TOL);
    }

    @Test
    public void testMultipleEvents() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        double[] y = new double[] { 0.0 };
        // Event at t=2.0, stop
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
            public double g(double t, double[] y) {
                return t - 2.0;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-6, 100);
        // Another event at t=1.0, but should not be reached if first stops
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
            public double g(double t, double[] y) {
                return t - 1.0;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-6, 100);
        double tEnd = 10.0;
        integrator.integrate(simpleODE, 0.0, y, tEnd, y);
        // Should stop at first event (t=1.0)
        assertEquals("Should stop at first event", 1.0, y[0], TOL);
    }

    // ========== Step handler tests ==========

    @Test
    public void testFixedStepHandler() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        final double[] lastT = new double[1];
        integrator.addStepHandler(new FixedStepHandler() {
            public void handleStep(double t, double[] y, double[] yDot, boolean isLast) {
                lastT[0] = t;
            }
        }, 0.5);
        double[] y = new double[] { 0.0 };
        integrator.integrate(simpleODE, 0.0, y, 2.0, y);
        // Last step should be at t=2.0
        assertEquals("Last step time", 2.0, lastT[0], TOL);
    }

    @Test
    public void testStepHandlerWithInterpolator() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        final double[] lastT = new double[1];
        integrator.addStepHandler(new StepHandler() {
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                lastT[0] = interpolator.getCurrentTime();
            }
            public boolean requiresDenseOutput() { return false; }
        });
        double[] y = new double[] { 0.0 };
        integrator.integrate(simpleODE, 0.0, y, 3.0, y);
        assertEquals("Last step time via interpolator", 3.0, lastT[0], TOL);
    }

    // ========== Tolerance and step size edge cases ==========

    @Test(expected = IntegratorException.class)
    public void testTooSmallInitialStep() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1e-12, 1e-12, 1e-12, 1e-12);
        double[] y = new double[] { 0.0 };
        integrator.integrate(simpleODE, 0.0, y, 1.0, y);
    }

    @Test(expected = IntegratorException.class)
    public void testNegativeStepSize() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.1, 10.0, 1e-6, 1e-6);
        double[] y = new double[] { 0.0 };
        integrator.integrate(simpleODE, 0.0, y, -1.0, y);
    }

    @Test
    public void testZeroStepSize() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.0, 10.0, 1e-6, 1e-6);
        double[] y = new double[] { 0.0 };
        integrator.integrate(simpleODE, 0.0, y, 0.0, y);
        assertEquals("No integration", 0.0, y[0], TOL);
    }

    // ========== Exception handling ==========

    @Test(expected = DerivativeException.class)
    public void testDerivativeException() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.1, 10.0, 1e-6, 1e-6);
        FirstOrderDifferentialEquations badODE = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                throw new DerivativeException("bad");
            }
        };
        double[] y = new double[] { 0.0 };
        integrator.integrate(badODE, 0.0, y, 1.0, y);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEquations() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.1, 10.0, 1e-6, 1e-6);
        double[] y = new double[] { 0.0 };
        integrator.integrate(null, 0.0, y, 1.0, y);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullState() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.1, 10.0, 1e-6, 1e-6);
        integrator.integrate(simpleODE, 0.0, null, 1.0, null);
    }

    // ========== Additional coverage: step acceptance/rejection ==========

    @Test
    public void testStepRejectionDueToTolerance() throws DerivativeException, IntegratorException {
        // Use very tight tolerance to force step rejection
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1e-10, 1e-6, 1e-12, 1e-12);
        double[] y = new double[] { 1.0 };
        double tEnd = 0.01;
        integrator.integrate(stiffODE, 0.0, y, tEnd, y);
        // Should still produce reasonable result
        assertTrue("y should be positive", y[0] > 0);
    }

    @Test
    public void testLargeStepSize() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.1, 100.0, 1e-6, 1e-6);
        double[] y = new double[] { 0.0 };
        double tEnd = 1000.0;
        integrator.integrate(simpleODE, 0.0, y, tEnd, y);
        assertEquals("Large step integration", tEnd, y[0], TOL * tEnd);
    }

    @Test
    public void testMultipleStepsWithEvents() throws DerivativeException, IntegratorException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        final int[] eventCount = new int[1];
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                eventCount[0]++;
                return CONTINUE;
            }
            public double g(double t, double[] y) {
                return Math.sin(t); // multiple zero crossings
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-6, 100);
        double[] y = new double[] { 0.0 };
        integrator.integrate(simpleODE, 0.0, y, 10.0, y);
        assertTrue("Should have detected multiple events", eventCount[0] > 0);
    }

    // ========== Test for bug 71: event handling with step acceptance ==========

    @Test
    public void testBug71EventHandling() throws DerivativeException, IntegratorException {
        // This test targets the specific bug: event handling during step acceptance
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(0.01, 10.0, 1e-6, 1e-6);
        final double[] y = new double[] { 0.0 };
        // Event that triggers exactly at a step boundary
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
            public double g(double t, double[] y) {
                return t - 1.0;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1e-6, 100);
        double tEnd = 2.0;
        integrator.integrate(simpleODE, 0.0, y, tEnd, y);
        // Should stop at t=1.0
        assertEquals("Bug71: event at t=1.0", 1.0, y[0], TOL);
    }

    @Test
    public void testBug71EventWithStepRejection() throws DerivativeException, IntegratorException {
        // Combination of event and step rejection
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        final double[] y = new double[] { 1.0 };
        integrator.addEventHandler(new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
            public double g(double t, double[] y) {
                return t - 0.05;
            }
            public void resetState(double t, double[] y) {}
        }, 0.01, 1e-10, 100);
        double tEnd = 0.1;
        integrator.integrate(stiffODE, 0.0, y, tEnd, y);
        // Should stop at t=0.05
        assertEquals("Bug71: stiff event", 0.05, y[0], 1e-6);
    }
}