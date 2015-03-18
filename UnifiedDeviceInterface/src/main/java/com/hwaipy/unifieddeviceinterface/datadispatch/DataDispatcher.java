package com.hwaipy.unifieddeviceinterface.datadispatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Hwaipy
 */
public class DataDispatcher<DATA_TYPE> {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread("Data dispatch Thraed");
            currentDataDispatchThread = thread;
            return thread;
        }
    });
    private static Thread currentDataDispatchThread;
    private final EventListenerList eventListenerList = new EventListenerList();

    public void addDataIncomeListener(DataIncomeListener<DATA_TYPE> listener) {
        eventListenerList.add(DataIncomeListener.class, listener);
    }

    public void removeDataIncomeListener(DataIncomeListener<DATA_TYPE> listener) {
        eventListenerList.remove(DataIncomeListener.class, listener);
    }

    /**
     *
     * @param data 此函数是异步的，因此不要复用data对象
     */
    public void fireDataIncomeEvents(final DATA_TYPE data) {
        if (Thread.currentThread() == currentDataDispatchThread) {
            doFireDataIncomeEvents(data);
        } else {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    doFireDataIncomeEvents(data);
                }
            });
        }
    }

    private void doFireDataIncomeEvents(DATA_TYPE data) {
        DataIncomeEvent<DATA_TYPE> event = new DataIncomeEvent<>(this, data);
        for (DataIncomeListener<DATA_TYPE> listener : eventListenerList.getListeners(DataIncomeListener.class)) {
            listener.dataIncome(event);
        }
    }
}
