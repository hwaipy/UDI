//package com.hwaipy.rrdps;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Iterator;
//
///**
// *
// * @author Hwaipy
// */
//public class FinalResultParser {
//
//    private final ArrayList<Decoder.Entry> result;
//    private final PhaseLockingResultSet phaseLockingResultSet;
//    private final ArrayList<Result> resultSet = new ArrayList<>();
//
//    public FinalResultParser(ArrayList<Decoder.Entry> result, PhaseLockingResultSet phaseLockingResultSet) {
//        this.result = result;
//        this.phaseLockingResultSet = phaseLockingResultSet;
//        parse();
//    }
//
//    private void parse() {
//        Iterator<Decoder.Entry> iterator = result.iterator();
//        while (iterator.hasNext()) {
//            Decoder.Entry decodeEntry = iterator.next();
//            int roundIndex = decodeEntry.getRoundIndex();
//            int pulseIndex = decodeEntry.getPulseIndex();
//            int encode = decodeEntry.getEncode();
//            int decode = decodeEntry.getDecode();
//            int random = decodeEntry.getDecodingRandom().getRandom();
//            double phaseLockingContrast = phaseLockingResultSet.get(roundIndex).contrast();
//            if (pulseIndex >= 64) {
//                continue;
//            }
//            resultSet.add(new Result(roundIndex, pulseIndex, encode, decode, random, phaseLockingContrast));
//        }
//    }
//
//    public void output(File file) throws FileNotFoundException {
//        try (PrintWriter printWriter = new PrintWriter(file)) {
//            resultSet.stream().forEach((result) -> {
//                printWriter.println(result);
//            });
//        }
//    }
//
//    static class Result {
//
//        private final int roundIndex;
//        private final int pulseIndex;
//        private final int encode;
//        private final int decode;
//        private final int random;
//        private final double phaseLockingContrast;
//
//        private Result(int roundIndex, int pulseIndex, int encode, int decode, int random, double phaseLockingContrast) {
//            this.roundIndex = roundIndex;
//            this.pulseIndex = pulseIndex;
//            this.encode = encode;
//            this.decode = decode;
//            this.random = random;
//            this.phaseLockingContrast = phaseLockingContrast;
//        }
//
//        public int getRoundIndex() {
//            return roundIndex;
//        }
//
//        public int getPulseIndex() {
//            return pulseIndex;
//        }
//
//        public int getEncode() {
//            return encode;
//        }
//
//        public int getDecode() {
//            return decode;
//        }
//
//        public int getRandom() {
//            return random;
//        }
//
//        public double getPhaseLockingContrast() {
//            return phaseLockingContrast;
//        }
//
//        @Override
//        public String toString() {
//            return roundIndex + "," + pulseIndex + "," + encode + "," + decode + "," + random + "," + phaseLockingContrast;
//        }
//
//        public static Result parse(String line) {
//            String[] splits = line.split(" *, *");
//            int roundIndex = Integer.parseInt(splits[0]);
//            int pulseIndex = Integer.parseInt(splits[1]);
//            int encode = Integer.parseInt(splits[2]);
//            int decode = Integer.parseInt(splits[3]);
//            int random = Integer.parseInt(splits[4]);
//            double phaseLockingContrast = Double.parseDouble(splits[4]);
//            return new Result(roundIndex, pulseIndex, encode, decode, random, phaseLockingContrast);
//        }
//    }
//}
