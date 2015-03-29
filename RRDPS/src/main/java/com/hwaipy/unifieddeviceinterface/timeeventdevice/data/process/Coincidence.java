package com.hwaipy.unifieddeviceinterface.timeeventdevice.data.process;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;

/**
 *
 * @author Hwaipy
 */
public class Coincidence {

    private final TimeEvent event1;
    private final TimeEvent event2;
    private final long delay;

    public Coincidence(TimeEvent event1, TimeEvent event2, long delay) {
        this.event1 = event1;
        this.event2 = event2;
        this.delay = delay;
    }

    public long getTimeDifferent() {
        return event1.getTime() - event2.getTime() + delay;
    }

    public long getAbsTimeDefferent() {
        long td = getTimeDifferent();
        return td > 0 ? td : -td;
    }

    public TimeEvent getEvent1() {
        return event1;
    }

    public TimeEvent getEvent2() {
        return event2;
    }

    public long getDelay() {
        return delay;
    }
}
