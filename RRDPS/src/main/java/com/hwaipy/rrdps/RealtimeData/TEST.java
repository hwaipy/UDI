package com.hwaipy.rrdps.RealtimeData;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Hwaipy
 */
public class TEST {

    public static void main(String[] args) throws IOException {
        File path = new File("/Users/Hwaipy/Desktop/RRDPS_Code/");
        File aliceFile = new File(path, "20150321084710-s-115.dat");
        File bobFile = new File(path, "20150321084710-R-APD2-115.dat");
        DataLoader dataLoader = DataLoader.load("20150321084710", aliceFile, bobFile);
//        dataLoader
    }
}
