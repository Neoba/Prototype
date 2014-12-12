package com.neoba.messages;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.neoba.Constants;
import com.neoba.CouchManager;
import com.neoba.Dsyncserver;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
public class UserVitalsMessage implements Message {

    String fid = "0";
    String facebookid = "0";
    private boolean userfound = false;
    private boolean already_following = false;
    private boolean self_follow = false;
    private boolean push_success = false;
    String vitals = "";
    Logger logger;

    public UserVitalsMessage(String username, UUID session) throws JSONException, Exception {
        logger = Logger.getLogger(UserVitalsMessage.class);
        View view = Dsyncserver.cclient.getView("dev_neoba", "userstoid");
        Query query = new Query();
        query.setKey(username);
        logger.info(" looking up user :" + username);
        query.setStale(Stale.FALSE);
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        if (result.size() == 1) {
            userfound = true;
            logger.info(" found user :" + username);
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
            JSONArray following = (JSONArray) user.get("following");
             for (int i = 0; i < followers.length(); i++) {
                if (userid.equals((String) ((JSONObject) followers.get(i)).get("id"))) {
                    already_following = true;
                }
            }
            facebookid = (String) user.get("facebook_id");
            if (already_following) {
                vitals += "1@";
            } else {
                vitals += "0@";
            }
            logger.info("sofar user :" + vitals);
            
            vitals += (username + "~" + CouchManager.getFacebookID(username) + "@");
            if(followers.length()==0)vitals += "X";
            for (int i = 0; i < followers.length(); i++) {
                String temp = (String) ((JSONObject) followers.get(i)).get("username");
                vitals += (temp + "~" + CouchManager.getFacebookID(temp)+ "&");
            }
            vitals += "@";
            if(following.length()==0)vitals += "X";
            for (int i = 0; i < following.length(); i++) {
                String temp = (String) ((JSONObject) following.get(i)).get("username");
                vitals += (temp + "~" + CouchManager.getFacebookID(temp)+ "&");
            }
            vitals += "@";
        }

    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(6 + 4 + vitals.length());
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_VITALS);

        if (!userfound) {
            reply.writeInt(Constants.W_ERR_NONEXISTENT_USER);
        } else {
            reply.writeInt(Constants.W_SUCCESS);

        }
        logger.info("final vitals :" + vitals);
        reply.writeInt(vitals.length());
        for (byte b : vitals.getBytes()) {
            reply.writeByte(b);
        }
        return reply;
    }

}
