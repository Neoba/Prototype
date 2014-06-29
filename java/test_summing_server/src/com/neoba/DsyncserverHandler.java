/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.io.IOException;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import org.codehaus.jettison.json.JSONException;

class DsyncserverHandler extends ChannelInboundHandlerAdapter {
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("removed "+Dsyncserver.usersessions.getKey(ctx.channel()));
        Dsyncserver.usersessions.removeValue((Channel)ctx.channel());
        System.out.println("disconnected user");    
    }
        
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws JSONException, IOException, VcdiffDecodeException, VcdiffEncodeException {
        ByteBuf in = (ByteBuf) msg;
        MessageInterpreter mi=new MessageInterpreter(ctx,in);
        ByteBuf reply=mi.generateReply();
        ctx.write(reply);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
        Throwable cause) {
        if (cause instanceof ReadTimeoutException) {
            System.out.println("removed by timeout "+Dsyncserver.usersessions.getKey(ctx.channel()));
            Dsyncserver.usersessions.removeValue((Channel)ctx.channel());
         }
        else
            cause.printStackTrace();
        ctx.close();
    }
}
