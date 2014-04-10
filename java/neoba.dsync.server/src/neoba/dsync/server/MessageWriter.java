/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neoba.dsync.server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author Atul Vinayak
 */
public class MessageWriter {

    /**
     * Prepares a welcome message Buffer. This initializes a 
     * newly connected client with the latest document.
     * @param buff the ByteBuffer initialized to null
     * @param age current age of the Dictionary 
     * @param delta the last value of delta
     * @param charset current charset
     * @param dict current dictionary
     */
    public void WelcomeMessage(ByteBuffer buff, byte age, byte[] delta, Charset charset, String dict) {
        if (delta != null) {
            buff = ByteBuffer.allocate(9 + dict.length() + delta.length);
            buff.put(age);
            buff.putInt(delta.length);
            buff.put(delta);

        } else {
            buff = ByteBuffer.allocate(9 + dict.length());
            buff.put(age);
            buff.putInt(0);

        }
        buff.putInt(dict.length());
        buff.put(charset.encode(dict));
        buff.flip();
    }

}
