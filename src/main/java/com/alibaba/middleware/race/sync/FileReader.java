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

        byte[] tmp = {124, 109, 121, 115, 113, 108, 45, 98, 105, 110, 46, 48, 48, 48, 48, 49, 50, 57, 56, 48, 53, 52, 50, 50, 52, 54, 124, 49, 52, 57, 54, 55, 50, 48, 54, 51, 55, 48, 48, 48, 124, 109, 105, 100, 100, 108, 101, 119, 97, 114, 101, 124, 115, 116, 117, 100, 101, 110, 116, 124, 73, 124, 105, 100, 58, 49, 58, 49, 124, 78, 85, 76, 76, 124, 49, 124, 102, 105, 114, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -67, -83, 124, 108, 97, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -25, -108, -80, -25, -108, -80, 124, 115, 101, 120, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -25, -108, -73, 124, 115, 99, 111, 114, 101, 58, 49, 58, 48, 124, 78, 85, 76, 76, 124, 56, 54, 124, 10, 124, 109, 121, 115, 113, 108, 45, 98, 105, 110, 46, 48, 48, 48, 48, 49, 50, 57, 56, 48, 53, 52, 50, 50, 52, 55, 124, 49, 52, 57, 54, 55, 50, 48, 54, 51, 55, 48, 48, 48, 124, 109, 105, 100, 100, 108, 101, 119, 97, 114, 101, 124, 115, 116, 117, 100, 101, 110, 116, 124, 73, 124, 105, 100, 58, 49, 58, 49, 124, 78, 85, 76, 76, 124, 50, 124, 102, 105, 114, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -111, -88, 124, 108, 97, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -28, -70, -108, -24, -81, -102, 124, 115, 101, 120, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -25, -108, -73, 124, 115, 99, 111, 114, 101, 58, 49, 58, 48, 124, 78, 85, 76, 76, 124, 55, 48, 124, 10, 124, 109, 121, 115, 113, 108, 45, 98, 105, 110, 46, 48, 48, 48, 48, 49, 50, 57, 56, 48, 53, 52, 50, 50, 52, 56, 124, 49, 52, 57, 54, 55, 50, 48, 54, 51, 55, 48, 48, 48, 124, 109, 105, 100, 100, 108, 101, 119, 97, 114, 101, 124, 115, 116, 117, 100, 101, 110, 116, 124, 73, 124, 105, 100, 58, 49, 58, 49, 124, 78, 85, 76, 76, 124, 51, 124, 102, 105, 114, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -112, -76, 124, 108, 97, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -25, -101, -118, -28, -72, -119, 124, 115, 101, 120, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -91, -77, 124, 115, 99, 111, 114, 101, 58, 49, 58, 48, 124, 78, 85, 76, 76, 124, 55, 50, 124, 10, 124, 109, 121, 115, 113, 108, 45, 98, 105, 110, 46, 48, 48, 48, 48, 49, 50, 57, 56, 48, 53, 52, 50, 50, 52, 57, 124, 49, 52, 57, 54, 55, 50, 48, 54, 51, 55, 48, 48, 48, 124, 109, 105, 100, 100, 108, 101, 119, 97, 114, 101, 124, 115, 116, 117, 100, 101, 110, 116, 124, 73, 124, 105, 100, 58, 49, 58, 49, 124, 78, 85, 76, 76, 124, 52, 124, 102, 105, 114, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -23, -125, -111, 124, 108, 97, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -123, -78, -26, -120, -112, 124, 115, 101, 120, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -27, -91, -77, 124, 115, 99, 111, 114, 101, 58, 49, 58, 48, 124, 78, 85, 76, 76, 124, 57, 50, 124, 10, 124, 109, 121, 115, 113, 108, 45, 98, 105, 110, 46, 48, 48, 48, 48, 49, 50, 57, 56, 48, 53, 52, 50, 50, 53, 48, 124, 49, 52, 57, 54, 55, 50, 48, 54, 51, 55, 48, 48, 48, 124, 109, 105, 100, 100, 108, 101, 119, 97, 114, 101, 124, 115, 116, 117, 100, 101, 110, 116, 124, 73, 124, 105, 100, 58, 49, 58, 49, 124, 78, 85, 76, 76, 124, 53, 124, 102, 105, 114, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -23, -126, -71, 124, 108, 97, 115, 116, 95, 110, 97, 109, 101, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -26, -103, -74, 124, 115, 101, 120, 58, 50, 58, 48, 124, 78, 85, 76, 76, 124, -25, -108, -73, 124, 115, 99, 111, 114, 101, 58, 49, 58, 48, 124};
        System.out.println(new String(tmp));
    }
}
