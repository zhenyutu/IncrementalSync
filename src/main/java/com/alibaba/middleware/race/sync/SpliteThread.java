package com.alibaba.middleware.race.sync;

import java.util.concurrent.CountDownLatch;

/**
 * Created by tuzhenyu on 17-2-27.
 * @author tuzhenyu
 */
public class SpliteThread extends Thread{
    private LogStore logStore;
    private CountDownLatch countDownLatch;

    public SpliteThread(CountDownLatch countDownLatch,LogStore logStore){
        this.logStore = logStore;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run(){
        try {
            logStore.spliteBytes();
            countDownLatch.countDown();
            System.out.println(Thread.currentThread().getName()+"-split finished");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
