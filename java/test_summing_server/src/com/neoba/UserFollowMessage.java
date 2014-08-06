package com.neoba;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.UUID;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class UserFollowMessage implements Message {

    String fid="77";
    private boolean userfound = false;
    private boolean already_following = false;
    private boolean self_follow = false;

    public UserFollowMessage(String username, UUID session) throws JSONException {
        View view = Dsyncserver.cclient.getView("dev_neoba", "userstoid");
        Query query = new Query();
        query.setKey(username);
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        if (result.size() == 1) {
            userfound = true;
            this.fid = result.iterator().next().getValue();
            if (this.fid.equals((String) Dsyncserver.usersessions.get(session))) {
                self_follow = true;
                return;
            }
        }

        if (userfound) {
            String userid = (String) Dsyncserver.usersessions.get(session);
            JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(fid));
            JSONArray followers = (JSONArray) user.get("followers");
            for (int i = 0; i < followers.length(); i++) {
                if (userid.equals((String) ((JSONObject)followers.get(i)).get("id"))) {
                    already_following = true;
                    return;
                }
            }
            JSONObject self = new JSONObject((String) Dsyncserver.cclient.get(userid));
            JSONObject fo=new JSONObject();
            fo.put("username", self.get("username"));
            fo.put("id", userid);
            followers.put(fo);
            user.put("followers", followers);
            Dsyncserver.cclient.replace(fid, user.toString());
            
            
            JSONArray following = (JSONArray) self.get("following");
            JSONObject fl=new JSONObject();
            fl.put("username", username);
            fl.put("id", fid);
            following.put(fl);
            self.put("following", following);
            Dsyncserver.cclient.replace(userid, self.toString());
            
            
        }
    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(6+8);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_FOLLOW);
        
        if (already_following) {
            reply.writeInt(Constants.W_ERR_ALREADY_FOLLOWING);
        } else if (self_follow) {
            reply.writeInt(Constants.W_ERR_SELF_FOLLOW);
        } else if (!userfound) {
            reply.writeInt(Constants.W_ERR_NONEXISTENT_USER);
        } else {
            reply.writeInt(Constants.W_SUCCESS);
            
        }
        reply.writeLong(Long.parseLong(this.fid));
        return reply;
    }

}
