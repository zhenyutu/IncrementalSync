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
        try {
            FileInputStream in = new FileInputStream("/home/tuzhenyu/work/middlewareTester/middle/middleware5|student.txt");
            FileChannel fc = in.getChannel();
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            byte[] all = new byte[700];
//            buffer.get(all);
//            System.out.println(Arrays.toString(all));
            byte[] tmp = new byte[3];
            int i = 0;

            while (buffer.hasRemaining()){
                int id = buffer.getInt();
                System.out.println(id);
                buffer.get(tmp);
                System.out.println(new String(tmp));
                buffer.get(tmp);
                System.out.println(new String(tmp));
                buffer.get(tmp);
                System.out.println(new String(tmp));
                buffer.get(tmp);
                System.out.println(new String(tmp));
                int score = buffer.getInt();
                System.out.println(score);
                i++;
                if (i>30)
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        byte[] tmp = {-25,-85,-117};
//        byte[] tmp2 = {-26,-80,-111};
//        System.out.println(new String(tmp2));
    }
}
