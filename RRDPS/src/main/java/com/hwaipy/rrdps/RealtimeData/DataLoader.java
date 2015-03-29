package com.hwaipy.rrdps.RealtimeData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author Hwaipy
 */
public class DataLoader {

  private static final File PATH = new File("./dataloader/");
  private static final int DATA_TYPE_PL = 0xBB;
  private static final int DATA_TYPE_TDC = 0xFF;
  private static final int DATA_TYPE_RNG = 0xAA;
  private static final int DATA_TYPE_SEND_RNG = 0xCC;
  private final String id;
  private final InputStream aliceStream;
  private final InputStream bobStream;
  private File aliceQRNGFile;
  private File aliceTDCFile;
  private File bobQRNGFile;
  private File bobTDCFile;
  private File phaseLockingDataFile;
  private File phaseLockingResultFile;

  private DataLoader(String id, InputStream aliceStream, InputStream bobStream) {
    this.id = id;
    this.aliceStream = aliceStream;
    this.bobStream = bobStream;
  }

  private void load() throws IOException {
    aliceQRNGFile = new File(PATH, id + "_A_QRGN.data");
    aliceTDCFile = new File(PATH, id + "_A_TDC.data");
    bobQRNGFile = new File(PATH, id + "_B_QRGN.data");
    bobTDCFile = new File(PATH, id + "_B_TDC.data");
    phaseLockingDataFile = new File(PATH, id + "_PL_data.data");
    phaseLockingResultFile = new File(PATH, id + "_PL_result.data");
    loadData(aliceStream, aliceTDCFile, aliceQRNGFile, null, null);
    loadData(bobStream, bobTDCFile, bobQRNGFile, phaseLockingDataFile, phaseLockingResultFile);
  }

  private void loadData(InputStream inputStream, File tdcFile, File qrngFile, File phaseLockingDataFile, File phaseLockingResultFile) throws IOException {
    InputStream aliceFrameStream = new FrameInputStream(inputStream, 2048);
    PrintWriter phaseLockingDataWriter = null;
    PrintWriter phaseLockingResultWriter = null;
    try (BufferedOutputStream tdcOutputStream = new BufferedOutputStream(new FileOutputStream(tdcFile))) {
      try (BufferedOutputStream qrngOutputStream = new BufferedOutputStream(new FileOutputStream(qrngFile))) {
        if (phaseLockingDataFile != null) {
          phaseLockingDataWriter = new PrintWriter(new FastFileWriter(phaseLockingDataFile));
        }
        if (phaseLockingResultFile != null) {
          phaseLockingResultWriter = new PrintWriter(new FastFileWriter(phaseLockingResultFile));
        }
        //发射端第一个随机数计数为1
        while (true) {
          int[] buffer = readToBuffer(aliceFrameStream);
          if (buffer == null) {
            break;
          }
          int dataType = buffer[1];
          switch (dataType) {
            case DATA_TYPE_PL:
              processPhaseLocking(buffer, qrngOutputStream, phaseLockingDataWriter, phaseLockingResultWriter);
              break;
            case DATA_TYPE_TDC:
              processTDCData(buffer, tdcOutputStream);
              break;
            case DATA_TYPE_RNG:
              break;
            case DATA_TYPE_SEND_RNG:
              processRNGData(buffer, qrngOutputStream);
              break;
            default:
//              throw new RuntimeException("" + dataType);
          }
        }
        if (phaseLockingDataWriter != null) {
          phaseLockingDataWriter.close();
        }
        if (phaseLockingResultWriter != null) {
          phaseLockingResultWriter.close();
        }
      }
    }
  }

  private int[] readToBuffer(InputStream inputStream) throws IOException {
    int[] buffer = new int[8];
    for (int i = 0; i < 8; i++) {
      buffer[i] = inputStream.read();
      if (buffer[i] == -1) {
        return null;
      }
    }
    return buffer;
  }

  private int getCoarse(int[] buffer) {
    int coarse;
    coarse = buffer[3] & 0x0F;
    coarse = (coarse << 8) | buffer[2];
    coarse = (coarse << 8) | buffer[5];
    coarse = (coarse << 8) | buffer[4];
    return coarse;
  }

  private final int[] preCoarse = new int[]{-1, -1};

  private void processTDCData(int[] buffer, OutputStream outputStream) throws IOException {
    //没有判断同步信号是否丢失
    int curCoarse = getCoarse(buffer);
    boolean drop = false;
    //判断APD1，APD2通道当前粗计数是否比前一个粗计数小（各通道时间绝对增长）
    if (buffer[6] == 0x42) {
      if (preCoarse[0] > curCoarse && (preCoarse[0] - curCoarse < 10000)) {
        drop = true;
      }
      else {
        preCoarse[0] = curCoarse;
      }
    }
    else if (buffer[6] == 0x43) {
      if (preCoarse[1] > curCoarse && (preCoarse[1] - curCoarse < 10000)) {
        drop = true;
      }
      else {
        preCoarse[1] = curCoarse;
      }
    }
    if (!drop) {
      for (int b : buffer) {
        outputStream.write(b);
      }
    }
  }

  private int qrngRepeat = 0;
  private int preQRNGIndex = -1;

  private void processRNGData(int[] buffer, BufferedOutputStream qrngOutputStream) throws IOException {
    int[] data = new int[]{buffer[0], buffer[3], buffer[2], buffer[5], buffer[4], buffer[7], buffer[6]};
    int index = (data[0] << 16) + (data[1] << 8) + data[2];
    if (preQRNGIndex == -1) {
      preQRNGIndex = index;
    }
    for (int i = 3; i < 7; i++) {
      qrngOutputStream.write(data[i]);
    }
    qrngRepeat++;
    if (index < preQRNGIndex) {
      //Index逆序
      throw new RuntimeException();
    }
    else if (index == preQRNGIndex) {
      //正常
      if (qrngRepeat == 4) {
        //正常，且攒齐4个
        preQRNGIndex++;
        qrngRepeat = 0;
      }
    }
    else {
      //有丢数
      int fixCount = (4 - qrngRepeat) + (index - preQRNGIndex - 1) * 4;
      for (int i = 0; i < fixCount; i++) {
        for (int j = 3; j < 7; j++) {
          qrngOutputStream.write(data[j]);
        }
      }
      qrngRepeat = 0;
      preQRNGIndex = index;
      System.out.println(fixCount + " Random Number missed.");
    }
  }

  private int nextPhaseLockingIndex = 0;

  private void processPhaseLocking(int[] buffer, BufferedOutputStream qrngOutputStream, PrintWriter phaseLockingDataWriter, PrintWriter phaseLockingResultWriter) throws IOException {
    int index = buffer[0];
    if (index != nextPhaseLockingIndex) {
      System.out.println("Phase Locking Data lost.");
    }
    nextPhaseLockingIndex = (index + 1) % 256;
    int flag = (buffer[3] & 0xF0);
    switch (flag) {
      //稳相算法中间数据
      case 0xF0:
//                phaseLockingProcess(buffer, phaseLockingDataWriter);
        break;
      //稳相算法输出结果
      case 0xA0:
        phaseLockingResult(buffer, phaseLockingResultWriter);
        break;
      //稳相算法输出结果
      case 0xC0:
        phaseLockingData(buffer, phaseLockingResultWriter);
        break;
      case 0xB0:
        phaseLockingOut(buffer, qrngOutputStream, phaseLockingResultWriter);
        break;
      default:
        throw new RuntimeException();
    }
  }

  private final double[] halfVoltage = new double[256];
  private final int[] countAPD1 = new int[256];
  private final int[] countAPD2 = new int[256];

  private void phaseLockingProcess(int[] buffer, PrintWriter phaseLockingDataWriter) throws IOException {
    int step = (buffer[5] - 1) % 256;
    if (step < 0) {
      throw new RuntimeException();
    }
    halfVoltage[step] = ((buffer[2] | ((buffer[3] & 0x0F) << 8)) - 2048) * 5.0 / 4096.0;
    countAPD2[step] = (buffer[4] << 4) | ((buffer[7] & 0xF0) >> 4);
    countAPD1[step] = buffer[6] | ((buffer[7] & 0x03) << 8);
    phaseLockingDataWriter.printf("通道,%d,%f,%d,%d,", (step + 1), halfVoltage[step], countAPD1[step], countAPD2[step]);
    if (step != 4) {
      phaseLockingDataWriter.print("\r\n");
    }
    else {
      double sinX = countAPD1[2] - countAPD1[0];
      double cosX = countAPD1[3] - countAPD1[1];
      double PMVoltage = Math.abs(halfVoltage[1] - halfVoltage[0]) * 2.0;
      double offsetVoltage = halfVoltage[1];
      double atanX = Math.atan(sinX / cosX);
      phaseLockingDataWriter.printf("%f,", atanX);
      atanX = atanX / Math.PI;
      phaseLockingDataWriter.printf("%f,", atanX);
      if (sinX >= 0 && cosX < 0) {
        atanX = atanX + 1;
      }
      else if (sinX < 0 && cosX < 0) {
        atanX = atanX - 1;
      }
      phaseLockingDataWriter.printf("%f,", atanX);
      double expectedAlgrithmOut = -1 * atanX * PMVoltage + offsetVoltage;
      phaseLockingDataWriter.printf("%f\t", atanX * PMVoltage * 4096.0 / 5.0);
      phaseLockingDataWriter.printf("%f\t", 2048 + 1 * expectedAlgrithmOut * 4096.0 / 5.0);
      phaseLockingDataWriter.printf("VC计算结果,%f,反正切值,%f,计算结果,%f,压差,%f,半波电压值,%f,偏移电压,%f\n", expectedAlgrithmOut, atanX, expectedAlgrithmOut, expectedAlgrithmOut - halfVoltage[4], PMVoltage, offsetVoltage);
    }
  }

  private void phaseLockingResult(int[] buffer, PrintWriter phaseLockingResultWriter) throws IOException {
    int pocNum = ((buffer[4] & 0x0F) << 4) | ((buffer[7] & 0xF0) >> 4);
    int dacValue = buffer[6] | ((buffer[7] & 0x0F) << 8);
    phaseLockingResultWriter.printf("稳相计算结果：POC编号,%3d,PM电压,%3d,%f\n", pocNum, dacValue, (dacValue - 2048) * 5.0 / 4096.0);
  }

  private void phaseLockingData(int[] buffer, PrintWriter phaseLockingResultWriter) throws IOException {
    int apdCount = buffer[5] + (buffer[2] << 8);
    int pocNum = buffer[4];
    int dacValue = buffer[6] | ((buffer[7] & 0x0F) << 8);
    phaseLockingResultWriter.printf("稳相计算结果：POC编号,%3d,PM电压,%3d,%f,APD计数,%3d,\n", pocNum, dacValue, (dacValue - 2048) * 5.0 / 4096.0, apdCount);
  }

  private int preRNGIndex = 1;

  private void phaseLockingOut(int[] buffer, BufferedOutputStream qrngOutputStream, PrintWriter phaseLockingResultWriter) throws IOException {
    int pocNum = buffer[4];
    int dacValue = buffer[6] | ((buffer[7] & 0x0F) << 8);
    phaseLockingResultWriter.printf("随机数输出：POC编号,%3d,PM电压,%3d,%f,\n", pocNum, dacValue, (dacValue - 2048) * 5.0 / 4096.0);
    int index = ((buffer[2] << 8) + buffer[5]) % 0x10000;
    if (preRNGIndex != index && preRNGIndex != 1) {
      System.out.println("丢随机数");
      for (int i = 0; i < (index + 0x10000 - preRNGIndex) % 0x10000; i++) {
        qrngOutputStream.write(pocNum);
      }
    }
    preRNGIndex = (index + 1) % 0x10000;
    qrngOutputStream.write(pocNum);
  }

  public static DataLoader load(String id, InputStream aliceStream, InputStream bobStream) throws IOException {
    DataLoader dataLoader = new DataLoader(id, aliceStream, bobStream);
    dataLoader.load();
    return dataLoader;
  }

  public static DataLoader load(String id, File aliceFile, File bobFile) throws IOException {
    DataLoader dataLoader;
    try (FileInputStream aliceStream = new FileInputStream(aliceFile)) {
      try (FileInputStream bobStream = new FileInputStream(bobFile)) {
        dataLoader = load(id, aliceStream, bobStream);
      }
    }
    return dataLoader;
  }

}
