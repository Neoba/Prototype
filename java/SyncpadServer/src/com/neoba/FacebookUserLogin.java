/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author root
 */
public class FacebookUserLogin {

    UUID sessid;
    FacebookUser fbuser;
    String sid = null;
    int response;
    Logger logger = Logger.getLogger(FacebookUserLogin.class);

    FacebookUserLogin(String string, String regid, byte uagent) throws IOException, MalformedURLException, JSONException {
        fbuser = new FacebookUser(string);
        String id = fbuser.getId();
        sid = fbuser.getSyncpadId();
        if (sid != null) {

            JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(sid));
            response = Constants.W_SUCCESS;
            sessid = UUID.randomUUID();
            switch (uagent) {
                case Constants.USER_AGENT_ANDROID:
                    CouchManager.set_gcm_rid(sid, regid);
                    break;
                case Constants.USER_AGENT_CONSOLE:
                    break;
            }

            Dsyncserver.usersessions.put(sessid, sid);
            logger.info(user.getString("username") + " :user found");
            logger.info(user.getString("username") + " :session- " + sessid);

        } else {
            response = Constants.W_ERR_NONEXISTENT_USER;
            logger.info(fbuser.getName() + " :user dosn't exist ");
            id = null;
        }
    }

    Long getid() {
        if(sid!=null)
            return Long.parseLong(sid);
        return null;
    }

    ByteBuf result() {
        ByteBuf reply;
        if (response == Constants.W_SUCCESS) {
            reply = buffer(6 + 16);
        } else {
            reply = buffer(6);
        }

        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_LOGIN);
        reply.writeInt(response);
        if (response == Constants.W_SUCCESS) {
            reply.writeLong(sessid.getLeastSignificantBits());
            reply.writeLong(sessid.getMostSignificantBits());
        }
        return reply;
    }
}
