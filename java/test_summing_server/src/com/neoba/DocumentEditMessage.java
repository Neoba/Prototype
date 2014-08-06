package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.io.IOException;
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

    Boolean haspermission = false,documents_are_synced=false;

    DocumentEditMessage(UUID doc, byte[] diff,int version, UUID sessid) throws JSONException, IOException, VcdiffDecodeException, VcdiffEncodeException {
        JSONObject json = new JSONObject((String) Dsyncserver.cclient.get(doc.toString()));
        JSONArray diffarray = null;
        JSONArray editors = json.getJSONArray("permission_edit");
        String user = (String) Dsyncserver.usersessions.get(sessid);
        if(((String)json.get("creator")).equals(user)){
            haspermission=true;
        }
        
        if(!haspermission) for (int i = 0; i < editors.length(); i++) {
            if (user.equals(editors.get(i))) {
                haspermission = true;
                break;
            }
        }
        //age is just a number
        int age = (int) json.get("version");
        if(age+1==version)
            documents_are_synced=true;
        
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
            System.out.println("version: "+age);
            json.put("version", age);
            Dsyncserver.cclient.replace(doc.toString(), json.toString());

        }

    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(2 + 4);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.DOCUMENT_EDIT);
        if(haspermission && documents_are_synced)
            reply.writeInt(Constants.W_SUCCESS);
        else if(!documents_are_synced)
            reply.writeInt(Constants.W_ERR_DOCUMENT_OUT_OF_SYNC);
        else
            reply.writeInt(Constants.W_ERR_UNPRIVILAGED_USER);
        return reply;
    }

}
