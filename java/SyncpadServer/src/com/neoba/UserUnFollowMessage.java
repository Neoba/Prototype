package com.neoba;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class UserUnFollowMessage implements Message {

    String fid = "0";
    private boolean userfound = false;
    private boolean already_following = false;
    private boolean self_follow = false;
    private boolean push_success = false;
    static final Logger logger = Logger.getLogger(UserUnFollowMessage.class);
    TreeSet<String> revoked;

    public UserUnFollowMessage(String username, UUID session) throws JSONException, Exception {
        String userid = (String) Dsyncserver.usersessions.get(session);
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        TreeSet<String> owned_ufollwer = new TreeSet<String>();
        TreeSet<String> new_sdocs_read_set = new TreeSet<String>();
        TreeSet<String> new_sdocs_edit_set = new TreeSet<String>();
        revoked = new TreeSet<String>();
        JSONArray new_following = new JSONArray();
        JSONArray new_follower = new JSONArray();
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
        JSONObject following = null;
        if (userfound) {
            following = new JSONObject((String) Dsyncserver.cclient.get(fid));
            JSONArray currently_fing = user.getJSONArray("following");
            for (int i = 0; i < currently_fing.length(); i++) {
                JSONObject fingi = currently_fing.getJSONObject(i);
                if (!fingi.get("id").equals(fid)) {
                    new_following.put(fingi);
                } else {
                    already_following = true;
                }
            }

            JSONArray currently_fers = following.getJSONArray("followers");

            for (int i = 0; i < currently_fers.length(); i++) {
                JSONObject fingi = currently_fers.getJSONObject(i);
                if (!fingi.get("id").equals(userid)) {
                    new_follower.put(fingi);
                }
            }

            logger.debug(currently_fers);
            logger.debug(currently_fing);
            logger.debug(new_follower);
            logger.debug(new_following);
        }

        if (already_following && userfound) {

            JSONArray self_docs_read = user.getJSONArray("docs");
            JSONArray self_docs_edit = user.getJSONArray("edit_docs");
            for (int i = 0; i < self_docs_read.length(); i++) {
                new_sdocs_read_set.add(self_docs_read.getString(i));
            }
            for (int i = 0; i < self_docs_edit.length(); i++) {
                new_sdocs_edit_set.add(self_docs_edit.getString(i));
            }

            logger.debug(new_sdocs_read_set);
            logger.debug(new_sdocs_edit_set);

            View view0 = Dsyncserver.cclient.getView("dev_neoba", "created_docs");
            Query query0 = new Query();
            query0.setKey(String.format("\"%s\"", this.fid));
            query0.setStale( Stale.FALSE );
            ViewResponse result0 = Dsyncserver.cclient.query(view0, query0);
            for (ViewRow row : result0) {
                logger.debug("Doc in processing: "+row.getValue());
                if (new_sdocs_read_set.contains(row.getValue())) {
                    revoked.add(row.getValue());
                    JSONObject sdoc = new JSONObject((String) Dsyncserver.cclient.get(row.getValue()));
                    JSONArray sdoc_read = sdoc.getJSONArray("permission_read");
                    JSONArray sdoc_edit = sdoc.getJSONArray("permission_edit");
                    JSONArray new_sdoc_read = new JSONArray();
                    JSONArray new_sdoc_edit = new JSONArray();
                    for (int i = 0; i < sdoc_read.length(); i++) {
                        if (!sdoc_read.getString(i).equals(userid)) {
                            new_sdoc_read.put(sdoc_read.getString(i));
                        }
                    }
                    for (int i = 0; i < sdoc_edit.length(); i++) {
                        if (!sdoc_edit.getString(i).equals(userid)) {
                            new_sdoc_edit.put(sdoc_edit.getString(i));
                        }
                    }
                    logger.debug("document's new access owners "+row.getValue());
                    logger.debug(new_sdoc_read);
                    logger.debug(new_sdoc_edit);
                    
                    sdoc.put("permission_read", new_sdoc_read);
                    sdoc.put("permission_edit", new_sdoc_edit);
                    Dsyncserver.cclient.replace(row.getValue(), sdoc.toString());

                }
                new_sdocs_read_set.remove(row.getValue());
                new_sdocs_edit_set.remove(row.getValue());

            }
                    logger.debug("new documet list of"+username);
                    logger.debug(new_sdocs_read_set);
                    logger.debug(new_sdocs_edit_set);
            JSONArray final_sread_docs = new JSONArray();
            JSONArray final_sedit_docs = new JSONArray();

            for (String s : new_sdocs_read_set) {
                final_sread_docs.put(s);
            }
            for (String s : new_sdocs_edit_set) {
                final_sedit_docs.put(s);
            }
            user.put("docs", final_sread_docs);
            user.put("edit_docs", final_sedit_docs);
            user.put("following", new_following);
            Dsyncserver.cclient.replace(userid, user.toString());
            following.put("followers", new_follower);
            Dsyncserver.cclient.replace(fid, following.toString());

            JSONObject unfollowaction = new JSONObject();
            unfollowaction.put("type", "unfollowed");
            unfollowaction.put("userid", userid);
            JSONArray revokearray = new JSONArray();
            for (String s : revoked) {
                revokearray.put(s);
            }
            unfollowaction.put("docs", revokearray);
            ArrayList<String> regids = new ArrayList<String>();
            for (String rid : CouchManager.get_gcm_rids(fid)) {
                regids.add(rid);
            }
            if (regids.size() != 0) {
                if (GoogleCloudMessager.Push(session, regids, (String) user.get("username"), "unfollowed you", unfollowaction)) {

                    push_success = true;
                }
            } else {

                push_success = true;
            }

        }

    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(6 + 8 + 4 + 16 * revoked.size());
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_UNFOLLOW);
        if (!already_following) {
            reply.writeInt(Constants.W_ERR_NOT_ALREADY_FOLLOWING);
        } else if (self_follow) {
            reply.writeInt(Constants.W_ERR_SELF_UNFOLLOW);
        } else if (!userfound) {
            reply.writeInt(Constants.W_ERR_NONEXISTENT_USER);
        } else if (!push_success) {
            reply.writeInt(Constants.W_ERR_PUSH_FAILED);
        } else {
            reply.writeInt(Constants.W_SUCCESS);

        }
        reply.writeLong(Long.parseLong(this.fid));
        reply.writeInt(revoked.size());
        for (String s : revoked) {
            UUID id = UUID.fromString(s);
            reply.writeLong(id.getLeastSignificantBits());
            reply.writeLong(id.getMostSignificantBits());
        }
        return reply;
    }

}
