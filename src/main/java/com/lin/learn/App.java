package com.lin.learn;

import com.lin.learn.base.ThreadSleep;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 */
public class App {
    //public static AtomicInteger num = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ThreadSleep.runIntEmbedSafePoint();
    }
}
