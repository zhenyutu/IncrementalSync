package com.alibaba.middleware.race.sync;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by tuzhenyu on 17-6-8.
 * @author tuzhenyu
 */
public class FileReader {
    public static void main(String[] args) throws Exception{
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

//        byte[] tmp = {54, 48, 49, 9, -25, -114, -117, 9, -25, -108, -100, 9, -25, -108, -73, 9, 51, 51, 54, 10, 51, 51, 54, 9, 0, 0, 2, 9, 91, -27, -68, -96, -28, -72, 9, -119, 0, 0, 9, 49, 53, 49, 55, 54, 56, 56, 55, 10, 49, 48, 48, 9, 0, 0, 0, 9, 0, 0, 0, 9, 0, 0, 0, 9, 48, 10, 54, 48, 53, 9, -23, -110, -79, 9, -27, -112, -101, -23, -99, -103, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 55, 9, -27, -120, -104, 9, -23, -69, -114, -28, -70, -108, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 57, 9, -27, -66, -112, 9, -27, -123, -85, -28, -72, -103, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 49, 9, -27, -68, -96, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 51, 9, -25, -114, -117, 9, -23, -109, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 49, 53, 9, -27, -66, -112, 9, -27, -112, -115, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 55, 9, -27, -111, -88, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 57, 9, -27, -66, -112, 9, -28, -72, -103, -26, -80, -111, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 49, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 55, 57, 10, 54, 50, 51, 9, -26, -99, -114, 9, -23, -69, -114, -26, -103, -81, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 53, 9, -23, -110, -79, 9, -26, -103, -81, -24, -114, -119, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 50, 55, 9, -27, -66, -112, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 57, 9, -26, -99, -88, 9, -23, -69, -114, 9, -27, -91, -77, 9, 51, 53, 10, 54, 51, 49, 9, -28, -66, -81, 9, -28, -70, -84, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 51, 51, 9, -27, -66, -112, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 53, 9, -23, -110, -79, 9, -28, -71, -103, -28, -70, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 55, 9, -27, -68, -96, 9, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 57, 9, -27, -66, -112, 9, -28, -70, -116, 9, -27, -91, -77, 9, 55, 57, 10, 54, 52, 49, 9, -26, -99, -88, 9, -25, -108, -78, -26, -127, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 52, 51, 9, -23, -125, -111, 9, -27, -122, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 53, 9, -27, -66, -112, 9, -23, -109, -83, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 55, 9, -27, -112, -76, 9, -27, -118, -79, -28, -72, -125, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 57, 9, -24, -75, -75, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 49, 9, -27, -66, -112, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 51, 9, -27, -67, -83, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 53, 9, -23, -110, -79, 9, -28, -71, -103, -23, -86, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 55, 9, -27, -66, -112, 9, -26, -107, -113, -25, -85, -117, 9, -27, -91, -77, 9, 55, 57, 10, 54, 53, 57, 9, -23, -104, -82, 9, -27, -92, -87, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 49, 9, -28, -66, -81, 9, -28, -71, -103, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 51, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 51, 53, 10, 54, 54, 53, 9, -23, -110, -79, 9, -28, -70, -116, -26, -107, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 55, 9, -26, -79, -97, 9, -26, -103, -74, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 54, 57, 9, -27, -66, -112, 9, -28, -72, -128, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 49, 9, -26, -79, -97, 9, -28, -70, -116, -27, -123, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 51, 9, -24, -75, -75, 9, -27, -118, -79, -27, -118, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 53, 9, -27, -66, -112, 9, -28, -72, -103, 9, -27, -91, -77, 9, 55, 57, 10, 54, 55, 55, 9, -25, -114, -117, 9, -28, -70, -108, -23, -109, -83, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 57, 9, -23, -126, -71, 9, -28, -72, -103, -27, -118, -79, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 49, 9, -27, -66, -112, 9, -25, -108, -100, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 51, 9, -23, -104, -82, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 53, 9, -23, -110, -79, 9, -26, -107, -113, -25, -108, -80, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 55, 9, -27, -66, -112, 9, -27, -112, -101, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 57, 9, -27, -68, -96, 9, -25, -85, -117, -27, -97, -114, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 49, 9, -23, -110, -79, 9, -27, -92, -87, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 51, 9, -27, -66, -112, 9, -27, -112, -101, -24, -114, -119, 9, -27, -91, -77, 9, 55, 57, 10, 54, 57, 53, 9, -23, -110, -79, 9, -25, -108, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 57, 55, 9, -24, -75, -75, 9, -26, -103, -81, 9, -27, -91, -77, 9, 51, 53, 10, 54, 57, 57, 9, -27, -66, -112, 9, -26, -104, -114, -28, -65, -118, 9, -27, -91, -77, 9, 51, 51, 54, 10};
//        byte[] tmp = {53, 48, 49, 9, -27, -68, -96, 9, -28, -72, -119, 9, -25, -108, -73, 9, 48, 10, 53, 48, 51, 9, -27, -83, -103, 9, -27, -118, -79, -26, -80, -111, 9, -25, -108, -73, 9, 48, 10, 53, 48, 53, 9, -26, -97, -77, 9, -27, -97, -114, -27, -123, -78, 9, -25, -108, -73, 9, 48, 10, 53, 48, 55, 9, -27, -111, -88, 9, -24, -81, -102, -27, -112, -101, 9, -27, -91, -77, 9, 48, 10, 53, 48, 57, 9, -23, -104, -82, 9, -23, -109, -83, -28, -72, -125, 9, -25, -108, -73, 9, 48, 10, 53, 49, 49, 9, -23, -126, -71, 9, -28, -70, -84, 9, -27, -91, -77, 9, 48, 10, 53, 49, 51, 9, -27, -120, -104, 9, -24, -81, -102, 9, -27, -91, -77, 9, 48, 10, 53, 49, 53, 9, -27, -112, -76, 9, -27, -115, -127, 9, -25, -108, -73, 9, 48, 10, 53, 49, 55, 9, -23, -103, -120, 9, -28, -72, -127, -25, -108, -80, 9, -25, -108, -73, 9, 48, 10, 53, 49, 57, 9, -27, -120, -104, 9, -27, -123, -83, -27, -118, -101, 9, -27, -91, -77, 9, 48, 10, 53, 50, 49, 9, -26, -97, -77, 9, -27, -112, -115, 9, -27, -91, -77, 9, 48, 10, 53, 50, 51, 9, -23, -85, -104, 9, -28, -70, -116, 9, -25, -108, -73, 9, 48, 10, 53, 50, 53, 9, -28, -66, -81, 9, -26, -103, -74, 9, -25, -108, -73, 9, 48, 10, 53, 50, 55, 9, -27, -112, -76, 9, -27, -112, -115, -23, -99, -103, 9, -25, -108, -73, 9, 48, 10, 53, 50, 57, 9, -26, -98, -105, 9, -28, -71, -103, -27, -123, -85, 9, -25, -108, -73, 9, 48, 10, 53, 51, 49, 9, -27, -66, -112, 9, -28, -71, -99, -28, -70, -108, 9, -27, -91, -77, 9, 48, 10, 53, 51, 51, 9, -26, -99, -88, 9, -26, -103, -74, -27, -92, -87, 9, -25, -108, -73, 9, 48, 10, 53, 51, 53, 9, -28, -66, -81, 9, -27, -92, -87, 9, -27, -91, -77, 9, 48, 10, 53, 51, 55, 9, -24, -75, -75, 9, -23, -99, -103, -27, -122, -101, 9, -25, -108, -73, 9, 48, 10, 53, 51, 57, 9, -27, -66, -112, 9, -28, -72, -125, 9, -27, -91, -77, 9, 48, 10, 53, 52, 49, 9, -27, -67, -83, 9, -28, -69, -106, 9, -27, -91, -77, 9, 48, 10, 53, 52, 51, 9, -26, -99, -114, 9, -27, -101, -101, -25, -85, -117, 9, -27, -91, -77, 9, 48, 10, 53, 52, 53, 9, -26, -79, -97, 9, -27, -92, -87, -23, -99, -103, 9, -27, -91, -77, 9, 48, 10, 53, 52, 55, 9, -27, -120, -104, 9, -27, -115, -127, 9, -27, -91, -77, 9, 48, 10, 53, 52, 57, 9, -23, -126, -71, 9, -23, -86, -113, 9, -25, -108, -73, 9, 48, 10, 53, 53, 49, 9, -23, -104, -82, 9, -28, -72, -103, 9, -27, -91, -77, 9, 48, 10, 53, 53, 51, 9, -23, -85, -104, 9, -27, -122, -101, -27, -123, -85, 9, -25, -108, -73, 9, 48, 10, 53, 53, 53, 9, -27, -67, -83, 9, -27, -97, -114, -23, -109, -83, 9, -25, -108, -73, 9, 48, 10, 53, 53, 55, 9, -27, -111, -88, 9, -25, -108, -78, -27, -122, -101, 9, -25, -108, -73, 9, 48, 10, 53, 53, 57, 9, -23, -110, -79, 9, -27, -123, -83, 9, -27, -91, -77, 9, 48, 10, 53, 54, 49, 9, -27, -83, -103, 9, -28, -71, -99, -23, -86, -113, 9, -25, -108, -73, 9, 48, 10, 53, 54, 51, 9, -27, -111, -88, 9, -25, -108, -100, -27, -97, -114, 9, -27, -91, -77, 9, 48, 10, 53, 54, 53, 9, -26, -79, -97, 9, -24, -81, -102, -28, -72, -127, 9, -27, -91, -77, 9, 48, 10, 53, 54, 55, 9, -27, -83, -103, 9, -28, -70, -84, 9, -27, -91, -77, 9, 48, 10, 53, 54, 57, 9, -26, -79, -97, 9, -28, -71, -103, 9, -27, -91, -77, 9, 48, 10, 53, 55, 49, 9, -27, -112, -76, 9, -27, -122, -101, -28, -72, -128, 9, -25, -108, -73, 9, 48, 10, 53, 55, 51, 9, -26, -99, -88, 9, -28, -71, -99, 9, -27, -91, -77, 9, 48, 10, 53, 55, 53, 9, -23, -85, -104, 9, -27, -92, -87, -25, -108, -80, 9, -27, -91, -77, 9, 48, 10, 53, 55, 55, 9, -27, -111, -88, 9, -27, -123, -78, 9, -27, -91, -77, 9, 48, 10, 53, 55, 57, 9, -23, -85, -104, 9, -28, -70, -70, 9, -25, -108, -73, 9, 48, 10, 53, 56, 49, 9, -23, -104, -82, 9, -28, -70, -84, -27, -123, -78, 9, -25, -108, -73, 9, 48, 10, 53, 56, 51, 9, -27, -111, -88, 9, -27, -92, -87, -25, -108, -100, 9, -25, -108, -73, 9, 48, 10, 53, 56, 53, 9, -27, -111, -88, 9, -26, -107, -113, -25, -85, -117, 9, -27, -91, -77, 9, 48, 10, 53, 56, 55, 9, -24, -75, -75, 9, -25, -108, -80, 9, -27, -91, -77, 9, 48, 10, 53, 56, 57, 9, -27, -66, -112, 9, -28, -70, -70, 9, -25, -108, -73, 9, 48, 10, 53, 57, 49, 9, -27, -120, -104, 9, -23, -99, -103, -23, -69, -114, 9, -25, -108, -73, 9, 48, 10, 53, 57, 51, 9, -28, -66, -81, 9, -23, -99, -103, -26, -103, -81, 9, -25, -108, -73, 9, 48, 10, 53, 57, 53, 9, -26, -99, -114, 9, -23, -109, -83, -28, -66, -99, 9, -27, -91, -77, 9, 48, 10, 53, 57, 55, 9, -25, -114, -117, 9, -24, -114, -119, -26, -80, -111, 9, -27, -91, -77, 9, 48, 10, 53, 57, 57, 9, -23, -85, -104, 9, -28, -72, -118, 9, -27, -91, -77, 9, 48, 10};
//        byte[] tmp = {54, 48, 49, 9, -25, -114, -117, 9, -25, -108, -100, 9, -25, -108, -73, 9, 51, 51, 54, 10, 51, 51, 54, 9, 0, 0, 2, 9, 91, -27, -68, -96, -28, -72, 9, -119, 0, 0, 9, 49, 53, 49, 55, 54, 56, 56, 55, 10, 49, 48, 48, 9, 0, 0, 0, 9, 0, 0, 0, 9, 0, 0, 0, 9, 48, 10, 54, 48, 53, 9, -23, -110, -79, 9, -27, -112, -101, -23, -99, -103, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 55, 9, -27, -120, -104, 9, -23, -69, -114, -28, -70, -108, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 57, 9, -27, -66, -112, 9, -27, -123, -85, -28, -72, -103, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 49, 9, -27, -68, -96, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 51, 9, -25, -114, -117, 9, -23, -109, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 49, 53, 9, -27, -66, -112, 9, -27, -112, -115, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 55, 9, -27, -111, -88, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 57, 9, -27, -66, -112, 9, -28, -72, -103, -26, -80, -111, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 49, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 55, 57, 10, 54, 50, 51, 9, -26, -99, -114, 9, -23, -69, -114, -26, -103, -81, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 53, 9, -23, -110, -79, 9, -26, -103, -81, -24, -114, -119, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 50, 55, 9, -27, -66, -112, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 57, 9, -26, -99, -88, 9, -23, -69, -114, 9, -27, -91, -77, 9, 51, 53, 10, 54, 51, 49, 9, -28, -66, -81, 9, -28, -70, -84, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 51, 51, 9, -27, -66, -112, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 53, 9, -23, -110, -79, 9, -28, -71, -103, -28, -70, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 55, 9, -27, -68, -96, 9, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 57, 9, -27, -66, -112, 9, -28, -70, -116, 9, -27, -91, -77, 9, 55, 57, 10, 54, 52, 49, 9, -26, -99, -88, 9, -25, -108, -78, -26, -127, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 52, 51, 9, -23, -125, -111, 9, -27, -122, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 53, 9, -27, -66, -112, 9, -23, -109, -83, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 55, 9, -27, -112, -76, 9, -27, -118, -79, -28, -72, -125, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 57, 9, -24, -75, -75, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 49, 9, -27, -66, -112, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 51, 9, -27, -67, -83, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 53, 9, -23, -110, -79, 9, -28, -71, -103, -23, -86, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 55, 9, -27, -66, -112, 9, -26, -107, -113, -25, -85, -117, 9, -27, -91, -77, 9, 55, 57, 10, 54, 53, 57, 9, -23, -104, -82, 9, -27, -92, -87, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 49, 9, -28, -66, -81, 9, -28, -71, -103, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 51, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 51, 53, 10, 54, 54, 53, 9, -23, -110, -79, 9, -28, -70, -116, -26, -107, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 55, 9, -26, -79, -97, 9, -26, -103, -74, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 54, 57, 9, -27, -66, -112, 9, -28, -72, -128, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 49, 9, -26, -79, -97, 9, -28, -70, -116, -27, -123, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 51, 9, -24, -75, -75, 9, -27, -118, -79, -27, -118, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 53, 9, -27, -66, -112, 9, -28, -72, -103, 9, -27, -91, -77, 9, 55, 57, 10, 54, 55, 55, 9, -25, -114, -117, 9, -28, -70, -108, -23, -109, -83, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 57, 9, -23, -126, -71, 9, -28, -72, -103, -27, -118, -79, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 49, 9, -27, -66, -112, 9, -25, -108, -100, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 51, 9, -23, -104, -82, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 53, 9, -23, -110, -79, 9, -26, -107, -113, -25, -108, -80, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 55, 9, -27, -66, -112, 9, -27, -112, -101, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 57, 9, -27, -68, -96, 9, -25, -85, -117, -27, -97, -114, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 49, 9, -23, -110, -79, 9, -27, -92, -87, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 51, 9, -27, -66, -112, 9, -27, -112, -101, -24, -114, -119, 9, -27, -91, -77};
//        byte[] tmp = {54, 48, 49, 9, -25, -114, -117, 9, -25, -108, -100, 9, -25, -108, -73, 9, 51, 51, 54, 10, 51, 51, 54, 9, 0, 0, 2, 9, 91, -27, -68, -96, -28, -72, 9, -119, 0, 0, 9, 49, 53, 49, 55, 54, 56, 56, 55, 10, 49, 48, 48, 9, 0, 0, 0, 9, 0, 0, 0, 9, 0, 0, 0, 9, 48, 10, 54, 48, 53, 9, -23, -110, -79, 9, -27, -112, -101, -23, -99, -103, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 55, 9, -27, -120, -104, 9, -23, -69, -114, -28, -70, -108, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 57, 9, -27, -66, -112, 9, -27, -123, -85, -28, -72, -103, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 49, 9, -27, -68, -96, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 51, 9, -25, -114, -117, 9, -23, -109, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 49, 53, 9, -27, -66, -112, 9, -27, -112, -115, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 55, 9, -27, -111, -88, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 57, 9, -27, -66, -112, 9, -28, -72, -103, -26, -80, -111, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 49, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 55, 57, 10, 54, 50, 51, 9, -26, -99, -114, 9, -23, -69, -114, -26, -103, -81, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 53, 9, -23, -110, -79, 9, -26, -103, -81, -24, -114, -119, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 50, 55, 9, -27, -66, -112, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 57, 9, -26, -99, -88, 9, -23, -69, -114, 9, -27, -91, -77, 9, 51, 53, 10, 54, 51, 49, 9, -28, -66, -81, 9, -28, -70, -84, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 51, 51, 9, -27, -66, -112, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 53, 9, -23, -110, -79, 9, -28, -71, -103, -28, -70, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 55, 9, -27, -68, -96, 9, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 57, 9, -27, -66, -112, 9, -28, -70, -116, 9, -27, -91, -77, 9, 55, 57, 10, 54, 52, 49, 9, -26, -99, -88, 9, -25, -108, -78, -26, -127, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 52, 51, 9, -23, -125, -111, 9, -27, -122, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 53, 9, -27, -66, -112, 9, -23, -109, -83, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 55, 9, -27, -112, -76, 9, -27, -118, -79, -28, -72, -125, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 57, 9, -24, -75, -75, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 49, 9, -27, -66, -112, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 51, 9, -27, -67, -83, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 53, 9, -23, -110, -79, 9, -28, -71, -103, -23, -86, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 55, 9, -27, -66, -112, 9, -26, -107, -113, -25, -85, -117, 9, -27, -91, -77, 9, 55, 57, 10, 54, 53, 57, 9, -23, -104, -82, 9, -27, -92, -87, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 49, 9, -28, -66, -81, 9, -28, -71, -103, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 51, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 51, 53, 10, 54, 54, 53, 9, -23, -110, -79, 9, -28, -70, -116, -26, -107, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 55, 9, -26, -79, -97, 9, -26, -103, -74, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 54, 57, 9, -27, -66, -112, 9, -28, -72, -128, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 49, 9, -26, -79, -97, 9, -28, -70, -116, -27, -123, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 51, 9, -24, -75, -75, 9, -27, -118, -79, -27, -118, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 53, 9, -27, -66, -112, 9, -28, -72, -103, 9, -27, -91, -77, 9, 55, 57, 10, 54, 55, 55, 9, -25, -114, -117, 9, -28, -70, -108, -23, -109, -83, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 57, 9, -23, -126, -71, 9, -28, -72, -103, -27, -118, -79, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 49, 9, -27, -66, -112, 9, -25, -108, -100, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 51, 9, -23, -104, -82, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 53, 9, -23, -110, -79, 9, -26, -107, -113, -25, -108, -80, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 55, 9, -27, -66, -112, 9, -27, -112, -101, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 57, 9, -27, -68, -96, 9, -25, -85, -117, -27, -97, -114, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 49, 9, -23, -110, -79, 9, -27, -92, -87, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 51, 9, -27, -66, -112, 9, -27, -112, -101, -24, -114, -119, 9, -27, -91, -77, 9, 55, 57, 10, 54, 57, 53, 9, -23, -110, -79, 9, -25, -108, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 57, 55, 9, -24, -75, -75, 9, -26, -103, -81, 9, -27, -91, -77, 9, 51, 53, 10, 54, 57, 57, 9, -27, -66, -112, 9, -26, -104, -114, -28, -65, -118, 9, -27, -91, -77, 9, 51, 51, 54, 10};
        byte[] tmp = {54, 48, 49, 9, -25, -114, -117, 9, -25, -108, -100, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 48, 50, 9, -27, -68, -96, 9, -28, -72, -119, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 48, 53, 9, -23, -110, -79, 9, -27, -112, -101, -23, -99, -103, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 55, 9, -27, -120, -104, 9, -23, -69, -114, -28, -70, -108, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 57, 9, -27, -66, -112, 9, -27, -123, -85, -28, -72, -103, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 49, 9, -27, -68, -96, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 51, 9, -25, -114, -117, 9, -23, -109, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 49, 53, 9, -27, -66, -112, 9, -27, -112, -115, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 55, 9, -27, -111, -88, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 57, 9, -27, -66, -112, 9, -28, -72, -103, -26, -80, -111, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 49, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 55, 57, 10, 54, 50, 51, 9, -26, -99, -114, 9, -23, -69, -114, -26, -103, -81, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 53, 9, -23, -110, -79, 9, -26, -103, -81, -24, -114, -119, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 50, 55, 9, -27, -66, -112, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 57, 9, -26, -99, -88, 9, -23, -69, -114, 9, -27, -91, -77, 9, 51, 53, 10, 54, 51, 49, 9, -28, -66, -81, 9, -28, -70, -84, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 51, 51, 9, -27, -66, -112, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 53, 9, -23, -110, -79, 9, -28, -71, -103, -28, -70, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 55, 9, -27, -68, -96, 9, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 57, 9, -27, -66, -112, 9, -28, -70, -116, 9, -27, -91, -77, 9, 55, 57, 10, 54, 52, 49, 9, -26, -99, -88, 9, -25, -108, -78, -26, -127, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 52, 51, 9, -23, -125, -111, 9, -27, -122, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 53, 9, -27, -66, -112, 9, -23, -109, -83, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 55, 9, -27, -112, -76, 9, -27, -118, -79, -28, -72, -125, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 57, 9, -24, -75, -75, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 49, 9, -27, -66, -112, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 51, 9, -27, -67, -83, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 53, 9, -23, -110, -79, 9, -28, -71, -103, -23, -86, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 55, 9, -27, -66, -112, 9, -26, -107, -113, -25, -85, -117, 9, -27, -91, -77, 9, 55, 57, 10, 54, 53, 57, 9, -23, -104, -82, 9, -27, -92, -87, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 49, 9, -28, -66, -81, 9, -28, -71, -103, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 51, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 51, 53, 10, 54, 54, 53, 9, -23, -110, -79, 9, -28, -70, -116, -26, -107, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 55, 9, -26, -79, -97, 9, -26, -103, -74, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 54, 57, 9, -27, -66, -112, 9, -28, -72, -128, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 49, 9, -26, -79, -97, 9, -28, -70, -116, -27, -123, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 51, 9, -24, -75, -75, 9, -27, -118, -79, -27, -118, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 53, 9, -27, -66, -112, 9, -28, -72, -103, 9, -27, -91, -77, 9, 55, 57, 10, 54, 55, 55, 9, -25, -114, -117, 9, -28, -70, -108, -23, -109, -83, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 57, 9, -23, -126, -71, 9, -28, -72, -103, -27, -118, -79, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 49, 9, -27, -66, -112, 9, -25, -108, -100, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 51, 9, -23, -104, -82, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 53, 9, -23, -110, -79, 9, -26, -107, -113, -25, -108, -80, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 55, 9, -27, -66, -112, 9, -27, -112, -101, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 57, 9, -27, -68, -96, 9, -25, -85, -117, -27, -97, -114, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 49, 9, -23, -110, -79, 9, -27, -92, -87, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 51, 9, -27, -66, -112, 9, -27, -112, -101, -24, -114, -119, 9, -27, -91, -77, 9, 55, 57, 10, 54, 57, 53, 9, -23, -110, -79, 9, -25, -108, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 57, 55, 9, -24, -75, -75, 9, -26, -103, -81, 9, -27, -91, -77, 9, 51, 53, 10, 54, 57, 57, 9, -27, -66, -112, 9, -26, -104, -114, -28, -65, -118, 9, -27, -91, -77, 9, 51, 51, 54, 10};
        System.out.println(new String(tmp));

//        String str = "54, 48, 49, 9, -25, -114, -117, 9, -25, -108, -100, 9, -25, -108, -73, 9, 51, 51, 54, 10, 51, 51, 54, 9, 0, 0, 2, 9, 91, -27, -68, -96, -28, -72, 9, -119, 0, 0, 9, 49, 53, 49, 55, 54, 56, 56, 55, 10, 49, 48, 48, 9, 0, 0, 0, 9, 0, 0, 0, 9, 0, 0, 0, 9, 48, 10, 54, 48, 53, 9, -23, -110, -79, 9, -27, -112, -101, -23, -99, -103, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 55, 9, -27, -120, -104, 9, -23, -69, -114, -28, -70, -108, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 48, 57, 9, -27, -66, -112, 9, -27, -123, -85, -28, -72, -103, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 49, 9, -27, -68, -96, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 51, 9, -25, -114, -117, 9, -23, -109, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 49, 53, 9, -27, -66, -112, 9, -27, -112, -115, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 55, 9, -27, -111, -88, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 49, 57, 9, -27, -66, -112, 9, -28, -72, -103, -26, -80, -111, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 49, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 55, 57, 10, 54, 50, 51, 9, -26, -99, -114, 9, -23, -69, -114, -26, -103, -81, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 53, 9, -23, -110, -79, 9, -26, -103, -81, -24, -114, -119, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 50, 55, 9, -27, -66, -112, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 50, 57, 9, -26, -99, -88, 9, -23, -69, -114, 9, -27, -91, -77, 9, 51, 53, 10, 54, 51, 49, 9, -28, -66, -81, 9, -28, -70, -84, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 51, 51, 9, -27, -66, -112, 9, -27, -88, -91, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 53, 9, -23, -110, -79, 9, -28, -71, -103, -28, -70, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 55, 9, -27, -68, -96, 9, -23, -105, -75, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 51, 57, 9, -27, -66, -112, 9, -28, -70, -116, 9, -27, -91, -77, 9, 55, 57, 10, 54, 52, 49, 9, -26, -99, -88, 9, -25, -108, -78, -26, -127, -84, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 52, 51, 9, -23, -125, -111, 9, -27, -122, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 53, 9, -27, -66, -112, 9, -23, -109, -83, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 55, 9, -27, -112, -76, 9, -27, -118, -79, -28, -72, -125, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 52, 57, 9, -24, -75, -75, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 49, 9, -27, -66, -112, 9, -26, -103, -81, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 51, 9, -27, -67, -83, 9, -27, -123, -78, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 53, 9, -23, -110, -79, 9, -28, -71, -103, -23, -86, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 53, 55, 9, -27, -66, -112, 9, -26, -107, -113, -25, -85, -117, 9, -27, -91, -77, 9, 55, 57, 10, 54, 53, 57, 9, -23, -104, -82, 9, -27, -92, -87, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 49, 9, -28, -66, -81, 9, -28, -71, -103, -28, -70, -116, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 51, 9, -27, -66, -112, 9, -28, -72, -125, 9, -25, -108, -73, 9, 51, 53, 10, 54, 54, 53, 9, -23, -110, -79, 9, -28, -70, -116, -26, -107, -113, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 54, 55, 9, -26, -79, -97, 9, -26, -103, -74, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 54, 57, 9, -27, -66, -112, 9, -28, -72, -128, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 49, 9, -26, -79, -97, 9, -28, -70, -116, -27, -123, -83, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 51, 9, -24, -75, -75, 9, -27, -118, -79, -27, -118, -101, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 55, 53, 9, -27, -66, -112, 9, -28, -72, -103, 9, -27, -91, -77, 9, 55, 57, 10, 54, 55, 55, 9, -25, -114, -117, 9, -28, -70, -108, -23, -109, -83, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 55, 57, 9, -23, -126, -71, 9, -28, -72, -103, -27, -118, -79, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 49, 9, -27, -66, -112, 9, -25, -108, -100, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 51, 9, -23, -104, -82, 9, -28, -72, -128, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 53, 9, -23, -110, -79, 9, -26, -107, -113, -25, -108, -80, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 56, 55, 9, -27, -66, -112, 9, -27, -112, -101, 9, -25, -108, -73, 9, 51, 51, 54, 10, 54, 56, 57, 9, -27, -68, -96, 9, -25, -85, -117, -27, -97, -114, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 49, 9, -23, -110, -79, 9, -27, -92, -87, 9, -27, -91, -77, 9, 51, 51, 54, 10, 54, 57, 51, 9, -27, -66, -112, 9, -27, -112, -101, -24, -114, -119, 9, -27, -91, -77";
//        String[] list = str.split(", ");
//        System.out.println(list.length);

    }
}
