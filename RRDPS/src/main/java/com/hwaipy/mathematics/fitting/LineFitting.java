package com.hwaipy.mathematics.fitting;

import com.hwaipy.mathematics.statistics.Statistics;

/**
 * 线性拟合算法。 <i>y</i> = <i>s·x</i> + <i>i</i>
 *
 * @author Hwaipy
 */
public class LineFitting {

    private final double[] dataX;
    private final double[] dataY;
    private double slope = 0;
    private double intercept = 0;

    /**
     * 构造并完成拟合。
     *
     * @param dataX
     * @param dataY
     */
    public LineFitting(double[] dataX, double[] dataY) {
        this.dataX = dataX;
        this.dataY = dataY;
        if (dataX.length != dataY.length) {
            throw new IllegalArgumentException("dataX and dataY contains different items.");
        }
        fit();
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    private void fit() {
        int length = dataX.length;
        double meanX = Statistics.mean(dataX);
        double meanY = Statistics.mean(dataY);
        double quadraticSumX = Statistics.quadraticSum(dataX);
        double[] xy = new double[length];
        for (int i = 0; i < length; i++) {
            xy[i] = dataX[i] * dataY[i];
        }
        double sumXY = Statistics.sum(xy);
        slope = (sumXY - length * meanX * meanY) / (quadraticSumX - length * meanX * meanX);
        intercept = meanY - meanX * slope;
    }
}
