package com.alibaba.middleware.race.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    private ArrayList<Long> addList = new ArrayList<>();
    private Map<Long,Integer> addMap = new HashMap<>();
    private boolean[] finishArr = null;
    private byte[] clear = new byte[20];

    public void pullBytesFormFile(String file,String schema,String table,int start,int end) throws IOException {
        logger.info("get into the pullBytesFormFile");
        resultBuffer = ByteBuffer.allocate((end-start+1)*20);
        finishArr = new boolean[end-start+1];
        byte[] lastLogs = null;
        byte[] logs = null;
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
        operate(logs,schemaTable,-2,lastLogs.length,start,end);
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
            if (preLogEnd==logEnd)
                break;
            operate(logs,schemaTableName,preLogEnd,logEnd,startId,endId);
            logEnd = preLogEnd;
        }

        return lastLogs;
    }

    private void operate(byte[] logs,String schemaTableName,int preLogEnd,int logEnd,int startId,int endId)throws IOException{
        int start = findFirstByte(logs,preLogEnd+2,SPLITE_FLAG,2);
        int end = findFirstByte(logs,start,SPLITE_FLAG,2);
        String schemaTable = getStrFromBytes(logs,start,end);
        if (!schemaTableName.equals(schemaTable)){
            return;
        }
        start = end;
        end = findFirstByte(logs,start,SPLITE_FLAG,1);
        String operate = getStrFromBytes(logs,start,end);
        start = end;
        logger.info("schemaTable: "+schemaTable+" operate: "+operate);
        switch (operate){
            case "I":
                insertOperate(logs, start, logEnd,startId,endId);
                break;
            case "U":
                updateOperate(logs, start, logEnd,startId,endId);
                break;
            case "D":
                deleteOperate(logs, start, logEnd,startId,endId);
                break;
            default:
                logger.info("error!");
        }
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
        if (id<startId||id>endId){
            if (addList.contains(id)){
                insert(logs,addMap.get(id),end,startId);
            }
        }else {
            if (finishArr[(int)id-startId])
                return;
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
           if (id>=startId&&id<=endId&&!finishArr[(int)id-startId]){ //范围之内且未进行最终操作
               for (;position+2<end;){
                   byte tag = logs[position+3];
                   byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                   updateTag((int)id,tag,tmp,startId);
               }
           }else if ((id<startId||id>endId)&&addList.contains(id)){ //范围之外，映射进入范围之内
               for (;position+2<end;){
                   byte tag = logs[position+3];
                   byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                   updateTag(addMap.get(id),tag,tmp,startId);
               }
           }
       }else {
           if (lastId<=endId&&lastId>=startId){
               if ((id<startId||id>endId)&&!addList.contains(id)){ //范围之内改到范围之外　删除范围之内的值
                   finishArr[(int)lastId-startId] = true;
               }else {                      //范围之内改到范围之内　删除范围之内的值，添加范围之内的值
                   for (;position+2<end;){
                       byte tag = logs[position+3];
                       byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                       updateTag((int)id,tag,tmp,startId);
                   }
                   finishArr[(int)lastId-startId] = true;
                   finishArr[(int)lastId-startId] = false;
               }
           }else {
               if (id>=startId&&id<=endId){   //范围之外改到范围之内　添加范围之外的值
                   for (;position+2<end;){
                       byte tag = logs[position+3];
                       byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                       updateTag((int)id,tag,tmp,startId);
                   }
                   addList.add(lastId);
                   addMap.put(lastId,(int)id);
               }else {
                   if (addList.contains(id)){
                       for (;position+2<end;){
                           byte tag = logs[position+3];
                           byte[] tmp = findSingleStr(logs,position,SPLITE_FLAG,3);
                           updateTag(addMap.get(id),tag,tmp,startId);
                       }
                       addList.add(lastId);
                       addMap.put(lastId,addMap.get(id));
                       addList.remove(id);
                       addMap.remove(id);
                   }
               }
           }
       }
    }
    private void deleteOperate(byte[] logs,int start,int end,int startId,int endId){
        long lastId = Long.parseLong(new String(findSingleStr(logs,start,SPLITE_FLAG,2)));
        if (lastId>startId&&lastId<endId&&!finishArr[(int)lastId-startId])
            finishArr[(int) lastId-startId] = true;
//        ByteBuffer buffer = (ByteBuffer) resultBuffer.position((lastId-startId)*20);
//        buffer.put(clear);
    }

    public static void main(String[] args) throws IOException{
        LogStore handler = new LogStore();
        String file = "/home/tuzhenyu/tmp/canal_data/1/canal.txt";
//        String file = "/home/tuzhenyu/tmp/canal_data/canal2.txt";
        long startConsumer = System.currentTimeMillis();
        handler.pullBytesFormFile(file,"middleware5","student",100,200);
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
