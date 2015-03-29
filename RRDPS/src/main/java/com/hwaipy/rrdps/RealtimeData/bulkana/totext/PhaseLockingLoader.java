/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps.RealtimeData.bulkana.totext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Administrator 2015-3-29
 */
public class PhaseLockingLoader {

  public static PhaseLockingResultSet load(File file) throws IOException {
    PhaseLockingLoader phaseLockingLoader = new PhaseLockingLoader(file);
    return phaseLockingLoader.parse();
  }
  private final File file;

  private PhaseLockingLoader(File file) {
    this.file = file;
  }

  private PhaseLockingResultSet parse() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      feed(line);
    }
    return new PhaseLockingResultSet(results);
  }

  private boolean firstLine = true;

  private void feed(String line) {
    String[] split = line.split(",");
    if (firstLine) {
      firstLine = false;
      feed(1, Integer.parseInt(split[6]));
    } else {
      feed(Integer.parseInt(split[0]), Integer.parseInt(split[3]));
    }
  }

  private final int[] temp = new int[23];
  private int position = 0;
  private final ArrayList<Double> results = new ArrayList<>();

  private void feed(int index, int count) {
    if (index - 1 != position) {
      throw new RuntimeException();
    }
    temp[position] = count;
    position++;
    if (position >= 23) {
      position = 0;
      Arrays.sort(temp);
      double error = ((double) temp[0]) / (temp[0] + temp[22]);
      results.add(error);
    }
  }

  public static void main(String[] args) throws IOException {
    load(new File("E:\\Experiments\\RRDPS\\采数\\20150325\\原始数据-原始数据解析\\20150324215356-R-APD2-034_稳相数据.csv"));
  }

  private void ana() {
    double[] sums = new double[128];
    int[] counts = new int[128];
    for (int i = 0; i < results.size(); i++) {
      double result = results.get(i);
      int index = i % 128;
      sums[index] += result;
      counts[index]++;
    }
    for (int i = 0; i < 128; i++) {
      sums[i] /= counts[i];
    }
    for (double sum : sums) {
      System.out.println(sum);
    }
  }

  private void outputEveryResult() {
    for (Double result : results) {
      System.out.println(result);
    }
  }
}
