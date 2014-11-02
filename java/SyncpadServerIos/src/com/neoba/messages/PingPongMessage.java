/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba.messages;

import com.neoba.Constants;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import io.netty.channel.Channel;

/**
 *
 * @author atul
 */
public class PingPongMessage implements Message{


    @Override
    public ByteBuf result(){
       
        ByteBuf reply=buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.PINGPONG);
        reply.writeInt(Constants.W_PING);
       
        return reply;
    }
    
}
