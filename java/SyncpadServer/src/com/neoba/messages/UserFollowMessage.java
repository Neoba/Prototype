package com.neoba.messages;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.neoba.Constants;
import com.neoba.CouchManager;
import com.neoba.Dsyncserver;
import com.neoba.GoogleCloudMessager;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.ArrayList;
import java.util.UUID;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
public class UserFollowMessage implements Message {

    String fid = "0";
    private boolean userfound = false;
    private boolean already_following = false;
    private boolean self_follow = false;
    private boolean push_success = false;

    public UserFollowMessage(String username, UUID session) throws JSONException, Exception {
        View view = Dsyncserver.cclient.getView("dev_neoba", "userstoid");
        Query query = new Query();
        query.setKey(username);
        query.setStale( Stale.FALSE );
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
                if (userid.equals((String) ((JSONObject) followers.get(i)).get("id"))) {
                    already_following = true;
                    return;
                }
            }
            JSONObject self = new JSONObject((String) Dsyncserver.cclient.get(userid));
            JSONObject fo = new JSONObject();
            fo.put("username", self.get("username"));
            fo.put("id", userid);
            followers.put(fo);
            user.put("followers", followers);

            JSONArray following = (JSONArray) self.get("following");
            JSONObject fl = new JSONObject();
            fl.put("username", username);
            fl.put("id", fid);
            following.put(fl);
            self.put("following", following);

            JSONObject followaction = new JSONObject();
            followaction.put("type", "follow");
            followaction.put("username", (String) self.get("username"));
            followaction.put("id", (String) (String) Dsyncserver.usersessions.get(session));
            ArrayList<String> regids = new ArrayList<String>();
            for (String rid : CouchManager.get_gcm_rids(fid)) {
                regids.add(rid);
            }
            if (regids.size() != 0) {
                if (GoogleCloudMessager.Push(session, regids, (String) self.get("username"), "you have a new follower!", followaction)) {
                    Dsyncserver.cclient.replace(fid, user.toString());
                    Dsyncserver.cclient.replace(userid, self.toString());
                    push_success = true;
                }
            }else{
                    Dsyncserver.cclient.replace(fid, user.toString());
                    Dsyncserver.cclient.replace(userid, self.toString());
                    push_success = true;
            }
        }
    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(6 + 8);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_FOLLOW);

        if (already_following) {
            reply.writeInt(Constants.W_ERR_ALREADY_FOLLOWING);
        } else if (self_follow) {
            reply.writeInt(Constants.W_ERR_SELF_FOLLOW);
        } else if (!userfound) {
            reply.writeInt(Constants.W_ERR_NONEXISTENT_USER);
        } else if (!push_success) {
            reply.writeInt(Constants.W_ERR_PUSH_FAILED);
        } else {
            reply.writeInt(Constants.W_SUCCESS);

        }
        reply.writeLong(Long.parseLong(this.fid));
        return reply;
    }

}
