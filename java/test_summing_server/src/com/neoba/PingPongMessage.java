/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;

/**
 *
 * @author atul
 */
class PingPongMessage {

    public PingPongMessage(String ping) {
        System.out.println(ping+"!");
    }
    
    ByteBuf result(){
       
        ByteBuf reply=buffer(2+4+Long.SIZE*2);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.PINGPONG);
        reply.writeInt(4);
        
        reply.writeBytes("PONG".getBytes());
        
        
        return reply;
    }
    
}
