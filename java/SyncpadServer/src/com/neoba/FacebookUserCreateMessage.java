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
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author root
 */
class FacebookUserCreateMessage {
    Long id;
    Boolean isnametaken=false;
    public FacebookUserCreateMessage(String username,String access_token) throws JSONException, IOException {
        View view = Dsyncserver.cclient.getView("dev_neoba", "userstoid");
        Query query = new Query();
        query.setKey(username);
        query.setStale( Stale.FALSE );
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        Logger logger=Logger.getLogger(UserCreateMessage.class);
        FacebookUser fuser=new FacebookUser(access_token);
        
        if(result.size()!=0)
        {
            isnametaken=true;
            logger.error("username taken");
            
        }
        if (!isnametaken) {
            id = Dsyncserver.cclient.incr("idgen", 1, 1152921504606846976L);
            JSONObject user = new JSONObject();
            user.put("username", username);
            user.put("type", "user");
            user.put("name", fuser.getName());
            user.put("email",fuser.getEmail());
            user.put("facebook_id", fuser.getId());
            user.put("facebook_friends", fuser.getFriends());
            user.put("type", "user");
            user.put("followers", new JSONArray());
            user.put("gcm_registration","");
            user.put("following", new JSONArray());
            user.put("docs", new JSONArray());
            user.put("edit_docs", new JSONArray());
            Dsyncserver.cclient.add(id.toString(), user.toString());
            logger.info("created user "+user.toString());
        }
    }

    ByteBuf result() {
        ByteBuf reply=buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_CREATE);
        if(!isnametaken)
            reply.writeInt(Constants.W_SUCCESS);
        else
            reply.writeInt(Constants.W_ERR_DUP_USERNAME);
        return reply;
    }
    

    
}
