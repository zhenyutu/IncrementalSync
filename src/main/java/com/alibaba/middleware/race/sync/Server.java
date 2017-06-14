package com.alibaba.middleware.race.sync;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 服务器类，负责push消息到client Created by wanshao on 2017/5/25.
 */
public class Server {

    // 保存channel
    private static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();

    public static Map<String, Channel> getMap() {
        return map;
    }

    public static void setMap(Map<String, Channel> map) {
        Server.map = map;
    }

    public static void main(String[] args) throws Exception {
        initProperties();
        Logger logger = LoggerFactory.getLogger(Server.class);
        Server server = new Server();
        logger.info("com.alibaba.middleware.race.sync.Server is running....");
        // 第一个参数是Schema Name
        logger.info("Schema:" + args[0]);
        // 第二个参数是Schema Name
        logger.info("table:" + args[1]);
        // 第三个参数是start pk Id
        logger.info("start:" + args[2]);
        // 第四个参数是end pk Id
        logger.info("end:" + args[3]);

        ByteBuffer buffer = getData(logger,args[2],args[3]);

        server.startServer(5527,buffer);
//        server.startServer(5527,args[0],args[1],args[2],args[3]);
//        server.startServer(5527,"middleware5","student","100","200");
    }

    /**
     * 打印赛题输入 赛题输入格式： schemaName tableName startPkId endPkId，例如输入： middleware student 100 200
     * 上面表示，查询的schema为middleware，查询的表为student,主键的查询范围是(100,200)，注意是开区间 对应DB的SQL为： select * from middleware.student where
     * id>100 and id<200
     */

    /**
     * 初始化系统属性
     */
    private static void initProperties() {
        System.setProperty("middleware.test.home", Constants.TESTER_HOME);
        System.setProperty("middleware.teamcode", Constants.TEAMCODE);
        System.setProperty("app.logging.level", Constants.LOG_LEVEL);
    }


    private void startServer(int port,final ByteBuffer buffer) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        // 注册handler
                        ch.pipeline().addLast(new ServerDemoInHandler(buffer));
                        ch.pipeline().addLast("encoder", new LengthFieldPrepender(4, false));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static ByteBuffer getData(Logger logger, String start, String end)throws Exception{
        logger.info("get into the getData");
        LogStore logStore = LogStore.getInstance();
        int statId = Integer.parseInt(start);
        int endId = Integer.parseInt(end);
        logStore.init(statId,endId,Constants.DATA_HOME);
//        logger.info(schemaTable + "-"+statId +"-"+endId);
        long startConsumer = System.currentTimeMillis();
        for (int i=0;i<3;i++){
            new ProduceThread(logStore,Constants.DATA_HOME).start();
        }
        logStore.parseBytes("middleware5|student",Integer.parseInt(start),Integer.parseInt(end));
        logger.info("finish the parse");
        ByteBuffer buffer = logStore.parse();
        long endConsumer = System.currentTimeMillis();
        logger.info("the cost time: "+(endConsumer-startConsumer));

        return buffer;
    }
}
