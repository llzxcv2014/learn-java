package com.lin.learn;

import com.lin.learn.base.ThreadSleep;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 */
public class App {
git status
    //public static AtomicInteger num = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ThreadSleep.runIntEmbedSafePoint();
    }
}
