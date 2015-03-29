package com.hwaipy.rrdps;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer.TimeEventList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Hwaipy
 */
public class Tagger {

    private final TimeEventList syncList;
    private final TimeEventList apdList;
    private final long gate;

    public Tagger(TimeEventList syncList, TimeEventList apdList, long gate) {
        this.syncList = syncList;
        this.apdList = apdList;
        this.gate = gate;
    }

    public ArrayList<Entry> tag() {
        ArrayList<Entry> result = new ArrayList<>();
        Iterator<TimeEvent> syncIterator = syncList.iterator();
        Iterator<TimeEvent> apdIterator = apdList.iterator();
        TimeEvent syncEvent = syncIterator.next();
        long syncTime = syncEvent.getTime();
        int roundIndex = 0;
        System.out.println(apdList.size());
        TimeEvent event = apdIterator.next();
        while (true) {
            if ((syncEvent == null) && syncIterator.hasNext()) {
                syncEvent = syncIterator.next();
                syncTime = syncEvent.getTime();
                roundIndex++;
            }
            if ((event == null) && apdIterator.hasNext()) {
                event = apdIterator.next();
            }
            if (syncEvent == null || event == null) {
                break;
            }
            long time = event.getTime();
            if (time < syncTime -1000) {
                event = null;
            } else if (time > syncTime + 256000) {
                syncEvent = null;
            } else {
                DecodingRandom random = ((ExtandedTimeEvent<DecodingRandom>) syncEvent).getProperty();
                long[] tagResult = doTag(event.getTime(), syncTime, random.getDelay1());
                int pulseIndex = (int)tagResult[0];
                long apdTime = tagResult[1];
                if (pulseIndex >= 0) {
                    result.add(new Entry(roundIndex, pulseIndex, event.getChannel() - 2,apdTime));
                }
                event = null;
            }
        }
        return result;
    }

    private long [] doTag(long apdTime, long syncTime, int delayPulse) {
        long time = apdTime - delayPulse * 2000;
        int deltaTime = (int) (time - syncTime);
        long tagResult[]={-1,0};
//        int pulseIndex = -1;
        int index = deltaTime / 2000;
        if ((deltaTime <= index * 2000 + gate / 2) && (deltaTime >= index * 2000 - gate / 2)) {
            tagResult[0] = index;
            tagResult[1]=apdTime;
        } else if ((deltaTime >= (index + 1) * 2000 - gate / 2) && (deltaTime <= (index + 1) * 2000 + gate / 2)) {
            tagResult[0] = index + 1;
            tagResult[1]=apdTime;
        }
        if (tagResult[0]> 127) {
            tagResult[0]= -1;
        }
        return tagResult;
    }

    public class Entry {

        private final int roundIndex;
        private final int pulseIndex;
        private final int code;
        private final long apdTime;

        private Entry(int roundIndex, int pulseIndex, int code, long apdTime) {
            this.roundIndex = roundIndex;
            this.pulseIndex = pulseIndex;
            this.code = code;
            this.apdTime=apdTime;
        }

        public int getRoundIndex() {
            return roundIndex;
        }

        public int getPulseIndex() {
            return pulseIndex;
        }

        public int getCode() {
            return code;
        }

        /**
         * @return the apdTime
         */
        public long getApdTime() {
            return apdTime;
        }
    }
}
