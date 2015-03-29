package com.hwaipy.unifieddeviceinterface.timeeventdevice.standardfile;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;

/**
 *
 * @author Hwaipy
 */
public class NaiveFormat {

    public static final long naiveFormat(TimeEvent timeEvent) {
        long time = timeEvent.getTime();
        int channel = timeEvent.getChannel();
        long value = (time << 16) + channel;
        return value;
    }
}
