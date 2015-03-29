package com.hwaipy.rrdps;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.Iterator;

/**
 *
 * @author Hwaipy
 */
public class MergedTimeEventList implements TimeEventList {

    private final TimeEventList list1;
    private final TimeEventList list2;

    public MergedTimeEventList(TimeEventList list1, TimeEventList list2) {
        this.list1 = list1;
        this.list2 = list2;
    }

    @Override
    public int size() {
        return list1.size() + list2.size();
    }

    @Override
    public TimeEvent get(int index) {
        if (index >= size()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        Iterator<TimeEvent> iterator = iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    @Override
    public void set(TimeEvent event, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<TimeEvent> iterator() {
        return new MergedIterator();
    }

    private class MergedIterator implements Iterator<TimeEvent> {

        private final Iterator<TimeEvent> iterator1 = list1.iterator();
        private final Iterator<TimeEvent> iterator2 = list2.iterator();
        private TimeEvent event1;
        private TimeEvent event2;

        private MergedIterator() {
            if (iterator1.hasNext()) {
                event1 = iterator1.next();
            }
            if (iterator2.hasNext()) {
                event2 = iterator2.next();
            }
        }

        @Override
        public boolean hasNext() {
            return (event1 != null) || (event2 != null);
        }

        @Override
        public TimeEvent next() {
            TimeEvent event;
            if (event1 == null && event2 == null) {
                throw new RuntimeException();
            } else if (event1 == null && event2 != null) {
                event = event2;
                event2 = null;
            } else if (event1 != null && event2 == null) {
                event = event1;
                event1 = null;
            } else if (event1.getTime() > event2.getTime()) {
                event = event2;
                event2 = null;
            } else {
                event = event1;
                event1 = null;
            }
            if (event1 == null && iterator1.hasNext()) {
                event1 = iterator1.next();
            }
            if (event2 == null && iterator2.hasNext()) {
                event2 = iterator2.next();
            }
            return event;
        }
    }
}
