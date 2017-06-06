package com.alibaba.middleware.race.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理client端的请求 Created by wanshao on 2017/5/25.
 */
public class ServerDemoInHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ServerDemoInHandler.class);
    private static final int PAGE_SIZE = 4*1024;
    private static final int REMAINING_SIZE = 20;
    private static final byte SPLITE_FLAG = (byte)124;
    private static final byte END_FLAG = (byte)10;
    private static final byte SPACE_FLAG = (byte)32;

    private int position = 0;

    private Map<String,ByteBuffer> fileByteBuffer = new HashMap<>();
    private Map<String,FileChannel> fileChannel = new HashMap<>();

    /**
     * 根据channel
     * 
     * @param ctx
     * @return
     */
    public static String getIPString(ChannelHandlerContext ctx) {
        String ipString = "";
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1, colonAt);
        return ipString;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 保存channel
        Server.getMap().put(getIPString(ctx), ctx.channel());

        logger.info("com.alibaba.middleware.race.sync.ServerDemoInHandler.channelRead");
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        // msg中存储的是ByteBuf类型的数据，把数据读取到byte[]中
        result.readBytes(result1);
        String resultStr = new String(result1);
        // 接收并打印客户端的信息
        System.out.println("com.alibaba.middleware.race.sync.Client said:" + resultStr);

        while (true) {
            // 向客户端发送消息
            String message = (String) getMessage();
            if (message != null) {
                Channel channel = Server.getMap().get("127.0.0.1");
                ByteBuf byteBuf = Unpooled.wrappedBuffer(message.getBytes());
                channel.writeAndFlush(byteBuf).addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("Server发送消息成功！");
                    }
                });

            }
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private Object getMessage() throws InterruptedException {
        // 模拟下数据生成，每隔5秒产生一条消息
        Thread.sleep(5000);

        return "message generated in ServerDemoInHandler";

    }

    private void pullBytesFormFile(String file) throws IOException{
        byte[] lastLogs = null;
        byte[] logs;
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, Math.min(channel.size(), PAGE_SIZE));

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
            lastLogs = getLogFromBytes(logs);
        }

        channel.close();
    }

    private byte[] getLogFromBytes(byte[] logs) throws IOException{
        int start = 0, end ,LogEnd;
        byte[] lastLogs = null;
        while (true){
            LogEnd = findFirstByte(logs,start,END_FLAG,1);
            if (LogEnd == start){
                lastLogs = new byte[logs.length-start+1];
                System.arraycopy(logs,start-1,lastLogs,0,logs.length-start+1);
                break;
            }
            start = start + 37;
            end = findFirstByte(logs,start,SPLITE_FLAG,2);
            String schemaTable = getStrFromBytes(logs,start,end);
            start = end;
            end = findFirstByte(logs,start,SPLITE_FLAG,1);
            String operate = getStrFromBytes(logs,start,end);
            start = end;
            position = end;

            switch (operate){
                case "I":
                    insertOperate(schemaTable, logs, start, LogEnd);
                    break;
                case "U":
                    updateOperate(schemaTable, logs, start, LogEnd);
                    break;
                case "D":
                    deleteOperate(schemaTable, logs, start, LogEnd);
                    break;
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

    private void insertOperate(String schemaTable,byte[] logs,int start,int end)throws IOException{
        ByteBuffer buffer = fileByteBuffer.get(schemaTable);
        if (buffer==null){
            buffer = ByteBuffer.allocate(PAGE_SIZE);
            fileByteBuffer.put(schemaTable,buffer);
        }

        buffer.put(findSingleStr(logs,start,SPLITE_FLAG,3));
        for (int n = 3;;n=n+3){
            if (position+1<end){
                buffer.put(SPACE_FLAG);
                buffer.put(findSingleStr(logs,position,SPLITE_FLAG,3));
            }else {
                break;
            }
        }
        buffer.put(END_FLAG);
//        if (buffer.remaining()<REMAINING_SIZE)
//        {
            FileChannel channel = fileChannel.get(schemaTable);
            if (channel==null){
                String fileName = Constants.TESTER_HOME+"/"+schemaTable+".txt";
                channel = new RandomAccessFile(fileName, "rw").getChannel();
                fileChannel.put(schemaTable,channel);
            }
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
//        }
    }
    private void updateOperate(String schemaTable,byte[] logs,int start,int end){

    }
    private void deleteOperate(String schemaTable,byte[] logs,int start,int end){

    }

    public static void main(String[] args) throws IOException{
        ServerDemoInHandler handler = new ServerDemoInHandler();
        String file = "/home/tuzhenyu/tmp/canal_data/canal.txt";
        handler.pullBytesFormFile(file);
    }
}
