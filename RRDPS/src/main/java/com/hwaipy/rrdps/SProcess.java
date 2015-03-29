package com.hwaipy.rrdps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author Hwaipy
 */
public class SProcess {

  public static void main(String[] args) throws Exception {
    File file = new File("/Volumes/HITACHI/data/0325-原始数据解析/list.csv");
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GB2312"));
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      System.out.println(line);
      String[] fileNames = line.split(",");
      String result = parse(fileNames);
      try (RandomAccessFile raf = new RandomAccessFile("output.csv", "rw")) {
        raf.seek(raf.length());
        raf.write((result + System.lineSeparator()).getBytes("UTF-8"));
      }
    }

  }

  private static String parse(String[] files) throws Exception {
    File path = new File("/Volumes/HITACHI/data/0325-原始数据解析/");
    long delay1 = 192940900l;
    long delay2 = 192935400l;
    SExperiment experiment = new SExperiment(path, files);
    String id = files[0].substring(0, 14);
    String index = files[0].substring(17, 20);
    experiment.setMask((byte) 0xff);
    experiment.loadData();
    System.out.println("load data finshed!");
    experiment.sync(delay1, delay2);
    System.out.println("sync finshed!");
    experiment.filterAndMerge(1000, 258000);
    System.out.println("merge finshed!");
    ArrayList<Decoder.Entry> result = experiment.decoding(600);
    System.out.println("decode finshed!");
    ResultParser resultParser = new ResultParser(result);
    resultParser.ResultOutFile(result, null);
    resultParser.ResultStatistics(result, null);
    resultParser.ResultbyGate(result, experiment.getBobQRNGList(), null);
    experiment.test();

    ArrayList<Object> take = GlobalResult.take();
    int totalRound = (int) take.get(0);
    int click = (int) take.get(1);
    double ratio = (float) take.get(2);
    double miu = click / ((double) totalRound) * 400;
    int[] codeCount0 = (int[]) take.get(3);
    int[] errorCount0 = (int[]) take.get(4);
    int[] codeCount1 = (int[]) take.get(5);
    int[] errorCount1 = (int[]) take.get(6);
    StringBuilder sb = new StringBuilder();
    sb.append(id).append(",").append(index).append(",").append(miu).append(",").append(totalRound).append(",").append(ratio).append(",");
    for (int i = 0; i < 128; i++) {
      sb.append(codeCount0[i]).append(",").append(errorCount0[i]).append(",").append(codeCount1[i]).append(",").append(errorCount1[i]).append(",");
    }
    return sb.toString();
  }

}
