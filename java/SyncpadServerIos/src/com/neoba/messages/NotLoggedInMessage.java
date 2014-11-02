/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba.messages;

import com.neoba.Constants;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;

/**
 *
 * @author atul
 */
public class NotLoggedInMessage implements Message{
    
    public NotLoggedInMessage() {

    }
    
    @Override
    public ByteBuf result(){
        
        ByteBuf reply=buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.CREDENTIAL_REQ);
        reply.writeInt(Constants.W_ERR_NOT_LOGGED_IN);
        return reply;
    }
    
}
