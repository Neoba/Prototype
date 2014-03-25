package com.neoba.dsync.vcdiff;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.DataFormatException;

public class Test {

//    protected static final String dd_ = "abcdefghijklmnop";
//    protected static final String tt_ = "abcdwxyzefghefghefghzzzz";

    protected static final String dd_ = "Observable consequences of the hypothesis that the "
            + "observed universe is a numerical simulation\n"
            + "performed on a cubic space-time lattice or grid are explored. "
            + "The simulation scenario is first\n"
            + "motivated by extrapolating current trends in computational "
            + "resource requirements for lattice QCD\n"
            + "into the future. Using the historical development of "
            + "lattice gauge theory technology as a guide";
   protected static final String tt_ = "Observable consequences of the hypothesis that the "
            + "observed universe is a numerical simulation\n"
            + "performed on a cubic fase-time lattice or grid are explored. "
            + "The simulation scenario is first"
            + "motivated by extrapolating current trends in computational "
            + "resource requirements facade for mayhem QCD\n"
            + "into the future. Using the historical development of "
            + "lattice gauge theory mutual agreement technology as a guide";
    public static void main(String args[]) throws IOException, ClassNotFoundException, FileNotFoundException, DataFormatException {

//        Vcdiff d;
//        d = new Vcdiff ();
//        d.blockSize = 2;
//        List<Object> delt = d.encode(dd_,tt_);
//        System.out.println(delt);
//        String sp = d.decode(dd_,delt);
//        System.out.println(sp);
        
        System.out.println(dd_);
        VcdiffBinary v;
        v = new VcdiffBinary(true);
        v.blockSize = 2;
        byte[] delta = v.encode(dd_,tt_);
        System.out.println("delta size: "+delta.length+" Bytes");
        String s = v.decode(dd_,delta);
        System.out.println(s);
        
        
        VcdiffBinary q;
        q = new VcdiffBinary(false);
        q.blockSize = 2;
        byte[] deslta = q.encode(dd_,tt_);
        System.out.println("delta size: "+deslta.length+" Bytes");
        String w = q.decode(dd_,deslta);
        
    }

}
