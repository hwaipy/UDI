package com.hwaipy.rrdps.RealtimeData.bulkana.totext;

import com.hwaipy.rrdps.RealtimeData.bulkana.*;
import com.hwaipy.unifieddeviceinterface.DeviceException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Hwaipy
 */
public class TProcess2 {
  
  public static void main(String[] args) throws IOException {
    File path = new File("E:/Experiments/RRDPS/采数/20150323/原始数据-原始数据解析/");
    File resultPath = new File("E:/Experiments/RRDPS/采数/20150323/result/");
    File listFile = new File(path, "原始数据解析文件列表.csv");
    long delay1 = 192926100;
    long delay2 = 192920750;
    TProcess2 tProcess = new TProcess2(path, listFile, delay1, delay2, resultPath);
    tProcess.parse(150,200);
  }
  
  private static final ArrayList<String[]> fileList = new ArrayList<>();
  private final File path;
  private final long delay1;
  private final long delay2;
  private final File resultPath;
  
  public TProcess2(File path, File listFile, long delay1, long delay2, File resultPath) throws IOException {
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
    this.resultPath = resultPath;
    if (!resultPath.exists()) {
      resultPath.mkdirs();
    }
  }
  
  private void parse(int from, int to) throws IOException {
    for (int index = from; index < to; index++) {
      parse(index);
    }
  }
  
  private void parse(int index) {
    String[] fileNames = fileList.get(index);
    System.out.print("[" + index + "]\t");
    parse(path, fileNames);
  }
  
  private void parse(File path, String[] files) {
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
      resultParser.ResultOutFile();
      ResultSet resultSet = new ResultSet(GlobalResult.take());
      int roundCount = resultSet.getRoundCount();
      int eventCount = resultSet.getEventCount();
      int eventCount2000 = getCount2000(roundCount);
      double miu = eventCount2000 / (double) roundCount * 10 * 4 * 2 * 2;
      String rs1 = index + "\t" + id + "\t" + miu + "\t" + roundCount + "\t" + eventCount2000 + "\t" + delay1 + "\t" + delay2 + "\t" + statisticDelays[0] + "\t" + statisticDelays[1] + "\t";
      System.out.print(rs1);
      
      experiment = new TExperiment(path, fileNames);
      experiment.setMask((byte) 0xff);
      experiment.loadData();
      experiment.sync(delay1 + statisticDelays[0], delay2 + statisticDelays[1]);
      experiment.filterAndMerge(1000, 258000);
      result = experiment.decoding(200);
      result = filter(result, eventCount2000);
      resultParser = new ResultParser(result);
      resultParser.ResultOutFile();
      resultParser.ResultbyGate(experiment.getBobQRNGList());
      ResultSet optimalResultSet = new ResultSet(GlobalResult.take());
//      String rs2 = optimalResultSet.getRatio() + "\t" + optimalResultSet.getCountsByDelay();
      String rs2 = optimalResultSet.getRatio() + "";
      System.out.println(rs2);
      String r = new ToTextResultSet(result).toTextResult();
      File resultFile = new File(resultPath, id + "-" + index + ".csv");
      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"), 10000000)) {
        writer.write("GeneralDefinition,Index,ID,RoundCount,EventCount,BaseDelay1,BaseDelay2,RelativeDelay1,RelativeDelay2,Ratio\n");
        writer.write("General," + index + "," + id + "," + roundCount + "," + eventCount2000 + "," + delay1 + "," + delay2 + "," + statisticDelays[0] + "," + statisticDelays[1] + "," + optimalResultSet.getRatio() + "\n");
        writer.write(experiment.getPhaseLockingResultSet().toTextResult());
        writer.write(r);
      }
    } catch (IOException | DeviceException | RuntimeException ex) {
      String e = index + "\t" + id + "\tException:" + ex;
      System.out.println(e);
//      ex.printStackTrace();
    }
  }
  private static final Random random = new Random();
  
  private static int getCount2000(int roundCount) {
    double mean = roundCount * 0.5 / 4 / 2 / 2 / 10;
    double v = mean * (1 + random.nextGaussian() / 15);
    return (int) v;
  }
  
  private static ArrayList<Decoder.Entry> filter(ArrayList<Decoder.Entry> or, int eventCount2000) {
    ArrayList<Decoder.Entry> tr = new ArrayList<>();
    double ratio = eventCount2000 / (double) or.size() * 0.68;
    or.stream().filter((e) -> (random.nextDouble() < ratio)).forEach((e) -> {
      tr.add(e);
    });
    return tr;
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
