package com.alibaba.middleware.race.sync;

import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by wanshao on 2017/5/25.
 * @author tuzhenyu
 */
public class Client {

    private final static int port = Constants.SERVER_PORT;
    // idle时间
    private static String ip;
    private volatile EventLoopGroup workerGroup;
    private volatile Bootstrap bootstrap;

    public static void main(String[] args) throws Exception {
        initProperties();
        Logger logger = LoggerFactory.getLogger(Client.class);
        logger.info("Welcome");
        // 从args获取server端的ip
        ip = args[0];
        logger.info("ip:"+ip);
//        ip = "127.0.0.1";
        Client client = new Client();
        client.connect(logger,ip, port);

    }

    private static void initProperties() {
        System.setProperty("middleware.test.home", Constants.TESTER_HOME);
        System.setProperty("middleware.teamcode", Constants.TEAMCODE);
        System.setProperty("app.logging.level", Constants.LOG_LEVEL);
    }

    public void connect(Logger logger,String host, int port) throws Exception {
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();

        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addFirst("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                ch.pipeline().addLast(new IdleStateHandler(10, 0, 0));
                ch.pipeline().addLast(new ClientDemoInHandler(workerGroup));
            }
        });
        doConnect(logger,host,port);

    }

    private void doConnect(final Logger logger,final String host, final int port) {

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                if (!f.isSuccess()) {
                    logger.info("Started Tcp Client Failed");
                    f.channel().eventLoop().schedule( new Runnable() {
                        @Override
                        public void run() {
                            doConnect(logger,host,port);
                        }
                    }, 1, TimeUnit.SECONDS);
                }
            }
        });
    }


}
