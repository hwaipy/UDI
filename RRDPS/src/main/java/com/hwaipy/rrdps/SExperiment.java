package com.hwaipy.rrdps;

import com.hwaipy.unifieddeviceinterface.DeviceException;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.data.TimeEventDataManager;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.data.TimeEventLoader;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.pxi40ps1data.PXI40PS1Loader;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.StreamTimeEventList;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventSegment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Hwaipy
 */
public class SExperiment {

  private static final boolean DEBUG = false;
  private static final int CHANNEL_APD1 = 2;
  private static final int CHANNEL_APD2 = 3;
  private static final Map<String, String[]> FILENAME_MAP = new HashMap<>();

  private String index;
  private final File path;
  private TimeEventList aliceRandomList;
  private TimeEventList bobRandomList;
  private TimeEventList apd1List;
  private TimeEventList apd2List;
  private TimeEventList apdList;
  private byte mask = (byte) 0xff;
  private ArrayList<DecodingRandom> BobQRNGList;
//    private File FineTimeCalibrateFile = new File("G:\\DPS数据处理\\DPS实验数据\\FineTimeCalibrateFile-13.txt");
  private File FineTimeCalibrateFile = null;
  private final String[] fileNames;

  public SExperiment(File path, String[] fileNames) {
    this.path = path;
    this.fileNames = fileNames;
  }

  public void loadData() throws IOException, DeviceException {
    File AliceTDCFile = new File(path, fileNames[0]);
    File AliceQRNGFile = new File(path, fileNames[1]);
    File BobTDCFile = new File(path, fileNames[2]);
    File BobQRNGFile = new File(path, fileNames[3]);
    TimeEventSegment aliceSegment = loadTDCFile(AliceTDCFile);
    System.out.println("Load alice TDC");
    TimeEventSegment bobSegment = loadTDCFile(BobTDCFile);
    System.out.println("Load bob TDC");

    ArrayList<EncodingRandom> aliceQRNGList = loadEncodingQRNGFile(AliceQRNGFile);
    System.out.println("Load alice QRNG");
    ArrayList<DecodingRandom> bobQRNGList = loadDecodingQRNGFile(BobQRNGFile, getMask());
    System.out.println("Load bob QRNG");
    setBobQRNGList(bobQRNGList);
    System.out.println("Set bob QRNG");
//        ArrayList<PhaseLockingResult> phaseLockingResultlist = loadPhaseLockerFile(plrFile, pldFile);
    aliceRandomList = timingQRNG(aliceQRNGList, aliceSegment.getEventList(1));
    System.out.println("Timing alice QRNG");
    setBobRandomList(timingQRNG(bobQRNGList, bobSegment.getEventList(1)));
    System.out.println("Timing bob QRNG");
    System.out.println(aliceQRNGList.size() + "\t" + getBobRandomList().size());
    GlobalResult.push(Math.min(aliceQRNGList.size(), getBobRandomList().size()));
    apd1List = bobSegment.getEventList(CHANNEL_APD1);
    apd2List = bobSegment.getEventList(CHANNEL_APD2);
//        System.out.println(apd1List.size()+"\t"+apd2List.size());
  }

  public void sync(long delay1, long delay2) {
//        TimeEventList aliceGPSList = aliceSegment.getEventList(0);
//        TimeEventList aliceSyncList = aliceSegment.getEventList(1);
//        TimeEventList bobGPSList = bobSegment.getEventList(0);
//        TimeEventList bobSyncList = bobSegment.getEventList(1);
//        long coarseDelay = aliceGPSList.get(0).getTime() - bobGPSList.get(0).getTime();
//        RecursionCoincidenceMatcher syncMatcher = new RecursionCoincidenceMatcher(bobSyncList, aliceSyncList, 1000000, coarseDelay);

    for (int i = 0; i < apd1List.size(); i++) {
      TimeEvent e = apd1List.get(i);
      apd1List.set(new TimeEvent(e.getTime() - delay1, e.getChannel()), i);
    }
    for (int i = 0; i < apd2List.size(); i++) {
      TimeEvent e = apd2List.get(i);
      apd2List.set(new TimeEvent(e.getTime() - delay2, e.getChannel()), i);
    }
  }

  public void filterAndMerge(long before, long after) {
    System.out.println(apd1List.size());
    System.out.println(apd2List.size());
    apd1List = doFilter(apd1List, getBobRandomList(), before, after);
    apd2List = doFilter(apd2List, getBobRandomList(), before, after);
    System.out.println(apd1List.size());
    System.out.println(apd2List.size());
    apdList = new MergedTimeEventList(apd1List, apd2List);
    System.out.println("-" + apdList.size());
  }

  public ArrayList<Decoder.Entry> decoding(long gate) {
    Tagger tagger = new Tagger(getBobRandomList(), apdList, gate);
    ArrayList<Tagger.Entry> tags = tagger.tag();

    Decoder decoder = new Decoder(tags, aliceRandomList, getBobRandomList());
    ArrayList<Decoder.Entry> result = decoder.decode();
    return result;
  }

  private TimeEventList doFilter(TimeEventList apdList, TimeEventList bobRandomList, long before, long after) {
    Iterator<TimeEvent> apdIterator = apdList.iterator();
    Iterator<TimeEvent> syncIterator = bobRandomList.iterator();
    TimeEvent syncEvent = syncIterator.next();
    long startTime = syncEvent.getTime() - before;
    long endTime = syncEvent.getTime() + after;
    TimeEvent apdEvent = apdIterator.next();
    StreamTimeEventList newList = new StreamTimeEventList();
    while (true) {
      long time = apdEvent.getTime();
      if (time <= endTime) {
        if (time >= startTime) {
          newList.offer(apdEvent);
        }
        if (apdIterator.hasNext()) {
          apdEvent = apdIterator.next();
        }
        else {
          apdEvent = null;
        }
      }
      else {
        if (syncIterator.hasNext()) {
          syncEvent = syncIterator.next();
          startTime = syncEvent.getTime() - before;
          endTime = syncEvent.getTime() + after;
        }
        else {
          syncEvent = null;
        }
      }
      if (apdEvent == null || syncEvent == null) {
        break;
      }
    }
    return newList;
  }

  private TimeEventSegment loadTDCFile(File file) throws IOException, DeviceException {
    TimeEventLoader loader = new PXI40PS1Loader(file, FineTimeCalibrateFile);
//        TimeEventLoader loader = new PXI40PS1Loader(file,null);
    return TimeEventDataManager.loadTimeEventSegment(loader);
  }

  private ArrayList<EncodingRandom> loadEncodingQRNGFile(File file) throws IOException, DeviceException {
    FileInputStream input = new FileInputStream(file);
    byte[] b = new byte[16];
    int index = 0;
    ArrayList<EncodingRandom> list = new ArrayList<>();
    while (true) {
      int[] randomList = new int[128];
      int read = input.read(b);
      if (read < 16) {
        break;
      }
      for (int i = 0; i < 128; i++) {
        if (((b[(i / 8)] >>> (7 - (i % 8))) & 0x01) == 0x01) {
          randomList[i] = 0;
        }
        else {
          randomList[i] = 1;
        }
      }
//            System.out.println(Arrays.toString(randomList));
      EncodingRandom encodingRandom = new EncodingRandom(randomList);
      list.add(encodingRandom);
      index++;
    }
//        for (EncodingRandom encodingRandom : list) {
//            System.out.println(encodingRandom);
//        }
    return list;
  }

  private ArrayList<DecodingRandom> loadDecodingQRNGFile(File file, byte mask) throws IOException, DeviceException {
    FileInputStream input = new FileInputStream(file);
    int b;
    b = input.read();//加20km光纤，延时100us,丢一个随机数。
    ArrayList<DecodingRandom> list = new ArrayList<>();
    while (true) {
      b = input.read();
      if (b == -1) {
        break;
      }
      int[] RrandomList = new int[7];
      int[] delaypulse = new int[2];
      byte R = (byte) b;
      for (int i = 0; i < 7; i++) {
        if ((((R & mask) >>> i) & 0x01) == 0x01) {
          RrandomList[i] = 1;
        }
        else {
          RrandomList[i] = 0;
        }
      }
      delaypulse[0] = (RrandomList[0] + RrandomList[1] * 2 + RrandomList[2] * 4 + RrandomList[3] * 8);
      delaypulse[1] = RrandomList[4] * 16 + RrandomList[5] * 32 + RrandomList[6] * 64;
      //System.out.println( delaypulse[0]+"\t"+ delaypulse[1]);
      list.add(new DecodingRandom(delaypulse[0], delaypulse[1]));
    }
    return list;
  }

  private <T> TimeEventList timingQRNG(ArrayList<T> QRNGList, TimeEventList timingList) {
    //数据校验
    if (DEBUG) {
      Iterator<TimeEvent> iterator = timingList.iterator();
      TimeEvent t1 = iterator.next();
      while (iterator.hasNext()) {
        TimeEvent t2 = iterator.next();
        long deltaT = t2.getTime() - t1.getTime();
        if (Math.abs(deltaT) > 150000000 && Math.abs(deltaT) < 700000000000l) {
          throw new RuntimeException();
        }
        t1 = t2;
      }
    }

    int length = Math.min(QRNGList.size(), timingList.size());
    StreamTimeEventList streamTimeEventList = new StreamTimeEventList();
    Iterator<TimeEvent> iterator = timingList.iterator();
    for (int i = 0; i < length; i++) {
      T random = QRNGList.get(i);
      TimeEvent timeEvent;
      if (iterator.hasNext()) {
        timeEvent = iterator.next();
      }
      else {
        throw new RuntimeException();
      }
      ExtandedTimeEvent<T> ete = new ExtandedTimeEvent<>(timeEvent.getTime(), timeEvent.getChannel(), random);
      //System.out.println(timeEvent.getTime()+"\t"+ete.getTime());
      streamTimeEventList.offer(ete);
    }
    return streamTimeEventList;
  }

  public ArrayList<PhaseLockingResult> loadPhaseLockerFile(File plrFile, File pldFile) throws FileNotFoundException, IOException {
//        File path = new File("G:\\DPS数据处理\\DPS实验数据\\2015-2-11\\一次一稳");
//        File plrFile = new File(path, "20150212024655-PC-APD1-6_稳相结果.csv");
//        File pldFile = new File(path, "20150212024655-PC-APD1-6_稳相数据.csv");
    ArrayList<PhaseLockingResult> phaseLockingResultlist = new ArrayList<>();;
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

  private PhaseLockingResult parse(String[] plrs, String[] plds) {
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

  void test() {
    MergedTimeEventList m = new MergedTimeEventList(apd1List, apd2List);
    System.out.println(m.size());
    Iterator<TimeEvent> iterator = m.iterator();
    long t = 0;
    int i = 0;
    while (iterator.hasNext()) {
      TimeEvent next = iterator.next();
      if (next.getTime() < t) {
        System.out.println("wrong");
      }
      else {
        t = next.getTime();
      }
    }
  }

  /**
   * @return the mask
   */
  public byte getMask() {
    return mask;
  }

  /**
   * @param mask the mask to set
   */
  public void setMask(byte mask) {
    this.mask = mask;
  }

  /**
   * @return the bobRandomList
   */
  public TimeEventList getBobRandomList() {
    return bobRandomList;
  }

  /**
   * @param bobRandomList the bobRandomList to set
   */
  public void setBobRandomList(TimeEventList bobRandomList) {
    this.bobRandomList = bobRandomList;
  }

  /**
   * @return the BobQRNGList
   */
  public ArrayList<DecodingRandom> getBobQRNGList() {
    return BobQRNGList;
  }

  /**
   * @param BobQRNGList the BobQRNGList to set
   */
  public void setBobQRNGList(ArrayList<DecodingRandom> BobQRNGList) {
    this.BobQRNGList = BobQRNGList;
  }

}
