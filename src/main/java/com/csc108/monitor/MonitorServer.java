package com.csc108.monitor;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Created by zhangbaoshan on 2016/1/28.
 */
public class MonitorServer {
    public static final String CONNECTION_TOKEN="_936fc45f90b548339be569ade28374e9";

    public static final ChannelGroup allChannels=new DefaultChannelGroup("Server");
    /**
     * 启动Server
     */
    public static void ServerStart(String Ip,int port){
        ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipleline = pipeline();
                pipleline.addLast("encode", new TimeEncoder());
                pipleline.addLast("decode", new TimeDecoder());
                pipleline.addLast("handler", new CommandHandler());
                return pipleline;
            }
        });
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        Channel channel= bootstrap.bind(new InetSocketAddress(Ip,port));
        allChannels.add(channel);
    }

    public static void shutdown() {
        try {
            ChannelGroupFuture future = allChannels.close();
            future.awaitUninterruptibly();//阻塞，直到服务器关闭
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            //System.exit(1);
        }
    }
}
