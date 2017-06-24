package com.alibaba.middleware.race.sync;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

/**
 * 处理client端的请求 Created by wanshao on 2017/5/25.
 */
public class ServerDemoInHandler extends ChannelInboundHandlerAdapter {
    private String start;
    private String end;

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public ServerDemoInHandler(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public static String getIPString(ChannelHandlerContext ctx) {
        String ipString = "";
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1, colonAt);
        return ipString;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String clientIp = getIPString(ctx);
        logger.info("clientIp:" + clientIp);
        // 保存channel
        Server.getMap().put(clientIp, ctx.channel());

        logger.info("com.alibaba.middleware.race.sync.ServerDemoInHandler.channelRead");
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        // msg中存储的是ByteBuf类型的数据，把数据读取到byte[]中
        result.readBytes(result1);
        String resultStr = new String(result1);
        // 接收并打印客户端的信息
        logger.info("com.alibaba.middleware.race.sync.Client said:" + resultStr);
        logger.info("begin to run...");

        LogStore logStore = LogStore.getInstance();
        ByteBuffer buffer = logStore.parse(Integer.parseInt(start), Integer.parseInt(end));
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        logger.info("finish the parse");

        byte[] zipData = compress(data);
        logger.info("length:" + data.length + "-" + zipData.length);

        Channel channel = Server.getMap().get(clientIp);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(zipData);
        channel.writeAndFlush(byteBuf).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.info("Server发送消息成功！");
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    // TODO： who?
    public static byte[] compress(byte[] inputByte) throws IOException {
        int len = 0;
        Deflater defl = new Deflater();
        defl.setInput(inputByte);
        defl.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outputByte = new byte[1024];
        try {
            while (!defl.finished()) {
                len = defl.deflate(outputByte);
                bos.write(outputByte, 0, len);
            }
            defl.end();
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }
}
