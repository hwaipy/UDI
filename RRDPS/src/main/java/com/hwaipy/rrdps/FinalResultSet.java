//package com.hwaipy.rrdps;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//
///**
// *
// * @author Hwaipy
// */
//public class FinalResultSet {
//
//    private final File path;
//    private final ArrayList<FinalResult> finalResults = new ArrayList<>();
//    private final ArrayList<FinalResultParser.Result> resultList = new ArrayList<>();
//    private final int[] encode0 = new int[64];
//    private final int[] encode1 = new int[64];
//    private final int[] encode0Correct = new int[64];
//    private final int[] encode1Correct = new int[64];
//
//    public FinalResultSet(File path) throws IOException {
//        this.path = path;
//        load();
//        parse();
//    }
//
//    private void load() throws IOException {
//        File[] files = path.listFiles((File dir, String name) -> {
//            return name.toLowerCase().endsWith("result.csv");
//        });
//        for (File file : files) {
//            finalResults.add(new FinalResult(file));
//        }
//        finalResults.stream().forEach((finalResult) -> {
//            resultList.addAll(finalResult.results);
//        });
//    }
//
//    private void parse() {
//        resultList.stream().forEach((result) -> {
//            int encode = result.getEncode();
//            int decode = result.getDecode();
//            int random = result.getRandom();
//            if (encode == 0) {
//                encode0[random]++;
//                if (decode == 0) {
//                    encode0Correct[random]++;
//                }
//            } else {
//                encode1[random]++;
//                if (decode != 0) {
//                    encode1Correct[random]++;
//                }
//            }
//        });
//    }
//
//    public int getEncodeCount(int encode) {
//        return encode == 0 ? sum(encode0) : sum(encode1);
//    }
//
//    public int getEncodeCount(int encode, int random) {
//        return encode == 0 ? encode0[random] : encode1[random];
//    }
//
//    public double getErrorRate(int encode) {
//        if (encode == 0) {
//            int total = sum(encode0);
//            int correct = sum(encode0Correct);
//            return (total - correct) / 1. / total;
//        } else {
//            int total = sum(encode1);
//            int correct = sum(encode1Correct);
//            return (total - correct) / 1. / total;
//        }
//    }
//
//    public double getErrorRate(int encode, int random) {
//        if (encode == 0) {
//            int total = encode0[random];
//            int correct = encode0Correct[random];
//            return (total - correct) / 1. / total;
//        } else {
//            int total = encode1[random];
//            int correct = encode1Correct[random];
//            return (total - correct) / 1. / total;
//        }
//    }
//
//    public int getRoundCount() {
//        int count = 0;
//        return finalResults.stream().map((finalResult) -> finalResult.getRoundCount()).map((roundCount) -> roundCount).reduce(count, Integer::sum);
//    }
//
//    public int getSiftedKeyCount() {
//        int count = 0;
//        return finalResults.stream().map((finalResult) -> finalResult.getRowKeyCount()).map((roundCount) -> roundCount).reduce(count, Integer::sum);
//    }
//
//    private int sum(int[] array) {
//        int sum = 0;
//        for (int i : array) {
//            sum += i;
//        }
//        return sum;
//    }
//
//    static class FinalResult {
//
//        private final File file;
//        private final ArrayList<FinalResultParser.Result> results = new ArrayList<>();
//
//        public FinalResult(File file) throws IOException {
//            this.file = file;
//            load();
//        }
//
//        private void load() throws IOException {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//            while (true) {
//                String line = reader.readLine();
//                if (line == null) {
//                    break;
//                }
//                results.add(FinalResultParser.Result.parse(line));
//            }
//        }
//
//        public int getRoundCount() {
//            return results.get(results.size() - 1).getRoundIndex();
//        }
//
//        public int getRowKeyCount() {
//            return results.size();
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//        File path = new File("/Users/Hwaipy/Desktop/RRDPS/结果/");
//        FinalResultSet finalResultSet = new FinalResultSet(path);
//        System.out.println("Round count: " + finalResultSet.getRoundCount());
//        System.out.println("Sifted key count: " + finalResultSet.getSiftedKeyCount());
//        System.out.println("Encode 0: " + finalResultSet.getEncodeCount(0) + ", with error rate " + finalResultSet.getErrorRate(0));
//        System.out.println("Encode 1: " + finalResultSet.getEncodeCount(1) + ", with error rate " + finalResultSet.getErrorRate(1));
//        System.out.println("----------------------------------------");
//        System.out.println("r\tc0\ter0\tc1\ter1");
//        for (int i = 0; i < 64; i++) {
//            System.out.println(i + "\t" + finalResultSet.getEncodeCount(0, i) + "\t" + finalResultSet.getErrorRate(0, i) + "\t" + finalResultSet.getEncodeCount(1, i) + "\t" + finalResultSet.getErrorRate(1, i));
//        }
//    }
//}
