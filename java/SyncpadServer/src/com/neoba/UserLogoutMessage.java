/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author Atul Vinayak
 */
class UserLogoutMessage {

    private boolean session_not_found;
    Logger logger = Logger.getLogger(UserLogoutMessage.class);

    public UserLogoutMessage(UUID sessid) {
        if (Dsyncserver.usersessions.containsKey(sessid)) {
            Dsyncserver.usersessions.remove(sessid);
            Dsyncserver.useragents.remove(sessid);

        } else {
            session_not_found = true;
            logger.error(sessid + " no such session");
        }
        logger.debug(sessid + " current sessions- " + Dsyncserver.usersessions.get(sessid));
        logger.debug(sessid + " and useragents- " + Dsyncserver.useragents.get(sessid));
    }

    ByteBuf result() {
        ByteBuf reply = buffer(6);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.LOGOUT);
        if (session_not_found) {
            reply.writeInt(Constants.W_ERR_SESSION_NOT_FOUND);
        } else {
            reply.writeInt(Constants.W_SUCCESS);
        }
        return reply;
    }

}
