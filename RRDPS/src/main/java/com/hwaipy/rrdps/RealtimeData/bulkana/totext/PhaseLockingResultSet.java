package com.hwaipy.rrdps.RealtimeData.bulkana.totext;

import java.util.ArrayList;

/**
 *
 * @author Administrator 2015-3-29
 */
public class PhaseLockingResultSet {

  private final double[][] results;

  PhaseLockingResultSet(ArrayList<Double> results) {
    this.results = new double[results.size() / 128][128];
    for (int i = 0; i < this.results.length * 128; i++) {
      this.results[i / 128][toDelay(i % 128)] = results.get(i);
    }
  }

  private int toDelay(int index) {
    int delay1 = index % 16;
    int delay2 = index / 16;
    return delay2 + 15 - delay1;
  }

  public double getPhaseLockingErrorRate(int second, int delay) {
    return results[second][delay];
  }

  public double getPhaseLockingErrorRate(long time, int delay) {
    double timeInSecond = time / 1000000000000l;
    int second = (int) (timeInSecond - 0.05);
    return getPhaseLockingErrorRate(second, delay);
  }

  public String toTextResult() {
    StringBuilder sb = new StringBuilder();
    sb.append("PhaseLockingReusltDefinition,Second,Error[128]\n");
    for (int second = 0; second < results.length; second++) {
      sb.append("PhaseLockingResult,").append(second);
      double[] rs = results[second];
      for (double r : rs) {
        sb.append(",").append(r);
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
