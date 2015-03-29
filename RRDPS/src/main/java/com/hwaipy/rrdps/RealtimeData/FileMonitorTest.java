/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps.RealtimeData;

import java.io.File;

/**
 * @author Hwaipy 2015-3-23
 */
public class FileMonitorTest {

  public static void main(String[] args) throws Exception {
    File file = new File("//TDC-recieve/20150322115632-R-APD2-318.dat");
    System.out.println(file.exists());
  }

}
