package com.hwaipy.rrdps;

import java.util.Arrays;

/**
 *
 * @author Hwaipy
 */
public class EncodingRandom {

    private  int[] random;

    public EncodingRandom(int[] random) {
        this.random = random;
        if (random.length != 128) {
            throw new RuntimeException();
        }
    }

    public int getEncode(int pulseIndex, int delay) {
        if (pulseIndex >= delay && pulseIndex <= 127) {
            int a = random[pulseIndex];
            int b = random[pulseIndex - delay];
            return a ^ b;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(random); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
