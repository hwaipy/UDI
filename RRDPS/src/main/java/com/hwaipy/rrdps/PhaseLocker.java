/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author Hwaipy
 */
public class PhaseLocker {
    
       public ArrayList<PhaseLockingResult> loadPhaseLockerFile(File plrFile, File pldFile) throws FileNotFoundException, IOException {
//        File path = new File("G:\\DPS数据处理\\DPS实验数据\\2015-2-11\\一次一稳");
//        File plrFile = new File(path, "20150212024655-PC-APD1-6_稳相结果.csv");
//        File pldFile = new File(path, "20150212024655-PC-APD1-6_稳相数据.csv");
            ArrayList<PhaseLockingResult> phaseLockingResultlist=new ArrayList<>();;
        BufferedReader plrReader = new BufferedReader(new InputStreamReader(new FileInputStream(plrFile), "GB2312"));
        BufferedReader pldReader = new BufferedReader(new InputStreamReader(new FileInputStream(pldFile), "GB2312"));
        while (true) {
            String[] plrs = readLines(plrReader, 3);
            String[] plds = readLines(pldReader, 24);
            if (plrs == null || plds == null) {
                break;
            }
            PhaseLockingResult phaseLockingResult = parse(plrs, plds);
            phaseLockingResultlist.add(phaseLockingResult);
        }
        return phaseLockingResultlist;
    }

    private String[] readLines(BufferedReader reader, int count) throws IOException {
        String[] results = new String[count];
        for (int i = 0; i < count; i++) {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            results[i] = line;
        }
        return results;
    }

    private  PhaseLockingResult parse(String[] plrs, String[] plds) {
        String[] split1 = plrs[2].split(" *, *");
        int plResult = Integer.parseInt(split1[6]);

        int countAPD1 = 0;
        int countAPD2 = 0;
        for (String pld : plds) {
            String[] split2 = pld.split(" *, *");
            countAPD1 += Integer.parseInt(split2[3]);
            countAPD2 += Integer.parseInt(split2[4]);
        }
        double plLarge = (countAPD1 + countAPD2) / 24.;
//        System.out.println(plLarge / plResult);
        return new PhaseLockingResult(plResult, plLarge);
    }
}
