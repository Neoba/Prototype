/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.UUID;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class GrantPermissionMessage implements Message {

    private boolean unfollowed_user = false;
    private boolean unprivilaged = false;

    public GrantPermissionMessage(UUID doc, HashMap<String, Byte> permissions, Channel channel) throws JSONException {
        String userid = (String) Dsyncserver.usersessions.getKey(channel);
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));

        JSONArray followers = (JSONArray) (user.get("followers"));

        if(followers.length()<permissions.size())
            unfollowed_user=true;
        
        if(!unfollowed_user)
        {
            Boolean f;
            for(String p:permissions.keySet()){
                f=false;
                for(int i=0;i<followers.length();i++)
                    if(p.equals((String)followers.get(i) ) ){
                        f=true;
                        break;
                    }
                if(!f)
                {
                    unfollowed_user=true;
                    break;
                }
            }
        
        }
        if (!unfollowed_user) {

            JSONObject document = new JSONObject((String) Dsyncserver.cclient.get(doc.toString()));

            if (!((String) document.get("creator")).equals(userid)) {
                this.unprivilaged = true;
                return;
            }

            TreeSet<String> readset = new TreeSet<>();
            JSONArray readarray = (JSONArray) (document.get("permission_read"));
            for (int i = 0; i < readarray.length(); i++) {
                readset.add(readarray.getString(i));
            }

            TreeSet<String> editset = new TreeSet<>();
            JSONArray editarray = (JSONArray) (document.get("permission_edit"));
            for (int i = 0; i < editarray.length(); i++) {
                editset.add(editarray.getString(i));
            }

            for (String id : permissions.keySet()) {
                readset.remove(id);
                editset.remove(id);
                switch (permissions.get(id)) {
                    case Constants.PERMISSION_NONE:
                        readset.remove(id);
                        editset.remove(id);
                        
                        break;
                    case Constants.PERMISSION_READ:
                        readset.add(id);
                        editset.remove(id);
                        break;
                    case Constants.PERMISSION_EDIT:
                        readset.remove(id);
                        editset.add(id);
                        break;
                }
            }
            readarray = new JSONArray();
            for (String s : readset) {
                readarray.put(s);
            }
            editarray = new JSONArray();
            for (String s : editset) {
                editarray.put(s);
            }
            document.put("permission_read", readarray);
            document.put("permission_edit", editarray);

            Dsyncserver.cclient.replace(doc.toString(), document.toString());
        }
    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.GRANT_PERMISSION);
        if (unfollowed_user) {
            reply.writeInt(Constants.W_ERR_SHARE_WITH_UNFOLLOWED_USER);
        } else if (unprivilaged) {
            reply.writeInt(Constants.W_ERR_UNPRIVILAGED_USER);
        } else {
            reply.writeInt(Constants.W_SUCCESS);
        }
        return reply;
    }

}
