package com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.Iterator;

/**
 *
 * @author Hwaipy
 */
public class StandardPeriodTimeEventList implements TimeEventList {

    private final long start;
    private final long period;
    private final int size;
    private final int channel;

    public StandardPeriodTimeEventList(long start, long period, int size, int channel) {
        this.start = start;
        this.period = period;
        this.size = size;
        this.channel = channel;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public TimeEvent get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return new TimeEvent(start + index * period, channel);
    }

    @Override
    public void set(TimeEvent event, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<TimeEvent> iterator() {
        return new Iterator<TimeEvent>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public TimeEvent next() {
                TimeEvent event = get(index);
                index++;
                return event;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
}
