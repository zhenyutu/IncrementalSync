package com.alibaba.middleware.race.sync;

import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args) throws Exception {
        LogStore logStore = LogStore.getInstance();
        int start = 100;
        int end = 200;

        logStore.init(start, end);
        String path = "/home/hujianxin/tmp/canal_data/1";

        long startConsumer = System.currentTimeMillis();

        for (int i = 0; i < 1; i++) {
            new ProduceThread(logStore, path).start();
        }
        logStore.splitBytes(start, end);
        System.out.println("finish the parse");
        ByteBuffer buffer = logStore.parse(start, end);
        logStore.flush(buffer);
        long endConsumer = System.currentTimeMillis();
        System.out.println(endConsumer - startConsumer);
    }

}
