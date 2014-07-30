/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class GetDigestMessage implements Message {

    JSONArray docs;
    JSONArray wdocs;
    int bufsize = 2 + 4;
    int doccount = 0;

    public GetDigestMessage(UUID sessid) throws JSONException {
        String userid = (String) Dsyncserver.usersessions.get(sessid);
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        docs = user.getJSONArray("docs");
        wdocs = user.getJSONArray("edit_docs");

        for (int i = 0; i < docs.length(); i++) {
            JSONObject doc = new JSONObject((String) Dsyncserver.cclient.get((String) docs.get(i)));
            System.out.println((String) doc.get("title") + ":version" + (Integer) doc.get("version"));
            bufsize += ((JSONArray) doc.get("diff")).length();
            bufsize += ((String) doc.get("dict")).length();
            bufsize += ((String) doc.get("title")).length();
            bufsize += (16 + 4 + 4 + 4 + 4);
            bufsize += 1;
            doccount += 1;
        }
    }

    @Override
    public ByteBuf result() {
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(UserLoginMessage.class.getName()).log(Level.SEVERE, null, ex);
//        }
        TreeSet<String> wset = new TreeSet();
        for (int i = 0; i < wdocs.length(); i++) {
            try {
                wset.add(wdocs.getString(i));
            } catch (JSONException ex) {
                Logger.getLogger(GetDigestMessage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ByteBuf reply = buffer(bufsize);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.GET_DIGEST);
        reply.writeInt(doccount);
        try {
            for (int i = 0; i < docs.length(); i++) {
                JSONObject doc = new JSONObject((String) Dsyncserver.cclient.get((String) docs.get(i)));
                
                UUID id = UUID.fromString((String) docs.get(i));
                reply.writeLong(id.getLeastSignificantBits());
                reply.writeLong(id.getMostSignificantBits());
                reply.writeInt(((JSONArray) doc.get("diff")).length());
                for (int k = 0; k < ((JSONArray) doc.get("diff")).length(); k++) {
                    reply.writeByte((int) ((JSONArray) doc.get("diff")).get(k));
                }
                reply.writeInt(((String) doc.get("dict")).length());
                for (byte b : ((String) doc.get("dict")).getBytes()) {
                    reply.writeByte(b);
                }
                reply.writeInt(((String) doc.get("title")).length());
                for (byte b : ((String) doc.get("title")).getBytes()) {
                    reply.writeByte(b);
                }
                reply.writeInt(doc.getInt("version"));
                if(wset.contains((String) docs.get(i)))
                    reply.writeByte(Constants.PERMISSION_EDIT);
                else
                    reply.writeByte(Constants.PERMISSION_READ);
            }

        } catch (Exception J) {
            System.err.println(J);
        }
        return reply;

    }

}
