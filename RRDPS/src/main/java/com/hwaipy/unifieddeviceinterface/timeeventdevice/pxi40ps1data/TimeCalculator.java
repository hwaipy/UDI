package com.hwaipy.unifieddeviceinterface.timeeventdevice.pxi40ps1data;

import com.hwaipy.unifieddeviceinterface.DeviceException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author Hwaipy
 */
class TimeCalculator {

    private static final long COARSE_TIME_LIMIT = 1 << 28;
    private long[] carry ={0,0,0,0,0,0,0,0};
    private long[] lastCoarseTime ={0,0,0,0,0,0,0,0};
    private long[] coarseTime ={0,0,0,0,0,0,0,0};
    private final FineTimeCalibrator calibrator;

    
    public TimeCalculator(FineTimeCalibrator calibrator) {
        this.calibrator = calibrator;
    }

    long calculate(ByteBuffer buffer, int channel) throws DeviceException {
        long[] b = new long[8];
        for (int i = 0; i < 8; i++) {
            b[i] = buffer.get(i);
            if (b[i] < 0) {
                b[i] += 256;
            }
        }
        long fineTime = ((b[3] & 0x10) << 4) | b[7];
         coarseTime[channel] = (b[4]) | (b[5] << 8)
                | (b[2] << 16) | ((b[3] & 0x0F) << 24);
         if (coarseTime[channel] < lastCoarseTime[channel]) {
//        if (coarseTime[channel] < lastCoarseTime[channel] && (lastCoarseTime [channel]> 100000000) && (coarseTime[channel] < 161000000)) {//2015-1-26基于DPS实验修改
            carry[channel]++;
        }
        lastCoarseTime[channel] = coarseTime[channel];
        long time = -getExactTime(fineTime, channel)
                + ((coarseTime[channel] + (carry[channel] << 28)) * 6250);
//        if (channel==0) {
//            System.out.println(coarseTime[channel]);
//        }
        return time;
    }

    private long getExactTime(long exactTime, int channel) {
        return calibrator.calibration(channel, (int) exactTime);
    }
}
