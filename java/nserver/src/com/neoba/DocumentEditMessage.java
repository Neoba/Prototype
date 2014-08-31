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
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class DocumentEditMessage implements Message {

    Boolean haspermission = false, documents_are_synced = false;
    Boolean creatoredited=false;
    private boolean push_success = false;
    Logger logger=Logger.getLogger(DocumentEditMessage.class);
    DocumentEditMessage(UUID doc, byte[] diff, int version, UUID sessid) throws JSONException, IOException, VcdiffDecodeException, VcdiffEncodeException, Exception {
        JSONObject json = new JSONObject((String) Dsyncserver.cclient.get(doc.toString()));
        byte[] oldiff = Arrays.copyOf(diff, diff.length);
        JSONArray diffarray = null;
        JSONArray editors = json.getJSONArray("permission_edit");
        JSONArray readers = json.getJSONArray("permission_read");
        String user = (String) Dsyncserver.usersessions.get(sessid);
        JSONObject userdoc = new JSONObject((String) Dsyncserver.cclient.get(user));

        if (((String) json.get("creator")).equals(user)) {
            haspermission = true;
            creatoredited=true;
            logger.info(sessid+" :edited by the creator itself");
        }
        logger.debug(sessid+" editors of the document- "+editors.toString());
        logger.debug(sessid+" readers of the document- "+readers.toString());
        if (!haspermission) {
            for (int i = 0; i < editors.length(); i++) {
                if (user.equals(editors.get(i))) {
                    haspermission = true;
                    logger.info(sessid+" :edited as editor: "+user);
                    break;
                }
            }
        }

        if(!haspermission)
            logger.error(sessid+" has no permission to edit "+doc);
        
        int age = (Integer) json.get("version");
        if (age + 1 == version) {
            documents_are_synced = true;
            logger.info(sessid+" :sync check passed");
        }else{
            logger.error(sessid+" sync check failed.. received version: "+version+", expected version: "+age+1);
        }

        if (haspermission && documents_are_synced) {
            Utils.printhex("diff_received",diff, diff.length);
            age += 1;
            String dict = json.getString("dict");

            if (age % 5 == 0) {
                dict = new VcdiffDecoder(dict, diff).decode();
                json.put("dict", dict);
                logger.info(sessid+" :dictionary updated");
                logger.info(sessid+" :new dictionary- "+dict); 
                diff = new VcdiffEncoder(dict, dict).encode();
            }
            diffarray = new JSONArray();
            for (byte b : diff) {
                diffarray.put(b);
            }
            json.put("diff", diffarray);
            logger.info(sessid+" 's copy: " + new VcdiffDecoder(dict, diff).decode());
            logger.info(sessid+" version: " + age);
            json.put("version", age);

            //prepare gcm ids list of android users
            ArrayList<String> readlist_android = new ArrayList<String>();
            for (int i = 0; i < readers.length(); i++) {
                if (!user.equals(readers.getString(i))) {
                    //add android pushes
                    for(String rid:CouchManager.get_gcm_rids(readers.getString(i)))
                        readlist_android.add(rid);
                }
            }
            
            if(!creatoredited)
            {
                for(String rid:CouchManager.get_gcm_rids((String) json.get("creator")))
                        readlist_android.add(rid);
            }
            
            logger.debug(sessid+" GCM ids: "+readlist_android);
            JSONObject action = new JSONObject();
            action.put("type", "edit");
            action.put("id", doc.toString());
            action.put("age", (Integer) json.get("version"));
            action.put("diff", Base64.encodeToString(oldiff, true));
            //if in case gcm is not working uncomment next line and comment the line with pushsuccess
            //Dsyncserver.cclient.replace(doc.toString(), json.toString());
            if (!readlist_android.isEmpty()) {
                logger.info(sessid+" pushing to android devices..");
                if (GoogleCloudMessager.Push(sessid,readlist_android, json.getString("title"), userdoc.getString("username") + " made edits to this document", action)) {
                    push_success = true;
                    Dsyncserver.cclient.replace(doc.toString(), json.toString());
                }
            }
            else{
                push_success=true;
                Dsyncserver.cclient.replace(doc.toString(), json.toString());
            }
        }
    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(2 + 4);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.DOCUMENT_EDIT);

         if (!documents_are_synced) {
            reply.writeInt(Constants.W_ERR_DOCUMENT_OUT_OF_SYNC);
        } else if(!haspermission) {
            reply.writeInt(Constants.W_ERR_UNPRIVILAGED_USER);
        } else if (!push_success) {
            reply.writeInt(Constants.W_ERR_PUSH_FAILED);
        }  else {
            reply.writeInt(Constants.W_SUCCESS);
        }
        return reply;
    }

}
