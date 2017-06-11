package com.alibaba.middleware.race.sync;

/**
 * Created by tuzhenyu on 17-2-27.
 * @author tuzhenyu
 */
public class ProduceThread2 extends Thread{
    private LogStore2 logStore2;
    String path;

    public ProduceThread2(LogStore2 logStore2, String path){
        this.logStore2 = logStore2;
        this.path = path;
    }

    @Override
    public void run(){
        try {
            logStore2.pullBytesFormFile(path);
            System.out.println(Thread.currentThread().getName()+" finished");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
