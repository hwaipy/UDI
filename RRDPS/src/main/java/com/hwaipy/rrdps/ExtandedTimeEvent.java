/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.rrdps;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;

/**
 *
 * @author Hwaipy
 * @param <T>
 */
public class ExtandedTimeEvent<T> extends TimeEvent {

    private final T random;

    public ExtandedTimeEvent(long time, int channel, T t) {
        super(time, channel);
        this.random = t;
    }

    public T getProperty() {
        return random;
    }
}
