package com.alibaba.middleware.race.sync;

import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.Inflater;

/**
 * Created by wanshao on 2017/5/25.
 * @author tuzhenyu
 */
public class ClientDemoInHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private EventLoopGroup workerGroup;

    public ClientDemoInHandler(EventLoopGroup workerGroup){
        this.workerGroup = workerGroup;
    }

    // 接收server端的消息，并打印出来
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.info("client get the buffer date");
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        byte[] data = uncompress(result1);
        logger.info("length:"+result1.length+"-"+data.length);
//        logger.info(Arrays.toString(data));
        writeBytes(data);
        logger.info("finish the write result And close the ctx");
        result.release();
        ctx.close();
        workerGroup.shutdownGracefully();
    }

    // 连接成功后，向server发送消息
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("com.alibaba.middleware.race.sync.ClientDemoInHandler.channelActive");
        String msg = "I am prepared to receive messages";
        ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
        encoded.writeBytes(msg.getBytes());
        ctx.write(encoded);
        ctx.flush();
        logger.info("finish write and flash");
    }

    private void writeBytes(byte[] bytes) throws IOException{
        logger.info("get into the writeBytes");
        String fileName = Constants.RESULT_HOME+"/Result.rs";

        FileOutputStream fos = new FileOutputStream(fileName);
        fos.write(bytes);
        fos.flush();
        fos.close();
        logger.info("finish write data to Result.rs");
    }

    public static byte[] uncompress(byte[] inputByte) throws IOException {
        int len = 0;
        Inflater infl = new Inflater();
        infl.setInput(inputByte);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outByte = new byte[1024];
        try {
            while (!infl.finished()) {
                len = infl.inflate(outByte);
                if (len == 0) {
                    break;
                }
                bos.write(outByte, 0, len);
            }
            infl.end();
        } catch (Exception e) {
            //
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }
}
