package com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Hwaipy
 */
public class RecursionCoincidenceMatcher implements Iterable<Coincidence> {

    private final TimeEventList list1;
    private final TimeEventList list2;
    private long gate;
    private long initDelay;
    private final ArrayList<Coincidence> coincidences = new ArrayList<>();
    private boolean find = false;
    private int coincidenceCount = 0;

    public RecursionCoincidenceMatcher(TimeEventList list1, TimeEventList list2, long gate, long initDelay) {
        this.list1 = list1;
        this.list2 = list2;
        this.gate = gate;
        this.initDelay = initDelay;
    }

    public RecursionCoincidenceMatcher(TimeEventList list1, TimeEventList list2) {
        this(list1, list2, 50, 0);
    }

    public long getGate() {
        return gate;
    }

    public void setGate(long gate) {
        this.gate = gate;
        find = false;
    }

    public long getInitDelay() {
        return initDelay;
    }

    public void setInitDelay(long initDelay) {
        this.initDelay = initDelay;
        find = false;
    }

    public int find() {
        long delay = initDelay;
        coincidences.clear();
        Iterator<TimeEvent> iterator1 = list1.iterator();
        Iterator<TimeEvent> iterator2 = list2.iterator();
        TimeEvent event1 = iterator1.hasNext() ? iterator1.next() : null;
        TimeEvent event2 = iterator2.hasNext() ? iterator2.next() : null;
        while (event1 != null && event2 != null) {
            long time1 = event1.getTime();
            long time2 = event2.getTime() - delay;
            if (time1 < time2 - gate) {
                event1 = iterator1.hasNext() ? iterator1.next() : null;
//                System.out.println("Jump 1");
//                System.out.println("C:\t" + (time2 - time1));
            } else if (time2 < time1 - gate) {
                event2 = iterator2.hasNext() ? iterator2.next() : null;
//                System.out.println("Jump 2");
//                System.out.println("C:\t" + (time1 - time2));
            } else {
                coincidences.add(new Coincidence(event1, event2, delay));
                delay = event2.getTime() - event1.getTime();
                event1 = iterator1.hasNext() ? iterator1.next() : null;
                event2 = iterator2.hasNext() ? iterator2.next() : null;
//                System.out.println("New delay " + delay);
            }
        }
        coincidenceCount = coincidences.size();
        find = true;
        return coincidenceCount;
    }

    public int getCoincidenceCount() {
        if (!find) {
            throw new RuntimeException();
        }
        return coincidenceCount;
    }

    @Override
    public Iterator<Coincidence> iterator() {
        if (!find) {
            throw new RuntimeException();
        }
        return coincidences.iterator();
    }

    public int size() {
        if (!find) {
            throw new RuntimeException();
        }
        return coincidences.size();
    }

    public Coincidence get(int index) {
        if (!find) {
            throw new RuntimeException();
        }
        return coincidences.get(index);
    }
}
