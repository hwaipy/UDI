package com.hwaipy.unifieddeviceinterface.timeeventdevice.io;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;

/**
 *
 * @author Hwaipy
 */
public class TimeEventSerializer {

    //TODO Channel可能超过8个，因此对Storage的处理需要改进
    public long serialize(TimeEvent timeEvent) {
        long s = timeEvent.getTime();
        long c = timeEvent.getChannel();
        if (s == Long.MAX_VALUE) {
            throw new RuntimeException();
//            return 24 | c;
        } else if (s == Long.MIN_VALUE) {
            throw new RuntimeException();
//            return 8 | c;
        } else {
            s <<= 4;
            return s | c;
        }
    }

    public TimeEvent deserialize(long data) {
        int channel = (int) data & 7;
        long time = data & 0xFFFFFFFFFFFFFFF8L;
        if (time == 24) {
            time = Long.MAX_VALUE;
        } else if (time == 8) {
            time = Long.MIN_VALUE;
        } else {
            time >>= 4;
        }
        return new TimeEvent(time, channel);
    }

    public static void main(String[] args) {
        System.out.println(24 | 1);
    }
}
