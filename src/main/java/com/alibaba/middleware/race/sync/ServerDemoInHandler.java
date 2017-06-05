package com.alibaba.middleware.race.sync;

import io.netty.handler.codec.string.LineSeparator;
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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

/**
 * 处理client端的请求 Created by wanshao on 2017/5/25.
 */
public class ServerDemoInHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ServerDemoInHandler.class);
    private static final int PAGE_SIZE = 4*1024;
    private static final byte SPLITE_FLAG = (byte)124;
    private static final byte END_FLAG = (byte)10;

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
            getLogFromBytes(bytes);
        }

        channel.close();
    }

    private void getLogFromBytes(byte[] logs){
        int start = 0, end;
        while (true){
            start = start + 37;
            end = findFirstByte(logs,start,SPLITE_FLAG);
            String schema = getStrFromBytes(logs,start,end);
            start = end;
            end = findFirstByte(logs,start,SPLITE_FLAG);
            String table = getStrFromBytes(logs,start,end);
            start = end;
            end = findFirstByte(logs,start,SPLITE_FLAG);
            String operate = getStrFromBytes(logs,start,end);
            start = end;
            end = findFirstByte(logs,end,END_FLAG);

            switch (operate){
                case "I":
                    insertOperate(schema, table, operate, logs, start, end);
                    break;
                case "U":
                    updateOperate(schema, table, operate, logs, start, end);
                    break;
                case "D":
                    deleteOperate(schema, table, operate, logs, start, end);
                    break;
            }

            start = end;
        }
    }

    private String getStrFromBytes(byte[] logs,int start,int end){
        byte[] schemaBytes = new byte[end-start-1];
        System.arraycopy(schemaBytes,start+1,schemaBytes,0,end-start-1);
        return new String(schemaBytes);
    }

    private int findFirstByte(byte[] logs,int start,byte value){
        int index = start;
        for (int i=start;i<logs.length;i++){
            if (logs[i] == value){
                index = i;
                break;
            }
        }
        return index;
    }

    private void insertOperate(String schema,String table,String operate,byte[] logs,int start,int end){

    }
    private void updateOperate(String schema,String table,String operate,byte[] logs,int start,int end){

    }
    private void deleteOperate(String schema,String table,String operate,byte[] logs,int start,int end){

    }
}
