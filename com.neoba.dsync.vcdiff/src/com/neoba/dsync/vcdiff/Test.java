package com.neoba.dsync.vcdiff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Test {

    protected static final String dd_ = "This is a very good test string";
    protected static final String tt_ = "This is the very best test string!";

    public static void main(String args[]) throws IOException, ClassNotFoundException {
//        Vcdiff v;
//        v = new Vcdiff();
//        v.blockSize = 3;
//        List<Object> delta = v.encode(dd_, tt_);
//        System.out.println(delta);
//
//        //ByteArrayOutputStream out = new ByteArrayOutputStream();
//        //Compression.CompressList(delta, out);
//        
//        //InputStream in = new ByteArrayInputStream(out.toByteArray());
//        //List<Object> rediffed = Compression.DecompressList(in);
//        
//        //System.out.println(out.size());
//        String s = v.decode(dd_,delta);
//        System.out.println(s);
//        
        RollingHash rh=new RollingHash();
        System.out.println(rh.hash("ab"));
        System.out.println(rh.nextHash('c'));
        //System.out.println(rh.nextHash('c'));
    }
}
