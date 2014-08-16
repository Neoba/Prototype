package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class DocumentEditMessage implements Message {

    Boolean haspermission = false, documents_are_synced = false;
    private boolean push_success=false;

    DocumentEditMessage(UUID doc, byte[] diff, int version, UUID sessid) throws JSONException, IOException, VcdiffDecodeException, VcdiffEncodeException, Exception {
        JSONObject json = new JSONObject((String) Dsyncserver.cclient.get(doc.toString()));
        byte[] oldiff=Arrays.copyOf(diff, diff.length);
        JSONArray diffarray = null;
        JSONArray editors = json.getJSONArray("permission_edit");
        JSONArray readers = json.getJSONArray("permission_read");
        String user = (String) Dsyncserver.usersessions.get(sessid);
        JSONObject userdoc = new JSONObject((String) Dsyncserver.cclient.get(user));

        if (((String) json.get("creator")).equals(user)) {
            haspermission = true;
        }

        if (!haspermission) {
            for (int i = 0; i < editors.length(); i++) {
                if (user.equals(editors.get(i))) {
                    haspermission = true;
                    break;
                }
            }
        }

        int age = (int) json.get("version");
        if (age + 1 == version) {
            documents_are_synced = true;
        }

        if (haspermission && documents_are_synced) {
            System.out.println("diff: ");
            Utils.printhex(diff, diff.length);
            age += 1;
            String dict = json.getString("dict");
            
            if (age % 5 == 0) {
                dict = new VcdiffDecoder(dict, diff).decode();
                json.put("dict", dict);
                System.out.println("dictionary changed " + dict + "\nnew diff:");
                diff = new VcdiffEncoder(dict, dict).encode();
                Utils.printhex(diff, diff.length);
            }
            diffarray = new JSONArray();
            for (byte b : diff) {
                diffarray.put(b);
            }
            json.put("diff", diffarray);
            System.out.println("Did you mean: " + new VcdiffDecoder(dict, diff).decode());
            System.out.println("version: " + age);
            json.put("version", age);
            Dsyncserver.cclient.replace(doc.toString(), json.toString());
            ArrayList<String> readlist = new ArrayList<>();
            readers.put((String) json.get("creator"));
            editors.put((String) json.get("creator"));
            for (int i = 0; i < readers.length(); i++) {
                if (!user.equals(readers.getString(i))) {
                    readlist.add(CouchManager.getGcmRegId(readers.getString(i)));
                }
            }
            JSONObject action = new JSONObject();
            action.put("type", "edit");
            action.put("id", doc.toString());
            action.put("age", (int) json.get("version"));
            action.put("diff", Base64.encodeToString(oldiff, true));
            if(GoogleCloudMessager.Push(readlist, json.getString("title"), userdoc.getString("username") + " made edits to this document", action))
            {       push_success=true;
                    
            }

        }

    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(2 + 4);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.DOCUMENT_EDIT);
        if (haspermission && documents_are_synced) {
            reply.writeInt(Constants.W_SUCCESS);
        } else if (!documents_are_synced) {
            reply.writeInt(Constants.W_ERR_DOCUMENT_OUT_OF_SYNC);
        } else if (!push_success) {
            reply.writeInt(Constants.W_ERR_PUSH_FAILED);
        } else {
            reply.writeInt(Constants.W_ERR_UNPRIVILAGED_USER);
        }
        return reply;
    }

}
