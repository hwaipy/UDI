package com.hwaipy.rrdps;

import com.hwaipy.mathematics.fitting.LineFitting;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process.Coincidence;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process.RecursionCoincidenceMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Hwaipy
 */
public class Calibrator {

    private final RecursionCoincidenceMatcher cmSync;
    private final RecursionCoincidenceMatcher cmSignal;

    public Calibrator(RecursionCoincidenceMatcher cmSync, RecursionCoincidenceMatcher cmSignal) {
        this.cmSync = cmSync;
        this.cmSignal = cmSignal;
    }

    public Collection<Coincidence> calibrate() {
        ArrayList<Coincidence> calibratedList = new ArrayList<>();
        Iterator<Coincidence> syncIterator = cmSync.iterator();
        Coincidence coincidenceStart = syncIterator.next();
        long timeStart1 = coincidenceStart.getEvent1().getTime();
        long timeStart2 = coincidenceStart.getEvent2().getTime();
        int index = 0;
        while (cmSignal.get(index).getEvent2().getTime() < timeStart2 && index < cmSignal.size()) {
            index++;
        }
        while (syncIterator.hasNext() && index < cmSignal.size()) {
            Coincidence coincidenceEnd = syncIterator.next();
            long timeEnd1 = coincidenceEnd.getEvent1().getTime();
            long timeEnd2 = coincidenceEnd.getEvent2().getTime();
            while (index < cmSignal.size()) {
                TimeEvent event = cmSignal.get(index).getEvent2();
                long time = event.getTime();
                if (time < timeEnd2) {
                    time = (long) (((double) (time - timeStart2)) / (timeEnd2 - timeStart2) * (timeEnd1 - timeStart1) + timeStart1);
                    TimeEvent calibratedEvent = new TimeEvent(time, event.getChannel());
                    Coincidence calibratedCoincidence = new Coincidence(cmSignal.get(index).getEvent1(), calibratedEvent, 0);
                    calibratedList.add(calibratedCoincidence);
                    index++;
                    if (index >= cmSignal.size()) {
                        break;
                    }
                } else {
                    break;
                }
            }
            timeStart1 = timeEnd1;
            timeStart2 = timeEnd2;
        }
        return calibratedList;
    }

    public Collection<Coincidence> calibrateByLineFitting(int s) {
        System.out.println("Calibrating by LineFitting.");
        int size = 2 * s;
        int startIndex = s - 1;
        int endIndex = s;
        ArrayList<Coincidence> calibratedList = new ArrayList<>();
        LinkedList<Coincidence> fittingList = new LinkedList<>();
        Iterator<Coincidence> syncIterator = cmSync.iterator();
        while (syncIterator.hasNext() && fittingList.size() < size) {
            fittingList.addLast(syncIterator.next());
        }
        if (fittingList.size() < size) {
            throw new RuntimeException();
        }
        int index = 0;
        while (true) {
//            System.out.println("Calibration loop begins. Here shows the fitting list");
//            for (Coincidence coincidence : fittingList) {
//                System.out.println("----\t" + coincidence.getEvent1().getTime() + "\t" + coincidence.getEvent2().getTime());
//            }
            Coincidence coincidenceStart = fittingList.get(startIndex);
            Coincidence coincidenceEnd = fittingList.get(endIndex);
            long timeStart1 = coincidenceStart.getEvent1().getTime();
            long timeStart2 = coincidenceStart.getEvent2().getTime();
            long timeEnd1 = coincidenceEnd.getEvent1().getTime();
            long timeEnd2 = coincidenceEnd.getEvent2().getTime();
//            System.out.println("Start Time 1 = " + timeStart1);
//            System.out.println("Start Time 2 = " + timeStart2);
//            System.out.println("End Time 1 = " + timeEnd1);
//            System.out.println("End Time 1 = " + timeEnd2);
            while (index < cmSignal.size() && cmSignal.get(index).getEvent2().getTime() < timeStart2) {
                index++;
            }
            double[] dataX = new double[size];
            double[] dataY = new double[size];
            for (int i = 0; i < size; i++) {
                dataX[i] = fittingList.get(i).getEvent2().getTime() - timeStart2;
                dataY[i] = fittingList.get(i).getEvent1().getTime() - timeStart1;
            }
            LineFitting lineFitting = new LineFitting(dataX, dataY);
            double slope = lineFitting.getSlope();
            double intercept = lineFitting.getIntercept();
//            System.out.println("Slope = " + slope);
//            System.out.println("Intercept = " + intercept);
            while (index < cmSignal.size()) {
                TimeEvent event = cmSignal.get(index).getEvent2();
                long time = event.getTime();
                if (time < timeEnd2) {
//                    System.out.println("An event need to be calibrated. Time = " + time);
                    long calibratedTime = (long) ((time - timeStart2) * slope + intercept + timeStart1);
//                    System.out.println("New time is " + time);
                    TimeEvent calibratedEvent = new TimeEvent(calibratedTime, event.getChannel());
                    Coincidence calibratedCoincidence = new Coincidence(cmSignal.get(index).getEvent1(), calibratedEvent, 0);
                    calibratedList.add(calibratedCoincidence);
                    if (calibratedCoincidence.getAbsTimeDefferent() > 10000) {
                        System.out.println("---------- Here is a bad event. ----------");
                        System.out.println("Fitting list");
                        for (int i = 0; i < size; i++) {
                            System.out.println("----\t" + dataX[i] + "\t" + dataY[i]);
                        }
                        System.out.println("Start Time 1 = " + timeStart1);
                        System.out.println("Start Time 2 = " + timeStart2);
                        System.out.println("End Time 1 = " + timeEnd1);
                        System.out.println("End Time 1 = " + timeEnd2);
                        System.out.println("Slope = " + slope);
                        System.out.println("Intercept = " + intercept);
                        System.out.println("The event need to be calibrated: " + time);
                        System.out.println("New time is " + calibratedTime);
                        System.out.println("Related time is " + cmSignal.get(index).getEvent1());
                        System.out.println("---------- Ended. ----------");
                        System.exit(10086);
                    }
                    index++;
                    if (index >= cmSignal.size()) {
                        break;
                    }
                } else {
                    break;
                }
            }
            if (syncIterator.hasNext()) {
                fittingList.removeFirst();
                fittingList.addLast(syncIterator.next());
            } else {
                break;
            }
        }
        return calibratedList;
    }
}
