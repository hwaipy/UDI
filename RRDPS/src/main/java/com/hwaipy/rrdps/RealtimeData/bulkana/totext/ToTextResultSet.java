/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps.RealtimeData.bulkana.totext;

import com.hwaipy.rrdps.RealtimeData.bulkana.Decoder;
import java.util.ArrayList;

/**
 *
 * @author Administrator 2015-3-29
 */
public class ToTextResultSet {

  private final ArrayList<Decoder.Entry> result;

  public ToTextResultSet(ArrayList<Decoder.Entry> result) {
    this.result = result;
  }

  public String toTextResult() {
    StringBuilder sb = new StringBuilder();
    sb.append("EventDefinition,")
            .append("RoundIndex").append(",")
            .append("PulseIndex").append(",")
            .append("Encode").append(",")
            .append("Decode").append(",")
            .append("Delay").append(",")
            .append("APDTime").append(",")
            .append("PhaseLockingError").append("\n");
    result.stream().forEach((r) -> {
      sb.append("Event,")
              .append(r.getRoundIndex()).append(",")
              .append(r.getPulseIndex()).append(",")
              .append(r.getEncode()).append(",")
              .append(r.getDecode()).append(",")
              .append(r.getDelay()).append(",")
              .append(r.getAPDTime()).append(",")
              .append(r.getPhaseLockingError()).append("\n");
    });
    return sb.toString();
  }
}
