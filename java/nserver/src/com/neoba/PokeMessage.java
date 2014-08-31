package com.neoba;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.ArrayList;
import java.util.UUID;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class PokeMessage implements Message {

    String fid = "77";
    private boolean userfound = false;
    private boolean already_following = false;
    private boolean self_follow = false;
    private boolean push_success = false;

    public PokeMessage(String username, UUID session) throws JSONException, Exception {
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
            JSONObject self = new JSONObject((String) Dsyncserver.cclient.get(userid));
            JSONObject pokeaction = new JSONObject();
            pokeaction.put("type", "poke");
            pokeaction.put("username", (String) self.get("username"));
            ArrayList<String> regids = new ArrayList<String>();
            for (String rid : CouchManager.get_gcm_rids(fid)) {
                regids.add(rid);
            }

            if (GoogleCloudMessager.Push(session, regids, (String) self.get("username"), "poke!", pokeaction)) {
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
