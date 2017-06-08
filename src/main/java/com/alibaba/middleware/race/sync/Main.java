package com.alibaba.middleware.race.sync;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created by tuzhenyu on 17-6-5.
 * @author tuzhenyu
 */
public class Main {
    private static final int PAGE_SIZE = 4*1024;
    private static int position;

    public static void main(String[] args) throws IOException {

        String file = "/home/tuzhenyu/tmp/canal_data/test.txt";
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        MappedByteBuffer buffer;

        for (long i = channel.size(); i > 0 ; i=i-PAGE_SIZE)
        {
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, Math.max(i-PAGE_SIZE , 0), Math.min(i , PAGE_SIZE));
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            System.out.println(Arrays.toString(bytes));
            System.out.println(bytes.length);

//            System.out.println(findFirstByte(bytes,37,(byte)124));

            int start = bytes.length, end;
            start = findNextEnt(bytes,start,(byte)10);
            System.out.println(start);
            start = findFirstByte(bytes,start+2,(byte)124,2);
            System.out.println(start);
            end = findFirstByte(bytes,start,(byte)124,2);
            String schema = getResultStr(bytes,start,end);
            System.out.println(schema);
////            start = end;
////            end = findFirstByte(bytes,start,(byte)124,1);
////            String table = getResultStr(bytes,start,end);
////            System.out.println(table);
            start = end;
            end = findFirstByte(bytes,start,(byte)124,1);
            String operate = getResultStr(bytes,start,end);
            System.out.println(operate);
            start = end;
            String id = new String(findSingleStr(bytes,start,(byte)124,3));
            System.out.println(id);
////            position = end;
            end = findFirstByte(bytes,position,(byte)10,1);
            for (int n = 3;;n=n+3){
                if (position+1<end){
                    System.out.print(findSingleStr(bytes,position,(byte)124,3));
                    System.out.print(" ");
                }else {
                    break;
                }
            }
//            System.out.print(findSingleStr(bytes,end,(byte)124,3));
//            System.out.print(" ");
//            System.out.print(findSingleStr(bytes,end,(byte)124,6));
//            System.out.print(" ");
//            System.out.print(findSingleStr(bytes,end,(byte)124,9));
//            System.out.print(" ");
//            System.out.println(findSingleStr(bytes,end,(byte)124,12));
//            System.out.println(findFirstByte(bytes,end,(byte)10,1));

            break;
        }

        channel.close();
    }

    private static String getResultStr(byte[] logs,int start,int end){
        byte[] schemaBytes = new byte[end-start-1];
        System.arraycopy(logs,start+1,schemaBytes,0,end-start-1);
        return new String(schemaBytes);
    }

    private static int findFirstByte(byte[] logs,int start,byte value,int num){
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

    private static String findSingleStr(byte[] logs,int start,byte value,int num){
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
        return new String(schemaBytes);
    }

    private static int findNextEnt(byte[] logs,int start,byte value){
        int index;
        for (int i=start-2;;i--){
            if (logs[i] == value){
                index = i;
                break;
            }
        }
        return index;
    }

//    private static boolean compareTo(String str1,String str2){
//        int length1 = str1.length();
//        int length2 = str2.length();
//        if (length1<length2)
//            return false;
//        else if (length1>length2)
//            return true;
//        else {
//            return str1.compareTo(str2)>=0;
//        }
//    }
}
