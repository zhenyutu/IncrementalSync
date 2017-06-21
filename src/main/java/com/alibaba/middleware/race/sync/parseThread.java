package com.alibaba.middleware.race.sync;

/**
 * Created by tuzhenyu on 17-2-27.
 * @author tuzhenyu
 */
public class parseThread extends Thread{
    private LogStore logStore;
    private int start;
    private int end;

    public parseThread(LogStore logStore,int start,int end){
        this.logStore = logStore;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run(){
        try {
            logStore.parseBytes(start,end);
            System.out.println(Thread.currentThread().getName()+"-parse finished");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
