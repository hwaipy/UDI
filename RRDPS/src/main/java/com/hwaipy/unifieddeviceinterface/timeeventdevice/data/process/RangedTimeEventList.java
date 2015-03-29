package com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.Iterator;

/**
 *
 * @author Hwaipy
 */
public class RangedTimeEventList implements TimeEventList {

    private final TimeEventList originalList;
    private final int offset;
    private final int size;

    public RangedTimeEventList(TimeEventList originalList, long start, long end) {
        this.originalList = originalList;
        int os = 0;
        int sz = 0;
        Iterator<TimeEvent> iterator = originalList.iterator();
        while (iterator.hasNext()) {
            long time = iterator.next().getTime();
            if (time < start) {
                os++;
            } else if (time < end) {
                sz++;
            }
        }
        offset = os;
        size = sz;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public TimeEvent get(int index) {
        return originalList.get(index + offset);
    }

    @Override
    public void set(TimeEvent event, int index) {
        originalList.set(event, index + offset);
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
                TimeEvent e = get(index);
                index++;
                return e;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
