package com.hwaipy.rrdps.RealtimeData.bulkana;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Hwaipy
 */
public class ResultParser {

  private final ArrayList<Decoder.Entry> result;

  public ResultParser(ArrayList<Decoder.Entry> result) {
    this.result = result;
  }

  public void ResultOutFile(ArrayList<Decoder.Entry> result, String id) throws IOException {
    int ErrorCount = 0;
    int RightCodeCount = 0;
    int ErrorCount0 = 0;
    int ErrorCount1 = 0;
    int CodeCount0 = 0;
    int CodeCount1 = 0;
    float Ratio = 0;
    for (Decoder.Entry result1 : result) {
      if (result1.getEncode() == 0) {
        if (result1.getDecode() == 0) {
          CodeCount0++;
        }
        else {
          ErrorCount1++;
        }
      }
      else if (result1.getEncode() == 1) {
        if (result1.getDecode() == 1) {
          CodeCount1++;
        }
        else {
          ErrorCount0++;
        }
      }
    }
    RightCodeCount = CodeCount0 + CodeCount1;
    ErrorCount = ErrorCount0 + ErrorCount1;
    Ratio = (float) RightCodeCount / (float) ErrorCount;
    GlobalResult.push(RightCodeCount + ErrorCount);
    GlobalResult.push(Ratio);
  }

  public void ResultbyGate(ArrayList<Decoder.Entry> result, ArrayList<DecodingRandom> bobQRNGList, String id) throws IOException {
    int[] ErrorCount0 = new int[128];
    int[] ErrorCount1 = new int[128];
    int[] CodeCount0 = new int[128];
    int[] CodeCount1 = new int[128];
    int delay = 0;
    for (Decoder.Entry result1 : result) {
      DecodingRandom decodingRandom = bobQRNGList.get(result1.getRoundIndex());
      delay = decodingRandom.getDelay();
      if (result1.getEncode() == 0) {
        if (result1.getDecode() == 0) {
          CodeCount0[delay]++;
        }
        else {
          ErrorCount1[delay]++;
        }
      }
      else if (result1.getEncode() == 1) {
        if (result1.getDecode() == 1) {
          CodeCount1[delay]++;
        }
        else {
          ErrorCount0[delay]++;
        }
      }
    }
    GlobalResult.push(CodeCount0);
    GlobalResult.push(ErrorCount0);
    GlobalResult.push(CodeCount1);
    GlobalResult.push(ErrorCount1);
  }

}
