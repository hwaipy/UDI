/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps.RealtimeData.bulkana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * @author Hwaipy 2015-3-28
 */
public class AutoRun {

  private static final ArrayList<String[]> fileList = new ArrayList<>();
  private static final File path = new File("./原始数据-原始数据解析/");

  public static void main(String[] args) throws IOException {
    File file = new File(path, "原始数据解析文件列表.csv");
    long delay1 = 94749450;
    long delay2 = 94744200;
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GB2312"));
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      String[] fileNames = line.split(",");
      fileList.add(fileNames);
    }
    System.out.println(fileList.size());
    parse(1, fileList.size(), delay1, delay2);
  }

  private static void parse(int from, int to, long delay1, long delay2) throws IOException {
    try (RandomAccessFile raf = new RandomAccessFile("output_" + from + "-" + to + ".csv", "rw")) {
      for (int index = from; index < to; index++) {
        String result = parse(index, delay1, delay2);
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
      SExperiment experiment = new SExperiment(path, files);
      experiment.setMask((byte) 0xff);
      experiment.loadData();
      experiment.sync(delay1, delay2);
      experiment.filterAndMerge(1000, 258000);
      int[] statisticDelays = experiment.statisticDelay(600);
      ArrayList<Decoder.Entry> result = experiment.decoding(2000);
      ResultParser resultParser = new ResultParser(result);
      resultParser.ResultOutFile(result, null);
      ResultSet resultSet = new ResultSet(GlobalResult.take());
      double miu = resultSet.getMiu();
      int roundCount = resultSet.getRoundCount();
      int eventCount = resultSet.getEventCount();
      String rs1 = index + "\t" + id + "\t" + miu + "\t" + roundCount + "\t" + eventCount + "\t" + delay1 + "\t" + delay2 + "\t" + statisticDelays[0] + "\t" + statisticDelays[1] + "\t";
      System.out.print(rs1);

      experiment = new SExperiment(path, files);
      experiment.setMask((byte) 0xff);
      experiment.loadData();
      experiment.sync(delay1 + statisticDelays[0], delay2 + statisticDelays[1]);
      experiment.filterAndMerge(1000, 258000);
      result = experiment.decoding(600);
      resultParser = new ResultParser(result);
      resultParser.ResultOutFile(result, null);
      resultParser.ResultbyGate(result, experiment.getBobQRNGList(), null);
      ResultSet optimalResultSet = new ResultSet(GlobalResult.take());
      String rs2 = optimalResultSet.getRatio() + "\t" + optimalResultSet.getCountsByDelay();
      System.out.println(optimalResultSet.getRatio());
      String rs = rs1 + rs2;
      return rs;
    } catch (Exception ex) {
      String e = index + "\t" + id + "\tException:" + ex;
      System.out.println(e);
//      ex.printStackTrace();
      return e;
    }
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
