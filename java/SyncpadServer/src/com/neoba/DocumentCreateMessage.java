/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.io.IOException;
import java.util.UUID;
import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class DocumentCreateMessage implements Message{
    UUID id;
    String userid;
    Logger logger=Logger.getLogger(DocumentCreateMessage.class);
    public DocumentCreateMessage(UUID doc,UUID session) throws JSONException, IOException, VcdiffEncodeException {

        this.id=doc;
        this.userid= (String)Dsyncserver.usersessions.get(session);
        
        //insert to database
        JSONObject json=new JSONObject();
        
        json.put("dict", "");
        JSONArray jdiff=new JSONArray();
        for(byte b:new VcdiffEncoder("", "").encode())
            jdiff.put(b);
        json.put("diff",jdiff);
        json.put("version",-1);
        json.put("type", "document");
        json.put("title", "");
        json.put("creator",userid);
        json.put("global", false);
        json.put("history",(new JSONArray()));
        json.put("permission_read",(new JSONArray()));
        json.put("permission_edit",(new JSONArray()));
        Dsyncserver.cclient.set(this.id.toString(),json.toString());
        
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        JSONArray docs=user.getJSONArray("docs");
        docs.put(id.toString());
        JSONArray edits=user.getJSONArray("edit_docs");

        edits.put(id.toString());
        user.put("docs",docs);
        user.put("edit_docs",edits);
        Dsyncserver.cclient.replace(userid, user.toString());
        logger.info(session+": Created new Document ["+ this.id.toString()+"] : "+(String)Dsyncserver.cclient.get(this.id.toString()));
    }
    
    @Override
    public ByteBuf result(){
       
        ByteBuf reply=buffer(2+4+16);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.DOCUMENT_CREATE);
        reply.writeInt(Constants.W_SUCCESS);
        reply.writeLong(id.getLeastSignificantBits());
        reply.writeLong(id.getMostSignificantBits());
        return reply;
    }
    
}
