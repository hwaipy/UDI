package com.hwaipy.rrdps.RealtimeData.bulkana;

/**
 *
 * @author Hwaipy
 */
public class DecodingRandom {

  private final int delay1;
  private final int delay2;

  public DecodingRandom(int delay1, int delay2) {
    this.delay1 = delay1;
    this.delay2 = delay2;
  }

  public int getDelay1() {
    return delay1;
  }

  public int getDelay2() {
    return delay2;
  }

  public int getDelay() {
    return delay2 + 15 - delay1;
  }
}
