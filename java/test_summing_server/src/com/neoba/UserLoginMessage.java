/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class UserLoginMessage implements Message{

    Boolean found=false;
    String id=null;
    int response;
    public UserLoginMessage(String username, byte[] passhash) throws JSONException {
        StringBuilder passb = new StringBuilder();
        for (byte b : passhash) {
            passb.append(String.format("%02X", b));
        }
        View view = Dsyncserver.cclient.getView("dev_neoba", "usernamelistview");
        Query query = new Query();
        query.setIncludeDocs(true);
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        for(ViewRow row : result) {
            if(row.getKey().equals(username)){
                found=true;
                id=row.getId();
                break;
            }
        }
        
        if(found){
            JSONObject user=new JSONObject((String)Dsyncserver.cclient.get(id));
            System.out.println(id+" "+username);
            if(!((String)user.get("password")).equals(passb.toString()))
                response=Constants.W_ERR_PWD_INCORRECT;
            else
                response=Constants.W_SUCCESS;
        }
        else
            response=Constants.W_ERR_NONEXISTENT_USER;
        
    }

    @Override
    public ByteBuf result() {
        
        ByteBuf reply;
            if(response==Constants.W_SUCCESS)
                reply=buffer(6+8);
            else
                reply=buffer(6);
                
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_LOGIN);
        reply.writeInt(response);
        if(response==Constants.W_SUCCESS)
            reply.writeLong(Long.parseLong(id));
        return reply;
    }
    
    String getid(){
        return id;
    }
    
}
