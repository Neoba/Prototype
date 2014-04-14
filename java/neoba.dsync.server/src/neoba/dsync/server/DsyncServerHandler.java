/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neoba.dsync.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 *
 * @author Atul Vinayak
 */
class DsyncServerHandler extends SimpleChannelInboundHandler<String> {
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

public void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        // Send the received message to all channels but the current one.
        for (Channel c: channels) {
            if (c != ctx.channel()) {
                c.writeAndFlush("[" + ctx.channel().remoteAddress() + "] " +
                        msg + '\n');
            } else {
                c.writeAndFlush("[you] " + msg + '\n');
            }
        }

        // Close the connection if the client has sent 'bye'.
        if ("bye".equals(msg.toLowerCase())) {
            ctx.close();
        }
    }
    @Override
    public void channelRead0(ChannelHandlerContext chc, String i) throws Exception {
        Channel incoming = chc.channel();
        for(Channel channel:channels){
            if(channel!=incoming){
                channel.write(incoming.remoteAddress()+" "+i+"\n");
            }
        }
    }
    
}
