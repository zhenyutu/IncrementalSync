package com.alibaba.middleware.race.sync;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created by tuzhenyu on 17-6-8.
 * @author tuzhenyu
 */
public class FileReader {
    public static void main(String[] args) {
//        try {
//            FileInputStream in = new FileInputStream("/home/tuzhenyu/work/middlewareTester/middle/middleware5|student.txt");
//            FileChannel fc = in.getChannel();
//            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
//            byte[] all = new byte[700];
////            buffer.get(all);
////            System.out.println(Arrays.toString(all));
//            byte[] tmp = new byte[3];
//            int i = 0;
//
//            while (buffer.hasRemaining()){
//                int id = buffer.getInt();
//                System.out.println(id);
//                buffer.get(tmp);
//                System.out.println(new String(tmp));
//                buffer.get(tmp);
//                System.out.println(new String(tmp));
//                buffer.get(tmp);
//                System.out.println(new String(tmp));
//                buffer.get(tmp);
//                System.out.println(new String(tmp));
//                int score = buffer.getInt();
//                System.out.println(score);
//                i++;
//                if (i>100)
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        byte[] tmp = {124, 109, 121, 115, 113, 108, 45, 98, 105, 110, 46, 48, 48, 48, 48, 49, 54, 57, 49, 56, 49, 56, 50, 55, 48, 48, 124, 49, 52, 57, 54, 55, 51, 48, 48, 55, 48, 48, 48, 48, 124, 109, 105, 100, 100, 108, 101, 119, 97, 114, 101, 124, 115, 116, 117, 100, 101, 110, 116, 124, 85, 124, 105, 100, 58, 49, 58, 49, 124, 49, 52, 48, 56, 57, 50, 49, 124, 49, 52, 48, 56, 57, 50, 49, 124, 115, 99, 111, 114, 101, 58, 49, 58, 48, 124, 54, 49, 50, 124, 49, 53, 55, 124};
        System.out.println(new String(tmp));
    }
}
