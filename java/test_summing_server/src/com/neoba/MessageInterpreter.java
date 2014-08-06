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
    UUID doc,sessid;
    
    MessageInterpreter(ChannelHandlerContext ctx, ByteBuf buff) {
        System.out.println("request-recieved");
        VERSION = buff.getByte(0);
        type = buff.getByte(1);
        size = buff.getInt(2);
        if(type!=Constants.PINGPONG && type!=Constants.USER_CREATE && type!=Constants.USER_LOGIN ){
            sessid=new UUID(buff.getLong(14), buff.getLong(6));
        }else
            sessid=null;
        this.buff = buff;
        this.ctx = ctx;
    }

    ByteBuf generateReply() throws JSONException, IOException, VcdiffDecodeException, VcdiffEncodeException {
        switch (type) {
            case Constants.PINGPONG:
                return new PingPongMessage().result();  
            case Constants.USER_CREATE:
                string = buff.toString(6, size, Charset.forName("UTF-8"));
                byte[] passhash = new byte[20];
                buff.getBytes(6 + size, passhash, 0, 20);
                return new UserCreateMessage(string, passhash).result();
            case Constants.USER_LOGIN:
                string = buff.toString(6, size, Charset.forName("UTF-8"));
                passhash = new byte[20];
                buff.getBytes(6 + size, passhash, 0, 20);
                int reglength=buff.getInt(6+size+20);
                String regid=buff.toString(6+size+20+4, reglength, Charset.forName("UTF-8"));
                System.out.println("REG : "+regid);
                UserLoginMessage sess = new UserLoginMessage(string, passhash,regid);
                if (sess.getid() != null) {
                    System.out.println("New user session: " + Dsyncserver.usersessions);
                }
                return sess.result();

        }

        if (isloggedin()) {
            switch (type) {
                
                case Constants.DOCUMENT_CREATE:
                    string = buff.toString(6+16, size, Charset.forName("UTF-8"));
                    return new DocumentCreateMessage(string, sessid).result();
                case Constants.DOCUMENT_EDIT:
                    doc = new UUID(buff.getLong(30), buff.getLong(22));
                    byte[] diff = new byte[size];
                    buff.getBytes(22+16, diff, 0, size);
                    int version=buff.getInt(22+16+size);
                    return new DocumentEditMessage(doc, diff, version,sessid).result();
                case Constants.GET_DIGEST:
                    return new GetDigestMessage(sessid).result();
                case Constants.USER_FOLLOW:
                    string = buff.toString(6+16, size, Charset.forName("UTF-8"));
                    return new UserFollowMessage(string, sessid).result();
                case Constants.LOGOUT:
                    return new UserLogoutMessage(sessid).result();
                case Constants.GRANT_PERMISSION:
                    HashMap<String, Byte> permissions = new HashMap();
                    int base = 22+16;
                    doc = new UUID(buff.getLong(30), buff.getLong(22));
                    for (int i = 0; i < size; i++) {
                        permissions.put(((Long)buff.getLong(base + 1)).toString(), buff.getByte(base));
                        base += 9;
                    }
                    System.out.println(permissions);
                    return new GrantPermissionMessage(doc, permissions, sessid).result();
                
            }
        } else {
            System.err.println("somebody forgot to log in");
            return new NotLoggedInMessage().result();
        }
        
        return null;
    }

    private boolean isloggedin() {
        return Dsyncserver.usersessions.containsKey(sessid);
    }
}
