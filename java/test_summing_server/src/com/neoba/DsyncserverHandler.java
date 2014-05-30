/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.charset.Charset;

/**
 *
 * @author atul
 */
class DsyncserverHandler extends
        ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx,
        Object msg) {
        ByteBuf in = (ByteBuf) msg;
        
        
        int a=in.getInt(0);
        int b=in.getInt(4);
      
        System.out.println("SServer received: "+a+" "+b);
        //System.out.println("SServer received: " + in.toString(Charset.forName("UTF-8")));
//        int a= Integer.parseInt(in.toString(Charset.forName("UTF-8")).split(" ")[0]);
//        int b= Integer.parseInt(in.toString(Charset.forName("UTF-8")).split(" ")[1]);
        in.clear();
        in.writeInt(a+b);
        ctx.write(in);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
        Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
