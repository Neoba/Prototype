/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba.messages;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.neoba.Constants;
import com.neoba.Dsyncserver;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
public class UserCreateMessage implements Message{
    Long id;
    Boolean isnametaken=false;
    public UserCreateMessage(String username, byte[] passhash) throws JSONException {
        
        View view = Dsyncserver.cclient.getView("dev_neoba", "userstoid");
        Query query = new Query();
        query.setKey(username);
        query.setStale( Stale.FALSE );
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        Logger logger=Logger.getLogger(UserCreateMessage.class);
        if(result.size()!=0)
        {
            isnametaken=true;
            logger.error("username taken");
        }else
        {
            //System.out.println(result.iterator().next());
        }
        if (!isnametaken) {
            StringBuilder passb = new StringBuilder();
            for (byte b : passhash) {
                passb.append(String.format("%02X", b));
            }
            id = Dsyncserver.cclient.incr("idgen", 1, 1152921504606846976L);
            JSONObject user = new JSONObject();

            user.put("username", username);
            user.put("password", passb.toString());
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

    @Override
    public ByteBuf result() {
        
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
