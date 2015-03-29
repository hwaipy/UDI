package com.hwaipy.unifieddeviceinterface.timeeventdevice;

import com.hwaipy.unifieddeviceinterface.datadispatch.DataDispatcher;
import com.hwaipy.unifieddeviceinterface.datadispatch.DataIncomeEvent;
import com.hwaipy.unifieddeviceinterface.datadispatch.DataIncomeListener;
import java.nio.ByteBuffer;
import java.util.Collection;

/**
 *
 * @author HwaipyLab
 */
public abstract class AbstractTimeEventStreamComponent implements TimeEventStreamComponent {

    private final TimeEventDevice device;
    private final DataIncomeListener<ByteBuffer> listener = new DataIncomeListener<ByteBuffer>() {
        @Override
        public void dataIncome(DataIncomeEvent<ByteBuffer> event) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    private final DataDispatcher<Collection<TimeEvent>> dataDispatcher = new DataDispatcher<>();

    public AbstractTimeEventStreamComponent(TimeEventDevice device) {
        this.device = device;
    }

    @Override
    public void initialization() {
        device.addDataIncomeListener(listener);
    }

    @Override
    public void uninitialization() {
        device.removeDataIncomeListener(listener);
    }

    @Override
    public void addDataIncomeListener(DataIncomeListener<Collection<TimeEvent>> listener) {
        dataDispatcher.addDataIncomeListener(listener);
    }

    @Override
    public void removeDataIncomeListener(DataIncomeListener<Collection<TimeEvent>> listener) {
        dataDispatcher.removeDataIncomeListener(listener);
    }
}
