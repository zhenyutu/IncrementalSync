package com.alibaba.middleware.race.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by tuzhenyu on 17-6-7.
 * @author tuzhenyu
 */
public class LogStore {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

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
    private static final byte EMPTY_FLAG = (byte)0;

    private static final byte INSERT_FLAG = (byte)73;
    private static final byte UPDATE_FLAG = (byte)85;
    private static final byte DELETE_FLAG = (byte)68;

    private ArrayBlockingQueue<byte[]> bufferQueue = new ArrayBlockingQueue<>(4);
    private Map<Integer,Long> filePositionMap = new HashMap<>();
    private Map<Integer,FileChannel> fileChannelMap = new HashMap<>();
    private volatile int fileNum = 1;

    private boolean running = false;
    private int position = 0;

    private Map<Long,Long> addMap = new HashMap<>(10000);
    private boolean[] finishArr = null;

    private byte[] empty_28 = new byte[28];
    private byte[] empty_3 = new byte[3];

    private ByteBuffer resultBuffer = null;

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

    public void parseBytes(int start,int end)throws Exception{
        logger.info("get into the parseBytes");
        byte[] logs;
        byte[] lastLogs = null;
        int num = 0;
        while (true){
            logs = bufferQueue.take();
            lastLogs = parseBytesFromQueue(logs,lastLogs,start,end);
            if(logs.length != PAGE_SIZE){
                num++;
            }
            if(num>9)
                break;
        }
    }

    private byte[] parseBytesFromQueue(byte[] bytes,byte[] lastLogs,int start,int end){
        byte[] logs = null;

        if (lastLogs != null){
            logs = new byte[lastLogs.length+bytes.length];
            System.arraycopy(lastLogs, 0, logs, 0, lastLogs.length);
            System.arraycopy(bytes, 0, logs, lastLogs.length, bytes.length);
        }else
            logs = bytes;
        byte[] newLastLogs = null;
        try {
            newLastLogs = getLogFromBytes(logs, start, end);
        }catch (IOException e){
            logger.info("error in the parseBytesFromQueue");
            e.printStackTrace();
        }

        return newLastLogs;
    }

    private byte[] getLogFromBytes(byte[] logs,int startId,int endId) throws IOException{
        int nextLogEnd,logEnd = 0,lastLogEnd,start = logs.length-1;
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
            operate(logs,logEnd,nextLogEnd,startId,endId);
            logEnd = nextLogEnd;
        }

        return lastLogs;
    }

    private void operate(byte[] logs,int preLogEnd,int logEnd,int startId,int endId)throws IOException{
        int start = findFirstByte(logs,preLogEnd+2,SPLITE_FLAG,4);
        byte operate = logs[start+1];
        start = findFirstByte(logs,start,SPLITE_FLAG,1);
        switch (operate){
            case INSERT_FLAG:
                insertOperate(logs, start, logEnd,startId,endId);
                break;
            case UPDATE_FLAG:
                updateOperate(logs, start, logEnd,startId,endId);
                break;
            case DELETE_FLAG:
                deleteOperate(logs, start, logEnd,startId,endId);
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
        position = index;
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

    private byte[] findSingleStr(byte[] logs,int start,byte value,int num){
        int index = start;
        int end = start;
        for (int i=start+1;i<logs.length;i++){
            if (logs[i] == value){
                num--;
                if (num<=0){
                    end = i;
                    break;
                }
                index = i;
            }
        }
        position = end;
        byte[] schemaBytes = new byte[end-index-1];
        System.arraycopy(logs,index+1,schemaBytes,0,end-index-1);
        return schemaBytes;
    }

    private void insertOperate(byte[] logs,int start,int end,int startId,int endId)throws IOException{
        byte[] idBytes = findSingleStr(logs,start,SPLITE_FLAG,3);
        long id = Long.parseLong(new String(idBytes));

        ByteBuffer tmpBuffer = (ByteBuffer) resultBuffer.position((int)id*28);
        tmpBuffer.putLong(id);
        for (int n = 0;;n=n+2){
            if (position+2<end){
                byte tag = logs[position+7];
                byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                updateTag(id,tag,tmp);
            }else {
                break;
            }
        }
        if (id>startId&&id<endId)
            finishArr[(int) id-startId] = false;
    }

    private void updateTag(long id,byte tag,byte[] bytes)throws IOException{
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
    }
    private void updateOperate(byte[] logs,int start,int end,int startId,int endId)throws IOException{
        long lastId = Long.parseLong(new String(findSingleStr(logs,start,SPLITE_FLAG,2)));
        long id = Long.parseLong(new String(findSingleStr(logs,position,SPLITE_FLAG,1)));

        if (lastId==id){
            if (!addMap.keySet().contains(lastId)){
                for (;position+2<end;){
                    byte tag = logs[position+7];
                    byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                    updateTag((int)id,tag,tmp);
                }
            }else {
                for (;position+2<end;){
                    byte tag = logs[position+7];
                    byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
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
                for (;position+2<end;){
                    byte tag = logs[position+7];
                    byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                    updateTag(lastId,tag,tmp);
                }

                addMap.put(id,lastId);
            }else {
                for (;position+2<end;){
                    byte tag = logs[position+7];
                    byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                    updateTag(addMap.get(lastId),tag,tmp);
                }
                addMap.put(id,addMap.get(lastId));
                addMap.remove(lastId);
            }
        }
    }
    private void deleteOperate(byte[] logs,int start,int end,int startId,int endId){
        long lastId = Long.parseLong(new String(findSingleStr(logs,start,SPLITE_FLAG,2)));

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
                    result.put(SPACE_FLAG);
                    id = buffer.getInt();
                    result.put(String.valueOf(id).getBytes());
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

        long startConsumer = System.currentTimeMillis();
        for (int i=0;i<3;i++){
            new ProduceThread(logStore,path)    .start();
        }
        logStore.parseBytes(start,end);
        System.out.println("finish the parse");
        ByteBuffer buffer = logStore.parse(start,end);
        logStore.flush(buffer);
        long endConsumer = System.currentTimeMillis();
        System.out.println(endConsumer-startConsumer);
    }


}
