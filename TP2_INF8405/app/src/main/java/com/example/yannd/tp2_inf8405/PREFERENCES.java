package com.example.yannd.tp2_inf8405;

import java.util.BitSet;

/**
 * Created by yannd on 2016-03-03.
 */
public enum PREFERENCES{
    BAR(0),
    PARK(1),
    RESTAURANT(2),
    CAFE(3),
    LIBRARY(4),
    UNIVERSITY(5),
    NUMBER_OF_PREFERENCES(6);
    private int value;

    public int getValue() {
        return value;
    }
    public BitSet getBitsetValue() {
        BitSet val = new BitSet(NUMBER_OF_PREFERENCES.getValue());
        val.set(value, true);
        return val;
    }
    PREFERENCES(int value){
        this.value = value;
    }
}
