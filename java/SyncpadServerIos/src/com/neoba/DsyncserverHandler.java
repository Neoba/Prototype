/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import com.neoba.utils.ConsoleUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 *
 * @author atul
 */
public class DsyncserverHandler extends SimpleChannelInboundHandler<HttpContent> {

    Logger logger = Logger.getLogger(DsyncserverHandler.class);
    static int msgcount;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpContent con) throws Exception {

        ByteBuf in = (ByteBuf) con.copy().content();
        if (in.array().length == 0) {
            String time=(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())).toString();
            respondAndFlush(Unpooled.wrappedBuffer(("<h1>Neoba Experimental Server 1</h1>"+time+"<br>Status:OK<br>Please use the client").getBytes()), ctx,true);
            logger.info("Browser request - responded");
            return;
        }
        //System.out.println(new String(in.array()));
        msgcount+=1;
        logger.info("Message Received from ip "+ctx.channel().remoteAddress()+":"+msgcount);
        ConsoleUtils.printhex("request",in.array(), in.array().length);
        MessageInterpreter mi = new MessageInterpreter(ctx, in);
        ByteBuf reply = mi.generateReply();
        ConsoleUtils.printhex("response",reply.array(), reply.array().length);
        respondAndFlush(reply, ctx,false);
        logger.info("Message Responded :"+msgcount);

    }

    void respondAndFlush(ByteBuf reply, ChannelHandlerContext ctx, boolean isBrowser) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, reply);
        response.headers().set(CONTENT_TYPE, isBrowser?"text/html":"application/octet-stream");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

}
