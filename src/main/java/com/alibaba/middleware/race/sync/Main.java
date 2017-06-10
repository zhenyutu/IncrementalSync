package com.alibaba.middleware.race.sync;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by tuzhenyu on 17-6-5.
 * @author tuzhenyu
 */
public class Main {
    public static void main(String[] args) {
        byte[] str = {124, 109, 121, 115, 113, 108, 45, 98, 105, 110, 46, 48, 48, 48, 48, 49, 56, 52, 55, 48, 56, 53, 56, 53, 51, 50, 124, 49, 52, 57, 54, 56, 50, 56, 50, 49, 52, 48, 48, 48, 124, 109, 105, 100, 100, 108, 101, 119, 97, 114, 101, 53, 124, 115, 116, 117, 100, 101, 110, 116, 124, 73, 124, 105, 100, 58, 49, 58, 49, 124, 78, 85, 76, 76, 124, 49, 124, 102, 105, 114, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -112, -76, 124, 108, 97, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -28, -71, -99, 124, 115, 101, 120, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -91, -77, 124, 115, 99, 111, 114, 101, 58, 49, 58, 48, 124, 78, 85, 76, 76, 124, 54, 48, 124, 10};
        System.out.println(new String(str));
    }

}
