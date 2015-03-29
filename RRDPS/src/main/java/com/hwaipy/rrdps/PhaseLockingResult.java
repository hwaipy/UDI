package com.hwaipy.rrdps;

/**
 *
 * @author Hwaipy
 */
public class PhaseLockingResult {

    private final double min;
    private final double max;

    public PhaseLockingResult(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double contrast() {
        return max / min;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

}
