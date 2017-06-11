package com.alibaba.middleware.race.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created by wanshao on 2017/5/25.
 */
public class ClientDemoInHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(Client.class);

    // 接收server端的消息，并打印出来
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.info("com.alibaba.middleware.race.sync.ClientDemoInHandler.channelRead");
        logger.info("client get the buffer date");
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        logger.info(Arrays.toString(result1));
        writeBytes(result1);
        logger.info("get the result");
//        System.out.println("com.alibaba.middleware.race.sync.Server said:" + new String(result1));
        result.release();
        ctx.writeAndFlush("I have received your messages and wait for next messages");
        ctx.close();
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
    }

    private void writeBytes(byte[] bytes) throws IOException{
        logger.info("get into the writeBytes");
        String fileName = Constants.RESULT_HOME+"/Result.rs";
        FileChannel channel = new RandomAccessFile(fileName, "rw").getChannel();
        channel.write(ByteBuffer.wrap(bytes));
        File file = new File(fileName);
        logger.info("the file exist:"+file.exists());
    }
}
