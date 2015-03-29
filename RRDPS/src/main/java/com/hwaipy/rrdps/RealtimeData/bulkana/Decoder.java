package com.hwaipy.rrdps.RealtimeData.bulkana;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Hwaipy
 */
public class Decoder {

  private final Collection<Tagger.Entry> tags;
  private final TimeEventList aliceQRNG;
  private final TimeEventList bobQRNG;
  private final com.hwaipy.rrdps.RealtimeData.bulkana.totext.PhaseLockingResultSet phaseLockingResultSet;

  public Decoder(Collection<Tagger.Entry> tags, TimeEventList aliceQRNG, TimeEventList bobQRNG, com.hwaipy.rrdps.RealtimeData.bulkana.totext.PhaseLockingResultSet phaseLockingResultSet) {
    this.tags = tags;
    this.aliceQRNG = aliceQRNG;
    this.bobQRNG = bobQRNG;
    this.phaseLockingResultSet = phaseLockingResultSet;
  }

  public ArrayList<Entry> decode() {
    ArrayList<Entry> result = new ArrayList<>();
    for (Tagger.Entry tag : tags) {
      int roundIndex = tag.getRoundIndex();
      int pulseIndex = tag.getPulseIndex();
      int decode = tag.getCode();
      long apdtime = tag.getApdTime();
      if (roundIndex >= aliceQRNG.size() || roundIndex >= bobQRNG.size()) {
        break;
      }
      EncodingRandom encodingRandom = ((ExtandedTimeEvent<EncodingRandom>) aliceQRNG.get(roundIndex)).getProperty();
      DecodingRandom decodingRandom = ((ExtandedTimeEvent<DecodingRandom>) bobQRNG.get(roundIndex)).getProperty();
      int delay = decodingRandom.getDelay();
      int encode = encodingRandom.getEncode(pulseIndex, delay);
      double phaseLockingError = phaseLockingResultSet.getPhaseLockingErrorRate(apdtime, delay);
      if (encode >= 0) {
        Entry entry = new Entry(roundIndex, pulseIndex, encode, decode, delay, apdtime, phaseLockingError);
        result.add(entry);
      }
    }
    return result;
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
