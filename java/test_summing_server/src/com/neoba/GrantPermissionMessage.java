/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.ArrayList;
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
   private boolean push_success = false;
    private boolean push_successn = false;
     private boolean push_successr = false;
      private boolean push_successw = false;

    public GrantPermissionMessage(UUID doc, HashMap<String, Byte> permissions, UUID session) throws JSONException, Exception {
        String userid = (String) Dsyncserver.usersessions.get(session);
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        TreeSet<String> oldreadset=new TreeSet();
        TreeSet<String> oldeditset=new TreeSet();
        JSONArray followers = (JSONArray) (user.get("followers"));

        if (followers.length() < permissions.size()) {
            unfollowed_user = true;
        }

        if (!unfollowed_user) {
            Boolean f;
            for (String p : permissions.keySet()) {
                f = false;
                for (int i = 0; i < followers.length(); i++) {
                    if (p.equals(((JSONObject) followers.get(i)).get("id"))) {
                        f = true;
                        break;
                    }
                }
                if (!f) {
                    unfollowed_user = true;
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
                oldreadset.add(readarray.getString(i));
            }

            TreeSet<String> editset = new TreeSet<>();
            JSONArray editarray = (JSONArray) (document.get("permission_edit"));
            for (int i = 0; i < editarray.length(); i++) {
                editset.add(editarray.getString(i));
                oldeditset.add(editarray.getString(i));
            }

            for (String s : readset) {
                if (!permissions.keySet().contains(s)) {
                    permissions.put(s, Constants.PERMISSION_NONE);
                }
            }

            for (String id : permissions.keySet()) {
                JSONObject fojson = new JSONObject((String) Dsyncserver.cclient.get(id));
                TreeSet<String> docsset = new TreeSet<>();
                JSONArray docsarray = (JSONArray) (fojson.get("docs"));
                for (int i = 0; i < docsarray.length(); i++) {
                    docsset.add(docsarray.getString(i));
                }

                TreeSet<String> editdocsset = new TreeSet<>();
                JSONArray editdocsarray = (JSONArray) (fojson.get("edit_docs"));
                for (int i = 0; i < editdocsarray.length(); i++) {
                    editdocsset.add(editdocsarray.getString(i));
                }
                readset.remove(id);
                editset.remove(id);
                docsset.remove(doc.toString());
                editdocsset.remove(doc.toString());
                switch (permissions.get(id)) {
                    case Constants.PERMISSION_NONE:
                        readset.remove(id);
                        editset.remove(id);
                        editdocsset.remove(doc.toString());
                        docsset.remove(doc.toString());
                        break;
                    case Constants.PERMISSION_READ:
                        readset.add(id);
                        editset.remove(id);
                        editdocsset.remove(doc.toString());
                        docsset.add(doc.toString());
                        break;
                    case Constants.PERMISSION_EDIT:
                        readset.add(id);
                        editset.add(id);
                        editdocsset.add(doc.toString());
                        docsset.add(doc.toString());
                        break;
                }
                docsarray = new JSONArray();
                for (String s : docsset) {
                    docsarray.put(s);
                }
                editdocsarray = new JSONArray();
                for (String s : editdocsset) {
                    editdocsarray.put(s);
                }
                fojson.put("docs", docsarray);
                fojson.put("edit_docs", editdocsarray);

                Dsyncserver.cclient.replace(id, fojson.toString());
            }
            readarray = new JSONArray();
            for (String s : readset) {
                readarray.put(s);
            }
            editarray = new JSONArray();
            for (String s : editset) {
                editarray.put(s);
            }
            
            for(String s:readset)
                oldreadset.remove(s);
            for(String s:editset)
                oldeditset.remove(s);
            if (oldeditset.size() > 0 || oldreadset.size() > 0) {
                ArrayList<String> readpush = new ArrayList<String>();
                System.out.println(readset);
                System.out.println(editset);
                for (String s : oldreadset) {
                        readpush.add(CouchManager.getGcmRegId(s));
                }

                JSONObject action = new JSONObject();
                action.put("type", "permission_revoke");
                action.put("id", doc.toString());
                if (readpush.size() > 0) {
                    if(GoogleCloudMessager.Push(readpush, document.getString("title"), user.getString("username") + " revoked access to "+document.getString("title"), action))
                        push_successn=true;
                }else
                    push_successn=true;
            }else
                    push_successn=true;
            if ( push_successn && (editarray.length() > 0 || readarray.length() > 0)) {
                ArrayList<String> readpush = new ArrayList<String>();
                ArrayList<String> writepush = new ArrayList<String>();
                System.out.println(readset);
                System.out.println(editset);
                for (String s : readset) {
                    if(!editset.contains(s))
                        readpush.add(CouchManager.getGcmRegId(s));
                }
                for (String s : editset) {
                    writepush.add(CouchManager.getGcmRegId(s));
                }
                JSONObject action = new JSONObject();
                action.put("type", "permission_grant");
                action.put("dict", document.getString("dict"));
                JSONArray diffa= new JSONArray(document.getString("diff"));
                byte[] diff=new byte[diffa.length()];
                for(int i=0;i<diffa.length();i++)
                    diff[i]=(byte)diffa.getInt(i);
                action.put("diff",Base64.encodeToString(diff, true));
                action.put("age", (int) document.get("version"));
                action.put("id", doc.toString());
                action.put("title", document.getString("title"));
                if (writepush.size() > 0) {
                    action.put("permission", Constants.PERMISSION_EDIT);
                    if(GoogleCloudMessager.Push(writepush, document.getString("title"), user.getString("username") + " shared a new document", action))
                       push_successw=true; 
                }else
                     push_successw=true; 
                if (readpush.size() > 0) {
                    action.put("permission", Constants.PERMISSION_READ);
                    if(GoogleCloudMessager.Push(readpush, document.getString("title"), user.getString("username") + " shared a new document (read-only)", action))
                        push_successr=true;
                }else
                     push_successr=true; 
            }else{
                push_successr=push_successw=true;
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
        }else if (!push_successn ||  !push_successw ||  !push_successr) {
            reply.writeInt(Constants.W_ERR_PUSH_FAILED);
        } else {
            reply.writeInt(Constants.W_SUCCESS);
        }
        return reply;
    }

}

/*


package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.UUID;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


class GrantPermissionMessage implements Message {

    private boolean unfollowed_user = false;
    private boolean unprivilaged = false;

    public GrantPermissionMessage(UUID doc, HashMap<String, Byte> permissions, UUID session) throws JSONException {
        String userid = (String) Dsyncserver.usersessions.get(session);
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));

        JSONArray followers = (JSONArray) (user.get("followers"));

        if (followers.length() < permissions.size()) {
            unfollowed_user = true;
        }

        if (!unfollowed_user) {
            Boolean f;
            for (String p : permissions.keySet()) {
                f = false;
                for (int i = 0; i < followers.length(); i++) {
                    if (p.equals(((JSONObject) followers.get(i)).get("id"))) {
                        f = true;
                        break;
                    }
                }
                if (!f) {
                    unfollowed_user = true;
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
                JSONObject fojson = new JSONObject((String) Dsyncserver.cclient.get(id));
                System.out.println(fojson.toString());
                TreeSet<String> docsset = new TreeSet<>();
                JSONArray docsarray = (JSONArray) (fojson.get("docs"));
                for (int i = 0; i < docsarray.length(); i++) {
                    docsset.add(docsarray.getString(i));
                }

                TreeSet<String> editdocsset = new TreeSet<>();
                JSONArray editdocsarray = (JSONArray) (fojson.get("edit_docs"));
                for (int i = 0; i < editdocsarray.length(); i++) {
                    editdocsset.add(editdocsarray.getString(i));
                }
                readset.remove(id);
                editset.remove(id);
                docsset.remove(doc.toString());
                editdocsset.remove(doc.toString());
                switch (permissions.get(id)) {
                    case Constants.PERMISSION_NONE:
                        readset.remove(id);
                        editset.remove(id);
                        editdocsset.remove(doc.toString());
                        docsset.remove(doc.toString());
                        break;
                    case Constants.PERMISSION_READ:
                        readset.add(id);
                        editset.remove(id);
                        editdocsset.remove(doc.toString());
                        docsset.add(doc.toString());
                        break;
                    case Constants.PERMISSION_EDIT:
                        readset.add(id);
                        editset.add(id);
                        editdocsset.add(doc.toString());
                        docsset.add(doc.toString());
                        break;
                }
                docsarray = new JSONArray();
                for (String s : docsset) {
                    docsarray.put(s);
                }
                editdocsarray = new JSONArray();
                for (String s : editdocsset) {
                    editdocsarray.put(s);
                }
                fojson.put("docs", docsarray);
                fojson.put("edit_docs", editdocsarray);

                Dsyncserver.cclient.replace(id, fojson.toString());
//                permission_granted.writeByte(Constants.VERSION);
//                permission_granted.writeByte(Constants.GRANTED_PERMISSION);
//                permission_granted.writeInt(permissions.get(id));
//                permission_granted.writeLong(doc.getLeastSignificantBits());
//                permission_granted.writeLong(doc.getMostSignificantBits());
//                try {
//                    ChannelFuture cf=((Channel) Dsyncserver.usersessions.get(id)).write(permission_granted);
//                    ((Channel) Dsyncserver.usersessions.get(id)).flush();
//                    permission_granted.clear();
//                    System.err.println(id+" granted permission ");
//                    
//                } catch (Exception e) {
//                    System.err.println(e);
//                    //CouchQ.enqueue(id,permission_granted);
//                }
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

*/
