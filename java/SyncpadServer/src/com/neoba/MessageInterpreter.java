/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

/**
 *
 * @author atul
 */
class MessageInterpreter {

    byte VERSION;
    byte type;
    int size;
    ByteBuf buff;
    ChannelHandlerContext ctx;
    String string;
    UUID doc, sessid;
    Logger logger = Logger.getLogger(MessageInterpreter.class);
    MessageInterpreter(ChannelHandlerContext ctx, ByteBuf buff) {
        
        VERSION = buff.getByte(0);
        type = buff.getByte(1);
        size = buff.getInt(2);
        if (       type != Constants.PINGPONG 
                && type != Constants.USER_CREATE 
                && type != Constants.USER_LOGIN
                && type != Constants.FACEBOOK_USER_CREATE
                && type != Constants.FACEBOOK_USER_LOGIN) {
            sessid = new UUID(buff.getLong(14), buff.getLong(6));
            if(isloggedin()) logger.info("Authenticated message from user "+Dsyncserver.usersessions.get(sessid)+" with session "+sessid);
        } else {
            sessid = null;
        }
        this.buff = buff;
        this.ctx = ctx;
    }

    ByteBuf generateReply() throws JSONException, IOException, VcdiffDecodeException, VcdiffEncodeException, Exception {
        if(VERSION!=Constants.VERSION){
                logger.info("Version of client too old: "+Integer.toHexString(VERSION));
                return new ProtocolExpiredMessage(type).result();
        }
        switch (type) {
            case Constants.PINGPONG:
                logger.info("Ping received");
                return new PingPongMessage().result();
            case Constants.USER_CREATE:
                string = buff.toString(6, size, Charset.forName("UTF-8"));
                byte[] passhash = new byte[20];
                buff.getBytes(6 + size, passhash, 0, 20);
                logger.info("creating user "+string);
                return new UserCreateMessage(string, passhash).result();
            case Constants.FACEBOOK_USER_CREATE:
                string = buff.toString(6, size, Charset.forName("UTF-8"));
                logger.info("facebook access_token: "+string);
                return new FacebookUserCreateMessage(string).result();
            case Constants.USER_LOGIN:
                string = buff.toString(6, size, Charset.forName("UTF-8"));
                String regid = null;
                passhash = new byte[20];
                buff.getBytes(6 + size, passhash, 0, 20);
                byte uagent=buff.getByte(6+size+20);
                
                switch(uagent){
                    case Constants.USER_AGENT_ANDROID:
                        logger.info("user agent detected: Android OS >= 4.0.3");
                        int reglength = buff.getInt(6 + size + 20+1);
                         regid = buff.toString(6 + size + 20 + 1+4, reglength, Charset.forName("UTF-8"));
                        break;
                    case Constants.USER_AGENT_CONSOLE:
                        logger.info("user agent detected: Console");
                        regid="";
                        break;
                }
                
                
                logger.debug("User Agent: "+uagent);
                
                logger.info("logging in new user");
                UserLoginMessage sess = new UserLoginMessage(string, passhash, regid,uagent);
                if (sess.getid() != null) {
                    logger.info("new user session: " + sess.getid() + " "+string);
                    Dsyncserver.useragents.put(sess.sessid, uagent);
                    logger.info("user_agents: " + Dsyncserver.useragents);
                }
                return sess.result();

        }

        if (isloggedin()) {
            switch (type) {

                case Constants.DOCUMENT_CREATE:
                    doc = new UUID(buff.getLong(30), buff.getLong(22));
                    logger.info(sessid+" :creating new document- ");
                    return new DocumentCreateMessage(doc, sessid).result();
                case Constants.DOCUMENT_DELETE:
                    doc = new UUID(buff.getLong(30), buff.getLong(22));
                    logger.info(sessid+" :deleting document- "+doc);
                     return new DocumentDeleteMessage(doc, sessid).result();
                case Constants.DOCUMENT_EDIT:
                    doc = new UUID(buff.getLong(30), buff.getLong(22));
                    byte[] diff = new byte[size];
                    buff.getBytes(22 + 16, diff, 0, size);
                    int version = buff.getInt(22 + 16 + size);
                    logger.info(sessid+" :editing document - "+doc);
                    return new DocumentEditMessage(doc, diff, version, sessid).result();
                case Constants.GET_DIGEST:
                    logger.info(sessid+" :getting digest");
                    return new GetDigestMessage(sessid).result();
                case Constants.USER_FOLLOW:
                    string = buff.toString(6 + 16, size, Charset.forName("UTF-8"));
                    logger.info(sessid+" :following user "+string);
                    return new UserFollowMessage(string, sessid).result();
                case Constants.USER_UNFOLLOW:
                    string = buff.toString(6 + 16, size, Charset.forName("UTF-8"));
                    logger.info(sessid+" :unfollowing user "+string);
                    return new UserUnFollowMessage(string, sessid).result();
                case Constants.POKE:
                    string = buff.toString(6 + 16, size, Charset.forName("UTF-8"));
                    logger.info(sessid+" :poking user "+string);
                    return new PokeMessage(string, sessid).result();
                case Constants.LOGOUT:
                    logger.info(sessid+" :logged out ");
                    return new UserLogoutMessage(sessid).result();
                case Constants.GRANT_PERMISSION:
                    HashMap<String, Byte> permissions = new HashMap();
                    int base = 22 + 16;
                    doc = new UUID(buff.getLong(30), buff.getLong(22));
                    for (int i = 0; i < size; i++) {
                        permissions.put(((Long) buff.getLong(base + 1)).toString(), buff.getByte(base));
                        base += 9;
                    }
                    logger.info(sessid+" granting permissions: to doc "+doc+": "+permissions);
                    return new GrantPermissionMessage(doc, permissions, sessid).result();

            }
        } else {
            logger.info("session id received is non-existant: "+sessid);
            return new NotLoggedInMessage().result();
        }
        
        return null;
    }

    private boolean isloggedin() {
        return Dsyncserver.usersessions.containsKey(sessid);
    }
}
