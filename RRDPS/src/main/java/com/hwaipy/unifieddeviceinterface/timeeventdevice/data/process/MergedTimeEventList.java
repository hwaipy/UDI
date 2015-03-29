package com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set(TimeEvent event, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<TimeEvent> iterator() {

        return new Iterator<TimeEvent>() {
            private final Iterator<TimeEvent> iterator1 = list1.iterator();
            private final Iterator<TimeEvent> iterator2 = list2.iterator();
            private TimeEvent event1;
            private TimeEvent event2;
            private boolean inited = false;

            private void init() {
                fill();
                inited = true;
            }

            @Override
            public boolean hasNext() {
                if (!inited) {
                    init();
                }
                return event1 != null || event2 != null;
            }

            @Override
            public TimeEvent next() {
                if (!inited) {
                    init();
                }
                TimeEvent r;
                if (event1 == null && event2 == null) {
                    throw new NoSuchElementException();
                }
                if (event1 == null) {
                    r = event2;
                    event2 = null;
                } else if (event2 == null) {
                    r = event1;
                    event1 = null;
                } else {
                    if (event1.getTime() < event2.getTime()) {
                        r = event1;
                        event1 = null;
                    } else {
                        r = event2;
                        event2 = null;
                    }
                }
                fill();
                return r;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            private void fill() {
                if (event1 == null && iterator1.hasNext()) {
                    event1 = iterator1.next();
                }
                if (event2 == null && iterator2.hasNext()) {
                    event2 = iterator2.next();
                }
            }
        };
    }
}
