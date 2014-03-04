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
        Vcdiff v;
        v = new Vcdiff();
        v.blockSize = 2;
        List<Object> delta = v.encode("abcdefghijklmnop","abcdwxyzefghefghefghzzzz");
        System.out.println(delta);

        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        //Compression.CompressList(delta, out);
        
        //InputStream in = new ByteArrayInputStream(out.toByteArray());
        //List<Object> rediffed = Compression.DecompressList(in);
        
        //System.out.println(out.size());
        String s = v.decode(dd_,delta);
        System.out.println(s);
        
        String testcase="abcdcdcd";
        BlockText dictText = new BlockText(testcase,2);
        RollingHash hasher = new RollingHash();
        Dictionary instance = new Dictionary();
        instance.populateDictionary(dictText, hasher);
        
        System.out.println(instance.getMatch(25543, 2, "cd").text);
        
    }
}
