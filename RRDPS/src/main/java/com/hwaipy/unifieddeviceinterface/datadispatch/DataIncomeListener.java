package com.hwaipy.unifieddeviceinterface.datadispatch;

import java.util.EventListener;

/**
 *
 * @author Hwaipy
 */
public interface DataIncomeListener<DATA_TYPE> extends EventListener {

    public void dataIncome(DataIncomeEvent<DATA_TYPE> event);
}
