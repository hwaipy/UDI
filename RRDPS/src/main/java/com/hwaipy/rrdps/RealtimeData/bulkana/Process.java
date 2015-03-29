package com.hwaipy.rrdps.RealtimeData.bulkana;

import com.hwaipy.unifieddeviceinterface.DeviceException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Hwaipy
 */
public class Process {

  public static void main(String[] args) throws IOException, DeviceException {
    String id = "20150325035338";
    String index = "174";
    File path = new File("/Volumes/HITACHI/新建文件夹/0325-原始数据解析/");
    long delay1 = 192941000l;
    long delay2 = 192935600l;
    Experiment experiment = new Experiment(id, index, path);
    experiment.setMask((byte) 0xff);
    experiment.loadData();
    System.out.println("load data finshed!");
    experiment.sync(delay1, delay2);
    System.out.println("sync finshed!");
    experiment.filterAndMerge(1000, 258000);
    System.out.println("merge finshed!");
    ArrayList<Decoder.Entry> result = experiment.decoding(400);
    System.out.println("decode finshed!");
    ResultParser resultParser = new ResultParser(result);
    resultParser.ResultOutFile(result, id);
//    resultParser.ResultStatistics(result, id);
    resultParser.ResultbyGate(result, experiment.getBobQRNGList(), id);
    experiment.test();

  }

}
