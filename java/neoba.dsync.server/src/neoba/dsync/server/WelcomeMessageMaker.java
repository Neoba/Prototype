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
public class WelcomeMessageMaker {

    private final Charset charset = Charset.forName("UTF-8");
    public byte age;
    public byte[] delta;
    public String dict;
    public ByteBuffer welcome;

    public WelcomeMessageMaker(byte age, byte[] delta, String dict) {

        this.age = age;
        this.delta = delta;
        this.dict = dict;

        if (delta != null) {
            welcome = ByteBuffer.allocate(9 + dict.length() + delta.length);
            welcome.put(age);
            welcome.putInt(delta.length);
            welcome.put(delta);

        } else {
            welcome = ByteBuffer.allocate(9 + dict.length());
            welcome.put(age);
            welcome.putInt(0);

        }
        welcome.putInt(dict.length());
        welcome.put(charset.encode(dict));
        welcome.flip();

    }
    public ByteBuffer getBuffer()
    {
        return welcome;
    }
}
