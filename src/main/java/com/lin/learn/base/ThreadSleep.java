package com.lin.learn.base;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用Thread.sleep(0)这个方法嵌入安全点（safe point）避免长时间GC，
 * 削峰填谷。在可数循环（Counted Loop）的情况下，HotSpot虚拟机搞了一个优化，
 * 就是等待循环结束，进入安全点，才能开始工作。
 * <br><br/>
 * see:
 * <br></br>
 * <a href="https://juejin.cn/post/7139741080597037063">没有二十年功力，写不出Thread.sleep(0)这一行“看似无用”的代码！</a>
 * <br></br>
 * <a href="https://juejin.cn/post/7142709426401574949">写个续集，填坑来了！关于“Thread.sleep(0)这一行‘看似无用’的代码”里面留下的坑。</a>
 * <br></br>
 * 注意：从JDK10起，HotSpot实现了Loop Strip Mining优化，解决了counted loop中安全点轮询的问题
 * <br></br>
 * see:
 * <br></br>
 * <a href="https://stackoverflow.com/questions/67068057/the-main-thread-exceeds-the-set-sleep-time">The main thread exceeds the set sleep time</a>
 * <br></br>
 * <a href="https://bugs.openjdk.org/browse/JDK-8223051">support loops with long (64b) trip counts</a>
 *
 * <br><br/>
 * <h3>可数循环<h3/>
 * see:
 * <br></br>
 * <a href="https://juejin.cn/post/6844903878765314061/">HBase实战：记一次Safepoint导致长时间STW的踩坑之旅</a>
 * <h3>安全点<h3/>
 * see:
 * <br></br>
 * <a href="http://psy-lob-saw.blogspot.com/2015/12/safepoints.html">安全点的意义、副作用以及开销</a>
 * <br></br>
 * <a href="https://mp.weixin.qq.com/s/Imyo_cQ5OWdY9fY0Qz3nzw">我被读者卷了...</a>
 * <br></br>
 * <a href="https://mp.weixin.qq.com/s?__biz=Mzg3NjU3NTkwMQ==&mid=2247509056&idx=1&sn=1d8383e50127b6b45186d243b92f5037&scene=21#wechat_redirect">真是绝了！这段被JVM动了手脚的代码！</a>
 * <br></br>
 * <a href="https://zhuanlan.zhihu.com/p/286110609">jvm大局观之内存管理篇: 理解jvm安全点,写出更高效的代码</a>
 * <br></br>
 * <a href="http://blog.ragozin.info/2012/10/safepoints-in-hotspot-jvm.html">Safepoints in HotSpot JVM</a>
 * <br></br>
 * <a href="https://www.zhihu.com/question/29268019/answer/43762165">现代JVM中的Safe Region和Safe Point到底是如何定义和划分的? - RednaxelaFX的回答 - 知乎</a>
 * <br></br>
 * <a href="https://www.javatt.com/p/47329">Java-JVM-安全点SafePoint</a>
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
