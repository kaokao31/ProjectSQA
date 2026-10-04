package org.apache.commons.math.ode.nonstiff;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.FirstOrderIntegrator;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;

public class EmbeddedRungeKuttaIntegratorTest {

    private static final double DEFAULT_ABSOLUTE_TOLERANCE = 1e-10;
    private static final double DEFAULT_RELATIVE_TOLERANCE = 1e-10;
    private static final double DEFAULT_STEP_SIZE = 0.1;

    private FirstOrderDifferentialEquations ode;
    private EmbeddedRungeKuttaIntegrator integrator;

    @Before
    public void setUp() {
        ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0]; // dy/dt = y
            }
        };
        // Use a concrete implementation: DormandPrince853Integrator
        integrator = new DormandPrince853Integrator(
                DEFAULT_STEP_SIZE, 1.0, DEFAULT_ABSOLUTE_TOLERANCE, DEFAULT_RELATIVE_TOLERANCE);
    }

    @Test
    public void testConstructor() {
        // Verify that the integrator is constructed with the given step size
        assertEquals(DEFAULT_STEP_SIZE, integrator.getCurrentStepStart(), 1e-15);
        // Additional checks: getOrder should be 5 for DormandPrince853
        assertTrue("Order should be positive", integrator.getOrder() > 0);
    }

    @Test
    public void testIntegrateSimple() throws IntegratorException {
        double t0 = 0.0;
        double[] y0 = {1.0};
        double tEnd = 1.0;
        double[] yEnd = new double[1];
        integrator.integrate(ode, t0, y0, tEnd, yEnd);
        // Exact solution: y = exp(t)
        double expected = Math.exp(tEnd);
        assertEquals("Integration result should match exponential growth", expected, yEnd[0], 1e-8);
    }

    @Test
    public void testIntegrateWithStepHandler() throws IntegratorException {
        final boolean[] handlerCalled = {false};
        StepHandler handler = new StepHandler() {
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                handlerCalled[0] = true;
            }
            public boolean requiresDenseOutput() { return false; }
        };
        integrator.addStepHandler(handler);
        double[] yEnd = new double[1];
        integrator.integrate(ode, 0.0, new double[]{1.0}, 1.0, yEnd);
        assertTrue("Step handler should have been called", handlerCalled[0]);
    }

    @Test
    public void testIntegrateWithEventHandler() throws IntegratorException {
        final boolean[] eventOccurred = {false};
        EventHandler eventHandler = new EventHandler() {
            public double g(double t, double[] y) {
                return y[0] - 2.0; // event when y reaches 2
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                eventOccurred[0] = true;
                return STOP;
            }
            public void resetState(double t, double[] y) {}
        };
        integrator.addEventHandler(eventHandler, 0.1, 1e-9, 100);
        double[] yEnd = new double[1];
        integrator.integrate(ode, 0.0, new double[]{1.0}, 10.0, yEnd);
        assertTrue("Event handler should have been triggered", eventOccurred[0]);
        // y should be approximately 2 at event time
        assertEquals("Event should occur at y=2", 2.0, yEnd[0], 0.1);
    }

    @Test
    public void testIntegrateWithBothHandlers() throws IntegratorException {
        final boolean[] stepCalled = {false};
        final boolean[] eventCalled = {false};
        StepHandler stepHandler = new StepHandler() {
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepCalled[0] = true;
            }
            public boolean requiresDenseOutput() { return false; }
        };
        EventHandler eventHandler = new EventHandler() {
            public double g(double t, double[] y) {
                return y[0] - 3.0;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                eventCalled[0] = true;
                return STOP;
            }
            public void resetState(double t, double[] y) {}
        };
        integrator.addStepHandler(stepHandler);
        integrator.addEventHandler(eventHandler, 0.1, 1e-9, 100);
        double[] yEnd = new double[1];
        integrator.integrate(ode, 0.0, new double[]{1.0}, 10.0, yEnd);
        assertTrue("Step handler should have been called", stepCalled[0]);
        assertTrue("Event handler should have been triggered", eventCalled[0]);
    }

    @Test(expected = IntegratorException.class)
    public void testIntegrateZeroStepSize() throws IntegratorException {
        // Create integrator with zero step size
        EmbeddedRungeKuttaIntegrator zeroStepIntegrator =
                new DormandPrince853Integrator(0.0, 1.0, 1e-10, 1e-10);
        zeroStepIntegrator.integrate(ode, 0.0, new double[]{1.0}, 1.0, new double[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntegrateNegativeStepSize() throws IntegratorException {
        // Negative step size should be rejected
        EmbeddedRungeKuttaIntegrator negativeStepIntegrator =
                new DormandPrince853Integrator(-0.1, 1.0, 1e-10, 1e-10);
        negativeStepIntegrator.integrate(ode, 0.0, new double[]{1.0}, 1.0, new double[1]);
    }

    @Test(expected = NullPointerException.class)
    public void testIntegrateNullODE() throws IntegratorException {
        integrator.integrate(null, 0.0, new double[]{1.0}, 1.0, new double[1]);
    }

    @Test
    public void testGetOrder() {
        // DormandPrince853 has order 5
        assertEquals("Order should be 5", 5, integrator.getOrder());
    }

    @Test
    public void testGetA() {
        // Check that getA returns a non-null matrix with correct dimensions
        double[][] a = integrator.getA();
        assertNotNull("A matrix should not be null", a);
        int order = integrator.getOrder();
        // For DormandPrince853, A is 8x8 (or 7x7 depending on implementation)
        assertTrue("A matrix should have at least one row", a.length > 0);
        for (double[] row : a) {
            assertNotNull("Row should not be null", row);
            assertEquals("Row length should match number of rows", a.length, row.length);
        }
    }

    @Test
    public void testGetB() {
        // Check that getB returns non-null arrays
        double[] b = integrator.getB();
        assertNotNull("B array should not be null", b);
        assertTrue("B array should have positive length", b.length > 0);
    }

    @Test
    public void testGetC() {
        // Check that getC returns non-null arrays
        double[] c = integrator.getC();
        assertNotNull("C array should not be null", c);
        assertTrue("C array should have positive length", c.length > 0);
    }

    @Test
    public void testErrorEstimation() throws IntegratorException {
        // Integrate with a known solution and check that error estimate is within tolerance
        double t0 = 0.0;
        double[] y0 = {1.0};
        double tEnd = 2.0;
        double[] yEnd = new double[1];
        integrator.integrate(ode, t0, y0, tEnd, yEnd);
        double exact = Math.exp(tEnd);
        double error = Math.abs(yEnd[0] - exact);
        // The tolerance is 1e-10, but due to accumulation, error may be larger
        // We just check it's reasonably small
        assertTrue("Error should be less than 1e-6", error < 1e-6);
    }

    @Test
    public void testMultipleSteps() throws IntegratorException {
        // Integrate over many steps to ensure stability
        double t0 = 0.0;
        double[] y0 = {1.0};
        double tEnd = 100.0;
        double[] yEnd = new double[1];
        integrator.integrate(ode, t0, y0, tEnd, yEnd);
        double exact = Math.exp(tEnd);
        // Relative error should be small
        double relError = Math.abs(yEnd[0] - exact) / exact;
        assertTrue("Relative error should be less than 1e-4", relError < 1e-4);
    }

    @Test
    public void testReset() throws IntegratorException {
        // Test that resetting the integrator works
        double[] yEnd = new double[1];
        integrator.integrate(ode, 0.0, new double[]{1.0}, 1.0, yEnd);
        double firstResult = yEnd[0];
        // Reset and integrate again from same initial conditions
        integrator.resetState();
        integrator.integrate(ode, 0.0, new double[]{1.0}, 1.0, yEnd);
        assertEquals("Results should be identical after reset", firstResult, yEnd[0], 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntegrateNullInitialState() throws IntegratorException {
        integrator.integrate(ode, 0.0, null, 1.0, new double[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntegrateNullFinalState() throws IntegratorException {
        integrator.integrate(ode, 0.0, new double[]{1.0}, 1.0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIntegrateMismatchedDimensions() throws IntegratorException {
        // ODE dimension is 1, but initial state has 2 elements
        integrator.integrate(ode, 0.0, new double[]{1.0, 2.0}, 1.0, new double[1]);
    }
}