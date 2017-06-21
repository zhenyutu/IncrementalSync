package com.alibaba.middleware.race.sync;

import java.util.concurrent.CountDownLatch;

/**
 * Created by tuzhenyu on 17-2-27.
 * @author tuzhenyu
 */
public class ProduceThread extends Thread{
    private LogStore logStore;
    private String path;
    private CountDownLatch countDownLatch;

    public ProduceThread(CountDownLatch countDownLatch,LogStore logStore, String path){
        this.logStore = logStore;
        this.path = path;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run(){
        try {
            logStore.pullBytesFormFile(path);
            countDownLatch.countDown();
            System.out.println(Thread.currentThread().getName()+"-produce finished");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
