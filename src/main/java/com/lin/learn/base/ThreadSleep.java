package com.lin.learn.base;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用Thread.sleep(0)这个方法嵌入安全点（safe point）避免长时间GC，
 * 削峰填谷。在可数循环（Counted Loop）的情况下，HotSpot虚拟机搞了一个优化，
 * 就是等待循环结束，进入安全点，才能开始工作。
 * <br><br/>
 *  * see <a href="https://juejin.cn/post/7139741080597037063"></a>
 *
 * 注意：从JDK10起，HotSpot实现了Loop Strip Mining优化，解决了counted loop中安全点轮询的问题
 * see <a href="https://stackoverflow.com/questions/67068057/the-main-thread-exceeds-the-set-sleep-time">The main thread exceeds the set sleep time</a>
 * a href="https://bugs.openjdk.org/browse/JDK-8223051">support loops with long (64b) trip counts</a>
 *
 * <br><br/>
 * <h3>可数循环<h3/>
 * see:
 * <a href="https://juejin.cn/post/6844903878765314061/">HBase实战：记一次Safepoint导致长时间STW的踩坑之旅</a>
 * <h3>安全点<h3/>
 * see: <a href="http://blog.ragozin.info/2012/10/safepoints-in-hotspot-jvm.html">Safepoints in HotSpot JVM</a>
 */
public class ThreadSleep {

    public static AtomicInteger num = new AtomicInteger(0);

    /*
     * 下面这段程序干了几件事情：
     * 1. 启动连个长的、不间断的循环（内部没有安全点检查）
     * 2. 主线程进入睡眠状态1秒钟
     * 3. 在1000ms之后，JVM尝试在Safepoint停止，以便Java线程进行定期清理，
     *    但是直到可数循环完成后才能执行此操作
     * 4. 主线程的Thread.sleep方法从native返回，发现安全点操作正在进行中，于是把自己挂起，
     *    直到操作结束
     */
    public static void runInt() throws InterruptedException {
        Runnable runnable = () -> {
            // 可数循环
            for (int i = 0; i < 1000000000; i++) {
                num.getAndAdd(1);
            }
            System.out.println(Thread.currentThread().getName() + "执行结束");
        };

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        t1.start();
        t2.start();
        Thread.sleep(1000);
        System.out.println("num = " + num);
    }

    public static void runLong() throws InterruptedException {
        Runnable runnable = () -> {
            // 可数循环
            for (long i = 0; i < 1000000000; i++) {
                num.getAndAdd(1);
            }
            System.out.println(Thread.currentThread().getName() + "执行结束");
        };

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        t1.start();
        t2.start();
        Thread.sleep(1000);
        System.out.println("num = " + num);
    }

    public static void runIntEmbedSafePoint() throws InterruptedException {
        Runnable runnable = () -> {
            // 可数循环
            for (int i = 0; i < 1000000000; i++) {
                num.getAndAdd(1);
                //add safe point
                if (i % 1000 == 0) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(Thread.currentThread().getName() + "执行结束");
        };

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        t1.start();
        t2.start();
        Thread.sleep(1000);
        System.out.println("num = " + num);
    }
}
