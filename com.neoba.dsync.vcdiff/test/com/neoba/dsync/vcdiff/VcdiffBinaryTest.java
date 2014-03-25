/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba.dsync.vcdiff;

import junit.framework.TestCase;

/**
 *
 * @author Atul Vinayak
 */
public class VcdiffBinaryTest extends TestCase {
    
    public VcdiffBinaryTest(String testName) {
        super(testName);
    }

    /**
     * Test of encode method, of class VcdiffBinary.
     */
    public void testEncode() throws Exception {
        System.out.println("encode");
        String dict = "";
        String target = "";
        VcdiffBinary instance = null;
        byte[] expResult = null;
        byte[] result = instance.encode(dict, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decode method, of class VcdiffBinary.
     */
    public void testDecode() throws Exception {
        System.out.println("decode");
        String dict = "";
        byte[] diffBuffer = null;
        VcdiffBinary instance = null;
        String expResult = "";
        String result = instance.decode(dict, diffBuffer);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
