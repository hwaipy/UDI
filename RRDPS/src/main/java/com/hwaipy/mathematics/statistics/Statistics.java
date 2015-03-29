package com.hwaipy.mathematics.statistics;

/**
 * 简单的统计函数
 * @author Hwaipy
 */
public class Statistics {

    /**
     * 求和
     * @param data
     * @return
     */
    public static double sum(double[] data) {
        double sum = 0;
        for (double d : data) {
            sum += d;
        }
        return sum;
    }

    /**
     * 平均值
     * @param data
     * @return
     */
    public static double mean(double[] data) {
        double sum = sum(data);
        return sum / data.length;
    }

    /**
     * 平方和
     * @param data
     * @return
     */
    public static double quadraticSum(double[] data) {
        double sum = 0;
        for (double d : data) {
            sum += d * d;
        }
        return sum;
    }
}
