package com.alibaba.middleware.race.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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

    private int position = 0;

    private Map<String,ByteBuffer> fileByteBuffer = new HashMap<>();
    private Map<String,FileChannel> fileChannel = new HashMap<>();


    public void pullBytesFormFile(String file,String schema,String table,String start,String end) throws IOException {
        logger.info("get into the pullBytesFormFile");
        byte[] lastLogs = null;
        byte[] logs;
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, Math.min(channel.size(), PAGE_SIZE));
        String schemaTable = schema+"|"+table;

        for (long i = 0; i < channel.size() ; i=i+PAGE_SIZE)
        {
            if (!buffer.hasRemaining())
            {
                buffer = channel.map(FileChannel.MapMode.READ_ONLY, i, Math.min(channel.size() - i , PAGE_SIZE));
            }
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            if (lastLogs != null){
                logs = new byte[lastLogs.length+bytes.length];
                System.arraycopy(lastLogs, 0, logs, 0, lastLogs.length);
                System.arraycopy(bytes, 0, logs, lastLogs.length, bytes.length);
            }else {
                logs = bytes;
            }
            lastLogs = getLogFromBytes(logs, schemaTable, start, end);
        }

        flush(schemaTable);
        channel.close();
    }

    private byte[] getLogFromBytes(byte[] logs,String schemaTableName,String startId,String endId) throws IOException{
        int start = 0, end ,LogEnd;
        byte[] lastLogs = null;
        while (true){
            LogEnd = findFirstByte(logs,start,END_FLAG,1);
            if (LogEnd == start){
                lastLogs = new byte[logs.length-start];
                System.arraycopy(logs,start,lastLogs,0,logs.length-start);
                break;
            }
            start = findFirstByte(logs,start,SPLITE_FLAG,2);
            end = findFirstByte(logs,start,SPLITE_FLAG,2);
            String schemaTable = getStrFromBytes(logs,start,end);
            if (!schemaTableName.equals(schemaTable)){
                start = LogEnd+1;
                continue;
            }
            start = end;
            end = findFirstByte(logs,start,SPLITE_FLAG,1);
            String operate = getStrFromBytes(logs,start,end);
            start = end;
            byte[] idBytes = findSingleStr(logs,start,SPLITE_FLAG,3);
            String id = new String(idBytes);
            if (compareTo(startId,id)||compareTo(id,endId)){
                start = LogEnd+1;
                continue;
            }
            logger.info("the log is:"+schemaTable+"-"+id);

            switch (operate){
                case "I":
                    insertOperate(schemaTable, idBytes, logs, LogEnd);
                    break;
                case "U":
                    updateOperate(schemaTable, logs, position, LogEnd);
                    break;
                case "D":
                    deleteOperate(schemaTable, logs, position, LogEnd);
                    break;
                default:
                    throw new IOException("error");
            }
            if (end>=logs.length-1)
                break;
            start = LogEnd+1;
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

    private void insertOperate(String schemaTable,byte[] idBytes,byte[] logs,int end)throws IOException{
        ByteBuffer buffer = fileByteBuffer.get(schemaTable);
        if (buffer==null){
            buffer = ByteBuffer.allocate(PAGE_SIZE);
            fileByteBuffer.put(schemaTable,buffer);
        }

        buffer.put(idBytes);
        for (int n = 3;;n=n+3){
            if (position+1<end){
                buffer.put(SPACE_FLAG);
                buffer.put(findSingleStr(logs,position,SPLITE_FLAG,3));
            }else {
                break;
            }
        }
        buffer.put(END_FLAG);
        if (buffer.remaining()<REMAINING_SIZE)
        {
            FileChannel channel = fileChannel.get(schemaTable);
            if (channel==null){
                String fileName = Constants.MIDDLE_HOME+"/"+schemaTable+".txt";
                channel = new RandomAccessFile(fileName, "rw").getChannel();
                fileChannel.put(schemaTable,channel);
            }

            buffer.flip();
            channel.write(buffer);
            buffer.clear();
        }
    }
    private void updateOperate(String schemaTable,byte[] logs,int start,int end){

    }
    private void deleteOperate(String schemaTable,byte[] logs,int start,int end){

    }

    public static void main(String[] args) throws IOException{
        LogStore handler = new LogStore();
        String file = "/home/tuzhenyu/tmp/canal_data/canal.txt";
        long startConsumer = System.currentTimeMillis();
        handler.pullBytesFormFile(file,"middleware3","student","10","10000");
        long endConsumer = System.currentTimeMillis();
        System.out.println(endConsumer-startConsumer);
    }

    private void flush(String schemaTable) throws IOException{
        for (Map.Entry entry : fileByteBuffer.entrySet()){
            String key = (String) entry.getKey();
            ByteBuffer buffer= fileByteBuffer.get(key);
            FileChannel channel = fileChannel.get(key);
            if (channel==null){
                String fileName = Constants.TESTER_HOME+"/"+schemaTable+".txt";
                channel = new RandomAccessFile(fileName, "rw").getChannel();
                fileChannel.put(schemaTable,channel);
            }
            buffer.flip();
            channel.write(buffer);
        }
    }

    private boolean compareTo(String str1,String str2){
        int length1 = str1.length();
        int length2 = str2.length();
        if (length1<length2)
            return false;
        else if (length1>length2)
            return true;
        else {
            return str1.compareTo(str2)>=0;
        }
    }
}
