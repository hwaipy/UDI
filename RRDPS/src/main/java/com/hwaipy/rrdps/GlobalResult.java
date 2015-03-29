/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps;

import java.util.ArrayList;

/**
 * @author Hwaipy 2015-3-24
 */
public class GlobalResult {

  private static ArrayList<Object> list = new ArrayList<>();

  public static void push(Object data) {
    System.err.println("GlobalResult: " + data);
    list.add(data);
  }

  public static ArrayList<Object> take() {
    ArrayList<Object> r = list;
    list = new ArrayList<>();
    return r;
  }

}
