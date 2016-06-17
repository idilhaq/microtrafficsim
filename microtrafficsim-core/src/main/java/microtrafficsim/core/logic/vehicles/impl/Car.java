package microtrafficsim.core.logic.vehicles.impl;

import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.opengl.utils.Color;

import java.util.function.Function;

/**
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Car extends AbstractVehicle {
    // AbstractVehicle
    public static int maxVelocity = 5; // in km/h
    private static float dawdleFactor = 0.2f;
    private static float dashFactor = 0f;
    // Hulk
    public static int maxAnger = Integer.MAX_VALUE;
    private int anger;
    private int totalAnger;

    public Car(SimulationConfig config, VehicleStateListener stateListener, Route route) {
        super(config, stateListener, route);
        anger = 0;
        totalAnger = 0;
    }

    public Car(SimulationConfig config, VehicleStateListener stateListener, Route route, int spawnDelay) {
        super(config, stateListener, route, spawnDelay);
        anger = 0;
        totalAnger = 0;
    }

    @Override
    protected Color getColorFor(int velocity) {
        Color[] redToGreen = new Color[] {
                Color.fromRGB(0xD61E24),
                Color.fromRGB(0xED7834),
                Color.fromRGB(0xFFC526),
                Color.fromRGB(0x8FA63F),
                Color.fromRGB(0x0776E3)
        };
        Color[] orangeToBlue = new Color[] {
                Color.fromRGB(0xE30707),
                Color.fromRGB(0xE33E07),
                Color.fromRGB(0xEB7F00),
                Color.fromRGB(0x225378),
                Color.fromRGB(0x225378),
                Color.fromRGB(0x225378)
        };
        Color[] redToBlue = new Color[] {
                Color.fromRGB(0xE33E07),
                Color.fromRGB(0xFFC806),
                Color.fromRGB(0x009489),
                Color.fromRGB(0x167DA3),
                Color.fromRGB(0x1D3578),
                Color.fromRGB(0x162352)
        };
        Color[] darkRedToBrown = new Color[] {
                Color.fromRGB(0x8C3730),
                Color.fromRGB(0xBF6E3F),
                Color.fromRGB(0xD98943),
                Color.fromRGB(0xF2A649),
                Color.fromRGB(0x594F3C),
                Color.fromRGB(0x594F3C)
        };
        return redToBlue[velocity];
    }

    /*
    |==========|
    | (i) Hulk |
    |==========|
    */
    @Override
    public void becomeMoreAngry() {
        anger = Math.min(anger + 1, maxAnger);
        totalAnger += 1;
    }

    @Override
    public void calmDown() {
        anger = Math.max(anger - 1, 0);
    }

    @Override
    public int getAnger() {
        return anger;
    }

    @Override
    public int getTotalAnger() {
        return totalAnger;
    }

    @Override
    public int getMaxAnger() {
        return maxAnger;
    }

    /*
    |=====================|
    | (c) AbstractVehicle |
    |=====================|
    */
    @Override
    protected Function<Integer, Integer> createAccelerationFunction() {
        // 1 - e^(-1s/15s) = 1 - 0,9355 = 0.0645
//    return v -> (int)(0.0645f * maxVelocity + 0.9355f * v);
        return v -> v+1;
    }

    @Override
    protected Function<Integer, Integer> createDawdleFunction() {
        // Dawdling only 5km/h
//    return v -> (v < 5) ? 0 : (v - 5);
        return v -> (v < 1) ? 0 : v-1;
    }

    @Override
    protected int getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    protected float getDawdleFactor() {
        return dawdleFactor;
    }

    public static void setDawdleFactor(float dawdleFactor) {
        Car.dawdleFactor = dawdleFactor;
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected float getDashFactor() {
        return dashFactor;
    }

    public static void setDashFactor(float dashFactor) {
        Car.dashFactor = dashFactor;
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDashAndDawdleFactor(float dashFactor, float dawdleFactor) {
        Car.dashFactor = dashFactor;
        Car.dawdleFactor = dawdleFactor;
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}