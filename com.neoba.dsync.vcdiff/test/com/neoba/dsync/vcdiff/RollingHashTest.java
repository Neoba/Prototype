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
public class RollingHashTest extends TestCase {
    
    public RollingHashTest(String testName) {
        super(testName);
    }

    /**
     * Test of moduloExponet method, of class RollingHash.
     */
    public void testModuloExponet() {
        System.out.println("moduloExponet");
        int base = 0;
        int power = 0;
        int modulo = 0;
        RollingHash instance = new RollingHash();
        int expResult = 0;
        int result = instance.moduloExponet(base, power, modulo);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hash method, of class RollingHash.
     */
    public void testHash() {
        System.out.println("hash");
        String toHash = "";
        RollingHash instance = new RollingHash();
        int expResult = 0;
        int result = instance.hash(toHash);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextHash method, of class RollingHash.
     */
    public void testNextHash() {
        System.out.println("nextHash");
        char toAdd = ' ';
        RollingHash instance = new RollingHash();
        int expResult = 0;
        int result = instance.nextHash(toAdd);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
