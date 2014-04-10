/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neoba.dsync.client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author Atul Vinayak
 */
public class MessageReader {

    public void WelcomeMessage(ByteBuffer buff, byte age, byte[] delta, Charset charset, String dict) {
        age = buff.get();
        int deltasize = buff.getInt();
        if (deltasize > 0) {
            delta = new byte[deltasize];
            buff.get(delta, 0, deltasize);
        }
        int dictsize = buff.getInt();
        byte[] dictarr = new byte[dictsize];
        buff.get(dictarr);
        dict = charset.decode(ByteBuffer.wrap(dictarr)).toString();
    }
}
