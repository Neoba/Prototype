/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;

/**
 *
 * @author atul
 */

class MessageInterpreter {
    
    	byte VERSION;
	byte type;
	int size;
        ByteBuf buff;
        
    MessageInterpreter(ByteBuf buff){
        
            VERSION=buff.getByte(0);
            type=buff.getByte(1);
            size=buff.getInt(2);
            this.buff=buff;
            
	}

    ByteBuf generateReply() {
        
        switch(type){
            case Constants.DOCUMENT_CREATE:
                String Docname=buff.toString(6, size, Charset.forName("UTF-8"));
                return new DocumentCreateMessage(Docname).result();
                
            case Constants.PINGPONG:
                String ping=buff.toString(6, size, Charset.forName("UTF-8"));
                return new PingPongMessage(ping).result();
        }
        return null;
    }
}
