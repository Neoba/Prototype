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
import java.util.UUID;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class GetDigestMessage implements Message{

    ViewResponse result;
    int bufsize = 2 + 4;
    int doccount = 0;

    public GetDigestMessage() throws JSONException {
        View view = Dsyncserver.cclient.getView("dev_neoba", "idsview");
        Query query = new Query();
        query.setIncludeDocs(true);
        result = Dsyncserver.cclient.query(view, query);
        for (ViewRow row : result) {
            doccount += 1;
            System.out.println(row.getKey() + " :" + row.getValue());
            JSONObject json = new JSONObject(row.getValue());
            bufsize += ((JSONArray) json.get("diff")).length();
            bufsize += ((String) json.get("dict")).length();
            bufsize += ((String) json.get("title")).length();
            bufsize += (16 + 4 + 4 + 4 + 4);
        }
    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(bufsize);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.GET_DIGEST);
        reply.writeInt(doccount);
        try{
        for (ViewRow row : result) {
            JSONObject json = new JSONObject(row.getValue());
            UUID id = UUID.fromString(row.getKey());
            reply.writeLong(id.getLeastSignificantBits());
            reply.writeLong(id.getMostSignificantBits());
            reply.writeInt(((JSONArray) json.get("diff")).length());
            for (int i = 0; i < ((JSONArray) json.get("diff")).length(); i++) {
                reply.writeByte((int) ((JSONArray) json.get("diff")).get(i));
            }
            reply.writeInt(((String) json.get("dict")).length());
            for (byte b : ((String) json.get("dict")).getBytes()) {
                reply.writeByte(b);
            }
            reply.writeInt(((String) json.get("title")).length());
            for (byte b : ((String) json.get("title")).getBytes()) {
                reply.writeByte(b);
            }
            reply.writeInt(json.getInt("age"));
        }
        }
        catch(JSONException J)
        {
            System.err.println(J);
        }
        return reply;

    }

}
