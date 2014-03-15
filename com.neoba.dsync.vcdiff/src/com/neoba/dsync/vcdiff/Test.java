package com.neoba.dsync.vcdiff;

import java.io.IOException;
import java.util.List;

public class Test {

    protected static final String dd_ = "abcdefghijklmnop";
    protected static final String tt_ = "abcdwxyzefghefghefghzzzz";

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        Vcdiff v;
        v = new Vcdiff();
        v.blockSize = 2;
        List<Object> delta = v.encode(dd_,tt_);
        System.out.println(delta);
        String s = v.decode(dd_,delta);
        System.out.println(s);
    }
}
