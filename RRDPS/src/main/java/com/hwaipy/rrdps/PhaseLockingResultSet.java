package com.hwaipy.rrdps;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Hwaipy
 */
public class PhaseLockingResultSet implements Iterable<PhaseLockingResult> {

    private final ArrayList<PhaseLockingResult> results = new ArrayList<>();

    public PhaseLockingResultSet() {
    }

    public void add(PhaseLockingResult result) {
        results.add(result);
    }

    @Override
    public Iterator<PhaseLockingResult> iterator() {
        return results.iterator();
    }

    public int size() {
        return results.size();
    }

    public PhaseLockingResult get(int index) {
        return results.get(index);
    }

    public double average() {
        double sumC = 0;
        for (PhaseLockingResult result : this) {
            double c = result.contrast();
            if (Double.isFinite(c)) {
                sumC += 1 / c;
            }
        }
        return size() / sumC;
    }

    public double rate(double contrast) {
        int count = 0;
        for (PhaseLockingResult result : this) {
            if (result.contrast() >= contrast) {
                count++;
            }
        }
        return count * 1. / size();
    }
}
