package com.alibaba.middleware.race.sync;

/**
 * Created by tuzhenyu on 17-2-27.
 * @author tuzhenyu
 */
public class SpliteThread extends Thread{
    private LogStore logStore;

    public SpliteThread(LogStore logStore){
        this.logStore = logStore;
    }

    @Override
    public void run(){
        try {
            logStore.spliteBytes();
            System.out.println(Thread.currentThread().getName()+"-split finished");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
