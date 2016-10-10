package com.csc108.monitor;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

class TimeDecoder extends FrameDecoder {  
	private static final Logger log = Logger.getLogger("monitor");
	
	private final ChannelBuffer buffer = dynamicBuffer();
	 /**
	  * 解码
	  */
    protected Object decode(ChannelHandlerContext ctx, Channel channel,  
            ChannelBuffer channelBuffer) throws Exception {
		if (channelBuffer.readableBytes() < 4) {
		    	return null;
		   }
		if (channelBuffer.readable()) {
			channelBuffer.readBytes(buffer, channelBuffer.readableBytes());
		  }
		String name = (String)UnSerializableObject(buffer.array());
		return name;  
    }  
    /**
     * 反序列化
     * @param b
     * @return
     */
    public Object UnSerializableObject(byte[] b) {
		Object object = null;
		ByteArrayInputStream inStream = null;
	    ObjectInputStream in = null;
		try {
			inStream=new ByteArrayInputStream(b);
			in = new ObjectInputStream(inStream);
			 object= in.readObject();
			 in.close();
			 inStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e, e);
		}
		return object;
	}
  
} 