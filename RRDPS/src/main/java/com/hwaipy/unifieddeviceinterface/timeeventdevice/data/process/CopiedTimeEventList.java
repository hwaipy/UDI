package com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Hwaipy
 */
public class CopiedTimeEventList implements TimeEventList {

    private final ArrayList<TimeEvent> eventList;

    public CopiedTimeEventList(TimeEventList originalList) {
        eventList = new ArrayList<>(originalList.size());
        for (TimeEvent timeEvent : originalList) {
            eventList.add(timeEvent);
        }
    }

    @Override
    public int size() {
        return eventList.size();
    }

    @Override
    public TimeEvent get(int index) {
        return eventList.get(index);
    }

    @Override
    public void set(TimeEvent event, int index) {
        eventList.set(index, event);
    }

    @Override
    public Iterator<TimeEvent> iterator() {
        return eventList.iterator();
    }
}
