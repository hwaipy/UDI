package com.hwaipy.unifieddeviceinterface.datadispatch;

import java.util.EventObject;

/**
 *
 * @author Hwaipy
 */
public class DataIncomeEvent<DATA_TYPE> extends EventObject {

    private final DATA_TYPE data;

    public DataIncomeEvent(Object source, DATA_TYPE data) {
        super(source);
        this.data = data;
    }

    public DATA_TYPE getData() {
        return data;
    }
}
