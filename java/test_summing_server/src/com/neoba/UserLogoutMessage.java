/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.UUID;

/**
 *
 * @author Atul Vinayak
 */
class UserLogoutMessage {

    private boolean session_not_found;

    public UserLogoutMessage(UUID sessid) {
        if (Dsyncserver.usersessions.containsKey(sessid)) {
            Dsyncserver.usersessions.remove(sessid);
        } else {
            session_not_found = true;
        }
    }

    ByteBuf result() {
        ByteBuf reply = buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.USER_FOLLOW);
        if (session_not_found) {
            reply.writeInt(Constants.W_ERR_SESSION_NOT_FOUND);
        } else {
            reply.writeInt(Constants.W_SUCCESS);
        }
        return reply;
    }

}
