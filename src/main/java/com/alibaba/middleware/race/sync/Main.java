package com.alibaba.middleware.race.sync;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created by tuzhenyu on 17-6-5.
 * @author tuzhenyu
 */
public class Main {
    public static void main(String[] args) throws Exception{
//        byte[] str = {124, 109, 121, 115, 113, 108, 45, 98, 105, 110, 46, 48, 48, 48, 48, 49, 50, 57, 56, 48, 53, 52, 50, 50, 52, 54, 124, 49, 52, 57, 54, 55, 50, 48, 54, 51, 55, 48, 48, 48, 124, 109, 105, 100, 100, 108, 101, 119, 97, 114, 101, 124, 115, 116, 117, 100, 101, 110, 116, 124, 73, 124, 105, 100, 58, 49, 58, 49, 124, 78, 85, 76, 76, 124, 49, 124, 102, 105, 114, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -67, -83, 124, 108, 97, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -25, -108, -80, -25, -108, -80, 124, 115, 101, 120, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -25, -108, -73, 124, 115, 99, 111, 114, 101, 58, 49, 58, 48, 124, 78, 85, 76, 76, 124, 56, 54, 124, 10};
//        System.out.println(new String(str));

        File file = new File("/home/tuzhenyu/3.txt");
        FileChannel channel = new RandomAccessFile(file, "r").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());
        channel.read(buffer);
        System.out.println(Arrays.toString(buffer.array()));
    }

}
