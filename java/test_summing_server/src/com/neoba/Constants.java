/*1
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

/**
 *
 * @author atul
 */
public class Constants {
    
    static final byte VERSION=0x1;
    
    static final byte PINGPONG=0x1;
    static final byte DOCUMENT_CREATE=0x2;
    static final byte DOCUMENT_EDIT=0x3;
    static final byte GET_DIGEST=0x4;
    static final byte USER_CREATE=0x5;
    static final byte USER_LOGIN=0x6;
    static final byte USER_FOLLOW=0x7;
    static final byte GRANT_PERMISSION=0x8;
    static final byte LOGOUT=0x9;
    static final byte CLIENT_ACK=(byte) 0xFF;
    
    static final int W_SUCCESS=0xFFFF;
    static final int W_PING=0x504F4E47;
    
    static final int W_ERR_UNKNOWN=0x8FFF;
    static final int W_ERR_DUP_USERNAME=0x8000;
    static final int W_ERR_PWD_INCORRECT=0x8001;
    static final int W_ERR_NONEXISTENT_USER=0x8002;
    static final int W_ERR_NOT_LOGGED_IN=0x8003;
    static final int W_ERR_DUP_FOLLOWER=0x8004;
    static final int W_ERR_SHARE_WITH_UNFOLLOWED_USER=0x8005;
    static final int W_ERR_UNPRIVILAGED_USER=0x8006;
    static final int W_ERR_ALREADY_FOLLOWING=0x8007;
    static final int W_ERR_DOCUMENT_OUT_OF_SYNC=0x8008;
    static final int W_ERR_SELF_FOLLOW=0x8009;
    static final int W_ERR_SESSION_NOT_FOUND=0x800A;
    
    static final byte CREDENTIAL_REQ=0x70;
    static final byte GRANTED_PERMISSION=0x71;
    
    static final byte PERMISSION_NONE=0x00;
    static final byte PERMISSION_READ=0x01;
    static final byte PERMISSION_EDIT=0x02;
    
    static final int HTTP_MAX_BODY_SIZE=1048576;
    
    static final int NEOBA_SECRET=0xCE71A9;
    
}
