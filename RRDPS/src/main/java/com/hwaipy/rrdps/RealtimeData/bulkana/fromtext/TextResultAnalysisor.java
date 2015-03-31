/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps.RealtimeData.bulkana.fromtext;

import com.hwaipy.rrdps.RealtimeData.bulkana.fromtext.Result.Entry;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Administrator 2015-3-30
 */
public class TextResultAnalysisor {

  private final File[] files;
  private int from = 0;
  private int to = 0;
  private double ratioThreshold;

  public TextResultAnalysisor(File path) {
    files = path.listFiles();
    to = files.length;
  }

  public void setRange(int from, int to) {
    this.from = from;
    this.to = to;
  }

  public void setRatioThreshold(double threshold) {
    this.ratioThreshold = threshold;
  }

  private Collection<Result> getResults() throws IOException {
    ArrayList<Result> results = new ArrayList<>();
    for (int i = from; i < to; i++) {
      File file = files[i];
      Result result = new Result(file);
      if (result.getRatio() >= ratioThreshold) {
        results.add(result);
      }
    }
    return results;
  }

  private Collection<Entry> getEntries() throws IOException {
    ArrayList<Entry> entries = new ArrayList<>();
    Collection<Result> results = getResults();
    results.stream().forEach((result) -> {
      entries.addAll(result.getEntries());
    });
    return entries;
  }

  public void analysisQBER() throws IOException {
    Collection<Result> results = getResults();
    int totalRound = 0;
    totalRound = results.stream().map((result) -> result.getRoundCount()).reduce(totalRound, Integer::sum);
    Collection<Entry> entries = getEntries();
    int[] r00 = new int[129];
    int[] r01 = new int[129];
    int[] r10 = new int[129];
    int[] r11 = new int[129];
    for (Entry entry : entries) {
      if (entry.getEncode() == 0 && entry.getDecode() == 0) {
        r00[entry.getDelay()]++;
        r00[128]++;
      } else if (entry.getEncode() == 0 && entry.getDecode() == 1) {
        r01[entry.getDelay()]++;
        r01[128]++;
      } else if (entry.getEncode() == 1 && entry.getDecode() == 0) {
        r10[entry.getDelay()]++;
        r10[128]++;
      } else if (entry.getEncode() == 1 && entry.getDecode() == 1) {
        r11[entry.getDelay()]++;
        r11[128]++;
      }
    };
    System.out.println("Total round: " + totalRound);
    System.out.println("Effective experiment time: " + (totalRound / 6500. / 3600) + "hours");
    System.out.println("Total click: " + entries.size());
    System.out.println();
    for (int i = 0; i < 129; i++) {
      if (i == 128) {
        System.out.println();
        System.out.println("Total:");
      }
//      System.out.println("Delay " + i + ": QBER0=" + (r01[i] / ((double) r00[i] + r01[i])) + ", QBER1=" + (r10[i] / ((double) r10[i] + r11[i])) + ", QBERmean=" + ((r01[i] + r10[i]) / ((double) r00[i] + r01[i] + r10[i] + r11[i])));
      System.out.println(i + "\t" + (r01[i] / ((double) r00[i] + r01[i])) + "\t" + (r10[i] / ((double) r10[i] + r11[i])) + "\t" + ((r01[i] + r10[i]) / ((double) r00[i] + r01[i] + r10[i] + r11[i])));
    }
  }

  public void analysisRatioToPhaseLocking() throws IOException {
    int[] r00 = new int[1000];
    int[] r01 = new int[1000];
    int[] r10 = new int[1000];
    int[] r11 = new int[1000];
    for (Entry entry : getEntries()) {
//      if (entry.getDelay() == 1) {
      int[] r;
      if (entry.getEncode() == 0 && entry.getDecode() == 0) {
        r = r00;
      } else if (entry.getEncode() == 0 && entry.getDecode() == 1) {
        r = r01;
      } else if (entry.getEncode() == 1 && entry.getDecode() == 0) {
        r = r10;
      } else if (entry.getEncode() == 1 && entry.getDecode() == 1) {
        r = r11;
      } else {
        throw new RuntimeException();
      }
      double plr = entry.getPhaseLockingError();
      int index = (int) (plr * 1000);
      r[index]++;
//      }
    }
    for (int i = 0; i < 1000; i++) {
      int d00 = r00[i];
      int d01 = r01[i];
      int d10 = r10[i];
      int d11 = r11[i];
      int d0 = d00 + d01;
      int d1 = d10 + d11;
      int d = d0 + d1;
      double e0 = d0 == 0 ? 0 : d01 / (double) d0;
      double e1 = d1 == 0 ? 0 : d10 / (double) d1;
//      System.out.println((i / 10.) + "%\t" + d + "\t" + e0 + "\t" + e1);
      if (d != 0) {
        double e = (d01 + d10) / (double) d;
        System.out.println((i / 10.) + "%\t" + e);
      }
    }
  }

  public void analysisRatioToRelativeTime() throws IOException {
    long period = 5100000000l;
    int bin = 50;
    int[] r00 = new int[bin];
    int[] r01 = new int[bin];
    int[] r10 = new int[bin];
    int[] r11 = new int[bin];
    for (Entry entry : getEntries()) {
      if (entry.getDelay() == 1) {
        int[] r;
        if (entry.getEncode() == 0 && entry.getDecode() == 0) {
          r = r00;
        } else if (entry.getEncode() == 0 && entry.getDecode() == 1) {
          r = r01;
        } else if (entry.getEncode() == 1 && entry.getDecode() == 0) {
          r = r10;
        } else if (entry.getEncode() == 1 && entry.getDecode() == 1) {
          r = r11;
        } else {
          throw new RuntimeException();
        }
        long apdTime = entry.getAPDTime();
        long apdTimeInSecond = apdTime % 1000000000000l;
        long w = period / bin;
        System.out.println(w);
        int p = (int) ((apdTimeInSecond % period) / w);
        r[p]++;
      }
    }
    for (int i = 0; i < bin; i++) {
      int d00 = r00[i];
      int d01 = r01[i];
      int d10 = r10[i];
      int d11 = r11[i];
      int d0 = d00 + d01;
      int d1 = d10 + d11;
      int d = d0 + d1;
      double e0 = d0 == 0 ? 0 : d01 / (double) d0;
      double e1 = d1 == 0 ? 0 : d10 / (double) d1;
//      System.out.println((i / 10.) + "%\t" + d + "\t" + e0 + "\t" + e1);
      if (d != 0) {
        double e = (d01 + d10) / (double) d;
        System.out.println(i + "\t" + d + "\t" + e);
      }
    }
  }

  public void analysisGateEfficiency() throws IOException {
    int eventCount2000 = 0;
    Collection<Result> results = getResults();
    for (Result result : results) {
      eventCount2000 += result.getEventCount2000();
//      System.out.println(result.getEventCount2000());
      if (result.getEventCount2000() < 500 || result.getEventCount2000() > 2000) {
        System.out.println(result.getIndex() + "-" + result.getId());
      }
    }
    Collection<Entry> entries = getEntries();
    int eventCount = entries.size();
    System.out.println(eventCount2000);
    System.out.println(eventCount);
  }

  public static void main(String[] args) throws IOException {
    TextResultAnalysisor textResultAnalysisor = new TextResultAnalysisor(new File("E:/Experiments/RRDPS/采数/result-20km/"));
    textResultAnalysisor.setRatioThreshold(6);
    textResultAnalysisor.analysisQBER();
//    textResultAnalysisor.analysisGateEfficiency();
  }

}
