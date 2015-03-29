package com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process;

import com.hwaipy.mathematics.fitting.LineFitting;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Hwaipy
 */
public class TimeCalibrator {

    private final Iterable<Coincidence> mapping;
    private final TimeEventList list;

    public TimeCalibrator(Iterable<Coincidence> mapping, TimeEventList list) {
        this.mapping = mapping;
        this.list = list;
    }

    public void calibrate() {
        Iterator<Coincidence> iterator = mapping.iterator();
        boolean isError = false;
        Coincidence coincidenceStart = iterator.next();
        long timeStart1 = coincidenceStart.getEvent1().getTime();
        long timeStart2 = coincidenceStart.getEvent2().getTime();
        int index = 0;
        while (list.get(index).getTime() < timeStart2 && index < list.size()) {
            list.set(TimeEvent.ERROR_EVENT, index);
            index++;
        }
        while (iterator.hasNext() && index < list.size()) {
            Coincidence coincidenceEnd = iterator.next();
            long timeEnd1 = coincidenceEnd.getEvent1().getTime();
            long timeEnd2 = coincidenceEnd.getEvent2().getTime();
            isError = (timeEnd1 - timeStart1) < 10000000;
            while (index < list.size()) {
                TimeEvent event = list.get(index);
                long time = event.getTime();
                if (time < timeEnd2) {
                    time = (long) (((double) (time - timeStart2)) / (timeEnd2 - timeStart2) * (timeEnd1 - timeStart1) + timeStart1);
                    if (isError) {
                        list.set(TimeEvent.ERROR_EVENT, index);
                    } else {
                        list.set(new TimeEvent(time, event.getChannel()), index);
                    }
                    index++;
                    if (index >= list.size()) {
                        break;
                    }
                } else {
                    break;
                }
            }
            timeStart1 = timeEnd1;
            timeStart2 = timeEnd2;
        }
        while (index < list.size()) {
            list.set(TimeEvent.ERROR_EVENT, index);
            index++;
        }
    }

    public void calibrateOverall() {
        Iterator<Coincidence> iterator = mapping.iterator();
        Coincidence startCoincidence = iterator.next();
        Coincidence endCoincidence = startCoincidence;
        while (iterator.hasNext()) {
            endCoincidence = iterator.next();
        }
        long timeStart1 = startCoincidence.getEvent1().getTime();
        long timeStart2 = startCoincidence.getEvent2().getTime();
        long timeEnd1 = endCoincidence.getEvent1().getTime();
        long timeEnd2 = endCoincidence.getEvent2().getTime();
        System.out.println(timeStart1);
        System.out.println(timeStart2);
        System.out.println(timeEnd1);
        System.out.println(timeEnd2);
        int index = 0;
        while (index < list.size() && list.get(index).getTime() < timeStart2) {
            list.set(TimeEvent.ERROR_EVENT, index);
            index++;
        }
        while (index < list.size()) {
            TimeEvent event = list.get(index);
            long time = event.getTime();
            if (time < timeEnd2) {
                time = (long) (((double) (time - timeStart2)) / (timeEnd2 - timeStart2) * (timeEnd1 - timeStart1) + timeStart1);
                list.set(new TimeEvent(time, event.getChannel()), index);
                index++;
                if (index >= list.size()) {
                    break;
                }
            } else {
                break;
            }
        }
        while (index < list.size()) {
            list.set(TimeEvent.ERROR_EVENT, index);
            index++;
        }
    }

    public void calibrateByLineFitting() {
        System.out.println("Hello calibration");
        LinkedList<Coincidence> fittingList = new LinkedList<>();
        Iterator<Coincidence> mappingIterator = mapping.iterator();
        while (mappingIterator.hasNext() && fittingList.size() < 10) {
            fittingList.addLast(mappingIterator.next());
        }
        if (fittingList.size() < 10) {
            throw new RuntimeException();
        }

        int index = 0;
        boolean isError = false;
        while (true) {
            System.out.println("Calibration loop begins. Here shows the fitting list");
            for (Coincidence coincidence : fittingList) {
                System.out.println("----\t" + coincidence.getEvent1().getTime() + "\t" + coincidence.getEvent2().getTime());
            }
            Coincidence coincidenceStart = fittingList.get(4);
            Coincidence coincidenceEnd = fittingList.get(5);
            long timeStart1 = coincidenceStart.getEvent1().getTime();
            long timeStart2 = coincidenceStart.getEvent2().getTime();
            long timeEnd1 = coincidenceEnd.getEvent1().getTime();
            long timeEnd2 = coincidenceEnd.getEvent2().getTime();
            System.out.println("Start Time 1 = " + timeStart1);
            System.out.println("Start Time 2 = " + timeStart2);
            System.out.println("End Time 1 = " + timeEnd1);
            System.out.println("End Time 1 = " + timeEnd2);
            while (index < list.size() && list.get(index).getTime() < timeStart2) {
                list.set(TimeEvent.ERROR_EVENT, index);
                index++;
            }
            double[] dataX = new double[10];
            double[] dataY = new double[10];
            for (int i = 0; i < 10; i++) {
                dataX[i] = fittingList.get(i).getEvent2().getTime();
                dataY[i] = fittingList.get(i).getEvent1().getTime();
            }
            LineFitting lineFitting = new LineFitting(dataX, dataY);
            double slope = lineFitting.getSlope();
            double intercept = lineFitting.getIntercept();
            System.out.println("Slope = " + slope);
            System.out.println("Intercept = " + intercept);
            isError = (timeEnd1 - timeStart1) < 10000000;
            if (isError) {
                System.out.println("isError");
            }
            while (index < list.size()) {
                TimeEvent event = list.get(index);
                long time = event.getTime();
                if (time < timeEnd2) {
                    System.out.println("An event need to be calibrated. Time = " + time);
                    time = (long) (time * slope + intercept);
                    System.out.println("New time is " + time);
//                    if (isError) {
//                        list.set(TimeEvent.ERROR_EVENT, index);
//                    } else {
                    list.set(new TimeEvent(time, event.getChannel()), index);
//                    }
                    index++;
                    if (index >= list.size()) {
                        break;
                    }
                } else {
                    break;
                }
            }
            if (mappingIterator.hasNext()) {
                fittingList.removeFirst();
                fittingList.addLast(mappingIterator.next());
            } else {
                break;
            }
        }
        while (index < list.size()) {
            list.set(TimeEvent.ERROR_EVENT, index);
            index++;
        }
    }

    private class BufferedIterator {

        private final Iterator<TimeEvent> iterator;
        private TimeEvent event;

        public BufferedIterator(Iterator<TimeEvent> iterator) {
            this.iterator = iterator;
            if (iterator.hasNext()) {
                event = iterator.next();
            } else {
                event = null;
            }
        }

        public boolean hasNext() {
            return event != null;
        }

        public TimeEvent get() {
            return event;
        }

        public TimeEvent next() {
            if (iterator.hasNext()) {
                event = iterator.next();
            } else {
                event = null;
            }
            return event;
        }
    }

    public static void calibrate(Iterable<Coincidence> mapping, TimeEventList list) {
        new TimeCalibrator(mapping, list).calibrate();
    }

    public static void calibrateOverall(Iterable<Coincidence> mapping, TimeEventList list) {
        new TimeCalibrator(mapping, list).calibrateOverall();
    }
}
