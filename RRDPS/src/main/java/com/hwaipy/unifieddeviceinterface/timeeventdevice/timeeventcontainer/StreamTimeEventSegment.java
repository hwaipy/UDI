package com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;

/**
 *
 * @author Hwaipy Lab
 */
public class StreamTimeEventSegment extends DefaultTimeEventSegment {

    private StreamTimeEventSegment(StreamTimeEventList[] timeEventLists) {
        super(timeEventLists);
    }

    public void offer(TimeEvent timeEvent) {
        int channel = timeEvent.getChannel();
        ((StreamTimeEventList) timeEventLists[channel]).offer(timeEvent);
    }

    public void clear() {
        for (TimeEventList timeEventList : timeEventLists) {
            ((StreamTimeEventList) timeEventList).clear();
        }
    }

    public static StreamTimeEventSegment newStreamTimeEventSegment(int channelCount) {
        StreamTimeEventList[] timeEventLists = new StreamTimeEventList[channelCount];
        for (int i = 0; i < channelCount; i++) {
            timeEventLists[i] = new StreamTimeEventList();
        }
        return new StreamTimeEventSegment(timeEventLists);
    }
}
