package com.hwaipy.unifieddeviceinterface.timeeventdevice.timeeventcontainer;

import com.hwaipy.unifieddeviceinterface.timeeventdevice.TimeEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Hwaipy Lab
 */
public class StreamTimeEventList implements TimeEventList {

  private final ArrayList<TimeEvent> list = new ArrayList<>();
  private TimeEvent lastEvent = null;

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public TimeEvent get(int index) {
    return list.get(index);
  }

  @Override
  public void set(TimeEvent event, int index) {
    list.set(index, event);
  }

  @Override
  public Iterator<TimeEvent> iterator() {
    return list.listIterator();
  }

  public void offer(TimeEvent event) {
    if (lastEvent != null) {
      if (lastEvent.getChannel() != event.getChannel()) {
        throw new IllegalArgumentException("Channel not match");
      }
      if (lastEvent.getTime() > event.getTime()) {
        //TODO Add a Warning here
        System.out.println("A Time Reverse.");
        return;
      }
    }
    list.add(event);
    lastEvent = event;
  }

  public void clear() {
    list.clear();
  }

}
