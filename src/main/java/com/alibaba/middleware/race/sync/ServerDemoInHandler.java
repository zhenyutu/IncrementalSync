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

import java.nio.ByteBuffer;

/**
 * 处理client端的请求 Created by wanshao on 2017/5/25.
 */
public class ServerDemoInHandler extends ChannelInboundHandlerAdapter {
    private String schema;
    private String table;
    private String start;
    private String end;

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public ServerDemoInHandler(String schema,String table,String start,String end){
        this.schema = schema;
        this.table = table;
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

        // 保存channel
        Server.getMap().put(getIPString(ctx), ctx.channel());

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
        int statId = Integer.parseInt(start);
        int endId = Integer.parseInt(end);
        logStore.init(statId,endId);
//        String path = "/canal_data/1";
        String schemaTable = schema+"|"+table;
        logger.info(schemaTable);
        long startConsumer = System.currentTimeMillis();
        for (int i=0;i<3;i++){
            new ProduceThread(logStore,Constants.DATA_HOME).start();
        }
        logStore.parseBytes(schemaTable,Integer.parseInt(start),Integer.parseInt(end));
        logger.info("finish the parse");
        ByteBuffer buffer = logStore.parse();
//        logStore.flush(buffer);
        long endConsumer = System.currentTimeMillis();
        logger.info("the cost time: "+(endConsumer-startConsumer));
        logger.info("the cost time: "+(endConsumer-startConsumer));
        logger.info("finish the parse");

//        String message = "finish the parse";
        Channel channel = Server.getMap().get("127.0.0.1");
//        ByteBuf byteBuf = Unpooled.wrappedBuffer(message.getBytes());
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer);
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

    private Object getMessage() throws InterruptedException {
        // 模拟下数据生成，每隔5秒产生一条消息
        Thread.sleep(5000);

        return "message generated in ServerDemoInHandler";

    }
}
