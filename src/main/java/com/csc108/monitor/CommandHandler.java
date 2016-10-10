package com.csc108.monitor;

import com.csc108.log.LogFactory;
import com.csc108.monitor.command.CommandBase;
import com.csc108.monitor.command.CommandFactory;
import org.jboss.netty.channel.*;

import java.text.SimpleDateFormat;

/**
 * Created by zhangbaoshan on 2016/5/24.
 */
public class CommandHandler extends SimpleChannelHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("mylogger2");
    private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static final String CONNECTION_TOKEN="_936fc45f90b548339be569ade28374e9";

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)throws Exception {
        log.info("Server Channel closed " + e);
    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)throws Exception {
        log.info("Server Channel connected " + e);
    }

    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("channelDisconnected"+e);
        super.channelDisconnected(ctx, e);
    }
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("channelOpen");
        MonitorServer.allChannels.add(e.getChannel());
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        String cmdText=((String) e.getMessage());
        ChannelFuture future =null;
        if(cmdText.contains(CONNECTION_TOKEN)==false){
            try{
                future = e.getChannel().write("Unauthorized command!");
            }finally {
                if(future!=null)
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }else{
            String message="OmServerResponse success:"+cmdText+" /n";
            String[] args = cmdText.replace(CONNECTION_TOKEN,"").split(";");
            String response="";
            try{
//                CommandBase cmd = CommandFactory.getInstance().fetchCommand(args);
//                if(cmd==null)
//                    response="Can't handle command "+args[0];
//                else
//                    response = cmd.run(args);
                response = CommandFactory.getInstance().runCommand(args);
            }catch (Exception ex){
                ex.printStackTrace();
                LogFactory.error("Process monitor command error!",ex);
                response =ex.toString();
            }

            future = e.getChannel().write(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
