package com.csc108.monitor;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

class TimeEncoder extends SimpleChannelHandler {
	private static final Logger log = Logger.getLogger("monitor");
	private final ChannelBuffer buffer = dynamicBuffer(100000000);	
    /**
     * 编码
     */
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)  
            throws Exception {  
    	  byte[] date=SerializableObject(e.getMessage());
    	    buffer.writeInt(date.length);
    		buffer.writeBytes(date);
    		Channels.write(ctx,e.getFuture(),buffer);  
      
    }  
	  /**
     * 序列化对象
     */
	public static  byte[] SerializableObject(Object object) {
		  ByteArrayOutputStream out = null;
		  ObjectOutputStream outObject = null;
		  byte[] result=null;
		try {
			out = new ByteArrayOutputStream();
			outObject = new ObjectOutputStream(out);
			outObject.writeObject(object);
			outObject.flush();
			result=out.toByteArray();
			outObject.close();
			out.close();
	    	} catch (IOException e) {
			e.printStackTrace();
			log.error(e, e);
	    	}
		return result;
	}
}