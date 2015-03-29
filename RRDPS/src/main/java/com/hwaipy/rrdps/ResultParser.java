package com.hwaipy.rrdps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Hwaipy
 */
public class ResultParser {

  private final ArrayList<Decoder.Entry> result;

  public ResultParser(ArrayList<Decoder.Entry> result) {
    this.result = result;
    System.out.println(result.get(result.size() - 1).getRoundIndex());
  }

  public double[] ResultOutFile(ArrayList<Decoder.Entry> result, String id) throws IOException {
    int ErrorCount = 0;
    int RightCodeCount = 0;
    int ErrorCount0 = 0;
    int ErrorCount1 = 0;
    int CodeCount0 = 0;
    int CodeCount1 = 0;
    float Ratio = 0, APD1Ratio = 0, APD2Ratio = 0;
    File codewrite = new File("G:\\DPS数据处理\\DPS实验数据\\数据处理TXT\\" + id + "成码数据" + ".txt"); // 相对路径，如果没有则要建立一个新的output。txt文件  
    codewrite.createNewFile(); // 创建新文件  
    BufferedWriter code = new BufferedWriter(new FileWriter(codewrite));
    for (int i = 0; i < result.size(); i++) {
      if (result.get(i).getEncode() == 0) {

        if (result.get(i).getDecode() == 0) {
          CodeCount0++;
          code.write("Round: \t" + result.get(i).getRoundIndex() + "\t APDTime\t" + result.get(i).getAPDTime() + "\t PulseIndex: \t" + result.get(i).getPulseIndex()
                  + "\t Encode: \t" + result.get(i).getEncode() + "\t Decode: \t" + result.get(i).getDecode() + "\r\n");
        }
        else {
          ErrorCount1++;
          code.write("Round: \t" + result.get(i).getRoundIndex() + "\t APDTime\t" + result.get(i).getAPDTime() + "\t PulseIndex: \t" + result.get(i).getPulseIndex()
                  + "\t Encode: \t" + result.get(i).getEncode() + "\t Decode: \t" + result.get(i).getDecode() + "\t Error Code!" + "\r\n");
        }
      }
      else if (result.get(i).getEncode() == 1) {
        if (result.get(i).getDecode() == 1) {
          CodeCount1++;
          code.write("Round: \t" + result.get(i).getRoundIndex() + "\t APDTime\t" + result.get(i).getAPDTime() + "\t PulseIndex: \t" + result.get(i).getPulseIndex()
                  + "\t Encode: \t" + result.get(i).getEncode() + "\t Decode: \t" + result.get(i).getDecode() + "\r\n");
        }
        else {
          ErrorCount0++;
          code.write("Round: \t" + result.get(i).getRoundIndex() + "\t APDTime\t" + result.get(i).getAPDTime() + "\t PulseIndex: \t" + result.get(i).getPulseIndex()
                  + "\t Encode: \t" + result.get(i).getEncode() + "\t Decode: \t" + result.get(i).getDecode() + "\t Error Code!" + "\r\n");
        }
      }
    }
    APD1Ratio = (float) CodeCount0 / (float) ErrorCount1;
    APD2Ratio = (float) CodeCount1 / (float) ErrorCount0;
    RightCodeCount = CodeCount0 + CodeCount1;
    ErrorCount = ErrorCount0 + ErrorCount1;
    Ratio = (float) RightCodeCount / (float) ErrorCount;
    GlobalResult.push(RightCodeCount + ErrorCount);
    GlobalResult.push(Ratio);
    double total = (float) RightCodeCount + (float) ErrorCount;
    code.write("APD1 Code 0:\t" + CodeCount0 + "\t Error Code 1:\t" + ErrorCount1 + "\t APD1 Ratio:\t" + APD1Ratio + "\r\n");
    code.write("APD2 Code 1:\t" + CodeCount1 + "\t Error Code 0:\t" + ErrorCount0 + "\t APD2 Ratio:\t" + APD2Ratio + "\r\n");
    code.write("Total Right Code:\t" + RightCodeCount + "\t Total Error Code :\t" + ErrorCount + "\t Ratio:\t" + Ratio + "\r\n");
    code.flush();
    code.close();

    System.out.println("APD1 Code 0:\t" + CodeCount0 + "\t Error Code 1:\t" + ErrorCount1 + "\t APD1 Ratio:\t" + APD1Ratio);
    System.out.println("APD2 Code 1:\t" + CodeCount1 + "\t Error Code 0:\t" + ErrorCount0 + "\t APD2 Ratio:\t" + APD2Ratio);
    System.out.println("Total Right Code:\t" + RightCodeCount + "\t Total Error Code :\t" + ErrorCount + "\t Ratio:\t" + Ratio);

    return new double[]{total, Ratio};
  }

  public void ResultbyGate(ArrayList<Decoder.Entry> result, ArrayList<DecodingRandom> bobQRNGList, String id) throws IOException {
    int ErrorCount = 0;
    int RightCodeCount = 0;
    int[] ErrorCount0 = new int[128];
    int[] ErrorCount1 = new int[128];
    int[] CodeCount0 = new int[128];
    int[] CodeCount1 = new int[128];
    int delay = 0;
    float[] delaycount = new float[128];
    float[] Ratio = new float[128], APD1Ratio = new float[128], APD2Ratio = new float[128], NormalizedCodeCount0 = new float[128], NormalizedCodeCount1 = new float[128];

    System.out.println("\r\nResult analysis by gate\r\n");
//        File codewrite = new File("G:\\DPS数据处理\\DPS实验数据\\数据处理TXT\\" + id + "成码数据" + ".txt"); // 相对路径，如果没有则要建立一个新的output。txt文件  
//        codewrite.createNewFile(); // 创建新文件  
//        BufferedWriter code = new BufferedWriter(new FileWriter(codewrite));
    for (int i = 0; i < result.size(); i++) {
      DecodingRandom decodingRandom = bobQRNGList.get(result.get(i).getRoundIndex());
      delay = decodingRandom.getDelay();
      if (result.get(i).getEncode() == 0) {
        if (result.get(i).getDecode() == 0) {
          CodeCount0[delay]++;
        }
        else {
          ErrorCount1[delay]++;
        }
      }
      else if (result.get(i).getEncode() == 1) {
        if (result.get(i).getDecode() == 1) {
          CodeCount1[delay]++;
        }
        else {
          ErrorCount0[delay]++;
        }
      }
    }

    for (int i = 0; i < 128; i++) {

      APD1Ratio[i] = (float) CodeCount0[i] / (float) ErrorCount1[i];
      APD2Ratio[i] = (float) CodeCount1[i] / (float) ErrorCount0[i];
      NormalizedCodeCount0[i] = (CodeCount0[i] + ErrorCount1[i]) * 128 / (128 - delaycount[i]);
      NormalizedCodeCount1[i] = (CodeCount1[i] + ErrorCount0[i]) * 128 / (128 - delaycount[i]);
//        RightCodeCount = CodeCount0 + CodeCount1;
//        ErrorCount = ErrorCount0 + ErrorCount1;
//        Ratio = (float) RightCodeCount / (float) ErrorCount;
      System.out.println(i + "\t APD1 Code 0:\t" + CodeCount0[i] + "\t Error Code 1:\t" + ErrorCount1[i]
              + "\t APD1 Ratio:\t" + APD1Ratio[i] + "\t Normalized　Code 0:\t" + NormalizedCodeCount0[i]);
      System.out.println(i + "\t APD2 Code 1:\t" + CodeCount1[i] + "\t Error Code 0:\t" + ErrorCount0[i]
              + "\t APD2 Ratio:\t" + APD2Ratio[i] + "\t Normalized　Code 1:\t" + NormalizedCodeCount1[i]);
      //System.out.println("Total Right Code:\t" + RightCodeCount + "\t Total Error Code :\t" + ErrorCount + "\t Ratio:\t" + Ratio);
    }

    GlobalResult.push(CodeCount0);
    GlobalResult.push(ErrorCount0);
    GlobalResult.push(CodeCount1);
    GlobalResult.push(ErrorCount1);
  }

  public void ResultStatistics(ArrayList<Decoder.Entry> result, String id) throws IOException {

    int ErrorCount0 = 0;
    int ErrorCount1 = 0;
    int CodeCount0 = 0;
    int CodeCount1 = 0;
    float APD1Ratio = 0, APD2Ratio = 0;
    int Second = 1;
    File codewrite = new File("G:\\DPS数据处理\\DPS实验数据\\数据处理TXT\\" + id + "每秒统计" + ".txt"); // 相对路径，如果没有则要建立一个新的output。txt文件  
    codewrite.createNewFile(); // 创建新文件  
    BufferedWriter code = new BufferedWriter(new FileWriter(codewrite));
    System.out.println("Result analysis by second\r\n");
    code.write("Second\t" + "APD1 Code 0\t" + "Error Code 1\t" + "APD1 Ratio\t"
            + "APD2 Code 1\t" + "Error Code 0\t" + "APD2 Ratio\t" + "\r\n");
    System.out.println("Second\t" + "APD1 Code 0\t" + "Error Code 1\t" + "APD1 Ratio\t"
            + "APD2 Code 1\t" + "Error Code 0\t" + "APD2 Ratio\t" + "\r\n");
    for (int i = 0; i < result.size(); i++) {
      //TODO 时间信息需要补上
      if (Second > (int) (result.get(i).getAPDTime() / 1000000000000l)) {

        if (result.get(i).getEncode() == 0) {

          if (result.get(i).getDecode() == 0) {
            CodeCount0++;

          }
          else {
            ErrorCount1++;
          }
        }
        else if (result.get(i).getEncode() == 1) {
          if (result.get(i).getDecode() == 1) {
            CodeCount1++;
          }
          else {
            ErrorCount0++;
          }
        }
      }
      else {
        APD1Ratio = (float) CodeCount0 / (float) ErrorCount1;
        APD2Ratio = (float) CodeCount1 / (float) ErrorCount0;
        code.write((Second - 1) + "\t" + CodeCount0 + "\t" + ErrorCount1 + "\t " + APD1Ratio
                + "\t " + CodeCount1 + "\t " + ErrorCount0 + "\t " + APD2Ratio + "\r\n");

        System.out.println((Second - 1) + "\t" + CodeCount0 + "\t" + ErrorCount1 + "\t " + APD1Ratio
                + "\t " + CodeCount1 + "\t " + ErrorCount0 + "\t " + APD2Ratio + "\r\n");
        APD1Ratio = 0;
        APD2Ratio = 0;
        CodeCount0 = 0;
        CodeCount1 = 0;
        ErrorCount0 = 0;
        ErrorCount1 = 0;
        Second = (int) (result.get(i).getAPDTime() / 1000000000000l) + 1;
      }

    }
    code.flush();
    code.close();

  }

  public void ResultPrint(ArrayList<Decoder.Entry> result) {
    int ErrorCount = 0;
    int RightCodeCount = 0;
    int ErrorCount0 = 0;
    int ErrorCount1 = 0;
    int CodeCount0 = 0;
    int CodeCount1 = 0;
    float Ratio = 0, APD1Ratio = 0, APD2Ratio = 0;
    System.out.println("Code Count: " + result.size() + "\r\n");
    for (int i = 0; i < result.size(); i++) {
      if (result.get(i).getEncode() == 0) {

        if (result.get(i).getDecode() == 0) {
          CodeCount0++;
          System.out.println("Round: \t" + result.get(i).getRoundIndex() + "\t PulseIndex: \t" + result.get(i).getPulseIndex()
                  + "\t Encode: \t" + result.get(i).getEncode() + "\t Decode: \t" + result.get(i).getDecode());
        }
        else {
          ErrorCount1++;
          System.out.println("Round: \t" + result.get(i).getRoundIndex() + "\t PulseIndex: \t" + result.get(i).getPulseIndex()
                  + "\t Encode: \t" + result.get(i).getEncode() + "\t Decode: \t" + result.get(i).getDecode() + "\t Error Code!");
        }
      }
      else if (result.get(i).getEncode() == 1) {
        if (result.get(i).getDecode() == 1) {
          CodeCount1++;
          System.out.println("Round: \t" + result.get(i).getRoundIndex() + "\t PulseIndex: \t" + result.get(i).getPulseIndex()
                  + "\t Encode: \t" + result.get(i).getEncode() + "\t Decode: \t" + result.get(i).getDecode());
        }
        else {
          ErrorCount0++;
          System.out.println("Round: \t" + result.get(i).getRoundIndex() + "\t PulseIndex: \t" + result.get(i).getPulseIndex()
                  + "\t Encode: \t" + result.get(i).getEncode() + "\t Decode: \t" + result.get(i).getDecode() + "\t Error Code!");
        }
      }
    }
    APD1Ratio = CodeCount0 / ErrorCount1;
    APD2Ratio = CodeCount1 / ErrorCount0;
    RightCodeCount = CodeCount0 + CodeCount1;
    ErrorCount = ErrorCount0 + ErrorCount1;
    Ratio = (float) RightCodeCount / (float) ErrorCount;
    System.out.println("APD1 Code 0:\t" + CodeCount0 + "\t Error Code 1:\t" + ErrorCount1 + "\t APD1 Ratio:\t" + APD1Ratio);
    System.out.println("APD2 Code 1:\t" + CodeCount1 + "\t Error Code 0:\t" + ErrorCount0 + "\t APD2 Ratio:\t" + APD2Ratio);
    System.out.println("Total Right Code:\t" + RightCodeCount + "\t Total Error Code :\t" + ErrorCount + "\t Ratio:\t" + Ratio);

  }

}
