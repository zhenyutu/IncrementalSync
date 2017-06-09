package com.alibaba.middleware.race.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuzhenyu on 17-6-7.
 * @author tuzhenyu
 */
public class LogStore {
    private static Logger logger = LoggerFactory.getLogger(ServerDemoInHandler.class);

    private static final LogStore INSTANCE = new LogStore();
    public static LogStore getInstance() {
        return INSTANCE;
    }

    private static final int PAGE_SIZE = 4*1024;
    private static final int REMAINING_SIZE = 50;
    private static final byte SPLITE_FLAG = (byte)124;
    private static final byte END_FLAG = (byte)10;
    private static final byte SPACE_FLAG = (byte)9;

    private static final byte FIRST_FLAG = (byte)114;
    private static final byte LAST_FLAG = (byte)115;
    private static final byte SEX_FLAG = (byte)120;
    private static final byte SCORE_FLAG = (byte)111;
    private static final byte EMPTY_FLAG = (byte)0;

    private int position = 0;
    private ByteBuffer resultBuffer = null;

    private Map<String,ByteBuffer> fileByteBuffer = new HashMap<>();
    private Map<String,FileChannel> fileChannel = new HashMap<>();

    private ArrayList<Integer> addList = new ArrayList<>();
    private ArrayList<Integer> deleteList = new ArrayList<>();
    private Map<Integer,Integer> addMap = new HashMap<>();

    public void pullBytesFormFile(String file,String schema,String table,int start,int end) throws IOException {
        logger.info("get into the pullBytesFormFile");
        resultBuffer = ByteBuffer.allocate((end-start+1)*20);
        byte[] lastLogs = null;
        byte[] logs;
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        MappedByteBuffer buffer;
        String schemaTable = schema+"|"+table;

        for (long i = channel.size(); i > 0 ; i=i-PAGE_SIZE)
        {
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, Math.max(i-PAGE_SIZE , 0), Math.min(i , PAGE_SIZE));
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            if (lastLogs != null){
                logs = new byte[lastLogs.length+bytes.length];
                System.arraycopy(bytes, 0, logs, 0, bytes.length);
                System.arraycopy(lastLogs, 0, logs, bytes.length, lastLogs.length);
            }else {
                logs = bytes;
            }
            lastLogs = getLogFromBytes(logs, schemaTable, start, end);
        }

        flush(schemaTable);
        channel.close();
    }

    private byte[] getLogFromBytes(byte[] logs,String schemaTableName,int startId,int endId) throws IOException{
        int start = 0, end ,preLogEnd,logEnd= logs.length;
        end =  findFirstByte(logs,start,END_FLAG,1);
        byte[] lastLogs = new byte[end+1];
        System.arraycopy(logs,0,lastLogs,0,end+1);

        while (true){
            preLogEnd = findNextEnt(logs,logEnd,END_FLAG);
            if (preLogEnd==0)
                preLogEnd=preLogEnd-2;
            start = findFirstByte(logs,preLogEnd+2,SPLITE_FLAG,2);
            end = findFirstByte(logs,start,SPLITE_FLAG,2);
            String schemaTable = getStrFromBytes(logs,start,end);
            if (!schemaTableName.equals(schemaTable)){
                logEnd = preLogEnd;
                continue;
            }
            start = end;
            end = findFirstByte(logs,start,SPLITE_FLAG,1);
            String operate = getStrFromBytes(logs,start,end);
            start = end;
            switch (operate){
                case "I":
                    insertOperate(schemaTable, logs, start, logEnd,startId,endId);
                    break;
                case "U":
                    updateOperate(logs, start, logEnd,startId,endId);
                    break;
                case "D":
                    deleteOperate(schemaTable, logs, position, logEnd);
                    break;
                default:
                    throw new IOException("error");
            }
            if (preLogEnd<=0)
                break;
            logEnd = preLogEnd;
        }

        return lastLogs;
    }


    private String getStrFromBytes(byte[] logs,int start,int end){
        byte[] schemaBytes = new byte[end-start-1];
        System.arraycopy(logs,start+1,schemaBytes,0,end-start-1);
        return new String(schemaBytes);
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
        int index = 0;
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

    private void insertOperate(String schemaTable,byte[] logs,int start,int end,int startId,int endId)throws IOException{
        byte[] idBytes = findSingleStr(logs,start,SPLITE_FLAG,3);
        int id = Integer.parseInt(new String(idBytes));
        if (id==1){
            System.out.println("");
        }
        if ((id<startId||id>endId)&&!addList.contains(id)){
           return;
        }

        ByteBuffer buffer = (ByteBuffer) resultBuffer.position((id-startId)*20);
        buffer.putInt(id);
        for (int n = 0;;n=n+2){
            if (position+2<end){
                byte tag = logs[position+3];
                byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                insertTag(id,tag,tmp,startId);
            }else {
                break;
            }
        }
    }
    private void insertTag(int id,byte tag,byte[] bytes,int startId){
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
                if (resultBuffer.get((id-startId)*20+16)==EMPTY_FLAG){
                    ByteBuffer buffer4 = (ByteBuffer) resultBuffer.position((id-startId)*20+16);
                    buffer4.putInt(Integer.parseInt(new String(bytes)));
                }
                byte[] tmp = resultBuffer.array();
                break;
        }
    }
    private void updateTag(int id,byte tag,byte[] bytes,int startId){
        switch (tag){
            case FIRST_FLAG:
                ByteBuffer buffer1 = (ByteBuffer) resultBuffer.position((id-startId)*20+4);
                buffer1.put(bytes);
                break;
            case LAST_FLAG:
                ByteBuffer buffer2 = (ByteBuffer) resultBuffer.position((id-startId)*20+7);
                buffer2.put(bytes);
                break;
            case SEX_FLAG:
                ByteBuffer buffer3 = (ByteBuffer) resultBuffer.position((id-startId)*20+13);
                buffer3.put(bytes);
                break;
            case SCORE_FLAG:
                ByteBuffer buffer4 = (ByteBuffer) resultBuffer.position((id-startId)*20+16);
                buffer4.putInt(Integer.parseInt(new String(bytes)));
                break;
        }
    }
    private void updateOperate(byte[] logs,int start,int end,int startId,int endId){
       int lastId = Integer.parseInt(new String(findSingleStr(logs,start,SPLITE_FLAG,2)));
       int id = Integer.parseInt(new String(findSingleStr(logs,position,SPLITE_FLAG,1)));
       if (lastId==id){
           if ((id<startId||id>endId)&&!addList.contains(id)){
               return;
           }
           if (id>=startId&&id<=endId&&!deleteList.contains(id)){
               for (;position+2<end;){
                   byte tag = logs[position+3];
                   byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                   updateTag(id,tag,tmp,startId);
               }
           }else if ((id<startId||id>endId)&&addList.contains(id)){
               for (;position+2<end;){
                   byte tag = logs[position+1];
                   byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                   updateTag(addMap.get(id),tag,tmp,startId);
               }
           }
       }else {
           if (lastId<=endId&&lastId>=startId){
               if (id<startId||id>endId){ //范围之内改到范围之外　删除范围之内的值
                   deleteList.add(lastId);
               }else {                      //范围之内改到范围之内　删除范围之内的值，添加范围之内的值
                   deleteList.add(lastId);
                   deleteList.remove(deleteList.indexOf(id));
               }
           }else {
               if (id>startId&&id<endId){   //范围之外改到范围之内　添加范围之外的值
                   addList.add(lastId);
                   addMap.put(lastId,id);
               }
           }
       }
    }
    private void deleteOperate(String schemaTable,byte[] logs,int start,int end){

    }

    public static void main(String[] args) throws IOException{
        LogStore handler = new LogStore();
        String file = "/home/tuzhenyu/tmp/canal_data/canal1.txt";
        long startConsumer = System.currentTimeMillis();
        handler.pullBytesFormFile(file,"middleware5","student",0,1000);
        long endConsumer = System.currentTimeMillis();
        System.out.println(endConsumer-startConsumer);
    }

    private void flush(String schemaTable) throws IOException{
        String fileName = Constants.MIDDLE_HOME+"/"+schemaTable+".txt";
        FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel();
        ByteBuffer buffer = (ByteBuffer)resultBuffer.position(0);
        channel.write(buffer);
    }

    private boolean compareTo(String str1,String str2){
        int length1 = str1.length();
        int length2 = str2.length();
        if (length1<length2)
            return false;
        else if (length1>length2)
            return true;
        else {
            return str1.compareTo(str2)>0;
        }
    }
}
