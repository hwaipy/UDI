/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps.ana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Hwaipy 2015-3-28
 */
public class InterferenceAna {

  private final File file;

  private InterferenceAna(File file) {
    this.file = file;
  }

  private void parse() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      feed(line);
    }
    //    ana();
    //    outputEveryResult();
    outputInStates();
  }

  private boolean firstLine = false;

  private void feed(String line) {
    String[] split = line.split(",");
    if (firstLine) {
      firstLine = false;
      feed(1, Integer.parseInt(split[6]));
    }
    else {
      feed(Integer.parseInt(split[1]), Integer.parseInt(split[4]));
    }
  }

  private int sum = 0;
  private int position = 0;
  private final ArrayList<Double> results = new ArrayList<>();

  private void feed(int index, int count) {
    if (index - 1 != position) {
      throw new RuntimeException();
    }
    sum += count;
    position++;
    if (position >= 23) {
      position = 0;
      double error = sum / 23.;
      results.add(error);
      sum = 0;
    }
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
    for (double r : sums) {
      System.out.println(r);
    }
  }

  private void outputEveryResult() {
    results.stream().forEach((result) -> {
      System.out.println(result);
    });
  }

  private void outputInStates() {
    int round = 0;
    System.out.print(round + "\t");
    round++;
    for (int i = 0; i < results.size(); i++) {
      System.out.print(results.get(i) + "\t");
      if (i % 128 == 127) {
        System.out.println();
        System.out.print(round + "\t");
        round++;
      }
    }
  }

  public static void main(String[] args) throws IOException {
//    new InterferenceAna(new File("/Volumes/HITACHI/data/0325-原始数据解析/20150324201115-R-APD2-001_稳相数据.csv")).parse();
    new InterferenceAna(new File("/Users/Hwaipy/Downloads/20150328005546-R-APD2-012_稳相数据.csv")).parse();
  }

}
