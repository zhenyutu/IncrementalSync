package com.alibaba.middleware.race.sync;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by tuzhenyu on 17-2-27.
 * @author tuzhenyu
 */
public class parseThread extends Thread{
    private LogStore logStore;
    private int start;
    private int end;
    private CountDownLatch countDownLatch;

    public parseThread(CountDownLatch countDownLatch,LogStore logStore,int start,int end){
        this.logStore = logStore;
        this.start = start;
        this.end = end;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run(){
        try {
            logStore.parseBytes(start,end);
            System.out.println(Thread.currentThread().getName()+"-parse finished");
            countDownLatch.countDown();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
