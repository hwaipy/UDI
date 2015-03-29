package com.hwaipy.rrdps.RealtimeData.bulkana.totext;

import com.hwaipy.rrdps.RealtimeData.bulkana.*;
import com.hwaipy.unifieddeviceinterface.DeviceException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Hwaipy
 */
public class TProcess {

  public static void main(String[] args) throws IOException {
    File path = new File("E:/Experiments/RRDPS/采数/20150325/原始数据-原始数据解析/");
    File listFile = new File(path, "原始数据解析文件列表.csv");
    long delay1 = 192940600;
    long delay2 = 192935000;
    TProcess tProcess = new TProcess(path, listFile, delay1, delay2);
    tProcess.parse(3, 4);
  }

  private static final ArrayList<String[]> fileList = new ArrayList<>();
  private final File path;
  private final long delay1;
  private final long delay2;

  public TProcess(File path, File listFile, long delay1, long delay2) throws IOException {
    this.path = path;
    this.delay1 = delay1;
    this.delay2 = delay2;
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(listFile), "GB2312"));
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      String[] fileNames = line.split(",");
      fileList.add(fileNames);
    }
  }

  private void parse(int from, int to) throws IOException {
    try (RandomAccessFile raf = new RandomAccessFile("./input-output/output_" + from + "-" + to + ".csv", "rw")) {
      for (int index = from; index < to; index++) {
        String result = parse(index);
        raf.seek(raf.length());
        raf.write((result + System.lineSeparator()).getBytes("UTF-8"));
      }
    }
  }

  private String parse(int index) {
    String[] fileNames = fileList.get(index);
    return parse(path, fileNames);
  }

  private String parse(File path, String[] files) {
    String id = files[0].substring(0, 14);
    String index = files[0].substring(17, 20);
    ArrayList<String> fileNames = new ArrayList<>();
    fileNames.addAll(Arrays.asList(files));
    fileNames.add(id + "-R-APD2-" + index + "_稳相数据.csv");
    try {
      TExperiment experiment = new TExperiment(path, fileNames);
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

      experiment = new TExperiment(path, fileNames);
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
      System.out.println(rs2);
      String rs = rs1 + rs2;
      return rs;
    } catch (IOException | DeviceException | RuntimeException ex) {
      String e = index + "\t" + id + "\tException:" + ex;
      System.out.println(e);
//      ex.printStackTrace();
      return e;
    }
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
