/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import io.netty.channel.Channel;
import java.io.IOException;
import java.util.UUID;
import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class DocumentCreateMessage implements Message{
    UUID id;
    String name;
    String userid;
    public DocumentCreateMessage(String name,Channel ch) throws JSONException, IOException, VcdiffEncodeException {
        this.name=name;
        this.id=UUID.randomUUID();
        this.userid= (String)Dsyncserver.usersessions.getKey(ch);
        //insert to database
        JSONObject json=new JSONObject();
        json.put("title", name);
        json.put("dict", " ");
        JSONArray jdiff=new JSONArray();
        for(byte b:new VcdiffEncoder(" ", " ").encode())
            jdiff.put(b);
        json.put("diff",jdiff);
        json.put("age", 0);
        json.put("type", "document");
        json.put("creator",userid);
        json.put("global", false);
        
        json.put("permission_read",(new JSONArray()).put(userid));
        json.put("permission_edit",(new JSONArray()).put(userid));
        
        Dsyncserver.cclient.set(this.id.toString(),json.toString());
        System.out.println("Created new Document ["+ this.id.toString()+"] : "+(String)Dsyncserver.cclient.get(this.id.toString()));
    }
    
    @Override
    public ByteBuf result(){
       
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
