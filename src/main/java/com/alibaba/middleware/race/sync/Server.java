package com.alibaba.middleware.race.sync;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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

    private static final int PAGE_SIZE = 200*1024*1024;

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

//        dataCarry(logger,Constants.DATA_HOME,Constants.MIDDLE_HOME);
//
//        ByteBuffer buffer = getData(logger,args[2],args[3]);

        logger.info("start the server");
        server.startServer(5527);
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


    private void startServer(int port) throws InterruptedException {
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
                        ch.pipeline().addLast(new ServerDemoInHandler());
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

    private static void dataCarry(Logger logger,String path,String middle)throws IOException{
        for (int i=1;i<=10;i++){
            String file1 = path+"/"+i+".txt";
            String file2 = middle+"/"+i+".txt";
            logger.info(file1);
            FileChannel channel1 = new RandomAccessFile(file1, "rw").getChannel();
            FileChannel channel2 = new RandomAccessFile(file2, "rw").getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);
            while (-1 != (channel1.read(buffer))){
                buffer.flip();
                channel2.write(buffer);
                buffer.clear();
            }
        }
    }

    private static void dataCarry2(Logger logger,String path,String middle)throws IOException{
        for (int i=1;i<=10;i++){
            String file1 = path+"/"+i+".txt";
            String file2 = middle+"/"+i+".txt";
            logger.info(file1);
            FileChannel channel1 = new RandomAccessFile(file1, "rw").getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);
            int num = 0;
            while (-1 != (channel1.read(buffer))){
                buffer.flip();
                MappedByteBuffer buffer2 = new RandomAccessFile(file2, "rw").getChannel()
                        .map(FileChannel.MapMode.READ_WRITE,num*PAGE_SIZE,(long) buffer.limit());
                buffer2.put(buffer);
                buffer.clear();
                num++;
            }
        }
    }

    private static ByteBuffer getData(Logger logger, String start, String end)throws Exception{
        logger.info("get into the getData");
        LogStore logStore = LogStore.getInstance();
        int statId = Integer.parseInt(start);
        int endId = Integer.parseInt(end);
        logStore.init(statId,endId);
        long startConsumer = System.currentTimeMillis();
        for (int i=0;i<1;i++){
            new ProduceThread(logStore,Constants.MIDDLE_HOME).start();
        }
        logStore.parseBytes(Integer.parseInt(start),Integer.parseInt(end));
        logger.info("finish the parse");
        ByteBuffer buffer = logStore.parse();
        long endConsumer = System.currentTimeMillis();
        logger.info("the cost time: "+(endConsumer-startConsumer));
        logger.info("buffer length:"+buffer.array().length);

        return buffer;
    }
}
