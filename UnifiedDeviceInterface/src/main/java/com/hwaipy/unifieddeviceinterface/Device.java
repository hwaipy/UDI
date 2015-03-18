package com.hwaipy.unifieddeviceinterface;

/**
 *
 * @author HwaipyLab
 */
public interface Device {

    public void open() throws DeviceException;

    public void close() throws DeviceException;
}
