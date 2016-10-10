package com.csc108.disruptor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class PerformanceCounter {
    private static AtomicInteger integer = new AtomicInteger(0);
    public static void increase(){
        integer.incrementAndGet();
    }

    public static Integer getCounter(){
        return integer.getAndIncrement();
    }
}
