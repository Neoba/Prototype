package com.neoba.dsync.vcdiff;

import java.io.IOException;
import java.util.List;

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
            + "performed on a cubic spacetime lattice or grid are explored. "
            + "The simulation scenario is first\n"
            + "motivated by extrapolating alternate trends in computational "
            + "resource requirements for lattice QCD\n"
            + "into the future. Using the historical development of "
            + "lattice gauge theory technology as a guide";
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        Vcdiff d;
        d = new Vcdiff ();
        d.blockSize = 2;
        List<Object> delt = d.encode(dd_,tt_);
        System.out.println(delt);
        String sp = d.decode(dd_,delt);
        System.out.println(sp);
        
        
        VcdiffBinary v;
        v = new VcdiffBinary();
        v.blockSize = 2;
        List<Object> delta = v.encode(dd_,tt_);
        System.out.println(delta);
        String s = v.decode(dd_,delta);
        System.out.println(s);
    }
}
