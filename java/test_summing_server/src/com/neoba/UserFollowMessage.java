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
import io.netty.channel.Channel;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class UserFollowMessage implements Message{

    String fid;
    private boolean userfound = false;
    private boolean already_following=false;

    public UserFollowMessage(Long id, Channel ch) throws JSONException {
        this.fid = id.toString();
        
        View view = Dsyncserver.cclient.getView("dev_neoba", "usernamelistview");
        Query query = new Query();
        query.setIncludeDocs(true);
        ViewResponse result = Dsyncserver.cclient.query(view, query);

        for (ViewRow row : result) {

            if (row.getId().equals(fid)) {
                userfound = true;
                break;
            }
        }

        if (userfound) {
            String userid = (String) Dsyncserver.usersessions.getKey(ch);
            JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(fid));
            JSONArray followers = (JSONArray) user.get("followers");
            for(int i=0;i<followers.length();i++)
                if(userid.equals((String)followers.get(i)))
                {
                    already_following=true;
                    return;
                }
            followers.put(userid);
            System.out.println(followers);
            user.put("followers", followers);
            Dsyncserver.cclient.replace(fid, user.toString());
        }
    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_FOLLOW);

        if(already_following)
            reply.writeInt(Constants.W_ERR_ALREADY_FOLLOWING);
        else if(!userfound)
            reply.writeInt(Constants.W_ERR_NONEXISTENT_USER);
        else
            reply.writeInt(Constants.W_SUCCESS);
        return reply;
    }

}
