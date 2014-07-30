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
import static io.netty.buffer.Unpooled.buffer;
import io.netty.channel.Channel;
import java.util.Objects;
import java.util.UUID;
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
    private boolean self_follow=false;
    
    public UserFollowMessage(Long id, UUID session) throws JSONException {
        this.fid = id.toString();
        if(id.toString().equals((String)Dsyncserver.usersessions.get(session))){
            self_follow=true;
            return;
        }
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
            String userid = (String) Dsyncserver.usersessions.get(session);
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
        else if(self_follow)
            reply.writeInt(Constants.W_ERR_SELF_FOLLOW);
        else if(!userfound)
            reply.writeInt(Constants.W_ERR_NONEXISTENT_USER);
        else
            reply.writeInt(Constants.W_SUCCESS);
        return reply;
    }

}
