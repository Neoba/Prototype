package com.neoba.messages;

import com.neoba.messages.Message;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.neoba.Constants;
import com.neoba.CouchManager;
import com.neoba.Dsyncserver;
import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
public class GetDigestMessage implements Message {

    JSONArray docs;
    JSONArray wdocs;
    JSONArray followers;
    JSONArray following;
    String userid;
    int bufsize = 2 + 4;
    int doccount = 0, followerc = 0, followingc = 0, createdc = 0;
    ViewResponse result;
    static final Logger logger = Logger.getLogger(GetDigestMessage.class);
    UUID sessid;    
    TreeSet<String> ownedset;
    
    public GetDigestMessage(UUID sessid) throws JSONException {
        this.sessid = sessid;
        userid = (String) Dsyncserver.usersessions.get(sessid);
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        docs = user.getJSONArray("docs");
        wdocs = user.getJSONArray("edit_docs");
        followers = user.getJSONArray("followers");
        following = user.getJSONArray("following");
        ownedset=getOwnedSet(userid);
        
        for (int i = 0; i < docs.length(); i++) {
            JSONObject doc = new JSONObject((String) Dsyncserver.cclient.get((String) docs.get(i)));
            logger.debug(sessid + " :calculating digest of " + doc.get("title") + " version " + (Integer) doc.get("version"));
            bufsize += ((JSONArray) doc.get("diff")).length();
            bufsize += ((String) doc.get("dict")).length();
            bufsize += (getOwner((String)docs.get(i))).length();
            bufsize += (16 + 4 + 4 + 4 + 4);
            bufsize += 1;
            bufsize+=1;
            doccount += 1;
        }
        bufsize += (4 + 4);     //4 each for specifiying count of followers and following list
        for (int i = 0; i < followers.length(); i++) {
            JSONObject f = (JSONObject) followers.get(i);
            bufsize += 4;
            String fbid= CouchManager.getFacebookID(f.getString("username"));
            bufsize += f.getString("username").length();
            bufsize+=(1+fbid.length());
            bufsize += 8;
            followerc += 1;

        }
        for (int i = 0; i < following.length(); i++) {
            JSONObject f = (JSONObject) following.get(i);
            bufsize += 4;
            String fbid= CouchManager.getFacebookID(f.getString("username"));
            bufsize += f.getString("username").length();
            bufsize+=(1+fbid.length());
            bufsize += 8;
            followingc += 1;
        }
        View view = Dsyncserver.cclient.getView("dev_neoba", "created_docs");
        Query query = new Query();
        query.setKey(String.format("\"%s\"", userid));
        query.setStale( Stale.FALSE );
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        bufsize += 4;
        for (ViewRow row : result) {
            bufsize += 16;
            bufsize += (4 + 4);
            JSONObject doc = new JSONObject((String) Dsyncserver.cclient.get(row.getValue()));
            JSONArray edit_p = doc.getJSONArray("permission_edit");
            bufsize += (edit_p.length() * 8);
            JSONArray read_p = doc.getJSONArray("permission_read");
            bufsize += (read_p.length() * 8);
        }
        bufsize += 4;
    }
    
    public TreeSet<String> getOwnedSet(String userid){
        View view = Dsyncserver.cclient.getView("dev_neoba", "created_docs");
        Query query = new Query();
        query.setKey(String.format("\"%s\"", userid));
        query.setStale( Stale.FALSE );
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        TreeSet<String> ts=new TreeSet<String>();
        for(ViewRow row : result){
            ts.add(row.getValue());
        }
        return ts;
        
    }
    public String getOwner(String id) throws JSONException{
        JSONObject doc = new JSONObject((String) Dsyncserver.cclient.get(id));
        String creatorid=doc.getString("creator");
        JSONObject owner=new JSONObject((String) Dsyncserver.cclient.get(creatorid));
        return owner.getString("username")+"~"+owner.getString("facebook_id");
    }

    @Override
    public ByteBuf result() {
        TreeSet<String> wset = new TreeSet();
        for (int i = 0; i < wdocs.length(); i++) {
            try {
                wset.add(wdocs.getString(i));
            } catch (JSONException ex) {
                logger.error(sessid + " :Broken JSON " + ex);
            }
        }
        ByteBuf reply = buffer(bufsize);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.GET_DIGEST);
        reply.writeInt(doccount);
        try {
            for (int i = 0; i < docs.length(); i++) {
                JSONObject doc = new JSONObject((String) Dsyncserver.cclient.get((String) docs.get(i)));
                logger.debug(sessid + " :digesting doc " + doc.get("title") + " version " + (Integer) doc.get("version"));
                UUID id = UUID.fromString((String) docs.get(i));
                reply.writeLong(id.getLeastSignificantBits());
                reply.writeLong(id.getMostSignificantBits());
                reply.writeInt(((JSONArray) doc.get("diff")).length());
                for (int k = 0; k < ((JSONArray) doc.get("diff")).length(); k++) {
                    reply.writeByte((Integer) ((JSONArray) doc.get("diff")).get(k));
                }
                reply.writeInt(((String) doc.get("dict")).length());
                for (byte b : ((String) doc.get("dict")).getBytes()) {
                    reply.writeByte(b);
                }
                String temp=getOwner(id.toString());
                reply.writeInt(temp.length());
                
                for (byte b : (temp).getBytes()) {
                    reply.writeByte(b);
                }
                reply.writeInt(doc.getInt("version"));
                if (wset.contains((String) docs.get(i))) {
                    reply.writeByte(Constants.PERMISSION_EDIT);
                } else {
                    reply.writeByte(Constants.PERMISSION_READ);
                }
                
                if(ownedset.contains((String) docs.get(i)))
                    reply.writeByte(1);
                else
                    reply.writeByte(0);
            }
            reply.writeInt(followerc);
            for (int i = 0; i < followers.length(); i++) {
                JSONObject f = (JSONObject) followers.get(i);
                String fbid=CouchManager.getFacebookID( f.getString("username"));
                reply.writeInt(f.getString("username").length()+1+fbid.length());
                logger.debug(sessid + " :digesting follower " + f.getString("username"));
                
                for (byte b : ((String) f.get("username")+"~"+fbid).getBytes()) {
                    reply.writeByte(b);
                }
                reply.writeLong(Long.parseLong(f.getString("id")));
            }
            reply.writeInt(followingc);
            for (int i = 0; i < following.length(); i++) {
                JSONObject f = (JSONObject) following.get(i);
                logger.debug(sessid + " :digesting follower " + f.getString("username"));
                String fbid=CouchManager.getFacebookID( f.getString("username"));
                reply.writeInt(f.getString("username").length()+1+fbid.length());
                for (byte b : ((String) f.get("username")+"~"+fbid).getBytes()) {
                    reply.writeByte(b);
                }
                reply.writeLong(Long.parseLong(f.getString("id")));
            }
            View view = Dsyncserver.cclient.getView("dev_neoba", "created_docs");
            Query query = new Query();
            query.setKey(String.format("\"%s\"", userid));
            ViewResponse result = Dsyncserver.cclient.query(view, query);
            reply.writeInt(result.size());
            for (ViewRow row : result) {
                UUID cd = UUID.fromString(row.getValue());
                reply.writeLong(cd.getLeastSignificantBits());
                reply.writeLong(cd.getMostSignificantBits());
                JSONObject doc = new JSONObject((String) Dsyncserver.cclient.get(row.getValue()));
                JSONArray read_p = doc.getJSONArray("permission_read");
                reply.writeInt(read_p.length());
                for (int i = 0; i < read_p.length(); i++) {
                    reply.writeLong(Long.parseLong(read_p.getString(i)));
                }
                JSONArray edit_p = doc.getJSONArray("permission_edit");
                reply.writeInt(edit_p.length());
                for (int i = 0; i < edit_p.length(); i++) {
                    reply.writeLong(Long.parseLong(edit_p.getString(i)));
                }

            }

            logger.debug(sessid + " :digested permissions info ");
            logger.info(sessid + " :digestion successful ");

        } catch (Exception J) {
            logger.error(sessid + " :Error preparing digest " + J);
        }
        return reply;

    }

}
