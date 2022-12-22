package com.lin.learn.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 讨论Discard Policy这个拒绝策略
 * <br></br>
 * see:
 * <br></br>
 * <a href="https://www.cnblogs.com/thisiswhy/p/16466015.html">看起来是线程池的BUG，但是我认为是源码设计不合理</a>
 */
public class LearnDiscardPolicy {

    public static void run() throws InterruptedException {
        // 创建任务
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            tasks.add(() -> {
                System.out.println("callable" + finalI);
                Thread.sleep(500);
                return null;
            });
        }

        // 创建核心数是2的线程池，在线程里调用了线程池的invokeAll方法
        //ExecutorService executor = Executors.newFixedThreadPool(2);

        // 自定义线程池
        ExecutorService executor = new ThreadPoolExecutor(
                1,
                1,
                1,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.DiscardPolicy());
        Thread executorInvokerThread = new Thread(() -> {
            try {
                /*
                 * Executes the given tasks, returning a list of Futures holding their status and results when all complete.
                 * 执行给定的任务集合，在左右任务完成后返回一个包含其结果和状态的Futures列表。
                 */
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("invokeAll returned");
        });
        executorInvokerThread.start();

        Thread.sleep(800);
        System.out.println("shutdown");
        // executor.shutdown();
        /*
         * 如果改成shutdownNow，程序没有退出，使用转储线程查看，可以看到线程一直在等待。
         * 并且很容易定位到invokeAll这个方法
         *
         * 对于这个问题，官方解释到：shutdownNow方法返回一个List列表，里面放的就是还没有执行的任务，
         * 所以需要对返回列表进行进一步的处理。shutdownNow返回List<Runnable>。
         *
         * 拿到这个返回值后如何取消任务？
         * 线程池提交任务有两种方式：
         * 1. 用execute()方法提交Runnable任务，shutdownNow返回的是未被执行的Runnable的列表
         * 2. submit()方法提交Runnable任务，那么会被封装一个FutureTask对象，所以调用shutdownNow方法返回的是未被执行的FutureTask的列表
         *
         * 修改如下：遍历shutdownNow方法返回的List集合，然后判断是否是Future类型，接着调用其cancel方法。
         *
         * 如果我们自定义线程池：核心线程数、最大线程数、队列长度都是1，采用的线程拒绝策略是DiscardPolicy
         * 再次运行，发现仍然会阻塞，线程仍然是WAITING状态。因为这些任务都被静默处理了，既不抛出异常也不能调用cancel方法
         * 对于这个问题官方回复到DiscardPolicy在现实场景很少使用到，并且不建议大家使用。
         */
        List<Runnable> runnables = executor.shutdownNow();
        for (Runnable r : runnables) {
            if (r instanceof Future) {
                ((Future<?>) r).cancel(false);
            }
        }
        System.out.println("Shutdown complete");
    }

    public static void main(String[] args) throws InterruptedException {
        run();
    }
}
