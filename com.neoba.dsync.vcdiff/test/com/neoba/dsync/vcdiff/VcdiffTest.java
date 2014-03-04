/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba.dsync.vcdiff;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Atul Vinayak
 */
public class VcdiffTest extends TestCase {
    
    public VcdiffTest(String testName) {
        super(testName);
    }

    /**
     * Test of encode method, of class Vcdiff.
     */
    public void testEncode() {
        System.out.println("encode");
        Vcdiff instance = new Vcdiff();
        instance.blockSize=2;
        List<Object> result = instance.encode("abcdefghijklmnop","abcdwxyzefghefghefghzzzz");
        //The output should be [0, 4, wxyz, 4, 4, 4, 4, 4, 4, zzzz]
        //This is the example given in RFC 3284
        assertEquals((Integer)result.get(0),(Integer)0);
        assertEquals((Integer)result.get(1),(Integer)4);
        assertEquals((String)result.get(2),"wxyz");
        assertEquals((Integer)result.get(3),(Integer)4);
        assertEquals((Integer)result.get(4),(Integer)4);
        assertEquals((Integer)result.get(5),(Integer)4);
        assertEquals((Integer)result.get(6),(Integer)4);
        assertEquals((Integer)result.get(7),(Integer)4);
        assertEquals((Integer)result.get(8),(Integer)4);
        assertEquals((String)result.get(9),"zzzz");
        
    }

    /**
     * Test of decode method, of class Vcdiff.
     */
    public void testDecode() {
        System.out.println("decode");
        String dict = "";
        List<Object> diff = new ArrayList<>();
        Vcdiff instance = new Vcdiff();
        instance.blockSize=2;
        diff.add(0);
        diff.add(4);
        diff.add("wxyz");
        diff.add(4);
        diff.add(4);
        diff.add(4);
        diff.add(4);
        diff.add(4);
        diff.add(4);
        diff.add("zzzz");

        String result = instance.decode("abcdefghijklmnop", diff);
        assertEquals("abcdwxyzefghefghefghzzzz", result);
    }
    
}
