package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EmbeddedRungeKuttaIntegratorTest {

    private static final double EPSILON = 1e-10;
    private static final double ABS_TOLERANCE = 1e-8;
    private static final double REL_TOLERANCE = 1e-8;

    private TestStepHandler stepHandler;
    private TestEventHandler eventHandler;
    private FirstOrderDifferentialEquations simpleEquation;

    @Before
    public void setUp() {
        stepHandler = new TestStepHandler();
        eventHandler = null;
        simpleEquation = new FirstOrderDifferentialEquations() {
            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -y[0]; // dy/dt = -y -> solution y = y0 * exp(-t)
            }
        };
    }

    @Test
    public void testForwardIntegrationWithStepHandler() throws DerivativeException, IntegratorException {
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);
        integrator.addStepHandler(stepHandler);

        double t0 = 0.0;
        double[] y0 = new double[]{1.0};
        double tEnd = 5.0;

        integrator.integrate(simpleEquation, t0, y0, tEnd, new double[]{1.0});

        Assert.assertTrue("Step handler should have been called", stepHandler.getNumberOfCalls() > 0);
        Assert.assertEquals("Final time should be tEnd", tEnd, stepHandler.getLastT(), 1e-6);
        double expectedY = Math.exp(-tEnd);
        Assert.assertEquals("Final value should match analytical solution", expectedY, stepHandler.getLastY()[0], 1e-4);
    }

    @Test
    public void testBackwardIntegration() throws DerivativeException, IntegratorException {
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);
        integrator.addStepHandler(stepHandler);

        double t0 = 5.0;
        double[] y0 = new double[]{Math.exp(-5.0)};
        double tEnd = 0.0;

        integrator.integrate(simpleEquation, t0, y0, tEnd, new double[]{0.0});

        Assert.assertTrue("Step handler should have been called", stepHandler.getNumberOfCalls() > 0);
        Assert.assertEquals("Final time should be tEnd", tEnd, stepHandler.getLastT(), 1e-6);
        double expectedY = Math.exp(0.0);
        Assert.assertEquals("Final value should match analytical solution", expectedY, stepHandler.getLastY()[0], 1e-4);
    }

    @Test
    public void testEventDetectionForward() throws DerivativeException, IntegratorException {
        eventHandler = new TestEventHandler() {
            private int callCount = 0;
            private double nextEventTime = 1.0;

            @Override
            public double g(double t, double[] y) {
                return t - nextEventTime;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                callCount++;
                nextEventTime = t + 1.0;
                return Action.RESUME;
            }

            @Override
            public void resetState(double t, double[] y) {
                // nothing to reset
            }

            public int getCallCount() {
                return callCount;
            }
        };

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);
        integrator.addEventHandler(eventHandler, 0.1, ABS_TOLERANCE, 100);
        integrator.addStepHandler(stepHandler);

        double t0 = 0.0;
        double[] y0 = new double[]{1.0};
        double tEnd = 5.0;

        integrator.integrate(simpleEquation, t0, y0, tEnd, new double[]{1.0});

        Assert.assertEquals("Should have detected 4 events", 4, ((TestEventHandler) eventHandler).getCallCount());
        Assert.assertEquals("Final time should be tEnd", tEnd, stepHandler.getLastT(), 1e-6);
    }

    @Test
    public void testEventDetectionStop() throws DerivativeException, IntegratorException {
        eventHandler = new TestEventHandler() {
            @Override
            public double g(double t, double[] y) {
                return t - 2.0;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {
                // nothing
            }
        };

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);
        integrator.addEventHandler(eventHandler, 0.1, ABS_TOLERANCE, 100);
        integrator.addStepHandler(stepHandler);

        double t0 = 0.0;
        double[] y0 = new double[]{1.0};
        double tEnd = 5.0;

        integrator.integrate(simpleEquation, t0, y0, tEnd, new double[]{1.0});

        Assert.assertEquals("Integration should stop at t=2", 2.0, stepHandler.getLastT(), 1e-6);
    }

    @Test
    public void testIntegrationWithSmallStepSize() throws DerivativeException, IntegratorException {
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0001, 0.01, ABS_TOLERANCE, REL_TOLERANCE);
        integrator.addStepHandler(stepHandler);

        double t0 = 0.0;
        double[] y0 = new double[]{1.0};
        double tEnd = 0.1;

        integrator.integrate(simpleEquation, t0, y0, tEnd, new double[]{1.0});

        Assert.assertTrue("Step handler should have been called many times", stepHandler.getNumberOfCalls() > 10);
        double expectedY = Math.exp(-tEnd);
        Assert.assertEquals("Final value should match analytical solution", expectedY, stepHandler.getLastY()[0], 1e-4);
    }

    @Test
    public void testIntegrationWithLargeStepSize() throws DerivativeException, IntegratorException {
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(1.0, 1000.0, 1e-3, 1e-3);
        integrator.addStepHandler(stepHandler);

        double t0 = 0.0;
        double[] y0 = new double[]{1.0};
        double tEnd = 1000.0;

        integrator.integrate(simpleEquation, t0, y0, tEnd, new double[]{1.0});

        Assert.assertTrue("Step handler should have been called", stepHandler.getNumberOfCalls() > 0);
        double expectedY = Math.exp(-tEnd);
        Assert.assertEquals("Final value should match analytical solution", expectedY, stepHandler.getLastY()[0], 1e-4);
    }

    @Test
    public void testExceptionOnNonFiniteDerivative() {
        FirstOrderDifferentialEquations badEquation = new FirstOrderDifferentialEquations() {
            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = Double.NaN; // produce NaN
            }
        };

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);
        integrator.addStepHandler(stepHandler);

        try {
            integrator.integrate(badEquation, 0.0, new double[]{1.0}, 1.0, new double[]{0.0});
            Assert.fail("Should have thrown DerivativeException or IntegratorException");
        } catch (DerivativeException | IntegratorException e) {
            // expected
        }
    }

    @Test
    public void testDimensionMismatchInStartVector() throws DerivativeException, IntegratorException {
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);

        try {
            integrator.integrate(simpleEquation, 0.0, new double[]{1.0, 2.0}, 1.0, new double[]{0.0});
            Assert.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDimensionMismatchInEndVector() throws DerivativeException, IntegratorException {
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);

        try {
            integrator.integrate(simpleEquation, 0.0, new double[]{1.0}, 1.0, new double[]{0.0, 0.0});
            Assert.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testEventAtInitialTime() throws DerivativeException, IntegratorException {
        eventHandler = new TestEventHandler() {
            private boolean firstEvent = true;

            @Override
            public double g(double t, double[] y) {
                return firstEvent ? t : t - 1.0;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                firstEvent = false;
                return Action.RESUME;
            }

            @Override
            public void resetState(double t, double[] y) {
                // nothing
            }
        };

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);
        integrator.addEventHandler(eventHandler, 0.1, ABS_TOLERANCE, 100);

        double t0 = 0.0;
        double[] y0 = new double[]{1.0};
        double tEnd = 2.0;

        integrator.integrate(simpleEquation, t0, y0, tEnd, new double[]{0.0});

        // Should not crash at initial time event
        Assert.assertTrue(true);
    }

    @Test
    public void testMultipleEvents() throws DerivativeException, IntegratorException {
        final List<Double> eventTimes = new ArrayList<>();
        eventHandler = new TestEventHandler() {
            private double nextEvent = 0.5;

            @Override
            public double g(double t, double[] y) {
                return t - nextEvent;
            }

            @Override
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                eventTimes.add(t);
                nextEvent = t + 0.5;
                return t < 2.0 ? Action.RESUME : Action.STOP;
            }

            @Override
            public void resetState(double t, double[] y) {
                // nothing
            }
        };

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.01, 10.0, ABS_TOLERANCE, REL_TOLERANCE);
        integrator.addEventHandler(eventHandler, 0.1, ABS_TOLERANCE, 100);

        double t0 = 0.0;
        double[] y0 = new double[]{1.0};
        double tEnd = 3.0;

        integrator.integrate(simpleEquation, t0, y0, tEnd, new double[]{0.0});

        Assert.assertEquals("Should have 4 events (0.5, 1.0, 1.5, 2.0)", 4, eventTimes.size());
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals("Event at wrong time", (i + 1) * 0.5, eventTimes.get(i), 1e-6);
        }
    }

    // Helper classes
    private static class TestStepHandler implements StepHandler {
        private int numberOfCalls = 0;
        private double lastT;
        private double[] lastY;

        @Override
        public void handleStep(StepInterpolator interpolator, boolean isLast) {
            numberOfCalls++;
            lastT = interpolator.getInterpolatedTime();
            lastY = interpolator.getInterpolatedState();
        }

        @Override
        public boolean requiresDenseOutput() {
            return false;
        }

        public int getNumberOfCalls() {
            return numberOfCalls;
        }

        public double getLastT() {
            return lastT;
        }

        public double[] getLastY() {
            return lastY;
        }

        public void reset() {
            numberOfCalls = 0;
            lastT = Double.NaN;
            lastY = null;
        }
    }

    private static abstract class TestEventHandler implements EventHandler {
        @Override
        public abstract double g(double t, double[] y);

        @Override
        public abstract Action eventOccurred(double t, double[] y, boolean increasing);

        @Override
        public abstract void resetState(double t, double[] y);
    }
}