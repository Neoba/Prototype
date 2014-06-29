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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class UserCreateMessage implements Message{
    Long id;
    Boolean isnametaken=false;
    UserCreateMessage(String username, byte[] passhash) throws JSONException {
        
        View view = Dsyncserver.cclient.getView("dev_neoba", "usernamelistview");
        Query query = new Query();
        query.setIncludeDocs(true);
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        for(ViewRow row : result) {
            
            if(row.getKey().equals(username)){
                isnametaken=true;
                break;
            }
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
            Dsyncserver.cclient.add(id.toString(), user.toString());
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
