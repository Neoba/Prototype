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
 * @author root
 */
public class ProtocolExpiredMessage implements Message{
    
    int type;
    public ProtocolExpiredMessage(int type) {
        this.type=type;
    }
    
    @Override
    public ByteBuf result(){
        
        ByteBuf reply=buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(type);
        reply.writeInt(Constants.W_ERR_PROTOCOL_EXPIRED);
        return reply;
    }
}
