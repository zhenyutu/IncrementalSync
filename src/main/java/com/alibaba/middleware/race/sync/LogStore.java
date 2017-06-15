package com.alibaba.middleware.race.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final byte SPLITE_FLAG = (byte)124;
    private static final byte END_FLAG = (byte)10;
    private static final byte SPACE_FLAG = (byte)9;

    private static final byte FIRST_FLAG = (byte)114;
    private static final byte LAST_FLAG = (byte)115;
    private static final byte SEX_FLAG = (byte)120;
    private static final byte SCORE_FLAG = (byte)111;
    private static final byte EMPTY_FLAG = (byte)0;

    private static final byte INSERT_FLAG = (byte)73;
    private static final byte UPDATE_FLAG = (byte)85;
    private static final byte DELETE_FLAG = (byte)68;


    private ArrayBlockingQueue<byte[]> bufferQueue = new ArrayBlockingQueue<>(4);
    private Map<Integer,Long> filePositionMap = new HashMap<>();
    private Map<Integer,FileChannel> fileChannelMap = new HashMap<>();
    private volatile int fileNum = 10;

    private boolean running = false;
    private int position = 0;
    private ByteBuffer resultBuffer = null;

    private Map<Long,Integer> addMap = new HashMap<>();
    private boolean[] finishArr = null;

    public void init(int start,int end)throws Exception{
        logger.info("get into the init");
        resultBuffer = ByteBuffer.allocate((end-start+1)*20);
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
                    filePosition = channel.size();
                    filePositionMap.put(fileNum,filePosition);
                }
                buffer = channel.map(FileChannel.MapMode.READ_ONLY, Math.max(filePosition-PAGE_SIZE , 0), Math.min(filePosition , PAGE_SIZE));
                filePosition = filePosition - PAGE_SIZE;
                filePositionMap.put(fileNum,filePosition);

                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                bufferQueue.put(bytes);

                if (filePosition<0){
                    if (fileNum>1){
                        fileChannelMap.get(fileNum).close();
                        fileNum--;
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
                operate(logs,-2,lastLogs.length,start,end);
                num++;
            }
            if(num>9)
                break;
        }
    }

    private byte[] parseBytesFromQueue(byte[] bytes,byte[] lastLogs,int start,int end)throws IOException{
        byte[] logs ;
        byte[] newLastLogs;

        if (lastLogs != null){
            int firstEnd = findFirstEnt(bytes,bytes.length-1,END_FLAG);
            int length = bytes.length-firstEnd-1;
            byte[] log = new byte[lastLogs.length+length];
            System.arraycopy(bytes, firstEnd+1, log, 0,length);
            System.arraycopy(lastLogs, 0, log, length, lastLogs.length);
            operate(log,-2,log.length-1,start,end);

            logs = bytes;
            newLastLogs = getLogFromBytes(logs,firstEnd, start, end);

        }else{
            logs = bytes;
            newLastLogs = getLogFromBytes(logs,logs.length, start, end);
        }

        return newLastLogs;
    }

    private byte[] getLogFromBytes(byte[] logs,int logEnd,int startId,int endId) throws IOException{
        int start = 0, end ,preLogEnd;
        end =  findFirstByte(logs,start,END_FLAG,1);
        byte[] lastLogs = new byte[end+1];
        System.arraycopy(logs,0,lastLogs,0,end+1);

        while (true){
            preLogEnd = findNextEnt(logs,logEnd,END_FLAG);
            if (preLogEnd==logEnd)
                break;
            operate(logs,preLogEnd,logEnd,startId,endId);
            logEnd = preLogEnd;
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

    private static int findFirstEnt(byte[] logs,int start,byte value){
        int index = start;
        for (int i=start;i>=0;i--){
            if (logs[i] == value){
                index = i;
                break;
            }
        }
        return index;
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

    private static int findNextEnt(byte[] logs,int start,byte value){
        int index = start;
        for (int i=start-2;i>=0;i--){
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
        if (id<=startId||id>=endId){
            if (addMap.keySet().contains(id)){
                insert(logs,addMap.get(id),end,startId);
            }
        }else {
            if (addMap.keySet().contains(id)){
                insert(logs,addMap.get(id),end,startId);
            }else if (!finishArr[(int)id-startId])
                insert(logs,(int)id,end,startId);
        }
    }

    private void insert(byte[] logs,int id ,int end,int startId){
        finishArr[id-startId] = true;
        ByteBuffer buffer = (ByteBuffer) resultBuffer.position((id-startId)*20);
        buffer.putInt(id);
        for (int n = 0;;n=n+2){
            if (position+2<end){
                byte tag = logs[position+3];
                byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                updateTag(id,tag,tmp,startId);
            }else {
                break;
            }
        }
    }
    private void updateTag(int id,byte tag,byte[] bytes,int startId){
        switch (tag){
            case FIRST_FLAG:
                if (resultBuffer.get((id-startId)*20+4)==EMPTY_FLAG){
                    ByteBuffer buffer1 = (ByteBuffer) resultBuffer.position((id-startId)*20+4);
                    buffer1.put(bytes);
                }
                break;
            case LAST_FLAG:
                if (resultBuffer.get((id-startId)*20+7)==EMPTY_FLAG){
                    ByteBuffer buffer2 = (ByteBuffer) resultBuffer.position((id-startId)*20+7);
                    buffer2.put(bytes);
                }
                break;
            case SEX_FLAG:
                if (resultBuffer.get((id-startId)*20+13)==EMPTY_FLAG){
                    ByteBuffer buffer3 = (ByteBuffer) resultBuffer.position((id-startId)*20+13);
                    buffer3.put(bytes);
                }
                break;
            case SCORE_FLAG:
                if (resultBuffer.get((id-startId)*20+19)==EMPTY_FLAG){
                    ByteBuffer buffer4 = (ByteBuffer) resultBuffer.position((id-startId)*20+16);
                    buffer4.putInt(Integer.parseInt(new String(bytes)));
                }
                break;
        }
    }
    private void updateOperate(byte[] logs,int start,int end,int startId,int endId){
        long lastId = Long.parseLong(new String(findSingleStr(logs,start,SPLITE_FLAG,2)));
        long id = Long.parseLong(new String(findSingleStr(logs,position,SPLITE_FLAG,1)));
        if (lastId==id){
            if (id>startId&&id<endId){ //范围之内且未进行最终操作
                if (!finishArr[(int)id-startId]){
                    for (;position+2<end;){
                        byte tag = logs[position+3];
                        byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                        updateTag((int)id,tag,tmp,startId);
                    }
                }else if (addMap.keySet().contains(id)){
                    for (;position+2<end;){
                        byte tag = logs[position+3];
                        byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                        updateTag(addMap.get(id),tag,tmp,startId);
                    }
                }
            }else if ((id<=startId||id>=endId)&&addMap.keySet().contains(id)){ //范围之外，映射进入范围之内
                for (;position+2<end;){
                    byte tag = logs[position+3];
                    byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                    updateTag(addMap.get(id),tag,tmp,startId);
                }
            }
        }else {
            if (lastId<endId&&lastId>startId){
                if ((id<=startId||id>=endId)){ //范围之内改到范围之外　删除范围之内的值
                    if (!addMap.keySet().contains(id))
                        finishArr[(int)lastId-startId] = true;
                    else{
                        finishArr[(int)lastId-startId] = true;
                        addMap.put(lastId,addMap.get(id));
                        addMap.remove(id);
                    }
                }else {                      //范围之内改到范围之内　删除范围之内的值，添加范围之内的值
                    if (!addMap.keySet().contains(id)){
                        for (;position+2<end;){
                            byte tag = logs[position+3];
                            byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                            updateTag((int)id,tag,tmp,startId);
                        }
                        finishArr[(int)id-startId] = true;
                        finishArr[(int)lastId-startId] = true;

                        addMap.put(lastId,(int)id);
                    }else {
                        for (;position+2<end;){
                            byte tag = logs[position+3];
                            byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                            updateTag(addMap.get(id),tag,tmp,startId);
                        }
                        addMap.put(lastId,addMap.get(id));
                        addMap.remove(id);
                    }
                }
            }else {
                if (id>startId&&id<endId){   //范围之外改到范围之内　添加范围之外的值
                    for (;position+2<end;){
                        byte tag = logs[position+3];
                        byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                        updateTag((int)id,tag,tmp,startId);
                    }
                    addMap.put(lastId,(int)id);
                }else {
                    if (addMap.keySet().contains(id)){
                        for (;position+2<end;){
                            byte tag = logs[position+3];
                            byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                            updateTag(addMap.get(id),tag,tmp,startId);
                        }
                        addMap.put(lastId,addMap.get(id));
                        addMap.remove(id);
                    }
                }
            }
        }
    }
    private void deleteOperate(byte[] logs,int start,int end,int startId,int endId){
        long lastId = Long.parseLong(new String(findSingleStr(logs,start,SPLITE_FLAG,2)));
        if (lastId>=startId&&lastId<=endId&&!finishArr[(int)lastId-startId])
            finishArr[(int) lastId-startId] = true;
    }

    public ByteBuffer parse(){
        ByteBuffer buffer = (ByteBuffer)resultBuffer.position(0);
        ByteBuffer result = ByteBuffer.allocate(buffer.capacity());
        int id;
        byte[] name = new byte[3];
        byte[] empty = new byte[16];
        while(buffer.hasRemaining()){
            id = buffer.getInt();
            if (id!=0){
                result.put(String.valueOf(id).getBytes());
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
                result.put(END_FLAG);
            }else
                buffer.get(empty);
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
//        logStore.init(100,500);
        String path = "/canal_data/1";
        long startConsumer = System.currentTimeMillis();
        for (int i=0;i<3;i++){
            new ProduceThread(logStore,path)    .start();
        }
        logStore.parseBytes(100,500);
        System.out.println("finish the parse");
        ByteBuffer buffer = logStore.parse();
        logStore.flush(buffer);
        long endConsumer = System.currentTimeMillis();
        System.out.println(endConsumer-startConsumer);
    }


}
