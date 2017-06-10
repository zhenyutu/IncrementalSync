package com.alibaba.middleware.race.sync;

/**
 * Created by tuzhenyu on 17-2-27.
 * @author tuzhenyu
 */
public class ProduceThread extends Thread{
    private LogStore logStore;
    String path;

    public ProduceThread(LogStore logStore,String path){
        this.logStore = logStore;
        this.path = path;
    }

    @Override
    public void run(){
        try {
            logStore.pullBytesFormFile(path);
            System.out.println(Thread.currentThread().getName()+" finished");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
