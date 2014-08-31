/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class UserLoginMessage implements Message{

    Boolean found=false;
    String id=null;
    UUID sessid;
    int response;
    Logger logger=Logger.getLogger(UserLoginMessage.class);
    public UserLoginMessage(String username, byte[] passhash,String regid,byte uagent) throws JSONException {
        StringBuilder passb = new StringBuilder();
        for (byte b : passhash) {
            passb.append(String.format("%02X", b));
        }
        View view = Dsyncserver.cclient.getView("dev_neoba", "userstoid");
        Query query = new Query();
        query.setIncludeDocs(true);
        query.setStale( Stale.FALSE );
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
            
            if(!((String)user.get("password")).equals(passb.toString()))
            {
                response=Constants.W_ERR_PWD_INCORRECT;
                logger.info(username+" :incorrect password ");
                id=null;
            }
            else
            {
                response=Constants.W_SUCCESS;
                sessid=UUID.randomUUID();
                switch(uagent){
                    case Constants.USER_AGENT_ANDROID:
                        CouchManager.set_gcm_rid(id, regid);
                        break;
                    case Constants.USER_AGENT_CONSOLE:
                        break;
                }
                
                Dsyncserver.usersessions.put(sessid, id.toString());
                logger.info(username+" :user found");
                logger.info(username+" :session- "+sessid);
            }
        }
        else
        {
            response=Constants.W_ERR_NONEXISTENT_USER;
            logger.info(username+" :user dosn't exist ");
            id=null;
        }
        
    }

    @Override
    public ByteBuf result() {
        ByteBuf reply;
            if(response==Constants.W_SUCCESS)
                reply=buffer(6+16);
            else
                reply=buffer(6);
                
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_LOGIN);
        reply.writeInt(response);
        if(response==Constants.W_SUCCESS)
        {
            reply.writeLong(sessid.getLeastSignificantBits());
            reply.writeLong(sessid.getMostSignificantBits());
        }
        return reply;
    }
    
    String getid(){
        return id;
    }
    
}
