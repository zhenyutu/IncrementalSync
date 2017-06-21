package com.alibaba.middleware.race.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tuzhenyu on 17-6-7.
 * @author tuzhenyu
 */
public class LogStore {
    private static Logger logger = LoggerFactory.getLogger(LogStore.class);

    private static final LogStore INSTANCE = new LogStore();
    public static LogStore getInstance() {
        return INSTANCE;
    }

    private static final int PAGE_SIZE = 20*1024*1024;
    private static final int PAGE_COUNT = 10*1024*1024;


    private static final byte SPLITE_FLAG = (byte)124;
    private static final byte END_FLAG = (byte)10;
    private static final byte SPACE_FLAG = (byte)9;

    private static final byte FIRST_FLAG = (byte)110;
    private static final byte LAST_FLAG = (byte)97;
    private static final byte SEX_FLAG = (byte)48;
    private static final byte SCORE1_FLAG = (byte)49;
    private static final byte SCORE2_FLAG = (byte)58;

    private static final byte INSERT_FLAG = (byte)73;
    private static final byte UPDATE_FLAG = (byte)85;
    private static final byte DELETE_FLAG = (byte)68;

    private LinkedBlockingQueue<byte[]> bufferQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<byte[]> logQueue = new LinkedBlockingQueue<>();


    private Map<Integer,Long> filePositionMap = new HashMap<>();
    private Map<Integer,FileChannel> fileChannelMap = new HashMap<>();

    private volatile int fileNum = 1;
    private final static int THREAD_NUM = 5;

    private volatile boolean running = false;
    private volatile boolean[] finishArr = null;
    private Map<Long,Long> addMap = new ConcurrentHashMap<>(550000);
    private ByteBuffer resultBuffer = null;

    private byte[] empty_28 = new byte[28];
    private byte[] empty_3 = new byte[3];

    public void init(int start,int end)throws Exception{
        logger.info("get into the init");
        resultBuffer = ByteBuffer.allocate(PAGE_COUNT*28);
        finishArr = new boolean[end-start+1];
    }

    public void pullBytesFormFile(String path) throws Exception {
        logger.info("get into the pullBytesFormFile");

        while (true){
            MappedByteBuffer buffer;
            synchronized(this){
                if (running)
                    break;
                FileChannel channel = fileChannelMap.get(fileNum);
                if (channel==null){
                    String file = path + "/" + fileNum + ".txt";
                    logger.info(file);
                    channel = new RandomAccessFile(file, "r").getChannel();
                    fileChannelMap.put(fileNum,channel);
                }
                Long filePosition = filePositionMap.get(fileNum);
                if (filePosition==null){
                    filePosition = 0L;
                    filePositionMap.put(fileNum,filePosition);
                }
                buffer = channel.map(FileChannel.MapMode.READ_ONLY, filePosition , Math.min(channel.size()-filePosition , PAGE_SIZE));
                filePosition = filePosition + PAGE_SIZE;
                filePositionMap.put(fileNum,filePosition);

                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                bufferQueue.put(bytes);

                if (filePosition>channel.size()){
                    if (fileNum<10){
                        fileChannelMap.get(fileNum).close();
                        fileNum++;
                    }
                    else{
                        running = true;
                        break;
                    }
                }
            }

        }
    }

    public void spliteBytes()throws Exception{
        logger.info("get into the spliteBytes");
        byte[] logs;
        byte[] lastLogs = null;
        int num = 0;
        while (true){
            logs = bufferQueue.take();
            lastLogs = parseBytesFromQueue(logs,lastLogs);
            if(logs.length != PAGE_SIZE){
                num++;
            }
            if(num>9){
                for (int i=0;i<2*THREAD_NUM;i++){
                    byte[] tmp = new byte[1];
                    logQueue.put(tmp);
                }
                break;
            }
        }
    }

    private byte[] parseBytesFromQueue(byte[] bytes,byte[] lastLogs) throws Exception{
        byte[] logs ;
        byte[] newLastLogs;

        if (lastLogs != null){
            int firstEnd = findNextEnt(bytes,-2,END_FLAG);
            int length = firstEnd+1;
            byte[] log = new byte[length+lastLogs.length];
            System.arraycopy(lastLogs, 0, log, 0, lastLogs.length);
            System.arraycopy(bytes, 0, log, lastLogs.length, length);
            logQueue.put(log);

            logs = bytes;
            newLastLogs = getLogFromBytes(logs,firstEnd);

        }else{
            logs = bytes;
            newLastLogs = getLogFromBytes(logs,0);
        }

        return newLastLogs;
    }

    private byte[] getLogFromBytes(byte[] logs,int logEnd) throws Exception{
        int nextLogEnd,lastLogEnd,start = logs.length-1;
        byte[] lastLogs = null;
        if (!(logs[start]==END_FLAG)){
            lastLogEnd =  findpreEnt(logs,start,END_FLAG);
            lastLogs = new byte[start-lastLogEnd];
            System.arraycopy(logs,lastLogEnd+1,lastLogs,0,start-lastLogEnd);
        }

        while (true){
            nextLogEnd = findNextEnt(logs,logEnd,END_FLAG);
            if (nextLogEnd==logEnd)
                break;
            byte[] log = new byte[nextLogEnd-logEnd];
            System.arraycopy(logs, logEnd+1, log, 0, nextLogEnd-logEnd);
            logQueue.put(log);

            logEnd = nextLogEnd;
        }

        return lastLogs;
    }

    public void parseBytes(int startId,int endId)throws Exception{
        while (true){
            byte[] log = logQueue.take();
            if (log.length>1){
                operate(log,startId,endId);
            }
            else
                break;
        }
    }

    private void operate(byte[] log,int startId,int endId)throws Exception{
        int start = findFirstByte(log,0,SPLITE_FLAG,4);
        byte operate = log[start+1];
        start = findFirstByte(log,start,SPLITE_FLAG,1);
        switch (operate){
            case INSERT_FLAG:
                insertOperate(log, start,startId,endId);
                break;
            case UPDATE_FLAG:
                updateOperate(log, start,startId,endId);
                break;
            case DELETE_FLAG:
                deleteOperate(log, start,startId,endId);
                break;
            default:
                logger.info("error!");
        }
    }


    private int findFirstByte(byte[] logs,int start,byte value,int num){
        int index = start;
        for (int i=start+1;i<logs.length;i++){
            if (logs[i] == value){
                num--;
                if (num<=0){
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    private static int findpreEnt(byte[] logs,int start,byte value){
        int index = start;
        for (int i=start;i>=0;i--){
            if (logs[i] == value){
                index = i;
                break;
            }
        }
        return index;
    }

    private static int findNextEnt(byte[] logs,int start,byte value){
        int index = start;
        for (int i=start+2;i<logs.length;i++){
            if (logs[i] == value){
                index = i;
                break;
            }
        }
        return index;
    }

    private void insertOperate(byte[] logs,int start,int startId,int endId)throws IOException{
        int idStart,idEnd=start;
        idStart = findFirstByte(logs,idEnd,SPLITE_FLAG,2);
        idEnd = findFirstByte(logs,idStart,SPLITE_FLAG,1);
        byte[] idBytes = new byte[idEnd-idStart-1];
        System.arraycopy(logs,idStart+1,idBytes,0,idEnd-idStart-1);
        long id = Long.parseLong(new String(idBytes));

        ByteBuffer tmpBuffer = (ByteBuffer) resultBuffer.position((int)id*28);
        tmpBuffer.putLong(id);
        for (int n = 0;;n=n+2){
            if (idEnd+2<logs.length){
                byte tag = logs[idEnd+7];
                idStart = findFirstByte(logs,idEnd,SPLITE_FLAG,2);
                idEnd = findFirstByte(logs,idStart,SPLITE_FLAG,1);
                byte[] tmp = new byte[idEnd-idStart-1];
                System.arraycopy(logs,idStart+1,tmp,0,idEnd-idStart-1);
                updateTag(id,tag,tmp);
            }else {
                break;
            }
        }
        if (id>startId&&id<endId)
            finishArr[(int) id-startId] = false;
    }

    private void updateTag(long id,byte tag,byte[] bytes)throws IOException{
        try{
            switch (tag){
                case FIRST_FLAG:
                    ByteBuffer buffer1 = (ByteBuffer) resultBuffer.position((int)id*28+8);
                    buffer1.put(bytes);
                    break;
                case LAST_FLAG:
                    ByteBuffer buffer2 = (ByteBuffer) resultBuffer.position((int)id*28+11);
                    if (bytes.length==6){
                        buffer2.put(bytes);
                    }else {
                        buffer2.put(bytes);
                        buffer2.put(empty_3);
                    }
                    break;
                case SEX_FLAG:
                    ByteBuffer buffer3 = (ByteBuffer) resultBuffer.position((int)id*28+17);
                    buffer3.put(bytes);
                    break;
                case SCORE1_FLAG:
                    ByteBuffer buffer4 = (ByteBuffer) resultBuffer.position((int)id*28+20);
                    buffer4.putInt(Integer.parseInt(new String(bytes)));
                    break;
                case SCORE2_FLAG:
                    ByteBuffer buffer5 = (ByteBuffer) resultBuffer.position((int)id*28+24);
                    buffer5.putInt(Integer.parseInt(new String(bytes)));
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void updateOperate(byte[] logs,int start,int startId,int endId)throws Exception{
        int idStart = findFirstByte(logs,start,SPLITE_FLAG,1);
        int idEnd0 = findFirstByte(logs,idStart,SPLITE_FLAG,1);
        int idEnd = findFirstByte(logs,idEnd0,SPLITE_FLAG,1);

        byte[] idBytes1 = new byte[idEnd0-idStart-1];
        System.arraycopy(logs,idStart+1,idBytes1,0,idEnd0-idStart-1);
        long lastId = Long.parseLong(new String(idBytes1));

        byte[] idBytes2 = new byte[idEnd-idEnd0-1];
        System.arraycopy(logs,idEnd0+1,idBytes2,0,idEnd-idEnd0-1);
        long id = Long.parseLong(new String(idBytes2));
        if (lastId>PAGE_COUNT&&!addMap.keySet().contains(lastId))
            System.out.println("");

        if (lastId==id){
            if (!addMap.keySet().contains(lastId)){
                for (;idEnd+2<logs.length;){
                    byte tag = logs[idEnd+7];

                    idStart = findFirstByte(logs,idEnd,SPLITE_FLAG,2);
                    idEnd = findFirstByte(logs,idStart,SPLITE_FLAG,1);
                    byte[] tmp = new byte[idEnd-idStart-1];
                    System.arraycopy(logs,idStart+1,tmp,0,idEnd-idStart-1);

                    updateTag((int)id,tag,tmp);
                }
            }else {
                for (;idEnd+2<logs.length;){
                    byte tag = logs[idEnd+7];

                    idStart = findFirstByte(logs,idEnd,SPLITE_FLAG,2);
                    idEnd = findFirstByte(logs,idStart,SPLITE_FLAG,1);
                    byte[] tmp = new byte[idEnd-idStart-1];
                    System.arraycopy(logs,idStart+1,tmp,0,idEnd-idStart-1);

                    updateTag(addMap.get(id),tag,tmp);
                }
            }
        }else {
            if(lastId>startId&&lastId<endId){
                finishArr[(int) lastId-startId] = true;
            }
            if (id>startId&&id<endId){
                finishArr[(int) id-startId] = false;
            }

            if (!addMap.keySet().contains(lastId)){
                for (;idEnd+2<logs.length;){
                    byte tag = logs[idEnd+7];

                    idStart = findFirstByte(logs,idEnd,SPLITE_FLAG,2);
                    idEnd = findFirstByte(logs,idStart,SPLITE_FLAG,1);
                    byte[] tmp = new byte[idEnd-idStart-1];
                    System.arraycopy(logs,idStart+1,tmp,0,idEnd-idStart-1);

                    updateTag(lastId,tag,tmp);
                }

                addMap.put(id,lastId);
            }else {
                for (;idEnd+2<logs.length;){
                    byte tag = logs[idEnd+7];

                    idStart = findFirstByte(logs,idEnd,SPLITE_FLAG,2);
                    idEnd = findFirstByte(logs,idStart,SPLITE_FLAG,1);
                    byte[] tmp = new byte[idEnd-idStart-1];
                    System.arraycopy(logs,idStart+1,tmp,0,idEnd-idStart-1);

                    updateTag(addMap.get(lastId),tag,tmp);
                }
                addMap.put(id,addMap.get(lastId));
                addMap.remove(lastId);
            }
        }
    }
    private void deleteOperate(byte[] logs,int start,int startId,int endId){
        int idStart,idEnd=start;
        idStart = findFirstByte(logs,idEnd,SPLITE_FLAG,1);
        idEnd = findFirstByte(logs,idStart,SPLITE_FLAG,1);
        byte[] idBytes = new byte[idEnd-idStart-1];
        System.arraycopy(logs,idStart+1,idBytes,0,idEnd-idStart-1);
        long lastId = Long.parseLong(new String(idBytes));

        if (!addMap.keySet().contains(lastId)){
            ByteBuffer buffer1 = (ByteBuffer) resultBuffer.position((int)lastId*28);
            buffer1.put(empty_28);

        }else {
            addMap.remove(lastId);
        }

        if (lastId>=startId&&lastId<=endId&&!finishArr[(int)lastId-startId])
            finishArr[(int) lastId-startId] = true;
    }


    public ByteBuffer parse(int startId,int endId){
        logger.info("enter the parse : "+ addMap.size());
        ByteBuffer result = ByteBuffer.allocate((endId-startId+1)*28);
        long id,index;
        byte[] name = new byte[3];
        byte[] empty = new byte[20];
        for (int i=startId+1;i<endId;i++){
            if (!finishArr[i-startId]) {
                if (addMap.keySet().contains((long)i)) {
                    index = addMap.get((long)i);
                } else {
                    index = (long)i;
                }
                ByteBuffer buffer = (ByteBuffer) resultBuffer.position((int)index*28);
                id = buffer.getLong();
                if (id!=0){
                    result.put(String.valueOf(i).getBytes());
                    result.put(SPACE_FLAG);
                    buffer.get(name);
                    result.put(name);
                    result.put(SPACE_FLAG);
                    buffer.get(name);
                    result.put(name);
                    buffer.get(name);
                    if ((name[1]|name[2])!=0) {
                        result.put(name);
                    }
                    result.put(SPACE_FLAG);
                    buffer.get(name);
                    result.put(name);
//                    result.put(SPACE_FLAG);
//                    id = buffer.getInt();
//                    result.put(String.valueOf(id).getBytes());
                    result.put(SPACE_FLAG);
                    id = buffer.getInt();
                    result.put(String.valueOf(id).getBytes());
                    result.put(END_FLAG);
                }else
                    buffer.get(empty);
            }
        }

        result.flip();
        return result;
    }

    public void flush(ByteBuffer buffer) throws IOException{
        String fileName = Constants.MIDDLE_HOME+"/RESULT.rs";
        FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel();
        channel.write(buffer);
    }


    public static void main(String[] args) throws Exception{
        LogStore logStore = getInstance();
        int start = 100000;
        int end = 2000000;

        logStore.init(start,end);
        String path = "/home/tuzhenyu/tmp/canal_data/2";

        CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM+2);
        long startConsumer = System.currentTimeMillis();

        for (int i=0;i<1;i++){
            new ProduceThread(countDownLatch,logStore,path).start();
        }

        new SpliteThread(countDownLatch,logStore).start();

        for (int i=0;i<THREAD_NUM;i++){
            new parseThread(countDownLatch,logStore,start,end).start();
        }

        countDownLatch.await();
        logStore.parseBytes(start,end);
        System.out.println("finish the parse");
        ByteBuffer buffer = logStore.parse(start,end);
        logStore.flush(buffer);
        long endConsumer = System.currentTimeMillis();
        System.out.println(endConsumer-startConsumer);
    }
}
