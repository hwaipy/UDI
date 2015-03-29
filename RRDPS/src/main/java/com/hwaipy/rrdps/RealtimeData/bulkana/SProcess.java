package com.hwaipy.rrdps.RealtimeData.bulkana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author Hwaipy
 */
public class SProcess {

  private static final ArrayList<String[]> fileList = new ArrayList<>();
  private static final File path = new File("/Users/Hwaipy/Desktop/原始数据-原始数据解析/");

  public static void main(String[] args) throws IOException {
    File file = new File(path, "原始数据解析文件列表.csv");
    long delay1 = 192940500l;
    long delay2 = 192934900l;
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GB2312"));
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      String[] fileNames = line.split(",");
      fileList.add(fileNames);
    }
    parse(3, 4, delay1, delay2);
  }

  private static void parse(int from, int to, long delay1, long delay2) throws IOException {
    for (int index = from; index < to; index++) {
      String result = parse(index, delay1, delay2);
      try (RandomAccessFile raf = new RandomAccessFile("output_" + from + "-" + to + ".csv", "rw")) {
        raf.seek(raf.length());
        raf.write((result + System.lineSeparator()).getBytes("UTF-8"));
      }
    }
  }

  private static String parse(int index, long delay1, long delay2) {
    String[] fileNames = fileList.get(index);
    return parse(path, fileNames, delay1, delay2);
  }

  private static String parse(File path, String[] files, long delay1, long delay2) {
    String id = files[0].substring(0, 14);
    String index = files[0].substring(17, 20);
    try {
      ResultSet resultSet = doParse(path, files, delay1, delay2, 2000);
      double miu = resultSet.getMiu();
      String rs1 = index + "\t" + id + "\t" + miu + "\t";
      System.out.print(rs1);
      ResultSet optimalResultSet = searchForOptimal(path, files, delay1, delay2, 600);
      String rs2 = optimalResultSet.getRatio() + "\t" + optimalResultSet.getCountsByDelay();
      System.out.println(rs2);
      String rs = rs1 + rs2;
      return rs;
    } catch (Exception ex) {
      String e = index + "\t" + id + "\tException:" + ex;
      System.out.println(e);
      return e;
    }
  }

  private static ResultSet searchForOptimal(File path, String[] files, long delay1, long delay2, int gate) throws Exception {
    long d1 = delay1;
    long d2 = delay2;
    int bestI1 = doSearchForOptimal(path, files, d1, d2, gate, true);
    d1 = d1 + bestI1 * 100;
    int bestI2 = doSearchForOptimal(path, files, d1, d2, gate, false);
    d2 = d2 + bestI2 * 100;
    bestI1 = doSearchForOptimal(path, files, d1, d2, gate, true);
    d1 = d1 + bestI1 * 100;
    return doParse(path, files, d1, d2, gate);
  }

  private static int doSearchForOptimal(File path, String[] files, long delay1, long delay2, int gate, boolean dimension) throws Exception {
    double bestCount = 0;
    int bestI = 0;
    for (int i = -5; i < 6; i++) {
      long d1 = delay1 + (dimension ? i * 100 : 0);
      long d2 = delay2 + (dimension ? 0 : i * 100);
      ResultSet resultSet = doParse(path, files, d1, d2, gate);
      double count = resultSet.getEventCount();
      if (count > bestCount) {
        bestCount = count;
        bestI = i;
      }
    }
    return bestI;
  }

  private static ResultSet doParse(File path, String[] files, long delay1, long delay2, long gate) throws Exception {
    SExperiment experiment = new SExperiment(path, files);
    experiment.setMask((byte) 0xff);
    experiment.loadData();
    experiment.sync(delay1, delay2);
    experiment.filterAndMerge(1000, 258000);
    ArrayList<Decoder.Entry> result = experiment.decoding(gate);
    ResultParser resultParser = new ResultParser(result);
    resultParser.ResultOutFile(result, null);
    resultParser.ResultbyGate(result, experiment.getBobQRNGList(), null);
    return new ResultSet(GlobalResult.take());
  }

  private static class ResultSet {

    private final ArrayList<Object> results;

    public ResultSet(ArrayList<Object> results) {
      this.results = results;
    }

    public int getRoundCount() {
      try {
        return (int) results.get(0);
      } catch (Exception e) {
        return 0;
      }
    }

    public int getEventCount() {
      try {
        return (int) results.get(1);
      } catch (Exception e) {
        return 0;
      }
    }

    public double getMiu() {
      try {
        return getEventCount() / (double) getRoundCount()
                * 10//Detection Efficiency
                * 4//Fiber Link
                * 2//Base Choice
                * 2//Bob efficiency
                ;
      } catch (Exception e) {
        return 0;
      }
    }

    public double getRatio() {
      try {
        return (float) results.get(2);
      } catch (Exception e) {
        return 0;
      }
    }

    public String getCountsByDelay() {
      int[] codeCount0 = (int[]) results.get(3);
      int[] errorCount0 = (int[]) results.get(4);
      int[] codeCount1 = (int[]) results.get(5);
      int[] errorCount1 = (int[]) results.get(6);
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 128; i++) {
        sb.append(codeCount0[i]).append("\t").append(errorCount0[i]).append("\t").append(codeCount1[i]).append("\t").append(errorCount1[i]).append("\t");
      }
      return sb.toString();
    }

  }
}
