/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.UUID;

/**
 *
 * @author atul
 */
class DocumentCreateMessage {
    UUID id;
    String name;
    public DocumentCreateMessage(String name) {
        this.name=name;
        this.id=UUID.randomUUID();
        //insert to database
        Dsyncserver.cclient.set(this.id.toString(), name);
        System.out.println("Created new Document ["+ this.id.toString()+"] : "+(String)Dsyncserver.cclient.get(this.id.toString()));
    }
    
    ByteBuf result(){
       
        ByteBuf reply=buffer(2+4+Long.SIZE*2);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.DOCUMENT_CREATE);
        reply.writeInt((Long.SIZE/8)*2);
        reply.writeLong(id.getLeastSignificantBits());
        reply.writeLong(id.getMostSignificantBits());
        //printhex(reply.array());
        return reply;
    }
    
}
