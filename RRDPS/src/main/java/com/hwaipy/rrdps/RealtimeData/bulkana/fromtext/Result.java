package com.hwaipy.rrdps.RealtimeData.bulkana.fromtext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Administrator 2015-3-30
 */
public class Result {

  private final ArrayList<Entry> entries = new ArrayList<>();
  private double ratio = 0;
  private int eventCount2000;
  private int roundCount;
  private String index;
  private String id;

  public Result(File file) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    load(reader);
  }

  public double getRatio() {
    return ratio;
  }

  public int getEventCount2000() {
    return eventCount2000;
  }

  public String getIndex() {
    return index;
  }

  public String getId() {
    return id;
  }

  public int getRoundCount() {
    return roundCount;
  }

  public Collection<Entry> getEntries() {
    return Collections.unmodifiableCollection(entries);
  }

  private void load(BufferedReader reader) throws IOException {
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      String[] split = line.split(",");
      switch (split[0]) {
        case "General":
          ratio = Double.parseDouble(split[9]);
          eventCount2000 = Integer.parseInt(split[4]);
          index = split[1];
          id = split[2];
          roundCount = Integer.parseInt(split[3]);
          break;
        case "Event":
          Entry entry = new Entry(Integer.parseInt(split[1]), Integer.parseInt(split[2]),
                  Integer.parseInt(split[3]), Integer.parseInt(split[4]),
                  Integer.parseInt(split[5]), Long.parseLong(split[6]),
                  Double.parseDouble(split[7]));
          entries.add(entry);
          break;
        default:
          break;
      }
    }
  }

  public class Entry {

    private final int roundIndex;
    private final int pulseIndex;
    private final int encode;
    private final int decode;
    private final int delay;
    private final long APDTime;
    private final double phaseLockingError;

    private Entry(int roundIndex, int pulseIndex, int encode, int decode, int delay, long APDTime, double phaseLockingError) {
      this.roundIndex = roundIndex;
      this.pulseIndex = pulseIndex;
      this.encode = encode;
      this.decode = decode;
      this.delay = delay;
      this.APDTime = APDTime;
      this.phaseLockingError = phaseLockingError;
    }

    public int getRoundIndex() {
      return roundIndex;
    }

    public int getPulseIndex() {
      return pulseIndex;
    }

    public int getEncode() {
      return encode;
    }

    public int getDecode() {
      return decode;
    }

    public int getDelay() {
      return delay;
    }

    public long getAPDTime() {
      return APDTime;
    }

    public double getPhaseLockingError() {
      return phaseLockingError;
    }

  }
}
