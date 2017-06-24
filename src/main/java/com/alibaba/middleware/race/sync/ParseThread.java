package com.alibaba.middleware.race.sync;

/**
 * Created by tuzhenyu on 17-6-23.
 */
public class ParseThread implements Runnable {
    private LogStore logStore;
    private byte[] log;
    private int startId;
    private int endId;

    public ParseThread(byte[] log, int startId, int endId) {
        this.logStore = LogStore.getInstance();
        this.log = log;
        this.startId = startId;
        this.endId = endId;
    }

    @Override
    public void run() {
        try {
            logStore.operate(log, startId, endId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
