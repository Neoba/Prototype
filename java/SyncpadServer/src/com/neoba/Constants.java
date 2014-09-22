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
    
    public static final byte VERSION=                          0x02;
    
    public static final byte PINGPONG=                         0x1;
    public static final byte DOCUMENT_CREATE=                  0x2;
    public static final byte DOCUMENT_EDIT=                    0x3;
    public static final byte GET_DIGEST=                       0x4;
    public static final byte USER_CREATE=                      0x5;
    public static final byte USER_LOGIN=                       0x6;
    public static final byte USER_FOLLOW=                      0x7;
    public static final byte GRANT_PERMISSION=                 0x8;
    public static final byte LOGOUT=                           0x9;
    public static final byte POKE=                             0xA;
    public static final byte DOCUMENT_DELETE=                  0xB;
    public static final byte USER_UNFOLLOW=                    0xC;
    public static final byte FACEBOOK_USER_CREATE=             (byte)0x0F5;
    public static final byte FACEBOOK_USER_LOGIN=              (byte)0x0F6;
    
    public static final byte CLIENT_ACK=                       (byte) 0xFF;
    
    
    public static final int W_SUCCESS=                         0x0000FFFF;
    public static final int W_PING=                            0x504F4E47;
    
    public static final int W_ERR_UNKNOWN=                     0x00008FFF;
    
    public static final int W_ERR_DUP_USERNAME=                0x00008000;
    public static final int W_ERR_PWD_INCORRECT=               0x00008001;
    public static final int W_ERR_NONEXISTENT_USER=            0x00008002;
    public static final int W_ERR_NOT_LOGGED_IN=               0x00008003;
    public static final int W_ERR_DUP_FOLLOWER=                0x00008004;
    public static final int W_ERR_SHARE_WITH_UNFOLLOWED_USER=  0x00008005;
    public static final int W_ERR_UNPRIVILAGED_USER=           0x00008006;
    public static final int W_ERR_ALREADY_FOLLOWING=           0x00008007;
    public static final int W_ERR_DOCUMENT_OUT_OF_SYNC=        0x00008008;
    public static final int W_ERR_SELF_FOLLOW=                 0x00008009;
    public static final int W_ERR_SESSION_NOT_FOUND=           0x0000800A;
    public static final int W_ERR_PUSH_FAILED=                 0x0000800B;
    public static final int W_ERR_DOCUMENT_NONEXISTANT=        0x0000800C;
    public static final int W_ERR_NOT_ALREADY_FOLLOWING=       0x0000800D;
    public static final int W_ERR_SELF_UNFOLLOW=               0x0000800E;
    public static final int W_ERR_PROTOCOL_EXPIRED=            0x0000800F;
    
    public static final byte CREDENTIAL_REQ=                   0x70;
    public static final byte GRANTED_PERMISSION=               0x71;
    
    public static final byte PERMISSION_NONE=                  0x00;
    public static final byte PERMISSION_READ=                  0x01;
    public static final byte PERMISSION_EDIT=                  0x02;
    
    public static final byte USER_AGENT_ANDROID=               10;
    public static final byte USER_AGENT_IOS=                   11;
    public static final byte USER_AGENT_CONSOLE=               12;
    
    public static final int HTTP_MAX_BODY_SIZE=                1048576;
    public static final int NEOBA_SECRET=                      0xCE71A9;
    
    public static final String GCM_API_KEY=                    "key=AIzaSyC7EVwGwadMtVj_XZMBbhsp1YT7b4_Bcf4";
    public static final String GCM_API_ENDPOINT=               "https://android.googleapis.com/gcm/send";
    public static final int GCM_MAX_RETRY=                     1;

}
